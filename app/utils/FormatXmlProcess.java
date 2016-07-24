package utils;

import business.Articles;

import java.util.Date;

/**
 * Created by libaozhong on 2015/6/16.
 */
public class FormatXmlProcess {
    /**
     * 封装文字类的返回消息
     * @param to
     * @param from
     * @param content
     * @return
     */
    public String formatXmlAnswer(String to, String from, String content) {
        StringBuffer sb = new StringBuffer();
        Date date = new Date();
        sb.append("<xml><ToUserName><![CDATA[");
        sb.append(to);
        sb.append("]]></ToUserName><FromUserName><![CDATA[");
        sb.append(from);
        sb.append("]]></FromUserName><CreateTime>");
        sb.append(date.getTime());
        sb.append("</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[");
        sb.append(content);
        sb.append("]]></Content><FuncFlag>0</FuncFlag></xml>");
        return sb.toString();
    }
    /**
     * 封装链接的返回消息
     * @param to
     * @param from
     * @return
     */
    public String formatLinkXmlAnswer(String to, String from, String title,String Description,String url,String msgid) {
        StringBuffer sb = new StringBuffer();
        Date date = new Date();
        sb.append("<xml><ToUserName><![CDATA[");
        sb.append(to);
        sb.append("]]></ToUserName><FromUserName><![CDATA[");
        sb.append(from);
        sb.append("]]></FromUserName><CreateTime>");
        sb.append(date.getTime());
        sb.append("</CreateTime><MsgType><![CDATA[link]]></MsgType><Title><![CDATA[");
        sb.append(title);
        sb.append("]]></Title><Description><![CDATA[");
        sb.append(Description);
        sb.append("]]</Description><Url><![CDATA[");
        sb.append(url);
        sb.append("]]</Url><MsgId>");
        sb.append(msgid);
        sb.append("</MsgId></xml>");
        return sb.toString();
    }

    public String formatArticlesXmlAnswer(String to, String from, Articles result) {
        StringBuffer sb = new StringBuffer();
        Date date = new Date();
        sb.append("<xml><ToUserName><![CDATA[");
        sb.append(to);
        sb.append("]]></ToUserName><FromUserName><![CDATA[");
        sb.append(from);
        sb.append("]]></FromUserName><CreateTime>");
        sb.append(date.getTime());
        sb.append("</CreateTime><MsgType><![CDATA[news]]></MsgType><ArticleCount><![CDATA[");
        sb.append(result.getArticleCount());
        sb.append("]]></ArticleCount><Articles><item><Title><![CDATA[");
        sb.append(result.getTitle());
        sb.append("]]</Title><Description>");
        sb.append(result.getDescription());
        sb.append("</Description><PicUrl><![CDATA[");
        sb.append(result.getPicUrl());
        sb.append("]]</PicUrl><Url><![CDATA[");
        sb.append(result.getUrl());
        sb.append("]]</Url></item></Articles></xml>");
        return sb.toString();
    }
}
