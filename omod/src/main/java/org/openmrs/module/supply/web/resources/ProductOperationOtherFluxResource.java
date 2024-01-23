package org.openmrs.module.supply.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.ProductOperation;
import org.openmrs.module.supply.ProductOperationOtherFlux;
import org.openmrs.module.supply.api.ProductOperationService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
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
import java.util.List;

@SubResource(parent = ProductOperationResource.class, path = "otherFlux", supportedClass = ProductOperationOtherFlux.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" })
public class ProductOperationOtherFluxResource extends DelegatingSubResource<ProductOperationOtherFlux, ProductOperation, ProductOperationResource> {
	
	ProductOperationService getService() {
		return Context.getService(ProductOperationService.class);
	}
	
	@Override
	public ProductOperationOtherFlux getByUniqueId(String s) {
		return getService().getProductOperationOtherFlux(s);
	}
	
	@Override
	protected void delete(ProductOperationOtherFlux productOperationOtherFlux, String s, RequestContext requestContext)
	        throws ResponseException {
	}
	
	@Override
	public void purge(ProductOperationOtherFlux productOperationOtherFlux, RequestContext requestContext)
	        throws ResponseException {
	}
	
	@Override
	public ProductOperationOtherFlux newDelegate() {
		return new ProductOperationOtherFlux();
	}
	
	@Override
	public ProductOperationOtherFlux save(ProductOperationOtherFlux productOperationOtherFlux) {
		return getService().saveProductOperationOtherFlux(productOperationOtherFlux);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			//			description.addProperty("productOperation", Representation.REF);
			description.addProperty("productCode", Representation.REF);
			//			description.addProperty("productAttribute", Representation.REF);
			description.addProperty("quantity");
			description.addProperty("label");
			description.addProperty("location", Representation.REF);
			description.addProperty("uuid");
		} else if (representation instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("productCode", Representation.REF);
			//			description.addProperty("productAttribute", Representation.REF);
			description.addProperty("quantity");
			description.addProperty("label");
			description.addProperty("uuid");
		} else if (representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("quantity");
			description.addProperty("label");
			description.addProperty("uuid");
		}
		return description;
	}
	
	@PropertyGetter("display")
	public String getDisplayString(ProductOperationOtherFlux flux) {
		if (flux.getOperation() == null)
			return "";
		
		return flux.getLabel() + " - " + flux.getProductCode().getProduct().getDispensationName() + " : "
		        + flux.getQuantity();
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("productCode", new RefProperty("#/definitions/ProductCodeGet"))
		        //                .property("productAttribute", new RefProperty("#/definitions/ProductAttributeGet"))
		        .property("quantity", new DoubleProperty()).property("label", new StringProperty())
		        .property("location", new RefProperty("#/definitions/LocationGet")).property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		//        description.addProperty("productAttribute");
		description.addRequiredProperty("productCode");
		description.addRequiredProperty("quantity");
		description.addRequiredProperty("label");
		description.addRequiredProperty("location");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("quantity", new DoubleProperty()).property("label", new StringProperty())
		        .property("uuid", new StringProperty()).required("quantity").required("label").required("location");
		if (rep instanceof FullRepresentation) {
			model.property("location", new RefProperty("#/definitions/LocationGet"))
			//                    .property("productAttribute", new RefProperty("#/definitions/ProductAttributeGet"))
			        .property("productCode", new RefProperty("#/definitions/ProductCodeGet"));
		} else {
			model.property("productCode", new StringProperty().example("uuid")).property("location",
			    new StringProperty().example("uuid"));
		}
		model.required("quantity").required("label").required("productCode").required("location");
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("productCode");
		description.addProperty("quantity");
		description.addProperty("label");
		description.addProperty("location");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("productCode", new StringProperty().example("uuid"))
		        //                .property("productAttribute", new StringProperty().example("uuid"))
		        .property("location", new StringProperty().example("uuid")).property("quantity", new DoubleProperty())
		        .property("label", new StringProperty()).property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public ProductOperation getParent(ProductOperationOtherFlux productOperationOtherFlux) {
		return productOperationOtherFlux.getOperation();
	}
	
	@Override
	public void setParent(ProductOperationOtherFlux productOperationOtherFlux, ProductOperation productOperation) {
		productOperationOtherFlux.setOperation(productOperation);
	}
	
	@Override
	public PageableResult doGetAll(ProductOperation productOperation, RequestContext requestContext)
	        throws ResponseException {
		List<ProductOperationOtherFlux> otherFluxes = new ArrayList<ProductOperationOtherFlux>(
		        productOperation.getOtherFluxes());
		return new NeedsPaging<ProductOperationOtherFlux>(otherFluxes, requestContext);
	}
}
