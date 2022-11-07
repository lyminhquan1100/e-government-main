$(document).ready(function () {

    let WORK_UNIT_VUE = new Vue({
        el: "#work-unit",
        data: {
            maxId: 1,
            listWrapUnitLv1: [],

            unitParentId: "",
            nameWorkUnit: "",
            unitIdSelected: "",
            listUnitSelect: [],

            isShowErrorSelectUnit: false,
            isShowErrorNameWorkUnit: false,

            $elementClickAdd: "",
        },
        methods: {
            getWorkUnitInfo() {
                let self = this;
                $.ajax({
                    type: "GET",
                    url: "/api/admin/work_unit/getWorkUnitInfo?type=all",
                    success: function (response) {
                        self.listWrapUnitLv1 = response.data[0];
                        self.maxId = response.data[1];
                    }
                })
            },
            prepareDataSave() {
                let listWorkUnit = [];
                $(".item").each(function () {
                    let workUnitItem = {
                        id: Number($(this).children(".id-item").val()),
                        level: Number($(this).children(".level-item").val()),
                        workUnitLevelAboveId: Number($(this).children(".level-above-id-item").val()),
                        unitId: Number($(this).children(".unit-id-item").val()),
                        name: $(this).children(".name-item").val(),
                    };
                    listWorkUnit.push(workUnitItem);
                })
                return listWorkUnit;
            },
            saveWorkUnit() {
                let self = this;
                $.ajax({
                    type: "POST",
                    url: "/api/admin/work_unit/saveWorkUnit",
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
            getListUnitSelect() {
                let self = this;
                $.ajax({
                    type: "GET",
                    url: "/api/admin/unit/getListUnitChild?unitParentId=" + self.unitParentId,
                    success: function (response) {
                        self.listUnitSelect = response.data;
                        if (self.listUnitSelect.length > 0) {
                            self.unitIdSelected = self.listUnitSelect[0].id;
                            $("#modal_add_work_unit").modal("show");
                        } else {
                            self.resetPopup();
                            window.alert.show("error", "Không có danh sách cấp đơn vị!", 2000);
                        }
                    }
                })
            },
            validateNameWorkUnit() {
                this.isShowErrorNameWorkUnit = !this.nameWorkUnit.trim();
            },
            validatePopup() {
                this.validateNameWorkUnit();
                return !this.isShowErrorNameWorkUnit;
            },
            OkPopup() {
                let self = this;
                if (!self.validatePopup()) {
                    return;
                }
                $("#modal_add_work_unit").modal("hide");

                let nameUnitSelect = $("#select-unit option:selected").text();
                let levelWorkUnitAdd;
                let workUnitIdParent;
                let workUnitId = ++self.maxId;
                let $wrapListItem;
                if (this.$elementClickAdd.hasClass("add-work-unit-lv1")) {
                    levelWorkUnitAdd = 1;
                    workUnitIdParent = 0;
                    $wrapListItem = this.$elementClickAdd.closest('.wrap-unit-lv1');
                } else {
                    levelWorkUnitAdd = Number(this.$elementClickAdd.closest(".item").children(".level-item").val()) + 1;
                    workUnitIdParent = Number(this.$elementClickAdd.closest(".item").children(".id-item").val());
                    $wrapListItem = this.$elementClickAdd.closest('.wrap-item').children(".list-item");
                }

                $wrapListItem.append(
                    `<div class="wrap-item">
                        <div class="item">
                            <i class="fa fa-caret-down icon-item visible"></i>
                            <div class="label-item">${self.nameWorkUnit} (${nameUnitSelect})</div>
                            <input type="text" hidden class="name-item" value="${self.nameWorkUnit}">
                            <input type="number" hidden class="id-item" value="${workUnitId}">
                            <input type="number" hidden class="level-item" value="${levelWorkUnitAdd}">
                            <input type="number" hidden class="level-above-id-item" value="${workUnitIdParent}">
                            <input type="number" hidden class="unit-id-item" value="${self.unitIdSelected}">
                            <span class="wrap-btn">
                                <button class="btn btn-add-work-unit btn-primary">Add</button>
                                <button class="btn btn-delete-work-unit btn-danger">Delete</button>
                            </span>
                        </div>
                        <div class="list-item">
                        </div>
                    </div>`
                );
                this.$elementClickAdd.closest(".item").children(".icon-item").removeClass("visible");
            },
            resetPopup() {
                this.listUnitSelect = [];
                this.unitParentId = "";
                this.nameWorkUnit = "";
                this.isShowErrorSelectUnit = false;
                this.isShowErrorNameWorkUnit = false;
                this.$elementClickAdd = "";
            },
        },
        mounted() {
            let self = this;
            self.getWorkUnitInfo();

            $(document).on("click", ".btn-add-work-unit", function () {
                self.$elementClickAdd = $(this);

                if ($(this).hasClass("add-work-unit-lv1")) {
                    let unitIdLv1 = Number($(this).parent(".wrap-title-unit").children(".id-unit-lv1").val());
                    let unitNameLv1 = $(this).parent(".wrap-title-unit").children(".name-unit-lv1").text();
                    self.listUnitSelect.push({
                        id: unitIdLv1,
                        name: unitNameLv1,
                    });
                    self.unitIdSelected = unitIdLv1;
                    $("#modal_add_work_unit").modal("show");
                } else {
                    self.unitParentId = Number($(this).closest(".item").children(".unit-id-item").val());
                    self.getListUnitSelect();
                }
            });

            $(document).on("click", ".btn-delete-work-unit", function () {
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

            $("#modal_add_work_unit").on("hidden.bs.modal", function () {
                self.resetPopup();
            })

            $(document).on("click", ".btn-save-work-unit", function () {
                self.saveWorkUnit();
            })
        }
    })
})