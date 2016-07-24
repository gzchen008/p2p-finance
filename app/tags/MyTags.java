package tags;

import groovy.lang.Closure;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import com.shove.Convert;
import constants.Constants;
import play.templates.FastTags;
import play.templates.TagContext;
import play.templates.GroovyTemplate.ExecutableTemplate;
import play.templates.JavaExtensions;
import utils.Arith;
import utils.Page;

public class MyTags extends FastTags {

    /**
     * 分页标签
     */
    public static void _page(Map<String, Object> params, Closure body, PrintWriter out,
                             ExecutableTemplate template, int fromLine) {

        int currPage = (Integer) params.get("currPage");
        int pageSize = (Integer) (params.get("pageSize") == null ? Constants.TEN : params.get("pageSize"));
        int totalCount = (Integer) params.get("totalCount");
        int maxSize = params.get("maxSize") == null ? 0 : (Integer) params.get("maxSize");
        int theme = (Integer) params.get("theme") == null ? Constants.PAGE_SIMPLE : (Integer) params.get("theme");
        int style = (Integer) params.get("style") == null ? Constants.PAGE_STYLE_DEFAULT : (Integer) params.get("style");
        String funMethod = (String) params.get("funMethod");
        String pageTitle = params.get("pageTitle") == null ? "条记录" : params.get("pageTitle") + "";
        String condition = null;
        if (body != null) {
            condition = JavaExtensions.toString(body);
        }


        Page page = new Page();

        page.currPage = currPage;
        page.pageSize = pageSize;
        page.totalCount = totalCount;
        page.funMethod = funMethod;
        page.pageTitle = pageTitle;
        page.maxSize = maxSize;
        page.style = style;
        if (condition != null) {
            page.conditions = condition;
        }

        if (theme == Constants.PAGE_SIMPLE) {
            String pageTag = page.getThemeNumber();
            out.println(pageTag);
        }

        if (theme == Constants.PAGE_ASYNCH) {
            String pageTag = page.getThemeNumberScript();
            out.println(pageTag);
        }

    }

//	public static void _table(Map<String, Object> params, Closure body, PrintWriter out, 
//			ExecutableTemplate template, int fromLine) { 
//		
//		List<String> columnNames =  (List<String>) params.get("columnNames");//表头列名
//		List<String> fields =  (List<String>) params.get("fields");//字段名
//		List<Object> listMap = (List<Object>) params.get("data");//对象集合
//		
//		String tableStyle = (String) params.get("tableStyle");//table样式
//		String thStyle = (String) params.get("thStyle");//table th样式
//		String trStyle = (String) params.get("trStyle");//table tr样式
//		String tdStyle = (String) params.get("tdStyle");//table td样式
//		
//		Sheet sheet = new Sheet();
//		String sheetTag = sheet.getTableCode(columnNames, fields, listMap,tableStyle,thStyle,trStyle,tdStyle);
//		
//		
//		out.println(sheetTag);
//		
//	}


//	public static void _page2(Map<String, Integer> params, Closure body, PrintWriter out,
//			ExecutableTemplate template, int fromLine) {
//		
//		int currPage = params.get("currPage");
//		int pageSize = params.get("pageSize") == null ? 10 : params.get("pageSize");
//		int totalCount = params.get("totalCount");
//		int type = params.get("type");
//		
//		Page page = new Page();
//		
//		page.currPage = currPage;
//		page.pageSize = pageSize;
//		page.totalCount = totalCount;
//		page.type = type;
//		
//		
//		String pageTag = page.getThemeNumberScript();
//		out.println(pageTag);
//	}

    /**
     * 金额格式化标签
     */
    public static void _format(Map<String, Object> params, Closure body, PrintWriter out,
                               ExecutableTemplate template, int fromLine) {
        double money = (Double) params.get("money");

        String result = "";

        if (money <= 10000) {
            result = String.format("%.2f", money);
        } else if (10000 < money && money <= 100000000) {
            result = Arith.round(money / 10000, 2) + "万";
        } else {
            result = Arith.round(money / 100000000, 4) + "亿";
        }

        out.println(result);
    }

    public static void _formatMoney(Map<String, Object> params, Closure body, PrintWriter out,
                                    ExecutableTemplate template, int fromLine) {
        double money = (Double) params.get("money");

        NumberFormat formater = new DecimalFormat("###,##0.00");

        String result = formater.format(money);

        if (result.indexOf(".") == -1) {
            result += ".00";
        }
        out.println(result);
    }

    public static void _table(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        TagContext.current().data.put("tr_class_odd", args.get("tr_class_odd"));
        TagContext.current().data.put("tr_class_even", args.get("tr_class_even"));

        out.print("<table " + serialize(args, "tr_class_odd", "tr_class_even") + ">");
        out.println(JavaExtensions.toString(body));
        out.print("</table>");
    }

    public static void _tr(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        TagContext.current().data.put("tr_index", args.get("tr_index"));

        TagContext.current().data.put("th_height", args.get("th_height"));
        TagContext.current().data.put("th_align", args.get("th_align"));
        TagContext.current().data.put("th_valign", args.get("th_valign"));
        TagContext.current().data.put("th_bgcolor", args.get("th_bgcolor"));

        TagContext.current().data.put("td_height", args.get("td_height"));
        TagContext.current().data.put("td_align", args.get("td_align"));
        TagContext.current().data.put("td_valign", args.get("td_valign"));
        TagContext.current().data.put("td_bgcolor_odd", args.get("td_bgcolor_odd"));
        TagContext.current().data.put("td_bgcolor_even", args.get("td_bgcolor_even"));

        Object cls = args.get("class");

        if (cls == null) {
            if (args.get("tr_index") == null) {
                cls = null;
            } else if (Convert.strToInt(args.get("tr_index") + "", 0) % 2 == 1) {
                cls = TagContext.parent("table").data.get("tr_class_odd");
            } else {
                cls = TagContext.parent("table").data.get("tr_class_even");
            }
        }

        cls = (cls == null) ? "" : " class = \"" + cls + "\"";

        out.print("<tr " + cls + serialize(args, "th_height", "th_align", "th_valign", "th_bgcolor", "td_height", "td_align", "td_valign", "td_bgcolor_odd", "td_bgcolor_even", "tr_index", "class") + ">");
        out.println(JavaExtensions.toString(body));
        out.print("</tr>");
    }

    public static void _th(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        Object height = args.get("height");
        Object align = args.get("align");
        Object valign = args.get("valign");
        Object bgcolor = args.get("bgcolor");

        height = (height == null) ? TagContext.parent("tr").data.get("th_height") : height;
        align = (align == null) ? TagContext.parent("tr").data.get("th_align") : align;
        valign = (valign == null) ? TagContext.parent("tr").data.get("th_valign") : valign;
        bgcolor = (bgcolor == null) ? TagContext.parent("tr").data.get("th_bgcolor") : bgcolor;

        height = (height == null) ? "" : " height = \"" + height + "\"";
        align = (align == null) ? "" : " align = \"" + align + "\"";
        valign = (valign == null) ? "" : " valign = \"" + valign + "\"";
        bgcolor = (bgcolor == null) ? "" : " bgcolor = \"" + bgcolor + "\"";

        out.print("<th " + height + align + valign + serialize(args, "height", "align", "valign", "bgcolor") + ">");
        out.println(JavaExtensions.toString(body));
        out.print("</th>");
    }

    public static void _td(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        Object height = args.get("height");
        Object align = args.get("align");
        Object valign = args.get("valign");

        height = (height == null) ? TagContext.parent("tr").data.get("td_height") : height;
        align = (align == null) ? TagContext.parent("tr").data.get("td_align") : align;
        valign = (valign == null) ? TagContext.parent("tr").data.get("td_valign") : valign;

        height = (height == null) ? "" : " height = \"" + height + "\"";
        align = (align == null) ? "" : " align = \"" + align + "\"";
        valign = (valign == null) ? "" : " valign = \"" + valign + "\"";

        Object bgcolor = args.get("bgcolor");

        if (bgcolor == null) {
            if (TagContext.parent("tr").data.get("tr_index") == null) {
                bgcolor = null;
            } else if (Convert.strToInt(TagContext.parent("tr").data.get("tr_index") + "", 0) % 2 == 1) {
                bgcolor = TagContext.parent("tr").data.get("td_bgcolor_odd");
            } else {
                bgcolor = TagContext.parent("tr").data.get("td_bgcolor_even");
            }
        }

        bgcolor = (bgcolor == null) ? "" : " bgcolor = \"" + bgcolor + "\"";

        out.print("<td " + height + align + valign + bgcolor + serialize(args, "height", "align", "valign", "bgcolor") + ">");
        out.println(JavaExtensions.toString(body));
        out.print("</td>");
    }

    /**
     * 转换 '-' 为逾期的标签
     *
     * @param args
     * @param body
     * @param out
     * @param template
     * @param fromLine
     */
    public static void _overdue(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        String day = args.get("day") == null ? null : args.get("day").toString();

        if (null == day)
            return;

        if (day.contains("-")) {
            out.println(day.replace("-", "逾期"));
        }
    }

    public static void _img(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
      /*
            "${currBackstageSet?.supervisorPlatformLog}", alt:"LOGO", title:"${currBackstageSet?.companyName}"*/


        StringBuilder html = new StringBuilder();
        html.append("<img src=\"");
        html.append(args.get("src"));
        html.append("\"");
        html.append(" alt=\"");
        html.append(args.get("alt"));
        html.append("\"");
        if(args.get("width") != null) {
            html.append(" width=\"");
            html.append(args.get("width"));
            html.append("\"");
        }
        if(args.get("height") != null) {
            html.append(" height=\"");
            html.append(args.get("height"));
            html.append("\"");
        }
        if(args.get("title") != null) {
            html.append(" title=\"");
            html.append(args.get("title"));
            html.append("\"");
        }
        html.append(" />");
        out.print(html.toString());
    }
}
