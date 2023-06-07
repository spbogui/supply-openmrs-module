package org.openmrs.module.supply;

import org.openmrs.BaseOpenmrsData;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "ProductCode")
@Table(name = "supply2_product_code")
public class ProductCode extends BaseOpenmrsData {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_code_id", nullable = false)
	private Integer productCodeId;
	
	@Column(name = "code", nullable = false, unique = true)
	private String code;
	
	@ManyToOne
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;
	
	@ManyToOne
	@JoinColumn(name = "program_id", nullable = false)
	private ProductProgram program;
	
	//    @JsonIgnore
	//    @ManyToMany(cascade = CascadeType.ALL)
	//    @JoinTable(name = "supply2_product_code_regime_members", joinColumns = @JoinColumn(name = "product_code_id"), inverseJoinColumns = @JoinColumn(name = "regime_id"))
	//    private Set<ProductRegime> regimes = new HashSet<ProductRegime>();
	//
	//    @OneToMany(mappedBy = "productCode", fetch = FetchType.EAGER)
	//    private Set<ProductPrice> prices = new HashSet<ProductPrice>();
	
	//    @Transient
	//    private ProductPrice currentPrice;
	
	private Integer quantityInStock;
	
	public Integer getProductCodeId() {
		return productCodeId;
	}
	
	public void setProductCodeId(Integer productCodeId) {
		this.productCodeId = productCodeId;
	}
	
	@Override
	public Integer getId() {
		return getProductCodeId();
	}
	
	@Override
	public void setId(Integer id) {
		setProductCodeId(id);
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public Product getProduct() {
		return product;
	}
	
	public void setProduct(Product product) {
		this.product = product;
	}
	
	public ProductProgram getProgram() {
		return program;
	}
	
	public void setProgram(ProductProgram program) {
		this.program = program;
	}
	
	//    public Set<ProductRegime> getRegimes() {
	//        return regimes;
	//    }
	//
	//    public void setRegimes(Set<ProductRegime> regimes) {
	//        this.regimes = regimes;
	//    }
	//
	//    public Set<ProductPrice> getPrices() {
	//        return prices;
	//    }
	//
	//    public void setPrices(Set<ProductPrice> prices) {
	//        this.prices = prices;
	//    }
	
	//    public void setCurrentPrice(ProductPrice currentPrice) {
	//        this.currentPrice = currentPrice;
	//    }
	
	public Integer getQuantityInStock() {
		return quantityInStock;
	}
	
	public void setQuantityInStock(Integer quantityInStock) {
		this.quantityInStock = quantityInStock;
	}
	
	//	public void addPrice(ProductPrice price) {
	//        if (prices == null) {
	//            prices = new HashSet<>();
	//        }
	//        price.setProductCoe(this);
	//        prices.add(price);
	//    }
	
	//    public ProductPrice getCurrentPrice() {
	//        for (ProductPrice price : prices) {
	//            if (price.getActive()) {
	//                currentPrice = price;
	//                break;
	//            }
	//        }
	//        return currentPrice;
	//    }
	//
	//    public void addRegime(ProductRegime regime) {
	//        getRegimes().add(regime);
	//    }
	//
	//    public void removeRegime(ProductRegime regime) {
	//        getRegimes().remove(regime);
	//    }
}
