package edu.asu.ecommerce.dataaccess.models;

public class Brand {
    private int brandId;
    private String brandName;
    private String brandLogo;

    public Brand(String brandName, String brandLogo) {
        this.brandName = brandName;
        this.brandLogo = brandLogo;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getBrandLogo() {
        return brandLogo;
    }

    public void setBrandLogo(String brandLogo) {
        this.brandLogo = brandLogo;
    }
}
