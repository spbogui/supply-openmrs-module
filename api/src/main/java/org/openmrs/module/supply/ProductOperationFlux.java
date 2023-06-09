package org.openmrs.module.supply;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Location;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("JpaAttributeTypeInspection")
@Entity(name = "ProductOperationFlux")
@Table(name = "supply2_product_operation_flux")
public class ProductOperationFlux extends BaseOpenmrsObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_operation_flux_id", nullable = false)
    private Integer productAttributeFluxId;

    @ManyToOne
    @JoinColumn(name = "product_code_id")
    private ProductCode productCode;

    @Column(name = "quantity", nullable = false)
    private Double quantity;

    @Column(name = "related_quantity")
    private Double relatedQuantity;

    @Column(name = "related_quantity_label")
    private String relatedQuantityLabel;

    @ManyToOne
    @JoinColumn(name = "operation_id", nullable = false)
    private ProductOperation operation;

    @Column(name = "observation")
    private String observation;

    @SuppressWarnings("JpaAttributeTypeInspection")
    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @OneToMany(mappedBy = "operationFlux", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProductOperationFluxAttribute> attributes = new HashSet<>();

    public ProductOperationFlux() {
    }

    public Integer getProductAttributeFluxId() {
        return productAttributeFluxId;
    }

    public void setProductAttributeFluxId(Integer productAttributeFluxId) {
        this.productAttributeFluxId = productAttributeFluxId;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getRelatedQuantity() {
        return relatedQuantity;
    }

    public void setRelatedQuantity(Double relatedQuantity) {
        this.relatedQuantity = relatedQuantity;
    }

    public String getRelatedQuantityLabel() {
        return relatedQuantityLabel;
    }

    public void setRelatedQuantityLabel(String fluxLabel) {
        this.relatedQuantityLabel = fluxLabel;
    }

    public ProductOperation getOperation() {
        return operation;
    }

    public void setOperation(ProductOperation operation) {
        this.operation = operation;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public ProductCode getProductCode() {
        return productCode;
    }

    public void setProductCode(ProductCode productCode) {
        this.productCode = productCode;
    }

    public Set<ProductOperationFluxAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<ProductOperationFluxAttribute> attributes) {
        this.attributes = attributes;
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

    public void addAttribute(ProductOperationFluxAttribute attribute) {
        if (this.attributes == null) {
            attributes = new HashSet<>();
        }
        attribute.setOperationFlux(this);
        attributes.add(attribute);
    }

    public void addAllAttributes(List<ProductOperationFluxAttribute> fluxAttributes) {
        if (this.attributes == null) {
            attributes = new HashSet<>();
        }
        for (ProductOperationFluxAttribute attribute : fluxAttributes) {
            addAttribute(attribute);
        }
    }
}
