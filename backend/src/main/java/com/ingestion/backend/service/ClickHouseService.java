package com.ingestion.backend.service;

import com.ingestion.backend.model.IngestionRequest;
import com.ingestion.backend.model.IngestionResult;
import com.ingestion.backend.model.TableSchema;
import com.ingestion.backend.model.TableSchema.ColumnDefinition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ClickHouseService {

    private static final Logger logger = LoggerFactory.getLogger(ClickHouseService.class);
    private final Map<String, IngestionResult> ingestionStatuses = new ConcurrentHashMap<>();

    // Test connection to ClickHouse using the provided credentials
    public boolean testConnection(IngestionRequest request) {
        try (Connection connection = getConnection(request)) {
            return connection != null && !connection.isClosed();
        } catch (Exception e) {
            logger.error("Connection test failed: ", e);
            return false;
        }
    }

    // List all tables in the specified database
    public List<String> listTables(IngestionRequest request) throws SQLException {
        List<String> tables = new ArrayList<>();
        
        try (Connection connection = getConnection(request);
             ResultSet rs = connection.getMetaData().getTables(request.getDatabase(), null, "%", new String[]{"TABLE"})) {
            
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        
        return tables;
    }

    // Get schema information for a specific table
    public TableSchema getTableSchema(String tableName, IngestionRequest request) throws SQLException {
        List<ColumnDefinition> columns = new ArrayList<>();
        
        try (Connection connection = getConnection(request);
             Statement stmt = connection.createStatement()) {
            
            // Use DESCRIBE to get column info
            try (ResultSet rs = stmt.executeQuery("DESCRIBE " + tableName)) {
                while (rs.next()) {
                    String columnName = rs.getString("name");
                    String columnType = rs.getString("type");
                    columns.add(new ColumnDefinition(columnName, columnType));
                }
            }
        }
        
        return new TableSchema(tableName, columns);
    }

    // Preview data from a table
    public List<Map<String, Object>> previewTable(String tableName, IngestionRequest request, int limit) throws SQLException {
        List<Map<String, Object>> resultList = new ArrayList<>();
        
        // Build the query - use selected columns if specified
        StringBuilder queryBuilder = new StringBuilder("SELECT ");
        if (request.getSelectedColumns() != null && !request.getSelectedColumns().isEmpty()) {
            queryBuilder.append(String.join(", ", request.getSelectedColumns()));
        } else {
            queryBuilder.append("*");
        }
        queryBuilder.append(" FROM ").append(tableName).append(" LIMIT ").append(limit);
        
        // Execute query
        try (Connection connection = getConnection(request);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(queryBuilder.toString())) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                resultList.add(row);
            }
        }
        
        return resultList;
    }

    // Ingest data from ClickHouse to a Flat File
    public IngestionResult ingestClickHouseToFlatFile(IngestionRequest request) {
        String ingestionId = UUID.randomUUID().toString();
        IngestionResult result = new IngestionResult();
        result.setIngestionId(ingestionId);
        result.setStatus("STARTED");
        result.setStartTime(System.currentTimeMillis());
        ingestionStatuses.put(ingestionId, result);
        
        // Start the ingestion in a separate thread
        new Thread(() -> {
            try {
                executeClickHouseToFlatFileIngestion(request, result);
            } catch (Exception e) {
                logger.error("Error during ingestion: ", e);
                result.setStatus("FAILED");
                result.setMessage("Error: " + e.getMessage());
                result.setEndTime(System.currentTimeMillis());
            }
        }).start();
        
        return result;
    }

    private void executeClickHouseToFlatFileIngestion(IngestionRequest request, IngestionResult result) {
        AtomicInteger recordCount = new AtomicInteger(0);
        
        try {
            result.setStatus("IN_PROGRESS");
            
            // Build the query using selected columns
            StringBuilder queryBuilder = new StringBuilder("SELECT ");
            if (request.getSelectedColumns() != null && !request.getSelectedColumns().isEmpty()) {
                queryBuilder.append(String.join(", ", request.getSelectedColumns()));
            } else {
                queryBuilder.append("*");
            }
            queryBuilder.append(" FROM ").append(request.getTableName());
            
            // Handle JOIN configuration if provided
            if (request.getJoinConfig() != null && !request.getJoinConfig().isEmpty()) {
                String joinType = request.getJoinConfig().getOrDefault("type", "INNER JOIN");
                String joinTable = request.getJoinConfig().get("table");
                String joinCondition = request.getJoinConfig().get("condition");
                
                if (joinTable != null && joinCondition != null) {
                    queryBuilder.append(" ").append(joinType).append(" ")
                               .append(joinTable).append(" ON ")
                               .append(joinCondition);
                }
            }
            
            String query = queryBuilder.toString();
            logger.info("Executing query: {}", query);
            
            try (Connection connection = getConnection(request);
                 Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();
                
                // Determine delimiter to use (default to comma)
                String delimiter = request.getDelimiter() != null ? request.getDelimiter() : ",";
                
                try (FileWriter writer = new FileWriter(request.getFilePath())) {
                    // Write headers
                    for (int i = 1; i <= columnCount; i++) {
                        writer.append(meta.getColumnName(i));
                        if (i < columnCount) writer.append(delimiter);
                    }
                    writer.append("\n");
                    
                    // Write data rows
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            String value = rs.getString(i);
                            // Handle values that contain the delimiter
                            if (value != null && value.contains(delimiter)) {
                                writer.append("\"").append(value.replace("\"", "\"\"")).append("\"");
                            } else {
                                writer.append(value != null ? value : "");
                            }
                            if (i < columnCount) writer.append(delimiter);
                        }
                        writer.append("\n");
                        recordCount.incrementAndGet();
                    }
                }
                
                result.setStatus("COMPLETED");
                result.setTotalRecords(recordCount.get());
                result.setMessage("Successfully exported " + recordCount.get() + " records to " + request.getFilePath());
            }
        } catch (Exception e) {
            logger.error("Error during ClickHouse to Flat File ingestion: ", e);
            result.setStatus("FAILED");
            result.setMessage("Error: " + e.getMessage());
        } finally {
            result.setEndTime(System.currentTimeMillis());
        }
    }

    // Ingest data from a Flat File to ClickHouse
    public IngestionResult ingestFlatFileToClickHouse(MultipartFile file, IngestionRequest request) {
    String ingestionId = UUID.randomUUID().toString();
    IngestionResult result = new IngestionResult();
    result.setIngestionId(ingestionId);
    result.setStatus("STARTED");
    result.setStartTime(System.currentTimeMillis());
    ingestionStatuses.put(ingestionId, result);
    
    // Start the ingestion in a separate thread
    new Thread(() -> {
        try {
            executeFlatFileToClickHouseIngestion(file, request, result);
        } catch (Exception e) {
            logger.error("Error during ingestion: ", e);
            result.setStatus("FAILED");
            result.setMessage("Error: " + e.getMessage());
            result.setEndTime(System.currentTimeMillis());
        }
    }).start();
    
    return result;
}

private void executeFlatFileToClickHouseIngestion(MultipartFile file, IngestionRequest request, IngestionResult result) {
    AtomicInteger recordCount = new AtomicInteger(0);
    
    try {
        result.setStatus("IN_PROGRESS");
        
        // Create a temporary file from the uploaded MultipartFile
        File tempFile = File.createTempFile("upload_", ".csv");
        file.transferTo(tempFile);
        
        // Determine delimiter to use (default to comma)
        String delimiter = request.getDelimiter() != null ? request.getDelimiter() : ",";
        
        // Parse the CSV file to get headers and data types
        List<String> headers = new ArrayList<>();
        Map<String, String> columnTypes = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(tempFile))) {
            // Read headers
            String headerLine = br.readLine();
            if (headerLine != null) {
                headers = Arrays.asList(headerLine.split(delimiter));
            }
            
            // Read first data row to determine types
            String dataLine = br.readLine();
            if (dataLine != null) {
                String[] values = dataLine.split(delimiter);
                for (int i = 0; i < values.length; i++) {
                    columnTypes.put(headers.get(i), getColumnType(values[i]));
                }
            }
        }
        
        // Create ClickHouse table using inferred column types
        String createTableQuery = buildCreateTableQuery(request.getTableName(), headers, columnTypes);
        try (Connection connection = getConnection(request);
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTableQuery);
        }
        
        // Insert data in batches
        try (BufferedReader br = new BufferedReader(new FileReader(tempFile))) {
            // Skip header line
            br.readLine();
            
            String insertQuery = buildInsertQuery(request.getTableName(), headers);
            try (Connection connection = getConnection(request);
                 PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                
                String line;
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(delimiter);
                    for (int i = 0; i < values.length; i++) {
                        stmt.setString(i + 1, values[i]);
                    }
                    stmt.addBatch();
                    
                    if (recordCount.incrementAndGet() % 1000 == 0) {
                        stmt.executeBatch();
                    }
                }
                
                // Execute remaining batch
                stmt.executeBatch();
            }
        }
        
        result.setStatus("COMPLETED");
        result.setTotalRecords(recordCount.get());
        result.setMessage("Successfully ingested " + recordCount.get() + " records from file to ClickHouse.");
    } catch (Exception e) {
        logger.error("Error during Flat File to ClickHouse ingestion: ", e);
        result.setStatus("FAILED");
        result.setMessage("Error: " + e.getMessage());
    } finally {
        result.setEndTime(System.currentTimeMillis());
    }
}

    private Connection getConnection(IngestionRequest request) throws SQLException {
        String jdbcUrl = String.format("jdbc:clickhouse://%s:%d/%s", request.getHost(), request.getPort(), request.getDatabase());
        return DriverManager.getConnection(jdbcUrl, request.getUsername(), request.getPassword());
    }

    private String getColumnType(String value) {
        // Simple heuristic to determine column type (you can expand this as needed)
        if (value.matches("-?\\d+(\\.\\d+)?")) {
            return "FLOAT";
        } else if (value.matches("-?\\d+")) {
            return "INT32";
        } else {
            return "String";
        }
    }

    private String buildCreateTableQuery(String tableName, List<String> headers, Map<String, String> columnTypes) {
        StringBuilder queryBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        queryBuilder.append(tableName).append(" (");
        
        for (int i = 0; i < headers.size(); i++) {
            String columnName = headers.get(i);
            String columnType = columnTypes.get(columnName);
            queryBuilder.append(columnName).append(" ").append(columnType);
            if (i < headers.size() - 1) {
                queryBuilder.append(", ");
            }
        }
        
        queryBuilder.append(") ENGINE = MergeTree() ORDER BY tuple();");
        return queryBuilder.toString();
    }

    private String buildInsertQuery(String tableName, List<String> headers) {
        StringBuilder queryBuilder = new StringBuilder("INSERT INTO ");
        queryBuilder.append(tableName).append(" (");
        
        queryBuilder.append(String.join(", ", headers));
        queryBuilder.append(") VALUES (");
        
        queryBuilder.append("?,".repeat(headers.size()).substring(0, headers.size() * 2 - 1));  // Prepare placeholders
        
        queryBuilder.append(")");
        return queryBuilder.toString();
    }

    public IngestionResult getIngestionStatus(String ingestionId) {
    IngestionResult result = ingestionStatuses.get(ingestionId);
    
    if (result == null) {
        throw new IllegalArgumentException("Ingestion not found for the given ID: " + ingestionId);
    }
    
    return result;
}
@Autowired
    private DataSource dataSource;

    public List<String> getColumnsForTable(String tableName) throws SQLException {
        String query = "DESCRIBE TABLE " + tableName;
        List<String> columns = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                columns.add(rs.getString("name")); // "name" column gives column name in ClickHouse
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Optionally log or rethrow
        }
        return columns;
    }
}
