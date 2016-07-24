package controllers.supervisor.financeManager;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import net.sf.json.JSONObject;
import constants.Constants;
import constants.IPSConstants;
import controllers.Check;
import controllers.supervisor.SupervisorController;
import models.t_ips_details;
import models.t_platform_detail_types;
import models.t_user_recharge_details;
import models.t_users;
import models.v_platform_detail;
import models.v_user_info;
import models.v_user_withdrawal_info;
import models.v_user_withdrawals;
import business.DealDetail;
import business.IpsDetail;
import business.Supervisor;
import business.User;
import play.Logger;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 
 * 类名:PlatformAccountManager 功能:平台账户管理
 */

public class PlatformAccountManager extends SupervisorController {

	/**
	 * 待审核提现列表
	 */
	//@Check(Constants.TRUST_FUNDS)
	public static void toReviewWithdraws() {
		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数
		String beginTime = params.get("startDate"); // 开始时间
		String endTime = params.get("endDate"); // 结束时间
		String key = params.get("key"); 
		String orderTye = params.get("orderType") ;
		
		ErrorInfo error = new ErrorInfo();
		
		PageBean<v_user_withdrawal_info> page = User
				.queryWithdrawalBySupervisor(1L, Constants.WITHDRAWAL_CHECK_PENDING, beginTime, endTime, key,
				orderTye, currPage, pageSize, error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(page);
	}

	/**
	 * 待付款提现列表
	 */
	//@Check(Constants.TRUST_FUNDS)
	public static void toPayWithdraws() {
		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数
		String beginTime = params.get("startDate"); // 开始时间
		String endTime = params.get("endDate"); // 结束时间
		String key = params.get("key"); 
		String orderTye = params.get("orderType") ;
		
		ErrorInfo error = new ErrorInfo();

		PageBean<v_user_withdrawal_info> page = User
				.queryWithdrawalBySupervisor(1L, Constants.WITHDRAWAL_PAYING, beginTime, endTime, key,
				orderTye, currPage, pageSize, error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(page);
	}

	/**
	 * 已付款提现列表
	 */
	public static void paidWithdraws() {
		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数
		String beginTime = params.get("startDate"); // 开始时间
		String endTime = params.get("endDate"); // 结束时间
		String key = params.get("key"); 
		String orderTye = params.get("orderType") ;
		
		ErrorInfo error = new ErrorInfo();
		
		PageBean<v_user_withdrawal_info> page = User
				.queryWithdrawalBySupervisor(2L, Constants.WITHDRAWAL_SPEND, beginTime, endTime, key,
				orderTye, currPage, pageSize, error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(page);
	}

	/**
	 * 未通过提现列表
	 */
	public static void noPassWithdraws() {
		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数
		String beginTime = params.get("startDate"); // 开始时间
		String endTime = params.get("endDate"); // 结束时间
		String key = params.get("key"); 
		String orderTye = params.get("orderType") ;
		ErrorInfo error = new ErrorInfo();

		PageBean<v_user_withdrawal_info> page = User
				.queryWithdrawalBySupervisor(1L, Constants.WITHDRAWAL_NOT_PASS, beginTime, endTime, key,
				orderTye, currPage, pageSize, error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(page);
	}
	
	/**
	 * 详情  
	 */
	public static void withdrawDetail(long withdrawalId) {
		ErrorInfo error = new ErrorInfo();
		
		long supervisorId = 1L;
		v_user_withdrawals withdrawal =  User.queryWithdrawalDetailBySupervisor(
				supervisorId, withdrawalId, error);

		JSONObject json = new JSONObject();

		json.put("error", error);
		json.put("withdrawal", withdrawal);

		renderJSON(json);
	}

	/**
	 * 提现审核通过
	 */
	//@Check(Constants.TRUST_FUNDS)
	public static void withdrawPass(long withdrawalId) {
		ErrorInfo error = new ErrorInfo();
		User.auditWithdrawalPass(1L, withdrawalId, error);

		flash.error(error.msg);

		toReviewWithdraws();
	}

	/**
	 * 不通过(提现申请审核)
	 */
	//@Check(Constants.TRUST_FUNDS)
	public static void withdrawReview(long withdrawalId) {
		
		if(0 == withdrawalId)
			return ;
		
		String reason = params.get("reason");
		
		String returnType = params.get("returnType");
		
		ErrorInfo error = new ErrorInfo();
		User.auditWithdrawalDispass(1L, withdrawalId, reason, false, error);

		flash.error(error.msg);
		
		if(returnType.equalsIgnoreCase("2")){//该判断跳转到待付款提现列表
			toPayWithdraws();
		}else{
	        toReviewWithdraws();
		}
	}

	// 模拟登录
	public static void simulateLogin() {
		render();
	}

	/**
	 * 付款通知初始化
	 */
	public static void payNotificationInit(long withdrawalId) {
		ErrorInfo error = new ErrorInfo();

		long supervisorId = 1L;
		v_user_withdrawals withdrawal = User.withdrawalDetail(supervisorId,
				withdrawalId, error);

		JSONObject json = new JSONObject();

		json.put("error", error);
		json.put("withdrawal", withdrawal);

		renderJSON(json);
	}

	/**
	 * 付款通知
	 */
	public static void payNotification(long userId, long withdrawalId, String type) {
		ErrorInfo error = new ErrorInfo();
		
		if (StringUtils.isBlank(type)) {
			error.code = -1;
			error.msg = "请选择付款通知方式";
			
			renderJSON(error);
		}
		
		User.withdrawalNotice(userId, 0, withdrawalId, type, false, error); 

		renderJSON(error);
	}

	/**
	 * 打印付款单
	 */
	public static void printPayBill(long withdrawalId) {
		ErrorInfo error = new ErrorInfo();
		
		v_user_withdrawals withdrawal = User.printPayBill(
				withdrawalId, error);

		JSONObject json = new JSONObject();

		String status = null;
		
		json.put("error", error);
		json.put("name", withdrawal.name);
		json.put("amount", withdrawal.amount);
		json.put("time", DateUtil.dateToString(withdrawal.time));
		json.put("audit_time", DateUtil.dateToString(withdrawal.audit_time));
		
		switch (withdrawal.status) {
			case Constants.WITHDRAWAL_CHECK_PENDING: status = "待审核"; break;
			case Constants.WITHDRAWAL_PAYING: status = "付款中"; break;
			case Constants.WITHDRAWAL_SPEND: status = "已付款"; break;
			case Constants.WITHDRAWAL_NOT_PASS: status = "未通过 "; break;
		}
		json.put("status", status);
		withdrawal.account_name = withdrawal.account_name == null ? "" : withdrawal.account_name;
		withdrawal.account = withdrawal.account == null ? "" : withdrawal.account;
		withdrawal.bank_name = withdrawal.bank_name == null ? "" : withdrawal.bank_name;
		json.put("bankInfo", "收款人：" + withdrawal.account_name + 
				"<br/>账号：" + withdrawal.account + 
				"<br/>银行：" + withdrawal.bank_name);
		
		renderJSON(json);
	}

	//未通过提现详情
	public static void noPassWithdrawDetail() {
		
	}

	/**
	 * 未通过提现原因
	 */
	public static void noPassWithdrawReason(long withdrawalId) {
		ErrorInfo error = new ErrorInfo();

		String reason = User.withdrawalDispassReason(withdrawalId, error);

		JSONObject json = new JSONObject();

		json.put("error", error);
		json.put("reason", reason);

		renderJSON(json);
	}
	
	/**
	 * 选择会员
	 */
	public static void forRecharge(String name, String currPage, String pageSize) {
		ErrorInfo error = new ErrorInfo();

		PageBean<v_user_info> page = User.queryUserBySupervisor(name, null,
				null, null, null, null, currPage,pageSize, error);
		
		JSONObject json = new JSONObject();

		json.put("error", error);
		json.put("page", page);

		renderJSON(json);
	}

	/**
	 *  手工充值页面
	 */
	@Check(Constants.TRUST_FUNDS)
	public static void manualRecharge() {
		render();
	}
	
	/**
	 * 通过名字查询用户
	 * @param name
	 */
	public static void queryUserByName(String name) {
		ErrorInfo error = new ErrorInfo();
		t_users user = User.queryUserByName(name, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		renderJSON(user);
	}
	
	/**
	 * 手工充值
	 * @param name
	 * @param amount
	 */
	@Check(Constants.TRUST_FUNDS)
	public static void rechargeByHand(String name, double amount) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Supervisor.currSupervisor().id;
		User.rechargeByHand(supervisorId, name, amount, error);
		
		renderJSON(error);
	}

	/**
	 * 充值记录
	 */
	public static void rechargeRecord(int type, int status, String name, String startDate, String endDate, int currPage){
		PageBean<t_user_recharge_details> page = DealDetail.queryUserRechargeDetails(type, status, name, startDate, endDate, currPage);
		
		if(null == page)
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		
		render(page);
	}
	
	/**
	 * 提现记录
	 */
	public static void withdrawRecords(int currPage, int pageSize, String name, int status, String startDate, String endDate){
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_withdrawals> page = DealDetail.queryWithdrawRecords(currPage, pageSize, name, status, startDate, endDate, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}
	
	// 交易记录
	public static void transactionRecords(int type, int operation, int side,
			String beginTime, String endTime, String name, int currPage) {
        ErrorInfo error = new ErrorInfo();
		
		PageBean <v_platform_detail> page = DealDetail.platformDetail(type, operation, side, beginTime, endTime, name, currPage, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		List<t_platform_detail_types> allType = DealDetail.queryType(type, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		Map<String, Double> account = DealDetail.total(error);
		double income = account.get("income");
		double expense = account.get("expense");
		
		render(page, allType, income, expense);
	}

	// 本金保障账户管理
	// 本金保障principalProtection
	public static void ppAccountManagement() {
		render();
	}

	/**
	 * 本金保障账户概要
	 */
	public static void ppAccountInfo() {
		ErrorInfo error = new ErrorInfo();
		
		Map<String, Object> account = DealDetail.accountSummary(error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(account);
	}

	/**
	 *  添加保障金
	 */
	public static void addPrincipalProtection(double amount, String summary) {
		ErrorInfo error = new ErrorInfo();
		
		DealDetail.addCapital(amount, summary, error);
		
		if(error.code < 0){
			flash.error(error.msg);
			ppAccountInfo();
		}
		
		flash.success(error.msg);
		
		ppAccountInfo();
	}

	/**
	 *  保障金收支记录
	 */
	public static void principalProtectionRecords(int type, int operation, int side,
			String beginTime, String endTime, String name, int currPage) {
		ErrorInfo error = new ErrorInfo();
		
		PageBean <v_platform_detail> page = DealDetail.platformDetail(type, operation, side, beginTime, endTime, name, currPage, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		List<t_platform_detail_types> allType = DealDetail.queryType(type, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		Map<String, Double> total = DealDetail.total(error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page, allType, total);
	}
	
	/**
	 *  保障金收支记录
	 */
	public static void recordDetail(long id) {
		ErrorInfo error = new ErrorInfo();
		
		v_platform_detail detail = DealDetail.detail(id, error);
		
		renderJSON(detail);
	}
	
	/**
	 *  通过收入|支出来查找平台交易类型名称
	 */
	public static void queryDetailTypeNames(int type){
		ErrorInfo error = new ErrorInfo();
		
		List<t_platform_detail_types> types = DealDetail.queryType(type, error);

		renderJSON(types);
	}
	
	/**
	 * 支付掉单处理方法
	 * @param uid
	 * @param payNumber
	 */
	public static void offSingleDeal(String payNumber){
		
		ErrorInfo error = new ErrorInfo();
		String info = "";
		if(StringUtils.isBlank(payNumber)){
			info = "不存在的补单记录";
			render(info);
		}
		
		t_user_recharge_details user_recharge = null;
		user_recharge = t_user_recharge_details.find("pay_number = ? and is_completed = ?", payNumber, false).first();
		if(null == user_recharge){
			info = "不存在的补单记录";
			render(info);
		}
		
		if(user_recharge.payment_gateway_id == Constants.GO_GATEWAY){
			//国付宝掉单处理
			User.goffSingle(payNumber,user_recharge,error);
			
		}
		
        info =error.msg;
        render(info);
	}
	
	/**
	 * 环迅主动对账接口
	 * 需提供主动对账地址给环迅一定要严格区分http和https，按照实际的访问地址进行提交
	 * @param billno
	 * @param mercode
	 * @param Currency_type
	 * @param amount
	 * @param date
	 * @param succ
	 * @param msg
	 * @param attach
	 * @param ipsbillno
	 * @param retencodetype
	 * @param signature
	 */
	public static void ipsOffSingleDeal(String billno, String mercode, String Currency_type, String amount, String date, String succ,
			String msg, String attach, String ipsbillno, String retencodetype, String signature) {
		ErrorInfo error = new ErrorInfo();
		
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
		String info = "ipscheckok";
		if(!verify) {
			renderText(info);
			return;
		}
		
		if (succ == null) {
			info = "交易失败";
			renderText(info);
			return;
		}
		
		if(!succ.equalsIgnoreCase("Y")) {
			info = "ipscheckok";
			Logger.info("hxinfo_order_fail==:%s", billno);
			renderText(info);
			return;
		} 
		
		t_user_recharge_details user_recharge = null;
		user_recharge = t_user_recharge_details.find("pay_number = ? and is_completed = ?", billno, false).first();
		if(null == user_recharge){
			renderText(info);
			return;
		}
		
		User.recharge(billno, Double.parseDouble(amount), error);
		 
		if(error.code < 0) {
			renderText(info);
			return;
		}
		
		Logger.info("hxinfo_ok==:%s", billno);
		renderText(info);
	}
	
	/**
	 * 交易掉单
	 * @param currPage
	 * @param pageSize
	 * @param merBillNo
	 * @param userName
	 * @param type
	 * @param beginTime
	 * @param endTime
	 * @param status
	 */
	public static void ipsDetails(int currPage, int pageSize, String merBillNo, String userName, int type, 
			Date beginTime, Date endTime, int status) {
		ErrorInfo error = new ErrorInfo();
		PageBean<t_ips_details> page = IpsDetail.queryDetails(currPage, pageSize, merBillNo, userName, type, beginTime, endTime, status, error);
		
		boolean isWS = IPSConstants.IS_WS_UNFREEZE;
		
		render(page,isWS);
	}
	
	/**
	 * 交易补单
	 * @param merBillNo
	 * @param type
	 */
	public static void ipsRepair(String merBillNo, int type) {
		ErrorInfo error = new ErrorInfo();
		
		IpsDetail detail = new IpsDetail();
		detail.merBillNo = merBillNo;
		detail.type = type;
		detail.repair(error);
		
		renderJSON(error);
	}
}
