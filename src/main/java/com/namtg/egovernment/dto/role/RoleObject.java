package com.namtg.egovernment.dto.role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class RoleObject {
    private boolean isCanAccessDocument;
    private boolean isOwnerDocument;
    private boolean isCanViewDocument;
    private boolean isCanUpdateDocument;
    private boolean isCanDeleteDocument;
    private boolean isCanSendDocument;
    private boolean isCanApproveDocument;
    private boolean isCanManageNews;
    private boolean isCanManageField;
    private boolean isCanManageReasonDeniedComment;
    private boolean isCanManageUser;
    private boolean isCanManageUnit;
}
