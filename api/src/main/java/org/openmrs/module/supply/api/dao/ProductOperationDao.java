package org.openmrs.module.supply.api.dao;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;
import org.openmrs.*;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.supply.*;
import org.openmrs.module.supply.api.ProductOperationService;
import org.openmrs.module.supply.api.ProductService;
import org.openmrs.module.supply.enumerations.Incidence;
import org.openmrs.module.supply.enumerations.OperationStatus;
import org.openmrs.module.supply.enumerations.QuantityType;
import org.openmrs.module.supply.utils.OperationConstants;
import org.openmrs.module.supply.utils.ReportConstants;
import org.openmrs.module.supply.utils.SupplyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Repository("supply.ProductOperationDao")
public class ProductOperationDao {
	
	@Autowired
	DbSessionFactory sessionFactory;
	
	private DbSession getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	public void validateOperation(ProductOperation operation) {
		if (!operation.getIncidence().equals(Incidence.NONE)) {
			createStocks(operation);
		}
	}
	
	private ProductAttribute getProductAttributeWithNextExpiryDate(ProductCode productCode) {
		return (ProductAttribute) getSession().createCriteria(ProductAttribute.class)
		        .add(Restrictions.eq("productCode", productCode)).add(Restrictions.ge("expiryDate", new Date()))
		        .addOrder(Order.asc("expiryDate")).setMaxResults(1).uniqueResult();
	}
	
	private void generateStockStatus(ProductOperation operation) {
        List<ProductCode> productCodes = operation.getProductList();
        List<ProductStockStatus> productStockStatus = getAllProductStockStatuses(operation.getLocation(), operation.getProductProgram());

//        boolean isReportOperation = operation.getOperationType().getUuid().equals(OperationConstants.REPORT_OPERATION) ||
//                operation.getOperationType().getUuid().equals(OperationConstants.URGENT_REPORT_OPERATION);

        for (ProductCode productCode : productCodes) {
            ProductStockStatus productCodeInStatus = productStockStatus.stream().filter(p -> p.getProductCode().equals(productCode)).findFirst().orElse(null);
            Double available = getProductQuantityInStock(productCode, operation.getLocation()) * 1.;
            Double monthlyConsumption = 0.;

            if (operation.getOperationStatus().equals(OperationStatus.SUBMITTED)) {
                ProductOperationOtherFlux otherFluxMonthlyConsumption = operation.getOtherFluxes().stream()
                        .filter(o -> o.getLabel().equals(ReportConstants.MONTHLY_CONSUMPTION) && o.getProductCode().equals(productCode)).findFirst().orElse(null);
                if (otherFluxMonthlyConsumption != null) {
                    monthlyConsumption = otherFluxMonthlyConsumption.getQuantity();
                }
                if (available == 0) {
                    ProductOperationOtherFlux otherFluxAvailable = operation.getOtherFluxes().stream()
                            .filter(o -> o.getLabel().equals(ReportConstants.AVAILABLE_QUANTITY) && o.getProductCode().equals(productCode)).findFirst().orElse(null);
                    if (otherFluxAvailable != null) {
                        available = otherFluxAvailable.getQuantity();
                    }
                }
            } else if (!operation.getOperationType().getUuid().equals(OperationConstants.REPORT_OPERATION) &&
                    !operation.getOperationType().getUuid().equals(OperationConstants.URGENT_REPORT_OPERATION)) {
                available = getProductQuantityInStock(productCode, operation.getLocation()) * 1.;
            }

            ProductAttribute productAttribute = getProductAttributeWithNextExpiryDate(productCode);

            if (productCodeInStatus != null) {
                if (!productCodeInStatus.getAverageConsumedQuantity().equals(monthlyConsumption) ||
                        !productCodeInStatus.getQuantityInStock().equals(available)) {
                    if (operation.getOperationType().getUuid().equals(OperationConstants.INVENTORY_OPERATION)) {
                        productCodeInStatus.setAverageConsumedQuantity(0.);
                    } else {
                        if (monthlyConsumption != 0. && productCodeInStatus.getAverageConsumedQuantity() == 0) {
                            productCodeInStatus.setAverageConsumedQuantity(monthlyConsumption);
                        }
                    }
                    productCodeInStatus.setQuantityInStock(available);
                    productCodeInStatus.setStockDate(new Date());
                    if (productAttribute != null) {
                        productCodeInStatus.setExpiryNextDate(productAttribute.getExpiryDate());
                    }
                    saveProductStockStatus(productCodeInStatus);
                }
            } else {
                productCodeInStatus = new ProductStockStatus(
                        productCode,
                        operation.getLocation(),
                        available,
                        monthlyConsumption,
                        new Date()
                );

                if (productAttribute != null) {
                    productCodeInStatus.setExpiryNextDate(productAttribute.getExpiryDate());
                }
                saveProductStockStatus(productCodeInStatus);
            }

        }
    }
	
	public void cancelOperation(ProductOperation operation) {
		if (!operation.getIncidence().equals(Incidence.NONE)) {
			createStocks(operation);
		}
	}
	
	public ProductAttribute createStock(ProductOperationFluxAttribute fluxAttribute, ProductOperation operation) {
		ProductAttributeStock attributeStock = new ProductAttributeStock();
		attributeStock.setOperation(operation);
		attributeStock.setAttribute(fluxAttribute.getAttribute());
		attributeStock.setLocation(fluxAttribute.getLocation());
		attributeStock.setQuantityInStock(getQuantity(operation, fluxAttribute));
		saveProductAttributeStock(attributeStock);
		return fluxAttribute.getAttribute();
	}
	
	public ProductOperationFluxAttribute createFluxAttribute(ProductAttributeStock attributeStock,
	        ProductOperationFlux flux, Double quantity) {
		ProductOperationFluxAttribute fluxAttribute = new ProductOperationFluxAttribute();
		fluxAttribute.setOperationFlux(flux);
		fluxAttribute.setAttribute(attributeStock.getAttribute());
		fluxAttribute.setLocation(attributeStock.getLocation());
		fluxAttribute.setQuantity(quantity);
		getSession().saveOrUpdate(fluxAttribute);
		return fluxAttribute;
	}
	
	private void createStocks(ProductOperation operation) throws APIException {
        if (!operation.getOperationStatus().equals(OperationStatus.VALIDATED)
                && !operation.getOperationStatus().equals(OperationStatus.CANCELED)
                && !operation.getOperationStatus().equals(OperationStatus.TREATED)) {
            return;
        }
        Set<ProductOperationFlux> fluxes = operation.getFluxes();
        if (fluxes != null && !fluxes.isEmpty()) {
            for (ProductOperationFlux flux : fluxes) {
                if (flux.getQuantity() >= 0) {
                    if (flux.getProductCode().getQuantityInStock() == 0
                            && operation.getIncidence().equals(Incidence.NEGATIVE)
                            && !operation.getOperationType().getUuid().equals(OperationConstants.DISTRIBUTION_OPERATION)
                    ) {
                        throw new APIException("Le produit " + flux.getProductCode().getProduct().getDispensationName()
                                + " (" + flux.getProductCode().getCode() + ") n'existe pas en ce moment dans votre stock");
                    } else {
                        if (!flux.getAttributes().isEmpty()) {
                            List<String> attributes = new ArrayList<>();
                            for (ProductOperationFluxAttribute fluxAttribute : flux.getAttributes()) {
                                if (!attributes.contains(fluxAttribute.getAttribute().getUuid())) {
                                    attributes.add(createStock(fluxAttribute, operation).getUuid());
                                }
                            }
                        } else {
                            List<ProductAttributeStock> stocks = getAllProductAttributeStocks(operation.getLocation(),
                                    flux.getProductCode());
                            Double fluxQuantity = flux.getQuantity();
                            for (ProductAttributeStock stock : stocks) {
                                if (stock.getQuantityInStock() > 0) {
                                    Double quantity = stock.getQuantityInStock() >= fluxQuantity ? fluxQuantity : stock.getQuantityInStock();
                                    createStock(createFluxAttribute(stock, flux, quantity), operation);
                                    if (stock.getQuantityInStock() >= quantity) {
                                        break;
                                    } else {
                                        fluxQuantity -= stock.getQuantityInStock();
                                    }
                                }
                            }
                        }
                        if (operation.getIncidence().equals(Incidence.NEGATIVE)) {
                            if (flux.getProductCode().getQuantityInStock() == 0) {
                                ProductNotification notification = new ProductNotification();
                                notification.setProductCode(flux.getProductCode());
                                notification.setNotification(OperationConstants.PRODUCT_RUPTURE_NOTIFICATION);
                                notification.setNotificationInfo("Produit en rupture depuis le : "
                                        + operation.getOperationDate().toString());
                                notification.setLocation(operation.getLocation());
                                notification.setNotifiedTo(operation.getLocation());
                                notification.setNotificationDate(operation.getOperationDate());
                                getSession().save(notification);
                            }
                        }
                    }
                }
            }

            createNotification(operation);
        }
//        if (fluxes != null && !fluxes.isEmpty()) {
//            for (ProductOperationFlux flux : fluxes) {
//                if (flux.getQuantity() >= 0) {
//                    if (flux.getProductCode().getQuantityInStock() == 0
//                            && operation.getIncidence().equals(Incidence.NEGATIVE)
//                            && !operation.getOperationType().getUuid().equals(OperationConstants.DISTRIBUTION_OPERATION)) {
//                        throw new APIException("Le produit " + flux.getProductCode().getProduct().getDispensationName()
//                                + " (" + flux.getProductCode().getCode() + ") n'existe pas en ce moment dans votre stock");
//                    } else {
//                        if (!flux.getAttributes().isEmpty()) {
//                            List<ProductAttribute> attributes = new ArrayList<>();
//                            for (ProductOperationFluxAttribute fluxAttribute : flux.getAttributes()) {
//                                if (!attributes.contains(fluxAttribute.getAttribute())) {
//                                    ProductAttributeStock attributeStock = new ProductAttributeStock();
//                                    attributeStock.setOperation(operation);
//                                    attributeStock.setAttribute(fluxAttribute.getAttribute());
//                                    attributeStock.setLocation(fluxAttribute.getLocation());
//                                    attributeStock.setQuantityInStock(getQuantity(operation, fluxAttribute));
//                                    saveProductAttributeStock(attributeStock);
//                                    attributes.add(fluxAttribute.getAttribute());
//                                }
//                            }
//                        } else {
//                            List<ProductAttributeStock> stocks = getAllProductAttributeStocks(operation.getLocation(),
//                                    flux.getProductCode());
//                            Integer quantity = flux.getQuantity().intValue();
//                            for (ProductAttributeStock stock : stocks) {
//                                ProductOperationFluxAttribute attribute = new ProductOperationFluxAttribute();
//                                attribute.setOperationFlux(flux);
//                                attribute.setAttribute(stock.getAttribute());
//                                attribute.setLocation(operation.getLocation());
//                                attribute.setQuantity(stock.getQuantityInStock() >= quantity ? quantity.doubleValue()
//                                        : stock.getQuantityInStock());
//                                getSession().saveOrUpdate(attribute);
//
//                                ProductAttributeStock attributeStock = new ProductAttributeStock();
//                                attributeStock.setOperation(operation);
//                                attributeStock.setAttribute(stock.getAttribute());
//                                attributeStock.setLocation(stock.getLocation());
//                                if (stock.getQuantityInStock() >= quantity) {
//                                    attributeStock.setQuantityInStock(stock.getQuantityInStock() - quantity);
//                                } else {
//                                    attributeStock.setQuantityInStock(0);
//                                }
//                                saveProductAttributeStock(attributeStock);
//
//                                stock.setVoided(true);
//                                stock.setDateVoided(new Date());
//                                stock.setVoidedBy(Context.getAuthenticatedUser());
//                                stock.setVoidReason("Voided by user because not to be used");
//                                saveProductAttributeStock(stock);
//
//                                if (stock.getQuantityInStock() >= quantity) {
//                                    break;
//                                } else {
//                                    quantity -= stock.getQuantityInStock();
//                                }
//                            }
//                        }
//                        if (flux.getProductCode().getQuantityInStock() == 0) {
//                            ProductNotification notification = new ProductNotification();
//                            notification.setProductCode(flux.getProductCode());
//                            notification.setNotification(OperationConstants.PRODUCT_RUPTURE_NOTIFICATION);
//                            notification.setNotificationInfo("Produit en rupture depuis le : "
//                                    + operation.getOperationDate().toString());
//                            notification.setLocation(operation.getLocation());
//                            notification.setNotifiedTo(operation.getLocation());
//                            notification.setNotificationDate(operation.getOperationDate());
//                            getSession().save(notification);
//                        }
//                    }
//                }
//            }
//            createNotification(operation);
//        }
    }
	
	private String formatDate(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		return formatter.format(date);
	}
	
	private void createNotification(ProductOperation operation) {
		if (operation.getOperationType().getUuid().equals(OperationConstants.TRANSFER_OUT_OPERATION)) {
			ProductNotification notification = new ProductNotification();
			notification.setNotification(OperationConstants.TRANSFER_IN_NOTIFICATION);
			notification.setNotificationInfo("Nouveau transfert entrant depuis le "
			        + formatDate(operation.getOperationDate()) + " en provenance de " + operation.getLocation().getName());
			notification.setLocation(operation.getLocation());
			notification.setNotifiedTo(operation.getExchangeLocation());
			notification.setNotificationDate(operation.getOperationDate());
			notification.setOperationType(getProductOperationType(OperationConstants.TRANSFER_IN_OPERATION));
			getSession().save(notification);
		} else if (operation.getOperationType().getUuid().equals(OperationConstants.PRODUCT_RETURN_OUT_OPERATION)
		        || operation.getOperationType().getUuid().equals(OperationConstants.RECEPTION_RETURN_OPERATION)) {
			ProductNotification notification = new ProductNotification();
			notification.setNotification(OperationConstants.PRODUCT_RETURN_IN_NOTIFICATION);
			notification.setNotificationInfo("Nouveau retour de produits depuis le "
			        + formatDate(operation.getOperationDate()) + " en provenance de " + operation.getLocation().getName());
			notification.setLocation(operation.getLocation());
			if (operation.getExchangeLocation() != null) {
				notification.setNotifiedTo(operation.getExchangeLocation());
			} else {
				if (SupplyUtils.isDirectClient(operation.getLocation())) {
					Location location = Context.getLocationService().getLocationByUuid(
					    "NPSPLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
					if (location != null) {
						notification.setNotifiedTo(location);
					}
				} else {
					notification.setNotifiedTo(operation.getLocation().getParentLocation());
				}
			}
			notification.setNotificationDate(operation.getOperationDate());
			notification.setOperationType(getProductOperationType(OperationConstants.PRODUCT_RETURN_IN_OPERATION));
			getSession().save(notification);
		} else if (operation.getOperationType().getUuid().equals(OperationConstants.DISTRIBUTION_OPERATION)) {
			ProductNotification notification = new ProductNotification();
			notification.setNotification(OperationConstants.REPORT_TREATMENT_NOTIFICATION);
			notification.setNotificationInfo("Nouvelle livraison en cours depuis le : "
			        + formatDate(operation.getOperationDate()) + " en provenance de du fournisseur "
			        + operation.getLocation().getName());
			notification.setLocation(operation.getLocation());
			notification.setNotifiedTo(operation.getParentOperation().getLocation());
			notification.setNotificationDate(operation.getOperationDate());
			notification.setOperationType(getProductOperationType(OperationConstants.RECEPTION_OPERATION));
			getSession().save(notification);
		} else if (operation.getOperationType().getUuid().equals(OperationConstants.REPORT_OPERATION)
		        && operation.getOperationStatus().equals(OperationStatus.REJECTED)) {
			ProductNotification notification = new ProductNotification();
			notification.setNotification(OperationConstants.REPORT_REJECTED_NOTIFICATION);
			notification.setNotificationInfo("Rapport soumis rejetté  depuis le : "
			        + formatDate(operation.getOperationDate()) + " par votre fournisseur "
			        + SupplyUtils.getUserLocation().getName());
			notification.setLocation(SupplyUtils.getUserLocation());
			notification.setNotifiedTo(operation.getLocation());
			notification.setNotificationDate(operation.getOperationDate());
			notification.setOperationType(getProductOperationType(OperationConstants.RECEPTION_OPERATION));
			getSession().save(notification);
		}
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
	
	private Integer voidPreviousStock(ProductOperation operation, ProductOperationFluxAttribute fluxAttribute) {
		Integer quantityInStock = 0;
		ProductAttributeStock attributeStock = getProductAttributeStock(fluxAttribute.getAttribute(),
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
			return criteria
			        .add(Restrictions.eq("operationType", operationType))
			        .add(Restrictions.eq("location", location))
			        .add(Restrictions.eq("voided", false))
			        .add(
			            Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
			                Restrictions.eq("operationStatus", OperationStatus.APPROVED),
			                Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
			                Restrictions.eq("operationStatus", OperationStatus.TREATED))).list();
		}
		return criteria.add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("location", location))
		        .add(Restrictions.eq("voided", false)).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperation> getAllProductOperation(ProductOperationType operationType, ProductProgram program,
	        Location location, Boolean validatedOnly, Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class);
		if (validatedOnly) {
			return criteria
			        .add(Restrictions.eq("operationType", operationType))
			        .add(Restrictions.eq("location", location))
			        .add(Restrictions.eq("program", program))
			        .add(Restrictions.eq("voided", false))
			        .add(
			            Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
			                Restrictions.eq("operationStatus", OperationStatus.APPROVED),
			                Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
			                Restrictions.eq("operationStatus", OperationStatus.TREATED))).list();
		} else if (includeVoided) {
			return criteria.add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("program", program))
			        .add(Restrictions.eq("location", location)).list();
		}
		return criteria.add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("location", location))
		        .add(Restrictions.eq("program", program)).add(Restrictions.eq("voided", false)).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperation> getAllProductOperation(ProductOperationType operationType, ProductProgram program,
	        Date startDate, Date endDate, Location location, Boolean validatedOnly, Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class)
		        .add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("location", location))
		        .add(Restrictions.eq("productProgram", program))
		        .add(Restrictions.between("operationDate", startDate, endDate));
		
		if (validatedOnly) {
			return criteria.add(
			    Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
			        Restrictions.eq("operationStatus", OperationStatus.APPROVED),
			        Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
			        Restrictions.eq("operationStatus", OperationStatus.TREATED))).list();
		} else if (!includeVoided) {
			return criteria.add(Restrictions.eq("voided", false)).list();
		}
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperation> getAllProductOperation(ProductOperationType operationType, Location location,
	        Date startDate, Date endDate, Boolean validatedOnly, Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class)
		        .add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("location", location))
		        //		        .add(Restrictions.eq("productProgram", program))
		        .add(Restrictions.between("operationDate", startDate, endDate));
		
		if (validatedOnly) {
			return criteria.add(
			    Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
			        Restrictions.eq("operationStatus", OperationStatus.APPROVED),
			        Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
			        Restrictions.eq("operationStatus", OperationStatus.TREATED))).list();
		} else if (!includeVoided) {
			return criteria.add(Restrictions.eq("voided", false)).list();
		}
		return criteria.list();
	}
	
	public List<ProductOperation> getAllProductOperation(ProductOperationType operationType, ProductProgram program,
	        Date startDate, Date endDate, Location location, Boolean validatedOnly, Boolean includeVoided,
	        Boolean forChildLocations) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class)
		        .add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("productProgram", program))
		        .add(Restrictions.between("operationDate", startDate, endDate));
		return getHistoricalProductOperations(location, validatedOnly, includeVoided, forChildLocations, criteria);
	}
	
	public List<ProductOperation> getAllProductOperation(List<ProductOperationType> operationTypes, ProductProgram program,
	        Date startDate, Date endDate, List<Location> locations, Boolean validatedOnly, Boolean includeVoided,
	        Boolean forChildLocations) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class)
		        .add(Restrictions.in("operationType", operationTypes)).add(Restrictions.eq("productProgram", program))
		        .add(Restrictions.between("operationDate", startDate, endDate));
		return getHistoricalProductOperations(locations, validatedOnly, includeVoided, criteria);
	}
	
	public List<ProductOperation> getAllProductOperation(ProductOperationType operationType, ProductProgram program,
	        String operationNumber, Date startDate, Date endDate, Location location, Boolean validatedOnly,
	        Boolean includeVoided, Boolean forChildLocations) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class)
		        .add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("productProgram", program))
		        .add(Restrictions.eq("operationNumber", operationNumber))
		        .add(Restrictions.between("operationDate", startDate, endDate));
		return getHistoricalProductOperations(location, validatedOnly, includeVoided, forChildLocations, criteria);
	}
	
	public List<ProductOperation> getAllProductOperation(ProductOperationType operationType, ProductProgram program,
	        String operationNumber, Location location, Boolean validatedOnly, Boolean includeVoided,
	        Boolean forChildLocations) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class)
		        .add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("productProgram", program))
		        .add(Restrictions.eq("operationNumber", operationNumber));
		return getHistoricalProductOperations(location, validatedOnly, includeVoided, forChildLocations, criteria);
	}
	
	@SuppressWarnings("unchecked")
	private List<ProductOperation> getHistoricalProductOperations(Location location, Boolean validatedOnly,
	        Boolean includeVoided, Boolean forChildLocations, Criteria criteria) {
		if (forChildLocations) {
			criteria.add(Restrictions.eq("exchangeLocation", location));
		} else {
			criteria.add(Restrictions.eq("location", location));
		}
		if (!SupplyUtils.getUserLocation().equals(location)) {
			criteria.add(Restrictions.eq("exchangeLocation", SupplyUtils.getUserLocation()));
		}
		
		if (validatedOnly) {
			return criteria.add(
			    Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
			        Restrictions.eq("operationStatus", OperationStatus.APPROVED),
			        Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
			        Restrictions.eq("operationStatus", OperationStatus.TREATED))).list();
		} else if (!includeVoided) {
			return criteria.add(Restrictions.eq("voided", false)).list();
		}
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	private List<ProductOperation> getHistoricalProductOperations(List<Location> locations, Boolean validatedOnly,
	        Boolean includeVoided, Criteria criteria) {
		criteria.add(Restrictions.in("location", locations));
		if (validatedOnly) {
			return criteria.add(
			    Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
			        Restrictions.eq("operationStatus", OperationStatus.APPROVED),
			        Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
			        Restrictions.eq("operationStatus", OperationStatus.TREATED))).list();
		} else if (!includeVoided) {
			return criteria.add(Restrictions.eq("voided", false)).list();
		}
		return criteria.list();
	}
	
	//	@SuppressWarnings("unchecked")
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
		        .add(Restrictions.eq("location", location));
		if (validated) {
			criteria.add(Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
			    Restrictions.eq("operationStatus", OperationStatus.APPROVED),
			    Restrictions.eq("operationStatus", OperationStatus.TREATED),
			    Restrictions.eq("operationStatus", OperationStatus.SUBMITTED)));
		}
		if (!includeVoided) {
			criteria.add(Restrictions.eq("voided", false));
		}
		return (ProductOperation) criteria.addOrder(Order.desc("operationDate")).setMaxResults(1).uniqueResult();
		
	}
	
	public ProductOperation getLastProductOperation(ProductOperationType operationType, ProductProgram program,
	        Location location, Boolean validated, Boolean includeVoided, Date endDate) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class)
		        .add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("productProgram", program))
		        .add(Restrictions.eq("location", location)).add(Restrictions.lt("operationDate", endDate));
		if (validated) {
			criteria.add(Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
			    Restrictions.eq("operationStatus", OperationStatus.APPROVED),
			    Restrictions.eq("operationStatus", OperationStatus.TREATED),
			    Restrictions.eq("operationStatus", OperationStatus.SUBMITTED)));
		}
		if (!includeVoided) {
			criteria.add(Restrictions.eq("voided", false));
		}
		return (ProductOperation) criteria.addOrder(Order.desc("operationDate")).setMaxResults(1).uniqueResult();
		
	}
	
	public ProductOperation getLastProductOperation(ProductOperationType operationType, ProductProgram program,
	        String operationNumber, Location location, Boolean validated, Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class)
		        .add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("productProgram", program))
		        .add(Restrictions.eq("operationNumber", operationNumber)).add(Restrictions.eq("location", location));
		if (validated) {
			criteria.add(Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
			    Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
			    Restrictions.eq("operationStatus", OperationStatus.TREATED),
			    Restrictions.eq("operationStatus", OperationStatus.APPROVED)));
		}
		if (!includeVoided) {
			criteria.add(Restrictions.eq("voided", false));
		}
		return (ProductOperation) criteria.addOrder(Order.desc("operationDate")).setMaxResults(1).uniqueResult();
		
	}
	
	public ProductOperation getLastProductOperation(List<ProductOperationType> operationTypes, ProductProgram program,
	        Location location, Boolean includeVoided) {
		Criteria criteria = getSession()
		        .createCriteria(ProductOperation.class)
		        .add(Restrictions.in("operationType", operationTypes))
		        .add(Restrictions.eq("productProgram", program))
		        .add(Restrictions.eq("location", location))
		        .add(Restrictions.eq("voided", includeVoided))
		        .add(
		            Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
		                Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
		                Restrictions.eq("operationStatus", OperationStatus.TREATED),
		                Restrictions.eq("operationStatus", OperationStatus.APPROVED)));
		return (ProductOperation) criteria.addOrder(Order.desc("operationDate")).setMaxResults(1).uniqueResult();
		
	}
	
	public ProductOperation getLastProductOperation(List<ProductOperationType> operationTypes, ProductCode productCode,
	        ProductProgram program, Location location, Boolean includeVoided) {
		Criteria criteria = getSession()
		        .createCriteria(ProductOperation.class, "o")
		        .createAlias("o.fluxes", "f")
		        .add(Restrictions.in("o.operationType", operationTypes))
		        .add(Restrictions.eq("o.productProgram", program))
		        .add(Restrictions.eq("o.location", location))
		        .add(Restrictions.eq("o.voided", includeVoided))
		        .add(Restrictions.eq("f.productCode", productCode))
		        .add(
		            Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
		                Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
		                Restrictions.eq("operationStatus", OperationStatus.TREATED),
		                Restrictions.eq("operationStatus", OperationStatus.APPROVED)));
		return (ProductOperation) criteria.addOrder(Order.desc("operationDate")).setMaxResults(1).uniqueResult();
		
	}
	
	public ProductOperation getLastProductOperation(List<ProductOperationType> operationTypes, ProductCode productCode,
	        Date limitDate, ProductProgram program, Location location, Boolean includeVoided) {
		Criteria criteria = getSession()
		        .createCriteria(ProductOperation.class, "o")
		        .createAlias("o.fluxes", "f")
		        .add(Restrictions.in("o.operationType", operationTypes))
		        .add(Restrictions.eq("o.productProgram", program))
		        .add(Restrictions.eq("o.location", location))
		        .add(Restrictions.eq("o.voided", includeVoided))
		        .add(Restrictions.eq("f.productCode", productCode))
		        .add(Restrictions.lt("o.operationDate", limitDate))
		        .add(
		            Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
		                Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
		                Restrictions.eq("operationStatus", OperationStatus.TREATED),
		                Restrictions.eq("operationStatus", OperationStatus.APPROVED)));
		return (ProductOperation) criteria.addOrder(Order.desc("operationDate")).setMaxResults(1).uniqueResult();
		
	}
	
	public ProductOperation getLastProductOperation(List<ProductOperationType> operationTypes, ProductCode productCode,
	        Date limitDate, Date beforeDate, ProductProgram program, Location location, Boolean includeVoided) {
		Criteria criteria = getSession()
		        .createCriteria(ProductOperation.class, "o")
		        .createAlias("o.fluxes", "f")
		        .add(Restrictions.in("o.operationType", operationTypes))
		        .add(Restrictions.eq("o.productProgram", program))
		        .add(Restrictions.eq("o.location", location))
		        .add(Restrictions.eq("o.voided", includeVoided))
		        .add(Restrictions.eq("f.productCode", productCode))
		        .add(Restrictions.lt("o.operationDate", limitDate))
		        .add(Restrictions.gt("o.operationDate", beforeDate))
		        .add(
		            Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
		                Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
		                Restrictions.eq("operationStatus", OperationStatus.TREATED),
		                Restrictions.eq("operationStatus", OperationStatus.APPROVED)));
		return (ProductOperation) criteria.addOrder(Order.desc("operationDate")).setMaxResults(1).uniqueResult();
		
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperation> getAllProductOperationByTypes(List<ProductOperationType> operationTypes,
	        ProductProgram program, Location location, Boolean validatedOnly, Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class)
		        .add(Restrictions.in("operationType", operationTypes)).add(Restrictions.eq("productProgram", program))
		        .add(Restrictions.eq("location", location)).add(Restrictions.eq("voided", includeVoided));
		if (validatedOnly) {
			criteria.add(Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
			    Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
			    Restrictions.eq("operationStatus", OperationStatus.TREATED),
			    Restrictions.eq("operationStatus", OperationStatus.APPROVED)));
		}
		return criteria.addOrder(Order.desc("operationDate")).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperation> getAllProductOperationByTypes(List<ProductOperationType> operationTypes,
	        Location location, Boolean validatedOnly, Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class)
		        .add(Restrictions.in("operationType", operationTypes)).add(Restrictions.eq("location", location))
		        .add(Restrictions.eq("voided", includeVoided));
		if (validatedOnly) {
			criteria.add(Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
			    Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
			    Restrictions.eq("operationStatus", OperationStatus.TREATED),
			    Restrictions.eq("operationStatus", OperationStatus.APPROVED)));
		}
		return criteria.addOrder(Order.desc("operationDate")).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperation> getAllProductOperation(List<ProductOperationType> operationTypes, Location location,
	        Boolean includeVoided) {
		Criteria criteria = getSession()
		        .createCriteria(ProductOperation.class)
		        .add(Restrictions.in("operationType", operationTypes))
		        .add(Restrictions.eq("exchangeLocation", location))
		        .add(Restrictions.eq("voided", includeVoided))
		        .add(
		            Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
		                Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
		                Restrictions.eq("operationStatus", OperationStatus.TREATED),
		                Restrictions.eq("operationStatus", OperationStatus.APPROVED)))
		        .add(Restrictions.isNull("childrenOperation"));
		return (List<ProductOperation>) criteria.addOrder(Order.desc("operationDate")).list();
		
	}
	
	public ProductOperation getLastProductOperation(List<ProductOperationType> operationTypes, ProductProgram program,
	        Date limitEndDate, Location location, Boolean validated, Boolean includeVoided) throws APIException {
		Criteria criteria = getSession().createCriteria(ProductOperation.class);
		if (validated) {
			return (ProductOperation) criteria
			        .add(Restrictions.in("operationType", operationTypes))
			        .add(Restrictions.eq("location", location))
			        .add(Restrictions.eq("voided", includeVoided))
			        .add(
			            Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
			                Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
			                Restrictions.eq("operationStatus", OperationStatus.TREATED),
			                Restrictions.eq("operationStatus", OperationStatus.APPROVED)))
			        .add(Restrictions.eq("productProgram", program)).add(Restrictions.lt("operationDate", limitEndDate))
			        .addOrder(Order.desc("operationDate")).setMaxResults(1).uniqueResult();
		}
		return (ProductOperation) criteria
		        .add(Restrictions.in("operationType", operationTypes))
		        .add(Restrictions.eq("productProgram", program))
		        .add(Restrictions.eq("location", location))
		        .add(
		            Restrictions.and(Restrictions.ne("operationStatus", OperationStatus.VALIDATED),
		                Restrictions.ne("operationStatus", OperationStatus.SUBMITTED),
		                Restrictions.ne("operationStatus", OperationStatus.TREATED),
		                Restrictions.ne("operationStatus", OperationStatus.APPROVED))).add(Restrictions.eq("voided", false))
		        .add(Restrictions.lt("operationDate", limitEndDate)).addOrder(Order.desc("operationDate")).setMaxResults(1)
		        .uniqueResult();
	}
	
	public ProductOperation getLastProductOperation(ProductOperationType operationType, ProductProgram program,
	        Date limitEndDate, Location location, Boolean validated, Boolean includeVoided) throws APIException {
		Criteria criteria = getSession().createCriteria(ProductOperation.class);
		if (validated) {
			return (ProductOperation) criteria
			        .add(Restrictions.eq("operationType", operationType))
			        .add(Restrictions.eq("location", location))
			        .add(Restrictions.eq("voided", includeVoided))
			        .add(
			            Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
			                Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
			                Restrictions.eq("operationStatus", OperationStatus.TREATED),
			                Restrictions.eq("operationStatus", OperationStatus.APPROVED)))
			        .add(Restrictions.eq("productProgram", program)).add(Restrictions.lt("operationDate", limitEndDate))
			        .addOrder(Order.desc("operationDate")).setMaxResults(1).uniqueResult();
		}
		return (ProductOperation) criteria
		        .add(Restrictions.eq("operationType", operationType))
		        .add(Restrictions.eq("productProgram", program))
		        .add(Restrictions.eq("location", location))
		        .add(
		            Restrictions.and(Restrictions.ne("operationStatus", OperationStatus.VALIDATED),
		                Restrictions.ne("operationStatus", OperationStatus.SUBMITTED),
		                Restrictions.ne("operationStatus", OperationStatus.TREATED),
		                Restrictions.ne("operationStatus", OperationStatus.APPROVED))).add(Restrictions.eq("voided", false))
		        .add(Restrictions.lt("operationDate", limitEndDate)).addOrder(Order.desc("operationDate")).setMaxResults(1)
		        .uniqueResult();
	}
	
	public ProductOperation getProductOperationByOperationNumber(ProductOperationType operationType, String operationNumber,
	        Location location, Boolean validated) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class);
		if (validated) {
			return (ProductOperation) criteria
			        .add(Restrictions.eq("operationType", operationType))
			        .add(Restrictions.eq("location", location))
			        .add(Restrictions.eq("voided", false))
			        .add(
			            Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
			                Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
			                Restrictions.eq("operationStatus", OperationStatus.TREATED),
			                Restrictions.eq("operationStatus", OperationStatus.APPROVED)))
			        .add(Restrictions.eq("operationNumber", operationNumber)).addOrder(Order.desc("operationDate"))
			        .setMaxResults(1).uniqueResult();
		}
		return (ProductOperation) criteria.add(Restrictions.eq("operationType", operationType))
		        .add(Restrictions.eq("location", location)).add(Restrictions.eq("voided", false))
		        .add(Restrictions.eq("operationNumber", operationNumber)).addOrder(Order.desc("operationDate"))
		        .setMaxResults(1).uniqueResult();
	}
	
	public ProductOperation getProductOperationByOperationNumber(ProductOperationType operationType, String operationNumber,
	        Location location, Boolean validated, Date endDate) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class)
		        .add(Restrictions.lt("operationDate", endDate)).add(Restrictions.eq("operationType", operationType))
		        .add(Restrictions.eq("location", location)).add(Restrictions.eq("voided", false))
		        .add(Restrictions.eq("operationNumber", operationNumber));
		if (validated) {
			return (ProductOperation) criteria
			        .add(
			            Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
			                Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
			                Restrictions.eq("operationStatus", OperationStatus.TREATED),
			                Restrictions.eq("operationStatus", OperationStatus.APPROVED)))
			        .addOrder(Order.desc("operationDate")).setMaxResults(1).uniqueResult();
		}
		return (ProductOperation) criteria.addOrder(Order.desc("operationDate")).setMaxResults(1).uniqueResult();
	}
	
	public ProductOperation getProductOperationByOperationNumber(ProductOperationType operationType, ProductProgram program,
	        String operationNumber, Location location, Boolean validated) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class)
		        .add(Restrictions.eq("operationType", operationType)).add(Restrictions.eq("location", location))
		        .add(Restrictions.eq("voided", false)).add(Restrictions.eq("operationNumber", operationNumber))
		        .add(Restrictions.eq("productProgram", program)).addOrder(Order.desc("operationDate"));
		if (validated) {
			criteria.add(Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
			    Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
			    Restrictions.eq("operationStatus", OperationStatus.TREATED),
			    Restrictions.eq("operationStatus", OperationStatus.APPROVED)));
		}
		return (ProductOperation) criteria.setMaxResults(1).uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperation> getProductOperationByOperationNumber(String operationNumber, Location location,
	        Boolean validated) {
		Criteria criteria = getSession().createCriteria(ProductOperation.class);
		if (validated) {
			return (List<ProductOperation>) criteria
			        .add(Restrictions.eq("location", location))
			        .add(Restrictions.eq("voided", false))
			        .add(
			            Restrictions.or(Restrictions.eq("operationStatus", OperationStatus.VALIDATED),
			                Restrictions.eq("operationStatus", OperationStatus.SUBMITTED),
			                Restrictions.eq("operationStatus", OperationStatus.TREATED),
			                Restrictions.eq("operationStatus", OperationStatus.APPROVED)))
			        .add(Restrictions.eq("operationNumber", operationNumber)).addOrder(Order.desc("operationDate")).list();
		}
		return (List<ProductOperation>) criteria
		        .add(Restrictions.eq("location", location))
		        .add(Restrictions.eq("voided", false))
		        .add(Restrictions.eq("operationNumber", operationNumber))
		        .add(
		            Restrictions.and(Restrictions.ne("operationStatus", OperationStatus.VALIDATED),
		                Restrictions.ne("operationStatus", OperationStatus.SUBMITTED),
		                Restrictions.eq("operationStatus", OperationStatus.TREATED),
		                Restrictions.ne("operationStatus", OperationStatus.APPROVED))).addOrder(Order.desc("operationDate"))
		        .list();
	}
	
	public ProductOperation saveProductOperation(ProductOperation operation) throws APIException, ParseException {
        if (operation.getOperationType().getUuid().equals(OperationConstants.TRANSFER_OUT_OPERATION) ||
                operation.getOperationType().getUuid().equals(OperationConstants.PRODUCT_RETURN_OUT_OPERATION)) {
            if (operation.getOperationNumber() == null || operation.getOperationNumber().isEmpty()) {
                operation.setOperationNumber(generateDeliveryNumber());
            }
        }

        ProductNotification notification = null;

        if (operation.getOperationStatus().equals(OperationStatus.NOT_COMPLETED)) {

            if (operation.getOperationType().getUuid().equals(OperationConstants.INVENTORY_OPERATION)) {
                operation.addAllFluxes(createInventoryFluxes(operation));
            } else if (operation.getOperationType().getUuid().equals(OperationConstants.RECEPTION_OPERATION)
                    || operation.getOperationType().getUuid().equals(OperationConstants.TRANSFER_IN_OPERATION) ||
                    operation.getOperationType().getUuid().equals(OperationConstants.PRODUCT_RETURN_IN_OPERATION)) {
                if (operation.getFluxes().isEmpty() && operation.getParentOperation() != null) {
                    operation.addAllFluxes(createFluxesFromOperation(operation, operation.getParentOperation()));
                }
            } else if (operation.getOperationType().getUuid().equals(OperationConstants.REPORT_OPERATION)) {
                if (operation.getOtherFluxes().isEmpty()) {
                    operation.addAllOtherFlux(createReportOtherFluxes(operation));
                }
            } else if (operation.getOperationType().getUuid().equals(OperationConstants.URGENT_REPORT_OPERATION)) {
                if (operation.getOtherFluxes().isEmpty()) {
                    operation.addAllOtherFlux(createEmergencyReportOtherFluxes(operation));
                }
            }

        } else if (operation.getOperationStatus().equals(OperationStatus.VALIDATED) ||
                operation.getOperationStatus().equals(OperationStatus.TREATED)) {

            if (operation.getOperationType().getUuid().equals(OperationConstants.REPORT_OPERATION)) {
                createReportMonthlyConsumption(operation);
            } else if (operation.getOperationType().getUuid().equals(OperationConstants.URGENT_REPORT_OPERATION)) {
                System.out.println("Urgent report calculating monthly consumption");
            } else if (operation.getOperationType().getUuid().equals(OperationConstants.INVENTORY_OPERATION)
                    || operation.getOperationType().getUuid().equals(OperationConstants.PARTIAL_INVENTORY_OPERATION)) {
                createInventoryAdjustment(operation);
            }

            validateOperation(operation);
        } else if (operation.getOperationStatus().equals(OperationStatus.CANCELED)) {
            if (operation.getOperationType().getUuid().equals(OperationConstants.DISPENSATION_OPERATION)) {
                ProductOperationAttribute attribute = operation.getAttributes()
                        .stream().filter(a -> a.getOperationAttributeType().getUuid().equals("ENCOUNTERAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))
                        .findFirst().orElse(null);
                if (attribute != null) {
                    String encounterUuid = attribute.getValue();
                    Encounter encounter = Context.getEncounterService().getEncounterByUuid(encounterUuid);
                    if (encounter != null) {
                        Context.getEncounterService().voidEncounter(encounter, "Voided by user because canceled");
                    }
                }
            }
            cancelOperation(operation);
        } else if (operation.getOperationStatus().equals(OperationStatus.SUBMITTED)) {
            notification = getProductNotification(
                    operation,
                    OperationConstants.REPORT_SUBMISSION_NOTIFICATION,
                    operation.getLocation().getParentLocation());
            if (SupplyUtils.isDirectClient(SupplyUtils.getUserLocation())) {
                List<ProductCode> productCodes = operation.getProductList();
                Double leadTime = 0.;

                ProductOperationAttribute attribute = operation.getAttributes().stream()
                        .filter(a -> a.getOperationAttributeType().getUuid().equals("LEADTIMEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))
                        .findFirst().orElse(null);

                if (attribute != null) {
                    leadTime = Double.parseDouble(attribute.getValue());
                }

                for (ProductCode productCode :
                        productCodes) {
                    createProposedQuantity(productCode, operation, leadTime);
                }
            }
        } else if (operation.getOperationStatus().equals(OperationStatus.APPROVED)) {
            createDistribution(operation);
        }

        if ((operation.getOperationType().getUuid().equals(OperationConstants.INVENTORY_OPERATION)
                || operation.getOperationType().getUuid().equals(OperationConstants.PARTIAL_INVENTORY_OPERATION)) &&
                !operation.getOperationStatus().equals(OperationStatus.VALIDATED) && !operation.getFluxes().isEmpty()) {
            updateInventoryFluxesRelatedQuantity(operation);
        }

        if (operation.getOperationStatus().equals(OperationStatus.VALIDATED) ||
                operation.getOperationStatus().equals(OperationStatus.TREATED) ||
                operation.getOperationStatus().equals(OperationStatus.CANCELED) ||
                operation.getOperationStatus().equals(OperationStatus.SUBMITTED)) {
            generateStockStatus(operation);
        }

        getSession().saveOrUpdate(operation);

        if (notification != null) {
            getSession().save(notification);
        }

        return operation;

//        if (operation.getOperationStatus().equals(OperationStatus.VALIDATED)
//                || operation.getOperationStatus().equals(OperationStatus.TREATED)) {
//            validateOperation(operation);
//
//        } else if (operation.getOperationStatus().equals(OperationStatus.CANCELED)) {
//
//        } else if (operation.getOperationStatus().equals(OperationStatus.SUBMITTED)) {
//            notification = getProductNotification(
//                    operation,
//                    OperationConstants.REPORT_SUBMISSION_NOTIFICATION,
//                    operation.getLocation().getParentLocation());
//            if (SupplyUtils.isDirectClient(SupplyUtils.getUserLocation())) {
//                List<ProductCode> productCodes = operation.getProductList();
//                Double leadTime = 0.;
//
//                ProductOperationAttribute attribute = operation.getAttributes().stream()
//                        .filter(a -> a.getOperationAttributeType().getUuid().equals("LEADTIMEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))
//                        .findFirst().orElse(null);
//
//                if (attribute != null) {
//                    leadTime = Double.parseDouble(attribute.getValue());
//                }
//
//                for (ProductCode productCode :
//                        productCodes) {
//                    createProposedQuantity(productCode, operation, leadTime);
//                }
//            }
//        } else if (operation.getOperationStatus().equals(OperationStatus.APPROVED)) {
//            createDistribution(operation);
//        }

    }
	
	private void createReportMonthlyConsumption(ProductOperation operation) {
        List<ProductOperationOtherFlux> operationOtherFluxList = operation.getOtherFluxes()
                .stream().filter(f -> (f.getLabel().equals(ReportConstants.DISTRIBUTED_QUANTITY_M2)
                        || f.getLabel().equals(ReportConstants.DISTRIBUTED_QUANTITY_M1)
                        || f.getLabel().equals(ReportConstants.DISTRIBUTED_QUANTITY)))
                .collect(Collectors.toList());

        List<ProductOperationOtherFlux> otherFluxes = new ArrayList<>();

        if (SupplyUtils.isDirectClient(operation.getLocation())) {
            otherFluxes = getOperationOtherFluxes(
                    Collections.singletonList(operation.getOperationType()),
                    operation.getLocation(), operation.getProductProgram(), ReportConstants.DISTRIBUTED_QUANTITY_M2, operation.getOperationDate(),
                    SupplyUtils.getMonthsForCMM(operation.getLocation()) - 1);

            if (otherFluxes == null) {
                otherFluxes = new ArrayList<>();
            }
        }

        for (ProductCode productCode : operation.getProductList()) {

            if (!otherFluxes.isEmpty()) {
                List<ProductOperationOtherFlux> oldDistributedM2 = otherFluxes.stream().filter(f -> f.getProductCode().equals(productCode)).collect(Collectors.toList());
                operationOtherFluxList.addAll(oldDistributedM2);
            }

            Double quantity = 0.;
            for (ProductOperationOtherFlux operationOtherFlux : operationOtherFluxList.stream().filter(f -> f.getProductCode().equals(productCode)).collect(Collectors.toList())) {
                quantity += operationOtherFlux.getQuantity();
            }
            Double monthlyConsumptionQuantity = Math.round(quantity / (otherFluxes.size() + 3)) * 1.;

            ProductOperationOtherFlux existingMonthlyConsumption = operation.getOtherFluxes()
                    .stream().filter(f -> f.getProductCode().equals(productCode) && f.getLabel().equals(ReportConstants.MONTHLY_CONSUMPTION))
                    .findFirst().orElse(null);

            if (existingMonthlyConsumption != null) {
                existingMonthlyConsumption.setQuantity(monthlyConsumptionQuantity);
                saveProductOperationOtherFlux(existingMonthlyConsumption);
            } else {
                ProductOperationOtherFlux newOtherFlux = new ProductOperationOtherFlux(productCode, ReportConstants.MONTHLY_CONSUMPTION, monthlyConsumptionQuantity, operation.getLocation());
                operation.addOtherFlux(newOtherFlux);
            }

            ProductOperationOtherFlux existingProposedMonthlyConsumption = operation.getOtherFluxes()
                    .stream().filter(f -> f.getProductCode().equals(productCode) && f.getLabel().equals(ReportConstants.PROPOSED_MONTHLY_CONSUMPTION))
                    .findFirst().orElse(null);

            if (existingProposedMonthlyConsumption != null) {
                existingProposedMonthlyConsumption.setQuantity(monthlyConsumptionQuantity);
                saveProductOperationOtherFlux(existingProposedMonthlyConsumption);
            } else {
                ProductOperationOtherFlux proposedOtherFlux = new ProductOperationOtherFlux(productCode, ReportConstants.PROPOSED_MONTHLY_CONSUMPTION, monthlyConsumptionQuantity, operation.getLocation());
                operation.addOtherFlux(proposedOtherFlux);
            }
        }
    }
	
	private Double createProposedQuantity(ProductCode productCode, ProductOperation operation, Double leadTime) {
        double quantity = 0.;
        ProductOperationOtherFlux monthlyConsumption = operation.getOtherFluxes().stream()
                .filter(p -> p.getProductCode().equals(productCode) && p.getLabel().equals(ReportConstants.MONTHLY_CONSUMPTION))
                .findFirst().orElse(null);

        ProductOperationOtherFlux availableQuantity = operation.getOtherFluxes().stream()
                .filter(p -> p.getProductCode().equals(productCode) && p.getLabel().equals(ReportConstants.AVAILABLE_QUANTITY))
                .findFirst().orElse(null);

        if (monthlyConsumption != null && availableQuantity != null) {
            if (monthlyConsumption.getQuantity() > 0) {
                quantity = Math.round((SupplyUtils.getLocationStockMax(operation.getLocation()) + leadTime) * monthlyConsumption.getQuantity() - availableQuantity.getQuantity()) * 1.;
            }
            ProductOperationOtherFlux otherFlux = new ProductOperationOtherFlux(
                    productCode, ReportConstants.PROPOSED_QUANTITY, quantity >= 0. ? quantity : 0, operation.getLocation()
            );
            otherFlux.setOperation(operation);
            saveProductOperationOtherFlux(otherFlux);
            if (quantity > 0) {
                return quantity;
            }
        }
        return 0.;
    }
	
	private void createDistribution(ProductOperation operation) throws ParseException {
        List<ProductCode> productCodes = operation.getProductList();

        ProductOperation distribution = new ProductOperation();
        ProductOperationAttribute treatmentAttribute = operation.getAttributes().stream()
                .filter(a -> a.getOperationAttributeType().getUuid().equals(ReportConstants.TREATMENT_DATE)).findFirst().orElse(null);
        if (treatmentAttribute != null) {
            DateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date treatmentDate = sourceFormat.parse(treatmentAttribute.getValue());
            distribution.setOperationDate(treatmentDate);
        } else {
            distribution.setOperationDate(new Date());
        }

        distribution.setProductProgram(operation.getProductProgram());
        distribution.setOperationType(getProductOperationType(OperationConstants.DISTRIBUTION_OPERATION));
        distribution.setQuantityType(QuantityType.DISPENSATION);
        distribution.setIncidence(Incidence.NEGATIVE);
        distribution.setLocation(SupplyUtils.getUserLocation());
        distribution.setExchangeLocation(operation.getLocation());
        distribution.setOperationStatus(OperationStatus.NOT_COMPLETED);
        distribution.setOperationNumber(generateDeliveryNumber());
        distribution.setParentOperation(operation);

        Double leadTime = 0.;

        ProductOperationAttribute attribute = operation.getAttributes().stream()
                .filter(a -> a.getOperationAttributeType().getUuid().equals("LEADTIMEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))
                .findFirst().orElse(null);

        if (attribute != null) {
            leadTime = Double.parseDouble(attribute.getValue());
        }

        for (ProductCode productCode : productCodes) {
            Double quantity = createProposedQuantity(productCode, operation, leadTime);
            ProductOperationFlux flux = new ProductOperationFlux();
            flux.setLocation(SupplyUtils.getUserLocation());
            flux.setProductCode(productCode);
            flux.setQuantity(productCode.getQuantityInStock() <= quantity ? quantity - productCode.getQuantityInStock() : quantity);
            flux.setRelatedQuantity(productCode.getQuantityInStock() <= quantity ? quantity - productCode.getQuantityInStock() : quantity);
            flux.setRelatedQuantityLabel("Quantité proposée");
            distribution.addFlux(flux);
        }

        getSession().saveOrUpdate(distribution);
    }
	
	private String generateDeliveryNumber() {
		String code = getLocationCode(SupplyUtils.getUserLocation());
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"));
		calendar.setTime(new Date());
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		
		String generatedCode = code + "-" + year + (month < 10 ? "0" + month : month) + "-"
		        + SupplyUtils.generateRandom(5).toUpperCase();
		
		List<ProductOperation> operations = getProductOperationByOperationNumber(generatedCode,
		    SupplyUtils.getUserLocation(), true);
		if (operations != null && !operations.isEmpty()) {
			generatedCode = generateDeliveryNumber();
		}
		return generatedCode;
	}
	
	private String getLocationCode(Location location) {
        LocationAttribute attribute = location
                .getAttributes().stream().filter(a -> a.getAttributeType().getUuid().equals("ccb5b9f5-9432-485b-973b-6cde77f4ea3d"))
                .findFirst().orElse(null);
        if (attribute != null) {
            return attribute.getValueReference();
        }
        return "";
    }
	
	private static ProductNotification getProductNotification(ProductOperation operation, String notificationType,
	        Location notifiedTo) {
		ProductNotification notification = new ProductNotification();
		notification.setLocation(operation.getLocation());
		notification.setOperationType(operation.getOperationType());
		notification.setNotificationDate(new Date());
		notification.setNotifiedTo(notifiedTo);
		notification.setNotification(notificationType);
		notification.setNotificationInfo(operation.getOperationNumber() + " - " + operation.getOperationDate().toString());
		return notification;
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
	
	private List<ProductOperationFlux> createInventoryFluxes(ProductOperation operation) {
        List<ProductAttributeStock> stocks = getAllProductAttributeStocks(SupplyUtils.getUserLocation(), operation.getProductProgram());
        List<ProductOperationFlux> fluxes = new ArrayList<>(operation.getFluxes());
        for (ProductAttributeStock stock : stocks) {
            if (!fluxContainsAttribute(fluxes, stock.getAttribute())) {
                ProductOperationFlux flux = createFlux(operation, 0., stock.getAttribute().getProductCode());
                flux.setRelatedQuantity(stock.getQuantityInStock().doubleValue());
                flux.setRelatedQuantityLabel("Quantité Théorique");
                flux.setProductCode(stock.getAttribute().getProductCode());

                ProductOperationFluxAttribute attribute = new ProductOperationFluxAttribute();
                attribute.setAttribute(stock.getAttribute());
                attribute.setQuantity(0.);
                attribute.setLocation(operation.getLocation());

                flux.addAttribute(attribute);
                fluxes.add(flux);
            }
        }
        return fluxes;
    }
	
	private void updateInventoryFluxesRelatedQuantity(ProductOperation operation) {
        List<ProductAttributeStock> stocks = getAllProductAttributeStocks(SupplyUtils.getUserLocation(), operation.getProductProgram());
        for (ProductOperationFlux flux : operation.getFluxes()) {
            ProductOperationFluxAttribute fluxAttribute = flux.getAttributes().stream().findFirst().orElse(null);
            if (fluxAttribute != null) {
                ProductAttribute attribute = fluxAttribute.getAttribute();
                ProductAttributeStock stock = stocks.stream().filter(s -> s.getAttribute().equals(attribute)).findFirst().orElse(null);
                if (stock != null) {
                    if (!stock.getQuantityInStock().equals(flux.getRelatedQuantity().intValue())) {
                        flux.setRelatedQuantity(stock.getQuantityInStock().doubleValue());
                        saveProductOperationFlux(flux);
                    }
                }
            }
        }
    }
	
	private List<ProductOperationFlux> createFluxesFromOperation(ProductOperation operation, ProductOperation parentOperation) {
        List<ProductOperationFlux> fluxes = new ArrayList<>();
        for (ProductOperationFlux flux : parentOperation.getFluxes()) {
            if (flux.getQuantity() > 0) {
                for (ProductOperationFluxAttribute fluxAttribute : flux.getAttributes()) {
                    ProductOperationFlux operationFlux = createFlux(operation, fluxAttribute.getQuantity(), flux.getProductCode());
                    operationFlux.setRelatedQuantity(fluxAttribute.getQuantity());
                    if (operation.getOperationType().getUuid().equals(OperationConstants.RECEPTION_OPERATION)) {
                        operationFlux.setRelatedQuantityLabel("Quantité livrée");
                    } else if (operation.getOperationType().getUuid().equals(OperationConstants.TRANSFER_IN_OPERATION)) {
                        operationFlux.setRelatedQuantityLabel("Quantité transférée");
                    }
                    operationFlux.addAllAttributes(createFluxAttribute(Collections.singleton(fluxAttribute), SupplyUtils.getUserLocation()));
                    fluxes.add(operationFlux);
                }
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
		getSession().saveOrUpdate(productAttribute);
		return productAttribute;
	}
	
	private Boolean fluxContainsAttribute(List<ProductOperationFlux> fluxes, ProductAttribute attribute) {
        Set<ProductOperationFluxAttribute> fluxAttributes = new HashSet<>();
        for (ProductOperationFlux flux : fluxes) {
            fluxAttributes.addAll(flux.getAttributes());
        }
        return fluxAttributes.stream().filter(fa -> fa.getAttribute().getUuid().equals(attribute.getUuid())).findFirst().orElse(null) != null;
    }
	
	private Boolean fluxContainsProduct(ProductOperation operation, ProductCode productCode) {
		for (ProductOperationFlux flux : operation.getFluxes()) {
			if (flux.getProductCode().equals(productCode)) {
				return true;
			}
		}
		return false;
	}
	
	private ProductOperationFlux createFlux(ProductOperation operation, Double quantity, ProductCode productCode) {
		ProductOperationFlux operationFlux = new ProductOperationFlux();
		operationFlux.setQuantity(quantity);
		operationFlux.setLocation(operation.getLocation());
		operationFlux.setProductCode(productCode);
		return operationFlux;
	}
	
	private void createInventoryAdjustment(ProductOperation operation) {
        ProductOperation positiveAdjustment = new ProductOperation();
        ProductOperation negativeAdjustment = new ProductOperation();

        for (ProductOperationFlux flux : operation.getFluxes()) {
            ProductOperationFluxAttribute attribute = new ProductOperationFluxAttribute();
            List<ProductOperationFluxAttribute> attributes = new ArrayList<>(flux.getAttributes());
            for (ProductOperationFluxAttribute current : attributes) {
                attribute.setAttribute(current.getAttribute());
                attribute.setLocation(current.getLocation());
                break;
            }
            if (flux.getRelatedQuantity() != null) {
                if (flux.getQuantity() < flux.getRelatedQuantity()) {
                    ProductOperationFlux fluxToAdd = createFlux(operation,
                            flux.getRelatedQuantity() - flux.getQuantity(), flux.getProductCode());
                    attribute.setQuantity(fluxToAdd.getQuantity());
                    fluxToAdd.addAttribute(attribute);
                    negativeAdjustment.addFlux(fluxToAdd);
                } else if (flux.getQuantity() > flux.getRelatedQuantity()) {
                    ProductOperationFlux fluxToAdd = createFlux(operation,
                            flux.getRelatedQuantity() - flux.getQuantity(), flux.getProductCode());
                    attribute.setQuantity(fluxToAdd.getQuantity());
                    fluxToAdd.addAttribute(attribute);
                    positiveAdjustment.addFlux(fluxToAdd);
                }
            }
        }

        if (!positiveAdjustment.getFluxes().isEmpty()) {
            positiveAdjustment
                    .setOperationType(getProductOperationType(OperationConstants.INVENTORY_POSITIVE_ADJUST_OPERATION));
            setAdjustmentProperties(operation, positiveAdjustment);
        }
        if (!positiveAdjustment.getFluxes().isEmpty()) {
            negativeAdjustment
                    .setOperationType(getProductOperationType(OperationConstants.INVENTORY_NEGATIVE_ADJUST_OPERATION));
            setAdjustmentProperties(operation, negativeAdjustment);
        }
    }
	
	private void setAdjustmentProperties(ProductOperation operation, ProductOperation adjustment) {
		adjustment.setOperationDate(operation.getOperationDate());
		adjustment.setIncidence(Incidence.NONE);
		adjustment.setProductProgram(operation.getProductProgram());
		adjustment.setOperationStatus(OperationStatus.VALIDATED);
		adjustment.setOperationNumber(operation.getOperationNumber());
		adjustment.setLocation(operation.getLocation());
		adjustment.setParentOperation(operation);
		getSession().saveOrUpdate(adjustment);
	}
	
	@SuppressWarnings("unchecked")
	private List<ProductAttributeStock> getAllProductAttributeStocks(Location location, ProductProgram program) {
		return (List<ProductAttributeStock>) getSession().createCriteria(ProductAttributeStock.class, "s")
		        .createAlias("s.attribute", "a").createAlias("a.productCode", "p")
		        .add(Restrictions.eq("s.location", location)).add(Restrictions.eq("s.voided", false))
		        .add(Restrictions.ge("quantityInStock", 0)).add(Restrictions.eq("p.program", program)).list();
	}
	
	public void purgeProductOperation(ProductOperation productOperation) {
		getSession().delete(productOperation);
	}
	
	//    @SuppressWarnings("unchecked")
	public List<ProductOperation> findLatestOperationsByProgram(ProductOperationType operationType, Location location, Date endDate) {
        Query query = getSession().createQuery("SELECT o.productProgram, MAX(o.operationDate) " +
                        "FROM ProductOperation o " +
                        "WHERE o.operationType = :operationType AND o.location = :location AND o.operationDate <= :endDate AND o.operationStatus = :status AND o.voided = false " +
                        "GROUP BY o.productProgram")
                .setParameter("operationType", operationType)
                .setParameter("location", location)
                .setParameter("status", OperationStatus.VALIDATED)
                .setParameter("endDate", endDate);

        List<ProductOperation> operations = new ArrayList<>();

        List<?> list = query.list();
        for (Object o : list) {
            Object[] row = (Object[]) o;
//            System.out.println(row[0] + ", " + row[1]);

            ProductOperation productOperation = (ProductOperation) getSession().createCriteria(ProductOperation.class)
                    .add(Restrictions.eq("operationType", operationType))
                    .add(Restrictions.eq("productProgram", row[0]))
                    .add(Restrictions.eq("location", location))
                    .add(Restrictions.eq("operationDate", row[1]))
                    .setMaxResults(1).uniqueResult();
            ;

            if (productOperation != null) {
                operations.add(productOperation);
            }
        }
        return operations;
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
	
	public ProductOperationFlux saveProductOperationFlux(ProductOperationFlux productOperationFlux) {
		getSession().saveOrUpdate(productOperationFlux);
		return productOperationFlux;
	}
	
	public void purgeProductOperationFlux(ProductOperationFlux productOperationFlux) {
		getSession().delete(productOperationFlux);
	}
	
	public ProductOperationOtherFlux getProductOperationOtherFlux(String uuid) {
		Criteria criteria = getSession().createCriteria(ProductOperationOtherFlux.class);
		return (ProductOperationOtherFlux) criteria.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	public ProductOperationOtherFlux saveProductOperationOtherFlux(ProductOperationOtherFlux productOperationOtherFlux) {
		getSession().saveOrUpdate(productOperationOtherFlux);
		return productOperationOtherFlux;
	}
	
	public void purgeProductOperationOtherFlux(ProductOperationOtherFlux productOperationOtherFlux) {
		getSession().delete(productOperationOtherFlux);
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
	
	@SuppressWarnings("unchecked")
	public List<ProductAttributeStock> getAllProductAttributeStocks(Location location, ProductCode productCode) {
		return (List<ProductAttributeStock>) getSession().createCriteria(ProductAttributeStock.class, "s")
		        .createAlias("s.attribute", "a").add(Restrictions.eq("s.location", location))
		        .add(Restrictions.gt("s.quantityInStock", 0)).add(Restrictions.eq("s.voided", false))
		        .add(Restrictions.eq("a.productCode", productCode))
		        //		        .add(Restrictions.eq("stock.program", program))
		        .addOrder(Order.asc("a.expiryDate")).list();
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
	        Boolean availableOnly, Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductAttributeStock.class, "s").createAlias("s.attribute", "a")
		        .createAlias("a.productCode", "p");
		if (!includeVoided) {
			criteria.add(Restrictions.eq("s.voided", false));
		}
		if (availableOnly) {
			criteria.add(Restrictions.gt("s.quantityInStock", 0));
		}
		return criteria.add(Restrictions.eq("s.location", location)).add(Restrictions.eq("p.program", program)).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductAttributeStock> getAllProductAttributeStocks(Location location, ProductProgram program,
	        Date startDate, Date endDate, Boolean availableOnly, Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductAttributeStock.class, "s").createAlias("s.attribute", "a")
		        .createAlias("a.productCode", "p").createAlias("s.operation", "o");
		if (!includeVoided) {
			criteria.add(Restrictions.eq("s.voided", false));
		}
		if (availableOnly) {
			criteria.add(Restrictions.gt("s.quantityInStock", 0));
		}
		criteria.add(Restrictions.between("o.operationDate", startDate, endDate));
		return criteria.add(Restrictions.eq("s.location", location)).add(Restrictions.eq("p.program", program)).list();
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
	
	public ProductAttributeStock getProductAttributeStock(ProductAttribute productAttribute,
	//			, ProductProgram productProgram,
	        Location location) {
		return (ProductAttributeStock) getSession().createCriteria(ProductAttributeStock.class)
		        .add(Restrictions.eq("attribute", productAttribute))
		        //				.add(Restrictions.eq("program", productProgram))
		        .add(Restrictions.eq("location", location)).add(Restrictions.eq("voided", false)).uniqueResult();
	}
	
	public Integer getProductQuantityInStock(ProductCode productCode, Location location) {
		List<ProductAttributeStock> stocks = getAllProductAttributeStocks(location, productCode);
		Integer quantity = 0;
		if (stocks != null) {
			for (ProductAttributeStock stock : stocks) {
				quantity += stock.getQuantityInStock();
			}
		}
		return quantity;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductAttributeStock> getProductAttributeStockByExpiryDate(ProductCode productCode, Date currentDate,
	        Location location) {
		return getSession().createCriteria(ProductAttributeStock.class, "s").createAlias("s.attribute", "a")
		        .add(Restrictions.eq("a.productCode", productCode)).add(Restrictions.lt("a.expiryDate", currentDate))
		        .add(Restrictions.eq("s.location", location)).add(Restrictions.eq("s.voided", false)).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductAttributeStock> getProductAttributeStockByExpired(Date currentDate, Location location,
	        ProductProgram program) {
		return getSession().createCriteria(ProductAttributeStock.class, "s").createAlias("s.attribute", "a")
		        .createAlias("a.productCode", "p").add(Restrictions.lt("a.expiryDate", currentDate))
		        .add(Restrictions.eq("p.program", program)).add(Restrictions.gt("s.quantityInStock", 0))
		        .add(Restrictions.eq("s.location", location)).add(Restrictions.eq("s.voided", false)).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductAttributeStock> getProductAttributeStockByExpiring(Date currentDate, Location location,
	        ProductProgram program) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(currentDate.getTime());
		calendar.add(Calendar.DATE, 90);
		return getSession().createCriteria(ProductAttributeStock.class, "s").createAlias("s.attribute", "a")
		        .createAlias("a.productCode", "p")
		        .add(Restrictions.between("a.expiryDate", currentDate, calendar.getTime()))
		        .add(Restrictions.eq("s.location", location)).add(Restrictions.gt("s.quantityInStock", 0))
		        .add(Restrictions.eq("p.program", program)).add(Restrictions.eq("s.voided", false)).list();
	}
	
	public List<ProductOperationOtherFlux> getAllProductOperationOtherFluxByOperationAndProduct(ProductOperation operation,
	        ProductCode product, Location location) {
		return null;
	}
	
	public ProductOperationOtherFlux getProductOperationOtherFluxByProductAndOperationAndLabel(ProductCode product,
	        ProductOperation operation, String label, Location location) {
		return null;
	}
	
	@SuppressWarnings("unchecked")
    private List<ProductCode> getAllActivitiesProducts(Date startDate, Date endDate, ProductProgram program,
                                                       Location location) {
        List<ProductOperation> operations = getSession().createCriteria(ProductOperation.class)
                .add(Restrictions.eq("productProgram", program))
                .add(Restrictions.between("operationDate", startDate, endDate))
                .add(Restrictions.eq("operationStatus", OperationStatus.VALIDATED))
                .add(Restrictions.eq("voided", false))
                .add(Restrictions.eq("location", location))
                .list();

        List<ProductCode> productCodes = new ArrayList<>();
        for (ProductOperation operation : operations) {
            for (ProductCode productCode : operation.getProductList()) {
                if (!productCodes.contains(productCode)) {
                    productCodes.add(productCode);
                }
            }
        }

        return productCodes;
    }
	
	private List<ProductOperationOtherFlux> createReportOtherFluxes(ProductOperation currentReport) {
        List<ProductOperationOtherFlux> otherFluxes = new ArrayList<>();
        ProductOperation inventory = currentReport.getParentOperation();
        if (inventory != null) {
            ProductOperationAttribute attribute = inventory.getAttributes().stream()
                    .filter(a -> a.getOperationAttributeType().getUuid().equals("INVENTORYSTARTDATEAAAAAAAAAAAAAAAAAAAA"))
                    .findFirst().orElse(null);
            if (attribute != null) {
                Date startDate;
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    String dateString = attribute.getValue().split("T")[0];
                    startDate = format.parse(dateString);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                Date endDate = inventory.getOperationDate();

                List<ProductOperationFlux> adjustmentFLuxes = getOperationFluxes(
                        Arrays.asList(
                                getProductOperationType(OperationConstants.TRANSFER_OUT_OPERATION),
                                getProductOperationType(OperationConstants.TRANSFER_IN_OPERATION),
                                getProductOperationType(OperationConstants.DONATION_OPERATION),
                                getProductOperationType(OperationConstants.PRODUCT_RETURN_OUT_OPERATION),
                                getProductOperationType(OperationConstants.PRODUCT_RETURN_IN_OPERATION),
                                getProductOperationType(OperationConstants.RECEPTION_RETURN_OPERATION)
                        ),
                        startDate,
                        endDate,
                        currentReport.getLocation(),
                        currentReport.getProductProgram()
                );

//                ProductOperation previousReport = getLastProductOperation(
//                        getProductOperationType(OperationConstants.REPORT_OPERATION),
//                        inventory.getProductProgram(),
//                        endDate,
//                        inventory.getLocation(),
//                        true,
//                        false
//                );

                List<ProductOperationFlux> dispensationFLuxes = getOperationFluxes(
                        Collections.singletonList(getProductOperationType(
                                OperationConstants.DISPENSATION_OPERATION)
                        ),
                        startDate,
                        endDate,
                        currentReport.getLocation(),
                        currentReport.getProductProgram()
                );

                List<ProductOperationOtherFlux> childrenUsedFluxes = new ArrayList<>();

                if (!getChildLocationListWithPrograms(currentReport.getLocation()).isEmpty()) {
                    childrenUsedFluxes = getOperationOtherFluxes(
                            Collections.singletonList(getProductOperationType(OperationConstants.REPORT_OPERATION)),
                            currentReport.getOperationNumber(),
                            currentReport.getLocation(),
                            currentReport.getProductProgram(),
                            ReportConstants.DISTRIBUTED_QUANTITY,
                            true,
                            -1
                    );
                }

//                List<ProductOperationOtherFlux> lastConsumptionReportFluxes = getOperationOtherFluxes(
//                        Collections.singletonList(getProductOperationType(OperationConstants.REPORT_OPERATION)),
//                        inventory.getLocation(),
//                        inventory.getProductProgram(),
//                        ReportConstants.DISTRIBUTED_QUANTITY,
//                        SupplyUtils.getMonthsForCMM()
//                );

                List<ProductOperationOtherFlux> childrenAvailableFluxes = new ArrayList<>();

                if (!getChildLocationListWithPrograms(currentReport.getLocation()).isEmpty()) {
                    childrenAvailableFluxes = getOperationOtherFluxes(
                            Collections.singletonList(getProductOperationType(OperationConstants.REPORT_OPERATION)),
                            currentReport.getOperationNumber(),
                            currentReport.getLocation(),
                            currentReport.getProductProgram(), ReportConstants.AVAILABLE_QUANTITY,
                            true,
                            -1
                    );
                }

                List<ProductOperationFlux> receptionFLuxes = getOperationFluxes(
                        Collections.singletonList(getProductOperationType(OperationConstants.RECEPTION_OPERATION)),
                        startDate,
                        endDate,
                        currentReport.getLocation(),
                        currentReport.getProductProgram()
                );

                ProductOperation previousInventory = getLastProductOperation(
                        getProductOperationType(OperationConstants.INVENTORY_OPERATION),
                        currentReport.getProductProgram(),
                        endDate,
                        currentReport.getLocation(),
                        true, false);

                ProductOperation previousReport = getLastProductOperation(
                        getProductOperationType(OperationConstants.REPORT_OPERATION),
                        currentReport.getProductProgram(),
                        endDate,
                        currentReport.getLocation(),
                        true, false);

                List<ProductOperationFlux> lossFLuxes = getOperationFluxes(
                        Arrays.asList(
                                getProductOperationType(OperationConstants.EXPIRY_OPERATION),
                                getProductOperationType(OperationConstants.THIEF_OPERATION),
                                getProductOperationType(OperationConstants.SPOILED_OPERATION),
                                getProductOperationType(OperationConstants.OTHER_OUT_OPERATION),
                                getProductOperationType(OperationConstants.DESTROYED_OPERATION)
                        ),
                        startDate,
                        endDate,
                        currentReport.getLocation(),
                        currentReport.getProductProgram()
                );

                List<ProductCode> productCodes = getAllActivitiesProducts(startDate, endDate, currentReport.getProductProgram(), currentReport.getLocation());

                for (ProductCode productCode : productCodes) {
                    Double initialQuantity = 0.;
                    ProductOperationOtherFlux availableOtherFlux = null;
                    if (previousReport != null) {
                        availableOtherFlux = previousReport.getOtherFluxes().stream()
                                .filter(o -> o.getProductCode().equals(productCode) && o.getLabel().equals(ReportConstants.AVAILABLE_QUANTITY)).findFirst().orElse(null);
                        if (availableOtherFlux != null) {
                            initialQuantity = availableOtherFlux.getQuantity();
                        }

                    }
                    if (availableOtherFlux == null && previousInventory != null) {
                        initialQuantity = getProductFluxQuantity(
                                productCode,
                                previousInventory.getFluxes().stream().filter(f -> f.getProductCode().equals(productCode)).collect(Collectors.toList()));

                        if (!getChildLocationListWithPrograms(currentReport.getLocation()).isEmpty()) {
                            for (Location location : getChildLocationListWithPrograms(currentReport.getLocation())) {
                                ProductOperation childReport = getProductOperationByOperationNumber(
                                        getProductOperationType(OperationConstants.REPORT_OPERATION),
                                        currentReport.getProductProgram(),
                                        currentReport.getOperationNumber(),
                                        location, true);
                                if (childReport != null) {
                                    ProductOperationOtherFlux otherFlux = childReport.getOtherFluxes().stream()
                                            .filter(o -> o.getProductCode().equals(productCode) && o.getLabel().equals(ReportConstants.INITIAL_QUANTITY)).findFirst().orElse(null);
                                    if (otherFlux != null) {
                                        initialQuantity += otherFlux.getQuantity();
                                    }
                                }
                            }
                        }
                    }

                    otherFluxes.add(createOtherFlux(
                            initialQuantity,
                            ReportConstants.INITIAL_QUANTITY,
                            productCode,
                            inventory.getLocation()));

                    otherFluxes.add(getAvailableQuantity(productCode, inventory, childrenAvailableFluxes));
                    otherFluxes.add(getReceivedQuantity(productCode, receptionFLuxes, inventory.getLocation()));
                    otherFluxes.add(getDistributedQuantity(productCode, dispensationFLuxes, childrenUsedFluxes, inventory.getLocation()));
                    otherFluxes.add(getAdjustmentQuantity(productCode, inventory, currentReport, adjustmentFLuxes));
//                    otherFluxes.add(getAverageMonthlyConsumption(productCode, lastConsumptionReportFluxes, inventory.getLocation()));

                    Double lostQuantity = getProductFluxQuantity(
                            productCode,
                            lossFLuxes.stream().filter(f -> f.getProductCode().equals(productCode)).collect(Collectors.toList()));
                    if (!getChildLocationListWithPrograms(currentReport.getLocation()).isEmpty()) {
                        for (Location location : getChildLocationListWithPrograms(currentReport.getLocation())) {
                            ProductOperation childReport = getProductOperationByOperationNumber(
                                    getProductOperationType(OperationConstants.REPORT_OPERATION),
                                    currentReport.getProductProgram(),
                                    currentReport.getOperationNumber(),
                                    location, true);
                            if (childReport != null) {
                                ProductOperationOtherFlux otherFlux = childReport.getOtherFluxes().stream()
                                        .filter(o -> o.getProductCode().equals(productCode) && o.getLabel().equals(ReportConstants.LOSS_QUANTITY)).findFirst().orElse(null);
                                if (otherFlux != null) {
                                    lostQuantity += otherFlux.getQuantity();
                                }
                            }
                        }
                    }
                    otherFluxes.add(createOtherFlux(
                            lostQuantity,
                            ReportConstants.LOSS_QUANTITY,
                            productCode,
                            inventory.getLocation())
                    );


                    if (previousReport != null) {
                        otherFluxes.add(createOtherFlux(
                                getProductOtherFluxQuantity(
                                        productCode,
                                        previousReport.getOtherFluxes().stream().filter(f -> f.getProductCode().equals(productCode)).collect(Collectors.toList()),
                                        ReportConstants.DISTRIBUTED_QUANTITY),
                                ReportConstants.DISTRIBUTED_QUANTITY_M1,
                                productCode,
                                inventory.getLocation())
                        );

                        otherFluxes.add(createOtherFlux(
                                getProductOtherFluxQuantity(
                                        productCode,
                                        previousReport.getOtherFluxes().stream().filter(f -> f.getProductCode().equals(productCode)).collect(Collectors.toList()),
                                        ReportConstants.DISTRIBUTED_QUANTITY_M1),
                                ReportConstants.DISTRIBUTED_QUANTITY_M2,
                                productCode,
                                inventory.getLocation())
                        );
                    }
                }
            }
        }
        return otherFluxes;
    }
	
	private List<ProductOperationOtherFlux> createEmergencyReportOtherFluxes(ProductOperation currentReport) {
        List<ProductOperationOtherFlux> otherFluxes = new ArrayList<>();
        ProductOperation inventory = currentReport.getParentOperation();
        for (ProductCode productCode : inventory.getProductList()) {

            ProductOperation inventoryBefore = getLastProductOperation(Arrays.asList(getProductOperationType(
                                    OperationConstants.INVENTORY_OPERATION),
                            getProductOperationType(OperationConstants.PARTIAL_INVENTORY_OPERATION)
                    ),
                    productCode,
                    inventory.getOperationDate(),
                    currentReport.getProductProgram(), currentReport.getLocation(), false);

            ProductOperation latestReport = getLastProductOperation(
                    Arrays.asList(getProductOperationType(
                                    OperationConstants.REPORT_OPERATION),
                            getProductOperationType(OperationConstants.URGENT_REPORT_OPERATION)
                    ), productCode, currentReport.getProductProgram(), currentReport.getLocation(), false
            );

            if (inventoryBefore != null) {
                List<ProductOperationFlux> dispensationFLuxes = getOperationFluxes(
                        Arrays.asList(getProductOperationType(
                                        OperationConstants.DISPENSATION_OPERATION),
                                getProductOperationType(OperationConstants.DISTRIBUTION_OPERATION)
                        ),
                        inventory.getOperationDate(),
                        inventoryBefore.getOperationDate(),
                        inventory.getLocation(),
                        inventory.getProductProgram()
                );

                List<ProductOperationFlux> adjustmentFLuxes = getOperationFluxes(
                        Arrays.asList(
                                getProductOperationType(OperationConstants.TRANSFER_OUT_OPERATION),
                                getProductOperationType(OperationConstants.TRANSFER_IN_OPERATION),
                                getProductOperationType(OperationConstants.DONATION_OPERATION),
                                getProductOperationType(OperationConstants.PRODUCT_RETURN_OUT_OPERATION),
                                getProductOperationType(OperationConstants.PRODUCT_RETURN_IN_OPERATION),
                                getProductOperationType(OperationConstants.RECEPTION_RETURN_OPERATION)
                        ),
                        inventory.getOperationDate(),
                        inventoryBefore.getOperationDate(),
                        inventory.getLocation(),
                        inventory.getProductProgram()
                );

                List<ProductOperationFlux> lossFLuxes = getOperationFluxes(
                        Arrays.asList(
                                getProductOperationType(OperationConstants.EXPIRY_OPERATION),
                                getProductOperationType(OperationConstants.THIEF_OPERATION),
                                getProductOperationType(OperationConstants.SPOILED_OPERATION),
                                getProductOperationType(OperationConstants.OTHER_OUT_OPERATION),
                                getProductOperationType(OperationConstants.DESTROYED_OPERATION)
                        ),
                        inventory.getOperationDate(),
                        inventoryBefore.getOperationDate(),
                        currentReport.getLocation(),
                        currentReport.getProductProgram()
                );

                List<ProductOperationFlux> receptionFLuxes = getOperationFluxes(
                        Collections.singletonList(getProductOperationType(OperationConstants.RECEPTION_OPERATION)),
                        inventory.getOperationDate(),
                        inventoryBefore.getOperationDate(),
                        currentReport.getLocation(),
                        currentReport.getProductProgram()
                );

                otherFluxes.add(getAvailableQuantity(productCode, inventory, null));
                otherFluxes.add(getReceivedQuantity(productCode, receptionFLuxes, inventory.getLocation()));
                otherFluxes.add(getDistributedQuantity(productCode, dispensationFLuxes, null, inventory.getLocation()));
                otherFluxes.add(getAdjustmentQuantity(productCode, inventory, currentReport, adjustmentFLuxes));
                otherFluxes.add(createOtherFlux(
                        getProductFluxQuantity(
                                productCode,
                                lossFLuxes.stream().filter(f -> f.getProductCode().equals(productCode)).collect(Collectors.toList())),
                        ReportConstants.LOSS_QUANTITY,
                        productCode,
                        inventory.getLocation())
                );

                if (latestReport != null) {
                    otherFluxes.add(createOtherFlux(
                            getProductOtherFluxQuantity(
                                    productCode,
                                    latestReport.getOtherFluxes().stream().filter(f -> f.getProductCode().equals(productCode)).collect(Collectors.toList()),
                                    ReportConstants.DISTRIBUTED_QUANTITY),
                            ReportConstants.DISTRIBUTED_QUANTITY_M1,
                            productCode,
                            inventory.getLocation())
                    );

                    otherFluxes.add(createOtherFlux(
                            getProductOtherFluxQuantity(
                                    productCode,
                                    latestReport.getOtherFluxes().stream().filter(f -> f.getProductCode().equals(productCode)).collect(Collectors.toList()),
                                    ReportConstants.DISTRIBUTED_QUANTITY_M1),
                            ReportConstants.DISTRIBUTED_QUANTITY_M2,
                            productCode,
                            inventory.getLocation())
                    );
                }
            }
        }

        return otherFluxes;
    }
	
	private ProductOperationOtherFlux createOtherFlux(Double quantity, String label, ProductCode productCode,
	        Location location) throws HibernateException {
		return new ProductOperationOtherFlux(productCode, label, quantity, location);
	}
	
	private Double getProductFluxQuantity(ProductCode productCode, List<ProductOperationFlux> fluxes)
	        throws HibernateException {
		Double quantity = 0.;
		for (ProductOperationFlux flux : fluxes) {
			if (flux.getProductCode().equals(productCode))
				quantity += flux.getQuantity();
		}
		
		return quantity;
	}
	
	private Double getProductOtherFluxQuantity(ProductCode productCode, List<ProductOperationOtherFlux> fluxes, String label)
	        throws HibernateException {
		Double quantity = 0.;
		for (ProductOperationOtherFlux flux : fluxes) {
			if (flux.getProductCode().equals(productCode) && Objects.equals(label, flux.getLabel()))
				quantity += flux.getQuantity();
		}
		
		return quantity;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperationFlux> getOperationFluxes(List<ProductOperationType> operationTypes, Date startDate,
	        Date endDate, Location location, ProductProgram program) throws HibernateException {
		Criteria criteria = getSession().createCriteria(ProductOperationFlux.class, "f").createAlias("f.operation", "o")
		        .add(Restrictions.in("o.operationType", operationTypes)).add(Restrictions.eq("o.productProgram", program))
		        .add(Restrictions.eq("o.location", location))
		        .add(Restrictions.between("o.operationDate", startDate, endDate))
		        .add(Restrictions.eq("o.operationStatus", OperationStatus.VALIDATED))
		        .add(Restrictions.eq("o.voided", false));
		
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperationFlux> getOperationFluxes(List<ProductOperationType> operationTypes, Date startDate,
	        Date endDate, Location location, ProductCode productCode) throws HibernateException {
		Criteria criteria = getSession().createCriteria(ProductOperationFlux.class, "f").createAlias("f.operation", "o")
		        .add(Restrictions.in("o.operationType", operationTypes)).add(Restrictions.eq("f.productCode", productCode))
		        .add(Restrictions.eq("o.location", location))
		        .add(Restrictions.between("o.operationDate", startDate, endDate))
		        .add(Restrictions.eq("o.operationStatus", OperationStatus.VALIDATED))
		        .add(Restrictions.eq("o.voided", false));
		
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductOperationFlux> getOperationChildrenLocationFluxes(List<ProductOperationType> operationTypes,
	        Date startDate, Date endDate, Location location, ProductProgram program) throws HibernateException {
		Criteria criteria = getSession()
		        .createCriteria(ProductOperationFlux.class, "f")
		        .createAlias("f.operation", "o")
		        .createAlias("f.location", "l")
		        .add(Restrictions.in("o.operationType", operationTypes))
		        .add(Restrictions.eq("o.productProgram", program))
		        .add(Restrictions.eq("l.parentLocation", location))
		        .add(Restrictions.between("o.operationDate", startDate, endDate))
		        .add(
		            Restrictions.or(Restrictions.eq("o.operationStatus", OperationStatus.VALIDATED),
		                Restrictions.eq("o.operationStatus", OperationStatus.APPROVED)))
		        .add(Restrictions.eq("o.voided", false));
		
		return criteria.list();
	}
	
	private List<Location> getChildLocationListWithPrograms(Location location) {
        List<Location> childLocationList = new ArrayList<>();
        if (location.getChildLocations().isEmpty()) {
            return Collections.emptyList();
        }
        for (Location childLocation : location.getChildLocations()) {
            if (childLocation.getAttributes()
                    .stream().anyMatch(a -> a.getAttributeType().getUuid().equals("AVAILPRGRMCCCCCCCCCCCCCCCCCCCCCCCCCC") && a.getValueReference() != null)) {
                childLocationList.add(childLocation);
            }
        }

        return childLocationList;
    }
	
	@SuppressWarnings("unchecked")
	private List<ProductOperationOtherFlux> getOperationOtherFluxes(List<ProductOperationType> operationTypes,
	        String operationNumber, Location location, ProductProgram program, String label, Boolean childrenFluxes,
	        Integer count) {
		Criteria criteria = getSession()
		        .createCriteria(ProductOperationOtherFlux.class, "f")
		        .createAlias("f.operation", "o")
		        .add(Restrictions.eq("f.label", label))
		        .add(Restrictions.in("o.operationType", operationTypes))
		        .add(Restrictions.eq("o.productProgram", program))
		        .add(Restrictions.eq("o.operationNumber", operationNumber))
		        .add(
		            Restrictions.or(Restrictions.eq("o.operationStatus", OperationStatus.VALIDATED),
		                Restrictions.eq("o.operationStatus", OperationStatus.APPROVED)))
		        .add(Restrictions.eq("o.voided", false)).addOrder(Order.desc("o.operationDate"));
		if (childrenFluxes) {
			criteria.add(Restrictions.in("o.location", getChildLocationListWithPrograms(location)));
		} else {
			criteria.add(Restrictions.eq("o.location", location));
		}
		if (count != -1) {
			criteria.setMaxResults(count);
		}
		
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	private List<ProductOperationOtherFlux> getOperationOtherFluxes(List<ProductOperationType> operationTypes,
	        Location location, ProductProgram program, String label, Date currentReportDate, Integer count) {
		return getSession()
		        .createCriteria(ProductOperationOtherFlux.class, "f")
		        .createAlias("f.operation", "o")
		        .add(Restrictions.in("o.operationType", operationTypes))
		        .add(Restrictions.eq("o.productProgram", program))
		        .add(
		            Restrictions.or(Restrictions.eq("o.operationStatus", OperationStatus.VALIDATED),
		                Restrictions.eq("o.operationStatus", OperationStatus.APPROVED)))
		        .add(Restrictions.eq("o.voided", false)).add(Restrictions.eq("o.location", location))
		        .add(Restrictions.eq("f.label", label)).add(Restrictions.lt("o.operationDate", currentReportDate))
		        .addOrder(Order.desc("o.operationDate")).setMaxResults(count).list();
	}
	
	private ProductOperationOtherFlux getDistributedQuantity(
            ProductCode productCode,
            List<ProductOperationFlux> dispensationFLuxes,
            List<ProductOperationOtherFlux> childrenUsedFluxes,
            Location location) {
        Double quantity = 0.;

        if (dispensationFLuxes != null && !dispensationFLuxes.isEmpty()) {
            quantity = getProductFluxQuantity(
                    productCode,
                    dispensationFLuxes.stream().filter(f -> f.getProductCode().equals(productCode)).collect(Collectors.toList()));
        }

        if (childrenUsedFluxes != null && !childrenUsedFluxes.isEmpty()) {
            quantity += getProductOtherFluxQuantity(
                    productCode,
                    childrenUsedFluxes.stream().filter(f -> f.getProductCode().equals(productCode) &&
                            !f.getLocation().equals(location)).collect(Collectors.toList()),
                    ReportConstants.DISTRIBUTED_QUANTITY
            );
        }

        return new ProductOperationOtherFlux(productCode, ReportConstants.DISTRIBUTED_QUANTITY, quantity, location);
    }
	
	private ProductOperationOtherFlux getAvailableQuantity(
            ProductCode productCode,
            ProductOperation inventory,
            List<ProductOperationOtherFlux> childrenAvailableFluxes) {

        Double quantity = getProductFluxQuantity(
                productCode,
                inventory.getFluxes().stream().filter(f -> f.getProductCode().equals(productCode)).collect(Collectors.toList()));

        if (childrenAvailableFluxes != null && !childrenAvailableFluxes.isEmpty()) {
            quantity += getProductOtherFluxQuantity(
                    productCode,
                    childrenAvailableFluxes.stream().filter(f -> f.getProductCode().equals(productCode) &&
                            !f.getLocation().equals(inventory.getLocation())).collect(Collectors.toList()),
                    ReportConstants.AVAILABLE_QUANTITY
            );
        }

        return new ProductOperationOtherFlux(productCode, ReportConstants.AVAILABLE_QUANTITY, quantity, inventory.getLocation());
    }
	
	private ProductOperationOtherFlux getAdjustmentQuantity(ProductCode productCode, ProductOperation inventory, ProductOperation currentReport, List<ProductOperationFlux> adjustmentFluxes) {
        Double positiveQuantity = 0.;
        Double negativeQuantity = 0.;

        for (ProductOperation inventoryAdjustment : inventory.getChildrenOperation().stream().filter(f -> f.getOperationType().getUuid()
                .equals(OperationConstants.INVENTORY_POSITIVE_ADJUST_OPERATION)).collect(Collectors.toList())) {
            negativeQuantity = getProductFluxQuantity(
                    productCode,
                    inventoryAdjustment.getFluxes().stream().filter(f -> f.getProductCode().equals(productCode)).collect(Collectors.toList()));
        }

        for (ProductOperation inventoryAdjustment : inventory.getChildrenOperation().stream().filter(f -> f.getOperationType().getUuid()
                .equals(OperationConstants.INVENTORY_NEGATIVE_ADJUST_OPERATION)).collect(Collectors.toList())) {
            positiveQuantity = -getProductFluxQuantity(
                    productCode,
                    inventoryAdjustment.getFluxes().stream().filter(f -> f.getProductCode().equals(productCode)).collect(Collectors.toList()));
        }

        if (adjustmentFluxes != null && !adjustmentFluxes.isEmpty()) {
            positiveQuantity += getProductFluxQuantity(
                    productCode,
                    adjustmentFluxes.stream()
                            .filter(f -> f.getProductCode().equals(productCode) && f.getOperation().getIncidence().equals(Incidence.POSITIVE))
                            .collect(Collectors.toList()));
            negativeQuantity +=
                    getProductFluxQuantity(
                            productCode,
                            adjustmentFluxes.stream()
                                    .filter(f -> f.getProductCode().equals(productCode) && f.getOperation().getIncidence().equals(Incidence.NEGATIVE))
                                    .collect(Collectors.toList()));
        }

        Double adjustmentQuantity = positiveQuantity - negativeQuantity;

        if (!getChildLocationListWithPrograms(inventory.getLocation()).isEmpty()) {
            for (Location location : getChildLocationListWithPrograms(currentReport.getLocation())) {
                ProductOperation childReport = getProductOperationByOperationNumber(
                        getProductOperationType(OperationConstants.REPORT_OPERATION),
                        currentReport.getProductProgram(),
                        currentReport.getOperationNumber(),
                        location, true);
                if (childReport != null) {
                    ProductOperationOtherFlux otherFlux = childReport.getOtherFluxes().stream()
                            .filter(o -> o.getProductCode().equals(productCode) && o.getLabel().equals(ReportConstants.ADJUSTMENT_QUANTITY)).findFirst().orElse(null);
                    if (otherFlux != null) {
                        adjustmentQuantity += otherFlux.getQuantity();
                    }
                }
            }
        }

        return new ProductOperationOtherFlux(productCode, ReportConstants.ADJUSTMENT_QUANTITY, adjustmentQuantity, inventory.getLocation());
    }
	
	private ProductOperationOtherFlux getReceivedQuantity(ProductCode productCode, List<ProductOperationFlux> receptionFLuxes, Location location) {
        Double quantity = 0.;
        if (receptionFLuxes != null) {
            quantity = getProductFluxQuantity(
                    productCode,
                    receptionFLuxes.stream().filter(f -> f.getProductCode().equals(productCode)).collect(Collectors.toList()));
        }
        return new ProductOperationOtherFlux(productCode, ReportConstants.RECEIVED_QUANTITY, quantity, location);
    }
	
	private ProductOperationOtherFlux getAverageMonthlyConsumption(ProductCode productCode, List<ProductOperationOtherFlux> lastMonthDistributed, Location location) {
        List<ProductOperationOtherFlux> productOperationOtherFluxes = lastMonthDistributed.stream().filter(f -> f.getProductCode().equals(productCode)).collect(Collectors.toList());
        Integer months = productOperationOtherFluxes.size();
        Double quantity = 0.;

        for (ProductOperationOtherFlux otherFlux : productOperationOtherFluxes) {
            quantity += otherFlux.getQuantity();
        }
        Double average = (double) Math.round(quantity / months);
        return new ProductOperationOtherFlux(productCode, ReportConstants.PROPOSED_MONTHLY_CONSUMPTION, average, location);
    }
	
	@SuppressWarnings("unchecked")
	public List<ProductNotification> getAllProductNotification(Boolean includeRead, Boolean includeClosed) {
		Criteria criteria = getSession().createCriteria(ProductNotification.class).add(
		    Restrictions.eq("notifiedTo", SupplyUtils.getUserLocation()));
		if (!includeRead) {
			criteria.add(Restrictions.eq("notificationRead", false));
		}
		if (!includeClosed) {
			criteria.add(Restrictions.eq("notificationClosed", false));
		}
		
		return criteria.list();
	}
	
	public ProductNotification saveNotification(ProductNotification notification) {
		getSession().saveOrUpdate(notification);
		return notification;
	}
	
	public ProductNotification getNotification(String uuid) {
		return (ProductNotification) getSession().createCriteria(ProductNotification.class)
		        .add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductNotification> getAllTransferNotification(Boolean includeRead, Boolean includeClosed) {
		Criteria criteria = getSession().createCriteria(ProductNotification.class)
		        .add(Restrictions.eq("notifiedTo", SupplyUtils.getUserLocation()))
		        .add(Restrictions.eq("notification", OperationConstants.TRANSFER_OUT_NOTIFICATION));
		if (!includeRead) {
			criteria.add(Restrictions.eq("notificationRead", false));
		}
		if (!includeClosed) {
			criteria.add(Restrictions.eq("notificationClosed", false));
		}
		
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductNotification> getAllProductReturnNotification(Boolean includeRead, Boolean includeClosed) {
		Criteria criteria = getSession().createCriteria(ProductNotification.class)
		        .add(Restrictions.eq("notifiedTo", SupplyUtils.getUserLocation()))
		        .add(Restrictions.eq("notification", OperationConstants.PRODUCT_RETURN_IN_NOTIFICATION));
		if (!includeRead) {
			criteria.add(Restrictions.eq("notificationRead", false));
		}
		if (!includeClosed) {
			criteria.add(Restrictions.eq("notificationClosed", false));
		}
		
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductNotification> getAllRuptureNotification(Boolean includeRead, Boolean includeClosed) {
		Criteria criteria = getSession().createCriteria(ProductNotification.class)
		        .add(Restrictions.eq("notifiedTo", SupplyUtils.getUserLocation()))
		        .add(Restrictions.eq("notification", OperationConstants.PRODUCT_RUPTURE_NOTIFICATION));
		if (!includeRead) {
			criteria.add(Restrictions.eq("notificationRead", false));
		}
		if (!includeClosed) {
			criteria.add(Restrictions.eq("notificationClosed", false));
		}
		
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductNotification> getAllReceptionNotification(boolean includeRead, boolean includeClosed) {
		Criteria criteria = getSession().createCriteria(ProductNotification.class)
		        .add(Restrictions.eq("notifiedTo", SupplyUtils.getUserLocation()))
		        .add(Restrictions.eq("notification", OperationConstants.REPORT_TREATMENT_NOTIFICATION));
		if (!includeRead) {
			criteria.add(Restrictions.eq("notificationRead", false));
		}
		if (!includeClosed) {
			criteria.add(Restrictions.eq("notificationClosed", false));
		}
		
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductNotification> getAllRejectReportNotification(boolean includeRead, boolean includeClosed) {
		Criteria criteria = getSession().createCriteria(ProductNotification.class)
		        .add(Restrictions.eq("notifiedTo", SupplyUtils.getUserLocation()))
		        .add(Restrictions.eq("notification", OperationConstants.REPORT_REJECTED_NOTIFICATION));
		if (!includeRead) {
			criteria.add(Restrictions.eq("notificationRead", false));
		}
		if (!includeClosed) {
			criteria.add(Restrictions.eq("notificationClosed", false));
		}
		
		return criteria.list();
	}
	
	public Double getMonthlyConsumption(ProductCode productCode, Location location, List<Location> locations) {
		ProductOperation firstOperation = getFirstProductOperation(Arrays.asList(
		    getProductOperationType(OperationConstants.RECEPTION_OPERATION),
		    getProductOperationType(OperationConstants.TRANSFER_IN_OPERATION),
		    getProductOperationType(OperationConstants.INVENTORY_OPERATION)), location, productCode);
		
		Double quantity = 0.;
		
		if (firstOperation != null) {
			Integer divider = SupplyUtils.getDateDiffInMonth(firstOperation.getOperationDate(), new Date());
			if (divider > SupplyUtils.getMonthsForCMM(location)) {
				divider = SupplyUtils.getMonthsForCMM(location);
			}
			
			LocalDate startDate = new LocalDate().minusMonths(SupplyUtils.getMonthsForCMM(location));
			
			List<ProductOperationFlux> fluxes = getOperationFluxes(
			    Collections.singletonList(getProductOperationType(OperationConstants.DISPENSATION_OPERATION)),
			    startDate.toDate(), new Date(), location, productCode);
			if (fluxes != null && !fluxes.isEmpty()) {
				for (ProductOperationFlux flux : fluxes) {
					quantity += flux.getQuantity();
				}
			}
			if (!locations.isEmpty()) {
				
			}
			return quantity / divider;
		}
		return quantity;
	}
	
	public ProductOperation getFirstProductOperation(List<ProductOperationType> operationTypes, Location location,
	        ProductCode productCode) {
		Criteria criteria = getSession()
		        .createCriteria(ProductOperation.class, "o")
		        .createAlias("o.fluxes", "f")
		        .add(Restrictions.in("o.operationType", operationTypes))
		        .add(Restrictions.eq("f.productCode", productCode))
		        .add(Restrictions.eq("o.location", location))
		        .add(Restrictions.eq("o.voided", false))
		        .add(
		            Restrictions.or(Restrictions.eq("o.operationStatus", OperationStatus.VALIDATED),
		                Restrictions.eq("o.operationStatus", OperationStatus.APPROVED),
		                Restrictions.eq("o.operationStatus", OperationStatus.TREATED),
		                Restrictions.eq("o.operationStatus", OperationStatus.SUBMITTED)));
		
		return (ProductOperation) criteria.addOrder(Order.asc("o.operationDate")).setMaxResults(1).uniqueResult();
	}
	
	public ProductStockStatus getProductStockStatus(String uuid) {
		Criteria criteria = getSession().createCriteria(ProductStockStatus.class);
		return (ProductStockStatus) criteria.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	public ProductStockStatus saveProductStockStatus(ProductStockStatus productStockStatus) {
		getSession().saveOrUpdate(productStockStatus);
		return productStockStatus;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductStockStatus> getAllProductStockStatuses(Location location, ProductProgram program) {
		Criteria criteria = getSession().createCriteria(ProductStockStatus.class, "s").createAlias("s.productCode", "p")
		        .add(Restrictions.eq("p.program", program));
		return criteria.add(Restrictions.eq("s.location", location)).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductStockStatus> getAllProductStockStatuses(Location location) {
		Criteria criteria = getSession().createCriteria(ProductStockStatus.class);
		return criteria.add(Restrictions.eq("location", location)).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductStockStatus> getAllProductStockStatuses(Location location, ProductProgram program, Date startDate,
	        Boolean forChildren) {
		Criteria criteria = getSession().createCriteria(ProductStockStatus.class, "s").createAlias("s.productCode", "p")
		        .add(Restrictions.eq("p.program", program));
		if (forChildren) {
			criteria.add(Restrictions.in("s.location", getChildLocationListWithPrograms(location)));
		} else {
			criteria.add(Restrictions.eq("s.location", location));
		}
		return criteria.add(Restrictions.ge("s.stockDate", startDate)).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductStockStatus> getProductStockStatusByProductCode(ProductCode productCode, ProductProgram program,
	        Location location, Date startDate, Boolean forChildren) {
		Criteria criteria = getSession().createCriteria(ProductStockStatus.class, "s").createAlias("s.productCode", "p");
		if (forChildren) {
			criteria.add(Restrictions.in("s.location", getChildLocationListWithPrograms(location)));
		} else {
			criteria.add(Restrictions.eq("s.location", location));
		}
		return criteria.add(Restrictions.eq("s.productCode", productCode)).add(Restrictions.eq("p.program", program))
		        .add(Restrictions.ge("s.stockDate", startDate)).list();
	}
}
