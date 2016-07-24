package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 用于用户的一些详情显示
 * @author cp
 * @version 6.0
 * @created 2014年6月20日 下午5:01:31
 */
@Entity
public class v_user_users extends Model {
	
	public String city_name;
	
	public String province_name;
	
	public String education_name;
	
	public String marital_name;
	
	public String house_name;
	
	public String car_name;
	
	public Date time;
	
	public String name;
	
	public int credit_score;
	
	public Long credit_level_id;
	
	public String photo;
	
	public String reality_name;
	
	public String email;
	
	public boolean is_email_verified;
	
	public String mobile;
	
	public boolean is_mobile_verified;
	
	public String id_number;
	
	public int sex;
	
	public Date birthday;
	
}
