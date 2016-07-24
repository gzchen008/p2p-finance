package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import business.User;
import play.db.jpa.Model;
import utils.ErrorInfo;

/**
 * 
 * @author lzp
 * @version 6.0
 * @created 2014-4-4 下午3:41:24
 */

@Entity
public class t_user_recharge_details extends Model {

	public long user_id;
	
	public Date time;
	
	public int payment_gateway_id;
	
	public String pay_number;
	
	public double amount;
	
	public boolean is_completed;
	
	public Date completed_time;
	
	public String order_no;

	public int type;
	
	@Transient
	public String name;
	
	public String getName() {
		return User.queryUserNameById(this.user_id, new ErrorInfo());
	}
}
