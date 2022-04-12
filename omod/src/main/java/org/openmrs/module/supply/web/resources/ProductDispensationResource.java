package org.openmrs.module.supply.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.ProductDispensation;
import org.openmrs.module.supply.api.ProductOperationService;
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
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + SupplyResourceController.SUPPLY_REST_NAMESPACE + "Dispensation", supportedClass = ProductDispensation.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" }, order = 2)
public class ProductDispensationResource extends ProductOperationResource {
	
	@PropertyGetter("uuid")
	public static String getUuid(ProductDispensation dispensation) {
		return dispensation.getUuid();
	}
	
	ProductOperationService getService() {
		return Context.getService(ProductOperationService.class);
	}
	
	@Override
	public ProductDispensation getByUniqueId(String s) {
		return getService().getProductDispensation(s);
	}
	
	//	@Override
	//	protected void delete(ProductDispensation operation, String s, RequestContext requestContext) throws ResponseException {
	//
	//	}
	
	@Override
	public ProductDispensation newDelegate() {
		return new ProductDispensation();
	}
	
	//	@Override
	//	public ProductDispensation save(ProductDispensation dispensation) {
	//		if (dispensation.getLocation() == null) {
	//			dispensation.setLocation(SupplyUtils.getUserLocation());
	//		}
	//		return getService().saveProductDispensation(dispensation);
	//	}
	
	//	@Override
	//	public void purge(ProductDispensation productOperation, RequestContext requestContext) throws ResponseException {
	//		getService().purgeProductOperation(productOperation);
	//	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = super.getRepresentationDescription(Representation.FULL);
			description.addProperty("encounter", Representation.FULL);
			description.addProperty("provider", Representation.DEFAULT);
			description.addProperty("age");
			description.addProperty("gender");
			description.addProperty("goal");
			description.addProperty("prescriptionDate");
			description.addProperty("treatmentDuration");
			description.addProperty("treatmentEndDate");
			description.addProperty("productRegime", Representation.DEFAULT);
			description.addProperty("regimeLine");
			//			description.addProperty("productOperation", Representation.FULL);
			description.addProperty("uuid");
		} else if (representation instanceof DefaultRepresentation) {
			description = super.getRepresentationDescription(Representation.DEFAULT);
			description.addProperty("encounter", Representation.DEFAULT);
			description.addProperty("provider", Representation.REF);
			description.addProperty("age");
			description.addProperty("gender");
			description.addProperty("goal");
			description.addProperty("prescriptionDate");
			description.addProperty("treatmentDuration");
			description.addProperty("treatmentEndDate");
			description.addProperty("productRegime", Representation.DEFAULT);
			description.addProperty("regimeLine");
			//			description.addProperty("productOperation", Representation.DEFAULT);
			description.addProperty("uuid");
		} else if (representation instanceof RefRepresentation) {
			description = super.getRepresentationDescription(Representation.REF);
			description.addProperty("encounter", Representation.REF);
			description.addProperty("provider", Representation.REF);
			description.addProperty("age");
			description.addProperty("gender");
			description.addProperty("goal");
			description.addProperty("prescriptionDate");
			description.addProperty("treatmentDuration");
			description.addProperty("treatmentEndDate");
			description.addProperty("productRegime", Representation.REF);
			description.addProperty("regimeLine");
			description.addProperty("uuid");
		}
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(Representation.DEFAULT);
		model.property("age", new IntegerProperty()).property("gender", new StringProperty())
		        .property("goal", new StringProperty()).property("provider", new RefProperty("#/definitions/ProviderGet"))
		        .property("treatmentDuration", new StringProperty())
		        .property("encounter", new RefProperty("#/definitions/EncounterGet"))
		        .property("treatmentEndDate", new DateProperty()).property("prescriptionDate", new DateProperty())
		        .property("productRegime", new RefProperty("#/definitions/ProductRegimeGet"))
		        .property("regimeLine", new IntegerProperty())
		        .property("productOperation", new RefProperty("#/definitions/ProductOperationGet"));
		
		model.required("productOperation");
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getCREATEModel(Representation.FULL);
		model.property("age", new IntegerProperty()).property("gender", new StringProperty())
		        .property("goal", new StringProperty()).property("provider", new RefProperty("#/definitions/ProviderGet"))
		        .property("treatmentDuration", new StringProperty())
		        .property("encounter", new RefProperty("#/definitions/EncounterCreate"))
		        .property("treatmentEndDate", new DateProperty()).property("prescriptionDate", new DateProperty())
		        .property("productRegime", new RefProperty("#/definitions/ProductRegimeGet"))
		        .property("regimeLine", new IntegerProperty())
		//		        .property("productOperation", new RefProperty("#/definitions/ProductOperationCreate"))
		;
		
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getUPDATEModel(Representation.FULL);
		model.property("age", new IntegerProperty()).property("gender", new StringProperty())
		        .property("goal", new StringProperty()).property("provider", new RefProperty("#/definitions/ProviderGet"))
		        .property("treatmentDuration", new StringProperty())
		        .property("encounter", new RefProperty("#/definitions/EncounterUpdate"))
		        .property("treatmentEndDate", new DateProperty()).property("prescriptionDate", new DateProperty())
		        .property("productRegime", new RefProperty("#/definitions/ProductRegimeGet"))
		        .property("regimeLine", new IntegerProperty())
		//		        .property("productOperation", new RefProperty("#/definitions/ProductOperationUpdate"))
		;
		
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = super.getCreatableProperties();
		description.addProperty("encounter");
		description.addProperty("provider");
		description.addProperty("age");
		description.addProperty("gender");
		description.addProperty("goal");
		description.addProperty("prescriptionDate");
		description.addProperty("treatmentDuration");
		description.addProperty("treatmentEndDate");
		description.addProperty("productRegime");
		description.addProperty("regimeLine");
		//		description.addRequiredProperty("productOperation");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = super.getUpdatableProperties();
		description.addProperty("encounter");
		description.addProperty("provider");
		description.addProperty("age");
		description.addProperty("gender");
		description.addProperty("goal");
		description.addProperty("prescriptionDate");
		description.addProperty("treatmentDuration");
		description.addProperty("treatmentEndDate");
		description.addProperty("productRegime");
		description.addProperty("regimeLine");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<ProductDispensation>(getService().getAllProductDispensation(SupplyUtils.getUserLocation(),
		    false), context);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		// String period = context.getParameter("period");
		String program = context.getParameter("program");
		String filter = context.getParameter("filter");
		
		//		List<ProductDispensation> productOperations = new ArrayList<ProductDispensation>();
		if (StringUtils.isNotEmpty(filter) && filter.contains("identifier")) {
			String identifier = filter.split(":")[1];
			List<ProductDispensation> productDispensationList = getService().getAllProductDispensation(identifier,
			    SupplyUtils.getUserLocation(), false);
			if (productDispensationList != null) {
				return new NeedsPaging<ProductDispensation>(productDispensationList, context);
			}
		}
		return super.doSearch(context);
	}
}
