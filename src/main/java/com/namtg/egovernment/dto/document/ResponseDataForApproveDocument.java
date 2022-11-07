package com.namtg.egovernment.dto.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class ResponseDataForApproveDocument {
    private float versionCurrent;
    private String contentCurrent;
    private List<ContentUpdateAndNameUpdater> listUpdate;
}
