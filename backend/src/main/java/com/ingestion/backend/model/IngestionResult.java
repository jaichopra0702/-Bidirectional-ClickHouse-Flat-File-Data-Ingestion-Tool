// IngestionResult.java
package com.ingestion.backend.model;

public class IngestionResult {
    private String ingestionId;
    private String status; // STARTED, IN_PROGRESS, COMPLETED, FAILED
    private int totalRecords;
    private String message;
    private long startTime;
    private long endTime;
    
    public IngestionResult() {
    }
    
    public String getIngestionId() {
        return ingestionId;
    }
    
    public void setIngestionId(String ingestionId) {
        this.ingestionId = ingestionId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getTotalRecords() {
        return totalRecords;
    }
    
    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    // Convenience method to calculate duration
    public long getDurationMillis() {
        if (endTime > 0 && startTime > 0) {
            return endTime - startTime;
        }
        return 0;
    }
}