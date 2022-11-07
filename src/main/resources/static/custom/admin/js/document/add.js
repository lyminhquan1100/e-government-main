let documentVue;
$(document).ready(function () {
    let formDocument;

    documentVue = new Vue({
        el: "#modal_add_document",
        data: {
            typeModalDocument: "add",
            isReceived: false,
            workUnitIdReceive: "",

            id: "",
            isUpdateDocument: false,
            listMember: [],
            listAllViewerAndUpdater: [],
            listMemberSameWorkUnit: [],
            version_pre: 1,
            version_suf: 0,
            workUnitId: "",
            listWorkUnit: [],
            isSelectAllMemberView: false,
            isSelectAllMemberUpdate: false,
            isSelectAllMemberSameWorkUnitView: false,
            isSelectAllMemberSameWorkUnitUpdate: false,
            messageErrorSelectViewer: "Trường này là bắt buộc",
            messageErrorSelectUpdater: "Trường này là bắt buộc",

            title: "",
            target: "",
            conclude: "",
            nameCreator: "",

            isShowErrorSelectWorkUnit: false,
            isShowErrorContent: false,
            isShowErrorSelectApprover: false,
            isShowErrorSelectUpdater: false,
            isShowErrorSelectViewer: false,
            isShowErrorSelectDeleter: false,
            isShowErrorSelectSender: false,

            isHiddenBtnSave: false,
        },
        watch: {
            isSelectAllMemberView(isSelectAll) {
                this.isShowErrorSelectViewer = false;
                if (isSelectAll) {
                    this.isSelectAllMemberSameWorkUnitView = false;
                }
            },
            isSelectAllMemberUpdate(isSelectAll) {
                this.isShowErrorSelectUpdater = false;
                if (isSelectAll) {
                    this.isSelectAllMemberSameWorkUnitUpdate = false;
                }
            },
            isSelectAllMemberSameWorkUnitView(value) {
                this.isShowErrorSelectViewer = false;
                if (value) {
                    this.isSelectAllMemberView = false;
                }
            },
            isSelectAllMemberSameWorkUnitUpdate(value) {
                this.isShowErrorSelectUpdater = false;
                if (value) {
                    this.isSelectAllMemberUpdate = false;
                }
            },
            workUnitId(value) {
                if (!value || this.isUpdateDocument) {
                    return;
                }
                this.getListMemberCanTakePartIn();
                this.getListMemberSameWorkUnit();
            },

        },
        computed: {
            textBtnModal() {
                if (this.typeModalDocument === 'add') {
                    return 'Lưu'
                } else if (this.typeModalDocument === 'inviteUpdate') {
                    return 'Chấp nhận'
                } else if (this.typeModalDocument === 'receive') {
                    return 'Nhận văn bản'
                }
            },
            textHeaderModal() {
                if (this.typeModalDocument === 'add') {
                    return 'Văn bản'
                } else if (this.typeModalDocument === 'inviteUpdate') {
                    return `<span>Mời chỉnh sửa văn bản</span> 
                            <i style="margin: 0 24px" class="fa fa-arrow-right"></i> 
                            <span class="open-modal-edit" style="color: blue; text-decoration: underline; cursor: pointer;">Chỉnh sửa</span>`
                } else if (this.typeModalDocument === 'receive') {
                    return 'Nhận văn bản'
                }
            }
        },
        methods: {
            setCurrentUserInList() {
                $("#select_viewer").val(currentUserId).trigger("change");
                $("#select_updater").val(currentUserId).trigger("change");
                $("#select_approver").val(currentUserId).trigger("change");
                $("#select_deleter").val(currentUserId).trigger("change");
                $("#select_sender").val(currentUserId).trigger("change");
            },

            save() {
                let self = this;
                if (!$("#form-document").valid()) {
                    return;
                }
                if (!this.validateForm()) {
                    return;
                }

                let listViewerId = self.isSelectAllMemberView ?
                    self.listAllViewerAndUpdater.map(member => member.id).toString() :
                    self.isSelectAllMemberSameWorkUnitView ? self.listMemberSameWorkUnit.map(member => member.id).toString() : $("#select_viewer").val().toString();

                let listUpdaterId = self.isSelectAllMemberUpdate ?
                    self.listAllViewerAndUpdater.map(member => member.id).toString() :
                    self.isSelectAllMemberSameWorkUnitUpdate ? self.listMemberSameWorkUnit.map(member => member.id).toString() : $("#select_updater").val().toString()

                let data = {
                    workUnitId: self.workUnitId,
                    title: self.title,
                    target: self.target,
                    content: CKEDITOR.instances["textarea-content-document"].getData(),
                    version: Number(this.version_pre) + Number(this.version_suf * 0.1),
                    listViewerId: listViewerId,
                    listUpdaterId: listUpdaterId,
                    listApproverId: $("#select_approver").val().toString(),
                    listDeleterId: $("#select_deleter").val().toString(),
                    listSenderId: $("#select_sender").val().toString(),
                }

                if (this.id) {
                    data.id = this.id;
                }

                $.ajax({
                    type: "POST",
                    url: "/api/admin/document/save",
                    contentType: "application/json",
                    data: JSON.stringify(data),
                    beforeSend: function () {
                        window.loader.show();
                    },
                    success: function (response) {
                        window.loader.hide();
                        if (response.status.code === 1000) {
                            if (tableDocument) {
                                tableDocument.ajax.reload();
                            }
                            window.alert.show("success", "Lưu văn bản thành công", 2000);
                            $('#modal_add_document').modal("hide");
                        } else if (response.status.code === 1604) {
                            window.alert.show("error", "Tiêu đề văn bản đã tồn tại", 2000);
                        } else {
                            window.alert.show("error", "Đã có lỗi xảy ra, vui lòng thử lại sau", 2000);
                        }
                    }
                })
            },
            receive() {
                let self = this;
                $.ajax({
                    type: "POST",
                    url: "/api/admin/document/receive?workUnitIdReceive=" + self.workUnitIdReceive + "&documentId=" + self.id,
                    beforeSend: function () {
                        window.loader.show();
                    },
                    success: function (res) {
                        window.loader.hide();
                        if (res.status.code === 1000) {
                            if (tableDocument) {
                                tableDocument.ajax.reload();
                            }
                            window.alert.show("success", "Nhận văn bản thành công", 2000);
                            $('#modal_add_document').modal("hide");
                        } else {
                            window.alert.show("error", "Đã có lỗi xảy ra, vui lòng thử lại sau", 2000);
                        }
                    }
                })
            },
            detail() {
                let self = this;
                $.ajax({
                    type: "GET",
                    url: "/api/admin/document/detail/" + self.id,
                    success: function (response) {
                        if (response.status.code === 1000) {
                            let data = response.data;
                            self.isUpdateDocument = true;
                            self.listWorkUnit.push({
                                id: data.workUnitId,
                                name: data.nameWorkUnit
                            })
                            self.workUnitId = data.workUnitId;
                            self.getListMemberSameWorkUnit();
                            self.title = data.title;
                            self.target = data.target;
                            self.nameCreator = data.nameCreator;
                            if (data.concludeParse) {
                                self.conclude = data.concludeParse;
                            }
                            CKEDITOR.instances["textarea-content-document"].setData(data.content);
                            let version = data.version;
                            if (!version.toString().includes(".")) {
                                self.version_pre = version;
                                self.version_suf = 0;
                            } else {
                                self.version_pre = version.toString().split(".")[0];
                                self.version_suf = version.toString().split(".")[1];
                            }

                            self.listMember = data.listMemberCanTakePartIn;
                            setTimeout(function () {
                                $("#select_viewer").val(data.listViewerId).trigger("change");
                                $("#select_updater").val(data.listUpdaterId).trigger("change");
                                $("#select_approver").val(data.listApproverId).trigger("change");
                                $("#select_deleter").val(data.listDeleterId).trigger("change");
                                $("#select_sender").val(data.listSenderId).trigger("change");
                            }, 1000);

                            if (!data.enable) {
                                self.disabledAll();
                                return;
                            }

                            let permissionDocument = data.permissionDocument;
                            if (permissionDocument.owner) {
                                self.disabledElementWithOwner();
                                self.isHiddenBtnSave = false;
                            } else {
                                self.disabledElementDetail(permissionDocument);
                                self.isHiddenBtnSave = !permissionDocument.canEditPermission;
                            }
                        }
                    }
                })
            },
            handleReceiveDocument(document) {
                let self = this;
                self.typeModalDocument = 'receive';
                self.isReceived = document.received;
                self.isHiddenBtnSave = self.isReceived;
                self.workUnitIdReceive = document.workUnitIdReceive;
                self.id = document.id;
                self.listWorkUnit.push({
                    id: document.workUnitId,
                    name: document.nameWorkUnit
                });
                self.workUnitId = document.workUnitId;
                self.title = document.title;
                self.target = document.target;
                self.nameCreator = document.nameCreator;
                if (document.concludeParse) {
                    self.conclude = document.concludeParse;
                }
                CKEDITOR.instances["textarea-content-document"].setData(document.content);
                let version = document.version;
                if (!version.toString().includes(".")) {
                    self.version_pre = version;
                    self.version_suf = 0;
                } else {
                    self.version_pre = version.toString().split(".")[0];
                    self.version_suf = version.toString().split(".")[1];
                }
                self.disabledWhenHandleReceiveDocument();
            },
            handleInviteUpdateDocument(document) {
                let self = this;
                self.typeModalDocument = 'inviteUpdate';
                self.isHiddenBtnSave = true;
                self.id = document.id;
                self.listWorkUnit.push({
                    id: document.workUnitId,
                    name: document.nameWorkUnit
                });
                self.workUnitId = document.workUnitId;
                self.title = document.title;
                self.target = document.target;
                self.nameCreator = document.nameCreator;
                if (document.concludeParse) {
                    self.conclude = document.concludeParse;
                }
                CKEDITOR.instances["textarea-content-document"].setData(document.content);
                let version = document.version;
                if (!version.toString().includes(".")) {
                    self.version_pre = version;
                    self.version_suf = 0;
                } else {
                    self.version_pre = version.toString().split(".")[0];
                    self.version_suf = version.toString().split(".")[1];
                }
                self.disabledElementDetail(undefined);
            },
            disabledAll() {
                $(".select-work-unit").prop("disabled", true);
                $(".input-title").prop("disabled", true);
                $(".input-target").prop("disabled", true);
                CKEDITOR.instances['textarea-content-document'].setReadOnly(true);
                $(".input-closing-deadline").prop("disabled", true);
                $(".input-version-pre").prop("disabled", true);
                $(".input-version-suf").prop("disabled", true);
                $("#select-all-member-view").prop("disabled", true);
                $("#select-all-member-update").prop("disabled", true);
                $("#select_viewer").prop("disabled", true);
                $("#select_updater").prop("disabled", true);
                $("#select_deleter").prop("disabled", true);
                $("#select_approver").prop("disabled", true);
                $("#select_sender").prop("disabled", true);
                $(".btn-save-document").prop("disabled", true);
            },
            disabledWhenHandleReceiveDocument() {
                $(".select-work-unit").prop("disabled", true);
                $(".input-title").prop("disabled", true);
                $(".input-target").prop("disabled", true);
                CKEDITOR.instances['textarea-content-document'].setReadOnly(true);
                $(".input-closing-deadline").prop("disabled", true);
                $(".input-version-pre").prop("disabled", true);
                $(".input-version-suf").prop("disabled", true);
            },
            disabledElementWithOwner() {
                $(".select-work-unit").prop("disabled", true);
                $(".input-version-pre").prop("disabled", true);
                $(".input-version-suf").prop("disabled", true);
                CKEDITOR.instances['textarea-content-document'].setReadOnly(true);
            },
            disabledElementDetail(permissionDocument) {
                $(".select-work-unit").prop("disabled", true);
                $(".input-title").prop("disabled", true);
                $(".input-target").prop("disabled", true);
                $(".input-closing-deadline").prop("disabled", true);
                $(".input-version-pre").prop("disabled", true);
                $(".input-version-suf").prop("disabled", true);
                CKEDITOR.instances['textarea-content-document'].setReadOnly(true);

                if (!permissionDocument) {
                    $("#select-all-member-view").prop("disabled", true);
                    $("#select-all-member-update").prop("disabled", true);
                    $("#select_viewer").prop("disabled", true);
                    $("#select_updater").prop("disabled", true);
                    $("#select_deleter").prop("disabled", true);
                    $("#select_approver").prop("disabled", true);
                    $("#select_sender").prop("disabled", true);
                    $(".btn-save-document").prop("disabled", true);
                } else {
                    if (!permissionDocument.canEditViewer) {
                        $("#select-all-member-view").prop("disabled", true);
                        $("#select_viewer").prop("disabled", true);
                    }
                    if (!permissionDocument.canEditUpdater) {
                        $("#select-all-member-update").prop("disabled", true);
                        $("#select_updater").prop("disabled", true);
                    }
                    if (!permissionDocument.canEditApprover) {
                        $("#select_approver").prop("disabled", true);
                    }
                    if (!permissionDocument.canEditDeleter) {
                        $("#select_deleter").prop("disabled", true);
                    }
                    if (!permissionDocument.canEditSender) {
                        $("#select_sender").prop("disabled", true);
                    }
                }
            },
            enableElementDetail() {
                $(".select-work-unit").prop("disabled", false);
                $(".input-title").prop("disabled", false);
                $(".input-target").prop("disabled", false);
                $(".input-closing-deadline").prop("disabled", false);
                $(".input-version-pre").prop("disabled", false);
                $(".input-version-suf").prop("disabled", false);
                CKEDITOR.instances['textarea-content-document'].setReadOnly(false);

                $("#select-all-member-view").prop("disabled", false);
                $("#select-all-member-update").prop("disabled", false);
                $("#select_viewer").prop("disabled", false);
                $("#select_updater").prop("disabled", false);
                $("#select_deleter").prop("disabled", false);
                $("#select_approver").prop("disabled", false);
                $("#select_sender").prop("disabled", false);
                $(".btn-save-document").prop("disabled", false);
            },
            deleteDocument() {
                let self = this;
                if (self.id) {
                    $.ajax({
                        type: "POST",
                        url: "/api/admin/document/delete/" + self.id,
                        beforeSend: function () {
                            window.loader.show();
                        },
                        success: function (response) {
                            window.loader.hide();
                            if (response.status.code === 1000) {
                                tableDocument.ajax.reload();
                                window.alert.show("success", "Xóa bài viết thành công", 2000);
                                $("#modal_delete").modal("hide");
                            } else {
                                window.alert.show("error", "Đã có lỗi xảy ra, vui lòng thử lại sau", 2000);
                            }
                        }
                    })
                }
            },
            validateForm() {
                this.validateSelectWorkUnit();
                this.validateContent();
                this.validateSelectViewer();
                this.validateSelectApprover();
                this.validateSelectUpdater();
                this.validateSelectDeleter();
                this.validateSelectSender();
                return !this.isShowErrorContent
                    && !this.isShowErrorSelectApprover && !this.isShowErrorSelectUpdater
                    && !this.isShowErrorSelectViewer && !this.isShowErrorSelectDeleter
                    && !this.isShowErrorSelectSender;
            },
            validateSelectDeleter() {
                this.isShowErrorSelectDeleter = $("#select_deleter").val().toString() == "";
            },
            validateSelectSender() {
                this.isShowErrorSelectSender = $("#select_sender").val().toString() == "";
            },
            validateSelectViewer() {
                if (this.isSelectAllMemberView) {
                    this.isShowErrorSelectViewer = false;
                } else if (this.isSelectAllMemberSameWorkUnitView) {
                    this.messageErrorSelectViewer = "Không tồn tại thành viên nào";
                    this.isShowErrorSelectViewer = this.listMemberSameWorkUnit.length == 0;
                } else {
                    this.messageErrorSelectViewer = "Trường này là bắt buộc";
                    this.isShowErrorSelectViewer = $("#select_viewer").val().toString() == "";
                }
            },
            validateSelectUpdater() {
                if (this.isSelectAllMemberUpdate) {
                    this.isShowErrorSelectUpdater = false;
                } else if (this.isSelectAllMemberSameWorkUnitUpdate) {
                    this.messageErrorSelectUpdater = "Không tồn tại thành viên nào";
                    this.isShowErrorSelectUpdater = this.listMemberSameWorkUnit.length == 0;
                } else {
                    this.messageErrorSelectUpdater = "Trường này là bắt buộc";
                    this.isShowErrorSelectUpdater = $("#select_updater").val().toString() == "";
                }
            },
            validateSelectApprover() {
                this.isShowErrorSelectApprover = $("#select_approver").val().toString() == "";
            },
            validateSelectWorkUnit() {
                this.isShowErrorSelectWorkUnit = !this.workUnitId;
            },
            validateContent() {
                this.isShowErrorContent = CKEDITOR.instances["textarea-content-document"].getData().trim() == "";
            },
            resetPopup() {
                let self = this;
                self.getListWorkUnit();
                this.isUpdateDocument = false;
                this.typeModalDocument = 'add';
                this.isReceived = false;
                this.workUnitIdReceive = false;
                this.id = "";
                this.workUnitId = "";
                this.version_pre = 1;
                this.version_suf = 0;
                this.listMember = [];
                this.isSelectAllMemberView = false;
                this.isSelectAllMemberUpdate = false;
                this.isSelectAllMemberSameWorkUnitView = false;
                this.isSelectAllMemberSameWorkUnitUpdate = false;
                this.title = "";
                this.target = "";
                this.conclude = "";
                this.nameCreator = "";
                CKEDITOR.instances["textarea-content-document"].setData("");
                $("#closing-deadline").val(moment(new Date().addDays(30)).format('DD/MM/YYYY'));
                $("#select_viewer").val("").trigger("change");
                $("#select_approver").val("").trigger("change");
                $("#select_updater").val("").trigger("change");
                $("#select_deleter").val("").trigger("change");
                $("#select_sender").val("").trigger("change");

                this.isShowErrorSelectWorkUnit = false;
                this.isShowErrorSelectViewer = false;
                this.isShowErrorSelectUpdater = false;
                this.isShowErrorSelectApprover = false;
                this.isShowErrorSelectDeleter = false;
                this.isShowErrorSelectSender = false;
                setTimeout(function () {
                    self.isShowErrorContent = false;
                }, 100);

                this.isHiddenBtnSave = false;
            },
            getListMemberCanTakePartIn() {
                let self = this;
                $.ajax({
                    type: "GET",
                    url: "/api/admin/user/getListUserCanTakePartInDocument?workUnitId=" + Number(self.workUnitId),
                    success: function (response) {
                        if (response.status.code === 1000) {
                            self.listMember = response.data;
                            setTimeout(function () {
                                self.setCurrentUserInList();
                            }, 1000);
                        }
                    }
                })
            },
            getListMemberSameWorkUnit() {
                let self = this;
                $.ajax({
                    type: "GET",
                    url: "/api/admin/user/getListMemberSameWorkUnit?workUnitId=" + Number(self.workUnitId),
                    success: function (response) {
                        if (response.status.code === 1000) {
                            self.listMemberSameWorkUnit = response.data;
                        }
                    }
                })
            },
            getAllMemberForViewAndUpdate() {
                let self = this;
                $.ajax({
                    type: "GET",
                    url: "/api/admin/user/getAllMemberForViewAndUpdate",
                    success: function (response) {
                        if (response.status.code === 1000) {
                            self.listAllViewerAndUpdater = response.data;
                        }
                    }
                })
            },
            getListWorkUnit() {
                let self = this;
                $.ajax({
                    type: "GET",
                    url: "/api/admin/work_unit/getListWorkUnit",
                    success: function (response) {
                        self.listWorkUnit = response.data;
                    }
                })
            }
        },
        created() {
            this.getListWorkUnit();
            this.getAllMemberForViewAndUpdate();
        },
        mounted() {
            let self = this;

            $("#closing-deadline").val(moment(new Date().addDays(30)).format('DD/MM/YYYY'));

            $("#select_viewer").select2({
                placeholder: '',
            });
            $("#select_approver").select2({
                placeholder: '',
            });
            $("#select_updater").select2({
                placeholder: '',
            });
            $("#select_deleter").select2({
                placeholder: '',
            });
            $("#select_sender").select2({
                placeholder: '',
            });
            configOneDateNotTime('closing-deadline');

            $('#modal_add_document').on('hidden.bs.modal', function () {
                self.resetPopup();
                self.enableElementDetail();
                formDocument.resetForm();
            })

            $('#modal_delete').on('hidden.bs.modal', function () {
                self.id = "";
            })

            formDocument = $("#form-document").validate({
                errorElement: "p",
                errorClass: "error-message",
                errorPlacement: function (error, element) {
                    error.insertAfter(element);
                },
                ignore: [],
                rules: {
                    title: {
                        required: true,
                        maxlength: 200,
                    },
                    target: {
                        required: true,
                        maxlength: 200,
                    },
                },
                messages: {
                    title: {
                        required: "Trường này là bắt buộc",
                        maxlength: "Tối đa 200 ký tự",
                    },
                    target: {
                        required: "Trường này là bắt buộc",
                        maxlength: "Tối đa 200 ký tự",
                    },
                }
            });
        }
    })

    $(document).on("click", ".detail-document", function () {
        documentVue.id = Number($(this).attr("id").replace("btn_detail_", ""));
        documentVue.detail();
    })

    $(document).on("click", ".delete-document", function () {
        documentVue.id = Number($(this).attr("id").replace("btn_delete_", ""));
    })

    $(document).on("click", "#btn_submit_delete", function () {
        documentVue.deleteDocument();
    })

    $(document).on("click", ".change-enable-document", function () {
        $.ajax({
            type: "POST",
            url: "/api/admin/document/changeEnable/" + Number($(this).attr('id').replace('enable-document-', '')),
            beforeSend: function () {
                window.loader.show();
            },
            success: function (response) {
                window.loader.hide();
                setTimeout(function () {
                    window.location.reload();
                }, 2000);
                if (response.status.code === 1000) {
                    window.alert.show('success', "Thay đổi trạng thái văn bản thành công", 2000);
                } else {
                    window.alert.show("error", "Đã có lỗi xảy ra, vui lòng thử lại sau", 2000);
                }
            }
        })
    });

    $(document).on("click", ".change-public-document", function () {
        $.ajax({
            type: "POST",
            url: "/api/admin/document/changePublic/" + Number($(this).attr('id').replace('public-document-', '')),
            beforeSend: function () {
                window.loader.show();
            },
            success: function (response) {
                window.loader.hide();
                setTimeout(function () {
                    window.location.reload();
                }, 2000);
                if (response.status.code === 1000) {
                    window.alert.show('success', "Thay đổi công khai văn bản thành công", 2000);
                } else {
                    window.alert.show("error", "Đã có lỗi xảy ra, vui lòng thử lại sau", 2000);
                }
            }
        })
    });

    $(document).on("click", ".open-modal-edit", function () {
        updateContentVue.id = documentVue.id;
        updateContentVue.getContentDocument();
        $('#modal_add_document').modal("hide");
        $('#modal_update_content_document').modal("show");
    })

    let contentDocument = CKEDITOR.replace('textarea-content-document', {
        language: 'vi',
        height: 200,
        removePlugins: 'elementspath'
    });
    CKEDITOR.config.toolbar = [
        ['Styles', 'Format', 'Font', 'FontSize'],
        ['Bold', 'Italic', 'Underline', 'StrikeThrough', '-', 'Undo', 'Redo', '-', 'Cut', 'Copy', 'Paste', 'Find', 'Replace', '-', 'Outdent', 'Indent', '-', 'Print'],
        ['NumberedList', 'BulletedList', '-', 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock'],
        ['Image', 'Table', '-', 'Link', 'Flash', 'Smiley', 'TextColor', 'BGColor', 'Source']
    ];

    contentDocument.on('change', function () {
        documentVue.validateContent();
    });
})