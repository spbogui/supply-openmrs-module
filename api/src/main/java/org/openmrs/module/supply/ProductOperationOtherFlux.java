package org.openmrs.module.supply;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Location;

import javax.persistence.*;

@SuppressWarnings("JpaAttributeTypeInspection")
@Entity(name = "ProductOperationOtherFlux")
@Table(name = "supply_product_operation_other_flux")
public class ProductOperationOtherFlux extends BaseOpenmrsObject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_operation_other_flux_id", nullable = false)
	private Integer productOperationOtherFluxId;
	
	@ManyToOne
	@JoinColumn(name = "product_id")
	private Product product;
	
	//	@ManyToOne
	//	@JoinColumn(name = "product_attribute_id")
	//	private ProductAttribute productAttribute;
	
	@Column(name = "label", nullable = false)
	private String label;
	
	@Column(name = "quantity", nullable = false)
	private Double quantity;
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "operation_id", nullable = false)
	private ProductOperation operation;
	
	@ManyToOne
	@JoinColumn(name = "location_id", nullable = false)
	private Location location;
	
	public ProductOperationOtherFlux() {
	}
	
	public Integer getProductOperationOtherFluxId() {
		return productOperationOtherFluxId;
	}
	
	public void setProductOperationOtherFluxId(Integer productAttributeOtherFluxId) {
		this.productOperationOtherFluxId = productAttributeOtherFluxId;
	}
	
	public Product getProduct() {
		return product;
	}
	
	public void setProduct(Product product) {
		this.product = product;
	}
	
	//	public ProductAttribute getProductAttribute() {
	//		return productAttribute;
	//	}
	//
	//	public void setProductAttribute(ProductAttribute productAttribute) {
	//		this.productAttribute = productAttribute;
	//	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public Double getQuantity() {
		return quantity;
	}
	
	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}
	
	public ProductOperation getOperation() {
		return operation;
	}
	
	public void setOperation(ProductOperation operation) {
		this.operation = operation;
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
