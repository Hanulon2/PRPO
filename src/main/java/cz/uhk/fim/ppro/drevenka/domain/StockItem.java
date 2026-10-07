package cz.uhk.fim.ppro.drevenka.domain;

import jakarta.persistence.*;

@Entity
@Table(
    name = "stock_items",
    uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "warehouse_id"})
)
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Column(nullable = false)
    private int physicalQuantity = 0;

    @Column(nullable = false)
    private int reservedQuantity = 0;

    public StockItem() {}

    public StockItem(Product product, Warehouse warehouse, int physicalQuantity, int reservedQuantity) {
        this.product = product;
        this.warehouse = warehouse;
        this.physicalQuantity = physicalQuantity;
        this.reservedQuantity = reservedQuantity;
    }

    public int getAvailableQuantity() {
        return Math.max(0, physicalQuantity - reservedQuantity);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }

    public int getPhysicalQuantity() { return physicalQuantity; }
    public void setPhysicalQuantity(int physicalQuantity) { this.physicalQuantity = physicalQuantity; }

    public int getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(int reservedQuantity) { this.reservedQuantity = reservedQuantity; }
}
