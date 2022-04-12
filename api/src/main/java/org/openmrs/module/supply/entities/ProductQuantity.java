package org.openmrs.module.supply.entities;

import java.io.Serializable;

public class ProductQuantity implements Serializable {
	
	private String program;
	
	private Double quantity;
	
	public ProductQuantity(String program, Double quantity) {
		this.program = program;
		this.quantity = quantity;
	}
	
	public ProductQuantity() {
	}
	
	public String getProgram() {
		return program;
	}
	
	public void setProgram(String program) {
		this.program = program;
	}
	
	public Double getQuantity() {
		return quantity;
	}
	
	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}
}
