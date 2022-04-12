package org.openmrs.module.supply;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.util.OpenmrsUtil;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Comparator;

@Entity(name = "ProductOperationAttributeType")
@Table(name = "supply_product_operation_attribute_type")
public class ProductOperationAttributeType extends BaseOpenmrsObject implements Comparable<ProductOperationAttributeType> {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_operation_attribute_type_id", nullable = false)
	private Integer productOperationAttributeTypeId;
	
	@Column(name = "name", nullable = false, length = 200)
	private String name;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "format", nullable = false)
	private String format;
	
	@Column(name = "foreign_key")
	private Integer foreignKey;
	
	@Column(name = "sort_weight")
	private Double sortWeight;
	
	@Column(name = "searchable")
	private Boolean searchable = false;
	
	public ProductOperationAttributeType() {
	}
	
	public Integer getProductOperationAttributeTypeId() {
		return productOperationAttributeTypeId;
	}
	
	public void setProductOperationAttributeTypeId(Integer productOperationTypeId) {
		this.productOperationAttributeTypeId = productOperationTypeId;
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
	
	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
	
	public Integer getForeignKey() {
		return foreignKey;
	}
	
	public void setForeignKey(Integer foreignKey) {
		this.foreignKey = foreignKey;
	}
	
	public Double getSortWeight() {
		return sortWeight;
	}
	
	public void setSortWeight(Double sortWeight) {
		this.sortWeight = sortWeight;
	}
	
	public Boolean getSearchable() {
		return searchable;
	}
	
	public void setSearchable(Boolean searchable) {
		this.searchable = searchable;
	}
	
	@Override
	public Integer getId() {
		return productOperationAttributeTypeId;
	}
	
	@Override
	public void setId(Integer integer) {
		productOperationAttributeTypeId = integer;
	}
	
	@Override
	public String getUuid() {
		return super.getUuid();
	}
	
	@Override
	public void setUuid(String uuid) {
		super.setUuid(uuid);
	}
	
	@Override
	public int compareTo(ProductOperationAttributeType o) {
		DefaultComparator patDefaultComparator = new DefaultComparator();
		return patDefaultComparator.compare(this, o);
	}
	
	public static class DefaultComparator implements Comparator<ProductOperationAttributeType>, Serializable {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public int compare(ProductOperationAttributeType pat1, ProductOperationAttributeType pat2) {
			return OpenmrsUtil.compareWithNullAsGreatest(pat1.getProductOperationAttributeTypeId(),
			    pat2.getProductOperationAttributeTypeId());
			
		}
	}
}
