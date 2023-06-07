package org.openmrs.module.supply;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Location;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("JpaAttributeTypeInspection")
@Entity(name = "ProductAttribute")
@Table(name = "supply2_product_attribute")
public class ProductAttribute extends BaseOpenmrsData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_attribute_id", nullable = false)
    private Integer productAttributeId;

    @ManyToOne
    @JoinColumn(name = "product_code_id", nullable = false)
    private ProductCode productCode;

    @Column(name = "batch_number", nullable = false)
    private String batchNumber;

    @Temporal(TemporalType.DATE)
    @Column(name = "expiry_date", nullable = false)
    private Date expiryDate;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL)
    private Set<ProductOperationFluxAttribute> fluxAttributes = new HashSet<>();

    public ProductAttribute() {
    }

    public Integer getProductAttributeId() {
        return productAttributeId;
    }

    public void setProductAttributeId(Integer productAttributeFluxId) {
        this.productAttributeId = productAttributeFluxId;
    }

    public ProductCode getProductCode() {
        return productCode;
    }

    public void setProductCode(ProductCode productCode) {
        this.productCode = productCode;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Set<ProductOperationFluxAttribute> getFluxAttributes() {
        return fluxAttributes;
    }

    public void setFluxAttributes(Set<ProductOperationFluxAttribute> fluxAttributes) {
        this.fluxAttributes = fluxAttributes;
    }

    @Override
    public Integer getId() {
        return null;
    }

    @Override
    public void setId(Integer integer) {

    }

    @Override
    public String getUuid() {
        return super.getUuid();
    }

    @Override
    public void setUuid(String uuid) {
        super.setUuid(uuid);
    }
}
