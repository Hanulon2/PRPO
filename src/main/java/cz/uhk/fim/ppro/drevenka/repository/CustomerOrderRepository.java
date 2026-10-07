package cz.uhk.fim.ppro.drevenka.repository;

import cz.uhk.fim.ppro.drevenka.domain.CustomerOrder;
import cz.uhk.fim.ppro.drevenka.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findAllByOrderByOrderDateDesc();

    List<CustomerOrder> findByStatus(OrderStatus status);

    long countByStatus(OrderStatus status);
}
