$(document).ready(function () {
    $(".list-news").slimScroll({
        height: '300px',
        wheelStep : 10,
        touchScrollStep : 500
    });

    $(document).on("click", ".document", function () {
        let seo = $(this).attr("value");
        let documentId = Number($(this).attr("id").replace("post-", ""));
        localStorage.setItem("documentId", documentId);
        window.location.href = "/document/" + seo;
    })

    // marquee
    $(document).on("click", ".marquee-news", function () {
        let seo = $(this).attr("value");
        window.location.href = "/news/" + seo;
    })

    // news col1
    $(document).on("click", ".redirect-to-detail-news", function () {
        let seo = $(this).attr("value");
        window.location.href = "/news/" + seo;
    })
    $(document).on("click", ".title-news-col1", function () {
        let seo = $(this).attr("value");
        window.location.href = "/news/" + seo;
    })

    // news col2
    $(document).on("click", ".image-news-col2", function () {
        let seo = $(this).attr("value");
        window.location.href = "/news/" + seo;
    })
    $(document).on("click", ".title-news-col2", function () {
        let seo = $(this).attr("value");
        window.location.href = "/news/" + seo;
    })


})