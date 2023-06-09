package org.openmrs.module.supply;

import org.openmrs.BaseOpenmrsObject;

import javax.persistence.*;

@Entity(name = "ProductUnit")
@Table(name = "supply2_product_unit")
public class ProductUnit extends BaseOpenmrsObject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_unit_id", nullable = false)
	private Integer productUnitId;
	
	@Column(name = "name", nullable = false, unique = true)
	private String name;
	
	@Column(name = "description")
	private String description;
	
	public ProductUnit() {
	}
	
	public Integer getProductUnitId() {
		return productUnitId;
	}
	
	public void setProductUnitId(Integer productUnitId) {
		this.productUnitId = productUnitId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public Integer getId() {
		return getProductUnitId();
	}
	
	@Override
	public void setId(Integer integer) {
		setProductUnitId(integer);
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
