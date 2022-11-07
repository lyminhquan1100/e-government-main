$(document).ready(function () {
    let MODAL_SEND_DOCUMENT_VUE = new Vue({
        el: "#modal_send_document",
        data: {
            listWrapUnitLv1: [],
            documentId: "",
            workUnitId: "",
        },
        methods: {
            getListWorkUnit() {
                let self = this;
                $.ajax({
                    type: "GET",
                    url: "/api/admin/work_unit/getWorkUnitInfo?type=all",
                    success: function (response) {
                        self.listWrapUnitLv1 = response.data[0];
                    }
                })
            },
            checkDisabledWorkUnit() {
                let self = this;
                $(".id-item").each(function () {
                    let workUnitId = $(this).val();
                    if (workUnitId == self.workUnitId) {
                        $(this).addClass("disabled");
                        $(this).parent(".item").children(".label-item").addClass("disabled");
                    }
                })
            },
            okSendDocument() {
                let listWorkUnitIdReceiveAndPermission = [];
                $(".id-item.active").each(function () {
                    let $itemParent = $(this).parent(".item");
                    listWorkUnitIdReceiveAndPermission.push({
                        workUnitId: Number($(this).val()),
                        canEditViewer: $itemParent.find(".is-can-edit-viewer").is(":checked"),
                        canEditUpdater: $itemParent.find(".is-can-edit-updater").is(":checked"),
                        canEditApprover: $itemParent.find(".is-can-edit-approver").is(":checked"),
                        canEditDeleter: $itemParent.find(".is-can-edit-deleter").is(":checked"),
                        canEditSender: $itemParent.find(".is-can-edit-sender").is(":checked"),
                    });
                })
                if (listWorkUnitIdReceiveAndPermission.length == 0) {
                    window.alert.show("error", "Chưa có đơn vị nào được chọn", 2000);
                    return;
                }
                let data = {
                    documentId: this.documentId,
                    listWorkUnitIdReceiveAndPermission: listWorkUnitIdReceiveAndPermission,
                }
                $.ajax({
                    type: "POST",
                    url: "/api/admin/document/sendDocument",
                    data: JSON.stringify(data),
                    contentType: "application/json",
                    beforeSuccess: function () {
                        window.loader.show();
                    },
                    success: function (response) {
                        window.loader.hide();
                        $("#modal_send_document").modal("hide");
                        if (response.status.code === 1000) {
                            window.alert.show("success", "Gửi văn bản thành công", 2000);
                        } else {
                            window.alert.show("error", "Đã có lỗi xảy ra, vui lòng thử lại sau", 2000);
                        }
                    }
                })
            },
            resetPopup() {
                this.documentId = "";
                $(".id-item").each(function () {
                    $(this).removeClass("disabled active");
                    $(this).parent(".item").children(".label-item").removeClass("disabled active");
                })
                $(".is-can-edit-viewer").addClass("hidden").prop("checked", false);
                $(".is-can-edit-updater").addClass("hidden").prop("checked", false);
                $(".is-can-edit-approver").addClass("hidden").prop("checked", false);
                $(".is-can-edit-deleter").addClass("hidden").prop("checked", false);
                $(".is-can-edit-sender").addClass("hidden").prop("checked", false);
            },
            getListInfoWorkUnitSent() {
                let self = this;
                // todo: lấy ra thông tin danh sách những workUnit đã được gửi văn bản
                $.ajax({
                    type: "GET",
                    url: "/api/admin/document-receive/get-list-info-work-unit-sent?documentId=" + self.documentId,
                    success: function (response) {
                        if (response.status.code === 1000) {
                            let listWorkUnitSentAndPermission = response.data;
                            let listWorkUnitId = listWorkUnitSentAndPermission.map(obj => obj.workUnitId);
                            let mapObjByWorkUnitId = new Map(listWorkUnitSentAndPermission.map(obj => [obj.workUnitId, obj]));
                            $(".id-item").each(function () {
                                let workUnitId = Number($(this).val());
                                if (listWorkUnitId.includes(workUnitId)) {
                                    $(this).addClass("active");
                                    let $itemParent = $(this).parent(".item");
                                    $itemParent.find(".label-item").addClass("active can-not-edit");
                                    let objPermission = mapObjByWorkUnitId.get(workUnitId);
                                    $itemParent.find(".is-can-edit-viewer").removeClass("hidden").prop("checked", objPermission.canEditViewer);
                                    $itemParent.find(".is-can-edit-updater").removeClass("hidden").prop("checked", objPermission.canEditUpdater);
                                    $itemParent.find(".is-can-edit-approver").removeClass("hidden").prop("checked", objPermission.canEditApprover);
                                    $itemParent.find(".is-can-edit-deleter").removeClass("hidden").prop("checked", objPermission.canEditDeleter);
                                    $itemParent.find(".is-can-edit-sender").removeClass("hidden").prop("checked", objPermission.canEditSender);
                                }
                            })
                        }
                    }
                })
            }
        },
        mounted() {
            let self = this;
            self.getListWorkUnit();

            $(document).on("click", ".send-document", function () {
                let documentIdAndWorkUnitId = $(this).attr("id").replace("btn_send_", "");
                self.documentId = Number(documentIdAndWorkUnitId.split("_")[0]);
                self.workUnitId = Number(documentIdAndWorkUnitId.split("_")[1]);
                self.getListInfoWorkUnitSent();
                self.checkDisabledWorkUnit();
            })

            $(document).on("click", ".label-item", function () {
                if ($(this).hasClass("disabled") || $(this).hasClass("can-not-edit")) {
                    return;
                }
                $(this).toggleClass("active");
                let $itemParent = $(this).parent(".item");
                $itemParent.children(".id-item").toggleClass("active");

                $itemParent.find(".is-can-edit-viewer").toggleClass("hidden");
                $itemParent.find(".is-can-edit-updater").toggleClass("hidden");
                $itemParent.find(".is-can-edit-approver").toggleClass("hidden");
                $itemParent.find(".is-can-edit-deleter").toggleClass("hidden");
                $itemParent.find(".is-can-edit-sender").toggleClass("hidden");
            })

            $('#modal_send_document').on('hidden.bs.modal', function () {
                self.resetPopup();
            })
        }
    })
})