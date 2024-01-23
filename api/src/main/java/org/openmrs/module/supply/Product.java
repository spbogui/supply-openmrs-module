package org.openmrs.module.supply;

import org.openmrs.BaseOpenmrsData;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "Product")
@Table(name = "supply2_product")
public class Product extends BaseOpenmrsData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "conversion_unit", nullable = false)
    private Double conversionUnit;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<ProductName> names = new HashSet<ProductName>();

    @OneToMany(mappedBy = "product")
    private Set<ProductCode> productCodes = new HashSet<>();

    @Transient
    private String dispensationName;

    @Transient
    private String packagingName;

    @Transient
    private String dispensationUnit;

    @Transient
    private String packagingUnit;

    public Product() {
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Double getConversionUnit() {
        return conversionUnit;
    }

    public void setConversionUnit(Double conversionUnit) {
        this.conversionUnit = conversionUnit;
    }

    public Set<ProductName> getNames() {
        return names;
    }

    public void setNames(Set<ProductName> names) {
        this.names = names;
    }

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

    public String getPackagingUnit() {
        for (ProductName name : names) {
            if (name.getProductNameType().equals("PACKAGING")) {
                packagingUnit = name.getUnit().getName();
                break;
            }
        }
        return packagingUnit;
    }

    public void setPackagingUnit(String packagingUnit) {
        this.packagingUnit = packagingUnit;
    }

    public String getDispensationUnit() {
        for (ProductName name : names) {
            if (name.getProductNameType().equals("DISPENSATION")) {
                dispensationUnit = name.getUnit().getName();
                break;
            }
        }
        if (dispensationUnit == null) {
            dispensationUnit = getPackagingUnit();
        }
        return dispensationUnit;
    }

    public void setDispensationUnit(String dispensationUnit) {
        this.dispensationUnit = dispensationUnit;
    }

    public Set<ProductCode> getProductCodes() {
        return productCodes;
    }

    public void setProductCodes(Set<ProductCode> productCodes) {
        this.productCodes = productCodes;
    }

    public void addName(ProductName name) {
        if (names == null) {
            names = new HashSet<>();
        }
        name.setProduct(this);
        names.add(name);
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
