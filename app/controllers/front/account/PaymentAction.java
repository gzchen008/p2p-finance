package controllers.front.account;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.cache.Cache;
import utils.Converter;
import utils.ErrorInfo;
import utils.NumberUtil;
import com.google.gson.Gson;
import com.shove.Convert;
import com.shove.security.Encrypt;
import business.BackstageSet;
import business.Bid;
import business.IpsDetail;
import business.OverBorrow;
import business.Payment;
import business.User;
import business.UserAuditItem;
import business.Vip;
import constants.Constants;
import constants.IPSConstants;
import constants.IPSConstants.RegisterGuarantorType;
import constants.OptionKeys;
import constants.Constants.MerToUserType;
import constants.Constants.PayType;
import constants.Constants.RechargeType;
import constants.IPSConstants.IPSPostUrl;
import constants.IPSConstants.TransferType;
import controllers.BaseController;
import controllers.front.bid.BidAction;
import controllers.front.invest.InvestAction;
import controllers.supervisor.bidManager.BidAgencyAction;
import controllers.supervisor.bidManager.BidPlatformAction;
import controllers.supervisor.bidManager.OverBorrowAction;
import controllers.supervisor.bidManager.UserAuditItemAction;
import controllers.supervisor.financeManager.LoanManager;
import controllers.supervisor.financeManager.PayableBillManager;
import controllers.supervisor.financeManager.PlatformAccountManager;
import controllers.supervisor.financeManager.ReceivableBillManager;
import utils.ParseClientUtil;

/**
 * 资金托管
 *
 * @author lzp
 * @version 6.0
 * @created 2014-9-16
 */
public class PaymentAction extends BaseController {
    /**
     * 开户
     */
    public static void createAcct() {
        String client = ParseClientUtil.parseClient(request);

        Map<String, String> args = Payment.createAcct(client);

        Logger.info("createAcct args:" + args);
        render(args);
    }

    /**
     * 开户回调
     */
    public static void createAcctCB(Long memberId, String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign,String ipsAcctNo) {
        ErrorInfo error = new ErrorInfo();

        Logger.info("createAcctCB exec ipsAcctNo：" + ipsAcctNo);
        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pStatus","10");
        jsonObject.put("ipsAcctNo",ipsAcctNo);
        jsonObject.put("pMemo1",memberId);
        pay.jsonPara = jsonObject;

        pay.createAcctCB(error);

        flash.error(error.msg);

        CheckAction.approve();
    }

    /**
     * 开户回调（异步）
     */
    public static void createAcctCBSys(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        ErrorInfo error = new ErrorInfo();

        Logger.info("-----------开户回调（异步）:----------");
        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.createAcctCB(error);

        renderText("{\"code\":\"" + error.code + "\"}");
    }

    /**
     * 标的登记
     */
    public static void registerSubject() {
        renderText("");
    }

    /**
     * 标的登记回调
     */
    public static void registerSubjectCB(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        if (IPSConstants.IS_REPAIR_TEST) {
            flash.error("模拟发标掉单");

            AccountHome.home();
        }

        Logger.info("-----------标的登记回调:----------");
        Logger.info("pMerCode:" + pMerCode);
        Logger.info("pErrCode:" + pErrCode);
        Logger.info("pErrMsg:" + pErrMsg);

        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.registerSubjectCB(error);

        String operation = pay.jsonPara.getString("pMemo3");
        String bidNo = pay.jsonPara.getString("pBidNo");
        String info = IpsDetail.getIpsInfo(Long.parseLong(bidNo), error);

        Gson gson = new Gson();
        Map<String, String> map = gson.fromJson(info, Map.class);
        Bid bid = new Bid();
        bid.id = Long.parseLong(map.get("bidId"));
//		Bid bid = (Bid) Cache.get("bid_"+operation+"_"+bidNo);
        flash.error(error.msg);

        if (IPSConstants.BID_CREATE.equals(operation)) {
            flash.put("msg", error.msg);

            if (bid.id > 0) {
                flash.put("no", OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, error) + bid.id);
                flash.put("title", bid.title);
                DecimalFormat myformat = new DecimalFormat();
                myformat.applyPattern("##,##0.00");
                flash.put("amount", myformat.format(bid.amount));
                flash.put("status", bid.status);
                flash.put("mobile", bid.user.mobile);
                flash.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
            }

            long agencyId = bid.agencyId;
            //Cache.delete("bid_"+operation+"_"+bidNo);

			/*发布机构合作标*/
            if (agencyId > 0) {
                flash.error(error.code < 0 ? error.msg : "发布成功!");

                BidAgencyAction.agencyBidList();
            }

            BidAction.applyNow(bid.productId, error.code == 0 ? 1 : error.code, 1);
        } else if (IPSConstants.BID_CANCEL.equals(operation)) {
            AccountHome.auditingLoanBids("", "", "", "");
        } else if (IPSConstants.BID_CANCEL_B.equals(operation)) {
            BidPlatformAction.fundraiseingList();
        } else if (IPSConstants.BID_CANCEL_S.equals(operation)) {
            BidPlatformAction.auditingList();
        } else if (IPSConstants.BID_CANCEL_I.equals(operation)) {
            BidPlatformAction.fundraiseingList();
        } else if (IPSConstants.BID_CANCEL_M.equals(operation)) {
            BidPlatformAction.fullList();
        } else if (IPSConstants.BID_CANCEL_F.equals(operation)) {
            AccountHome.loaningBids("", "", "", "");
        } else if (IPSConstants.BID_CANCEL_N.equals(operation)) {
            AccountHome.loaningBids("", "", "", "");
        }
    }

    /**
     * 标的登记回调（异步）
     */
    public static void registerSubjectCBSys(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        if (IPSConstants.IS_REPAIR_TEST) {
            flash.error("模拟发标掉单");

            AccountHome.home();
        }

        Logger.info("-----------标的登记回调（异步）:----------");
        Logger.info("pMerCode:" + pMerCode);
        Logger.info("pErrCode:" + pErrCode);
        Logger.info("pErrMsg:" + pErrMsg);

        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.registerSubjectCB(error);

        renderText("{\"code\":\"" + error.code + "\"}");
    }

    /**
     * 登记债权人
     */
    public static void registerCreditor() {
        renderText("登记债权人");
    }

    /**
     * 登记债权人回调
     */
    public static void registerCreditorCB(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        if (IPSConstants.IS_REPAIR_TEST) {
            flash.error("模拟投标掉单");

            AccountHome.home();
        }

        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.registerCreditorCB(error);

        ErrorInfo oldError = new ErrorInfo();
        oldError.code = error.code;
        oldError.msg = error.msg;

        String pMerBillNo = pay.jsonPara.getString("pMerBillNo");
        String info = IpsDetail.getIpsInfo(Long.parseLong(pMerBillNo), error);

        Gson gson = new Gson();
        Map<String, String> map = gson.fromJson(info, Map.class);
        long bidId = Convert.strToLong(map.get("bidId") + "", -1);
        int investAmount = Convert.strToInt(map.get("investAmount") + "", -1);

        if ((oldError.code < 0 && oldError.code != Constants.ALREADY_RUN) || error.code < 0) {
            if (error.code < 0) {
                flash.error(error.msg);
            } else {
                flash.error(oldError.msg);
            }

            InvestAction.invest(bidId, "");
        }

        flash.put("amount", NumberUtil.amountFormat(investAmount));
        String showBox = Encrypt.encrypt3DES(Constants.SHOW_BOX, bidId + Constants.ENCRYPTION_KEY);
        InvestAction.invest(bidId, showBox);
    }

    /**
     * 登记债权人回调（异步）
     */
    public static void registerCreditorCBSys(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("----------- 登记债权人回调（异步）:----------");
        ErrorInfo error = new ErrorInfo();

        if (IPSConstants.IS_REPAIR_TEST) {
            flash.error("模拟投标掉单");

            AccountHome.home();
        }

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;

        pay.print();

        JSONObject resultJson = pay.registerCreditorCB(error);

        String pPostUrl = IPSPostUrl.UNFREEZE_INVEST_AMOUNT;
        if (resultJson == null) {
            resultJson = new JSONObject();
            pPostUrl = IPSPostUrl.REGISTER_CREDITOR;
        }

        resultJson.put("code", error.code);
        resultJson.put("msg", error.msg);
        resultJson.put("pPostUrl", pPostUrl);
        resultJson.put("oldMerBillNo", pay.jsonPara.getString("pMerBillNo"));

        Logger.info("----------登记债权人(异步)-------------:" + resultJson.toString());
        Logger.info("----------登记债权人(异步) end-------------");
        renderText(resultJson.toString());
    }

    /**
     * 登记债权人(ws处理业务逻辑)
     */
    public static void registerCreditorWS(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        if (IPSConstants.IS_REPAIR_TEST) {
            flash.error("模拟投标掉单");

            AccountHome.home();
        }
        Logger.info("----------登记债权人(ws处理业务逻辑) start-------------");
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;

        JSONObject resultJson = pay.registerCreditorCB(error);

        String pPostUrl = IPSPostUrl.UNFREEZE_INVEST_AMOUNT;
        if (resultJson == null) {
            resultJson = new JSONObject();
            pPostUrl = IPSPostUrl.REGISTER_CREDITOR;
        }

        resultJson.put("code", error.code);
        resultJson.put("msg", error.msg);
        resultJson.put("pPostUrl", pPostUrl);
        resultJson.put("oldMerBillNo", pay.jsonPara.getString("pMerBillNo"));

        Logger.info("----------登记债权人(ws处理业务逻辑)-------------:" + resultJson.toString());
        Logger.info("----------登记债权人(ws处理业务逻辑) end-------------");
        renderText(resultJson.toString());
    }

    /**
     * 登记债权人(ws处理业务逻辑后post返回)
     */
    public static void registerCreditorPost(String result) {
        ErrorInfo error = new ErrorInfo();
        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) start-------------:");
        result = Encrypt.decrypt3DES(result, Constants.ENCRYPTION_KEY);

        JSONObject json = (JSONObject) Converter.xmlToObj(result);
        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) result-------------:" + result);
        String pMerBillNo = json.getString("oldMerBillNo");
        String info = IpsDetail.getIpsInfo(Long.parseLong(pMerBillNo), error);

        Gson gson = new Gson();
        Map<String, String> map = gson.fromJson(info, Map.class);
        long bidId = Convert.strToLong(map.get("bidId") + "", -1);
        int investAmount = Convert.strToInt(map.get("investAmount") + "", -1);
        int code = json.getInt("code");
        String msg = json.getString("msg");

        if (code < 0 && code != Constants.ALREADY_RUN) {
            flash.error(msg);

            InvestAction.invest(bidId, "");
        }

        flash.put("amount", NumberUtil.amountFormat(investAmount));
        String showBox = Encrypt.encrypt3DES(Constants.SHOW_BOX, bidId + Constants.ENCRYPTION_KEY);
        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) end-------------:");
        InvestAction.invest(bidId, showBox);
    }

    /**
     * 登记债权人(解冻投资金额)
     */
    public static void unfreezeInvestAmountPost(String result) {
        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) start-------------:");
        result = Encrypt.decrypt3DES(result, Constants.ENCRYPTION_KEY);

        JSONObject json = (JSONObject) Converter.xmlToObj(result);
        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) result-------------:" + result);
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
        int investAmount = Convert.strToInt(map.get("investAmount") + "", -1);

        if (error.code < 0 && error.code != Constants.ALREADY_RUN) {
            flash.error(error.msg);

            InvestAction.invest(bidId, "");
        }

        flash.put("amount", NumberUtil.amountFormat(investAmount));
        String showBox = Encrypt.encrypt3DES(Constants.SHOW_BOX, bidId + Constants.ENCRYPTION_KEY);
        InvestAction.invest(bidId, showBox);
    }

    /**
     * 登记债权转让
     */
    public static void registerCretansfer() {
        renderText("");
    }

    /**
     * 登记债权转让回调
     */
    public static void registerCretansferCB(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        if (IPSConstants.IS_REPAIR_TEST) {
            flash.error("模拟债权转让掉单");

            AccountHome.home();
        }

        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.registerCretansferCB(error);

        InvestAccount.myDebts(error.code, error.msg);
    }

    /**
     * 登记债权转让回调（异步）
     */
    public static void registerCretansferCBSys(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("-----------登记债权转让回调（异步）:----------");
        ErrorInfo error = new ErrorInfo();

        if (IPSConstants.IS_REPAIR_TEST) {
            flash.error("模拟债权转让掉单");

            AccountHome.home();
        }

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.registerCretansferCB(error);

        renderText("{\"code\":\"" + error.code + "\"}");
    }

    /**
     * 自动投标签约
     */
    public static void autoNewSigning() {
        renderText("");
    }

    /**
     * 自动投标签约回调
     */
    public static void autoNewSigningCB(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.autoNewSigningCB(error);

        if (StringUtils.isNotBlank(p3DesXmlPara)) {
            p3DesXmlPara = Encrypt.decrypt3DES(p3DesXmlPara, Constants.ENCRYPTION_KEY);
        }

        Logger.info("pErrMsg:\n" + pErrMsg + "\np3DesXmlPara:\n" + p3DesXmlPara + "\nerror.msg:\n" + error.msg);

        if (error.code < 0) {
            InvestAccount.auditmaticInvest(error.code, error.msg);
        }

        InvestAccount.auditmaticInvest(1, "开启投标机器人成功");
    }

    /**
     * 自动投标签约回调（异步）
     */
    public static void autoNewSigningCBSys(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("-----------自动投标签约回调（异步）:----------");
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.autoNewSigningCB(error);

        renderText("{\"code\":\"" + error.code + "\"}");
    }

    /**
     * 自动还款签约
     */
    public static void repaymentSigning() {
        Map<String, String> args = Payment.repaymentSigning();

        render(args);
    }

    /**
     * 自动还款签约回调
     */
    public static void repaymentSigningCB(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.repaymentSigningCB(error);

        flash.error(error.msg);

        BidAction.index(0, 0, 0);
    }

    /**
     * 自动还款签约回调（异步）
     */
    public static void repaymentSigningCBSys(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("-----------自动投标签约回调（异步）:----------");
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.repaymentSigningCB(error);

        renderText("{\"code\":\"" + error.code + "\"}");
    }

    /**
     * 充值
     */
    public static void doDpTrade() {
        renderText("");
    }

    /**
     * 充值回调(同步)
     */
    public static void doDpTradeCB(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        if (IPSConstants.IS_REPAIR_TEST) {
            flash.error("模拟充值掉单");

            AccountHome.home();
        }

        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.doDpTradeCB(error);

        long userId = pay.jsonPara.getLong("pMemo1");
        Map<String, Object> mapRecharge = (Map<String, Object>) Cache.get("rechargePayIps" + userId);

        //支付发标保证金
        if (mapRecharge != null) {
            Bid bid = (Bid) mapRecharge.get("bid");

            if (error.code < 0 && error.code != Constants.ALREADY_RUN) {
                flash.error(error.msg);

                BidAction.applyNow(bid.productId, error.code, 1);
            }

            bid.createBid = true;
            bid.user.id = userId;
            bid.createBid(error);

            if (error.code < 0) {
                flash.put("msg", error.msg);

                BidAction.applyNow(bid.productId, error.code, 1);
            }

            Cache.delete("bid_" + session.getId()); // 删除错误带回页面数据的缓存
            Cache.delete("rechargePayIps" + bid.userId);

			/*投标奖励*/
            if (bid.bonusType != Constants.NOT_REWARD) {
                User user = User.currUser();

                switch (Constants.PAY_TYPE_INVEST) {
                    //平台内部进行转账
                    case PayType.INNER:
                        flash.error("资金托管模式下，不能以平台内部进行转账的方式支付投标奖励费");

                        BidAction.applyNow(bid.productId, -1, 1);
                        //通过独立普通网关
                    case PayType.INDEPENDENT:
                        if (bid.investBonus > user.balanceDetail.user_amount2) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("rechargeType", RechargeType.InvestBonus);
                            map.put("fee", bid.investBonus);
                            map.put("bid", bid);
                            Cache.set("rechargePay" + User.currUser().id, map, IPSConstants.CACHE_TIME);
                            flash.error("请支付投标奖励费");

                            FundsManage.rechargePay();
                        } else {
                            bid.deductInvestBonus(error);

                            if (error.code < 0) {
                                render(Constants.ERROR_PAGE_PATH_FRONT);
                            }
                        }
                        //通过共享资金托管账户网关
                    case PayType.SHARED:
                        if (bid.investBonus > user.balance) {
                            flash.error("您可用余额不足支付投标奖励费，请充值后再发布借款标");

                            FundsManage.recharge();
                        }

                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("rechargeType", RechargeType.InvestBonus);
                        map.put("fee", bid.investBonus);
                        map.put("bid", bid);
                        Cache.set("rechargePay" + User.currUser().id, map, IPSConstants.CACHE_TIME);
                        flash.error("请支付投标奖励费");

                        FundsManage.rechargePay();
                        //资金托管网关
                    case PayType.IPS:
                        if (bid.investBonus > user.balance) {
                            flash.error("您可用余额不足支付投标奖励费，请充值后再发布借款标");

                            FundsManage.recharge();
                        }

                        map = new HashMap<String, Object>();
                        map.put("rechargeType", RechargeType.InvestBonus);
                        map.put("fee", bid.investBonus);
                        map.put("bid", bid);
                        Cache.set("rechargePay" + User.currUser().id, map, IPSConstants.CACHE_TIME);
                        flash.error("请支付投标奖励费");

                        PaymentAction.transferUserToMer();
                }
            }

            Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CREATE, bid);

            render("@front.account.PaymentAction.registerSubject", args);
        }

        if (error.code < 0 && error.code != Constants.ALREADY_RUN) {
            flash.error(error.msg);
        } else {
            flash.error("充值成功");
        }

        FundsManage.recharge();
    }

    /**
     * 充值回调（异步）
     */
    public static void doDpTradeCBSys(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("-----------充值回调（异步）:----------");
        ErrorInfo error = new ErrorInfo();

        if (IPSConstants.IS_REPAIR_TEST) {
            flash.error("模拟充值掉单");

            AccountHome.home();
        }

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;

        pay.print();
        pay.doDpTradeCB(error);

        JSONObject resultJson = new JSONObject();

        resultJson.put("code", error.code);
        resultJson.put("msg", error.msg);
        resultJson.put("pMemo1", pay.jsonPara.getString("pMemo1"));
        resultJson.put("pPostUrl", IPSPostUrl.DO_DP_TRADE);

        Logger.info("----------充值异步(ws处理业务逻辑)-------------:" + resultJson.toString());
        Logger.info("---------充值异步(ws处理业务逻辑) end-------------");
        renderText(resultJson.toString());
    }

    /**
     * 充值回调（ws处理业务逻辑）
     */
    public static void doDpTradeWS(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("--------- 充值(ws处理业务逻辑) start-------------");
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;

        pay.print();
        pay.doDpTradeCB(error);

        JSONObject resultJson = new JSONObject();

        resultJson.put("code", error.code);
        resultJson.put("msg", error.msg);
        resultJson.put("pMemo1", pay.jsonPara.getString("pMemo1"));
        resultJson.put("pPostUrl", IPSPostUrl.DO_DP_TRADE);

        Logger.info("----------充值(ws处理业务逻辑)-------------:" + resultJson.toString());
        Logger.info("---------充值(ws处理业务逻辑) end-------------");
        renderText(resultJson.toString());
    }

    /**
     * 充值回调（ws处理业务逻辑后post返回）
     */
    public static void doDpTradePost(String result) {
        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) start-------------:");
        result = Encrypt.decrypt3DES(result, Constants.ENCRYPTION_KEY);
        ErrorInfo error = new ErrorInfo();

        JSONObject json = (JSONObject) Converter.xmlToObj(result);
        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) result-------------:" + result);

        error.code = json.getInt("code");
        error.msg = json.getString("msg");

        long userId = json.getLong("pMemo1");
        Map<String, Object> mapRecharge = (Map<String, Object>) Cache.get("rechargePayIps" + userId);

        //支付发标保证金
        if (mapRecharge != null) {
            Bid bid = (Bid) mapRecharge.get("bid");

            if (error.code < 0 && error.code != Constants.ALREADY_RUN) {
                flash.error(error.msg);

                BidAction.applyNow(bid.productId, error.code, 1);
            }

            bid.createBid = true;
            bid.user.id = userId;
            bid.createBid(error);

            if (error.code < 0) {
                flash.put("msg", error.msg);

                BidAction.applyNow(bid.productId, error.code, 1);
            }

            Cache.delete("bid_" + session.getId()); // 删除错误带回页面数据的缓存
            Cache.delete("rechargePayIps" + bid.userId);

			/*投标奖励*/
            if (bid.bonusType != Constants.NOT_REWARD) {
                User user = User.currUser();

                switch (Constants.PAY_TYPE_INVEST) {
                    //平台内部进行转账
                    case PayType.INNER:
                        flash.error("资金托管模式下，不能以平台内部进行转账的方式支付投标奖励费");

                        BidAction.applyNow(bid.productId, -1, 1);
                        //通过独立普通网关
                    case PayType.INDEPENDENT:
                        if (bid.investBonus > user.balanceDetail.user_amount2) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("rechargeType", RechargeType.InvestBonus);
                            map.put("fee", bid.investBonus);
                            map.put("bid", bid);
                            Cache.set("rechargePay" + User.currUser().id, map, IPSConstants.CACHE_TIME);
                            flash.error("请支付投标奖励费");

                            FundsManage.rechargePay();
                        } else {
                            bid.deductInvestBonus(error);

                            if (error.code < 0) {
                                render(Constants.ERROR_PAGE_PATH_FRONT);
                            }
                        }
                        //通过共享资金托管账户网关
                    case PayType.SHARED:
                        if (bid.investBonus > user.balance) {
                            flash.error("您可用余额不足支付投标奖励费，请充值后再发布借款标");

                            FundsManage.recharge();
                        }

                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("rechargeType", RechargeType.InvestBonus);
                        map.put("fee", bid.investBonus);
                        map.put("bid", bid);
                        Cache.set("rechargePay" + User.currUser().id, map, IPSConstants.CACHE_TIME);
                        flash.error("请支付投标奖励费");

                        FundsManage.rechargePay();
                        //资金托管网关
                    case PayType.IPS:
                        if (bid.investBonus > user.balance) {
                            flash.error("您可用余额不足支付投标奖励费，请充值后再发布借款标");

                            FundsManage.recharge();
                        }

                        map = new HashMap<String, Object>();
                        map.put("rechargeType", RechargeType.InvestBonus);
                        map.put("fee", bid.investBonus);
                        map.put("bid", bid);
                        Cache.set("rechargePay" + User.currUser().id, map, IPSConstants.CACHE_TIME);
                        flash.error("请支付投标奖励费");

                        PaymentAction.transferUserToMer();
                }
            }

            Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CREATE, bid);

            render("@front.account.PaymentAction.registerSubject", args);
        }

        if (error.code < 0 && error.code != Constants.ALREADY_RUN) {
            flash.error(error.msg);
        } else {
            flash.error("充值成功");
        }

        FundsManage.recharge();
    }

    /**
     * 转账
     */
    public static void transfer() {
        renderText("");
    }

    /**
     * 转账回调
     */
    public static void transferCB(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.transferCB(error);

        int pTransferType = pay.jsonPara.getInt("pTransferType");

        if (error.code < 0) {
            Logger.info("transferCB" + error.msg);
            flash.error(error.msg);

            if (pTransferType == TransferType.COMPENSATE_REPAYMENT) {
                AccountHome.myLoanBills(0, 0, 0, null, 1);
            }

            LoanManager.readyReleaseList();
        }

        //放款回调，处理投标奖励
        if (pTransferType == TransferType.INVEST) {
            //发放奖励方式配置为资金托管模式
            if (Constants.PAY_TYPE_FUND == PayType.IPS) {
                String pMerBillNo = pay.jsonPara.getString("pMerBillNo");

                String info = IpsDetail.getIpsInfo(Long.parseLong(pMerBillNo), error);

                Gson gson = new Gson();
                Map<String, String> map = gson.fromJson(info, Map.class);
                Bid bid = new Bid();
                bid.id = Long.parseLong(map.get("bidId"));
//				Bid bid = (Bid) Cache.get(pMerBillNo)

                if (bid.id < 0) {
                    error.code = -1;
                    error.msg = "放款失败";

                    return;
                }

                if (bid.bonusType != Constants.NOT_REWARD) {
                    List<Map<String, Object>> pDetails = bid.queryInvestFunds(error);
                    JSONObject memo = new JSONObject();
                    memo.put("pPayType", MerToUserType.Fund);
                    memo.put("pMerBillNo", pMerBillNo);
                    Map<String, String> args = Payment.transferMerToUser(pDetails, memo);

                    render("@front.account.PaymentAction.transferMerToUser", args);
                }
            }
        }

        if (pTransferType == TransferType.COMPENSATE_REPAYMENT) {
            flash.error(error.msg);

            AccountHome.myLoanBills(0, 0, 0, null, 1);
        }

        if (error.code < 0) {
            flash.error(error.msg);
        } else {
            flash.error("放款成功");
        }

        LoanManager.readyReleaseList();
    }

    /**
     * 转账回调（异步）
     */
    public static void transferCBSys(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("-----------转账回调（异步）:----------");
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.transferCB(error);

        renderText("{\"code\":\"" + error.code + "\"}");
    }

    /**
     * 登记担保方
     *
     * @param error
     */
    public static void registerGuarantor() {
        renderText("");
    }

    /**
     * 登记担保方回调
     *
     * @param error
     */
    public static void registerGuarantorCB(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.registerGuarantorCB(error);

        int type = pay.jsonPara.getInt("pMemo3");
        flash.error(error.msg);

        switch (type) {
            case RegisterGuarantorType.COMPENSATE:
                PayableBillManager.overdueUnpaidBills();
                break;
            case RegisterGuarantorType.OFFLINE_REPAYMENT:
                ReceivableBillManager.toReceiveBills();
                break;
            default:
                ReceivableBillManager.overdueBills();
                break;
        }
    }

    /**
     * 登记担保方回调（异步）
     *
     * @param error
     */
    public static void registerGuarantorCBSys(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        ErrorInfo error = new ErrorInfo();
        Logger.info("-----------登记担保方回调（异步）:----------");

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.registerGuarantorCB(error);

        renderText("{\"code\":\"" + error.code + "\"}");
    }

    /**
     * 本金垫付
     */
    public static void compensate() {
        renderText("");
    }

    /**
     * 代偿还款
     */
    public static void compensateRepayment() {
        renderText("");
    }

    /**
     * 本金垫付回调
     */
    public static void compensateCB(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.compensateCB(error);

        flash.error(error.msg);

        PayableBillManager.overdueUnpaidBills();
    }

    /**
     * 本金垫付回调（异步）
     */
    public static void compensateCBSys(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        ErrorInfo error = new ErrorInfo();
        Logger.info("-----------本金垫付回调（异步）:----------");

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.compensateCB(error);

        renderText("{\"code\":\"" + error.code + "\"}");
    }

    /**
     * 线下收款回调
     */
    public static void offlineRepaymentCB(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.offlineRepaymentCB(error);

        flash.error(error.msg);

        ReceivableBillManager.overdueBills();
    }

    /**
     * 线下收款回调（异步）
     */
    public static void offlineRepaymentCBSys(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        ErrorInfo error = new ErrorInfo();
        Logger.info("-----------线下收款回调（异步）:----------");

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.offlineRepaymentCB(error);

        renderText("{\"code\":\"" + error.code + "\"}");
    }

    /**
     * 还款
     */
    public static void repaymentNewTrade() {
        renderText("");
    }

    /**
     * 还款回调
     */
    public static void repaymentNewTradeCB(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        if (IPSConstants.IS_REPAIR_TEST) {
            flash.error("模拟还款掉单");

            AccountHome.home();
        }

        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.repaymentNewTradeCB(error);

        if (error.code < 0 && error.code != Constants.ALREADY_RUN) {
            flash.error(error.msg);
        } else {
            flash.error("还款成功");
        }

        AccountHome.myLoanBills(0, 0, 0, null, 0);
    }

    /**
     * 还款回调（异步）
     */
    public static void repaymentNewTradeCBSys(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("-----------还款回调（异步）:----------");
        if (IPSConstants.IS_REPAIR_TEST) {
            flash.error("模拟还款掉单");

            AccountHome.home();
        }

        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.repaymentNewTradeCB(error);

        JSONObject resultJson = new JSONObject();
        resultJson.put("code", error.code);
        resultJson.put("msg", error.msg);
        resultJson.put("pPostUrl", IPSPostUrl.REPAYMENT_NEW_TRADE);

        Logger.info("----------登记债权人(ws处理业务逻辑)-------------:" + resultJson.toString());
        Logger.info("----------登记债权人(ws处理业务逻辑) end-------------");
        renderText(resultJson.toString());
    }

    /**
     * 还款回调（ws处理业务逻辑）
     */
    public static void repaymentNewTradeWS(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("----------还款回调(ws处理业务逻辑) start-------------");

        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.repaymentNewTradeCB(error);

        JSONObject resultJson = new JSONObject();
        resultJson.put("code", error.code);
        resultJson.put("msg", error.msg);
        resultJson.put("pPostUrl", IPSPostUrl.REPAYMENT_NEW_TRADE);

        Logger.info("----------登记债权人(ws处理业务逻辑)-------------:" + resultJson.toString());
        Logger.info("----------登记债权人(ws处理业务逻辑) end-------------");
        renderText(resultJson.toString());
    }

    /**
     * 还款回调（ws处理业务逻辑后post返回）
     */
    public static void repaymentNewTradePost(String result) {
        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) start-------------:");
        result = Encrypt.decrypt3DES(result, Constants.ENCRYPTION_KEY);

        JSONObject json = (JSONObject) Converter.xmlToObj(result);

        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) result-------------:" + result);
        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) end-------------:");

        int code = json.getInt("code");
        String msg = json.getString("msg");

        if (code < 0 && code != Constants.ALREADY_RUN) {
            flash.error(msg);
        } else {
            flash.error("还款成功");
        }

        AccountHome.myLoanBills(0, 0, 0, null, 0);
    }

    /**
     * 解冻保证金
     */
    public static void guaranteeUnfreeze() {
        renderText("");
    }

    /**
     * 解冻保证金回调
     *
     * @param pMerCode
     * @param pErrCode
     * @param pErrMsg
     * @param p3DesXmlPara
     * @param pSign
     */
    public static void guaranteeUnfreezeCB(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.guaranteeUnfreezeCB(error);
    }

    /**
     * 解冻保证金回调（异步）
     *
     * @param pMerCode
     * @param pErrCode
     * @param pErrMsg
     * @param p3DesXmlPara
     * @param pSign
     */
    public static void guaranteeUnfreezeCBSys(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("-----------解冻保证金回调（异步）:----------");
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.guaranteeUnfreezeCB(error);

        renderText("{\"code\":\"" + error.code + "\"}");
    }

    /**
     * 提现
     */
    public static void doDwTrade() {
        renderText("");
    }

    /**
     * 提现回调
     */
    public static void doDwTradeCB(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        if (IPSConstants.IS_REPAIR_TEST) {
            flash.error("模拟提现掉单");

            AccountHome.home();
        }

        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.doDwTradeCB(error);

        if (error.code < 0 && error.code != Constants.ALREADY_RUN) {
            flash.error(error.msg);
        } else {
            flash.error("提现成功");
        }

        FundsManage.withdrawal();
    }

    /**
     * 提现回调（异步）
     */
    public static void doDwTradeCBSys(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("----------- 提现回调（异步）:----------");
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.doDwTradeCB(error);

        JSONObject resultJson = new JSONObject();
        resultJson.put("code", error.code);
        resultJson.put("msg", error.msg);
        resultJson.put("pPostUrl", IPSPostUrl.DO_DW_TRADE);

        Logger.info("----------提现回调（异步）(ws处理业务逻辑)-------------:" + resultJson.toString());
        Logger.info("----------提现回调（异步）(ws处理业务逻辑) end-------------");
        renderText(resultJson.toString());
    }

    /**
     * 提现回调（ws处理业务逻辑）
     */
    public static void doDwTradeWS(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("----------- 提现回调（ws处理业务逻辑）start----------");
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.doDwTradeCB(error);

        JSONObject resultJson = new JSONObject();
        resultJson.put("code", error.code);
        resultJson.put("msg", error.msg);
        resultJson.put("pPostUrl", IPSPostUrl.DO_DW_TRADE);

        Logger.info("----------提现回调(ws处理业务逻辑)-------------:" + resultJson.toString());
        Logger.info("----------提现回调(ws处理业务逻辑) end-------------");
        renderText(resultJson.toString());
    }

    /**
     * 提现回调（ws处理业务逻辑后post返回）
     */
    public static void doDwTradePost(String result) {
        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) start-------------:");
        result = Encrypt.decrypt3DES(result, Constants.ENCRYPTION_KEY);

        JSONObject json = (JSONObject) Converter.xmlToObj(result);
        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) result-------------:" + result);


        if (json.getInt("code") < 0 && json.getInt("code") != Constants.ALREADY_RUN) {
            flash.error(json.getString("msg"));
        } else {
            flash.error("提现成功");
        }

        Logger.info("----------登记债权人(ws处理业务逻辑后post返回) end-------------:");
        FundsManage.withdrawal();
    }

    /**
     * 账户余额查询
     */
    public static void queryForAccBalance(String name) {
        ErrorInfo error = new ErrorInfo();

        if (StringUtils.isBlank(name)) {
            name = User.currUser().name;
        }

        renderText(Payment.queryForAccBalance(name, error).toString());
    }

    /**
     * 商户端获取银行列表查询
     */
    public static void getBankList() {
        ErrorInfo error = new ErrorInfo();
        renderText(Payment.getBankList(error).toString());
    }

    /**
     * 账户信息查询
     */
    public static void queryMerUserInfo() {
        ErrorInfo error = new ErrorInfo();
        renderText(Payment.queryMerUserInfo(User.currUser().ipsAcctNo, error).toString());
    }

    /**
     * 转账-用户转商户
     */
    public static void transferUserToMer() {
        User user = User.currUser();
        Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);

        if (null == map) {
            renderText("请求过时或已提交!");
        }

        double fee = (Double) map.get("fee");

        ErrorInfo error = new ErrorInfo();
        Map<String, String> args = Payment.transferUserToMer(User.currUser(), fee, error);

        render(args);
    }

    /**
     * 转账-用户转商户回调
     */
    public static void transferUserToMerCB(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("转账-用户转商户-同步回调");
        Logger.info("pMerCode:%s", pMerCode);
        Logger.info("pErrCode:%s", pErrCode);
        Logger.info("pErrMsg:%s", pErrMsg);
        Logger.info("p3DesXmlPara:%s", Encrypt.decrypt3DES(p3DesXmlPara, Constants.ENCRYPTION_KEY));
        Logger.info("pSign:%s", pSign);
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.transferUserToMerCB(error);

        if (error.code < 0 && error.code != Constants.ALREADY_RUN) {
            flash.error(error.msg);

            AccountHome.home();
        }

        User user = User.currUser();
        Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);

        if (map == null) {
            flash.error("请求已过期");

            AccountHome.home();
        }

        int rechargeType = (Integer) map.get("rechargeType");

        if (rechargeType == RechargeType.VIP) {
            if (error.code == Constants.ALREADY_RUN) {
                flash.error("支付vip费用成功");
                Cache.delete("rechargePay" + user.id);

                AccountHome.home();
            }

            int serviceTime = (Integer) map.get("serviceTime");
            Vip vip = new Vip();
            vip.isPay = true;
            vip.serviceTime = serviceTime;
            vip.renewal(user, error);

            if (error.code < 0) {
                flash.error(error.msg);
            } else {
                flash.error("支付vip费用成功");
            }

            Cache.delete("rechargePay" + user.id);

            AccountHome.home();
        }

        if (rechargeType == RechargeType.InvestBonus) {
            Bid bid = (Bid) map.get("bid");
            bid.deductInvestBonus(error);

            if (error.code < 0) {
                flash.error(error.msg);
                render(error);
            }

            Cache.delete("rechargePay" + bid.userId);

            Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CREATE, bid);

            render("@front.account.PaymentAction.registerSubject", args);
        }

        if (rechargeType == RechargeType.UploadItems) {
            if (error.code == Constants.ALREADY_RUN) {
                flash.error("支付资料审核费成功");
                Cache.delete("rechargePay" + user.id);

                AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
            }

            UserAuditItem.submitUploadedItems(user.id, user.balance, error);

            if (error.code < 0) {
                flash.error(error.msg);
            } else {
                flash.error("支付资料审核费成功");
            }

            Cache.delete("rechargePay" + user.id);

            AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
        }

        if (rechargeType == RechargeType.UploadItemsOB) {
            if (error.code == Constants.ALREADY_RUN) {
                flash.error("支付资料审核费成功");
                Cache.delete("rechargePay" + user.id);

                AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
            }

            int _amount = (Integer) map.get("amount");
            String reason = (String) map.get("reason");
            List<Map<String, String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
            OverBorrow overBorrow = new OverBorrow();
            overBorrow.isPay = true;
            overBorrow.applyFor(user, _amount, reason, auditItems, error);

            if (error.code < 0) {
                flash.error(error.msg);
            } else {
                flash.error("申请超额借款成功");
            }

            Cache.delete("rechargePay" + user.id);

            AccountHome.home();
        }
    }

    /**
     * 转账-用户转商户-异步回调
     */
    public static void transferUserToMerCBSys(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("转账-用户转商户-异步回调");
        Logger.info("pMerCode:%s", pMerCode);
        Logger.info("pErrCode:%s", pErrCode);
        Logger.info("pErrMsg:%s", pErrMsg);
        Logger.info("p3DesXmlPara:%s", Encrypt.decrypt3DES(p3DesXmlPara, Constants.ENCRYPTION_KEY));
        Logger.info("pSign:%s", pSign);
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;


        User user = new User();
        Logger.info("-------------------------" + pay.jsonPara + "-----------------------------");
        user.id = Convert.strToLong(pay.jsonPara.getString("pMemo1").trim(), 0);
        Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);

        if (map == null) {
            Logger.error("请求已过期");

            renderText("{\"code\":\"" + error.code + "\"}");
        }

        int rechargeType = (Integer) map.get("rechargeType");
		
		/* 投标奖励需要页面提交,故此屏蔽后台通知  */
        if (rechargeType == RechargeType.InvestBonus)
            renderText("{\"code\":\"" + error.code + "\"}");

        pay.transferUserToMerCB(error);

        if (error.code < 0) {
            Logger.error(error.msg);
            renderText("{\"code\":\"" + error.code + "\"}");
        }

        if (rechargeType == RechargeType.VIP) {
            int serviceTime = (Integer) map.get("serviceTime");
            Vip vip = new Vip();
            vip.isPay = true;
            vip.serviceTime = serviceTime;
            vip.renewal(user, error);

            //Cache.delete("rechargePay" + user.id);

            renderText("{\"code\":\"" + error.code + "\"}");
        }
		
		/* 投标奖励需要页面提交,故此方法失效  */
		/*if (rechargeType == RechargeType.InvestBonus) {
			Bid bid = (Bid) map.get("bid");
			bid.deductInvestBonus(error);
			
			if(error.code < 0) {
				Logger.error("请求已过期");
				return;
			}
			
			Cache.delete("rechargePay" + bid.userId);
			
			Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CREATE, bid);
			
			render("@front.account.PaymentAction.registerSubject", args);
		}*/

        if (rechargeType == RechargeType.UploadItems) {
            UserAuditItem.submitUploadedItems(user.id, user.balance, error);
            //Cache.delete("rechargePay" + user.id);

            renderText("{\"code\":\"" + error.code + "\"}");
        }

        if (rechargeType == RechargeType.UploadItemsOB) {
            int _amount = (Integer) map.get("amount");
            String reason = (String) map.get("reason");
            List<Map<String, String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
            OverBorrow overBorrow = new OverBorrow();
            overBorrow.isPay = true;
            overBorrow.applyFor(user, _amount, reason, auditItems, error);
            //Cache.delete("rechargePay" + user.id);

            renderText("{\"code\":\"" + error.code + "\"}");
        }
    }

    /**
     * 转账-商户转用户(POST)
     */
    public static void transferMerToUser() {
        User user = User.currUser();
        ErrorInfo error = new ErrorInfo();
        Payment.transferMerToUser(user.id, 10, error);

        renderText(error.msg);
    }

    /**
     * 转账-商户转用户回调
     */
    public static void transferMerToUserCB(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("转账-商户转用户回调");
        Logger.info("pMerCode:%s", pMerCode);
        Logger.info("pErrCode:%s", pErrCode);
        Logger.info("pErrMsg:%s", pErrMsg);
        Logger.info("p3DesXmlPara:%s", Encrypt.decrypt3DES(p3DesXmlPara, Constants.ENCRYPTION_KEY));
        Logger.info("pSign:%s", pSign);
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.transferMerToUserCB(error);

        JSONObject memo = JSONObject.fromObject(IpsDetail.getIpsInfo(pay.jsonPara.getLong("pMerBillNo"), error));
        int pPayType = memo.getInt("pPayType");

        if (MerToUserType.Fund == pPayType) {
            if (error.code < 0 && error.code != Constants.ALREADY_RUN) {
                flash.error(error.msg);
            } else {
                flash.error("放款成功");
            }

            LoanManager.readyReleaseList();
        }

        if (MerToUserType.ItemFefund == pPayType) {
            if (error.code < 0 && error.code != Constants.ALREADY_RUN) {
                flash.error(error.msg);
            }
			
			/* 如果是超额借款详情 */
            long overBorrowId = memo.getLong("overBorrowId");

            if (overBorrowId > 0) {
                OverBorrowAction.overBorrowDetails(overBorrowId);
            }

            long bidId = memo.getLong("bidId");
			
			/* 如果是标详情审核 */
            if (bidId > 0) {
                int detail = memo.getInt("detail"); // 具体详情页 1.审核中 2.募集中 3.满标 4.成功的... 5.失败的...

                switch (detail) {
                    case 1:
                        BidPlatformAction.auditingDetail(bidId);
                        break;
                    case 2:
                        BidPlatformAction.fundraiseingDetail(bidId);
                        break;
                    case 3:
                        BidPlatformAction.fullDetail(bidId);
                        break;
                }
            }

            UserAuditItemAction.auditDetail(memo.getString("signUserId"));
        }
    }

    /**
     * 转账-商户转用户-异步回调
     */
    public static void transferMerToUserCBSys(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("转账-商户转用户异步回调");
        Logger.info("pMerCode:%s", pMerCode);
        Logger.info("pErrCode:%s", pErrCode);
        Logger.info("pErrMsg:%s", pErrMsg);
        Logger.info("p3DesXmlPara:%s", Encrypt.decrypt3DES(p3DesXmlPara, Constants.ENCRYPTION_KEY));
        Logger.info("pSign:%s", pSign);
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.transferMerToUserCB(error);

        renderText("{\"code\":\"" + error.code + "\"}");
    }

    /**
     * 解冻投资金额(POST)
     */
    public static void unfreezeInvestAmount(String merBillNo) {
        ErrorInfo error = new ErrorInfo();

        String pIpsBillNo = IpsDetail.queryMemoByMerBillNo(merBillNo, error);

        //解冻交易金额post
        Map<String, String> args = Payment.unfreezeInvestAmount(merBillNo, pIpsBillNo, false, error);

        render(args);
    }

    /**
     * 解冻投资金额(WS)
     */
    public static void unfreezeInvestAmountWS(String merBillNo) {
        ErrorInfo error = new ErrorInfo();

        String pIpsBillNo = IpsDetail.queryMemoByMerBillNo(merBillNo, error);

        //解冻交易金额ws
        Payment.unfreezeInvestAmount(merBillNo, pIpsBillNo, true, error);

        renderJSON(error);
    }

    /**
     * 解冻投资金额回调(POST，用于管理员补单)
     */
    public static void unfreezeInvestAmountCB(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("解冻投资金额回调");
        Logger.info("pMerCode:%s", pMerCode);
        Logger.info("pErrCode:%s", pErrCode);
        Logger.info("pErrMsg:%s", pErrMsg);
        Logger.info("p3DesXmlPara:%s", Encrypt.decrypt3DES(p3DesXmlPara, Constants.ENCRYPTION_KEY));
        Logger.info("pSign:%s", pSign);
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.unfreezeInvestAmountCB(error);

        if (error.code < 0) {
            flash.error(error.msg);
        } else {
            flash.error("解冻投资金额成功");
        }

        PlatformAccountManager.ipsDetails(0, 0, "", "", 0, null, null, 0);
    }

    /**
     * 解冻投资金额回调（异步）(POST/WS)
     */
    public static void unfreezeInvestAmountCBSys(String pMerCode, String pErrCode, String pErrMsg, String p3DesXmlPara, String pSign) {
        Logger.info("解冻投资金额回调");
        Logger.info("pMerCode:%s", pMerCode);
        Logger.info("pErrCode:%s", pErrCode);
        Logger.info("pErrMsg:%s", pErrMsg);
        Logger.info("p3DesXmlPara:%s", Encrypt.decrypt3DES(p3DesXmlPara, Constants.ENCRYPTION_KEY));
        Logger.info("pSign:%s", pSign);
        ErrorInfo error = new ErrorInfo();

        Payment pay = new Payment();
        pay.pMerCode = pMerCode;
        pay.pErrCode = pErrCode;
        pay.pErrMsg = pErrMsg;
        pay.p3DesXmlPara = p3DesXmlPara;
        pay.pSign = pSign;
        pay.unfreezeInvestAmountCB(error);

        renderText("{\"code\":\"" + error.code + "\"}");
    }
}
