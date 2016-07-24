
var recommendPhone = $.getQueryString("mobile");
var result=isPhone(recommendPhone);
var rph=result.mobile;

$("img[data-load='load']").click(function () {
    window.location.href = sourceBaseUrl + "/share/downloadRouter.html?mobile="+rph;
})/**
 * Created by libaozhong on 2015/5/14.
 */
$("#go_register").click(function(){
    window.location.href = sourceBaseUrl+"/share/quickRegister.html?mobile="+rph;
});

function show(){
    if($("#go_register").hasClass("bk-img")){
    $("#go_register").removeClass("bk-img");
        $("#go_register").addClass("bk-img-two");
    }else if($("#go_register").hasClass("bk-img-two")){
        $("#go_register").removeClass("bk-img-two");
        $("#go_register").addClass("bk-img");
    }
}

if($("#go_register")[0]){
    setInterval(show,1000);
}


