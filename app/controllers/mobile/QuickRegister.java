package controllers.mobile;

import business.User;
import constants.Constants;
import controllers.BaseController;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.mvc.Http;
import utils.ErrorInfo;
import utils.ParseClientUtil;
import utils.WebChartUtil;


/**
 * Created by libaozhong on 2015/5/27.
 */
public class QuickRegister extends BaseController {
    public static void quickRegister(){
       String mobile= params.get("mobile");
        String fpHots= Constants.FP_HOST;
            if (ParseClientUtil.isWeiXin()) {
                String url = WebChartUtil.buildWeChatGateUrl("3", mobile);
                Logger.info("url：" + url);
                redirect(url);
           }
        render(fpHots);
    }

    public static void qrredirect(){

        String fpHots= Constants.FP_HOST;
        render(fpHots);


    }
    public static void quickLogin(){
        String mobile= params.get("mobile");
            if (ParseClientUtil.isWeiXin()) {
                String url = WebChartUtil.buildWeChatGateUrl("4", mobile);
                Logger.info("url：" + url);
                redirect(url);
            }
        render();
        }


    public static void doQuickLogin() {
        Logger.info("doQuickLogin");
        ErrorInfo error = new ErrorInfo();

        String name = params.get("name");
        String password = params.get("password");
        String openId = params.get("openId");
        flash.put("name", name);
        flash.put("password", password);
        flash.put("openId", openId);
        Logger.info("name"+name+"openId"+openId);
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
            Logger.info(error.msg);
            validate = false;
        }

        if (user.loginFromH5(password, error) < 0) {
            flash.error(error.msg);
            Logger.info(error.msg);
            validate = false;
        }

        if (validate) {
            if (StringUtils.isNotEmpty(openId)) {//bindweixin
                String bindingName = user.findBySocialToFp(WebChartUtil.WECHAT, openId, error);
                if (StringUtils.isEmpty(bindingName)) {//未绑定过才去绑定
                    user.bindingSocialToFp(WebChartUtil.WECHAT, openId, error);
                    if (error.code < 0) {
                        flash.error(error.msg);
                        quickLogin();
                    }else{
                        Logger.info("doRegister  openId放到cookie 中");
                        play.mvc.Http.Cookie cookie = new play.mvc.Http.Cookie();
                        cookie.value = openId;
                        Http.Request.current().cookies.put("openId", cookie);
                    }
                }

            }
            MainContent.moneyMatters();
        }
        Logger.info(error.msg);
       renderTemplate("mobile/QuickRegister/quickLogin.html",flash);
    }

    public static void webChartBind(String mobile,String openId, ErrorInfo error){
        if(StringUtils.isNotEmpty(openId)){//bindweixin
            User user=new User();
            user.name=mobile;
            user.bindingSocialToFp(WebChartUtil.WECHAT, openId, error);
        }

        JSONObject json = new JSONObject();
        json.put("error", error);
        renderTemplate("/mobile/registerSuccess");
    }

    public static void registerSuccess(String ...openid){
        Http.Request reuqets = Http.Request.current();
        String fpHots= Constants.FP_HOST;
        render(fpHots);
    }
    public static void privacy(){
    render();
    }
}
