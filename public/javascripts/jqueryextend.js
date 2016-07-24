/**
 * Created by libaozhong on 2015/2/4.
 */

(function($, window) {
    var ua = navigator.userAgent.toLocaleLowerCase();

    $.extend({
        jumpLocation:function (location) {
        window.location = location;
    },
        bindRedirect: function(obj) {

            var actions = ["login", "signature"];
                var btn = obj;
                var value=btn.data('button');
                if (actions.indexOf(value) > -1) {
                    $.jumpLocation("yy://" + value);
                } else if ("firstpurchase" == value) {
                    if (value == "firstpurchase") {
                        if (ua.indexOf('iphone') > -1) {
                            // TODO: 下次发布ios版的时候把这行代码去掉
                            $.jumpLocation("yy://prodcutlist#{'type':'FP.PRODUCT.TYPE.1','category':'STF'}");
                        } else {
                            $.jumpLocation("yy://productlist#{'type':'FP.PRODUCT.TYPE.1','category':'STF'}");
                        }
                    }
                } else if("purchase" == value){
                    if (ua.indexOf('iphone') > -1) {
                        // TODO: 下次发布ios版的时候把这行代码去掉
                        $.jumpLocation("yy://prodcutlist#{'type':'FP.PRODUCT.TYPE.1','category':'STF'}");
                    } else {
                        jumpLocation("yy://productlist#{'type':'FP.PRODUCT.TYPE.1','category':'STF'}");
                    }
                }
                else if ("fundDetail" == value) {
                    if (ua.indexOf('iphone') > -1) {
                        var shareType = btn.data("share-type");
                        if (shareType == 1) {
                            var prodType = btn.data("prod-type");
                            var category = btn.data("category");
                            var id = btn.data("id");
                            var name = btn.data("prod-mame");
                            var url = "yy://productdetail#{'type':'" + prodType + "','category':'" + category + "','name':'" + name + "','code':'" + id + "'}";
                            $.jumpLocation(url);
                        }
                        else if (shareType == 1) {
                            var id = btn.data("id");
                            var name = btn.data("prod-mame");
                            var url = "yy://p2pprdouctdetail#{'name':'" + name + "','id':'" + id + "'}";
                            $.jumpLocation(url);
                        }
                    }
                }
                else {
                    $.jumpLocation(loadurl);
                }

        },
        geturl: function(localurl) {
            return localurl;
        },
        geturlPhoneNum: function(localurl) {
            var queryString = localurl.substr(localurl.indexOf("?") + 1).split("&");
            for (var a in queryString) {
                if (queryString[a].indexOf("mobile") > -1) {
                    return queryString[a].split("=")[1];
                }
            }
        },
        getVerifyCode: function(phoneNum,type) {
            $.ajax({
                type: "GET",
                url:  "/mobile/verifyCode",
                data: {"mobile": phoneNum,"type":type},
                success: function (data) {
                    if (0 != data.error.code) {
                        $('#ipvcerrorinfo').html("<span style='color: red'>data.error.msg</span>");
                    }
                },
                error : function(XMLHttpRequest) {
                    $('#ipvcerrorinfo').html("<span style='color: red'>网络繁忙！</span>");
                }
            });

            //$.ajax({
            //    type: "POST",
            //    dataType: "json",
            //    url: apiBaseUrl + '/core/verificationcode',
            //    data: {
            //        "mobilePhoneNo": phoneNum,
            //        "type": "REGISTER"
            //    },
            //    success: function(data) {
            //        if (data.message.severity != 0) {
            //            $('#ipvcerrorinfo').html("<span style='color: red'>验证码获取失败，请重试！</span>");
            //        }
            //    },
            //    error: function(data) {
            //        $('#ipvcerrorinfo').html("<span style='color: red'>网络繁忙！</span>");
            //    }
            //});
        },
        registry: function(custmobile, verifyCode, passWord, recommendPhone) {
            console.debug(custmobile + "," + passWord + "," + verifyCode);
            var formParams = "mobilePhoneNo="+custmobile+
                "&passWord="+passWord+
                "&verifyCode="+verifyCode+
                "&recommendPhone="+recommendPhone;

            $.ajax({
                type: "POST",
                dataType: "json",
                url: apiBaseUrl + '/core/register',
                async: true,
                data:formParams,

                success: function(data) {
                    if (data.message.code == "0100") {
                        $.ajax({
                                type: "POST",
                                dataType: "json",
                                url: apiBaseUrl + '/account/activity/register',
                                success: function (data) {
                                  return  location.href = loadurl;
                                },
                                error: function (data) {
                                    $('#errorinfo').html("<span style='color: red' class='error-pos'>网络繁忙！</span>");
                                }
                            }
                        );
                        window.location.href = $.geturl(loadurl);
                    } else {
                        $("#errorinfo").html("<span style='color: red' class='error-pos'>" + data.message.summary + "</span>");
                    }
                },
                error: function(data) {
                $("#errorinfo").html("<span style='color: red' class='error-pos'>网络异常</span>");
                }
            });
        },
        getQueryString: function(name) {
            var reg = new RegExp("(^|@)" + name + "=([^@]*)(@|$)", "i");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]);
            return null;
        },
        time: function time(o, wait, mobilePhoneNo,type) { //o为按钮的对象，p为可选，这里是60秒过后，提示文字的改变,wait 为时间
            console.info(wait);

            var w = wait;
            var next=wait;
            if (typeof w === "undefined") {
                w = 120; //默认120秒后再次获取验证码
                next=120;
            }
            var timeId;

            if (w <= 0) {
                o.html("获取验证码"); //改变按钮中value的值
                clearTimeout(timeId);
                $("#getveriycode").removeClass("gray-color").addClass("verify-color");
                o.click(function() {
                    var mobilePhoneNo = $("#phoneNum").val();
                    if (mobilePhoneNo.length != 11) {
                        $("#phoneNumerrorinfo").html("<span>手机号码有误！</span>");
                        return;
                    }
                    $("#getveriycode").removeClass("gray-color").addClass("verify-color");
                    $("#getveriycode").unbind("click");
                    $.time($("#getveriycode"), 60, mobilePhoneNo,0);
                    $.getVerifyCode(mobilePhoneNo,0);
                });
            } else {
                o.html("等待"+w + "秒"); //改变按钮中value的值
                o.removeClass("verify-color").addClass("gray-color");
                w--;
                timeId = setTimeout(function() {
                    time(o, w, mobilePhoneNo); //循环调用
                }, 1000)
            }
        },
        validatePhoneNum: function(phoneNum, errorcontrol) {
            if (phoneNum.length == 0) {
                alert('请输入手机号码！');
                // errorcontrol.val('请输入手机号码！');
                return false;
            }
            if (phoneNum.length != 11) {
                alert('请输入有效的手机号码！');
                // errorcontrol.val('请输入有效的手机号码！');
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
                alert('请输入有效的手机号码！');
                //  errorcontrol.val('请输入有效的手机号码！');
                return false;
            }
        }

    });
}(jQuery, window))
Date.prototype.Format = function(fmt)
{ //author: meizz
    var o = {
        "M+" : this.getMonth()+1,                 //月份
        "d+" : this.getDate(),                    //日
        "h+" : this.getHours(),                   //小时
        "m+" : this.getMinutes(),                 //分
        "s+" : this.getSeconds(),                 //秒
        "q+" : Math.floor((this.getMonth()+3)/3), //季度
        "S"  : this.getMilliseconds()             //毫秒
    };
    if(/(y+)/.test(fmt))
        fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
    for(var k in o)
        if(new RegExp("("+ k +")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
    return fmt;
}
var terminaType= (function(window){

    var u = navigator.userAgent, app = navigator.appVersion;
return {
    trident: u.indexOf('Trident') > -1, //IE内核
    presto: u.indexOf('Presto') > -1, //opera内核
    webKit: u.indexOf('AppleWebKit') > -1, //苹果、谷歌内核
    gecko: u.indexOf('Gecko') > -1 && u.indexOf('KHTML') == -1,//火狐内核
    mobile: !!u.match(/AppleWebKit.*Mobile.*/), //是否为移动终端
    ios: !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), //ios终端
    android:u.indexOf('Android') > -1 || u.indexOf('Linux') > -1, //android终端或者uc浏览器
    iPhone: u.indexOf('iPhone') > -1 , //是否为iPhone或者QQHD浏览器
    iPad: u.indexOf('iPad') > -1, //是否iPad
    webApp: u.indexOf('Safari') == -1, //是否web应该程序，没有头部与底部
    weixin: u.indexOf('MicroMessenger') >-1, //是否微信 （2015-01-22新增）
    qq: u.match(/\sQQ/i) == " qq",//是否QQ
    IPadQQ: u.indexOf('IPadQQ') >-1,
    app:u.indexOf("jindoujialicai")>-1
}
})(window);
