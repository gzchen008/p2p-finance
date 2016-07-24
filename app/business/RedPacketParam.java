package business;


import com.google.gson.JsonObject;
import constants.Constants;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import play.Logger;
import utils.MD5Util;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.KeyStore;
import javax.net.ssl.*;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by libaozhong on 2015/6/11.
 */
@SuppressWarnings("deprecation")
public class RedPacketParam   {
    public static final String MCH_ID = Constants.MCH_ID;      //商户号
    public static final String WXAPPID =Constants.WECHAT_APPID;     //公众账号appid
    public static final String NICK_NAME = Constants.REDPACKET_APPLY_NAME;   //提供方名称
    public static final String SEND_NAME = Constants.SEND_NAME;   //商户名称
    public static final int MIN_VALUE = 100;       //红包最小金额 单位:分
    public static final int MAX_VALUE = 200;       //红包最大金额 单位:分
    public static final int TOTAL_NUM = 1;         //红包发放人数
    public static final String SUB_MCH_ID  = "123123";         //zishang
    public static  String CLIENT_IP;   //调用接口的机器IP
    public static final String ACT_NAME = "XX";    //活动名称
    public static final String REMARK = "XX";      //备注
    public static final String KEY =Constants.WECHAT_KEY;         //秘钥
    public static final int FAIL = 0;              //领取失败
    public static final int SUCCESS = 1;           //领取成功
    public static final int LOCK = 2;              //已在余额表中锁定该用户的余额,防止领取的红包金额大于预算

    /**
     * 对请求参数名ASCII码从小到大排序后签名
     *
     * @param params
     */
    public static void sign(SortedMap<String, String> params) {
        Set<Map.Entry<String, String>> entrys = params.entrySet();
        Iterator<Map.Entry<String, String>> it = entrys.iterator();
        StringBuffer result = new StringBuffer();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            if (StringUtils.isNotBlank(entry.getValue())) {
                result.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
        }
        Logger.info("key:"+KEY);
        result.append("key=").append(KEY);
        params.put("sign", MD5Util.getMD5String(result.toString()).toUpperCase());
    }
    public static synchronized  RedPacketBill getAmount(String openid,String billNo,RedPacket redPacket){
        //该用户获取的随机红包金额
        int amount = (int) Math.round(Math.random()*(redPacket.getMaxValue()-redPacket.getMinValue())+redPacket.getMinValue());
        //如果此次随机金额比商户红包余额还要大,则返回商户红包余额
        if(amount > redPacket.getBalance()){
            amount =  redPacket.getBalance();
        }
        RedPacketBill redPacketBill = new RedPacketBill();
        redPacketBill.setAddTime(new Date());
        redPacketBill.setAmount(amount);
        redPacketBill.setOpenid(openid);
        redPacketBill.setResult(RedPacketParam.LOCK);
        redPacketBill.setBillNo(billNo);
        //先锁定用户领取的金额,防止领取金额超过预算金额
      //  service.save(hongbao);
        return redPacketBill;
    }
    /**
     * 生成提交给微信服务器的xml格式参数
     *
     * @param params
     * @return
     */
    public static String getRequestXml(SortedMap<String, String> params) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        Set es = params.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
     if (
             "max_value".equalsIgnoreCase(k)||
                    "min_value ".equalsIgnoreCase(k)||
                    "total_amount".equalsIgnoreCase(k)||
                    "mch_id".equalsIgnoreCase(k)||
                    "total_num".equalsIgnoreCase(k)
//                    "re_openid".equalsIgnoreCase(k)||
//                    "client_ip".equalsIgnoreCase(k)||
//                    "logo_imgurl".equalsIgnoreCase(k)||
//                    "share_content".equalsIgnoreCase(k)||
//                    "share_url".equalsIgnoreCase(k)||
//                    "share_imgurl".equalsIgnoreCase(k)||
//                    "nick_name".equalsIgnoreCase(k) || "
//         send_name".equalsIgnoreCase(k) || "wishing".equalsIgnoreCase(k)
//            || "act_name".equalsIgnoreCase(k) ||
//             "remark".equalsIgnoreCase(k)
//                   || "sign".equalsIgnoreCase(k)
) {
              sb.append("<" + k + ">" + v + "</" + k + ">");
          } else {
            sb.append("<" + k + ">" + "<![CDATA[" + v + "]]></" + k + ">");

         }
        }
        sb.append("</xml>");
        return sb.toString();
    }

    /**
     * 创建map
     *
     * @param billNo
     * @param openid
     * @param amount
     * @return
     */
    public static SortedMap<String, String> createMap(String billNo,RedPacket redPacket, String openid, int amount) throws UnknownHostException {
        CLIENT_IP=getLocalIp();
        SortedMap<String, String> params = new TreeMap<String, String>();
        params.put("wxappid", WXAPPID);
        params.put("nonce_str", createNonceStr());
        params.put("mch_billno", billNo);
        params.put("mch_id", MCH_ID);
        params.put("nick_name", NICK_NAME);
        params.put("send_name", SEND_NAME);
        params.put("re_openid", openid);
        params.put("total_amount",amount+ "");
        params.put("min_value",amount + "");
        params.put("max_value",  amount + "");
        params.put("total_num", 1 + "");
        params.put("wishing", Constants.WISHING);
        params.put("client_ip", CLIENT_IP);
        params.put("act_name", redPacket.getActName());
        params.put("remark", redPacket.getRemark());
//        params.put("logo_imgurl", redPacket.getLogo_imgurl());
//        params.put("share_content ", redPacket.getContent());
//        params.put("share_url", redPacket.getShare_url());
//        params.put("share_imgurl", redPacket.getShare_imgurl());
        Logger.info("发送红包参数"+params.toString());
        return params;
    }

    private static String getLocalIp() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        String ip = addr.getHostAddress();//获得本机IP
        return ip;
    }

    /**
     * 生成随机字符串
     *
     * @return
     */
    public static String createNonceStr() {
        return UUID.randomUUID().toString().toUpperCase().replace("-", "");
    }

    /**
     * 生成商户订单号
     *
     * @param userId 该用户的userID
     * @return
     */
    public static String createBillNo(String userId) {
        //组成： mch_id+yyyymmdd+10位一天内不能重复的数字
        //10位一天内不能重复的数字实现方法如下:
        //因为每个用户绑定了userId,他们的userId不同,加上随机生成的(10-length(userId))可保证这10位数字不一样
        Date dt = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyymmdd");
        String nowTime = df.format(dt);
        int length = 10 - userId.length();
        return MCH_ID + nowTime + userId + getRandomNum(length);
    }

    /**
     * 生成特定位数的随机数字
     *
     * @param length
     * @return
     */
    private static String getRandomNum(int length) {
        String val = "";
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            val += String.valueOf(random.nextInt(10));
        }
        return val;
    }
    private static void trustAllHttpsCertificates() throws Exception {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
                .getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
                .getSocketFactory());
    }

    static class miTM implements javax.net.ssl.TrustManager,
            javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }
       private static RedPacketBill parseXml(String xmlStr, String openId, Long redpacketId) throws DocumentException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
           RedPacketBill rpb = null;
           if (xmlStr.length() <= 0 || xmlStr == null){
               return null;
             }
     Document document = DocumentHelper.parseText(xmlStr);
    // 获得文档的根节点
    Element root = document.getRootElement();
           rpb = new RedPacketBill();
    // 遍历根节点下所有子节点
           rpb.setAddTime(new Date());
           String status=root.element("return_code").getText();
           rpb.setReturnMsg(root.element("return_msg").getText());
           rpb.setOpenid(openId);
           rpb.setRedPackId(redpacketId);
           rpb.setResult(0);
           rpb.setRemark(xmlStr);
           if(!status.equals("FAIL")){
               rpb.setBillNo(root.element("mch_billno").getText());
               rpb.setResult(1);
               rpb.setAmount(Integer.parseInt(root.element("total_amount").getText()));
               rpb.setReturnMsg("发放成功");
           };

          return rpb;
       }
    /**
     * post提交到微信服务器
     *
     * @param requestXML
     * @returnMCH_ID
     */
    public static JSONObject post(String requestXML, FileInputStream inputStream, String openId, String redpacketId) throws Exception {
        JSONObject json=new JSONObject();
        Long redPack=Long.parseLong(redpacketId);
        Logger.info("执行发送红包开始");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try {
            keyStore.load(inputStream, MCH_ID.toCharArray());
        } finally {
            inputStream.close();
        }

        String result = null;

        KeyManager[] managers;
        SSLContext context = SSLContext.getInstance("TLS");
        KeyManagerFactory keyManagerFactory =
                KeyManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore,MCH_ID.toCharArray());
        managers = keyManagerFactory.getKeyManagers();
        context.init(managers, null, null);
        URL url = new URL("https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack");
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        SSLSocketFactory socketFactory = context.getSocketFactory();
        con.setSSLSocketFactory(socketFactory);
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
         out.write(requestXML.getBytes());
        out.flush();
        out.close();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      int code = con.getResponseCode();
      if (HttpsURLConnection.HTTP_OK == code){
      String temp = in.readLine();
       /*连接成一个字符串*/
     while (temp != null) {
        if (result != null)
         result += temp;
          else
       result = temp;
    temp = in.readLine();

  }
              Logger.info(result);
              RedPacketBill resultBill= parseXml(result,openId,redPack);
          if(resultBill!=null) {
              json.put("code", resultBill.getResult());
              json.put("msg", resultBill.getReturnMsg());
              if(resultBill.getResult()==1){
                  resultBill.saveRedPacketBill(resultBill);
              }
          }else{
              json.put("code", 0);
              json.put("msg", "发送失败");
          }
     }
        return json;

    }
}
