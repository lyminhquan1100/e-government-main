package com.namtg.egovernment.repository.unit;

import com.namtg.egovernment.entity.unit.WorkUnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WorkUnitRepository extends JpaRepository<WorkUnitEntity, Long> {
    @Query(value = "select w.id from work_unit as w", nativeQuery = true)
    List<Long> getAllWorkUnitId();

    @Modifying
    @Query(value = "delete from WorkUnitEntity w " +
            "where w.id in ?1")
    void deleteByListId(List<Long> listWorkUnitIdDelete);

    List<WorkUnitEntity> findByIdIn(List<Long> listWorkUnitId);

    @Query(value = "select w from WorkUnitEntity w " +
            "where w.id = ?1")
    WorkUnitEntity getById(Long workUnitId);

    @Query(value = "select wu.* from work_unit as wu " +
            "where wu.id in (" +
            "   select wuu.work_unit_id from work_unit_user as wuu " +
            "   where wuu.user_id = ?1)", nativeQuery = true)
    List<WorkUnitEntity> getListWorkUnitCanCreateUser(Long currentUserId);

    @Query(value = "select w from WorkUnitEntity w")
    List<WorkUnitEntity> getAllWorkUnit();

    @Query(value = "select w.name from WorkUnitEntity w " +
            "where w.id = ?1")
    String getNameById(Long workUnitId);

}
