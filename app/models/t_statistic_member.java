package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

@Entity
public class t_statistic_member extends Model {

	public int year;
	public int month;
	public int day;
	public int new_member;
	public int new_recharge_member;
	public double new_member_recharge_rate;
	public int new_vip_count;
	public int member_count;
	public double member_activity;
	public int borrow_member_count;
	public int invest_member_count;
	public int composite_member;
	public int vip_count;
}
