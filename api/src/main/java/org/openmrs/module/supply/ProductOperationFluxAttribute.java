package org.openmrs.module.supply;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Location;

import javax.persistence.*;

@Entity(name = "ProductOperationFluxAttribute")
@Table(name = "supply2_product_operation_flux_attribute")
public class ProductOperationFluxAttribute extends BaseOpenmrsObject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_operation_flux_attribute_id", nullable = false)
	private Integer productOperationFluxAttributeId;
	
	@Column(name = "quantity", nullable = false)
	private Double quantity;
	
	@ManyToOne
	@JoinColumn(name = "product_attribute_id")
	private ProductAttribute attribute;
	
	@ManyToOne
	@JoinColumn(name = "operation_flux_id", nullable = false)
	private ProductOperationFlux operationFlux;
	
	@SuppressWarnings("JpaAttributeTypeInspection")
	@ManyToOne
	@JoinColumn(name = "location_id", nullable = false)
	private Location location;
	
	public ProductOperationFluxAttribute() {
	}
	
	public Integer getProductOperationFluxAttributeId() {
		return productOperationFluxAttributeId;
	}
	
	public void setProductOperationFluxAttributeId(Integer productOperationFluxAttributeId) {
		this.productOperationFluxAttributeId = productOperationFluxAttributeId;
	}
	
	public Double getQuantity() {
		return quantity;
	}
	
	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}
	
	public ProductAttribute getAttribute() {
		return attribute;
	}
	
	public void setAttribute(ProductAttribute productAttribute) {
		this.attribute = productAttribute;
	}
	
	public ProductOperationFlux getOperationFlux() {
		return operationFlux;
	}
	
	public void setOperationFlux(ProductOperationFlux operationFlux) {
		this.operationFlux = operationFlux;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
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
