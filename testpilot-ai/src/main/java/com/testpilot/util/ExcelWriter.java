package com.testpilot.util;

import com.testpilot.model.TestCase;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Utility class for generating Excel files from test case data.
 */
public class ExcelWriter {

    /**
     * Converts a list of TestCase objects into an Excel spreadsheet byte array.
     * Uses Apache POI for XSSF (.xlsx) generation.
     * 
     * @param cases List of test cases to export.
     * @return Byte array containing the Excel file data.
     * @throws Exception if workbook generation or writing fails.
     */
    public static byte[] write(List<TestCase> cases) throws Exception {
        // Initialize Workbook and a single Sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Cases");

        // Create the header row and define column titles
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Title");
        header.createCell(2).setCellValue("Preconditions");
        header.createCell(3).setCellValue("Steps");
        header.createCell(4).setCellValue("Expected Result");

        // Iterate through the test cases and populate subsequent rows
        int rowIdx = 1;
        for (TestCase tc : cases) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(tc.id);
            row.createCell(1).setCellValue(tc.title);
            // Join lists with newlines for multi-line cell content
            row.createCell(2).setCellValue(String.join("\n", tc.preconditions));
            row.createCell(3).setCellValue(String.join("\n", tc.steps));
            row.createCell(4).setCellValue(tc.expectedResult);
        }

        // Export the workbook to a byte array stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }
}
