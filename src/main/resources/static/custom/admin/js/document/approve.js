let modalApproveDocumentVue;
$(document).ready(function () {
    modalApproveDocumentVue = new Vue({
        el: "#modal_approve_document",
        data: {
            documentId: "",
            version_pre: 1,
            version_suf: 0,
            version_current_pre: 1,
            version_current_suf: 0,
            listUpdateBackUp: [],
            listUpdate: [],

            typeVersion: "KEEP_VERSION",
            listOldVersion: [],
            oldVersionId: "",

            versionCurrent: "",
            contentCurrent: "",

            isShowErrorVersion: false,
            isShowErrorContentSummary: false,
        },
        watch: {
            oldVersionId(oldVersionId) {
                if (!oldVersionId) {
                    return;
                }
                this.getContentOldVersion(oldVersionId);
            },
            typeVersion(typeVersion) {
                if (typeVersion === "KEEP_VERSION") {
                    this.isShowErrorVersion = false;
                }
            }
        },
        methods: {
            getDataForApprove() {
                let self = this;
                $.ajax({
                    type: "GET",
                    url: "/api/admin/document/getDataForApproveDocument?documentId=" + self.documentId,
                    success: function (response) {
                        if (response.status.code === 1000) {
                            let data = response.data;
                            self.contentCurrent = data.contentCurrent;
                            CKEDITOR.instances["textarea-content-summary"].setData(data.contentCurrent);
                            self.versionCurrent = Number(data.versionCurrent);
                            let version = data.versionCurrent;
                            if (version.toString().includes(".")) {
                                self.version_pre = version.toString().split(".")[0];
                                self.version_suf = version.toString().split(".")[1];
                            } else {
                                self.version_pre = version;
                                self.version_suf = 0;
                            }
                            self.version_current_pre = self.version_pre;
                            self.version_current_suf = self.version_suf;
                            self.listUpdate = data.listUpdate;
                            self.listUpdateBackUp = JSON.parse(JSON.stringify(self.listUpdate));

                            self.checkChanged();
                        }
                    }
                })
            },
            checkChanged() {
                let self = this;
                self.listUpdate = JSON.parse(JSON.stringify(self.listUpdateBackUp));

                let rowsSummary = self.getRowsSummary();
                this.listUpdate.forEach((obj, idx) => {
                    let contentUpdate = jQuery('<div>').html(obj.contentUpdate).html()
                        .replaceAll("&nbsp;", "");
                    let rowsItem = contentUpdate.split("\n").filter(row => row !== '');
                    self.compareAndHighlight(rowsItem, rowsSummary, idx);
                })
            },
            getRowsSummary() {
                let contentSummary = jQuery('<div>').html(CKEDITOR.instances["textarea-content-summary"].getData()).html()
                    .replaceAll("&nbsp;", "");
                return contentSummary.split("\n").filter(row => row !== '');
            },
            compareAndHighlight(rowsItem, rowsSummary, idx) {
                let result = [];
                while (rowsItem.length < rowsSummary.length) {
                    rowsItem.push('<p></p>');
                }
                while (rowsItem.length > rowsSummary.length) {
                    rowsSummary.push('<p></p>');
                }
                let lengthRows = rowsItem.length;
                for (let i = 0; i < lengthRows; i++) {
                    // highlight
                    if (rowsItem[i] != rowsSummary[i]) {
                        result.push(`<div class="highlight">
                                        <span>${jQuery('<div>').html(rowsItem[i]).text()}</span>
                                        <div class="icon-move" id="icon-move_${i}">
                                            <i class="fa fa-chevron-right"></i>
                                        </div>
                                    </div>`)
                    } else {
                        result.push(`<p>${jQuery('<div>').html(rowsItem[i]).text()}</p>`)
                    }
                }
                this.listUpdate[idx].contentUpdate = result.join('\n');
            },
            validateForm() {
                this.validateVersion();
                this.validateContentSummary();
                return !this.isShowErrorVersion && !this.isShowErrorContentSummary;
            },
            validateVersion() {
                let versionNew = Number(this.version_pre) + Number(this.version_suf * 0.1);
                this.isShowErrorVersion = this.typeVersion == "NEW_VERSION" ? versionNew <= this.versionCurrent : false;
            },
            validateContentSummary() {
                this.isShowErrorContentSummary = !CKEDITOR.instances["textarea-content-summary"].getData();
            },

            getContentOldVersion(oldVersionId) {
                $.ajax({
                    type: "GET",
                    url: "/api/admin/old_version_document/getContentOldVersion?oldVersionId=" + oldVersionId,
                    success: function (response) {
                        if (response.status.code === 1000) {
                            CKEDITOR.instances["textarea-content-summary"].setData(response.data);
                        }
                    }
                })
            },

            saveContentSummary() {
                let self = this;
                if (!self.validateForm()) {
                    return;
                }

                let data = {
                    documentId: self.documentId,
                    versionNew: Number(this.version_pre) + Number(this.version_suf * 0.1),
                    contentNew: CKEDITOR.instances["textarea-content-summary"].getData(),
                }
                if (self.typeVersion === "KEEP_VERSION") {
                    data.versionNew = Number(this.version_current_pre) + Number(this.version_current_suf * 0.1);
                } else {
                    data.versionNew = Number(this.version_pre) + Number(this.version_suf * 0.1)
                }
                $.ajax({
                    type: "POST",
                    url: "/api/admin/document/saveApproveContent",
                    data: JSON.stringify(data),
                    contentType: "application/json",
                    beforeSend: function () {
                        window.loader.show();
                    },
                    success: function (response) {
                        window.loader.hide();
                        if (response.status.code === 1000) {
                            window.alert.show("success", "Đã lưu nội dung cho phiên bản mới", 2000);
                            $("#modal_approve_document").modal("hide");
                            tableDocument.ajax.reload();
                        }
                    }
                })
            },
            getListOldVersion() {
                let self = this;
                $.ajax({
                    type: "GET",
                    url: "/api/admin/old_version_document/getListOldVersion?documentId=" + self.documentId,
                    success: function (response) {
                        self.listOldVersion = response.data;
                    }
                })
            },
            getTextVersion(version) {
                if (!version.toString().includes(".")) {
                    return version + ".0"
                }
                return version;
            },
            resetPopup() {
                this.documentId = "";
                this.version_pre = 1;
                this.version_suf = 0;
                this.listUpdate = [];
                this.versionCurrent = "";
                this.contentCurrent = "";
                this.isShowErrorVersion = false;
                this.isShowErrorContentSummary = false;
                this.typeVersion = "KEEP_VERSION";
                this.oldVersionId = "";
            }
        },
        mounted() {
            let self = this;

            $(document).on("click", ".approve-content-document", function () {
                self.documentId = $(this).attr("id").replace("btn_approve_content_", "");
                self.getDataForApprove();
                self.getListOldVersion();
            })

            $(document).on("click", ".icon-move", function () {
                let contentMoved = $(this).parent(".highlight").find("span").text();
                let indexReplaced = Number($(this).attr("id").replace("icon-move_", ""));
                let rowsSummary = self.getRowsSummary();
                rowsSummary[indexReplaced] = `<p>${contentMoved}</p>`;
                CKEDITOR.instances["textarea-content-summary"].setData(rowsSummary.join("\n"));

                self.checkChanged();
            })

            $("#modal_approve_document").on("hidden.bs.modal", function () {
                self.resetPopup();
            })
        },
    });

    let contentSummary = CKEDITOR.replace('textarea-content-summary', {
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

    contentSummary.on('change', function () {
        modalApproveDocumentVue.validateContentSummary();
        modalApproveDocumentVue.checkChanged();
    });
})