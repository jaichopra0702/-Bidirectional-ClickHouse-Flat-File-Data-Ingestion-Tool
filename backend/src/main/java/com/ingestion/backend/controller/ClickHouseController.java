package com.ingestion.backend.controller;

import com.ingestion.backend.service.ClickHouseService;
import com.ingestion.backend.service.FlatFileService;
import com.ingestion.backend.model.IngestionRequest;
import com.ingestion.backend.model.IngestionResult;
import com.ingestion.backend.model.TableSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "http://localhost:3000")  // Allow React front-end
public class ClickHouseController {

    @Autowired
    private ClickHouseService clickHouseService;
    
    @Autowired
    private FlatFileService flatFileService;

    // Other existing endpoints...

    // New endpoint to retrieve price data from uk_price_paid
    @GetMapping("/price-data")
    public ResponseEntity<?> getPriceData(@RequestParam(defaultValue = "100") int limit) {
        try {
            // Fetch price data from the uk_price_paid table in ClickHouse
            List<Map<String, Object>> data = clickHouseService.previewTable("uk_price_paid", null, limit);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving price data: " + e.getMessage());
        }
    }

    // Endpoint to test connection to ClickHouse
    @PostMapping("/test-connection")
    public ResponseEntity<?> testConnection(@RequestBody IngestionRequest request) {
        try {
            boolean isConnected = clickHouseService.testConnection(request);
            if (isConnected) {
                return ResponseEntity.ok("Connection successful");
            } else {
                return ResponseEntity.status(400).body("Connection failed");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error testing connection: " + e.getMessage());
        }
    }

    // Endpoint to get list of tables from ClickHouse
    @PostMapping("/tables")
    public ResponseEntity<?> getTables(@RequestBody IngestionRequest request) {
        try {
            List<String> tables = clickHouseService.listTables(request);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving tables: " + e.getMessage());
        }
    }

    // Endpoint to get schema (column names) for a specific table
    @PostMapping("/schema/{table}")
    public ResponseEntity<?> getTableSchema(@PathVariable String table, @RequestBody IngestionRequest request) {
        try {
            TableSchema schema = clickHouseService.getTableSchema(table, request);
            return ResponseEntity.ok(schema);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving schema: " + e.getMessage());
        }
    }

    // Endpoint to get data from any table with a limit (preview data)
    @PostMapping("/preview/{table}")
    public ResponseEntity<?> previewTableData(@PathVariable String table,
                                            @RequestBody IngestionRequest request,
                                            @RequestParam(defaultValue = "100") int limit) {
        try {
            List<Map<String, Object>> data = clickHouseService.previewTable(table, request, limit);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error previewing data: " + e.getMessage());
        }
    }

    // Endpoint to parse and preview flat file
    @PostMapping("/preview-file")
    public ResponseEntity<?> previewFileData(@RequestParam("file") MultipartFile file, 
                                           @RequestParam("delimiter") String delimiter,
                                           @RequestParam(defaultValue = "100") int limit) {
        try {
            List<Map<String, Object>> data = flatFileService.previewFile(file, delimiter, limit);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error previewing file: " + e.getMessage());
        }
    }

    // Endpoint to handle ingestion requests from ClickHouse to FlatFile
    @PostMapping("/ingest/clickhouse-to-flatfile")
    public ResponseEntity<?> startIngestionClickHouseToFlatFile(@RequestBody IngestionRequest request) {
        try {
            IngestionResult result = clickHouseService.ingestClickHouseToFlatFile(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error during ingestion: " + e.getMessage());
        }
    }

    // Endpoint to handle ingestion from FlatFile to ClickHouse
    @PostMapping("/ingest/flatfile-to-clickhouse")
    public ResponseEntity<?> startIngestionFlatFileToClickHouse(
            @RequestParam("file") MultipartFile file,
            @RequestPart("request") IngestionRequest request) {
        try {
            IngestionResult result = clickHouseService.ingestFlatFileToClickHouse(file, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error during ingestion: " + e.getMessage());
        }
    }

    // Endpoint to check the status of an ongoing or completed ingestion
    @GetMapping("/ingest/status/{ingestionId}")
    public ResponseEntity<?> getIngestionStatus(@PathVariable String ingestionId) {
        try {
            IngestionResult status = clickHouseService.getIngestionStatus(ingestionId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving status: " + e.getMessage());
        }
    }
}
