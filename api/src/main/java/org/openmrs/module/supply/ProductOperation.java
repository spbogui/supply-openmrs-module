package org.openmrs.module.supply;

import org.openmrs.Auditable;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Location;
import org.openmrs.module.supply.enumerations.Incidence;
import org.openmrs.module.supply.enumerations.OperationStatus;
import org.openmrs.module.supply.enumerations.QuantityType;

import javax.persistence.*;
import java.util.*;

@SuppressWarnings("JpaAttributeTypeInspection")
@Entity(name = "ProductOperation")
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "supply_product_operation")
public class ProductOperation extends BaseOpenmrsData implements Auditable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_operation_id", nullable = false)
	private Integer productOperationId;
	
	@Column(name = "operation_number")
	private String operationNumber;
	
	@ManyToOne
	@JoinColumn(name = "program_id", nullable = false)
	private ProductProgram productProgram;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "operation_date", nullable = false)
	private Date operationDate;
	
	@ManyToOne
	@JoinColumn(name = "operation_type", nullable = false)
	private ProductOperationType operationType;
	
	@ManyToOne
	@JoinColumn(name = "linked_operation")
	private ProductOperation parentOperation;
	
	@Column(name = "operation_status", nullable = false)
	private OperationStatus operationStatus;
	
	@Column(name = "incidence", nullable = false)
	private Incidence incidence;
	
	@Column(name = "quantity_type", nullable = false)
	private QuantityType quantityType = QuantityType.DISPENSATION;
	
	@Column(name = "observation")
	private String observation;
	
	@OneToMany(mappedBy = "operation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<ProductOperationFlux> fluxes = new HashSet<ProductOperationFlux>();
	
	@OneToMany(mappedBy = "operation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<ProductOperationOtherFlux> otherFluxes = new HashSet<ProductOperationOtherFlux>();
	
	@OneToMany(mappedBy = "operation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<ProductOperationAttribute> attributes = new HashSet<ProductOperationAttribute>();
	
	@OneToMany(mappedBy = "parentOperation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<ProductOperation> childrenOperation = null;
	
	@ManyToOne
	@JoinColumn(name = "exchange_location")
	private Location exchangeLocation;
	
	@ManyToOne
	@JoinColumn(name = "location_id", nullable = false)
	private Location location;
	
	@Transient
	private Double totalPurchasePrice = 0.;
	
	@Transient
	private Double totalSalePrice = 0.;
	
	public ProductOperation() {
	}
	
	public Double getTotalSalePrice() {
		for (ProductOperationFlux flux : fluxes) {
			totalSalePrice += flux.getProductCode().getCurrentPrice().getSalePrice() * flux.getQuantity();
		}
		return totalSalePrice;
	}
	
	public void setTotalSalePrice(Double totalSalePrice) {
		this.totalSalePrice = totalSalePrice;
	}
	
	public Double getTotalPurchasePrice() {
		for (ProductOperationFlux flux : fluxes) {
			totalPurchasePrice += flux.getProductCode().getCurrentPrice().getPurchasePrice() * flux.getQuantity();
		}
		return totalPurchasePrice;
	}
	
	public void setTotalPurchasePrice(Double totalPurchasePrice) {
		this.totalPurchasePrice = totalPurchasePrice;
	}
	
	public Integer getProductOperationId() {
		return productOperationId;
	}
	
	public void setProductOperationId(Integer productOperationId) {
		this.productOperationId = productOperationId;
	}
	
	public String getOperationNumber() {
		return operationNumber;
	}
	
	public void setOperationNumber(String operationNumber) {
		this.operationNumber = operationNumber;
	}
	
	public ProductProgram getProductProgram() {
		return productProgram;
	}
	
	public void setProductProgram(ProductProgram productProgram) {
		this.productProgram = productProgram;
	}
	
	public Date getOperationDate() {
		return operationDate;
	}
	
	public void setOperationDate(Date operationDate) {
		this.operationDate = operationDate;
	}
	
	public ProductOperationType getOperationType() {
		return operationType;
	}
	
	public void setOperationType(ProductOperationType operationType) {
		this.operationType = operationType;
	}
	
	public ProductOperation getParentOperation() {
		return parentOperation;
	}
	
	public void setParentOperation(ProductOperation parentOperation) {
		this.parentOperation = parentOperation;
	}
	
	public OperationStatus getOperationStatus() {
		return operationStatus;
	}
	
	public void setOperationStatus(OperationStatus operationStatus) {
		this.operationStatus = operationStatus;
	}
	
	public Incidence getIncidence() {
		return incidence;
	}
	
	public void setIncidence(Incidence incidence) {
		this.incidence = incidence;
	}
	
	public String getObservation() {
		return observation;
	}
	
	public void setObservation(String observation) {
		this.observation = observation;
	}
	
	public QuantityType getQuantityType() {
		return quantityType;
	}
	
	public void setQuantityType(QuantityType quantityType) {
		this.quantityType = quantityType;
	}
	
	public Set<ProductOperationFlux> getFluxes() {
		return fluxes;
	}
	
	public void setFluxes(Set<ProductOperationFlux> fluxes) {
		this.fluxes = fluxes;
	}
	
	public Set<ProductOperationOtherFlux> getOtherFluxes() {
		return otherFluxes;
	}
	
	public void setOtherFluxes(Set<ProductOperationOtherFlux> otherFluxes) {
		this.otherFluxes = otherFluxes;
	}
	
	public Set<ProductOperationAttribute> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(Set<ProductOperationAttribute> attributes) {
		this.attributes = attributes;
	}
	
	public Set<ProductOperation> getChildrenOperation() {
		return childrenOperation;
	}
	
	public void setChildrenOperation(Set<ProductOperation> childrenOperation) {
		this.childrenOperation = childrenOperation;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public Location getExchangeLocation() {
		return exchangeLocation;
	}
	
	public void setExchangeLocation(Location exchangeLocation) {
		this.exchangeLocation = exchangeLocation;
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
	
	public void addFlux(ProductOperationFlux flux) {
		if (fluxes == null) {
			fluxes = new HashSet<ProductOperationFlux>();
		}
		flux.setOperation(this);
		fluxes.add(flux);
	}
	
	public void removeFlux(ProductOperationFlux fluxToRemove) {
		if (fluxes != null && fluxes.size() != 0) {
			for (ProductOperationFlux flux : fluxes) {
				if (flux.getUuid().equals(fluxToRemove.getUuid())) {
					fluxes.remove(fluxToRemove);
					return;
				}
			}
		}
	}
	
	public void addAllFluxes(List<ProductOperationFlux> operationFluxes) {
		if (fluxes == null) {
			fluxes = new HashSet<ProductOperationFlux>();
		}
		for (ProductOperationFlux flux : operationFluxes) {
			addFlux(flux);
		}
	}
	
	public void addOtherFlux(ProductOperationOtherFlux flux) {
		if (otherFluxes == null) {
			otherFluxes = new HashSet<ProductOperationOtherFlux>();
		}
		flux.setOperation(this);
		otherFluxes.add(flux);
	}
	
	public void removeOtherFlux(ProductOperationOtherFlux fluxToRemove) {
		if (otherFluxes != null && otherFluxes.size() != 0) {
			for (ProductOperationOtherFlux flux : otherFluxes) {
				if (flux.getUuid().equals(fluxToRemove.getUuid())) {
					otherFluxes.remove(fluxToRemove);
					return;
				}
			}
		}
	}
	
	public void addOperationAttribute(ProductOperationAttribute operationAttribute) {
		if (attributes == null) {
			attributes = new HashSet<ProductOperationAttribute>();
		}
		operationAttribute.setOperation(this);
		attributes.add(operationAttribute);
	}
	
	public void removeOperationAttribute(ProductOperationAttribute operationAttributeToRemove) {
		if (attributes != null && attributes.size() != 0) {
			for (ProductOperationAttribute operationAttribute : attributes) {
				if (operationAttribute.getUuid().equals(operationAttributeToRemove.getUuid())) {
					attributes.remove(operationAttributeToRemove);
					return;
				}
			}
		}
	}
	
	public List<ProductOperationAttribute> getActiveOperationAttributes() {
        List<ProductOperationAttribute> activeOperationAttributes = new ArrayList<>();
        for (ProductOperationAttribute attribute : getAttributes()) {
            if (!attribute.getVoided()) {
                activeOperationAttributes.add(attribute);
            }
        }
        return activeOperationAttributes;
    }
	
	public List<ProductOperationFluxAttribute> getFluxAttributes() {
        List<ProductOperationFluxAttribute> attributes = new ArrayList<>();
        for (ProductOperationFlux flux : getFluxes()) {
            attributes.addAll(flux.getAttributes());
        }

        return attributes;
    }
}
