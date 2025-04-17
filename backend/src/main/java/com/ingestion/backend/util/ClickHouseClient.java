package com.ingestion.backend.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ClickHouseClient {

    public static Connection getConnection(String host, String port, String database, String jwtToken) throws SQLException {
        String url = "jdbc:clickhouse://" + host + ":" + port + "/" + database;

        Properties properties = new Properties();
        properties.setProperty("user", "default"); // Or any ClickHouse user configured for JWT auth
        properties.setProperty("password", "0702"); // Pass JWT token as password

        return DriverManager.getConnection(url, properties);
    }
}
