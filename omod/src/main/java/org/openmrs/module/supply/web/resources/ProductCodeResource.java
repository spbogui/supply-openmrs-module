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

@Resource(name = RestConstants.VERSION_1 + SupplyResourceController.SUPPLY_REST_NAMESPACE + "Code", supportedClass = ProductCode.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" })
public class ProductCodeResource extends DataDelegatingCrudResource<ProductCode> {
	
	//	@PropertyGetter("prices")
	//	public static Set<ProductPrice> getPrices(ProductCode productCode) {
	//		return new LinkedHashSet<ProductPrice>(productCode.getPrices());
	//	}
	//
	//	@PropertySetter("prices")
	//	public static void setPrices(ProductCode productCode, List<ProductPrice> prices)
	//	        throws ResourceDoesNotSupportOperationException {
	//		if (productCode.getPrices() != null && productCode.getPrices().containsAll(prices)) {
	//			return;
	//		}
	//		for (ProductPrice price : prices) {
	//			ProductPrice existingPrice = productCode.getPrices() != null ? getMatchingPrice(price, productCode.getPrices())
	//			        : null;
	//			if (existingPrice != null) {
	//				copyPriceFields(existingPrice, price);
	//			} else {
	////				productCode.addPrice(price);
	//			}
	//		}
	//	}
	
	//	private static void copyPriceFields(ProductPrice existingPrice, ProductPrice price) {
	//		existingPrice.setActive(price.getActive());
	//		existingPrice.setPurchasePrice(price.getPurchasePrice());
	//		existingPrice.setSalePrice(price.getSalePrice());
	//	}
	//
	//	private static ProductPrice getMatchingPrice(ProductPrice price, Set<ProductPrice> prices) {
	//		for (ProductPrice existingPrice : prices) {
	//			String uuid = price.getUuid();
	//			if (uuid != null && uuid.equals(existingPrice.getUuid())) {
	//				return existingPrice;
	//			}
	//		}
	//		return null;
	//	}
	
	//	@PropertyGetter("regimes")
	//	private static Set<ProductRegime> getRegimes(ProductCode productCode) {
	//		return new LinkedHashSet<ProductRegime>(productCode.getRegimes());
	//	}
	//
	//	@PropertySetter("regimes")
	//	private static void setRegimes(ProductCode instance, List<ProductRegime> regimes) {
	//		instance.getRegimes().clear();
	//		for (ProductRegime regime : regimes)
	//			instance.addRegime(regime);
	//	}
	
	ProductService getService() {
		return Context.getService(ProductService.class);
	}
	
	@Override
	public ProductCode getByUniqueId(String s) {
		ProductCode productCode = getService().getProductCode(s);
		if (productCode == null) {
			productCode = getService().getProductCodeByCode(s);
		}
		return productCode;
	}
	
	@Override
	protected void delete(ProductCode productCode, String s, RequestContext requestContext) throws ResponseException {
		
	}
	
	@Override
	public ProductCode newDelegate() {
		return new ProductCode();
	}
	
	@Override
	public ProductCode save(ProductCode productCode) {
		return getService().saveProductCode(productCode);
	}
	
	@Override
	public void purge(ProductCode productCode, RequestContext requestContext) throws ResponseException {
		
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("code");
			description.addProperty("product", Representation.DEFAULT);
			//			description.addProperty("prices", Representation.DEFAULT);
			//			description.addProperty("currentPrice", Representation.DEFAULT);
			//			description.addProperty("program", Representation.DEFAULT);
			//			description.addProperty("regimes", Representation.DEFAULT);
			description.addProperty("quantityInStock");
			description.addProperty("uuid");
		} else if (representation instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("code");
			description.addProperty("product", Representation.REF);
			//			description.addProperty("prices", Representation.REF);
			//			description.addProperty("currentPrice", Representation.REF);
			description.addProperty("program", Representation.REF);
			//			description.addProperty("regimes", Representation.REF);
			description.addProperty("uuid");
		} else if (representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("code");
			description.addProperty("product", Representation.REF);
			description.addProperty("program", Representation.REF);
			//			description.addProperty("currentPrice", Representation.REF);
			description.addProperty("uuid");
		}
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("code", new StringProperty()).property("product", new RefProperty("#/definitions/ProductGet"))
		//		        .property("prices", new RefProperty("#/definitions/ProductPriceGet"))
		        .property("program", new RefProperty("#/definitions/ProductProgramGet"))
		        //		        .property("regimes", new RefProperty("#/definitions/ProductRegimeGet"))
		        .property("uuid", new StringProperty());
		return super.getGETModel(rep);
	}
	
	@Override
	public List<String> getPropertiesToExposeAsSubResources() {
		return Arrays.asList("");
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("code");
		description.addRequiredProperty("product");
		description.addRequiredProperty("program");
		//		description.addProperty("prices");
		//		description.addProperty("regimes");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("code", new StringProperty());
		
		if (rep instanceof FullRepresentation) {
			model.property("product", new RefProperty("#/definitions/ProductGet")).property("program",
			    new RefProperty("#/definitions/ProductProgramCreate"));
			//			        .property("prices", new RefProperty("#/definitions/ProductPriceCreate"))
			//			        .property("regimes", new RefProperty("#/definitions/ProductRegimeCreate"));
		} else {
			model.property("product", new StringProperty().example("uuid")).property("program",
			    new StringProperty().example("uuid"));
			//			        .property("prices", new StringProperty().example("uuid"))
			//			        .property("regimes", new StringProperty().example("uuid"));
		}
		
		model.required("product").required("code").required("program");
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("code", new StringProperty()).property("product", new StringProperty().example("uuid"))
		        .property("program", new StringProperty().example("uuid"))
		//		        .property("regimes", new StringProperty().example("uuid"))
		;
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("code");
		description.addProperty("product");
		description.addProperty("program");
		//		description.addProperty("regimes");
		//		description.addProperty("prices");
		description.addProperty("uuid");
		
		return description;
	}
	
	@Override
	protected NeedsPaging<ProductCode> doGetAll(RequestContext context) throws ResponseException {
		List<ProductCode> products = getService().getProductCodes(false);
		return new NeedsPaging<ProductCode>(products, context);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		
		String code = context.getRequest().getParameter("code");
		String programUuid = context.getRequest().getParameter("program");
		String filter = context.getRequest().getParameter("filter");
		
		List<ProductCode> productCodes = new ArrayList<ProductCode>();
		
		if (StringUtils.isNotBlank(code)) {
			ProductCode productCode = getService().getProductCodeByCode(code);
			if (productCode != null) {
				productCodes.add(productCode);
			}
		} else if (StringUtils.isNotBlank(programUuid)) {
			ProductProgram program = getService().getProductProgram(programUuid);
			if (program != null) {
				if (StringUtils.isNotBlank(filter)) {
					if (filter.contains("regime")) {
						String[] regimeInfo = filter.split(":");
						if (regimeInfo.length == 2) {
							String regime = filter.split(":")[1];
							ProductRegime productRegime = getService().getProductRegimeByConceptName(regime);
							if (productRegime != null) {
								List<ProductCode> result = getService().getProductCodes(program, productRegime);
								if (result != null) {
									productCodes.addAll(result);
								}
							}
						}
					}
				} else {
					//                    productCodes.addAll(program.getProductCodes());
					List<ProductCode> productCodeArrayList = getService().getProductCodes(program, false);
					if (productCodeArrayList != null) {
						productCodes.addAll(productCodeArrayList);
					}
					
				}
			}
		}
		return new NeedsPaging<ProductCode>(productCodes, context);
	}
	
}
