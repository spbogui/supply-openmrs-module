package org.openmrs.module.supply;

import org.apache.commons.lang.StringUtils;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.search.LuceneAnalyzers;
import org.openmrs.module.supply.api.ProductService;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Date;

@Entity(name = "ProductOperationAttribute")
@Table(name = "supply2_product_operation_attribute")
public class ProductOperationAttribute extends BaseOpenmrsData implements Serializable, Comparable<ProductOperationAttribute> {
	
	private static final long serialVersionUID = 11231211232111L;
	
	private static final Logger log = LoggerFactory.getLogger(ProductOperationAttribute.class);
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_operation_attribute_id", nullable = false)
	private Integer productOperationAttributeId;
	
	@ManyToOne
	@JoinColumn(nullable = false, name = "operation_attribute_type")
	private ProductOperationAttributeType operationAttributeType;
	
	@Column(name = "value", nullable = false)
	private String value;
	
	@ManyToOne(cascade = CascadeType.ALL, optional = false)
	@JoinColumn(name = "product_operation", nullable = false)
	private ProductOperation operation;
	
	@SuppressWarnings("JpaAttributeTypeInspection")
	@ManyToOne
	@JoinColumn(name = "location_id", nullable = false)
	private Location location;
	
	public ProductOperationAttribute() {
	}
	
	/**
	 * Constructor for creating a basic attribute
	 * 
	 * @param type PersonAttributeType
	 * @param value String
	 */
	public ProductOperationAttribute(ProductOperationAttributeType type, String value) {
		this.operationAttributeType = type;
		this.value = value;
	}
	
	/**
	 * Shallow copy of this PersonAttribute. Does NOT copy personAttributeId
	 * 
	 * @return a shallows copy of <code>this</code>
	 */
	public ProductOperationAttribute copy() {
		return copyHelper(new ProductOperationAttribute());
	}
	
	/**
	 * The purpose of this method is to allow subclasses of PersonAttribute to delegate a portion of
	 * their copy() method back to the superclass, in case the base class implementation changes.
	 * 
	 * @param target a PersonAttribute that will have the state of <code>this</code> copied into it
	 * @return Returns the PersonAttribute that was passed in, with state copied into it
	 */
	protected ProductOperationAttribute copyHelper(ProductOperationAttribute target) {
		target.setOperation(getOperation());
		target.setOperationAttributeType(getOperationAttributeType());
		target.setValue(getValue());
		return target;
	}
	
	public Integer getProductOperationAttributeId() {
		return productOperationAttributeId;
	}
	
	public void setProductOperationAttributeId(Integer productOperationId) {
		this.productOperationAttributeId = productOperationId;
	}
	
	public ProductOperationAttributeType getOperationAttributeType() {
		return operationAttributeType;
	}
	
	public void setOperationAttributeType(ProductOperationAttributeType operationAttributeType) {
		this.operationAttributeType = operationAttributeType;
	}
	
	@Fields({
	        @Field(name = "valuePhrase", analyzer = @Analyzer(definition = LuceneAnalyzers.PHRASE_ANALYZER), boost = @Boost(8f)),
	        @Field(name = "valueExact", analyzer = @Analyzer(definition = LuceneAnalyzers.EXACT_ANALYZER), boost = @Boost(4f)),
	        @Field(name = "valueStart", analyzer = @Analyzer(definition = LuceneAnalyzers.START_ANALYZER), boost = @Boost(2f)),
	        @Field(name = "valueAnywhere", analyzer = @Analyzer(definition = LuceneAnalyzers.ANYWHERE_ANALYZER)) })
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public ProductOperation getOperation() {
		return operation;
	}
	
	public void setOperation(ProductOperation operation) {
		this.operation = operation;
	}
	
	//
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	@Override
	public Integer getId() {
		return productOperationAttributeId;
	}
	
	@Override
	public void setId(Integer integer) {
		productOperationAttributeId = integer;
	}
	
	@Override
	public String getUuid() {
		return super.getUuid();
	}
	
	@Override
	public void setUuid(String uuid) {
		super.setUuid(uuid);
	}
	
	public boolean equalsContent(ProductOperationAttribute otherAttribute) {
        boolean returnValue = true;

        // these are the methods to compare.
        String[] methods = {"getOperationAttributeType", "getValue", "getVoided"};

        Class<? extends ProductOperationAttribute> attributeClass = this.getClass();

        // loop over all of the selected methods and compare this and other
        for (String methodAttribute : methods) {
            try {
                Method method = attributeClass.getMethod(methodAttribute);

                Object thisValue = method.invoke(this);
                Object otherValue = method.invoke(otherAttribute);

                if (otherValue != null) {
                    returnValue &= otherValue.equals(thisValue);
                }

            } catch (NoSuchMethodException e) {
                log.warn("No such method for comparison " + methodAttribute, e);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("Error while comparing attributes", e);
            }

        }

        return returnValue;
    }
	
	@Override
	public String toString() {
		Object o = getHydratedObject();
		if (o instanceof Attributable) {
			return ((Attributable<?>) o).getDisplayString();
		} else if (o != null) {
			return o.toString();
		}
		
		return this.value;
	}
	
	/**
	 * Will try to create an object of class 'PersonAttributeType.format'. If that implements
	 * <code>Attributable</code>, hydrate(value) is called. Defaults to just returning getValue()
	 * 
	 * @return hydrated object or getValue() <strong>Should</strong> load class in format property
	 *         <strong>Should</strong> still load class in format property if not Attributable
	 */
	public Object getHydratedObject() {
		
		if (getValue() == null) {
			return null;
		}
		
		try {
			Class<?> c = OpenmrsClassLoader.getInstance().loadClass(getOperationAttributeType().getFormat());
			try {
				Object o = c.newInstance();
				if (o instanceof Attributable) {
					Attributable attr = (Attributable) o;
					return attr.hydrate(getValue());
				} else if (o instanceof Encounter) {
					return Context.getEncounterService().getEncounterByUuid(getValue());
				} else if (o instanceof Provider) {
					return Context.getProviderService().getProviderByUuid(getValue());
				} else if (o instanceof ProductRegime) {
					return Context.getService(ProductService.class).getProductRegime(getValue());
				}
				//				else if (o instanceof Date) {
				//					return o;
				//				}
			}
			catch (InstantiationException e) {
				// try to hydrate the object with the String constructor
				log.trace("Unable to call no-arg constructor for class: " + c.getName());
				return c.getConstructor(String.class).newInstance(getValue());
			}
		}
		catch (Exception e) {
			
			// No need to warn if the input was blank
			if (StringUtils.isBlank(getValue())) {
				return null;
			}
			
			log.warn("Unable to hydrate value: " + getValue() + " for type: " + getOperationAttributeType(), e);
		}
		
		log.debug("Returning value: '" + getValue() + "'");
		return getValue();
	}
	
	public void voidAttribute(String reason) {
		setVoided(true);
		setVoidedBy(Context.getAuthenticatedUser());
		setVoidReason(reason);
		setDateVoided(new Date());
	}
	
	@Override
	public int compareTo(ProductOperationAttribute o) {
		DefaultComparator paDComparator = new DefaultComparator();
		return paDComparator.compare(this, o);
	}
	
	/**
	 * Provides a default comparator.
	 * 
	 * @since 1.12
	 **/
	public static class DefaultComparator implements Comparator<ProductOperationAttribute>, Serializable {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public int compare(ProductOperationAttribute pa1, ProductOperationAttribute pa2) {
			int retValue;
			if ((retValue = OpenmrsUtil.compareWithNullAsGreatest(pa1.getOperationAttributeType(),
			    pa2.getOperationAttributeType())) != 0) {
				return retValue;
			}
			
			if ((retValue = pa1.getVoided().compareTo(pa2.getVoided())) != 0) {
				return retValue;
			}
			
			if ((retValue = OpenmrsUtil.compareWithNullAsLatest(pa1.getDateCreated(), pa2.getDateCreated())) != 0) {
				return retValue;
			}
			
			if ((retValue = OpenmrsUtil.compareWithNullAsGreatest(pa1.getValue(), pa2.getValue())) != 0) {
				return retValue;
			}
			
			return OpenmrsUtil.compareWithNullAsGreatest(pa1.getProductOperationAttributeId(),
			    pa2.getProductOperationAttributeId());
		}
	}
}
