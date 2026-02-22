package com.testpilot.model;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Document metadata.
 */
public class Document {
    private Long id;
    private String filename;
    private LocalDateTime uploadedAt;

    public Document() {
    }

    public Document(Long id, String filename, LocalDateTime uploadedAt) {
        this.id = id;
        this.filename = filename;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
