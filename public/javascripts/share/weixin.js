var nonceStr = createNonceStr();
var timestamp = createTimestamp();
var url = window.location.href;

var signature = sign(nonceStr, timestamp, url);
wx.config({
    debug: false,
    appId: 'wx0994aa8f0061604e',
    timestamp: timestamp,
    nonceStr: nonceStr,
    signature: signature,
    jsApiList: [
        'checkJsApi',
        'onMenuShareTimeline',
        'onMenuShareAppMessage'
    ]
});

wx.ready(function() {
    // 1 判断当前版本是否支持指定 JS 接口，支持批量判断
    wx.checkJsApi({
        jsApiList: [
            'onMenuShareAppMessage',
            'onMenuShareTimeline'
        ],
        success: function(res) {
            console.info(JSON.stringify(res));
        }
    });
    //加载值区，在这里进行统一的设计


    var shareData = {
        title: shareTitle,
        desc: descContent,
        link: url,
        imgUrl: imgUrl
    };
    wx.onMenuShareAppMessage(shareData);
    wx.onMenuShareTimeline(shareData);
});

wx.error(function(res) {
    console.error("weixin: "+ res.errMsg);
});


