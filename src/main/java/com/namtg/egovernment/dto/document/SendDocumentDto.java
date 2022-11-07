package com.namtg.egovernment.dto.document;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SendDocumentDto {
    private Long documentId;
    private List<WorkUnitIdReceiveAndPermission> listWorkUnitIdReceiveAndPermission;
}
