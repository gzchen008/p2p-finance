package reports;

import constants.Constants;
import models.t_statistic_platform_float;
import models.t_users;

/**
 * 平台浮存金数据统计
 * @author lwh
 *
 */
public class StatisticPlatformFloat {
	
	
	public static Double queryBalanceFloatsum(){
		
		Double balance_float_sum = 0.0;//账户可用余额浮存
		String sql = "select sum(balance) from t_users ";
		  
		if(Constants.IPS_ENABLE) {
			sql = "select sum(balance + balance2) from t_users ";
		} 
		
		try {
			balance_float_sum = t_users.find(sql).first();//账户可用余额浮存
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == balance_float_sum){
			balance_float_sum = 0.0;
		}
		return balance_float_sum;
	}
	
	public static Double queryFreezeFloatsum(){
		Double freeze_float_sum = 0.0;//冻结资金浮存
		

		String sql = "select sum(freeze) from t_users ";

		try {
			freeze_float_sum = t_users.find(sql).first();// 冻结资金浮存
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (null == freeze_float_sum) {
			freeze_float_sum = 0.0;
		}
		
		return freeze_float_sum;
	}
	
	
	public static Long queryHasBalanceUseraccount(){
		Long has_balance_user_account = 0l;//有可用余额账户数量
		

		String sql = "select count(id) from t_users where balance > 0 ";

		if(Constants.IPS_ENABLE) {
			sql = "select count(id) from t_users where balance > 0 or balance2 > 0";
		}
		
		try {
			has_balance_user_account = t_users.find(sql).first();// 有可用余额账户数量
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (null == has_balance_user_account) {
			has_balance_user_account = 0l;
		}
		
		return has_balance_user_account;
	}
	
	
	public static Long queryHasBalancevipUseraccount(){
		Long has_balance_vip_user_account = 0l;//有可用余额的VIP账户数量
		

		String sql = "select count(id) from t_users where balance > 0 and vip_status = 1 ";

		if(Constants.IPS_ENABLE) {
			sql = "select count(id) from t_users where (balance > 0 or balance2 > 0) and vip_status = 1 ";
		}
		
		try {
			has_balance_vip_user_account = t_users.find(sql).first();// 有可用余额的VIP账户数量
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (null == has_balance_vip_user_account) {
			has_balance_vip_user_account = 0l;
		}
		
		return has_balance_vip_user_account;
	}
	
	
	public static Double queryVipBalancefloat(){
		Double vip_balance_float = 0.0;//VIP账户可用余额浮存
		

		String sql = "select sum(balance) from t_users where vip_status = 1 ";

		if(Constants.IPS_ENABLE) {
			sql = "select sum(balance + balance2) from t_users where vip_status = 1 ";
		}
		
		try {
			vip_balance_float = t_users.find(sql).first();// VIP账户可用余额浮存
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (null == vip_balance_float){
			vip_balance_float = 0.0;
		}
		
		return vip_balance_float;
	}
	
	
	//判断对象是否存在
	public static boolean judgeIsnew(int year,int month,int day){
		t_statistic_platform_float floa = null;
		
		try {
			floa = t_statistic_platform_float.find(" year = ? and month = ? and day = ?", year,month,day).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == floa){
			return true;
		}
		return false;
	}
	
	
	//获取对象
	public static t_statistic_platform_float getTarget(int year,int month,int day){
		t_statistic_platform_float floa = null;
		
		try {
			floa = t_statistic_platform_float.find(" year = ? and month = ? and day = ?", year,month,day).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return floa;
	}
}
