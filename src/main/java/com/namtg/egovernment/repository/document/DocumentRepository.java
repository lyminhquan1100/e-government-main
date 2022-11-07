package com.namtg.egovernment.repository.document;

import com.namtg.egovernment.entity.document.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;


public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

    @Query(value = "select d from DocumentEntity d " +
            "left join DocumentUserEntity du on d.id = du.documentId " +
            "where du.userId = ?2 " +
            "and (du.isOwner = true or du.isView = true) " +
            "and d.title like concat ('%', ?1, '%') " +
            "and d.isDeleted = false")
    Page<DocumentEntity> getPage(String keyword, Long currentUserId, Pageable pageable);

    DocumentEntity findByIdAndIsDeletedFalse(Long id);

    @Query(value = "select p from DocumentEntity p " +
            "where p.title like concat ('%', ?1, '%') " +
            "and p.isDeleted = false " +
            "and (p.isPublic = true or p.id in " +
            "   (select du.documentId from DocumentUserEntity du " +
            "   where du.userId = ?2 " +
            "   and (du.isOwner = true or du.isView = true)))")
    List<DocumentEntity> getListForUser(String keyword, Long currentUserId);

    @Query(value = "select p.* from document as p " +
            "where p.seo = ?1 and p.is_deleted = false limit 1", nativeQuery = true)
    DocumentEntity findBySeoAndIsDeletedFalse(String seo);

    @Query(value = "select p from DocumentEntity p " +
            "where p.isDeleted = false " +
            "and p.createdTime = " +
            "   (select max(p.createdTime) from DocumentEntity p " +
            "   where p.isDeleted = false " +
            "   and (p.isPublic = true or p.id in " +
            "   (select du.documentId from DocumentUserEntity du " +
            "   where du.userId = ?1 " +
            "   and (du.isOwner = true or du.isView = true))))")
    DocumentEntity getDocumentLatest(Long currentUserId);

    List<DocumentEntity> findByIdIn(Collection<Long> listDocumentId);

    List<DocumentEntity> findByIdInAndIsDeletedFalse(Collection<Long> listDocumentId);

    @Query(value = "select p.id from DocumentEntity p " +
            "inner join CommentEntity c on c.documentId = p.id " +
            "inner join ReplyCommentEntity rc on rc.commentId = c.id " +
            "where rc.id = ?1 and p.isDeleted = false")
    Long getDocumentIdByReplyCommentId(Long replyCommentId);

    @Query(value = "select d.* from document as d " +
            "where d.is_deleted = false order by d.created_time desc limit 5", nativeQuery = true)
    List<DocumentEntity> getListTop5Document();

    @Query(value = "select count(p) from DocumentEntity p " +
            "where p.title = ?1 and p.workUnitId = ?2 and p.id <> ?3 and p.isDeleted = false")
    int countDocumentExist(String title, Long workUnitId, Long documentId);

    @Query(value = "select count(p) from DocumentEntity p " +
            "where p.title = ?1 and p.workUnitId = ?2 and p.isDeleted = false")
    int countDocumentExist(String title, Long workUnitId);

    @Query(value = "select p.content from DocumentEntity p " +
            "where p.id = ?1 and p.isDeleted = false")
    String getContentById(Long documentId);
}
