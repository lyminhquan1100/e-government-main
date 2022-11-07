package com.namtg.egovernment.util.export.export_impl;

import com.namtg.egovernment.util.export.ExportFile;
import liquibase.util.csv.opencsv.CSVWriter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class ExportCsv<T> implements ExportFile<T> {
    @Override
    public void exportFile(HttpServletResponse response,
                           String[] lineHeader,
                           List<T> listDataForExport,
                           Function<T, String[]> convertFunction,
                           String fileName,
                           String titleWord,
                           String contentWord) {
        try {
            response.setContentType("txt/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".csv");
            response.setContentType("application/csv;charset=UTF-8");
            response.getWriter().write('\ufeff');

            CSVWriter csvWriter = new CSVWriter(response.getWriter(), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
            csvWriter.writeNext(lineHeader);

            listDataForExport
                    .stream()
                    .map(convertFunction)
                    .forEach(csvWriter::writeNext);

            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
