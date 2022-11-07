package com.namtg.egovernment.repository.old_version_document;

import com.namtg.egovernment.entity.old_version_document.OldVersionDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OldVersionDocumentRepository extends JpaRepository<OldVersionDocumentEntity, Long> {

    @Query(value = "select * from old_version_document as o " +
            "where o.document_id = ?1 " +
            "and cast(o.version as DECIMAL(3,1)) = cast(?2 as DECIMAL(3,1)) " +
            "and o.is_deleted = false", nativeQuery = true)
    OldVersionDocumentEntity findByDocumentIdAndVersion(Long documentId, float version);

    List<OldVersionDocumentEntity> findByDocumentIdAndIsDeletedFalse(Long documentId);

    @Query(value = "select o.content from OldVersionDocumentEntity o " +
            "where o.id = ?1 and o.isDeleted = false")
    String getContentOldVersion(Long oldVersionId);
}
