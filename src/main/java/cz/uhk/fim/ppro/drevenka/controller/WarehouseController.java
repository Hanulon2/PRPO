package cz.uhk.fim.ppro.drevenka.controller;

import cz.uhk.fim.ppro.drevenka.repository.ProductRepository;
import cz.uhk.fim.ppro.drevenka.repository.WarehouseRepository;
import cz.uhk.fim.ppro.drevenka.service.StockService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/warehouse")
public class WarehouseController {

    private final StockService stockService;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public WarehouseController(StockService stockService,
                               ProductRepository productRepository,
                               WarehouseRepository warehouseRepository) {
        this.stockService = stockService;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("movements", stockService.getRecentMovements());
        model.addAttribute("products", productRepository.findAllByIsActiveTrueOrderByNameAsc());
        model.addAttribute("warehouses", warehouseRepository.findAll());
        return "warehouse/index";
    }

    @PostMapping("/receive")
    public String receiveStock(@RequestParam("productId") Long productId,
                               @RequestParam("warehouseId") Long warehouseId,
                               @RequestParam("quantity") int quantity,
                               @RequestParam(value = "note", required = false) String note,
                               RedirectAttributes redirectAttributes) {
        try {
            stockService.receiveStock(productId, warehouseId, quantity, note);
            redirectAttributes.addFlashAttribute("successMessage", "Úspěšně naskladněno " + quantity + " ks z výroby.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chyba při příjmu: " + ex.getMessage());
        }
        return "redirect:/warehouse";
    }

    @PostMapping("/transfer")
    public String transferStock(@RequestParam("productId") Long productId,
                                @RequestParam("sourceWarehouseId") Long sourceWarehouseId,
                                @RequestParam("targetWarehouseId") Long targetWarehouseId,
                                @RequestParam("quantity") int quantity,
                                @RequestParam(value = "note", required = false) String note,
                                RedirectAttributes redirectAttributes) {
        try {
            stockService.transferStock(productId, sourceWarehouseId, targetWarehouseId, quantity, note);
            redirectAttributes.addFlashAttribute("successMessage", "Meziskladový svoz (" + quantity + " ks) byl úspěšně zaevidován a stav byl převeden.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chyba při převodu: " + ex.getMessage());
        }
        return "redirect:/warehouse";
    }

    @PostMapping("/inventory")
    public String correctInventory(@RequestParam("productId") Long productId,
                                   @RequestParam("warehouseId") Long warehouseId,
                                   @RequestParam("newQuantity") int newQuantity,
                                   @RequestParam("reason") String reason,
                                   RedirectAttributes redirectAttributes) {
        try {
            stockService.correctInventory(productId, warehouseId, newQuantity, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Inventurní stav byl úspěšně dorovnán a zaznamenán v auditním deníku.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chyba při inventuře: " + ex.getMessage());
        }
        return "redirect:/warehouse";
    }
}
