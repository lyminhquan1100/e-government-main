package com.namtg.egovernment.controller.user;

import com.namtg.egovernment.config.security.CustomUserDetail;
import com.namtg.egovernment.dto.field.FieldAndListNews;
import com.namtg.egovernment.entity.document.DocumentEntity;
import com.namtg.egovernment.entity.news.NewsEntity;
import com.namtg.egovernment.entity.user.UserEntity;
import com.namtg.egovernment.service.RedirectService;
import com.namtg.egovernment.service.document.DocumentService;
import com.namtg.egovernment.service.field.FieldService;
import com.namtg.egovernment.service.news.NewsService;
import com.namtg.egovernment.service.user.UserService;
import com.namtg.egovernment.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
public class UserController {
    @Autowired
    private DocumentService documentService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private FieldService fieldService;

    @Autowired
    private RedirectService redirectService;

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(@AuthenticationPrincipal CustomUserDetail currentUserLogin) {
        if (currentUserLogin == null) {
            return "common/login";
        } else {
            String urlDefault = redirectService.getUrlDefault(currentUserLogin);
            return "redirect:" + urlDefault;
        }
    }

    @GetMapping("/register")
    public String register() {
        return "common/register";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "common/forgot_password";
    }

    @GetMapping("/confirmForgotPassword")
    public String confirmForgotPassword(@RequestParam("token") String token, Model model) {
        boolean isConfirmSuccess = userService.confirmForgotPassword(token);
        if (isConfirmSuccess) {
            model.addAttribute("token", token);
            return "common/set_password";
        } else {
            return "common/not_found";
        }
    }

    @RequestMapping(value = {"/", "/home"})
    public String home(Model model,
                       @AuthenticationPrincipal CustomUserDetail customUserDetail) {
        NewsEntity newsLatest = newsService.getNewsLatest();
        model.addAttribute("newsLatest", newsLatest != null ? newsLatest : new NewsEntity());

        List<NewsEntity> listTop5NewsLatest = newsService.getListTop5NewsLatest();
        model.addAttribute("listTop5NewsLatest", listTop5NewsLatest);

        model.addAttribute("listTop5Document",
                documentService.getListTop5Document(customUserDetail != null ? customUserDetail.getId() : null));

        List<FieldAndListNews> listDataFieldAndNews = fieldService.getListDataFieldAndNews();
        model.addAttribute("listDataFieldAndNews", listDataFieldAndNews);

        return "user/home";
    }

    @RequestMapping(value = {"/document", "/document/{seo}"})
    public String document(Model model,
                           @PathVariable(value = "seo", required = false) String seo,
                           @AuthenticationPrincipal CustomUserDetail customUserDetail) {
        if (seo != null) {
            DocumentEntity documentDetail = documentService.findBySeoAndIsDeletedFalse(seo);
            model.addAttribute("postId", documentDetail.getId());
        }
        DocumentEntity documentLatest = documentService.getDocumentLatest(customUserDetail != null ? customUserDetail.getId() : null);
        model.addAttribute("documentLatest", documentLatest != null ? documentLatest : new DocumentEntity());
        return "user/document";
    }

    @GetMapping("/my_profile")
    public String myProfile(@AuthenticationPrincipal CustomUserDetail customUserDetail,
                            Model model) {
        if (customUserDetail == null) {
            return "common/not_found";
        }
        UserEntity userEntity = userService.getById(customUserDetail.getId());
        model.addAttribute("currentUser", userEntity);
        return "user/my_profile";
    }

    @GetMapping("/news/{seo}")
    public String detailNews(@PathVariable String seo,
                             Model model) {
        NewsEntity newsLatest = newsService.getNewsLatest();
        model.addAttribute("newsLatest", newsLatest != null ? newsLatest : new NewsEntity());
        NewsEntity newsEntity = newsService.getBySeo(seo);
        newsEntity.setCreatedTimeStr(DateUtils.convertDateToStringWithPattern(newsEntity.getCreatedTime(), "yyyy-MM-dd HH:mm:ss"));
        model.addAttribute("newsEntity", newsEntity);
        return "user/detail_news";
    }

}
