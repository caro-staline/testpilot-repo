package com.testpilot.rag.chunking;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class RecursiveTextChunker {

    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int DEFAULT_OVERLAP = 200;

    // Separators in order of preference
    private static final String[] SEPARATORS = { "\n\n", "\n", "\\. ", " " };

    public List<String> chunk(String text) {
        return chunk(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
    }

    public List<String> chunk(String text, int chunkSize, int overlap) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        return splitText(text, chunkSize, overlap, 0);
    }

    private List<String> splitText(String text, int chunkSize, int overlap, int separatorIndex) {
        List<String> finalChunks = new ArrayList<>();

        if (text.length() <= chunkSize) {
            finalChunks.add(text);
            return finalChunks;
        }

        // If we ran out of separators, resort to character splitting
        if (separatorIndex >= SEPARATORS.length) {
            return splitByCharacter(text, chunkSize, overlap);
        }

        String separator = SEPARATORS[separatorIndex];
        String[] splits = text.split(separator); // simple split for now, could be regex
        if (separator.equals("\\. ")) {
            // Re-add the period for sentence splitting if we want (simple implementation
            // assumes it's gone)
            // For better sentence splitting, we might want a more robust regex or library
        }

        List<String> goodSplits = new ArrayList<>();

        for (String s : splits) {
            if (s.isEmpty())
                continue;
            // Recursively split if still too big
            if (s.length() > chunkSize) {
                goodSplits.addAll(splitText(s, chunkSize, overlap, separatorIndex + 1));
            } else {
                goodSplits.add(s);
            }
        }

        // Merge small splits into chunks
        return mergeSplits(goodSplits, chunkSize, overlap, separator);
    }

    private List<String> mergeSplits(List<String> splits, int chunkSize, int overlap, String separator) {
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        String joiner = separator.equals("\\. ") ? ". " : separator; // restore separator for joining

        for (String split : splits) {
            if (currentChunk.length() + split.length() + joiner.length() > chunkSize) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    // Implement overlap logic here if needed, for simplicity we just start fresh or
                    // keep last segment
                    // Proper overlap with recursive splitting is complex;
                    // a simple valid approach is to keep the last 'overlap' characters if possible,
                    // or just start a new chunk.
                    // For this implementation, we reset.
                    currentChunk = new StringBuilder();
                }
            }
            if (currentChunk.length() > 0) {
                currentChunk.append(joiner);
            }
            currentChunk.append(split);
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    private List<String> splitByCharacter(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start += (chunkSize - overlap);
        }
        return chunks;
    }
}
