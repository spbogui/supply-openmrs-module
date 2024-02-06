package org.openmrs.module.supply.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.ProductCode;
import org.openmrs.module.supply.ProductProgram;
import org.openmrs.module.supply.ProductStockStatus;
import org.openmrs.module.supply.api.ProductOperationService;
import org.openmrs.module.supply.api.ProductService;
import org.openmrs.module.supply.utils.SupplyUtils;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + SupplyResourceController.SUPPLY_REST_NAMESPACE + "StockStatus", supportedClass = ProductStockStatus.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" })
public class ProductStockStatusResource extends DelegatingCrudResource<ProductStockStatus> {
	
	public ProductStockStatusResource() {
	}
	
	ProductOperationService getService() {
		return Context.getService(ProductOperationService.class);
	}
	
	@Override
	public ProductStockStatus getByUniqueId(String s) {
		return getService().getProductStockStatus(s);
	}
	
	@Override
	protected void delete(ProductStockStatus productStockStatus, String s, RequestContext requestContext)
	        throws ResponseException {
		
	}
	
	@Override
	public ProductStockStatus newDelegate() {
		return new ProductStockStatus();
	}
	
	@Override
	public ProductStockStatus save(ProductStockStatus productStockStatus) {
		return getService().saveProductStockStatus(productStockStatus);
	}
	
	@Override
	public void purge(ProductStockStatus productProgram, RequestContext requestContext) throws ResponseException {
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("productCode", Representation.REF);
			description.addProperty("quantityInStock");
			description.addProperty("averageConsumedQuantity");
			description.addProperty("expiryNextDate");
			description.addProperty("stockDate");
			description.addProperty("location", Representation.REF);
			description.addProperty("uuid");
		} else if (representation instanceof DefaultRepresentation || representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("productCode", Representation.REF);
			description.addProperty("quantityInStock");
			description.addProperty("expiryNextDate");
			description.addProperty("location", Representation.REF);
			description.addProperty("uuid");
		}
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("productCode", new RefProperty("#/definitions/ProductCodeGet"))
		        .property("expiryNextDate", new DateProperty())
		        .property("location", new RefProperty("#/definitions/LocationGet"))
		        .property("quantityInStock", new DoubleProperty()).property("averageConsumedQuantity", new DoubleProperty())
		        .property("stockDate", new DateProperty()).property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("productCode");
		description.addProperty("location");
		description.addProperty("quantityInStock");
		description.addProperty("averageConsumedQuantity");
		description.addProperty("expiryNextDate");
		description.addProperty("stockDate");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("productCode", new StringProperty()).property("location", new StringProperty())
		        .property("quantityInStock", new DoubleProperty()).property("averageConsumedQuantity", new DoubleProperty())
		        .property("expiryNextDate", new DateProperty()).property("stockDate", new DateProperty())
		        .property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("productCode");
		description.addRequiredProperty("location");
		description.addRequiredProperty("quantityInStock");
		description.addRequiredProperty("averageConsumedQuantity");
		description.addRequiredProperty("expiryNextDate");
		description.addRequiredProperty("stockDate");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("productCode", new StringProperty()).property("location", new StringProperty())
		        .property("quantityInStock", new DoubleProperty()).property("averageConsumedQuantity", new DoubleProperty())
		        .property("expiryNextDate", new DateProperty()).property("stockDate", new DateProperty());
		return model;
	}
	
	@Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        List<ProductStockStatus> productStockStatuses = getService().getAllProductStockStatuses(SupplyUtils.getUserLocation());
        return new NeedsPaging<>(productStockStatuses, context);
    }
	
	@Override
    protected PageableResult doSearch(RequestContext context) {
        List<ProductStockStatus> productStockStatuses = new ArrayList<>();

        String locationUuid = context.getParameter("location");
        String productCodeUuid = context.getParameter("productCode");
        String dateString = context.getParameter("date");
        String filter = context.getParameter("filter");
        String programUuid = context.getParameter("program");


        if (StringUtils.isNotEmpty(dateString) && StringUtils.isNotEmpty(programUuid)) {
            ProductProgram program = Context.getService(ProductService.class).getProductProgram(programUuid);
            if (program != null) {
                DateFormat sourceFormat = new SimpleDateFormat("dd-MM-yyyy");
                try {
                    Date startDate = sourceFormat.parse(dateString);
                    Location currentLocation = SupplyUtils.getUserLocation();

                    if (StringUtils.isNotEmpty(locationUuid)) {
                        Location location = Context.getLocationService().getLocationByUuid(locationUuid);
                        if (location != null) {
                            currentLocation = location;
                        }
                    }

                    if (StringUtils.isNotEmpty(productCodeUuid)) {
                        ProductCode productCode = Context.getService(ProductService.class).getProductCode(productCodeUuid);
                        if (productCode != null) {
                            List<ProductStockStatus> productStockStatusList = getService()
                                    .getProductStockStatusByProductCode(
                                            productCode,
                                            program,
                                            currentLocation,
                                            startDate,
                                            StringUtils.isNotEmpty(filter) && filter.contains("forChildren"));
                            if (productStockStatusList != null) {
                                productStockStatuses.addAll(productStockStatusList);
                            }
                        }
                    } else {
                        List<ProductStockStatus> productStockStatusList = getService().getAllProductStockStatuses(currentLocation, program, startDate,
                                StringUtils.isNotEmpty(filter) && filter.contains("forChildren"));
                        if (productStockStatusList != null) {
                            productStockStatuses.addAll(productStockStatusList);
                        }
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }


        }

        return new NeedsPaging<>(productStockStatuses, context);
    }
}
