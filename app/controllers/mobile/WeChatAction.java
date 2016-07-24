package controllers.mobile;
import business.RedPacket;
import business.RedPacketBill;
import business.RedPacketParam;
import business.User;
import constants.Constants;
import controllers.BaseController;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Scope;
//import sun.beans.editors.LongEditor;
import utils.ErrorInfo;
import utils.ParseClientUtil;
import utils.WebChartUtil;
import utils.WechatProcess;

import java.io.*;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;

/**
 * Created by libaozhong on 2015/6/4.
 */
public class WeChatAction extends BaseController {

    public static void authentication() throws IOException {
        play.mvc.Http.Response.current().setHeader("contentType", "text/html; charset=utf-8");
        String result = "";
        /** 判断是否是微信接入激活验证，只有首次接入验证时才会收到echostr参数，此时需要把它直接返回 */
        Http.Request reuqets = Http.Request.current();
        String echostr =reuqets.params.get("echostr");
        if (echostr != null && echostr.length() > 1) {
            result = echostr;
        }

        try {
            OutputStream os = Http.Response.current().out;
            os.write(result.getBytes("UTF-8"));
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void  processAction() {
        play.mvc.Http.Response.current().setHeader("contentType", "text/html; charset=utf-8");
        Logger.info("POST 方法接收");
        StringBuffer sb = new StringBuffer();
        Logger.info("建立字符串");
        try{
            Logger.info("读取结束");
            String xml =Http.Request.current().params.get("body"); //次即为接收到微信端发送过来的xml数据
            Logger.info("接收到字符串:"+xml);
            String result = new WechatProcess().processWechatMag(xml);
            OutputStream os = Http.Response.current().out;
            os.write(result.getBytes("UTF-8"));
            os.flush();
            os.close();
        }catch (Exception e){
         Logger.info(e.getMessage());
        }

    }
    public static void sendPacket() throws Exception {
        String redPacketId=params.get("redPacketId");
        String openid=params.get("openid");
        if(StringUtils.isEmpty(openid)){
            JSONObject josn = sendPacketPost(openid, redPacketId);
            processSendResult(josn);
        }else {
            Http.Response.current().setCookie("redPacketId", redPacketId);
            if (ParseClientUtil.isWeiXin()) {
                String url = WebChartUtil.buildWeChatGateUrl("7", redPacketId);
                Logger.info("url：" + url);
                redirect(url);
            }
        }
    }
    public static void getOpenId() throws IOException {
        Http.Response.current().setContentTypeIfNotSet("text/html; charset=utf-8");
        Logger.info("用户进入：");
        String code = params.get("code");
        String status = params.get("state");
        String mobile = params.get("mobile");
        Logger.info("code为：" + code + "status:" + status);
        String openId = null;

        openId = getOpenIdAndSessionToken(code);
        try {
            OutputStream os = Http.Response.current().out;
            os.write(openId.getBytes("UTF-8"));
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 微信回调
     * @throws IOException
     */
    public static void weChatCB() throws Exception {
        Http.Response.current().setContentTypeIfNotSet("text/html; charset=utf-8");
        Logger.info("用户进入：");
        String code= params.get("code");
        String status= params.get("state");
        String mobile= params.get("mobile");
        Logger.info("code为：" + code + "status:" + status+"mobile"+mobile);
        String openId=null;

       openId= getOpenIdAndSessionToken(code);

        if (openId == null) {//请求过期失效
            renderTemplate("mobile/WeChatAction/weChatFailTip.html");
        }

        JSONObject paramsJson = new JSONObject();
        paramsJson.put("openId", openId);
        paramsJson.put("status", status);
        Logger.info(">>  weChatCB  openid:" + openId + "status:" + status+"mobile"+mobile);

        ErrorInfo error = new ErrorInfo();
        User user = new User();
        user.mobile = mobile;
        String name = user.findBySocialToFp(WebChartUtil.WECHAT, openId, error);
        Logger.info("查询结果：name" + name);

        if(status.equals(Constants.WEIXINSTATUS.LOGIN)){
            Logger.info("登录openid:"+openId+"status:"+status);
            weChatLogin(user, name, openId, error);
        }else if(status.equals(Constants.WEIXINSTATUS.REGISTER)){
            Logger.info("注册openid:"+openId+"status:"+status);
            weChatRegister(user, name, openId, error);
        }else if(status.equals(Constants.WEIXINSTATUS.QUICKREGISTERSUCCESS)){
            Logger.info("快速注册openid:"+openId+"status:"+status+"name:"+name);
         webChartQuickRegister(user,name, openId,mobile);
        }else if(status.equals(Constants.WEIXINSTATUS.QUICKLOGIN)){
            Logger.info("openid:"+openId+"status:"+status);
            webChartQuickLogin(user, name, openId, error);
        }else if(status.equals("6")){
            Logger.info("showOpenId openid:" + openId + "status:" + status);
            showOpenId(openId);
        }else if(status.equals(Constants.WEIXINSTATUS.SENDPACKET)) {
            if(mobile==null){
                mobile=  Http.Request.current().cookies.get("redPacketId").value;
                Logger.info("redpacketId:"+Http.Request.current().cookies.get("redPacketId").value);
            }
            Logger.info("showOpenId openid:" + openId + "status:" + status+"mobile"+mobile);
            JSONObject josn = sendPacketPost(openId, mobile);
            Logger.info("发送完毕"+josn.toString());
            processSendResult(josn);
            //如果发红包此处mobile指的是活动id

        } else{
            //TODO
            weChatLogin(user, name, openId, error);

        }
    }

    private static void processSendResult(JSONObject josn) {
        Logger.info("发送完毕跳转" + josn.toString());
        String code=josn.get("code").toString();
        Logger.info(code);
        if(code.equals("1")){
            return;
        }
       renderTemplate("mobile/WeChatAction/sendSuccess.html",code);
    }

    private static JSONObject sendPacketPost(String openId,String redPacketId) throws Exception {
             JSONObject josn= new JSONObject();
             Logger.info("进入红包方法：" + openId);
             RedPacket redPacket=new RedPacket();
             redPacket.setId(Long.parseLong(redPacketId));
             RedPacket redEntity = redPacket.queryRedPacket(redPacket.getId(), openId);
            if(redEntity.getBalance()==null ||redEntity.getBalance().intValue() < 100){
                Logger.info("红余额不足：" + openId);
                josn.put("code", 0);
                josn.put("msg", "余额不足");
                return josn;
            }
             Logger.info("创建billNo：" + openId);
             String billNum = RedPacketParam.createBillNo(openId.substring(0, 6));
             RedPacketBill redPacketBill =new RedPacketBill();
             try {
                 Logger.info("红包未发放可以发：" + openId);
                 RedPacketBill redPacketBillResult = RedPacketParam.getAmount(openId, billNum, redPacket);
                 SortedMap<String, String> map = RedPacketParam.createMap(billNum, redPacket, openId, redPacketBillResult.getAmount());
                 RedPacketParam.sign(map);
                 FileInputStream certInstream = WebChartUtil.getCertInstream();
                 String requestXML = RedPacketParam.getRequestXml(map);
                 Logger.info("开始发红包：" + requestXML);
                 josn = RedPacketParam.post(requestXML, certInstream, openId, redPacketId);
                 return josn;
             }catch(Exception e){
                Logger.error(e.getMessage());
                 josn.put("code", 0);
                 josn.put("msg", e.getMessage());
                 return josn;
             }
         }


    private static void webChartQuickLogin(User user, String name,String openId, ErrorInfo error) {
        Logger.info("weChatLogin:openid" + openId);
        if (name == null) {
            Logger.info("weChatLogin:name为空");
            renderTemplate("mobile/QuickRegister/quickLogin.html", openId);
        }
        user.name = name;
        Logger.info("userId" + user.id);
        if (user.id < 0) {
            error.code = -1;
            error.msg = "该用户名不存在";
            renderTemplate("mobile/QuickRegister/quickLogin.html", openId);
        }
        Logger.info("userId" + user.id + "登录");
        user.loginCommon(error);
        if (error.code < 0) {
            Logger.info("userId"+user.id+"登录错误");
            renderTemplate("mobile/QuickRegister/quickLogin.html", openId);
        }
        Logger.info("返回产品列表");
        MainContent.moneyMatters();
    }

    private static void showOpenId(String openId) {
        renderJSON(openId);
    }

    private static String getOpenIdAndSessionToken(String code) throws IOException {
        JSONObject auth = WebChartUtil.getOpenIdAuth(code);
        if(auth!=null) {
            Logger.info("cookie 不存在access_token");
            String openId = auth.get("openid").toString();
//            String  access_token=auth.get("refresh_token").toString();
//            Http.Response.current().setCookie("refresh_token",access_token);
//            Http.Response.current().setCookie("expireDate", String.valueOf(new Date().getTime()));
            return openId;
        }
        return null;
    }

    private static void weChatRegister(User user, String name, String openId, ErrorInfo error) {
        JSONObject jsonOne = new JSONObject();
        jsonOne.put("openId",openId);
        jsonOne.put("name",name);
        renderTemplate("mobile/LoginAction/register.html", jsonOne);
    }

    private static void  webChartQuickRegister(User user, String name, String openId,String mobile){
        String fpHots= Constants.FP_HOST;
        JSONObject jsonOne = new JSONObject();
        jsonOne.put("mobile", mobile);
        jsonOne.put("openId", openId);
        jsonOne.put("name", name);
        renderTemplate("mobile/QuickRegister/quickRegister.html", jsonOne, fpHots);
    }

    private static void weChatLogin(User user, String name,String openId, ErrorInfo error){
               Logger.info("weChatLogin:openid" + openId);
        if (name == null) {
            Logger.info("weChatLogin:name为空");
            renderTemplate("mobile/LoginAction/login.html", openId);
        }
        user.name = name;
        Logger.info("userId" + user.id);
        if (user.id < 0) {
            error.code = -1;
            error.msg = "该用户名不存在";
            renderTemplate("mobile/LoginAction/register.html", openId);
        }
        Logger.info("userId" + user.id + "登录");
        user.loginCommon(error);
        if (error.code < 0) {
            Logger.info("userId"+user.id+"登录错误");
            renderTemplate("mobile/LoginAction/login.html", openId);
        }
        Logger.info("返回产品列表");
        MainContent.moneyMatters();
    }
    public static void landding(){
        render();
    }

}
