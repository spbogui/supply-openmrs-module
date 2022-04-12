package org.openmrs.module.supply.utils;

import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.ProductDispensation;
import org.openmrs.module.supply.ProductOperation;
import org.openmrs.module.supply.ProductOperationAttribute;
import org.openmrs.module.supply.ProductProgram;
import org.openmrs.module.supply.api.ProductService;
import org.openmrs.module.supply.api.SupplyService;

import java.util.*;

public class SupplyUtils {
	
	public static Location getUserLocation() {
		if (Context.getUserContext().getLocation() != null) {
			return Context.getUserContext().getLocation();
		}
		return Context.getLocationService().getDefaultLocation();
	}
	
	public static List<Location> getUserLocations() {
        List<Location> locations = new ArrayList<>();
        locations.add(getUserLocation());
        locations.addAll(getUserLocation().getChildLocations());
        return locations;
    }
	
	public static List<ProductProgram> getLocationPrograms(Location location) {
        List<ProductProgram> productPrograms = new ArrayList<>();
        for (LocationAttribute attribute : location.getActiveAttributes()) {
            if (attribute.getAttributeType().getName().equals("Programmes Disponibles")) {
                String programString = attribute.getValue().toString();
                if (programString != null) {
                    String[] programsString = programString.split(",");
                    for (String programName : programsString) {
                        productPrograms.add(Context.getService(ProductService.class).getProductProgramByName(programName));
                    }
                }
                break;
            }
        }
        return productPrograms;
    }
	
	public static List<ProductProgram> getUserLocationPrograms() {
		return getLocationPrograms(getUserLocation());
	}
	
	public static String join(String separator, List<String> input) {
		
		if (input == null || input.size() <= 0)
			return "";
		
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < input.size(); i++) {
			
			sb.append(input.get(i));
			
			// if not the last item
			if (i != input.size() - 1) {
				sb.append(separator);
			}
			
		}
		
		return sb.toString();
		
	}
	
	public static ProductOperationAttribute getAttribute(ProductOperation operation, String productOperationAttributeTypeUuid) {
		for (ProductOperationAttribute attribute : operation.getAttributes()) {
			if (attribute.getOperationAttributeType().getUuid().equals(productOperationAttributeTypeUuid)) {
				return attribute;
			}
		}
		return null;
	}
	
	public static Integer getConceptIdInGlobalProperties(String property) {
		String value = Context.getAdministrationService().getGlobalProperty("supply.dispensation" + property + "Concept");
		if (!value.isEmpty()) {
			return Integer.parseInt(value);
		}
		return null;
	}
	
	public static Concept getConceptInGlobalProperties(String property) {
		String value = Context.getAdministrationService().getGlobalProperty("supply.dispensation" + property + "Concept");
		if (!value.isEmpty()) {
			Integer conceptId = Integer.parseInt(value);
			return Context.getConceptService().getConcept(conceptId);
		}
		return null;
	}
	
	public static Obs getObs(String property, Location location, Patient patient) {
		Obs obs = new Obs();
		obs.setConcept(getConceptInGlobalProperties(property));
		obs.setLocation(location);
		obs.setPerson(patient);
		return obs;
	}
	
	public static Set<Obs> getDispensationObsList(ProductDispensation dispensationInfo, Patient patient) {
        Set<Obs> obsSet = new HashSet<>();
        if (dispensationInfo.getProductRegime() != null) {
            Obs obsRegimen = getObs("Regimen", dispensationInfo.getLocation(), patient);
            obsRegimen.setValueCoded(dispensationInfo.getProductRegime().getConcept());
            obsRegimen.setObsDatetime(dispensationInfo.getOperationDate());
            obsSet.add(obsRegimen);
        }

        Obs obsGoal = getObs("Goal", dispensationInfo.getLocation(), patient);
        obsGoal.setValueText(dispensationInfo.getGoal());
        obsGoal.setObsDatetime(dispensationInfo.getOperationDate());
        obsSet.add(obsGoal);

        Obs obsTreatmentDays = getObs("TreatmentDays", dispensationInfo.getLocation(), patient);
        obsTreatmentDays.setValueNumeric(dispensationInfo.getTreatmentDuration().doubleValue());
        obsTreatmentDays.setObsDatetime(dispensationInfo.getOperationDate());
        obsSet.add(obsTreatmentDays);

        Obs obsDispensationDate = getObs("DispensationDate", dispensationInfo.getLocation(), patient);
        obsDispensationDate.setValueDate(dispensationInfo.getOperationDate());
        obsDispensationDate.setObsDatetime(dispensationInfo.getOperationDate());
        obsSet.add(obsDispensationDate);

        if (dispensationInfo.getRegimeLine() != null) {
            Obs obsRegimenLine = getObs("RegimenLine", dispensationInfo.getLocation(), patient);
            obsRegimenLine.setValueNumeric(dispensationInfo.getRegimeLine().doubleValue());
            obsRegimenLine.setObsDatetime(dispensationInfo.getOperationDate());
            obsSet.add(obsRegimenLine);
        }

        if (dispensationInfo.getTreatmentEndDate() != null) {
            Obs obsTreatmentEndDate = getObs("TreatmentEndDate", dispensationInfo.getLocation(), patient);
            obsTreatmentEndDate.setValueDate(dispensationInfo.getTreatmentEndDate());
            obsTreatmentEndDate.setObsDatetime(dispensationInfo.getOperationDate());
            obsSet.add(obsTreatmentEndDate);
        }

        return obsSet;
    }
	
	public static Date getPatientDeathDate(Patient patient) {
		if (patient.getPerson().getDeathDate() != null) {
			return patient.getPerson().getDeathDate();
		}
		return Context.getService(SupplyService.class).deathDate(patient, SupplyUtils.getUserLocation());
	}
	
	public static Date getPatientTransferDate(Patient patient) {
		return Context.getService(SupplyService.class).transferDate(patient, SupplyUtils.getUserLocation());
	}
}
