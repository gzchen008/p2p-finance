package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;

import constants.Constants;
import play.db.jpa.Model;

/**
 * 用户投标记录
* @author lwh
* @version 6.0
* @created 2014年4月24日 下午4:14:14
 */

@Entity
public class v_invest_records extends Model {

	public Date time;
	public String title;
	public String no;
	public Double invest_amount;
	public Double bid_amount;
	public Double apr;
	public Integer status;
	public Integer transfer_status;
	public String name;
	public String bid_user_name;
	public Long bid_id;
	public Long user_id;
	public Integer question_count;
	public Integer answer_count;
	@Transient
	public double Forecast_earnings;
	@Transient
	public Date repayment_time;
	@Transient
	public String strStatus;

	public void getStrStatus() {
		if (null == this.strStatus) {
			switch (this.status) {
				case Constants.BID_AUDIT:
					this.strStatus = "审核中";
					break;
				case Constants.BID_ADVANCE_LOAN:
					this.strStatus = "提前借款";
					break;
				case Constants.BID_FUNDRAISE:
					this.strStatus = "筹款中";
					break;
				case Constants.BID_EAIT_LOAN:
					this.strStatus = "待放款";
					break;
				case Constants.BID_REPAYMENT:
					this.strStatus = "还款中";
					break;
				case Constants.BID_REPAYMENTS:
					this.strStatus = "已还款";
					break;
				case Constants.BID_AUDIT_VERIFY:
					this.strStatus = "审核中待验证";
					break;
				case Constants.BID_ADVANCE_LOAN_VERIFY:
					this.strStatus = "前提借款待验证";
					break;
				case Constants.BID_COMPENSATE_REPAYMENT:
					this.strStatus = "本金垫付还款中(已放款)";
					break;
				case Constants.BID_NOT_THROUGH:
					this.strStatus = "审核不通过";
					break;
				case Constants.BID_PEVIEW_NOT_THROUGH:
					this.strStatus = "借款中不通过";
					break;
				case Constants.BID_LOAN_NOT_THROUGH:
					this.strStatus = "放款不通过";
					break;
				case Constants.BID_FLOW:
					this.strStatus = "流标";
					break;
				case Constants.BID_REPEAL:
					this.strStatus = "撤销";
					break;
				default:
					this.strStatus = "状态有误,谨慎操作!";
					break;
			}
		}
	}
}