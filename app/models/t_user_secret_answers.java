package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import play.db.jpa.Model;

/**
 * 
 * @author lzp
 * @version 6.0
 * @created 2014-4-4 下午3:41:24
 */

@Entity
public class t_user_secret_answers extends Model {

	public long user_id;
	
	public int no;
	
	public long question_id;
	
	@Transient
	public String question;
	
	@Column(name = "answer")
	public String answer;
	
	
	public t_user_secret_answers() {
		super();
	}

}
