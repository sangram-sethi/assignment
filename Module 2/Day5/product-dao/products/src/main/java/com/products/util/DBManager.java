package com.products.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
    
    private static final String URL = "jdbc:postgresql://localhost:5432/nacl";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "12345";

    private DBManager() {

    }

    public static Connection getConnection() {

        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);

        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed", e);
        }
        
    }
}
