let tableUser;
$(document).ready(function () {
    let columnDefinitions = [
        {"data": "name", "orderable": false, "defaultContent": "", "class": 'text-center'},
        {"data": "listWorkUnitName", "orderable": false, "defaultContent": "", "class": 'text-center'},
        {"data": "email", "orderable": false, "defaultContent": "", "class": 'text-center'},
        {"data": "birthday", "orderable": false, "defaultContent": "", "class": 'text-center'},
        {"data": "gender", "orderable": false, "defaultContent": "", "class": 'text-center'},
        {"data": "phone", "orderable": false, "defaultContent": "", "class": 'text-center'},
        {"data": "address", "orderable": false, "defaultContent": "", "class": 'text-center'},
        {"data": null, "orderable": false, "defaultContent": "", "class": 'text-center'},
        {"data": null, "orderable": false, "defaultContent": "", "class": 'text-center'},
    ];

    let getPageUser = function (requestData, renderFunction, link_api) {

        let params = {
            "page": (requestData.start / requestData.length) + 1,
            "size": requestData.length,
            "sortField": "createdTime",
            "sortDir": "desc",
            "keywordNameUser": $("#search_by_name_user").val(),
            "keywordNameWorkUnit": $("#search_by_work_unit").val(),
        };
        jQuery.get(link_api, params, function (response) {
            let content = {
                "draw": requestData.draw,
                "recordsTotal": response.totalElements,
                "recordsFiltered": response.totalElements,
                "data": response.content
            };
            renderFunction(content);
            window.loader.hide();
        });
    };

    tableUser = $("#user_table").DataTable({
        "language": {
            "url": "/libs/new_data_table/js/vie.json"
        },
        "lengthMenu": [
            [10, 20, 50],
            [10, 20, 50]
        ],

        "searching": false,
        rowId: 'id',
        "ordering": false,
        "pagingType": "full_numbers",
        "serverSide": true,
        "columns": columnDefinitions,
        "ajax": function (data, callback) {
            getPageUser(data, callback, "/api/admin/user/getPage");
        },
        columnDefs: [
            {
                "render": function (listWorkUnitName) {
                    let result = `<ul style="list-style: disc; text-align: left; margin-left: 12px">`;
                    listWorkUnitName.forEach(function (nameWorkUnit) {
                        result += `<li>${nameWorkUnit}</li>`;
                    })
                    result += `</ul>`;
                    return result;
                },
                "targets": 1
            },
            {
                "render": function (birthday) {
                    if (!birthday) {
                        return "";
                    }
                    return moment(birthday).format("DD//MM/YYYY");
                },
                "targets": 3
            },
            {
                "render": function (gender) {
                    if (gender == 1) {
                        return "Nam";
                    } else if (gender == 2) {
                        return "Nữ";
                    } else if (gender == 9) {
                        return "Khác";
                    } else {
                        return "";
                    }
                },
                "targets": 4
            },
            {
                "render": function (data) {
                    let isCurrentUser = data.id == currentUserId;
                    let result;
                    if (data.status === 1) {
                        result = `<div class="wrap-switch"> <label class="switch">
										  <input id="active-user-${data.id}" class="change-active-user" type="checkbox" checked>
										  <span class="slider round"></span>
										</label> </div>`;
                    } else {
                        result = `<div class="wrap-switch"> <label class="switch">
										  <input id="active-user-${data.id}" class="change-active-user" type="checkbox">
										  <span class="slider round"></span>
										</label> </div>`;
                    }
                    $("#active-user-" + data.id).prop("disabled", isCurrentUser);
                    return result;
                },
                "targets": 7
            },
            {
                "render": function (data) {
                    return `<button type="button" data-toggle="modal" data-target="#modal_add_user" id="btn_detail_${data.id}" class="btn btn-sm btn-primary detail-user">Chi tiết</button>`;
                },
                "targets": 8
            },
        ]
    });

    $(document).on("click", "#btn_search_by_name_user", function () {
        tableUser.ajax.reload();
    });

    $(document).on("click", "#btn_search_by_work_unit", function () {
        tableUser.ajax.reload();
    });

    $("#search_by_name_user").on("keypress", function (e) {
        if (e.keyCode === 13) {
            tableUser.ajax.reload();
        }
    })

    $("#search_by_work_unit").on("keypress", function (e) {
        if (e.keyCode === 13) {
            tableUser.ajax.reload();
        }
    })

})