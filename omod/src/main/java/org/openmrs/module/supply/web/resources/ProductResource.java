package org.openmrs.module.supply.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.*;
import org.openmrs.module.supply.api.ProductService;
import org.openmrs.module.supply.web.controller.SupplyResourceController;
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

import java.util.*;

@Resource(name = RestConstants.VERSION_1 + SupplyResourceController.SUPPLY_REST_NAMESPACE, supportedClass = Product.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" })
public class ProductResource extends DataDelegatingCrudResource<Product> {
	
	@PropertyGetter("names")
	public static Set<ProductName> getNames(Product product) {
		return new LinkedHashSet<ProductName>(product.getNames());
	}
	
	@PropertySetter("names")
	public static void setNames(Product product, List<ProductName> names) throws ResourceDoesNotSupportOperationException {
		if (product.getNames() != null && product.getNames().containsAll(names)) {
			return;
		}
		//        if (product.getNames() != null && !product.getNames().isEmpty()) {
		//            throw new ResourceDoesNotSupportOperationException("names can only be set for newly created objects !");
		//        }
		
		for (ProductName name : names) {
			ProductName existingName = product.getNames() != null ? getMatchingName(name, product.getNames()) : null;
			if (existingName != null) {
				copyNameFields(existingName, name);
			} else {
				product.addName(name);
			}
		}
	}
	
	private static void copyNameFields(ProductName existingName, ProductName name) {
		existingName.setName(name.getName());
		existingName.setProductNameType(name.getProductNameType());
		existingName.setUnit(name.getUnit());
	}
	
	private static ProductName getMatchingName(ProductName name, Set<ProductName> names) {
		for (ProductName existingName : names) {
			String uuid = name.getUuid();
			if (uuid != null && uuid.equals(existingName.getUuid())) {
				return existingName;
			}
		}
		return null;
	}
	
	ProductService getService() {
		return Context.getService(ProductService.class);
	}
	
	@Override
	public Product getByUniqueId(String s) {
		Product product = getService().getProduct(s);
		if (product == null) {
			product = getService().getProductByCode(s);
		}
		return product;
	}
	
	@Override
	protected void delete(Product product, String s, RequestContext requestContext) throws ResponseException {
		
	}
	
	@Override
	public Product newDelegate() {
		return new Product();
	}
	
	@Override
	public Product save(Product product) {
		return getService().saveProduct(product);
	}
	
	@Override
	public void purge(Product product, RequestContext requestContext) throws ResponseException {
		
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("conversionUnit");
			description.addProperty("dispensationName");
			description.addProperty("dispensationUnit");
			description.addProperty("packagingName");
			description.addProperty("packagingUnit");
			description.addProperty("names", Representation.DEFAULT);
			description.addProperty("uuid");
		} else if (representation instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("conversionUnit");
			description.addProperty("dispensationName");
			description.addProperty("dispensationUnit");
			description.addProperty("packagingName");
			description.addProperty("packagingUnit");
			description.addProperty("names", Representation.REF);
			description.addProperty("uuid");
		} else if (representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("names");
			description.addProperty("conversionUnit");
			description.addProperty("dispensationName");
			description.addProperty("packagingName");
			description.addProperty("uuid");
		}
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("conversionUnit", new DoubleProperty()).property("names",
		    new RefProperty("#/definitions/ProductNameGet"));
		return super.getGETModel(rep);
	}
	
	@Override
	public List<String> getPropertiesToExposeAsSubResources() {
		return Collections.singletonList("names");
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("names");
		description.addRequiredProperty("conversionUnit");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("conversionUnit", new DoubleProperty());
		
		if (rep instanceof FullRepresentation) {
			model.property("names", new RefProperty("#/definitions/ProductNameGet"));
		} else {
			model.property("names", new StringProperty().example("uuid"));
		}
		
		model.required("names").required("conversionUnit");
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("conversionUnit", new DoubleProperty()).property("names", new StringProperty().example("uuid"));
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("names");
		description.addProperty("conversionUnit");
		
		return description;
	}
	
	@Override
	protected NeedsPaging<Product> doGetAll(RequestContext context) throws ResponseException {
		List<Product> products = getService().getAllProducts(false);
		return new NeedsPaging<Product>(products, context);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		
		//		String code = context.getRequest().getParameter("code");
		//		String programUuid = context.getRequest().getParameter("program");
		//		String filter = context.getRequest().getParameter("filter");
		
		List<Product> products = new ArrayList<Product>();
		
		return new NeedsPaging<Product>(products, context);
	}
	
}
