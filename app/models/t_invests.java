package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
* @author zhs
* @version 6.0
* @created 2014年4月4日 下午5:27:06
 */
@Entity
public class t_invests extends Model {
	public long user_id;
	public Date time;
	public String mer_bill_no;
	public String ips_bill_no;
	public long bid_id;
	public double amount;
	public double fee;//理财管理费
	public int transfer_status;
	public long transfers_id;
	public boolean is_automatic_invest;
	public double correct_amount;
	public double correct_interest;
	
	public String pact;
	public String intermediary_agreement;
	public String guarantee_invest;
	
	public t_invests(long user_id,long bid_id){
		this.bid_id = bid_id;
		this.user_id = user_id;
	}
	
	public t_invests() {
		
	}

}
