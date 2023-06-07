package org.openmrs.module.supply;

import org.openmrs.BaseOpenmrsObject;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "ProductProgram")
@Table(name = "supply2_product_program")
public class ProductProgram extends BaseOpenmrsObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_program_id", nullable = false)
    private Integer productProgramId;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL)
    private Set<ProductCode> productCodes = new HashSet<>();

    public ProductProgram() {
    }

    public Integer getProductProgramId() {
        return productProgramId;
    }

    public void setProductProgramId(Integer productRegimenId) {
        this.productProgramId = productRegimenId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<ProductCode> getProductCodes() {
        return productCodes;
    }

    public void setProductCodes(Set<ProductCode> productCodes) {
        this.productCodes = productCodes;
    }

    @Override
    public Integer getId() {
        return productProgramId;
    }

    @Override
    public void setId(Integer integer) {
        this.productProgramId = integer;
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
