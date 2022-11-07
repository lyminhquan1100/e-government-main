package com.namtg.egovernment.entity.document;

import com.namtg.egovernment.dto.document.PermissionDocumentDto;
import com.namtg.egovernment.dto.user.IdAndName;
import com.namtg.egovernment.entity.comment.CommentEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(name = "document")
public class DocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long workUnitId;
    private float version;
    private String title;
    private String content;
    private String target;
    private String conclude;
    private int numberView;
    private String seo;
    private boolean isEnable;
    private boolean isPublic;

    private boolean isDeleted;
    private Long createdByUserId;
    private Long updatedByUserId;
    //    @JsonFormat(pattern = "hh:mm dd/MM/yyyy")
    private Date createdTime;
    private Date updatedTime;

    @Transient
    private boolean isReceived;

    @Transient
    private List<CommentEntity> listComment;

    @Transient
    private Long numberComment;

    @Transient
    private String concludeParse;

    @Transient
    private String nameWorkUnit;

    @Transient
    private String nameCreator;

    @Transient
    private List<IdAndName> listMemberCanTakePartIn;
    @Transient
    private List<Long> listViewerId;
    @Transient
    private List<Long> listUpdaterId;
    @Transient
    private List<Long> listApproverId;
    @Transient
    private List<Long> listDeleterId;
    @Transient
    private List<Long> listSenderId;

    @Transient
    private Long workUnitIdReceive;

    @Transient
    private PermissionDocumentDto permissionDocument; // dành cho detail

    public DocumentEntity(Long workUnitId, float version, String title, String content, String target, String conclude,
                          String seo) {
        this.workUnitId = workUnitId;
        this.version = version;
        this.title = title;
        this.content = content;
        this.target = target;
        this.conclude = conclude;
        this.numberView = 0;
        this.seo = seo;
        this.isDeleted = false;
        this.createdTime = new Date();
        this.updatedTime = new Date();
    }
}
