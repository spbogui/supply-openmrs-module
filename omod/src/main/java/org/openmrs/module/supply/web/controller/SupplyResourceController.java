package org.openmrs.module.supply.web.controller;

import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("${rootrootArtifactid}.SupplyResourceController")
@RequestMapping(value = "rest/" + RestConstants.VERSION_1 + SupplyResourceController.SUPPLY_REST_NAMESPACE + "Supply")
public class SupplyResourceController extends MainResourceController {
	
	public static final String SUPPLY_REST_NAMESPACE = "/product";
	
	@Override
	public String getNamespace() {
		return RestConstants.VERSION_1 + SUPPLY_REST_NAMESPACE;
	}
}
