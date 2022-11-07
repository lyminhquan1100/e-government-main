package com.namtg.egovernment.util;

import org.apache.poi.ss.usermodel.*;

public class ExcelUtils {
    public static CellStyle createStyleHeader(Workbook workbook) {
        CellStyle styleHeader = workbook.createCellStyle();
        styleHeader.setWrapText(true);
        styleHeader.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleHeader.setBorderRight(BorderStyle.THIN);
        styleHeader.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        styleHeader.setBorderBottom(BorderStyle.THIN);
        styleHeader.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return styleHeader;
    }


    public static CellStyle createStyleCell(Workbook workbook) {
        CellStyle styleCell = workbook.createCellStyle();
        styleCell.setWrapText(true);
        return styleCell;
    }

    public static Font createFont(Workbook workbook, int fontSize) {
        Font newFont = workbook.createFont();
        newFont.setFontHeightInPoints((short) fontSize);
        return newFont;
    }
}
