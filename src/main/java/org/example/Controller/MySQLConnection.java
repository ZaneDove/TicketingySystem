package org.example.Controller;

import java.sql.*;

public class MySQLConnection {
    // database configuration
    private static final String URL = "jdbc:mysql://localhost:3306/DISP_G1?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "xiaweiyu";


    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        // Load the MySQL JDBC driver
        Class.forName("com.mysql.cj.jdbc.Driver");
        // Create and return a database connection
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
