/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.supply.api.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.supply.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository("supply.SupplyDao")
public class SupplyDao {
	
	@Autowired
	DbSessionFactory sessionFactory;
	
	private DbSession getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	public List<ProductOperation> getAllProductInventoryByProgram(Location location, ProductProgram program,
	        ProductOperationType operationType, Boolean includeVoided) {
		return null;
	}
	
	public ProductOperation getLastProductDispensationByPatient(Location location, String identifier,
	        ProductProgram productProgram, Date dispensationDate) {
		return null;
	}
	
	public List<ProductOperation> getMobilePatientDispensation(Location location, ProductProgram program) {
		return null;
	}
	
	public List<ProductOperation> getMobileDispensationToTransform(Location location, ProductProgram program) {
		return null;
	}
	
	public void transformDispensation(ProductOperation dispensation) {
		
	}
	
	public Integer getLastOperationProductQuantityReceived(Product product, ProductOperation inventory, Location location,
	        Boolean isUrgent) {
		return null;
	}
	
	public Integer getLastOperationProductQuantityInStock(Product product, ProductOperation inventory, Location location,
	        Boolean isUrgent) {
		return null;
	}
	
	public Integer getLastOperationProductQuantityLost(Product product, ProductOperation inventory, Location location,
	        Boolean isUrgent) {
		return null;
	}
	
	public Integer getLastOperationProductQuantityDistributed(Product product, ProductOperation inventory,
	        Location location, Boolean isUrgent) {
		return null;
	}
	
	public Integer getLastOperationProductQuantityDistributedOneMonthAgo(Product product, ProductOperation inventory,
	        Location location, Boolean isUrgent) {
		return null;
	}
	
	public Integer getLastOperationProductQuantityDistributedTwoMonthAgo(Product product, ProductOperation inventory,
	        Location location, Boolean isUrgent) {
		return null;
	}
	
	public Integer getChildLocationsThatKnownRupture(Product product, ProductOperation inventory, Location location,
	        Boolean isUrgent) {
		return null;
	}
	
	public Double getProductAverageMonthlyConsumption(Product product, ProductProgram productProgram, Location location,
	        Boolean includeVoided) {
		return null;
	}
	
	public void saveInventory(ProductOperation operation) {
		
	}
	
	public Boolean isTransferred(Patient patient, Location location) {
		return transferDate(patient, location) != null;
	}
	
	public Date transferDate(Patient patient, Location location) {
		Obs obs = (Obs) getSession()
		        .createQuery(
		            "SELECT o FROM Obs o "
		                    + "WHERE o.person = :patient AND o.concept.conceptId = 164595 AND "
		                    + " o.valueDatetime >= (SELECT MAX(e.encounterDatetime) FROM Encounter e WHERE e.patient = :patient AND e.encounterType.uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f' AND e.voided = false AND e.location = :location GROUP BY e.patient) AND "
		                    + " o.voided = false AND o.location = :location").setParameter("patient", patient)
		        .setParameter("location", location).setMaxResults(1).uniqueResult();
		if (obs != null) {
			return obs.getValueDate() != null ? obs.getValueDate() : obs.getValueDatetime();
		}
		return null;
	}
	
	public Boolean isDead(Patient patient, Location location) {
		return deathDate(patient, location) != null;
	}
	
	public Date deathDate(Patient patient, Location location) {
		Obs obs = (Obs) getSession()
		        .createQuery(
		            "SELECT o FROM Obs o " + "WHERE o.person = :patient AND o.concept.conceptId = 1543 AND "
		                    + "o.voided = false AND o.location = :location").setParameter("patient", patient)
		        .setParameter("location", location).uniqueResult();
		if (obs != null) {
			return obs.getValueDate() != null ? obs.getValueDate() : obs.getValueDatetime();
		}
		return null;
	}
	
	public Date admissionDate(Patient patient, Location location) {
		Encounter encounter = (Encounter) getSession()
		        .createQuery(
		            "SELECT e FROM Encounter e "
		                    + "WHERE e.patient = :patient AND "
		                    + " e.encounterDatetime >= (SELECT MAX(em.encounterDatetime) FROM Encounter em WHERE em.patient = :patient AND em.encounterType.uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f' AND em.voided = false AND em.location = :location GROUP BY em.patient) AND "
		                    + " e.voided = false AND e.location = :location AND e.encounterType.uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f'")
		        .setParameter("patient", patient).setParameter("location", location).uniqueResult();
		return encounter != null ? encounter.getEncounterDatetime() : null;
	}
	
	@SuppressWarnings("unchecked")
    public List<Location> getDirectClientLocations() {
        try {
            Query query = getSession().createQuery(
                    "SELECT l.location FROM LocationAttribute l WHERE "
                            + "l.attributeType.uuid = 'NPSPCLIENTCCCCCCCCCCCCCCCCCCCCCCCCCC' AND "
                            + "l.valueReference = 'true' AND l.voided = :includeVoided ");
            query.setParameter("includeVoided", false);
            return query.list();
        } catch (HibernateException e) {
            System.out.println(e.getMessage());
        }
        return new ArrayList<>();
    }
	
	public Patient getPatientByIdentifier(String identifier) {
		try {
			Query query = getSession().createQuery(
			    "SELECT pi.patient FROM PatientIdentifier pi WHERE pi.identifier = :identifier AND " + "pi.voided = false ");
			query.setParameter("identifier", identifier);
			
			System.out.println("-------------------------------------->" + query.uniqueResult());
			return (Patient) query.uniqueResult();
		}
		catch (HibernateException e) {
			System.out.println("-------------------------------------->" + e.getMessage());
		}
		return null;
	}
}
