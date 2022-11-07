package com.namtg.egovernment.dto.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class PermissionDocumentDto {
    private boolean isOwner;
    private boolean isCanEditPermission;

    private boolean isCanEditViewer;
    private boolean isCanEditUpdater;
    private boolean isCanEditApprover;
    private boolean isCanEditDeleter;
    private boolean isCanEditSender;

    public static PermissionDocumentDto permissionDocumentWithIsOwner = new PermissionDocumentDto(
            true,
            false,
            true,
            true,
            true,
            true,
            true);

    public static PermissionDocumentDto permissionDocumentWithNotPermission = new PermissionDocumentDto(
            false,
            false,
            false,
            false,
            false,
            false,
            false);

}
