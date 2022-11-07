package com.namtg.egovernment.entity.old_version_document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "content_document_update")
@NoArgsConstructor
public class ContentDocumentUpdateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userIdUpdate;
    private Long documentId;
    private float version;
    private String content;

    private boolean isDeleted;
    private Date createdTime;
    private Date updatedTime;

    public ContentDocumentUpdateEntity(Long userIdUpdate, Long documentId, float version, String content) {
        this.userIdUpdate = userIdUpdate;
        this.documentId = documentId;
        this.version = version;
        this.content = content;
        this.createdTime = new Date();
        this.updatedTime = new Date();
    }
}
