package org.openmrs.module.supply.api.dao;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.CriteriaQuery;
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
//import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
		Criteria criteria = getSession().createCriteria(Product.class, "p").createAlias("p.productCodes", "pc");
		return (Product) criteria.add(Restrictions.eq("pc.code", code)).setMaxResults(1).uniqueResult();
	}
	
	public Product saveProduct(Product product) {
		getSession().saveOrUpdate(product);
		return product;
	}
	
	public void purgeProduct(Product product) {
		getSession().delete(product);
	}
	
	//	public List<Product> uploadProduct(MultipartFile file) {
	//		try {
	//			List<Product> products = CSVHelper.csvProducts(file.getInputStream());
	//			for (Product product : products) {
	//				saveProduct(product);
	//			}
	//			return products;
	//		}
	//		catch (IOException e) {
	//			throw new RuntimeException("fail to store csv data: " + e.getMessage());
	//		}
	//	}
	
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
		Concept concept = Context.getConceptService().getConceptByUuid(conceptUuid);
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
	
	//	public void uploadProductRegimens(MultipartFile file) {
	//		try {
	//			List<Product> products = CSVHelper.csvProductRegimes(file.getInputStream());
	//			for (Product product : products) {
	//				saveProduct(product);
	//			}
	//
	//		}
	//		catch (IOException e) {
	//			throw new RuntimeException("fail to store csv data: " + e.getMessage());
	//		}
	//	}
	
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
	
	public void purgeUnusedAttributes(ProductOperationFlux flux) {
		if (!flux.getAttributes().isEmpty()) {
			for (ProductOperationFluxAttribute attribute : flux.getAttributes()) {
				if (!attributeIsUsed(attribute)) {
					getSession().delete(attribute);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void purgeUnusedAttributes() {
		String queryString = "SELECT p FROM ProductAttribute p WHERE p.location = :location p NOT IN ("
		        + " SELECT paf.attribute FROM ProductOperationFluxAttribute paf)";
		List<ProductAttribute> attributes = getSession().createQuery(queryString)
		        .setParameter("location", SupplyUtils.getUserLocation()).list();
		if (attributes != null) {
			if (!attributes.isEmpty()) {
				for (ProductAttribute attribute : attributes) {
					getSession().delete(attribute);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public Boolean attributeIsUsed(ProductOperationFluxAttribute attribute) {
		List<ProductOperationFluxAttribute> fluxAttributes = getSession()
		        .createCriteria(ProductOperationFluxAttribute.class)
		        .add(Restrictions.eq("location", SupplyUtils.getUserLocation()))
		        .add(Restrictions.eq("attribute", attribute)).list();
		if (!fluxAttributes.isEmpty()) {
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
    public List<ProductCode> getProductWithoutRegimeByProgram(ProductProgram productProgram) {
        //        String sqlQuery =
        //                "SELECT product_id FROM ( " +
        //                        "SELECT pp.product_id, count(ppr.product_regime_id) countRegimen  FROM supply_product pp " +
        //                        "LEFT JOIN supply_product_regime_members pprm on pp.product_id = pprm.product_id " +
        //                        "LEFT JOIN supply_product_regime ppr on ppr.product_regime_id = pprm.regime_id " +
        //                        "LEFT JOIN supply_product_program_members pppm on pp.product_id = pppm.product_id " +
        //                        "WHERE program_id = " + productProgram.getProductProgramId() + " " +
        //                        "GROUP BY pp.product_id " +
        //                        "HAVING countRegimen = (SELECT COUNT(*) FROM supply_product_regime) OR countRegimen = 0) _";

        //        Query query = getSession().createSQLQuery(sqlQuery);
        List<ProductRegime> regimes = getAllProductRegimes();

        Query query = getSession().createQuery(
                "SELECT p FROM ProductCode p LEFT JOIN p.regimes r WHERE p.program = :program GROUP BY p");
        query.setParameter("program", productProgram);

        List<ProductCode> products = query.list();

        return products.stream().filter((p) -> p.getRegimes().isEmpty() || p.getRegimes().size() == regimes.size()).collect(Collectors.toList());

        //        List<Product> productList = new ArrayList<>();
        //
        //        if (productIds != null) {
        //            for (Integer productId : productIds) {
        //                productList.add(getProduct(productId));
        //            }
        //        }
        //        return productList;
    }
	
	public ProductCode getProductCode(String uuid) {
		return (ProductCode) getSession().createCriteria(ProductCode.class).add(Restrictions.eq("uuid", uuid))
		        .uniqueResult();
	}
	
	public ProductCode getProductCodeByCode(String code) {
		return (ProductCode) getSession().createCriteria(ProductCode.class).add(Restrictions.eq("code", code))
		        .uniqueResult();
	}
	
	public ProductCode saveProductCode(ProductCode productCode) {
		getSession().saveOrUpdate(productCode);
		return productCode;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductCode> getProductCodes(ProductProgram program, ProductRegime productRegime) {
		//        String sqlQuery = "SELECT DISTINCT spc.* " +
		//                "FROM " +
		//                "    (SELECT * FROM supply2_product_code WHERE program_id = " + program.getProductProgramId() + ") spc " +
		//                "        LEFT JOIN " +
		//                "    supply2_product_code_regime_members spcrm ON spcrm.product_code_id = spc.product_code_id " +
		//                "        LEFT JOIN " +
		//                "    supply2_product_regime spr ON spcrm.regime_id = spr.product_regime_id" +
		//                "WHERE concept_id = " + productRegime.getConcept().getConceptId();
		
		Query query = getSession().createQuery(
		    "SELECT DISTINCT p FROM ProductCode p JOIN p.regimes r WHERE r.concept = :concept AND p.program = :program");
		query.setParameter("program", program);
		query.setParameter("concept", productRegime.getConcept());
		return query.list();
		
		//        Query query = getSession().createSQLQuery(sqlQuery);
		
		//        return getSession().createCriteria(ProductCode.class, "p").createAlias("p.regimes", "r")
		//                .add(Restrictions.eq("p.program", program))
		//                .add(Restrictions.eq("r.concept", productRegime.getConcept()))
		//                .list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductCode> getProductCodes(ProductProgram program, Boolean includeVoided) {
		
		Criteria criteria = getSession().createCriteria(ProductCode.class).add(Restrictions.eq("program", program));
		if (!includeVoided) {
			criteria.add(Restrictions.eq("voided", false));
		}
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ProductCode> getProductCodes(Boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(ProductCode.class);
		if (!includeVoided) {
			criteria.add(Restrictions.eq("voided", false));
		}
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
    public List<ProductCode> getAvailableProductCode(ProductProgram program) {
        Criteria criteria = getSession().createCriteria(ProductAttributeStock.class, "s")
                .createAlias("s.attribute", "a")
                .createAlias("a.productCode", "p")
                .add(Restrictions.eq("s.voided", false))
                .add(Restrictions.eq("p.program", program))
                .add(Restrictions.gt("s.quantityInStock", 0));
        List<ProductAttributeStock> stocks = criteria.list();

        List<ProductCode> productCodes = new ArrayList<>();

        if (stocks != null) {
            for (ProductAttributeStock stock : stocks) {
                ProductCode productCode = stock.getAttribute().getProductCode();
                if (!productCodes.contains(productCode)) {
                    productCodes.add(productCode);
                }
            }
        }
        return productCodes;
    }
}
