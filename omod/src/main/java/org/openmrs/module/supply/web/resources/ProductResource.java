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
	
	@PropertyGetter("prices")
	public static Set<ProductPrice> getPrices(Product product) {
		return new LinkedHashSet<ProductPrice>(product.getPrices());
	}
	
	@PropertySetter("prices")
	public static void setPrices(Product product, List<ProductPrice> prices) throws ResourceDoesNotSupportOperationException {
		if (product.getPrices() != null && product.getPrices().containsAll(prices)) {
			return;
		}
		for (ProductPrice price : prices) {
			ProductPrice existingPrice = product.getPrices() != null ? getMatchingPrice(price, product.getPrices()) : null;
			if (existingPrice != null) {
				copyPriceFields(existingPrice, price);
			} else {
				product.addPrice(price);
			}
		}
	}
	
	private static void copyPriceFields(ProductPrice existingPrice, ProductPrice price) {
		existingPrice.setActive(price.getActive());
		existingPrice.setPurchasePrice(price.getPurchasePrice());
		existingPrice.setSalePrice(price.getSalePrice());
	}
	
	private static ProductPrice getMatchingPrice(ProductPrice price, Set<ProductPrice> prices) {
		for (ProductPrice existingPrice : prices) {
			String uuid = price.getUuid();
			if (uuid != null && uuid.equals(existingPrice.getUuid())) {
				return existingPrice;
			}
		}
		return null;
	}
	
	@PropertyGetter("programs")
	private static Set<ProductProgram> getPrograms(Product product) {
		return new LinkedHashSet<ProductProgram>(product.getPrograms());
	}
	
	@PropertySetter("programs")
	private static void setPrograms(Product instance, List<ProductProgram> programs) {
		instance.getPrograms().clear();
		for (ProductProgram program : programs)
			instance.addProgram(program);
	}
	
	@PropertyGetter("regimes")
	private static Set<ProductRegime> getRegimes(Product product) {
		return new LinkedHashSet<ProductRegime>(product.getRegimes());
	}
	
	@PropertySetter("regimes")
	private static void setRegimes(Product instance, List<ProductRegime> regimes) {
		instance.getRegimes().clear();
		for (ProductRegime regime : regimes)
			instance.addRegime(regime);
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
			description.addProperty("code");
			description.addProperty("conversionUnit");
			description.addProperty("dispensationName");
			description.addProperty("packagingName");
			description.addProperty("names", Representation.DEFAULT);
			description.addProperty("prices", Representation.DEFAULT);
			description.addProperty("currentPrice", Representation.DEFAULT);
			description.addProperty("programs", Representation.DEFAULT);
			description.addProperty("regimes", Representation.DEFAULT);
			description.addProperty("stock");
			description.addProperty("stockAll");
			description.addProperty("uuid");
		} else if (representation instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("code");
			description.addProperty("conversionUnit");
			description.addProperty("dispensationName");
			description.addProperty("packagingName");
			description.addProperty("names", Representation.REF);
			description.addProperty("prices", Representation.REF);
			description.addProperty("currentPrice", Representation.REF);
			description.addProperty("programs", Representation.REF);
			description.addProperty("regimes", Representation.REF);
			description.addProperty("stock");
			description.addProperty("stockAll");
			description.addProperty("uuid");
		} else if (representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("code");
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
		model.property("code", new StringProperty()).property("conversionUnit", new DoubleProperty())
		        .property("names", new RefProperty("#/definitions/ProductNameGet"))
		        .property("prices", new RefProperty("#/definitions/ProductPriceGet"))
		        .property("programs", new RefProperty("#/definitions/ProductProgramGet"))
		        .property("regimes", new RefProperty("#/definitions/ProductRegimeGet"));
		return super.getGETModel(rep);
	}
	
	@Override
	public List<String> getPropertiesToExposeAsSubResources() {
		return Arrays.asList("names", "prices");
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("code");
		description.addRequiredProperty("names");
		description.addRequiredProperty("conversionUnit");
		description.addProperty("prices");
		description.addProperty("programs");
		description.addProperty("regimes");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("code", new StringProperty()).property("conversionUnit", new DoubleProperty());
		
		if (rep instanceof FullRepresentation) {
			model.property("names", new RefProperty("#/definitions/ProductNameGet"))
			        .property("programs", new RefProperty("#/definitions/ProductProgramCreate"))
			        .property("prices", new RefProperty("#/definitions/ProductPriceCreate"))
			        .property("regimes", new RefProperty("#/definitions/ProductRegimeCreate"));
		} else {
			model.property("names", new StringProperty().example("uuid"))
			        .property("programs", new StringProperty().example("uuid"))
			        .property("prices", new StringProperty().example("uuid"))
			        .property("regimes", new StringProperty().example("uuid"));
		}
		
		model.required("names").required("code").required("conversionUnit");
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("code", new StringProperty()).property("conversionUnit", new DoubleProperty())
		        .property("names", new StringProperty().example("uuid"))
		        .property("programs", new StringProperty().example("uuid"))
		        .property("regimes", new StringProperty().example("uuid"));
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("code");
		description.addProperty("names");
		description.addProperty("conversionUnit");
		description.addProperty("programs");
		description.addProperty("regimes");
		description.addProperty("prices");
		
		return description;
	}
	
	@Override
	protected NeedsPaging<Product> doGetAll(RequestContext context) throws ResponseException {
		List<Product> products = getService().getAllProducts(false);
		return new NeedsPaging<Product>(products, context);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		
		String code = context.getRequest().getParameter("code");
		String programUuid = context.getRequest().getParameter("program");
		// String regime = context.getRequest().getParameter("regime");
		String filter = context.getRequest().getParameter("filter");
		
		List<Product> products = new ArrayList<Product>();
		
		if (StringUtils.isNotBlank(code)) {
			Product product = getService().getProductByCode(code);
			if (product != null) {
				products.add(product);
			}
		} else if (StringUtils.isNotBlank(programUuid)) {
			ProductProgram program = getService().getProductProgram(programUuid);
			if (program != null) {
				if (StringUtils.isNotBlank(filter)) {
					if (filter.contains("regime")) {
						String[] regimeInfo = filter.split(":");
						if (regimeInfo.length < 2 && programUuid.contains("PNLSARVIO")) {
							List<Product> obtainedProducts = getService().getProductWithoutRegimeByProgram(program);
							if (obtainedProducts != null) {
								products.addAll(obtainedProducts);
							}
						} else {
							String regime = filter.split(":")[1];
							ProductRegime productRegime = getService().getProductRegimeByConceptName(regime);
							if (productRegime != null) {
								products.addAll(productRegime.getProducts());
							}
						}
					}
				} else {
					products.addAll(program.getProducts());
				}
			}
		}
		return new NeedsPaging<Product>(products, context);
	}
	
}
