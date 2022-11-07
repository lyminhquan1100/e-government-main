package com.namtg.egovernment.service.old_version_document;

import com.namtg.egovernment.dto.response.ServerResponseDto;
import com.namtg.egovernment.entity.old_version_document.OldVersionDocumentEntity;
import com.namtg.egovernment.repository.old_version_document.OldVersionDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class OldVersionDocumentService {
    @Autowired
    private OldVersionDocumentRepository oldVersionDocumentRepository;

    public void saveOldVersion(Long currentUserId, Long documentId, float oldVersion, String oldContent) {
        OldVersionDocumentEntity oldVersionDocument = oldVersionDocumentRepository
                .findByDocumentIdAndVersion(documentId, oldVersion);
        if (oldVersionDocument == null) {
            oldVersionDocument = new OldVersionDocumentEntity();
            oldVersionDocument.setCreatedTime(new Date());
        }
        oldVersionDocument.setUpdatedTime(new Date());
        oldVersionDocument.setUserIdApprove(currentUserId);
        oldVersionDocument.setDocumentId(documentId);
        oldVersionDocument.setVersion(oldVersion);
        oldVersionDocument.setContent(oldContent);
        oldVersionDocumentRepository.save(oldVersionDocument);
    }

    public List<OldVersionDocumentEntity> getListOldVersion(Long documentId) {
        if (documentId == null) {
            return Collections.emptyList();
        }
        return oldVersionDocumentRepository.findByDocumentIdAndIsDeletedFalse(documentId);
    }

    public ServerResponseDto getContentOldVersion(Long oldVersionId) {
        if (oldVersionId == null) {
            return ServerResponseDto.ERROR;
        }
        return ServerResponseDto.successWithData(oldVersionDocumentRepository.getContentOldVersion(oldVersionId));
    }
}
