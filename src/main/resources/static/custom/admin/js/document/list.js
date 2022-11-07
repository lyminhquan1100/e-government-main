$(document).ready(function () {

    let columnDefinitions = [
        {"data": "nameWorkUnit", "orderable": false, "defaultContent": "", "class": 'text-center'},
        {"data": "title", "orderable": false, "defaultContent": "", "class": 'text-center'},
        {"data": "target", "orderable": false, "defaultContent": "", "class": 'text-center'},
        {"data": "version", "orderable": false, "defaultContent": "", "class": 'text-center'},
        {"data": "nameCreator", "orderable": false, "defaultContent": "", "class": 'text-center'},
        {"data": "createdTime", "orderable": false, "defaultContent": "", "class": 'text-center'},
        {"data": null, "orderable": false, "defaultContent": "", "class": 'text-center'},
        {"data": null, "orderable": false, "defaultContent": "", "class": 'text-center'},
        {"data": null, "orderable": false, "defaultContent": "", "class": 'text-center'},
    ];

    let getPageDiscussionPost = function (requestData, renderFunction, link_api) {

        let params = {
            "page": (requestData.start / requestData.length) + 1,
            "size": requestData.length,
            "sortField": "createdTime",
            "sortDir": "desc",
            "keyword": $("#search_document").val(),
        };
        jQuery.get(link_api, params, function (response) {
            let content = {
                "draw": requestData.draw,
                "recordsTotal": response.totalElements,
                "recordsFiltered": response.totalElements,
                "data": response.content
            };
            renderFunction(content);
        });
    };

    tableDocument = $("#document_table").DataTable({
        "language": {
            "url": "/libs/new_data_table/js/vie.json"
        },
        "lengthMenu": [
            [10, 20, 50],
            [10, 20, 50]
        ],

        "searching": false,
        rowId: 'id',
        "ordering": true,
        "pagingType": "full_numbers",
        "serverSide": true,
        "columns": columnDefinitions,
        "ajax": function (data, callback) {
            getPageDiscussionPost(data, callback, "/api/admin/document/getPage");
        },
        columnDefs: [
            {
                "render": function (version) {
                    if (!version.toString().includes(".")) {
                        return version + ".0";
                    }
                    return version;
                },
                "targets": 3
            },
            {
                "render": function (data) {
                    let isOwnerDocument = data.createdByUserId == currentUserId;
                    let result;
                    if (data.enable === true) {
                        result = `<div class="wrap-switch"> <label class="switch">
										  <input id="enable-document-${data.id}" class="change-enable-document" type="checkbox" checked>
										  <span class="slider round"></span>
										</label> </div>`;
                    } else {
                        result = `<div class="wrap-switch"> <label class="switch">
										  <input id="enable-document-${data.id}" class="change-enable-document" type="checkbox">
										  <span class="slider round"></span>
										</label> </div>`;
                    }
                    $("#enable-document-" + data.id).prop("disabled", !isOwnerDocument);
                    return result;
                },
                "targets": 6
            },
            {
                "render": function (data) {
                    let isOwnerDocument = data.createdByUserId == currentUserId;
                    let result;
                    if (data.public === true) {
                        result = `<div class="wrap-switch"> <label class="switch">
										  <input id="public-document-${data.id}" class="change-public-document" type="checkbox" checked>
										  <span class="slider round"></span>
										</label> </div>`;
                    } else {
                        result = `<div class="wrap-switch"> <label class="switch">
										  <input id="public-document-${data.id}" class="change-public-document" type="checkbox">
										  <span class="slider round"></span>
										</label> </div>`;
                    }
                    $("#public-document-" + data.id).prop("disabled", !isOwnerDocument);
                    return result;
                },
                "targets": 7
            },
            {
                "render": function (data) {
                    let divDisplayed = ``;
                    let isEnable = data.enable;
                    if (data.listViewerId.includes(Number(currentUserId))) {
                        divDisplayed += `<button style="margin: 2px 0" type="button" data-toggle="modal" data-target="#modal_add_document" id="btn_detail_${data.id}" class="btn btn-sm btn-primary detail-document">Chi tiết</button>`;
                    }
                    if (data.listUpdaterId.includes(Number(currentUserId))) {
                        divDisplayed += `<button style="margin: 2px 2px 2px 10px;" type="button" data-toggle="modal" data-target="#modal_update_content_document" id="btn_update_content_${data.id}" class="btn btn-sm btn-primary update-content-document">Chỉnh sửa nội dung</button>`;
                        $("#btn_update_content_" + data.id).prop("disabled", !isEnable);
                    }
                    if (data.listApproverId.includes(Number(currentUserId))) {
                        divDisplayed += `<button style="margin: 2px 2px 2px 10px;" type="button" data-toggle="modal" data-target="#modal_approve_document" id="btn_approve_content_${data.id}" class="btn btn-sm btn-primary approve-content-document">Phê duyệt</button>`;
                        $("#btn_approve_content_" + data.id).prop("disabled", !isEnable);
                    }
                    if (data.listSenderId.includes(Number(currentUserId))) {
                        divDisplayed += `<button style="margin: 2px 2px 2px 10px;" data-toggle="modal" id="btn_send_${data.id}_${data.workUnitId}" data-target="#modal_send_document" class="btn btn-sm btn-primary send-document">Gửi</button>`;
                        $("#btn_send_" + data.id + "_" + data.workUnitId).prop("disabled", !isEnable);
                    }
                    if (data.listDeleterId.includes(Number(currentUserId))) {
                        divDisplayed += `<button style="margin: 2px 2px 2px 10px;" data-toggle="modal" id="btn_delete_${data.id}" data-target="#modal_delete" class="btn btn-sm btn-danger delete-document">Xóa</button>`;
                    }
                    if (data.listViewerId.includes(Number(currentUserId))) {
                        divDisplayed += `<button style="margin: 2px 2px 2px 10px;" type="button" data-title="${data.title}" id="btn_download_${data.id}" class="btn btn-sm btn-primary download-document">Tải xuống</button>`;
                    }

                    return divDisplayed;
                },
                "targets": 8
            },
        ],
        rowCallback: function (row, data) {
            // case quá thời gian đưa ra kết luận, mà vẫn chưa đưa ra kết luận
            // if (moment(data.closingDeadline).isBefore(new Date()) && !data.conclude) {
            //     $('td', row).css('background-color', '#ffffdd');
            // }
        },

    });

    $(document).on("click", "#btn_search", function () {
        tableDocument.ajax.reload();
    });

    $("#search_document").on("keypress", function (e) {
        if (e.keyCode === 13) {
            tableDocument.ajax.reload();
        }
    })

    $(document).on("click", ".download-document", function () {
        let documentId = Number($(this).attr("id").replace("btn_download_", ""));
        let titleDocument = $(this).attr("data-title");
        $.ajax({
            type: "GET",
            url: "/api/admin/document/getContent?id=" + documentId,
            success: function (response) {
                let contentDocument = response.data;

                let preHtml = "<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'><head><meta charset='utf-8'<title>Export HTML to Doc</title></head>";
                let postHtml = "</body></html>";
                let html = preHtml + contentDocument + postHtml;

                let blob = new Blob(['\ufeff', html], {
                    type: 'application/msword'
                })

                let url = 'data:application/vnd.ms-word;charset=utf-8,' + encodeURIComponent(html);
                let fileName = titleDocument + ".doc";

                let downloadLink = document.createElement("a");
                document.body.append(downloadLink);
                if (navigator.msSaveOrOpenBlob) {
                    navigator.msSaveOrOpenBlob(blob, fileName);
                } else {
                    downloadLink.href = url;
                    downloadLink.download = fileName;
                    downloadLink.click();
                }

                document.body.removeChild(downloadLink);
            }
        })

    })

})