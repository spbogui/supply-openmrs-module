package org.openmrs.module.supply;

import org.openmrs.Encounter;
import org.openmrs.Provider;

import javax.persistence.*;
import java.util.Date;

@SuppressWarnings("JpaAttributeTypeInspection")
@Entity(name = "ProductDispensation")
@Table(name = "supply_product_dispensation")
public class ProductDispensation extends ProductOperation {
	
	@Column(name = "treatment_duration")
	private Integer treatmentDuration;
	
	@ManyToOne
	@JoinColumn(name = "provider_id")
	private Provider provider;
	
	@Column(name = "prescription_date")
	private Date prescriptionDate;
	
	@ManyToOne
	@JoinColumn(name = "regime_id")
	private ProductRegime productRegime;
	
	@Column(name = "regime_line")
	private Integer regimeLine;
	
	@Column(name = "age")
	private Integer age;
	
	@Column(name = "gender")
	private String gender;
	
	@Column(name = "goal")
	private String goal;
	
	@Column(name = "treatment_end_date")
	private Date treatmentEndDate;
	
	@ManyToOne
	@JoinColumn(name = "encounter_id")
	private Encounter encounter;
	
	public ProductDispensation() {
	}
	
	public Integer getTreatmentDuration() {
		return treatmentDuration;
	}
	
	public void setTreatmentDuration(Integer treatmentDuration) {
		this.treatmentDuration = treatmentDuration;
	}
	
	public Provider getProvider() {
		return provider;
	}
	
	public void setProvider(Provider provider) {
		this.provider = provider;
	}
	
	public Date getPrescriptionDate() {
		return prescriptionDate;
	}
	
	public void setPrescriptionDate(Date prescriptionDate) {
		this.prescriptionDate = prescriptionDate;
	}
	
	public ProductRegime getProductRegime() {
		return productRegime;
	}
	
	public void setProductRegime(ProductRegime productRegime) {
		this.productRegime = productRegime;
	}
	
	public Integer getRegimeLine() {
		return regimeLine;
	}
	
	public void setRegimeLine(Integer regimeLine) {
		this.regimeLine = regimeLine;
	}
	
	public Integer getAge() {
		return age;
	}
	
	public void setAge(Integer age) {
		this.age = age;
	}
	
	public String getGender() {
		return gender;
	}
	
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public String getGoal() {
		return goal;
	}
	
	public void setGoal(String goal) {
		this.goal = goal;
	}
	
	public Date getTreatmentEndDate() {
		return treatmentEndDate;
	}
	
	public void setTreatmentEndDate(Date treatmentEndDate) {
		this.treatmentEndDate = treatmentEndDate;
	}
	
	public Encounter getEncounter() {
		return encounter;
	}
	
	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}
}
