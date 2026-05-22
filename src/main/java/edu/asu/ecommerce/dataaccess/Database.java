package edu.asu.ecommerce.dataaccess;


import java.sql.*;

public class Database {

    public static void main(String[] args) throws SQLException {
        String sql = "select * from Customer";
        String url = "jdbc:sqlserver://localhost:1433;databaseName=Amazon;encrypt=true;trustServerCertificate=true;";
        Connection con = DriverManager.getConnection(url, "sa", "123456");

        Statement st = con.createStatement();
        ResultSet res = st.executeQuery(sql);

        while (res.next()){
            System.out.println(res.getString(2));
        }

        st.close();
        con.close();


    }
}
