package com.ingestion.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FlatFileService {

    private static final Logger logger = LoggerFactory.getLogger(FlatFileService.class);

    // Preview contents of a flat file
    public List<Map<String, Object>> previewFile(MultipartFile file, String delimiter, int limit) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Read header
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("File is empty");
            }
            
            String[] headers = headerLine.split(delimiter, -1);
            
            // Read data rows
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < limit) {
                String[] values = line.split(delimiter, -1);
                Map<String, Object> row = new HashMap<>();
                
                for (int i = 0; i < Math.min(headers.length, values.length); i++) {
                    String header = headers[i];
                    String value = values[i];
                    
                    // Try to convert to appropriate data type
                    try {
                        Long longVal = Long.parseLong(value);
                        row.put(header, longVal);
                    } catch (NumberFormatException e1) {
                        try {
                            Double doubleVal = Double.parseDouble(value);
                            row.put(header, doubleVal);
                        } catch (NumberFormatException e2) {
                            row.put(header, value);
                        }
                    }
                }
                
                result.add(row);
                count++;
            }
        }
        
        return result;
    }
}