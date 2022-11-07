package com.namtg.egovernment.entity.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(name = "document_receive")
public class DocumentReceiveEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long workUnitId;
    private Long documentId;
    private boolean isReceived;

    private boolean isCanEditViewer;
    private boolean isCanEditUpdater;
    private boolean isCanEditApprover;
    private boolean isCanEditDeleter;
    private boolean isCanEditSender;

    private Date createdTime;
    private Long createdByUserId;

    public DocumentReceiveEntity(Long workUnitId, Long documentId, Long creatorId) {
        this.workUnitId = workUnitId;
        this.documentId = documentId;
        this.isReceived = false;
        this.createdTime = new Date();
        this.createdByUserId = creatorId;
    }
}
