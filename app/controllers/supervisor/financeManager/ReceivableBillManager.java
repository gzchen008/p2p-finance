package controllers.supervisor.financeManager;

import java.util.Map;
import business.BackstageSet;
import business.Bid;
import business.Bill;
import business.Payment;
import business.Supervisor;
import constants.Constants;
import constants.IPSConstants.CompensateType;
import constants.IPSConstants.RegisterGuarantorType;
import controllers.supervisor.SupervisorController;
import controllers.supervisor.bidManager.BidPlatformAction;
import models.v_bid_repayment;
import models.v_bill_detail;
import models.v_bill_has_received;
import models.v_bill_receiving;
import models.v_bill_receiving_overdue;
import models.v_bill_receviable_statistical;
import models.v_bill_repayment_record;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;

/**
 * 
 * 类名:ReceivableBillManager
 * 功能:应收账单管理
 */

public class ReceivableBillManager extends SupervisorController {

	/**
	 * 待收款借款账单列表
	 */
	public static void toReceiveBills(){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_bill_receiving> page = Bill.queryBillReceiving(supervisor.id, yearStr, monthStr,
				typeStr, key, orderType, currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}

	/**
	 * 逾期账单列表
	 */
	public static void overdueBills(){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_bill_receiving_overdue> page = Bill.queryBillReceivingOverdue(supervisor.id, yearStr, monthStr,
				typeStr, key, orderType, currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}

	/**
	 * 已收款借款账单列表
	 */
	public static void receivedBills(){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_bill_has_received> page = Bill.queryBillHasReceived(supervisor.id, yearStr, monthStr,
				typeStr, key, orderType, currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}

	/**
	 * 已完成借款标列表
	 */
	public static void repaymentList(){
		ErrorInfo error = new ErrorInfo();
		
		PageBean<v_bid_repayment> pageBean = new PageBean<v_bid_repayment>();
		pageBean.page = Bid.queryBidRepayment(pageBean, 0, error, BidPlatformAction.getParameter(pageBean, null));

		if (null == pageBean.page) 
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR); 
		
		render(pageBean);
	}

	/**
	 * 应收款借款账单统计表
	 */
	public static void receivableBills(){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_bill_receviable_statistical> page = Bill.queryBillReceivedStatical(supervisor.id, yearStr, monthStr,
				 orderType, currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}

	/**
	 * 账单详情
	 */
	public static void billDetail(String billId, int type) { 
		int currPage = 1;
		String curPage = params.get("currPage");
		
		if(curPage != null) {
			currPage = Integer.parseInt(curPage);
		}
		
		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		v_bill_detail billDetail = Bill.queryBillDetails(id, error);
		PageBean<v_bill_repayment_record> page = Bill.queryBillReceivables(billDetail.bid_id, currPage, 0, error);
		BackstageSet backSet = BackstageSet.getCurrentBackstageSet();
		
		render(billDetail, page, backSet, type);
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

	//线下收款
	public static void offlineReceive(String billId, int type){
		//国付宝支持线下收款
		if(!Constants.IS_OFFLINERECEIVE) {
			overdueBills();
		}
		
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		if (error.code < 0) {
			flash.error(error.msg);
			overdueBills();
		}
		
		Bill bill = new Bill();
		bill.id = id;
		
		//本金垫付后线下收款
		if (bill.status == Constants.ADVANCE_PRINCIIPAL_REPAYMENT) {
			bill.offlineReceive(error);
			
			flash.error(error.msg);
			
			if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
				toReceiveBills();
			} else {
				overdueBills();
			}
		}
		
		if (Constants.IPS_ENABLE) {
			//判断是否需要登记担保方
			if(Constants.IS_GUARANTOR){
				if (!Bid.queryIsRegisterGuarantor(bill.bidId)) {
					Map<String, String> args = Payment.registerGuarantor(bill.bidId, type, error);
					
					render("@front.account.PaymentAction.registerGuarantor", args);
				}
			}
			
			String pMerBillNo = Bill.getMerBillNo(error, id);
			
			if (error.code < 0) {
				flash.error(error.msg);
				
				if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
					toReceiveBills();
				} else {
					overdueBills();
				}
			}
			
			Map<String,String> args = Payment.compensate(pMerBillNo, id, CompensateType.OFFLINE_REPAYMENT, error);
		
			if(error.code == 100) {
				render("front/account/PaymentAction/loan.html" ,args);
			}
		} else {
			bill.offlineCollection(Supervisor.currSupervisor().id, error);
		}
		
		flash.error(error.msg);
		
		if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
			toReceiveBills();
		} else {
			overdueBills();
		}
	}
	
}
