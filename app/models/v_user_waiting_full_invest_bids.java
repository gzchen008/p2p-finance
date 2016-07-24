package models;

import javax.persistence.Entity;
import javax.persistence.Transient;
import play.db.jpa.Model;

import java.util.Date;

@Entity
public class v_user_waiting_full_invest_bids extends Model{
	
		public long bid_id;
	     public Date time;
		public long user_id;
		public double bid_amount;
		public double invest_amount;
		public String name;
		public String image_filename;
		public double apr;
		public double loan_schedule;
		public int status;
		public int period;
		public int period_unit;
		public String no;
		public String title;
		public int product_item_count;
		public int user_item_count;
		public String product_name;
		
		@Transient
		public double receiving_amount;//本息合计

		public double getReceiving_amount() {
			
			double receiving_amount = 0;
			
			if(this.period_unit == -1){//年
				receiving_amount = this.invest_amount*this.apr*this.period/100+this.invest_amount;
			}
			
			if(this.period_unit == 0){//月
				receiving_amount = this.invest_amount*this.apr/12*this.period/100+this.invest_amount;
			}
			
			if(this.period_unit == 1){//日
				receiving_amount = this.invest_amount*this.apr/365*this.period/100+this.invest_amount;
			}
			
			return receiving_amount;
		}
		
		
		
	
	
}
