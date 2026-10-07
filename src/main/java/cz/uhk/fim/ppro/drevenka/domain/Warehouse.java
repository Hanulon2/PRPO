package cz.uhk.fim.ppro.drevenka.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "warehouses")
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 200)
    private String address;

    @Column(nullable = false)
    private boolean isExpeditionPoint = false;

    public Warehouse() {}

    public Warehouse(String code, String name, String address, boolean isExpeditionPoint) {
        this.code = code;
        this.name = name;
        this.address = address;
        this.isExpeditionPoint = isExpeditionPoint;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isExpeditionPoint() { return isExpeditionPoint; }
    public void setExpeditionPoint(boolean expeditionPoint) { isExpeditionPoint = expeditionPoint; }
}
