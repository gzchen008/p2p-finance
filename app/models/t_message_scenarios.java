package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 发送消息应用场景
* @author lwh
* @version 6.0
* @created 2014年4月4日 下午3:42:48
 */
@Entity
public class t_message_scenarios extends Model {

	public Date time;
	public String scenarios;
}
