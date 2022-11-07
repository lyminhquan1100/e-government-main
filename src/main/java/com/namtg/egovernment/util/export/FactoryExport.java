package com.namtg.egovernment.util.export;

import com.namtg.egovernment.enum_common.TypeExport;
import com.namtg.egovernment.util.export.export_impl.ExportCsv;
import com.namtg.egovernment.util.export.export_impl.ExportExcel;
import com.namtg.egovernment.util.export.export_impl.ExportWord;

public class FactoryExport {
    public static ExportFile exportFileWithType(TypeExport typeExport) {
        if (typeExport == TypeExport.CSV) {
            return new ExportCsv();
        }
        if (typeExport == TypeExport.EXCEL) {
            return new ExportExcel();
        }
        if (typeExport == TypeExport.WORD) {
            return new ExportWord();
        }
        throw new IllegalArgumentException("Invalid type export");
    }
}
