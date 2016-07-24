package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import play.db.jpa.Model;

/**
 * 理财账单
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:42:20
 */
@Entity
public class t_bill_invests extends Model {
	public long user_id;
	public long bid_id;
	public long invest_id;
	public String mer_bill_no;
	public String title;
	public Date receive_time;
	public double receive_corpus;
	public double overdue_fine;
	public double receive_interest;
	public int status;
	public int periods;
	public Date real_receive_time;
	public double real_receive_corpus;
	public double real_receive_interest;
	@Transient
	public double receive_amount;
	@Transient
	public String dxreceive_amount;//大写金额
	
	public t_bill_invests(){
		
	}
	
	public t_bill_invests(long id,String title, double receive_amount, int status, Date receive_time
			, Date real_receive_time){
		this.id = id;
		this.title = title;
		this.receive_amount = receive_amount;
		this.status = status;
		this.receive_time = receive_time;
		this.real_receive_time = real_receive_time;
	}
	
	public t_bill_invests(long id,String title, int status, Date receive_time,double receive_amount,Date real_receive_time){
		this.id = id;
		this.title = title;
		this.status = status;
		this.receive_time = receive_time;
		this.receive_amount = receive_amount;
		this.real_receive_time = real_receive_time;
	}
}
