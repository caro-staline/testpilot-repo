const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export interface DocumentMetadata {
    id: number;
    filename: string;
    uploadedAt: string;
}

export interface DocumentChunk {
    id: number;
    documentId: number;
    chunkIndex: number;
    content: string;
}

export interface TestCase {
    id: string;
    scenario: string;
    title: string;
    preconditions: string[];
    steps: string[];
    expectedResult: string;
}

export const api = {
    // Document endpoints
    async listDocuments(): Promise<DocumentMetadata[]> {
        const res = await fetch(`${API_BASE_URL}/rag/documents`);
        if (!res.ok) throw new Error('Failed to fetch documents');
        return res.json();
    },

    async listChunks(docId: number): Promise<DocumentChunk[]> {
        const res = await fetch(`${API_BASE_URL}/rag/documents/${docId}/chunks`);
        if (!res.ok) throw new Error('Failed to fetch chunks');
        return res.json();
    },

    async uploadPdf(file: File): Promise<string> {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('sourceId', 'ui-upload');

        const res = await fetch(`${API_BASE_URL}/rag/upload/pdf`, {
            method: 'POST',
            body: formData,
        });
        if (!res.ok) throw new Error('Upload failed');
        return res.text();
    },

    // Test Case endpoints
    async generateTestCases(userStory: string): Promise<TestCase[]> {
        const res = await fetch(`${API_BASE_URL}/api/generatetestcase/json`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userStory }),
        });
        if (!res.ok) throw new Error('Generation failed');
        return res.json();
    },

    async generateMultimodal(userStory: string, screenshot: File): Promise<TestCase[]> {
        const formData = new FormData();
        formData.append('userStory', userStory);
        formData.append('screenshot', screenshot);

        const res = await fetch(`${API_BASE_URL}/api/generatetestcase/multimodel`, {
            method: 'POST',
            body: formData,
        });
        if (!res.ok) throw new Error('Multimodal generation failed');
        return res.json();
    },

    async exportExcel(testCases: TestCase[]): Promise<Blob> {
        const res = await fetch(`${API_BASE_URL}/api/generatetestcase/excel`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(testCases),
        });
        if (!res.ok) throw new Error('Export failed');
        return res.blob();
    },
};
