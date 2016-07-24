package controllers.front.account;

import constants.Constants;
import controllers.BaseController;
import controllers.interceptor.FInterceptor;
import models.t_user_cps_income;
import models.v_user_cps_user_count;
import models.v_user_cps_users;
import business.BackstageSet;
import business.User;
import play.mvc.With;
import utils.ErrorInfo;
import utils.PageBean;

@With(FInterceptor.class)
public class Spread extends BaseController {

	//-------------------------------GPS推广-------------------------
	//我的GPS链接
	public static void spreadLink(){
		User user = User.currUser();
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		render(user, backstageSet);
	}
	
	//我成功推广的会员
	public static void spreadUser(){
		User user = User.currUser();
		long userId = user.id;
		
		String type = params.get("type");
		String key = params.get("key");
		String year = params.get("year");
		String month = params.get("month");
		String currPage = params.get("currPage");
		String pageSize = params.get("currSize");
		
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_cps_users> page = User.queryCpsSpreadUsers(userId ,type, key, 
				year, month, currPage, pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		v_user_cps_user_count cpsCount = User.queryCpsCount(userId, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		render(user, page, cpsCount);
	}
	
	//推广会员详情
	public static void userDetail(){
		render();
	}
	
	/**
	 * 我的推广会员收入
	 */
	public static void spreadIncome(){
		User user = User.currUser();
		long userId = user.id;
		
		String year = params.get("year");
		String month = params.get("month");
		String currPage = params.get("currPage");
		String pageSize = params.get("currSize");
		
		ErrorInfo error = new ErrorInfo();
		
		PageBean<t_user_cps_income> page = User.queryCpsSpreadIncome(userId, 
				year,month,currPage,pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		v_user_cps_user_count cpsCount = User.queryCpsCount(userId, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		render(user, page,cpsCount);
	}
	
	/**
	 * 推广收入明细
	 */
	public static void incomeDetail(){
		User user = User.currUser();
		long userId = user.id;
		
		String type = params.get("type");
		String key = params.get("key");
		String year = params.get("year");
		String month = params.get("month");
		String currPage = params.get("currPage");
		String pageSize = params.get("currSize");
		
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_cps_users> page = User.queryCpsSpreadUsers(userId ,type, key, 
				year, month, currPage, pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		render(user, page, year, month);
	}
	
	
}
