//package com.namtg.egovernment.job;
//
//import com.namtg.egovernment.entity.discussion_post.PostEntity;
//import com.namtg.egovernment.entity.user.UserEntity;
//import com.namtg.egovernment.service.EmailService;
//import com.namtg.egovernment.service.discussion_post.PostService;
//import com.namtg.egovernment.service.notification.NotificationAdminService;
//import com.namtg.egovernment.service.user.UserService;
//import com.namtg.egovernment.util.DateUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.Date;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Component
//public class NotifyConclusionPostJob {
//    @Autowired
//    private EmailService emailService;
//
//    @Autowired
//    private PostService postService;
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private NotificationAdminService notificationAdminService;
//
//    @Scheduled(cron = "${job.notifyConclusionPost}")
//    public void notifyConclusionPost() {
//        log.info("Start job notify conclusion post");
//        List<PostEntity> listPost = postService.findByIsDeletedFalse();
//        String nowStr = DateUtils.convertDateToString(new Date());
//
//        List<PostEntity> listPostMustConclusion = listPost
//                .stream()
//                .filter(post ->
//                        post.getClosingDeadline() != null &&
//                                post.getConclude() == null &&
//                                DateUtils.convertDateToString(post.getClosingDeadline()).equals(nowStr))
//                .collect(Collectors.toList());
//
//        if (listPostMustConclusion.isEmpty()) {
//            log.info("Dont send notify because listPostMustConclusion is empty");
//            return;
//        }
//
//        sendNotify(listPostMustConclusion);
//    }
//
//    private void sendNotify(List<PostEntity> listPostMustConclusion) {
//        List<UserEntity> listAdminReceiveNotificationConclusion = userService
//                .getListAdminCanReceiveNotificationConclusion();
//        if (listAdminReceiveNotificationConclusion.isEmpty()) {
//            return;
//        }
//
//        sendNotifyByMail(listPostMustConclusion, listAdminReceiveNotificationConclusion);
//        sendNotifyByWeb(listPostMustConclusion, listAdminReceiveNotificationConclusion);
//    }
//
//    private void sendNotifyByWeb(List<PostEntity> listPostMustConclusion,
//                                 List<UserEntity> listAdminReceiveNotificationConclusion) {
//        notificationAdminService
//                .createNotificationConclusion(listPostMustConclusion, listAdminReceiveNotificationConclusion);
//    }
//
//    private void sendNotifyByMail(List<PostEntity> listPostMustConclusion,
//                                  List<UserEntity> listAdminReceiveNotificationConclusion) {
//        List<String> listTitlePostMustConclusion = listPostMustConclusion
//                .stream()
//                .map(PostEntity::getTitle)
//                .collect(Collectors.toList());
//
//        if (listAdminReceiveNotificationConclusion.isEmpty()) {
//            return;
//        }
//
//        listAdminReceiveNotificationConclusion.forEach(admin -> {
//            log.info("Start send email notify conclusion post for admin with id: {}, email: {}", admin.getId(), admin.getEmail());
//            emailService.sendMailNotifyConclusionPost(admin.getEmail(), listTitlePostMustConclusion);
//        });
//    }
//}
