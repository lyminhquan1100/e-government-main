package com.namtg.egovernment.repository.user;

import com.namtg.egovernment.entity.user.RoleEntity;
import com.namtg.egovernment.enum_common.TypeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    @Query(value = "select r from RoleEntity r " +
            "where r.id = ?1")
    RoleEntity getById(Long roleId);

    @Query(value = "select r.id from RoleEntity r")
    Set<Long> getSetAllRoleId();

    @Query(value = "select r from RoleEntity r " +
            "where r.name in ?1")
    Set<RoleEntity> getSetRoleByListName(List<TypeRole> listRoleName);
}
