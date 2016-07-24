package models;

import javax.persistence.Entity;
import play.Logger;
import play.db.jpa.Model;

/**
 * 
* @author zhs
* @version 6.0
* @created 2014年4月4日 下午5:05:08
 */
@Entity
public class t_dict_ipaddress extends Model {
	public long ip_start;
	public Long ip_end;
	public String country;
	public String province;
	public String city;
	public String type;
	public long region_id;
	
	public static String queryCityByIp(String ip) {
		long _ip = com.shove.net.IPAddress.toLong(ip);
		String city = null;
		
		try {
			city = t_dict_ipaddress.find("select city from t_dict_ipaddress where ip_start <= ? and ? <= ip_end", _ip, _ip).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();

			return null;
		}
		
		return city;
	}
}
