let showModalDetailContentDocument;
$(document).ready(function () {
    let documentId;

    showModalDetailContentDocument = function (data) {
        documentId = data.documentId;
        $("#modal_detail_change_content_document").modal("show");
        $("#name-updater").text(data.nameUpdater);
        $("#title-document").text(data.titleDocument);
        CKEDITOR.instances["textarea-old-content"].setData(data.oldContent);
        CKEDITOR.instances["textarea-new-content"].setData(data.newContent);
    }

    $('#modal_detail_change_content_document').on('hidden.bs.modal', function () {
        CKEDITOR.instances["textarea-old-content"].setData("");
        CKEDITOR.instances["textarea-new-content"].setData("");
    })

    $(document).on("click", ".open-modal-approve", function () {
        modalApproveDocumentVue.documentId = documentId;
        modalApproveDocumentVue.getDataForApprove();
        $("#modal_detail_change_content_document").modal("hide");
        $("#modal_approve_document").modal("show");
    })

    let oldContent = CKEDITOR.replace('textarea-old-content', {
        language: 'vi',
        height: 200,
        removePlugins: 'elementspath'
    });
    let newContent = CKEDITOR.replace('textarea-new-content', {
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
});