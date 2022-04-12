package org.openmrs.module.supply;

import org.openmrs.BaseOpenmrsObject;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "ProductProgram")
@Table(name = "supply_product_program")
public class ProductProgram extends BaseOpenmrsObject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_program_id", nullable = false)
	private Integer productProgramId;
	
	@Column(name = "name", nullable = false, unique = true)
	private String name;
	
	@Column(name = "description")
	private String description;
	
	@ManyToMany(mappedBy = "programs", fetch = FetchType.LAZY)
	private Set<Product> products = new HashSet<Product>();
	
	public ProductProgram() {
	}
	
	public Integer getProductProgramId() {
		return productProgramId;
	}
	
	public void setProductProgramId(Integer productRegimenId) {
		this.productProgramId = productRegimenId;
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
	
	public Set<Product> getProducts() {
		return products;
	}
	
	public void setProducts(Set<Product> products) {
		this.products = products;
	}
	
	@Override
	public Integer getId() {
		return productProgramId;
	}
	
	@Override
	public void setId(Integer integer) {
		this.productProgramId = integer;
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
