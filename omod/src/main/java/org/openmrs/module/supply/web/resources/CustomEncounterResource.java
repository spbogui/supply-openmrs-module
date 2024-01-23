package org.openmrs.module.supply.web.resources;

import org.apache.commons.lang.StringUtils;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.api.SupplyService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.EncounterResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.ObsResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.EncounterResource1_9;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/encounter", supportedClass = Encounter.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" }, order = 5)
public class CustomEncounterResource extends EncounterResource1_9 {
	
	SupplyService getService() {
		return Context.getService(SupplyService.class);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String filter = context.getRequest().getParameter("filter");
		String endDateString = context.getRequest().getParameter("endDate");
		
		if (StringUtils.isNotBlank(filter)) {
			
			List<Encounter> encounters = new ArrayList<Encounter>();
			String[] filters = filter.split(",");
			Patient patient = null;
			EncounterType encounterType = null;
			
			for (String f : filters) {
				if (f.contains("patient")) {
					String[] split = f.split(":");
					patient = Context.getPatientService().getPatientByUuid(split[1]);
				} else if (f.contains("encounterType")) {
					String[] split = f.split(":");
					encounterType = Context.getEncounterService().getEncounterTypeByUuid(split[1]);
				}
			}
			Date endDate = null;
			if (StringUtils.isNotBlank(endDateString)) {
				DateFormat sourceFormat = new SimpleDateFormat("dd-MM-yyyy");
				try {
					endDate = sourceFormat.parse(endDateString);
				}
				catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
			if (filter.contains("last")) {
				if (patient != null && encounterType != null && endDate == null) {
					Encounter encounter = getService().getPatientLastEncounter(patient, encounterType);
					if (encounter != null) {
						encounters.add(encounter);
					}
				} else if (patient != null && encounterType != null) {
					Encounter encounter = getService().getPatientLastEncounter(patient, encounterType, endDate);
					if (encounter != null) {
						encounters.add(encounter);
					}
				}
			}
			return new AlreadyPaged<Encounter>(context, encounters, false, (long) encounters.size());
		}
		
		return super.doSearch(context);
	}
	
}
