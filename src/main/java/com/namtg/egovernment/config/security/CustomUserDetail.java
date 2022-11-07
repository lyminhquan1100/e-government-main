package com.namtg.egovernment.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Setter
@Getter
public class CustomUserDetail extends User {
    private Long id;
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

    public CustomUserDetail(Long id,
                            boolean isOwnerDocument,
                            boolean isCanViewDocument,
                            boolean isCanUpdateDocument,
                            boolean isCanDeleteDocument,
                            boolean isCanSendDocument,
                            boolean isCanApproveDocument,
                            boolean isCanManageNews,
                            boolean isCanManageField,
                            boolean isCanManageReasonDeniedComment,
                            boolean isCanManageUser,
                            boolean isCanManageUnit,
                            Collection<? extends GrantedAuthority> authorities) {
        super("username", "password", true, true, true, true, authorities);
        this.id = id;
        this.isOwnerDocument = isOwnerDocument;
        this.isCanViewDocument = isCanViewDocument;
        this.isCanUpdateDocument = isCanUpdateDocument;
        this.isCanDeleteDocument = isCanDeleteDocument;
        this.isCanSendDocument = isCanSendDocument;
        this.isCanApproveDocument = isCanApproveDocument;
        this.isCanManageNews = isCanManageNews;
        this.isCanManageField = isCanManageField;
        this.isCanManageReasonDeniedComment = isCanManageReasonDeniedComment;
        this.isCanManageUser = isCanManageUser;
        this.isCanManageUnit = isCanManageUnit;
    }

}
