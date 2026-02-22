package com.testpilot.model;

/**
 * Data Transfer Object for Document chunks.
 */
public class DocumentChunk {
    private Long id;
    private Long documentId;
    private int chunkIndex;
    private String content;

    public DocumentChunk() {
    }

    public DocumentChunk(Long id, Long documentId, int chunkIndex, String content) {
        this.id = id;
        this.documentId = documentId;
        this.chunkIndex = chunkIndex;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
