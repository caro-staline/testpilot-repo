package com.testpilot.util;

public class JsonExtractor {

    public static String extract(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');

        if (start == -1 || end == -1) {
            throw new RuntimeException("No JSON array found in LLM response:\n" + text);
        }
        return text.substring(start, end + 1);
    }
}
