package com.testpilot.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public class FileUtil {

    public static File toFile(MultipartFile multipartFile) throws Exception {
        File temp = File.createTempFile("upload-", multipartFile.getOriginalFilename());
        multipartFile.transferTo(temp);
        return temp;
    }
}
