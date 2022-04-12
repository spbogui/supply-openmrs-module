package org.openmrs.module.supply.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.*;
import org.openmrs.module.supply.api.ProductOperationService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@SubResource(parent = ProductOperationResource.class, path = "flux", supportedClass = ProductOperationFlux.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" })
public class ProductOperationFluxResource extends DelegatingSubResource<ProductOperationFlux, ProductOperation, ProductOperationResource> {
	
	ProductOperationService getService() {
		return Context.getService(ProductOperationService.class);
	}
	
	@Override
	public ProductOperationFlux getByUniqueId(String s) {
		return getService().getProductOperationFlux(s);
	}
	
	@Override
	protected void delete(ProductOperationFlux ProductOperationFlux, String s, RequestContext requestContext)
	        throws ResponseException {
	}
	
	@Override
	public void purge(ProductOperationFlux productOperationFlux, RequestContext requestContext) throws ResponseException {
		getService().purgeProductOperationFlux(productOperationFlux);
	}
	
	@Override
	public ProductOperationFlux newDelegate() {
		return new ProductOperationFlux();
	}
	
	@Override
	public ProductOperationFlux save(ProductOperationFlux ProductOperationFlux) {
		return getService().saveProductOperationFlux(ProductOperationFlux);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("quantity");
			description.addProperty("relatedQuantity");
			description.addProperty("relatedQuantityLabel");
			description.addProperty("product", Representation.DEFAULT);
			description.addProperty("location", Representation.REF);
			description.addProperty("observation");
			description.addProperty("attributes", Representation.DEFAULT);
			description.addProperty("auditInfo");
			description.addProperty("uuid");
			description.addProperty("voided");
		} else if (representation instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("attributes", Representation.REF);
			description.addProperty("product", Representation.REF);
			description.addProperty("quantity");
			description.addProperty("relatedQuantity");
			description.addProperty("relatedQuantityLabel");
			description.addProperty("display");
			description.addProperty("observation");
			description.addProperty("uuid");
		} else if (representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("relatedQuantity");
			description.addProperty("relatedQuantityLabel");
			description.addProperty("quantity");
			description.addProperty("product");
			description.addProperty("uuid");
		}
		return description;
	}
	
	@PropertyGetter("attributes")
	public static Set<ProductOperationFluxAttribute> getAttributes(ProductOperationFlux flux) {
		return new LinkedHashSet<ProductOperationFluxAttribute>(flux.getAttributes());
	}
	
	@PropertySetter("attributes")
	public static void setAttributes(ProductOperationFlux flux, List<ProductOperationFluxAttribute> attributes)
	        throws ResourceDoesNotSupportOperationException {
		if (flux.getAttributes() != null && flux.getAttributes().containsAll(attributes)) {
			return;
		}
		
		//		if (flux.getAttributes() != null && !flux.getAttributes().isEmpty()) {
		//			throw new ResourceDoesNotSupportOperationException(
		//			        "Operation flux attributes can only be set for newly created objects !");
		//		}
		
		for (ProductOperationFluxAttribute attribute : attributes) {
			ProductOperationFluxAttribute existingAttribute = flux.getAttributes() != null ? getMatchingAttribute(attribute,
			    flux.getAttributes()) : null;
			if (existingAttribute != null) {
				copyAttributeFields(existingAttribute, attribute);
			} else {
				flux.addAttribute(attribute);
			}
		}
	}
	
	private static void copyAttributeFields(ProductOperationFluxAttribute existingAttribute,
	        ProductOperationFluxAttribute attribute) {
		existingAttribute.setAttribute(attribute.getAttribute());
		existingAttribute.setQuantity(attribute.getQuantity());
	}
	
	private static ProductOperationFluxAttribute getMatchingAttribute(ProductOperationFluxAttribute attribute,
	        Set<ProductOperationFluxAttribute> attributes) {
		for (ProductOperationFluxAttribute existingAttribute : attributes) {
			if (existingAttribute.getUuid() != null && existingAttribute.getUuid().equals(attribute.getUuid())) {
				return existingAttribute;
			}
		}
		return null;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("product", new RefProperty("#/definitions/ProductGet"))
		        .property("attributes", new RefProperty("#/definitions/ProductAttributeGet"))
		        .property("quantity", new IntegerProperty()).property("relatedQuantity", new DoubleProperty())
		        .property("relatedQuantityLabel", new StringProperty()).property("display", new StringProperty())
		        .property("observation", new StringProperty())
		        .property("location", new RefProperty("#/definitions/LocationGet")).property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("product");
		description.addRequiredProperty("quantity");
		description.addRequiredProperty("location");
		description.addProperty("attributes");
		description.addProperty("relatedQuantity");
		description.addProperty("relatedQuantityLabel");
		description.addProperty("observation");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("product", new StringProperty().example("uuid"))
		        .property("location", new StringProperty().example("uuid")).property("quantity", new IntegerProperty())
		        .property("relatedQuantity", new DoubleProperty()).property("relatedQuantityLabel", new StringProperty())
		        .property("observation", new StringProperty()).property("uuid", new StringProperty())
		        // .required("attributes")
		        .required("quantity").required("location");
		
		if (rep instanceof FullRepresentation) {
			model.property("location", new RefProperty("#/definitions/LocationCreate"))
			        .property("attributes", new RefProperty("#/definitions/ProductOperationFluxAttributeCreate"))
			        .property("product", new RefProperty("#/definitions/ProductGet"));
		}
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("product");
		description.addProperty("attributes");
		description.addProperty("quantity");
		description.addProperty("relatedQuantity");
		description.addProperty("relatedQuantityLabel");
		description.addProperty("location");
		description.addProperty("observation");
		return description;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("attributes", new RefProperty("#/definitions/ProductAttributeGet"))
		        .property("quantity", new IntegerProperty()).property("relatedQuantity", new DoubleProperty())
		        .property("relatedQuantityLabel", new StringProperty()).property("observation", new StringProperty())
		        .property("location", new RefProperty("#/definitions/LocationGet")).property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public ProductOperation getParent(ProductOperationFlux ProductOperationFlux) {
		return ProductOperationFlux.getOperation();
	}
	
	@Override
	public void setParent(ProductOperationFlux ProductOperationFlux, ProductOperation productOperation) {
		ProductOperationFlux.setOperation(productOperation);
	}
	
	@Override
	public PageableResult doGetAll(ProductOperation productOperation, RequestContext requestContext)
	        throws ResponseException {
		List<ProductOperationFlux> fluxes = new ArrayList<ProductOperationFlux>(productOperation.getFluxes());
		return new NeedsPaging<ProductOperationFlux>(fluxes, requestContext);
	}
	
	@PropertyGetter("display")
	public String getDisplayString(ProductOperationFlux flux) {
		if (flux.getAttributes() == null)
			return "";
		
		return null;
	}
}
