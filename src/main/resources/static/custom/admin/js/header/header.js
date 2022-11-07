let tableDocument;

let isManageField;
let isManageReasonDeniedComment;
let isManageNews;
let levelUser;
let ministryIdOfUser;
let agenciesIdOfUser;
let roleName;
let currentUserId;

$(document).ready(function () {
    isManageField = $("#is_manage_field").val() == "true";
    isManageReasonDeniedComment = $("#is_manage_reason_denied_comment").val() == "true";
    isManageNews = $("#is_manage_news").val() == "true";
    levelUser = $("#level_user").val();
    ministryIdOfUser = $("#ministry_id").val();
    agenciesIdOfUser = $("#agencies_id").val();
    roleName = $("#role_name").val();
    currentUserId = $("#current_user_id").val();

    $("#wrap-menu").hide();
    $("#popup-notify").hide();

    /* click bars */
    $(document).on("click", "#bars-menu", function () {
        $("#wrap-menu").toggle();
    })

    $(document).mouseup(function (e) {
        if ($("#wrap-bars-menu").has(e.target).length > 0) {
            return;
        }

        let menu = $("#wrap-menu");
        if (!menu.is(e.target) && menu.has(e.target).length === 0) {
            menu.hide();
        }
    });
    /*-------------------------*/

    /* click btn notification */
    $(document).on("click", "#btn-notify", function () {
        $("#popup-notify").toggle();

        if (!$("#popup-notify").is(":hidden")) {
            popupNotifyVue.getListNotification();
        }

        $("#btn-select-all-notify").addClass("active");
        $("#btn-select-not-read-notify").removeClass("active");

        if (popupNotifyVue.numberNotification !== 0) {
            popupNotifyVue.setAllNotificationToWatched();
        }
    })

    $(document).mouseup(function (e) {
        if ($("#btn-notify").has(e.target).length > 0) {
            return;
        }

        let popupNotify = $("#popup-notify");
        if (!popupNotify.is(e.target) && popupNotify.has(e.target).length === 0) {
            popupNotify.hide();
        }
    });
    /*-------------------------*/

    let popupNotifyVue = new Vue({
        el: "#wrap-notify",
        data: {
            numberNotification: 0,
            listNotification: [],

            notificationId: "",
        },
        methods: {
            getListNotification() {
                let self = this;
                $.ajax({
                    type: "GET",
                    url: "/api/admin/notification/getListNotification",
                    success: function (response) {
                        if (response.status.code === 1000) {
                            self.listNotification = response.data;
                        }
                    }
                })
            },

            getListNotificationNotRead() {
                this.listNotification = this.listNotification.filter(function (notification) {
                    return !notification.read;
                })
            },

            getNumberNotificationHavenWatch() {
                let self = this;
                $.ajax({
                    type: "GET",
                    url: "/api/admin/notification/getNumberNotificationHavenWatch",
                    success: function (response) {
                        self.numberNotification = response.data;
                        if (self.numberNotification > 0) {
                            $(".wrap-number-notify").removeClass("hidden");
                        }
                    }
                })
            },

            setAllNotificationToWatched() {
                let self = this;
                $(".wrap-number-notify").addClass("hidden");
                self.numberNotification = 0;

                $.ajax({
                    type: "GET",
                    url: "/api/admin/notification/setAllNotificationToWatched",
                    success: function (res) {
                    }
                })
            },

            detailNotification() {
                let self = this;

                $.ajax({
                    type: "GET",
                    url: "/api/admin/notification/detail/" + self.notificationId,
                    success: function (response) {
                        let code = response.status.code;

                        if (code === 1000) {
                            $("#popup-notify").hide();

                            let dataNotificationResponse = response.data;
                            let typeNotification = dataNotificationResponse.typeNotificationAdmin;

                            if (typeNotification === "COMMENT" || typeNotification === "REPLY_COMMENT" || typeNotification === "EDIT_COMMENT") {
                                $("#modal_confirm_comment").modal("show");
                                modalConfirmComment.detailModalConfirmComment(dataNotificationResponse);
                            } else if (typeNotification === "CREATE_POST") {
                                documentVue.id = dataNotificationResponse.documentId;
                                documentVue.detail();
                                $('#modal_add_discussion_post').modal("show");
                            } else if (typeNotification === "CONCLUSION_POST") {
                                let seoDocument = dataNotificationResponse.seoDocument;
                                window.location.href = "/admin/document/" + seoDocument;
                            } else if (typeNotification === "RECEIVE_DOCUMENT") {
                                $("#modal_add_document").modal("show");
                                documentVue.handleReceiveDocument(dataNotificationResponse.document);
                            } else if (typeNotification === "INVITE_UPDATE_DOCUMENT") {
                                $("#modal_add_document").modal("show");
                                documentVue.handleInviteUpdateDocument(dataNotificationResponse.document);
                            } else if (typeNotification === "REMIND_UPDATED_CONTENT_DOCUMENT") {
                                showModalDetailContentDocument(dataNotificationResponse.infoContentDocumentChange);
                            }
                        } else if (code === 1601) {
                            window.alert.show("error", "Bình luận không tồn tại", 2000);
                        } else if (code === 1700) {
                            window.alert.show("error", "Văn bản không tồn tại", 2000);
                        } else if (code === 1701) {
                            window.alert.show("error", "Nội dung chỉnh sửa không tồn tại", 2000);
                        }
                    }
                })
            },

        },
        mounted() {
            let self = this;

            self.getNumberNotificationHavenWatch();

            $(document).on("click", ".notify-item", function () {
                self.notificationId = Number($(this).attr("id").replace("notification_", ""));
                self.detailNotification();
            })

            $(document).on("click", ".btn-select-type-notify", function () {
                if ($(this).hasClass("active")) {
                    return;
                }

                let idElement = $(this).attr("id");
                if (idElement === "btn-select-all-notify") {
                    self.getListNotification();
                } else {
                    self.getListNotificationNotRead();
                }

                $(".btn-select-type-notify").each(function () {
                    $(this).removeClass("active");
                })
                $(this).addClass("active");

            })

        }
    })
})