$(document).ready(function () {
    let formUser;
    let isLevelGovernment = $("#is_level_government").val() == 'true';

    let userVue = new Vue({
        el: "#modal_add_user",
        data: {
            id: "",
            name: "",
            email: "",
            gender: 1,
            phone: "",
            address: "",
            password: "",
            passwordConfirm: "",

            isManageUnit: false,
            isManageField: false,
            isManageReasonDeniedComment: false,
            isManageNews: false,

            listWrapUnitLv1: [],

            isShowErrorBirthday: false,
            isShowErrorTimeBirthday: false,
            isShowErrorSelectMinistry: false,
            isShowErrorSelectAgencies: false,

        },
        methods: {
            save() {
                if (!$("#form-user").valid()) {
                    return;
                }
                if (!this.validateForm()) {
                    return;
                }

                let data = new FormData();
                data.append("name", this.name);
                data.append("email", this.email);
                data.append("password", this.password);
                data.append("birthday", $("#birthday").val());
                data.append("phone", this.phone);
                data.append("address", this.address);
                data.append("gender", this.gender);
                if (this.$refs['image-avatar'].getImageSelected()) {
                    data.append("avatar", this.$refs['image-avatar'].getImageSelected());
                }
                if (isLevelGovernment) {
                    data.append("manageUnit", this.isManageUnit);
                    data.append("manageField", this.isManageField);
                    data.append("manageReasonDeniedComment", this.isManageReasonDeniedComment);
                    data.append("manageNews", this.isManageNews);
                } else {
                    data.append("manageUnit", false);
                    data.append("manageField", false);
                    data.append("manageReasonDeniedComment", false);
                    data.append("manageNews", false);
                }

                if (this.id) {
                    data.append("id", this.id);
                }
                $(".id-item.active").each(function (idx) {
                    data.append("listWorkUnitIdAndAction[" + idx + "].workUnitId", Number($(this).val()));
                    data.append("listWorkUnitIdAndAction[" + idx + "].canReceiveDocument",
                        $(this).parent(".item").find(".is-can-receive-document").is(":checked"));
                    data.append("listWorkUnitIdAndAction[" + idx + "].canManageUser",
                        $(this).parent(".item").find(".is-can-manage-user").is(":checked"));
                })

                $.ajax({
                    type: "POST",
                    url: "/api/admin/user/save",
                    processData: false,
                    contentType: false,
                    data: data,
                    beforeSend: function () {
                        window.loader.show();
                    },
                    success: function (response) {
                        window.loader.hide();
                        let code = response.status.code;
                        if (code === 1000) {
                            tableUser.ajax.reload();
                            window.alert.show("success", "L??u th??nh c??ng", 2000);
                            $('#modal_add_user').modal("hide");
                        } else if (code === 1600) {
                            window.alert.show("error", "Email ???? t???n t???i", 2000);
                        } else {
                            window.alert.show("error", "???? c?? l???i x???y ra, vui l??ng th??? l???i sau", 2000);
                        }
                    }
                })

            },
            detail() {
                let self = this;
                $.ajax({
                    type: "GET",
                    url: "/api/admin/user/detail/" + self.id,
                    success: function (response) {
                        if (response.status.code === 1000) {
                            let userEntity = response.data;
                            self.name = userEntity.name;
                            self.email = userEntity.email;
                            self.phone = userEntity.phone;
                            self.gender = userEntity.gender;
                            self.address = userEntity.address;
                            self.levelUser = userEntity.levelUser;
                            if (userEntity.urlAvatar) {
                                self.$refs['image-avatar'].hasImage = true;
                                $(self.$refs['image-avatar'].$el).find('img').attr("src", userEntity.urlAvatar);
                            }
                            if (userEntity.birthday) {
                                $("#birthday").val(userEntity.birthday);
                            }

                            let mapWorkUnitIdWithIsCanReceiveDocument = userEntity.mapWorkUnitIdWithIsCanReceiveDocument;
                            let mapWorkUnitIdWithIsCanManageUser = userEntity.mapWorkUnitIdWithIsCanManageUser;

                            let listWorkUnitId = Object.keys(mapWorkUnitIdWithIsCanReceiveDocument).map(id => Number(id));
                            $(".id-item").each(function () {
                                let workUnitId = $(this).val();
                                if (listWorkUnitId.includes(Number(workUnitId))) {
                                    $(this).addClass("active");
                                    $(this).parent(".item").children(".label-item").addClass("active");
                                    let $checkboxCanReceiveDocument = $(this).parent(".item").find(".is-can-receive-document");
                                    let $checkboxCanManageUser = $(this).parent(".item").find(".is-can-manage-user");
                                    $checkboxCanReceiveDocument.removeClass("hidden");
                                    $checkboxCanManageUser.removeClass("hidden");
                                    $checkboxCanReceiveDocument.prop("checked", mapWorkUnitIdWithIsCanReceiveDocument[workUnitId]);
                                    $checkboxCanManageUser.prop("checked", mapWorkUnitIdWithIsCanManageUser[workUnitId]);
                                }
                            });

                            userEntity.roles.map(r => r.name).forEach(nameRole => {
                                if (nameRole === "MANAGE_UNIT") {
                                    self.isManageUnit = true;
                                }
                                if (nameRole === "MANAGE_FIELD") {
                                    self.isManageField = true;
                                }
                                if (nameRole === "MANAGE_REASON_DENIED_COMMENT") {
                                    self.isManageReasonDeniedComment = true;
                                }
                                if (nameRole === "MANAGE_NEWS") {
                                    self.isManageNews = true;
                                }
                            })
                        }
                    }
                })
            },

            validateForm() {
                this.validateBirthday();
                this.validateTimeBirthday();
                return !this.isShowErrorBirthday && !this.isShowErrorTimeBirthday;
            },

            validateBirthday() {
                this.isShowErrorBirthday = $("#birthday").val() == "";
            },

            validateTimeBirthday() {
                let arrBirthday = $("#birthday").val().split("/");
                let birthday = new Date(arrBirthday[2] + "-" + arrBirthday[1] + "-" + arrBirthday[0]);
                this.isShowErrorTimeBirthday = birthday.getTime() > new Date().getTime();
            },

            getWorkUnitInfo() {
                let self = this;
                $.ajax({
                    type: "GET",
                    url: "/api/admin/work_unit/getWorkUnitInfo?type=notAll",
                    success: function (response) {
                        self.listWrapUnitLv1 = response.data[0];
                    }
                })
            },

            resetPopup() {
                this.id = "";
                this.name = "";
                this.email = "";
                this.password = "";
                this.passwordConfirm = "";
                this.phone = "";
                this.gender = 1;
                this.address = "";
                this.roleId = "";
                $("#birthday").val("");
                this.$refs['image-avatar'].hasImage = false;

                this.isManageUnit = false;
                this.isManageField = false;
                this.isManageReasonDeniedComment = false;
                this.isManageNews = false;

                this.isShowErrorBirthday = false;
                this.isShowErrorTimeBirthday = false;

                $(".label-item").each(function () {
                    $(this).removeClass("active");
                })
                $(".id-item").each(function () {
                    $(this).removeClass("active");
                })
                $(".is-can-receive-document").each(function () {
                    $(this).prop("checked", false);
                    $(this).addClass("hidden");
                })
                $(".is-can-manage-user").each(function () {
                    $(this).prop("checked", false);
                    $(this).addClass("hidden");
                })
            }
        },
        created() {
            this.getWorkUnitInfo();
        },
        mounted() {
            let self = this;

            configOneDateNotTime('birthday');
            $("#birthday").val("");

            $('#modal_add_user').on('hidden.bs.modal', function () {
                self.resetPopup();
                formUser.resetForm();
            })

            $(document).on("click", ".label-item", function () {
                if ($(this).hasClass("active")) {
                    $(this).removeClass("active");
                    $(this).parent(".item").find(".id-item").removeClass("active");
                    $(this).parent(".item").find(".is-can-manage-user").addClass("hidden");
                    $(this).parent(".item").find(".is-can-receive-document").addClass("hidden");
                    return;
                }

                let $wrapItemLv1 = $(this).closest(".wrap-item-lv1");
                $wrapItemLv1.find(".label-item").each(function () {
                    $(this).removeClass("active");
                })
                $wrapItemLv1.find(".id-item").each(function () {
                    $(this).removeClass("active");
                })
                $wrapItemLv1.find(".is-can-manage-user").each(function () {
                    $(this).addClass("hidden");
                })
                $wrapItemLv1.find(".is-can-receive-document").each(function () {
                    $(this).addClass("hidden");
                })
                $(this).addClass("active");
                $(this).parent(".item").find(".id-item").addClass("active");
                $(this).parent(".item").find(".is-can-manage-user").removeClass("hidden");
                $(this).parent(".item").find(".is-can-receive-document").removeClass("hidden");
            })

            formUser = $("#form-user").validate({
                errorElement: "p",
                errorClass: "error-message",
                errorPlacement: function (error, element) {
                    error.insertAfter(element);
                },
                ignore: [],
                rules: {
                    name: {
                        required: true,
                        maxlength: 50,
                    },
                    email: {
                        required: true,
                        maxlength: 100,
                        validateFormatEmail: true,
                    },
                    phone: {
                        required: true,
                        validateFormatPhone: true,
                    },
                    password: {
                        required: true,
                        minlength: 8,
                        maxlength: 32,
                    },
                    passwordConfirm: {
                        required: true,
                        equalTo: "#password"
                    },
                    address: {
                        required: true,
                        maxlength: 200,
                    },
                },
                messages: {
                    name: {
                        required: "Tr?????ng n??y l?? b???t bu???c",
                        maxlength: "T???i ??a 50 k?? t???",
                    },
                    email: {
                        required: "Tr?????ng n??y l?? b???t bu???c",
                        maxlength: "T???i ??a 100 k?? t???",
                    },
                    phone: {
                        required: "Tr?????ng n??y l?? b???t bu???c",
                    },
                    password: {
                        required: "Tr?????ng n??y l?? b???t bu???c",
                        minlength: "M???t kh???u t??? 8 ?????n 32 k?? t???",
                        maxlength: "M???t kh???u t??? 8 ?????n 32 k?? t???",
                    },
                    passwordConfirm: {
                        required: "Tr?????ng n??y l?? b???t bu???c",
                        equalTo: "M???t kh???u v?? m???t kh???u x??c nh???n kh??ng kh???p",
                    },
                    address: {
                        required: "Tr?????ng n??y l?? b???t bu???c",
                        maxlength: "T???i ??a 200 k?? t???",
                    },
                }

            });

            $.validator.addMethod("validateFormatEmail", function (value) {
                let regex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
                return regex.test(value);
            }, "?????nh d???ng kh??ng ch??nh x??c");

            $.validator.addMethod("validateFormatPhone", function (value) {
                let regex = /^\(?([0-9]{3})\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$/;
                return regex.test(value);
            }, "?????nh d???ng kh??ng ch??nh x??c");

        }
    })

    $(document).on("click", ".detail-user", function () {
        userVue.id = Number($(this).attr("id").replace("btn_detail_", ""));
        userVue.detail();
    })

    $(document).on("click", ".change-active-user", function () {
        if ($(this).hasClass("disabled")) {
            return;
        }
        $.ajax({
            type: "POST",
            url: "/api/admin/user/changeStatus/" + Number($(this).attr('id').replace('active-user-', '')),
            beforeSend: function () {
                window.loader.show();
            },
            success: function (response) {
                window.loader.hide();
                setTimeout(function () {
                    tableUser.ajax.reload();
                }, 2000);
                if (response.status.code === 1000) {
                    window.alert.show('success', "Thay ?????i tr???ng th??i ng?????i d??ng th??nh c??ng", 2000);
                } else {
                    window.alert.show("error", "???? c?? l???i x???y ra, vui l??ng th??? l???i sau", 2000);
                }
            }
        })
    });

})