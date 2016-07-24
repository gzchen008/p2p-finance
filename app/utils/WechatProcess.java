package utils;

import business.Articles;
import business.LinkMessage;
import business.ReceiveXmlEntity;
import play.Logger;

import java.util.Random;

/**
 * Created by libaozhong on 2015/6/16.
 */
public class WechatProcess {
    public String processWechatMag(String xml){
        Logger.info("WechatProcess.processWechatMag");
        /** 解析xml数据 */
        ReceiveXmlEntity xmlEntity = new ReceiveXmlProcess().getMsgEntity(xml);

        /** 以文本消息为例，调用图灵机器人api接口，获取回复内容 */
        String result = "";
        Logger.info(xmlEntity.getMsgType());
        Logger.info(String.valueOf("text".equals(xmlEntity.getMsgType())));
        String openId= xmlEntity.getFromUserName();
        if("text".equals(xmlEntity.getMsgType())) {
            Logger.info(xmlEntity.getContent());
            Logger.info("xmlEntity.getContent()");
            if(xmlEntity.getContent().equals("我要祝福")){
//                Logger.info("link生成");
//                LinkMessage link=new LinkMessage();
//                link.setContent("点击领取红包");
//                link.setTitle("加薪猫送红包");
//                link.setLink("http:p2p.sunlights.me/mobile/weixin/sendRedpact?redPacketId=1");
//                link.setMsgId(String.valueOf(new Random(5234567890123456l).nextLong()));
//                Logger.info(link.toString());
//                result = new FormatXmlProcess().formatLinkXmlAnswer(xmlEntity.getFromUserName(), xmlEntity.getToUserName(),
//                        link.getTitle(),link.getContent(),link.getLink(),link.getMsgId());
//                Logger.info(result.toString());
                String[] zhufu=new String[]{"你真帅啊！","你的妆化的真好!","不请人家吃饭生气啦","一缕春风，两滴夏雨，三片秋叶，四朵冬雪，做成五颜六色的礼盒；打着七彩八飘的丝带，用九分真诚，十分热情，装进无数祝福送给你，愿端午节好运",
                "even though we are apart, you are in my heart this season.千山万水，隔不断我在佳节对你的思念。","嗨，兄弟，又升职了？！恭喜恭喜，请客请客！","Wishing you and yours a happy happy new year.  万事如意，合家平安",
                "Wishing you a wonderful beginning, and love to last a lifetime.\n" +
                        "　　愿你有一个美妙的开始,相爱一辈子。",""};
                int dt = new Random().nextInt(7);
                Logger.info(String.valueOf(dt));
                 result= zhufu[dt];
                Logger.info(result);
                result = new FormatXmlProcess().formatXmlAnswer(xmlEntity.getFromUserName(), xmlEntity.getToUserName(), result);
            }else if(xmlEntity.getContent().equals("我要红包")){
                result="<a href=\"http://p2pv2.sunlights.me/mobile/weixin/sendRedpact?redPacketId=1&openid=openId\">领取红包</a>";
                result = new FormatXmlProcess().formatXmlAnswer(xmlEntity.getFromUserName(), xmlEntity.getToUserName(), result);
            }else if(xmlEntity.getContent().equals("我要爱情")){
//                Articles articles =new Articles();
//                articles.setArticleCount(1);
//                articles.setContent("");
//                articles.setDescription("爱情这种虚幻的东西,只有佛祖你保佑你！");
//                articles.setPicUrl("");
//                articles.setTitle("");
//                articles.setUrl("http:p2p.sunlights.me/mobile/weixin/sendRedpact?redPacketId=1");
//                new FormatXmlProcess().formatArticlesXmlAnswer(xmlEntity.getFromUserName(), xmlEntity.getToUserName(), articles);
                result="你确定你要的是爱情?还是情爱？";
                result = new FormatXmlProcess().formatXmlAnswer(xmlEntity.getFromUserName(), xmlEntity.getToUserName(), result);
            }else if(xmlEntity.getContent().equals("test")){
                Articles articles =new Articles();
                articles.setArticleCount(1);
                articles.setContent("");
                articles.setDescription("爱情这种虚幻的东西,只有佛祖你保佑你！");
                articles.setPicUrl("");
                articles.setTitle("");
                articles.setUrl("http:p2p.sunlights.me/mobile/weixin/sendRedpact?redPacketId=1");
                result =  new FormatXmlProcess().formatArticlesXmlAnswer(xmlEntity.getFromUserName(), xmlEntity.getToUserName(), articles);;
            };;


        //    result = new TulingApiProcess().getTulingResult(xmlEntity.getContent());
        }else{
            result = new FormatXmlProcess().formatXmlAnswer(xmlEntity.getFromUserName(), xmlEntity.getToUserName(), result);
        }

        /** 此时，如果用户输入的是“你好”，在经过上面的过程之后，result为“你也好”类似的内容
         *  因为最终回复给微信的也是xml格式的数据，所有需要将其封装为文本类型返回消息
         * */

        return result;
    }
}
