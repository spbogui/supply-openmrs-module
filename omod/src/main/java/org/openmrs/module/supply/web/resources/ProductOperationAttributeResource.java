package org.openmrs.module.supply.web.resources;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Attributable;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.ProductOperation;
import org.openmrs.module.supply.ProductOperationAttribute;
import org.openmrs.module.supply.ProductOperationAttributeType;
import org.openmrs.module.supply.api.ProductOperationService;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.List;

@SubResource(parent = ProductOperationResource.class, path = "attribute", supportedClass = ProductOperationAttribute.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.11.*", "1.12.*", "2.*" })
public class ProductOperationAttributeResource extends DelegatingSubResource<ProductOperationAttribute, ProductOperation, ProductOperationResource> {
	
	ProductOperationService getService() {
		return Context.getService(ProductOperationService.class);
	}
	
	@Override
	public ProductOperationAttribute getByUniqueId(String s) {
		return getService().getOperationAttribute(s);
	}
	
	@Override
	protected void delete(ProductOperationAttribute attribute, String s, RequestContext requestContext)
	        throws ResponseException {
	}
	
	@Override
	public void purge(ProductOperationAttribute attribute, RequestContext requestContext) throws ResponseException {
	}
	
	@Override
	public ProductOperationAttribute newDelegate() {
		return new ProductOperationAttribute();
	}
	
	@Override
	public ProductOperationAttribute save(ProductOperationAttribute attribute) {
		return getService().saveOperationAttribute(attribute);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = null;
		if (representation instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("operationAttributeType", Representation.DEFAULT);
			description.addProperty("value");
			//            description.addProperty("hydratedObject");
			description.addProperty("auditInfo");
			description.addProperty("voided");
			description.addProperty("location", Representation.DEFAULT);
			description.addProperty("uuid");
		} else if (representation instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("operationAttributeType", Representation.REF);
			description.addProperty("value");
			description.addProperty("location", Representation.REF);
			description.addProperty("uuid");
		} else if (representation instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("operationAttributeType", Representation.REF);
			description.addProperty("display");
			description.addProperty("value");
			description.addProperty("uuid");
		}
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("operationAttributeType", new RefProperty("#/definitions/ProductOperationAttributeTypeGet"))
		        .property("value", new StringProperty()).property("location", new RefProperty("#/definitions/LocationGet"))
		        .property("uuid", new StringProperty());
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("operationAttributeType");
		description.addRequiredProperty("value");
		description.addRequiredProperty("location");
		description.addProperty("uuid");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("operationAttributeType", new StringProperty().example("uuid"))
		        .property("location", new StringProperty().example("uuid")).property("value", new StringProperty())
		        .property("uuid", new StringProperty()).required("operationAttributeType").required("value")
		        .required("location");
		
		if (rep instanceof FullRepresentation) {
			model.property("location", new RefProperty("#/definitions/LocationCreate")).property("operationAttributeType",
			    new RefProperty("#/definitions/ProductOperationAttributeTypeCreate"));
		}
		return model;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("operationAttributeType");
		description.addProperty("value");
		description.addProperty("location");
		return description;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		model.property("operationAttributeType", new StringProperty()).property("value", new StringProperty());
		return model;
	}
	
	@Override
	public ProductOperation getParent(ProductOperationAttribute operationAttribute) {
		return operationAttribute.getOperation();
	}
	
	@Override
	public void setParent(ProductOperationAttribute operationAttribute, ProductOperation productOperation) {
		operationAttribute.setOperation(productOperation);
	}
	
	@Override
    public PageableResult doGetAll(ProductOperation productOperation, RequestContext requestContext)
            throws ResponseException {
        List<ProductOperationAttribute> attributes = new ArrayList<ProductOperationAttribute>();

        final String attributeType = requestContext.getRequest().getParameter("attributeType");

        if (StringUtils.isNotBlank(attributeType)) {
            productOperation.getAttributes().stream()
                    .filter(a -> a.getOperationAttributeType().getUuid().equals(attributeType) && !a.getVoided())
                    .findFirst().ifPresent(attributes::add);
        } else {
            for (ProductOperationAttribute attribute : productOperation.getAttributes()) {
                if (!attribute.getVoided()) {
                    attributes.add(attribute);
                }
            }
        }
        return new NeedsPaging<ProductOperationAttribute>(attributes, requestContext);
    }
	
	@PropertyGetter("display")
	public String getDisplayString(ProductOperationAttribute attribute) {
		if (attribute.getOperationAttributeType() == null)
			return "";
		
		return attribute.getOperationAttributeType().getName() + " : " + attribute.getValue();
	}
	
	//    @PropertySetter("hydratedObject")
	//    public void setHydratedObject(ProductOperationAttribute personAttribute, String attributableUuid) {
	//        try {
	//            Class<?> attributableClass = OpenmrsClassLoader.getInstance().loadClass(
	//                    personAttribute.getOperationAttributeType().getFormat());
	//            Attributable value = (Attributable) ConversionUtil.convert(attributableUuid, attributableClass);
	//            personAttribute.setValue(value.serialize());
	//        } catch (ClassNotFoundException e) {
	//            throw new APIException("Could not convert value to Attributable", e);
	//        }
	//    }
	//
	@PropertySetter("value")
	public void setValue(ProductOperationAttribute productOperationAttribute, String value) {
		ProductOperationAttributeType attributeType = productOperationAttribute.getOperationAttributeType();
		if (attributeType == null) {
			productOperationAttribute.setValue(value);
		} else {
			// Check if expected value is attributable and do the right thing.
			try {
				String format = productOperationAttribute.getOperationAttributeType().getFormat();
				if (format == null) {
					productOperationAttribute.setValue(value);
				} else {
					Class<?> clazz = Context.loadClass(productOperationAttribute.getOperationAttributeType().getFormat());
					if (Attributable.class.isAssignableFrom(clazz)) {
						Attributable instance = (Attributable) ConversionUtil.convert(value, clazz);
						if (instance != null) {
							productOperationAttribute.setValue(instance.serialize());
						} else {
							// Could not find a corresponding domain object, so just set the value?
							productOperationAttribute.setValue(value);
						}
					} else {
						// Not Attributable just assign
						productOperationAttribute.setValue(value);
					}
				}
			}
			catch (ClassNotFoundException cnfe) {
				// No Class found? just assign the string
				productOperationAttribute.setValue(value);
			}
			catch (ConversionException ce) {
				// Couldn't convert? just assign the string
				productOperationAttribute.setValue(value);
			}
		}
	}
	
	@PropertyGetter("value")
	public Object getValue(ProductOperationAttribute pa) {
		Object value = pa.getHydratedObject();
		if (value == null) {
			return null;
		}
		
		return ConversionUtil.convertToRepresentation(value, Representation.DEFAULT);
	}
}
