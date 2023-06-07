package org.openmrs.module.supply;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Location;

import javax.persistence.*;

@Entity(name = "ProductPrice")
@Table(name = "supply2_product_price")
public class ProductPrice extends BaseOpenmrsData {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_price_id", nullable = false)
	private Integer productPriceId;
	
	//	@ManyToOne(cascade = CascadeType.ALL, optional = false)
	//	@JoinColumn(name = "product_id", nullable = false)
	//	private Product product;
	//
	//	@ManyToOne
	//	@JoinColumn(name = "product_program_id", nullable = false)
	//	private ProductProgram program;
	
	@ManyToOne
	@JoinColumn(name = "product_code_id", nullable = false)
	private ProductCode productCode;
	
	@Column(name = "sale_price", nullable = false)
	private Double salePrice;
	
	@Column(name = "purchase_price", nullable = false)
	private Double purchasePrice;
	
	@Column(name = "is_active")
	private Boolean active;
	
	@SuppressWarnings("JpaAttributeTypeInspection")
	@ManyToOne
	@JoinColumn(name = "location_id", nullable = false)
	private Location location;
	
	public ProductPrice() {
	}
	
	public Integer getProductPriceId() {
		return productPriceId;
	}
	
	public void setProductPriceId(Integer productPriceId) {
		this.productPriceId = productPriceId;
	}
	
	public ProductCode getProductCode() {
		return productCode;
	}
	
	public void setProductCode(ProductCode productCode) {
		this.productCode = productCode;
	}
	
	//	public Product getProduct() {
	//		return product;
	//	}
	//
	//	public void setProduct(Product product) {
	//		this.product = product;
	//	}
	//
	//	public ProductProgram getProgram() {
	//		return program;
	//	}
	//
	//	public void setProgram(ProductProgram program) {
	//		this.program = program;
	//	}
	
	public Double getSalePrice() {
		return salePrice;
	}
	
	public void setSalePrice(Double salePrice) {
		this.salePrice = salePrice;
	}
	
	public Double getPurchasePrice() {
		return purchasePrice;
	}
	
	public void setPurchasePrice(Double purchasePrice) {
		this.purchasePrice = purchasePrice;
	}
	
	public Boolean getActive() {
		return active;
	}
	
	public void setActive(Boolean active) {
		this.active = active;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public Boolean isActive() {
		return active;
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
