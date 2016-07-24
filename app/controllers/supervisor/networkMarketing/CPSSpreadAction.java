package controllers.supervisor.networkMarketing;

import com.shove.Convert;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import business.BackstageSet;
import business.User;
import models.t_statistic_cps;
import models.t_user_details;
import models.v_user_cps_detail;
import models.v_user_cps_user_info;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;

/**
 * GPS会员推广管理
 * 
 * @author bsr
 * 
 */
public class CPSSpreadAction extends SupervisorController {

	/**
	 * 查询所有的GPS会员推广列表
	 */
	public static void CPSAll() {
		int currPage = 1;
		
		currPage = Convert.strToInt(params.get("currPage"),1);
		
		String name = null;
		
		if(params.get("name") != null) {
			name = params.get("name");
		}
		
		int orderType = 0;
		
		if((params.get("orderType") != null) && !(params.get("orderType").equals("")) ) {
			orderType = Integer.parseInt(params.get("orderType"));
		}
		
		PageBean<v_user_cps_user_info> page = User.queryCpsUserInfo(name, orderType, currPage, Constants.PAGE_SIZE);
		
		render(page);
	}

	/**
	 * 查询GPS会员明细
	 */
	public static void CPSDetail(String sign, String beginTime, String endTime, int currPage, String name,int pageSize) {
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			
			renderJSON(error);
		}
		
		PageBean<v_user_cps_detail> page = User.queryCpsDetail(userId, name, beginTime, endTime, currPage, pageSize);
		
		render(page);
	}

	/**
	 * 佣金发放明细
	 */
	public static void CPSRebateDetail() {
		int currPage = 1;
		
		if(params.get("currPage") != null) {
			currPage = Integer.parseInt(params.get("currPage"));
		}
		
		String name = null;
		
		if(params.get("name") != null) {
			currPage = Integer.parseInt(params.get("name"));
		}
		
		PageBean<t_user_details> page = User.queryCpsCommissionDetail(1L, name, currPage, 2);
		
		render(page);
		
	}

	/**
	 * 佣金发放交易明细
	 */
	public static void CPSTransactionDetail() {
		render();
	}

	/**
	 * 佣金发放统计
	 */
	public static void CPSRebateStatistic(int year, int month, int currPage) {
		PageBean<t_statistic_cps> page = User.queryCpsOfferInfo(1L, year, month, currPage);
		
		render(page);
	}

	/**
	 * 推广规则设置
	 */
	public static void CPSSpreadRule() {
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		render(backstageSet);
	}

	/**
	 * 保存GPS推广规则
	 */
	public static void saveRule(int cpsRewardType, double rewardForCounts, double rewardForRate) {
		ErrorInfo error = new ErrorInfo();
		
		BackstageSet backstageSet = new BackstageSet();
		backstageSet.cpsRewardType = cpsRewardType;
		backstageSet.rewardForCounts = rewardForCounts;
		backstageSet.rewardForRate = rewardForRate;
		
		backstageSet.CPSPromotion(error);
		
		if(error.code<0) {
			flash.error(error.msg);
			render("@CPSSpreadRule",backstageSet);
		}
		
		flash.success(error.msg);
		CPSSpreadRule();
	}
}
