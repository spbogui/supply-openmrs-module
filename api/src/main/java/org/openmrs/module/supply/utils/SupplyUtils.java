package org.openmrs.module.supply.utils;

import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.ProductDispensation;
import org.openmrs.module.supply.ProductOperation;
import org.openmrs.module.supply.ProductOperationAttribute;
import org.openmrs.module.supply.ProductProgram;
import org.openmrs.module.supply.api.ProductService;
import org.openmrs.module.supply.api.SupplyService;

import java.text.DecimalFormat;
import java.util.*;

public class SupplyUtils {
	
	public static final DecimalFormat df = new DecimalFormat("0.00");
	
	public static Location getUserLocation() {
		if (Context.getUserContext().getLocation() != null) {
			return Context.getUserContext().getLocation();
		}
		return Context.getLocationService().getDefaultLocation();
	}
	
	public static String generateRandom(Integer targetStringLength) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
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
		
		if (input == null || input.size() == 0)
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
	
	public static Boolean isDirectClient(Location location) {
		for (LocationAttribute attribute : location.getActiveAttributes()) {
			if (attribute.getAttributeType().getName().equals("Client Direct NPSP")) {
				if (attribute.getValue().equals(true)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static String getLocationType(Location location) {
        String locationType = "CenterAndOrganisations";
        if (isDirectClient(location)) {
            locationType = "DirectClient";
        } else {
            LocationTag tag = location.getTags().stream().filter(t -> t.getName().equals("DISTRICT SANITAIRE")).findFirst().orElse(null);
            if (location.getTags().stream().filter(t -> t.getName().equals("DISTRICT SANITAIRE")).findFirst().orElse(null) != null) {
                locationType = "District";
            } else {
                if (location.getTags().stream().filter(t -> t.getName().equals("SERVICE")).findFirst().orElse(null) != null) {
                    locationType = "PointOfServiceDelivery";
                }
            }
        }
        return locationType;
    }
	
	public static Double getStockMax() {
		return getLocationStockMax(getUserLocation());
	}
	
	public static Double getLocationStockMax(Location location) {
		String locationType = getLocationType(location);
		String stockMaxInProperty = Context.getAdministrationService().getGlobalProperty(
		    "supplyInfo.stockMax" + locationType);
		String unit = stockMaxInProperty.split(" ")[1];
		double stockMax = 0.0;
		if (unit.startsWith("M"))
			stockMax = Double.parseDouble(stockMaxInProperty.split(" ")[0]);
		else if (unit.startsWith("D")) {
			stockMax = Double.parseDouble(stockMaxInProperty.split(" ")[0]) / 30;
		} else if (unit.startsWith("W")) {
			stockMax = Double.parseDouble(stockMaxInProperty.split(" ")[0]) / 4;
		}
		
		//        System.out.println("|-----------------------------------------------------------> : " + stockMax);
		return stockMax;
	}
	
	public static Integer getMonthsForCMM(Location location) {
		if (isDirectClient(location)) {
			return getMonthsForCMMDirectClient();
		}
		return getMonthsForCMMEts();
	}
	
	public static Integer getMonthsForCMMEts() {
		String months = Context.getAdministrationService().getGlobalProperty("supplyInfo.monthsForCMM");
		return Integer.parseInt(months);
	}
	
	public static Integer getMonthsForCMMDirectClient() {
		String months = Context.getAdministrationService().getGlobalProperty("supplyInfo.monthsForCMMDirectClient");
		return Integer.parseInt(months);
	}
	
	public static Date getMinDate(List<ProductOperation> operations) {
        List<Date> dates = new ArrayList<>();
        for (ProductOperation operation : operations) {
            dates.add(operation.getOperationDate());
        }
        return Collections.min(dates);
    }
	
	public static Date getMaxDate(List<ProductOperation> operations) {
        List<Date> dates = new ArrayList<>();
        for (ProductOperation operation : operations) {
            dates.add(operation.getOperationDate());
        }
        return Collections.max(dates);
    }
	
	public static Integer getDateDiffInMonth(Date startDate, Date endDate) {
		LocalDate start = new LocalDate(startDate);
		LocalDate end = new LocalDate(endDate);
		return Months.monthsBetween(start, end).getMonths();
	}
}
