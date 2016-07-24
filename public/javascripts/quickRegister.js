/**
 * Created by libaozhong on 2015/1/22.
 */
var success = "/share/registerSuccess.html";
(function ($, window) {
    //获取url手机号

    var recommendPhone = $.getQueryString("mobile");
    var rp="";
var recomMobile=recomMobile|{};
var result=isPhone(recommendPhone);
    if(!result.is && recomMobile){
        result.mobile=recomMobile;
        result.is=true;
    }
    rp=result.mobile;
    if(result.is){
        shareUrl= sourceBaseUrl+"/share/share.html?mobile="+rp;
        $("#recommendPhone").val(rp);
        $("#recommendPhone").attr("readonly", "true");
    }
    //获取验证码{

    $("#getveriycode").click(
        function () {
            var mobilePhoneNo = $("#phoneNum").val();
            if (mobilePhoneNo.length != 11) {
                $("#phoneNumerrorinfo").html("<span>手机号码有误！</span>");
                return;
            }
            var verifyCodeElement = $("#getveriycode");
            verifyCodeElement.removeClass("verify-color").addClass("gray-color");
            verifyCodeElement.unbind("click");
            $.getVerifyCode(mobilePhoneNo,"0");
            $.time(verifyCodeElement, 60, mobilePhoneNo,"0");
        }
    );

    $('#agreeTerms').click(function () {
        if ($('#agreeTerms').is(':checked')) {
            $('#register').removeAttr("disabled");

            $('#register').removeClass("gray-color");
        } else {
            $('#register').attr("disabled", "disabled");

            $('#register').addClass("gray-color");

        }
    });
    $(".eye").click(
        function(){
            if( $("#loginpwd").attr("type")=="password"){
                $(".eye").css("backgroundPosition","0 0px");
                $(".eye").css("background-size","100%");
                $("#loginpwd").attr("type","text");
            }else
            if( $("#loginpwd").attr("type")=="text"){
                $(".eye").css("backgroundPosition","0 -59px");
                $(".eye").css("background-size","100%");
                $("#loginpwd").attr("type","password");
            }

        }
    )
    //注册
    $("#register").click(
        function () {
            var mobilePhoneNo = $("#phoneNum").val();

            var pwd = $("#loginpwd").val();
            if (pwd.length < 6) {
                $("#pwderrorinfo").html("<span >密码过于简单！</span>");
                return;
            }
            if (!/^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$/.test(pwd)) {
                $("#pwderrorinfo").html("<span >6-20位字母和数字组合！</span>");
                return;
            }
            var verifyCode = $("#inputverifycode").val();
            if (verifyCode.length < 4) {
                $("#ipvcerrorinfo").html("<span>验证码填写错误！</span>");
                return;
            }
            if (!recommendPhone) {
                recommendPhone = $("#recommendPhone").val();
            }
            if (!validatePhoneNum(mobilePhoneNo, $("#phoneNumerrorinfo"))) {
                return;
            };
            success = "/share/registerSuccess.html?mobile="+mobilePhoneNo;

                var openId=$("#openId").val();
                 var name=$("#name").val();
            registry(mobilePhoneNo, verifyCode, pwd, recommendPhone,openId,name);
        }
    );
    //注册方法
    function registry(custmobile, verifyCode, passWord, recommendPhone,openId,name) {
        console.debug(custmobile + "," + passWord + "," + verifyCode);
        var formParams = "mobilePhoneNo=" + custmobile +
            "&passWord=" + passWord +
            "&verifyCode=" + verifyCode +
            "&recommendPhone=" + recommendPhone
            +"openId"+openId
            ;
        $("label").each(function () {
            var lab = $(this);
            if (lab.data('error') == 'error') {
                lab.html('');
            }
        });
        try {
            $.ajax({
                type: "POST",
                url:  "/mobile/register",
                data: {
                    name: custmobile,
                    password: passWord,
                    verifyCode:verifyCode,
                    recommended: recommendPhone,
                    openId:openId,
                    queryName:name
                },
                success: function (data) {
                    if (0 == data.error.code) {
                        window.location.href = "/mobile/registerSuccess?errorcode=0@mobile="+custmobile;

                    }if(-2 == data.error.code){
                        window.location.href = "/mobile/registerSuccess?errorcode=-2@mobile="+custmobile;
                    }else
                    {
                        $("#phoneNumerrorinfo").html("<span>" + data.error.msg + "</span>");
                    }
                },
                error: function (XMLHttpRequest) {
                    $('#phoneNumerrorinfo').html("<span>网络繁忙！</span>");
                }
            });
        } catch (err) {
            $('#phoneNumerrorinfo').html("<span>网络繁忙！</span>");
        }
    };
    //    $.ajax({
    //        type: "POST",
    //        dataType: "json",
    //        url:  '/mobile/register',
    //        async: true,
    //        data: formParams,
    //
    //        success: function (data) {
    //            //if (data.message.code == "0100") {
    //            //    $.ajax({
    //            //            type: "POST",
    //            //            dataType: "json",
    //            //            url: apiBaseUrl + '/account/activity/register',
    //            //            success: function (data) {
    //
    //                            return location.href = success;
    //                        },
    //                        error: function (data) {
    //                            $('#phoneNumerrorinfo').html("<span>网络繁忙！</span>");
    //                        }
    //                    }
    //                );
    //                window.location.href = $.geturl(success);
    //            } else {
    //                $("#phoneNumerrorinfo").html("<span>" + data.message.summary + "</span>");
    //            }
    //        },
    //        error: function (data) {
    //            $("#phoneNumerrorinfo").html("<span>网络异常</span>");
    //        }
    //    });
    //}

    //统一验证手机号



}(jQuery, window));
