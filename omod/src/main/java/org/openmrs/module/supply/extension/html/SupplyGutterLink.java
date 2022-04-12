package org.openmrs.module.supply.extension.html;

import org.openmrs.api.context.Context;
import org.openmrs.module.web.extension.LinkExt;

public class SupplyGutterLink extends LinkExt {
	
	@Override
	public String getLabel() {
		return Context.getMessageSourceService().getMessage("Pharmacie");
	}
	
	@Override
	public String getUrl() {
		return "module/supply/supply.form#/supply";
	}
	
	@Override
	public String getRequiredPrivilege() {
		return "Manage Pharmacy";
	}
}
