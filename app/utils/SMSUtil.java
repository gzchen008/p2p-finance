package utils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Random;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import constants.Constants;

import play.Logger;

public class SMSUtil {

	/**
	 * 发送短信
	 * @param mobile
	 * @param content
	 * @param error
	 */
	public static void sendSMS(String mobile,String content, ErrorInfo error) {
		if(StringUtils.isBlank(content)) {
			error.code = -1;
			error.msg = "请输入短信内容";
			
			return;
		}
		String contentAll = "尊敬的用户(先生/女士)，您申请的信息验证码为：" + content +"（2分钟内有效）。请勿泄露您的验证码。谢谢！[P2P网贷平台]";
		// TODO 开发阶段关闭短信验证
		//BackstageSet backstageSet  = BackstageSet.getCurrentBackstageSet();
		/*String balance = EimsSMS.getBalance(backstageSet.smsAccount, backstageSet.smsPassword);
		double balance_long = Convert.strToDouble(balance, 0);
		
		if(balance_long <= 0.0){
			error.code = -2;
			error.msg = "短信平台已欠费,请联系管理员!";
			
			return;
		}
		//invoke YiYue's SMS channel "2office"
		try {
			SmsSendByHttp(mobile, contentAll, error);
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//EimsSMS.send(backstageSet.smsAccount, backstageSet.smsPassword, content, mobile);
		*/
		error.msg = "短信发送成功";
	}
	
	/**
	 * 发送校验码
	 * @param mobile
	 * @param error
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public static void sendCode(String mobile, ErrorInfo error) {
		Logger.info(SMSUtil.class.getName() + "sendCode():" + "sendCode start");
		error.clear();
		
		//BackstageSet backstageSet  = BackstageSet.getCurrentBackstageSet();
		/*String balance = EimsSMS.getBalance(backstageSet.smsAccount, backstageSet.smsPassword);
		double balance_long = Convert.strToDouble(balance, 0);
		if(balance_long <= 0.0){
			error.code = -2;
			error.msg = "短信平台已欠费,请联系管理员!";
			
			return;
		}*/
        int randomCode;
        try{
           String randomCode1=play.cache.Cache.get(mobile).toString();
           randomCode=Integer.valueOf(randomCode1);
        }catch (Exception e){
            randomCode = (new Random()).nextInt(8999) + 1000;// 最大值位9999
        }
		
		String content = "尊敬的用户"+ mobile + "(先生/女士)，您申请的信息验证码为：" + randomCode +"（10分钟内有效）。请勿泄露您的验证码。谢谢！【金豆荚】";
		//invoke YiYue's SMS channel "2office"
		// TODO 关闭短信发送
		/*try {
			SmsSendByHttp(mobile, content, error);
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//EimsSMS.send(backstageSet.smsAccount, backstageSet.smsPassword, content, mobile);
		play.cache.Cache.set(mobile, randomCode, "10min");

		error.msg = "短信验证码发送成功";
	}
	
	public static void SmsSendByHttp(String mobile, String content, ErrorInfo error) throws HttpException, IOException{
		Logger.info("--------->>>>send SMS code now!");
		HttpClient httpClient = new HttpClient();
	    PostMethod postMethod = new PostMethod("http://sms.2office.net:8080/WebService/sms3.aspx");
	    if(Constants.CHECK_CODE){
		    httpClient.getHostConfiguration().setProxy(Constants.SMS_PROXY, 3128);//local machine didn't need add this proxy
	    }
        postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=gbk");// 在头文件中设置转码

        NameValuePair[] data = {
                 new NameValuePair("account", "2523040"),
                 new NameValuePair("password", formatPwd()),
                 new NameValuePair("mobile", mobile),
                 new NameValuePair("content", content),
                 new NameValuePair("channel", "252304001"),
                 new NameValuePair("smsid", getSmsId()),
                 new NameValuePair("sendType", "1")};
        postMethod.setRequestBody(data);
        int statusCode = httpClient.executeMethod(postMethod);

        String result = postMethod.getResponseBodyAsString();
        Logger.info("statusCode:"+statusCode+", result:"+result+"");
        postMethod.releaseConnection();

	}
	
	public static void SmsSendByWS(String mobile, String content, ErrorInfo error){
		try{
			String service_url = "http://sms.2office.net:8080/WebService/sms3.aspx";
			Service service = new Service();
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(new java.net.URL(service_url));

			// 设置要调用的方法
			call.setOperationName(new QName("http://tempuri.org", "SendSms3"));
			// 该方法需要的参数
			call.addParameter(new QName("http://tempuri.org","account"), org.apache.axis.encoding.XMLType.XSD_STRING,
					javax.xml.rpc.ParameterMode.IN);
			call.addParameter(new QName("http://tempuri.org","password"), org.apache.axis.encoding.XMLType.XSD_STRING,
					javax.xml.rpc.ParameterMode.IN);
			call.addParameter(new QName("http://tempuri.org","mobile"), org.apache.axis.encoding.XMLType.XSD_STRING,
					javax.xml.rpc.ParameterMode.IN);
			call.addParameter(new QName("http://tempuri.org","content"), org.apache.axis.encoding.XMLType.XSD_STRING,
					javax.xml.rpc.ParameterMode.IN);
			call.addParameter(new QName("http://tempuri.org","channel"), org.apache.axis.encoding.XMLType.XSD_STRING,
					javax.xml.rpc.ParameterMode.IN);
			call.addParameter(new QName("http://tempuri.org","smsid"), org.apache.axis.encoding.XMLType.XSD_STRING,
					javax.xml.rpc.ParameterMode.IN);
			call.addParameter(new QName("http://tempuri.org","sendType"), org.apache.axis.encoding.XMLType.XSD_STRING,
					javax.xml.rpc.ParameterMode.IN);
			
			// 方法的返回值类型
			call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);
			call.setUseSOAPAction(true);
			call.setSOAPActionURI("http://tempuri.org");
			// 调用该方法
			String password = formatPwd();
			String res = call.invoke(new Object[] {"2523040", password, mobile, content, "252304001",getSmsId(),"1"}).toString();

			System.out.println(res.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	//返回加密后的密码
	public static String formatPwd(){
		String password = md5Encrypt("yiyuezc597" + "9a15294089130ec6a8d27502d808a2a1").toLowerCase();
		System.out.println(password);
		//不足32位，前面补0
		if( password.length() < 32 ){
			int length = 32 - password.length();
			for(int i=0; i<length; i++){
				password = "0" + password;
			}
		}
		return password;
	}

	//这是生成smsid的代码，当然也可以按自己业务需要自定义
	private static String getSmsId(){
			Date date = new Date();
			java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS");
			return formatter.format(date);
		}
	
	//这是生成md5的代码
	private static String md5Encrypt(String input) {
	    MessageDigest md = null;
	    try {
	        if (md == null) {
	            md = MessageDigest.getInstance("MD5");
	        }
	        byte buffer[] = input.getBytes();
	        md.update(buffer);
	        byte bDigest[] = md.digest();
	        md.reset();
	        BigInteger bi = new BigInteger(1, bDigest);
	        return bi.toString(16);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}

}

