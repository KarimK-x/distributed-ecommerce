package edu.asu.ecommerce.dataaccess.models;

public class Brand {
    private int brandId;
    private String brandName;
    private String brandLogo;

    public Brand(int brandId, String brandName, String brandLogo) {
        this.brandId = brandId;
        this.brandName = brandName;
        this.brandLogo = brandLogo;
    }

    public Brand(String brandName, String brandLogo) {
        this.brandName = brandName;
        this.brandLogo = brandLogo;
    }

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
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
