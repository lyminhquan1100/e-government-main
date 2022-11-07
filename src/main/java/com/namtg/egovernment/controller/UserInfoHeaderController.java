package com.namtg.egovernment.controller;

import com.namtg.egovernment.config.security.CustomUserDetail;
import com.namtg.egovernment.dto.role.RoleObject;
import com.namtg.egovernment.entity.unit.WorkUnitUserEntity;
import com.namtg.egovernment.entity.user.RoleEntity;
import com.namtg.egovernment.entity.user.UserEntity;
import com.namtg.egovernment.enum_common.TypeRole;
import com.namtg.egovernment.repository.unit.WorkUnitUserRepository;
import com.namtg.egovernment.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Set;
import java.util.stream.Collectors;

@ControllerAdvice
public class UserInfoHeaderController {
    @Autowired
    private UserService userService;

    @Autowired
    private WorkUnitUserRepository workUnitUserRepository;

    @ModelAttribute
    public void getCurrentUser(@AuthenticationPrincipal CustomUserDetail customUserDetail,
                               Model model) {
        if (customUserDetail == null) {
            return;
        }
        Long userId = customUserDetail.getId();
        UserEntity userEntity = userService.getById(userId);

        model.addAttribute("roleObject", convertSetRoleToRoleObject(userEntity.getId(), userEntity.getRoles()));
        model.addAttribute("currentUser", userEntity);
    }

    private RoleObject convertSetRoleToRoleObject(Long userId, Set<RoleEntity> roles) {
        Set<TypeRole> setTypeRole = roles.stream().map(RoleEntity::getName).collect(Collectors.toSet());

        boolean isOwnerDocument = setTypeRole.contains(TypeRole.OWNER_DOCUMENT);
        boolean isCanViewDocument = setTypeRole.contains(TypeRole.VIEW_DOCUMENT);
        boolean isCanUpdateDocument = setTypeRole.contains(TypeRole.UPDATE_DOCUMENT);
        boolean isCanDeleteDocument = setTypeRole.contains(TypeRole.DELETE_DOCUMENT);
        boolean isCanSendDocument = setTypeRole.contains(TypeRole.SEND_DOCUMENT);
        boolean isCanApproveDocument = setTypeRole.contains(TypeRole.APPROVE_DOCUMENT);

        boolean isCanAccessDocument = isOwnerDocument || isCanViewDocument || isCanUpdateDocument
                || isCanDeleteDocument || isCanSendDocument || isCanApproveDocument;

        boolean isCanManageUser = userService.isLevelGovernment(userId) ||
                workUnitUserRepository.getByUserId(userId)
                        .stream()
                        .anyMatch(WorkUnitUserEntity::isCanManageUser);

        return new RoleObject(
                isCanAccessDocument,
                isOwnerDocument,
                isCanViewDocument,
                isCanUpdateDocument,
                isCanDeleteDocument,
                isCanSendDocument,
                isCanApproveDocument,
                setTypeRole.contains(TypeRole.MANAGE_NEWS),
                setTypeRole.contains(TypeRole.MANAGE_FIELD),
                setTypeRole.contains(TypeRole.MANAGE_REASON_DENIED_COMMENT),
                isCanManageUser,
                setTypeRole.contains(TypeRole.MANAGE_UNIT));
    }
}
