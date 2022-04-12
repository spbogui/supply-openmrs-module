package org.openmrs.module.supply.web.resources;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.api.SupplyService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.LocationResource1_8;

import java.util.ArrayList;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/location", supportedClass = Location.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" }, order = 2)
public class CustomLocationResource extends LocationResource1_8 {
	
	SupplyService getService() {
		return Context.getService(SupplyService.class);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		
		String filter = context.getRequest().getParameter("filter");
		
		if (StringUtils.isNotBlank(filter) && StringUtils.isNotEmpty(filter)) {
			if (filter.equals("client")) {
				return new NeedsPaging<Location>(getService().getDirectClientLocations(), context);
			} else if (filter.contains("tags")) {
				String[] tags = filter.split(":")[1].split(",");
				List<Location> locations = new ArrayList<Location>();
				for (String tagUuid : tags) {
					LocationTag locationTag = Context.getLocationService().getLocationTagByUuid(tagUuid);
					if (locationTag != null) {
						List<Location> locationsByTag = Context.getLocationService().getLocationsByTag(locationTag);
						if (locationsByTag != null) {
							locations.addAll(locationsByTag);
						}
					}
				}
				return new NeedsPaging<Location>(locations, context);
			}
		}
		return super.doSearch(context);
	}
	
}
