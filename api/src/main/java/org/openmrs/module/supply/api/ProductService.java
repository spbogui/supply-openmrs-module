package org.openmrs.module.supply.api;

import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.supply.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Transactional
public interface ProductService extends OpenmrsService {
	
	@Transactional(readOnly = true)
	Product getProduct(Integer id) throws APIException;
	
	@Transactional(readOnly = true)
	Product getProduct(String uuid) throws APIException;
	
	@Transactional(readOnly = true)
	List<Product> getAllProducts(Boolean includeVoided) throws APIException;
	
	List<Product> getProductWithoutRegimeByProgram(ProductProgram productProgram);
	
	Product getProductByCode(String code) throws APIException;
	
	Product saveProduct(Product product) throws APIException;
	
	void purgeProduct(Product product) throws APIException;
	
	List<Product> uploadProduct(MultipartFile flie) throws APIException;
	
	/**
	 * *********** Price
	 */
	
	@Transactional(readOnly = true)
	ProductPrice getProductPrice(Integer id) throws APIException;
	
	@Transactional(readOnly = true)
	ProductPrice getProductPrice(String uuid) throws APIException;
	
	@Transactional
	ProductPrice saveProductPrice(ProductPrice productPrice) throws APIException;
	
	List<ProductPrice> getAllProductPrices() throws APIException;
	
	@Transactional
	void purgeProductPrice(ProductPrice productPrice) throws APIException;
	
	/**
	 * *********** Unit
	 */
	
	@Transactional(readOnly = true)
	ProductUnit getProductUnit(Integer id) throws APIException;
	
	@Transactional(readOnly = true)
	ProductUnit getProductUnit(String uuid) throws APIException;
	
	@Transactional(readOnly = true)
	List<ProductUnit> getAllProductUnits() throws APIException;
	
	@Transactional(readOnly = true)
	ProductUnit getProductUnitByName(String unitName) throws APIException;
	
	ProductUnit saveProductUnit(ProductUnit productUnit) throws APIException;
	
	/**
	 * *********** Name
	 */
	
	@Transactional(readOnly = true)
	ProductName getProductName(Integer id) throws APIException;
	
	@Transactional(readOnly = true)
	ProductName getProductName(String uuid) throws APIException;
	
	ProductName saveProductName(ProductName productName) throws APIException;
	
	void purgeProductName(ProductName productName) throws APIException;
	
	/**
	 * *********** Program
	 */
	
	@Transactional(readOnly = true)
	ProductProgram getProductProgram(Integer id) throws APIException;
	
	@Transactional(readOnly = true)
	ProductProgram getProductProgram(String uuid) throws APIException;
	
	@Transactional(readOnly = true)
	ProductProgram getProductProgramByName(String name) throws APIException;
	
	@Transactional(readOnly = true)
	List<ProductProgram> getAllProductPrograms() throws APIException;
	
	@Transactional(readOnly = true)
	List<ProductProgram> getUserLocationProductPrograms() throws APIException;
	
	ProductProgram saveProductProgram(ProductProgram program) throws APIException;
	
	/**
	 * *********** Regime
	 */
	
	ProductRegime saveProductRegime(ProductRegime productRegime) throws APIException;
	
	@Transactional(readOnly = true)
	ProductRegime getProductRegime(Integer id) throws APIException;
	
	@Transactional(readOnly = true)
	ProductRegime getProductRegime(String uuid) throws APIException;
	
	@Transactional(readOnly = true)
	ProductRegime getProductRegimeByConceptName(String name) throws APIException;
	
	@Transactional(readOnly = true)
	ProductRegime getProductRegimeByConcept(Integer conceptId) throws APIException;
	
	@Transactional(readOnly = true)
	ProductRegime getProductRegimeByConcept(String conceptUuid) throws APIException;
	
	@Transactional(readOnly = true)
	List<ProductRegime> getAllProductRegimes() throws APIException;
	
	void uploadProductRegimens(MultipartFile file);
	
	/**
	 * *********** Attribute
	 */
	
	@Transactional(readOnly = true)
	List<ProductAttribute> getAllProductAttributes(Location location, Boolean includeVoided);
	
	@Transactional(readOnly = true)
	List<ProductAttribute> getAllProductAttributesByProduct(Product product);
	
	@Transactional(readOnly = true)
	ProductAttribute getProductAttribute(Integer id);
	
	@Transactional(readOnly = true)
	ProductAttribute getProductAttribute(String uuid);
	
	@Transactional
	ProductAttribute saveProductAttribute(ProductAttribute productAttribute);
	
	@Transactional(readOnly = true)
	ProductAttribute getProductAttributeByBatchNumber(String batchNumber, Location location);
	
	@Transactional(readOnly = true)
	ProductAttribute getOtherProductAttributeByBatchNumber(String batchNumber, Product product, Location location);
	
	@Transactional
	Integer purgeUnusedAttributes();
	
	@Transactional(readOnly = true)
	ProductCode getProductCode(String uuid);
	
	@Transactional(readOnly = true)
	ProductCode getProductCodeByCode(String code);
	
	@Transactional
	ProductCode saveProductCode(ProductCode productCode);
	
	@Transactional(readOnly = true)
	List<ProductCode> getProductCodes(ProductProgram program, ProductRegime productRegime);
	
	@Transactional(readOnly = true)
	List<ProductCode> getProductCodes(ProductProgram program, Boolean includeVoided);
	
	@Transactional(readOnly = true)
	List<ProductCode> getProductCodes(Boolean includeVoided);
}
