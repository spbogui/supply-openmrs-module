package org.openmrs.module.supply;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Location;

import javax.persistence.*;

@SuppressWarnings("ALL")
@Entity(name = "ProductAttributeStock")
@Table(name = "supply2_product_attribute_stock")
public class ProductAttributeStock extends BaseOpenmrsData {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_attribute_stock_id", nullable = false)
	private Integer productAttributeStockId;
	
	@ManyToOne
	@JoinColumn(name = "product_attribute", nullable = false)
	private ProductAttribute attribute;
	
	@ManyToOne
	@JoinColumn(name = "operation_id")
	private ProductOperation operation;
	
	//    @ManyToOne
	//    @JoinColumn(name = "program_id")
	//    private ProductProgram program;
	
	@Column(name = "quantity_in_stock", nullable = false)
	private Integer quantityInStock;
	
	@ManyToOne
	@JoinColumn(name = "location_id", nullable = false)
	private Location location;
	
	public ProductAttributeStock() {
	}
	
	public Integer getProductAttributeStockId() {
		return productAttributeStockId;
	}
	
	public void setProductAttributeStockId(Integer productAttributeStockId) {
		this.productAttributeStockId = productAttributeStockId;
	}
	
	public ProductAttribute getAttribute() {
		return attribute;
	}
	
	public void setAttribute(ProductAttribute attribute) {
		this.attribute = attribute;
	}
	
	public Integer getQuantityInStock() {
		return quantityInStock;
	}
	
	public void setQuantityInStock(Integer quantityInStock) {
		this.quantityInStock = quantityInStock;
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
	
	public String getProductInStock() {
		return attribute.getProductCode().getProduct().getPackagingName() + " - " + attribute.getProductCode().getCode()
		        + " [" + attribute.getBatchNumber() + "]";
	}
	
	//    public ProductProgram getProgram() {
	//        return program;
	//    }
	//
	//    public void setProgram(ProductProgram program) {
	//        this.program = program;
	//    }
	
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
