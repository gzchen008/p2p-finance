(function ($) {
    $.extend({
        ProgressBar : {
            createProgress : function (identifier, css) {
                var tmpl = "<div class=''><div class=''>&nbsp;</div></div>";
                if (instance.length) {

                }
            },
            setProgress : function (identifier, percent) {
                var instance = $("." + identifier) || $("#" + identifier);
                var progress = instance.find(".progress");
                progress.width((parseInt(instance.width(), 10) * parseInt(percent, 10) /100).toFixed(2));
            }
        }
    });
})(jQuery);