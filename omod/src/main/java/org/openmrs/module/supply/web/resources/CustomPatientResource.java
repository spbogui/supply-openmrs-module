package org.openmrs.module.supply.web.resources;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.api.SupplyService;
import org.openmrs.module.supply.utils.SupplyUtils;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientResource1_8;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/patient", supportedClass = Patient.class, supportedOpenmrsVersions = { "1.8.*",
        "1.9.*", "1.11.*", "1.12.*", "2.*" }, order = 4)
public class CustomPatientResource extends PatientResource1_8 {
	
	//	@PropertyGetter("deathDate")
	//	private static Date getDeathDate(Patient patient) {
	//		if (patient.getPerson().getDeathDate() != null) {
	//			return patient.getPerson().getDeathDate();
	//		} else {
	//			SupplyUtils.getPatientDeathDate(patient);
	//		}
	//		return null;
	//	}
	//
	//	//    @PropertyGetter("transferDate")
	//	//    private static Date getTransferDate(Patient patient) {
	//	//        return SupplyUtils.getPatientTransferDate(patient);
	//	//    }
	//
	//	@Override
	//	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
	//		DelegatingResourceDescription description = super.getRepresentationDescription(rep);
	//		//		description.addProperty("transferDate");
	//		description.addProperty("deathDate");
	//		return description;
	//	}
	
	SupplyService getService() {
		return Context.getService(SupplyService.class);
	}
	
	@Override
	protected AlreadyPaged<Patient> doSearch(RequestContext context) {
		
		List<Patient> patients = new ArrayList<Patient>();
		String identifier = context.getRequest().getParameter("identification");
		String filter = context.getRequest().getParameter("filter");
		
		if (StringUtils.isNotBlank(identifier) && StringUtils.isNotEmpty(identifier)) {
			Patient patient = getService().getPatientByIdentifier(identifier);
			if (patient != null) {
				if (StringUtils.isNotBlank(filter) && StringUtils.isNotEmpty(filter)) {
					if (filter.equals("dead")) {
						if (getService().isDead(patient, SupplyUtils.getUserLocation())) {
							patients.add(patient);
						}
					} else if (filter.equals("transferred")) {
						if (getService().isTransferred(patient, SupplyUtils.getUserLocation())) {
							patients.add(patient);
						}
					}
				} else {
					patients.add(patient);
				}
			}
			return new AlreadyPaged<Patient>(context, patients, false, (long) patients.size());
		}
		
		return super.doSearch(context);
	}
	
}
