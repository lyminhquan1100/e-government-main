let updateContentVue;
$(document).ready(function () {
    updateContentVue = new Vue({
        el: "#modal_update_content_document",
        data: {
            id: "",
            isShowErrorContent: false,
        },
        methods: {
            resetPopup() {
                let self = this;
                self.id = "";
                CKEDITOR.instances["textarea-update-content-document"].setData("");
                setTimeout(function () {
                    self.isShowErrorContent = false;
                }, 100);
            },
            getContentDocument() {
                let self = this;
                $.ajax({
                    type: "GET",
                    url: "/api/admin/content_document_update/getContentUpdated?documentId=" + self.id,
                    success: function (response) {
                        if (response.status.code === 1000) {
                            CKEDITOR.instances["textarea-update-content-document"].setData(response.data);
                        }
                    }
                })
            },
            validateContent() {
                this.isShowErrorContent = CKEDITOR.instances["textarea-update-content-document"].getData().trim() == "";
                return !this.isShowErrorContent;
            },
            updateContent() {
                if (!this.validateContent()) {
                    return;
                }
                let data = {
                    id: this.id,
                    content: CKEDITOR.instances["textarea-update-content-document"].getData()
                }
                $.ajax({
                    type: "POST",
                    url: "/api/admin/content_document_update/updateContent",
                    contentType: "application/json",
                    data: JSON.stringify(data),
                    beforeSend: function () {
                        window.loader.show();
                    },
                    success: function (response) {
                        window.loader.hide();
                        if (response.status.code === 1000) {
                            window.alert.show("success", "Đã gửi yêu cầu chỉnh sửa văn bản", 1000);
                            $("#modal_update_content_document").modal("hide");
                        }
                    }
                })
            }
        },
        mounted() {
            let self = this;
            $('#modal_update_content_document').on('hidden.bs.modal', function () {
                self.resetPopup();
            })

        },
    });

    $(document).on("click", ".update-content-document", function () {
        updateContentVue.id = Number($(this).attr("id").replace("btn_update_content_", ""));
        updateContentVue.getContentDocument();
    })

    let updateContentDocument = CKEDITOR.replace('textarea-update-content-document', {
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

    updateContentDocument.on('change', function () {
        updateContentVue.validateContent();
    });
})