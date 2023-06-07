package org.openmrs.module.supply.api.dao;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.supply.*;
import org.openmrs.module.supply.utils.CSVHelper;
import org.openmrs.module.supply.utils.SupplyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository("supply.ProductDao")
public class ProductDao {
	
	@Autowired
	DbSessionFactory sessionFactory;
	
	private DbSession getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	public Product getProduct(Integer id) {
		return (Product) getSession().get(Product.class, id);
	}
	
	public Product getProduct(String uuid) {
		Criteria criteria = getSession().createCriteria(Product.class);
		return (Product) criteria.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<Product> getAllProducts(Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(Product.class);
		if (includeVoided) {
			return (List<Product>) criteria.list();
		}
		return (List<Product>) criteria.add(Restrictions.eq("voided", false)).list();
	}
	
	public Product getProductByCode(String code) {
		Criteria criteria = getSession().createCriteria(Product.class);
		return (Product) criteria.add(Restrictions.eq("code", code)).uniqueResult();
	}
	
	public Product saveProduct(Product product) {
		getSession().saveOrUpdate(product);
		return product;
	}
	
	public void purgeProduct(Product product) {
		getSession().delete(product);
	}
	
	public List<Product> uploadProduct(MultipartFile file) {
		try {
			List<Product> products = CSVHelper.csvProducts(file.getInputStream());
			for (Product product : products) {
				saveProduct(product);
			}
			return products;
		}
		catch (IOException e) {
			throw new RuntimeException("fail to store csv data: " + e.getMessage());
		}
	}
	
	public ProductPrice getProductPrice(Integer id) {
		return (ProductPrice) getSession().get(ProductPrice.class, id);
	}
	
	public ProductPrice getProductPrice(String uuid) {
		Criteria criteria = getSession().createCriteria(ProductPrice.class);
		return (ProductPrice) criteria.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	public ProductPrice saveProductPrice(ProductPrice productPrice) {
		getSession().saveOrUpdate(productPrice);
		return productPrice;
	}
	
	public void purgeProductPrice(ProductPrice productPrice) {
		getSession().delete(productPrice);
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductPrice> getAllProductPrices() {
		return getSession().createCriteria(ProductPrice.class)
		        .add(Restrictions.eq("location", SupplyUtils.getUserLocation())).list();
	}
	
	public ProductUnit getProductUnit(Integer id) {
		return (ProductUnit) getSession().get(ProductUnit.class, id);
	}
	
	public ProductUnit getProductUnit(String uuid) {
		Criteria criteria = getSession().createCriteria(ProductUnit.class);
		return (ProductUnit) criteria.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductUnit> getAllProductUnits() {
		return getSession().createCriteria(ProductUnit.class).list();
	}
	
	public ProductUnit getProductUnitByName(String unitName) {
		Criteria criteria = getSession().createCriteria(ProductUnit.class);
		return (ProductUnit) criteria.add(Restrictions.eq("name", unitName)).uniqueResult();
	}
	
	public ProductUnit saveProductUnit(ProductUnit productUnit) {
		getSession().saveOrUpdate(productUnit);
		return productUnit;
	}
	
	public ProductName getProductName(Integer id) {
		return (ProductName) getSession().get(ProductName.class, id);
	}
	
	public ProductName getProductName(String uuid) {
		Criteria criteria = getSession().createCriteria(ProductName.class);
		return (ProductName) criteria.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	public ProductName saveProductName(ProductName productName) {
		getSession().saveOrUpdate(productName);
		return productName;
	}
	
	public void purgeProductName(ProductName productName) {
		getSession().delete(productName);
	}
	
	public ProductProgram getProductProgram(Integer id) {
		return (ProductProgram) getSession().get(ProductProgram.class, id);
	}
	
	public ProductProgram getProductProgram(String uuid) {
		Criteria criteria = getSession().createCriteria(ProductProgram.class);
		return (ProductProgram) criteria.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	public ProductProgram getProductProgramByName(String name) {
		Criteria criteria = getSession().createCriteria(ProductProgram.class);
		return (ProductProgram) criteria.add(Restrictions.eq("name", name)).uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductProgram> getAllProductPrograms() {
		return getSession().createCriteria(ProductProgram.class).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductProgram> getUserLocationProductPrograms() {
		return null;
	}
	
	public ProductProgram saveProductProgram(ProductProgram program) {
		getSession().saveOrUpdate(program);
		return program;
	}
	
	public ProductRegime getProductRegime(Integer id) {
		return (ProductRegime) getSession().get(ProductRegime.class, id);
	}
	
	public ProductRegime getProductRegime(String uuid) {
		Criteria criteria = getSession().createCriteria(ProductRegime.class);
		return (ProductRegime) criteria.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	public ProductRegime getProductRegimeByConceptName(String name) {
		Concept concept = Context.getConceptService().getConceptByName(name);
		Criteria criteria = getSession().createCriteria(ProductRegime.class);
		return (ProductRegime) criteria.add(Restrictions.eq("concept", concept)).uniqueResult();
	}
	
	public ProductRegime getProductRegimeByConcept(Integer conceptId) {
		Concept concept = Context.getConceptService().getConcept(conceptId);
		Criteria criteria = getSession().createCriteria(ProductRegime.class);
		return (ProductRegime) criteria.add(Restrictions.eq("concept", concept)).uniqueResult();
	}
	
	public ProductRegime getProductRegimeByConcept(String conceptUuid) {
		Concept concept = Context.getConceptService().getConcept(conceptUuid);
		Criteria criteria = getSession().createCriteria(ProductRegime.class);
		return (ProductRegime) criteria.add(Restrictions.eq("concept", concept)).uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductRegime> getAllProductRegimes() {
		return getSession().createCriteria(ProductRegime.class).list();
	}
	
	public ProductRegime saveProductRegime(ProductRegime productRegime) {
		Concept concept = productRegime.getConcept();
		productRegime.setUuid(concept.getUuid());
		getSession().saveOrUpdate(productRegime);
		return productRegime;
	}
	
	public void uploadProductRegimens(MultipartFile file) {
		try {
			List<Product> products = CSVHelper.csvProductRegimes(file.getInputStream());
			for (Product product : products) {
				saveProduct(product);
			}
			
		}
		catch (IOException e) {
			throw new RuntimeException("fail to store csv data: " + e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductAttribute> getAllProductAttributes(Location location, Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductAttribute.class);
		if (includeVoided) {
			return (List<ProductAttribute>) criteria.list();
		}
		return (List<ProductAttribute>) criteria.add(Restrictions.eq("voided", false)).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductAttribute> getAllProductAttributesByProduct(Product product) {
		Criteria criteria = getSession().createCriteria(ProductAttribute.class);
		return criteria.add(Restrictions.eq("product", product)).list();
	}
	
	public ProductAttribute getProductAttribute(Integer id) {
		return (ProductAttribute) getSession().get(ProductAttribute.class, id);
	}
	
	public ProductAttribute getProductAttribute(String uuid) {
		Criteria criteria = getSession().createCriteria(ProductAttribute.class);
		return (ProductAttribute) criteria.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	public ProductAttribute saveProductAttribute(ProductAttribute productAttribute) {
		getSession().saveOrUpdate(productAttribute);
		return productAttribute;
	}
	
	public ProductAttribute getProductAttributeByBatchNumber(String batchNumber, Location location) {
		Criteria criteria = getSession().createCriteria(ProductAttribute.class);
		return (ProductAttribute) criteria.add(Restrictions.eq("batchNumber", batchNumber))
		        .add(Restrictions.eq("location", location)).uniqueResult();
	}
	
	public ProductAttribute getOtherProductAttributeByBatchNumber(String batchNumber, Product product, Location location) {
		Criteria criteria = getSession().createCriteria(ProductAttribute.class);
		return (ProductAttribute) criteria.add(Restrictions.eq("batchNumber", batchNumber))
		        .add(Restrictions.eq("location", location)).add(Restrictions.ne("product", product)).uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public Integer purgeUnusedAttributes() {
		List<ProductAttribute> productAttributes = getSession()
		        .createQuery(
		            "FROM ProductAttribute p WHERE p.productAttributeId NOT IN (SELECT pf.productAttribute.productAttributeId FROM ProductOperationFluxAttribute pf)")
		        .list();
		for (ProductAttribute attribute : productAttributes) {
			getSession().delete(attribute);
		}
		return productAttributes.size();
	}
	
	@SuppressWarnings("unchecked")
    public List<Product> getProductWithoutRegimeByProgram(ProductProgram productProgram) {
        String sqlQuery =
                "SELECT product_id FROM ( " +
                        "SELECT pp.product_id, count(ppr.product_regime_id) countRegimen  FROM supply_product pp " +
                        "LEFT JOIN supply_product_regime_members pprm on pp.product_id = pprm.product_id " +
                        "LEFT JOIN supply_product_regime ppr on ppr.product_regime_id = pprm.regime_id " +
                        "LEFT JOIN supply_product_program_members pppm on pp.product_id = pppm.product_id " +
                        "WHERE program_id = " + productProgram.getProductProgramId() + " " +
                        "GROUP BY pp.product_id " +
                        "HAVING countRegimen = (SELECT COUNT(*) FROM supply_product_regime) OR countRegimen = 0) _";

        Query query = getSession().createSQLQuery(sqlQuery);
        List<Integer> productIds = query.list();

        List<Product> productList = new ArrayList<>();

        if (productIds != null) {
            for (Integer productId : productIds) {
                productList.add(getProduct(productId));
            }
        }
        return productList;
    }
	
	public ProductCode getProductCode(String uuid) {
		return null;
	}
	
	public ProductCode getProductCodeByCode(String code) {
		return null;
	}
	
	public ProductCode saveProductCode(ProductCode productCode) {
		return null;
	}
	
	public List<ProductCode> getProductCodes(ProductProgram program, ProductRegime productRegime) {
		return null;
	}
	
	public List<ProductCode> getProductCodes(ProductProgram program, Boolean includeVoided) {
		return null;
	}
	
	public List<ProductCode> getProductCodes(Boolean includeVoided) {
		return null;
	}
}
