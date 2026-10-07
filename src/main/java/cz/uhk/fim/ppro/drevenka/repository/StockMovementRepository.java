package cz.uhk.fim.ppro.drevenka.repository;

import cz.uhk.fim.ppro.drevenka.domain.Product;
import cz.uhk.fim.ppro.drevenka.domain.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findTop50ByOrderByCreatedAtDesc();

    List<StockMovement> findByProductOrderByCreatedAtDesc(Product product);
}
