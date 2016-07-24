package controllers.front.account;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;
import play.mvc.Before;
import business.BackstageSet;
import business.TemplateEmail;
import business.User;
import utils.EmailUtil;
import utils.ErrorInfo;
import utils.RegexUtils;
import utils.SMSUtil;
import constants.Constants;
import constants.IPSConstants.IpsCheckStatus;
import controllers.interceptor.FInterceptor;

public class CheckAction extends FInterceptor {
	@Before(only = {"front.account.FundsManage.recharge",
			"front.account.InvestAccount.auditmaticInvest",
			"front.account.FundsManage.withdrawal"
			})
	public static void checkIpsAcct(){
		User user = User.currUser();
		
		if(null == user) {
			LoginAndRegisterAction.login();
		}
		
		if(Constants.IPS_ENABLE && (user.getIpsStatus() != IpsCheckStatus.IPS)){
			//CheckAction.approve();
			renderTemplate("/front/account/CheckAction/approve.html");
		}
	}
	
	/**
	 * ips认证
	 */
	public static void approve() {
		render();
	}
	
	/**
	 * ips认证(弹框)
	 */
	public static void check() {
		int status = User.currUser().getIpsStatus();
		BackstageSet set = BackstageSet.getCurrentBackstageSet(); 
        String phone = set.companyTelephone; // 电话号码
        String qq1 = set.companyQQ1; // QQ1
        String qq2 = set.companyQQ2; // QQ2
		switch (status) {
		case IpsCheckStatus.EMAILISNULL:
			//finishEmail();
			if (User.currUser().getIpsStatus() != IpsCheckStatus.EMAILISNULL) {
				check();
			}
	        renderTemplate("/front/account/CheckAction/finishEmail.html",phone, qq1, qq2);
			break;
		case IpsCheckStatus.NONE:
			//checkEmail();
			if (User.currUser().getIpsStatus() != IpsCheckStatus.NONE) {
				check();
			}
			ErrorInfo error = new ErrorInfo();
			User user = User.currUser();
			TemplateEmail.activeEmail(user, error);
			String email = user.email;
			String emailUrl = EmailUtil.emailUrl(email);
			
			renderTemplate("/front/account/CheckAction/checkEmail.html",email, emailUrl, phone, qq1, qq2);
			break;
		case IpsCheckStatus.EMAIL:
			//checkEmailSuccess();
			if (User.currUser().getIpsStatus() != IpsCheckStatus.EMAIL) {
				check();
			}
			
			renderTemplate("/front/account/CheckAction/checkEmailSuccess.html",phone, qq1, qq2);
			break;
		case IpsCheckStatus.REAL_NAME:
			//checkMobile();
			if (User.currUser().getIpsStatus() != IpsCheckStatus.REAL_NAME) {
				check();
			}
			
			String companyName = set.companyName; // 公司名称
			
			renderTemplate("/front/account/CheckAction/checkMobile.html",companyName, phone, qq1, qq2);
			break;
		case IpsCheckStatus.MOBILE:
			//createIpsAcct();
			if (User.currUser().getIpsStatus() != IpsCheckStatus.MOBILE) {
				check();
			}
						
			renderTemplate("/front/account/CheckAction/createIpsAcct.html",phone, qq1, qq2);
			break;
		case IpsCheckStatus.IPS:
			//checkSuccess();
			if (User.currUser().getIpsStatus() != IpsCheckStatus.IPS) {
				check();
			}
			
			renderTemplate("/front/account/CheckAction/checkSuccess.html");
			break;
		default:
			break;
		}
	}
	
	/**
	 * 邮箱认证
	 */
	public static void checkEmail() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.NONE) {
			check();
		}
		
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		TemplateEmail.activeEmail(user, error);
		String email = user.email;
		String emailUrl = EmailUtil.emailUrl(email);
		
		BackstageSet set = BackstageSet.getCurrentBackstageSet(); 
		String phone = set.platformTelephone; // 电话号码
		String qq1 = set.companyQQ1; // QQ1
		String qq2 = set.companyQQ2; // QQ2
		
		render(email, emailUrl, phone, qq1, qq2);
	}
	
	/**
	 * 发送激活邮件
	 */
	public  void sendActiveEmail() {
		ErrorInfo error = new ErrorInfo();
		
		if (User.currUser().getIpsStatus() != IpsCheckStatus.NONE) {
			error.code = -1;
			error.msg = "非法请求";
			
			renderJSON(error);
		}
		
		User user = User.currUser();
		TemplateEmail.activeEmail(user, error);
		
		if (error.code >= 0) {
			error.msg = "激活邮件发送成功！";
		}
		
		renderJSON(error);
	}
	
	/**
	 * 邮箱认证成功
	 */
	public static void checkEmailSuccess() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.EMAIL) {
			check();
		}
		
		BackstageSet set = BackstageSet.getCurrentBackstageSet(); 
		String phone = set.platformTelephone; // 电话号码
		String qq1 = set.companyQQ1; // QQ1
		String qq2 = set.companyQQ2; // QQ2
		
		render(phone, qq1, qq2);
	}
	
	/**
	 * 完善邮箱页面
	 */
	public static void finishEmail() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.EMAILISNULL) {
			check();
		}
		
		BackstageSet set = BackstageSet.getCurrentBackstageSet(); 
        String phone = set.companyTelephone; // 电话号码
        String qq1 = set.companyQQ1; // QQ1
        String qq2 = set.companyQQ2; // QQ2
		
		render(phone, qq1, qq2);
	}
	/**
	 * 实名认证页面
	 */
	public static void checkRealName() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.EMAIL) {
			check();
		}
		
		BackstageSet set = BackstageSet.getCurrentBackstageSet(); 
        String phone = set.companyTelephone; // 电话号码
        String qq1 = set.companyQQ1; // QQ1
        String qq2 = set.companyQQ2; // QQ2
		
		render(phone, qq1, qq2);
	}
	/**
	 * 完善邮箱
	 */
	public static void doFinishEmail(String email){

		User user = User.currUser();
		if (user.getIpsStatus() != IpsCheckStatus.EMAILISNULL) {
			check();
		}
		
		flash.put("email", email);
		user.email = email;
		
		if (StringUtils.isBlank(email)) {
			flash.error("邮箱不能为空");
			
			finishEmail();
		}
		if (!RegexUtils.isEmail(email)) {
			flash.error("请填写正确的邮箱地址");
			finishEmail();
		}
		ErrorInfo error = new ErrorInfo();
		User.isEmailExist(email, error);
		
        String sql = "update t_users set email = ? where id = ?";
		
		int rows = 0;
		
		try {
			rows = JpaHelper.execute(sql, email, user.id).executeUpdate();
		} catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("更新用户邮箱时："+e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次更新邮箱失败！";
		}
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
		}
		if (error.code < 0) {
			flash.error(error.msg);
			
			finishEmail();
		}
		checkEmail();
	}
	/**
	 * 实名认证
	 */
	public static void doCheckRealName(String realName, String idNumber) {
		User user = User.currUser();
		if (user.getIpsStatus() != IpsCheckStatus.EMAIL) {
			check();
		}
		
		flash.put("realName", realName);
		flash.put("idNumber", idNumber);
		
		if (StringUtils.isBlank(realName)) {
			flash.error("真实姓名不能为空");
			
			checkRealName();
		}
		
		if (StringUtils.isBlank(idNumber)) {
			flash.error("身份证不能为空");
			
			checkRealName();
		}
		
		ErrorInfo error = new ErrorInfo();
		user.checkRealName(realName, idNumber, error);
		
		if (error.code < 0) {
			flash.error(error.msg);
			
			checkRealName();
		}
		
		checkMobile();
	}
	
	/**
	 * 手机认证页面
	 */
	public static void checkMobile() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.REAL_NAME) {
			check();
		}
		
		BackstageSet set = BackstageSet.getCurrentBackstageSet(); 
		String companyName = set.companyName; // 公司名称
		String phone = set.platformTelephone; // 电话号码
		String qq1 = set.companyQQ1; // QQ1
		String qq2 = set.companyQQ2; // QQ2
		
		render(companyName, phone, qq1, qq2);
	}
	
	/**
	 * 发送短信验证码
	 * @param mobile
	 */
	public static void sendCode(String mobile) {
		ErrorInfo error = new ErrorInfo();
		flash.put("mobile", mobile);
		
		if(StringUtils.isBlank(mobile) ) {
			flash.error("手机号码不能为空");
		}
		
		if(!RegexUtils.isMobileNum(mobile)) {
			flash.error("请输入正确的手机号码");
		}
		
		SMSUtil.sendCode(mobile, error);
		
		if (error.code < 0) {
			flash.error(error.msg);
		}
		
		flash.put("isSending", true);
		
		checkMobile();
	}
	
	/**
	 * 手机认证
	 * @param mobile
	 * @param code
	 */
	public static void doCheckMobile(String mobile, String code) {
		User user = User.currUser();
		if (user.getIpsStatus() != IpsCheckStatus.REAL_NAME) {
			check();
		}
		
		flash.put("mobile", mobile);
		flash.put("code", code);
		
		if (StringUtils.isBlank(mobile)) {
			flash.error("手机号不能为空");
			
			checkMobile();
		}
		
		if (StringUtils.isBlank(code)) {
			flash.error("验证码不能为空");
			
			checkMobile();
		}
		
		ErrorInfo error = new ErrorInfo();
		user.checkMoible(mobile, code, error);
		
		if (error.code < 0) {
			flash.error(error.msg);
			
			checkMobile();
		}
		
		createIpsAcct();
	}
	
	/**
	 * 资金托管开户页面
	 */
	public static void createIpsAcct() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.MOBILE) {
			check();
		}
		
		BackstageSet set = BackstageSet.getCurrentBackstageSet(); 
		String phone = set.platformTelephone; // 电话号码
		String qq1 = set.companyQQ1; // QQ1
		String qq2 = set.companyQQ2; // QQ2
		
		render(phone, qq1, qq2);
	}
	
	/**
	 * 认证成功
	 */
	public static void checkSuccess() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.IPS) {
			check();
		}
		
		render();
	}
}
