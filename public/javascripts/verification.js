// JavaScript Document
function veriAllInput(allInputsResults){
    for(var i=0; i<arguments.length;i++){
        if(arguments[i]!=true){
//            showErrorTips(arguments[i]);
            return false;
        }else{
            continue;
        }
    }
    return true;
}
function veriAllInput_inText(allInputsResults){
    for(var i=0; i<arguments.length;i++){
        if(arguments[i]!=true){
            $("#tipsBox").text(arguments[i]);
            //showErrorTips(arguments[i]);
            return false;
        }else{
            continue;
        }
    }
    return true;
}
function veriChangeVal(controlButtonId,allInputsIdOrClass){
    var obj_checkBox = new Array();
    var str = "";
    for(var i=1; i<arguments.length; i++){
        if(1==i){
            str = arguments[i];
        }else{
            str = str +","+arguments[i];
        }
        if($(arguments[i]).is(":checkbox")){
            obj_checkBox.push($(arguments[i]));
        }
    }
    var flag = true;
    for(var i=1; i<arguments.length; i++){
        //alert($(arguments[i]).is(":checkbox"));
        if($(arguments[i]).val().length==0){
            flag = false;
            break;
        }
        if($(arguments[i]).is(":checkbox")){
            if($(arguments[i]).is(":checked")==false){
                flag = false;
                break;
            }
        }
    }
    if(flag){
        $("#"+controlButtonId).removeAttr("disabled");
    }
    $(document).on({
        input:function(){
            if($(this).val().toString().length==0){
                $("#"+controlButtonId).attr("disabled","disabled");
            }else{
                var obj = $(document).find("input[type!='checkbox']").not(":hidden").not("[readonly]");
                for(var i=0; i<obj.length; i++){
                    if(obj.filter("input:eq("+i+")").val().toString().length==0){
                        return false;
                    }
                }
                for(var i=0; i<obj_checkBox.length; i++){
                    if(!obj_checkBox[i].is(":checked")){
                        return false;
                    }
                }
                $("#"+controlButtonId).removeAttr("disabled");
            }
        },
        change:function(){
            if($(this).is(":checkbox")){
                if($(this).is(":checked")){
                    $("#"+controlButtonId).removeAttr("disabled");
                }else{
                    $("#"+controlButtonId).attr("disabled","disabled");
                }
            }
        }
    },str);
}
/*
 显示错误信息
 */
function showErr(err){
    alert(err.message);
}
/*
 手机号验证
 Param:phoneStr 输入的手机字符串
 */
function veriPhoneNumber(phoneStr){
    try{
        var str = phoneStr.replace(/\s+/g,"");
        if(str.length==0){
            return "手机号不能为空，请重新输入！";
        }
        if(str.length!=11||isNaN(str)){
            return "您输入的手机号格式不正确，请重新输入！";
        }
        return true;
    }catch(err){
        showErr(err);
    }
}
/*登陆密码验证*/
function veriPassword(passwordStr){
    if(passwordStr.length==0){
        return "密码不能为空，请重新输入！";
    }
    if(passwordStr.length<6){
        return "密码少于6位，请重新输入";
    }
    return true;
}

/*
 短信校验码验证
 Param:valCodeStr 输入的短信校验码
 */
function veriValCode(valCodeStr){
    var str = valCodeStr.toString();
    if(isNaN(str)){
        return "您输入的验证码不正确，请重新输入！";
    }
    if(str.length==0){
        return "验证码不能为空！";
    }
    return true;
}
/*
 短信验证消息倒计时
 Param:seconds  倒计时时间，秒
 Param:elementId	需要用到倒计时的元素
 */
function getValCodeTimeout(seconds,elementId){
    var time = parseInt(seconds);
    var btn = $("#"+elementId);
    btn.attr("disabled","disabled");
    btn.text("重新获取("+time+")");
    var timeOutInter = setInterval(function(){
        time = time-1;
        btn.text("重新获取("+time+")");
        if(time<0){
            btn.text("获取验证码");
            clearInterval(timeOutInter);
            btn.removeAttr("disabled");
        }
    },1000);
}



function showTime(balanceTime, dayId, hoursId, minutesId, secondsId){
    var SysSecond=parseInt(balanceTime);

    var timeOutInter = setInterval(function(){
        SysSecond = SysSecond-1;

        if (SysSecond > 0) {
            var time = SysSecond;
            int_day=Math.floor(time/86400);
            time-=int_day*86400;
            int_hour=Math.floor(time/3600);
            time-=int_hour*3600;
            int_minute=Math.floor(time/60);
            time-=int_minute*60;
            int_second=Math.floor(time);

            if(int_hour<10){
                int_hour="0"+int_hour;
            }

            if(int_minute<10){
                int_minute="0"+int_minute;
            }

            if (int_second<10){
                int_second="0"+int_second;
            }
            $(dayId).html(int_day);
            $(hoursId).html(int_hour);
            $(minutesId).html(int_minute);
            $(secondsId).html(int_second);
        }else{
            $(dayId).html("00");
            $(hoursId).html("00");
            $(minutesId).html("00");
            $(secondsId).html("00");
        }
    },1000);

}

/*
 checkBox验证
 */
function veriCheckBox(checkBoxId){
    if (!$(checkBoxId).is(":checked")){
        return "请勾选选项";
    }
    return true;
}
/*
 checkBox验证
 */
function veriEmail(email){
    if(email.length == 0){
        return "邮箱不能为空！";
    }
    if(!email.isEmail()){
        return "邮箱格式不正确！";
    }
    return true;
}
/*
 身份证校验
 */
function veriIdentity(arrIdCard){
    if(arrIdCard.length == 0){
        return "身份证信息不能为空！";
    }
    if(!arrIdCard.isCardId()){
        return "您输入的身份证信息有误，请核对后重新输入！";
    }
    return true;
}
/*
 身份证15转18位
 */
function idCard15To18(id){
    var W = new Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1);
    var A = new Array("1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2");
    var i,j,s=0;
    var newid;
    newid = id;
    newid = newid.substring(0,6)+"19"+newid.substring(6,id.length);
    for(i=0;i<newid.length;i++ ){
        j= parseInt(newid.substring(i,i+1))*W[i];
        s=s+j;
    }
    s = s % 11;
    newid=newid+A[s];
    return newid;
}


/*
 姓名验证
 */
function veriUserName(nameStr){
    if(nameStr.length==0){
        return "姓名不能为空";
    }
    var reg = /^[u4E00-u9FA5]+$/;
    if(reg.test(nameStr)){
        return "您输入的姓名格式不对，请重新输入";
    }
    return true;
}
/*
 银行卡校验
 */
function veriBankcardNo(bankcardNoStr){
    var str = bankcardNoStr;
    if(isNaN(str)){
        return "银行卡格式不正确";
    }
    if(str.length==0){
        return "银行卡号不能为空";
    }
    if(str.length<16){
        return "银行卡号格式不正确"
    }
    return true;
}
/*
 select校验
 */
function veriSelect(selectVal,selectName){
    var str = selectVal;
    if('0'==str||null==str||"null"==str){
        return "请选择"+selectName;
    }
    return true;
}

/*检测最小投资金额*/
function verificationMinAmount(amount,minAmount,maxAmount){
    var str = amount.toString();
    var tamount = Number(amount);
    var tminAmount = Number(minAmount);
    var tmaxAmount = Number(maxAmount);
    if(tamount==0){
        return "输入金额不能为0";
    }
    if(isNaN(str)){
        return "您输入的金额格式有误";
    }
    if(str.match("-")!=null){
        return "输入金额不能为负数";
    }
    if(tamount<tminAmount){
        return "起投金额最低"+minAmount+"元";
    }
    if(arguments.length>2){
        if(tamount>tmaxAmount&&maxAmount!=""){
            return "输入金额大于剩余可投金额";
        }
    }
    if(2<(str.length-1-str.indexOf('.'))&&str.indexOf('.')!=-1){
        return "只能保留2位小数";
    }
    if(str.length==0){
        return "输入金额不能为空";
    }
    return true;
}
/*
 输入金额校验
 */
function veriMoney(strMoney,minAmount,maxAmount){
    //alert(maxAmount);
    var str = strMoney.toString();
    var str_min = minAmount.toString();
    var str_max = maxAmount==null?null:maxAmount.toString();
    if(Number(strMoney)==0){
        return "输入金额不能为0";
    }
    if(str.length==0||isNaN(str)){
        return "输入金额不正确！";
    }
    if(str.match("-")!=null){
        return "输入的金额不能为负！"
    }
    if(2<(str.length-1-str.indexOf('.'))&&str.indexOf('.')!=-1){
        return "只能保留2位小数！";
    }
    if(str.length>0&&Number(str)>Number(str_max)&&(str_max!=null)){
        return "输入金额超过最大限额！";
    }
    if(str.length>0&&Number(str)<Number(str_min)){
        return "输入金额小于最小限额！";
    }
    return true;
}
/*
 格式化金钱
 */

function formatMoney(strMoney){
    var str = strMoney.toString();
    if(str.indexOf('.')==-1){
        return str.toString()+".00";
    }
    if((str.length-1-str.indexOf('.'))==1){
        return str.toString()+"0";
    }
    if((str.length-1)==str.indexOf('.')){
        return str.toString()+"00";
    }
    return Number(str).toFixed(2).toString();
}
/*格式化显示金钱*/
function allFormatMoney(strMoney){
    var str = Number(strMoney).toFixed(2);
    var dottedIndex = str.indexOf(".");
    var resultStr = str.substring(dottedIndex,str.length);
    var tempStr = "";
    var count = parseInt(dottedIndex/3);
    var remainder = dottedIndex%3;
    if(count==1&&remainder==0){
        resultStr = str;
    }else{
        tempStr = str.substring(0,remainder);
        for(var i=0; i<count; i++){
            if(i==0&&remainder==0){
                tempStr = tempStr + str.substring((remainder+3*i),(remainder+3*(i+1)));
            }else{
                tempStr = tempStr +","+ str.substring((remainder+3*i),(remainder+3*(i+1)));
            }
        }
        resultStr = tempStr+resultStr;
    }
    return resultStr;
}

