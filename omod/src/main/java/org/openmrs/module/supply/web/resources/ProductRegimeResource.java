package org.openmrs.module.supply.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.ProductRegime;
import org.openmrs.module.supply.api.ProductService;
import org.openmrs.module.supply.web.controller.SupplyResourceController;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + SupplyResourceController.SUPPLY_REST_NAMESPACE + "Regime", supportedClass = ProductRegime.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" })
public class ProductRegimeResource extends DelegatingCrudResource<ProductRegime> {
	
	ProductService getService() {
		return Context.getService(ProductService.class);
	}
	
	@Override
	public ProductRegime getByUniqueId(String s) {
		return getService().getProductRegime(s);
	}
	
	@Override
	protected void delete(ProductRegime productRegime, String s, RequestContext requestContext) throws ResponseException {
		
	}
	
	@Override
	public ProductRegime newDelegate() {
		return new ProductRegime();
	}
	
	@Override
	public ProductRegime save(ProductRegime productRegime) {
		return getService().saveProductRegime(productRegime);
	}
	
	@Override
	public void purge(ProductRegime productRegime, RequestContext requestContext) throws ResponseException {
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("concept", Representation.DEFAULT);
			description.addProperty("uuid");
			//			description.addProperty("products", Representation.DEFAULT);
		} else if (representation instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("concept", Representation.DEFAULT);
			description.addProperty("uuid");
		} else if (representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("concept", Representation.REF);
			description.addProperty("uuid");
		}
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("concept", new RefProperty("#/definitions/ConceptGet"))
		//		        .property("products", new RefProperty("#/definitions/ProductGet"))
		        .property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("concept");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("concept", new RefProperty("#/definitions/ConceptCreate")).property("uuid", new StringProperty());
		model.required("concept");
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("concept");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("concept", new RefProperty("#/definitions/ConceptUpdate"));
		return model;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<ProductRegime> Regime = getService().getAllProductRegimes();
		return new NeedsPaging<ProductRegime>(Regime, context);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String name = context.getParameter("name");
		
		List<ProductRegime> productRegimes = new ArrayList<ProductRegime>();
		if (StringUtils.isNotBlank(name) && StringUtils.isNotEmpty(name)) {
			ProductRegime regime = getService().getProductRegimeByConceptName(name);
			if (regime != null) {
				productRegimes.add(regime);
			}
		}
		
		return new NeedsPaging<ProductRegime>(productRegimes, context);
	}
}
