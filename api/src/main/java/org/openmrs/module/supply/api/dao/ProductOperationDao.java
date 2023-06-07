package org.openmrs.module.supply.api.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.*;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.supply.*;
import org.openmrs.module.supply.api.ProductOperationService;
import org.openmrs.module.supply.api.ProductService;
import org.openmrs.module.supply.api.SupplyService;
import org.openmrs.module.supply.enumerations.Incidence;
import org.openmrs.module.supply.enumerations.OperationStatus;
import org.openmrs.module.supply.utils.SupplyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository("supply.ProductOperationDao")
public class ProductOperationDao {
	
	@Autowired
	DbSessionFactory sessionFactory;
	
	private DbSession getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	public Boolean validateOperation(ProductOperation operation) {
		if (!operation.getIncidence().equals(Incidence.NONE)) {
			return createStocks(operation);
		}
		return false;
	}
	
	public Boolean cancelOperation(ProductOperation operation) {
		if (!operation.getIncidence().equals(Incidence.NONE)) {
			return createStocks(operation);
		}
		return false;
	}
	
	private boolean createStocks(ProductOperation operation) {
		if (!operation.getOperationStatus().equals(OperationStatus.VALIDATED)
		        && !operation.getOperationStatus().equals(OperationStatus.DISABLED)
		        && !operation.getOperationStatus().equals(OperationStatus.TREATED)) {
			return false;
		}
		Set<ProductOperationFlux> fluxes = operation.getFluxes();
		if (fluxes != null && fluxes.size() != 0) {
			for (ProductOperationFlux flux : fluxes) {
				for (ProductOperationFluxAttribute fluxAttribute : flux.getAttributes()) {
					ProductAttributeStock attributeStock = new ProductAttributeStock();
					attributeStock.setOperation(operation);
					attributeStock.setAttribute(fluxAttribute.getAttribute());
					attributeStock.setLocation(fluxAttribute.getLocation());
					attributeStock.setQuantityInStock(getQuantity(operation, fluxAttribute));
					//					attributeStock.setProgram(operation.getProductProgram());
					saveProductAttributeStock(attributeStock);
				}
				
			}
			return true;
		}
		return false;
	}
	
	private Integer getQuantity(ProductOperation operation, ProductOperationFluxAttribute flux) {
		Integer quantityInStock = voidPreviousStock(operation, flux);
		if (operation.getOperationStatus().equals(OperationStatus.VALIDATED)
		        || operation.getOperationStatus().equals(OperationStatus.TREATED)) {
			if (operation.getIncidence().equals(Incidence.POSITIVE)) {
				return quantityInStock + flux.getQuantity().intValue();
			} else if (operation.getIncidence().equals(Incidence.NEGATIVE)) {
				return quantityInStock - flux.getQuantity().intValue();
			}
		} else {
			if (operation.getIncidence().equals(Incidence.POSITIVE)) {
				return quantityInStock - flux.getQuantity().intValue();
			} else if (operation.getIncidence().equals(Incidence.NEGATIVE)) {
				return quantityInStock + flux.getQuantity().intValue();
			}
		}
		return flux.getQuantity().intValue();
	}
	
	private Integer voidPreviousStock(ProductOperation operation, ProductOperationFluxAttribute flux) {
		Integer quantityInStock = 0;
		ProductAttributeStock attributeStock = getProductAttributeStock(flux.getAttribute(), operation.getProductProgram(),
		    operation.getLocation());
		if (attributeStock != null) {
			quantityInStock = attributeStock.getQuantityInStock();
			attributeStock.setVoided(true);
			attributeStock.setDateVoided(new Date());
			attributeStock.setVoidedBy(Context.getAuthenticatedUser());
			attributeStock.setVoidReason("Voided by user because not to be used");
			Context.getService(ProductOperationService.class).saveProductAttributeStock(attributeStock);
		}
		return quantityInStock;
	}
	
	public ProductOperation getProductOperation(Integer operationId) {
		return (ProductOperation) getSession().get(ProductOperation.class, operationId);
	}
	
	public ProductOperation getProductOperation(String uuid) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class);
		return (ProductOperation) criteria.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperation> getAllProductOperation(ProductOperationType operationType, Location location,
	        Boolean validatedOnly) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class);
		if (validatedOnly) {
			return criteria.add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("location", location))
			        .add(Restrictions.eq("voided", false))
			        .add(Restrictions.eq("operationStatus", OperationStatus.VALIDATED)).list();
		}
		return criteria.add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("location", location))
		        .add(Restrictions.eq("voided", false)).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperation> getAllProductOperation(ProductOperationType operationType, ProductProgram program,
	        Location location, Boolean validatedOnly, Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class);
		if (validatedOnly) {
			return criteria.add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("location", location))
			        .add(Restrictions.eq("program", program)).add(Restrictions.eq("voided", false))
			        .add(Restrictions.eq("operationStatus", OperationStatus.VALIDATED)).list();
		} else if (includeVoided) {
			return criteria.add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("program", program))
			        .add(Restrictions.eq("location", location)).list();
		}
		return criteria.add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("location", location))
		        .add(Restrictions.eq("program", program)).add(Restrictions.eq("voided", false)).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperation> getAllProductOperation(ProductOperationType operationType, Date startDate, Date endDate,
	        Location location, Boolean validatedOnly, Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class);
		if (validatedOnly) {
			return criteria.add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("location", location))
			        .add(Restrictions.between("operationDate", startDate, endDate)).add(Restrictions.eq("voided", false))
			        .add(Restrictions.eq("operationStatus", OperationStatus.VALIDATED)).list();
		} else if (includeVoided) {
			return criteria.add(Restrictions.eq("operationType", operationType))
			        .add(Restrictions.between("operationDate", startDate, endDate))
			        .add(Restrictions.eq("location", location)).list();
		}
		return criteria.add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("location", location))
		        .add(Restrictions.between("operationDate", startDate, endDate)).add(Restrictions.eq("voided", false)).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperation> getAllProductOperation(Location location, Boolean includeVoided) {
		//		ProductOperation latestInventory = getLastProductOperation(
		//		    getProductOperationType("INVENTORYOOOOOOOOOOOOOOOOOOOOOOOOOOOOO"), location, true, false);
		//		Criteria criteria = getSession().createCriteria(ProductOperation.class);
		//		if (latestInventory != null) {
		//			if (includeVoided) {
		//				return criteria.add(Restrictions.le("operationDate", latestInventory.getOperationDate()))
		//				        .add(Restrictions.eq("location", location)).list();
		//			}
		//			return criteria.add(Restrictions.le("operationDate", latestInventory.getOperationDate()))
		//			        .add(Restrictions.eq("location", location)).add(Restrictions.eq("voided", false)).list();
		//		}
		//		return criteria.add(Restrictions.le("operationDate", new Date())).add(Restrictions.eq("location", location))
		//		        .add(Restrictions.eq("voided", false)).list();
		return null;
	}
	
	public ProductOperation getLastProductOperation(ProductOperationType operationType, ProductProgram program,
	        Location location, Boolean validated, Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class)
		        .add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("productProgram", program))
		        .add(Restrictions.eq("location", location)).add(Restrictions.eq("voided", includeVoided));
		if (validated) {
			criteria.add(Restrictions.eq("operationStatus", OperationStatus.VALIDATED));
		} else {
			criteria.add(Restrictions.ne("operationStatus", OperationStatus.VALIDATED));
		}
		return (ProductOperation) criteria.addOrder(Order.desc("operationDate")).setMaxResults(1).uniqueResult();
		
	}
	
	public ProductOperation getLastProductOperation(ProductOperationType operationType, ProductProgram program,
	        Date limitEndDate, Location location, Boolean validated, Boolean includeVoided) throws APIException {
		Criteria criteria = getSession().createCriteria(ProductOperation.class);
		if (validated) {
			return (ProductOperation) criteria.add(Restrictions.eq("operationType", operationType))
			        .add(Restrictions.eq("location", location)).add(Restrictions.eq("voided", includeVoided))
			        .add(Restrictions.eq("operationStatus", OperationStatus.VALIDATED))
			        .add(Restrictions.eq("productProgram", program)).add(Restrictions.lt("operationDate", limitEndDate))
			        .addOrder(Order.desc("operationDate")).setMaxResults(1).uniqueResult();
		}
		//		if (includeVoided) {
		//			return (ProductOperation) criteria.add(Restrictions.eq("operationType", operationType))
		//			        .add(Restrictions.eq("location", location)).add(Restrictions.eq("productProgram", program))
		//			        .add(Restrictions.lt("operationDate", limitEndDate)).addOrder(Order.desc("operationDate"))
		//			        .setMaxResults(1).uniqueResult();
		//		}
		return (ProductOperation) criteria.add(Restrictions.eq("operationType", operationType))
		        .add(Restrictions.eq("productProgram", program)).add(Restrictions.eq("location", location))
		        .add(Restrictions.ne("operationStatus", OperationStatus.VALIDATED)).add(Restrictions.eq("voided", false))
		        .add(Restrictions.lt("operationDate", limitEndDate)).addOrder(Order.desc("operationDate")).setMaxResults(1)
		        .uniqueResult();
	}
	
	public ProductOperation getProductOperationByOperationNumber(ProductOperationType operationType, String operationNumber,
	        Location location, Boolean validated) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class);
		if (validated) {
			return (ProductOperation) criteria.add(Restrictions.eq("operationType", operationType))
			        .add(Restrictions.eq("location", location)).add(Restrictions.eq("voided", false))
			        .add(Restrictions.eq("operationStatus", OperationStatus.VALIDATED))
			        .add(Restrictions.eq("operationNumber", operationNumber)).addOrder(Order.desc("operationDate"))
			        .setMaxResults(1).uniqueResult();
		}
		return (ProductOperation) criteria.add(Restrictions.eq("operationType", operationType))
		        .add(Restrictions.eq("location", location)).add(Restrictions.eq("voided", false))
		        .add(Restrictions.eq("operationNumber", operationNumber)).addOrder(Order.desc("operationDate"))
		        .setMaxResults(1).uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperation> getProductOperationByOperationNumber(String operationNumber, Location location,
	        Boolean validated) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class);
		if (validated) {
			return (List<ProductOperation>) criteria.add(Restrictions.eq("location", location))
			        .add(Restrictions.eq("voided", false))
			        .add(Restrictions.eq("operationStatus", OperationStatus.VALIDATED))
			        .add(Restrictions.eq("operationNumber", operationNumber)).addOrder(Order.desc("operationDate")).list();
		}
		return (List<ProductOperation>) criteria.add(Restrictions.eq("location", location))
		        .add(Restrictions.eq("voided", false)).add(Restrictions.eq("operationNumber", operationNumber))
		        .add(Restrictions.ne("operationStatus", OperationStatus.VALIDATED)).addOrder(Order.desc("operationDate"))
		        .list();
	}
	
	public ProductOperation saveProductOperation(ProductOperation operation) {
		if (operation.getOperationStatus().equals(OperationStatus.NOT_COMPLETED)) {
			if (operation.getOperationType().getUuid().equals("INVENTORYOOOOOOOOOOOOOOOOOOOOOOOOOOOOO")) {
				if (operation.getFluxes().size() == 0) {
					ProductOperationAttribute attribute = SupplyUtils.getAttribute(operation,
					    "INVENTORYTYPEAAAAAAAAAAAAAAAAAAAAAAAAA");
					if (attribute != null && attribute.getValue().equals("TOTAL")) {
						operation.addAllFluxes(createInventoryFluxes(operation));
					}
				}
			} else if (operation.getOperationType().getUuid().equals("RECEPTIONOOOOOOOOOOOOOOOOOOOOOOOOOOOOO")
			        || operation.getOperationType().getUuid().equals("TRANSFERTINOOOOOOOOOOOOOOOOOOOOOOOOOOO")) {
				if (operation.getFluxes().size() == 0 && operation.getParentOperation() != null) {
					operation.addAllFluxes(createReceptionFluxes(operation, operation.getParentOperation()));
				}
			}
		} else if (operation.getOperationStatus().equals(OperationStatus.VALIDATED)) {
			System.out.println("-----------------------------------------> Entered in dispensation validation");
			if (operation.getOperationType().getUuid().equals("DISPENSATIONOOOOOOOOOOOOOOOOOOOOOOOOOO")) {
				Set<ProductOperationFlux> fluxes = operation.getFluxes();
				if (fluxes != null && fluxes.size() != 0) {
					
					for (ProductOperationFlux flux : fluxes) {
						Double quantity = flux.getQuantity();
						//                        Set<ProductOperationFluxAttribute> fluxAttributes = new HashSet<>();
						List<ProductAttributeStock> stocks = getAllProductAttributeStocks(operation.getLocation(),
						    operation.getProductProgram(), flux.getProduct());
						for (ProductAttributeStock stock : stocks) {
							
							ProductOperationFluxAttribute fluxAttribute = new ProductOperationFluxAttribute();
							fluxAttribute.setAttribute(stock.getAttribute());
							fluxAttribute.setLocation(flux.getLocation());
							fluxAttribute.setOperationFlux(flux);
							if (!fluxContainsAttribute(operation, stock.getAttribute())) {
								if (stock.getQuantityInStock() >= flux.getQuantity()) {
									fluxAttribute.setQuantity(quantity);
									saveOperationFluxAttribute(fluxAttribute);
									break;
								} else {
									fluxAttribute.setQuantity(stock.getQuantityInStock().doubleValue());
									quantity -= stock.getQuantityInStock().doubleValue();
									saveOperationFluxAttribute(fluxAttribute);
								}
							}
						}
						//						flux.setAttributes(fluxAttributes);
						saveProductOperationFlux(flux);
					}
				}
				
				Patient patient = Context.getService(SupplyService.class).getPatientByIdentifier(
				    operation.getOperationNumber());
				if (patient != null) {
					ProductDispensation dispensation = (ProductDispensation) operation /*getProductDispensation(productOperation.getUuid())*/;
					//                    if (dispensation != null) {
					//
					//                    }
					Encounter encounter = dispensation.getEncounter();
					if (encounter == null) {
						encounter = new Encounter();
						encounter.setLocation(operation.getLocation());
						encounter.setEncounterDatetime(operation.getOperationDate());
						encounter.setEncounterType(Context.getEncounterService().getEncounterType("DISPENSATION"));
						encounter.addProvider(Context.getEncounterService().getEncounterRole(1), dispensation.getProvider());
						encounter.setObs(SupplyUtils.getDispensationObsList(dispensation, patient));
						encounter.setPatient(patient);
					} else {
						Set<Obs> obsSet = new HashSet<Obs>();
						for (Obs obs : encounter.getAllObs()) {
							if (obs.getConcept().getConceptId()
							        .equals(SupplyUtils.getConceptIdInGlobalProperties("Regimen"))) {
								obs.setValueCoded(dispensation.getProductRegime().getConcept());
								obsSet.add(obs);
							} else if (obs.getConcept().getConceptId()
							        .equals(SupplyUtils.getConceptIdInGlobalProperties("RegimenLine"))) {
								if (dispensation.getRegimeLine() == 1) {
									obs.setValueCoded(Context.getConceptService().getConcept(164730));
								} else if (dispensation.getRegimeLine() == 2) {
									obs.setValueCoded(Context.getConceptService().getConcept(164732));
								} else if (dispensation.getRegimeLine() == 3) {
									obs.setValueCoded(Context.getConceptService().getConcept(164734));
								}
								obsSet.add(obs);
							} else if (obs.getConcept().getConceptId()
							        .equals(SupplyUtils.getConceptIdInGlobalProperties("Goal"))) {
								obs.setValueText(dispensation.getGoal());
								obsSet.add(obs);
							} else if (obs.getConcept().getConceptId()
							        .equals(SupplyUtils.getConceptIdInGlobalProperties("TreatmentDays"))) {
								obs.setValueNumeric(dispensation.getTreatmentDuration().doubleValue());
								obsSet.add(obs);
							} else if (obs.getConcept().getConceptId()
							        .equals(SupplyUtils.getConceptIdInGlobalProperties("TreatmentEndDate"))) {
								if (dispensation.getTreatmentEndDate() != null /*&& getProductRegimenId() != 105281*/) {
									obs.setValueDate(dispensation.getTreatmentEndDate());
									obsSet.add(obs);
								}
							}
						}
						encounter.setObs(obsSet);
					}
					
					dispensation.setEncounter(Context.getEncounterService().saveEncounter(encounter));
				}
			}
		}
		getSession().saveOrUpdate(operation);
		if (operation.getOperationStatus().equals(OperationStatus.VALIDATED)
		        || operation.getOperationStatus().equals(OperationStatus.TREATED)) {
			validateOperation(operation);
			if (operation.getOperationType().getUuid().equals("INVENTORYOOOOOOOOOOOOOOOOOOOOOOOOOOOOO")) {
				createInventoryAdjustment(operation);
			}
		} else if (operation.getOperationStatus().equals(OperationStatus.CANCELED)) {
			cancelOperation(operation);
		}
		return operation;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductAttributeStock> getAllProductAttributeStocks(Location location, ProductProgram program,
	        Product product) {
		return (List<ProductAttributeStock>) getSession().createCriteria(ProductAttributeStock.class, "stock")
		        .createAlias("stock.attribute", "attribute").add(Restrictions.eq("stock.location", location))
		        .add(Restrictions.eq("stock.voided", false)).add(Restrictions.eq("attribute.product", product))
		        .add(Restrictions.eq("stock.program", program)).addOrder(Order.desc("attribute.expiryDate")).list();
	}
	
	private List<ProductOperationFlux> createInventoryFluxes(ProductOperation operation) {
        List<ProductAttributeStock> stocks = getAllProductAttributeStocks(SupplyUtils.getUserLocation(), operation.getProductProgram());
        List<ProductOperationFlux> fluxes = new ArrayList<>();
        for (ProductAttributeStock stock : stocks) {
            if (!fluxContainsAttribute(operation, stock.getAttribute())) {
                ProductOperationFlux flux = createFlux(operation, 0., stock.getAttribute().getProductCode().getProduct());
                flux.setRelatedQuantity(stock.getQuantityInStock().doubleValue());
                flux.setRelatedQuantityLabel("Quantité Théortique");
                flux.setProduct(stock.getAttribute().getProductCode().getProduct());

                ProductOperationFluxAttribute attribute = new ProductOperationFluxAttribute();
                attribute.setAttribute(stock.getAttribute());
                attribute.setQuantity(stock.getQuantityInStock().doubleValue());
                attribute.setLocation(operation.getLocation());

                flux.addAttribute(attribute);
                fluxes.add(flux);
            }
        }
        return fluxes;
    }
	
	private List<ProductOperationFlux> createReceptionFluxes(ProductOperation operation, ProductOperation parentOperation) {
        List<ProductOperationFlux> fluxes = new ArrayList<>();
        for (ProductOperationFlux flux : parentOperation.getFluxes()) {

            if (!fluxContainsProduct(operation, flux.getProduct())) {
                ProductOperationFlux operationFlux = createFlux(operation, flux.getQuantity(), flux.getProduct());
                operationFlux.setRelatedQuantity(flux.getQuantity());
                operationFlux.setRelatedQuantityLabel("Quantité livree");

                flux.addAllAttributes(createFluxAttribute(flux.getAttributes(), flux.getLocation()));

                fluxes.add(flux);
            }
        }
        return fluxes;
    }
	
	private List<ProductOperationFluxAttribute> createFluxAttribute(Set<ProductOperationFluxAttribute> attributes, Location location) {
        List<ProductOperationFluxAttribute> fluxAttributes = new ArrayList<>();
        for (ProductOperationFluxAttribute fluxAttribute : attributes) {
            ProductOperationFluxAttribute attribute = new ProductOperationFluxAttribute();
            attribute.setAttribute(createAttribute(fluxAttribute.getAttribute(), location));
            attribute.setQuantity(fluxAttribute.getQuantity());
            attribute.setLocation(location);
            fluxAttributes.add(attribute);
        }

        return fluxAttributes;
    }
	
	private ProductAttribute createAttribute(ProductAttribute attribute, Location location) {
		
		ProductAttribute productAttribute = Context.getService(ProductService.class).getProductAttributeByBatchNumber(
		    attribute.getBatchNumber(), location);
		if (productAttribute != null) {
			return productAttribute;
		}
		productAttribute = new ProductAttribute();
		productAttribute.setProductCode(attribute.getProductCode());
		productAttribute.setBatchNumber(attribute.getBatchNumber());
		productAttribute.setLocation(location);
		productAttribute.setExpiryDate(attribute.getExpiryDate());
		return productAttribute;
	}
	
	private Boolean fluxContainsAttribute(ProductOperation operation, ProductAttribute attribute) {
		for (ProductOperationFlux flux : operation.getFluxes()) {
			Set<ProductOperationFluxAttribute> fluxAttributes = flux.getAttributes();
			for (ProductOperationFluxAttribute fluxAttribute : fluxAttributes) {
				if (fluxAttribute.getAttribute().equals(attribute)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private Boolean fluxContainsProduct(ProductOperation operation, Product product) {
		for (ProductOperationFlux flux : operation.getFluxes()) {
			if (flux.getProduct().equals(product)) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private List<ProductAttributeStock> getAllProductAttributeStocks(Location location, ProductProgram program) {
		return (List<ProductAttributeStock>) getSession().createCriteria(ProductAttributeStock.class)
		        .add(Restrictions.eq("location", location)).add(Restrictions.eq("voided", false))
		        .add(Restrictions.eq("program", program)).list();
	}
	
	private ProductOperationFlux createFlux(ProductOperation operation, Double quantity, Product product) {
		ProductOperationFlux operationFlux = new ProductOperationFlux();
		operationFlux.setQuantity(quantity);
		operationFlux.setLocation(operation.getLocation());
		operationFlux.setProduct(product);
		return operationFlux;
	}
	
	private void createInventoryAdjustment(ProductOperation operation) {
		ProductOperation positiveAdjustment = new ProductOperation();
		ProductOperation negativeAdjustment = new ProductOperation();
		
		for (ProductOperationFlux operationFlux : operation.getFluxes()) {
			if (operationFlux.getRelatedQuantity() != null) {
				if (operationFlux.getQuantity() < operationFlux.getRelatedQuantity()) {
					negativeAdjustment.addFlux(createFlux(operation,
					    operationFlux.getRelatedQuantity() - operationFlux.getQuantity(), operationFlux.getProduct()));
				} else if (operationFlux.getQuantity() > operationFlux.getRelatedQuantity()) {
					positiveAdjustment.addFlux(createFlux(operation,
					    operationFlux.getRelatedQuantity() - operationFlux.getQuantity(), operationFlux.getProduct()));
				}
			}
		}
		
		if (positiveAdjustment.getFluxes().size() != 0) {
			positiveAdjustment.setOperationDate(operation.getOperationDate());
			positiveAdjustment.setOperationType(getProductOperationType("INVENTORYADJUSTPOSITIVEOOOOOOOOOOOOO"));
			positiveAdjustment.setIncidence(Incidence.NONE);
			positiveAdjustment.setProductProgram(operation.getProductProgram());
			positiveAdjustment.setOperationStatus(OperationStatus.VALIDATED);
			positiveAdjustment.setLocation(operation.getLocation());
			
			saveProductOperation(positiveAdjustment);
		}
		if (positiveAdjustment.getFluxes().size() != 0) {
			negativeAdjustment.setOperationDate(operation.getOperationDate());
			negativeAdjustment.setOperationType(getProductOperationType("INVENTORYADJUSTNEGATIVEOOOOOOOOOOOOO"));
			negativeAdjustment.setIncidence(Incidence.NONE);
			negativeAdjustment.setProductProgram(operation.getProductProgram());
			negativeAdjustment.setOperationStatus(OperationStatus.VALIDATED);
			negativeAdjustment.setLocation(operation.getLocation());
			saveProductOperation(negativeAdjustment);
		}
		
	}
	
	public void purgeProductOperation(ProductOperation productOperation) {
		getSession().delete(productOperation);
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperationType> getAllProductOperationType() {
		return getSession().createCriteria(ProductOperationType.class).list();
	}
	
	public void purgeProductOperationType(ProductOperationType operationType) {
		getSession().delete(operationType);
	}
	
	public ProductOperationType saveProductOperationType(ProductOperationType operationType) {
		getSession().saveOrUpdate(operationType);
		return operationType;
	}
	
	public ProductOperationType getProductOperationType(Integer id) {
		return (ProductOperationType) getSession().get(ProductOperationType.class, id);
	}
	
	public ProductOperationType getProductOperationType(String uuid) {
		Criteria criteria = getSession().createCriteria(ProductOperationType.class);
		return (ProductOperationType) criteria.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperationAttributeType> getAllOperationAttributeType() {
		return getSession().createCriteria(ProductOperationAttributeType.class).list();
	}
	
	public void purgeOperationAttributeType(ProductOperationAttributeType operationAttributeType) {
		getSession().delete(operationAttributeType);
	}
	
	public ProductOperationAttributeType saveOperationAttributeType(ProductOperationAttributeType operationAttributeType) {
		getSession().saveOrUpdate(operationAttributeType);
		return operationAttributeType;
	}
	
	public ProductOperationAttributeType getOperationAttributeType(Integer id) {
		return (ProductOperationAttributeType) getSession().get(ProductOperationAttributeType.class, id);
	}
	
	public ProductOperationAttributeType getOperationAttributeType(String uuid) {
		Criteria criteria = getSession().createCriteria(ProductOperationAttributeType.class);
		return (ProductOperationAttributeType) criteria.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	public ProductOperationAttribute saveOperationAttribute(ProductOperationAttribute attribute) {
		getSession().saveOrUpdate(attribute);
		return attribute;
	}
	
	public ProductOperationAttribute getOperationAttribute(Integer id) {
		return (ProductOperationAttribute) getSession().get(ProductOperationAttribute.class, id);
	}
	
	public ProductOperationAttribute getOperationAttribute(String uuid) {
		Criteria criteria = getSession().createCriteria(ProductOperationAttribute.class);
		return (ProductOperationAttribute) criteria.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperationFlux> getAllProductOperationFluxes(Location location, Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductOperationAttribute.class);
		if (includeVoided) {
			return criteria.add(Restrictions.eq("location", location)).list();
		}
		return criteria.add(Restrictions.eq("location", location)).add(Restrictions.eq("voided", false)).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperationFlux> getAllProductOperationFluxes(Location location, Date startDate, Date endDate,
	        Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductOperationFlux.class);
		return criteria.add(Restrictions.eq("location", location))
		        .add(Restrictions.between("operationDate", startDate, endDate)).add(Restrictions.eq("voided", false)).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperationFlux> getAllProductOperationFluxByOperation(ProductOperation productOperation,
	        Boolean includeVoided) {
		if (includeVoided) {
			Criteria criteria = getSession().createCriteria(ProductOperationFlux.class);
			return criteria.add(Restrictions.eq("operation", productOperation)).list();
		}
		return getSession()
		        .createQuery(
		            "SELECT p FROM ProductOperationFlux WHERE p.operation = :operation AND p.operation.voided = false")
		        .setParameter("operation", productOperation).list();
	}
	
	public ProductOperationFlux getProductOperationFlux(Integer id) {
		return (ProductOperationFlux) getSession().get(ProductOperationFlux.class, id);
	}
	
	public ProductOperationFlux getProductOperationFlux(String uuid) {
		Criteria criteria = getSession().createCriteria(ProductOperationFlux.class);
		return (ProductOperationFlux) criteria.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	public ProductOperationFlux getProductOperationFluxByProductAndOperation(Product product,
	        ProductOperation productOperation) {
		return null;
	}
	
	public ProductOperationFlux saveProductOperationFlux(ProductOperationFlux productOperationFlux) {
		getSession().saveOrUpdate(productOperationFlux);
		return productOperationFlux;
	}
	
	public void purgeProductOperationFlux(ProductOperationFlux productOperationFlux) {
		getSession().delete(productOperationFlux);
	}
	
	public List<ProductOperationOtherFlux> getAllProductOperationOtherFluxes(Location location) {
		return null;
	}
	
	public ProductOperationOtherFlux getProductOperationOtherFlux(String uuid) {
		Criteria criteria = getSession().createCriteria(ProductOperationOtherFlux.class);
		return (ProductOperationOtherFlux) criteria.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	public ProductOperationOtherFlux getProductOperationOtherFlux(Integer id) {
		return (ProductOperationOtherFlux) getSession().get(ProductOperationOtherFlux.class, id);
	}
	
	public ProductOperationOtherFlux getProductOperationOtherFluxByAttributeAndOperation(ProductAttribute productAttribute,
	        ProductOperation productOperation, Location location) {
		return null;
	}
	
	public List<ProductOperationOtherFlux> getAllProductOperationOtherFluxByOperation(ProductOperation operation, Boolean b) {
		return null;
	}
	
	public ProductOperationOtherFlux saveProductOperationOtherFlux(ProductOperationOtherFlux productOperationOtherFlux) {
		getSession().saveOrUpdate(productOperationOtherFlux);
		return productOperationOtherFlux;
	}
	
	public void purgeProductOperationOtherFlux(ProductOperationOtherFlux productOperationOtherFlux) {
		getSession().delete(productOperationOtherFlux);
	}
	
	public List<ProductOperationFlux> getAllProductOperationFluxByOperationAndProduct(ProductOperation operation,
	        Product product) {
		return null;
	}
	
	public Integer getAllProductOperationFluxByOperationAndProductCount(ProductOperation operation, Product product) {
		return null;
	}
	
	public List<ProductOperationOtherFlux> getAllProductOperationOtherFluxByOperationAndProduct(ProductOperation operation,
	        Product product, Location location) {
		return null;
	}
	
	public Integer getAllProductOperationOtherFluxByOperationAndProductCount(ProductOperation operation, Product product) {
		return null;
	}
	
	public ProductOperationOtherFlux getProductOperationOtherFluxByProductAndOperation(Product product,
	        ProductOperation productOperation) {
		return null;
	}
	
	public ProductOperationOtherFlux getProductOperationOtherFluxByProductAndOperationAndLabel(Product product,
	        ProductOperation operation, String label, Location location) {
		return null;
	}
	
	public List<ProductOperationOtherFlux> getAllProductOperationOtherFluxByProductAndOperation(Product product,
	        ProductOperation operation, Location location) {
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductAttributeStock> getAllProductAttributeStocks(Location location, Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductAttributeStock.class);
		if (!includeVoided) {
			return criteria.add(Restrictions.eq("location", location)).add(Restrictions.eq("voided", false))
			        .add(Restrictions.ne("quantityInStock", 0)).list();
		}
		return criteria.add(Restrictions.eq("location", location)).add(Restrictions.ne("quantityInStock", 0)).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductAttributeStock> getAllProductAttributeStocks(Location location, ProductProgram program,
	        Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductAttributeStock.class);
		if (!includeVoided) {
			return criteria.add(Restrictions.eq("location", location)).add(Restrictions.eq("voided", false))
			        .add(Restrictions.eq("program", program)).add(Restrictions.gt("quantityInStock", 0)).list();
		}
		return criteria.add(Restrictions.eq("location", location)).add(Restrictions.eq("program", program))
		        .add(Restrictions.gt("quantityInStock", 0)).list();
	}
	
	public List<ProductAttributeStock> getAllProductAttributeStocks(Boolean includeVoided) {
		return null;
	}
	
	public ProductAttributeStock getAllProductAttributeStockByAttribute(ProductAttribute productAttribute,
	        Location location, Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductAttributeStock.class);
		return (ProductAttributeStock) criteria.add(Restrictions.eq("location", location))
		        .add(Restrictions.eq("voided", includeVoided)).add(Restrictions.eq("attribute", productAttribute))
		        .uniqueResult();
	}
	
	public ProductAttributeStock getProductAttributeStock(Integer id) {
		return (ProductAttributeStock) getSession().get(ProductAttributeStock.class, id);
	}
	
	public ProductAttributeStock getProductAttributeStock(String uuid) {
		return null;
	}
	
	public ProductAttributeStock saveProductAttributeStock(ProductAttributeStock productAttributeStock) {
		getSession().saveOrUpdate(productAttributeStock);
		return productAttributeStock;
	}
	
	public void purgeProductAttributeStock(ProductAttributeStock productAttributeStock) {
		getSession().delete(productAttributeStock);
	}
	
	public List<ProductAttributeStock> getProductAttributeStocksByProduct(Product product, ProductProgram program,
	        Location userLocation) {
		return null;
	}
	
	public Integer getProductQuantityInStock(Product product, ProductProgram productProgram) {
		return null;
	}
	
	public Integer getProductQuantityInStock(Product product, ProductProgram productProgram, Location location) {
		return null;
	}
	
	public ProductAttributeStock getProductAttributeStock(ProductAttribute productAttribute, ProductProgram productProgram,
	        Location location) {
		return (ProductAttributeStock) getSession().createCriteria(ProductAttributeStock.class)
		        .add(Restrictions.eq("attribute", productAttribute)).add(Restrictions.eq("program", productProgram))
		        .add(Restrictions.eq("location", location)).add(Restrictions.eq("voided", false)).uniqueResult();
	}
	
	public List<ProductAttributeStock> getAllProductAttributeStockByProduct(Product product, ProductProgram productProgram,
	        Location location) {
		return null;
	}
	
	public Integer getAllProductAttributeStockByProductCount(Product product, ProductProgram productProgram,
	        Location location, Boolean includeChildren) {
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperationFluxAttribute> getAllProductOperationFluxAttributes(Location userLocation,
	        Boolean includeVoided) {
		return getSession().createCriteria(ProductOperationFluxAttribute.class)
		        .add(Restrictions.eq("location", userLocation))
		        //				.add(Restrictions.eq("voided", includeVoided))
		        .list();
	}
	
	public void purgeProductOperationFluxAttribute(ProductOperationFluxAttribute productOperationFluxAttribute) {
		getSession().delete(productOperationFluxAttribute);
	}
	
	public ProductOperationFluxAttribute saveOperationFluxAttribute(
	        ProductOperationFluxAttribute productOperationFluxAttribute) {
		getSession().saveOrUpdate(productOperationFluxAttribute);
		return productOperationFluxAttribute;
	}
	
	public ProductOperationFluxAttribute getOperationFluxAttribute(String uuid) {
		Criteria criteria = getSession().createCriteria(ProductOperationFluxAttribute.class);
		return (ProductOperationFluxAttribute) criteria.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	public ProductOperationFluxAttribute getOperationFluxAttribute(Integer id) {
		return (ProductOperationFluxAttribute) getSession().get(ProductOperationFluxAttribute.class, id);
	}
	
	public ProductDispensation getProductDispensation(String uuid) {
		Criteria criteria = getSession().createCriteria(ProductDispensation.class);
		return (ProductDispensation) criteria.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	public ProductDispensation saveProductDispensation(ProductDispensation dispensation) {
		getSession().saveOrUpdate(dispensation);
		return dispensation;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductDispensation> getAllProductDispensation(Location location, Boolean includeVoided) {
		ProductOperationType operationType = getProductOperationType("DISPENSATIONOOOOOOOOOOOOOOOOOOOOOOOOOO");
		Criteria criteria = getSession().createCriteria(ProductOperation.class);
		if (includeVoided) {
			return criteria.add(Restrictions.eq("location", location)).add(Restrictions.eq("operationType", operationType))
			        .list();
		}
		return criteria.add(Restrictions.eq("location", location)).add(Restrictions.eq("voided", false))
		        .add(Restrictions.eq("operationType", operationType)).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductDispensation> getAllProductDispensation(String operationNumber, Location location,
	        Boolean includeVoided) {
		ProductOperationType operationType = getProductOperationType("DISPENSATIONOOOOOOOOOOOOOOOOOOOOOOOOOO");
		Criteria criteria = getSession().createCriteria(ProductOperation.class);
		if (includeVoided) {
			return criteria.add(Restrictions.eq("location", location)).add(Restrictions.eq("operationType", operationType))
			        .add(Restrictions.eq("operationNumber", operationNumber)).list();
		}
		return criteria.add(Restrictions.eq("location", location)).add(Restrictions.eq("operationNumber", operationNumber))
		        .add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("voided", false)).list();
	}
}
