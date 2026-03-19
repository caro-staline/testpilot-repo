package com.testpilot.rag.chunking;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

///**
// * Component for splitting large text blocks into smaller chunks recursively.
// * It prioritizes splitting at logical boundaries like paragraphs, then lines,
// * then sentences.
// */
//@Component
//public class RecursiveTextChunker {
//
//    private static final int DEFAULT_CHUNK_SIZE = 800;
//    private static final int DEFAULT_OVERLAP = 150;
//
//    // Separators ordered by preference for semantic splitting
//    private static final String[] SEPARATORS = { "\n\n", "\n", "\\. " };
//
//    /**
//     * Splits text into chunks using default size and overlap values.
//     * 
//     * @param text The input text.
//     * @return List of text chunks.
//     */
//    public List<String> chunk(String text) {
//        return chunk(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
//    }
//
//    /**
//     * Splits text into chunks with specified constraints.
//     * 
//     * @param text      The input text.
//     * @param chunkSize Maximum size of each chunk.
//     * @param overlap   Number of characters to overlap between adjacent chunks.
//     * @return List of text chunks.
//     */
//    public List<String> chunk(String text, int chunkSize, int overlap) {
//        if (text == null || text.isEmpty()) {
//            return new ArrayList<>();
//        }        
//        return splitText(text, chunkSize, overlap, 0);
//    }
//
//    /**
//     * Recursively splits text using a hierarchy of separators.
//     * 
//     * @param text           Current segment of text.
//     * @param chunkSize      Target maximum size.
//     * @param overlap        Desired overlap.
//     * @param separatorIndex Index of the current separator being used from
//     *                       SEPARATORS.
//     * @return List of split segments.
//     */
//    private List<String> splitText(String text, int chunkSize, int overlap, int separatorIndex) {
//        List<String> finalChunks = new ArrayList<>();
//
//        // If segment is small enough, no further splitting needed
//        if (text.length() <= chunkSize) {
//            finalChunks.add(text);
//            return finalChunks;
//        }
//
//        // Base case: If we've exhausted all separators, fallback to character-level
//        // splitting
//        if (separatorIndex >= SEPARATORS.length) {
//            return splitByCharacter(text, chunkSize, overlap);
//        }
//
//        String separator = SEPARATORS[separatorIndex];
//        String[] splits = text.split(separator); // Split text by the current separator
//       
//        List<String> goodSplits = new ArrayList<>();
//
//        for (String s : splits) {
//        	if (s.isEmpty())
//                continue;
//            
//            // If the resulting split is still too large, try splitting it again with the
//            // NEXT separator
//            if (s.length() > chunkSize) {
//                goodSplits.addAll(splitText(s, chunkSize, overlap, separatorIndex + 1));
//            } else {
//                goodSplits.add(s);
//            }
//        }
//        
//
//        // After recursive splitting, merge small segments back together into chunks of
//        // target size
//        return mergeSplits(goodSplits, chunkSize, overlap, separator);
//    }
//
//    /**
//     * Recombines small splits into chunks up to the maximum chunk size.
//     * 
//     * @param splits    List of small text segments.
//     * @param chunkSize Maximum chunk size.
//     * @param overlap   Desired overlap (simplified in this version).
//     * @param separator The original separator used to join the pieces back
//     *                  correctly.
//     * @return List of merged chunks.
//     */
//    private List<String> mergeSplits(List<String> splits, int chunkSize, int overlap, String separator) {
//        List<String> chunks = new ArrayList<>();
//        StringBuilder currentChunk = new StringBuilder();
//
//        // Restore period if splitting by sentence
//        String joiner = separator.equals("\\. ") ? ". " : separator;
//        for (String split : splits) {
//        	
//            // Check if adding the next split exceeds the limit
//            if (currentChunk.length() + split.length() + joiner.length() > chunkSize) {
//                if (currentChunk.length() > 0) {
//                    chunks.add(currentChunk.toString().trim());
//                    // Start a new chunk - overlap logic could be more robust here
//                    currentChunk = new StringBuilder();
//                }
//            }
//            // Append joiner if it's not the start of a chunk
//            if (currentChunk.length() > 0) {
//                currentChunk.append(joiner);
//            }
//            currentChunk.append(split);
//        }
//
//        // Add the final remaining chunk
//        if (currentChunk.length() > 0) {
//            chunks.add(currentChunk.toString().trim());
//        }
//      
//        return chunks;
//    }
//
//    /**
//     * Fallback method to split text purely by character count when no separators
//     * work.
//     * 
//     * @param text      Input text.
//     * @param chunkSize Target size.
//     * @param overlap   Number of characters to repeat in the next chunk.
//     * @return List of chunks.
//     */
//    private List<String> splitByCharacter(String text, int chunkSize, int overlap) {
//        List<String> chunks = new ArrayList<>();
//        int start = 0;
//        int end = 0 ;
//        while (start < text.length()) {
//            end = Math.min(start + chunkSize, text.length());
//            chunks.add(text.substring(start, end));
//            // Move start pointer back by overlap amount to preserve context
//            start += (chunkSize - overlap);
//        }
//        return chunks;
//    }
//}



@Component
public class RecursiveTextChunker {

    private static final int DEFAULT_CHUNK_SIZE = 800;
    private static final int DEFAULT_OVERLAP = 150;

    private static final String[] SEPARATORS = {"\n\n", "\n", "\\. ", " "};

    public List<String> chunk(String text) {
        return chunk(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
    }

    public List<String> chunk(String text, int chunkSize, int overlap) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        List<String> splits = recursiveSplit(text, 0, chunkSize);
        return mergeWithOverlap(splits, chunkSize, overlap);
    }

    private List<String> recursiveSplit(String text, int separatorIndex, int chunkSize) {

        if (text.length() <= chunkSize || separatorIndex >= SEPARATORS.length) {
            return List.of(text);
        }

        String separator = SEPARATORS[separatorIndex];
        String[] parts = text.split(separator);

        List<String> result = new ArrayList<>();

        for (String part : parts) {

            if (part.length() > chunkSize) {
                result.addAll(recursiveSplit(part, separatorIndex + 1, chunkSize));
            } else {
                result.add(part.trim());
            }
        }

        return result;
    }

    private List<String> mergeWithOverlap(List<String> splits, int chunkSize, int overlap) {

        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (String split : splits) {

            if (currentChunk.length() + split.length() + 1 > chunkSize) {

                String chunk = currentChunk.toString().trim();
                chunks.add(chunk);

                // Create overlap
                int overlapStart = Math.max(0, chunk.length() - overlap);
                String overlapText = chunk.substring(overlapStart);

                currentChunk = new StringBuilder(overlapText);
            }

            if (currentChunk.length() > 0) {
                currentChunk.append(" ");
            }

            currentChunk.append(split);
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }
}


