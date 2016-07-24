package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

@Entity
public class v_user_unverified_info extends Model {

	public Date register_time;
	public String name;
	public double credit_score;
	public String mobile;
	public String email;
	public boolean is_allow_login;
	public double user_amount;
	public double recharge_amount;
	public long invest_count;
	public double invest_amount;
	public String credit_level_image_filename;
	
	@Transient
	public String sign;//加密ID

	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}
}