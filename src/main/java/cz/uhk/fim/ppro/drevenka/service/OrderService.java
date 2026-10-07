package cz.uhk.fim.ppro.drevenka.service;

import cz.uhk.fim.ppro.drevenka.domain.*;
import cz.uhk.fim.ppro.drevenka.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockItemRepository stockItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockService stockService;

    public OrderService(CustomerOrderRepository orderRepository,
                        ProductRepository productRepository,
                        CustomerRepository customerRepository,
                        WarehouseRepository warehouseRepository,
                        StockItemRepository stockItemRepository,
                        StockMovementRepository stockMovementRepository,
                        StockService stockService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockItemRepository = stockItemRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.stockService = stockService;
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countOrdersToPack() {
        return orderRepository.countByStatus(OrderStatus.CONFIRMED);
    }

    @Transactional(readOnly = true)
    public long countOrdersWaitingForTransfer() {
        return orderRepository.countByStatus(OrderStatus.WAITING_FOR_TRANSFER);
    }

    /**
     * Vytvoření a okamžité vyhodnocení objednávky:
     * - Dostatek v Hradci -> CONFIRMED (rezervace v Hradci)
     * - Zboží v Třebechovicích -> WAITING_FOR_TRANSFER (rezervace v garáži)
     * - Nedostatek zásob celkem -> REJECTED (zamítnuto, zamezení prodeje neexistujícího zboží)
     */
    public CustomerOrder createSimulatedOrder(Long customerId, Long productId, int quantity, String note) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produkt nenalezen"));
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Zákazník nenalezen"));

        Warehouse hradec = warehouseRepository.findByCode("HRADEC")
                .orElseThrow(() -> new IllegalStateException("Sklad Hradec nenalezen"));
        Warehouse trebechovice = warehouseRepository.findByCode("TREBECHOVICE")
                .orElseThrow(() -> new IllegalStateException("Sklad Třebechovice nenalezen"));

        StockItem hradecStock = stockService.getOrCreateStockItem(product, hradec);
        StockItem trebStock = stockService.getOrCreateStockItem(product, trebechovice);

        int availableHradec = hradecStock.getAvailableQuantity();
        int availableTreb = trebStock.getAvailableQuantity();
        int totalAvailable = availableHradec + availableTreb;

        String orderNumber = "OBJ-" + (System.currentTimeMillis() % 100000);
        CustomerOrder order = new CustomerOrder(orderNumber, customer, OrderStatus.CONFIRMED, note);
        order.setOrderDate(LocalDateTime.now());

        // Snapshot jednotkové prodejní ceny (Price Snapshot)
        OrderItem item = new OrderItem(product, quantity, product.getSellingPrice());
        order.addItem(item);

        if (totalAvailable < quantity) {
            // NEDOSTATEK ZÁSOB: Objednávka je odmítnuta!
            order.setStatus(OrderStatus.REJECTED);
            order.setNote("ODMÍTNUTO SYSTÉMEM: Požadováno " + quantity + " ks, ale celkem volno pouze " + totalAvailable + " ks (Hradec: " + availableHradec + ", Garáž: " + availableTreb + "). Zákazník nebyl zavázán!");
            return orderRepository.save(order);
        }

        if (availableHradec >= quantity) {
            // Vše je ihned k dispozici v Hradci
            hradecStock.setReservedQuantity(hradecStock.getReservedQuantity() + quantity);
            stockItemRepository.save(hradecStock);
            order.setStatus(OrderStatus.CONFIRMED);
            order.setNote("Rezervováno v expedičním skladu Hradec Králové. Připraveno k zabalení.");
        } else {
            // Část v Hradci, část je nutné svézt z Třebechovic
            int fromHradec = availableHradec;
            int fromTreb = quantity - availableHradec;

            if (fromHradec > 0) {
                hradecStock.setReservedQuantity(hradecStock.getReservedQuantity() + fromHradec);
                stockItemRepository.save(hradecStock);
            }
            trebStock.setReservedQuantity(trebStock.getReservedQuantity() + fromTreb);
            stockItemRepository.save(trebStock);

            order.setStatus(OrderStatus.WAITING_FOR_TRANSFER);
            order.setNote("Čeká na závoz " + fromTreb + " ks z garáže Třebechovice. Vygenerován interní svozový požadavek.");
        }

        return orderRepository.save(order);
    }

    /**
     * Expedice objednávky (zabalení na balicím stole v Hradci):
     * Fyzický odpis zboží z regálů a uvolnění rezervace.
     */
    public CustomerOrder shipOrder(Long orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Objednávka nenalezena: " + orderId));

        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Tato objednávka již byla dříve expedována.");
        }
        if (order.getStatus() == OrderStatus.REJECTED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Odmítnutou nebo stornovanou objednávku nelze expedovat.");
        }

        Warehouse hradec = warehouseRepository.findByCode("HRADEC")
                .orElseThrow(() -> new IllegalStateException("Sklad Hradec nenalezen"));

        for (OrderItem item : order.getItems()) {
            StockItem stockItem = stockService.getOrCreateStockItem(item.getProduct(), hradec);
            int qty = item.getQuantity();

            if (stockItem.getPhysicalQuantity() < qty) {
                throw new IllegalStateException("Na skladě v Hradci fyzicky chybí " + item.getProduct().getName() + " pro dokončení expedice!");
            }

            stockItem.setPhysicalQuantity(stockItem.getPhysicalQuantity() - qty);
            stockItem.setReservedQuantity(Math.max(0, stockItem.getReservedQuantity() - qty));
            stockItemRepository.save(stockItem);

            // Auditní záznam výdeje
            StockMovement movement = new StockMovement(
                    item.getProduct(), hradec, null, qty,
                    MovementType.DISPATCH, order.getId(), "Petr Doležal",
                    "Expedice objednávky " + order.getOrderNumber() + " pro " + order.getCustomer().getName()
            );
            stockMovementRepository.save(movement);
        }

        order.setStatus(OrderStatus.SHIPPED);
        order.setNote("Zásilka zkompletována, zabalena a odeslána zákazníkovi dne " + LocalDateTime.now().toLocalDate());
        return orderRepository.save(order);
    }
}
