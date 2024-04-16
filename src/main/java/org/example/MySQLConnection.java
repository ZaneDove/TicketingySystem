package org.example;

import java.sql.*;

public class MySQLConnection {
    // 数据库配置
    private static final String URL = "jdbc:mysql://localhost:3306/DISP_G1?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "xiaweiyu";


    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        // 加载MySQL JDBC驱动
        Class.forName("com.mysql.cj.jdbc.Driver");
        // 创建并返回数据库连接
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
