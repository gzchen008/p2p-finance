package controllers.mobile.account;

import business.Payment;
import business.User;
import constants.Constants;
import constants.IPSConstants;
import controllers.BaseController;
import controllers.interceptor.H5Interceptor;
import controllers.mobile.LoginAction;
import controllers.mobile.MainContent;
import net.sf.json.JSONObject;
import play.Logger;
//import play.libs.Encrypt;
import play.mvc.With;
import utils.Converter;
import utils.ErrorInfo;
import utils.ParseClientUtil;

import java.util.Map;

@With(H5Interceptor.class)
public class EnchashAction extends BaseController {
	public static void enchash(String money){
        User user = User.currUser();
        if (user == null) {
            LoginAction.login();
        }

        JSONObject paramsJson = new JSONObject();

        ErrorInfo error = new ErrorInfo();
        double rechargeAmount = User.queryRechargeIn(user.getId(), error);//限制时间内充值的金额不能提现
        if (error.code < 0) {
            flash.error(error.msg);
            enchash(money);
        }

        double cashMoney = user.balance - rechargeAmount;
        paramsJson.put("balance", cashMoney < 0 ? 0.00 : cashMoney);
        paramsJson.put("money", money);
        paramsJson.put("withdrawal_day" , Constants.WITHDRAWAL_DAY);

        render(paramsJson);
	}

    public static void enchashConfirm(){
        ErrorInfo error = new ErrorInfo();
        double amount = 0;

        if(params.get("money") == null) {
            error.code = -1;
            error.msg = "请输入提现金额";
            flash.error(error.msg);
            enchash(null);
        }
        try {
            amount = Double.valueOf(params.get("money"));
            if(amount > Constants.MAX_VALUE) {
                error.code = -1;
                error.msg = "已超过最大充值金额" +Constants.MAX_VALUE+ "元";
                flash.error(error.msg);
                enchash(amount + "");
            }
        }catch (Exception e){
            e.printStackTrace();
            error.code = -1;
            error.msg = "提现金额格式不正确";
            flash.error(error.msg);
            enchash(amount+"");
        }

        User user = new User();
        user.id = User.currUser().getId();

        long withdrawalId = user.withdrawal(amount, 0, null, 0, true, error);

        if(error.code < 0) {
            flash.error(error.msg);
            enchash(amount+"");
        }

        Map<String, String> args= Payment.doDwTrade(withdrawalId, amount, error, ParseClientUtil.H5);

        if (error.code < 0) {
            flash.error(error.msg);
            enchash(amount+"");
        }

        render("@front.account.PaymentAction.doDwTrade", args);
    }


    public static void enchashCBSys() {
        Logger.info("-----------H5提现回调（异步）:----------");
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = params.get("pMerCode");
        pay.pErrCode = params.get("pErrCode");
        pay.pErrMsg = params.get("pErrMsg");
        pay.p3DesXmlPara = params.get("p3DesXmlPara");
        pay.pSign =  params.get("pSign");
        pay.doDwTradeCB(error);

        JSONObject resultJson = new JSONObject();
        resultJson.put("code", error.code);
        resultJson.put("msg", error.msg);
        resultJson.put("pPostUrl", IPSConstants.IPSH5Url.DO_DW_TRADE);
        resultJson.put("pTrdAmt",  pay.jsonPara.getString("pTrdAmt"));

        Logger.info("----------H5提现回调(ws处理业务逻辑)-------------:"+resultJson.toString());
        Logger.info("----------H5提现回调(ws处理业务逻辑) end-------------");
        renderText(resultJson.toString());
    }

    public static void enchashCB(){
        Logger.info("----------H5提现 start-------------:");
        String result = params.get("result");

        //result = Encrypt.decrypt3DES(result, Constants.ENCRYPTION_KEY);
        System.out.print("undecrypt:" + result);
        ErrorInfo error = new ErrorInfo();

        JSONObject json = (JSONObject) Converter.xmlToObj(result);
        Logger.info("----------H5提现 result-------------:"+result);

        error.code = json.getInt("code");
        error.msg = json.getString("msg");

        if (error.code < 0 && error.code != Constants.ALREADY_RUN) {
            flash.error(error.msg);
            enchash(json.getString("pTrdAmt"));
        }

        MainContent.property();
    }


}
