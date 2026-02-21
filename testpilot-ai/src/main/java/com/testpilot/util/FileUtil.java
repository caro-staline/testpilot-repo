package com.testpilot.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * Utility class for file operations.
 */
public class FileUtil {

    /**
     * Converts a Spring MultipartFile to a temporary local file on disk.
     * This is necessary for libraries like Tesseract that require a File object.
     * 
     * @param multipartFile The uploaded file.
     * @return A temporary File object.
     * @throws Exception if conversion or file creation fails.
     */
    public static File toFile(MultipartFile multipartFile) throws Exception {
        // Create a temporary file with a unique name
        File temp = File.createTempFile("upload-", multipartFile.getOriginalFilename());
        // Transfer the content of the multipart file to the new temporary file
        multipartFile.transferTo(temp);
        return temp;
    }
}
