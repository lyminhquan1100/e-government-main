package com.namtg.egovernment.repository.unit;

import com.namtg.egovernment.entity.unit.WorkUnitUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WorkUnitUserRepository extends JpaRepository<WorkUnitUserEntity, Long> {
    @Query(value = "select w.workUnitId from WorkUnitUserEntity w " +
            "where w.userId = ?1")
    List<Long> getListWorkUnitId(Long userId);

    @Modifying
    @Query(value = "delete from WorkUnitUserEntity w " +
            "where w.workUnitId in ?1")
    void deleteByListWorkUnitId(List<Long> listWorkUnitIdDelete);

    @Query(value = "select w from WorkUnitUserEntity w " +
            "where w.userId = ?1")
    List<WorkUnitUserEntity> getByUserId(Long userId);

    @Query(value = "select w from WorkUnitUserEntity w " +
            "where w.userId = ?1 " +
            "and w.workUnitId in ?2")
    List<WorkUnitUserEntity> findByUserIdAndWorkUnitIdIn(Long userId, List<Long> listWorkUnitIdUpdate);

    @Query(value = "select w.userId from WorkUnitUserEntity w " +
            "where w.workUnitId in ?1")
    List<Long> getListUserIdByListWorkUnitId(List<Long> listWorkUnitId);

    @Query(value = "select w from WorkUnitUserEntity w " +
            "where w.workUnitId in ?1 and w.isCanReceiveDocument = true")
    List<WorkUnitUserEntity> getByListWorkUnitId(List<Long> listWorkUnitIdReceiveDocument);

    @Query(value = "select w.userId from WorkUnitUserEntity w " +
            "where w.workUnitId in ?1 " +
            "and w.isCanReceiveDocument = true")
    List<Long> getListUserIdCanReceiveDocumentByListWorkUnitId(List<Long> listWorkUnitId);

    @Query(value = "select w from WorkUnitUserEntity w " +
            "where w.userId in ?1")
    List<WorkUnitUserEntity> findByListUserId(List<Long> listUserId);
}
