package com.namtg.egovernment.entity.old_version_document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "old_version_document")
@NoArgsConstructor
public class OldVersionDocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userIdApprove;
    private Long documentId;
    private float version;
    private String content;

    private boolean isDeleted;
    private Date createdTime;
    private Date updatedTime;

    public OldVersionDocumentEntity(Long userIdApprove, Long documentId, float version, String content) {
        this.userIdApprove = userIdApprove;
        this.documentId = documentId;
        this.version = version;
        this.content = content;
        this.createdTime = new Date();
        this.updatedTime = new Date();
    }
}
