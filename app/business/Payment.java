package business;

import java.io.Serializable;
import java.util.*;

import javax.persistence.Query;

import models.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import utils.*;
import business.Bid.Purpose;

import com.google.gson.Gson;
import com.shove.Convert;
import com.shove.security.Encrypt;

import constants.Constants;
import constants.Constants.MerToUserType;
import constants.Constants.PayType;
import constants.IPSConstants;
import constants.IPSConstants.*;

/**
 * 资金托管
 *
 * @author lzp
 * @version 6.0
 * @created 2014-9-16
 */
public class Payment implements Serializable {
    public String pMerCode;
    public String pErrCode;
    public String pErrMsg;
    public String p3DesXmlPara;
    public JSONObject jsonPara;
    public String pSign;


    /*public JSONObject getJsonPara() {
        String xmlPara = Encrypt.decrypt3DES(this.p3DesXmlPara, Constants.ENCRYPTION_KEY);

        if (StringUtils.isBlank(xmlPara))
            return new JSONObject();

        if (xmlPara.substring(0, 1).equals("?")) {
            xmlPara = xmlPara.substring(1);
        }

        return (JSONObject) Converter.xmlToObj(xmlPara);
    }*/

    public boolean checkSign() {
        if (StringUtils.isBlank(pMerCode) || StringUtils.isBlank(pSign)) {
            return false;
        }

        if (StringUtils.isBlank(p3DesXmlPara)) {
            return pSign.equals(Encrypt.MD5(pMerCode + Constants.ENCRYPTION_KEY));
        }

        return pSign.equals(Encrypt.MD5(pMerCode + p3DesXmlPara + Constants.ENCRYPTION_KEY));
    }

    public static boolean checkSign(String src, String sign) {
        if (StringUtils.isBlank(src) || StringUtils.isBlank(sign)) {
            return false;
        }

        return sign.equals(Encrypt.MD5(src + Constants.ENCRYPTION_KEY));
    }

    /**
     * 生成流水号(最长20位)
     *
     * @param userId    (不能为负，系统行为：0)
     * @param operation
     * @return
     */
    public static String createBillNo(long userId, int operation) {
        t_sequences sequence = new t_sequences();
        sequence.save();

        return sequence.id + "";
    }

    /**
     * 开户
     *
     * @return
     */
    public static Map<String, String> createAcct(String clientConstant) {
        User user = User.currUser();

        String pMerBillNo = createBillNo(user.id, IPSOperation.CREATE_IPS_ACCT);

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", pMerBillNo);

        jsonObj.put("pIdentType", IPSConstants.INDENT_TYPE);
        jsonObj.put("pIdentNo", user.idNumber);
        jsonObj.put("pRealName", user.realityName);
        jsonObj.put("pMobileNo", user.mobile);
        jsonObj.put("pEmail", user.email);
        jsonObj.put("pSmDate", DateUtil.simple(new Date()));

        if (ParseClientUtil.H5.equals(clientConstant)) {
            jsonObj.put("pWebUrl", IPSH5Url.CREATE_IPS_ACCT);
            jsonObj.put("pS2SUrl", IPSH5Url.CREATE_IPS_ACCT_SYS);
        } else {
            jsonObj.put("pWebUrl", IPSWebUrl.CREATE_IPS_ACCT);
            jsonObj.put("pS2SUrl", IPSS2SUrl.CREATE_IPS_ACCT);
        }
        jsonObj.put("pMemo1", "pMemo1");
        jsonObj.put("pMemo2", "pMemo2");
        jsonObj.put("pMemo3", "pMemo3");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("userId", user.id);
        jsonObjExtra.put("tranIP", DataUtil.getIp());
        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", null, null, null);
        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", null, null, null);
        Logger.info("strXml:" + strXml);
        Logger.info("strXmlExtra:" + strXmlExtra);

        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);

        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("type", IPSOperation.CREATE_IPS_ACCT + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("memberId", user.id + "");
        map.put("memberName", user.name);
        map.put("argSign", argSign);

        return map;
    }

    /**
     * 开户回调
     *
     * @param userId
     * @param error
     */
    public void createAcctCB(ErrorInfo error) {
        error.clear();
        Logger.info("here is createAcctCB");
        Logger.info("jsonPara:" + jsonPara.toString());
        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";
            Logger.info("无效签名");
            return;
        }
        Logger.info("jsonPara2:" + jsonPara.toString());
        /*if (!"MG00000F".equals(this.pErrCode)) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return;
        }

        if (!"10".equals(this.jsonPara.getString("pStatus")) && !this.pErrCode.equals("MY00000F")) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return;
        }*/


        String ipsAcctNo = this.jsonPara.getString("ipsAcctNo");

        User user = new User();
        user.ipsAcctNo = ipsAcctNo;
        user.updateIpsAcctNo(this.jsonPara.getLong("pMemo1"), error);
    }

    /**
     * 标的登记
     *
     * @return
     */
    public static Map<String, String> registerSubject(String operation, Bid bid) {
        User user = bid.user;

        String pMerBillNo = bid.merBillNo;

        JSONObject jsonObj = new JSONObject();

        String cycleType = IPSConstants.MONTH_CYCLE_TYPE;
        int cycleValue = bid.period;
        String paymentType = IPSConstants.PAID_MONTH_EQUAL_PRINCIPAL_INTEREST;

        if (bid.periodUnit == Constants.YEAR) {
            cycleValue *= 12;
        }

        if (bid.periodUnit == Constants.DAY) {
            cycleType = IPSConstants.DAY_CYCLE_TYPE;
        }

        if (bid.repayment.id == Constants.PAID_MONTH_ONCE_REPAYMENT) {
            paymentType = IPSConstants.PAID_MONTH_ONCE_REPAYMENT;
        } else if (bid.repayment.id == Constants.PAID_MONTH_EQUAL_PRINCIPAL_INTEREST) {

        } else {
            paymentType = IPSConstants.OTHER_REPAYMENT;
        }

        jsonObj.put("pMerBillNo", pMerBillNo);
        jsonObj.put("pBidNo", bid.bidNo);
        jsonObj.put("pRegDate", DateUtil.simple(new Date()));
        jsonObj.put("pLendAmt", String.format("%.2f", bid.amount));
        jsonObj.put("pGuaranteesAmt", String.format("%.2f", bid.bail));
        jsonObj.put("pTrdLendRate", String.format("%.2f", bid.apr));
        jsonObj.put("pTrdCycleType", cycleType);
        jsonObj.put("pTrdCycleValue", cycleValue);
        jsonObj.put("pLendPurpose", bid.purpose.id);
        jsonObj.put("pRepayMode", paymentType);
        jsonObj.put("pOperationType", operation.equals(IPSConstants.BID_CREATE) ? IPSConstants.OPERATION_TYPE_1 : IPSConstants.OPERATION_TYPE_2);
        jsonObj.put("pLendFee", String.format("%.2f", bid.serviceFees));

        jsonObj.put("pAcctType", IPSConstants.INDENT_TYPE);
        jsonObj.put("pIdentNo", user.idNumber);
        jsonObj.put("pRealName", user.realityName);
        jsonObj.put("pIpsAcctNo", user.ipsAcctNo);

        jsonObj.put("pWebUrl", IPSWebUrl.REGISTER_SUBJECT);
        jsonObj.put("pS2SUrl", IPSS2SUrl.REGISTER_SUBJECT);
        jsonObj.put("pMemo1", "pMemo1");
        jsonObj.put("pMemo2", "pMemo2");
        jsonObj.put("pMemo3", operation);

        long memberId = 0;

        if (IPSConstants.BID_CANCEL_B.equals(operation) ||
                IPSConstants.BID_CANCEL_S.equals(operation) ||
                IPSConstants.BID_CANCEL_I.equals(operation) ||
                IPSConstants.BID_CANCEL_M.equals(operation)) {
            memberId = Supervisor.currSupervisor().id;
        } else {
            memberId = user.id;
        }

        List<Invest> invests = Invest.queryAllInvestUser(bid.id);
        StringBuffer info = new StringBuffer();
        JSONArray pDetails = new JSONArray();
        JSONObject pRow = null;
        double totalInvestAmount = 0;

        //标的结束需要的参数（如：国付宝）
        for (Invest invest : invests) {
            pRow = new JSONObject();
            pRow.put("ipsBillNo", invest.ipsBillNo);
            pRow.put("investUserId", invest.investUserId);
            pDetails.add(pRow);
            info.append(invest.ipsBillNo + ",");
            totalInvestAmount += invest.investAmount;
        }

        String investInfo = info.length() > 0 ? info.substring(0, info.length() - 1) : "";
        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", null, null, null);
        Logger.info(strXml);
        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("mobile", user.mobile);
        jsonObjExtra.put("serialNumber", createBillNo(user.id, IPSOperation.REGISTER_SUBJECT)); //国付宝
        jsonObjExtra.put("bidEndDate", bid.investPeriod);
        jsonObjExtra.put("freezeTrxId", bid.ipsBillNo);
        jsonObjExtra.put("serviceFee", bid.serviceFees);
        jsonObjExtra.put("investInfo", investInfo);
        jsonObjExtra.put("pDetails", pDetails);
        jsonObjExtra.put("totalInvestAmount", totalInvestAmount);//国付宝所需的已投资金额
        jsonObjExtra.put("tranIP", DataUtil.getIp());

        String argeXtraPara = Encrypt.encrypt3DES(Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", "pRow", null, null), Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", memberId + "");
        map.put("type", IPSOperation.REGISTER_SUBJECT + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        return map;
    }

    /**
     * 流标
     *
     * @param operation
     * @param bid
     * @param userId
     * @param error
     */
    public static boolean flow(String operation, Bid bid, long userId, ErrorInfo error) {
        t_users user = User.queryUser2ByUserId(userId, error);


        String pMerBillNo = bid.merBillNo;

        JSONObject jsonObj = new JSONObject();

        String cycleType = IPSConstants.MONTH_CYCLE_TYPE;
        int cycleValue = bid.period;
        String paymentType = IPSConstants.PAID_MONTH_EQUAL_PRINCIPAL_INTEREST;

        if (bid.periodUnit == Constants.YEAR) {
            cycleValue *= 12;
        }

        if (bid.periodUnit == Constants.DAY) {
            cycleType = IPSConstants.DAY_CYCLE_TYPE;
        }

        if (bid.repayment.id == Constants.PAID_MONTH_ONCE_REPAYMENT) {
            paymentType = IPSConstants.PAID_MONTH_ONCE_REPAYMENT;
        } else {
            paymentType = IPSConstants.OTHER_REPAYMENT;
        }

        List<Invest> invests = Invest.queryAllInvestUser(bid.id);
        //StringBuffer info = new StringBuffer(bid.ipsBillNo+",");
        StringBuffer info = new StringBuffer();
        for (Invest invest : invests) {
            info.append(invest.ipsBillNo + ",");
        }

        //info.substring(0, info.length()-1);
        String investInfo = info.length() > 0 ? info.substring(0, info.length() - 1) : "";

        jsonObj.put("pMerBillNo", pMerBillNo);
        jsonObj.put("pBidNo", bid.bidNo);
        jsonObj.put("pRegDate", DateUtil.simple(new Date()));
        jsonObj.put("pLendAmt", String.format("%.2f", bid.amount));
        jsonObj.put("pGuaranteesAmt", String.format("%.2f", bid.bail));
        jsonObj.put("pTrdLendRate", String.format("%.2f", bid.apr));
        jsonObj.put("pTrdCycleType", cycleType);
        jsonObj.put("pTrdCycleValue", cycleValue);
        jsonObj.put("pRepayMode", paymentType);
        jsonObj.put("pOperationType", operation.equals(IPSConstants.BID_CREATE) ? IPSConstants.OPERATION_TYPE_1 : IPSConstants.OPERATION_TYPE_2);
        jsonObj.put("pLendFee", String.format("%.2f", bid.serviceFees));

        jsonObj.put("pAcctType", IPSConstants.INDENT_TYPE);
        jsonObj.put("pIdentNo", user.id_number);
        jsonObj.put("pRealName", user.reality_name);
        jsonObj.put("pIpsAcctNo", user.ips_acct_no);

        jsonObj.put("pWebUrl", IPSWebUrl.REGISTER_SUBJECT);
        jsonObj.put("pS2SUrl", IPSS2SUrl.REGISTER_SUBJECT);
        jsonObj.put("pMemo1", "pMemo1");
        jsonObj.put("pMemo2", "pMemo2");
        jsonObj.put("pMemo3", operation);

        long memberId = 0;

        if (IPSConstants.BID_CANCEL_B.equals(operation) ||
                IPSConstants.BID_CANCEL_S.equals(operation) ||
                IPSConstants.BID_CANCEL_I.equals(operation) ||
                IPSConstants.BID_CANCEL_M.equals(operation)) {
            memberId = Supervisor.currSupervisor().id;
        } else {
            memberId = userId;
        }
        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", null, null, null);
        System.out.println(strXml);
        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("investInfo", investInfo);
        jsonObjExtra.put("tranIP", DataUtil.getIp());
        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", null, null, null);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";
        if ("2.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", memberId + "");
        map.put("type", IPSOperation.FLOW_BID + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("flow", IPSConstants.BID_FLOW);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);


        String strJson = WS.url(IPSConstants.ACTION).setParameters(map).get().getString();
        Logger.info(strJson);

        JSONObject result = JSONObject.fromObject(strJson);
        Logger.info("--------------流标校验开始---------------------");
        Logger.info(result.get("p3DesXmlPara").toString());
        if (!Payment.checkSign(result.get("pMerCode").toString() + result.get("pErrCode") + result.get("pErrMsg") + result.get("p3DesXmlPara"), result.get("pSign").toString())) {
            error.code = -1;
            error.msg = "sign校验失败";
            Logger.info("--------------流标校验失败---------------------");
            return false;
        }
        Logger.info("--------------流标校验成功---------------------");
        if (!("MG02503F".equals(result.get("pErrCode")) || "MG02505F".equals(result.get("pErrCode")))) {
            return true;
        }

        return true;

    }

    /**
     * 标的登记回调
     *
     * @param userId
     * @param error
     */
    public void registerSubjectCB(ErrorInfo error) {
        error.clear();

        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";

            return;
        }

        if (!"1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion) && !pErrCode.equals("MG00000F")) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return;
        }

        String operation = this.jsonPara.getString("pMemo3");
        String bidNo = this.jsonPara.getString("pBidNo");

        String info = IpsDetail.getIpsInfo(Long.parseLong(bidNo), error);

        Gson gson = new Gson();
        Map<String, String> map = gson.fromJson(info, Map.class);
        Bid bid = new Bid();
        bid.id = Long.parseLong(map.get("bidId"));

        //标的登记返回的编号如果不在下面的所列的判断里，代表标的登记失败
        if (IPSConstants.BID_CREATE.equals(operation)) { //标的新增（发标）
            if (!(pErrCode.equals("MG02500F") ||
                    pErrCode.equals("MG00000F") ||
                    pErrCode.equals("MG02501F") ||
                    pErrCode.equals("MG02047F") ||
                    pErrCode.equals("MG02504F"))) {
                error.code = -1;
                error.msg = this.pErrMsg;

                return;
            }

            t_bids tbid = null;

            try {
                tbid = t_bids.findById(bid.id);
            } catch (Exception e) {
                Logger.error(e.getMessage());
                error.code = -1;
                error.msg = "数据库异常";

                return;
            }

            //资金托管1.0版本，标的登记成功马上返回的是MG02500F，而2.0版本返回的是MG00000F
            if ("2.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
                bid.afterCreateBid(tbid, bidNo, error);
            }

            if (error.code == -5) {
                error.code = 0;

                return;
            }

            if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
                /* 2014-12-11 待验证功能 */
                if (this.pErrCode.equals("MG02500F")) { // 待验证
                    return;
                } else if (this.pErrCode.equals("MG02501F")) { // 已验证
                    bid.afterCreateBid(tbid, bidNo, error);

                    if (error.code == -5) {
                        error.code = 0;

                        return;
                    }
                } else if (this.pErrCode.equals("MG02047F")) { // 未验证
                    Logger.info("--------------------标的未验证回调---------------------");

                    int row = this.setStatusMSG3(tbid.id, tbid.status);

                    if (row < 1)
                        return;

                    //bid.relieveUserBailFund("手机未验证", error);

					/* 2015-2-27 针对环迅增加未验证退回投标奖励 */
                    bid.refundInvestBonus(error);

                    return;
                }
            }

            IpsDetail.updateStatus(tbid.mer_bill_no, Status.SUCCESS, error);

            if (error.code < 0) {
                return;
            }
        } else if (IPSConstants.BID_CANCEL.equals(operation)) { //审核中->撤销
            if (this.pErrCode.equals("MG02503F") || this.pErrCode.equals("MG02505F") || this.pErrCode.equals("MG00000F")) {
                bid.auditToRepealBC(error);
            }
        } else if (IPSConstants.BID_CANCEL_B.equals(operation)) { //提前借款->借款中不通过
            if (this.pErrCode.equals("MG02503F") || this.pErrCode.equals("MG02505F") || this.pErrCode.equals("MG00000F")) {
                bid.advanceLoanToPeviewNotThroughBC(error);
            }
        } else if (IPSConstants.BID_CANCEL_S.equals(operation)) { // 审核中->审核不通过
            if (this.pErrCode.equals("MG02503F") || this.pErrCode.equals("MG02505F") || this.pErrCode.equals("MG00000F")) {
                bid.auditToNotThroughBC(error);
            }
        } else if (IPSConstants.BID_CANCEL_I.equals(operation)) { //募集中->借款中不通过
            if (this.pErrCode.equals("MG02503F") || this.pErrCode.equals("MG02505F") || this.pErrCode.equals("MG00000F")) {
                bid.fundraiseToPeviewNotThroughBC(error);
            }
        } else if (IPSConstants.BID_CANCEL_M.equals(operation)) { //满标->放款不通过
            if (this.pErrCode.equals("MG02503F") || this.pErrCode.equals("MG02505F") || this.pErrCode.equals("MG00000F")) {
                bid.fundraiseToLoanNotThroughBC(error);
            }
        } else if (IPSConstants.BID_CANCEL_F.equals(operation)) { //提前借款->撤销
            if (this.pErrCode.equals("MG02503F") || this.pErrCode.equals("MG02505F") || this.pErrCode.equals("MG00000F")) {
                bid.advanceLoanToRepealBC(error);
            }
        } else if (IPSConstants.BID_CANCEL_N.equals(operation)) { //募集中->撤销
            if (this.pErrCode.equals("MG02503F") || this.pErrCode.equals("MG02505F") || this.pErrCode.equals("MG00000F")) {
                bid.fundraiseToRepealBC(error);
            }
        } else if (IPSConstants.BID_ADVANCE_LOAN.equals(operation)) { //提前借款->流标
            if (this.pErrCode.equals("MG02503F") || this.pErrCode.equals("MG02505F") || this.pErrCode.equals("MG00000F")) {
                bid.advanceLoanToFlowBC(error);
            }
        } else if (IPSConstants.BID_FUNDRAISE.equals(operation)) { //募集中->流标
            if (this.pErrCode.equals("MG02503F") || this.pErrCode.equals("MG02505F") || this.pErrCode.equals("MG00000F")) {
                bid.fundraiseToFlowBC(error);
            }
        }
    }

    /**
     * 未验证
     */
    public int setStatusMSG3(long id, int status) {
        String sql = "update t_bids set status = ? where id = ?";

        Query query = JPA.em().createQuery(sql);
        query.setParameter(1, Constants.BID_NOT_VERIFY);
        query.setParameter(2, id);

        try {
            return query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("发标用户未验证" + e.getMessage());

            return -1;
        }
    }

    /**
     * 登记债权人接口
     *
     * @return
     */
    public static Map<String, String> registerCreditor(String pMerBillNo, long userId, long bidId, int pRegType, double pTrdAmt, ErrorInfo error) {
        return registerCreditorCommon(pMerBillNo, userId, bidId, pRegType, pTrdAmt, error, ParseClientUtil.PC);
    }

    /**
     * 登记债权人接口
     *
     * @return
     */
    public static Map<String, String> registerCreditorCommon(String pMerBillNo, long userId, long bidId, int pRegType, double pTrdAmt, ErrorInfo error, String client) {
        error.clear();

        Bid bid = Bid.queryBidForInvest(bidId, error);

        if (error.code < 0) {
            return null;
        }

        String purpose = Purpose.queryPurpose(bid.purpose.id, error);

        if (error.code < 0) {
            return null;
        }

        User user = User.queryUserforIPS(userId, error);

        if (error.code < 0) {
            return null;
        }

        JSONObject memo = new JSONObject();
        memo.put("userId", userId);
        memo.put("bidId", bidId);
        memo.put("pTrdAmt", pTrdAmt);

        IpsDetail detail = new IpsDetail();
        detail.merBillNo = pMerBillNo;
        detail.userName = user.name;
        detail.time = new Date();
        detail.type = IPSOperation.REGISTER_CREDITOR;
        detail.status = Status.FAIL;
        detail.memo = memo.toString();
        detail.create(error);

        if (error.code < 0) {
            return null;
        }

        IpsDetail.addMerNo(pMerBillNo, error);

        if (error.code < 0) {
            return null;
        }

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", pMerBillNo);
        jsonObj.put("pMerDate", DateUtil.simple(new Date()));
        jsonObj.put("pBidNo", bid.bidNo);
        jsonObj.put("pContractNo", "pContractNo");
        jsonObj.put("pRegType", pRegType);
        jsonObj.put("pAuthNo", pRegType == 1 ? "" : user.ipsBidAuthNo);
        jsonObj.put("pAuthAmt", String.format("%.2f", pTrdAmt));
        jsonObj.put("pTrdAmt", String.format("%.2f", pTrdAmt));
        jsonObj.put("pFee", "0");
        jsonObj.put("pAcctType", IPSConstants.ACCT_TYPE);
        jsonObj.put("pIdentNo", user.idNumber);
        jsonObj.put("pRealName", user.realityName);
        jsonObj.put("pAccount", user.ipsAcctNo);
        jsonObj.put("pUse", purpose);
        if (ParseClientUtil.H5.equals(client)) {
            jsonObj.put("pWebUrl", IPSH5Url.REGISTER_CREDITOR);
            jsonObj.put("pS2SUrl", IPSH5Url.REGISTER_CREDITOR_SYS);
        } else {
            jsonObj.put("pWebUrl", IPSWebUrl.REGISTER_CREDITOR);
            jsonObj.put("pS2SUrl", IPSS2SUrl.REGISTER_CREDITOR);
        }
        jsonObj.put("pMemo1", "pMemo1");
        jsonObj.put("pMemo2", "pMemo2");
        jsonObj.put("pMemo3", "pMemo3");

        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", null, null, null);
        Logger.info(strXml);
        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();

        String isFull = "Y";
        double serviceFee = 0;

        if (bid.amount != bid.hasInvestedAmount + pTrdAmt) {
            isFull = "N";
            serviceFee = Arith.round(pTrdAmt / bid.amount * bid.serviceFees, 2);
        }

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("bidContractNo", User.queryIpsAcctNo(bid.userId, error));
        jsonObjExtra.put("loanerId", bid.userId);
        jsonObjExtra.put("mobile", user.mobile);
        jsonObjExtra.put("transferAmount", bid.amount);
        jsonObjExtra.put("maxTenderRate", String.format("%.2f", backstageSet.investmentFee / 100));
        jsonObjExtra.put("borrowerRate", String.format("%.2f", bid.apr / 100));
        jsonObjExtra.put("transferAmount", String.format("%.2f", bid.amount));
        jsonObjExtra.put("IsFreeze", "Y");
        jsonObjExtra.put("isFull", isFull);
        jsonObjExtra.put("serviceFee", String.format("%.2f", serviceFee));
        jsonObjExtra.put("pFTrdFee", String.format("%.2f", bid.serviceFees));
        jsonObjExtra.put("pWSUrl", IPSWSUrl.REGISTER_CREDITOR);
        jsonObjExtra.put("tranIP", DataUtil.getIp());

        //满标金额，双乾使用
        jsonObjExtra.put("fullAmount", bid.amount);
        Logger.info("--------------------------------我是扩展参数：" + jsonObjExtra + "---------------------------------");
        String argeXtraPara = Encrypt.encrypt3DES(Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", null, null, null), Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", userId + "");
        map.put("type", IPSOperation.REGISTER_CREDITOR + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("autoInvest", pRegType == 1 ? "" : "autoInvest");
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        return map;
    }

    /**
     * 登记债权人回调
     */
    public JSONObject registerCreditorCB(ErrorInfo error) {
        error.clear();

        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";

            return null;
        }

        if (!pErrCode.equals("MG00000F")) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return null;
        }

        JSONObject jsonObj = this.jsonPara;
        String pMerBillNo = jsonObj.getString("pMerBillNo");
        String pIpsBillNo = this.jsonPara.getString("pP2PBillNo");
        double pFee = jsonObj.getDouble("pFee");
        //用于解冻投资金额
        if (jsonObj.containsKey("pMemo2") && "N".equals(jsonObj.getString("pMemo2").trim())) {
            //修改常量值
            IPSConstants.IS_WS_UNFREEZE = false;
        }
        Logger.info("-----1pMerBillNo:%s", pMerBillNo);
        String info = IpsDetail.getIpsInfo(Long.parseLong(pMerBillNo), error);

        if (error.code < 0) {
            return null;
        }

        Gson gson = new Gson();
        Map<String, String> map = gson.fromJson(info, Map.class);

        if (map == null) {
            error.code = -1;
            error.msg = "投标失败";

            return null;
        }

        IpsDetail.updateMerNo(pMerBillNo, error);

        if (error.code < 0) {
            error.msg = "投标已成功";

            return null;
        }

        long userId = Convert.strToLong(map.get("userId") + "", -1);
        long bidId = Convert.strToLong(map.get("bidId") + "", -1);
        int investAmount = Convert.strToInt(map.get("investAmount") + "", -1);
        Map<String, String> bid = Invest.bidMap(bidId, error);

        if (error.code < 0) {
            return null;
        }

        t_users user1 = User.queryUserforInvest(userId, error);

        if (error.code < 0) {
            return null;
        }

        if (this.jsonPara.containsKey("tranAmt")) {
            investAmount = this.jsonPara.getInt("tranAmt");
            map.put("investAmount", investAmount + "");
        }

        //满标控制
        if (bid != null && !bid.isEmpty()) {
            double hasAmt = Convert.strToDouble(bid.get("has_invested_amount").toString(), 0);
            double amount = Convert.strToDouble(bid.get("amount").toString(), 0);

            if (((hasAmt + investAmount) > amount)) {
                String memo = pIpsBillNo;
                IpsDetail.updateStatusAndMemo(pMerBillNo, Status.UNFREEZING, memo, error);

                if (IPSConstants.IS_WS_UNFREEZE) {
                    String merBillNo = createBillNo(1, 1);
                    //用于防重复解冻
                    IpsDetail.addMerNo(merBillNo, error);

                    JSONObject json = new JSONObject();

                    json.put("pMerBillNo", merBillNo);
                    json.put("oldMerBillNo", pMerBillNo);
                    json.put("pP2PBillNo", pIpsBillNo);

                    error.msg = "投标失败,本次投资金额已超上限";
                    error.code = -10;

                    return json;
                }

                error.msg = "投标失败,本次投资金额已超上限";
                error.code = -3;

                return null;
            }
        }

        Invest.doInvest(user1, bid, investAmount, pMerBillNo, pFee, error);

        if (error.code < 0) {
            return null;
        }

        long rows = Invest.queryIsInvest(pMerBillNo, error);

        if (error.code < 0) {
            JPA.setRollbackOnly();
            return null;
        }

        if (rows <= 0) {
            JPA.setRollbackOnly();
            error.code = -1;
            error.msg = "投资记录未插入成功";

            return null;
        }

        IpsDetail.updateInvestMer(pMerBillNo, pIpsBillNo, error);

        if (error.code < 0) {
            JPA.setRollbackOnly();
            return null;
        }

        IpsDetail.updateStatus(pMerBillNo, Status.SUCCESS, error);

        if (error.code < 0) {
            JPA.setRollbackOnly();
            return null;
        }

//		IpsDetail.deleteIpsInfo(Long.parseLong(pMerBillNo), error);

        return null;
    }

    /**
     * 登记债权转让接口
     *
     * @return
     */
    public static Map<String, Object> registerCretansfer(String pMerBillNo, long bidUser, long fromUserId, long toUserId, String bidNo, String pCreMerBillNo, double pCretAmt, double pCretAmt2, double interest, double pPayAmt, double pFromFee, String orderDate, String printAmt) {
        ErrorInfo error = new ErrorInfo();
        User fromUser = new User();
        fromUser.id = fromUserId;

        User toUser = new User();
        toUser.id = toUserId;

        IpsDetail.addMerNo(pMerBillNo, error);

        if (error.code < 0) {
            return null;
        }

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", pMerBillNo);
        jsonObj.put("pMerDate", DateUtil.simple(new Date()));
        jsonObj.put("pBidNo", bidNo);
        jsonObj.put("pContractNo", "pContractNo");

        jsonObj.put("pFromAccountType", IPSConstants.ACCT_TYPE);
        jsonObj.put("pFromName", fromUser.realityName);
        jsonObj.put("pFromAccount", fromUser.ipsAcctNo);
        jsonObj.put("pFromIdentType", IPSConstants.INDENT_TYPE);
        jsonObj.put("pFromIdentNo", fromUser.idNumber);

        jsonObj.put("pToAccountType", IPSConstants.ACCT_TYPE);
        jsonObj.put("pToAccountName", toUser.realityName);
        jsonObj.put("pToAccount", toUser.ipsAcctNo);
        jsonObj.put("pToIdentType", IPSConstants.INDENT_TYPE);
        jsonObj.put("pToIdentNo", toUser.idNumber);

        jsonObj.put("pCreMerBillNo", pCreMerBillNo);
        jsonObj.put("pCretAmt", String.format("%.2f", pCretAmt));
        jsonObj.put("pPayAmt", String.format("%.2f", pPayAmt));
        jsonObj.put("pFromFee", String.format("%.2f", pFromFee));
        jsonObj.put("pToFee", 0);
        jsonObj.put("pCretType", 1);

        jsonObj.put("pWebUrl", IPSWebUrl.REGISTER_CRETANSFER);
        jsonObj.put("pS2SUrl", IPSS2SUrl.REGISTER_CRETANSFER);
        jsonObj.put("pMemo1", "pMemo1");
        jsonObj.put("pMemo2", "pMemo2");
        jsonObj.put("pMemo3", "pMemo3");

        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", null, null, null);

        Logger.info(strXml);

        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("pBidAccount", User.queryIpsAcctNo(bidUser, error));
        jsonObjExtra.put("orderDate", orderDate);
        jsonObjExtra.put("interest", String.format("%.2f", interest));
        jsonObjExtra.put("BorrowerCreditAmt", String.format("%.2f", pCretAmt2));
        jsonObjExtra.put("printAmt", printAmt);
        jsonObjExtra.put("tranIP", DataUtil.getIp());
        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", null, null, null);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", toUser.id + "");
        map.put("type", IPSOperation.REGISTER_CRETANSFER);
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        return map;
    }

    /**
     * 登记债权转让回调
     */
    public void registerCretansferCB(ErrorInfo error) {
        error.clear();

        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";

            return;
        }

        if (!"MG00000F".equals(this.pErrCode)) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return;
        }

        String pMerBillNo = this.jsonPara.getString("pMerBillNo");

        IpsDetail.updateMerNo(pMerBillNo, error);

        if (error.code < 0) {
            error.msg = "债权转让已成功";

            return;
        }

        IpsDetail.updateStatus(pMerBillNo, Status.SUCCESS, error);

        if (error.code < 0) {
            return;
        }

        String info = IpsDetail.getIpsInfo(Long.parseLong(pMerBillNo), error);
        long debtId = JSONObject.fromObject(info).getLong("debtId");

        String paymentMerBillNo = null;


        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {

            paymentMerBillNo = Payment.transferForCretransfer(pMerBillNo, debtId, error);

            if (error.code < 0) {
                return;
            }


        } else {
            paymentMerBillNo = pMerBillNo;
        }

        Debt.dealDebtTransfer(paymentMerBillNo, debtId, null, true, error);
    }

    /**
     * 自动投标签约
     *
     * @return
     */
    public static Map<String, String> autoNewSigning(String pValidType, int pValidDate, String pTrdCycleType, int pSTrdCycleValue,
                                                     int pETrdCycleValue, double pSAmtQuota, double pEAmtQuota, double pSIRQuota, double pEIRQuota) {
        User user = User.currUser();

        String pMerBillNo = createBillNo(user.id, IPSOperation.AUTO_NEW_SIGNING);

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", pMerBillNo);
        jsonObj.put("pSigningDate", DateUtil.simple(new Date()));
        jsonObj.put("pIdentNo", user.idNumber);
        jsonObj.put("pRealName", user.realityName);
        jsonObj.put("pIpsAcctNo", user.ipsAcctNo);

        jsonObj.put("pValidType", pValidType);
        jsonObj.put("pValidDate", pValidDate);
        jsonObj.put("pTrdCycleType", pTrdCycleType);
        jsonObj.put("pSTrdCycleValue", pSTrdCycleValue);
        jsonObj.put("pETrdCycleValue", pETrdCycleValue);
        jsonObj.put("pSAmtQuota", String.format("%.2f", pSAmtQuota));
        jsonObj.put("pEAmtQuota", String.format("%.2f", pEAmtQuota));
        jsonObj.put("pSIRQuota", String.format("%.2f", pSIRQuota));
        jsonObj.put("pEIRQuota", String.format("%.2f", pEIRQuota));

        jsonObj.put("pWebUrl", IPSWebUrl.AUTO_NEW_SIGNING);
        jsonObj.put("pS2SUrl", IPSS2SUrl.AUTO_NEW_SIGNING);
        jsonObj.put("pMemo1", "pMemo1");
        jsonObj.put("pMemo2", "pMemo2");
        jsonObj.put("pMemo3", "pMemo3");

        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", null, null, null);

        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("pMemo4", "pMemo4");
        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", null, null, null);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("type", IPSOperation.AUTO_NEW_SIGNING + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("memberId", user.id + "");
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        return map;
    }

    /**
     * 自动投标签约回调
     *
     * @param userId
     * @param error
     */
    public void autoNewSigningCB(ErrorInfo error) {
        error.clear();

        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";

            return;
        }

        if (!"MG00000F".equals(this.pErrCode)) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return;
        }

        String pIpsAuthNo = this.jsonPara.getString("pIpsAuthNo");

        User user = new User();
        user.ipsBidAuthNo = pIpsAuthNo;
        user.updateIpsBidAuthNo(this.jsonPara.getLong("pMemo1"), error);
    }

    /**
     * 自动还款签约
     *
     * @return
     */
    public static Map<String, String> repaymentSigning() {
        User user = User.currUser();

        String pMerBillNo = createBillNo(user.id, IPSOperation.REPAYMENT_SIGNING);

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", pMerBillNo);
        jsonObj.put("pSigningDate", DateUtil.simple(new Date()));
        jsonObj.put("pIdentType", IPSConstants.INDENT_TYPE);
        jsonObj.put("pIdentNo", user.idNumber);
        jsonObj.put("pRealName", user.realityName);
        jsonObj.put("pIpsAcctNo", user.ipsAcctNo);

        jsonObj.put("pValidType", IPSConstants.VALID_TYPE);
        jsonObj.put("pValidDate", IPSConstants.VALID_DATE);

        jsonObj.put("pWebUrl", IPSWebUrl.REPAYMENT_SIGNING);
        jsonObj.put("pS2SUrl", IPSS2SUrl.REPAYMENT_SIGNING);
        jsonObj.put("pMemo1", "pMemo1");
        jsonObj.put("pMemo2", "pMemo2");
        jsonObj.put("pMemo3", "pMemo3");

        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", null, null, null);

        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", null, null, null);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", user.id + "");
        map.put("type", IPSOperation.REPAYMENT_SIGNING + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        return map;
    }

    /**
     * 自动还款签约回调
     *
     * @param userId
     * @param error
     */
    public void repaymentSigningCB(ErrorInfo error) {
        error.clear();

        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";

            return;
        }

        if (!"MG00000F".equals(this.pErrCode)) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return;
        }

        String pIpsAuthNo = this.jsonPara.getString("pIpsAuthNo");

        User user = new User();
        user.ipsRepayAuthNo = pIpsAuthNo;
        user.updateIpsRepayAuthNo(this.jsonPara.getLong("pMemo1"), error);
    }

    /**
     * 充值
     *
     * @return
     */
    public static Map<String, String> doDpTrade(double pTrdAmt, String pTrdBnkCode, ErrorInfo error, String client) {
        User user = User.currUser();

        String pMerBillNo = createBillNo(user.id, IPSOperation.DO_DP_TRADE);

        IpsDetail detail = new IpsDetail();
        detail.merBillNo = pMerBillNo;
        detail.userName = user.name;
        detail.time = new Date();
        detail.type = IPSOperation.DO_DP_TRADE;
        detail.status = Status.FAIL;
        detail.create(error);

        if (error.code < 0) {
            return null;
        }

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", pMerBillNo);

        jsonObj.put("pAcctType", IPSConstants.ACCT_TYPE);
        jsonObj.put("pIdentNo", user.idNumber);
        jsonObj.put("pRealName", user.realityName);
        jsonObj.put("pIpsAcctNo", user.ipsAcctNo);

        jsonObj.put("pTrdDate", DateUtil.simple(new Date()));
        jsonObj.put("pTrdAmt", String.format("%.2f", pTrdAmt));
        jsonObj.put("pTrdBnkCode", pTrdBnkCode);
        jsonObj.put("pMerFee", "0");
        jsonObj.put("pIpsFeeType", IPSConstants.IPS_FEE_TYPE);

        if (ParseClientUtil.H5.equals(client)) {
            jsonObj.put("pChannelType", IPSConstants.CHANNEL_TYPE_MOBILE);
            jsonObj.put("pWebUrl", IPSH5Url.DO_DP_TRADE);
        } else if (ParseClientUtil.APP.equals(client)) {
            jsonObj.put("pChannelType", IPSConstants.CHANNEL_TYPE_MOBILE);
            jsonObj.put("pWebUrl", IPSWebUrl.DO_DP_TRADE);
        } else {
            jsonObj.put("pChannelType", IPSConstants.CHANNEL_TYPE);
            jsonObj.put("pWebUrl", IPSWebUrl.DO_DP_TRADE);
        }
        if (ParseClientUtil.H5.equals(client)) {
            jsonObj.put("pS2SUrl", IPSH5Url.DO_DP_TRADE_SYS);
        } else {
            jsonObj.put("pS2SUrl", IPSS2SUrl.DO_DP_TRADE);
        }
        jsonObj.put("pMemo1", "pMemo1");
        jsonObj.put("pMemo2", "pMemo2");
        jsonObj.put("pMemo3", "pMemo3");

        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", null, null, null);

        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("pWSUrl", IPSWSUrl.DO_DP_TRADE);
        jsonObjExtra.put("mobile", user.mobile);
        jsonObjExtra.put("tranIP", DataUtil.getIp());
        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", null, null, null);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", user.id + "");
        map.put("type", IPSOperation.DO_DP_TRADE + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        User.sequence(0, pMerBillNo, pTrdAmt, Constants.ENTRUST_RECHARGE, error);

        return map;
    }

    /**
     * 充值回调
     *
     * @param userId
     * @param error
     */
    public void doDpTradeCB(ErrorInfo error) {
        error.clear();

        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";

            return;
        }

        if (!"MG00000F".equals(this.pErrCode)) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return;
        }

        String pMerBillNo = this.jsonPara.getString("pMerBillNo");
        String amount = this.jsonPara.getString("pTrdAmt");

        User.recharge(pMerBillNo, Double.parseDouble(amount), error);

        if (error.code < 0) {
            return;
        }

        IpsDetail.updateStatus(pMerBillNo, Status.SUCCESS, error);
    }

    /**
     * 转账 (放款)
     *
     * @param pMerBillNo
     * @param bidId
     * @param error
     */
    public static Map<String, String> transfer(String pMerBillNo, long bidId, ErrorInfo error) {
        error.clear();

        Bid bid = new Bid();
        bid.auditBid = true;
        bid.id = bidId;

        double tFee = Arith.round(bid.serviceFees, 2); // 借款人手续费
        double sum = Arith.round(bid.amount, 2);
        double sumFee = 0;
        t_users tUser = null;

        try {
            tUser = t_users.findById(bid.userId);
        } catch (Exception e) {
            Logger.info(e.getMessage());
            error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
            error.code = -111;

            return null;
        }

        IpsDetail detail = new IpsDetail();
        detail.merBillNo = pMerBillNo;
        detail.userName = tUser.name;
        detail.time = new Date();
        detail.type = IPSOperation.TRANSFER_ONE;
        detail.status = Status.FAIL;
        detail.memo = "" + bidId;
        detail.create(error);

        if (error.code < 0) {
            return null;
        }

        List<Invest> invests = Invest.queryAllInvestUser(bid.id);
        JSONArray pDetails = new JSONArray();
        JSONArray pDetails2 = new JSONArray();
        StringBuffer bufPBillNos = new StringBuffer();

        for (int i = 0; i < invests.size(); i++) {
            Invest invest = invests.get(i);
            String pOriMerBillNo = invest.merBillNo;
            double pTrdAmt = invest.investAmount;
            t_users fUser = null;

            try {
                fUser = t_users.findById(invest.investUserId);
            } catch (Exception e) {
                Logger.info(e.getMessage());
                error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
                error.code = -111;

                return null;
            }

            double pTTrdFee = 0;
            double per = pTrdAmt / sum;

            if (i == invests.size() - 1) {
                pTTrdFee = Arith.round(tFee - sumFee, 2);
            } else {
                pTTrdFee = Arith.round(tFee * per, 2);
                sumFee += pTTrdFee;
            }
            Logger.info("pOriMerBillNo>>>", pOriMerBillNo);
            JSONObject pRow = new JSONObject();
            JSONObject pRow2 = new JSONObject();
            pRow.put("pOriMerBillNo", pOriMerBillNo);
            pRow.put("pTrdAmt", String.format("%.2f", pTrdAmt));
            pRow.put("pFAcctType", IPSConstants.ACCT_TYPE);
            pRow.put("pFIpsAcctNo", fUser.ips_acct_no);
            pRow.put("pFTrdFee", "0");
            pRow.put("pTAcctType", IPSConstants.ACCT_TYPE);
            pRow.put("pTIpsAcctNo", tUser.ips_acct_no);
            pRow.put("pTTrdFee", String.format("%.2f", pTTrdFee));
            pRow2.put("invseterId", invest.investUserId);
            pRow2.put("ipsBillNo", invest.ipsBillNo);
            pDetails.add(pRow);
            pDetails2.add(pRow2);

            bufPBillNos.append(invest.ipsBillNo + ",");
        }

        String pBillNos = bufPBillNos.toString().length() > 0 ? bufPBillNos.toString().substring(0, bufPBillNos.toString().length() - 1) : "";

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", pMerBillNo);
        jsonObj.put("pBidNo", bid.bidNo);
        jsonObj.put("pDate", DateUtil.simple(new Date()));
        jsonObj.put("pTransferType", TransferType.INVEST);
        jsonObj.put("pTransferMode", 1);
        jsonObj.put("pS2SUrl", IPSS2SUrl.TRANSFER);
        jsonObj.put("pDetails", pDetails);
        jsonObj.put("pMemo1", "pMemo1");
        jsonObj.put("pMemo2", "pMemo2");
        jsonObj.put("pMemo3", "pMemo3");

        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", "pRow", null, null);

        Logger.info(strXml);

        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("contractNo", User.queryIpsAcctNo(bid.userId, error));
        jsonObjExtra.put("pDetails", pDetails2);
        jsonObjExtra.put("pBillNos", pBillNos);
        jsonObjExtra.put("pBidBillNo", bid.ipsBillNo);
        jsonObjExtra.put("loanerId", bid.userId);
        jsonObjExtra.put("mobile", User.queryUserByUserId(bid.userId, error).mobile);
        jsonObjExtra.put("amount", bid.amount);
        jsonObjExtra.put("serviceFees", bid.serviceFees);
        jsonObjExtra.put("pWebUrl", IPSWebUrl.TRANSFER);
        jsonObjExtra.put("tranIP", DataUtil.getIp());
        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", "pRow2", null, null);
        Logger.info(strXmlExtra);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", Supervisor.currSupervisor().id + "");
        map.put("type", IPSOperation.TRANSFER + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        map.put("isWS", "Y");

        Logger.info("arg3DesXmlPara:%s", strXml);

        String strJson = WS.url(IPSConstants.ACTION).setParameters(map).post().getString();

        if (strJson.trim().endsWith("form_post")) {
            error.code = 100;
            error.msg = "ws转post提交";

            map.put("isWS", "N");
            map.put("SubmitURL", IPSConstants.ACTION);

            return map;
        }

        if (IPSConstants.IS_REPAIR_TEST) {
            error.code = -1;
            error.msg = "模拟转账 (放款)掉单";

            return null;
        }

        Logger.info("转账回调原始数据\n%s", strJson);

        if (IpsDetail.isMerNoExist(pMerBillNo, error)) {

            error.code = -1;
            error.msg = "放款已成功";

            return null;
        }

        JSONObject cbJsonObj = JSONObject.fromObject(strJson);

        Payment pay = new Payment();
        pay.pMerCode = cbJsonObj.getString("pMerCode");
        pay.pErrCode = cbJsonObj.getString("pErrCode");
        pay.pErrMsg = cbJsonObj.getString("pErrMsg");
        pay.p3DesXmlPara = cbJsonObj.getString("p3DesXmlPara");
        pay.pSign = cbJsonObj.getString("pSign");
        pay.transferCB(error);

        return null;
    }

    /**
     * 转账 (债权转让)
     *
     * @param pCreMerBillNo
     * @param debtId
     * @param error
     * @return
     */
    public static String transferForCretransfer(String pCreMerBillNo, long debtId, ErrorInfo error) {
        error.clear();
        Map<String, Object> debtInfo = Debt.queryTransferInfo(debtId, error);

        if (error.code < 0) {
            return null;
        }

        long fromUserId = (Long) debtInfo.get("toUserId");
        long toUserId = (Long) debtInfo.get("fromUserId");
        String bidNo = (String) debtInfo.get("bidNo");
        double pPayAmt = (Double) debtInfo.get("pPayAmt");
        double pTTrdFee = (Double) debtInfo.get("managefee");

        t_users fUser = null;//money转出方
        t_users tUser = null;//money转入方

        try {
            fUser = t_users.findById(fromUserId);
            tUser = t_users.findById(toUserId);
        } catch (Exception e) {
            Logger.info(e.getMessage());
            error.msg = "数据库异常";
            error.code = -1;

            return null;
        }

        String pMerBillNo = createBillNo(0, IPSOperation.TRANSFER);
        JSONArray pDetails = new JSONArray();

        JSONObject pRow = new JSONObject();
        pRow.put("pOriMerBillNo", pCreMerBillNo);
        pRow.put("pTrdAmt", String.format("%.2f", pPayAmt));
        pRow.put("pFAcctType", IPSConstants.ACCT_TYPE);
        pRow.put("pFIpsAcctNo", fUser.ips_acct_no);
        pRow.put("pFTrdFee", 0);
        pRow.put("pTAcctType", IPSConstants.ACCT_TYPE);
        pRow.put("pTIpsAcctNo", tUser.ips_acct_no);
        pRow.put("pTTrdFee", String.format("%.2f", pTTrdFee));
        pDetails.add(pRow);

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", pMerBillNo);
        jsonObj.put("pBidNo", bidNo);
        jsonObj.put("pDate", DateUtil.simple(new Date()));
        jsonObj.put("pTransferType", TransferType.CRETRANSFER);
        jsonObj.put("pTransferMode", 1);
        jsonObj.put("pS2SUrl", IPSS2SUrl.TRANSFER);
        jsonObj.put("pDetails", pDetails);
        jsonObj.put("pMemo1", "pMemo1");
        jsonObj.put("pMemo2", "pMemo2");
        jsonObj.put("pMemo3", "pMemo3");

        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", "pRow", null, null);

        Logger.info("转账(债权转让)接口输入参数\n" + strXml);

        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        String argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", fUser.id + "");
        map.put("type", IPSOperation.TRANSFER + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argSign", argSign);

        IpsDetail detail = new IpsDetail();
        detail.merBillNo = pMerBillNo;
        detail.userName = fUser.name;
        detail.time = new Date();
        detail.type = IPSOperation.TRANSFER_FOUR;
        detail.status = Status.FAIL;
        detail.memo = "" + debtId;
        detail.create(error);

        if (error.code < 0) {
            return null;
        }

        String strJson = WS.url(IPSConstants.ACTION).setParameters(map).get().getString();
        Logger.info("转账(债权转让)接口输出参数\n" + strJson);

        if (IPSConstants.IS_REPAIR_TEST) {
            error.code = -1;
            error.msg = "模拟转账 (债权转让)掉单";

            return null;
        }

        if (StringUtils.isBlank(strJson)) {
            error.code = -1;
            error.msg = "转账失败";

            return null;
        }

        JSONObject cbJsonObj = JSONObject.fromObject(strJson);

        Payment pay = new Payment();
        pay.pMerCode = cbJsonObj.getString("pMerCode");
        pay.pErrCode = cbJsonObj.getString("pErrCode");
        pay.pErrMsg = cbJsonObj.getString("pErrMsg");
        pay.p3DesXmlPara = cbJsonObj.getString("p3DesXmlPara");
        pay.pSign = cbJsonObj.getString("pSign");
        pay.transferCB(error);

        if (error.code < 0) {
            return null;
        }

        IpsDetail.updateStatus(jsonObj.getString("pMerBillNo"), Status.SUCCESS, error);

        if (error.code < 0) {
            return null;
        }

        return jsonObj.getString("pMerBillNo");
    }

    /**
     * 登记担保方
     *
     * @return
     */
    public static Map<String, String> registerGuarantor(long bidId, int type, ErrorInfo error) {
        String pMerBillNo = createBillNo(0, IPSOperation.CREATE_IPS_ACCT);

        IpsDetail.addMerNo(pMerBillNo, error);

        if (error.code < 0) {
            return null;
        }

        t_bids bid = null;

        try {
            bid = t_bids.findById(bidId);
        } catch (Exception e) {
            Logger.error(e.getMessage());
            JPA.setRollbackOnly();
            error.code = -1;
            error.msg = "数据库异常";

            return null;
        }

        if (bid == null) {
            error.code = -1;
            error.msg = "标的不存在";

            return null;
        }

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", pMerBillNo);
        jsonObj.put("pMerDate", DateUtil.simple(new Date()));
        jsonObj.put("pBidNo", bid.bid_no);
        jsonObj.put("pAmount", String.format("%.2f", 2 * bid.amount));
        jsonObj.put("pMarginAmt", String.format("%.2f", 0f));
        jsonObj.put("pProFitAmt", String.format("%.2f", 0f));
        jsonObj.put("pAcctType", IPSConstants.ACCT_AGENCY);
        jsonObj.put("pFromIdentNo", IPSConstants.GUARANTOR_CODE);
        jsonObj.put("pAccountName", IPSConstants.GUARANTOR_NAME);
        jsonObj.put("pAccount", IPSConstants.GUARANTOR_CODE);
        jsonObj.put("pWebUrl", IPSWebUrl.REGISTER_GUARANTOR);
        jsonObj.put("pS2SUrl", IPSS2SUrl.REGISTER_GUARANTOR);
        jsonObj.put("pMemo1", "pMemo1");
        jsonObj.put("pMemo2", "pMemo2");
        jsonObj.put("pMemo3", "" + type);

        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", null, null, null);
        Logger.info("登记担保方请求");
        Logger.info(strXml);

        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        String argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", Supervisor.currSupervisor().id + "");
        map.put("type", "" + IPSOperation.REGISTER_GUARANTOR);
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argSign", argSign);

        return map;
    }

    /**
     * 登记担保方回调
     *
     * @param error
     */
    public void registerGuarantorCB(ErrorInfo error) {
        error.clear();
        Logger.info("登记担保方回调");
        Logger.info(this.pMerCode + this.pErrCode + this.pErrMsg + this.jsonPara);
        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";

            return;
        }

        if (!pErrCode.equals("MG00000F")) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return;
        }

        String pMerBillNo = this.jsonPara.getString("pMerBillNo");

        IpsDetail.updateMerNo(pMerBillNo, error);

        if (error.code < 0) {
            error.msg = "登记担保方已成功";

            return;
        }

        String pBidNo = this.jsonPara.getString("pBidNo");
        String sql = "update t_bids set is_register_guarantor = 1 where bid_no = ?";
        int rows = 0;

        try {
            rows = JPA.em().createNativeQuery(sql).setParameter(1, pBidNo).executeUpdate();
        } catch (Exception e) {
            JPA.setRollbackOnly();
            Logger.info(e.getMessage());
            error.code = -1;
            error.msg = "数据库异常";

            return;
        }

        if (rows == 0) {
            JPA.setRollbackOnly();
            error.code = -1;
            error.msg = "数据未更新";

            return;
        }

        error.code = 0;
        error.msg = "登记担保方成功";
    }

    /**
     * 代偿(本金垫付 & 线下收款)
     *
     * @param pMerBillNo
     * @param billId
     * @param type
     * @param error
     */
    public static Map<String, String> compensate(String pMerBillNo, long billId, int type, ErrorInfo error) {
        error.clear();

        IpsDetail.addMerNo(pMerBillNo, error);

        if (error.code < 0) {
            return null;
        }

        Map<String, Object> bill = null;
        List<Map<String, Object>> investBills = null;
        String sqlBorrow = "select new Map(b.id as id, b.bid_id as bid_id, b.periods as periods, bid.bid_no as bid_no, bid.invest_rate as invest_rate) from t_bills as b, t_bids as bid where b.bid_id = bid.id and b.id = ? and b.status not in (?,?,?)";
        String sqlInvest = "select new Map(bi.id as id, bi.mer_bill_no as mer_bill_no, bi.receive_corpus as receive_corpus,bi.receive_interest as receive_interest, bi.overdue_fine as overdue_fine, u.ips_acct_no as ips_acct_no) from t_bill_invests as bi, t_users as u where bi.user_id = u.id and bi.bid_id = ? and bi.periods = ? and bi.status not in (?,?,?,?)";

        try {
            bill = t_bills.find(sqlBorrow, billId, Constants.NORMAL_REPAYMENT,
                    Constants.ADVANCE_PRINCIIPAL_REPAYMENT, Constants.OVERDUE_PATMENT).first();
            investBills = t_bill_invests.find(sqlInvest, bill.get("bid_id"), bill.get("periods"), Constants.FOR_DEBT_MARK, Constants.NORMAL_RECEIVABLES,
                    Constants.ADVANCE_PRINCIIPAL_RECEIVABLES, Constants.OVERDUE_RECEIVABLES).fetch();
        } catch (Exception e) {
            Logger.info(e.getMessage());
            JPA.setRollbackOnly();
            error.code = -1;
            error.msg = "数据库异常";

            return null;
        }

        double investRate = (Double) bill.get("invest_rate");
        JSONArray pDetails = new JSONArray();

        for (Map<String, Object> investBill : investBills) {
            double receiveCorpus = (Double) investBill.get("receive_corpus");
            double receiveInterest = (Double) investBill.get("receive_interest");
            double overdueFine = (Double) investBill.get("overdue_fine");
            double investFee = Arith.round(Arith.mul(receiveInterest, investRate) / 100, 2);
            double pTrdAmt = receiveCorpus + receiveInterest + overdueFine;

            JSONObject pRow = new JSONObject();
            pRow.put("pOriMerBillNo", investBill.get("mer_bill_no"));
            pRow.put("pTrdAmt", String.format("%.2f", pTrdAmt));
            pRow.put("pFAcctType", IPSConstants.ACCT_AGENCY);
            pRow.put("pFIpsAcctNo", IPSConstants.GUARANTOR_CODE);
            pRow.put("pFTrdFee", "0");
            pRow.put("pTAcctType", IPSConstants.ACCT_TYPE);
            pRow.put("pTIpsAcctNo", investBill.get("ips_acct_no"));
            pRow.put("pTTrdFee", String.format("%.2f", investFee));
            pDetails.add(pRow);
        }

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", pMerBillNo);
        jsonObj.put("pBidNo", bill.get("bid_no"));
        jsonObj.put("pDate", DateUtil.simple(new Date()));
        jsonObj.put("pTransferType", TransferType.COMPENSATE);
        jsonObj.put("pTransferMode", 1);

        if (type == CompensateType.COMPENSATE) {
            jsonObj.put("pS2SUrl", IPSS2SUrl.COMPENSATE);
        } else {
            jsonObj.put("pS2SUrl", IPSS2SUrl.OFFLINE_REPAYMENT);
        }

        jsonObj.put("pDetails", pDetails);
        jsonObj.put("pMemo1", Supervisor.currSupervisor().id);
        jsonObj.put("pMemo2", "pMemo2");
        jsonObj.put("pMemo3", "" + billId);

        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", "pRow", null, null);
        Logger.info("代偿(本金垫付)请求参数");
        Logger.info(strXml);

        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("periods", bill.get("periods"));

        if (type == CompensateType.COMPENSATE) {
            jsonObjExtra.put("pWebUrl", IPSWSUrl.COMPENSATE);
        } else {
            jsonObjExtra.put("pWebUrl", IPSWSUrl.OFFLINE_REPAYMENT);
        }

        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", "pRow2", null, null);
        Logger.info(strXmlExtra);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", Supervisor.currSupervisor().id + "");
        map.put("type", IPSOperation.TRANSFER + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        map.put("isWS", "Y");

        String strJson = WS.url(IPSConstants.ACTION).setParameters(map).post().getString();
        Logger.info("代偿(本金垫付)返回原始数据\n%s", strJson);

        if (StringUtils.isBlank(strJson)) {
            error.code = -1;
            error.msg = "本金垫付失败";

            return null;
        }

        if (strJson.trim().endsWith("form_post")) {
            error.code = 100;
            error.msg = "ws转post提交";

            map.put("isWS", "N");
            map.put("SubmitURL", IPSConstants.ACTION);

            return map;
        }

        JSONObject cbJsonObj = JSONObject.fromObject(strJson);

        Payment pay = new Payment();
        pay.pMerCode = cbJsonObj.getString("pMerCode");
        pay.pErrCode = cbJsonObj.getString("pErrCode");
        pay.pErrMsg = cbJsonObj.getString("pErrMsg");
        pay.p3DesXmlPara = cbJsonObj.getString("p3DesXmlPara");
        pay.pSign = cbJsonObj.getString("pSign");

        if (type == CompensateType.COMPENSATE) {
            pay.compensateCB(error);
        } else {
            pay.offlineRepaymentCB(error);
        }

        return null;
    }

    /**
     * 代偿还款
     *
     * @param pMerBillNo
     * @param billId
     * @param error
     */
    public static Map<String, String> compensateRepayment(String pMerBillNo, long billId, ErrorInfo error) {
        error.clear();

        Map<String, Object> bill = null;
        List<Map<String, Object>> investBills = null;
        String sqlBorrow = "select new Map(b.id as id, b.bid_id as bid_id, b.periods as periods, b.mer_bill_no as mer_bill_no, bid.bid_no as bid_no, u.ips_acct_no as ips_acct_no, b.repayment_corpus as repayment_corpus, b.repayment_interest as repayment_interest, b.overdue_fine as overdue_fine) from t_bills as b, t_bids as bid, t_users as u where b.bid_id = bid.id and bid.user_id = u.id and b.id = ?";
        String sqlInvest = "select new Map(bi.id as id, bi.receive_corpus as receive_corpus,bi.receive_interest as receive_interest, bi.overdue_fine as overdue_fine) from t_bill_invests as bi where bi.bid_id = ? and bi.periods = ?";

        try {
            bill = t_bills.find(sqlBorrow, billId).first();
            investBills = t_bill_invests.find(sqlInvest, bill.get("bid_id"), bill.get("periods")).fetch();
        } catch (Exception e) {
            Logger.info(e.getMessage());
            JPA.setRollbackOnly();
            error.code = -1;
            error.msg = "数据库异常";

            return null;
        }

        String pOriMerBillNo = (String) bill.get("mer_bill_no");
        String pFIpsAcctNo = (String) bill.get("ips_acct_no");
        JSONArray pDetails = new JSONArray();
        double pTrdAmt = 0;

        for (Map<String, Object> investBill : investBills) {
            double receiveCorpus = (Double) investBill.get("receive_corpus");
            double receiveInterest = (Double) investBill.get("receive_interest");
            double overdueFine = (Double) investBill.get("overdue_fine");
            double pRowTrdAmt = receiveCorpus + receiveInterest + overdueFine;
            pTrdAmt += pRowTrdAmt;
        }

        JSONObject pRow = new JSONObject();
        pRow.put("pOriMerBillNo", pOriMerBillNo);
        pRow.put("pTrdAmt", String.format("%.2f", pTrdAmt));
        pRow.put("pFAcctType", IPSConstants.ACCT_TYPE);
        pRow.put("pFIpsAcctNo", pFIpsAcctNo);
        pRow.put("pFTrdFee", "0");
        pRow.put("pTAcctType", IPSConstants.ACCT_AGENCY);
        pRow.put("pTIpsAcctNo", IPSConstants.GUARANTOR_CODE);
        pRow.put("pTTrdFee", "0");
        pDetails.add(pRow);

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", pMerBillNo);
        jsonObj.put("pBidNo", bill.get("bid_no"));
        jsonObj.put("pDate", DateUtil.simple(new Date()));
        jsonObj.put("pTransferType", TransferType.COMPENSATE_REPAYMENT);
        jsonObj.put("pTransferMode", 1);
        jsonObj.put("pS2SUrl", IPSS2SUrl.TRANSFER);
        jsonObj.put("pDetails", pDetails);
        jsonObj.put("pMemo1", User.currUser().id);
        jsonObj.put("pMemo2", "pMemo2");
        jsonObj.put("pMemo3", "" + billId);

        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", "pRow", null, null);
        Logger.info("代偿还款请求参数");
        Logger.info(strXml);

        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        double repayCorpus = (Double) bill.get("repayment_corpus");
        double repayInterest = (Double) bill.get("repayment_interest");
        double repayOverdueFine = (Double) bill.get("overdue_fine");
        double repayAmount = repayCorpus + repayInterest + repayOverdueFine;
        jsonObjExtra.put("amount", repayAmount + "");
        jsonObjExtra.put("pWebUrl", IPSWebUrl.TRANSFER);
        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", "pRow2", null, null);
        Logger.info(strXmlExtra);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", User.currUser().id + "");
        map.put("type", IPSOperation.TRANSFER + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        map.put("isWS", "Y");

        String strJson = WS.url(IPSConstants.ACTION).setParameters(map).post().getString();
        Logger.info("代偿还款返回原始数据\n%s", strJson);

        if (StringUtils.isBlank(strJson)) {
            error.code = -1;
            error.msg = "还款失败";

            return null;
        }

        if (strJson.trim().endsWith("form_post")) {
            error.code = 100;
            error.msg = "ws转post提交";

            map.put("isWS", "N");
            map.put("SubmitURL", IPSConstants.ACTION);

            return map;
        }

        JSONObject cbJsonObj = JSONObject.fromObject(strJson);

        Payment pay = new Payment();
        pay.pMerCode = cbJsonObj.getString("pMerCode");
        pay.pErrCode = cbJsonObj.getString("pErrCode");
        pay.pErrMsg = cbJsonObj.getString("pErrMsg");
        pay.p3DesXmlPara = cbJsonObj.getString("p3DesXmlPara");
        pay.pSign = cbJsonObj.getString("pSign");
        pay.transferCB(error);

        return null;
    }

    /**
     * 本金垫付回调
     */
    public void compensateCB(ErrorInfo error) {
        Logger.info("本金垫付回调");
        Logger.info(this.pErrCode);
        Logger.info(this.pErrMsg);
        Logger.info(this.jsonPara.toString());

        error.clear();

        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";

            return;
        }

        if (pErrCode == null || !(pErrCode.equals("MG00000F") || pErrCode.equals("MG00008F"))) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return;
        }

        String pMerBillNo = this.jsonPara.getString("pMerBillNo");

        IpsDetail.updateMerNo(pMerBillNo, error);

        if (error.code < 0) {
            error.msg = "本金垫付已成功";

            return;
        }

        long supervisorId = this.jsonPara.getLong("pMemo1");
        long billId = this.jsonPara.getLong("pMemo3");

        new Bill().principalAdvancePayment(supervisorId, billId, error);
    }

    /**
     * 线下收款回调
     */
    public void offlineRepaymentCB(ErrorInfo error) {
        Logger.info("线下收款回调");
        Logger.info(this.pErrCode);
        Logger.info(this.pErrMsg);
        Logger.info(this.jsonPara.toString());

        error.clear();

        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";

            return;
        }

        if (pErrCode == null || !(pErrCode.equals("MG00000F") || pErrCode.equals("MG00008F"))) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return;
        }

        String pMerBillNo = this.jsonPara.getString("pMerBillNo");

        IpsDetail.updateMerNo(pMerBillNo, error);

        if (error.code < 0) {
            error.msg = "线下收款已成功";

            return;
        }

        long supervisorId = this.jsonPara.getLong("pMemo1");
        long billId = this.jsonPara.getLong("pMemo3");

        Bill bill = new Bill();
        bill.id = billId;
        bill.offlineCollection(supervisorId, error);
    }

    /**
     * 转账回调
     */
    public void transferCB(ErrorInfo error) {
        error.clear();

        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";

            return;
        }
        Logger.info("this.jsonPara.getString(\"pMemo1\")" + this.jsonPara.getString("pMemo1"));
        if (pErrCode == null || !(pErrCode.equals("MG00000F") || pErrCode.equals("MG00008F"))) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return;
        }

        String xmlPara = Encrypt.decrypt3DES(this.p3DesXmlPara, Constants.ENCRYPTION_KEY);

        if (xmlPara.substring(0, 1).equals("?")) {
            xmlPara = xmlPara.substring(1);
        }

        JSONObject jsonObj = (JSONObject) Converter.xmlToObj(xmlPara);
        String pMerBillNo = jsonObj.getString("pMerBillNo");
        int pTransferType = jsonObj.getInt("pTransferType");

        /**
         * 投资(放款)
         */
        if (pTransferType == TransferType.INVEST) {
//			Bid bid = (Bid) Cache.get(pMerBillNo);
            String info = IpsDetail.getIpsInfo(Long.parseLong(pMerBillNo), error);

            Gson gson = new Gson();
            Map<String, String> map = gson.fromJson(info, Map.class);
            Bid bid = new Bid();
            bid.id = Long.parseLong(map.get("bidId"));

            if (bid.id < 0) {
                error.code = -1;
                error.msg = "放款失败";

                return;
            }

            //发放奖励方式配置为资金托管模式
            if (Constants.PAY_TYPE_FUND == PayType.IPS && bid.bonusType != Constants.NOT_REWARD) {
                return;
            }

            bid.doEaitLoanToRepayment(error);

            if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
                if (!guaranteeUnfreeze(bid, error)) {
                    return;
                }
            }

            IpsDetail.updateStatus(pMerBillNo, Status.SUCCESS, error);

            return;
        }

        /**
         * 代偿还款(本金垫付还款)
         */
        if (pTransferType == TransferType.COMPENSATE_REPAYMENT) {
            long userId = this.jsonPara.getLong("pMemo1");
            long billId = this.jsonPara.getLong("pMemo3");

            Bill bill = new Bill();
            bill.setId(billId);
            bill.repayment(userId, error);

            return;
        }

        /**
         * 债权转让
         */
        if (pTransferType == TransferType.CRETRANSFER) {
            error.code = 0;
            error.msg = "转账(债权转让)成功";

            return;
        }

        error.code = -1;
        error.msg = "转账方式未知";
    }

    /**
     * 自动还款
     *
     * @return
     */
    public static boolean autoRepaymentNewTrade(long userId, Map<String, List<Map<String, Object>>> mapList, long billId, ErrorInfo error) {
        User user = new User();
        user.id = userId;
        double investmentFee = Bid.queryRateByBillId(billId, 1);

        String pMerBillNo = createBillNo(user.id, IPSOperation.REPAYMENT_NEW_TRADE);

        IpsDetail.addMerNo(pMerBillNo, error);

        IpsDetail detail = new IpsDetail();
        detail.merBillNo = pMerBillNo;
        detail.userName = user.name;
        detail.time = new Date();
        detail.type = IPSOperation.REPAYMENT_NEW_TRADE;
        detail.status = Status.FAIL;
        detail.memo = "" + billId;
        detail.create(error);

        if (error.code < 0) {
            return false;
        }

        JSONArray pDetails = new JSONArray();

        List<Map<String, Object>> investRepayment = mapList.get("investRepayment");
        List<Map<String, Object>> bidRepayment = mapList.get("bidRepayment");
        Map<String, Object> repaymentMap = bidRepayment.get(0);
        JSONObject repaymentJson = JSONObject.fromObject(repaymentMap);
        Map<String, Object> perBillNo = bidRepayment.get(1);

        JSONObject pRow = null;
        double receiveCorpus = 0;
        double receiveInterest = 0;
        double manageFee = 0;
        double receive = 0;

//		int mark = (Integer) repaymentMap.get("overdue_mark");

        for (Map<String, Object> param : investRepayment) {
            pRow = new JSONObject();

            receiveCorpus = (Double) param.get("receive_corpus");//投资本金
            receiveInterest = (Double) param.get("receive_interest");//投资利息
            manageFee = Arith.round(Arith.mul(receiveInterest, investmentFee) / 100, 2);// 投资管理费
            receive = receiveCorpus + receiveInterest;//计算投资人将获得的收益
            Logger.info(param.get("merBillNo").toString().toString());
            pRow.put("pCreMerBillNo", param.get("merBillNo").toString());
            pRow.put("pInAcctNo", param.get("ipsAcctNo").toString());
            pRow.put("pInFee", String.format("%.2f", manageFee));
            pRow.put("pOutInfoFee", "0");
            pRow.put("pInAmt", String.format("%.2f", receive));

            pDetails.add(pRow);
        }

        JSONObject jsonObj = new JSONObject();

        jsonObj.put("pBidNo", repaymentMap.get("bid_no"));
        jsonObj.put("pRepaymentDate", DateUtil.simple(new Date()));
        jsonObj.put("pMerBillNo", pMerBillNo);

        jsonObj.put("pRepayType", IPSConstants.REPAYMENT_TYPE_2);
        jsonObj.put("pIpsAuthNo", user.ipsRepayAuthNo);
        jsonObj.put("pOutAcctNo", repaymentMap.get("ips_acct_no"));//double payment = repaymentCorpus + repaymentInterest + repayOverdueFine
        jsonObj.put("pOutAmt", String.format("%.2f", repaymentJson.getDouble("repayment_corpus") + repaymentJson.getDouble("repayment_interest") + repaymentJson.getDouble("overdue_fine")));
        jsonObj.put("pOutFee", "0");

        jsonObj.put("pWebUrl", IPSWebUrl.REPAYMENT_NEW_TRADE);
        jsonObj.put("pS2SUrl", IPSS2SUrl.REPAYMENT_NEW_TRADE);
        jsonObj.put("pDetails", pDetails);
        jsonObj.put("pMemo1", "pMemo1");
        jsonObj.put("pMemo2", "pMemo2");
        jsonObj.put("pMemo3", perBillNo.get("bidRepayment"));
        Logger.info("还款:" + jsonObj.toString());
        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", "pRow", null, null);
        Logger.info(strXml);
        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        //还款期数
        jsonObjExtra.put("period", repaymentMap.get("period"));

        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", null, null, null);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", user.id + "");
        map.put("type", IPSOperation.REPAYMENT_NEW_TRADE + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("autoPayment", IPSConstants.AUTO_PAYMENT);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        String strJson = "";
        try {
            WSRequest request = null;
            request = WS.url(IPSConstants.ACTION);
            request.timeout = 180;  //设置为180秒

            strJson = request.setParameters(map).post().getString();
        } catch (Exception e) {
            Logger.info("自动还款时：%s", e.getMessage());
        }

        if (StringUtils.isBlank(strJson)) {
            return false;
        }

        Logger.info(strJson);

        JSONObject result = JSONObject.fromObject(strJson);

        Payment payment = new Payment();
        payment.pMerCode = result.get("pMerCode").toString();
        payment.pErrCode = result.get("pErrCode").toString();
        payment.pErrMsg = result.get("pErrMsg").toString();
        payment.p3DesXmlPara = result.get("p3DesXmlPara").toString();
        payment.pSign = result.get("pSign").toString();

        Logger.info("--------------自动还款校验开始---------------------");
        Logger.info(result.get("p3DesXmlPara").toString());

        payment.repaymentNewTradeCB(error);

        if (error.code < 0) {
            return false;
        }

        return true;
    }

    /**
     * 还款
     *
     * @return
     */
    public static Map<String, String> repaymentNewTrade(Map<String, List<Map<String, Object>>> mapList, long billId, ErrorInfo error) {
        User user = User.currUser();
        double investmentFee = Bid.queryRateByBillId(billId, 1);
        String pMerBillNo = Bill.getRepaymentBillNo(error, billId);

        if (error.code < 0) {
            return null;
        }

        IpsDetail detail = new IpsDetail();
        detail.merBillNo = pMerBillNo;
        detail.userName = user.name;
        detail.time = new Date();
        detail.type = IPSOperation.REPAYMENT_NEW_TRADE;
        detail.status = Status.FAIL;
        detail.memo = "" + billId;
        detail.create(error);

        if (error.code < 0) {
            return null;
        }

        IpsDetail.addMerNo(pMerBillNo, error);

        if (error.code < 0) {
            return null;
        }

        JSONArray pDetails = new JSONArray();

        List<Map<String, Object>> investRepayment = mapList.get("investRepayment");
        List<Map<String, Object>> bidRepayment = mapList.get("bidRepayment");
        Map<String, Object> repaymentMap = bidRepayment.get(0);
        Map<String, Object> perBillNo = bidRepayment.get(1);

        JSONObject pRow = null;
        double receiveCorpus = 0;
        double receiveInterest = 0;
        double overdue_fine = 0;
        double manageFee = 0;
        double receive = 0;
        double totalManageFee = 0;
        StringBuffer investInfo = new StringBuffer();
//		int mark = (Integer) repaymentMap.get("overdue_mark");
        int i = 1;

        for (Map<String, Object> param : investRepayment) {
            pRow = new JSONObject();
            receiveCorpus = (Double) param.get("receive_corpus");//投资本金
            receiveInterest = (Double) param.get("receive_interest");//投资利息
            overdue_fine = (Double) param.get("overdue_fine");//逾期罚息
            manageFee = Arith.round(Arith.mul(receiveInterest, investmentFee) / 100, 2);// 投资管理费
            totalManageFee += manageFee;
            receive = receiveCorpus + receiveInterest + overdue_fine;//计算投资人将获得的收益
            Logger.info(param.get("merBillNo").toString().toString());
            pRow.put("pCreMerBillNo", param.get("merBillNo").toString());
            pRow.put("pInAcctNo", param.get("ipsAcctNo").toString());
            pRow.put("pInFee", String.format("%.2f", manageFee));
            pRow.put("pOutInfoFee", "0");
            pRow.put("pInAmt", String.format("%.2f", receive));
            investInfo.append(param.get("mobile") + ":" + String.format("%.2f", (receive - manageFee)) + ",");
            pDetails.add(pRow);
        }

        String info = investInfo.length() == 0 ? "" : investInfo.substring(0, investInfo.length() - 1);

        JSONObject jsonObj = new JSONObject();

        jsonObj.put("pBidNo", repaymentMap.get("bid_no"));
        jsonObj.put("pRepaymentDate", DateUtil.simple(new Date()));
        jsonObj.put("pMerBillNo", pMerBillNo);

        jsonObj.put("pRepayType", IPSConstants.REPAYMENT_TYPE);
        jsonObj.put("pIpsAuthNo", user.ipsRepayAuthNo);
        jsonObj.put("pOutAcctNo", repaymentMap.get("ips_acct_no"));//double payment = repaymentCorpus + repaymentInterest + repayOverdueFine
        double repaymentCorpus = Convert.strToDouble(repaymentMap.get("repayment_corpus") + "", 0);
        double repaymentInterest = Convert.strToDouble(repaymentMap.get("repayment_interest") + "", 0);
        double repayOverdueFine = Convert.strToDouble(repaymentMap.get("overdue_fine") + "", 0);
        jsonObj.put("pOutAmt", String.format("%.2f", repaymentCorpus + repaymentInterest + repayOverdueFine));
        jsonObj.put("pOutFee", "0");

        jsonObj.put("pWebUrl", IPSWebUrl.REPAYMENT_NEW_TRADE);
        jsonObj.put("pS2SUrl", IPSS2SUrl.REPAYMENT_NEW_TRADE);
        jsonObj.put("pDetails", pDetails);
        jsonObj.put("pMemo1", "pMemo1");
        jsonObj.put("pMemo2", "pMemo2");
        jsonObj.put("pMemo3", perBillNo.get("bidRepayment"));
        Logger.info("还款:" + jsonObj.toString());
        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", "pRow", null, null);
        Logger.info(strXml);
        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("mobile", user.mobile);
        jsonObjExtra.put("investInfo", info);
        jsonObjExtra.put("totalManageFee", String.format("%.2f", totalManageFee));
        jsonObjExtra.put("leftPayment", new Bill().isEndPayment(Integer.parseInt(repaymentMap.get("bid_id").toString()), error) + "");
        jsonObjExtra.put("pWSUrl", IPSWSUrl.REPAYMENT_NEW_TRADE);
        jsonObjExtra.put("tranIP", DataUtil.getIp());

        //还款期数
        jsonObjExtra.put("period", repaymentMap.get("period"));

        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", null, null, null);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", user.id + "");
        map.put("type", IPSOperation.REPAYMENT_NEW_TRADE + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        return map;
    }

    /**
     * 还款回调
     *
     * @param userId
     * @param error
     */
    public void repaymentNewTradeCB(ErrorInfo error) {
        error.clear();

        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";

            return;
        }

        if (!("MG00000F".equals(this.pErrCode) || "MG00008F".equals(this.pErrCode))) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return;
        }

        String pMerBillNo = this.jsonPara.getString("pMerBillNo");

        Logger.info("-------------还款:" + pMerBillNo + "-----------------");

        IpsDetail.updateMerNo(pMerBillNo, error);

        if (error.code < 0) {
            error.msg = "还款已成功";

            return;
        }

        String pepaymentCache = this.jsonPara.getString("pMemo3");

        String info = IpsDetail.getIpsInfo(Long.parseLong(pepaymentCache), error);


        JSONObject jsonObj = JSONObject.fromObject(info);
        JSONArray bidRepayment = jsonObj.getJSONArray("bidRepayment");
        JSONArray investRepayment = jsonObj.getJSONArray("investRepayment");

        JSONObject perBillNoMap = (JSONObject) bidRepayment.get(1);

        long userId = perBillNoMap.getLong("userId");
        long billId = perBillNoMap.getLong("billId");
        long bidId = perBillNoMap.getLong("bidId");
        double repaymentCorpus = perBillNoMap.getDouble("repaymentCorpus");
        double repaymentInterest = perBillNoMap.getDouble("repaymentInterest");
        double repayOverdueFine = perBillNoMap.getDouble("repayOverdueFine");
        double balance = perBillNoMap.getDouble("balance");
        int period = perBillNoMap.getInt("period");
        int mark = perBillNoMap.getInt("mark");
        int status = perBillNoMap.getInt("status");

        Bill bill = new Bill();
        bill.id = billId;

        if (mark == Constants.BILL_NO_OVERDUE) {
            bill.normalPayment(bidId, userId, repaymentCorpus, repaymentInterest, balance, investRepayment, period, error);
        } else {
            bill.overduePayment(bidId, userId, repaymentCorpus, repaymentInterest, balance, investRepayment, status, repayOverdueFine, period, error);
        }

        if (error.code >= 0) {
            Cache.delete("pepaymentCache_" + pepaymentCache);
            IpsDetail.updateStatus(this.jsonPara.getString("pMerBillNo"), Status.SUCCESS, error);
        }
    }

    /**
     * 解冻保证金
     *
     * @param bidId
     * @param error
     */
    public static boolean guaranteeUnfreeze(Bid bid, ErrorInfo error) {
        error.clear();
        String pMerBillNo = createBillNo(0, IPSOperation.GUARANTEE_UNFREEZE);
        User user = bid.user;

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", pMerBillNo);

        jsonObj.put("pBidNo", bid.bidNo);
        jsonObj.put("pUnfreezeDate", DateUtil.simple(new Date()));
        jsonObj.put("pUnfreezeAmt", String.format("%.2f", bid.bail));
        jsonObj.put("pUnfreezenType", IPSConstants.UNFREEZENT_YPE);

        jsonObj.put("pAcctType", IPSConstants.ACCT_TYPE);
        jsonObj.put("pIdentNo", user.idNumber);
        jsonObj.put("pRealName", user.realityName);
        jsonObj.put("pIpsAcctNo", user.ipsAcctNo);

        jsonObj.put("pS2SUrl", IPSS2SUrl.GUARANTEE_UNFREEZE);
        jsonObj.put("pMemo1", "pMemo1");
        jsonObj.put("pMemo2", "pMemo2");
        jsonObj.put("pMemo3", "pMemo3");

        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", null, null, null);
        Logger.info("解栋保证金请求参数\n" + strXml);
        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("trxId", DateUtil.simple(new Date()) + pMerBillNo.substring(pMerBillNo.length() > 8 ? pMerBillNo.length() - 8 : 0));
        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", null, null, null);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", user.id + "");
        map.put("type", IPSOperation.GUARANTEE_UNFREEZE + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        String strJson = WS.url(IPSConstants.ACTION).setParameters(map).get().getString();
        Logger.info("解冻保证金回调原始数据\n%s", strJson);

        if (strJson == null) {
            error.code = -1;
            error.msg = "解冻保证金失败";

            return false;
        }

        JSONObject cbJsonObj = JSONObject.fromObject(strJson);

        Payment pay = new Payment();
        pay.pMerCode = cbJsonObj.getString("pMerCode");
        pay.pErrCode = cbJsonObj.getString("pErrCode");
        pay.pErrMsg = cbJsonObj.getString("pErrMsg");
        pay.p3DesXmlPara = cbJsonObj.getString("p3DesXmlPara");
        pay.pSign = cbJsonObj.getString("pSign");

        return pay.guaranteeUnfreezeCB(error);
    }

    /**
     * 解冻保证金回调
     */
    public boolean guaranteeUnfreezeCB(ErrorInfo error) {
        error.clear();

        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";

            return false;
        }

        if (pErrCode == null || !pErrCode.equals("MG00000F")) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return false;
        }

        error.code = 0;

        return true;
    }

    /**
     * 提现
     *
     * @return
     */
    public static Map<String, String> doDwTrade(long withdrawalId, double pTrdAmt, ErrorInfo error, String client) {
        User user = User.currUser();
        String pMerBillNo = createBillNo(user.id, IPSOperation.DO_DW_TRADE);
        double pFee = Arith.round(withdrawalFee(pTrdAmt), 2);

        IpsDetail.addMerNo(pMerBillNo, error);

        if (error.code < 0) {
            return null;
        }

        IpsDetail detail = new IpsDetail();
        detail.merBillNo = pMerBillNo;
        detail.userName = user.name;
        detail.time = new Date();
        detail.type = IPSOperation.DO_DW_TRADE;
        detail.status = Status.FAIL;

        JSONObject memo = new JSONObject();
        memo.put("withdrawId", String.valueOf(withdrawalId));
        memo.put("serviceFee", String.valueOf(pFee));

        detail.memo = memo.toString();

        detail.create(error);

        if (error.code < 0) {
            return null;
        }

        Cache.set("doDwTrade" + pMerBillNo, pFee, "30min");

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", pMerBillNo);
        jsonObj.put("pAcctType", IPSConstants.ACCT_TYPE);
        jsonObj.put("pOutType", IPSConstants.OUT_TYPE);
        jsonObj.put("pBidNo", "");
        jsonObj.put("pContractNo", "");
        jsonObj.put("pDwTo", "");
        jsonObj.put("pIdentNo", user.idNumber);
        jsonObj.put("pRealName", user.realityName);
        jsonObj.put("pIpsAcctNo", user.ipsAcctNo);
        jsonObj.put("pDwDate", DateUtil.simple(new Date()));
        jsonObj.put("pTrdAmt", String.format("%.2f", pTrdAmt));
        jsonObj.put("pMerFee", String.format("%.2f", pFee));
        jsonObj.put("pIpsFeeType", IPSConstants.IPS_FEE_TYPE);
        if (ParseClientUtil.H5.equals(client)) {
            jsonObj.put("pWebUrl", IPSH5Url.DO_DW_TRADE);
            jsonObj.put("pS2SUrl", IPSH5Url.DO_DW_TRADE_SYS);
        } else {
            jsonObj.put("pWebUrl", IPSWebUrl.DO_DW_TRADE);
            jsonObj.put("pS2SUrl", IPSS2SUrl.DO_DW_TRADE);
        }
        jsonObj.put("pMemo1", "pMemo1");
        jsonObj.put("pMemo2", "pMemo2");
        jsonObj.put("pMemo3", withdrawalId + "");

        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", null, null, null);

        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("mobile", user.mobile);
        jsonObjExtra.put("pWSUrl", IPSWSUrl.DO_DW_TRADE);
        jsonObjExtra.put("tranIP", DataUtil.getIp());

        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", null, null, null);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", user.id + "");
        map.put("type", IPSOperation.DO_DW_TRADE + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        return map;
    }

    /**
     * 提现回调
     *
     * @param userId
     * @param error
     */
    public void doDwTradeCB(ErrorInfo error) {
        error.clear();

        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";

            return;
        }

        //MG00001F提现失败 MG00010F提现待审核 MG00020F提现回退（乾多多）
        if (!"MG00000F".equals(this.pErrCode) && !"MG00020F".equals(this.pErrCode) && !"MG00010F".equals(this.pErrCode) && !"MG00001F".equals(this.pErrCode)) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return;
        }

        String pMerBillNo = this.jsonPara.getString("pMerBillNo");
        long withdrawalId = this.jsonPara.getLong("pMemo3");

        try {
            JPAPlugin.closeTx(false);
            JPAPlugin.startTx(false);
            IpsDetail.updateMerNo(pMerBillNo, error);
        } catch (Exception e) {
            Logger.error("回调更新流水号：" + pMerBillNo + " 异常：" + e.getMessage());
        } finally {
            JPAPlugin.closeTx(false);
            JPAPlugin.startTx(false);
            Logger.info("回调更新流水号，正常关闭事物");
        }

        if (error.code < 0) {
            error.msg = "提现已成功";

            return;
        }

        double serviceFee = 0;
        if (this.jsonPara.containsKey("serviceFee")) {
            serviceFee = Double.parseDouble(this.jsonPara.getString("serviceFee").trim());
        } else {
            serviceFee = Double.parseDouble(Cache.get("doDwTrade" + pMerBillNo).toString());
        }

        if ("MG00020F".equals(this.pErrCode)) {
            User.rollbackWithdrawal(withdrawalId, error);

            return;
        }

        //国付宝审核中
        if ("MG00010F".equals(this.pErrCode)) {
            User.withdrawalNotice(this.jsonPara.getLong("pMemo1"), serviceFee, withdrawalId, "1", false, error);

            return;
        }

        //提现失败解冻保证金
        if ("MG00001F".equals(this.pErrCode)) {
            User.auditWithdrawalDispass(-1L, withdrawalId, "第三方支付平台提现失败", true, error);

            error.msg = this.pErrMsg;

            return;
        }

        User.withdrawalNotice(this.jsonPara.getLong("pMemo1"), serviceFee, withdrawalId, "1", true, error);

        if (error.code < 0) {
            return;
        }

        IpsDetail.updateStatus(pMerBillNo, Status.SUCCESS, error);
    }

    /**
     * 账户余额查询
     *
     * @return
     */
    public static JSONObject queryForAccBalance(String name, ErrorInfo error) {
        error.clear();
        User user = new User();
        user.name = name;

        String strJson = queryForAccBalanceFromIps(user);

        if (strJson == null) {
            error.code = -1;
            error.msg = "账户余额查询失败";

            return null;
        }

        JSONObject jsonObj = JSONObject.fromObject(strJson);
        jsonObj.put("balance", user.balanceDetail.user_amount);
        jsonObj.put("freeze", user.balanceDetail.freeze);
        jsonObj.put("balance2", user.balanceDetail.user_amount2);

        JSONObject obj = new JSONObject();
        obj.put("userName", user.name);
        obj.put("系统余额", user.balanceDetail.user_amount);
        obj.put("系统冻结", user.balanceDetail.freeze);
        obj.put("托管余额", jsonObj.get("pBalance"));
        obj.put("托管冻结", jsonObj.get("pLock"));

        return obj;
    }

    public static String queryForAccBalanceFromIps(User user) {
        JSONObject argJson = new JSONObject();
        argJson.put("argIpsAccount", user.ipsAcctNo);
        String arg3DesXmlPara = Converter.jsonToXml(argJson.toString(), "pReq", null, null, null);
        arg3DesXmlPara = Encrypt.encrypt3DES(arg3DesXmlPara, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("pMerBillNo", createBillNo(user.id, IPSOperation.QUERY_FOR_ACC_BALANCE));
        jsonObjExtra.put("mobile", user.mobile);
        jsonObjExtra.put("tranIP", DataUtil.getIp());
        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", null, null, null);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + user.ipsAcctNo + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", User.currUser().id + "");
        map.put("type", IPSOperation.QUERY_FOR_ACC_BALANCE + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("argIpsAccount", user.ipsAcctNo);
        map.put("argeXtraPara", argeXtraPara);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argSign", argSign);

        String strJson = WS.url(IPSConstants.ACTION).setParameters(map).get().getString();
        Logger.debug("查询账户余额信息：" + strJson);
        return strJson;
    }

    /**
     * 商户端获取银行列表查询
     *
     * @return
     */
    public static List<Map<String, Object>> getBankList(ErrorInfo error) {
        error.clear();
        String argSign = Encrypt.MD5(IPSConstants.MER_CODE + Constants.ENCRYPTION_KEY);

        Map<String, String> map = new HashMap<String, String>();
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", User.currUser().id + "");
        map.put("type", IPSOperation.GET_BANK_LIST + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("argSign", argSign);

        String strJson = WS.url(IPSConstants.ACTION).setParameters(map).get().getString();
        Logger.debug("查询银行列表信息：" + strJson);
        Logger.info(strJson);
        if (strJson == null) {
            error.code = -1;
            error.msg = "商户端获取银行列表查询失败";

            return null;
        }

        JSONObject jsonObj = JSONObject.fromObject(strJson);

//		String src = "<pMerCode>" + jsonObj.getString("pMerCode") + "</pMerCode>" +
//		"<pErrCode>" + jsonObj.getString("pErrCode") + "</pErrCode>" +
//		"<pErrMsg>" + jsonObj.getString("pErrMsg") + "</pErrMsg>" +
//		"<pBankList>" + jsonObj.getString("pBankList") + "</pBankList>";
//
//		String pSign = jsonObj.getString("pSign");
//
//		if (!checkSign(src, pSign)) {
//			error.code = -1;
//			error.msg = "签名失败";
//
//			return null;
//		}

        //<pBankList>银行名称|银行卡别名|银行卡编号#银行名称|银行卡别名|银行卡编号</pBankList>
        String pBankList = jsonObj.getString("pBankList");
        Logger.info(pBankList);
        if (pBankList == null) {
            error.code = -1;
            error.msg = "商户银行列表失败";

            return null;
        }

        String[] banks = pBankList.split("#");
        List<Map<String, Object>> bankList = new ArrayList<Map<String, Object>>();

        for (String strBank : banks) {
            String[] bankParams = strBank.split("\\|");

            if (bankParams.length < 3) {
                continue;
            }

            Map<String, Object> bank = new HashMap<String, Object>();
            bank.put("name", bankParams[0]);
            bank.put("alias", bankParams[1]);
            bank.put("code", bankParams[2]);

            bankList.add(bank);
        }

        return bankList;
    }

    /**
     * 账户信息查询
     *
     * @return
     */
    public static JSONObject queryMerUserInfo(String argIpsAccount, ErrorInfo error) {
        String argSign = Encrypt.MD5(IPSConstants.MER_CODE + argIpsAccount + Constants.ENCRYPTION_KEY);

        Map<String, String> map = new HashMap<String, String>();
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", User.currUser().id + "");
        map.put("type", IPSOperation.QUERY_MER_USER_INFO + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("argIpsAccount", argIpsAccount);
        map.put("argSign", argSign);
        map.put("argMemo", "argMemo");

        String strJson = WS.url(IPSConstants.ACTION).setParameters(map).get().getString();

        if (strJson == null) {
            error.code = -1;
            error.msg = "账户信息查询失败";

            return null;
        }

        JSONObject jsonObj = JSONObject.fromObject(strJson);

//		String src = "<pMerCode>" + jsonObj.getString("pMerCode") + "</pMerCode>" +
//		"<pIpsAcctNo>" + jsonObj.getString("pIpsAcctNo") + "</pIpsAcctNo>" +
//		"<pEmail>" + jsonObj.getString("pEmail") + "</pEmail>" +
//		"<pStatus>" + jsonObj.getString("pStatus") + "</pStatus>" +
//		"<pUCardStatus>" + jsonObj.getString("pUCardStatus") + "</pUCardStatus>" +
//		"<pBankName>" + jsonObj.getString("pBankName") + "</pBankName>" +
//		"<pBankCard>" + jsonObj.getString("pBankCard") + "</pBankCard>" +
//		"<pBCardStatus>" + jsonObj.getString("pBCardStatus") + "</pBCardStatus>" +
//		"<pSignStatus>" + jsonObj.getString("pSignStatus") + "</pSignStatus>";
//
//		String pSign = jsonObj.getString("pSign");
//
//		if (!checkSign(src, pSign)) {
//			error.code = -1;
//			error.msg = "签名失败";
//
//			return null;
//		}

        return jsonObj;
    }

    /**
     * 交易查询
     *
     * @param pMerBillNo
     * @param pTradeType
     * @param error
     * @return 1#成功、2#失败、3#处理中、4#未查询到交易
     */
    public static JSONObject queryIpsStatus(String pMerBillNo, int pTradeType, Date date, ErrorInfo error) {
        error.clear();

        String type = "";
        String investMenberNo = null;  //投资流水号

        switch (pTradeType) {
            case IPSOperation.REGISTER_SUBJECT:
                type = RepairOperation.REGISTER_SUBJECT;
                break;
            case IPSOperation.REGISTER_CREDITOR:
                type = RepairOperation.REGISTER_CREDITOR;
                break;
            case IPSOperation.REGISTER_CRETANSFER:
                type = RepairOperation.REGISTER_CRETANSFER;
                break;
            case IPSOperation.DO_DP_TRADE:
                type = RepairOperation.DO_DP_TRADE;
                break;
            case IPSOperation.TRANSFER_ONE:
                type = RepairOperation.TRANSFER_ONE;
                investMenberNo = IpsDetail.queryMemberId(pMerBillNo, error);
                break;
            case IPSOperation.TRANSFER_FOUR:
                type = RepairOperation.TRANSFER_FOUR;
                break;
            case IPSOperation.REPAYMENT_NEW_TRADE:
                type = RepairOperation.REPAYMENT_NEW_TRADE;
                break;
            case IPSOperation.DO_DW_TRADE:
                type = RepairOperation.DO_DW_TRADE;
                break;
            case IPSOperation.TRANSFER:
                type = RepairOperation.TRANSFER;
                break;
            default:
                break;
        }

        String merBillNos = "";

        if (pTradeType == IPSOperation.TRANSFER_ONE) {   //供乾多多使用
            merBillNos = IpsDetail.queryInvestBillNos(pMerBillNo, error);
        }

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", pMerBillNo);
        jsonObj.put("pTradeType", type);
//		jsonObj.put("pTradeType", new DecimalFormat("00").format(pTradeType));

        String arg3DesXmlPara = Converter.jsonToXml(jsonObj.toString(), "pReq", null, null, null);
        arg3DesXmlPara = Encrypt.encrypt3DES(arg3DesXmlPara, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("ordDate", DateUtil.simple(date));
        jsonObjExtra.put("merBillNos", merBillNos);
        jsonObjExtra.put("investMenberNo", investMenberNo);
        jsonObjExtra.put("BeginDate", DateUtil.simple(DateUtil.dateAddDay(date, -2)));
        jsonObjExtra.put("EndDate", DateUtil.simple(DateUtil.dateAddDay(date, 2)));
        jsonObjExtra.put("tranIP", DataUtil.getIp());

        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", null, null, null);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("type", IPSOperation.QUERY_TRADE + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        String strJson = WS.url(IPSConstants.ACTION).setParameters(map).get().getString();

        if (StringUtils.isBlank(strJson)) {
            error.code = -1;
            error.msg = "交易查询失败";

            return jsonObj;
        }

        jsonObj = JSONObject.fromObject(strJson);
        String pErrCode = jsonObj.getString("pErrCode");
        String pErrMsg = jsonObj.getString("pErrMsg");

        if (!pErrCode.equals("MG00000F")) {
            error.code = -1;
            error.msg = pErrMsg;

            return jsonObj;
        }

        String p3DesXmlPara = Encrypt.decrypt3DES(jsonObj.getString("p3DesXmlPara"), Constants.ENCRYPTION_KEY);
        jsonObj = (JSONObject) Converter.xmlToObj(p3DesXmlPara);

        error.code = 0;

        return jsonObj;
    }

    /**
     * 登陆
     */
    public static Map<String, String> loginAccount() {
        User user = User.currUser();

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("contractNo", user.ipsAcctNo);
        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", null, null, null);
        Logger.info(strXml);
        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("contractNo", user.ipsAcctNo);
        jsonObjExtra.put("mobilePhone", user.mobile);
        jsonObjExtra.put("tranIP", DataUtil.getIp());

        String argeXtraPara = Encrypt.encrypt3DES(Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", null, null, null), Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", user.id + "");
        map.put("type", IPSOperation.LOGIN + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        return map;
    }

    /**
     * 提现服务费
     *
     * @param pTrdAmt
     * @return
     */
    public static double withdrawalFee(double pTrdAmt) {
        BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();

        return (pTrdAmt - backstageSet.withdrawFee) > 0 ? Arith.round(((pTrdAmt - backstageSet.withdrawFee) * backstageSet.withdrawRate) / 100, 2) : 0;
    }

    /**
     * 转账-用户转商户
     */
    public static Map<String, String> transferUserToMer(User user, double amount, ErrorInfo error) {
        error.clear();
        String pMerBillNo = createBillNo(user.id, IPSOperation.TRANSFER_USER_TO_MER);

        IpsDetail.addMerNo(pMerBillNo, error);

        if (error.code < 0) {
            return null;
        }

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", pMerBillNo);
        jsonObj.put("pMemo1", user.id);
        jsonObj.put("pWebUrl", IPSWebUrl.TRANSFER_USER_TO_MER);
        jsonObj.put("pS2SUrl", IPSS2SUrl.TRANSFER_USER_TO_MER);

        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", null, null, null);
        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("TransAmt", String.format("%.2f", amount));
        jsonObjExtra.put("UsrCustId", user.ipsAcctNo);

        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", null, null, null);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", user.id + "");
        map.put("type", IPSOperation.TRANSFER_USER_TO_MER + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        return map;
    }

    /**
     * 转账-用户转商户回调
     */
    public void transferUserToMerCB(ErrorInfo error) {
        error.clear();

        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";

            return;
        }

        if (pErrCode == null || !pErrCode.equals("MG00000F")) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return;
        }

        String pMerBillNo = this.jsonPara.getString("pMerBillNo");

        IpsDetail.updateMerNo(pMerBillNo, error);

        if (error.code < 0) {
            error.msg = "用户转商户已成功";

            return;
        }
    }

    /**
     * 转账(单笔-用于发放cps费用)-商户转用户(WS)
     */
    public static void transferMerToUser(long userId, double amount, ErrorInfo error) {
        error.clear();

        String pMerBillNo = createBillNo(userId, IPSOperation.TRANSFER_MER_TO_USER_SINGLE);

        IpsDetail.addMerNo(pMerBillNo, error);

        if (error.code < 0) {
            return;
        }

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", pMerBillNo);
        jsonObj.put("pMemo1", "transferMerToUser");
        jsonObj.put("pWebUrl", "pWebUrl");
        jsonObj.put("pS2SUrl", "pS2SUrl");

        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", null, null, null);
        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("transAmt", String.format("%.2f", amount));
        jsonObjExtra.put("inCustId", User.queryIpsAcctNo(userId, error));

        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pExtra", null, null, null);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", "" + userId);
        map.put("type", IPSOperation.TRANSFER_MER_TO_USER_SINGLE + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        Logger.info("转账-商户转用户请求参数");
        Logger.info("arg3DesXmlPara:\n%s", strXml);
        Logger.info("strXmlExtra:\n%s", strXmlExtra);

        String strJson = WS.url(IPSConstants.ACTION).setParameters(map).post().getString();
        Logger.info("转账-商户转用户回调原始数据\n%s", strJson);

        if (StringUtils.isBlank(strJson)) {
            error.code = -1;
            error.msg = "转账-商户转用户失败";

            return;
        }

        IpsDetail.updateMerNo(pMerBillNo, error);

        if (error.code < 0) {
            error.msg = "商户转用户已成功";

            return;
        }

        JSONObject cbJsonObj = JSONObject.fromObject(strJson);

        Payment pay = new Payment();
        pay.pMerCode = cbJsonObj.getString("pMerCode");
        pay.pErrCode = cbJsonObj.getString("pErrCode");
        pay.pErrMsg = cbJsonObj.getString("pErrMsg");
        pay.p3DesXmlPara = cbJsonObj.getString("p3DesXmlPara");
        pay.pSign = cbJsonObj.getString("pSign");
        pay.transferMerToUserSingleCB(error);
    }

    public void transferMerToUserSingleCB(ErrorInfo error) {
        error.clear();
        Logger.info("转账-商户转用户xml\n%s", this.jsonPara.toString());

        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";

            return;
        }

        if (pErrCode == null || !pErrCode.equals("MG00000F")) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return;
        }
    }

    /**
     * 转账-商户转用户(POST)
     */
    public static Map<String, String> transferMerToUser(List<Map<String, Object>> pDetails, JSONObject memo) {
        String pMerBillNo = createBillNo(0, IPSOperation.TRANSFER_MER_TO_USER);
        ErrorInfo error = new ErrorInfo();
        IpsDetail.addMerNo(pMerBillNo, error);

        if (error.code < 0) {
            return null;
        }

        IpsDetail.setIpsInfo(Long.parseLong(pMerBillNo), memo.toString(), error);

        if (error.code < 0) {
            return null;
        }

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", pMerBillNo);
        jsonObj.put("pMemo1", pMerBillNo);
        jsonObj.put("pWebUrl", IPSWebUrl.TRANSFER_MER_TO_USER);
        jsonObj.put("pS2SUrl", IPSS2SUrl.TRANSFER_MER_TO_USER);

        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", null, null, null);
        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("pDetails", pDetails);
        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pReq", "pRow", null, null);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("2.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", "0");
        map.put("type", IPSOperation.TRANSFER_MER_TO_USER + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        Logger.info("转账-商户转用户请求参数");
        Logger.info("arg3DesXmlPara:\n%s", strXml);
        Logger.info("strXmlExtra:\n%s", strXmlExtra);

        return map;
    }

    /**
     * 转账-商户转用户回调
     */
    public void transferMerToUserCB(ErrorInfo error) {
        error.clear();
        Logger.info("转账-商户转用户3des\n%s", this.jsonPara.toString());

        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";

            return;
        }

        if (pErrCode == null || !pErrCode.equals("MG00000F")) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return;
        }

        String pMerBillNo = this.jsonPara.getString("pMerBillNo");

        IpsDetail.updateMerNo(pMerBillNo, error);

        if (error.code < 0) {
            error.msg = "商户转用户已成功";

            return;
        }

        JSONObject memo = JSONObject.fromObject(IpsDetail.getIpsInfo(Long.parseLong(pMerBillNo), error));
        int pPayType = memo.getInt("pPayType");

        if (MerToUserType.Fund == pPayType) {
            String pOriMerBillNo = memo.getString("pMerBillNo");
            String info = IpsDetail.getIpsInfo(Long.parseLong(pOriMerBillNo), error);

            Gson gson = new Gson();
            Map<String, String> map = gson.fromJson(info, Map.class);
            Bid bid = new Bid();
            bid.id = Long.parseLong(map.get("bidId"));
//			Bid bid = (Bid) Cache.get(pOriMerBillNo);

            if (bid.id < 0) {
                error.code = -1;
                error.msg = "放款失败";

                return;
            }

            bid.doEaitLoanToRepayment(error);

            IpsDetail.updateStatus(pOriMerBillNo, Status.SUCCESS, error);
        }

        if (MerToUserType.ItemFefund == pPayType) {
            UserAuditItem item = new UserAuditItem();
            item.lazy = true;
            item.userId = memo.getLong("userId");
            item.mark = memo.getString("mark");
            item.isRefund = true;
            item.audit(memo.getLong("supervisorId"), memo.getInt("status"), memo.getBoolean("isVisible"), memo.getString("suggestion"), error);
        }
    }

    /**
     * 解冻投资金额(同步)
     */
    public static Map<String, String> unfreezeInvestAmount(String merBillNo, String ipsBillNo, boolean isWS, ErrorInfo error) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pMerBillNo", merBillNo);
        jsonObj.put("pP2PBillNo", ipsBillNo);
        jsonObj.put("pWebUrl", IPSWebUrl.UNFREEZE_INVEST_AMOUNT);
        jsonObj.put("pS2SUrl", IPSS2SUrl.UNFREEZE_INVEST_AMOUNT);

        String strXml = Converter.jsonToXml(jsonObj.toString(), "pReq", null, null, null);
        String arg3DesXmlPara = Encrypt.encrypt3DES(strXml, Constants.ENCRYPTION_KEY);
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\r", "");
        arg3DesXmlPara = arg3DesXmlPara.replaceAll("\n", "");

        JSONObject jsonObjExtra = new JSONObject();
        jsonObjExtra.put("nothing", "nothing");
        String strXmlExtra = Converter.jsonToXml(jsonObjExtra.toString(), "pReq", "pRow", null, null);
        String argeXtraPara = Encrypt.encrypt3DES(strXmlExtra, Constants.ENCRYPTION_KEY);
        String argSign = "";

        if ("2.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + argeXtraPara + Constants.ENCRYPTION_KEY);
        } else {
            argSign = Encrypt.MD5(IPSConstants.MER_CODE + arg3DesXmlPara + Constants.ENCRYPTION_KEY);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("action", IPSConstants.ACTION);
        map.put("domain", IPSConstants.DOMAIN);
        map.put("platform", IPSConstants.PLATFORM);
        map.put("memberId", "0");
        map.put("type", (isWS ? IPSOperation.UNFREEZE_INVEST_AMOUNT_WS : IPSOperation.UNFREEZE_INVEST_AMOUNT) + "");
        map.put("version", BackstageSet.getCurrentBackstageSet().entrustVersion);
        map.put("argMerCode", IPSConstants.MER_CODE);
        map.put("arg3DesXmlPara", arg3DesXmlPara);
        map.put("argeXtraPara", argeXtraPara);
        map.put("argSign", argSign);

        Logger.info("解冻投资金额请求参数");
        Logger.info("arg3DesXmlPara:\n%s", strXml);

        if (isWS) {
            String result = doWSQueryCmd(IPSConstants.ACTION, map);  //ws请求
            if (StringUtils.isNotBlank(result)) {
                Payment payment = new Payment();
                payment.unfreezeInvestAmountCBForWS(result, error);
                if (error.code < 1) {
                    //解冻失败，交给管理员到补单页面进行处理
                    String memo = ipsBillNo;
                    IpsDetail.updateStatusAndMemo(merBillNo, Status.UNFREEZING, memo, error);

                    error.code = -1;
                    error.msg = "解冻失败";
                    return map;
                }
            } else {
                //解冻失败，交给管理员到补单页面进行处理
                String memo = ipsBillNo;
                IpsDetail.updateStatusAndMemo(merBillNo, Status.UNFREEZING, memo, error);

                error.code = -1;
                error.msg = "解冻失败";
                return map;
            }
        }

        return map;
    }

    /**
     * 解冻投资金额回调(post/ws)
     */
    public void unfreezeInvestAmountCB(ErrorInfo error) {
        error.clear();
        Logger.info("解冻投资金额回调3des\n%s", this.jsonPara.toString());

        if (!Payment.checkSign(this.pMerCode + this.pErrCode + this.pErrMsg + this.p3DesXmlPara, this.pSign)) {
            error.code = -1;
            error.msg = "sign校验失败";

            return;
        }

        if (pErrCode == null || !pErrCode.equals("MG00000F")) {
            error.code = IPSConstants.FAIL_CODE;
            error.msg = this.pErrMsg;

            return;
        }

        String pMerBillNo = this.jsonPara.getString("pMerBillNo");

        //本地解冻
        Invest.unfreezeInvest(pMerBillNo, error);
        if (error.code < 0) {
            return;
        }

        //处理补单状态
        IpsDetail.updateStatusAndMemo(pMerBillNo, Status.UNFREEZED, "解冻成功", error);

        //防重复解冻
        IpsDetail.updateMerNo(pMerBillNo, error);
        if (error.code < 1) {
            return;
        }

        error.code = 1;
        error.msg = "解冻投资金额成功";
    }

    /**
     * 发送ws请求
     *
     * @param strURL
     * @param req
     * @return
     */
    public static String doWSQueryCmd(String strURL, Map<String, String> req) {
        String result = "";
        int status = 0;

        HttpResponse response = null;
        try {
            WSRequest request = WS.url(strURL);
            response = request.setParameters(req).get();
        } catch (Exception e) {
            Logger.info("ws请求时：%s", e.getMessage());
        }

        if (response != null) {
            status = response.getStatus();
            result = response.getString();
        }

        Logger.info("======WS请求结果：========");
        Logger.info("status：%s", status);
        Logger.info("result：%s", result);

        return result;
    }

    /**
     * 解冻投资金额，处理WS返回结果
     *
     * @param result
     * @param error
     */
    private void unfreezeInvestAmountCBForWS(String result, ErrorInfo error) {

        JSONObject jsonObj = null;
        try {
            jsonObj = JSONObject.fromObject(result);
        } catch (Exception e) {
            Logger.error("解析json时：%s", e.getMessage());
            error.code = -1;
            error.msg = "解析json异常";
            return;
        }

        this.pMerCode = jsonObj.getString("pMerCode");
        this.pErrCode = jsonObj.getString("pErrCode");
        this.pErrMsg = jsonObj.getString("pErrMsg");
        this.p3DesXmlPara = jsonObj.getString("p3DesXmlPara");
        this.pSign = jsonObj.getString("pSign");
        this.unfreezeInvestAmountCB(error);
        if (error.code < 1) {
            return;
        }

        error.code = 1;  //成功
    }

    /**
     * 打印回调的参数
     */
    public void print() {
        Logger.info("--------------:pErrCode" + this.pErrCode);
        Logger.info("--------------:pErrMsg" + this.pErrMsg);
        Logger.info("--------------:jsonPara" + this.jsonPara);
        Logger.info("--------------:pSign" + this.pSign);
    }
}
