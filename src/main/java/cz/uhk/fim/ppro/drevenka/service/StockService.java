package cz.uhk.fim.ppro.drevenka.service;

import cz.uhk.fim.ppro.drevenka.domain.*;
import cz.uhk.fim.ppro.drevenka.repository.ProductRepository;
import cz.uhk.fim.ppro.drevenka.repository.StockItemRepository;
import cz.uhk.fim.ppro.drevenka.repository.StockMovementRepository;
import cz.uhk.fim.ppro.drevenka.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StockService {

    private final StockItemRepository stockItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public StockService(StockItemRepository stockItemRepository,
                        StockMovementRepository stockMovementRepository,
                        ProductRepository productRepository,
                        WarehouseRepository warehouseRepository) {
        this.stockItemRepository = stockItemRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public StockItem getOrCreateStockItem(Product product, Warehouse warehouse) {
        return stockItemRepository.findByProductAndWarehouse(product, warehouse)
                .orElseGet(() -> stockItemRepository.save(new StockItem(product, warehouse, 0, 0)));
    }

    @Transactional(readOnly = true)
    public List<StockItem> getItemsBelowMinStock() {
        return stockItemRepository.findItemsBelowMinStockInExpeditionWarehouse();
    }

    @Transactional(readOnly = true)
    public List<StockItem> getStockItemsByProduct(Product product) {
        return stockItemRepository.findByProduct(product);
    }

    @Transactional(readOnly = true)
    public List<StockMovement> getRecentMovements() {
        return stockMovementRepository.findTop50ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<StockMovement> getMovementsByProduct(Product product) {
        return stockMovementRepository.findByProductOrderByCreatedAtDesc(product);
    }

    /**
     * Příjem zboží z dílny/truhlárny na sklad.
     */
    public StockMovement receiveStock(Long productId, Long warehouseId, int quantity, String note) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Množství pro příjem musí být větší než nula.");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produkt nenalezen: " + productId));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Sklad nenalezen: " + warehouseId));

        StockItem item = getOrCreateStockItem(product, warehouse);
        item.setPhysicalQuantity(item.getPhysicalQuantity() + quantity);
        stockItemRepository.save(item);

        StockMovement movement = new StockMovement(
                product, null, warehouse, quantity,
                MovementType.RECEIPT, null, "Petr Doležal", note
        );
        return stockMovementRepository.save(movement);
    }

    /**
     * Meziskladový svoz (převod) zboží z jednoho skladu na druhý (např. Třebechovice -> Hradec).
     */
    public StockMovement transferStock(Long productId, Long sourceWarehouseId, Long targetWarehouseId, int quantity, String note) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Množství pro přesun musí být větší než nula.");
        }
        if (sourceWarehouseId.equals(targetWarehouseId)) {
            throw new IllegalArgumentException("Zdrojový a cílový sklad musí být různé.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produkt nenalezen: " + productId));
        Warehouse source = warehouseRepository.findById(sourceWarehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Zdrojový sklad nenalezen: " + sourceWarehouseId));
        Warehouse target = warehouseRepository.findById(targetWarehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Cílový sklad nenalezen: " + targetWarehouseId));

        StockItem sourceItem = getOrCreateStockItem(product, source);
        if (sourceItem.getPhysicalQuantity() < quantity) {
            throw new IllegalStateException("Na zdrojovém skladě " + source.getName() + " není dostatek kusů (fyzicky je " + sourceItem.getPhysicalQuantity() + " ks, požadováno " + quantity + " ks).");
        }

        // Pokud byly na zdrojovém skladě kusy rezervovány, uvolní se a přesunou
        int reservedToTransfer = Math.min(sourceItem.getReservedQuantity(), quantity);
        sourceItem.setPhysicalQuantity(sourceItem.getPhysicalQuantity() - quantity);
        sourceItem.setReservedQuantity(sourceItem.getReservedQuantity() - reservedToTransfer);
        stockItemRepository.save(sourceItem);

        StockItem targetItem = getOrCreateStockItem(product, target);
        targetItem.setPhysicalQuantity(targetItem.getPhysicalQuantity() + quantity);
        targetItem.setReservedQuantity(targetItem.getReservedQuantity() + reservedToTransfer);
        stockItemRepository.save(targetItem);

        StockMovement movement = new StockMovement(
                product, source, target, quantity,
                MovementType.TRANSFER, null, "Petr Doležal", note
        );
        return stockMovementRepository.save(movement);
    }

    /**
     * Skladová inventura: narovnání stavu zásob se zaznamenáním rozdílu (manko / přebytek).
     */
    public StockMovement correctInventory(Long productId, Long warehouseId, int newPhysicalQuantity, String reason) {
        if (newPhysicalQuantity < 0) {
            throw new IllegalArgumentException("Skladová zásoba nemůže být záporná.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Při inventurní úpravě je povinné uvést důvod zjištěného rozdílu.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produkt nenalezen: " + productId));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Sklad nenalezen: " + warehouseId));

        StockItem item = getOrCreateStockItem(product, warehouse);
        int oldQuantity = item.getPhysicalQuantity();
        int diff = newPhysicalQuantity - oldQuantity;

        if (diff == 0) {
            return null; // Žádná změna
        }

        item.setPhysicalQuantity(newPhysicalQuantity);
        stockItemRepository.save(item);

        StockMovement movement = new StockMovement(
                product,
                diff < 0 ? warehouse : null,
                diff > 0 ? warehouse : null,
                Math.abs(diff),
                MovementType.INVENTORY_CORRECTION,
                null,
                "Petr Doležal",
                "Inventurní korekce: původně " + oldQuantity + " ks, nově " + newPhysicalQuantity + " ks. Důvod: " + reason
        );
        return stockMovementRepository.save(movement);
    }
}
