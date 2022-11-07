$(document).ready(function () {
    $(document).on("click", ".btn-back", function () {
        if (!window.location.pathname.split("/")[2]) {
            $(".write-comment").addClass("hidden");
            $("#page-list").removeClass("hidden");
            $("#page-detail").addClass("hidden");
            documentVue.getListDocument(1);
            documentVue.changeToListPage();
        } else {
            window.location.href = "/document";
        }
    })

    $(document).on("click", "#btn-comment", function () {
        $(".write-comment").toggleClass("hidden");
        if (!$(".write-comment").hasClass("hidden")) {
            $("#input-comment").focus();
        }
    })

})