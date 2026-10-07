package cz.uhk.fim.ppro.drevenka;

import cz.uhk.fim.ppro.drevenka.domain.Customer;
import cz.uhk.fim.ppro.drevenka.domain.CustomerOrder;
import cz.uhk.fim.ppro.drevenka.domain.OrderStatus;
import cz.uhk.fim.ppro.drevenka.domain.Product;
import cz.uhk.fim.ppro.drevenka.repository.CustomerRepository;
import cz.uhk.fim.ppro.drevenka.repository.ProductRepository;
import cz.uhk.fim.ppro.drevenka.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DrevenkaApplicationTests {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("Aplikace úspěšně nastartuje Spring kontext")
    void contextLoads() {
        assertNotNull(orderService);
    }

    @Test
    @DisplayName("Byznys pravidlo: Objednávka nad celkový počet kusů je systémem odmítnuta (REJECTED)")
    void shouldRejectOrderWhenStockIsInsufficient() {
        Product kaca = productRepository.findByCode("KAC-001").orElseThrow();
        Customer customer = customerRepository.findAll().getFirst();

        // Požadujeme 1000 ks, což dalece převyšuje zásoby na obou skladech
        CustomerOrder order = orderService.createSimulatedOrder(customer.getId(), kaca.getId(), 1000, "Test nadlimitního nákupu");

        assertEquals(OrderStatus.REJECTED, order.getStatus());
        assertTrue(order.getNote().contains("ODMÍTNUTO"));
    }
}
