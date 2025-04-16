// TableSchema.java
package com.ingestion.backend.model;

import java.util.List;
import java.util.Map;

public class TableSchema {
    private String tableName;
    private List<ColumnDefinition> columns;
    
    public TableSchema() {
    }
    
    public TableSchema(String tableName, List<ColumnDefinition> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnDefinition> columns) {
        this.columns = columns;
    }
    
    // Inner class for column definitions
    public static class ColumnDefinition {
        private String name;
        private String type;
        
        public ColumnDefinition() {
        }
        
        public ColumnDefinition(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}