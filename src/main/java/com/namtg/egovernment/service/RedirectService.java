package com.namtg.egovernment.service;

import com.namtg.egovernment.config.security.CustomUserDetail;
import org.springframework.stereotype.Service;

@Service
public class RedirectService {
    public String getUrlDefault(CustomUserDetail currentUserLogin) {
//        boolean isAdmin = currentUserLogin.isOwnerDocument() || currentUserLogin.isCanViewDocument()
//                || currentUserLogin.isCanUpdateDocument() || currentUserLogin.isCanDeleteDocument()
//                || currentUserLogin.isCanSendDocument() || currentUserLogin.isCanApproveDocument()
//                || currentUserLogin.isCanManageNews()
//                || currentUserLogin.isCanManageField()
//                || currentUserLogin.isCanManageReasonDeniedComment();
//        if (isAdmin) {
//            return "/admin/home";
//        } else {
//            return "/home";
//        }
        return "/home";
    }
}
