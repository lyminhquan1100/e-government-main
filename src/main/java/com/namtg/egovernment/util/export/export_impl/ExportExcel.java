package com.namtg.egovernment.util.export.export_impl;

import com.namtg.egovernment.util.ExcelUtils;
import com.namtg.egovernment.util.export.ExportFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

public class ExportExcel<T> implements ExportFile<T> {

    private static final int DEFAULT_COLUMN_WIDTH = 30;
    private static final int DEFAULT_FONT_HEIGHT = 72;
    private static final int DEFAULT_MULTIPLES_FOR_HEADER_HEIGHT = 3;

    @Override
    public void exportFile(HttpServletResponse response,
                           String[] lineHeader,
                           List<T> listDataForExport,
                           Function<T, String[]> convertFunction,
                           String fileName,
                           String titleWord,
                           String contentWord) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet();
            sheet.setDefaultColumnWidth(DEFAULT_COLUMN_WIDTH);
            CellStyle styleHeader = ExcelUtils.createStyleHeader(workbook);
            CellStyle styleCell = ExcelUtils.createStyleCell(workbook);
            styleCell.setFont(ExcelUtils.createFont(workbook, DEFAULT_FONT_HEIGHT));

            Row headerRow = sheet.createRow(0);
            headerRow.setHeight((short) (headerRow.getHeight() * DEFAULT_MULTIPLES_FOR_HEADER_HEIGHT));

            for (int col = 0; col < lineHeader.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellStyle(styleHeader);
                cell.setCellValue(lineHeader[col]);
            }
            AtomicInteger rowIdx = new AtomicInteger(1);
            listDataForExport
                    .stream()
                    .map(convertFunction)
                    .forEach(dataRow -> {
                        Row row = sheet.createRow(rowIdx.get());
                        IntStream.range(0, dataRow.length).boxed().forEach(colIdx -> {
                            row.createCell(colIdx).setCellValue(dataRow[colIdx]);
                        });
                        rowIdx.getAndIncrement();
                    });

            response.setContentType("application/ms-excel; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException("Fail when import data to excel file: " + e.getMessage());
        }

    }
}
