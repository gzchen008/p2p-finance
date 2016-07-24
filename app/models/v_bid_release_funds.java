package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;
import utils.ServiceFee;

/**
 * 待放款/已放款 借款标列表
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-5-28 上午10:32:40
 */
@Entity
public class v_bid_release_funds extends Model {
	public String bid_no;
	public String title;
	public Long user_id;
	public String user_name;
	public String email;
	public Long product_id;
	public String small_image_filename;
	public Double apr;
	public Date time;
	public Date real_invest_expire_time;
	public Double amount;
	public Integer status;
	public Date audit_time;
	public Long allocation_supervisor_id;
	public String supervisor_name;
	public Integer product_item_count;
	public String mark;
	public Integer period;
	public Integer period_unit;
	public Long bank_account_id;
	public Integer user_item_count_true; 
	public Integer repaymentId;
	public String credit_level_image_filename;
	@Transient
	public Double capital_interest_sum; 
	
	@Transient
	public String sign;
	@Transient
	public String signUserId;
	
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		return Security.addSign(this.id, Constants.BID_ID_SIGN);
	}
	
	
	
	public Double getCapital_interest_sum() {
		double rate = ServiceFee.interestCompute(this.amount, this.apr, this.period_unit, this.period, this.repaymentId);
		
		return this.amount + rate;
	}

	/**
	 * 获取加密user_id
	 */
	public String getSignUserId() {
		return Security.addSign(this.user_id, Constants.USER_ID_SIGN);
	}
	
	/*@Transient
	public Object user_item_count_true; // 用户通过资料数
	
	public Object getUser_item_count_true() {
		String hql = "SELECT count(uai2.id) AS user_item_count_true FROM("
				+ "select uai.id,  uai.audit_item_id from t_user_audit_items uai where status = ? GROUP BY uai.audit_item_id) uai2 "
				+ "where uai2.audit_item_id IN( "
				+ "SELECT  pail.audit_item_id  FROM t_product_audit_items_log pail WHERE pail.mark = ?)";

		Query query = JPA.em().createNativeQuery(hql);
		query.setParameter(1, Constants.AUDITED);
		query.setParameter(2, this.mark);
		
		try {
			return query.getResultList().get(0);
		} catch (Exception e) {
			
			return 0;
		}
	}*/
}
