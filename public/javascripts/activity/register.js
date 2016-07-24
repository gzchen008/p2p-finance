var RESULT_STATUS = {
	SUCCESS: "0507",
	FAIL:"2506"
};


//var host = "https://api-2.sunlights.me/api";
var host = (function() {
	var prdUrl1 = "51jdj.com";
	var prdUrl2 = "www.51jdj.com";
	var uatUrl = "p2p.sunlights.me";
	var localhost = "localhost:9000";
	var fpUrl = "";
	switch (window.location.host) {
		case prdUrl1:
		case prdUrl2:
			fpUrl = "https://api.sunlights.me/api";
			break;
		case uatUrl:
		case localhost:
			fpUrl = "https://api-2.sunlights.me/api";
			break;
		default :
			break;
	}
	return fpUrl;
})();

var URL = {
	broadcaset: host + "/customer/activity/report"
};

var Utils = (function ($) {
	var sendRequest = function (type, param, url, callbackFunc) {
		return $.ajax({
			type: type,
			data : param,
			url: url,
			async: false,
			success: function (result) {
				if (result && result != "") {
					if (result.message.code == RESULT_STATUS.SUCCESS && callbackFunc) {
						callbackFunc(result.value);
					}
				}
				else {
					alert("后台出错");
				}
			},
			error : function (result) {
			}
		});
	};
	return {
		sendRequest : sendRequest
	};
})(jQuery);

var Service = (function () {
	var getBroadcastInfo = function (callbackFunc) {
		Utils.sendRequest("GET", null, URL.broadcaset, callbackFunc) ;
	};
	return {
		getBroadcastInfo :getBroadcastInfo
	};
})();

var Business = (function ($) {
	var cacheData = {};
	var registerPointer = new Number(0);
	var rewardPointer = new Number(0);
	var rewardTitle = "";
	var init = function () {
		getBroadcastInfo();
		bindEvent();
	};

	var getBroadcastInfo = function () {
		Service.getBroadcastInfo (function (result){
			cacheData = result;
			rewardTitle = cacheData.rewardList.shift();
			displayData ();
			setInterval( displayData, 2000);
		});
	};

	var getDisplayData = function  (list, pointer, quantity) {
		var tempArr = [];
		var i = 0;
		while (i < quantity) {
			if (pointer == list.length) {
				pointer = 0;
			}
			tempArr.push(list[pointer]);
			pointer ++;
			i ++;
		}
		return {
			array : tempArr,
			pointer : pointer
		};
	};

	var displayData = function () {
		var registerData = getDisplayData(cacheData.registerList, registerPointer, 3);
		registerPointer = registerData.pointer;
		var rewardData = getDisplayData(cacheData.rewardList,rewardPointer, 2);
		rewardPointer = rewardData.pointer;
		rewardData.array.unshift(rewardTitle);
		var data = {
			registerList : registerData.array,
			rewardList : rewardData.array
		};
		renderTmpl(data);
	};

	var renderTmpl = function (data) {
		$("#broadcast").html("");
		$("#broadcastInfoTmpl").tmpl(data).appendTo("#broadcast");
	};

	var bindEvent = function () {
		$(".register-btn").on("click", registerBtnHandler);
		$(".register-new-customer-link").on("click", function () {
			var url = "http://" + window.location.host + "/front/help/index?typeId=13";
			window.location.href = url ;
		});
		$(".security-link").on("click", function () {
			var url = "http://" + window.location.host + "/front/principal/principalGuaranteeHome";
			window.location.href = url ;
		});
	};

	var registerBtnHandler = function () {
		if (_hmt) {
			_hmt.push(['_trackEvent', 'register', 'click', 'newRegister']);
		}
		var url = "http://" + window.location.host + "/register";
		window.location.href = url ;
	};

	return {
		init: init,
		displayData: displayData,
		renderTmpl : renderTmpl
	};
})(jQuery);

$(function (){
	Business.init ();

});
