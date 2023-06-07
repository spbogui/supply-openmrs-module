package org.openmrs.module.supply;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Concept;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "ProductRegime")
@Table(name = "supply2_product_regime")
public class ProductRegime extends BaseOpenmrsObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_regime_id", nullable = false)
    private Integer productRegimeId;

    @SuppressWarnings("JpaAttributeTypeInspection")
    @ManyToOne
    @JoinColumn(nullable = false, name = "concept_id", unique = true)
    private Concept concept;

//	@ManyToMany(mappedBy = "regimes", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//	private Set<Product> products = new HashSet<Product>();

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL)
    private Set<ProductCode> productCodes = new HashSet<>();

    public ProductRegime() {
    }

    public Integer getProductRegimeId() {
        return productRegimeId;
    }

    public void setProductRegimeId(Integer productRegimeId) {
        this.productRegimeId = productRegimeId;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

//	public Set<Product> getProducts() {
//		return products;
//	}
//
//	public void setProducts(Set<Product> products) {
//		this.products = products;
//	}

    public Set<ProductCode> getProductCodes() {
        return productCodes;
    }

    public void setProductCodes(Set<ProductCode> productCodes) {
        this.productCodes = productCodes;
    }

    @Override
    public Integer getId() {
        return productRegimeId;
    }

    @Override
    public void setId(Integer integer) {
        productRegimeId = integer;
    }

    @Override
    public String getUuid() {
        return super.getUuid();
    }

    @Override
    public void setUuid(String uuid) {
        super.setUuid(uuid);
    }
}
