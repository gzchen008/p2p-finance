package reports;

import java.util.Calendar;

import javax.persistence.Query;

import com.shove.Convert;

import models.t_statistic_member;
import models.t_user_vip_records;
import models.t_users;
import play.Logger;
import play.db.jpa.JPA;
import utils.Arith;
import utils.ErrorInfo;

/**
 * 会员数据统计分析表
 * @author zhs
 * @version 6.0
 * @created 2014-7-18
 */
public class StatisticMember {

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
	 * 添加本日统计数据
	 * @param error
	 * @return
	 */
	private static int add(ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		t_statistic_member entity = new t_statistic_member();
		
		entity.year = cal.get(Calendar.YEAR);
		entity.month = cal.get(Calendar.MONTH) + 1;
		entity.day = cal.get(Calendar.DAY_OF_MONTH);
		entity.new_member = queryNewMember(error);
		entity.new_recharge_member = queryNewRechargeMember(error);
		entity.new_member_recharge_rate = entity.new_member == 0 ? 0 : Arith.div(entity.new_recharge_member, entity.new_member, 2);
		entity.new_vip_count = queryNewVipCount(error);
		entity.member_count = queryMemberCount(error);
		entity.member_activity = queryMemberActivity(error);
		entity.borrow_member_count = queryBorrowMemberCount(error);
		entity.invest_member_count = queryInvestMemberCount(error);
		entity.composite_member = queryCompositeMemberCount(error);
		entity.vip_count = queryVipCouont(error);

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
	 * 更新本日统计数据
	 * @param error
	 * @return
	 */
	private static int update(ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		t_statistic_member entity = null;
		
		try {
			entity = t_statistic_member.find("year = ? and month = ? and day = ?", year, month, day).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		if (entity == null) {
			error.code = -1;
			error.msg = "本日本金保障统计分析表统计不存在";
			
			return error.code;
		}
		
		entity.year = cal.get(Calendar.YEAR);
		entity.month = cal.get(Calendar.MONTH) + 1;
		entity.day = cal.get(Calendar.DAY_OF_MONTH);
		entity.new_member = queryNewMember(error);
		entity.new_recharge_member = queryNewRechargeMember(error);
		entity.new_member_recharge_rate = entity.new_member == 0 ? 0 : Arith.div(entity.new_recharge_member, entity.new_member, 2);
		entity.new_vip_count = queryNewVipCount(error);
		entity.member_count = queryMemberCount(error);
		entity.member_activity = queryMemberActivity(error);
		entity.borrow_member_count = queryBorrowMemberCount(error);
		entity.invest_member_count = queryInvestMemberCount(error);
		entity.composite_member = queryCompositeMemberCount(error);
		entity.vip_count = queryVipCouont(error);

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
	 * 是否添加了本日数据
	 * @return
	 */
	private static boolean isAdd(ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		
		int count = 0;
		
		try {
			count = (int)t_statistic_member.count("year = ? and month = ? and day = ?", year, month, day);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return false;
		}
		
		return (count > 0);
	}
	
	/**
	 * 新增会员数
	 * @return
	 */
	public static int queryNewMember(ErrorInfo error){
		error.clear();
		int count = 0;
		
		try {
			count = (int) t_users.count("date_format(time, '%y%m%d') = date_format(curdate(), '%y%m%d')");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询新增会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询新增会员数出现异常！";
			
			return 0;
		}
		
		return count;
	}
	
	/**
	 * 新增充值会员数
	 * @return
	 */
	public static int queryNewRechargeMember(ErrorInfo error){
		error.clear();
		String sql = "SELECT COUNT(distinct b.id) FROM t_users as b JOIN t_user_recharge_details as a where a.user_id = b.id" +
				" AND date_format(a.time, '%y%m%d') = date_format(curdate(), '%y%m%d') " +
				"AND date_format(b.time, '%y%m%d') = date_format(curdate(), '%y%m%d')";
		
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		if(query.getResultList().size() == 0){
			return 0;
		}
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询新增充值会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询新增充值会员数出现异常！";
			
			return 0;
		}
		
		return (obj == null) ? 0 : Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 新增会员充值占比
	 * @return
	 */
	public static double queryNewMemberRechargeRate(ErrorInfo error){
		error.clear();
		String sql = "SELECT IFNULL(ROUND(((CASE WHEN (a.user_id = b.id and date_format(a.time, '%y%m%d') = date_format(curdate(), '%y%m%d'))" +
				" THEN COUNT(distinct a.user_id) END))/COUNT(distinct b.id)*100,2),0) FROM t_users as b JOIN " +
				"t_user_recharge_details as a where date_format(b.time, '%y%m%d') = date_format(curdate(), '%y%m%d')";
		
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		if(query.getResultList().size() == 0){
			return 0;
		}
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询新增会员充值占比时："+e.getMessage());
			error.code = -1;
			error.msg = "查询新增会员充值占比出现异常！";
			
			return 0;
		}
		
		return (obj == null) ? 0 : Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 新增VIP会员数
	 * @return
	 */
	public static int queryNewVipCount(ErrorInfo error){
		error.clear();
		int count = 0;
		
		try {
			count = (int) t_user_vip_records.count("date_format(start_time, '%y%m%d') = date_format(curdate(), '%y%m%d')");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询新增VIP会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询新增VIP会员数出现异常！";
			
			return 0;
		}
		
		return count;
	}
	
	/**
	 * 累计会员数
	 * @return
	 */
	public static int queryMemberCount(ErrorInfo error){
		error.clear();
		int count = 0;
		
		try {
			count = (int) t_users.count("");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询累计会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询累计会员数出现异常！";
			
			return 0;
		}
		
		return count;
	}
	
	/**
	 * 会员活跃度
	 * @return
	 */
	public static double queryMemberActivity(ErrorInfo error){
		error.clear();
		String sql = "SELECT ROUND((SELECT COUNT(b.id) FROM t_users AS b WHERE date_format(b.last_login_time, '%y%m%d')" +
				" = date_format(curdate(), '%y%m%d'))/count(a.id)*100,2) FROM t_users AS a";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		if(query.getResultList().size() == 0){
			return 0;
		}
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询会员活跃度时："+e.getMessage());
			error.code = -1;
			error.msg = "查询会员活跃度出现异常！";
			
			return 0;
		}
		
		return (obj == null) ? 0 : Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 借款会员数
	 * @return
	 */
	public static int queryBorrowMemberCount(ErrorInfo error){
		error.clear();
		int count = 0;
		
		try {
			count = (int) t_users.count("date_format(master_time_loan, '%y%m%d') = date_format(curdate(), '%y%m%d')");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询借款会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询借款会员数出现异常！";
			
			return 0;
		}
		
		return count;
	}
	
	/**
	 * 理财会员数
	 * @return
	 */
	public static int queryInvestMemberCount(ErrorInfo error){
		error.clear();
		int count = 0;
		
		try {
			count = (int) t_users.count("date_format(master_time_invest, '%y%m%d') = date_format(curdate(), '%y%m%d')");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询理财会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询理财会员数出现异常！";
			
			return 0;
		}
		
		return count;
	}
	
	/**
	 * 复合会员数
	 * @return
	 */
	public static int queryCompositeMemberCount(ErrorInfo error){
		error.clear();
		int count = 0;
		
		try {
			count = (int) t_users.count("date_format(master_time_complex, '%y%m%d') = date_format(curdate(), '%y%m%d')");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询复合会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询复合会员数出现异常！";
			
			return 0;
		}
		
		return count;
	}
	
	/**
	 * VIP会员数
	 * @return
	 */
	public static int queryVipCouont(ErrorInfo error){
		error.clear();
		int count = 0;
	
		try {
			count = (int) t_users.count("vip_status = 1");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询VIP会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询VIP会员数出现异常！";
			
			return 0;
		}
		
		return count;
	}
}
