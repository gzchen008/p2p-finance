package controllers.supervisor.financeManager;

import java.util.Map;
import net.sf.json.JSONObject;
import models.t_bill_invests;
import models.v_bid_bad;
import models.v_bill_invest_detail;
import models.v_bill_invests_overdue_unpaid;
import models.v_bill_invests_paid;
import models.v_bill_invests_payables_statistics;
import models.v_bill_invests_pending_payment;
import models.v_bill_invests_principal_advances;
import business.BackstageSet;
import business.Bid;
import business.Bill;
import business.BillInvests;
import business.Payment;
import business.Supervisor;
import constants.Constants;
import constants.IPSConstants.CompensateType;
import constants.IPSConstants.RegisterGuarantorType;
import controllers.supervisor.SupervisorController;
import controllers.supervisor.bidManager.BidPlatformAction;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;

/**
 * 
 * @ClassName:      PayableBillManager
 * @Description:	应付账单管理
 */

public class PayableBillManager extends SupervisorController {

	/**
	 * 待付款理财账单列表
	 */
	public static void toPayBills(){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_bill_invests_pending_payment> page = Bill.queryBillInvestPending(supervisor.id, yearStr, monthStr,
				typeStr, key, orderType, currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}

	/**
	 * 逾期未付理财账单列表
	 */
	public static void overdueUnpaidBills(){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_bill_invests_overdue_unpaid> page = Bill.queryBillOverdueUnpaid(supervisor.id, yearStr, monthStr,
				typeStr, key, orderType, currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}

	/**
	 * 已付款理财账单列表
	 */
	public static void paidBills(){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String paidType = params.get("paidType");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_bill_invests_paid> page = Bill.queryBillInvestPaid(supervisor.id, yearStr, monthStr,
				typeStr, key, paidType, orderType, currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}

	/**
	 * 本金垫付理财账单列表
	 */
	public static void principalAdvanceBills(){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_bill_invests_principal_advances> page = Bill.queryBillPrincipalAdvances(supervisor.id, yearStr, monthStr,
				typeStr, key, orderType, currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}

	/**
	 * 应付款理财账单统计表
	 */
	public static void payableBills(){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_bill_invests_payables_statistics> page = Bill.queryBillInvestStatistics(supervisor.id, yearStr, monthStr,
				 orderType, currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}

	/**
	 * 坏账借款标列表
	 */
	public static void badList(){
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_bad> pageBean = new PageBean<v_bid_bad>();
		pageBean.page = Bid.queryBidBad(pageBean, 0, error, BidPlatformAction.getParameter(pageBean, null));

		if (null == pageBean.page) 
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);  
		
		render(pageBean);
	}

	/**
	 * 账单详情
	 * @param billInvestId
	 */
	public static void investBillDetails(String billId, int type, int currPage){
		ErrorInfo error = new ErrorInfo();
		int pageSize = Constants.FIVE;
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		v_bill_invest_detail investDetail = BillInvests.queryMyInvestBillDetails(id, error);
		PageBean<t_bill_invests> page = BillInvests.queryMyInvestBillReceivables(investDetail.bid_id, investDetail.user_id, investDetail.invest_id, currPage,pageSize, error);
		BackstageSet backSet = BackstageSet.getCurrentBackstageSet();
		
		render(investDetail, backSet, page, type);
	}
	
	/**
	 * 待付款账单详情
	 * @param billInvestId
	 */
	public static void investBillForPay(String billId, int type, int status, int currPage){
		ErrorInfo error = new ErrorInfo();
		int pageSize = Constants.FIVE;
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		v_bill_invest_detail investDetail = BillInvests.queryMyInvestBillDetails(id, error);
		PageBean<t_bill_invests> page = BillInvests.queryMyInvestBillReceivables(investDetail.bid_id, investDetail.user_id, investDetail.invest_id, currPage,pageSize, error);
		BackstageSet backSet = BackstageSet.getCurrentBackstageSet();
		
		render(investDetail, backSet, page, type, status);
	}
	
	/**
	 * 对待付款理财账单付款
	 * @param billInvestId
	 */
	public static void payInvestBill(String investId){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		long id = Security.checkSign(investId, Constants.BILL_ID_SIGN, 3600, error);
		
		if(error.code < 0){
			json.put("error", error);
			renderJSON(json.toString());
			
		}
		
		Bill.investForPayment(id, error);
		
		if(error.code < 0){
			json.put("error", error);
			renderJSON(json.toString());
			
		}
		
		json.put("error", error);
		
		renderJSON(json.toString());
	}

	/**
	 * 借款标详情
	 */
	public static void bidDetail(long bidid, int type, int flag) { 
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = flag;
		bid.id = bidid;
		
		render(bid, type, flag);
	}
	
	//本金垫付
	public static void principalAdvance(int status, String billId, long bidId, int period){
		//国付宝支持线下收款
		if(!Constants.IS_OFFLINERECEIVE) {
			overdueUnpaidBills();
		}
		
		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		if (error.code < 0) {
			flash.error(error.msg);

			overdueUnpaidBills();
		}
		
		if (Constants.IPS_ENABLE) {
			if(Constants.IS_GUARANTOR){
				if (!Bid.queryIsRegisterGuarantor(bidId)) {
					Map<String, String> args = Payment.registerGuarantor(bidId, RegisterGuarantorType.COMPENSATE, error);
					
					render("@front.account.PaymentAction.registerGuarantor", args);
				}
			}
			
			String pMerBillNo = Bill.getMerBillNo(error, id);
			
			if (error.code < 0) {
				flash.error(error.msg);
				overdueUnpaidBills();
			}
			
			Map<String,String> args = Payment.compensate(pMerBillNo, id, CompensateType.COMPENSATE, error);
			
			if(error.code == 100) {
				render("front/account/PaymentAction/loan.html" ,args);
			}
		} else {
			Bill bill = new Bill();
			Supervisor supervisor = Supervisor.currSupervisor();
			bill.principalAdvancePayment(supervisor.id, id, error);
		}

		flash.error(error.msg);
		overdueUnpaidBills();
	}

}
