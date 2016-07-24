package controllers.front.account;

import business.*;
import business.Optimization.UserOZ;
import com.shove.Convert;
import constants.Constants;
import constants.Constants.PayType;
import constants.Constants.RechargeType;
import constants.IPSConstants;
import constants.OptionKeys;
import controllers.BaseController;
import controllers.SubmitCheck;
import controllers.SubmitOnly;
import controllers.SubmitRepeat;
import controllers.app.common.MessageUtil;
import controllers.app.common.MsgCode;
import controllers.app.common.Severity;
import controllers.front.bid.BidAction;
import models.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.mvc.With;
import utils.*;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@With({CheckAction.class, SubmitRepeat.class})
public class FundsManage extends BaseController {

	//-------------------------------资金管理-------------------------
	/**
	 * 账户信息
	 */
	public static void accountInformation(){
		User user = User.currUser();
		long userId = user.id;
		
		ErrorInfo error = new ErrorInfo();
		v_user_account_statistics accountStatistics = User.queryAccountStatistics(userId, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		UserOZ accountInfo = new UserOZ(userId);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		List<v_user_details> userDetails = User.queryUserDetail(userId, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		List<UserBankAccounts> userBanks = UserBankAccounts.queryUserAllBankAccount(userId);
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String content = News.queryContent(Constants.NewsTypeId.VIP_AGREEMENT, error);
		
		List<t_content_news> news = News.queryNewForFront(Constants.NewsTypeId.MONEY_TIPS, 3,error);
		
		boolean isIps = Constants.IPS_ENABLE;
		
		render(user, accountStatistics, accountInfo, userDetails, userBanks, backstageSet, content, news, isIps);
	}
	
	/**
	 * 添加银行账号
	 */
	public static void addBank(String addBankName, String addAccount, String addAccountName){
		User user = User.currUser();
		
		ErrorInfo error = new ErrorInfo();
		
		UserBankAccounts bankUser =  new UserBankAccounts();
		
		bankUser.userId = user.id;
		bankUser.bankName = addBankName;
		bankUser.account = addAccount;
		bankUser.accountName = addAccountName;
		
		bankUser.addUserBankAccount(error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	//保存银行账号
	public static void saveBank(){
		render();
	}
	
	/**
	 * 编辑银行账号
	 */

	public static void editBank(long editAccountId, String editBankName, String editAccount, String editAccountName){

		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		UserBankAccounts userAccount = new UserBankAccounts();
		
		userAccount.bankName = editBankName;
		userAccount.account = editAccount;
		userAccount.accountName = editAccountName;

		userAccount.editUserBankAccount(editAccountId, user.id, error);

		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 删除银行账号
	 */
	public static void deleteBank(long accountId){
		ErrorInfo error = new ErrorInfo();
		
		UserBankAccounts.deleteUserBankAccount(User.currUser().id, accountId, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 我的信用等级
	 */
	public static void myCredit(){
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		v_user_detail_score creditScore = User.queryCreditScore(user.id);

		List<t_user_over_borrows> overBorrows = OverBorrow.queryUserOverBorrows(user.id, error);
		
		if(error.code < 0) {
			render(user, Constants.ERROR_PAGE_PATH_FRONT);
		}

//		double creditInitialAmount = BackstageSet.queryCreditInitialAmount();
		double creditInitialAmount = BackstageSet.getCurrentBackstageSet().initialAmount;
		
		
		render(user,creditScore,overBorrows,creditInitialAmount);
	}
	
	/**
	 * 信用积分明细(成功借款)
	 */
	public static void creditDetailLoan(String key, int currPage){
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_loan> page = User.queryCreditDetailLoan(user.id, currPage, 0, key);
		
		render(page);
	}
	
	/**
	 * 信用积分明细(审核资料)
	 */
	public static void creditDetailAuditItem(String key, int currPage){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_audit_items> page = User.queryCreditDetailAuditItem(user.id, currPage, 0, key, error);
		
//		if(error.code < 0){
//			renderJSON(error);
//		}
		
		render(page);
	}
	
	/**
	 * 信用积分明细(成功投标)
	 */
	public static void creditDetailInvest(String key, int currPage){
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_invest> page = User.queryCreditDetailInvest(user.id, currPage, 0, key);
		
		render(page);
	}
	
	/**
	 * 信用积分明细(正常还款)
	 * @param key
	 */
	public static void creditDetailRepayment(String key, int currPage){
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_normal_repayment> page = User.queryCreditDetailRepayment(user.id, currPage, 0, key);
		
		render(page);
	}
	
	/**
	 * 信用积分明细(逾期扣分)
	 * @param key
	 */
	public static void creditDetailOverdue(String key, int currPage){
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_overdue> page = User.queryCreditDetailOverdue(user.id, currPage, 0, key);
		
		render(page);
	}
	
	/**
	 * 查看信用等级规则
	 */
	public static void viewCreditRule(){
		ErrorInfo error = new ErrorInfo();
		List<v_credit_levels> CreditLevels = CreditLevel.queryCreditLevelList(error);
		
		render(CreditLevels);
	}
	
	/**
	 * 查看信用积分规则
	 */
	public static void creditintegral(){
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		long auditItemCount = AuditItem.auditItemCount();
		
		ErrorInfo error = new ErrorInfo();

		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); 
		double amountKey = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value); // 积分对应额度
		
		render(backstageSet, auditItemCount, amountKey);
	}
	
	/**
	 * 查看科目积分规则
	 */
	public static void creditItem(String key, int currPage){
		ErrorInfo error = new ErrorInfo();
		
		PageBean<t_dict_audit_items> page = AuditItem.queryEnableAuditItems(key, currPage, 0, error); // 审核资料
		
		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); 
		double amountKey = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value); // 积分对应额度
		
		render(page, amountKey);
	}
	
	/**
	 * 审核资料
	 */
	
	/**
	 * 审核资料积分明细（信用积分规则弹窗）
	 */
	public static void auditItemScore(String keyword, String currPage, String pageSize) {
		ErrorInfo error = new ErrorInfo();
		PageBean<AuditItem> page = AuditItem.queryAuditItems(currPage, pageSize, keyword, true, error);
		
		render(page, error);
	}
	
	//申请超额借款
	public static void applyOverBorrow(){
		render();
	}

	//提交申请
	public static void submitApply(){
		render();
	}
	
	/**
	 * 查看超额申请详情
	 */
	public static void viewOverBorrow(long overBorrowId){
		ErrorInfo error = new ErrorInfo();
		List<v_user_audit_items> auditItems = OverBorrow.queryAuditItems(overBorrowId, error);
		t_user_over_borrows overBorrows = OverBorrow.queryOverBorrowById(overBorrowId, error);
		render(overBorrows, auditItems);
	}
	
	/**
	 * 查看超额申请详情(IPS)
	 */
	public static void viewOverBorrowIps(long overBorrowId){
		ErrorInfo error = new ErrorInfo();
		List<v_user_audit_items> auditItems = OverBorrow.queryAuditItems(overBorrowId, error);
		t_user_over_borrows overBorrows = OverBorrow.queryOverBorrowById(overBorrowId, error);
		render(overBorrows, auditItems);
	}
	
	/**
	 * 提交资料
	 */  
	public static void userAuditItem(long overBorrowId, long useritemId, long auditItemId, String filename){
		
		ErrorInfo error = new ErrorInfo();

		UserAuditItem item = new UserAuditItem();
		item.lazy = true;
		item.userId = User.currUser().id;
		item.id = useritemId;
		item.auditItemId = auditItemId;
		item.imageFileName = filename;
		item.overBorrowId = overBorrowId;
		item.createUserAuditItem(error);

		JSONObject json = new JSONObject();
		
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 充值
	 */
	@SubmitCheck
	public static void recharge(){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		
		if (Constants.IPS_ENABLE) {
			
			List<Map<String, Object>> bankList = null;
			String version = BackstageSet.getCurrentBackstageSet().entrustVersion;
			
			if("1.0".equals(version)) {
				bankList = Payment.getBankList(error);
			}
			
			render("@front.account.FundsManage.rechargeIps",user, bankList, version);
		}
		
		List<t_dict_payment_gateways> payType = user.gatewayForUse(error);
		
		render(user, payType);
	}
	
	/**
	 * app充值
	 */
	public static void rechargeApp(){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		
		if (Constants.IPS_ENABLE) {
			List<Map<String, Object>> bankList = Payment.getBankList(error);
			
			render("@front.account.FundsManage.rechargeIps",user, bankList);
		}
		
		List<t_dict_payment_gateways> payType = user.gatewayForUse(error);
		
		render(user, payType);
	}

    /**
     * 新版app确认充值--用于金豆荚app端
     */
    public static void rechargeApp2(){
        String bankCode = params.get("bankCode");
        double money = 0;

        try {
            if (params.get("money") == null) {
                MessageUtil.getInstance().setMessage(new controllers.app.common.Message(Severity.ERROR, MsgCode.RECHARGE_ERROR));
                renderJSON(MessageUtil.getInstance().toStr());
            }
            money = Double.valueOf(params.get("money"));
        }catch(Exception e){
            e.printStackTrace();
            MessageUtil.getInstance().setMessage(new controllers.app.common.Message(Severity.ERROR, MsgCode.RECHARGE_ERROR));
            renderJSON(MessageUtil.getInstance().toStr());
        }

        if (money <= 0) {
            MessageUtil.getInstance().setMessage(new controllers.app.common.Message(Severity.ERROR, MsgCode.RECHARGE_ERROR));
            renderJSON(MessageUtil.getInstance().toStr());
        }

        ErrorInfo error = new ErrorInfo();
        Map<String, String> args = Payment.doDpTrade(money, bankCode, error, ParseClientUtil.APP);

        render("@front.account.PaymentAction.doDpTrade", args);
    }

	/**
	 * app确认充值
	 */
	public static void submitRechargeApp(int type, double money, int bankType){
		ErrorInfo error = new ErrorInfo();
		
		if (Constants.IPS_ENABLE) {
			String bankCode = params.get("bankCode");
			
			if (money <= 0) {
				flash.error("请输入正确的充值金额");
				rechargeApp();
			}
			
			if (StringUtils.isBlank(bankCode) || bankCode.equals("0")) {
				flash.error("请选择充值银行");
				rechargeApp();
			}
			
			Map<String, String> args = Payment.doDpTrade(money, bankCode, error, ParseClientUtil.PC);
			
			render("@front.account.PaymentAction.doDpTrade", args);
		}
		
		flash.put("type", type);
		flash.put("money", money);
		flash.put("bankType",bankType);
		
		if(type<1 || type >2) {
			flash.error("请选择正确的充值方式");
			rechargeApp();
		}
		
		if(money == 0) {
			flash.error("请输入正确的充值金额");
			rechargeApp();
		}
		
		BigDecimal moneyDecimal = new BigDecimal(money);
		
		if(moneyDecimal.compareTo(new BigDecimal("0.02")) < 0) {
			flash.error("请输入正确的充值金额");
			rechargeApp();
		}
		
		if(type == 2) {
			Map<String, String> args = User.ipay(moneyDecimal, bankType, RechargeType.Normal, true, error);
			
			if(error.code < 0) {
				flash.error(error.msg);
				rechargeApp();
			}
			
			render("@front.account.FundsManage.submitRecharge",args);
		}
		
		if(type == 1) {
			Map<String, String> args = User.gpay(moneyDecimal, bankType, RechargeType.Normal, true, error);
			
			if(error.code != 0) {
				flash.error(error.msg);
				recharge();
			}
			
			render("@front.account.FundsManage.submitRecharge2",args);
		}
		
	}
	
	/**
	 * 支付vip，资料审核等服务费
	 */
	public static void rechargePay() {
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		List<t_dict_payment_gateways> payType = user.gatewayForUse(error);
		
		Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay"+user.id);
		
		if(null == map) 
			renderText("请求过时或已提交!");
			
		double fee = (Double) map.get("fee");
		double amount = 0;
		boolean isPay = false;
		
		if (Constants.IPS_ENABLE) {
			if (Constants.PAY_TYPE_VIP == PayType.INDEPENDENT) {
				amount = user.balanceDetail.user_amount2;
			} else if(Constants.PAY_TYPE_VIP == PayType.SHARED){
				/* 是否是共享资金托管 */
				isPay = true;
			}
		} else {
			amount = user.balanceDetail.user_amount;
		}
		
		render(user, payType, fee, amount, isPay);
	}
	
	/**
	 * 支付发标保证金
	 */
	@SubmitCheck
	public static void rechargePayIps(){
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		Map<String, Object> map = (Map<String, Object>)Cache.get("rechargePayIps"+user.id);
		
		if(null == map || map.size() == 0)
			renderText("请求超时!");
		
		double fee = (Double) map.get("fee");
		
		List<Map<String, Object>> bankList = null; 
		String version = BackstageSet.getCurrentBackstageSet().entrustVersion; 

		if("1.0".equals(version)) { 
			bankList = Payment.getBankList(error); 
		} 
		
		render("@front.account.FundsManage.rechargePayIps",user, bankList, fee);
	}
	
	/**
	 * 确认充值
	 */
	@SubmitOnly
	public static void submitRecharge(int type, double money, int bankType){
		ErrorInfo error = new ErrorInfo();
		
		if (Constants.IPS_ENABLE) {
			String bankCode = params.get("bankCode");
			
			if (money <= 0 || money > Constants.MAX_VALUE) {
				flash.error("充值金额范围需在[0~" + Constants.MAX_VALUE + "]之间");
				recharge();
			}
			
			if ((StringUtils.isBlank(bankCode) || bankCode.equals("0")) && "1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
				flash.error("请选择充值银行");
				recharge();
			}
			
			Map<String, String> args = Payment.doDpTrade(money, bankCode, error, ParseClientUtil.PC);
			
			render("@front.account.PaymentAction.doDpTrade", args);
		}
		
		flash.put("type", type);
		flash.put("money", money);
		flash.put("bankType",bankType);
		
		if(type<1 || type >2) {
			flash.error("请选择正确的充值方式");
			recharge();
		}
		
		if(money <= 0 || money > Constants.MAX_VALUE) {
			flash.error("充值金额范围需在[0~" + Constants.MAX_VALUE + "]之间");
			recharge();
		}
		
		BigDecimal moneyDecimal = new BigDecimal(money);
		
		if(moneyDecimal.compareTo(new BigDecimal("0.02")) < 0) {
			flash.error("请输入正确的充值金额");
			recharge();
		}
		
		if(type == 2) {
			Map<String, String> args = User.ipay(moneyDecimal, bankType, RechargeType.Normal, false, error);
			
			if(error.code < 0) {
				flash.error(error.msg);
				recharge();
			}
			
			render(args);
		}
		
		if(type == 1) {
			Map<String, String> args = User.gpay(moneyDecimal, bankType, RechargeType.Normal, false, error);
			
			if(error.code != 0) {
				flash.error(error.msg);
				recharge();
			}
			
			render("@front.account.FundsManage.submitRecharge2",args);
		}
		
	}
	
	/**
	 * 确认支付
	 */
	public static void submitRechargePay(int type, int bankType, boolean isUse){
		ErrorInfo error = new ErrorInfo();
		flash.put("type", type);
		flash.put("bankType",bankType);
		
		if(type<1 || type >2) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		User user = User.currUser();
		Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
		double fee = (Double) map.get("fee");
		int rechargeType = (Integer) map.get("rechargeType");
		double amount = 0;
		
		if (Constants.IPS_ENABLE) {
			if (Constants.PAY_TYPE_VIP == PayType.INDEPENDENT) {
				amount = user.balanceDetail.user_amount2;
			}
		} else {
			amount = user.balanceDetail.user_amount;
		}
		
		double money = isUse ? (fee - amount) : fee;
		
		if(money <= 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		BigDecimal moneyDecimal = new BigDecimal(money);
		
		if(moneyDecimal.compareTo(new BigDecimal("0.02")) < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		if(type == 2) {
			Map<String, String> args = User.ipay(moneyDecimal, bankType, rechargeType, false, error);
			
			if(error.code < 0) {
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			
			render("@front.account.FundsManage.submitRecharge",args);
		}
		
		if(type == 1) {
			Map<String, String> args = User.gpay(moneyDecimal, bankType, rechargeType, false, error);
			
			if(error.code != 0) {
				flash.error(error.msg);
				rechargePay();
			}
			
			render("@front.account.FundsManage.submitRecharge2",args);
		}
		
	}
	
	/**
	 * 环迅回调
	 */
	public static void callback(String billno, String mercode, String Currency_type, String amount, String date, String succ,
			String msg, String attach, String ipsbillno, String retencodetype, String signature) {
		ErrorInfo error = new ErrorInfo();
		
		String sql = "select user_id from t_user_recharge_details where pay_number = ?";
		Object obj = null;
		
		try {
			obj = t_user_recharge_details.find(sql, billno).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "根据pay_number查询用户ID出现错误!";
			
			return ;
		}
		
		if(null == obj) {
			error.code = -1;
			Logger.info("根据pay_number查询用户ID为null");
			
			return ;
		}

		//User.setCurrUser(Long.parseLong(obj.toString())); // 更新缓存中的用户对象，session发生了变法
		
		//返回订单加密的明文:billno+【订单编号】+currencytype+【币种】+amount+【订单金额】+date+【订单日期】+succ+【成功标志】+ipsbillno+【IPS订单编号】+retencodetype +【交易返回签名方式】+【商户内部证书】 
		String content="billno"+billno + "currencytype"+Currency_type+"amount"+amount+"date"+date+"succ"+succ+"ipsbillno"+ipsbillno+"retencodetype"+retencodetype;  //明文：订单编号+订单金额+订单日期+成功标志+IPS订单编号+币种

		boolean verify = false;

		//验证方式：16-md5withRSA  17-md5
		if(retencodetype.equals("16")) {
			cryptix.jce.provider.MD5WithRSA a=new cryptix.jce.provider.MD5WithRSA();
			a.verifysignature(content, signature, "D:\\software\\publickey.txt");

			//Md5withRSA验证返回代码含义
			//-99 未处理
			//-1 公钥路径错
			//-2 公钥路径为空
			//-3 读取公钥失败
			//-4 验证失败，格式错误
			//1： 验证失败
			//0: 成功
			if (a.getresult() == 0){
				verify = true;
			}	
		} else if(retencodetype.equals("17")) {
			User.validSign(content, signature, error);
			
			if(error.code == 0) {
				verify = true;
			}
		}
		String info = "";
		if(!verify) {
			info = "验证失败";
			render(info);
		}
		
		if (succ == null) {
			info = "交易失败";
			render(info);
		}
		
		if(!succ.equalsIgnoreCase("Y")) {
			info = "交易失败";
			render(info);
		} 
		
		User.recharge(billno, Double.parseDouble(amount), error);
		int rechargeType = Convert.strToInt(billno.split("X")[0], RechargeType.Normal);
		
		if (Constants.IPS_ENABLE) {
			if(error.code < 0) {
				flash.error(error.msg);
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			
			if (rechargeType == RechargeType.VIP) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.isPay = true;
				vip.serviceTime = serviceTime;
				vip.renewal(user, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("支付vip费用成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
			
			if (rechargeType == RechargeType.InvestBonus) {
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + User.currUser().id);
				Bid bid = (Bid) map.get("bid");
				bid.deductInvestBonus(error);
				
				if(error.code < 0) {
					flash.error(error.msg);
					render(error);
				}
				
				Cache.delete("rechargePay" + bid.userId);
				
				Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CREATE, bid);
				
				render("@front.account.PaymentAction.registerSubject", args);
			}
			
			if (rechargeType == RechargeType.UploadItems) {
				User user = User.currUser();
				double user_amount = Constants.PAY_TYPE_ITEM == PayType.INDEPENDENT ? user.balance2 : user.balance;
				
				UserAuditItem.submitUploadedItems(user.id, user_amount, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("支付资料审核费成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
			}
			
			if (rechargeType == RechargeType.UploadItemsOB) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("申请超额借款成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
		} else {
			if (rechargeType == RechargeType.CREATBID) {
				long user_id = User.currUser().id;
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user_id);
				Bid bid = (Bid) map.get("bid");
				bid.createBid = true;
				bid.user.id = user_id;
				bid.createBid(error);
				
				Cache.delete("rechargePay" + bid.userId);
				flash.put("msg", error.msg);
				 
				/* 页面需要的返回数据 */
				if(bid.id > 0){
					flash.put("no", OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, error) + bid.id);
					flash.put("title", bid.title);
					DecimalFormat myformat = new DecimalFormat();
					myformat.applyPattern("##,##0.000");
					flash.put("amount", myformat.format(bid.amount));
					flash.put("status", bid.status);
				}
				
				BidAction.applyNow(bid.productId, error.code, 1);
			}
			
			if (rechargeType == RechargeType.VIP) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.serviceTime = serviceTime;
				vip.renewal(user, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("支付vip费用成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if(rechargeType == RechargeType.UploadItems){
				User user = User.currUser();
				UserAuditItem.submitUploadedItems(user.id, user.balance, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("支付资料审核费成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if (rechargeType == RechargeType.UploadItemsOB) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("申请超额借款成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
		}
		 
		if(error.code < 0) {
			 flash.error(error.msg);
			 render(error);
		}
		
		if("1".equals(attach)) {
			rechargeApp();
		}
		
		info = "交易成功";
		render(info);
	}
	
	/**
	 * 环迅回调（异步）
	 */
	public static void callbackSys(String billno, String mercode, String Currency_type, String amount, String date, String succ,
			String msg, String attach, String ipsbillno, String retencodetype, String signature) {
		ErrorInfo error = new ErrorInfo();
		
		String sql = "select user_id from t_user_recharge_details where pay_number = ?";
		Object obj = null;
		
		try {
			obj = t_user_recharge_details.find(sql, billno).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "根据pay_number查询用户ID出现错误!";
			
			return ;
		}
		
		if(null == obj) {
			error.code = -1;
			Logger.info("根据pay_number查询用户ID为null");
			
			return ;
		}

		//User.setCurrUser(Long.parseLong(obj.toString())); // 更新缓存中的用户对象，session发生了变法
		
		//返回订单加密的明文:billno+【订单编号】+currencytype+【币种】+amount+【订单金额】+date+【订单日期】+succ+【成功标志】+ipsbillno+【IPS订单编号】+retencodetype +【交易返回签名方式】+【商户内部证书】 
		String content="billno"+billno + "currencytype"+Currency_type+"amount"+amount+"date"+date+"succ"+succ+"ipsbillno"+ipsbillno+"retencodetype"+retencodetype;  //明文：订单编号+订单金额+订单日期+成功标志+IPS订单编号+币种

		boolean verify = false;

		//验证方式：16-md5withRSA  17-md5
		if(retencodetype.equals("16")) {
			cryptix.jce.provider.MD5WithRSA a=new cryptix.jce.provider.MD5WithRSA();
			a.verifysignature(content, signature, "D:\\software\\publickey.txt");

			//Md5withRSA验证返回代码含义
			//-99 未处理
			//-1 公钥路径错
			//-2 公钥路径为空
			//-3 读取公钥失败
			//-4 验证失败，格式错误
			//1： 验证失败
			//0: 成功
			if (a.getresult() == 0){
				verify = true;
			}	
		} else if(retencodetype.equals("17")) {
			User.validSign(content, signature, error);
			
			if(error.code == 0) {
				verify = true;
			}
		}
		String info = "";
		if(!verify) {
			info = "验证失败";
			render(info);
		}
		
		if (succ == null) {
			info = "交易失败";
			render(info);
		}
		
		if(!succ.equalsIgnoreCase("Y")) {
			info = "交易失败";
			render(info);
		} 
		
		User.recharge(billno, Double.parseDouble(amount), error);
		int rechargeType = Convert.strToInt(billno.split("X")[0], RechargeType.Normal);
		
		if (Constants.IPS_ENABLE) {
			if(error.code < 0) {
				flash.error(error.msg);
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			
			if (rechargeType == RechargeType.VIP) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.isPay = true;
				vip.serviceTime = serviceTime;
				vip.renewal(user, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
			
			if (rechargeType == RechargeType.InvestBonus) {
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + User.currUser().id);
				Bid bid = (Bid) map.get("bid");
				bid.deductInvestBonus(error);
				
				if(error.code < 0) {
					flash.error(error.msg);
					render(error);
				}
				
				Cache.delete("rechargePay" + bid.userId);
				
				Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CREATE, bid);
				
				render("@front.account.PaymentAction.registerSubject", args);
			}
			
			if (rechargeType == RechargeType.UploadItems) {
				User user = User.currUser();
				double user_amount = Constants.PAY_TYPE_ITEM == PayType.INDEPENDENT ? user.balance2 : user.balance;
				
				UserAuditItem.submitUploadedItems(user.id, user_amount, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
			
			if (rechargeType == RechargeType.UploadItemsOB) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
		} else {
			if (rechargeType == RechargeType.CREATBID) {
				long user_id = User.currUser().id;
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user_id);
				Bid bid = (Bid) map.get("bid");
				bid.createBid = true;
				bid.user.id = user_id;
				bid.createBid(error);
				
				Cache.delete("rechargePay" + bid.userId);
				
				return;
			}
			
			if (rechargeType == RechargeType.VIP) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.serviceTime = serviceTime;
				vip.renewal(user, error);
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if(rechargeType == RechargeType.UploadItems){
				User user = User.currUser();
				UserAuditItem.submitUploadedItems(user.id, user.balance, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if (rechargeType == RechargeType.UploadItemsOB) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
		}
	}
	
	/**
	 * 国付宝回调
	 */
	public static void gCallback(String version,String charset,String language,String signType,String tranCode
			,String merchantID,String merOrderNum,String tranAmt,String feeAmt,String frontMerUrl,String backgroundMerUrl
			,String tranDateTime,String tranIP,String respCode,String msgExt,String orderId
			,String gopayOutOrderId,String bankCode,String tranFinishTime,String merRemark1,String merRemark2,String signValue) {
		ErrorInfo error = new ErrorInfo();
		String info = "";
		
		t_dict_payment_gateways gateway = User.gateway(Constants.GO_GATEWAY, error);
		
		if(GopayUtils.validateSign(version,tranCode, merchantID, merOrderNum,
	    		tranAmt, feeAmt, tranDateTime, frontMerUrl, backgroundMerUrl,
	    		orderId, gopayOutOrderId, tranIP, respCode,gateway._key, signValue)) {
			
			info = "验证失败，支付失败！";
			render(info);
		}
		
		Logger.info("respCode:"+respCode);
		
		if (!"0000".equals(respCode) && !"9999".equals(respCode)) {
			info = "支付失败！";
			render(info);
		}
		
		if ("9999".equals(respCode)) {
			info = "订单处理中，请耐心等待！";
			render(info);
		}
		
		User.recharge(merOrderNum, Double.parseDouble(tranAmt), error);
		int rechargeType = Convert.strToInt(merOrderNum.split("X")[0], RechargeType.Normal);
		
		if (Constants.IPS_ENABLE) {
			if(error.code < 0) {
				flash.error(error.msg);
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			
			if (rechargeType == RechargeType.VIP) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.isPay = true;
				vip.serviceTime = serviceTime;
				vip.renewal(user, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("支付vip费用成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
			
			if (rechargeType == RechargeType.InvestBonus) {
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + User.currUser().id);
				Bid bid = (Bid) map.get("bid");
				bid.deductInvestBonus(error);
				
				if(error.code < 0) {
					flash.error(error.msg);
					render(error);
				}
				
				Cache.delete("rechargePay" + bid.userId);
				
				Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CREATE, bid);
				
				render("@front.account.PaymentAction.registerSubject", args);
			}
			
			if (rechargeType == RechargeType.UploadItems) {
				User user = User.currUser();
				double user_amount = Constants.PAY_TYPE_ITEM == PayType.INDEPENDENT ? user.balance2 : user.balance;
				
				UserAuditItem.submitUploadedItems(user.id, user_amount, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("支付资料审核费成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
			}
			
			if (rechargeType == RechargeType.UploadItemsOB) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("申请超额借款成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
		} else {
			if (rechargeType == RechargeType.CREATBID) {
				long user_id = User.currUser().id;
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user_id);
				Bid bid = (Bid) map.get("bid");
				bid.createBid = true;
				bid.user.id = user_id;
				bid.createBid(error);
				
				Cache.delete("rechargePay" + bid.userId);
				flash.put("msg", error.msg);
				 
				/* 页面需要的返回数据 */
				if(bid.id > 0){
					flash.put("no", OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, error) + bid.id);
					flash.put("title", bid.title);
					DecimalFormat myformat = new DecimalFormat();
					myformat.applyPattern("##,##0.000");
					flash.put("amount", myformat.format(bid.amount));
					flash.put("status", bid.status);
				}
				
				BidAction.applyNow(bid.productId, error.code, 1);
			}
			
			if (rechargeType == RechargeType.VIP) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.serviceTime = serviceTime;
				vip.renewal(user, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("支付vip费用成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if(rechargeType == RechargeType.UploadItems){
				User user = User.currUser();
				UserAuditItem.submitUploadedItems(user.id, user.balance, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("支付资料审核费成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if (rechargeType == RechargeType.UploadItemsOB) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.success("申请超额借款成功");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
		}
		 
		if(error.code < 0) {
			 flash.error(error.msg);
			 render(error);
		}
		
		if("1".equals(merRemark1)) {
			rechargeApp();
		}
		
		info = "交易成功";
		render(info);
	}
	
	/**
	 * 国付宝回调（异步）
	 */
	public static void gCallbackSys(String version,String charset,String language,String signType,String tranCode
			,String merchantID,String merOrderNum,String tranAmt,String feeAmt,String frontMerUrl,String backgroundMerUrl
			,String tranDateTime,String tranIP,String respCode,String msgExt,String orderId
			,String gopayOutOrderId,String bankCode,String tranFinishTime,String merRemark1,String merRemark2,String signValue) {
		ErrorInfo error = new ErrorInfo();
		t_dict_payment_gateways gateway = User.gateway(Constants.GO_GATEWAY, error);
		
		if(GopayUtils.validateSign(version,tranCode, merchantID, merOrderNum,
	    		tranAmt, feeAmt, tranDateTime, frontMerUrl, backgroundMerUrl,
	    		orderId, gopayOutOrderId, tranIP, respCode,gateway._key, signValue)) {
			Logger.info("---------------验证失败，支付失败！------------");
			return ;
		}
		
		Logger.info("respCode:"+respCode);
		
		if (!"0000".equals(respCode) && !"9999".equals(respCode)) {
			Logger.info("---------------支付失败！------------");
			return ;
		}
		
		if ("9999".equals(respCode)) {
			Logger.info("---------------订单处理中，请耐心等待！------------");
			return ;
		}
		
		User.recharge(merOrderNum, Double.parseDouble(tranAmt), error);
		int rechargeType = Convert.strToInt(merOrderNum.split("X")[0], RechargeType.Normal);
		
		if (Constants.IPS_ENABLE) {
			if(error.code < 0) {
				flash.error(error.msg);
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			
			if (rechargeType == RechargeType.VIP) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.isPay = true;
				vip.serviceTime = serviceTime;
				vip.renewal(user, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
			
			if (rechargeType == RechargeType.InvestBonus) {
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + User.currUser().id);
				Bid bid = (Bid) map.get("bid");
				bid.deductInvestBonus(error);
				
				if(error.code < 0) {
					flash.error(error.msg);
					render(error);
				}
				
				Cache.delete("rechargePay" + bid.userId);
				
				Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CREATE, bid);
				
				render("@front.account.PaymentAction.registerSubject", args);
			}
			
			if (rechargeType == RechargeType.UploadItems) {
				User user = User.currUser();
				double user_amount = Constants.PAY_TYPE_ITEM == PayType.INDEPENDENT ? user.balance2 : user.balance;
				
				UserAuditItem.submitUploadedItems(user.id, user_amount, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
			
			if (rechargeType == RechargeType.UploadItemsOB) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
		} else {
			if (rechargeType == RechargeType.CREATBID) {
				long user_id = User.currUser().id;
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user_id);
				Bid bid = (Bid) map.get("bid");
				bid.createBid = true;
				bid.user.id = user_id;
				bid.createBid(error);
				
				Cache.delete("rechargePay" + bid.userId);
				
				return;
			}
			
			if (rechargeType == RechargeType.VIP) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.serviceTime = serviceTime;
				vip.renewal(user, error);
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if(rechargeType == RechargeType.UploadItems){
				User user = User.currUser();
				UserAuditItem.submitUploadedItems(user.id, user.balance, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if (rechargeType == RechargeType.UploadItemsOB) {
				User user = User.currUser();
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
		}
	}
	
	/**
	 * 提现
	 */
	@SubmitCheck
	public static void withdrawal(){
		User user = new User();
		user.id = User.currUser().id;
		ErrorInfo error = new ErrorInfo();
		
		String type = params.get("type");
		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String beginTime = params.get("startDate");
		String endTime = params.get("endDate");
		
		double amount = User.queryRechargeIn(user.id, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		double withdrawalAmount = user.balance - amount;
		//最多提现金额上限
		double maxWithDrawalAmount = Constants.MAX_VALUE;
		
		if(withdrawalAmount < 0) {
			withdrawalAmount = 0;
		}
		
		List<UserBankAccounts> banks = UserBankAccounts.queryUserAllBankAccount(user.id);
		
		PageBean<v_user_withdrawals> page = User.queryWithdrawalRecord(user.id, type, 
				beginTime, endTime, currPage, pageSize, error);
		boolean ipsEnable = Constants.IPS_ENABLE;
		
		render(user, withdrawalAmount, maxWithDrawalAmount, banks, page, ipsEnable);
	}
	
	/**
	 * 根据选择的银行卡id查询其信息
	 */
	public static void QueryBankInfo(long id){
		JSONObject json = new JSONObject();
		
		UserBankAccounts bank = new UserBankAccounts();
		bank.setId(id);
		
		json.put("bank", bank);
		
		renderJSON(json);
	}
	
	
//	/**
//	 * 提现记录
//	 */
//	public static void withdrawalRecord() {
//		User user = User.currUser();
//		
//		String type = params.get("type");
//		String currPage = params.get("currPage");
//		String pageSize = params.get("pageSize");
//		String beginTime = params.get("startDate");
//		String endTime = params.get("endDate");
//		
//		ErrorInfo error = new ErrorInfo();
//		PageBean<v_user_withdrawals> page = User.queryWithdrawalRecord(user.id, type, 
//				beginTime, endTime, currPage, pageSize, error);
//		
//		render(page);
//	}
	
	//申请提现
	public static void applyWithdrawal(){
		render();
	}
	
	/**
	 * 确认提现
	 */
	@SubmitOnly
	public static void submitWithdrawal(double amount, long bankId, String payPassword, int type, String ipsSelect){
		ErrorInfo error = new ErrorInfo();
		boolean flag = false;
		
		if(StringUtils.isNotBlank(ipsSelect) && ipsSelect.equals("1")) {
			flag = true;
		}
		if(amount <= 0) {
			flash.error("请输入提现金额");
			
			withdrawal();
		}
		
		if(amount > Constants.MAX_VALUE) {
			flash.error("已超过最大充值金额" +Constants.MAX_VALUE+ "元");
			
			withdrawal();
		}
		
		if (!(Constants.IPS_ENABLE && flag)) {
			if(StringUtils.isBlank(payPassword)) {
				flash.error("请输入交易密码");
				
				withdrawal();
			}
			
			if(type !=1 && type != 2) {
				flash.error("传入参数有误");
				
				withdrawal();
			}
			
			if(bankId <= 0) {
				flash.error("请选择提现银行");
				
				withdrawal();
			}
		}
		
		User user = new User();
		user.id = User.currUser().id;
		
		long withdrawalId = user.withdrawal(amount, bankId, payPassword, type, flag, error);
		
		if(Constants.IPS_ENABLE && flag) {
			if(error.code < 0) {
				flash.error(error.msg);
				
				withdrawal();
			}
			
			Map<String, String> args= Payment.doDwTrade(withdrawalId, amount, error, ParseClientUtil.PC);
			
			if (error.code < 0) {
				flash.error(error.msg);
				
				withdrawal();
			}
			
			render("@front.account.PaymentAction.doDwTrade", args);
		}
		
		flash.error(error.msg);
		
		withdrawal();
	}


    public static void enchashApp(){
        ErrorInfo error = new ErrorInfo();
        double amount = 0;

        if(params.get("money") == null) {
            error.code = -1;
            error.msg = "请输入提现金额";
        }
        try {
            amount = Double.valueOf(params.get("money"));
            if(amount > Constants.MAX_VALUE) {
                error.code = -1;
                error.msg = "已超过最大充值金额" +Constants.MAX_VALUE+ "元";
            }
        }catch (Exception e){
            e.printStackTrace();
            error.code = -1;
            error.msg = "提现金额格式不正确";
            MessageUtil.getInstance().setMessage(new controllers.app.common.Message(Severity.ERROR, MsgCode.ENCHASH_ERROR, error.msg));
            renderJSON(MessageUtil.getInstance().toStr());
        }

        if (error.code < 0) {
            MessageUtil.getInstance().setMessage(new controllers.app.common.Message(Severity.ERROR, MsgCode.ENCHASH_ERROR, error.msg));
            renderJSON(MessageUtil.getInstance().toStr());
        }

        User user = new User();
        if (user == null) {
            MessageUtil.getInstance().setMessage(new controllers.app.common.Message(Severity.ERROR, MsgCode.CURRENT_USER_FAIL));
            renderJSON(MessageUtil.getInstance().toStr());
        }
        user.id = User.currUser().id;

        long withdrawalId = user.withdrawal(amount, 0, null, 0, true, error);

        if(error.code < 0) {
            MessageUtil.getInstance().setMessage(new controllers.app.common.Message(Severity.ERROR, MsgCode.ENCHASH_ERROR, error.msg));
            renderJSON(MessageUtil.getInstance().toStr());
        }

        Map<String, String> args= Payment.doDwTrade(withdrawalId, amount, error, ParseClientUtil.PC);

        if (error.code < 0) {
            MessageUtil.getInstance().setMessage(new controllers.app.common.Message(Severity.ERROR, MsgCode.ENCHASH_ERROR, error.msg));
            renderJSON(MessageUtil.getInstance().toStr());
        }

        render("@front.account.PaymentAction.doDwTrade", args);
    }
	
	//转账
	public static void transfer(){
		render();
	}
	
	//确认转账
	public static void submitTransfer(){
		render();
	}
	
	/**
	 * 交易记录
	 */
	public static void dealRecord(int type, String beginTime, String endTime, int currPage, int pageSize){
	
		User user = User.currUser();
		PageBean<v_user_details> page = User.queryUserDetails(user.id, type, beginTime, endTime,currPage, pageSize);
		
		render(page);
	}
	
	//交易详情
	public static void dealDetails(){
		render();
	}
	
	/**
	 * 导出交易记录
	 */
	public static void exportDealRecords(){
		ErrorInfo error = new ErrorInfo();
		
    	List<v_user_details> details = User.queryAllDetails(error);
    	
    	if (error.code < 0) {
			renderText("下载数据失败");
		}
    	
    	JsonConfig jsonConfig = new JsonConfig();
    	jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
    	JSONArray arrDetails = JSONArray.fromObject(details, jsonConfig);
    	
    	for (Object obj : arrDetails) {
			JSONObject detail = (JSONObject)obj;
			int type = detail.getInt("type");
			double amount = detail.getDouble("amount");
			
			switch (type) {
			case 1:
				detail.put("inAmount", amount);
				detail.put("outAmount", "");
				break;
			case 2:
				detail.put("inAmount", "");
				detail.put("outAmount", amount);
				break;
			default:
				detail.put("inAmount", "");
				detail.put("outAmount", "");
				break;
			}
		}
    	
    	File file = ExcelUtils.export(
    			"交易记录", 
    			arrDetails,
				new String[] {"时间", "收入", "支出", "账户总额", "可用余额", "冻结金额", "待收金额", "科目", "明细"}, 
				new String[] {"time", "inAmount", "outAmount", "user_balance", "balance", "freeze", "recieve_amount", "name", "summary"});
    	
    	renderBinary(file, "交易记录.xls");
	}
	
	/**
	 * 资金托管测试页面
	 */
	public static void payment() {
		render();
	}
	
	/**
	 * 我的支付账号
	 */
	public static void payAccount() {
		render();
	}
	
	/**
	 * 支付账号登陆
	 */
	public static void loginAccount() {
		
		Map<String, String> args = Payment.loginAccount();
		
		
		render("@front.account.PaymentAction.loginAccount", args);
	}
	
	/**
	 * 查看(异步)
	 */
	public static void showitem(String mark, String signUserId){
		/* 解密userId */
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(userId < 1){
			renderText(error.msg);
		}
		
		UserAuditItem item = new UserAuditItem();
		item.lazy = true;
		item.userId = userId;
		item.mark = mark;
		
		render(item);
	}
}