package com.testpilot.rag.chunking;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TextChunker {

    private static final int CHUNK_SIZE = 800;
    private static final int OVERLAP = 100;

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();

        int length = text.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + CHUNK_SIZE, length);
            chunks.add(text.substring(start, end));
            start = end - OVERLAP;
            if (start < 0) start = 0;
        }

        return chunks;
    }
}
