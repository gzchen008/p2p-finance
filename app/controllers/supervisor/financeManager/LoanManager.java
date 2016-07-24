package controllers.supervisor.financeManager;

import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;
import constants.Constants;
import constants.Constants.MerToUserType;
import constants.Constants.PayType;
import controllers.MaliceFalsifyCheck;
import controllers.SubmitCheck;
import controllers.SubmitOnly;
import controllers.SubmitRepeat;
import controllers.supervisor.SupervisorController;
import controllers.supervisor.bidManager.BidPlatformAction;
import models.v_bid_release_funds;
import business.Bid;
import business.IpsDetail;
import business.Payment;
import business.Supervisor;
import business.UserBankAccounts;
import play.mvc.With;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;

/**
 * 放款管理
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-5-28 上午10:13:37
 */
@With({MaliceFalsifyCheck.class, SubmitRepeat.class})
public class LoanManager extends SupervisorController {
	
	/**
	 * 等待放款
	 */
	@SubmitCheck
	public static void readyReleaseList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_release_funds> pageBean = new PageBean<v_bid_release_funds>();
		pageBean.page = Bid.queryReleaseFunds(pageBean, Constants.BID_EAIT_LOAN, error, BidPlatformAction.getParameter(pageBean, null));

		if (null == pageBean.page) 
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);  
		
		boolean ipsEnable = constants.Constants.IPS_ENABLE;
		
		render(pageBean, ipsEnable);
	}

	/**
	 * 已放款
	 */
	public static void alreadyReleaseList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_release_funds> pageBean = new PageBean<v_bid_release_funds>();
		pageBean.page = Bid.queryReleaseFunds(pageBean, Constants.BID_REPAYMENT, error, BidPlatformAction.getParameter(pageBean, null));

		if (null == pageBean.page) 
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);  

		render(pageBean);
	}
	
	/**
	 * 用户账户信息
	 */
	public static void userBank(long id, int bankId){
		UserBankAccounts bank = null;
		List<UserBankAccounts> banks = null;
		
		if(bankId != 0) {
			bank = new UserBankAccounts();
			bank.id = bankId;
		}
		
		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = id;
		
		if(bid.status == Constants.BID_EAIT_LOAN)
			banks = UserBankAccounts.queryUserAllBankAccount(bid.userId);
		
		render(bank, banks, bid);
	}
	
	/**
	 * 放款
	 */
	@SubmitOnly
	public static void releaseAudit(String sign, String uuidRepeat) {
		/* 解密BidId */
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			readyReleaseList();
		}
		
		Bid bid = new Bid();
		//bid.auditBid = true;
		bid.id = bidId;
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		bid.eaitLoanToRepayment(error);
		
		if (Constants.IPS_ENABLE) {
			if (error.code < 0) {
				flash.error(error.msg); 
				
				readyReleaseList();
			}
			
			//String pMerBillNo = Payment.createBillNo(bid.userId, IPSOperation.TRANSFER);
			String pMerBillNo = bid.bidNo;
			IpsDetail.setIpsInfo(Long.parseLong(pMerBillNo), "{\"bidId\":\""+bid.id+"\"}", error);
//			Cache.set(pMerBillNo, bid, IPSConstants.CACHE_TIME);
			Map<String,String> args = Payment.transfer(pMerBillNo, bidId, error);
			
			if(error.code == 100){//双钱ws转post提交
				
				render("front/account/PaymentAction/loan.html" ,args);
			}
			
			//发放奖励方式配置为资金托管模式
			if (Constants.PAY_TYPE_FUND == PayType.IPS && bid.bonusType != Constants.NOT_REWARD) {
				List<Map<String, Object>> pDetails = bid.queryInvestFunds(error);
				JSONObject memo = new JSONObject();
				memo.put("pPayType", MerToUserType.Fund);
				memo.put("pMerBillNo", pMerBillNo);
				args = Payment.transferMerToUser(pDetails, memo);
				
				render("@front.account.PaymentAction.transferMerToUser", args);
			}
			
			flash.error(error.msg);
			
			readyReleaseList();
		}
		
		flash.error(error.msg); 

		readyReleaseList();
	}
	
	/**
	 * 放款标记
	 */
	public static void releaseSign(String sign){
		if(Constants.IPS_ENABLE){
			flash.error("资金托管模式不允放款标记!"); 
			
			readyReleaseList();
		}
		
		/* 解密BidId */
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 
			
			readyReleaseList();
		}
		
		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.isReleaseSign = true;
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		bid.releaseSign(error);
		
		if (Constants.IPS_ENABLE) {
			if (error.code < 0) {
				flash.error(error.msg); 
				
				readyReleaseList();
			}
			
			//String pMerBillNo = Payment.createBillNo(bid.userId, IPSOperation.TRANSFER);
			String pMerBillNo = bid.bidNo;
			IpsDetail.setIpsInfo(Long.parseLong(pMerBillNo), "{\"bidId\":\""+bid.id+"\"}", error);
//			Cache.set(pMerBillNo, bid, IPSConstants.CACHE_TIME);
			Payment.transfer(pMerBillNo, bidId, error);
			
			renderText("放款标记\n"+error.msg);
			//render("@front.account.PaymentAction.transfer", args);
		}
		
		flash.error(error.msg); 
		
		readyReleaseList();
	}
	
	/**
	 * 详情
	 */
	public static void detail(long bidid, int type) { 
		
		if(0 == bidid)  render();
		
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = Constants.BID_DFK; // 这里至为4和5是等价的
		bid.id = bidid;
		
		render(bid, type);
	}
}
