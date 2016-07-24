package utils;

import com.sun.org.apache.commons.collections.LRUMap;
import constants.Constants;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.portable.InputStream;
import play.Logger;
import play.Play;
import play.vfs.VirtualFile;

import java.io.*;
import java.net.URLEncoder;


/**
 * Created by libaozhong on 2015/6/4.
 */
public  class WebChartUtil {
    private static LRUMap cache=new LRUMap();
    public static final String WECHAT = "FP.SOCIAL.TYPE.1";
    public static String getOpenIdByToken(String access_token) throws IOException {
        WeChatConstants.REFRESHTOKEN = WeChatConstants.REFRESHTOKEN.replace("APPID", urlEnodeUTF8(Constants.WECHAT_APPID));
        WeChatConstants.REFRESHTOKEN = WeChatConstants.REFRESHTOKEN.replace("REFRESH_TOKEN", urlEnodeUTF8(access_token));
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(WeChatConstants.REFRESHTOKEN);
        getMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");// 在头文件中设置转码
        int statusCode = httpClient.executeMethod(getMethod);
        JSONObject resultStr = JSONObject.fromObject(getMethod.getResponseBodyAsString());
        Logger.info("getOpenIdAuth"+String.valueOf(resultStr==null));
        if (resultStr == null || resultStr.get("openid") == null) {
            Logger.info("getOpenIdAuth:opendid为空");
            return null;
        }
        Logger.info("openid最初值为："+resultStr.get("openid").toString()+"token:"+resultStr.get("access_token"));
        return resultStr.get("openid").toString();
    }

    //01184441bb65fbd817c6996609e6e4dQ
    static class WeChatConstants {
        private static String REFRESHTOKEN = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=APPID&grant_type=refresh_token&refresh_token=REFRESH_TOKEN";
        private static String OPENIDURL =  "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=redUrl&response_type=code&scope=snsapi_base&state=STATUS&mobile=MOBILE#wechat_redirect";
        public static String  GETTOKEN =   "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
        public static String CODEEXCHANGEOPENID="https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
    }
    public  static String buildWeChatGateUrl(String status,String mobile){
        String url=WeChatConstants.OPENIDURL;
        url = url.replace("APPID", urlEnodeUTF8(Constants.WECHAT_APPID));
        url = url.replace("SECRET", urlEnodeUTF8(Constants.WECHAT_APPSECRET));
        url= url.replace("redUrl", urlEnodeUTF8(Constants.WECHAT_CALLBACK_URL));
        if (StringUtils.isNotEmpty(status)) {
            Logger.info("WebChartUtil.buildWeChatGateUrl.status:" + status);
            url = url.replace("STATUS", status);
        }
        if (StringUtils.isNotEmpty(mobile)) {
            url= url.replace("MOBILE", mobile);
        }
        Logger.info("链接为:"+url);
        return url;
    }
    public static FileInputStream getCertInstream() throws IOException {
         Logger.info(Play.applicationPath.getCanonicalPath());
        Logger.info(Play.applicationPath.getAbsolutePath());
        FileInputStream fio=new FileInputStream(Play.applicationPath.getCanonicalPath()+"/conf/apiclient_cert.p12");
        return fio;
    }

    public  static String buildRequestOpenIdUrl(String code){
        String url = WeChatConstants.CODEEXCHANGEOPENID;
        url=url.replace("APPID", urlEnodeUTF8(Constants.WECHAT_APPID));
        url= url.replace("SECRET", urlEnodeUTF8(Constants.WECHAT_APPSECRET));
        if(StringUtils.isNotBlank(code)){
            url =url.replace("CODE", urlEnodeUTF8(code));
        }
        Logger.info( url);
        return url;
    }

    public static JSONObject getOpenIdAuth(String code) throws IOException {
               Logger.info("code开始拼接url" + code);
        String url= buildRequestOpenIdUrl(code);
        Logger.info("code拼接的url"+url);
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(url);
        getMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");// 在头文件中设置转码
        int statusCode = httpClient.executeMethod(getMethod);
        JSONObject resultStr = JSONObject.fromObject(getMethod.getResponseBodyAsString());
         Logger.info("getOpenIdAuth"+resultStr.toString());
        if (resultStr == null || resultStr.get("openid") == null) {
            Logger.info("getOpenIdAuth:opendid为空");
            return null;
        }
     Logger.info("openid最初值为："+resultStr.get("openid").toString()+"token:"+resultStr.get("access_token"));
        return resultStr;
    }


    public static String urlEnodeUTF8(String str) {
        String result = str;
        try {
            result = URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static JSONObject getToken() {
        String result = null;
        WeChatConstants.GETTOKEN  = WeChatConstants.GETTOKEN.replace("APPID", urlEnodeUTF8(Constants.WECHAT_APPID));
        WeChatConstants.GETTOKEN  = WeChatConstants.GETTOKEN.replace("APPSECRET",urlEnodeUTF8(Constants.WECHAT_APPSECRET));
        String severity = null;
        Object message = null;
        try {
          return  getReqMethod(WeChatConstants.GETTOKEN);

        }catch (Exception e){

        }
        return null;
    }
    public static JSONObject getReqMethod(String url) throws IOException {
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(url);
        getMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=gbk");// 在头文件中设置转码

        httpClient.setTimeout(3000);
        int statusCode = httpClient.executeMethod(getMethod);
        JSONObject jsonResult=new JSONObject();
        String resultStr = getMethod.getResponseBodyAsString();

        jsonResult =JSONObject.fromObject(resultStr);;

        return jsonResult;
    }



}
