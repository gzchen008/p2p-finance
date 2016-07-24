package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 我的会员账单--借款会员管理
 */

@Entity
public  class v_user_loan_info_bill extends Model {
	
	public long supervisor_id;
	public String type;
	public String name;
	public Date register_time;
	public double user_amount;
	public Date last_login_time;
	public int bid_count;
	public double bid_amount;
	public int invest_count;
	public double invest_amount;
	public double bid_loaning_amount;
	public double bid_repayment_amount;
	public int overdue_bill_count;
	public int bad_bid_count;
	public String credit_level_image_filename;
	
	@Transient
	public String sign;//加密ID
	
	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}
	
}