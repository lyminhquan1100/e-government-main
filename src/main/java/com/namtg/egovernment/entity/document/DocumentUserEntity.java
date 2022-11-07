package com.namtg.egovernment.entity.document;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "document_user")
public class DocumentUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long documentId;
    private Long userId;
    private boolean isOwner;
    private boolean isView;
    private boolean isUpdate;
    private boolean isDelete;
    private boolean isSend;
    private boolean isApprove;

    private boolean isCanEditPermission;
}
