/*
 * Project Name:xiaofengP2P
 * File Name:FPLoginAndRegisterAction.java
 * Package Name:controllers.front.account
 * Date:2015年3月10日上午9:07:14
 * Copyright (c) 2015, YiYue Company All Rights Reserved.
*/
package controllers.front.account;

import java.util.Date;
import java.util.List;
import java.util.Map;

import models.t_users;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.cache.Cache;
import play.libs.Images;
import play.mvc.With;
import utils.CaptchaUtil;
import utils.DateUtil;
import utils.EmailUtil;
import utils.ErrorInfo;
import utils.MobileUtil;
import utils.RegexUtils;
import utils.SMSUtil;
import utils.Security;
import business.BackstageSet;
import business.BottomLinks;
import business.News;
import business.TemplateEmail;
import business.TemplateSms;
import business.User;

import constants.Constants;
import constants.Templets;
import constants.IPSConstants.IpsCheckStatus;
import controllers.BaseController;
import controllers.DSecurity;

/**  
 * ClassName:   FPLoginAndRegisterAction
 * Description: this class just for FP register and login.
 *              the logic is the same as LoginAndRegisterAction 
 * Date:        2015年3月10日 上午9:07:14
 * @author      john woo  
 * @version     1.0
 * @since       JDK 1.6
 */
@With(DSecurity.class)
public class FPLoginAndRegisterAction  extends BaseController {


    /**
     * 跳转到登录页面
     */
    public static void login() {
        String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
        
         render(loginOrRegister);
    }

    public static void logout() {
        User user = User.currUser();

        if (user == null) {
            LoginAndRegisterAction.login();
        }

        ErrorInfo error = new ErrorInfo();

        user.logout(error);

        if (error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }

        login();
    }

    /**
     * 包含登录页面的登录
     */
    public static void logining() {
        
        business.BackstageSet  currBackstageSet = business.BackstageSet.getCurrentBackstageSet();
        Map<String,java.util.List<business.BottomLinks>> bottomLinks = business.BottomLinks.currentBottomlinks();
           
        if(null != currBackstageSet){
          Cache.delete("backstageSet");//清除系统设置缓存
        }
           
        if(null != bottomLinks){
          Cache.delete("bottomlinks");//清除底部连接缓存
        }
       
        ErrorInfo error = new ErrorInfo();

        String name = params.get("name");
        String url = request.headers.get("referer").value();
        String password = params.get("password");
        String code = params.get("code");
        String randomID = params.get("randomID");
        flash.put("name", name);
        flash.put("password", password);
        flash.put("code", code);

        if (StringUtils.isBlank(name)) {
            flash.error("请输入用户名");
            redirect(url);
        }

        if (StringUtils.isBlank(password)) {
            flash.error("请输入密码");
            redirect(url);
        }

        if (StringUtils.isBlank(code)) {
            flash.error("请输入验证码");
            redirect(url);

        }

        if (StringUtils.isBlank(randomID)) {
            flash.error("请刷新验证码");
            redirect(url);
        }

        if(!code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) {
            flash.error("验证码错误");
            redirect(url);
        }
        
        User user = new User();
        user.name = name;

        if (user.id < 0) {
            flash.error("该用户名不存在");
            redirect(url);
        }
        
        

        if (user.login(password,false, error) < 0) {
            flash.error(error.msg);
            redirect(url);
        }
        
        if (Constants.LOGIN.equalsIgnoreCase(url)) {
            if(Constants.IPS_ENABLE && (user.getIpsStatus() != IpsCheckStatus.IPS)){
                CheckAction.approve();
            }
            
            AccountHome.home();
        }

        redirect(url);
    }

    /**
     * 登录页面登录logining
     */
    public static void topLogin() {
        ErrorInfo error = new ErrorInfo();

        String userName = params.get("name");
        String password = params.get("password");
        String code = params.get("code");
        String randomID = params.get("randomID");

        flash.put("name", userName);
        flash.put("password", password);
        flash.put("code", code);

        if (StringUtils.isBlank(userName)) {
            flash.error("请输入用户名");
            login();
        }

        if (StringUtils.isBlank(password)) {
            flash.error("请输入密码");
            login();
        }

        if (StringUtils.isBlank(code)) {
            flash.error("请输入验证码");
            login();

        }

        if (StringUtils.isBlank(randomID)) {
            flash.error("请刷新验证码");
            login();
        }

        if (!code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) {
            flash.error("验证码错误");
            login();
        }

        User user = new User();
        user.name = userName;

        if (user.login(password,false, error) < 0) {
            flash.error(error.msg);
            login();
        }

        AccountHome.home();
    }

    /**
     * 跳转到注册页面
     */
    public static void register() {
        String loginOrRegister = Constants.LOGIN_AREAL_FLAG;

        ErrorInfo error = new ErrorInfo();
        String content = News.queryContent(Constants.NewsTypeId.REGISTER_AGREEMENT2, error);

        render(loginOrRegister, content);
    }

    /**
     * 获取验证码并返回页面
     */
    public static void codeReturn(String codeImg) {
        String randomID = (String) Cache.get(codeImg);

        JSONObject json = new JSONObject();
        json.put("randomID", randomID);

        renderJSON(json);
    }
    
    /**
     * 提供接口供FP验证注册
     * 
     * 
     */
    public static void fpRegistering(){
        String severity = "0";
        String code = "0100";
        String summary = "注册成功";
        String detail = "注册成功";
        
        response.setHeader("Access-Control-Allow-Origin","*");
        String data = params.get("body");
        
        ErrorInfo error = new ErrorInfo();
        if(data== null || data.equals("")){
            severity = "2";
            code = "0101";
            summary = "注册失败";
            detail = "FP传送的注册信息为空，无法注册";
        } else {
            JSONObject jsonResult = JSONObject.fromObject(data);
            String name = jsonResult.getString("mobilePhoneNo");
            String authentication_id = jsonResult.getString("authenticationId");
            String password = jsonResult.getString("password");
            User user = new User();

            user.time = new Date();
            user.name = name;
            user.password = password;
            user.mobile = name;
            user.authentication_id = authentication_id;
            
            user.registerForFP(error, severity, code, summary, detail);
        }

        JSONObject returnResult = new JSONObject();
        returnResult.put("severity", severity);
        returnResult.put("code", code);
        returnResult.put("summary", summary);
        returnResult.put("detail", detail);
        Logger.info("移动端注册到PC的注册结果" + returnResult);

        renderJSON(returnResult);
    }

    /**
     * 生成验证码图片
     * 
     * @param id
     */
    public static void getImg(String id) {
        Images.Captcha captcha = CaptchaUtil.CaptchaImage(id);

        renderBinary(captcha);
    }

    /**
     * 刷新验证码
     */
    public static void setCode() {
        String randomID = CaptchaUtil.setCaptcha();

        JSONObject json = new JSONObject();
        json.put("img", randomID);
        renderJSON(json.toString());
    }

    /**
     * 校验验证码
     */
    public static void checkCode(String randomId, String code) {

        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();

        if (StringUtils.isBlank(code)) {
            error.code = -1;
            error.msg = "请输入验证码";

            json.put("error", error);
            renderJSON(json);
        }

        if (StringUtils.isBlank(randomId)) {
            error.code = -1;
            error.msg = "请刷新验证码";

            json.put("error", error);
            renderJSON(json);
        }

        String radomCode = CaptchaUtil.getCode(randomId);

        if (!code.equalsIgnoreCase(radomCode)) {
            error.code = -1;
            error.msg = "验证码错误";

            json.put("error", error);
            renderJSON(json);
        }

        json.put("error", error);
        renderJSON(json);
    }

    /**
     * 验证邮箱是否已存在
     * 
     * @param email
     */
    public static void hasEmailExist(String email) {
        ErrorInfo error = new ErrorInfo();
        
        email = email.toLowerCase();
        User.isEmailExist(email, error);

        JSONObject json = new JSONObject();
        json.put("error", error);
        renderJSON(json.toString());

    }

    /**
     * 验证用户名是否已存在
     * 
     * @param name
     */
    public static void hasNameExist(String name) {
        ErrorInfo error = new ErrorInfo();

        User.isNameExist(name, error);

        JSONObject json = new JSONObject();
        json.put("error", error);

        renderJSON(json.toString());
    }

    /**
     * 验证手机号码是否已存在
     * 
     * @param name
     */
    public static void hasMobileExist(String telephone) {
        ErrorInfo error = new ErrorInfo();

        int nameIsExist = User.isMobileExist(telephone, error);

        JSONObject json = new JSONObject();
        json.put("result", nameIsExist);

        renderJSON(json.toString());
    }

    /**
     * 底部链接信息查询
     * 
     * @param name
     */
    public static void buttomLinks(String num) {
        /* 初始化底部链接 */
        List<BottomLinks> result = BottomLinks.queryFrontBottomLinks(num);
        JSONObject json = new JSONObject();
        json.put("result", result);
        renderJSON(json.toString());
    }

//    /**
//     * 查询平台设置信息
//     */
//    public static void systemInfo() {
//        ErrorInfo error = new ErrorInfo();
//
//        BackstageSet info = new BackstageSet();
//        List<BackstageSet> result = info.querySystemInfo(error);
//        JSONObject json = new JSONObject();
//        json.put("result", result);
//        renderJSON(json.toString());
//
//    }

    /**
     * 注册跳转到成功页面
     */
    public static void registerSuccess() {
        User user = User.currUser();
        if (user == null) {
            login();
        }
        
        if (Constants.IPS_ENABLE) {
            CheckAction.approve();
        }

        if (user.isEmailVerified) {
            AccountHome.home();
        }

        String loginOrRegister = Constants.LOGIN_AREAL_FLAG;

        render(loginOrRegister);
    }

    /**
     * 激活帐号
     * 
     * @param sign
     */
    public static void accountActivation(String sign) {
        ErrorInfo error = new ErrorInfo();

        long id = Security.checkSign(sign, Constants.ACTIVE, Constants.VALID_TIME, error);

        if (error.code < 0) {
            flash.error(error.msg);
            login();
        }

        User user = new User();
        user.id = id;

        user.activeEmail(error);
        
        if (Constants.IPS_ENABLE) {
            if (error.code < 0) {
                flash.error(error.msg);

                login();
            }
            
            User.setCurrUser(user);
            CheckAction.approve();
        }

        user.logout(error);
        flash.error(error.msg);

        login();
    }

    /**
     * 通过邮箱找回用户名
     */
    public static void findBackUsernameByEmail() {
        String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
        render(loginOrRegister);
    }

    /**
     * 通过邮箱找回用户名后跳转到成功页面
     */
    public static void emailSuccess() {
        String loginOrRegister = Constants.LOGIN_AREAL_FLAG;

        String email = flash.get("email");
        if (email == null) {

            login();
        }

        String emailUrl = EmailUtil.emailUrl(email);
        render(loginOrRegister, emailUrl);
    }

    /**
     * 发送找回用户名邮件
     */
    public static void saveUsernameByEmail(String email, String code,
            String randomID) {
        ErrorInfo error = new ErrorInfo();

        flash.put("email", email);

        if (StringUtils.isBlank(code)) {
            flash.error("请输入验证码");
            findBackUsernameByEmail();
        }

        if (StringUtils.isBlank(email)) {
            flash.error("请输入邮箱地址");
            findBackUsernameByEmail();
        }

        if (!RegexUtils.isEmail(email)) {
            flash.error("请输入正确的邮箱地址");
            findBackUsernameByEmail();
        }

        if (!code.equalsIgnoreCase(Cache.get(randomID).toString())) {
            flash.error("验证码错误");
            findBackUsernameByEmail();
        }

        User.isEmailExist(email, error);

        if (error.code != -2) {
            flash.error("对不起，该邮箱没有注册");
            findBackUsernameByEmail();
        }

        t_users user = User.queryUserByEmail(email, error);

        if (error.code < 0) {
            flash.error(error.msg);
            findBackUsernameByEmail();
        }

        TemplateEmail tEmail = new TemplateEmail();
        tEmail.id = 1;

        BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();

        String content = new String(tEmail.content);
        
        String url = Constants.LOGIN ;
        
        content = content.replace("<p","<div");
        content = content.replace("</p>","</div>");
        content = content.replace(Constants.EMAIL_NAME, user.name);
        content = content.replace(Constants.EMAIL_EMAIL, email);
        content = content.replace(Constants.EMAIL_TELEPHONE, backstageSet.companyTelephone);
        content = content.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
        content = content.replace(Constants.EMAIL_URL, "<a href = "+url+">"+url+"</a>");
        content = content.replace(Constants.EMAIL_TIME, DateUtil.dateToString(new Date()));

        TemplateEmail.sendEmail(0, email, tEmail.title, content, error);

        if (error.code < 0) {
            flash.error(error.msg);
            findBackUsernameByEmail();
        }
        
        flash.error("邮件发送成功");
        flash.put("emailUrl", EmailUtil.emailUrl(email));

        login();
    }

    /**
     * 通过手机找回用户名
     */
    public static void findBackUsernameByTele() {
        String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
        render(loginOrRegister);
    }

    /**
     * 通过手机找回用户名后跳转到成功页面
     */
    public static void teleSuccess() {
        String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
        render(loginOrRegister);
    }

    /**
     * 发送找回用户的短信
     */
    public static void saveUsernameByTele(String mobile, String code,
            String randomID) {
        ErrorInfo error = new ErrorInfo();

        flash.put("mobile", mobile);
        flash.put("code", code);

        if (StringUtils.isBlank(mobile)) {
            flash.error("请输入手机号码");
            findBackUsernameByTele();
        }

        if (StringUtils.isBlank(code)) {
            flash.error("请输入验证码");
            findBackUsernameByTele();
        }

        if(Cache.get(randomID) == null) {
            flash.error("请刷新验证码");
            findBackUsernameByTele();
        }

        if (StringUtils.isBlank(randomID)) {
            flash.error("请刷新验证码");
            findBackUsernameByTele();
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            flash.error("请输入正确的手机号码");
            findBackUsernameByTele();
        }

        if (!code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) {
            flash.error("验证码错误");
            findBackUsernameByTele();
        }

        User.isMobileExist(mobile, error);

        if (error.code != -2) {
            flash.error("该手机号码不存在或未绑定");
            findBackUsernameByTele();
        }

        MobileUtil.mobileFindUserName(mobile, error);

        if (error.code < 0) {
            flash.error(error.msg);
            findBackUsernameByTele();
        }
        
        BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
        TemplateSms sms = new TemplateSms();
        sms.id = Templets.S_FIND_USERNAME;
        
        t_users user = User.queryUserByMobile(mobile, error);
        
        String content = sms.content;
        
        content = content.replace(Constants.EMAIL_NAME, user.name);
        content = content.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);

        SMSUtil.sendSMS(mobile, content, error);

        if (error.code < 0) {
            flash.error(error.msg);
            findBackUsernameByTele();
        }

        flash.put("code", "");
        flash.error(error.msg);

        login();
    }

    /**
     * 通过手机重置密码
     */
    public static void resetPasswordByMobile() {
        String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
        render(loginOrRegister);
    }

    /**
     * 保存重设的密码
     */
    public static void savePasswordByMobile(String mobile, String code,
            String password, String confirmPassword) {
        ErrorInfo error = new ErrorInfo();
        
        User.updatePasswordByMobile(mobile, code, password, confirmPassword,
                error);

        if (error.code < 0) {
            flash.put("mobile", mobile);
            flash.put("code", code);
            flash.put("password", password);
            flash.put("confirmPassword", confirmPassword);

            flash.error(error.msg);
        }

        flash.error(error.msg);

        login();
    }

    /**
     * 通过邮箱重置密码
     */
    public static void resetPasswordByEmail() {
        String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
        render(loginOrRegister);
    }

    /**
     * 发送重置密码邮件
     */
    public static void sendResetEmail(String email, String code, String randomID) {
        ErrorInfo error = new ErrorInfo();

        flash.put("email", email);

        if (StringUtils.isBlank(code)) {
            flash.error("请输入验证码");
            resetPasswordByEmail();
        }

        if (StringUtils.isBlank(email)) {
            flash.error("请输入邮箱地址");
            resetPasswordByEmail();
        }

        if (!RegexUtils.isEmail(email)) {
            flash.error("请输入正确的邮箱地址");
            resetPasswordByEmail();
        }

        if (!code.equalsIgnoreCase(Cache.get(randomID).toString())) {
            flash.error("验证码错误");
            resetPasswordByEmail();
        }

        User.isEmailExist(email, error);

        if (error.code != -2) {
            flash.error("对不起，该邮箱没有注册");
            resetPasswordByEmail();
        }
        
        t_users user = User.queryUserByEmail(email, error);

        if (error.code < 0) {
            flash.error(error.msg);
            findBackUsernameByEmail();
        }
        
        TemplateEmail tEmail = new TemplateEmail();
        tEmail.id = 3;

        BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
        String sign = Security.addSign(user.id, Constants.PASSWORD);
        String url = Constants.RESET_PASSWORD_EMAIL + sign;

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
            findBackUsernameByEmail();
        }
        
//        EmailUtil.emailFindUserName(email, error);
//
//        if (error.code < 0) {
//            flash.error("邮件发送失败，请重新发送");
//            resetPasswordByEmail();
//        }

        flash.put("email", "");
        flash.put("code", "");
        flash.error("邮件发送成功");
        flash.put("emailUrl", EmailUtil.emailUrl(email));

        login();
    }
    
    /**
     * 跳转到重置密码页面
     */
    public static void resetPassword(String sign) {
        String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
        ErrorInfo error = new ErrorInfo();
        long id = Security.checkSign(sign, Constants.PASSWORD, Constants.VALID_TIME, error);
        
        if(error.code < 0) {
            flash.error(error.msg);
            login();
        }
        
        String name = User.queryUserNameById(id, error);
        
        render(loginOrRegister, name,sign);
    }
    
    /**
     * 保存重置密码
     */
    public static void savePasswordByEmail(String sign, String password, String confirmPassword) {
        ErrorInfo error = new ErrorInfo();
        
        long id = Security.checkSign(sign, Constants.PASSWORD, Constants.VALID_TIME, error);
        
        if(error.code < 0) {
            flash.error(error.msg);
            login();
        }
        
        User user = new User();
        user.id = id;
        user.updatePasswordByEmail(password, confirmPassword, error);
        
        if(error.code < 0) {
            flash.error(error.msg);
            resetPassword(sign);
        }
        
        flash.error(error.msg);
        
        login();
    }
    
    /**
     * 发送手机校验码
     * @param code
     */
    public static void verifyMobile(String mobile) {
        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();

        if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请输入手机号码";

            json.put("error", error);

            renderJSON(json);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请输入正确的手机号码";

            json.put("error", error);

            renderJSON(json);
        }

        User user = User.currUser();

        if (user == null || StringUtils.isBlank(user.mobile) || !user.mobile.equals(mobile)) {
            User.isMobileExist(mobile, error);

            if (error.code < 0) {

                json.put("error", error);

                renderJSON(json);
            }
        }

        SMSUtil.sendCode(mobile, error);

        json.put("error", error);

        renderJSON(json);
    }

}
