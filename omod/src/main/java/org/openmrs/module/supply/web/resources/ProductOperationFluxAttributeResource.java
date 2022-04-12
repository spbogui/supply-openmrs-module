package org.openmrs.module.supply.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.ProductAttribute;
import org.openmrs.module.supply.ProductOperationFlux;
import org.openmrs.module.supply.ProductOperationFluxAttribute;
import org.openmrs.module.supply.api.ProductOperationService;
import org.openmrs.module.supply.api.ProductService;
import org.openmrs.module.supply.utils.SupplyUtils;
import org.openmrs.module.supply.web.controller.SupplyResourceController;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
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

@Resource(name = RestConstants.VERSION_1 + SupplyResourceController.SUPPLY_REST_NAMESPACE + "OperationFluxAttribute", supportedClass = ProductOperationFluxAttribute.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" })
public class ProductOperationFluxAttributeResource extends DelegatingCrudResource<ProductOperationFluxAttribute> {
	
	ProductOperationService getService() {
		return Context.getService(ProductOperationService.class);
	}
	
	@Override
	public ProductOperationFluxAttribute getByUniqueId(String s) {
		return getService().getOperationFluxAttribute(s);
	}
	
	@Override
	protected void delete(ProductOperationFluxAttribute productOperationFluxAttribute, String s,
	        RequestContext requestContext) throws ResponseException {
		
	}
	
	@Override
	public ProductOperationFluxAttribute newDelegate() {
		return new ProductOperationFluxAttribute();
	}
	
	@Override
	public ProductOperationFluxAttribute save(ProductOperationFluxAttribute productOperationFluxAttribute) {
		return getService().saveOperationFluxAttribute(productOperationFluxAttribute);
	}
	
	@Override
	public void purge(ProductOperationFluxAttribute productOperationFluxAttribute, RequestContext requestContext)
	        throws ResponseException {
		getService().purgeProductOperationFluxAttribute(productOperationFluxAttribute);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("quantity");
			description.addProperty("attribute", Representation.DEFAULT);
			description.addProperty("operationFlux", Representation.DEFAULT);
			description.addProperty("location", Representation.DEFAULT);
			description.addProperty("uuid");
		} else if (representation instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("quantity");
			description.addProperty("attribute", Representation.DEFAULT);
			description.addProperty("operationFlux", Representation.DEFAULT);
			description.addProperty("location", Representation.DEFAULT);
			description.addProperty("uuid");
		} else if (representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("quantity");
			description.addProperty("attribute", Representation.DEFAULT);
			//			description.addProperty("location", Representation.REF);
			description.addProperty("uuid");
		}
		return description;
	}
	
	@PropertyGetter("attribute")
	public static ProductAttribute getAttribute(ProductOperationFluxAttribute instance) {
		return instance.getAttribute();
	}
	
	@PropertySetter("attribute")
	public static void setAttribute(ProductOperationFluxAttribute instance, ProductAttribute attribute) {
		ProductAttribute productAttribute = Context.getService(ProductService.class).getProductAttributeByBatchNumber(
		    attribute.getBatchNumber(), instance.getLocation());
		if (productAttribute != null) {
			attribute.setProductAttributeId(productAttribute.getProductAttributeId());
			attribute.setUuid(productAttribute.getUuid());
		}
		instance.setAttribute(attribute);
	}
	
	@PropertyGetter("operationFlux")
	public static ProductOperationFlux getOperationFlux(ProductOperationFluxAttribute instance) {
		return instance.getOperationFlux();
	}
	
	@PropertySetter("operationFlux")
	public static void setOperationFlux(ProductOperationFluxAttribute instance, ProductOperationFlux operationFlux) {
		instance.setOperationFlux(operationFlux);
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<ProductOperationFluxAttribute> fluxAttributes = getService().getAllProductOperationFluxAttributes(
		    SupplyUtils.getUserLocation(), false);
		return new NeedsPaging<ProductOperationFluxAttribute>(fluxAttributes, context);
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("attribute", new RefProperty("#/definitions/ProductAttributeGet"))
		        .property("quantity", new IntegerProperty()).property("operationFlux", new StringProperty())
		        .property("location", new StringProperty()).property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("quantity");
		description.addRequiredProperty("attribute");
		description.addRequiredProperty("operationFlux");
		description.addRequiredProperty("location");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("attribute", new RefProperty("#/definitions/ProductAttributeCreate"))
		        .property("quantity", new IntegerProperty()).property("operationFlux", new StringProperty())
		        .property("location", new StringProperty()).property("uuid", new StringProperty()).required("attribute")
		        .required("quantity").required("operationFlux").required("location");
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("quantity");
		description.addProperty("attribute");
		description.addProperty("operationFlux");
		description.addProperty("location");
		return description;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("attribute", new RefProperty("#/definitions/ProductAttributeCreate"))
		        .property("quantity", new IntegerProperty()).property("operationFlux", new StringProperty())
		        .property("location", new StringProperty());
		return model;
	}
}
