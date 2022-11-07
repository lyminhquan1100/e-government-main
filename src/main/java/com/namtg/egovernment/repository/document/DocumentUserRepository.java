package com.namtg.egovernment.repository.document;

import com.namtg.egovernment.entity.document.DocumentUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface DocumentUserRepository extends JpaRepository<DocumentUserEntity, Long> {
    @Query(value = "select du from DocumentUserEntity du " +
            "where du.documentId = ?1")
    List<DocumentUserEntity> findByDocumentId(Long documentId);

    @Modifying
    @Query(value = "delete from DocumentUserEntity du " +
            "where du.userId in ?1 and du.documentId = ?2")
    void deleteByListUserIdAndDocumentId(List<Long> listUserIdDelete, Long documentId);

    @Query(value = "select du from DocumentUserEntity du " +
            "where du.documentId = ?1")
    List<DocumentUserEntity> getByDocumentId(Long documentId);

    @Query(value = "select du from DocumentUserEntity du " +
            "where du.documentId in ?1")
    List<DocumentUserEntity> getByListDocumentId(List<Long> listDocumentId);

    @Modifying
    @Query(value = "delete from DocumentUserEntity du " +
            "where du.documentId = ?1")
    void deleteByDocumentId(Long documentId);

    @Query(value = "select count(du) from DocumentUserEntity du " +
            "where du.documentId = ?1")
    int countEntityHaveDocumentId(Long documentId);

    DocumentUserEntity findByDocumentIdAndUserId(Long documentId, Long userId);

    @Query(value = "select du.userId from DocumentUserEntity du " +
            "where du.documentId = ?1 " +
            "and (du.isOwner = true or du.isApprove = true)")
    List<Long> getListApproverIdByDocumentId(Long documentId);
}
