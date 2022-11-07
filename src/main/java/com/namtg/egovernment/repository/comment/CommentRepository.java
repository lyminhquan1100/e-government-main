package com.namtg.egovernment.repository.comment;

import com.namtg.egovernment.dto.comment.DocumentIdAndNumberCommentObject;
import com.namtg.egovernment.entity.comment.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    @Query(value = "select c from CommentEntity c " +
            "where c.id = ?1")
    CommentEntity getById(Long commentId);

    @Query(value = "select c from CommentEntity c " +
            "where c.documentId = ?1 and c.isDeleted = false")
    List<CommentEntity> findByDocumentIdAndIsDeletedFalse(Long documentId);

    @Query(value = "select c.documentId as documentId, count(c.id) as numberComment from CommentEntity c " +
            "where c.documentId in ?1 and (c.status = 'WAITING' or c.status = 'APPROVED') and c.isDeleted = false group by c.documentId")
    List<DocumentIdAndNumberCommentObject> getListDocumentIdAndNumberComment(List<Long> listDocumentId);

    List<CommentEntity> findByIdIn(Collection<Long> listCommentId);

    @Query(value = "select c from CommentEntity c " +
            "where c.documentId = ?1 and c.status = 'APPROVED' and c.isDeleted = false")
    List<CommentEntity> getListCommentApprovedByDocumentId(Long documentId);

    @Modifying
    @Query(value = "update CommentEntity c " +
            "set c.isDeleted = true " +
            "where c.id in ?1")
    void deleteByListId(List<Long> listCommentIdDeleted);

    CommentEntity findByIdAndIsDeletedFalse(Long commentId);

    @Query(value = "select c.documentId from CommentEntity c " +
            "where c.id = ?1 and c.isDeleted = false")
    Long getDocumentIdByCommentId(Long commentId);
}
