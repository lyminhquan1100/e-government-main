package com.namtg.egovernment.repository.user;

import com.namtg.egovernment.dto.user.IdAndName;
import com.namtg.egovernment.entity.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Query(value = "select u from UserEntity u " +
            "where u.email = ?1 and u.status = 1 and u.isDeleted = false")
    UserEntity findByEmailToCheckLogin(String email);

    List<UserEntity> findByIdInAndIsDeletedFalse(Collection<Long> listUserId);

    @Query(value = "select u from UserEntity u " +
            "where u.name like concat ('%', ?1, '%') and u.isDeleted = false")
    List<UserEntity> getList(String keywordNameUser);

    @Query(value = "select u from UserEntity u " +
            "inner join WorkUnitUserEntity wuu on u.id = wuu.userId " +
            "where u.name like concat ('%', ?1, '%') and u.isDeleted = false " +
            "and wuu.workUnitId in ?2")
    List<UserEntity> getList(String keywordNameUser, List<Long> listWorkUnitId);

    UserEntity findByIdAndIsDeletedFalse(Long userId);

    @Query(value = "select u from UserEntity u " +
            "where u.id = ?1 and u.status = 1 and u.isDeleted = false")
    UserEntity getById(Long userId);

    @Query(value = "select u.name from UserEntity u " +
            "where u.id = ?1 and u.isDeleted = false")
    String getNameUserByUserId(Long userId);

    @Query(value = "select u.* from user as u " +
            "inner join user_role as ur on u.id = ur.user_id " +
            "inner join role as r on r.id = ur.role_id " +
            "where r.name in ?1", nativeQuery = true)
    List<UserEntity> getListAdmin(Set<String> setRoleAdmin);

    @Query(value = "select count(u.id) from user as u " +
            "where u.email = ?1 and u.is_deleted = false", nativeQuery = true)
    int countEmail(String email);

    @Query(value = "select u from UserEntity u " +
            "where u.email = ?1 and u.isDeleted = false")
    UserEntity findByEmailAndIsDeletedFalse(String email);

    @Query(value = "select u.* from user as u " +
            "where u.id in " +
            "(select wuu.user_id from work_unit_user as wuu where wuu.work_unit_id in ?1) " +
            "and u.is_deleted = false", nativeQuery = true)
    List<UserEntity> getListUserByListWorkUnitId(List<Long> listWorkUnitId);

    @Query(value = "select u from UserEntity u " +
            "where u.status = 1 and u.isDeleted = false")
    List<UserEntity> findAllMemberActive();

    @Query(value = "select new com.namtg.egovernment.dto.user.IdAndName" +
            "(u.id, u.name) from UserEntity u " +
            "where u.id in ?1 " +
            "and u.isDeleted = false " +
            "and u.status = 1")
    List<IdAndName> getObjectIdAndNameByListId(List<Long> listUserId);
}
