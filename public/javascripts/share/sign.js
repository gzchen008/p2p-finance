
var createNonceStr = function() {
    return Math.random().toString(36).substr(2, 15);
};

var createTimestamp = function() {
    return parseInt(new Date().getTime() / 1000) + '';
};

var raw = function(args) {
    var keys = Object.keys(args);
    keys = keys.sort()
    var newArgs = {};
    keys.forEach(function(key) {
        newArgs[key.toLowerCase()] = args[key];
    });

    var string = '';
    for (var k in newArgs) {
        string += '&' + k + '=' + newArgs[k];
    }
    string = string.substr(1);
    return string;
};

/**
 * @synopsis 签名算法
 *
 * @param jsapi_ticket 用于签名的 jsapi_ticket
 * @param url 用于签名的 url ，注意必须与调用 JSAPI 时的页面 URL 完全一致
 *
 * @returns
 */
var sign = function(nonceStr, timestamp, url) {
    var signature = "";
    $.ajax({
        type: 'get',
        url: apiBaseUrl + '/thirdpart/getTicket',
        async: false,
        success: function(data) {
            var jsapi_ticket = data;
            var ret = {
                jsapi_ticket: jsapi_ticket,
                nonceStr: nonceStr,
                timestamp: timestamp,
                url: url
            };
            var string = raw(ret);
            var shaObj = new jsSHA(string, 'TEXT');
            signature = shaObj.getHash('SHA-1', 'HEX');
            return signature;
        },
        error: function(error) {
          console.log(error.toLocaleString)
        }
    });

    return signature;
};
