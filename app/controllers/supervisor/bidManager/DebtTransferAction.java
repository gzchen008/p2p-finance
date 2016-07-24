package controllers.supervisor.bidManager;

import java.util.List;
import java.util.Map;
import constants.Constants;
import controllers.Unit;
import controllers.UnitCheck;
import controllers.supervisor.SupervisorController;
import net.sf.json.JSONObject;
import models.v_debt_auction_records;
import models.v_debt_auditing_transfers;
import models.v_debt_no_pass_transfers;
import models.v_debt_transfer_failure;
import models.v_debt_transfering;
import models.v_debt_transfers_success;
import business.Debt;
import business.Supervisor;
import business.User;
import business.UserAuditItem;
import play.mvc.With;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;

/**
 *债权转让管理
 * @author zhs
 *
 */
@With(UnitCheck.class)
public class DebtTransferAction extends SupervisorController {
	
	
	/**
	 *待审核债权转让标
	 */
	@Unit(2)
	public static void debtTransferPending(){
		
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		String startDateStr = params.get("startDateStr");
		String endDateStr = params.get("endDateStr");
		String orderType = params.get("orderType");
		String typeStr = params.get("typeStr");
		String keyWords = params.get("keyWords");
		
        PageBean<v_debt_auditing_transfers>  page = Debt.queryAllAuditingTransfers( typeStr,startDateStr,endDateStr, keyWords, orderType,currPageStr,pageSizeStr);
        
        if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
        render(page);
	}
	
	
	
	
	/**
	 *转让中债权转让标
	 */
	@Unit(2)
	public static void debtIsTransfer(){
		                 
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		String orderType = params.get("orderType");
		String typeStr = params.get("typeStr");
		String keyWords = params.get("keyWords");
		
		
        PageBean<v_debt_transfering>  page = Debt.queryAllTransferingDebts( typeStr, keyWords, orderType,currPageStr,pageSizeStr);
         
        render(page);
	}
	
	
	
	
	
	
	/**
	 *成功的债权转让标
	 */
	@Unit(2)
	public static void successDebtTransfer(){
		                   
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		String startDateStr = params.get("startDateStr");
		String endDateStr = params.get("endDateStr");
		String orderType = params.get("orderType");
		String typeStr = params.get("typeStr");
		String keyWords = params.get("keyWords");
		
		
		
        PageBean<v_debt_transfers_success>  page = Debt.queryAllSuccessedDebts(typeStr,startDateStr,endDateStr, keyWords, orderType,currPageStr,pageSizeStr);
         
        render(page);
	}
	
	
	
	
	
	/**
	 *未通过的转让债权标
	 */
	@Unit(2)
	public static void nopassAssignedClaims(){
		                   
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		String startDateStr = params.get("startDateStr");
		String endDateStr = params.get("endDateStr");
		String orderType = params.get("orderType");
		String typeStr = params.get("typeStr");
		String keyWords = params.get("keyWords");
		
		 PageBean<v_debt_no_pass_transfers>  page = Debt.queryAllNopassDebts( typeStr,startDateStr,endDateStr, keyWords, orderType,currPageStr,pageSizeStr);
		 
		 render(page);
	}
	
	
	/**
	 *失败的债权转让标
	 */
	@Unit(2)
	public static void failedDebtTransfer(){
		             
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		String startDateStr = params.get("startDateStr");
		String endDateStr = params.get("endDateStr");
		String orderType = params.get("orderType");
		String typeStr = params.get("typeStr");
		String keyWords = params.get("keyWords");
		
		
	    PageBean<v_debt_transfer_failure>  page = Debt.queryAllFailureDebts( typeStr,startDateStr,endDateStr, keyWords, orderType,currPageStr,pageSizeStr);
	         
	     render(page);
	}
	
	
	
	
	
	/**
	 *待审核债权转让标的搜索
	 */
	@Unit(2)
	public static void TransferPeningSerch(){
		                    render();
	}
	
	/**
	 *待审核债权转让标的查看方式
	 */
	@Unit(2)
	public static void TransferPendingMode(){
		                    render();
	}
	
	/**
	 * 审核通过
	 * @param debtId
	 * @param type
	 * @param qualityStatus
	 */
	@Unit(2)
	public static void audit(String sign,int type,int qualityStatus){
		 
		ErrorInfo error = new ErrorInfo();
		
		long debtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		Supervisor supervisor = Supervisor.currSupervisor();
		
		Debt.auditDebtTransferPass(debtId, type, qualityStatus, supervisor.id, error);
		
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json);
	}
	
	
	
	
	
	
	/**
	 * 审核不通过操作
	 * @param debtId
	 * @param type
	 * @param qualityStatus
	 * @param nothroughReason
	 */
	@Unit(2)
	public static void notThrough(String sign,int type,String nothroughReason){
		                    
		ErrorInfo error = new ErrorInfo();
		long debtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		Supervisor supervisor = Supervisor.currSupervisor();
		
		Debt.auditDebtTransferNoPass(debtId, supervisor.id,nothroughReason, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json);
	}
	
	
	
	/**
	 *不通过页面的点击确定
	 */
	@Unit(2)
	public static void notThroughOk(){
		                    render();
	}
	
	
	
	/**
	 *转让中债权转让标的搜索
	 */
	@Unit(2)
	public static void debtIsTransferSerch(){
		                    render();
	}
	
	/**
	 *转让中债权转让标的查看方式
	 */
	@Unit(2)
	public static void debtIsTransferMode(){
		                    render();
	}
	
	
	/**
	 * 上一条记录
	 * @param debtId
	 * @param status
	 */
	@Unit(2)
	public static void ahead(String sign,int status,int type){
		
		long tempId = -1;
		ErrorInfo error = new ErrorInfo();
		
		long debtId =  Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		if(status == 0){//审核中
			
			tempId = Debt.auditingAhead(debtId, status, error);
			
			if(error.code < 0){
				render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
			}
			
			String signstr = Security.addSign(tempId, Constants.BID_ID_SIGN);
			transferDetails( signstr, status, type);
		}
		
		if (status == 1 || status == 2) {//竞拍转让中
			
			tempId = Debt.auctioningAhead(debtId, status, error);
			
			if(error.code < 0){
				render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
			}
			String signstr = Security.addSign(tempId, Constants.BID_ID_SIGN);
			transferDetails( signstr, status, type);
		}
		
		if (status == 3) {//已成功
			
			tempId = Debt.successAhead(debtId, status, error);
			
			if(error.code < 0){
				render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
			}
			String signstr = Security.addSign(tempId, Constants.BID_ID_SIGN);
			transferDetails( signstr, status, type);
		}
		
		if (status == -1) {//审核不通过
			
			tempId = Debt.nopassAhead(debtId, status, error);
			
			if(error.code < 0){
				render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
			}
			String signstr = Security.addSign(tempId, Constants.BID_ID_SIGN);
			transferDetails( signstr, status, type);
		}
		
		if (status == -2 || status == -3 || status == -5) {//失败的
			
			tempId = Debt.failureAhead(debtId, status, error);
			
			if(error.code < 0){
				render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
			}
			String signstr = Security.addSign(tempId, Constants.BID_ID_SIGN);
			transferDetails( signstr, status, type);
		}
	}
	
	
	/**
	 * 下一条记录
	 * @param debtId
	 * @param status
	 */
	@Unit(2)
	public static void back(String sign,int status, int type){
		
		long tempId = -1;
		ErrorInfo error = new ErrorInfo();
		
		long debtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		if (status == 0) {// 审核中
			
			tempId = Debt.auditingBack(debtId, status, error);
			
			if(error.code < 0){
				render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
			}
			
			String signstr = Security.addSign(tempId, Constants.BID_ID_SIGN);
			
			transferDetails( signstr, status, type);
			
		}
		
		if (status == 1 || status == 2 || status == 4) {//竞拍转让中
			
			tempId = Debt.auctioningBack(debtId, status, error);
			
			if(error.code < 0){
				render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
			}
			String signstr = Security.addSign(tempId, Constants.BID_ID_SIGN);
			
			transferDetails( signstr, status, type);
			
		}
		
		if (status == 3) {//已成功
			
			tempId = Debt.successBack(debtId, status, error);
			
			if(error.code < 0){
				render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
			}
			String signstr = Security.addSign(tempId, Constants.BID_ID_SIGN);
			
			transferDetails( signstr, status, type);
		}
		
		if (status == -1) {//审核不通过
			
			tempId = Debt.nopassBack(debtId, status, error);
			
			if(error.code < 0){
				render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
			}
			String signstr = Security.addSign(tempId, Constants.BID_ID_SIGN);
			
			transferDetails( signstr, status, type);
		}
		
		if (status == -2 || status == -3 || status == -5) {//失败的
			
			tempId = Debt.failureBack(debtId, status, error);
			
			if(error.code < 0){
				render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
			}
			String signstr = Security.addSign(tempId, Constants.BID_ID_SIGN);
			
			transferDetails( signstr, status, type);
		}
		
	}
	
	
	/**
	 *转让中债权转让标详情
	 */
	@Unit(2)
	public static void transferDetails(String sign,int status,int type){
		
		ErrorInfo error = new ErrorInfo();
		
		long debtId =  Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		Long bidUserId = Debt.getBidUserId(debtId, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		Debt debt = new Debt();
		debt.id = debtId;
		
		Map<String,String> historySituationMap = User.historySituation(debt.invest.userId,error);//转让者历史记录情况
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		List<UserAuditItem> uItems = UserAuditItem.queryUserAllAuditItem(bidUserId, debt.invest.bid.mark); // 用户正对产品上传的资料集合
		
		
		Map<String,Long> countMap = Debt.countMap(debtId, status,error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		
		//当前记录之前的记录总和
		long countFront = countMap.get("countFront");
		
		//当前记录之后的记录总和
		long countAfter = countMap.get("countAfter");
		
		render(debt,countFront,countAfter,historySituationMap,uItems,type);
	}
	
	
	
	
	/**
	 * 债权转让详情竞拍记录浏览
	 * @param pageNum
	 * @param debtId
	 */
	public static void viewDebtAllAuctionRecords(int pageNum,long debtId){
		
		
		ErrorInfo error = new ErrorInfo();
		int currPage = pageNum;
		if (params.get("currPage") != null) {
			currPage = Integer.parseInt(params.get("currPage"));
		}
		
		PageBean<v_debt_auction_records> page = Debt.queryAllAuctionRecords(currPage, debtId, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		render(page);
		
	}
	
	/**
	 *成功的债权转让标的搜索
	 */
	public static void successDebtSerch(){
		                    render();
	}
	
	/**
	 *成功的债权转让标的查看方式
	 */
	public static void successDebtMode(){
		                    render();
	}
	
	/**
	 *成功的债权转让标详情
	 */
	public static void successDebtDetails(){
		                    render();
	}
	
	
	/**
	 *未通过的转让债权标的原因点击
	 */
	public static void reason(){
		                    render();
	}
	
	/**
	 *未通过的转让债权标重审
	 */
	public static void retrial(){
		                    render();
	}
	

	/**
	 *未通过的转让债权标重审结果
	 */
	public static void retrialIsPass(){
		                    render();
	}
	
	
	
	/**
	 *失败的债权转让标的搜索
	 */
	public static void failedDebtSerch(){
		                    render();
	}
	
	/**
	 *失败的债权转让标的查看方式
	 */
	public static void failedDebtMode(){
		                    render();
	}
	
	/**
	 *失败的债权转让标详情
	 */
	public static void failedTransferDetails(){
		                    render();
	}
}
