package com.namtg.egovernment.config.security;

import com.namtg.egovernment.entity.user.RoleEntity;
import com.namtg.egovernment.entity.user.UserEntity;
import com.namtg.egovernment.service.user.UserService;
import com.namtg.egovernment.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();
        UserEntity userEntity = userService.findByEmailAndPassword(email, password);

        if (userEntity == null) {
            log.info("Email: {} or password: {} is incorrect", email, password);
            throw new BadCredentialsException("Email or password is incorrect!");
        }
        Set<RoleEntity> setRole = userEntity.getRoles();
        Collection<SimpleGrantedAuthority> authorities = new HashSet<>();

        boolean isOwnerDocument = false, isCanViewDocument = false, isCanUpdateDocument = false,
                isCanDeleteDocument = false, isCanSendDocument = false, isCanApproveDocument = false,
                isCanManageNews = false, isCanManageField = false, isCanManageReasonDeniedComment = false,
                isCanManageUser = false, isCanManageUnit = false;
        for (RoleEntity role : setRole) {
            String roleName = role.getName().toString();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            if (Constants.OWNER_DOCUMENT.equals(roleName)) {
                isOwnerDocument = true;
                isCanViewDocument = true;
                isCanUpdateDocument = true;
                isCanDeleteDocument = true;
                isCanSendDocument = true;
                isCanApproveDocument = true;
            }
            if (Constants.VIEW_DOCUMENT.equals(roleName)) {
                isCanViewDocument = true;
            }
            if (Constants.UPDATE_DOCUMENT.equals(roleName)) {
                isCanUpdateDocument = true;
            }
            if (Constants.DELETE_DOCUMENT.equals(roleName)) {
                isCanDeleteDocument = true;
            }
            if (Constants.SEND_DOCUMENT.equals(roleName)) {
                isCanSendDocument = true;
            }
            if (Constants.APPROVE_DOCUMENT.equals(roleName)) {
                isCanApproveDocument = true;
            }
            if (Constants.MANAGE_NEWS.equals(roleName)) {
                isCanManageNews = true;
            }
            if (Constants.MANAGE_FIELD.equals(roleName)) {
                isCanManageField = true;
            }
            if (Constants.MANAGE_REASON_DENIED_COMMENT.equals(roleName)) {
                isCanManageReasonDeniedComment = true;
            }
            if (Constants.MANAGE_USER.equals(roleName)) {
                isCanManageUser = true;
            }
            if (Constants.MANAGE_UNIT.equals(roleName)) {
                isCanManageUnit = true;
            }
        }

        UserDetails userDetails = new CustomUserDetail(userEntity.getId(),
                isOwnerDocument, isCanViewDocument, isCanUpdateDocument,
                isCanDeleteDocument, isCanSendDocument, isCanApproveDocument,
                isCanManageNews, isCanManageField, isCanManageReasonDeniedComment, isCanManageUser, isCanManageUnit,
                authorities);
        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
