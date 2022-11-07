package com.namtg.egovernment.repository.document;

import com.namtg.egovernment.entity.document.DocumentReceiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DocumentReceiveRepository extends JpaRepository<DocumentReceiveEntity, Long> {

    @Query(value = "select d.isReceived from DocumentReceiveEntity d " +
            "where d.documentId = ?1 and d.workUnitId = ?2")
    boolean isReceivedDocument(Long documentId, Long workUnitId);

    DocumentReceiveEntity findByDocumentIdAndWorkUnitId(Long documentId, Long workUnitIdReceive);

    List<DocumentReceiveEntity> findByDocumentId(Long documentId);

    @Query(value = "select d from DocumentReceiveEntity d " +
            "where d.isReceived = true " +
            "and d.workUnitId in ?1 " +
            "and d.documentId = ?2")
    List<DocumentReceiveEntity> getReceivedByListWorkUnitIdAndDocumentId(List<Long> listWorkUnitId, Long documentId);

    @Query(value = "select dr.work_unit_id from document_receive as dr " +
            "where dr.document_id = ?1 " +
            "and dr.is_received = true", nativeQuery = true)
    List<Long> getListWorkUnitIdReceiveThisDocument(Long documentId);
}
