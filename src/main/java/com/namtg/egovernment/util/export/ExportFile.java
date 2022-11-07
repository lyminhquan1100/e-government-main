package com.namtg.egovernment.util.export;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.function.Function;

public interface ExportFile<T> {
    void exportFile(HttpServletResponse response,
                    String[] lineHeader,
                    List<T> listDataForExport,
                    Function<T, String[]> convertFunction,
                    String fileName,
                    String titleWord,
                    String contentWord);

}
