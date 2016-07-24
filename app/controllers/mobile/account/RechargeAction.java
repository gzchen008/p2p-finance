package controllers.mobile.account;

import business.Payment;
import business.User;
import constants.Constants;
import constants.IPSConstants;
import controllers.BaseController;
import controllers.app.common.MsgCode;
import controllers.interceptor.H5Interceptor;
import controllers.mobile.LoginAction;
import net.sf.json.JSONObject;
import play.Logger;
//import play.libs.Encrypt;
import play.mvc.With;
import utils.Converter;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.ParseClientUtil;

import java.util.Date;
import java.util.Map;

/**
 * <p>Project: com.shovesoft.sp2p</p>
 * <p>Title: RechargeAction.java</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2014 Sunlights.cc</p>
 * <p>All Rights Reserved.</p>
 *
 * @author <a href="mailto:jiaming.wang@sunlights.cc">wangJiaMing</a>
 */

@With(H5Interceptor.class)
public class RechargeAction extends BaseController {

    public static void recharge(String investAmount) {
        User user = User.currUser();
        if (user == null) {
            LoginAction.login();
        }
        JSONObject paramJson = new JSONObject();
        paramJson.put("balance", user.balance);
        paramJson.put("investAmount", investAmount == null ? "0.00" : investAmount);
        render(paramJson);
    }

    public static void rechargeConfirm() {
        ErrorInfo errorInfo = new ErrorInfo();
        String bankCode = params.get("bankCode");//非必填
        String rechargeAmount = params.get("money");
        double money = 0;

        try {
            if (rechargeAmount == null) {//必填
                errorInfo.code = -1;
                errorInfo.msg = MsgCode.RECHARGE_ERROR.getMessage();
                flash.error(errorInfo.msg);
                recharge(rechargeAmount);
            }
            money = Double.valueOf(rechargeAmount);
        } catch (Exception e) {
            e.printStackTrace();
            errorInfo.code = -1;
            errorInfo.msg = MsgCode.RECHARGE_ERROR.getMessage();
            flash.error(errorInfo.msg);
            recharge(rechargeAmount);
        }

        if (money <= 0) {
            errorInfo.code = -1;
            errorInfo.msg = MsgCode.RECHARGE_ERROR.getMessage();
            flash.error(errorInfo.msg);
            recharge(rechargeAmount);
        }

        Map<String, String> args = Payment.doDpTrade(money, bankCode, errorInfo, ParseClientUtil.H5);

        render("@front.account.PaymentAction.doDpTrade", args);
    }


    /**
     * 充值回调（异步）
     */
    public static void rechargeCBSys() {
        Logger.info("-----------充值回调（异步）:----------");
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = params.get("pMerCode");
        pay.pErrCode = params.get("pErrCode");
        pay.pErrMsg = params.get("pErrMsg");
        pay.p3DesXmlPara = params.get("p3DesXmlPara");
        pay.pSign = params.get("pSign");

        pay.print();
        pay.doDpTradeCB(error);

        JSONObject resultJson = new JSONObject();

        resultJson.put("code", error.code);
        resultJson.put("msg", error.msg);
        resultJson.put("pMemo1", pay.jsonPara.getString("pMemo1"));
        resultJson.put("pPostUrl", IPSConstants.IPSH5Url.DO_DP_TRADE);
        resultJson.put("pTrdAmt", pay.jsonPara.getString("pTrdAmt"));
        resultJson.put("pIpsBillNo", pay.jsonPara.getString("pIpsBillNo"));

        Logger.info("----------充值异步(ws处理业务逻辑)-------------:" + resultJson.toString());
        Logger.info("---------充值异步(ws处理业务逻辑) end-------------");
        renderText(resultJson.toString());
    }

    /**
     * 充值回调（ws处理业务逻辑后post返回）
     */
    public static void rechargeCB() {
        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) start-------------:");
        String result = params.get("result");

        // TODO
        //result = Encrypt.decrypt3DES(result, Constants.ENCRYPTION_KEY);
        System.out.print("undecrypt:" + result);
        ErrorInfo error = new ErrorInfo();

        JSONObject json = (JSONObject) Converter.xmlToObj(result);
        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) result-------------:" + result);

        error.code = json.getInt("code");
        error.msg = json.getString("msg");

        if (error.code < 0 && error.code != Constants.ALREADY_RUN) {
            flash.error(error.msg);
            recharge(null);
        }

        rechargeSuccess(json.getString("pTrdAmt"), json.getString("pIpsBillNo"), DateUtil.dateToString3(new Date()));
    }

    public static void rechargeSuccess(String trdAmt, String billNo, String pTrdTime) {
        JSONObject paramsJson = new JSONObject();
        paramsJson.put("pTrdAmt", trdAmt);
        paramsJson.put("pIpsBillNo", billNo);
        paramsJson.put("pTrdTime", pTrdTime);

        render(paramsJson);
    }

}
