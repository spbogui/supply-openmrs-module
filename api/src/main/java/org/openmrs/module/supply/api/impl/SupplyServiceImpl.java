/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.supply.api.impl;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.UserService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.supply.*;
import org.openmrs.module.supply.api.SupplyService;
import org.openmrs.module.supply.api.dao.SupplyDao;

import java.util.Date;
import java.util.List;

public class SupplyServiceImpl extends BaseOpenmrsService implements SupplyService {
	
	SupplyDao dao;
	
	UserService userService;
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setDao(SupplyDao dao) {
		this.dao = dao;
	}
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	@Override
	public List<ProductOperation> getAllProductInventoryByProgram(Location location, ProductProgram program,
	        ProductOperationType operationType, Boolean includeVoided) throws APIException {
		return dao.getAllProductInventoryByProgram(location, program, operationType, includeVoided);
	}
	
	@Override
	public ProductOperation getLastProductDispensationByPatient(Location location, String identifier,
	        ProductProgram productProgram, Date dispensationDate) throws APIException {
		return dao.getLastProductDispensationByPatient(location, identifier, productProgram, dispensationDate);
	}
	
	@Override
	public List<ProductOperation> getMobilePatientDispensation(Location location, ProductProgram program)
	        throws APIException {
		return dao.getMobilePatientDispensation(location, program);
	}
	
	@Override
	public List<ProductOperation> getMobileDispensationToTransform(Location location, ProductProgram program)
	        throws APIException {
		return dao.getMobileDispensationToTransform(location, program);
	}
	
	@Override
	public void transformDispensation(ProductOperation dispensation) throws APIException {
		dao.transformDispensation(dispensation);
	}
	
	@Override
	public void validateInventory(ProductOperation operation) {
		dao.saveInventory(operation);
	}
	
	@Override
	public Integer getLastOperationProductQuantityReceived(Product product, ProductOperation inventory, Location location,
	        Boolean isUrgent) throws APIException {
		return dao.getLastOperationProductQuantityReceived(product, inventory, location, isUrgent);
	}
	
	@Override
	public Integer getLastOperationProductQuantityInStock(Product product, ProductOperation inventory, Location location,
	        Boolean isUrgent) throws APIException {
		return dao.getLastOperationProductQuantityInStock(product, inventory, location, isUrgent);
	}
	
	@Override
	public Integer getLastOperationProductQuantityLost(Product product, ProductOperation inventory, Location location,
	        Boolean isUrgent) throws APIException {
		return dao.getLastOperationProductQuantityLost(product, inventory, location, isUrgent);
	}
	
	@Override
	public Integer getLastOperationProductQuantityDistributed(Product product, ProductOperation inventory,
	        Location location, Boolean isUrgent) throws APIException {
		return dao.getLastOperationProductQuantityDistributed(product, inventory, location, isUrgent);
	}
	
	@Override
	public Integer getLastOperationProductQuantityDistributedOneMonthAgo(Product product, ProductOperation inventory,
	        Location location, Boolean isUrgent) throws APIException {
		return dao.getLastOperationProductQuantityDistributedOneMonthAgo(product, inventory, location, isUrgent);
	}
	
	@Override
	public Integer getLastOperationProductQuantityDistributedTwoMonthAgo(Product product, ProductOperation inventory,
	        Location location, Boolean isUrgent) throws APIException {
		return dao.getLastOperationProductQuantityDistributedTwoMonthAgo(product, inventory, location, isUrgent);
	}
	
	@Override
	public Integer getChildLocationsThatKnownRupture(Product product, ProductOperation inventory, Location location,
	        Boolean isUrgent) throws APIException {
		return dao.getChildLocationsThatKnownRupture(product, inventory, location, isUrgent);
	}
	
	@Override
	public Double getProductAverageMonthlyConsumption(Product product, ProductProgram productProgram, Location location,
	        Boolean includeVoided) throws APIException {
		return dao.getProductAverageMonthlyConsumption(product, productProgram, location, includeVoided);
	}
	
	@Override
	public Boolean isDead(Patient patient, Location location) throws APIException {
		return dao.isDead(patient, location);
	}
	
	@Override
	public Boolean isTransferred(Patient patient, Location location) throws APIException {
		return dao.isTransferred(patient, location);
	}
	
	@Override
	public Date admissionDate(Patient patient, Location location) throws APIException {
		return dao.admissionDate(patient, location);
	}
	
	@Override
	public Date deathDate(Patient patient, Location location) throws APIException {
		return dao.deathDate(patient, location);
	}
	
	@Override
	public Date transferDate(Patient patient, Location location) throws APIException {
		return dao.transferDate(patient, location);
	}
	
	@Override
	public List<Location> getDirectClientLocations() throws APIException {
		return dao.getDirectClientLocations();
	}
	
	@Override
	public Patient getPatientByIdentifier(String identifier) throws APIException {
		return dao.getPatientByIdentifier(identifier);
	}
	
}
