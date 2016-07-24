var PRODUCT_STATUS = {
    PRESELL: "1",
    SELL_ING: "2",
    REPAY_ING: "3",
    FINISH_REPAY:"4"
};

function countdown (prodArray) {
    var item = null;
    var tempArray = [];
    for (var i = 0; i < prodArray.length; i++) {
        item = prodArray[i];
        if (item.prodStatus == PRODUCT_STATUS.SELL_ING) {
            timeStr = item.sellEndTime;
        }
        else {
            timeStr = item.sellStartTime;
        }
        var countTime = new Date(timeStr);
        var now = new Date ();
        var leftTime = countTime.getTime() - now.getTime();
        var leftSec = parseInt(leftTime/1000);
        var day1=Math.floor(leftSec/(60*60*24));
        var hour=Math.floor((leftSec-day1*24*60*60)/3600);
        var minute=Math.floor((leftSec-day1*24*60*60-hour*3600)/60);
        var second=Math.floor(leftSec-day1*24*60*60-hour*3600-minute*60);
        var displayTime = day1 + "天" + hour + "小时" + minute + "分" + second + "秒";
        var prod = {prodId:item.prodId, countDown:displayTime };
        tempArray.push(prod);
    }
    console.log(tempArray.length);
    postMessage(tempArray);
}


onmessage = function (event) {
    var prodItems = event.data;
    setInterval(function (){
        countdown(prodItems);
    }, 1000)
};

