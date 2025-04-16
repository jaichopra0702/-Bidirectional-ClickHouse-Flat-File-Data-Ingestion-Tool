package com.ingestion.backend.controller;

import com.ingestion.backend.service.ClickHouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "http://localhost:3000") // Allow frontend to access this
public class TableController {

    @Autowired
    private ClickHouseService clickHouseService;

    @GetMapping("/tables/{tableName}/columns")
    public ResponseEntity<List<String>> getTableColumns(@PathVariable String tableName) {
        try {
            List<String> columns = clickHouseService.getColumnsForTable(tableName);
            return ResponseEntity.ok(columns);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
