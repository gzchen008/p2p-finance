package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 *后台催收账单管理-->标记逾期
 */
@Entity
public class v_bill_detail_for_mark_overdue extends Model{

	public String name;
	public long user_id;
	public long bid_id;
	public int notice_count_message;
	public int notice_count_mail;
	public int notice_count_telephone;
	public String mobile;
	public String immediate_family_mobile;
	public String email;
	public String immediate_family_email;
	public double ovdedue_fine;
	public double principal_interest_amount;
	public int overdue_count;
	public double total_pay_amount;
	public int overdue_mark;
	public String overdue_time;
	public Date repayment_time;
	public String bill_no;
	
	@Transient
	public String sign;
	
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
	}
}
