package controllers.mobile.account;

import business.Invest;
import business.IpsDetail;
import business.Payment;
import business.User;
import com.google.gson.Gson;
import com.shove.Convert;
import com.shove.security.Encrypt;
import constants.Constants;
import constants.IPSConstants;
import controllers.BaseController;
import controllers.app.common.Message;
import controllers.app.common.MessageVo;
import controllers.app.common.MsgCode;
import controllers.app.common.Severity;
import controllers.interceptor.H5Interceptor;
import controllers.mobile.LoginAction;
import controllers.mobile.MainContent;
import controllers.mobile.ProductAction;
import models.t_bids;
import net.sf.json.JSONObject;
import play.Logger;
import play.cache.Cache;
import play.mvc.With;
import utils.Converter;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.ParseClientUtil;

import java.util.Date;
import java.util.Map;

/**
 * <p>Project: com.shovesoft.sp2p</p>
 * <p>Title: InvestAction.java</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2014 Sunlights.cc</p>
 * <p>All Rights Reserved.</p>
 *
 * @author <a href="mailto:jiaming.wang@sunlights.cc">wangJiaMing</a>
 */

@With(H5Interceptor.class)
public class InvestAction extends BaseController {

    public static void allInvest(){
        ErrorInfo error = new ErrorInfo();

        double showInvestMoney = 0.00;
        User user = (User)Cache.get("userId_"+ Cache.get(params.get("userId")));

        if (user != null && params.get("bidId") != null) {
            Long bidId = Long.valueOf(params.get("bidId"));
            Map<String, String> bid = Invest.bidMap(bidId, error);
            if (error.code >= 0) {
                double amount = Double.parseDouble(bid.get("amount") + "");
                double has_invested_amount = Double.parseDouble(bid.get("has_invested_amount") + "");
                double balance = user.balance;
                if (balance > amount - has_invested_amount) {//可以余额>剩余可投金额=剩余可投金额
                    showInvestMoney = amount - has_invested_amount;
                }else{
                    showInvestMoney = balance;
                }
            }
        }

        MessageVo messageVo = new MessageVo(new Message(Severity.INFO, MsgCode.ALL_INVEST_SUCC));
        messageVo.setValue(showInvestMoney);

        renderJSON(JSONObject.fromObject(messageVo));
    }

    public static void confirmInvest(){
        User user = User.currUser();
        if (user == null) {
            LoginAction.login();
        }

        if(User.currUser().ipsAcctNo == null){//未开户
            AccountAction.createAcct();
        }

        if (params.get("bidId") == null) {
            MainContent.moneyMatters();
        }

        ErrorInfo error = new ErrorInfo();
        Map<String, String> args = controllers.front.invest.InvestAction.buildConfirmInvestParams(error, ParseClientUtil.H5);
        if (error.code < 0) {
            if (error.code == -999) {//余额不够跳转到充值页面
                RechargeAction.recharge(params.get("investAmount"));
            }else{
                Logger.info(">>确认投标失败：" + error.msg);
                flash.error(error.msg);
                ProductAction.productBid(params.get("bidId"));
            }
        }

        Logger.info(">>确认投标成功");
        renderTemplate("front/account/PaymentAction/registerCreditor.html", args);
    }


    /**
     * 登记债权人回调（异步）
     */
    public static void registerCreditorCBSys() {
        Logger.info("----------- 登记债权人回调（异步）:----------");
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = params.get("pMerCode");
        pay.pErrCode = params.get("pErrCode");
        pay.pErrMsg = params.get("pErrMsg");
        pay.p3DesXmlPara = params.get("p3DesXmlPara");
        pay.pSign =  params.get("pSign");

        pay.print();

        JSONObject resultJson = pay.registerCreditorCB(error);

        String pPostUrl = IPSConstants.IPSH5Url.UNFREEZE_INVEST_AMOUNT;
        if(resultJson == null) {
            resultJson = new JSONObject();
            pPostUrl = IPSConstants.IPSH5Url.REGISTER_CREDITOR;
        }

        resultJson.put("code", error.code);
        resultJson.put("msg", error.msg);
        resultJson.put("pPostUrl", pPostUrl);
        resultJson.put("oldMerBillNo", pay.jsonPara.getString("pMerBillNo"));
        resultJson.put("pTrdAmt", pay.jsonPara.getString("pTrdAmt"));

        Logger.info("----------登记债权人(异步)-------------:"+resultJson.toString());
        Logger.info("----------登记债权人(异步) end-------------");
        renderText(resultJson.toString());
    }

    /**
     * 投标成功
     */
    public static void registerCreditorCB(){

        String result = params.get("result");

        Logger.info("投标回调信息 start >>：" + result);

        ErrorInfo error = new ErrorInfo();
        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) start-------------:");
        result = Encrypt.decrypt3DES(result, Constants.ENCRYPTION_KEY);

        JSONObject json = (JSONObject)Converter.xmlToObj(result);
        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) result-------------:"+result);
        String pMerBillNo = json.getString("oldMerBillNo");
        String info =IpsDetail.getIpsInfo(Long.parseLong(pMerBillNo), error);

        Gson gson = new Gson();
        Map<String, String> map = gson.fromJson(info, Map.class);
        long bidId = Convert.strToLong(map.get("bidId") + "", -1);

        if (error.code < 0 && error.code != Constants.ALREADY_RUN) {
            flash.error(error.msg);
            ProductAction.productBid(bidId + "");
        }
        Logger.info("投标回调信息 end >>：");

        String pTrdAmt = json.getString("pTrdAmt");
        t_bids tbid = t_bids.findById(bidId);
        String pTrdTime = DateUtil.dateToString3(new Date());

        investSuccess(tbid.title, pTrdAmt, pTrdTime);
    }

    /**
     * 投标失败情况(解冻投资金额)
     */
    public static void unfreezeInvestAmountCB(String result) {

        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) start-------------:");
        result = Encrypt.decrypt3DES(result, Constants.ENCRYPTION_KEY);

        JSONObject json = (JSONObject)Converter.xmlToObj(result);
        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) result-------------:"+result);
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = json.getString("pMerCode");
        pay.pErrCode = json.getString("pErrCode");
        pay.pErrMsg = json.getString("pErrMsg");
        pay.p3DesXmlPara = json.getString("p3DesXmlPara");
        pay.pSign = json.getString("pSign");

        String pMerBillNo = json.getString("oldMerBillNo");

        pay.print();
        pay.unfreezeInvestAmountCB(error);

        Map<String, Object> map = (Map<String, Object>) Cache.get(pMerBillNo);
        long bidId = Convert.strToLong(map.get("bidId") + "", -1);

        if (error.code < 0 && error.code != Constants.ALREADY_RUN) {
            flash.error(error.msg);
            ProductAction.productBid(bidId+"");
        }

        MainContent.property();//TODO

    }


    public static void investSuccess(String bidTitle, String pTrdAmt, String pTrdTime){
        JSONObject paramsJson = new JSONObject();
        paramsJson.put("pBidTitle", bidTitle);
        paramsJson.put("pTrdAmt", pTrdAmt);
        paramsJson.put("pTrdTime", pTrdTime);

        render(paramsJson);
    }


}
