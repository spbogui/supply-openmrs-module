/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.supply.api;

import org.openmrs.*;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.supply.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * The main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 */
@Transactional(readOnly = true)
public interface SupplyService extends OpenmrsService {
	
	/**
	 * *********** Inventory
	 */
	@Transactional(readOnly = true)
	List<ProductOperation> getAllProductInventoryByProgram(Location location, ProductProgram program,
	        ProductOperationType operationType, Boolean includeVoided) throws APIException;
	
	/**
	 * *********** Dispensation
	 */
	
	@Transactional(readOnly = true)
	ProductOperation getLastProductDispensationByPatient(Location location, String identifier,
	        ProductProgram productProgram, Date dispensationDate) throws APIException;
	
	@Transactional(readOnly = true)
	List<ProductOperation> getMobilePatientDispensation(Location location, ProductProgram program) throws APIException;
	
	List<ProductOperation> getMobileDispensationToTransform(Location location, ProductProgram program) throws APIException;
	
	void transformDispensation(ProductOperation dispensation) throws APIException;
	
	void validateInventory(ProductOperation operation);
	
	/**
	 * *********** Report
	 */
	
	Integer getLastOperationProductQuantityReceived(Product product, ProductOperation inventory, Location location,
	        Boolean isUrgent) throws APIException;
	
	Integer getLastOperationProductQuantityInStock(Product product, ProductOperation inventory, Location location,
	        Boolean isUrgent) throws APIException;
	
	Integer getLastOperationProductQuantityLost(Product product, ProductOperation inventory, Location location,
	        Boolean isUrgent) throws APIException;
	
	Integer getLastOperationProductQuantityDistributed(Product product, ProductOperation inventory, Location location,
	        Boolean isUrgent) throws APIException;
	
	Integer getLastOperationProductQuantityDistributedOneMonthAgo(Product product, ProductOperation inventory,
	        Location location, Boolean isUrgent) throws APIException;
	
	Integer getLastOperationProductQuantityDistributedTwoMonthAgo(Product product, ProductOperation inventory,
	        Location location, Boolean isUrgent) throws APIException;
	
	Integer getChildLocationsThatKnownRupture(Product product, ProductOperation inventory, Location location,
	        Boolean isUrgent) throws APIException;
	
	Double getProductAverageMonthlyConsumption(Product product, ProductProgram productProgram, Location location,
	        Boolean includeVoided) throws APIException;
	
	/**
	 * *********** Other service
	 */
	
	Boolean isDead(Patient patient, Location location) throws APIException;
	
	Boolean isTransferred(Patient patient, Location location) throws APIException;
	
	Date admissionDate(Patient patient, Location location) throws APIException;
	
	Date deathDate(Patient patient, Location location) throws APIException;
	
	Date transferDate(Patient patient, Location location) throws APIException;
	
	List<Location> getDirectClientLocations() throws APIException;
	
	Patient getPatientByIdentifier(String identifier) throws APIException;
	
	Obs getPatientLastObs(Person patient, Concept concept, EncounterType encounterType) throws APIException;
	
	Obs getPatientLastObs(Person patient, Concept concept) throws APIException;
	
	Obs getPatientLastObs(Person patient, Concept concept, EncounterType encounterType, Date endDate) throws APIException;
	
	Obs getPatientLastObs(Person patient, Concept concept, Date endDate) throws APIException;
	
	Encounter getPatientLastEncounter(Patient patient, EncounterType encounterType);
	
	Encounter getPatientLastEncounter(Patient patient, EncounterType encounterType, Date endDate);
}
