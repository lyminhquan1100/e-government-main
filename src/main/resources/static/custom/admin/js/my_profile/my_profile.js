$(document).ready(function () {
    let formDetailUser;
    let currentUserId = Number($("#current-user-id").val());
    let myProfile = new Vue({
        el: "#my_profile",
        data: {
            name: "",
            email: "",
            gender: "",
            phone: "",
            address: "",
            birthday: "",
            urlAvatar: "",
            listWorkUnit: [],
        },
        methods: {
            getMyProfile() {
                if (currentUserId == 0) {
                    return;
                }
                let self = this;

                $.ajax({
                    type: "GET",
                    url: "/api/admin/user/my_profile/" + currentUserId,
                    success: function (response) {
                        if (response.status.code === 1000) {
                            let userEntity = response.data;
                            self.name = userEntity.name;
                            self.email = userEntity.email;
                            self.gender = userEntity.genderStr;
                            self.phone = userEntity.phone;
                            self.address = userEntity.address;
                            self.birthday = userEntity.birthdayStr;
                            self.urlAvatar = userEntity.urlAvatar;
                            self.listWorkUnit = userEntity.listWorkUnit;
                        }
                    }
                })
            },
        },
        mounted() {
            let self = this;
            self.getMyProfile();

            $(document).on("click", "#btn-edit-profile", function () {
                $("#modal_add_user").modal("show");
                modalDetailUser.detailUser();
            })

            /* show large image */
            $(document).on("click", ".img-avatar", function () {
                let srcImage = $(this).attr("src");
                $("#large-image").attr("src", srcImage);
                $("#show_image_popup").show();
                $("#overlay").show();
            })

            /* close large image */
            $(document).mouseup(function (e) {
                let imageLarge = $("#show_image_popup");
                if (!imageLarge.is(e.target) && imageLarge.has(e.target).length === 0) {
                    imageLarge.hide();
                    $("#overlay").hide();
                }
            });
        }
    });

    let modalDetailUser = new Vue({
        el: "#modal_add_user",
        data: {
            id: "",
            name: "",
            email: "",
            gender: 1,
            phone: "",
            address: "",

            isShowErrorBirthday: false,
            isShowErrorTimeBirthday: false,
        },
        methods: {
            detailUser() {
                let self = this;
                $.ajax({
                    type: "GET",
                    url: "/api/admin/user/detail/" + currentUserId,
                    success: function (response) {
                        if (response.status.code === 1000) {
                            let userEntity = response.data;
                            self.id = userEntity.id;
                            self.name = userEntity.name;
                            self.email = userEntity.email;
                            self.phone = userEntity.phone;
                            self.gender = userEntity.gender;
                            self.address = userEntity.address;
                            if (userEntity.urlAvatar) {
                                self.$refs['image-avatar'].hasImage = true;
                                $(self.$refs['image-avatar'].$el).find('img').attr("src", userEntity.urlAvatar);
                            }
                            if (userEntity.birthday) {
                                $("#birthday").val(userEntity.birthday);
                            }
                        }
                    }
                });
            },

            save() {
                let self = this;
                if (!$("#form-detail-user").valid()) {
                    return;
                }

                if (!this.validateForm()) {
                    return;
                }

                let data = new FormData();
                data.append("id", this.id);
                data.append("name", this.name);
                data.append("email", this.email);
                data.append("birthday", $("#birthday").val());
                data.append("phone", this.phone);
                data.append("address", this.address);
                data.append("gender", this.gender);
                data.append("isHaveAvatar", this.$refs['image-avatar'].hasImage);
                if (self.$refs['image-avatar'].getImageSelected()) {
                    data.append("avatar", self.$refs['image-avatar'].getImageSelected());
                }

                $.ajax({
                    type: "POST",
                    url: "/api/admin/user/update_profile",
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
                            window.alert.show("success", "L??u th??nh c??ng", 2000);
                            $('#modal_add_user').modal("hide");
                            myProfile.getMyProfile();
                        } else if (code === 1600) {
                            window.alert.show("error", "Email ???? t???n t???i", 2000);
                        } else {
                            window.alert.show("error", "???? c?? l???i x???y ra, vui l??ng th??? l???i sau", 2000);
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

            resetPopup() {
                this.id = "";
                this.name = "";
                this.email = "";
                this.phone = "";
                this.address = "";
                $("#birthday").val("");
                this.$refs['image-avatar'].hasImage = false;

                this.isShowErrorBirthday = false;
                this.isShowErrorTimeBirthday = false;
            }
        },
        mounted() {
            let self = this;

            configOneDateNotTime('birthday');
            $("#birthday").val("");

            $('#modal_add_user').on('hidden.bs.modal', function () {
                self.resetPopup();
                formDetailUser.resetForm();
            })

            formDetailUser = $("#form-detail-user").validate({
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
})