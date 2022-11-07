package com.namtg.egovernment.repository.old_version_document;

import com.namtg.egovernment.entity.old_version_document.ContentDocumentUpdateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContentDocumentUpdateRepository extends JpaRepository<ContentDocumentUpdateEntity, Long> {
    @Query(value = "select * from content_document_update as c " +
            "where c.document_id = ?1 " +
            "and cast(c.version as DECIMAL(3,1)) = cast(?2 as DECIMAL(3,1)) " +
            "and c.is_deleted = false", nativeQuery = true)
    List<ContentDocumentUpdateEntity> findByDocumentIdAndVersion(Long documentId, float versionCurrent);

    @Query(value = "select c.content from content_document_update as c " +
            "where c.document_id = ?1 " +
            "and cast(c.version as DECIMAL(3,1)) = cast(?2 as DECIMAL(3,1)) " +
            "and c.user_id_update = ?3 " +
            "and c.updated_time = " +
            "   (select MAX(c1.updated_time) from content_document_update as c1 " +
            "   where c1.document_id = ?1 " +
            "   and cast(c1.version as DECIMAL(3,1)) = cast(?2 as DECIMAL(3,1)) " +
            "   and c1.user_id_update = ?3)",
            nativeQuery = true)
    String getContentUpdated(Long documentId, float version, Long userId);

    @Query(value = "select * from content_document_update as c " +
            "where c.user_id_update = ?1 " +
            "and c.document_id = ?2 " +
            "and cast(c.version as DECIMAL(3,1)) = cast(?3 as DECIMAL(3,1))",
            nativeQuery = true)
    ContentDocumentUpdateEntity findByUserIdAndDocumentIdAndVersion(Long userId, Long documentId, float version);

    @Modifying
    @Query(value = "delete c.* from content_document_update as c " +
            "where c.document_id = ?1 " +
            "and cast(c.version as DECIMAL(3,1)) = cast(?2 as DECIMAL(3,1))", nativeQuery = true)
    void deleteByDocumentIdAndVersion(Long documentId, float version);

    @Query(value = "select c from ContentDocumentUpdateEntity c " +
            "where c.id = ?1")
    ContentDocumentUpdateEntity getById(Long contentDocumentUpdateId);
}
