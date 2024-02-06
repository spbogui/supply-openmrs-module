package org.openmrs.module.supply.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.Product;
import org.openmrs.module.supply.ProductAttribute;
import org.openmrs.module.supply.ProductAttributeStock;
import org.openmrs.module.supply.ProductCode;
import org.openmrs.module.supply.api.ProductOperationService;
import org.openmrs.module.supply.api.ProductService;
import org.openmrs.module.supply.utils.SupplyUtils;
import org.openmrs.module.supply.web.controller.SupplyResourceController;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
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

@Resource(name = RestConstants.VERSION_1 + SupplyResourceController.SUPPLY_REST_NAMESPACE + "Attribute", supportedClass = ProductAttribute.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" })
public class ProductAttributeResource extends DelegatingCrudResource<ProductAttribute> {
	
	ProductService getService() {
		return Context.getService(ProductService.class);
	}
	
	@Override
	public ProductAttribute getByUniqueId(String s) {
		return getService().getProductAttribute(s);
	}
	
	@Override
	protected void delete(ProductAttribute productUnit, String s, RequestContext requestContext) throws ResponseException {
		
	}
	
	@Override
	public ProductAttribute newDelegate() {
		return new ProductAttribute();
	}
	
	@Override
	public ProductAttribute save(ProductAttribute productUnit) {
		return getService().saveProductAttribute(productUnit);
	}
	
	@Override
	public void purge(ProductAttribute productAttribute, RequestContext requestContext) throws ResponseException {
		// getService().purgeA(productAttribute);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("productCode", Representation.DEFAULT);
			description.addProperty("batchNumber");
			description.addProperty("expiryDate");
			description.addProperty("location", Representation.REF);
			description.addProperty("quantityInStock");
			description.addProperty("uuid");
		} else if (representation instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("productCode", Representation.REF);
			description.addProperty("batchNumber");
			description.addProperty("expiryDate");
			description.addProperty("location", Representation.REF);
			description.addProperty("quantityInStock");
			description.addProperty("uuid");
		} else if (representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("productCode", Representation.REF);
			description.addProperty("batchNumber");
			description.addProperty("expiryDate");
			description.addProperty("quantityInStock");
			description.addProperty("uuid");
			
		}
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("productCode");
		description.addRequiredProperty("batchNumber");
		description.addRequiredProperty("expiryDate");
		description.addRequiredProperty("location");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("productCode");
		description.addProperty("batchNumber");
		description.addProperty("expiryDate");
		description.addProperty("location");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<ProductAttribute>(getService().getAllProductAttributes(SupplyUtils.getUserLocation(), false),
		        context);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String batchNumber = context.getParameter("batchNumber");
		String productUuid = context.getParameter("productCode");
		
		List<ProductAttribute> attributes = new ArrayList<ProductAttribute>();
		if (batchNumber != null) {
			ProductAttribute attribute = getService().getProductAttributeByBatchNumber(batchNumber,
			    SupplyUtils.getUserLocation());
			if (attribute != null) {
				attributes.add(attribute);
			}
		}
		return new NeedsPaging<ProductAttribute>(attributes, context);
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("productCode", new RefProperty("#/definitions/ProductCodeGet"))
		        .property("batchNumber", new StringProperty()).property("expiryDate", new DateProperty())
		        .property("location", new RefProperty("#/definitions/LocationGet"))
		        .property("quantityInStock", new IntegerProperty()).property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("batchNumber", new StringProperty()).property("expiryDate", new DateProperty())
		        .property("uuid", new StringProperty());
		
		if (rep instanceof FullRepresentation) {
			model.property("location", new RefProperty("#/definitions/LocationCreate")).property("productCode",
			    new RefProperty("#/definitions/ProductCodeCreate"));
		} else if (rep instanceof DefaultRepresentation) {
			model.property("productCode", new StringProperty().example("uuid")).property("location",
			    new StringProperty().example("uuid"));
		}
		model.required("location").required("batchNumber").required("expiryDate").required("product");
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("batchNumber", new StringProperty()).property("expiryDate", new DateProperty())
		        .property("uuid", new StringProperty()).property("productCode", new StringProperty().example("uuid"))
		        .property("location", new StringProperty().example("uuid"));
		return model;
	}
	
	@PropertyGetter("quantityInStock")
	public static Integer getQuantityInStock(ProductAttribute attribute) {
		ProductAttributeStock attributeStock = Context.getService(ProductOperationService.class)
		        .getProductAttributeStockByAttribute(attribute, SupplyUtils.getUserLocation(), false);
		if (attributeStock != null) {
			return attributeStock.getQuantityInStock();
		}
		return null;
	}
}
