package com.namtg.egovernment.entity.notification;

import com.namtg.egovernment.enum_common.TypeNotificationAdmin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(name = "notification_admin")
public class NotificationAdminEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userImpactId;
    private Long userReceiveId;

    @Enumerated(EnumType.STRING)
    private TypeNotificationAdmin type;
    private Long documentId;
    private Long commentId;
    private Long replyCommentId;
    private Long workUnitId; // dành cho loại notification nhận document

    private Long contentDocumentUpdateId; // dành cho loại notification thông báo cập nhật cho approver
    // đã có người cập nhật nội dung

    private boolean isRead;
    private boolean isWatched;
    private boolean isHidden;

    private Date createdTime;

    /* tên user tác động đến thông báo */
    @Transient
    private String nameUserImpact;

    @Transient
    private String urlAvatarUserImpact;

    @Transient
    private String titleDocument;

    @Transient
    private String timeNotification;

    @Transient
    private String nameWorkUnitDocument;

    public NotificationAdminEntity(Long userImpactId, Long userReceiveId, TypeNotificationAdmin type) {
        this.userImpactId = userImpactId;
        this.userReceiveId = userReceiveId;
        this.type = type;
        this.createdTime = new Date();
    }
}
