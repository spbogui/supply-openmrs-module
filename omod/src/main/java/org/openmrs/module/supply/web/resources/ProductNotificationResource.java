package org.openmrs.module.supply.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.*;
import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.ProductNotification;
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
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + SupplyResourceController.SUPPLY_REST_NAMESPACE + "Notification", supportedClass = ProductNotification.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" })
public class ProductNotificationResource extends DelegatingCrudResource<ProductNotification> {
	
	ProductOperationService getService() {
		return Context.getService(ProductOperationService.class);
	}
	
	@Override
	public ProductNotification getByUniqueId(String s) {
		return getService().getNotification(s);
	}
	
	@Override
	protected void delete(ProductNotification notification, String s, RequestContext requestContext)
	        throws ResponseException {
	}
	
	@Override
	public void purge(ProductNotification notification, RequestContext requestContext) throws ResponseException {
		//		getService().purgeProductAttributeStock(productAttributeStock);
	}
	
	@Override
	public ProductNotification newDelegate() {
		return new ProductNotification();
	}
	
	@Override
	public ProductNotification save(ProductNotification notification) {
		return getService().saveNotification(notification);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("notificationDate", Representation.REF);
			description.addProperty("productCode", Representation.REF);
			description.addProperty("notification");
			description.addProperty("notificationInfo");
			description.addProperty("location", Representation.REF);
			description.addProperty("notifiedTo", Representation.REF);
			description.addProperty("operationType", Representation.REF);
			description.addProperty("notificationRead");
			description.addProperty("notificationClosed");
			description.addProperty("uuid");
		} else if (representation instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("notificationDate");
			description.addProperty("notification");
			description.addProperty("notificationRead");
			description.addProperty("notificationClosed");
			description.addProperty("notifiedTo", Representation.REF);
			description.addProperty("uuid");
		} else if (representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("notification");
			description.addProperty("notifiedTo", Representation.REF);
			description.addProperty("uuid");
		}
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("productCode", new RefProperty("#/definitions/ProductCodeGet"))
		        .property("notifiedTo", new RefProperty("#/definitions/LocationGet"))
		        .property("notification", new StringProperty()).property("notificationInfo", new StringProperty())
		        .property("notificationRead", new BooleanProperty()).property("notificationClosed", new BooleanProperty())
		        .property("location", new RefProperty("#/definitions/LocationGet"))
		        .property("operationType", new RefProperty("#/definitions/ProductOperationTypeGet"))
		        .property("notificationDate", new DateProperty()).property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("notificationDate");
		description.addRequiredProperty("location");
		description.addRequiredProperty("notifiedTo");
		description.addRequiredProperty("location");
		description.addProperty("productCode");
		description.addProperty("notification");
		description.addProperty("notificationInfo");
		description.addProperty("operationType");
		description.addProperty("notificationRead");
		description.addProperty("notificationClosed");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		
		model.property("notification", new StringProperty()).property("notificationInfo", new StringProperty())
		        .property("notificationRead", new BooleanProperty()).property("notificationClosed", new BooleanProperty())
		        .property("notificationDate", new DateProperty()).property("uuid", new StringProperty());
		
		if (rep instanceof FullRepresentation) {
			model.property("productCode", new RefProperty("#/definitions/ProductCodeGet"))
			        .property("operationType", new RefProperty("#/definitions/ProductOperationTypeGet"))
			        .property("location", new RefProperty("#/definitions/LocationGet"))
			        .property("notifiedTo", new RefProperty("#/definitions/LocationGet"));
		} else {
			model.property("notifiedTo", new StringProperty().example("uuid"))
			        .property("operationType", new StringProperty().example("uuid"))
			        .property("location", new StringProperty().example("uuid"));
		}
		model.required("location").required("notification").required("notifiedTo");
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("notificationDate");
		description.addProperty("location");
		description.addProperty("notifiedTo");
		description.addProperty("location");
		description.addProperty("productCode");
		description.addProperty("notification");
		description.addProperty("notificationInfo");
		description.addProperty("operationType");
		description.addProperty("notificationRead");
		description.addProperty("notificationClosed");
		return description;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("notifiedTo", new StringProperty().example("uuid"))
		        .property("operationType", new StringProperty().example("uuid"))
		        .property("location", new StringProperty().example("uuid"))
		        .property("notificationInfo", new StringProperty()).property("notificationRead", new BooleanProperty())
		        .property("notificationClosed", new BooleanProperty()).property("notificationDate", new DateProperty())
		        .property("uuid", new StringProperty());
		
		if (rep instanceof FullRepresentation) {
			model.property("location", new RefProperty("#/definitions/LocationCreate"))
			        .property("operationType", new RefProperty("#/definitions/ProductOperationTypeCreate"))
			        .property("notifiedTo", new RefProperty("#/definitions/LocationCreate"));
		}
		return model;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<ProductNotification>(getService().getAllProductNotification(false, false), context);
	}
	
	@Override
    protected PageableResult doSearch(RequestContext context) {
        String filter = context.getParameter("filter");
        String includeRead = context.getParameter("includeRead");
        String includeClosed = context.getParameter("includeClosed");

        if (StringUtils.isNotBlank(filter)) {
            List<ProductNotification> notifications = new ArrayList<>();
            if (filter.contains("transfer")) {
                List<ProductNotification> transferNotification = getService().getAllTransferNotification(
                        includeRead != null && includeRead.equals("true"),
                        includeClosed != null && includeClosed.equals("true"));
                if (transferNotification != null) {
                    notifications.addAll(transferNotification);
                }
            } else if (filter.contains("rupture")) {
                List<ProductNotification> ruptureNotification = getService().getAllRuptureNotification(
                        includeRead != null && includeRead.equals("true"),
                        includeClosed != null && includeClosed.equals("true"));
                if (ruptureNotification != null) {
                    notifications.addAll(ruptureNotification);
                }
            } else if (filter.contains("returnProduct")) {
                List<ProductNotification> returnNotification = getService().getAllProductReturnNotification(
                        includeRead != null && includeRead.equals("true"),
                        includeClosed != null && includeClosed.equals("true"));
                if (returnNotification != null) {
                    notifications.addAll(returnNotification);
                }
            } else if (filter.contains("reception")) {
                List<ProductNotification> receptionNotification = getService().getAllReceptionNotification(
                        includeRead != null && includeRead.equals("true"),
                        includeClosed != null && includeClosed.equals("true"));
                if (receptionNotification != null) {
                    notifications.addAll(receptionNotification);
                }
            } else if (filter.contains("rejectReport")) {
                List<ProductNotification> receptionNotification = getService().getAllRejectReportNotification(
                        includeRead != null && includeRead.equals("true"),
                        includeClosed != null && includeClosed.equals("true"));
                if (receptionNotification != null) {
                    notifications.addAll(receptionNotification);
                }
            }
            return new NeedsPaging<ProductNotification>(notifications, context);
        }
        return new EmptySearchResult();
    }
}
