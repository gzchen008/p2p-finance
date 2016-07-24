package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;
import utils.ServiceFee;

/**
 * 审核中的借款标列表
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-21 上午10:47:08
 */
@Entity
public class v_bid_auditing extends Model {
	public String bid_no;
	public String title;
	public Long user_id;
	public String user_name;
	public Integer product_id;
	public String small_image_filename;
	public Double apr;
	public Integer period;
	public Integer period_unit;
	public Date time;
	public Double amount;
	public Integer status;
	@Transient
	public Double capital_interest_sum;
	public Integer product_item_count;
	public String mark;
	public String product_name;
	public Long order_sort;
	public String credit_level_image_filename;
	public Integer user_item_count_true; 
	public Integer user_item_count_false;
	public Integer repaymentId;
	
	@Transient
	public String sign;
	
	public String getSign(){
		return Security.addSign(this.id, Constants.BID_ID_SIGN);
	}
	
	public Double getCapital_interest_sum() {
		double rate = ServiceFee.interestCompute(this.amount, this.apr, this.period_unit, this.period, this.repaymentId);
		
		return this.amount + rate;
	}
	
	//@Transient
	//public Object user_item_count_true; // 用户通过资料数
	//@Transient
	//public Object user_item_count_false; // 用户未通过资料数
	
	/*public Object getUser_item_count_true() {
		String hql = "SELECT count(uai2.id) AS user_item_count_true FROM("
				+ "select uai.id,  uai.audit_item_id from t_user_audit_items uai where status = ? and user_id = ? GROUP BY uai.audit_item_id) uai2 "
				+ "where uai2.audit_item_id IN( "
				+ "SELECT  pail.audit_item_id  FROM t_product_audit_items_log pail WHERE pail.mark = ? and type = 1)";

		Query query = JPA.em().createNativeQuery(hql);
		query.setParameter(1, Constants.AUDITED);
		query.setParameter(2, this.user_id);
		query.setParameter(3, this.mark);
		
		try {
			return query.getResultList().get(0);
		} catch (Exception e) {
			
			return 0;
		}
	}
	
	public Object getUser_item_count_false() {
		String hql = "SELECT count(uai2.id) AS user_item_count_true FROM("
				+ "select uai.id,  uai.audit_item_id from t_user_audit_items uai where status = ? and user_id = ? GROUP BY uai.audit_item_id) uai2 "
				+ "where uai2.audit_item_id IN( "
				+ "SELECT  pail.audit_item_id  FROM t_product_audit_items_log pail WHERE pail.mark = ? and type = 1)";

		Query query = JPA.em().createNativeQuery(hql);
		query.setParameter(1, Constants.NOT_PASS);
		query.setParameter(2, this.user_id);
		query.setParameter(3, this.mark);
		
		try {
			return query.getResultList().get(0);
		} catch (Exception e) {
			return 0;
		}
	}*/
}		