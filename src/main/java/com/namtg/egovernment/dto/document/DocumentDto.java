package com.namtg.egovernment.dto.document;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DocumentDto {
    private Long id;
    private Long workUnitId;
    private float version;
    private String title;
    private String content;
    private String target;
    private String conclude;
    private String listApproverId;
    private String listUpdaterId;
    private String listViewerId;
    private String listDeleterId;
    private String listSenderId;

}
