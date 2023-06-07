package org.openmrs.module.supply;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.supply.api.ProductOperationService;
import org.openmrs.module.supply.entities.ProductQuantity;
import org.openmrs.module.supply.utils.SupplyUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(name = "Product")
@Table(name = "supply2_product")
public class Product extends BaseOpenmrsData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false)
    private Integer productId;

//    @Column(name = "code", nullable = false, unique = true)
//    private String code;

    @Column(name = "conversion_unit", nullable = false)
    private Double conversionUnit;

//    @JsonIgnore
//    @ManyToMany(cascade = CascadeType.ALL)
//    @JoinTable(name = "supply_product_program_members", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "program_id"))
//    private Set<ProductProgram> programs = new HashSet<ProductProgram>();

//    @JsonIgnore
//    @ManyToMany(cascade = CascadeType.ALL)
//    @JoinTable(name = "supply_product_regime_members", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "regime_id"))
//    private Set<ProductRegime> regimes = new HashSet<ProductRegime>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<ProductName> names = new HashSet<ProductName>();

//    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
//    private Set<ProductPrice> prices = new HashSet<ProductPrice>();

    @Transient
    private ProductPrice currentPrice;

    @Transient
    private String dispensationName;

    @Transient
    private String packagingName;

    @Transient
    private List<ProductQuantity> stock = new ArrayList<>();

    @Transient
    private List<ProductQuantity> stockAll = new ArrayList<>();

    public Product() {
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

//    public String getCode() {
//        return code;
//    }
//
//    public void setCode(String code) {
//        this.code = code;
//    }

    public Double getConversionUnit() {
        return conversionUnit;
    }

    public void setConversionUnit(Double conversionUnit) {
        this.conversionUnit = conversionUnit;
    }

//    public Set<ProductProgram> getPrograms() {
//        return programs;
//    }
//
//    public void setPrograms(Set<ProductProgram> programs) {
//        this.programs = programs;
//    }

//    public Set<ProductRegime> getRegimes() {
//        return regimes;
//    }
//
//    public void setRegimes(Set<ProductRegime> regimes) {
//        this.regimes = regimes;
//    }

    public Set<ProductName> getNames() {
        return names;
    }

    public void setNames(Set<ProductName> names) {
        this.names = names;
    }

//    public Set<ProductPrice> getPrices() {
//        return prices;
//    }
//
//    public void setPrices(Set<ProductPrice> prices) {
//        this.prices = prices;
//    }

    public void addDispensationName(ProductName name) {
        if (names == null) {
            names = new HashSet<ProductName>();
        }
        name.setProduct(this);
        name.setProductNameType("DISPENSATION");
        names.add(name);
    }

    public void addPackagingName(ProductName name) {
        if (names == null) {
            names = new HashSet<ProductName>();
        }
        name.setProduct(this);
        name.setProductNameType("PACKAGING");
        names.add(name);
    }

//    public void addProgram(ProductProgram productProgram) {
//        getPrograms().add(productProgram);
//    }
//
//    public void removeProgram(ProductProgram productProgram) {
//        getPrograms().remove(productProgram);
//    }

//    public void addRegime(ProductRegime regime) {
//        getRegimes().add(regime);
//    }
//
//    public void removeRegimen(ProductRegime regime) {
//        getRegimes().remove(regime);
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

    public String getDispensationName() {
        for (ProductName name : names) {
            if (name.getProductNameType().equals("DISPENSATION")) {
                dispensationName = name.getName();
                break;
            }
        }
        if (dispensationName == null) {
            dispensationName = getPackagingName();
        }
        return dispensationName;
    }

    public String getPackagingName() {
        for (ProductName name : names) {
            if (name.getProductNameType().equals("PACKAGING")) {
                packagingName = name.getName();
                break;
            }
        }
        return packagingName;
    }

    public void addName(ProductName name) {
        if (names == null) {
            names = new HashSet<>();
        }
        name.setProduct(this);
        names.add(name);
    }

//    public void addPrice(ProductPrice price) {
//        if (prices == null) {
//            prices = new HashSet<>();
//        }
//        price.setProduct(this);
//        prices.add(price);
//    }

    public List<ProductQuantity> getStockAll() {
        List<ProductProgram> programs = SupplyUtils.getUserLocationPrograms();
        // List<String> stockInformation = new ArrayList<String>();
        for (ProductProgram program : programs) {
            Double quantity = 0.;
            for (Location location : SupplyUtils.getUserLocations()) {
                List<ProductAttributeStock> stocks = Context.getService(ProductOperationService.class)
                        .getAllProductAttributeStocks(location, program, this);
                for (ProductAttributeStock stock : stocks) {
                    quantity += stock.getQuantityInStock();
                }
            }
            // stockInformation.add(program.getName() + "=" + quantity.intValue());
            stockAll.add(new ProductQuantity(program.getName(), quantity));
        }

        // stockAll = SupplyUtils.join(",", stockInformation);
        return stockAll;
    }

    public List<ProductQuantity> getStock() {
        List<ProductProgram> programs = SupplyUtils.getUserLocationPrograms();
        // List<String> stockInformation = new ArrayList<String>();
        // List<ProductQuantity> productQuantities = new ArrayList<>();

        for (ProductProgram program : programs) {
            Double quantity = 0.;
            List<ProductAttributeStock> stocks = Context.getService(ProductOperationService.class)
                    .getAllProductAttributeStocks(SupplyUtils.getUserLocation(), program, this);
            for (ProductAttributeStock stock : stocks) {
                quantity += stock.getQuantityInStock();
            }
            // ProductQuantity productQuantity = new ProductQuantity(program.getName(), quantity);
            this.stock.add(new ProductQuantity(program.getName(), quantity));

            // stockInformation.add(program.getName() + "=" + quantity.intValue());
        }

        // stock = SupplyUtils.join(",", stockInformation);
        return stock;
    }

    @Override
    public Integer getId() {
        return productId;
    }

    @Override
    public void setId(Integer integer) {
        setProductId(integer);
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
