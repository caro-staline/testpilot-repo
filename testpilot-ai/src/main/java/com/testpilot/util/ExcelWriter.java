package com.testpilot.util;

import com.testpilot.model.TestCase;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class ExcelWriter {

    public static byte[] write(List<TestCase> cases) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Title");
        header.createCell(2).setCellValue("Preconditions");
        header.createCell(3).setCellValue("Steps");
        header.createCell(4).setCellValue("Expected Result");

        int rowIdx = 1;
        for (TestCase tc : cases) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(tc.id);
            row.createCell(1).setCellValue(tc.title);
            row.createCell(2).setCellValue(String.join("\n", tc.preconditions));
            row.createCell(3).setCellValue(String.join("\n", tc.steps));
            row.createCell(4).setCellValue(tc.expectedResult);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }
}
