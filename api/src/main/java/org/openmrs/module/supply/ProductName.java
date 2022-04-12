package org.openmrs.module.supply;

import org.openmrs.BaseOpenmrsObject;

import javax.persistence.*;

@Entity(name = "ProductName")
@Table(name = "supply_product_name")
public class ProductName extends BaseOpenmrsObject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_name_id", nullable = false)
	private Integer productNameId;
	
	@ManyToOne(cascade = CascadeType.ALL, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;
	
	@Column(name = "product_name_type", nullable = false)
	private String productNameType;
	
	@Column(name = "name", nullable = false)
	private String name;
	
	@ManyToOne
	@JoinColumn(name = "product_unit", nullable = false)
	private ProductUnit unit;
	
	public ProductName() {
	}
	
	public ProductName(String name) {
		this.name = name;
	}
	
	public Product getProduct() {
		return product;
	}
	
	public void setProduct(Product product) {
		this.product = product;
	}
	
	public Integer getProductNameId() {
		return productNameId;
	}
	
	public void setProductNameId(Integer productNameId) {
		this.productNameId = productNameId;
	}
	
	public String getProductNameType() {
		return productNameType;
	}
	
	public void setProductNameType(String productNameType) {
		this.productNameType = productNameType;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public ProductUnit getUnit() {
		return unit;
	}
	
	public void setUnit(ProductUnit productUnit) {
		this.unit = productUnit;
	}
	
	@Override
	public Integer getId() {
		return productNameId;
	}
	
	@Override
	public void setId(Integer integer) {
		setProductNameId(integer);
	}
}
