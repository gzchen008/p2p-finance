package controllers.supervisor.bidManager;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import net.sf.json.JSONObject;
import business.Bid;
import business.BidQuestions;
import business.Bill;
import business.Invest;
import business.Payment;
import business.ProductAuditItem;
import business.StationLetter;
import business.Supervisor;
import business.User;
import business.UserAuditItem;
import constants.Constants;
import constants.IPSConstants;
import controllers.supervisor.SupervisorController;
import models.t_user_report_users;
import models.v_bid_auditing;
import models.v_bid_bad;
import models.v_bid_fundraiseing;
import models.v_bid_not_through;
import models.v_bid_overdue;
import models.v_bid_repaymenting;
import models.v_bid_repayment;
import models.v_bill_detail;
import models.v_bill_loan;
import models.v_bill_repayment_record;
import models.v_invest_records;
import models.v_user_audit_items;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;

/**
 * 平台借款标 Action
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-25 上午08:43:32
 */
public class BidPlatformAction extends SupervisorController {

	/**
	 * 获取 参数值
	 * @param pageBean 当前模板PageBean
	 * @return String [] 参数值
	 */
	public static String [] getParameter(PageBean pageBean, String userId){
		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数
		String condition = params.get("condition"); // 条件
		String keyword = params.get("keyword"); // 关键词
		String startDate = params.get("startDate"); // 开始时间
		String endDate = params.get("endDate"); // 结束时间
		String orderIndex = params.get("orderIndex"); // 排序索引
		String orderStatus = params.get("orderStatus"); // 升降标示
		
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		
		/* ""/null:标示非用户ID查询  */
		return new String[]{userId, condition, keyword, startDate, endDate, orderIndex, orderStatus};
	}
	
	/**
	 * 审核中的借款标列表
	 */
	public static void auditingList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_auditing> pageBean = new PageBean<v_bid_auditing>();
		pageBean.page = Bid.queryBidAuditing(pageBean, error, getParameter(pageBean, null));

		render(pageBean);
	}

	/**
	 *借款中的借款标列表
	 */
	public static void fundraiseingList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_fundraiseing> pageBean = new PageBean<v_bid_fundraiseing>();
		pageBean.page = Bid.queryBidFundraiseing(pageBean, Constants.V_FUNDRAISEING, error, getParameter(pageBean, null));
		
		render(pageBean);
	}

	/**
	 *满标待放款
	 */
	public static void fullList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_fundraiseing> pageBean = new PageBean<v_bid_fundraiseing>();
		pageBean.page = Bid.queryBidFundraiseing(pageBean, Constants.V_FULL, error, getParameter(pageBean, null));

		render(pageBean);
	}

	/**
	 *还款中的借款标列表
	 */
	public static void repaymentingList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_repaymenting> pageBean = new PageBean<v_bid_repaymenting>();
		pageBean.page = Bid.queryBidRepaymenting(pageBean, 0, error, getParameter(pageBean, null));

		render(pageBean);
	}

	/**
	 *逾期的借款标
	 */
	public static void overdueList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_overdue> pageBean = new PageBean<v_bid_overdue>();
		pageBean.page = Bid.queryBidOverdue(pageBean, error, getParameter(pageBean, null));

		render(pageBean);
	}

	/**
	 *已完成的借款标列表的搜索
	 */
	public static void repaymentList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_repayment> pageBean = new PageBean<v_bid_repayment>();
		pageBean.page = Bid.queryBidRepayment(pageBean, 0, error, getParameter(pageBean, null));
		
		render(pageBean);
	}

	/**
	 *未通过的借标列表款
	 */
	public static void notThroughList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_not_through> pageBean = new PageBean<v_bid_not_through>();
		pageBean.page = Bid.queryBidNotThrough(pageBean, error, getParameter(pageBean, null));
		
		render(pageBean);
	}

	/**
	 *坏账借款标列表
	 */
	public static void badList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_bad> pageBean = new PageBean<v_bid_bad>();
		pageBean.page = Bid.queryBidBad(pageBean, 0, error, getParameter(pageBean, null));

		render(pageBean);
	}
	
	/**
	 * 审核中的借款标详情
	 */
	public static void auditingDetail(long bidId){
		
		if(0 == bidId) render();
		
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = Constants.BID_SHZ;
		bid.id = bidId;
		
		render(bid);
	}
	
	/**
	 * 募集中的借款标详情
	 */
	public static void fundraiseingDetail(long bidId){
		
		if(0 == bidId) render();
		
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = Constants.BID_JKZ;
		bid.id = bidId;
		
		render(bid);
	}
	
	/**
	 * 满标的借款标详情
	 */
	public static void fullDetail(long bidId){
		
		if(0 == bidId) render();
		
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = Constants.BID_MBZ;
		bid.id = bidId;
		
		render(bid);
	}
	
	/**
	 * 借款成功(还款中的、已完成的、逾期的、坏账的借款标详情)的标详情
	 */
	public static void loanSucceedDetail(long bidId, int type, int falg){
		
		if(0 == bidId) render();
		
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = falg;
		bid.id = bidId;
		
		render(bid, type, falg);
	}
	
	/**
	 * 借款失败,初核不通过、借款中不通过、流标、撤销、放款审核不通过详情
	 */
	public static void notThroughDetail(long bidId){
		
		if(0 == bidId) render();
		
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = Constants.BID_SBZ;
		bid.id = bidId;
		
		render(bid);
	}
	
	/*public static void userItemsList(int currPage, String signUserId, long productId, int status) {
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(userId < 1)
			renderText(error.msg);
		
		PageBean<v_user_audit_items> pageBean = UserAuditItem.queryUserAuditItem(currPage + "", null, userId, error, null, null, null, productId + "", null);
		Product product = new Product();
		product.bidDetail = true;
		product.id = productId;
		
		render(pageBean, product, status);
	}*/
	
	/**
	 * 资料列表
	 */
	public static void userItemsList(String signUserId, long productId, int status, String mark) {
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(userId < 1)
			renderText(error.msg);
		
		List<v_user_audit_items> items = UserAuditItem.queryUserAuditItem(userId, productId, error);
		List<ProductAuditItem> requiredAuditItem = ProductAuditItem.queryAuditByProductMark(mark, false, Constants.NEED_AUDIT);

		render(items, requiredAuditItem, status);
	}
	
	/**
	 * 查询借款标的所有提问记录异步分页方法
	 */
	public static void bidQuestion(int currPage, long bidId){
		
		if(0 == bidId) render();
		
		ErrorInfo error = new ErrorInfo();
		PageBean<BidQuestions> pageBean = BidQuestions.queryQuestion(currPage, 1, bidId, "", Constants.SEARCH_ALL, -1, error);
		
		render(pageBean, error);
	}

	/**
	 * 投标记录
	 */
	public static void bidRecord(int currPage, long bidId) {
		
		if(0 == bidId) render();
		
		ErrorInfo error = new ErrorInfo();
		PageBean<v_invest_records> pageBean = new PageBean<v_invest_records>();
		pageBean.currPage = currPage;
		pageBean.page = Invest.bidInvestRecord(pageBean, bidId, error);
		
		render(pageBean);
	}
	
	/**
	 * 历史记录
	 */
	public static void historyDetail(Date time, String signUserId) {
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(userId < 1)
			renderText(error.msg);
		
		Map<String, String> historySituationMap = User.historySituation(userId,error);// 借款者历史记录情况
		
		render(time, historySituationMap);
	}
	
	/**
	 * 举报记录
	 */
	public static void reportRecord(int currPage, String signUserId){
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(userId < 1)
			renderText(error.msg);
		
		PageBean<t_user_report_users> pageBean = new PageBean<t_user_report_users>();
		pageBean.currPage = currPage;
		pageBean.page = User.queryBidRecordByUser(pageBean, userId, error);
		
		render(pageBean);
	}
	
	/**
	 * 还款情况
	 */
	public static void repaymentSituation(int currPage, long bidId){

		if(0 == bidId) render();

		ErrorInfo error = new ErrorInfo();
		PageBean<v_bill_loan> pageBean = new PageBean<v_bill_loan>();
		pageBean.currPage = currPage;
		pageBean.page = Bill.queryMyLoanBills(pageBean, -1, bidId, error);

		render(pageBean);
	}
	
	/**
	 * 还款情况详情
	 */
	public static void repaymentSituationDetail(int currPage, long billId) { 
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		
		v_bill_detail billDetail = Bill.queryBillDetails(billId, user.id, error);
		PageBean<v_bill_repayment_record> page = Bill.queryBillReceivables(billDetail.bid_id, currPage, 0, error);

		render(billDetail, page);
	}

	/**
	 * 管理员给用户发送站内信
	 */
	public static void sendMessages(String signUserId, String title, String content) {
		/* 解密userId */
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
	
		if(userId < 1)
			renderText(error.msg);
		
		if (StringUtils.isBlank(title) || StringUtils.isBlank(content))
			renderText("数据有误!");

		StationLetter letter = new StationLetter();
		letter.senderSupervisorId = Supervisor.currSupervisor().id;
		letter.receiverUserId = userId;
		letter.title = title;
		letter.content = content;
		letter.sendToUserBySupervisor(error);
		
		renderText(error.msg);
	}
	
	/**
	 * 设置优质标
	 */
	public static void siteQuality(long bidId, boolean status) {

		if(0 == bidId) renderText("设置出错!");
		
		ErrorInfo error = new ErrorInfo();
		Bid.editQuality(bidId, status, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 设置"火"标
	 */
	public static void siteHot(long bidId, boolean status) {

		if(0 == bidId) renderText("设置出错!");
		
		ErrorInfo error = new ErrorInfo();
		Bid.editHot(bidId, status, error);

		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 *  审核中->提前借款 
	 */
	public static void auditToadvanceLoan(String sign) {
		/* 解密BidId */
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			auditingList();
		}
		
		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		
		bid.auditToadvanceLoan(error);
		flash.error(error.msg); 
		
		auditingList();
	} 
	
	/**
	 *  审核中->募集中 
	 */
	public static void auditToFundraise(String sign) {
		checkAuthenticity();
		
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			auditingList();
		}

		String suggest = params.get("suggest");
		
		if(StringUtils.isBlank(suggest)){
			flash.error("数据有误!"); 

			auditingList();
		}
		
		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.auditSuggest = suggest; // 审核意见
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		
		bid.auditToFundraise(error);
		flash.error(error.msg); 
			
		auditingList();
	}
	
	/** 
	 * 提前借款->募集中 
	 */
	public static void advanceLoanToFundraise(String sign) { 
		checkAuthenticity();
		
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			auditingList();
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		
		String suggest = params.get("suggest");
		
		if(StringUtils.isBlank(suggest)){
			flash.error("数据有误!"); 

			if(bid.hasInvestedAmount == bid.amount)
				fullList();
			
			fundraiseingList();
		}
		
		bid.auditSuggest = suggest; // 审核意见
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		
		bid.advanceLoanToFundraise(error);
		flash.error(error.msg); 
			
		if(bid.hasInvestedAmount == bid.amount)
			fullList();
		
		fundraiseingList();
	}
	
	/** 
	 * 审核中->审核不通过
	 */
	public static void auditToNotThrough(String sign) { 
		checkAuthenticity();
		
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			auditingList();
		}

		String suggest = params.get("suggest");
		
		if(StringUtils.isBlank(suggest)){
			flash.error("数据有误!"); 

			auditingList();
		}
		
		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.auditSuggest = suggest; // 审核意见
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		
		bid.auditToNotThrough(error);
		
		if(Constants.IPS_ENABLE && error.code >= 0) {
			Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CANCEL_S, bid);
			
			render("@front.account.PaymentAction.registerSubject", args);
		}
		
		flash.error(error.msg);
			
		auditingList();
	}
	
	/** 
	 * 提前借款->借款中不通过 
	 */
	public static void advanceLoanToPeviewNotThrough(String sign) { 
		checkAuthenticity();
		
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			auditingList();
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		
		String suggest = params.get("suggest");
		
		if(StringUtils.isBlank(suggest)){
			flash.error("数据有误!"); 

			if(bid.hasInvestedAmount == bid.amount)
				fullList();
			
			fundraiseingList();
		}
		
		bid.auditSuggest = suggest; // 审核意见
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		
		bid.advanceLoanToPeviewNotThrough(error);
		
		if(Constants.IPS_ENABLE) {
			Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CANCEL_B, bid);
			
			render("@front.account.PaymentAction.registerSubject", args);
		}
		
		flash.error(error.msg); 
			
		if(bid.hasInvestedAmount == bid.amount)
			fullList();
		
		fundraiseingList();
	}
	
	/**
	 *  募集中->借款中不通过 
	 */
	public static void fundraiseToPeviewNotThrough(String sign) { 
		checkAuthenticity();
		
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			auditingList();
		}
		
		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		
		bid.fundraiseToPeviewNotThrough(error);
		
		if(Constants.IPS_ENABLE && error.code >= 0) {
			Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CANCEL_I, bid);
			
			render("@front.account.PaymentAction.registerSubject", args);
		}
		
		flash.error(error.msg); 
			
		fundraiseingList();
	}
	
	/** 
	 * 满标->待放款 
	 */
	public static void fundraiseToEaitLoan(String sign) { 
		checkAuthenticity();
		
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			auditingList();
		}
		
		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		
		bid.fundraiseToEaitLoan(error);
		flash.error(error.msg); 
			
		fullList();
	}
	
	/**
	 *  满标->放款不通过 
	 */
	public static void fundraiseToLoanNotThrough(String sign) { 
		checkAuthenticity();
		
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			auditingList();
		}
		
		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		
		bid.fundraiseToLoanNotThrough(error);
		
		if(Constants.IPS_ENABLE && error.code >= 0) {
			Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CANCEL_M, bid);
			
			render("@front.account.PaymentAction.registerSubject", args);
		}
		
		flash.error(error.msg); 
			
		fullList();
	}
}
