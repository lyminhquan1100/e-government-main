package com.namtg.egovernment.dto.document;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SaveApproveDocument {
    private Long documentId;
    private float versionNew;
    private String contentNew;
}
