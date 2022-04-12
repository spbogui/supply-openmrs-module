package org.openmrs.module.supply.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.Product;
import org.openmrs.module.supply.ProductName;
import org.openmrs.module.supply.api.ProductService;
import org.openmrs.module.webservices.rest.web.RequestContext;
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

@SubResource(parent = ProductResource.class, path = "name", supportedClass = ProductName.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" })
public class ProductNameResource extends DelegatingSubResource<ProductName, Product, ProductResource> {
	
	ProductService getService() {
		return Context.getService(ProductService.class);
	}
	
	@Override
	public ProductName getByUniqueId(String s) {
		return getService().getProductName(s);
	}
	
	@Override
	protected void delete(ProductName ProductOperationFlux, String s, RequestContext requestContext)
	        throws ResponseException {
	}
	
	@Override
	public void purge(ProductName ProductOperationFlux, RequestContext requestContext) throws ResponseException {
	}
	
	@Override
	public ProductName newDelegate() {
		return new ProductName();
	}
	
	@Override
	public ProductName save(ProductName name) {
		return getService().saveProductName(name);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("productNameType");
			description.addProperty("name");
			description.addProperty("unit", Representation.DEFAULT);
			description.addProperty("uuid");
			description.addProperty("voided");
		} else if (representation instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("productNameType");
			description.addProperty("name");
			description.addProperty("unit", Representation.DEFAULT);
			description.addProperty("uuid");
		} else if (representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("productNameType");
			description.addProperty("name");
			description.addProperty("unit", Representation.REF);
			description.addProperty("uuid");
		}
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("unit", new RefProperty("#/definitions/ProductUnitGet"))
		        .property("productNameType", new StringProperty()).property("name", new StringProperty())
		        .property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("productNameType");
		description.addRequiredProperty("name");
		description.addRequiredProperty("unit");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("unit", new RefProperty("#/definitions/ProductUnitCreate"))
		        .property("productNameType", new StringProperty()).property("name", new StringProperty())
		        .property("uuid", new StringProperty()).required("unit").required("name").required("productNameType");
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("productNameType");
		description.addProperty("name");
		description.addProperty("unit");
		return description;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("unit", new RefProperty("#/definitions/ProductUnitCreate"))
		        .property("productNameType", new StringProperty()).property("name", new StringProperty())
		        .property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public Product getParent(ProductName name) {
		return name.getProduct();
	}
	
	@Override
	public void setParent(ProductName name, Product product) {
		name.setProduct(product);
	}
	
	@Override
	public PageableResult doGetAll(Product product, RequestContext requestContext) throws ResponseException {
		List<ProductName> names = new ArrayList<ProductName>(product.getNames());
		return new NeedsPaging<ProductName>(names, requestContext);
	}
}
