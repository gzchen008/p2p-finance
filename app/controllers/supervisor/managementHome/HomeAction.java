package controllers.supervisor.managementHome;

import business.Bid;
import business.Invest;
import business.News;
import business.User;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import utils.ErrorInfo;

/**
 * 管理首页
 * @author zhs
 *
 */
public class HomeAction  extends SupervisorController {
	/**
	 * 管理首页
	 */
	public static void showHome() {
		ErrorInfo error = new ErrorInfo();
		long onlineUserNum = User.queryOnlineUserNum();
		long todayRegisterUserCount = User.queryTodayRegisterUserCount(error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long totalRegisterUserCount = User.queryTotalRegisterUserCount(error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long todayBidCount = Bid.queryTodayBidCount(error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long totalBidCount = Bid.queryTotalBidCount(error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		double totalBidDealAmount = Bid.queryTotalDealAmount(error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long totalInvestCount = Invest.queryTotalInvestCount(error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		double totalInvestDealAmount = Invest.queryTotalDealAmount(error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long totalNewsCount = News.queryTotalNewsCount(error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(onlineUserNum, todayRegisterUserCount, totalRegisterUserCount, todayBidCount, totalBidCount,totalBidDealAmount, totalInvestCount, totalInvestDealAmount, totalNewsCount);
	}
}
