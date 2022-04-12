package org.openmrs.module.supply.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.ProductUnit;
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

import java.util.List;

@Resource(name = RestConstants.VERSION_1 + SupplyResourceController.SUPPLY_REST_NAMESPACE + "Unit", supportedClass = ProductUnit.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" })
public class ProductUnitResource extends DelegatingCrudResource<ProductUnit> {
	
	ProductService getService() {
		return Context.getService(ProductService.class);
	}
	
	@Override
	public ProductUnit getByUniqueId(String s) {
		return getService().getProductUnit(s);
	}
	
	@Override
	protected void delete(ProductUnit productUnit, String s, RequestContext requestContext) throws ResponseException {
		
	}
	
	@Override
	public ProductUnit newDelegate() {
		return new ProductUnit();
	}
	
	@Override
	public ProductUnit save(ProductUnit productUnit) {
		return getService().saveProductUnit(productUnit);
	}
	
	@Override
	public void purge(ProductUnit productUnit, RequestContext requestContext) throws ResponseException {
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("uuid");
		} else if (representation instanceof DefaultRepresentation || representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("name");
			description.addProperty("uuid");
		}
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("name", new StringProperty()).property("description", new StringProperty())
		        .property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("name");
		description.addProperty("description");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("name", new StringProperty()).property("description", new StringProperty())
		        .property("uuid", new StringProperty());
		model.required("name");
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("name");
		description.addProperty("description");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("name", new StringProperty()).property("description", new StringProperty());
		return model;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<ProductUnit> productUnits = getService().getAllProductUnits();
		return new NeedsPaging<ProductUnit>(productUnits, context);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		return super.doSearch(context);
	}
}
