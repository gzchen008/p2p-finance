package controllers.front.account;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import constants.Constants;
import constants.Templets;
import controllers.AddCheck;
import controllers.BaseController;
import controllers.Check;
import controllers.DSecurity;
import controllers.interceptor.FInterceptor;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import models.t_dict_ad_citys;
import models.t_dict_ad_provinces;
import models.t_dict_cars;
import models.t_dict_educations;
import models.t_dict_houses;
import models.t_dict_maritals;
import models.t_user_vip_records;
import models.t_users;
import business.BackstageSet;
import business.News;
import business.SecretQuestion;
import business.TemplateEmail;
import business.User;
import business.Vip;
import play.Logger;
import play.cache.Cache;
import play.mvc.With;
import utils.DateUtil;
import utils.EmailUtil;
import utils.ErrorInfo;
import utils.RegexUtils;
import utils.Security;

@With({FInterceptor.class,DSecurity.class})
public class BasicInformation extends BaseController {

	//-------------------------------基本资料-------------------------
	
	/**
	 * 基本信息
	 */
	public static void basicInformation(){
		User user = User.currUser();
		user.id = User.currUser().id;
		
		ErrorInfo error = new ErrorInfo();
		
		if(error.code < 0) {
			render(user, Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		List<t_dict_cars> cars = (List<t_dict_cars>) Cache.get("cars");
		List<t_dict_ad_provinces> provinces = (List<t_dict_ad_provinces>) Cache.get("provinces");
		List<t_dict_educations> educations = (List<t_dict_educations>) Cache.get("educations");
		List<t_dict_houses> houses = (List<t_dict_houses>) Cache.get("houses");
		List<t_dict_maritals> maritals = (List<t_dict_maritals>) Cache.get("maritals");
		
		List<t_dict_ad_citys> cityList = null;
		if(flash.get("province") != null) {
			cityList = User.queryCity(Integer.parseInt(flash.get("province")));
		}else {
			cityList = User.queryCity(user.provinceId);
		}
		
		List<t_user_vip_records> vipRecords = Vip.queryVipRecord(user.id, error);
		
		if(error.code < 0) {
			render(user, Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String content = News.queryContent(Constants.NewsTypeId.VIP_AGREEMENT, error);
		boolean ipsEnable = constants.Constants.IPS_ENABLE;
		
 		render(user,cars,provinces,educations,houses,maritals,cityList,vipRecords,backstageSet,content,ipsEnable);
	}
	
	/**
	 * 根据省获得市联动
	 */
	public static void getCity(long provinceId){
		List<t_dict_ad_citys> cityList = User.queryCity(provinceId);
		JSONArray json = JSONArray.fromObject(cityList);
//		json.put("cityList", cityList);
		renderJSON(json);
	}
	
	/**
	 * 保存基本信息
	 */
	public static void saveInformation(String realityName, int sex, int age, int city, int province,
			String idNumber, int education, int marital, int car, int house, String mobile1,
			String mobile2, String code1,String code2, String email2){
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		flash.put("realityName", realityName);
		flash.put("sex", sex);
 		flash.put("age", age);
		flash.put("city", city);
		flash.put("province", province);
		flash.put("idNumber", idNumber);
		flash.put("education", education);
		flash.put("marital", marital);
		flash.put("car", car);
		flash.put("house", house);
		flash.put("mobile1", mobile1);
		flash.put("mobile2", mobile2);
		//flash.put("email", email);
		
		/*if(Constants.CHECK_CODE) {
			
			if(StringUtils.isBlank(mobile1)) {
				flash.error("请输入手机号码");

				basicInformation();
			}
			
			Object cCode1 = Cache.get(mobile1);
			
			Object cCode2 = "";
			
			if(StringUtils.isNotBlank(mobile2)) {
				cCode2 = Cache.get(mobile2);
			}
			
			
			if(cCode1 == null) {
				flash.error("验证码已失效，请重新点击发送验证码");
				
				basicInformation();
			}
			

			if(!cCode1.toString().equals(code1)) {
				flash.error("手机验证错误");
				
				basicInformation();
			}
			
			if(cCode2 == null || !cCode2.toString().equals(code2)) {
				mobile2 = null;
			}
			
			if (null != mobile2) {
				if(mobile1.equals(mobile2)){
					flash.error("两个手机号码不能一样");
					
					basicInformation();
				}
			}
		}*/
		
		User newUser = new User();
		newUser.id = user.id;
		
		if(null == newUser.realityName) newUser.realityName = realityName;
		newUser.setSex(sex);
		newUser.age = age;
		newUser.cityId = city;
		newUser.educationId = education;
		newUser.maritalId = marital;
		newUser.carId = car;
		if(null == newUser.idNumber) newUser.idNumber = idNumber;
		newUser.houseId = house;
		newUser.mobile1 = mobile1;
		newUser.mobile2 = mobile2;
		newUser.edit(newUser,error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			
			basicInformation();
		}
		flash.success(error.msg);
		
		basicInformation();
	}
	
	/**
	 * 弹出重置安保问题的页面
	 */
	public static void setSafeQuestionModify(){
		
		render();
	}
	
	/**
	 * vip详情
	 */
	public static void vipDetail(){
		BackstageSet options = BackstageSet.getCurrentBackstageSet();
		
		JSONObject json = new JSONObject();
		json.put("test", options);

		renderJSON(options);
	}
	
	/**
	 * 设置安全问题
	 */
	public static void setSafeQuestion(){
		User user = User.currUser();
		Logger.info("设置安全问题："+user.isSecretSet);
		List<SecretQuestion> questions = SecretQuestion.queryUserQuestion();
		render(user,questions);
	}
	
	/**
	 * 校验安全问题
	 */
	@AddCheck(Constants.IS_AJAX)
	public static void verifySafeQuestion(String questionName1, String questionName2, 
			String questionName3) {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		user.verifySafeQuestion(questionName1, questionName2, questionName3, error);
		
		JSONObject json = new JSONObject();
		 
		json.put("encryString", flash.get("encryString"));
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 保存安全问题
	 */
	@Check(Constants.VERIFY_SAFE_QUESTION)
	public static void saveSafeQuestion(String encryString, long secretQuestion1, 
			long secretQuestion2, long secretQuestion3, String answer1, 
			String answer2, String answer3){
		if( secretQuestion1 == 0 || 
			secretQuestion2 == 0 || 
			secretQuestion3 == 0 ||
			StringUtils.isBlank(answer1) ||
			StringUtils.isBlank(answer2) ||
			StringUtils.isBlank(answer3) ||
			secretQuestion1 == secretQuestion2 ||
			secretQuestion1 == secretQuestion3 ||
			secretQuestion2 == secretQuestion3 ||
			answer1.length() > 50 ||
			answer2.length() > 50 ||
			answer3.length() > 50 
		
		){
			flash.error("答案不能为空，且长度需在1~50之间!");
			
			setSafeQuestion();
		}
		
		User user = new User();
		user.id = User.currUser().id;
		ErrorInfo error = new ErrorInfo();
		
		user.secretQuestionId1 = secretQuestion1;
		user.secretQuestionId2 = secretQuestion2;
		user.secretQuestionId3 = secretQuestion3;
		user.answer1 = answer1;
		user.answer2 = answer2;
		user.answer3 = answer3;
		
		user.updateSecretQuestion(true, error);
		
		flash.error(error.msg);
		
		String fromPage = params.get("fromPage");
		
		if (StringUtils.isBlank(fromPage)) {
			setSafeQuestion();
		}
		
		if (fromPage.equals("modifyEmail")) {
			modifyEmail();
		}
		
		if (fromPage.equals("modifyPassword")) {
			modifyPassword();
		}
		
		if (fromPage.equals("modifyMobile")) {
			modifyMobile();
		}
		
		setSafeQuestion();
	}
	
	//得到安全问题
	public static void getSafeQuestion(){
		render();
	}
	
	/**
	 * 通过邮箱重置安全问题
	 */
	public static void resetSafeQuestion(){
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		TemplateEmail tEmail = new TemplateEmail();
		tEmail.id = 4;

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String sign = Security.addSign(user.id, Constants.SECRET_QUESTION);
		String url = Constants.RESET_QUESTION_EMAIL + sign;

		String content = tEmail.content;

		content = content.replace("<p","<div");
		content = content.replace("</p>","</div>");
		content = content.replace(Constants.EMAIL_NAME, user.name);
		content = content.replace(Constants.EMAIL_LOGIN, "<a href = "+Constants.LOGIN+">登录</a>");
		content = content.replace(Constants.EMAIL_TELEPHONE, backstageSet.companyTelephone);
		content = content.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
		content = content.replace(Constants.EMAIL_URL, "<a href = "+url+">"+url+"</a>");
		content = content.replace(Constants.EMAIL_TIME, DateUtil.dateToString(new Date()));

		System.out.println(content);
		
		TemplateEmail.sendEmail(0, user.email, tEmail.title, content, error);

		if (error.code < 0) {
			flash.error(error.msg);
			resetSafeQuestion();
		}
		
//		EmailUtil.emailResetSecretQuestion(user.name, user.email, error);
//		String emailUrl = EmailUtil.emailUrl(user.email);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		json.put("emailUrl", EmailUtil.emailUrl(user.email));
		
		renderJSON(json);
	}
	
	/**
	 * 重置安全问题页面
	 */
	public static void resetQuestion(String sign){
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(sign, Constants.SECRET_QUESTION, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			LoginAndRegisterAction.login();
		}
		
		String name = User.queryUserNameById(id, error);
		
		List<SecretQuestion> questions = SecretQuestion.queryUserQuestion();
		
		render(loginOrRegister, name, sign, questions);
	}
		
	/**
	 * 通过邮件重置后，保存安全问题答案
	 */
	public static void saveSafeQuestionByEmail(String sign, long secretQuestion1, 
			long secretQuestion2, long secretQuestion3, String answer1, 
			String answer2, String answer3){
		ErrorInfo error = new ErrorInfo();
		
		if( secretQuestion1 == 0 || 
			secretQuestion2 == 0 || 
			secretQuestion3 == 0 ||
			StringUtils.isBlank(answer1) ||
			StringUtils.isBlank(answer2) ||
			StringUtils.isBlank(answer3) ||
			secretQuestion1 == secretQuestion2 ||
			secretQuestion1 == secretQuestion3 ||
			secretQuestion2 == secretQuestion3 ||
			answer1.length() > 50 ||
			answer2.length() > 50 ||
			answer3.length() > 50  
			){
			error.code = -1;
			error.msg = "请填写正确的问题和答案!";
			
			flash.error(error.msg);
			
			AccountHome.home();
		}
		
		long id = Security.checkSign(sign, Constants.SECRET_QUESTION, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			LoginAndRegisterAction.login();
		}
		
		User user = new User();
		user.id = id;
		
		user.secretQuestionId1 = secretQuestion1;
		user.secretQuestionId2 = secretQuestion2;
		user.secretQuestionId3 = secretQuestion3;
		user.answer1 = answer1;
		user.answer2 = answer2;
		user.answer3 = answer3;
		
		user.updateSecretQuestion(false, error);
		
		flash.error(error.msg);
		
		AccountHome.home();
	}
	
	/**
	 * 激活邮箱
	 */
	public static void activeEmail() {
		ErrorInfo error = new ErrorInfo();
		JSONArray json = new JSONArray();
		
		User user = User.currUser();
		
		if(user.isEmailVerified) {
			error.code = -1;
			error.msg = "你的邮箱已激活，无需再次激活";
		}
		
		TemplateEmail.activeEmail(user, error);
		
		json.add(error);
		json.add(EmailUtil.emailUrl(user.email));
		
		renderJSON(json);
	}
	
	/**
	 * 保存邮箱
	 */
	public static void saveEmail(String email){
		ErrorInfo error = new ErrorInfo();
		User user = new User();
		user.id = User.currUser().id;
		
		user.email = email;
		
		JSONObject json = new JSONObject();
		
		if(user.editEmail(error) < 0) {
			json.put("error", error);
			renderJSON(json);
		}
		
		TemplateEmail.activeEmail(user, error);
		String emailUrl = EmailUtil.emailUrl(email);
		
		json.put("error", error);
		json.put("emailUrl", emailUrl);
		
		renderJSON(json);
	}
	
	/**
	 * 修改邮箱
	 */
	public static void modifyEmail() {
		User user = User.currUser();
		
		if(!user.isSecretSet) {
			flash.error("您还没有设置安全问题，为了保障您的安全，请先设置安全问题");
			flash.put("fromPage", "modifyEmail");
			setSafeQuestion();
		}
		
		render(user);
	}
	
	/**
	 * 绑定邮箱（回答安全问题）
	 */
	public static void bindEmail(String answer1, String answer2, String answer3) {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		user.verifySafeQuestion(answer1, answer2, answer3, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			modifyEmail();
		}
 		
		render(user);
	}
	
	/**
	 * 修改密码
	 */
	public static void modifyPassword(){
		User user = User.currUser();
		
		if(!user.isSecretSet) {
			flash.error("您还没有设置安全问题，为了保障您的安全，请先设置安全问题");
			flash.put("fromPage", "modifyPassword");
			setSafeQuestion();
		}
		
		render(user);
	}
	
	/**
	 * 保存密码
	 * @param oldPassword
	 * @param newPassword1
	 * @param newPassword2
	 */
	@Check({Constants.VERIFY_SAFE_QUESTION,Constants.IS_AJAX})
	public static void savePassword(String oldPassword, String newPassword1, 
			String newPassword2, String encryString){
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		if(oldPassword.equalsIgnoreCase(newPassword1)){
			JSONObject json = new JSONObject();
			json.put("error", "新密码与原密码一样，请重新输入");
			renderJSON(json);
		}
		
		user.editPassword(oldPassword, newPassword1, newPassword2, error);
        //invoke fp reset password function that create the record for table "c_authentication" and "c_customer"
        String severity = null;
        try {
            HttpClient httpClient = new HttpClient();
            PostMethod postMethod = new PostMethod(Constants.FP_RESETPW_URL);
            postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=gbk");// 在头文件中设置转码

            NameValuePair[] data = {
                     new NameValuePair("mobilePhoneNo", user.name),
                     new NameValuePair("passWord", newPassword1),
                     new NameValuePair("channel", "1")};
            postMethod.setRequestBody(data);
            int statusCode = httpClient.executeMethod(postMethod);

            String result = postMethod.getResponseBodyAsString();
            JSONObject jsonResult = JSONObject.fromObject(result);
            Object message = jsonResult.get("message");
            if(message!= null && message instanceof JSONObject){
                severity = ((JSONObject)message).getString("severity");
                if(!severity.equals("0")){
                    error.code = -1;
                    error.msg = ((JSONObject)message).getString("summary");
                    JSONObject json = new JSONObject();
                    json.put("error", error);
                    renderJSON(json);
                }
            }
            Logger.info("statusCode:"+statusCode+", result:"+result+"");
            postMethod.releaseConnection();
        } catch (HttpException e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put("error", error);
            renderJSON(json);
        } catch (IOException e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put("error", error);
            renderJSON(json);
        }
        JSONObject json = new JSONObject();
        json.put("error", error);
        renderJSON(json);
    }
    
    /**
     * 保存支付密码
     * @param oldPassword
     * @param newPassword1
     * @param newPassword2
     */
    @Check(Constants.VERIFY_SAFE_QUESTION)
    public static void editPayPassword(String oldPayPassword, String newPayPassword1, 
            String newPayPassword2, String encryString){
        
        if(oldPayPassword.equalsIgnoreCase(newPayPassword1)){
            JSONObject json = new JSONObject();
            json.put("error", "新密码与原密码一样，请重新输入");
            renderJSON(json);
        }
        
        User user = new User();
        user.id = User.currUser().id;
        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();
        
        user.editPayPassword(oldPayPassword, newPayPassword1, newPayPassword2, error);
        
        json.put("error", error);
        renderJSON(json);
    }
    
    /**
     * 添加支付密码
     * @param oldPassword
     * @param newPassword1
     * @param newPassword2
     */
    @Check(Constants.VERIFY_SAFE_QUESTION)
    public static void savePayPassword(String newPayPassword1, String newPayPassword2, 
            String encryString){
        User user = new User();
        user.id = User.currUser().id;
        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();
        
        user.addPayPassword(true, newPayPassword1, newPayPassword2, error);
        json.put("error", error);
        
        renderJSON(json);
    }
    
    /**
     * 重置支付密码
     * @param code
     * @param newPayPassword1
     * @param newPayPassword2
     */
    @Check(Constants.VERIFY_SAFE_QUESTION)
    public static void resetPayPassword(String code, String newPayPassword1, 
            String newPayPassword2, String encryString) {
        User user = new User();
        user.id = User.currUser().id;
        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();
        
        if(Constants.CHECK_CODE) {
            String cCode = String.valueOf(Cache.get(user.mobile));
            
            if(cCode == null) {
                error.code = -1;
                error.msg = "验证码已失效，请重新点击发送验证码";
                
                return;
            }
            
            if(!code.equals(cCode)) {
                error.code = -1;
                error.msg = "手机验证错误";
                
                return;
            }
        }
        
        user.addPayPassword(false, newPayPassword1, newPayPassword2, error);
        
        json.put("error", error);
        
        renderJSON(json);
    }
    
    /**
     * 修改手机
     */
    public static void modifyMobile(){
        User user = User.currUser();
		
		if(!user.isSecretSet) {
			flash.error("您还没有设置安全问题，为了保障您的安全，请先设置安全问题");
			flash.put("fromPage", "modifyMobile");
			setSafeQuestion();
		}
		
		render(user);
	}
	
	/**
	 * 保存手机号码
	 * @param code
	 * @param mobile
	 */
	public static void saveMobile(String code, String mobile) {
		ErrorInfo error = new ErrorInfo();
		User user = new User();
		user.id = User.currUser().id;
		
		user.mobile = mobile;
		user.editMobile(code, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	} 
	
	/**
	 * 绑定手机
	 */
	public static void bindMobile(String answer1, String answer2, String answer3) {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		user.verifySafeQuestion(answer1, answer2, answer3, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			modifyMobile();
		}
 		
		render(user);
	}
	
	/**
	 * 保存重置密码
	 */
	public static void savePayPasswordByEmail(String sign, String password, String confirmPassword) {
		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(sign, Constants.PASSWORD, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			LoginAndRegisterAction.login();
		}
		
		User user = new User();
		user.id = id;
		user.updatePayPasswordByEmail(password, confirmPassword, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			resetDelPassword(sign);
		}
		
		flash.error(error.msg);
		
		modifyPassword();
	}
	
	public static void resetDelPassword(String sign){
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(sign, Constants.PASSWORD, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			modifyPassword();
		}
		
		String name = User.queryUserNameById(id, error);
		
		render(loginOrRegister, name,sign);
	}

	/**
	 * 发送重置交易密码邮件
	 */
	public static void resetPayPasswordByEmail(String email) {
		ErrorInfo error = new ErrorInfo();

		flash.put("email", email);


		if (StringUtils.isBlank(email)) {
			flash.error("请输入邮箱地址");
			modifyPassword();
		}

		if (!RegexUtils.isEmail(email)) {
			flash.error("请输入正确的邮箱地址");
			modifyPassword();
		}

		

		User.isEmailExist(email, error);

		if (error.code != -2) {
			flash.error("对不起，该邮箱没有注册");
			modifyPassword();
		}
		
		t_users user = User.queryUserByEmail(email, error);

		if (error.code < 0) {
			flash.error(error.msg);
			modifyPassword();
		}
		
		TemplateEmail tEmail = new TemplateEmail();
		tEmail.id = Templets.E_FIND_DELPWD_EMAL;

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String sign = Security.addSign(user.id, Constants.PASSWORD);
		String url = Constants.RESET_PAY_PASSWORD_EMAIL + sign;

		String content = tEmail.content;

		content = content.replace("<p","<div");
		content = content.replace("</p>","</div>");
		content = content.replace(Constants.EMAIL_NAME, user.name);
		content = content.replace(Constants.EMAIL_LOGIN, "<a href = "+Constants.LOGIN+">登录</a>");
		content = content.replace(Constants.EMAIL_TELEPHONE, backstageSet.companyTelephone);
		content = content.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
		content = content.replace(Constants.EMAIL_URL, "<a href = "+url+">"+url+"</a>");
		content = content.replace(Constants.EMAIL_TIME, DateUtil.dateToString(new Date()));

		TemplateEmail.sendEmail(0, email, tEmail.title, content, error);

		if (error.code < 0) {
			flash.error(error.msg);
			modifyPassword();
		}
		

		flash.put("email", "");
		flash.put("code", "");
		flash.error("邮件发送成功");
		flash.put("emailUrl", EmailUtil.emailUrl(email));
		modifyPassword();
	}
	
}
