package cz.uhk.fim.ppro.drevenka.controller;

import cz.uhk.fim.ppro.drevenka.domain.CustomerOrder;
import cz.uhk.fim.ppro.drevenka.domain.OrderStatus;
import cz.uhk.fim.ppro.drevenka.repository.CustomerRepository;
import cz.uhk.fim.ppro.drevenka.repository.ProductRepository;
import cz.uhk.fim.ppro.drevenka.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public OrderController(OrderService orderService,
                           ProductRepository productRepository,
                           CustomerRepository customerRepository) {
        this.orderService = orderService;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("products", productRepository.findAllByIsActiveTrueOrderByNameAsc());
        model.addAttribute("customers", customerRepository.findAll());
        return "orders/list";
    }

    @PostMapping("/simulate")
    public String simulateOrder(@RequestParam("customerId") Long customerId,
                                @RequestParam("productId") Long productId,
                                @RequestParam("quantity") int quantity,
                                @RequestParam(value = "note", required = false) String note,
                                RedirectAttributes redirectAttributes) {
        try {
            CustomerOrder order = orderService.createSimulatedOrder(customerId, productId, quantity, note);
            if (order.getStatus() == OrderStatus.REJECTED) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "❌ " + order.getNote());
            } else if (order.getStatus() == OrderStatus.WAITING_FOR_TRANSFER) {
                redirectAttributes.addFlashAttribute("warningMessage",
                        "⚠️ Objednávka " + order.getOrderNumber() + " byla přijata, ale vyžaduje svoz z garáže: " + order.getNote());
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "✅ Objednávka " + order.getOrderNumber() + " úspěšně zarezervována v Hradci Králové a čeká na balicím stole!");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chyba při vytvoření objednávky: " + ex.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/{id}/ship")
    public String shipOrder(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            CustomerOrder order = orderService.shipOrder(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "📦 Objednávka " + order.getOrderNumber() + " byla úspěšně zabalena, odepsána ze skladu a expedována!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chyba při expedici: " + ex.getMessage());
        }
        return "redirect:/orders";
    }
}
