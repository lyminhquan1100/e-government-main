package com.namtg.egovernment.controller.admin;

import com.namtg.egovernment.config.security.CustomUserDetail;
import com.namtg.egovernment.entity.document.DocumentEntity;
import com.namtg.egovernment.entity.user.UserEntity;
import com.namtg.egovernment.service.document.DocumentService;
import com.namtg.egovernment.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminHomeController {
    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @GetMapping(value = {"/", "/home"})
    public String adminHome() {
        return "admin/admin_home";
    }

    @GetMapping("/document")
    public String adminDiscussionPost() {
        return "admin/manage_document/admin_document";
    }

    @GetMapping("/document/{seo}")
    public String adminDetailDiscussionPost(@PathVariable String seo, Model model) {
        DocumentEntity documentEntity = documentService.detailForAdmin(seo);
        model.addAttribute("post", documentEntity);
        return "admin/manage_document/admin_detail_document";
    }

    @GetMapping("/unit")
    public String adminUnit() {
        return "admin/manage_unit/admin_unit";
    }

    @GetMapping("/work_unit")
    public String adminWorkUnit() {
        return "admin/manage_unit/admin_work_unit";
    }

    @GetMapping("/field")
    public String adminField() {
        return "admin/admin_field";
    }

    @GetMapping("/user")
    public String adminManageUser() {
        return "admin/manage_user/admin_manage_user";
    }

    @GetMapping("/reason_denied_comment")
    public String adminReasonDeniedComment() {
        return "admin/admin_reason_denied_comment";
    }

    @GetMapping("/news")
    public String adminNews() {
        return "admin/admin_news";
    }

    @GetMapping("/my_profile")
    public String adminMyProfile(@AuthenticationPrincipal CustomUserDetail customUserDetail,
                                 Model model) {
        if (customUserDetail == null) {
            return "common/not_found";
        }
        UserEntity userEntity = userService.getById(customUserDetail.getId());
        model.addAttribute("currentUser", userEntity);
        return "admin/manage_user/admin_my_profile";
    }
}
