/**
 * MJL 2014/8/27.
 */
function widthbox(){
    var win_w = $(window,document).width(); //屏幕当前宽度
    $(".xn_c_contentwarp").width(win_w); //主盒子宽度等于屏幕宽度
    $(".xn_c_content").width(win_w-265);
    
    // 导航收缩
    var condqk = $(".xn_c_content").width(); //屏幕当前宽度
    $(".xn_c_con_enimg").click(function(){
     	var le_hidd = $(this).parent().siblings(".xn_c_content_leftul").is(":hidden"),
     		le_visi = $(this).parent().siblings(".xn_c_content_leftul").is(":visible")
     		if (le_hidd) {
     			$(this).parent().siblings(".xn_c_content_leftul").show();
     			$(this).removeClass("xn_c_con_enimg_off");
     			var iw = $(".xn_c_content").width();
     			$(".xn_c_content").width(iw-176);
     		};
     		if (le_visi) {
     			$(this).parent().siblings(".xn_c_content_leftul").hide();
     			$(this).addClass("xn_c_con_enimg_off");
     			$(".xn_c_content").width(condqk+176);
     		};
     });
}
$(function(){
    widthbox();
    //导航
    $(".xn_c_li_head_two").hide();
    $(".xn_c_li_bg").click(function(){
    	var ernav = $(this).parent().siblings(".xn_c_li_head_two").is(":hidden"),
    		ernav2 = $(this).parent().siblings(".xn_c_li_head_two").is(":visible")
    	if(ernav){
    		$(this).parent().siblings(".xn_c_li_head_two").show();
    		$(this).addClass("xn_c_li_bg_jian");
    	}
    	if(ernav2){
    		$(this).parent().siblings(".xn_c_li_head_two").hide();
    		$(this).removeClass("xn_c_li_bg_jian");
    	}
    });

    //选项卡
    $(".xf_ht_Tab:eq(0)").show();
    $("#xf_wyjkview_xxk").children("ul").find("li").click(function(){
        var xxkli = $(this).index();
        $(this).addClass("xf_con_wyjk_r_liishot").siblings().removeClass("xf_con_wyjk_r_liishot");
        $(".xf_ht_Tab").eq(xxkli).show().siblings(".xf_ht_Tab").hide();
    });

    // 伸缩面板按钮
    var nav_mh = $(".xn_c_content_leftul").height(),
        on_mh = $(".xn_c_con_leftbutton").height(),
        xc_ht = (nav_mh-on_mh)/2;
        $(".xn_c_con_leftbutton").css({"margin-top":xc_ht});
        $(".xn_c_li_bg").click(function(){
            var nav_mh = $(".xn_c_content_leftul").height(),
                on_mh = $(".xn_c_con_leftbutton").height(),
                xc_ht = (nav_mh-on_mh)/2;
                $(".xn_c_con_leftbutton").css({"margin-top":xc_ht});
        });
});
$(window).resize(function(){
    widthbox();
});