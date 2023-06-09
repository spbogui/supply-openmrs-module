package org.openmrs.module.supply;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.module.supply.enumerations.Incidence;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "ProductOperationType")
@Table(name = "supply2_product_operation_type")
public class ProductOperationType extends BaseOpenmrsObject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_operation_type_id", nullable = false)
	private Integer productOperationTypeId;
	
	@Column(name = "name", unique = true, nullable = false, length = 200)
	private String name;
	
	@Column(name = "default_incidence", nullable = false)
	private Incidence defaultIncidence;
	
	@Column(name = "description")
	private String description;
	
	@ManyToMany
	@JoinTable(name = "supply_product_operation_type_attribute_type", joinColumns = @JoinColumn(name = "operation_type"), inverseJoinColumns = @JoinColumn(name = "attribute_type"))
	private Set<ProductOperationAttributeType> operationAttributeTypes = new HashSet<ProductOperationAttributeType>();
	
	public ProductOperationType() {
	}
	
	public Integer getProductOperationTypeId() {
		return productOperationTypeId;
	}
	
	public void setProductOperationTypeId(Integer productOperationTypeId) {
		this.productOperationTypeId = productOperationTypeId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Incidence getDefaultIncidence() {
		return defaultIncidence;
	}
	
	public void setDefaultIncidence(Incidence defaultIncidence) {
		this.defaultIncidence = defaultIncidence;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Set<ProductOperationAttributeType> getOperationAttributeTypes() {
		return operationAttributeTypes;
	}
	
	public void setOperationAttributeTypes(Set<ProductOperationAttributeType> operationAttributeTypes) {
		this.operationAttributeTypes = operationAttributeTypes;
	}
	
	@Override
	public Integer getId() {
		return productOperationTypeId;
	}
	
	@Override
	public void setId(Integer integer) {
		productOperationTypeId = integer;
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
