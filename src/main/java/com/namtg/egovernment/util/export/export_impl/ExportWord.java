package com.namtg.egovernment.util.export.export_impl;

import com.namtg.egovernment.util.export.ExportFile;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class ExportWord<T> implements ExportFile<T> {

    private static final String COLOR_BLACK = "000000";
    private static String FONT_TEXT = "Times New Roman";
    private static int FONT_SIZE = 20;

    @Override
    public void exportFile(HttpServletResponse response,
                           String[] lineHeader,
                           List<T> listDataForExport,
                           Function<T, String[]> convertFunction,
                           String fileName,
                           String titleWord,
                           String contentWord) {

        XWPFDocument document = new XWPFDocument();
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setText(titleWord);
        titleRun.setColor(COLOR_BLACK);
        titleRun.setBold(true);
        titleRun.setFontFamily(FONT_TEXT);
        titleRun.setFontSize(FONT_SIZE);

        XWPFParagraph content = document.createParagraph();
        XWPFRun contentRun = content.createRun();
        contentRun.setText(contentWord);
        contentRun.setColor(COLOR_BLACK);
        contentRun.setFontFamily(FONT_TEXT);
        contentRun.setFontSize(FONT_SIZE);


        try {
            response.setContentType("application/msword; charset=UTF-8");
            response.setHeader("Content-disposition", "attachment; fileName=\"" + fileName + "\"");

            document.write(response.getOutputStream());
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
