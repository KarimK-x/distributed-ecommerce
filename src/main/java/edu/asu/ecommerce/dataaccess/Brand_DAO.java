package edu.asu.ecommerce.dataaccess;

import edu.asu.ecommerce.dataaccess.models.Brand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Brand_DAO {
    private final Connection con;

    public Brand_DAO(Connection con) {
        this.con = con;
    }

    public int insertBrand(Brand brand) throws SQLException {
        int brandId = brand.getBrandId();
        if (brandId <= 0) {
            brandId = getNextBrandId();
            brand.setBrandId(brandId);
        }

        String sql = "INSERT INTO Brand (brandID, brandName, logoURL) VALUES (?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, brandId);
            pst.setString(2, brand.getBrandName());
            pst.setString(3, brand.getBrandLogo());
            pst.executeUpdate();
        }

        return brandId;
    }

    public int insertBrand(String brandName, String logoUrl) throws SQLException {
        return insertBrand(new Brand(brandName, logoUrl));
    }

    public Integer getBrandIdByName(String brandName) throws SQLException {
        String sql = "SELECT brandID FROM Brand WHERE brandName = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, brandName);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("brandID");
                }
            }
        }
        return null;
    }

    public int findOrCreateBrand(String brandName, String logoUrl) throws SQLException {
        Integer existingId = getBrandIdByName(brandName);
        if (existingId != null) {
            return existingId;
        }
        return insertBrand(brandName, logoUrl);
    }

    private int getNextBrandId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(brandID), 0) + 1 AS nextId FROM Brand";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("nextId");
            }
        }
        return 1;
    }
}
