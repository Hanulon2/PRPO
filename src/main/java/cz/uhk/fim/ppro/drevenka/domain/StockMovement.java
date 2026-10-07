package cz.uhk.fim.ppro.drevenka.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_warehouse_id")
    private Warehouse sourceWarehouse;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_warehouse_id")
    private Warehouse targetWarehouse;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MovementType movementType;

    private Long orderId;

    @Column(nullable = false, length = 100)
    private String performedBy = "Petr Doležal";

    @Column(length = 500)
    private String reasonNote;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public StockMovement() {}

    public StockMovement(Product product, Warehouse sourceWarehouse, Warehouse targetWarehouse, int quantity, MovementType movementType, Long orderId, String performedBy, String reasonNote) {
        this.product = product;
        this.sourceWarehouse = sourceWarehouse;
        this.targetWarehouse = targetWarehouse;
        this.quantity = quantity;
        this.movementType = movementType;
        this.orderId = orderId;
        this.performedBy = performedBy;
        this.reasonNote = reasonNote;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Warehouse getSourceWarehouse() { return sourceWarehouse; }
    public void setSourceWarehouse(Warehouse sourceWarehouse) { this.sourceWarehouse = sourceWarehouse; }

    public Warehouse getTargetWarehouse() { return targetWarehouse; }
    public void setTargetWarehouse(Warehouse targetWarehouse) { this.targetWarehouse = targetWarehouse; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public MovementType getMovementType() { return movementType; }
    public void setMovementType(MovementType movementType) { this.movementType = movementType; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getReasonNote() { return reasonNote; }
    public void setReasonNote(String reasonNote) { this.reasonNote = reasonNote; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
