package org.openmrs.module.supply.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.*;
import org.openmrs.module.supply.api.ProductOperationService;
import org.openmrs.module.supply.api.ProductService;
import org.openmrs.module.supply.enumerations.Incidence;
import org.openmrs.module.supply.enumerations.OperationStatus;
import org.openmrs.module.supply.enumerations.QuantityType;
import org.openmrs.module.supply.utils.SupplyUtils;
import org.openmrs.module.supply.web.controller.SupplyResourceController;
import org.openmrs.module.webservices.docs.swagger.core.property.EnumProperty;
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
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Resource(name = RestConstants.VERSION_1 + SupplyResourceController.SUPPLY_REST_NAMESPACE + "Operation", supportedClass = ProductOperation.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" })
public class ProductOperationResource extends DataDelegatingCrudResource<ProductOperation> {
	
	ProductOperationService getService() {
		return Context.getService(ProductOperationService.class);
	}
	
	@Override
	public ProductOperation getByUniqueId(String s) {
		return getService().getProductOperation(s);
	}
	
	@Override
	protected void delete(ProductOperation operation, String s, RequestContext requestContext) throws ResponseException {
		
	}
	
	@Override
	public ProductOperation newDelegate() {
		return new ProductOperation();
	}
	
	@Override
	public ProductOperation save(ProductOperation productUnit) {
		if (productUnit.getLocation() == null) {
			productUnit.setLocation(SupplyUtils.getUserLocation());
		}
		return getService().saveProductOperation(productUnit);
	}
	
	@Override
	public void purge(ProductOperation productOperation, RequestContext requestContext) throws ResponseException {
		getService().purgeProductOperation(productOperation);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("operationNumber");
			description.addProperty("productProgram", Representation.REF);
			description.addProperty("operationDate");
			description.addProperty("operationType", Representation.FULL);
			description.addProperty("parentOperation", Representation.DEFAULT);
			description.addProperty("childrenOperation", Representation.DEFAULT);
			description.addProperty("operationStatus");
			description.addProperty("incidence");
			description.addProperty("quantityType");
			description.addProperty("observation");
			description.addProperty("attributes", Representation.DEFAULT);
			description.addProperty("fluxAttributes", Representation.DEFAULT);
			description.addProperty("fluxes", Representation.DEFAULT);
			description.addProperty("otherFluxes", Representation.DEFAULT);
			description.addProperty("exchangeLocation", Representation.DEFAULT);
			description.addProperty("location", Representation.DEFAULT);
			description.addProperty("uuid");
		} else if (representation instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("operationNumber");
			description.addProperty("productProgram", Representation.REF);
			description.addProperty("operationDate");
			description.addProperty("operationType", Representation.DEFAULT);
			description.addProperty("parentOperation", Representation.REF);
			description.addProperty("childrenOperation", Representation.REF);
			description.addProperty("operationStatus");
			description.addProperty("incidence");
			description.addProperty("quantityType");
			description.addProperty("observation");
			description.addProperty("attributes", Representation.REF);
			description.addProperty("fluxes", Representation.DEFAULT);
			description.addProperty("otherFluxes", Representation.REF);
			description.addProperty("exchangeLocation", Representation.REF);
			description.addProperty("fluxAttributes", Representation.DEFAULT);
			description.addProperty("location", Representation.REF);
			description.addProperty("uuid");
		} else if (representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("operationNumber");
			description.addProperty("productProgram", Representation.REF);
			description.addProperty("operationDate");
			description.addProperty("operationType", Representation.REF);
			description.addProperty("operationStatus");
			description.addProperty("incidence");
			description.addProperty("quantityType");
			description.addProperty("attributes", Representation.REF);
			description.addProperty("exchangeLocation", Representation.REF);
			description.addProperty("location", Representation.REF);
			description.addProperty("uuid");
		}
		return description;
	}
	
	@Override
	public List<String> getPropertiesToExposeAsSubResources() {
		return Arrays.asList("fluxes", "otherFluxes", "attributes");
	}
	
	@PropertyGetter("operationType")
	public static ProductOperationType getOperationType(ProductOperation operation) {
		return operation.getOperationType();
	}
	
	@PropertyGetter("fluxes")
	public static Set<ProductOperationFlux> getFluxes(ProductOperation operation) {
		return new LinkedHashSet<ProductOperationFlux>(operation.getFluxes());
	}
	
	@PropertySetter("fluxes")
	public static void setFluxes(ProductOperation operation, List<ProductOperationFlux> fluxes)
	        throws ResourceDoesNotSupportOperationException {
		if (operation.getFluxes() != null && operation.getFluxes().containsAll(fluxes)) {
			return;
		}
		if (operation.getFluxes() != null && !operation.getFluxes().isEmpty()) {
			throw new ResourceDoesNotSupportOperationException("Fluxes can only be set for newly created objects !");
		}
		for (ProductOperationFlux flux : fluxes) {
			ProductOperationFlux existingFlux = operation.getFluxes() != null ? getMatchingFlux(flux, operation.getFluxes())
			        : null;
			if (existingFlux != null) {
				copyFluxFields(existingFlux, flux);
			} else {
				operation.addFlux(flux);
			}
		}
	}
	
	private static void copyFluxFields(ProductOperationFlux existingFlux, ProductOperationFlux flux) {
		existingFlux.setQuantity(flux.getQuantity());
		existingFlux.setObservation(flux.getObservation());
		existingFlux.setLocation(flux.getLocation());
	}
	
	private static ProductOperationFlux getMatchingFlux(ProductOperationFlux flux,
	        Set<ProductOperationFlux> productAttributeFluxes) {
		for (ProductOperationFlux existingFlux : productAttributeFluxes) {
			if (existingFlux.getUuid() != null && existingFlux.getUuid().equals(flux.getUuid())) {
				return existingFlux;
			}
		}
		return null;
	}
	
	@PropertyGetter("attributes")
	public static Set<ProductOperationAttribute> getAttributes(ProductOperation operation) {
		return new LinkedHashSet<ProductOperationAttribute>(operation.getAttributes());
	}
	
	@PropertySetter("attributes")
	public static void setAttributes(ProductOperation operation, List<ProductOperationAttribute> attributes)
	        throws ResourceDoesNotSupportOperationException {
		if (operation.getAttributes() != null && operation.getAttributes().containsAll(attributes)) {
			return;
		}
		if (operation.getAttributes() != null && !operation.getAttributes().isEmpty()) {
			throw new ResourceDoesNotSupportOperationException("Fluxes can only be set for newly created objects !");
		}
		for (ProductOperationAttribute attribute : attributes) {
			ProductOperationAttribute existingAttribute = operation.getAttributes() != null ? getMatchingAttribute(
			    attribute, operation.getAttributes()) : null;
			if (existingAttribute != null) {
				copyAttributeFields(existingAttribute, attribute);
			} else {
				operation.addOperationAttribute(attribute);
			}
		}
	}
	
	private static void copyAttributeFields(ProductOperationAttribute existingAttribute, ProductOperationAttribute attribute) {
		existingAttribute.setOperationAttributeType(attribute.getOperationAttributeType());
		existingAttribute.setValue(attribute.getValue());
	}
	
	private static ProductOperationAttribute getMatchingAttribute(ProductOperationAttribute attribute,
	        Set<ProductOperationAttribute> attributes) {
		for (ProductOperationAttribute existingAttribute : attributes) {
			if (existingAttribute.getUuid() != null && existingAttribute.getUuid().equals(attribute.getUuid())) {
				return existingAttribute;
			}
		}
		return null;
	}
	
	@PropertyGetter("otherFluxes")
	public static Set<ProductOperationOtherFlux> getOtherFluxes(ProductOperation operation) {
		return new LinkedHashSet<ProductOperationOtherFlux>(operation.getOtherFluxes());
	}
	
	@PropertyGetter("fluxAttributes")
	public static Set<ProductOperationFluxAttribute> getFluxAttributes(ProductOperation operation) {
		return new LinkedHashSet<ProductOperationFluxAttribute>(operation.getFluxAttributes());
	}
	
	@PropertySetter("otherFluxes")
	public static void setOtherFluxes(ProductOperation operation, List<ProductOperationOtherFlux> fluxes)
	        throws ResourceDoesNotSupportOperationException {
		if (operation.getOtherFluxes() != null && operation.getOtherFluxes().containsAll(fluxes)) {
			return;
		}
		if (operation.getOtherFluxes() != null && !operation.getOtherFluxes().isEmpty()) {
			throw new ResourceDoesNotSupportOperationException("Other fluxes can only be set for newly created objects !");
		}
		for (ProductOperationOtherFlux flux : fluxes) {
			ProductOperationOtherFlux existingFlux = operation.getOtherFluxes() != null ? getMatchingOtherFlux(flux,
			    operation.getOtherFluxes()) : null;
			if (existingFlux != null) {
				copyOtherFluxFields(existingFlux, flux);
			} else {
				operation.addOtherFlux(flux);
			}
		}
	}
	
	private static void copyOtherFluxFields(ProductOperationOtherFlux existingFlux, ProductOperationOtherFlux flux) {
		existingFlux.setProduct(flux.getProduct());
		//existingFlux.setProductAttribute(flux.getProductAttribute());
		existingFlux.setLabel(flux.getLabel());
		existingFlux.setQuantity(flux.getQuantity());
		existingFlux.setLocation(flux.getLocation());
	}
	
	private static ProductOperationOtherFlux getMatchingOtherFlux(ProductOperationOtherFlux flux,
	        Set<ProductOperationOtherFlux> productAttributeFluxes) {
		for (ProductOperationOtherFlux existingFlux : productAttributeFluxes) {
			if (existingFlux.getUuid() != null && existingFlux.getUuid().equals(flux.getUuid())) {
				return existingFlux;
			}
		}
		return null;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("operationNumber", new StringProperty())
		        .property("productProgram", new StringProperty())
		        .property("operationDate", new StringProperty())
		        .property("location", new RefProperty("#/definitions/LocationGet"))
		        .property(
		            "operationStatus",
		            new EnumProperty(OperationStatus.class)._enum(Arrays.asList("AWAITING_VALIDATION", "DISABLED",
		                "VALIDATED", "NOT_COMPLETED", "SUBMITTED", "AWAITING_TREATMENT", "TREATED")))
		        .property(
		            "incidence",
		            new EnumProperty(Incidence.class)._enum(Arrays.asList(Incidence.EQUAL.name(), Incidence.NONE.name(),
		                Incidence.POSITIVE.name(), Incidence.NEGATIVE.name())))
		        .property("observation", new StringProperty())
		        .property("fluxes", new ArrayProperty(new RefProperty("#/definitions/ProductOperationFluxGet")))
		        .property("attributeOtherFluxes",
		            new ArrayProperty(new RefProperty("#/definitions/ProductOperationOtherFluxGet")))
		        //		        .property("fluxProducts", new ArrayProperty(new RefProperty("#/definitions/ProductGet")))
		        //		        .property("otherFluxProducts", new ArrayProperty(new RefProperty("#/definitions/ProductGet")))
		        .property("voided", new BooleanProperty()).property("uuid", new StringProperty());
		
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("operationNumber", new StringProperty())
		        .property("operationDate", new StringProperty())
		        .property(
		            "operationStatus",
		            new EnumProperty(OperationStatus.class)._enum(Arrays.asList("AWAITING_VALIDATION", "DISABLED",
		                "VALIDATED", "NOT_COMPLETED", "SUBMITTED", "AWAITING_TREATMENT", "TREATED")))
		        .property("quantityType",
		            new EnumProperty(QuantityType.class)._enum(Arrays.asList("DISPENSATION", "PACKAGING")))
		        .property(
		            "incidence",
		            new EnumProperty(Incidence.class)._enum(Arrays.asList(Incidence.EQUAL.name(), Incidence.NONE.name(),
		                Incidence.POSITIVE.name(), Incidence.NEGATIVE.name())))
		        .property("observation", new StringProperty()).property("uuid", new StringProperty())
		        .property("attributes", new RefProperty("#/definitions/ProductOperationAttributeCreate"));
		
		if (rep instanceof FullRepresentation) {
			model.property("location", new RefProperty("#/definitions/LocationCreate"))
			        .property("exchangeLocation", new RefProperty("#/definitions/LocationCreate"))
			        .property("fluxes", new RefProperty("#/definitions/ProductOperationFluxCreate"))
			        .property("otherFluxes", new RefProperty("#/definitions/ProductOperationOtherFluxCreate"))
			        .property("productProgram", new RefProperty("#/definitions/ProductProgramCreate"))
			        .property("parentOperation", new RefProperty("#/definitions/ProductOperationCreate"))
			        .property("operationType", new RefProperty("#/definitions/ProductOperationTypeCreate"));
		} else {
			model.property("location", new StringProperty().example("uuid"))
			        .property("exchangeLocation", new StringProperty().example("uuid"))
			        .property("fluxes", new StringProperty().example("uuid"))
			        .property("otherFluxes", new StringProperty().example("uuid"))
			        .property("productProgram", new StringProperty().example("uuid"))
			        .property("parentOperation", new StringProperty().example("uuid"))
			        .property("operationType", new StringProperty().example("uuid"));
		}
		
		model.required("location").required("operationDate").required("operationStatus").required("incidence");
		
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("operationNumber", new StringProperty())
		        .property("operationDate", new StringProperty())
		        .property(
		            "operationStatus",
		            new EnumProperty(OperationStatus.class)._enum(Arrays.asList("AWAITING_VALIDATION", "DISABLED",
		                "VALIDATED", "NOT_COMPLETED", "SUBMITTED", "AWAITING_TREATMENT", "TREATED")))
		        .property(
		            "incidence",
		            new EnumProperty(Incidence.class)._enum(Arrays.asList(Incidence.EQUAL.name(), Incidence.NONE.name(),
		                Incidence.POSITIVE.name(), Incidence.NEGATIVE.name())))
		        .property("quantityType",
		            new EnumProperty(QuantityType.class)._enum(Arrays.asList("DISPENSATION", "PACKAGING")))
		        .property("observation", new StringProperty()).property("uuid", new StringProperty())
		        .property("location", new StringProperty().example("uuid"))
		        .property("exchangeLocation", new StringProperty().example("uuid"))
		        .property("fluxes", new StringProperty().example("uuid"))
		        .property("otherFluxes", new StringProperty().example("uuid"))
		        .property("productProgram", new StringProperty().example("uuid"))
		        .property("parentOperation", new StringProperty().example("uuid"))
		        .property("operationAttributes", new StringProperty().example("uuid"));
		
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("operationNumber");
		description.addRequiredProperty("productProgram");
		description.addRequiredProperty("operationDate");
		description.addRequiredProperty("location");
		description.addRequiredProperty("operationStatus");
		description.addRequiredProperty("incidence");
		description.addRequiredProperty("operationType");
		description.addRequiredProperty("quantityType");
		description.addProperty("fluxes");
		description.addProperty("otherFluxes");
		description.addProperty("attributes");
		description.addProperty("parentOperation");
		description.addProperty("exchangeLocation");
		description.addProperty("observation");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("operationNumber");
		description.addProperty("productProgram");
		description.addProperty("operationDate");
		description.addProperty("location");
		description.addProperty("operationStatus");
		description.addProperty("incidence");
		description.addProperty("fluxes");
		//		description.addProperty("operationType");
		description.addProperty("quantityType");
		description.addProperty("otherFluxes");
		description.addProperty("attributes");
		description.addProperty("parentOperation");
		description.addProperty("exchangeLocation");
		description.addProperty("observation");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<ProductOperation>(getService().getAllProductOperation(SupplyUtils.getUserLocation(), false),
		        context);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		// String period = context.getParameter("period");
		String program = context.getParameter("program");
		String filter = context.getParameter("filter");
		String operationType = context.getParameter("type");
		String locationUuid = context.getParameter("location");
		
		List<ProductOperation> productOperations = new ArrayList<ProductOperation>();
		
		if (StringUtils.isNotBlank(operationType) && !StringUtils.isEmpty(operationType)) {
			ProductOperationType type = getService().getProductOperationType(operationType);
			if (type != null) {
				if (StringUtils.isNotBlank(filter) && !StringUtils.isEmpty(filter)) {
					if (filter.contains("last")) {
						//						System.out.println("--------------------------------> filter = " + filter);
						if (StringUtils.isNotBlank(program) && StringUtils.isNotEmpty(program)) {
							ProductProgram productProgram = Context.getService(ProductService.class).getProductProgram(
							    program);
							if (productProgram != null) {
								ProductOperation operation = getService().getLastProductOperation(type, productProgram,
								    SupplyUtils.getUserLocation(), filter.contains("validated"), false);
								if (operation != null) {
									productOperations.add(operation);
								}
							}
						}
						
					} else if (filter.contains("operationNumber")) {
						String operationNumber = filter.split(":")[1];
						
						Location location = StringUtils.isNotBlank(locationUuid) ? Context.getLocationService()
						        .getLocationByUuid(locationUuid) : SupplyUtils.getUserLocation();
						if (filter.contains("last")) {
							ProductOperation operation = getService().getProductOperationByOperationNumber(type,
							    operationNumber, location, filter.contains("validated"));
							if (operation != null) {
								productOperations.add(operation);
							}
							
						} else {
							List<ProductOperation> operations = getService().getProductOperationByOperationNumber(
							    operationNumber, location, filter.contains("validated"));
							if (operations != null) {
								productOperations.addAll(operations);
							}
						}
						
					} else if (filter.contains("period")) {
						DateFormat sourceFormat = new SimpleDateFormat("dd/MM/yyyy");
						String startDateString = filter.split(",")[0].split(":")[1];
						String endDateString = filter.split(",")[1].split(":")[1];
						try {
							Date startDate = sourceFormat.parse(startDateString);
							Date endDate = sourceFormat.parse(endDateString);
							List<ProductOperation> operations = getService().getAllProductOperation(type, startDate,
							    endDate, SupplyUtils.getUserLocation(), true, false);
							
							if (operations != null) {
								productOperations.addAll(operations);
							}
						}
						catch (ParseException e) {
							e.printStackTrace();
						}
					}
				} else {
					productOperations.addAll(getService().getAllProductOperation(type, SupplyUtils.getUserLocation(), false,
					    false));
				}
			}
		} else {
			if (StringUtils.isNotBlank(filter) && StringUtils.isNotEmpty(filter)) {
				if (filter.contains("operationNumber")) {
					String operationNumber = filter.split(",")[0].split(":")[1];
					List<ProductOperation> operations = getService().getProductOperationByOperationNumber(operationNumber,
					    SupplyUtils.getUserLocation(), filter.contains("validated"));
					if (operations != null) {
						productOperations.addAll(operations);
					}
				}
			}
		}
		
		return new NeedsPaging<ProductOperation>(productOperations, context);
	}
}
