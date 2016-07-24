var initAdvBanner = function() {
	var $scroll = $(".scroll-box"),
		windowWidth = $(window).width(),
		divWidth = $scroll.width();

	$scroll.css("margin-left",(windowWidth-divWidth)/2+"px");

	var sWidth = $("#banner ul li").width();
	var len = $("#banner ul li").length;
	var index = 0;
	var picTimer;

	var btn = "<div class='btnBg'></div><div class='btn'>";
	for(var i=0; i < len; i++) {
		btn += "<span></span>";
	}
	btn += "</div>";

	$("#banner").append(btn);

	$("#banner .btnBg").css("opacity",1);
	$(".btn").css("left",($(window).width()-$(".btn").outerWidth())/2+"px");

	//为小按钮添加鼠标滑入事件，以显示相应的内容
	$("#banner .btn span").css("opacity",1).mouseover(function() {
		index = $("#banner .btn span").index(this);
		showPics(index);
	}).eq(0).trigger("mouseover");

	//鼠标滑上焦点图时停止自动播放，滑出时开始自动播放
	$scroll.hover(function() {
		clearInterval(picTimer);
	},function() {
		picTimer = setInterval(function() {
			showPics(index);
			index++;
			if(index == len) {index = 0;}
		},3000);
	}).trigger("mouseleave");

	//显示图片函数，根据接收的index值显示相应的内容
	function showPics(index) {
		var nowLeft = -index*1920;
		$("#banner ul").stop(true,false).animate({"left":nowLeft+"px"},300);
		$("#banner .btn span").removeClass("active").eq(index).addClass("active");
		$("#banner .btn span").stop(true,false).animate({"opacity":"1"},300).eq(index).stop(true,false).animate({"opacity":"1"},300);
	}

	$(window).resize(function(){
		var windowWidth = $(window).width(),
			divWidth = $scroll.width();
		$scroll.css("margin-left",(windowWidth-divWidth)/2+"px");
		$(".btn").css("left",($(window).width()-$(".btn").outerWidth())/2+"px");
	});
};

var TOUCH_POINT = {
	START_X: 0,
	START_Y :0
};

var isUp = false;

var productListTouchMove = function (){
	document.getElementById("main").addEventListener('touchstart', function (event){
		$("#main").css({
			"transition": ""
		}) ;
		if (event.targetTouches.length == 1) {
			//TOUCH_POINT.START_X = event.targetTouches[0].pageX;
			//TOUCH_POINT.START_Y = event.targetTouches[0].pageY;
			TOUCH_POINT.START_Y = event.targetTouches[0].screenY;
		}
	});

	document.getElementById("main").addEventListener('touchmove', function(event) {
		//event.preventDefault();
		mouseEndX = event.targetTouches[0].pageX;
		mouseEndY = event.targetTouches[0].pageY;

		X = mouseEndX - TOUCH_POINT.START_X;
		Y = mouseEndY - TOUCH_POINT.START_Y;

		//  top 2 bottom
		if (Y > 0) {
		}
		// bottom 2 top
		else {
			//console.log(Y);
		}

		if (event.targetTouches.length == 1
			&& (parseInt(event.targetTouches[0].screenY) > parseInt(TOUCH_POINT.START_Y))) {

			$("#main").css("top", (event.targetTouches[0].screenY - parseInt(TOUCH_POINT.START_Y )) +"px") ;
			console.log(parseInt($("#product-list").css("top")) + " top move");
		}
	});

	document.getElementById("main").addEventListener('touchend', function(event) {
		console.log(parseInt($("#main").css("top")) + " top");
		if (parseInt($("#main").css("top")) > 0) {
			Business.getProductList();
			$("#main").css({
				"top": "0",
				"transition": "top 2s"
			}) ;
		}
	});
};

var URL = {
	productListUrl: "/mobile/products"
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
					callbackFunc(result);
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
	var getProductList= function (params, callbackFunc) {
		Utils.sendRequest("GET", params, URL.productListUrl, callbackFunc) ;
	};
	return {
		getProductList :getProductList
	};
})();

var PRODUCT_STATUS = {
	PRESELL: "1",
	SELL_ING: "2",
	REPAY_ING: "3",
	FINISH_REPAY:"4"
};

var Business = (function ($) {
	var worker = null;
	var countdownArr = [];
	var tmpls = {
		presellTmpl : $("#presellStatusTmpl"),
		sellingTmpl : $("#sellingStatusTmpl"),
		repayingTmpl : $("#repayingStatusTmpl"),
		finisRepayTmpl : $("#finishRepayStatusTmpl")
	};
	var pageIdx = 0;
	var pageSize = 12;
	var getProductList = function () {
		var params = {
			index :pageIdx,
			pageSize : pageSize
		};
		pageIdx++;
		Service.getProductList(params, function (result) {
			if (result.list && result.list.length) {
				var productArray = result.list;
				var arr = [];
				for (var i = 0; i < productArray.length; i ++ ) {
					var prod =productArray [i];
					switch  (prod.prodStatus){
						case PRODUCT_STATUS.PRESELL:
							arr = arr.concat(tmpls.presellTmpl.tmpl(prod));
							countdownArr.push(prod);
							break;
						case PRODUCT_STATUS.SELL_ING:
							arr = arr.concat(tmpls.sellingTmpl.tmpl(prod));
							countdownArr.push(prod);
							break;
						case PRODUCT_STATUS.REPAY_ING:
							arr = arr.concat(tmpls.repayingTmpl.tmpl(prod));
							break;
						case PRODUCT_STATUS.FINISH_REPAY:
							arr = arr.concat(tmpls.finisRepayTmpl.tmpl(prod));
							break;
					}
				}
				$(arr).prependTo("#list");
				if (!worker) {
					worker = createWorkerForCountDown();
				}
				else{
					worker.terminate();
					worker = createWorkerForCountDown();
				}
			}
		});
	};

	var productClickHandler = function (event, dom) {
		var id = $(dom).data("myid");
		window.location.href="/mobile/product/detail_v1?bidId="+id;
	};

	var createWorkerForCountDown = function () {
		var worker = new Worker("/public/javascripts/mobile/countdown.js");
		worker.postMessage(countdownArr);
		worker.onmessage = function(event) {
			if (event && event.data) {
				for (var i = 0; i < event.data.length; i++) {
					var prod = $("#prod"+event.data[i].prodId);
					prod.find(".countdown").html(event.data[i].countDown);
				}
			}
		}
		return worker;
	};
	var init = function () {
		getProductList();
	};

	return {
		init:init,
		getProductList : getProductList,
		productClickHandler: productClickHandler
	};
})(jQuery);

$(function () {
	initAdvBanner();
	productListTouchMove();
	//initCountdown();
	Business.init();
});