package com.namtg.egovernment.service.document;

import com.namtg.egovernment.dto.document.WorkUnitIdReceiveAndPermission;
import com.namtg.egovernment.dto.response.ServerResponseDto;
import com.namtg.egovernment.entity.document.DocumentReceiveEntity;
import com.namtg.egovernment.repository.document.DocumentReceiveRepository;
import com.namtg.egovernment.service.notification.NotificationAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DocumentReceiveService {
    @Autowired
    private DocumentReceiveRepository documentReceiveRepository;

    @Autowired
    private NotificationAdminService notificationAdminService;

    @Transactional
    public void saveDocumentReceive(Long documentId,
                                    List<WorkUnitIdReceiveAndPermission> listWorkUnitIdReceiveAndPermission,
                                    Long creatorId) {
        List<DocumentReceiveEntity> listInDb = documentReceiveRepository.findByDocumentId(documentId);
        Map<Long, DocumentReceiveEntity> mapEntityInDbByWorkUnitId = listInDb
                .stream()
                .collect(Collectors.toMap(DocumentReceiveEntity::getWorkUnitId, Function.identity()));

        List<DocumentReceiveEntity> listNeedSave = listWorkUnitIdReceiveAndPermission
                .stream()
                .map(objRequest -> {
                    Long workUnitId = objRequest.getWorkUnitId();
                    DocumentReceiveEntity entity = mapEntityInDbByWorkUnitId.getOrDefault(workUnitId,
                            new DocumentReceiveEntity());
                    entity.setDocumentId(documentId);
                    entity.setWorkUnitId(workUnitId);
                    entity.setCanEditViewer(objRequest.isCanEditViewer());
                    entity.setCanEditUpdater(objRequest.isCanEditUpdater());
                    entity.setCanEditApprover(objRequest.isCanEditApprover());
                    entity.setCanEditDeleter(objRequest.isCanEditDeleter());
                    entity.setCanEditSender(objRequest.isCanEditSender());
                    return entity;
                }).collect(Collectors.toList());
        documentReceiveRepository.saveAll(listNeedSave);

        // tạo thông báo nhận văn bản
        Set<Long> listWorkUnitIdInDb = listInDb
                .stream()
                .map(DocumentReceiveEntity::getWorkUnitId)
                .collect(Collectors.toSet());
        List<Long> listWorkUnitIdAdd = listWorkUnitIdReceiveAndPermission
                .stream()
                .map(WorkUnitIdReceiveAndPermission::getWorkUnitId)
                .filter(id -> !listWorkUnitIdInDb.contains(id))
                .collect(Collectors.toList());
        if (!listWorkUnitIdAdd.isEmpty()) {
            notificationAdminService.createNotifyReceiveDocument(
                    creatorId,
                    documentId,
                    listWorkUnitIdAdd);
        }
    }

    public boolean isReceivedDocument(Long documentId, Long workUnitId) {
        return documentReceiveRepository.isReceivedDocument(documentId, workUnitId);
    }

    @Transactional
    public DocumentReceiveEntity receiveDocument(Long documentId, Long workUnitIdReceive) {
        DocumentReceiveEntity documentReceiveEntity = documentReceiveRepository
                .findByDocumentIdAndWorkUnitId(documentId, workUnitIdReceive);
        if (documentReceiveEntity == null) {
            return null;
        }
        documentReceiveEntity.setReceived(true);
        return documentReceiveRepository.save(documentReceiveEntity);
    }

    public ServerResponseDto getListInfoWorkUnitSent(Long documentId) {
        return ServerResponseDto.successWithData(documentReceiveRepository.findByDocumentId(documentId));
    }

    public List<DocumentReceiveEntity> getReceivedByListWorkUnitIdAndDocumentId(List<Long> listWorkUnitId, Long documentId) {
        if (listWorkUnitId.isEmpty() || documentId == null) {
            return Collections.emptyList();
        }
        return documentReceiveRepository.getReceivedByListWorkUnitIdAndDocumentId(listWorkUnitId, documentId);
    }

    public List<Long> getListWorkUnitIdReceiveThisDocument(Long documentId) {
        return documentReceiveRepository.getListWorkUnitIdReceiveThisDocument(documentId);
    }
}
