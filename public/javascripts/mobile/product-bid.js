var RESULT_STATUS = {
	SUCCESS: "0507",
	FAIL:"2506"
};


var URL = {
	predictPriceUrl: "/app/services"
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
					result = $.parseJSON(result);
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
	var getPredictPrice = function (params, callbackFunc) {
		Utils.sendRequest("GET", params, URL.predictPriceUrl, callbackFunc);
	};
	return {
		getPredictPrice :getPredictPrice
	};
})();

var Contorller = (function () {
	var predictPrice = function () {
		var bidAmount = $("#bidAmount").val();
		if (bidAmount && isNaN(bidAmount) ) {
			alert("请输入数字");
			return ;
		}
		var PERIOD_TYPE = {
			YEAR: "-1",
			MONTH : "0",
			DAY : "1"
		};

		var days = 0;
		switch (bid.periodUnit) {
			case PERIOD_TYPE.YEAR:
				days = bid.period * 365;
				break;
			case PERIOD_TYPE.MONTH:
				days = bid.period * 30;
				break;
			case PERIOD_TYPE.DAY:
				days = bid.period;
				break;
		}

		var params = {
			OPT:22,
			amount: bidAmount,
			apr: bid.apr,
			deadline: days,
			repayType: bid.repayType,
			bonus: bid.bonus,
			loadType:1
		};
		Service.getPredictPrice (params, function (data) {
			console.log(data);
			$("#showPredictPrice").html(data.interest);
		});
	};

	var bindEvent = function () {
		$("#bidAmount").on("blur", predictPrice);
	};

	var init = function () {
		bindEvent ();
	};
	return {
		init: init,
		predictPrice:predictPrice
	};
})();

//$(function (){
//	Contorller.init();
//});

$(document).ready(function() {
	Contorller.init();
});