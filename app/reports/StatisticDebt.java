package reports;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import utils.Arith;
import models.t_bills;
import models.t_invest_transfers;
import models.t_invests;
import models.t_statistic_debt_situation;


/**
 * 债权转让情况数据统计表字段查询
 * @author lwh
 *
 */
public class StatisticDebt {
	
	
	
	 
	
	 double average_debt_amount = 0 ;//转让债权均标金额
	 
	 double deal_percent = 0 ;//转让债权成交率
	 double transfer_percent = 0;//债权转让率
	
	 
	 public  static  long queryDebtAccount(){
		 long debt_account = 0 ;//债权转让标总数量
		 String sql = "select count(*) from t_invest_transfers";
		 try {
			 debt_account = t_invest_transfers.find(sql).first() ;//债权转让标总数量
		} catch (Exception e) {
			e.printStackTrace();
			debt_account = 0;
		}
		
		return debt_account;
	 }
	 
	 public static Double queryDebtAmounSum(){
		 Double debt_amount_sum = 0.0 ;//债权转让总金额
		 String sql = "select sum(debt_amount) from t_invest_transfers";
		 try {
			 debt_amount_sum = t_invest_transfers.find(sql).first() ;//债权转让总金额 
		} catch (Exception e) {
			e.printStackTrace();
			debt_amount_sum = 0.0;
		}
		
		if(null == debt_amount_sum || debt_amount_sum == 0 || debt_amount_sum + "" == ""){
			debt_amount_sum = 0.0;
		}
		
		return debt_amount_sum ;
	 }
	 
	
	 
	 public static long queryIncreaseDebtAccount(){
		 long increase_debt_account  = 0 ;//本月新增转让标数量
		 
		String sql = "select count(*) from t_invest_transfers where DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m')";
		try {
			increase_debt_account = t_invest_transfers.find(sql).first();// 本月新增转让标数量
		} catch (Exception e) {
			e.printStackTrace();
			increase_debt_account = 0;
		}
		return increase_debt_account;
	 }
	 
	
	 public static Double queryIncreaseDebtAmountSum(){
		 
		 Double increase_debt_amount_sum = 0.0 ;//本月新增转让总额
		 
		String sql = "select sum(debt_amount) from t_invest_transfers where DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m')";
		try {
			increase_debt_amount_sum = t_invest_transfers.find(sql).first();// 本月新增转让总额
		} catch (Exception e) {
			e.printStackTrace();
			increase_debt_amount_sum = 0.0;
		}

		if (null == increase_debt_amount_sum || increase_debt_amount_sum == 0) {
			increase_debt_amount_sum = 0.0;
		}
		
		return increase_debt_amount_sum ;
	 }
	 
	
	 public static Long queryHasOverdueDebt(){
		 
		Long has_overdue_debt = 0l ;//转让债权标含逾期数量
		 
		String sql = "select invest_id from t_invest_transfers  where DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m')";
		
		List<Long> investIds = new ArrayList<Long>();
		List<Long> bidIds = new ArrayList<Long>();
		
		try {
			investIds = t_invest_transfers.find(sql).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			has_overdue_debt = 0l;
		}
		
		if(investIds.size() > 0){
			 String idStr = StringUtils.join(investIds, ",");
			 sql = "select bid_id from t_invests where id in ( "+idStr+" )";
			try {
				bidIds = t_invests.find(sql).fetch();
			} catch (Exception e) {
				e.printStackTrace();
				has_overdue_debt = 0l;
			}
			
			if(bidIds.size() > 0){
				idStr = StringUtils.join(bidIds, ",");
				sql = "select count(*) from t_bills where status in (-2,-3) and bid_id in ( "+idStr+" ) group by bid_id";
				
				try {
					has_overdue_debt = t_bills.find(sql).first();
				} catch (Exception e) {
					e.printStackTrace();
					has_overdue_debt = 0l;
				}
			}
		}
		
		if(null == has_overdue_debt){
			has_overdue_debt = 0l;
		}
		
		return has_overdue_debt;
	 }
	 
	
	 public static long querySuccessDebtAmount(){
		long success_debt_amount = 0;// 债权转让成功标数量
		
		String sql = "select count(*) from  t_invest_transfers  where DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') and status = 3";
		try {
			success_debt_amount = t_invest_transfers.find(sql).first();// 债权转让成功标数量
		} catch (Exception e) {
			e.printStackTrace();
			success_debt_amount = 0;
		}
		 
		return success_debt_amount;
	 }
	 
	 public static long queryAllInvests(){
		long temp = 0;
		
		String sql = "select count(*) from t_invests where DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') and transfer_status = 0";
		try {
			temp = t_invests.find(sql).first();
		} catch (Exception e) {
			e.printStackTrace();
			temp = 0;
		}
		
		return temp ;
	 }
	 
	 
	 /**
	  * 判断记录是否存在，不存在返回true,存在返回false
	  * @param year
	  * @param month
	  * @return
	  */
	 public static boolean judgeIsNew(int year,int month){
		 t_statistic_debt_situation statistic = null;
		 
		 try {
			 statistic = t_statistic_debt_situation.find("  year = ? and month = ?",year,month).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		if(null == statistic){
			return true;
		}
		 return false;
	 }
	 
	 
	//获取对象 
	public static t_statistic_debt_situation getTarget(int year, int month) {
		t_statistic_debt_situation statistic = null;

		try {
			statistic = t_statistic_debt_situation.find(
					"  year = ? and month = ?",year,month).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return statistic;
	}
	
	
	/**
	 * 数据统计--债权转让情况统计分析表
	 */
	public static void debtSituationStatistics(){
		
		 int year = 0 ;
		 int month = 0 ;
		 long debt_account = StatisticDebt.queryDebtAccount() ;//债权转让标总数量
		 Double debt_amount_sum = StatisticDebt.queryDebtAmounSum() ;//债权转让总金额
		 long increase_debt_account  = StatisticDebt.queryIncreaseDebtAccount() ;//本月新增转让标数量
		 Double increase_debt_amount_sum = StatisticDebt.queryIncreaseDebtAmountSum() ;//本月新增转让总额
		 long has_overdue_debt = StatisticDebt.queryHasOverdueDebt();//转让债权标含逾期数量
		 double overdue_percent = 0 ;//转让债权逾期占比
		 double average_debt_amount = 0 ;//转让债权均标金额
		 long success_debt_amount = StatisticDebt.querySuccessDebtAmount() ;//债权转让成功标数量
		 double deal_percent = 0 ;//转让债权成交率
		 double transfer_percent = 0;//债权转让率

		 
		 Calendar now = Calendar.getInstance(); 
		 year = now.get(Calendar.YEAR);  
		 month = now.get(Calendar.MONTH) + 1;
		 
		if(increase_debt_amount_sum > 0){
			overdue_percent = Arith.div(has_overdue_debt, increase_debt_amount_sum, 4)*100 ;//转让债权逾期占比
		}else{
			overdue_percent = 0;
		}
		
		if(increase_debt_account > 0){
			average_debt_amount = Arith.div(increase_debt_amount_sum, increase_debt_account, 2) ;//转让债权均标金额
		}else{
			average_debt_amount = 0;
		}
		
		
		if(increase_debt_account > 0){
			 deal_percent = Arith.div(success_debt_amount, increase_debt_account, 4)*100 ;//转让债权成交率
		}else{
			deal_percent = 0;
		}
		
		long temp = StatisticDebt.queryAllInvests();
		
		if(temp > 0){
			transfer_percent = Arith.div(increase_debt_account, temp, 4)*100;//债权转让率
		}else{
			transfer_percent = 0;
		}
		
		//判断是否有记录存在
		boolean flag = StatisticDebt.judgeIsNew(year, month);
		
		if(flag){
			t_statistic_debt_situation  statistic =  new t_statistic_debt_situation();
			
			statistic.year = year ;
			statistic.month = month ;
			statistic.debt_account = debt_account ;//债权转让标总数量
			statistic.debt_amount_sum = debt_amount_sum ;//债权转让总金额
			statistic.increase_debt_account  = increase_debt_account ;//本月新增转让标数量
			statistic.increase_debt_amount_sum = increase_debt_amount_sum ;//本月新增转让总额
			statistic.has_overdue_debt = has_overdue_debt ;//转让债权标含逾期数量
			statistic.overdue_percent = overdue_percent ;//转让债权逾期占比
			statistic.average_debt_amount = average_debt_amount ;//转让债权均标金额
			statistic.success_debt_amount = success_debt_amount ;//债权转让成功标数量
			statistic.deal_percent = deal_percent ;//转让债权成交率
			statistic.transfer_percent = transfer_percent;//债权转让率
			
			try {
				statistic.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			t_statistic_debt_situation  statistic = StatisticDebt.getTarget(year, month);
			statistic.debt_account = debt_account ;//债权转让标总数量
			statistic.debt_amount_sum = debt_amount_sum ;//债权转让总金额
			statistic.increase_debt_account  = increase_debt_account ;//本月新增转让标数量
			statistic.increase_debt_amount_sum = increase_debt_amount_sum ;//本月新增转让总额
			statistic.has_overdue_debt = has_overdue_debt ;//转让债权标含逾期数量
			statistic.overdue_percent = overdue_percent ;//转让债权逾期占比
			statistic.average_debt_amount = average_debt_amount ;//转让债权均标金额
			statistic.success_debt_amount = success_debt_amount ;//债权转让成功标数量
			statistic.deal_percent = deal_percent ;//转让债权成交率
			statistic.transfer_percent = transfer_percent;//债权转让率
			
			try {
				statistic.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}
}
