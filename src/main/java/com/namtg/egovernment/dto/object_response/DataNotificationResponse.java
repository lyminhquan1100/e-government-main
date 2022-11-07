package com.namtg.egovernment.dto.object_response;

import com.namtg.egovernment.dto.content_update_document.InfoContentDocumentChange;
import com.namtg.egovernment.entity.comment.CommentEntity;
import com.namtg.egovernment.entity.document.DocumentEntity;
import com.namtg.egovernment.entity.reply_comment.ReplyCommentEntity;
import com.namtg.egovernment.enum_common.TypeNotificationAdmin;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DataNotificationResponse {
    private TypeNotificationAdmin typeNotificationAdmin;

    private DocumentEntity document;
    private CommentEntity comment;
    private ReplyCommentEntity replyComment;
    private Long documentId;
    private String seoDocument;
    private InfoContentDocumentChange infoContentDocumentChange; // case: thông báo nhắc đã chỉnh sửa nội dung văn bản
}
