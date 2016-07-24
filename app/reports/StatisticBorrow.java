package reports;

import java.util.Calendar;
import javax.persistence.Query;
import com.shove.Convert;
import models.t_statistic_borrow;
import models.t_users;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;

/**
 * 借款情况统计分析表
 * @author lzp
 * @version 6.0
 * @created 2014-7-16
 */
public class StatisticBorrow {
	/**
	 * 周期性执行
	 * @param error
	 * @return
	 */
	public static int executeUpdate(ErrorInfo error) {
		error.clear();
		boolean isAdd = isAdd(error);
		
		if (error.code < 0) {
			return error.code;
		}
		
		if (isAdd) {
			update(error);
		} else {
			add(error);
		}
		
		error.code = 0;
		
		return error.code;
	}
	
	/**
	 * 添加本月统计数据
	 * @param error
	 * @return
	 */
	private static int add(ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		t_statistic_borrow entity = new t_statistic_borrow();
		entity.year = cal.get(Calendar.YEAR);
		entity.month = cal.get(Calendar.MONTH) + 1;
		entity.total_borrow_amount = queryTotalBorrowAmount(error);
		entity.this_month_borrow_amount = queryThisMonthBorrowAmount(error);
		entity.total_borrow_user_num = queryTotalBorrowUserNum(error);
		entity.new_borrow_user_num = queryNewBorrowUserNum(error);
		entity.finished_borrow_amount = queryFinishedBorrowAmount(error);
		entity.repaying_borrow_amount = queryRepayingBorrowAmount(error);
		entity.released_bids_num = queryReleasedBidsNum(error);
		entity.released_borrow_amount = queryReleasedBorrowAmount(error);
		entity.average_annual_rate = queryAverageAnnualRate(error);
		entity.average_borrow_amount = queryAverageBorrowAmount(error);
		entity.overdue_bids_num = queryOverduedBidsNum(error);
		entity.overdue_amount = queryOverdueAmount(error);
		entity.overdue_per = entity.this_month_borrow_amount == 0 ? 0 : entity.overdue_amount / entity.this_month_borrow_amount;
		entity.bad_bids_num = queryBadBidsNum(error);
		entity.bad_bill_amount = queryBadBillAmount(error);
		entity.bad_bill_amount_per = entity.this_month_borrow_amount == 0 ? 0 : entity.bad_bill_amount / entity.this_month_borrow_amount;

		try {
			entity.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		error.code = 0;

		return error.code;
	}
	
	/**
	 * 更新本月统计数据
	 * @param error
	 * @return
	 */
	private static int update(ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		t_statistic_borrow entity = null;
		
		try {
			entity = t_statistic_borrow.find("year = ? and month = ?", year, month).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		if (entity == null) {
			error.code = -1;
			error.msg = "本月借款情况统计不存在";
			
			return error.code;
		}
		
		entity.year = year;
		entity.month = month;
		entity.total_borrow_amount = queryTotalBorrowAmount(error);
		entity.this_month_borrow_amount = queryThisMonthBorrowAmount(error);
		entity.total_borrow_user_num = queryTotalBorrowUserNum(error);
		entity.new_borrow_user_num = queryNewBorrowUserNum(error);
		entity.finished_borrow_amount = queryFinishedBorrowAmount(error);
		entity.repaying_borrow_amount = queryRepayingBorrowAmount(error);
		entity.released_bids_num = queryReleasedBidsNum(error);
		entity.released_borrow_amount = queryReleasedBorrowAmount(error);
		entity.average_annual_rate = queryAverageAnnualRate(error);
		entity.average_borrow_amount = queryAverageBorrowAmount(error);
		entity.overdue_bids_num = queryOverduedBidsNum(error);
		entity.overdue_amount = queryOverdueAmount(error);
		entity.overdue_per = entity.this_month_borrow_amount == 0 ? 0 : entity.overdue_amount / entity.this_month_borrow_amount;
		entity.bad_bids_num = queryBadBidsNum(error);
		entity.bad_bill_amount = queryBadBillAmount(error);
		entity.bad_bill_amount_per = entity.this_month_borrow_amount == 0 ? 0 : entity.bad_bill_amount / entity.this_month_borrow_amount;

		try {
			entity.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		error.code = 0;

		return error.code;
	}
	
	/**
	 * 是否添加了本月数据
	 * @return
	 */
	private static boolean isAdd(ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int count = 0;
		
		try {
			count = (int)t_statistic_borrow.count("year = ? and month = ?", year, month);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return false;
		}
		
		error.code = 0;
		
		return (count > 0);
	}
	
	/**
	 * 查询累计借款总额
	 * @param error
	 * @return
	 */
	public static double queryTotalBorrowAmount(ErrorInfo error) {
		error.clear();
		String sql = "select sum(amount) from t_bids where status in (4, 5, 14)";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询累计借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询累计借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询本月借款总额
	 * @param error
	 * @return
	 */
	public static double queryThisMonthBorrowAmount(ErrorInfo error) {
		error.clear();
		String sql = "select sum(`t_bids`.`amount`) AS `sum(amount)` from `t_bids` where status in (4, 5, 14) and (date_format(`t_bids`.`time`,'%Y%m') = date_format(curdate(),'%Y%m'))";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询本月借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询本月借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询累计借款会员数
	 * @param error
	 * @return
	 */
	public static int queryTotalBorrowUserNum(ErrorInfo error) {
		error.clear();
		int count = 0;
		
		try {
			count = (int) t_users.count("master_identity = 1 or master_identity = 3");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询累计借款会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询累计借款会员数出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return count;
	}
	
	/**
	 * 查询新增借款会员数
	 * @param error
	 * @return
	 */
	public static int queryNewBorrowUserNum(ErrorInfo error) {
		error.clear();
		String sql = "select count(*) from t_users where (date_format(master_time_loan,'%Y%m') = date_format(curdate(),'%Y%m')) or (master_time_loan = null and master_identity = 3 and date_format(master_time_complex,'%Y%m') = date_format(curdate(),'%Y%m'))";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询新增借款会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询新增借款会员数出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询已完成借款总额
	 * @param error
	 * @return
	 */
	public static double queryFinishedBorrowAmount(ErrorInfo error) {
		error.clear();
		String sql = "select sum(amount) from t_bids where ((status = 5) and (date_format(time,'%Y%m') = date_format(curdate(),'%Y%m')))";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已完成借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已完成借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询还款中的借款总额
	 * @param error
	 * @return
	 */
	public static double queryRepayingBorrowAmount(ErrorInfo error) {
		error.clear();
		String sql = "select sum(repayment_corpus+repayment_interest+overdue_fine) from t_bills where status in (-1, -2) and date_format(repayment_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询还款中的借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询还款中的借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询已放款借款标数量
	 * @param error
	 * @return
	 */
	public static int queryReleasedBidsNum(ErrorInfo error) {
		error.clear();
		String sql = "select count(*) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已放款借款标数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已放款借款标数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询已放款借款总额
	 * @param error
	 * @return
	 */
	public static double queryReleasedBorrowAmount(ErrorInfo error) {
		error.clear();
		String sql = "select sum(amount) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已放款借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已放款借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 平均年利率(基于已放款借款标数量来算)
	 * @param error
	 * @return
	 */
	public static double queryAverageAnnualRate(ErrorInfo error) {
		error.clear();
		String sql = "select avg(apr) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已放款借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已放款借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 均借款金额(基于已放款借款标数量来算)
	 * @param error
	 * @return
	 */
	public static double queryAverageBorrowAmount(ErrorInfo error) {
		error.clear();
		String sql = "select avg(amount) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询均借款金额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询均借款金额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询逾期借款标数量
	 * @param error
	 * @return
	 */
	public static int queryOverduedBidsNum(ErrorInfo error) {
		error.clear();
		String sql = "select count(distinct bid_id) from t_bills where status in (-1,-2) and overdue_mark in (-1,-2,-3)";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询逾期借款标数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询逾期借款标数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询逾期总额
	 * @param error
	 * @return
	 */
	public static double queryOverdueAmount(ErrorInfo error) {
		error.clear();
		String sql = "select sum(repayment_corpus+real_repayment_interest+overdue_fine) from t_bills where status in (-1,-2) and overdue_mark in (-1,-2,-3) and date_format(repayment_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询逾期总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询逾期总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询坏账借款标数量
	 * @param error
	 * @return
	 */
	public static int queryBadBidsNum(ErrorInfo error) {
		error.clear();
		String sql = "select count(distinct bid_id) from t_bills where status in (-1,-2) and overdue_mark = -3";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询坏账借款标数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询坏账借款标数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询坏账总额
	 * @param error
	 * @return
	 */
	public static double queryBadBillAmount(ErrorInfo error) {
		error.clear();
		String sql = "select sum(repayment_corpus+real_repayment_interest+overdue_fine) from t_bills where status in (-1,-2) and overdue_mark = -3 and date_format(repayment_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询坏账总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询坏账总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
}
