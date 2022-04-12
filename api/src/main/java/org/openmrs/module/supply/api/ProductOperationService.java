package org.openmrs.module.supply.api;

import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.supply.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional
public interface ProductOperationService extends OpenmrsService {
	
	/**
	 * *********** Operation
	 */
	
	Boolean validateOperation(ProductOperation operation) throws APIException;
	
	Boolean cancelOperation(ProductOperation operation) throws APIException;
	
	ProductOperation getProductOperation(Integer productOperationId) throws APIException;
	
	ProductOperation getProductOperation(String uuid) throws APIException;
	
	List<ProductOperation> getAllProductOperation(ProductOperationType operationType, Location location,
	        Boolean validatedOnly, Boolean includeVoided) throws APIException;
	
	List<ProductOperation> getAllProductOperation(ProductOperationType operationType, ProductProgram productProgram,
	        Location location, Boolean validatedOnly, Boolean includeVoided) throws APIException;
	
	List<ProductOperation> getAllProductOperation(ProductOperationType operationType, Date startDate, Date endDate,
	        Location location, Boolean validatedOnly, Boolean includeVoided) throws APIException;
	
	ProductOperation getLastProductOperation(ProductOperationType operationType, ProductProgram program, Location location,
	        Boolean validated, Boolean includeVoided) throws APIException;
	
	ProductOperation getLastProductOperation(ProductOperationType operationType, ProductProgram program, Date limitEndDate,
	        Location location, Boolean validated, Boolean includeVoided) throws APIException;
	
	ProductOperation getProductOperationByOperationNumber(ProductOperationType operationType, String operationNumber,
	        Location location, Boolean validated) throws APIException;
	
	List<ProductOperation> getProductOperationByOperationNumber(String operationNumber, Location location, Boolean validated);
	
	List<ProductOperation> getAllProductOperation(Location location, Boolean includeVoided) throws APIException;
	
	ProductOperation saveProductOperation(ProductOperation productOperation) throws APIException;
	
	void purgeProductOperation(ProductOperation productOperation) throws APIException;
	
	/**
	 * *********** Operation type
	 */
	
	List<ProductOperationType> getAllProductOperationType() throws APIException;
	
	void purgeProductOperationType(ProductOperationType operationType) throws APIException;
	
	ProductOperationType saveProductOperationType(ProductOperationType operationType) throws APIException;
	
	ProductOperationType getProductOperationType(Integer id) throws APIException;
	
	ProductOperationType getProductOperationType(String uuid) throws APIException;
	
	/**
	 * *********** Operation attribute type
	 */
	
	List<ProductOperationAttributeType> getAllOperationAttributeType() throws APIException;
	
	void purgeOperationAttributeType(ProductOperationAttributeType operationAttributeType) throws APIException;
	
	ProductOperationAttributeType saveOperationAttributeType(ProductOperationAttributeType operationAttributeType)
	        throws APIException;
	
	ProductOperationAttributeType getOperationAttributeType(Integer id) throws APIException;
	
	ProductOperationAttributeType getOperationAttributeType(String uuid) throws APIException;
	
	/**
	 * *********** Operation attribute
	 */
	
	ProductOperationAttribute saveOperationAttribute(ProductOperationAttribute attribute) throws APIException;
	
	ProductOperationAttribute getOperationAttribute(Integer id) throws APIException;
	
	ProductOperationAttribute getOperationAttribute(String uuid) throws APIException;
	
	/**
	 * *********** Operation Flux
	 */
	@Transactional(readOnly = true)
	List<ProductOperationFlux> getAllProductOperationFluxes(Location location, Boolean includeVoided) throws APIException;
	
	@Transactional(readOnly = true)
	List<ProductOperationFlux> getAllProductOperationFluxes(Location location, Date startDate, Date endDate,
	        Boolean includeVoided) throws APIException;
	
	@Transactional(readOnly = true)
	List<ProductOperationFlux> getAllProductOperationFluxByOperation(ProductOperation productOperation, Boolean includeVoided)
	        throws APIException;
	
	@Transactional(readOnly = true)
	ProductOperationFlux getProductOperationFlux(Integer id) throws APIException;
	
	@Transactional(readOnly = true)
	ProductOperationFlux getProductOperationFlux(String uuid) throws APIException;
	
	@Transactional(readOnly = true)
	ProductOperationFlux getProductOperationFluxByProductAndOperation(Product product, ProductOperation productOperation)
	        throws APIException;
	
	@Transactional
	ProductOperationFlux saveProductOperationFlux(ProductOperationFlux productOperationFlux) throws APIException;
	
	@Transactional
	void purgeProductOperationFlux(ProductOperationFlux productOperationFlux) throws APIException;
	
	@Transactional(readOnly = true)
	List<ProductOperationFlux> getAllProductOperationFluxByOperationAndProduct(ProductOperation operation, Product product)
	        throws APIException;
	
	@Transactional(readOnly = true)
	Integer getAllProductOperationFluxByOperationAndProductCount(ProductOperation operation, Product product)
	        throws APIException;
	
	/**
	 * *********** Operation other flux
	 */
	
	List<ProductOperationOtherFlux> getAllProductOperationOtherFluxes(Location location) throws APIException;
	
	ProductOperationOtherFlux getProductOperationOtherFlux(String uuid) throws APIException;
	
	ProductOperationOtherFlux getProductOperationOtherFlux(Integer id) throws APIException;
	
	ProductOperationOtherFlux getProductOperationOtherFluxByAttributeAndOperation(ProductAttribute productAttribute,
	        ProductOperation productOperation, Location location) throws APIException;
	
	List<ProductOperationOtherFlux> getAllProductOperationOtherFluxByOperation(ProductOperation operation, Boolean b)
	        throws APIException;
	
	ProductOperationOtherFlux saveProductOperationOtherFlux(ProductOperationOtherFlux productOperationOtherFlux)
	        throws APIException;
	
	void purgeProductOperationOtherFlux(ProductOperationOtherFlux productOperationOtherFlux) throws APIException;
	
	/**
	 * *********** Operation other flux
	 */
	
	List<ProductOperationOtherFlux> getAllProductOperationOtherFluxByOperationAndProduct(ProductOperation operation,
	        Product product, Location location) throws APIException;
	
	Integer getAllProductOperationOtherFluxByOperationAndProductCount(ProductOperation operation, Product product)
	        throws APIException;
	
	ProductOperationOtherFlux getProductOperationOtherFluxByProductAndOperation(Product product,
	        ProductOperation productOperation) throws APIException;
	
	ProductOperationOtherFlux getProductOperationOtherFluxByProductAndOperationAndLabel(Product product,
	        ProductOperation productOperation, String label, Location location) throws APIException;
	
	List<ProductOperationOtherFlux> getAllProductOperationOtherFluxByProductAndOperation(Product product,
	        ProductOperation productOperation, Location location) throws APIException;
	
	/**
	 * *********** Attribute Stock
	 */
	
	List<ProductAttributeStock> getAllProductAttributeStocks(Location location, Boolean includeVoided) throws APIException;
	
	List<ProductAttributeStock> getAllProductAttributeStocks(Location location, ProductProgram program, Boolean includeVoided)
	        throws APIException;
	
	List<ProductAttributeStock> getAllProductAttributeStocks(Boolean includeVoided) throws APIException;
	
	List<ProductAttributeStock> getAllProductAttributeStockByAttribute(ProductAttribute productAttribute,
	        Boolean includeVoided) throws APIException;
	
	ProductAttributeStock getProductAttributeStockByAttribute(ProductAttribute productAttribute, Location location,
	        Boolean includeVoided) throws APIException;
	
	ProductAttributeStock getProductAttributeStock(Integer id) throws APIException;
	
	ProductAttributeStock getProductAttributeStock(String uuid) throws APIException;
	
	ProductAttributeStock saveProductAttributeStock(ProductAttributeStock productAttributeStock) throws APIException;
	
	void purgeProductAttributeStock(ProductAttributeStock productAttributeStock) throws APIException;
	
	List<ProductAttributeStock> getProductAttributeStocksByProduct(Product product, ProductProgram program,
	        Location userLocation) throws APIException;
	
	Integer getProductQuantityInStock(Product product, ProductProgram productProgram) throws APIException;
	
	Integer getProductQuantityInStock(Product product, ProductProgram productProgram, Location location) throws APIException;
	
	List<ProductAttributeStock> getAllProductAttributeStockByProduct(Product product, ProductProgram productProgram,
	        Location location) throws APIException;
	
	Integer getAllProductAttributeStockByProductCount(Product product, ProductProgram productProgram, Location location,
	        Boolean includeChildren) throws APIException;
	
	List<ProductAttributeStock> getAllProductAttributeStocks(Location location, ProductProgram program, Product product);
	
	/**
	 * *********** Operation Flux Attribute
	 */
	@Transactional(readOnly = true)
	List<ProductOperationFluxAttribute> getAllProductOperationFluxAttributes(Location userLocation, Boolean includeVoided);
	
	@Transactional(readOnly = true)
	void purgeProductOperationFluxAttribute(ProductOperationFluxAttribute productOperationFluxAttribute);
	
	@Transactional
	ProductOperationFluxAttribute saveOperationFluxAttribute(ProductOperationFluxAttribute productOperationFluxAttribute);
	
	@Transactional(readOnly = true)
	ProductOperationFluxAttribute getOperationFluxAttribute(String uuid);
	
	@Transactional(readOnly = true)
	ProductOperationFluxAttribute getOperationFluxAttribute(Integer id);
	
	/**
	 * *********** Dispensation
	 */
	ProductDispensation getProductDispensation(String uuid);
	
	ProductDispensation saveProductDispensation(ProductDispensation dispensation);
	
	List<ProductDispensation> getAllProductDispensation(Location location, Boolean includeVoided);
	
	List<ProductDispensation> getAllProductDispensation(String operationNumber, Location location, Boolean includeVoided);
}
