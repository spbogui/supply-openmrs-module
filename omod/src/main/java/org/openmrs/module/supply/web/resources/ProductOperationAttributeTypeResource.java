package org.openmrs.module.supply.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.ProductOperationAttributeType;
import org.openmrs.module.supply.api.ProductOperationService;
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

@Resource(name = RestConstants.VERSION_1 + SupplyResourceController.SUPPLY_REST_NAMESPACE + "OperationAttributeType", supportedClass = ProductOperationAttributeType.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" })
public class ProductOperationAttributeTypeResource extends DelegatingCrudResource<ProductOperationAttributeType> {
	
	ProductOperationService getService() {
		return Context.getService(ProductOperationService.class);
	}
	
	@Override
	public ProductOperationAttributeType getByUniqueId(String s) {
		return getService().getOperationAttributeType(s);
	}
	
	@Override
	protected void delete(ProductOperationAttributeType operationType, String s, RequestContext requestContext)
	        throws ResponseException {
	}
	
	@Override
	public ProductOperationAttributeType newDelegate() {
		return new ProductOperationAttributeType();
	}
	
	@Override
	public ProductOperationAttributeType save(ProductOperationAttributeType operationType) {
		return getService().saveOperationAttributeType(operationType);
	}
	
	@Override
	public void purge(ProductOperationAttributeType operationType, RequestContext requestContext) throws ResponseException {
		getService().purgeOperationAttributeType(operationType);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("format");
			description.addProperty("foreignKey");
			description.addProperty("searchable");
			description.addProperty("uuid");
		} else if (representation instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("name");
			description.addProperty("format");
			description.addProperty("foreignKey");
			description.addProperty("searchable");
			description.addProperty("uuid");
		} else if (representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("uuid");
		}
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("name");
		description.addProperty("description");
		description.addRequiredProperty("format");
		description.addProperty("foreignKey");
		description.addProperty("searchable");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("name");
		description.addProperty("description");
		description.addProperty("format");
		description.addProperty("foreignKey");
		description.addProperty("searchable");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<ProductOperationAttributeType> units = getService().getAllOperationAttributeType();
		return new NeedsPaging<ProductOperationAttributeType>(units, context);
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("name", new StringProperty()).property("description", new StringProperty())
		        .property("format", new StringProperty()).property("foreignKey", new StringProperty())
		        .property("searchable", new BooleanProperty()).property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("name", new StringProperty()).property("format", new StringProperty())
		        .property("description", new StringProperty()).property("foreignKey", new StringProperty())
		        .property("searchable", new BooleanProperty()).property("uuid", new StringProperty());
		model.required("name").required("format").required("searchable");
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("name", new StringProperty()).property("format", new StringProperty())
		        .property("description", new StringProperty()).property("foreignKey", new StringProperty())
		        .property("searchable", new BooleanProperty());
		return model;
	}
}
