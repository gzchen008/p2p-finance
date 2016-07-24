
/**
 * Created by libaozhong on 2015/5/6.
 */
var url=window.location.href;
if(url.indexOf("bestProduct")>-1){
    clearFocus();
    if( $("#menu_Tree").hasClass("ui-icon-gold-unselected")){
        $("#menu_Tree").removeClass("ui-icon-gold-unselected");
        $("#menu_Tree").addClass("ui-icon-gold-selected");
    }else if( $("#menu_Tree").hasClass("ui-icon-gold-selected")){
        $("#menu_Tree").addClass("ui-icon-gold-unselected");
        $("#menu_Tree").removeClass("ui-icon-gold-selected");
    }
}
if(url.indexOf("moneyMatters")>-1){
    clearFocus();
    if(  $("#menu_Two").hasClass("ui-icon-list-unselected")){
        $("#menu_Two").removeClass("ui-icon-list-unselected");
        $("#menu_Two").addClass("ui-icon-list-selected");
    }else if(  $("#menu_Two").hasClass("ui-icon-list-selected")){
        $("#menu_Two").addClass("ui-icon-list-unselected");
        $("#menu_Two").removeClass("ui-icon-list-selected");
    }
}
if(url.indexOf("property")>-1){
    clearFocus();
    if( $("#menu_Tree").hasClass("ui-icon-gold-unselected")){
        $("#menu_Tree").removeClass("ui-icon-gold-unselected");
        $("#menu_Tree").addClass("ui-icon-gold-selected");
    }else if( $("#menu_Tree").hasClass("ui-icon-gold-selected")){
        $("#menu_Tree").addClass("ui-icon-gold-unselected");
        $("#menu_Tree").removeClass("ui-icon-gold-selected");
    }
}
if(url.indexOf("me")>-1){
    clearFocus();
    if( $("#menu_Four").hasClass("ui-icon-more-unselected")){
        $("#menu_Four").removeClass("ui-icon-more-unselected");
        $("#menu_Four").addClass("ui-icon-more-selected");
    }else if( $("#menu_Four").hasClass("ui-icon-more-selected")){
        $("#menu_Four").addClass("ui-icon-more-unselected");
        $("#menu_Four").removeClass("ui-icon-more-selected");
    }
}

function clearFocus(){
    if( $("#menu_Tree").hasClass("ui-icon-gold-selected")) {
        $("#menu_Tree").addClass("ui-icon-gold-unselected");
        $("#menu_Tree").removeClass("ui-icon-gold-selected");
    }
    if(  $("#menu_Two").hasClass("ui-icon-list-selected")){
        $("#menu_Two").addClass("ui-icon-list-unselected");
        $("#menu_Two").removeClass("ui-icon-list-selected");
    }
    if($("#menu_One").hasClass("ui-icon-home-selected")){
        $("#menu_One").addClass("ui-icon-home-unselected");
        $("#menu_One").removeClass("ui-icon-home-selected");
    }
    if( $("#menu_Four").hasClass("ui-icon-more-selected")){
        $("#menu_Four").addClass("ui-icon-more-unselected");
        $("#menu_Four").removeClass("ui-icon-more-selected");
    }
};



/*
 设置请求路径
 */
function getBaseUrl(){
    //return "http://115.29.196.179:8080/api/service/test";
    return "http://localhost:9000";
//    return "https://www.qmcaifu.com/api/service";
}
function getBaseImgUrl(){
    return "http://115.29.196.179:8080/";
}
function getBabaUrl(){
    //return "http://192.168.1.221:8090/api/facade/sendFubaba";
    return "https://www.qmcaifu.com/api/facade/sendFubaba";
}

//James保存到cookie
function setCookie(name,value,days)
{
    var exp = new Date();
    exp.setTime(exp.getTime() + days*24*60*60*1000);
    document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString();
}
//James读取cookies 
function getCookie(name)
{
    var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");

    if(arr=document.cookie.match(reg))

        return unescape(arr[2]);
    else
        return null;
}

/*
 检查是否支持checkStorageSupport
 */
function checkStorageSupport() {

    // sessionStorage
    if (window.sessionStorage) {
        return true;
    } else {
        return false;
    }

    // localStorage
    if (window.localStorage) {
        return true;
    } else {
        return false;
    }
}
//显示图片弹窗
function showBigImg(imgUrl){
    closeBigImg();
    $("body").after('<div id="detailImg" onClick="$(this).detach();" style="display:none; overflow:hidden;"><div class="pinch-zoom" style="position:absolute; z-index:9999; background-size:contain; overflow:auto;"><img src="'+imgUrl+'" id="detailImgImg" style=""></div><div class="opacity_div" style="opacity:0.9;filter:Alpha(opacity=90); position:absolute; top:0; left:0;" id="detailImgOpacity"></div></div>');
    $("#detailImg").fadeIn();
    //background:url('+imgUrl+') center no-repeat;
}
//关闭图片弹窗
function closeBigImg(){
    $("#detailImg").fadeOut();
    $("#detailImg").detach();
}
//显示loading弹窗
function showLoading(){
    $("body").after('<div id="loading"><div id="icon_loading"><span class="cover_loading_img"><img src="images/icon_loading.gif" width="32px" /></span></div><div class="opacity_div"></div></div>');
}
//关闭loading弹窗
function closeLoading(){
    $("#loading").detach();
}
/*
 错误弹窗关闭
 */
function closeErrorTips(){
    $(".oa_er_bk").css("display","none");
}

//存储会话数据
function setSessionData(key,value){
    sessionStorage.setItem(key,value);
}
//获取会话数据
function getSessionData(key){
    return sessionStorage.getItem(key);
}
//删除会话数据
function removeSessionData(key){
    sessionStorage.removeItem(key);
}
//清除所有数据
function clearSessionData(){
    sessionStorage.clear();
}
//重新登录
function loginRestart(){
    $("body").html('<h1 style="text-align:center; color:#831b32; margin-top:80px; font-size:2.4rem; line-height:40px;">抱歉，您还未登录!</h1><p style="color:#999; font-size:1.4rem;">6秒后将返回登录页面,<a href="./login.html" style="color:#09c; font-weight:bold; text-decoration:underline;">立即登录</a></p>');
    var restart = setTimeout(function(){
        window.location.href="./login.html";
    },6000)
}
//获取设备信息
function getDeviceInfo(){
    return navigator.userAgent.toString();
}
