package com.namtg.egovernment.service.document;

import com.github.slugify.Slugify;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.namtg.egovernment.dto.document.*;
import com.namtg.egovernment.dto.response.ResponseCase;
import com.namtg.egovernment.dto.response.ServerResponseDto;
import com.namtg.egovernment.dto.user.IdAndName;
import com.namtg.egovernment.entity.comment.CommentEntity;
import com.namtg.egovernment.entity.document.DocumentEntity;
import com.namtg.egovernment.entity.document.DocumentReceiveEntity;
import com.namtg.egovernment.entity.document.DocumentUserEntity;
import com.namtg.egovernment.entity.old_version_document.ContentDocumentUpdateEntity;
import com.namtg.egovernment.repository.document.DocumentRepository;
import com.namtg.egovernment.service.comment.CommentService;
import com.namtg.egovernment.service.old_version_document.ContentDocumentUpdateService;
import com.namtg.egovernment.service.old_version_document.OldVersionDocumentService;
import com.namtg.egovernment.service.unit.WorkUnitService;
import com.namtg.egovernment.service.unit.WorkUnitUserService;
import com.namtg.egovernment.service.user.UserService;
import com.namtg.egovernment.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocumentService {
    @Autowired
    private CommentService commentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ContentDocumentUpdateService contentDocumentUpdateService;

    @Autowired
    private DocumentUserService documentUserService;

    @Autowired
    private WorkUnitService workUnitService;

    @Autowired
    private OldVersionDocumentService oldVersionDocumentService;

    @Autowired
    private DocumentReceiveService documentReceiveService;

    @Autowired
    private WorkUnitUserService workUnitUserService;

    public static final Predicate<DocumentUserEntity> isViewer = du -> du.isOwner() || du.isView();
    public static final Predicate<DocumentUserEntity> isUpdater = du -> du.isOwner() || du.isUpdate();
    public static final Predicate<DocumentUserEntity> isDeleter = du -> du.isOwner() || du.isDelete();
    public static final Predicate<DocumentUserEntity> isAppover = du -> du.isOwner() || du.isApprove();
    public static final Predicate<DocumentUserEntity> isSender = du -> du.isOwner() || du.isSend();


    public Page<DocumentEntity> getPage(String keyword, Long currentUserId, Pageable pageable) {
        Page<DocumentEntity> pageResult = documentRepository.getPage(keyword, currentUserId, pageable);
        List<Long> listDocumentId = pageResult
                .stream()
                .map(DocumentEntity::getId)
                .collect(Collectors.toList());
        Map<Long, List<DocumentUserEntity>> mapListDocumentUserRelateByDocumentId = documentUserService
                .getMapListDocumentUserRelateByDocumentId(listDocumentId);

        Map<Long, List<Long>> mapListViewerIdByDocumentId = documentUserService
                .getMapListUserIdByDocumentIdWithPredicate(listDocumentId, mapListDocumentUserRelateByDocumentId, isViewer);
        Map<Long, List<Long>> mapListUpdaterIdByDocumentId = documentUserService
                .getMapListUserIdByDocumentIdWithPredicate(listDocumentId, mapListDocumentUserRelateByDocumentId, isUpdater);
        Map<Long, List<Long>> mapListApproverIdByDocumentId = documentUserService
                .getMapListUserIdByDocumentIdWithPredicate(listDocumentId, mapListDocumentUserRelateByDocumentId, isAppover);
        Map<Long, List<Long>> mapListDeleterIdByDocumentId = documentUserService
                .getMapListUserIdByDocumentIdWithPredicate(listDocumentId, mapListDocumentUserRelateByDocumentId, isDeleter);
        Map<Long, List<Long>> mapListSenderIdByDocumentId = documentUserService
                .getMapListUserIdByDocumentIdWithPredicate(listDocumentId, mapListDocumentUserRelateByDocumentId, isSender);
        List<Long> listWorkUnitId = pageResult
                .stream()
                .map(DocumentEntity::getWorkUnitId)
                .collect(Collectors.toList());
        Map<Long, String> mapNameWorkUnitById = workUnitService.getMapNameWorkUnitByWorkUnitId(listWorkUnitId);
        List<Long> listCreatorId = pageResult
                .stream()
                .map(DocumentEntity::getCreatedByUserId)
                .collect(Collectors.toList());
        Map<Long, String> mapNameCreatorById = userService.getMapNameUserByUserId(listCreatorId);
        pageResult.forEach(document -> {
            document.setNameWorkUnit(mapNameWorkUnitById.get(document.getWorkUnitId()));
            document.setNameCreator(mapNameCreatorById.get(document.getCreatedByUserId()));
            document.setListViewerId(mapListViewerIdByDocumentId.get(document.getId()));
            document.setListUpdaterId(mapListUpdaterIdByDocumentId.get(document.getId()));
            document.setListDeleterId(mapListDeleterIdByDocumentId.get(document.getId()));
            document.setListApproverId(mapListApproverIdByDocumentId.get(document.getId()));
            document.setListSenderId(mapListSenderIdByDocumentId.get(document.getId()));
        });
        return pageResult;
    }

    @Transactional
    public ServerResponseDto save(Long creatorId, DocumentDto documentDto) throws ParseException {
        Long id = documentDto.getId();
        boolean isUpdate = id != null;

        DocumentEntity documentEntity;
        boolean isDocumentExist = isDocumentExist(isUpdate, documentDto.getId(), documentDto.getWorkUnitId(), documentDto.getTitle());
        if (isDocumentExist) {
            log.info("Document is exist with title: {}", documentDto.getTitle());
            return new ServerResponseDto(ResponseCase.DOCUMENT_EXIST);
        }

        if (isUpdate) {
            documentEntity = documentRepository.findByIdAndIsDeletedFalse(id);
            documentEntity.setUpdatedByUserId(creatorId);
            documentEntity.setUpdatedTime(new Date());
        } else {
            documentEntity = new DocumentEntity();
            documentEntity.setNumberView(0);
            documentEntity.setCreatedByUserId(creatorId);
            documentEntity.setCreatedTime(new Date());
            documentEntity.setEnable(true);
        }
        documentEntity.setWorkUnitId(documentDto.getWorkUnitId());
        documentEntity.setVersion(documentDto.getVersion());
        documentEntity.setTitle(documentDto.getTitle());
        documentEntity.setSeo(new Slugify().slugify(documentDto.getTitle()));
        documentEntity.setContent(documentDto.getContent());
        documentEntity.setTarget(documentDto.getTarget());
        documentRepository.save(documentEntity);

        documentUserService.save(documentEntity.getId(), documentEntity.getCreatedByUserId(),
                documentDto.getListViewerId(),
                documentDto.getListUpdaterId(),
                documentDto.getListApproverId(),
                documentDto.getListDeleterId(),
                documentDto.getListSenderId());

        return new ServerResponseDto(ResponseCase.SUCCESS);
    }

    private boolean isDocumentExist(boolean isUpdateDocument, Long documentId, Long workUnitId, String title) {
        return isUpdateDocument ?
                documentRepository.countDocumentExist(title, documentId, workUnitId) > 0 :
                documentRepository.countDocumentExist(title, workUnitId) > 0;
    }

    public ServerResponseDto detail(Long documentId, Long currentUserId) {
        DocumentEntity documentEntity = documentRepository.findByIdAndIsDeletedFalse(documentId);
        if (documentEntity == null) {
            log.info("documentEntity is null with id: {}", documentId);
            return new ServerResponseDto(ResponseCase.ERROR);
        }
        Long workUnitIdOfDocument = documentEntity.getWorkUnitId();
        documentEntity.setNameWorkUnit(workUnitService.getNameById(workUnitIdOfDocument));
        if (documentEntity.getConclude() != null) {
            documentEntity.setConcludeParse(Jsoup.parse(documentEntity.getConclude()).text());
        }
        List<IdAndName> listUserCanTakePartIn = userService.getListUserCanTakePartInDocument(workUnitIdOfDocument)
                .stream()
                .map(obj -> new IdAndName(obj.getId(), obj.getName()))
                .collect(Collectors.toList());
        Set<Long> setUserIdCanTakePartIn = listUserCanTakePartIn.stream()
                .map(IdAndName::getId)
                .collect(Collectors.toSet());

        List<DocumentUserEntity> listDocumentUserRelateDocument = documentUserService.getListDocumentUserByDocumentId(documentId);
        List<Long> listViewerId = listDocumentUserRelateDocument
                .stream()
                .filter(du -> du.isView() || du.isOwner())
                .map(DocumentUserEntity::getUserId)
                .collect(Collectors.toList());
        documentEntity.setListViewerId(listViewerId);
        List<Long> listUpdaterId = listDocumentUserRelateDocument
                .stream()
                .filter(du -> du.isUpdate() || du.isOwner())
                .map(DocumentUserEntity::getUserId)
                .collect(Collectors.toList());
        documentEntity.setListUpdaterId(listUpdaterId);
        List<Long> listDeleterId = listDocumentUserRelateDocument
                .stream()
                .filter(du -> du.isDelete() || du.isOwner())
                .map(DocumentUserEntity::getUserId)
                .collect(Collectors.toList());
        documentEntity.setListDeleterId(listDeleterId);
        List<Long> listApproverId = listDocumentUserRelateDocument
                .stream()
                .filter(du -> du.isApprove() || du.isOwner())
                .map(DocumentUserEntity::getUserId)
                .collect(Collectors.toList());
        documentEntity.setListApproverId(listApproverId);
        List<Long> listSenderId = listDocumentUserRelateDocument
                .stream()
                .filter(du -> du.isSend() || du.isOwner())
                .map(DocumentUserEntity::getUserId)
                .collect(Collectors.toList());
        documentEntity.setListSenderId(listSenderId);

        // thêm những userId ở ngoài workUnit trong trường hợp gửi văn bản
        List<Long> listWorkUnitIdReceiveThisDocument = documentReceiveService
                .getListWorkUnitIdReceiveThisDocument(documentId);
        List<Long> listUserIdAddedInCanTakePartIn = workUnitUserService
                .getListUserIdByListWorkUnitId(listWorkUnitIdReceiveThisDocument)
                .stream()
                .filter(userId -> !setUserIdCanTakePartIn.contains(userId))
                .collect(Collectors.toList());

        List<IdAndName> listAddedInCanTakePartIn = userService.getObjectIdAndNameByListId(listUserIdAddedInCanTakePartIn);
        listUserCanTakePartIn.addAll(listAddedInCanTakePartIn);
        documentEntity.setListMemberCanTakePartIn(listUserCanTakePartIn);
        //

        DocumentUserEntity documentUserEntity = documentUserService.getByDocumentIdAndUserId(documentId, currentUserId);
        if (documentUserEntity.isOwner()) {
            documentEntity.setPermissionDocument(PermissionDocumentDto.permissionDocumentWithIsOwner);
        } else if (documentUserEntity.isCanEditPermission()) {
            List<Long> listWorkUnitId = workUnitUserService.getListWorkUnitId(currentUserId);
            List<DocumentReceiveEntity> listDocumentReceived = documentReceiveService
                    .getReceivedByListWorkUnitIdAndDocumentId(listWorkUnitId, documentId);
            boolean isCanEditViewer = listDocumentReceived
                    .stream()
                    .anyMatch(DocumentReceiveEntity::isCanEditViewer);
            boolean isCanEditUpdater = listDocumentReceived
                    .stream()
                    .anyMatch(DocumentReceiveEntity::isCanEditUpdater);
            boolean isCanEditApprover = listDocumentReceived
                    .stream()
                    .anyMatch(DocumentReceiveEntity::isCanEditApprover);
            boolean isCanEditDeleter = listDocumentReceived
                    .stream()
                    .anyMatch(DocumentReceiveEntity::isCanEditDeleter);
            boolean isCanEditSender = listDocumentReceived
                    .stream()
                    .anyMatch(DocumentReceiveEntity::isCanEditSender);
            documentEntity.setPermissionDocument(new PermissionDocumentDto(
                    false, true, isCanEditViewer, isCanEditUpdater, isCanEditApprover, isCanEditDeleter, isCanEditSender));
        } else {
            documentEntity.setPermissionDocument(PermissionDocumentDto.permissionDocumentWithNotPermission);
        }
        return new ServerResponseDto(ResponseCase.SUCCESS, documentEntity);
    }

    @Transactional
    public ServerResponseDto delete(Long documentId) {
        DocumentEntity documentEntity = documentRepository.findByIdAndIsDeletedFalse(documentId);
        if (documentEntity == null) {
            log.info("documentEntity is null with id: {} when delete", documentId);
            return new ServerResponseDto(ResponseCase.ERROR);
        }
        documentEntity.setDeleted(true);
        documentRepository.save(documentEntity);
        documentUserService.deleteByDocumentId(documentId);
        return new ServerResponseDto(ResponseCase.SUCCESS);
    }

    public Page<DocumentEntity> getPageForUser(Long currentUserId, Long workUnitId, String keyword, String orderBy,
                                               Pageable pageable) {
        List<DocumentEntity> listResult = documentRepository.getListForUser(keyword, currentUserId);

        // default orderBy lasted
        Comparator<DocumentEntity> comparator = Comparator.comparing(DocumentEntity::getCreatedTime).reversed();
        if ("view".equals(orderBy)) {
            comparator = Comparator.comparing(DocumentEntity::getNumberView).reversed();
        }

        listResult = listResult.stream()
                .filter(d -> workUnitId == 0L || Objects.equals(d.getWorkUnitId(), workUnitId))
                .sorted(comparator)
                .collect(Collectors.toList());

        List<Long> listDocumentId = listResult
                .stream()
                .map(DocumentEntity::getId)
                .collect(Collectors.toList());
        Map<Long, Long> mapNumberCommentByPostId = commentService.getMapNumberCommentByDocumentId(listDocumentId);

        List<Long> listCreatorId = listResult
                .stream()
                .map(DocumentEntity::getCreatedByUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, String> mapCreatorByDocumentId = userService.getMapNameUserByUserId(listCreatorId);

        listResult.forEach(document -> {
            document.setNameCreator(mapCreatorByDocumentId.get(document.getCreatedByUserId()));
            document.setNumberComment(mapNumberCommentByPostId.getOrDefault(document.getId(), 0L));
        });

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), listResult.size());

        return new PageImpl<>(listResult.subList(start, end), pageable, listResult.size());
    }

    public DocumentEntity findBySeoAndIsDeletedFalse(String seo) {
        return documentRepository.findBySeoAndIsDeletedFalse(seo);
    }

    public DocumentEntity detailForAdmin(String seo) {
        DocumentEntity documentEntity = documentRepository.findBySeoAndIsDeletedFalse(seo);
        List<CommentEntity> listCommentApproved = commentService.getListCommentApprovedByDocumentId(documentEntity.getId());
        documentEntity.setListComment(listCommentApproved);
        return documentEntity;
    }

    public ServerResponseDto detailForUser(Long documentId, Long currentUserId) {
        DocumentEntity documentEntity = documentRepository.findByIdAndIsDeletedFalse(documentId);
        if (documentEntity == null) {
            return new ServerResponseDto(ResponseCase.ERROR);
        }

        List<CommentEntity> listComment = commentService.getListCommentByDocumentId(documentEntity.getId(), currentUserId);
        documentEntity.setListComment(listComment);

        return new ServerResponseDto(ResponseCase.SUCCESS, documentEntity);
    }

    public DocumentEntity getDocumentLatest(Long currentUserId) {
        return documentRepository.getDocumentLatest(currentUserId);
    }

    public ServerResponseDto plusView(Long postId) {
        DocumentEntity documentEntity = documentRepository.findByIdAndIsDeletedFalse(postId);
        if (documentEntity == null) {
            return new ServerResponseDto(ResponseCase.ERROR);
        }
        documentEntity.setNumberView(documentEntity.getNumberView() + 1);
        documentRepository.save(documentEntity);

        return new ServerResponseDto(ResponseCase.SUCCESS);
    }

    public Map<Long, String> getMapTitleDocumentByDocumentId(Collection<Long> listDocumentId) {
        if (listDocumentId.isEmpty()) {
            return Collections.emptyMap();
        }
        return documentRepository.findByIdIn(listDocumentId)
                .stream()
                .collect(Collectors.toMap(DocumentEntity::getId, DocumentEntity::getTitle));
    }

    public DocumentEntity getDocumentById(Long documentId) {
        return documentRepository.findByIdAndIsDeletedFalse(documentId);
    }

    public Long getDocumentIdByReplyCommentId(Long replyCommentId) {
        if (replyCommentId == null) {
            return null;
        }
        return documentRepository.getDocumentIdByReplyCommentId(replyCommentId);
    }

    public Long getDocumentIdByCommentId(Long commentId) {
        if (commentId == null) {
            return null;
        }
        return commentService.getDocumentIdByCommentId(commentId);
    }

    public ServerResponseDto saveConclusion(ConclusionDto conclusionDto) {
        DocumentEntity documentEntity = documentRepository.findByIdAndIsDeletedFalse(conclusionDto.getPostId());
        documentEntity.setConclude(conclusionDto.getConclusion());
        documentRepository.save(documentEntity);
        return new ServerResponseDto(ResponseCase.SUCCESS);
    }

    public List<DocumentEntity> getListTop5Document(Long currentUserId) {
        final int LENGTH_LIMIT = 5;
        List<DocumentEntity> listDocument = documentRepository.getListForUser(Constants.TEXT_EMPTY, currentUserId);
        return listDocument.subList(0, LENGTH_LIMIT < listDocument.size() ? LENGTH_LIMIT : listDocument.size());
    }

    public String getSeoById(Long postId) {
        return documentRepository.findByIdAndIsDeletedFalse(postId).getSeo();
    }

    public String getContent(Long documentId) {
        return documentRepository.getContentById(documentId);
    }

    public ServerResponseDto getDataForApproveDocument(Long documentId) {
        DocumentEntity documentEntity = documentRepository.findByIdAndIsDeletedFalse(documentId);
        float versionCurrent = documentEntity.getVersion();

        List<ContentDocumentUpdateEntity> listUpdateForVersionCurrent = contentDocumentUpdateService
                .getListUpdateForVersionCurrent(documentId, versionCurrent);
        if (listUpdateForVersionCurrent.isEmpty()) {
            return ServerResponseDto.successWithData(
                    new ResponseDataForApproveDocument(versionCurrent, documentEntity.getContent(), Collections.emptyList()));
        }

        List<Long> listUpdaterId = listUpdateForVersionCurrent
                .stream()
                .map(ContentDocumentUpdateEntity::getUserIdUpdate)
                .collect(Collectors.toList());
        Map<Long, String> mapNameUserByUserId = userService.getMapNameUserByUserId(listUpdaterId);

        List<ContentUpdateAndNameUpdater> listUpdate = listUpdateForVersionCurrent
                .stream()
                .map(object -> new ContentUpdateAndNameUpdater(
                        mapNameUserByUserId.getOrDefault(object.getUserIdUpdate(), ""), object.getContent()))
                .collect(Collectors.toList());
        return ServerResponseDto
                .successWithData(new ResponseDataForApproveDocument(versionCurrent, documentEntity.getContent(), listUpdate));
    }

    @Transactional
    public ServerResponseDto saveApproveContent(SaveApproveDocument saveApproveDocument, Long currentUserId) {
        if (saveApproveDocument == null) {
            log.info("saveApproveDocument is null when saveApproveContent");
            return ServerResponseDto.ERROR;
        }
        Long documentId = saveApproveDocument.getDocumentId();
        if (documentId == null) {
            log.info("documentId is null when saveApproveContent");
            return ServerResponseDto.ERROR;
        }
        DocumentEntity documentEntity = documentRepository.findByIdAndIsDeletedFalse(documentId);
        if (documentEntity == null) {
            log.info("Not found document with documentId: {} when saveApproveContent", documentId);
            return ServerResponseDto.ERROR;
        }

        float oldVersion = documentEntity.getVersion();
        float newVersion = saveApproveDocument.getVersionNew();

        if (oldVersion == newVersion) {
            // xóa các bản chỉnh sửa
            contentDocumentUpdateService.deleteByDocumentIdAndVersion(documentId, oldVersion);
        } else {
            // save old version
            oldVersionDocumentService.saveOldVersion(currentUserId,
                    documentId,
                    oldVersion,
                    documentEntity.getContent());
        }

        documentEntity.setVersion(newVersion);
        documentEntity.setContent(saveApproveDocument.getContentNew());
        documentRepository.save(documentEntity);
        return ServerResponseDto.SUCCESS;
    }

    @Transactional
    public ServerResponseDto sendDocument(SendDocumentDto sendDocumentDto, Long userImpactId) {
        List<WorkUnitIdReceiveAndPermission> listWorkUnitIdReceiveAndPermission =
                sendDocumentDto.getListWorkUnitIdReceiveAndPermission();

        Long documentId = sendDocumentDto.getDocumentId();
        if (documentId == null || listWorkUnitIdReceiveAndPermission.isEmpty()) {
            return ServerResponseDto.ERROR;
        }
        documentReceiveService.saveDocumentReceive(documentId, listWorkUnitIdReceiveAndPermission, userImpactId);
        return ServerResponseDto.SUCCESS;
    }

    public Map<Long, String> getMapNameWorkUnitByDocumentId(List<Long> listDocumentId) {
        if (listDocumentId.isEmpty()) {
            return Collections.emptyMap();
        }
        List<DocumentEntity> listDocument = documentRepository.findByIdInAndIsDeletedFalse(listDocumentId);
        List<Long> listWorkUnitId = listDocument
                .stream()
                .map(DocumentEntity::getWorkUnitId)
                .collect(Collectors.toList());
        Map<Long, String> mapNameWorkUnitByWorkUnitId = workUnitService.getMapNameWorkUnitByWorkUnitId(listWorkUnitId);
        Map<Long, Long> mapWorkUnitIdByDocumentId = listDocument
                .stream()
                .collect(Collectors.toMap(DocumentEntity::getId, DocumentEntity::getWorkUnitId));

        Map<Long, String> mapResult = Maps.newHashMapWithExpectedSize(listDocumentId.size());
        listDocumentId.forEach(documentId ->
                mapResult.put(documentId, mapNameWorkUnitByWorkUnitId.getOrDefault(
                        mapWorkUnitIdByDocumentId.getOrDefault(documentId, 0L), Constants.TEXT_EMPTY)));
        return mapResult;
    }

    @Transactional
    public ServerResponseDto receiveDocument(Long documentId, Long workUnitIdReceive) {
        // set isReceive --> true
        DocumentReceiveEntity documentReceiveEntity = documentReceiveService.receiveDocument(documentId, workUnitIdReceive);
        boolean isCanEditViewer = documentReceiveEntity.isCanEditViewer();
        boolean isCanEditUpdater = documentReceiveEntity.isCanEditUpdater();
        boolean isCanEditApprover = documentReceiveEntity.isCanEditApprover();
        boolean isCanEditDeleter = documentReceiveEntity.isCanEditDeleter();
        boolean isCanEditSender = documentReceiveEntity.isCanEditSender();

        // set những user có quyền nhận văn bản --> có các quyền tương ứng với các permission của DocumentReceiveEntity
        List<Long> listUserIdCanReceiveDocument = workUnitUserService.getListUserIdCanReceiveDocument(List.of(workUnitIdReceive));
        Map<Long, DocumentUserEntity> mapDocumentUserByUserId = documentUserService.getListDocumentUserByDocumentId(documentId)
                .stream()
                .collect(Collectors.toMap(DocumentUserEntity::getUserId, Function.identity()));

        List<DocumentUserEntity> listNeedSave = Lists.newArrayListWithExpectedSize(listUserIdCanReceiveDocument.size());
        listUserIdCanReceiveDocument.forEach(userId -> {
            DocumentUserEntity documentUser = mapDocumentUserByUserId.getOrDefault(userId, new DocumentUserEntity());
            documentUser.setDocumentId(documentId);
            documentUser.setUserId(userId);
            if (isCanEditViewer) {
                documentUser.setView(true);
            }
            if (isCanEditUpdater) {
                documentUser.setUpdate(true);
            }
            if (isCanEditApprover) {
                documentUser.setApprove(true);
            }
            if (isCanEditDeleter) {
                documentUser.setDelete(true);
            }
            if (isCanEditSender) {
                documentUser.setSend(true);
            }
            documentUser.setCanEditPermission(true);
            listNeedSave.add(documentUser);
        });
        documentUserService.saveList(listNeedSave);
        return ServerResponseDto.SUCCESS;
    }

    public ServerResponseDto changeEnable(Long documentId) {
        DocumentEntity documentEntity = documentRepository.findByIdAndIsDeletedFalse(documentId);
        if (documentEntity == null) {
            return ServerResponseDto.ERROR;
        }
        documentEntity.setEnable(!documentEntity.isEnable());
        documentRepository.save(documentEntity);
        return ServerResponseDto.SUCCESS;
    }

    public ServerResponseDto publicDocument(Long documentId) {
        DocumentEntity documentEntity = documentRepository.findByIdAndIsDeletedFalse(documentId);
        if (documentEntity == null) {
            return ServerResponseDto.ERROR;
        }
        documentEntity.setPublic(!documentEntity.isPublic());
        documentRepository.save(documentEntity);
        return ServerResponseDto.SUCCESS;
    }
}
