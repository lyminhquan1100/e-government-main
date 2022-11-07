package com.namtg.egovernment.service.user;

import com.namtg.egovernment.entity.user.RoleEntity;
import com.namtg.egovernment.enum_common.TypeRole;
import com.namtg.egovernment.repository.user.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    public List<RoleEntity> getList() {
        return roleRepository.findAll();
    }

    public RoleEntity getById(Long roleId) {
        return roleRepository.getById(roleId);
    }

    public Set<Long> getSetAllRoleId() {
        return roleRepository.getSetAllRoleId();
    }

    public Set<RoleEntity> getSetRoleByListName(List<TypeRole> listRoleName) {
        return roleRepository.getSetRoleByListName(listRoleName);
    }
}
