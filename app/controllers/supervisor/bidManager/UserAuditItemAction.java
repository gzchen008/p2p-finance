package controllers.supervisor.bidManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import constants.Constants;
import constants.Constants.MerToUserType;
import business.Product;
import controllers.supervisor.SupervisorController;
import business.Payment;
import business.Supervisor;
import business.User;
import business.UserAuditItem;
import business.Optimization.AuditItemOZ;
import models.v_user_audit_item_stats;
import models.v_user_audit_items;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;

/**
 * 会员借款资料审核管理
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-5-16 下午09:25:51
 */
public class UserAuditItemAction extends SupervisorController {
	
	/**
	 * 会员借款资料审核管理
	 */
	public static void userAuditItemList(int currPage, int pageSize, int condition, String keyword, int orderIndex, int orderStatus) {
		PageBean<v_user_audit_item_stats> pageBean = AuditItemOZ.queryStatistic(currPage, pageSize, condition, keyword, orderIndex, orderStatus);

		render(pageBean);
	}
	
	/**
	 * 审核明细
	 */
	public static void auditDetail(String signUserId){
		/* 解密userId */
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(userId < 1){
			flash.error(error.msg);
			
			userAuditItemList(1, 10, 0, "", 0, 0);
		}

		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String status = params.get("status");
		String startDate = params.get("startDate");
		String endDate = params.get("endDate");
		String productId = params.get("productId");
		String productType = params.get("productType");
		
		/* 产品名称列表 */
		List<Product> products = Product.queryProductNames(Constants.NOT_ENABLE, error);
		PageBean<v_user_audit_items> pageBean = UserAuditItem.queryUserAuditItem(currPage, pageSize, userId, error, status, startDate, endDate, productId, productType);
		/* 上一个,下一个 */
		UserAuditItem item = new UserAuditItem();
		item.userName = User.queryUserNameById(userId, error);
		item.userId = userId;
		
		if(null == pageBean.page || pageBean.page.size() == 0) {
			render(pageBean, item, signUserId);
		}
			
		/* 当前审核明细统计 */
		Map<String, Integer> auditStatistics = UserAuditItem.auditItemsStatistics(userId, error);

		render(pageBean, products, auditStatistics, signUserId, item);
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
	
	/**
	 * 审核页面(异步)
	 */
	public static void audititem(String mark, String signUserId){
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
	
	/**
	 * 审核同步get
	 * @param signUserId
	 * @param mark 资料唯一标示
	 * @param status 审核状态
	 * @param isVisible 是否可见
	 * @param suggestion 审核意见
	 * @param bidId 标的ID（可选）
	 * @param detail 标审核详情标示（可选）
	 */
	public static void audit(String signUserId, String mark, int status,
			boolean isVisible, String suggestion, long bidId, int detail,
			long overBorrowId) {
		/* 解密userId */
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(userId < 1){
			flash.error(error.msg);

			auditDetail(signUserId);
		}
		
		if( StringUtils.isBlank(mark) || 
			(status != Constants.AUDITED && status != Constants.NOT_PASS) || 
			StringUtils.isBlank(suggestion)   
		){
			flash.error("数据有误!");

			auditDetail(signUserId);
		}
		
		UserAuditItem item = new UserAuditItem();
		item.lazy = true;
		item.userId = userId;
		item.mark = mark;
		item.audit(Supervisor.currSupervisor().id, status, isVisible, suggestion, error);
		
		if (error.code == Constants.REFUND_ITEM_FEE) {
			List<Map<String, Object>> pDetails = new ArrayList<Map<String,Object>>();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("inCustId", User.queryIpsAcctNo(userId, error));
			map.put("transAmt", String.format("%.2f", item.auditItem.auditFee));
			pDetails.add(map);

			JSONObject memo = new JSONObject();
			memo.put("pPayType", MerToUserType.ItemFefund);
			memo.put("signUserId", signUserId);
			memo.put("userId", userId);
			memo.put("mark", mark);
			memo.put("supervisorId", Supervisor.currSupervisor().id);
			memo.put("status", status);
			memo.put("isVisible", isVisible);
			memo.put("suggestion", suggestion);
			
			/* 仅此做标示是否在标详情页审核 */
			memo.put("bidId", bidId); 
			memo.put("detail", detail); 
			memo.put("overBorrowId", overBorrowId);
			
			Map<String, String> args = Payment.transferMerToUser(pDetails, memo);
			
			render("@front.account.PaymentAction.transferMerToUser", args);
		}
		
		if(error.code < 0) {
			flash.error(error.msg);
		}

		if(overBorrowId > 1) {
			OverBorrowAction.overBorrowDetails(overBorrowId);
		}
		
		/* 标详情审核 */
		if(bidId > 1) {
			switch (detail) {
			case 1: BidPlatformAction.auditingDetail(bidId); break;
			case 2: BidPlatformAction.fundraiseingDetail(bidId); break;
			case 3: BidPlatformAction.fullDetail(bidId); break;
			}
		}
		
		auditDetail(signUserId);
	}
}
