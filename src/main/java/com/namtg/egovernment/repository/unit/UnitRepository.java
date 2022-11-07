package com.namtg.egovernment.repository.unit;

import com.namtg.egovernment.entity.unit.UnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UnitRepository extends JpaRepository<UnitEntity, Long> {

    @Query(value = "select u.id from unit as u", nativeQuery = true)
    List<Long> getListAllId();

    @Modifying
    @Query(value = "delete from UnitEntity u " +
            "where u.id in ?1")
    void deleteListUnitById(List<Long> listIdDeleted);

    @Query(value = "select u from UnitEntity u " +
            "where u.level = 1")
    List<UnitEntity> getListUnitIdLv1();

    @Query(value = "select u from UnitEntity u " +
            "where u.unitLevelAboveId = ?1")
    List<UnitEntity> getListUnitChild(Long unitParentId);

    List<UnitEntity> findByIdIn(List<Long> listUnitId);
}
