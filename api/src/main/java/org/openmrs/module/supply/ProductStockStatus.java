package org.openmrs.module.supply;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Location;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "ProductStockStatus")
@Table(name = "supply2_product_stock_status")
public class ProductStockStatus extends BaseOpenmrsObject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_stock_status_id", nullable = false)
	private Integer productStockStatusId;
	
	@ManyToOne
	@JoinColumn(name = "product_code_id", nullable = false)
	private ProductCode productCode;
	
	@ManyToOne
	@JoinColumn(name = "location_id", nullable = false)
	private Location location;
	
	@Column(name = "quantity_in_stock", nullable = false)
	private Double quantityInStock;
	
	@Column(name = "average_consumed_quantity", nullable = false)
	private Double averageConsumedQuantity;
	
	@Column(name = "stock_date", nullable = false)
	private Date stockDate;
	
	@Column(name = "expiry_next_date")
	private Date expiryNextDate;
	
	public ProductStockStatus() {
	}
	
	public ProductStockStatus(ProductCode productCode, Location location, Double quantityInStock,
	    Double averageConsumedQuantity, Date stockDate) {
		this.productCode = productCode;
		this.location = location;
		this.quantityInStock = quantityInStock;
		this.averageConsumedQuantity = averageConsumedQuantity;
		this.stockDate = stockDate;
	}
	
	public ProductCode getProductCode() {
		return productCode;
	}
	
	public void setProductCode(ProductCode productCode) {
		this.productCode = productCode;
	}
	
	public Integer getProductStockStatusId() {
		return productStockStatusId;
	}
	
	public void setProductStockStatusId(Integer productStockStatusId) {
		this.productStockStatusId = productStockStatusId;
	}
	
	@Override
	public Integer getId() {
		return getProductStockStatusId();
	}
	
	@Override
	public void setId(Integer id) {
		setProductStockStatusId(id);
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public Double getQuantityInStock() {
		return quantityInStock;
	}
	
	public void setQuantityInStock(Double quantityInStock) {
		this.quantityInStock = quantityInStock;
	}
	
	public Double getAverageConsumedQuantity() {
		return averageConsumedQuantity;
	}
	
	public void setAverageConsumedQuantity(Double averageConsumedQuantity) {
		this.averageConsumedQuantity = averageConsumedQuantity;
	}
	
	public Date getStockDate() {
		return stockDate;
	}
	
	public void setStockDate(Date stockDate) {
		this.stockDate = stockDate;
	}
	
	public Date getExpiryNextDate() {
		return expiryNextDate;
	}
	
	public void setExpiryNextDate(Date expiryNextDate) {
		this.expiryNextDate = expiryNextDate;
	}
}
