package org.openmrs.module.supply.api.impl;

import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.supply.*;
import org.openmrs.module.supply.api.ProductOperationService;
import org.openmrs.module.supply.api.dao.ProductOperationDao;

import java.util.Date;
import java.util.List;

public class ProductOperationServiceImpl extends BaseOpenmrsService implements ProductOperationService {
	
	ProductOperationDao dao;
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setDao(ProductOperationDao dao) {
		this.dao = dao;
	}
	
	@Override
	public Boolean validateOperation(ProductOperation operation) throws APIException {
		return dao.validateOperation(operation);
	}
	
	@Override
	public Boolean cancelOperation(ProductOperation operation) throws APIException {
		return dao.cancelOperation(operation);
	}
	
	@Override
	public ProductOperation getProductOperation(Integer productOperationId) throws APIException {
		return dao.getProductOperation(productOperationId);
	}
	
	@Override
	public ProductOperation getProductOperation(String uuid) throws APIException {
		return dao.getProductOperation(uuid);
	}
	
	@Override
	public List<ProductOperation> getAllProductOperation(ProductOperationType operationType, Location location,
	        Boolean validatedOnly, Boolean includeVoided) throws APIException {
		return dao.getAllProductOperation(operationType, location, validatedOnly);
	}
	
	@Override
	public List<ProductOperation> getAllProductOperation(ProductOperationType operationType, ProductProgram productProgram,
	        Location location, Boolean validatedOnly, Boolean includeVoided) throws APIException {
		return dao.getAllProductOperation(operationType, productProgram, location, validatedOnly, includeVoided);
	}
	
	@Override
	public List<ProductOperation> getAllProductOperation(ProductOperationType operationType, Date startDate, Date endDate,
	        Location location, Boolean validatedOnly, Boolean includeVoided) throws APIException {
		return dao.getAllProductOperation(operationType, startDate, endDate, location, validatedOnly, includeVoided);
	}
	
	@Override
	public List<ProductOperation> getAllProductOperation(Location location, Boolean includeVoided) throws APIException {
		return dao.getAllProductOperation(location, includeVoided);
	}
	
	@Override
	public ProductOperation getLastProductOperation(ProductOperationType operationType, ProductProgram program,
	        Location location, Boolean validated, Boolean includeVoided) throws APIException {
		return dao.getLastProductOperation(operationType, program, location, validated, includeVoided);
	}
	
	@Override
	public ProductOperation getLastProductOperation(ProductOperationType operationType, ProductProgram program,
	        Date limitEndDate, Location location, Boolean validated, Boolean includeVoided) throws APIException {
		return dao.getLastProductOperation(operationType, program, limitEndDate, location, validated, includeVoided);
	}
	
	@Override
	public ProductOperation getProductOperationByOperationNumber(ProductOperationType operationType, String operationNumber,
	        Location location, Boolean validated) throws APIException {
		return dao.getProductOperationByOperationNumber(operationType, operationNumber, location, validated);
	}
	
	@Override
	public List<ProductOperation> getProductOperationByOperationNumber(String operationNumber, Location location,
	        Boolean validated) {
		return dao.getProductOperationByOperationNumber(operationNumber, location, validated);
	}
	
	@Override
	public ProductOperation saveProductOperation(ProductOperation productOperation) throws APIException {
		return dao.saveProductOperation(productOperation);
	}
	
	@Override
	public void purgeProductOperation(ProductOperation productOperation) throws APIException {
		dao.purgeProductOperation(productOperation);
	}
	
	@Override
	public List<ProductOperationType> getAllProductOperationType() throws APIException {
		return dao.getAllProductOperationType();
	}
	
	@Override
	public void purgeProductOperationType(ProductOperationType operationType) throws APIException {
		dao.purgeProductOperationType(operationType);
	}
	
	@Override
	public ProductOperationType saveProductOperationType(ProductOperationType operationType) throws APIException {
		return dao.saveProductOperationType(operationType);
	}
	
	@Override
	public ProductOperationType getProductOperationType(Integer id) throws APIException {
		return dao.getProductOperationType(id);
	}
	
	@Override
	public ProductOperationType getProductOperationType(String uuid) throws APIException {
		return dao.getProductOperationType(uuid);
	}
	
	@Override
	public List<ProductOperationAttributeType> getAllOperationAttributeType() throws APIException {
		return dao.getAllOperationAttributeType();
	}
	
	@Override
	public void purgeOperationAttributeType(ProductOperationAttributeType operationAttributeType) throws APIException {
		dao.purgeOperationAttributeType(operationAttributeType);
	}
	
	@Override
	public ProductOperationAttributeType saveOperationAttributeType(ProductOperationAttributeType operationAttributeType)
	        throws APIException {
		return dao.saveOperationAttributeType(operationAttributeType);
	}
	
	@Override
	public ProductOperationAttributeType getOperationAttributeType(Integer id) throws APIException {
		return dao.getOperationAttributeType(id);
	}
	
	@Override
	public ProductOperationAttributeType getOperationAttributeType(String uuid) throws APIException {
		return dao.getOperationAttributeType(uuid);
	}
	
	@Override
	public ProductOperationAttribute saveOperationAttribute(ProductOperationAttribute attribute) throws APIException {
		return dao.saveOperationAttribute(attribute);
	}
	
	@Override
	public ProductOperationAttribute getOperationAttribute(Integer id) throws APIException {
		return dao.getOperationAttribute(id);
	}
	
	@Override
	public ProductOperationAttribute getOperationAttribute(String uuid) throws APIException {
		return dao.getOperationAttribute(uuid);
	}
	
	@Override
	public List<ProductOperationFlux> getAllProductOperationFluxes(Location location, Boolean includeVoided)
	        throws APIException {
		return dao.getAllProductOperationFluxes(location, includeVoided);
	}
	
	@Override
	public List<ProductOperationFlux> getAllProductOperationFluxes(Location location, Date startDate, Date endDate,
	        Boolean includeVoided) throws APIException {
		return dao.getAllProductOperationFluxes(location, startDate, endDate, includeVoided);
	}
	
	@Override
	public List<ProductOperationFlux> getAllProductOperationFluxByOperation(ProductOperation productOperation,
	        Boolean includeVoided) throws APIException {
		return dao.getAllProductOperationFluxByOperation(productOperation, includeVoided);
	}
	
	@Override
	public ProductOperationFlux getProductOperationFlux(Integer id) throws APIException {
		return dao.getProductOperationFlux(id);
	}
	
	@Override
	public ProductOperationFlux getProductOperationFlux(String uuid) throws APIException {
		return dao.getProductOperationFlux(uuid);
	}
	
	@Override
	public ProductOperationFlux getProductOperationFluxByProductAndOperation(Product product,
	        ProductOperation productOperation) throws APIException {
		return dao.getProductOperationFluxByProductAndOperation(product, productOperation);
	}
	
	@Override
	public ProductOperationFlux saveProductOperationFlux(ProductOperationFlux productOperationFlux) throws APIException {
		return dao.saveProductOperationFlux(productOperationFlux);
	}
	
	@Override
	public void purgeProductOperationFlux(ProductOperationFlux productOperationFlux) throws APIException {
		dao.purgeProductOperationFlux(productOperationFlux);
	}
	
	@Override
	public List<ProductOperationOtherFlux> getAllProductOperationOtherFluxes(Location location) throws APIException {
		return dao.getAllProductOperationOtherFluxes(location);
	}
	
	@Override
	public ProductOperationOtherFlux getProductOperationOtherFlux(String uuid) throws APIException {
		return dao.getProductOperationOtherFlux(uuid);
	}
	
	@Override
	public ProductOperationOtherFlux getProductOperationOtherFluxByAttributeAndOperation(ProductAttribute productAttribute,
	        ProductOperation productOperation, Location location) throws APIException {
		return dao.getProductOperationOtherFluxByAttributeAndOperation(productAttribute, productOperation, location);
	}
	
	@Override
	public List<ProductOperationOtherFlux> getAllProductOperationOtherFluxByOperation(ProductOperation operation, Boolean b)
	        throws APIException {
		return dao.getAllProductOperationOtherFluxByOperation(operation, b);
	}
	
	@Override
	public ProductOperationOtherFlux getProductOperationOtherFlux(Integer id) throws APIException {
		return dao.getProductOperationOtherFlux(id);
	}
	
	@Override
	public ProductOperationOtherFlux saveProductOperationOtherFlux(ProductOperationOtherFlux productOperationOtherFlux)
	        throws APIException {
		return dao.saveProductOperationOtherFlux(productOperationOtherFlux);
	}
	
	@Override
	public void purgeProductOperationOtherFlux(ProductOperationOtherFlux productOperationOtherFlux) throws APIException {
		dao.purgeProductOperationOtherFlux(productOperationOtherFlux);
	}
	
	@Override
	public List<ProductOperationFlux> getAllProductOperationFluxByOperationAndProduct(ProductOperation operation,
	        Product product) throws APIException {
		return dao.getAllProductOperationFluxByOperationAndProduct(operation, product);
	}
	
	@Override
	public Integer getAllProductOperationFluxByOperationAndProductCount(ProductOperation operation, Product product)
	        throws APIException {
		return dao.getAllProductOperationFluxByOperationAndProductCount(operation, product);
	}
	
	@Override
	public List<ProductOperationOtherFlux> getAllProductOperationOtherFluxByOperationAndProduct(ProductOperation operation,
	        Product product, Location location) throws APIException {
		return dao.getAllProductOperationOtherFluxByOperationAndProduct(operation, product, location);
	}
	
	@Override
	public Integer getAllProductOperationOtherFluxByOperationAndProductCount(ProductOperation operation, Product product)
	        throws APIException {
		return dao.getAllProductOperationOtherFluxByOperationAndProductCount(operation, product);
	}
	
	@Override
	public ProductOperationOtherFlux getProductOperationOtherFluxByProductAndOperation(Product product,
	        ProductOperation productOperation) throws APIException {
		return dao.getProductOperationOtherFluxByProductAndOperation(product, productOperation);
	}
	
	@Override
	public ProductOperationOtherFlux getProductOperationOtherFluxByProductAndOperationAndLabel(Product product,
	        ProductOperation operation, String label, Location location) throws APIException {
		return dao.getProductOperationOtherFluxByProductAndOperationAndLabel(product, operation, label, location);
	}
	
	@Override
	public List<ProductOperationOtherFlux> getAllProductOperationOtherFluxByProductAndOperation(Product product,
	        ProductOperation operation, Location location) throws APIException {
		return dao.getAllProductOperationOtherFluxByProductAndOperation(product, operation, location);
	}
	
	@Override
	public List<ProductAttributeStock> getAllProductAttributeStocks(Location location, Boolean includeVoided)
	        throws APIException {
		return dao.getAllProductAttributeStocks(location, includeVoided);
	}
	
	@Override
	public List<ProductAttributeStock> getAllProductAttributeStocks(Location location, ProductProgram program,
	        Boolean includeVoided) throws APIException {
		return dao.getAllProductAttributeStocks(location, program, includeVoided);
	}
	
	@Override
	public List<ProductAttributeStock> getAllProductAttributeStocks(Boolean includeVoided) throws APIException {
		return dao.getAllProductAttributeStocks(includeVoided);
	}
	
	@Override
	public List<ProductAttributeStock> getAllProductAttributeStockByAttribute(ProductAttribute productAttribute,
	        Boolean includeVoided) throws APIException {
		return null;
	}
	
	@Override
	public ProductAttributeStock getProductAttributeStockByAttribute(ProductAttribute productAttribute, Location location,
	        Boolean includeVoided) throws APIException {
		return dao.getAllProductAttributeStockByAttribute(productAttribute, location, includeVoided);
	}
	
	@Override
	public ProductAttributeStock getProductAttributeStock(Integer id) throws APIException {
		return dao.getProductAttributeStock(id);
	}
	
	@Override
	public ProductAttributeStock getProductAttributeStock(String uuid) throws APIException {
		return dao.getProductAttributeStock(uuid);
	}
	
	@Override
	public ProductAttributeStock saveProductAttributeStock(ProductAttributeStock productAttributeStock) throws APIException {
		return dao.saveProductAttributeStock(productAttributeStock);
	}
	
	@Override
	public void purgeProductAttributeStock(ProductAttributeStock productAttributeStock) throws APIException {
		dao.purgeProductAttributeStock(productAttributeStock);
	}
	
	@Override
	public List<ProductAttributeStock> getProductAttributeStocksByProduct(Product product, ProductProgram program,
	        Location userLocation) throws APIException {
		return dao.getProductAttributeStocksByProduct(product, program, userLocation);
	}
	
	@Override
	public Integer getProductQuantityInStock(Product product, ProductProgram productProgram) throws APIException {
		return dao.getProductQuantityInStock(product, productProgram);
	}
	
	@Override
	public Integer getProductQuantityInStock(Product product, ProductProgram productProgram, Location location)
	        throws APIException {
		return dao.getProductQuantityInStock(product, productProgram, location);
	}
	
	@Override
	public List<ProductAttributeStock> getAllProductAttributeStockByProduct(Product product, ProductProgram productProgram,
	        Location location) throws APIException {
		return dao.getAllProductAttributeStockByProduct(product, productProgram, location);
	}
	
	@Override
	public Integer getAllProductAttributeStockByProductCount(Product product, ProductProgram productProgram,
	        Location location, Boolean includeChildren) throws APIException {
		return dao.getAllProductAttributeStockByProductCount(product, productProgram, location, includeChildren);
	}
	
	@Override
	public List<ProductAttributeStock> getAllProductAttributeStocks(Location location, ProductProgram program,
	        Product product) {
		return dao.getAllProductAttributeStocks(location, program, product);
	}
	
	@Override
	public List<ProductOperationFluxAttribute> getAllProductOperationFluxAttributes(Location userLocation,
	        Boolean includeVoided) {
		return dao.getAllProductOperationFluxAttributes(userLocation, includeVoided);
	}
	
	@Override
	public void purgeProductOperationFluxAttribute(ProductOperationFluxAttribute productOperationFluxAttribute) {
		dao.purgeProductOperationFluxAttribute(productOperationFluxAttribute);
	}
	
	@Override
	public ProductOperationFluxAttribute saveOperationFluxAttribute(
	        ProductOperationFluxAttribute productOperationFluxAttribute) {
		return dao.saveOperationFluxAttribute(productOperationFluxAttribute);
	}
	
	@Override
	public ProductOperationFluxAttribute getOperationFluxAttribute(String uuid) {
		return dao.getOperationFluxAttribute(uuid);
	}
	
	@Override
	public ProductOperationFluxAttribute getOperationFluxAttribute(Integer id) {
		return dao.getOperationFluxAttribute(id);
	}
	
	@Override
	public ProductDispensation getProductDispensation(String uuid) {
		return dao.getProductDispensation(uuid);
	}
	
	@Override
	public ProductDispensation saveProductDispensation(ProductDispensation dispensation) {
		return dao.saveProductDispensation(dispensation);
	}
	
	@Override
	public List<ProductDispensation> getAllProductDispensation(Location location, Boolean includeVoided) {
		return dao.getAllProductDispensation(location, includeVoided);
	}
	
	@Override
	public List<ProductDispensation> getAllProductDispensation(String operationNumber, Location location,
	        Boolean includeVoided) {
		return dao.getAllProductDispensation(operationNumber, location, includeVoided);
	}
	
}
