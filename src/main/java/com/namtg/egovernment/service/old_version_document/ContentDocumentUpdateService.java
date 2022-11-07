package com.namtg.egovernment.service.old_version_document;

import com.namtg.egovernment.dto.document.ContentDocumentDto;
import com.namtg.egovernment.dto.response.ServerResponseDto;
import com.namtg.egovernment.entity.document.DocumentEntity;
import com.namtg.egovernment.entity.old_version_document.ContentDocumentUpdateEntity;
import com.namtg.egovernment.repository.old_version_document.ContentDocumentUpdateRepository;
import com.namtg.egovernment.service.document.DocumentService;
import com.namtg.egovernment.service.document.DocumentUserService;
import com.namtg.egovernment.service.notification.NotificationAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ContentDocumentUpdateService {
    @Autowired
    private DocumentService documentService;

    @Autowired
    private NotificationAdminService notificationAdminService;

    @Autowired
    private DocumentUserService documentUserService;

    @Autowired
    private ContentDocumentUpdateRepository repository;

    // tạo thông báo trong trường hợp:
    // 1. nội dung thay đổi
    @Transactional
    public ServerResponseDto updateContent(ContentDocumentDto contentDocumentDto, Long userUpdateId) {
        Long documentId = contentDocumentDto.getId();
        boolean isCreateNotification = true;
        DocumentEntity documentEntity = documentService.getDocumentById(documentId);
        if (documentEntity == null) {
            return ServerResponseDto.ERROR;
        }
        float version = documentEntity.getVersion();

        ContentDocumentUpdateEntity oldVersionDocument;
        oldVersionDocument = repository.findByUserIdAndDocumentIdAndVersion(userUpdateId, documentId, version);
        String contentUpdate = contentDocumentDto.getContent();
        if (oldVersionDocument != null) {
            isCreateNotification = !contentUpdate.equals(oldVersionDocument.getContent());

            oldVersionDocument.setContent(contentUpdate);
            oldVersionDocument.setUpdatedTime(new Date());
        } else {
            oldVersionDocument = new ContentDocumentUpdateEntity(
                    userUpdateId,
                    documentId,
                    documentEntity.getVersion(),
                    contentUpdate);
        }
        repository.save(oldVersionDocument);

        // tạo thông báo
        if (isCreateNotification) {
            List<Long> listApproverIdOfThisDocument = documentUserService.getListApproverIdByDocumentId(documentId);
            notificationAdminService.createNotificationRemindUpdatedContentDocument(
                    listApproverIdOfThisDocument,
                    oldVersionDocument.getId(),
                    documentId,
                    userUpdateId);
        }

        return ServerResponseDto.SUCCESS;
    }

    public List<ContentDocumentUpdateEntity> getListUpdateForVersionCurrent(Long documentId, float versionCurrent) {
        return repository.findByDocumentIdAndVersion(documentId, versionCurrent);
    }

    public String getContentUpdated(Long documentId, Long userId) {
        DocumentEntity documentEntity = documentService.getDocumentById(documentId);
        String contentUpdated = repository.getContentUpdated(documentId, documentEntity.getVersion(), userId);
        return contentUpdated != null ? contentUpdated : documentEntity.getContent();
    }

    @Transactional
    public void deleteByDocumentIdAndVersion(Long documentId, float version) {
        repository.deleteByDocumentIdAndVersion(documentId, version);
    }

    public ContentDocumentUpdateEntity getById(Long contentDocumentUpdateId) {
        return repository.getById(contentDocumentUpdateId);
    }
}
