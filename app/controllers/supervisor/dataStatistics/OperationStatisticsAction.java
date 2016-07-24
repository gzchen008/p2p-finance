package controllers.supervisor.dataStatistics;

import java.util.List;
import models.t_statistic_audit_items;
import models.t_statistic_borrow;
import models.t_statistic_debt_situation;
import models.t_statistic_financial_situation;
import models.t_statistic_member;
import models.t_statistic_product;
import business.StatisticalReport;
import constants.Constants;
import controllers.Unit;
import controllers.UnitCheck;
import controllers.supervisor.SupervisorController;
import play.mvc.With;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 运营数据统计分析
 * 
 * @author bsr
 * 
 */
@With(UnitCheck.class)
public class OperationStatisticsAction extends SupervisorController {

	/**
	 * 会员数据统计
	 */
	public static void userStatistic(int currPage, int pageSize, int year, int month, int day,
			String startDateStr,String endDateStr,int order) {
		ErrorInfo error = new ErrorInfo();
		 List<Object> years = FinancialStatisticsAction.getYears();
		PageBean<t_statistic_member> page =StatisticalReport.queryMember(currPage, pageSize, year, 
				month, day, startDateStr, endDateStr, order, error); 
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
				
		render(page,years);
	}

	/**
	 * 借款情况统计
	 */
	public static void loanStatistic(int currPage, int pageSize, int year, int month, int orderType) {
		ErrorInfo error = new ErrorInfo();
		 List<Object> years = FinancialStatisticsAction.getYears();
		PageBean<t_statistic_borrow> page = 
				StatisticalReport.queryBorrows(currPage, pageSize, year, month, orderType, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
				
		render(page,years);
	}

	/**
	 * 理财情况统计
	 */
	public static void investorsStatistic(int currPage, int pageSize, int year, int month,
			String startDateStr,String endDateStr,int order) {
		
		ErrorInfo error = new ErrorInfo();
		 List<Object> years = FinancialStatisticsAction.getYears();
		PageBean<t_statistic_financial_situation> page =StatisticalReport.queryInvest(currPage, pageSize, year, month, startDateStr, endDateStr, order, error); 
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
				
		render(page,years);
	}

	/**
	 * 借款标销量情况统计
	 */
	public static void loanBidStatistic(int currPage, int pageSize, int year, int month, int keywordType, String keyword, int orderType) {
		ErrorInfo error = new ErrorInfo();
		PageBean<t_statistic_product> page = 
				StatisticalReport.queryProducts(currPage, pageSize, year, month, keywordType, keyword, orderType, error);
		 List<Object> years = FinancialStatisticsAction.getYears();
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
				
		render(page,years);
	}

	/**
	 * 债权转让情况统计
	 */
	@Unit(2)
	public static void debtStatistic(int currPage, int pageSize, int year, int month,
			String startDateStr,String endDateStr,int order) {
		ErrorInfo error = new ErrorInfo();
		 List<Object> years = FinancialStatisticsAction.getYears();
		PageBean<t_statistic_debt_situation> page =StatisticalReport.queryDebt(currPage, pageSize, year, month, startDateStr, endDateStr, order, error); 
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
				
		render(page,years);
	}

	/**
	 * 审核科目库统计
	 */
	public static void auditItemsStatistic(int currPage, int pageSize, int year, int month, int keywordType, String keyword, int orderType) {
		ErrorInfo error = new ErrorInfo();
		 List<Object> years = FinancialStatisticsAction.getYears();
		PageBean<t_statistic_audit_items> page = 
				StatisticalReport.queryAuditItems(currPage, pageSize, year, month, keywordType, keyword, orderType, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
				
		render(page,years);
	}
}
