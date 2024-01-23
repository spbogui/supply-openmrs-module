package org.openmrs.module.supply.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.*;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + SupplyResourceController.SUPPLY_REST_NAMESPACE + "AttributeStock", supportedClass = ProductAttributeStock.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" })
public class ProductAttributeStockResource extends DelegatingCrudResource<ProductAttributeStock> {
	
	ProductOperationService getService() {
		return Context.getService(ProductOperationService.class);
	}
	
	@Override
	public ProductAttributeStock getByUniqueId(String s) {
		return getService().getProductAttributeStock(s);
	}
	
	@Override
	protected void delete(ProductAttributeStock productAttributeStock, String s, RequestContext requestContext)
	        throws ResponseException {
	}
	
	@Override
	public void purge(ProductAttributeStock productAttributeStock, RequestContext requestContext) throws ResponseException {
		getService().purgeProductAttributeStock(productAttributeStock);
	}
	
	@Override
	public ProductAttributeStock newDelegate() {
		return new ProductAttributeStock();
	}
	
	@Override
	public ProductAttributeStock save(ProductAttributeStock productAttributeStock) {
		return getService().saveProductAttributeStock(productAttributeStock);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("attribute", Representation.REF);
			description.addProperty("operation", Representation.REF);
			description.addProperty("quantityInStock");
			description.addProperty("display");
			description.addProperty("location", Representation.REF);
			description.addProperty("auditInfo");
			description.addProperty("uuid");
			description.addProperty("voided");
		} else if (representation instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("attribute", Representation.REF);
			description.addProperty("quantityInStock");
			description.addProperty("uuid");
		} else if (representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("quantityInStock");
			description.addProperty("uuid");
		}
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("attribute", new RefProperty("#/definitions/ProductAttributeGet"))
		        .property("operation", new RefProperty("#/definitions/ProductOperationGet"))
		        .property("quantityInStock", new IntegerProperty()).property("display", new StringProperty())
		        .property("location", new RefProperty("#/definitions/LocationGet")).property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("attribute");
		description.addRequiredProperty("operation");
		description.addRequiredProperty("quantityInStock");
		description.addRequiredProperty("location");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("quantityInStock", new IntegerProperty()).property("uuid", new StringProperty())
		        .required("attribute").required("operation").required("quantityInStock").required("location");
		
		if (rep instanceof FullRepresentation) {
			model.property("location", new RefProperty("#/definitions/LocationCreate"))
			        .property("attribute", new RefProperty("#/definitions/ProductAttributeCreate"))
			        .property("operation", new RefProperty("#/definitions/ProductOperationCreate"));
		} else {
			model.property("attribute", new StringProperty().example("uuid"))
			        .property("operation", new StringProperty().example("uuid"))
			        .property("location", new StringProperty().example("uuid"));
		}
		model.required("location").required("attribute").required("operation").required("quantityInStock");
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("attribute");
		description.addProperty("operation");
		description.addProperty("quantityInStock");
		description.addProperty("operation");
		description.addProperty("location");
		return description;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("attribute", new StringProperty().example("uuid"))
		        .property("operation", new StringProperty().example("uuid"))
		        .property("location", new StringProperty().example("uuid"))
		        .property("quantityInStock", new IntegerProperty()).property("uuid", new StringProperty());
		
		if (rep instanceof FullRepresentation) {
			model.property("location", new RefProperty("#/definitions/LocationCreate"))
			        .property("attribute", new RefProperty("#/definitions/ProductAttributeCreate"))
			        .property("operation", new RefProperty("#/definitions/ProductOperationCreate"));
		}
		return model;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<ProductAttributeStock>(getService().getAllProductAttributeStocks(
		    SupplyUtils.getUserLocation(), false), context);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		List<ProductAttributeStock> stocks = new ArrayList<ProductAttributeStock>();
		
		String attribute = context.getParameter("attribute");
		String program = context.getParameter("program");
		String filter = context.getParameter("filter");
		String includeVoided = context.getParameter("includeVoided");
		String availableOnly = context.getParameter("availableOnly");
		String period = context.getParameter("period");
		String product = context.getParameter("product");
		String dateString = context.getParameter("operationDate");
		String locationString = context.getParameter("location");
		
		Location location = StringUtils.isNotBlank(locationString) ? Context.getLocationService().getLocationByUuid(
		    locationString) : null;
		if (location == null) {
			location = SupplyUtils.getUserLocation();
		}
		
		if (StringUtils.isNotBlank(attribute)) {
			ProductAttribute productAttribute = Context.getService(ProductService.class).getProductAttribute(attribute);
			stocks = getService().getAllProductAttributeStockByAttribute(productAttribute, includeVoided.equals("true"));
			return new NeedsPaging<ProductAttributeStock>(stocks, context);
		} else if (StringUtils.isNotEmpty(filter)) {
			if (StringUtils.isNotBlank(program)) {
				ProductProgram productProgram = Context.getService(ProductService.class).getProductProgram(program);
				if (productProgram != null) {
					if (StringUtils.isNotEmpty(period)) {
						DateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd");
						String startDateString = period.split(",")[0];
						String endDateString = period.split(",")[1];
						try {
							Date startDate = sourceFormat.parse(startDateString);
							Date endDate = sourceFormat.parse(endDateString);
							List<ProductAttributeStock> productAttributeStocks = getService().getAllProductAttributeStocks(
							    SupplyUtils.getUserLocation(), productProgram, startDate, endDate,
							    availableOnly.equals("true"), includeVoided.equals("true"));
							if (productAttributeStocks != null) {
								stocks.addAll(productAttributeStocks);
							}
						}
						catch (ParseException e) {
							e.printStackTrace();
						}
					} else if (StringUtils.isNotBlank(dateString)) {
						DateFormat sourceFormat = new SimpleDateFormat("dd-MM-yyyy");
						try {
							Date operationDate = sourceFormat.parse(dateString);
							if (StringUtils.isNotBlank(product)) {
								if (filter.equals("expiry")) {
									ProductCode productCode = Context.getService(ProductService.class).getProductCode(
									    product);
									
									if (productCode != null && operationDate != null) {
										List<ProductAttributeStock> productAttributeStocks = getService()
										        .getProductAttributeStockByExpiryDate(productCode, operationDate, location);
										if (productAttributeStocks != null) {
											stocks.addAll(productAttributeStocks);
										}
									}
								}
							} else {
								if (filter.equals("expired")) {
									List<ProductAttributeStock> productAttributeStocks = getService()
									        .getProductAttributeStockByExpired(operationDate, location, productProgram);
									if (productAttributeStocks != null) {
										stocks.addAll(productAttributeStocks);
									}
								} else if (filter.equals("expiring")) {
									List<ProductAttributeStock> productAttributeStocks = getService()
									        .getProductAttributeStockByExpiring(operationDate, location, productProgram);
									if (productAttributeStocks != null) {
										stocks.addAll(productAttributeStocks);
									}
								}
							}
						}
						catch (ParseException e) {
							throw new RuntimeException(e);
						}
					} else {
						List<ProductAttributeStock> productAttributeStocks = getService().getAllProductAttributeStocks(
						    SupplyUtils.getUserLocation(), productProgram, availableOnly.equals("true"),
						    includeVoided.equals("true"));
						if (productAttributeStocks != null) {
							stocks.addAll(productAttributeStocks);
						}
					}
				}
			}
		}
		return new NeedsPaging<ProductAttributeStock>(stocks, context);
	}
	
	@PropertyGetter("display")
	public String getDisplayString(ProductAttributeStock stock) {
		if (stock.getAttribute() == null)
			return "";
		
		return stock.getProductInStock() + " : " + stock.getQuantityInStock();
	}
}
