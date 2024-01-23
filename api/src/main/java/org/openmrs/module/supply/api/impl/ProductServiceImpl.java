package org.openmrs.module.supply.api.impl;

import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.supply.*;
import org.openmrs.module.supply.api.ProductService;
import org.openmrs.module.supply.api.dao.ProductDao;
//import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ProductServiceImpl extends BaseOpenmrsService implements ProductService {
	
	ProductDao dao;
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setDao(ProductDao dao) {
		this.dao = dao;
	}
	
	@Override
	public Product getProduct(Integer id) {
		return dao.getProduct(id);
	}
	
	@Override
	public Product getProduct(String uuid) {
		return dao.getProduct(uuid);
	}
	
	@Override
	public List<Product> getAllProducts(Boolean includeVoided) {
		return dao.getAllProducts(includeVoided);
	}
	
	@Override
	public Product getProductByCode(String code) {
		return dao.getProductByCode(code);
	}
	
	@Override
	public Product saveProduct(Product product) {
		return dao.saveProduct(product);
	}
	
	@Override
	public void purgeProduct(Product product) {
		dao.purgeProduct(product);
	}
	
	//	@Override
	//	public List<Product> uploadProduct(MultipartFile file) {
	//		return dao.uploadProduct(file);
	//	}
	
	@Override
	public ProductPrice getProductPrice(Integer id) {
		return dao.getProductPrice(id);
	}
	
	@Override
	public ProductPrice getProductPrice(String uuid) {
		return dao.getProductPrice(uuid);
	}
	
	@Override
	public ProductPrice saveProductPrice(ProductPrice productPrice) {
		return dao.saveProductPrice(productPrice);
	}
	
	@Override
	public List<ProductPrice> getAllProductPrices() throws APIException {
		return dao.getAllProductPrices();
	}
	
	@Override
	public void purgeProductPrice(ProductPrice productPrice) {
		dao.purgeProductPrice(productPrice);
	}
	
	/**
	 * ********* Unit
	 */
	
	@Override
	public ProductUnit getProductUnit(Integer id) {
		return dao.getProductUnit(id);
	}
	
	@Override
	public ProductUnit getProductUnit(String uuid) {
		return dao.getProductUnit(uuid);
	}
	
	@Override
	public List<ProductUnit> getAllProductUnits() throws APIException {
		return dao.getAllProductUnits();
	}
	
	@Override
	public ProductUnit getProductUnitByName(String unitName) {
		return dao.getProductUnitByName(unitName);
	}
	
	@Override
	public ProductUnit saveProductUnit(ProductUnit productUnit) {
		return dao.saveProductUnit(productUnit);
	}
	
	@Override
	public ProductName getProductName(Integer id) {
		return dao.getProductName(id);
	}
	
	@Override
	public ProductName getProductName(String uuid) {
		return dao.getProductName(uuid);
	}
	
	@Override
	public ProductName saveProductName(ProductName productName) {
		return dao.saveProductName(productName);
	}
	
	@Override
	public void purgeProductName(ProductName productName) {
		dao.purgeProductName(productName);
	}
	
	@Override
	public ProductProgram getProductProgram(Integer id) throws APIException {
		return dao.getProductProgram(id);
	}
	
	@Override
	public ProductProgram getProductProgram(String uuid) throws APIException {
		return dao.getProductProgram(uuid);
	}
	
	@Override
	public ProductProgram getProductProgramByName(String name) throws APIException {
		return dao.getProductProgramByName(name);
	}
	
	@Override
	public List<ProductProgram> getAllProductPrograms() throws APIException {
		return dao.getAllProductPrograms();
	}
	
	@Override
	public List<ProductProgram> getUserLocationProductPrograms() throws APIException {
		return dao.getUserLocationProductPrograms();
	}
	
	@Override
	public ProductProgram saveProductProgram(ProductProgram program) throws APIException {
		return dao.saveProductProgram(program);
	}
	
	@Override
	public ProductRegime getProductRegime(Integer id) throws APIException {
		return dao.getProductRegime(id);
	}
	
	@Override
	public ProductRegime getProductRegime(String uuid) throws APIException {
		return dao.getProductRegime(uuid);
	}
	
	@Override
	public ProductRegime getProductRegimeByConceptName(String name) throws APIException {
		return dao.getProductRegimeByConceptName(name);
	}
	
	@Override
	public ProductRegime getProductRegimeByConcept(Integer conceptId) throws APIException {
		return dao.getProductRegimeByConcept(conceptId);
	}
	
	@Override
	public ProductRegime getProductRegimeByConcept(String conceptUuid) throws APIException {
		return dao.getProductRegimeByConcept(conceptUuid);
	}
	
	@Override
	public List<ProductRegime> getAllProductRegimes() throws APIException {
		return dao.getAllProductRegimes();
	}
	
	//	@Override
	//	public void uploadProductRegimens(MultipartFile file) {
	//		dao.uploadProductRegimens(file);
	//	}
	
	@Override
	public ProductRegime saveProductRegime(ProductRegime productRegime) throws APIException {
		return dao.saveProductRegime(productRegime);
	}
	
	@Override
	public List<ProductAttribute> getAllProductAttributes(Location location, Boolean includeVoided) {
		return dao.getAllProductAttributes(location, includeVoided);
	}
	
	@Override
	public List<ProductAttribute> getAllProductAttributesByProduct(Product product) {
		return dao.getAllProductAttributesByProduct(product);
	}
	
	@Override
	public ProductAttribute getProductAttribute(Integer id) {
		return dao.getProductAttribute(id);
	}
	
	@Override
	public ProductAttribute getProductAttribute(String uuid) {
		return dao.getProductAttribute(uuid);
	}
	
	@Override
	public ProductAttribute saveProductAttribute(ProductAttribute productAttribute) {
		return dao.saveProductAttribute(productAttribute);
	}
	
	@Override
	public ProductAttribute getProductAttributeByBatchNumber(String batchNumber, Location location) {
		return dao.getProductAttributeByBatchNumber(batchNumber, location);
	}
	
	@Override
	public ProductAttribute getOtherProductAttributeByBatchNumber(String batchNumber, Product product, Location location) {
		return dao.getOtherProductAttributeByBatchNumber(batchNumber, product, location);
	}
	
	@Override
	public void purgeUnusedAttributes(ProductOperationFlux flux) {
		dao.purgeUnusedAttributes(flux);
	}
	
	@Override
	public void purgeUnusedAttributes() {
		dao.purgeUnusedAttributes();
	}
	
	@Override
	public ProductCode getProductCode(String uuid) {
		return dao.getProductCode(uuid);
	}
	
	@Override
	public ProductCode getProductCodeByCode(String code) {
		return dao.getProductCodeByCode(code);
	}
	
	@Override
	public ProductCode saveProductCode(ProductCode productCode) {
		return dao.saveProductCode(productCode);
	}
	
	@Override
	public List<ProductCode> getProductCodes(ProductProgram program, ProductRegime productRegime) {
		return dao.getProductCodes(program, productRegime);
	}
	
	@Override
	public List<ProductCode> getProductCodes(ProductProgram program, Boolean includeVoided) {
		return dao.getProductCodes(program, includeVoided);
	}
	
	@Override
	public List<ProductCode> getProductCodes(Boolean includeVoided) {
		return dao.getProductCodes(includeVoided);
	}
	
	@Override
	public List<ProductCode> getProductWithoutRegimeByProgram(ProductProgram productProgram) {
		return dao.getProductWithoutRegimeByProgram(productProgram);
	}
	
	@Override
	public List<ProductCode> getAvailableProductCode(ProductProgram program) {
		return dao.getAvailableProductCode(program);
	}
	
}
