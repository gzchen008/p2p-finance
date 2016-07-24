/**
 * Created by libaozhong on 2015/1/28.
 */
var shareTitle = "金豆荚理财,积攒属于您的财富";
var descContent = "来金豆荚理财,专业理财师竭诚为您服务！";
var imgUrl = sourceBaseUrl + "/resources/img/activity/logo_icon.png";
(function($) {

    function setQRCode(qrCode) {
        if (qrCode) {
            var image = new Image();
            $("#parentcont").removeClass("margin-left2").addClass("margin-left3");
            image.src = "data:image/png;base64," + qrCode;
            $("#qrlogo").attr("src", image.src);
            $("#image-pop").attr("src", image.src);
        }
    }
    var downLoadURL =window.location.protocol+"//"+window.location.host+'/mobile/quickRegister';
    var mobile = $.getQueryString("mobile");
    if (mobile) {
        if (mobile != "99999999999") {

        //添加mobile到queryString为了统计下载人数
        downLoadURL += "?mobile=" + mobile;
        }
    }
    var token = mobile ? "" : getCookie("token");
    var body = {
        "content": downLoadURL,
        "token": token
    };
    if(mobile){
    $("#mobile").val(mobile);
    };
    $.ajax({
        type: "POST",
        contentType: 'application/json',
        dataType: "json",
        url: apiBaseUrl + "/customer/qrcode",
        ///customer/activity/qrcodewithtoken
        data: JSON.stringify(body),
        processData: false,
        success: function(data) {
            setQRCode(data.value);
        },
        error: function(data) {
            console.error("access error:" + data);
        }
    });

    $("#qrlogo").on("click", function() {
        $("#pop").removeClass("display-none").addClass("abso-position");
    });
    $("#image-pop").on("click", function() {
        $("#pop").removeClass("abso-position").addClass("display-none");
    });
})(jQuery);
