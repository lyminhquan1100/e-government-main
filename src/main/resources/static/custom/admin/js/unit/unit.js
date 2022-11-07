$(document).ready(function () {

    let UNIT_VUE = new Vue({
        el: "#unit",
        data: {
            maxId: 1,
            listUnit: [],
        },
        methods: {
            getUnitInfo() {
                let self = this;
                $.ajax({
                    type: "GET",
                    url: "/api/admin/unit/getUnitInfo",
                    success: function (response) {
                        self.listUnit = response.data[0];
                        self.maxId = response.data[1];
                    }
                })
            },
            prepareDataSave() {
                let listUnit = [];
                $(".item").each(function () {
                    let unitItem = {
                        id: Number($(this).children(".id-item").val()),
                        level: Number($(this).children(".level-item").val()),
                        unitLevelAboveId: Number($(this).children(".level-above-id-item").val()),
                        name: $(this).children(".label-item").val(),
                    };
                    listUnit.push(unitItem);
                })
                return listUnit;
            },
            saveUnit() {
                let self = this;
                $.ajax({
                    type: "POST",
                    url: "/api/admin/unit/saveUnit",
                    data: JSON.stringify(self.prepareDataSave()),
                    contentType: "application/json",
                    beforeSend: function () {
                        window.loader.show();
                    },
                    success: function (response) {
                        window.loader.hide();
                        if (response.status.code === 1000) {
                            window.alert.show("success", "Lưu thành công!", 2000);
                        } else {
                            window.alert.show("error", "Đã có lỗi xảy ra, vui lòng thử lại sau", 2000);
                        }
                    }
                })
            },
        },
        mounted() {
            let self = this;
            self.getUnitInfo();

            $(document).on("click", ".btn-add-unit", function () {
                let $elementListItem;
                let levelUnitAdd;
                let unitIdParent;
                let unitId = ++self.maxId;
                if ($(this).hasClass("btn-add-unit-lv1")) {
                    $elementListItem = $(this).parent("#unit").children(".list-item");
                    levelUnitAdd = 1;
                    unitIdParent = 0;
                } else {
                    $elementListItem = $(this).closest('.wrap-item').children(".list-item");
                    levelUnitAdd = Number($(this).closest(".item").children(".level-item").val()) + 1;
                    unitIdParent = Number($(this).closest(".item").children(".id-item").val());
                }

                $elementListItem.append(
                    `<div class="wrap-item">
                        <div class="item">
                            <i class="fa fa-caret-down icon-item visible"></i>
                            <input class="label-item" value="">
                            <input type="number" hidden class="id-item" value="${unitId}">
                            <input type="number" hidden class="level-item" value="${levelUnitAdd}">
                            <input type="number" hidden class="level-above-id-item" value="${unitIdParent}">
                            <span class="wrap-btn">
                                <button class="btn btn-add-unit btn-primary">Add</button>
                                <button class="btn btn-delete-unit btn-danger">Delete</button>
                            </span>
                        </div>
                        <div class="list-item">
                        </div>
                    </div>`
                );
                $(this).closest(".item").children(".icon-item").removeClass("visible");
            });


            $(document).on("click", ".btn-delete-unit", function () {
                let $listChild = $(this).closest(".list-item");
                let $icon = $(this).closest(".wrap-item").parent().closest('.wrap-item').children(".item").children(".icon-item");

                $(this).closest(".wrap-item").remove();
                if ($listChild.find(".wrap-item").length == 0) {
                    $icon.addClass("visible");
                }
            })

            $(document).on("click", ".icon-item", function () {
                let $listChild = $(this).closest(".wrap-item").children(".list-item");
                if ($listChild.hasClass("hidden")) {
                    $(this).css("transform", "rotate(0deg)");
                } else {
                    $(this).css("transform", "rotate(-90deg)");
                }
                $listChild.toggleClass("hidden");
            })

            $(document).on("click", ".btn-save-unit", function () {
                self.saveUnit();
            })
        }
    })
})