package cz.uhk.fim.ppro.drevenka.controller;

import cz.uhk.fim.ppro.drevenka.domain.Product;
import cz.uhk.fim.ppro.drevenka.domain.StockItem;
import cz.uhk.fim.ppro.drevenka.domain.StockMovement;
import cz.uhk.fim.ppro.drevenka.repository.CategoryRepository;
import cz.uhk.fim.ppro.drevenka.repository.ProductRepository;
import cz.uhk.fim.ppro.drevenka.service.StockService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockService stockService;

    public ProductController(ProductRepository productRepository,
                             CategoryRepository categoryRepository,
                             StockService stockService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.stockService = stockService;
    }

    @GetMapping
    public String listProducts(@RequestParam(value = "search", required = false) String search,
                               @RequestParam(value = "category", required = false) Long categoryId,
                               Model model) {
        List<Product> products;
        if (search != null && !search.trim().isEmpty()) {
            products = productRepository.searchActiveProducts(search.trim());
        } else if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId);
        } else {
            products = productRepository.findAllByIsActiveTrueOrderByNameAsc();
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedCategoryId", categoryId);
        return "products/list";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produkt nenalezen"));

        List<StockItem> stockItems = stockService.getStockItemsByProduct(product);
        List<StockMovement> movements = stockService.getMovementsByProduct(product);

        model.addAttribute("product", product);
        model.addAttribute("stockItems", stockItems);
        model.addAttribute("movements", movements);
        return "products/detail";
    }
}
