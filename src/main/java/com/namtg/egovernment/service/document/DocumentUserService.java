package com.namtg.egovernment.service.document;

import com.google.common.collect.Lists;
import com.namtg.egovernment.entity.document.DocumentUserEntity;
import com.namtg.egovernment.entity.user.UserEntity;
import com.namtg.egovernment.repository.document.DocumentUserRepository;
import com.namtg.egovernment.service.notification.NotificationAdminService;
import com.namtg.egovernment.service.unit.WorkUnitUserService;
import com.namtg.egovernment.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DocumentUserService {
    @Autowired
    private DocumentUserRepository documentUserRepository;

    @Autowired
    private NotificationAdminService notificationAdminService;

    @Autowired
    private WorkUnitUserService workUnitUserService;

    @Transactional
    public void save(Long documentId, Long ownerId,
                     String listViewerIdStr, String listUpdaterIdStr,
                     String listApproverIdStr, String listDeleterIdStr, String listSenderIdStr) {
        Set<Long> listViewerId = CollectionUtils.convertListIdStrToSetLong(listViewerIdStr);
        Set<Long> listUpdaterId = CollectionUtils.convertListIdStrToSetLong(listUpdaterIdStr);
        notificationAdminService.createNotificationInviteUpdateDocument(listUpdaterId, ownerId, documentId);
        Set<Long> listApproverId = CollectionUtils.convertListIdStrToSetLong(listApproverIdStr);
        Set<Long> listDeleterId = CollectionUtils.convertListIdStrToSetLong(listDeleterIdStr);
        Set<Long> listSenderId = CollectionUtils.convertListIdStrToSetLong(listSenderIdStr);

        Set<Long> setAllUserId = Stream.of(listViewerId, listUpdaterId, listApproverId, listDeleterId, listSenderId)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        setAllUserId.add(ownerId);

        List<DocumentUserEntity> listInDb = documentUserRepository.findByDocumentId(documentId);
        Set<Long> setUserIdInDb = listInDb.stream().map(DocumentUserEntity::getUserId).collect(Collectors.toSet());
        List<Long> listUserIdDelete = setUserIdInDb
                .stream()
                .filter(userId -> !setAllUserId.contains(userId))
                .collect(Collectors.toList());
        deleteByListUserIdAndDocumentId(listUserIdDelete, documentId);

        Map<Long, DocumentUserEntity> mapEntityByUserId = listInDb
                .stream()
                .collect(Collectors.toMap(DocumentUserEntity::getUserId, Function.identity()));

        List<DocumentUserEntity> listEntityNeedSave = Lists.newArrayListWithExpectedSize(setAllUserId.size());
        setAllUserId.forEach(userId -> {
            DocumentUserEntity entity = mapEntityByUserId.getOrDefault(userId, new DocumentUserEntity());
            entity.setUserId(userId);
            entity.setDocumentId(documentId);
            entity.setOwner(ownerId.equals(userId));
            entity.setView(listViewerId.contains(userId));
            entity.setUpdate(listUpdaterId.contains(userId));
            entity.setApprove(listApproverId.contains(userId));
            entity.setDelete(listDeleterId.contains(userId));
            entity.setSend(listSenderId.contains(userId));
            listEntityNeedSave.add(entity);
        });
        documentUserRepository.saveAll(listEntityNeedSave);
    }

    @Transactional
    public void deleteByListUserIdAndDocumentId(List<Long> listUserIdDelete, Long documentId) {
        if (listUserIdDelete.isEmpty() || documentId == null) {
            return;
        }
        documentUserRepository.deleteByListUserIdAndDocumentId(listUserIdDelete, documentId);
    }

    public List<DocumentUserEntity> getListDocumentUserByDocumentId(Long documentId) {
        return documentUserRepository.getByDocumentId(documentId);
    }

    public Map<Long, List<DocumentUserEntity>> getMapListDocumentUserRelateByDocumentId(List<Long> listDocumentId) {
        if (listDocumentId.isEmpty()) {
            return Collections.emptyMap();
        }
        List<DocumentUserEntity> listDocumentUser = documentUserRepository.getByListDocumentId(listDocumentId);
        return listDocumentUser
                .stream()
                .collect(Collectors.groupingBy(DocumentUserEntity::getDocumentId));
    }

    public Map<Long, List<Long>> getMapListUserIdByDocumentIdWithPredicate(
            List<Long> listDocumentId,
            Map<Long, List<DocumentUserEntity>> mapListDocumentUserRelateByDocumentId,
            Predicate<DocumentUserEntity> predicateFilter) {
        Map<Long, List<Long>> mapResult = new HashMap<>();
        listDocumentId.forEach(documentId -> {
            List<DocumentUserEntity> listDocumentUser = mapListDocumentUserRelateByDocumentId.get(documentId);
            mapResult.put(documentId,
                    listDocumentUser.stream()
                            .filter(predicateFilter)
                            .map(DocumentUserEntity::getUserId)
                            .collect(Collectors.toList()));
        });
        return mapResult;
    }

    @Transactional
    public void deleteByDocumentId(Long documentId) {
        documentUserRepository.deleteByDocumentId(documentId);
    }

    public boolean isReceivedDocument(Long documentId) {
        return documentUserRepository.countEntityHaveDocumentId(documentId) > 0;
    }

    @Transactional
    public void saveListUserCanViewDocument(List<Long> listUserId, Long documentId) {
        Map<Long, DocumentUserEntity> mapEntityInDbByUserId = documentUserRepository.findByDocumentId(documentId)
                .stream()
                .collect(Collectors.toMap(DocumentUserEntity::getUserId, Function.identity()));

        List<DocumentUserEntity> listNeedSave = Lists.newArrayListWithExpectedSize(listUserId.size());
        listUserId.forEach(userId -> {
            DocumentUserEntity documentUser = mapEntityInDbByUserId.getOrDefault(userId, new DocumentUserEntity());
            documentUser.setUserId(userId);
            documentUser.setDocumentId(documentId);
            documentUser.setView(true);
            listNeedSave.add(documentUser);
        });
        documentUserRepository.saveAll(listNeedSave);
    }

    public void setCanEditPermissionAndSave(List<Long> listWorkUnitId, Long documentId) {
        List<Long> listUserIdCanReceiveDocument = workUnitUserService
                .getListUserIdCanReceiveDocument(listWorkUnitId);
        if (listUserIdCanReceiveDocument.isEmpty()) {
            return;
        }
        Map<Long, DocumentUserEntity> mapEntityInDbByUserId = documentUserRepository.getByDocumentId(documentId)
                .stream()
                .collect(Collectors.toMap(DocumentUserEntity::getUserId, Function.identity()));

        List<DocumentUserEntity> listNeedSave = Lists.newArrayListWithExpectedSize(listUserIdCanReceiveDocument.size());
        listUserIdCanReceiveDocument.forEach(userId -> {
            DocumentUserEntity entity = mapEntityInDbByUserId.getOrDefault(userId, new DocumentUserEntity());
            entity.setUserId(userId);
            entity.setDocumentId(documentId);
            entity.setCanEditPermission(true);
            listNeedSave.add(entity);
        });
        documentUserRepository.saveAll(listNeedSave);
    }

    @Transactional
    public void saveList(List<DocumentUserEntity> listNeedSave) {
        if (listNeedSave.isEmpty()) {
            return;
        }
        documentUserRepository.saveAll(listNeedSave);
    }

    public DocumentUserEntity getByDocumentIdAndUserId(Long documentId, Long userId) {
        return documentUserRepository.findByDocumentIdAndUserId(documentId, userId);
    }

    public List<Long> getListApproverIdByDocumentId(Long documentId) {
        return documentUserRepository.getListApproverIdByDocumentId(documentId);
    }

}
