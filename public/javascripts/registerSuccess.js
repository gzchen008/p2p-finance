
(function ($, window) {
var recommendPhone = $.getQueryString("mobile");
    var errorCode = $.getQueryString("errorcode");
var result=isPhone(recommendPhone);
var rph=result.mobile;

$("img[data-load='load']").click(function () {
    window.location.href = sourceBaseUrl + "/share/downloadRouter.html?mobile="+rph;
})/**
 * Created by libaozhong on 2015/5/14.
 */

  function showAndHide(obj,obj1,obj2){
        obj.show();
        obj1.hide();
        obj2.hide();
    };
    var b=redirect(redirectLink);
function showRegister(){
    var obj=$("#go_register");
    if(obj.hasClass("bk-img")){
        obj.removeClass("bk-img");
        obj.addClass("bk-img-two");
    }else if(obj.hasClass("bk-img-two")){
        obj.removeClass("bk-img-two");
        obj.addClass("bk-img");
    }
}
    function showLogin(){
        var obj=$("#go_login");
        if(obj.hasClass("bk-img")){
            obj.removeClass("bk-img");
            obj.addClass("bk-img-two");
        }else if(obj.hasClass("bk-img-two")){
            obj.removeClass("bk-img-two");
            obj.addClass("bk-img");
        }
    }
    function showGolden(){
        var obj=$("#go_golden");
        if(obj.hasClass("bk-img")){
            obj.removeClass("bk-img");
            obj.addClass("bk-img-two");
        }else if(obj.hasClass("bk-img-two")){
            obj.removeClass("bk-img-two");
            obj.addClass("bk-img");
        }
    }
//if(b){
//    $("#welcome").html("金豆荚欢您光临!点击注册有惊喜哦!");
//    showAndHide($("#rg_bk"),$("#rg_ll"),$("rg_ss"));
//    $("#go_register").click(function () {
//        window.location.href = "/mobile/quickRegister?mobile=" + rph;
//    });
//    setInterval(showRegister,1000);
//}else{
    if(errorCode=="-2"){
        $("#welcome").html("此手机号已经注册请点击下方登录");
        showAndHide($("#rg_ll"),$("#rg_bk"),$("#rg_ss"));
        $("#go_login").click(function(){
            window.location.href = "/mobile/login?mobile="+rph;
        });
        setInterval(showLogin,1000);
    }else if(errorCode=="0"){
        $("#welcome").html("金豆荚欢您光临");
        showAndHide($("#rg_ss"),$("#rg_ll"),$("#rg_bk"));
        $("#go_golden").click(function(){
            window.location.href="/mobile/content/moneyMatters";
        })
        setInterval(showGolden,1000);
    };
//};

}(jQuery, window));
