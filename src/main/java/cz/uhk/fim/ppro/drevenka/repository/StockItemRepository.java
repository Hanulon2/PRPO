package cz.uhk.fim.ppro.drevenka.repository;

import cz.uhk.fim.ppro.drevenka.domain.Product;
import cz.uhk.fim.ppro.drevenka.domain.StockItem;
import cz.uhk.fim.ppro.drevenka.domain.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    Optional<StockItem> findByProductAndWarehouse(Product product, Warehouse warehouse);

    List<StockItem> findByProduct(Product product);

    List<StockItem> findByWarehouse(Warehouse warehouse);

    @Query("SELECT s FROM StockItem s WHERE s.warehouse.isExpeditionPoint = true " +
           "AND (s.physicalQuantity - s.reservedQuantity) < s.product.minStockLimit")
    List<StockItem> findItemsBelowMinStockInExpeditionWarehouse();
}
