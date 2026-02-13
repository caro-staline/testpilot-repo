package com.testpilot.rag.chunking;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class PdfTextChunker {

    private static final int CHUNK_SIZE = 800;
    private static final int OVERLAP = 100;

    public List<String> chunk(Reader reader) throws IOException {
        List<String> chunks = new ArrayList<>();

        char[] buffer = new char[CHUNK_SIZE];
        char[] overlap = new char[OVERLAP];

        int read;
        boolean first = true;

        while ((read = reader.read(buffer)) != -1) {

            String chunk;
            if (first) {
                chunk = new String(buffer, 0, read);
                first = false;
            } else {
                chunk = new String(overlap) + new String(buffer, 0, read);
            }

            chunks.add(chunk);

            // copy overlap safely
            int overlapStart = Math.max(0, read - OVERLAP);
            System.arraycopy(buffer, overlapStart, overlap, 0, OVERLAP);
        }

        return chunks;
    }
}
