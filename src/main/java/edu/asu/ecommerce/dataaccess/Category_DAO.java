package edu.asu.ecommerce.dataaccess;

import edu.asu.ecommerce.dataaccess.models.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Category_DAO {
    private final Connection con;

    public Category_DAO(Connection con) {
        this.con = con;
    }

    public int insertCategory(Category category) throws SQLException {
        int categoryId = category.getCategoryId();
        if (categoryId <= 0) {
            categoryId = getNextCategoryId();
            category.setCategoryId(categoryId);
        }

        String sql = "INSERT INTO Category (categoryID, categoryName) VALUES (?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, categoryId);
            pst.setString(2, category.getCategoryName());
            pst.executeUpdate();
        }

        return categoryId;
    }

    public int insertCategory(String categoryName) throws SQLException {
        return insertCategory(new Category(categoryName));
    }

    private int getNextCategoryId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(categoryID), 0) + 1 AS nextId FROM Category";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("nextId");
            }
        }
        return 1;
    }
}
