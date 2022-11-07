package com.namtg.egovernment.dto.document;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WorkUnitIdReceiveAndPermission {
    private Long workUnitId;
    private boolean isCanEditViewer;
    private boolean isCanEditUpdater;
    private boolean isCanEditApprover;
    private boolean isCanEditDeleter;
    private boolean isCanEditSender;
}
