package com.ingestion.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import ru.yandex.clickhouse.ClickHouseDataSource;
import java.util.Properties;

@Configuration
public class ClickHouseConfig {

    @Value("${clickhouse.host}")
    private String host;

    @Value("${clickhouse.port}")
    private int port;

    @Value("${clickhouse.database}")
    private String database;

    @Value("${clickhouse.username}")
    private String username;

    @Value("${clickhouse.password}")
    private String password;

    @Bean
    public DataSource clickHouseDataSource() {
        // Log values to confirm connection information
        System.out.println("Connecting to ClickHouse with user: " + username + " and database: " + database);

        // Format the ClickHouse JDBC URL
        String url = String.format("jdbc:clickhouse://%s:%d/%s?compress=0", host, port, database);

        // Set properties for the connection
        Properties properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", password);
        

        // Return the ClickHouseDataSource bean
        return new ClickHouseDataSource(url, properties);
    }
}
