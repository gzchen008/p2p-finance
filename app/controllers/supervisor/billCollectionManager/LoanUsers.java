package controllers.supervisor.billCollectionManager;

import constants.Constants;
import controllers.supervisor.SupervisorController;
import business.Bid;
import business.Supervisor;
import business.User;
import models.t_bids;
import models.v_bid_assigned;
import models.v_supervisors;
import models.v_user_loan_info_bad_d;
import models.v_user_loan_info_bill;
import models.v_user_loan_info_bill_d;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;

/**
 * 
 * 类名:loanUsers
 * 功能:借款会员管理
 */

public class LoanUsers extends SupervisorController {

	//我的会员账单-----借款会员管理
	public static void loanUserManager(){
        
		ErrorInfo error = new ErrorInfo();
		
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		String type = params.get("type");
		String startDate = params.get("startDate");
		String endDate = params.get("endDate");
		String orderType = params.get("orderType");
		String name = params.get("keywords");
		
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_user_loan_info_bill> page = new PageBean<v_user_loan_info_bill>();
		page = User.queryUserInfoBill(supervisor.id, type, startDate, endDate, name, orderType, currPageStr, pageSizeStr, error);
		
		render(page);
	}

	
	//借款标详情
	public static void bidDetail(long bidId, String light, int flag){
			Bid bid = new Bid();
			bid.bidDetail = true;
			bid.upNextFlag = flag;
			bid.id = bidId;
			
			render(bid, light, flag);
	}
	
	//借款标详情
	public static void bidDetailDept(long bidId){
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.id = bidId;
		
		renderTemplate("/supervisor/billCollectionManager/LoanUsers/bidDetail",bid);
	}
	
	//我的会员账单-----借款会员管理--借款标目录
	public static void userBidDetail(String sign,int type,String keywords,String status,int pageNum,int pageSize, String light){
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			renderJSON(error);
		}
		
		Supervisor supervisor = Supervisor.currSupervisor();
		PageBean<t_bids> page = Bid.queryUserInfoBillDetail( pageNum, pageSize,userId, type, supervisor.id, keywords, status, error);
		renderArgs.put("sign", sign);
		renderArgs.put("type", type);
		renderArgs.put("keywords", keywords);
		renderArgs.put("status", status);
		
		render(page, light);
	}
	
	
	/**
	 * 部门账单管理----已分配的借款会员管理
	 */
	public static void deptLoanUserManager(){
		
        ErrorInfo error = new ErrorInfo();
		
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		String type = params.get("type");
		String startDate = params.get("startDate");
		String endDate = params.get("endDate");
		String orderType = params.get("orderType");
		String name = params.get("keywords");
		
		
		PageBean<v_user_loan_info_bill_d> page = new PageBean<v_user_loan_info_bill_d>();
		page = User.queryUserInfoBillD( type, startDate, endDate, name, orderType, currPageStr, pageSizeStr, error);
		
		render(page);
	}
	
	/**
	 * 部门账单管理----已分配的借款标管理
	 */
	public static void deptLoanBidManager(){
		
        ErrorInfo error = new ErrorInfo();
		
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		String type = params.get("type");
		String startDate = params.get("startDate");
		String endDate = params.get("endDate");
		String _orderType = params.get("orderType");
		String orderType = _orderType == null ? "0" : _orderType;
		String name = params.get("keywords");
		
		
		PageBean<v_bid_assigned> page = new PageBean<v_bid_assigned>();
		page = User.queryBidInfoBillD( type, startDate, endDate, name, orderType, currPageStr, pageSizeStr, error);
		
		render(page);
	}
	
	/**
	 * 部门账单管理账单-----借款会员管理--借款标目录
	 * @param userId
	 * @param keywords
	 * @param status
	 * @param pageNum
	 * @param pageSize
	 */
	public static void deptLoanUserBidDetail(String sign,String keywords,String status,int pageNum,int pageSize,String light){
		
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			renderJSON(error);
		}
		
		PageBean<t_bids> page = Bid.queryDeptUserInfoBillDetail(pageNum, pageSize, userId, keywords, status, error);
		renderArgs.put("sign", sign);
		
		render(page, light);
	}
	
	/**
	 * 查询所有管理员
	 */
	public static void queryAllSupervisors(int currPage, int pageSize, String keyword, String userId, String type, String bidId){
		ErrorInfo error = new ErrorInfo();
		PageBean<v_supervisors> page = new PageBean<v_supervisors>();
		page = Supervisor.queryCustomers(currPage, pageSize, 0, 0, keyword, error);
		
		if(error.code < 0){
			renderJSON(error);
		}
		
		render(page,userId,type,bidId);
	}
	
	/**
	 * 部门账单管理----坏账会员管理
	 */
	public static void deptBadLoanUserManager(){
		
        ErrorInfo error = new ErrorInfo();
		
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		String type = params.get("type");
		String startDate = params.get("startDate");
		String endDate = params.get("endDate");
		String orderType = params.get("orderType");
		String name = params.get("keywords");
		
		
		PageBean<v_user_loan_info_bad_d> page = new PageBean<v_user_loan_info_bad_d>();
		page = User.queryUserInfoBadD( type, startDate, endDate, name, orderType, currPageStr, pageSizeStr, error);
		
		render(page);
	}
	
	/**
	 * 部门账单管理账单-----坏账会员管理--借款标目录
	 * @param userId
	 * @param keywords
	 * @param status
	 * @param pageNum
	 * @param pageSize
	 */
	public static void deptUserBidDetail(String sign,String keywords,String status,int pageNum,int pageSize){
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			renderJSON(error);
		}
		
		PageBean<t_bids> page = Bid.queryDeptUserInfoBillDetail(pageNum, pageSize, userId, keywords, status, error);
		renderArgs.put("sign", sign);
		
		render(page);
	}

}
