package com.ingestion.backend.model;

import java.util.List;
import java.util.Map;

public class IngestionRequest {
    private String sourceType;           // "ClickHouse" or "FlatFile"
    private String targetType;           // "ClickHouse" or "FlatFile"
    private String filePath;             // Path for flat file
    private String delimiter;            // Delimiter for flat file (comma, tab, etc.)
    private String tableName;            // ClickHouse table name
    private String host;                 // ClickHouse host
    private int port;                    // ClickHouse port
    private String database;             // ClickHouse database
    private String user;                 // ClickHouse user (existing)
    private String username;             // New: Username for additional use
    private String password;             // New: Password for additional use
    private String jwtToken;             // JWT token for authentication
    private List<String> selectedColumns; // Columns to include in the ingestion
    private Map<String, String> joinConfig; // For bonus feature - JOIN configuration
    
    // Constructor
    public IngestionRequest() {
    }
    
    // Getters and Setters
    public String getSourceType() {
        return sourceType;
    }
    
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getDelimiter() {
        return delimiter;
    }
    
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getDatabase() {
        return database;
    }
    
    public void setDatabase(String database) {
        this.database = database;
    }
    
    public String getUser() {
        return user;
    }
    
    public void setUser(String user) {
        this.user = user;
    }

    // New getter and setter for username
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // New getter and setter for password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getJwtToken() {
        return jwtToken;
    }
    
    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }
    
    public List<String> getSelectedColumns() {
        return selectedColumns;
    }
    
    public void setSelectedColumns(List<String> selectedColumns) {
        this.selectedColumns = selectedColumns;
    }
    
    public Map<String, String> getJoinConfig() {
        return joinConfig;
    }
    
    public void setJoinConfig(Map<String, String> joinConfig) {
        this.joinConfig = joinConfig;
    }
}
