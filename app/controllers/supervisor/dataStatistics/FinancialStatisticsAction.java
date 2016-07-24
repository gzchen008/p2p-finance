package controllers.supervisor.dataStatistics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import models.t_statistic_platform_float;
import models.t_statistic_platform_income;
import models.t_statistic_recharge;
import models.t_statistic_security;
import models.t_statistic_withdraw;
import business.StatisticalReport;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import reports.StatisticRecharge;
import reports.StatisticSecurity;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 财务数据统计分析
 * 
 * @author bsr
 * 
 */
public class FinancialStatisticsAction extends SupervisorController {
	
	public static List<Object> getYears(){
		Calendar cal=Calendar.getInstance();//使用日历类  
	    List<Object> years = new ArrayList<Object>();
		int yearTemp = cal.get(Calendar.YEAR);// 得到年
		
		for(int i=0;i<5;i++){
			years.add(yearTemp-i);
		}
		
		return years;
	}
	/**
	 * 充值统计
	 */
	public static void rechargeStatistic(int currPage, int pageSize, int year, int month,int day,
			String startDateStr,String endDateStr,int order) {
        ErrorInfo error = new ErrorInfo();
        
        List<Object> years = getYears();
		
		PageBean<t_statistic_recharge> page = StatisticalReport.queryRecharge(currPage, pageSize, year, 
				month, day, startDateStr, endDateStr, order, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		double totalAmount = StatisticRecharge.totalRecharge(error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		String date = DateUtil.dateToString1(new Date());
		
		render(page, totalAmount, date,years);
	}

	/**
	 * 提现统计
	 */
	public static void withdrawalStatistic(int currPage, int pageSize, int year, int month,int day,
			String startDateStr,String endDateStr,int order) {
		
		//java.text.DecimalFormat df=new java.text.DecimalFormat("0.00");//保留2位小数
		ErrorInfo error = new ErrorInfo();
		 List<Object> years = getYears();
		PageBean<t_statistic_withdraw> page = StatisticalReport.queryWIthdraw(currPage, pageSize, year, 
				month, day, startDateStr, endDateStr, order, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		Double amount = 0d;
		StringBuffer date = new StringBuffer();
		
		List<Object[]> records = StatisticalReport.queryPaymentSum();
		
		if (null != records && records.size() > 0) {
			
			for (int i = 0; i < records.size(); i++) {
				
				if (i == 0) {
					date.append("" + records.get(i)[0].toString() + "-" + records.get(i)[1].toString() + "-" + records.get(i)[2].toString());
				}
				
				amount += Double.parseDouble(records.get(i)[3].toString());
			}	
		}
		
		render(page, amount, date, years);
	}

	/**
	 * 平台收入统计
	 */
	public static void incomeStatistic(int currPage, int pageSize, int year, int month,int day,
			String startDateStr,String endDateStr,int order) {
        
		ErrorInfo error = new ErrorInfo();
		//java.text.DecimalFormat df=new java.text.DecimalFormat("0.00");//保留2位小数
		 List<Object> years = getYears();
		PageBean<t_statistic_platform_income> page = StatisticalReport.queryIncome(currPage, pageSize, year, 
				month, day, startDateStr, endDateStr, order, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		Double amount = 0d;
		StringBuffer date = new StringBuffer();
		List<Object[]> records = StatisticalReport.queryPlatformAllIncomeAndTime();
		
		if (null != records && records.size() > 0) {
			
			for (int i = 0; i < records.size(); i++) {
				
				if (i == 0) {
					date.append("" + records.get(i)[0].toString() + "-" + records.get(i)[1].toString() + "-" + records.get(i)[2].toString());
				}
				
				amount += Double.parseDouble(records.get(i)[3].toString()); 
			}
		}
		
		render(page, amount, date, years);
	}

	/**
	 * 平台浮存金统计
	 */
	public static void floatAurum(int currPage, int pageSize, int year, int month,int day,
			String startDateStr,String endDateStr,int order) {
		
         ErrorInfo error = new ErrorInfo();
         List<Object> years = getYears();
		PageBean<t_statistic_platform_float> page = StatisticalReport.queryFloat(currPage, pageSize, year, 
				month, day, startDateStr, endDateStr, order, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		Map<String,Object> map = StatisticalReport.queryFloatParamter();
		String date = DateUtil.dateToString1(new Date());
		render(page,map,date,years);
		
	}

	/**
	 * 保障本金统计
	 */
	public static void guaranteeStatistic(int currPage, int pageSize, int year, int month,int day,
			String startDateStr,String endDateStr,int order) {
        ErrorInfo error = new ErrorInfo();
        List<Object> years = getYears();
		PageBean<t_statistic_security> page = StatisticalReport.querySecurity(currPage, pageSize, year, 
				month, day, startDateStr, endDateStr, order, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		Map<String,Object> map = StatisticSecurity.statisticAmount(error);
		String date = DateUtil.dateToString1(new Date());
		
		render(page, map, date,years);
	}
}
