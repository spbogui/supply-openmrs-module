package org.openmrs.module.supply.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.ProductOperationType;
import org.openmrs.module.supply.api.ProductOperationService;
import org.openmrs.module.supply.enumerations.Incidence;
import org.openmrs.module.supply.web.controller.SupplyResourceController;
import org.openmrs.module.webservices.docs.swagger.core.property.EnumProperty;
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

import java.util.Arrays;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + SupplyResourceController.SUPPLY_REST_NAMESPACE + "OperationType", supportedClass = ProductOperationType.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" })
public class ProductOperationTypeResource extends DelegatingCrudResource<ProductOperationType> {
	
	ProductOperationService getService() {
		return Context.getService(ProductOperationService.class);
	}
	
	@Override
	public ProductOperationType getByUniqueId(String s) {
		return getService().getProductOperationType(s);
	}
	
	@Override
	protected void delete(ProductOperationType operationType, String s, RequestContext requestContext)
	        throws ResponseException {
		
	}
	
	@Override
	public ProductOperationType newDelegate() {
		return new ProductOperationType();
	}
	
	@Override
	public ProductOperationType save(ProductOperationType operationType) {
		return getService().saveProductOperationType(operationType);
	}
	
	@Override
	public void purge(ProductOperationType operationType, RequestContext requestContext) throws ResponseException {
		getService().purgeProductOperationType(operationType);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("name");
			description.addProperty("defaultIncidence");
			description.addProperty("description");
			description.addProperty("operationAttributeTypes");
			description.addProperty("uuid");
		} else if (representation instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("name");
			description.addProperty("defaultIncidence");
			description.addProperty("description");
			description.addProperty("uuid");
		} else if (representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("name");
			description.addProperty("defaultIncidence");
			description.addProperty("uuid");
		}
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("name", new StringProperty())
		        .property("description", new StringProperty())
		        .property("operationAttributeTypes", new RefProperty("#/definitions/ProductOperationAttributeTypeGet"))
		        .property(
		            "defaultIncidence",
		            new EnumProperty(Incidence.class)._enum(Arrays.asList(Incidence.EQUAL.name(), Incidence.NONE.name(),
		                Incidence.POSITIVE.name(), Incidence.NEGATIVE.name()))).property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("name");
		description.addRequiredProperty("defaultIncidence");
		description.addProperty("description");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("name", new StringProperty())
		        .property("description", new StringProperty())
		        .property(
		            "defaultIncidence",
		            new EnumProperty(Incidence.class)._enum(Arrays.asList(Incidence.EQUAL.name(), Incidence.NONE.name(),
		                Incidence.POSITIVE.name(), Incidence.NEGATIVE.name()))).property("uuid", new StringProperty());
		
		model.required("name").required("uuid").required("defaultIncidence");
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("name");
		description.addRequiredProperty("defaultIncidence");
		description.addProperty("description");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("name", new StringProperty())
		        .property("description", new StringProperty())
		        .property(
		            "defaultIncidence",
		            new EnumProperty(Incidence.class)._enum(Arrays.asList(Incidence.EQUAL.name(), Incidence.NONE.name(),
		                Incidence.POSITIVE.name(), Incidence.NEGATIVE.name())));
		return model;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<ProductOperationType> units = getService().getAllProductOperationType();
		return new NeedsPaging<ProductOperationType>(units, context);
	}
}
