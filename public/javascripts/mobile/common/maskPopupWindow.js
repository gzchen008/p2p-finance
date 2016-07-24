/**
 * Created by Element on 2015/6/4.
 */
(function (){
	jQuery.extend({
		alertWindow:{
			 show: function (alertContent) {
				if ($("#alertMaskWindow").length == 0) {
					var temp = '<div id="alertMaskWindow" class="mask">'
							+'<div id="alertMaskWindowContainer">'
							+ '<p style="margin-bottom:2rem; text-align: center;">' + alertContent + '</p>'
							+ '<div style="text-align: center; color:#000;">'
							+'<a style="padding: 0.5rem 1rem;border-radius: 2px; border: solid 1px silver; text-align: center; color:#000;">' +'确定'+ '</a>'
							+'</div>'
							+'</div>'
						+'</div>';
					$(document.body).append($(temp));
					$("#alertMaskWindow").css({
						'position': 'absolute',
						'top': '0px',
						'filter': 'alpha(opacity=60)',
						'background-color': 'rgba(105,105,105, 0.7)',
						'z-index': '10000',
						'left': '0px',
						//'opacity': '0.5',
						'-moz-opacity': '0.5',
						'font-size' : '1.5rem'
					});

					$("#alertMaskWindowContainer").css({
						'background-color' : "#fff",
						'padding':"2rem",
						'position' : "absolute",
						'top' : $(document).height ()/2 - $("#alertMaskWindowContainer").height()/2 -30 ,
						'left' : $(document).width ()/2 - $("#alertMaskWindowContainer").width()/2 - 20,
						'border-radius' : '4px'
					});

					$("#alertMaskWindowContainer a").on("click", function () {
						$("#alertMaskWindow").css({
							'display':'none'
						});
					});
				}
				 $("#alertMaskWindow").css({
					 "height":$(document).height(),
					"width": $(document).width (),
					 'display':'block'
				 });

			}
		}
	});
})(jQuery);
