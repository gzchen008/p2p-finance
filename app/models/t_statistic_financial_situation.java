package models;

import javax.persistence.Entity;
import play.db.jpa.Model;


/**
 * 理财情况统计分析表
 * @author lwh
 *
 */
@Entity
public class t_statistic_financial_situation extends Model{
	
	public int year ;
	public int month ;
	public double invest_accoumt ;
	public double increase_invest_account ;
	public long invest_user_account ;
	public long increase_invest_user_account ;
	public double per_capita_invest_amount ;
	public double per_bid_average_invest_amount ;
	public double per_capita_invest_debt ;
	public double per_capita_balance ;
	public double invest_user_conversion ;
}
