package com.namtg.egovernment.service.notification;

import com.namtg.egovernment.dto.content_update_document.InfoContentDocumentChange;
import com.namtg.egovernment.dto.object_response.DataNotificationResponse;
import com.namtg.egovernment.dto.response.ResponseCase;
import com.namtg.egovernment.dto.response.ServerResponseDto;
import com.namtg.egovernment.entity.comment.CommentEntity;
import com.namtg.egovernment.entity.document.DocumentEntity;
import com.namtg.egovernment.entity.notification.NotificationAdminEntity;
import com.namtg.egovernment.entity.old_version_document.ContentDocumentUpdateEntity;
import com.namtg.egovernment.entity.reply_comment.ReplyCommentEntity;
import com.namtg.egovernment.entity.user.UserEntity;
import com.namtg.egovernment.enum_common.StatusComment;
import com.namtg.egovernment.enum_common.TypeNotificationAdmin;
import com.namtg.egovernment.repository.notification.NotificationAdminRepository;
import com.namtg.egovernment.service.comment.CommentService;
import com.namtg.egovernment.service.document.DocumentReceiveService;
import com.namtg.egovernment.service.document.DocumentService;
import com.namtg.egovernment.service.document.DocumentUserService;
import com.namtg.egovernment.service.old_version_document.ContentDocumentUpdateService;
import com.namtg.egovernment.service.reply_comment.ReplyCommentService;
import com.namtg.egovernment.service.unit.WorkUnitService;
import com.namtg.egovernment.service.unit.WorkUnitUserService;
import com.namtg.egovernment.service.user.UserService;
import com.namtg.egovernment.util.Constants;
import com.namtg.egovernment.util.TimeUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationAdminService {
    @Autowired
    private NotificationAdminRepository repository;

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ReplyCommentService replyCommentService;

    @Autowired
    private WorkUnitUserService workUnitUserService;

    @Autowired
    private DocumentReceiveService documentReceiveService;

    @Autowired
    private WorkUnitService workUnitService;

    @Autowired
    private ContentDocumentUpdateService contentDocumentUpdateService;

    @Autowired
    private DocumentUserService documentUserService;

    public void createNotificationSubmitComment(Long userCommentId,
                                                Long documentId,
                                                Long commentId,
                                                Long replyCommentId,
                                                TypeNotificationAdmin typeNotification) {

        List<Long> listApproverId = documentUserService.getListApproverIdByDocumentId(documentId);
        List<NotificationAdminEntity> listNoti = listApproverId
                .stream()
                .map(approverId -> {
                    NotificationAdminEntity n = new NotificationAdminEntity(userCommentId, approverId, typeNotification);
                    n.setDocumentId(documentId);
                    n.setCommentId(commentId);
                    n.setReplyCommentId(replyCommentId);
                    return n;
                })
                .collect(Collectors.toList());
        repository.saveAll(listNoti);
    }

    public List<NotificationAdminEntity> getListNotification(Long userReceiveId) {
        List<NotificationAdminEntity> listNotification = repository.getByUserReceiveId(userReceiveId);
        if (listNotification.isEmpty()) {
            return listNotification;
        }

        List<Long> listUserImpactId = listNotification
                .stream()
                .map(NotificationAdminEntity::getUserImpactId)
                .collect(Collectors.toList());
        List<UserEntity> listUserImpact = userService.getListUserByListUserId(listUserImpactId);
        Map<Long, String> mapNameUserByUserId = listUserImpact
                .stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getName));
        Map<Long, String> mapUrlAvatarByUserId = listUserImpact
                .stream()
                .collect(HashMap::new, (m, v) -> m.put(v.getId(), v.getUrlAvatar()), HashMap::putAll);

        List<Long> listCommentId = listNotification
                .stream()
                .map(NotificationAdminEntity::getCommentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, String> mapTitleDocumentByCommentId = commentService.getMapTitleDocumentByCommentId(listCommentId);

        List<Long> listReplyCommentId = listNotification
                .stream()
                .map(NotificationAdminEntity::getReplyCommentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, String> mapTitleDocumentByReplyCommentId = replyCommentService
                .getMapTitleDocumentByReplyCommentId(listReplyCommentId);

        List<Long> listDocumentId = listNotification
                .stream()
                .map(NotificationAdminEntity::getDocumentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, String> mapTitleDocumentByDocumentId = documentService.getMapTitleDocumentByDocumentId(listDocumentId);
        Map<Long, String> mapNameWorkUnitByDocumentId = documentService.getMapNameWorkUnitByDocumentId(listDocumentId);

        listNotification.forEach(notification -> {
            notification.setTimeNotification(TimeUtils.convertTimeToString(notification.getCreatedTime()));
            String titleDocument = null;
            if (notification.getReplyCommentId() != null) {
                titleDocument = mapTitleDocumentByReplyCommentId.get(notification.getReplyCommentId());
            } else if (notification.getCommentId() != null) {
                titleDocument = mapTitleDocumentByCommentId.get(notification.getCommentId());
            } else if (notification.getDocumentId() != null) {
                titleDocument = mapTitleDocumentByDocumentId.get(notification.getDocumentId());
                notification.setNameWorkUnitDocument(mapNameWorkUnitByDocumentId.get(notification.getDocumentId()));
            }
            notification.setTitleDocument(titleDocument);
            notification.setNameUserImpact(mapNameUserByUserId.get(notification.getUserImpactId()));
            notification.setUrlAvatarUserImpact(mapUrlAvatarByUserId.getOrDefault(notification.getUserImpactId(), Constants.TEXT_EMPTY));
        });
        return listNotification
                .stream()
                .sorted(Comparator.comparing(NotificationAdminEntity::getCreatedTime).reversed())
                .collect(Collectors.toList());
    }

    public int getNumberNotificationHavenWatch(Long userReceiveId) {
        return (int) repository.getNumberNotificationHavenWatch(userReceiveId);
    }

    public void setAllNotificationToWatched(Long userReceiveId) {
        List<NotificationAdminEntity> listNotificationAdmin = repository.getByUserReceiveId(userReceiveId);

        listNotificationAdmin.forEach(notification -> notification.setWatched(true));
        repository.saveAll(listNotificationAdmin);
    }

    public ServerResponseDto detail(Long notificationId) {
        NotificationAdminEntity notification = repository.getById(notificationId);
        if (notification == null) {
            return new ServerResponseDto(ResponseCase.ERROR);
        }
        /* set notification --> đã đọc */
        notification.setRead(true);
        repository.save(notification);

        DataNotificationResponse dataResponse = new DataNotificationResponse();
        TypeNotificationAdmin typeNotificationAdmin = notification.getType();
        dataResponse.setTypeNotificationAdmin(typeNotificationAdmin);

        /* case type COMMENT or REPLY_COMMENT */
        if (typeNotificationAdmin == TypeNotificationAdmin.COMMENT
                || typeNotificationAdmin == TypeNotificationAdmin.REPLY_COMMENT
                || typeNotificationAdmin == TypeNotificationAdmin.EDIT_COMMENT) {
            return responseForDetailComment(notification, dataResponse);

        } else if (typeNotificationAdmin == TypeNotificationAdmin.CREATE_POST) {
            dataResponse.setDocumentId(notification.getDocumentId());
            return ServerResponseDto.successWithData(dataResponse);

        } else if (typeNotificationAdmin == TypeNotificationAdmin.CONCLUSION_POST) {
            Long documentId = notification.getDocumentId();
            dataResponse.setSeoDocument(documentService.getSeoById(documentId));
            return ServerResponseDto.successWithData(dataResponse);

        } else if (typeNotificationAdmin == TypeNotificationAdmin.RECEIVE_DOCUMENT) {
            Long documentId = notification.getDocumentId();
            DocumentEntity document = documentService.getDocumentById(documentId);
            if (document == null) {
                return new ServerResponseDto(ResponseCase.DOCUMENT_NOT_FOUND);
            }
            document.setNameWorkUnit(workUnitService.getNameById(document.getWorkUnitId()));

            Long workUnitIdReceive = notification.getWorkUnitId();
            document.setWorkUnitIdReceive(workUnitIdReceive);
            document.setReceived(documentReceiveService.isReceivedDocument(documentId, workUnitIdReceive));
            if (document.getConclude() != null) {
                document.setConcludeParse(Jsoup.parse(document.getConclude()).text());
            }

            dataResponse.setDocument(document);

            return ServerResponseDto.successWithData(dataResponse);
        } else if (typeNotificationAdmin == TypeNotificationAdmin.INVITE_UPDATE_DOCUMENT) {
            Long documentId = notification.getDocumentId();
            DocumentEntity document = documentService.getDocumentById(documentId);
            if (document == null) {
                return new ServerResponseDto(ResponseCase.DOCUMENT_NOT_FOUND);
            }
            document.setNameWorkUnit(workUnitService.getNameById(document.getWorkUnitId()));
            if (document.getConclude() != null) {
                document.setConcludeParse(Jsoup.parse(document.getConclude()).text());
            }
            dataResponse.setDocument(document);
            return ServerResponseDto.successWithData(dataResponse);

        } else if (typeNotificationAdmin == TypeNotificationAdmin.REMIND_UPDATED_CONTENT_DOCUMENT) {
            Long contentDocumentUpdateId = notification.getContentDocumentUpdateId();
            ContentDocumentUpdateEntity contentDocumentUpdate = contentDocumentUpdateService.getById(contentDocumentUpdateId);
            if (contentDocumentUpdate == null) {
                return new ServerResponseDto(ResponseCase.CONTENT_UPDATE_NOT_FOUND);
            }
            DocumentEntity documentEntity = documentService.getDocumentById(contentDocumentUpdate.getDocumentId());
            if (documentEntity == null) {
                return new ServerResponseDto(ResponseCase.DOCUMENT_NOT_FOUND);
            }
            String nameUpdater = userService.getNameUserById(notification.getUserImpactId());
            dataResponse.setInfoContentDocumentChange(new InfoContentDocumentChange(
                    documentEntity.getId(),
                    documentEntity.getTitle(),
                    documentEntity.getContent(),
                    contentDocumentUpdate.getContent(),
                    nameUpdater));
            return ServerResponseDto.successWithData(dataResponse);
        }

        return new ServerResponseDto(ResponseCase.SUCCESS);
    }

    private ServerResponseDto responseForDetailComment(NotificationAdminEntity notification,
                                                       DataNotificationResponse dataResponse) {
        Long documentId = getDocumentIdByNotification(notification);
        Long commentId = notification.getCommentId();
        Long replyCommentId = notification.getReplyCommentId();
        DocumentEntity documentEntity = documentService.getDocumentById(documentId);
        dataResponse.setDocument(documentEntity);

        ReplyCommentEntity replyCommentEntity = null;
        Set<Long> listUserId = new HashSet<>();

        // get Reply Comment
        if (replyCommentId != null) {
            replyCommentEntity = replyCommentService.getById(replyCommentId);
            if (replyCommentEntity == null) {
                return new ServerResponseDto(ResponseCase.COMMENT_NOT_FOUND);
            }
            listUserId.add(replyCommentEntity.getUserId());
            listUserId.add(replyCommentEntity.getTagUserId());
            listUserId.add(replyCommentEntity.getUserConfirmId());

            replyCommentEntity.setTimeReplyComment(TimeUtils.convertTimeToString(replyCommentEntity.getUpdatedTime()));
            if (replyCommentEntity.getStatus() == StatusComment.DENIED) {
                String reasonDeniedReplyCommentContent = replyCommentService
                        .getReasonDeniedContent(
                                replyCommentEntity.getReasonDeniedId(), replyCommentEntity.getReasonDeniedOther());
                replyCommentEntity.setReasonDeniedContent(reasonDeniedReplyCommentContent);
            }
            commentId = replyCommentEntity.getCommentId();
        }

        // get Comment
        CommentEntity commentEntity = commentService.getById(commentId);
        if (commentEntity == null) {
            return new ServerResponseDto(ResponseCase.COMMENT_NOT_FOUND);
        }
        listUserId.add(commentEntity.getUserId());
        listUserId.add(commentEntity.getUserConfirmId());

        List<UserEntity> listUser = userService.getListUserByListUserId(listUserId);
        Map<Long, String> mapNameUserByUserId = userService.getMapNameUserByUserId(listUser);
        Map<Long, String> mapUrlAvatarUserByUserId = userService.getMapUrlAvatarUserByUserId(listUser);

        if (replyCommentEntity != null) {
            replyCommentEntity.setNameUser(mapNameUserByUserId.get(replyCommentEntity.getUserId()));
            replyCommentEntity.setUrlAvatarUser(mapUrlAvatarUserByUserId.get(replyCommentEntity.getUserId()));
            replyCommentEntity.setNameUserTag(mapNameUserByUserId.get(replyCommentEntity.getTagUserId()));
            replyCommentEntity.setNameUserConfirm(mapNameUserByUserId.get(replyCommentEntity.getUserConfirmId()));
            dataResponse.setReplyComment(replyCommentEntity);
        }

        commentEntity.setNameUserComment(mapNameUserByUserId.get(commentEntity.getUserId()));
        commentEntity.setUrlAvatarUserComment(mapUrlAvatarUserByUserId.get(commentEntity.getUserId()));
        commentEntity.setNameUserConfirm(mapNameUserByUserId.get(commentEntity.getUserConfirmId()));
        commentEntity.setTimeComment(TimeUtils.convertTimeToString(commentEntity.getUpdatedTime()));
        if (commentEntity.getStatus() == StatusComment.DENIED) {
            String reasonDeniedCommentContent = commentService
                    .getReasonDeniedContent(commentEntity.getReasonDeniedId(), commentEntity.getReasonDeniedOther());
            commentEntity.setReasonDeniedContent(reasonDeniedCommentContent);
        }
        dataResponse.setComment(commentEntity);

        return ServerResponseDto.successWithData(dataResponse);
    }

    private Long getDocumentIdByNotification(NotificationAdminEntity notification) {
        Long replyCommentId = notification.getReplyCommentId();
        Long commentId = notification.getCommentId();

        if (replyCommentId != null) {
            return documentService.getDocumentIdByReplyCommentId(replyCommentId);
        } else {
            return documentService.getDocumentIdByCommentId(commentId);
        }
    }

    @Transactional
    public void createNotifyReceiveDocument(Long userImpactId,
                                            Long documentId,
                                            List<Long> listWorkUnitIdReceiveDocument) {
        Map<Long, List<Long>> mapListUserIdReceiveByWorkUnitId = workUnitUserService
                .getMapListUserIdReceiveDocumentByWorkUnitId(listWorkUnitIdReceiveDocument);

        List<NotificationAdminEntity> listNotificationAdmin = new ArrayList<>();
        mapListUserIdReceiveByWorkUnitId.keySet().forEach(workUnitIdReceive -> {
            List<NotificationAdminEntity> list = mapListUserIdReceiveByWorkUnitId.get(workUnitIdReceive).stream()
                    .map(userReceiveId -> {
                        NotificationAdminEntity n = new NotificationAdminEntity(
                                userImpactId, userReceiveId, TypeNotificationAdmin.RECEIVE_DOCUMENT);
                        n.setDocumentId(documentId);
                        n.setWorkUnitId(workUnitIdReceive);
                        return n;
                    })
                    .collect(Collectors.toList());
            listNotificationAdmin.addAll(list);
        });

        repository.saveAll(listNotificationAdmin);
    }

    public void createNotificationInviteUpdateDocument(Set<Long> listUpdaterId, Long ownerId, Long documentId) {
        Set<Long> listUpdaterIdExist = new HashSet<>(repository.getByDocumentIdAndTypeInviteUpdateDocument(documentId));
        List<NotificationAdminEntity> listNotificationInviteUpdateDocument = listUpdaterId
                .stream()
                .filter(userReceiveId -> !listUpdaterIdExist.contains(userReceiveId) && !Objects.equals(userReceiveId, ownerId))
                .map(userReceiveId -> {
                    NotificationAdminEntity n = new NotificationAdminEntity(
                            null, userReceiveId, TypeNotificationAdmin.INVITE_UPDATE_DOCUMENT);
                    n.setDocumentId(documentId);
                    return n;
                })
                .collect(Collectors.toList());
        repository.saveAll(listNotificationInviteUpdateDocument);
    }

    public void createNotificationRemindUpdatedContentDocument(List<Long> listApproverId,
                                                               Long contentUpdateDocumentId,
                                                               Long documentId,
                                                               Long userUpdateId) {
        listApproverId.remove(userUpdateId); // không tạo noti cho updater đồng thời là approver
        List<NotificationAdminEntity> listNotiNeedSave = listApproverId
                .stream()
                .map(userReceiveId -> {
                    NotificationAdminEntity n = new NotificationAdminEntity(
                            userUpdateId, userReceiveId, TypeNotificationAdmin.REMIND_UPDATED_CONTENT_DOCUMENT);
                    n.setContentDocumentUpdateId(contentUpdateDocumentId);
                    n.setDocumentId(documentId);
                    return n;
                })
                .collect(Collectors.toList());
        repository.saveAll(listNotiNeedSave);
    }
}
