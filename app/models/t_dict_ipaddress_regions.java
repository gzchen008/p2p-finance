package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
* @author zhs
* @version 6.0
* @created 2014年4月4日 下午5:03:18
 */
@Entity
public class t_dict_ipaddress_regions extends Model {
	public String country;
	public String province;
}
