package cz.uhk.fim.ppro.drevenka.controller;

import cz.uhk.fim.ppro.drevenka.domain.StockItem;
import cz.uhk.fim.ppro.drevenka.domain.Warehouse;
import cz.uhk.fim.ppro.drevenka.repository.CustomerRepository;
import cz.uhk.fim.ppro.drevenka.repository.ProductRepository;
import cz.uhk.fim.ppro.drevenka.repository.WarehouseRepository;
import cz.uhk.fim.ppro.drevenka.service.OrderService;
import cz.uhk.fim.ppro.drevenka.service.StockService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final StockService stockService;
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final CustomerRepository customerRepository;

    public DashboardController(StockService stockService,
                               OrderService orderService,
                               ProductRepository productRepository,
                               WarehouseRepository warehouseRepository,
                               CustomerRepository customerRepository) {
        this.stockService = stockService;
        this.orderService = orderService;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.customerRepository = customerRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<StockItem> belowMin = stockService.getItemsBelowMinStock();
        model.addAttribute("belowMinItems", belowMin);
        model.addAttribute("toPackCount", orderService.countOrdersToPack());
        model.addAttribute("toTransferCount", orderService.countOrdersWaitingForTransfer());
        model.addAttribute("recentMovements", stockService.getRecentMovements());
        model.addAttribute("allProducts", productRepository.findAllByIsActiveTrueOrderByNameAsc());
        model.addAttribute("warehouses", warehouseRepository.findAll());
        model.addAttribute("customers", customerRepository.findAll());

        Warehouse hradec = warehouseRepository.findByCode("HRADEC").orElse(null);
        Warehouse trebechovice = warehouseRepository.findByCode("TREBECHOVICE").orElse(null);
        model.addAttribute("hradec", hradec);
        model.addAttribute("trebechovice", trebechovice);

        return "dashboard";
    }
}
