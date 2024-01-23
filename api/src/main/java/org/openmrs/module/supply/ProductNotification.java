package org.openmrs.module.supply;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Location;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "ProductNotification")
@Table(name = "supply2_product_notification")
public class ProductNotification extends BaseOpenmrsObject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_notification_id", nullable = false)
	private Integer productNotificationId;
	
	@Column(name = "notification_date", nullable = false)
	private Date notificationDate;
	
	@ManyToOne
	@JoinColumn(name = "product_code_id")
	private ProductCode productCode;
	
	@ManyToOne
	@JoinColumn(name = "operation_type_id")
	private ProductOperationType operationType;
	
	@Column(name = "notification", nullable = false)
	private String notification;
	
	@Column(name = "notification_info")
	private String notificationInfo;
	
	@ManyToOne
	@JoinColumn(name = "notified_to", nullable = false)
	private Location notifiedTo;
	
	@Column(name = "notification_read", nullable = false)
	private Boolean notificationRead = false;
	
	@Column(name = "notification_closed", nullable = false)
	private Boolean notificationClosed = false;
	
	@ManyToOne
	@JoinColumn(name = "location_id", nullable = false)
	private Location location;
	
	public ProductNotification() {
	}
	
	public Integer getProductNotificationId() {
		return productNotificationId;
	}
	
	public void setProductNotificationId(Integer productRegimenId) {
		this.productNotificationId = productRegimenId;
	}
	
	public Date getNotificationDate() {
		return notificationDate;
	}
	
	public void setNotificationDate(Date notificationDate) {
		this.notificationDate = notificationDate;
	}
	
	@Override
	public Integer getId() {
		return productNotificationId;
	}
	
	@Override
	public void setId(Integer integer) {
		this.productNotificationId = integer;
	}
	
	@Override
	public String getUuid() {
		return super.getUuid();
	}
	
	@Override
	public void setUuid(String uuid) {
		super.setUuid(uuid);
	}
	
	public ProductCode getProductCode() {
		return productCode;
	}
	
	public void setProductCode(ProductCode productCode) {
		this.productCode = productCode;
	}
	
	public String getNotification() {
		return notification;
	}
	
	public void setNotification(String notification) {
		this.notification = notification;
	}
	
	public ProductOperationType getOperationType() {
		return operationType;
	}
	
	public void setOperationType(ProductOperationType operationType) {
		this.operationType = operationType;
	}
	
	public String getNotificationInfo() {
		return notificationInfo;
	}
	
	public void setNotificationInfo(String notificationInfo) {
		this.notificationInfo = notificationInfo;
	}
	
	public Location getNotifiedTo() {
		return notifiedTo;
	}
	
	public void setNotifiedTo(Location notifiedTo) {
		this.notifiedTo = notifiedTo;
	}
	
	public Boolean getNotificationRead() {
		return notificationRead;
	}
	
	public void setNotificationRead(Boolean notificationRead) {
		this.notificationRead = notificationRead;
	}
	
	public Boolean getNotificationClosed() {
		return notificationClosed;
	}
	
	public void setNotificationClosed(Boolean notificationClosed) {
		this.notificationClosed = notificationClosed;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
}
