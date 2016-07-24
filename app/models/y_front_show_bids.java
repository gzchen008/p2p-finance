package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import constants.Constants;
import business.CreditLevel;
import play.db.jpa.Model;
import utils.ErrorInfo;


@Entity
public class y_front_show_bids extends Model{

	public double min_invest_amount;
	public String title;
	public Double amount;
	public Integer period;
	public Integer  period_unit;
	public Integer status;
	public Double apr;
	public Double has_invested_amount;
	public String no;
    public Date time;



//	@Transient
//	public String strStatus;
//	
//	public String getStrStatus() {
//		if(null == this.strStatus){
//			switch (this.status) {
//				case Constants.BID_AUDIT: this.strStatus = "审核中"; break;
//				case Constants.BID_ADVANCE_LOAN: this.strStatus = "提前借款"; break;
//				case Constants.BID_FUNDRAISE: this.strStatus = "募集中"; break;
//				case Constants.BID_EAIT_LOAN: this.strStatus = "待放款"; break;
//				case Constants.BID_REPAYMENT: this.strStatus = "还款中"; break;
//				case Constants.BID_COMPENSATE_REPAYMENT: this.strStatus = "本金垫付还款中"; break;
//				case Constants.BID_REPAYMENTS: this.strStatus = "已还款"; break;
//				case Constants.BID_NOT_THROUGH: this.strStatus = "审核不通过"; break;
//				case Constants.BID_PEVIEW_NOT_THROUGH: this.strStatus = "借款中不通过"; break;
//				case Constants.BID_LOAN_NOT_THROUGH: this.strStatus = "放款不通过"; break;
//				case Constants.BID_FLOW: this.strStatus = "流标"; break;
//				case Constants.BID_REPEAL: this.strStatus = "撤销"; break;
//				case Constants.BID_NOT_VERIFY: this.strStatus = "未验证"; break;
//				default : this.strStatus = "状态有误,谨慎操作!"; break;
//			}
//		}
//		
//		return this.strStatus;
//	}
	
	

	
}
