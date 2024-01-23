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
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.ObsResource1_8;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/obs", supportedClass = Obs.class, supportedOpenmrsVersions = { "1.8.*",
        "1.9.*", "1.11.*", "1.12.*", "2.*" }, order = 6)
public class CustomObsResource extends ObsResource1_8 {
	
	SupplyService getService() {
		return Context.getService(SupplyService.class);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		
		String filter = context.getRequest().getParameter("filter");
		String endDateString = context.getRequest().getParameter("endDate");
		
		if (StringUtils.isNotBlank(filter)) {
			List<Obs> obsList = new ArrayList<Obs>();
			
			String[] filters = filter.split(",");
			Person person = null;
			Concept concept = null;
			EncounterType encounterType = null;
			
			for (String f : filters) {
				if (f.contains("person")) {
					String[] split = f.split(":");
					person = Context.getPersonService().getPersonByUuid(split[1]);
				} else if (f.contains("concept")) {
					String[] split = f.split(":");
					concept = Context.getConceptService().getConceptByUuid(split[1]);
				} else if (f.contains("encounterType")) {
					String[] split = f.split(":");
					encounterType = Context.getEncounterService().getEncounterTypeByUuid(split[1]);
				}
			}
			
			if (concept == null && person == null) {
				return new AlreadyPaged<Obs>(context, obsList, false, (long) 0);
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
				if (encounterType != null) {
					if (endDate != null) {
						Obs obs = getService().getPatientLastObs(person, concept, encounterType, endDate);
						if (obs != null) {
							obsList.add(obs);
						}
					} else {
						Obs obs = getService().getPatientLastObs(person, concept, encounterType);
						if (obs != null) {
							obsList.add(obs);
						}
					}
				} else {
					if (endDate != null) {
						Obs obs = getService().getPatientLastObs(person, concept, endDate);
						if (obs != null) {
							obsList.add(obs);
						}
					} else {
						Obs obs = getService().getPatientLastObs(person, concept);
						if (obs != null) {
							obsList.add(obs);
						}
					}
				}
				//                if (endDate == null) {
				//                    if (person != null && concept != null && encounterType != null) {
				//                        Obs obs = getService().getPatientLastObs(person, concept, encounterType);
				//                        System.out
				//                                .println("----------------------------------------> in last without end date and with encounter type");
				//                        if (obs != null) {
				//                            System.out.println("----------------------------------------> in obs obtained");
				//                            obsList.add(obs);
				//                        }
				//                    } else if (person != null && concept != null) {
				//                        Obs obs = getService().getPatientLastObs(person, concept);
				//                        System.out
				//                                .println("----------------------------------------> in last without end Date and encounter type");
				//                        if (obs != null) {
				//                            System.out.println("----------------------------------------> in last obs obtained"
				//                                    + obs.getUuid());
				//                            obsList.add(obs);
				//                        }
				//                    }
				//                } else {
				//                    if (person != null && concept != null && encounterType != null) {
				//                        Obs obs = getService().getPatientLastObs(person, concept, encounterType, endDate);
				//                        if (obs != null) {
				//                            obsList.add(obs);
				//                        }
				//                    } else if (person != null && concept != null) {
				//                        Obs obs = getService().getPatientLastObs(person, concept, endDate);
				//                        if (obs != null) {
				//                            obsList.add(obs);
				//                        }
				//                    }
				//                }
			}
			return new AlreadyPaged<Obs>(context, obsList, false, (long) obsList.size());
		}
		
		return super.doSearch(context);
	}
	
}
