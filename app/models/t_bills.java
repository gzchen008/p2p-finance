package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import play.db.jpa.Model;

/**
 * 账单
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:45:42
 */
@Entity
public class t_bills extends Model {
	public long bid_id;
	public String title;
	public Date repayment_time;
	public double repayment_corpus;
	public double repayment_interest;
	public int status;
	public String mer_bill_no;
	public String repayment_bill_no;
	public int periods;
	public Date real_repayment_time;
	public double real_repayment_corpus;
	public double real_repayment_interest;
	public int overdue_mark;
	public Date mark_overdue_time;
	public double overdue_fine;
	public Date mark_bad_time;
	public int notice_count_message;
	public int notice_count_mail;
	public int notice_count_telphone;
	@Transient
	public double current_pay_amount;
	
	public t_bills() {
		
	}
	
	/**
	 * 用于借款账单的历史还款情况
	 */
	public t_bills(String title, double real_repayment_corpus, double real_repayment_interest,
			double overdue_fine, int overdue_mark, int status, Date repayment_time,
			Date real_repayment_time) {
		
		this.title = title;
		this.current_pay_amount = real_repayment_corpus + real_repayment_interest + overdue_fine;
		this.overdue_mark = overdue_mark;
		this.status = status;
		this.repayment_time = repayment_time;
		this.real_repayment_time = real_repayment_time;
		
	}
	
	/**
	 * 用于系统标记逾期
	 */
	public t_bills(long id, long bid_id, int periods,
			double repayment_corpus, double repayment_interest) {
		
		this.id = id;
		this.bid_id = bid_id ;
		this.periods = periods;
		this.repayment_corpus = repayment_corpus;
		this.repayment_interest = repayment_interest;
	}
}
