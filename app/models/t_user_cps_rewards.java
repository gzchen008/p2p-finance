package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
 * @author lzp
 * @version 6.0
 * @created 2014-4-4 下午3:41:24
 */

@Entity
public class t_user_cps_rewards extends Model {

	public Date time;
	public long cps_extensions_id;
	public long relation_id;
	public double management_fee;
	public double reword_amount;
	public String description;
	public boolean status;
	public Date reword_time;

	
}
