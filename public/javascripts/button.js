//下面的配置在部署的时候需要改变


apiBaseUrl = baseUrl + '/api';
var shareUrl=null;
sourceBaseUrl=baseUrl;
var loadurl=sourceBaseUrl+"/share/downloadRouter.html";
officialsite=baseUrl+'/appdown';

weiXinRedirectUrl = "https://mp.weixin.qq.com/mp/redirect?url=";

var ua = navigator.userAgent.toLowerCase();
var redirectLink=["mobile/registerSuccess"];
var service = function(){
    isWeiXin= function() {

        if(/MicroMessenger/ig.test(ua)) {
            return true;
        } else {
            return false;
        }
    };
    notinapp= function (){

        if(ua.indexOf("jindoujialicai") == -1) {
            return true;
        } else {
            return false;
        }
    };
    isPhone=function(mobile){
        var result={
            is:false,
            mobile:null
        };
        if(recommendPhone){
            if(recommendPhone.indexOf("99999999999")==-1){
                if("null"!=recommendPhone){
                    var tep= recommendPhone.substring(0,11);
                    if(validatePhoneNum(tep)){
                        result.is=true;
                        result.mobile=tep;
                        return result;
                    }
                }

            }
        }
        return result;
    };
    getCookie= function (cookie_name) {
        var allcookies = document.cookie;
        var cookie_pos = allcookies.indexOf(cookie_name);
        // 如果找到了索引，就代表cookie存在，
        // 反之，就说明不存在。
        if (cookie_pos != -1) {
            // 把cookie_pos放在值的开始，只要给值加1即可。
            cookie_pos += cookie_name.length + 1;
            var cookie_end = allcookies.indexOf(";", cookie_pos);
            if (cookie_end == -1) {
                cookie_end = allcookies.length;
            }
            var value = unescape(allcookies.substring(cookie_pos, cookie_end));
        }
        return value;
    };
    redirect=  function (str){
        //
        var fpToken=getCookie("token");
        var p2pToken=getCookie("token");
        $.each(str,function(n,value) {
            if(!existToken && window.location.href.indexOf(value) >-1){
                window.location.href=shareUrl;
                return;
            }
        });

    };
    return {
        isWeiXin:isWeiXin,
        notinapp:notinapp,
        isPhone:isPhone,
        getCookie:getCookie,
        redirect:redirect
      };
    };
function isWeiXin() {

    if(/MicroMessenger/ig.test(ua)) {
        return true;
    } else {
        return false;
    }
};
function notinapp() {

    if(ua.indexOf("jindoujialicai") == -1) {
        return true;
    } else {
        return false;
    }

};
function isPhone(recommendPhone){
    var result={
        is:false,
        mobile:null
    };
    if(recommendPhone){
        if(recommendPhone.indexOf("99999999999")==-1){
            if("null"!=recommendPhone){
                var tep= recommendPhone.substring(0,11);
                if(validatePhoneNum(tep)){
                    result.is=true;
                    result.mobile=tep;
                    return result;
                }
            }

        }
    }
    return result;
};

function getCookie(cookie_name) {
    var allcookies = document.cookie;
    var cookie_pos = allcookies.indexOf(cookie_name);
    // 如果找到了索引，就代表cookie存在，
    // 反之，就说明不存在。
    if (cookie_pos != -1) {
        // 把cookie_pos放在值的开始，只要给值加1即可。
        cookie_pos += cookie_name.length + 1;
        var cookie_end = allcookies.indexOf(";", cookie_pos);
        if (cookie_end == -1) {
            cookie_end = allcookies.length;
        }
        var value = unescape(allcookies.substring(cookie_pos, cookie_end));
    }
    return value;
}

function addCookie(name, value, expires, path, domain) {
    var str = name + "=" + escape(value);
    if (expires != "") {
        var date = new Date();
        date.setTime(date.getTime() + expires * 24 * 3600 * 1000); //expires单位为天
        str += ";expires=" + date.toGMTString();
    }
    if (path != "") {
        str += ";path=" + path; //指定可访问cookie的目录
    }
    if (domain != "") {
        str += ";domain=" + domain; //指定可访问cookie的域
    }
    document.cookie = str;
}


function redirect(str){

    var existToken =getCookie("SPSP_SESSION");
var a=false;
    $.each(str,function(n,value) {
        if((!existToken) && window.location.href.indexOf(value) >-1){
          a=true;
        }

    });
    return a;

};

function validatePhoneNum(phoneNum, errorcontrol) {
    var errorStr="";
    if (phoneNum.length == 0) {
        if(!!errorcontrol){
            errorcontrol.html("<span >请输入手机号码！</span>");
        }
        return false;
    }
    if (phoneNum.length != 11) {
        if(!!errorcontrol){
            errorcontrol.html("<span>请输入有效的手机号码！</span>");
        }
        return false;
    }
    /// <summary>
    /// 匹配移动手机号
    /// </summary>
    var PATTERN_CMCMOBILENUM = /^1(3[4-9]|5[012789]|8[78])\d{8}$/;
    /// <summary>
    /// 匹配电信手机号
    /// </summary>
    var PATTERN_CTCMOBILENUM = /^18[0-9]\d{8}$/;
    /// <summary>
    /// 匹配联通手机号
    /// </summary>
    var PATTERN_CUTMOBILENUM = /^1(3[0-2]|5[56]|8[56])\d{8}$/;
    /// <summary>
    /// 匹配CDMA手机号
    /// </summary>
    var PATTERN_CDMAMOBILENUM = /^1[357][0-9]\d{8}$/;
    if (PATTERN_CMCMOBILENUM.test(phoneNum) || PATTERN_CTCMOBILENUM.test(phoneNum) || PATTERN_CUTMOBILENUM.test(phoneNum) || PATTERN_CDMAMOBILENUM.test(phoneNum)) {
        return true;
    } else {
        if(!!errorcontrol){
            errorcontrol.html("<span>请输入有效的手机号吗！</span>");
        }
        return false;
    }
}
/***
 *
 * 需要跳转的链接
 * @type {string[]}
 */




(function($) {
    $('share-sigin').attr('src', baseUrl + "/activity/images/logo_icon.png");
    $('logon-fp').attr('src', baseUrl + "/activity/images/logo_icon.png");
    $('logon-fp').attr('src', baseUrl + "/activity/images/logo_icon.png");
    $('#share').attr('share-imageurl', baseUrl + "/activity/images/logo_icon.png");
    var ua = navigator.userAgent.toLowerCase();
    var actions = ["login", "signature"];

    function jumpLocation(location) {

        window.location = location;
    }

    function buttonActions(value) {

        var btn = $('#purchase_btn');
        if (actions.indexOf(value) > -1) {
            jumpLocation("yy://" + value);
        } else if ("firstpurchase" == value) {
            var buttonData = btn.data('button');
            if (buttonData === "firstpurchase") {
                if (ua.indexOf('iphone') > -1) {
                    // TODO: 下次发布ios版的时候把这行代码去掉
                    jumpLocation("yy://prodcutlist#{'type':'FP.PRODUCT.TYPE.1','category':'STF'}");
                } else {
                    jumpLocation("yy://productlist#{'type':'FP.PRODUCT.TYPE.1','category':'STF'}");
                }
            }
        } else if("purchase" == value){
            if (ua.indexOf('iphone') > -1) {
                // TODO: 下次发布ios版的时候把这行代码去掉
                jumpLocation("yy://prodcutlist#{'type':'FP.PRODUCT.TYPE.1','category':'STF'}");
            } else {
                jumpLocation("yy://productlist#{'type':'FP.PRODUCT.TYPE.1','category':'STF'}");
            }
        }
        else if ("fundDetail" == value) {
            var PRODUCT_TYPE = {
                FUND : 1,
                P2P : 4
            };
            if (ua.indexOf('iphone') > -1) {
                var shareType = btn.data("share-type");
                if (shareType == PRODUCT_TYPE.FUND) {
                    var prodType = btn.data("prod-type");
                    var category = btn.data("category");
                    var id = btn.data("id");
                    var name = btn.data("prod-mame");
                    var url = "yy://productdetail#{'type':'" + prodType + "','category':'" + category + "','name':'" + name + "','code':'" + id + "'}";
                    jumpLocation(url);
                }
                else if (shareType == PRODUCT_TYPE.P2P) {
                    var id = btn.data("id");
                    var name = btn.data("prod-mame");
                    var url = "yy://p2pprdouctdetail#{'name':'" + name + "','id':'" + id + "'}";
                    jumpLocation(url);
                }
            }
        }
        else {
            jumpLocation(sourceBaseUrl+"/share/downloadRouter.html");
        }
    }

    function bindEvent(btn) {
        var buttonData = btn.data('button');
        if (ua.indexOf('android') > -1 || ua.indexOf('linux') > -1 || ua.indexOf('iphone') > -1||ua.indexOf('ipad') > -1) {
            $('#purchase_btn button').on("click", function() {
                buttonActions(buttonData);
            });
        } else {
            console.info("not support this system");
        }
    }

    $(document).ready(function() {
        var btn = $('#purchase_btn');
        if (btn) {
            if (ua.indexOf("jindoujialicai") == -1) {
                var button = $('#purchase_btn button');
                var hasName = btn.data("has-name") ? true : false;

                if (!hasName) {
                    button.text("下载");
                }
                btn.attr("data-button", "download");
            } else {

                var buttonData = btn.data('button');
                if (actions.indexOf(buttonData) > -1) {
                    var existToken = getCookie("token");
                    if (existToken) {
                        var signBtn = $('div button');
                        signBtn.text("查看金豆");
                        btn.removeClass("display-hidden");
                        signBtn.on("click", function() {
                            buttonActions('signature');
                            signBtn.stopPropagation();
                        })
                    }
                }

            }
            btn.removeClass("display-hidden");
            bindEvent(btn);
        }
    });
})(jQuery);
