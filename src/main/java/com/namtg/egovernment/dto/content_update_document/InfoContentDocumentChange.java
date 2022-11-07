package com.namtg.egovernment.dto.content_update_document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class InfoContentDocumentChange {
    private Long documentId;
    private String titleDocument;
    private String oldContent;
    private String newContent;
    private String nameUpdater;
}
