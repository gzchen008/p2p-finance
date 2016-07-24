package controllers.mobile;

import business.User;
import constants.Constants;
import controllers.BaseController;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.mvc.Http;
import utils.ErrorInfo;
import utils.RegexUtils;
import utils.WebChartUtil;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Project: com.shovesoft.sp2p</p>
 * <p>Title: LoginController.java</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2014 Sunlights.cc</p>
 * <p>All Rights Reserved.</p>
 *
 * @author <a href="mailto:jiaming.wang@sunlights.cc">wangJiaMing</a>
 */

public class LoginAction extends BaseController {

	public static void landingpage() {
		render();
	}

    /*
     * 跳转到登录页面
     */
    public static void login() {

        User user = User.currUser();
        if (user != null) {
            MainContent.property();
        }
        render();
    }
    public static void getOpenId() {
        Map<String,String> map=new HashMap<String,String>();
        map.put("status","6");
        weChatGate(map);
    }
    /**
     * 进入微信统一入口
     * @throws IOException
     *
     * @param map
     */
    private static void weChatGate(Map<String, String> map) {
        Logger.info("进入");
        String status =  map.get("status");
        String mobile =map.get("mobile");
        Logger.info("WeChatAction.weChatGate.status:" + status + "mobile:" + mobile);
        String url = WebChartUtil.buildWeChatGateUrl(status, mobile);
        Logger.info("url：" + url);
        redirect(url);
    }



    public static void doLogin() {
        ErrorInfo error = new ErrorInfo();
        
        String name = params.get("name");
        String password = params.get("password");
        String openId = params.get("openId");
        flash.put("name", name);
        flash.put("password", password);
        flash.put("openId", openId);
        Logger.info("name" + name + "openId" + openId);
        boolean validate = true;
        
        if (StringUtils.isBlank(name)) {
            error.code = -1;
            error.msg = "请输入用户名";
            flash.error(error.msg);
            validate = false;
        }
        if (StringUtils.isBlank(password)) {
        	error.code = -1;
            error.msg = "请输入密码";
            flash.error(error.msg);
            validate = false;
        }

        User user = new User();
        user.name = name;

        if (user.id < 0) {
            error.code = -1;
            error.msg = "该用户名不存在";
            flash.error(error.msg);
            validate = false;
        }

        if (user.loginFromH5(password, error) < 0) {
            flash.error(error.msg);
            validate = false;
        }

        if (validate) {
            String url = flash.get("url");
            if (StringUtils.isNotBlank(url)) {
                redirect(url);
            }else {
                MainContent.moneyMatters();
//            	ProductAction.productList();
            }
        } else {
            flash.keep("url");
            login();
        }

    }

    /**
     * 跳转到注册页面
     */
    public static void register() {
        render();
    }

    public static void doRegister() {
        JSONObject json = new JSONObject();
        ErrorInfo error = new ErrorInfo();
        json.put("error", error);
        String mobile = params.get("name");//the user name is mobile
        String password = params.get("password");
        String verifyCode = params.get("verifyCode");
        String recommendUserName = params.get("recommended");
        String openId = params.get("openId");
        String queryName = params.get("queryName");

        registerValidation(error, mobile, password, verifyCode);

        if (error.code < 0) {
            json.put("error", error);
            renderJSON(json);
        }
        String authentication_id = User.registerToFp(error, mobile, password);

        if (error.code < 0 && error.code != -2) {
            json.put("error", error);
            renderJSON(json);
        }

        User user = new User();
        user.time = new Date();
        user.name = mobile;
        user.password = password;
        user.mobile = mobile;
        user.isMobileVerified = true;
        user.authentication_id = authentication_id;
        user.recommendUserName = recommendUserName;
        user.register(error);

        if (error.code < 0) {
            json.put("error", error);
            renderJSON(json);
        }
        user.registerGiveJinDou(error, mobile);

        Logger.info("queryName" + queryName);
        if (!StringUtils.isNotEmpty(queryName)) {
            if (StringUtils.isNotEmpty(openId)) {//bindweixin
                String bindingName = user.findBySocialToFp(WebChartUtil.WECHAT, openId, error);
                if (StringUtils.isEmpty(bindingName)) {//未绑定过才去绑定
                    user.bindingSocialToFp(WebChartUtil.WECHAT, openId, error);
                    if (error.code < 0) {
                        json.put("error", error);
                        renderJSON(json);
                    }
                    Logger.info("doRegister  openId放到cookie 中");
                    play.mvc.Http.Cookie cookie = new play.mvc.Http.Cookie();
                    cookie.value = openId;
                    Http.Request.current().cookies.put("openId", cookie);
                }
            }
            renderJSON(json);
        }
    }

    private static void registerValidation(ErrorInfo error, String mobile, String password, String verifyCode) {
        if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请填写手机号";
            return;
        }
        if (StringUtils.isBlank(password)) {
            error.code = -1;
            error.msg = "请输入密码";
            return;
        }
        if (StringUtils.isBlank(verifyCode)) {
            error.code = -1;
            error.msg = "请输入验证码";
            return;
        }
        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请填写正确的手机号码";
            return;
        }
        if (!RegexUtils.isValidPassword(password)) {
            error.code = -1;
            error.msg = "请填写符合要求的密码";
            return;
        }

        String cacheVerifyCode = Cache.get(mobile) + "";
        Cache.delete(mobile);
        if (Constants.CHECK_CODE && !verifyCode.equals(cacheVerifyCode)) {
            error.code = -1;
            error.msg = "验证码输入有误";
            return;
        }

        User.isNameExist(mobile, error);
    }


    /**
     * 理财师注册
     */
    public static void cfpRegister() {
        JSONObject json = new JSONObject();
        ErrorInfo error = new ErrorInfo();
        String mobile = params.get("name");//the user name is mobile
        String password = params.get("password");
        String verifyCode = params.get("verifyCode");
        String recommendUserName = params.get("recommended");
        registerValidation(error, mobile, password, verifyCode);

        if (error.code < 0) {
            json.put("error", error);
            renderJSON(json);
        }
        String authentication_id = User.registerToFp(error, mobile, password);
        if (error.code < 0) {
            json.put("error", error);
            renderJSON(json);
        }
        User user = new User();
        user.time = new Date();
        user.name = mobile;
        user.password = password;
        user.mobile = mobile;
        user.isMobileVerified = true;
        user.authentication_id = authentication_id;
        user.recommendUserName = recommendUserName;
        user.cfpflag = true;//是理财师注册
        user.register(error);
    }


}
