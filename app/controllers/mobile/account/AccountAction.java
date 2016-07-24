package controllers.mobile.account;

import business.Payment;
import controllers.interceptor.H5Interceptor;
import models.t_users;
import play.Logger;
import play.mvc.With;
import utils.ErrorInfo;
import business.User;
import controllers.BaseController;
import controllers.front.account.PaymentAction;
import controllers.mobile.LoginAction;
import controllers.mobile.MainContent;

/**
 * <p>Project: com.shovesoft.sp2p</p>
 * <p>Title: AccountAction.java</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2014 Sunlights.cc</p>
 * <p>All Rights Reserved.</p>
 *
 * @author <a href="mailto:jiaming.wang@sunlights.cc">wangJiaMing</a>
 */

@With(H5Interceptor.class)
public class AccountAction extends BaseController {
    public static void createAcctCB() {
        Logger.info("开户回调信息 start >>：");
        User user = User.currUser();
        Logger.info("user:" + user);
        String pErrCode = params.get("pErrCode");
        if (pErrCode != null && !"MG00000F".equals(pErrCode)) {
            String pErrMsg = params.get("pErrMsg");
            Logger.info("开户失败：" + pErrMsg);
            flash.error(pErrMsg);
            createAcct();
        }
        Logger.info("开户回调信息 end >>：");

        render();
    }

    /**
     * 开户回调（异步）
     */
    public static void createAcctCBSys() {
        ErrorInfo error = new ErrorInfo();

        Logger.info("-----------开户回调（异步）:----------");
        Payment pay = new Payment();
        pay.pMerCode = params.get("pMerCode");
        pay.pErrCode = params.get("pErrCode");
        pay.pErrMsg = params.get("pErrMsg");
        pay.p3DesXmlPara = params.get("p3DesXmlPara");
        pay.pSign =  params.get("pSign");
        pay.createAcctCB(error);

        renderText("{\"code\":\""+error.code+"\"}");
    }

    public static void createAcct() {
        User user = User.currUser();
        if (user == null || user.id < 0) {
            LoginAction.login();
        }

        if (flash.get("realName") == null && user.realityName != null) {
            flash("realName", user.realityName);
        }
        if (flash.get("cardId") == null && user.idNumber != null) {
            flash("cardId", user.idNumber);
        }
        if (flash.get("email") == null && user.email != null) {
            flash("email", user.email);
        }

        render();
    }

    public static void saveUser(){
        User user = User.currUser();
        Logger.info("user:" + user);
        if (user == null || user.id < 0) {
            LoginAction.login();
        }

        Logger.info("current user id:" + user.id);

        String realName = params.get("realName");
        String idNo = params.get("cardId");
        String email = params.get("email");

        flash.put("realName", realName);
        flash.put("cardId", idNo);
        flash.put("email", email);

        ErrorInfo error = new ErrorInfo();
        User newUser = new User();
        newUser.id = user.id;
        newUser.idNumber = idNo;
        newUser.realityName = realName;
        newUser.email = email;
        newUser.isEmailVerified = true;
        newUser.isAddBaseInfo = true;

        newUser.appEditUser(user,error);
        if(error.code != 0){
            flash.error(error.msg);
            createAcct();//异常返回此页面
        }

        t_users t_users = user.queryUser2ByUserId(user.getId(), error);
        if (t_users.ips_acct_no != null) {//已开户
            MainContent.property();
        }
        PaymentAction.createAcct();

    }


}
