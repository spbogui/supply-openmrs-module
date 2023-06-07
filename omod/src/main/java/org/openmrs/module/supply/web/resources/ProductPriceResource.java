package org.openmrs.module.supply.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.context.Context;

import org.openmrs.module.supply.Product;
import org.openmrs.module.supply.ProductCode;
import org.openmrs.module.supply.ProductPrice;
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

@SubResource(parent = ProductCodeResource.class, path = "price", supportedClass = ProductPrice.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" })
public class ProductPriceResource extends DelegatingSubResource<ProductPrice, ProductCode, ProductCodeResource> {
	
	ProductService getService() {
		return Context.getService(ProductService.class);
	}
	
	@Override
	public ProductPrice getByUniqueId(String s) {
		return getService().getProductPrice(s);
	}
	
	@Override
	protected void delete(ProductPrice productPrice, String s, RequestContext requestContext) throws ResponseException {
		
	}
	
	@Override
	public ProductPrice newDelegate() {
		return new ProductPrice();
	}
	
	@Override
	public ProductPrice save(ProductPrice productPrice) {
		return getService().saveProductPrice(productPrice);
	}
	
	@Override
	public void purge(ProductPrice productPrice, RequestContext requestContext) throws ResponseException {
		getService().purgeProductPrice(productPrice);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			//			description.addProperty("product", Representation.DEFAULT);
			//            description.addProperty("program", Representation.DEFAULT);
			description.addProperty("salePrice");
			description.addProperty("purchasePrice");
			description.addProperty("active");
			description.addProperty("location", Representation.REF);
			description.addProperty("uuid");
		} else if (representation instanceof DefaultRepresentation || representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			//			description.addProperty("product", Representation.REF);
			//            description.addProperty("program", Representation.REF);
			description.addProperty("salePrice");
			description.addProperty("purchasePrice");
			description.addProperty("active");
			description.addProperty("location", Representation.REF);
			description.addProperty("uuid");
		}
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model
		//                .property("program", new RefProperty("#/definitions/ProductProgramGet"))
		.property("salePrice", new DoubleProperty()).property("purchasePrice", new DoubleProperty())
		        .property("active", new BooleanProperty())
		        .property("location", new RefProperty("#/definitions/LocationGet")).property("uuid", new StringProperty());
		
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		//        description.addRequiredProperty("name");
		//		description.addRequiredProperty("product");
		//        description.addRequiredProperty("productProgram");
		description.addRequiredProperty("salePrice");
		description.addRequiredProperty("purchasePrice");
		description.addRequiredProperty("location");
		description.addRequiredProperty("active");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model
		//                .property("program", new RefProperty("#/definitions/ProductProgramCreate"))
		.property("salePrice", new DoubleProperty()).property("purchasePrice", new DoubleProperty())
		        .property("active", new BooleanProperty())
		        .property("location", new RefProperty("#/definitions/LocationGet")).property("uuid", new StringProperty());
		model.required("salePrice").required("purchasePrice").required("location");
		
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		//        description.addProperty("name");
		//		description.addProperty("product");
		//        description.addProperty("productProgram");
		description.addProperty("salePrice");
		description.addProperty("purchasePrice");
		description.addProperty("location");
		description.addProperty("active");
		return description;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model
		//                .property("program", new RefProperty("#/definitions/ProductProgramCreate"))
		.property("salePrice", new DoubleProperty()).property("purchasePrice", new DoubleProperty())
		        .property("active", new BooleanProperty())
		        .property("location", new RefProperty("#/definitions/LocationGet")).property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public ProductCode getParent(ProductPrice productPrice) {
		return productPrice.getProductCode();
	}
	
	@Override
	public void setParent(ProductPrice productPrice, ProductCode product) {
		productPrice.setProductCode(product);
	}
	
	@Override
	public PageableResult doGetAll(ProductCode product, RequestContext requestContext) throws ResponseException {
		List<ProductPrice> prices = new ArrayList<ProductPrice>(product.getPrices());
		return new NeedsPaging<ProductPrice>(prices, requestContext);
	}
}
