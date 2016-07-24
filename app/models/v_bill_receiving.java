package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 应收账单管理--待收款借款账单列表
 */
@Entity
public class v_bill_receiving extends Model {
     public long bid_id;
     public int year;
     public int month;
     public String bill_no;
     public String name;
     public String bid_no;
     public double amount;
     public double apr;
     public String title;
     public String period;
     public Date repayment_time;
     public String overdue_time;
     public long overdue_count;
     public String supervisor_name;
     public String supervisor_name2;
     
     @Transient
 	public String sign;
 	
 	/**
 	 * 获取加密ID
 	 */
 	public String getSign() {
 		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
 	}
     
}