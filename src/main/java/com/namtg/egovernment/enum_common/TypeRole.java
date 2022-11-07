package com.namtg.egovernment.enum_common;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
public enum TypeRole {
    OWNER_DOCUMENT, VIEW_DOCUMENT, UPDATE_DOCUMENT, DELETE_DOCUMENT, SEND_DOCUMENT, APPROVE_DOCUMENT,
    MANAGE_NEWS, MANAGE_FIELD, MANAGE_REASON_DENIED_COMMENT, MANAGE_USER, MANAGE_UNIT;

    public static Set<TypeRole> setRoleAdmin =
            new HashSet<>(Arrays.asList(
                    TypeRole.OWNER_DOCUMENT, TypeRole.VIEW_DOCUMENT,
                    TypeRole.UPDATE_DOCUMENT, TypeRole.DELETE_DOCUMENT,
                    TypeRole.SEND_DOCUMENT, TypeRole.APPROVE_DOCUMENT,
                    TypeRole.MANAGE_NEWS, TypeRole.MANAGE_FIELD, TypeRole.MANAGE_REASON_DENIED_COMMENT, TypeRole.MANAGE_USER, TypeRole.MANAGE_UNIT));
}
