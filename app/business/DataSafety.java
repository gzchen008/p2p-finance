package business;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang.StringUtils;
import constants.Constants;
import models.t_user_details;
import models.t_users;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;

/**
 * 数据防篡改
 * Description:
 * @author zhs
 * vesion: 6.0 
 * @date 2014-8-1 上午10:39:41
 */
public class DataSafety implements Serializable{
	
	public long id;
	private long _id;
	
	public String sign1;
	public String sign2;
	public String balance1;
	public String freeze1;
	public String amount;
	public String balance2;
	public String freeze2;
	public String recieveAmount;
	
	public void setId(long id){
		Map<String,Object> userMap = new HashMap<String,Object>();
		Map<String,Object> userDetailMap = new HashMap<String,Object>();
		
		String sql = "select new Map(a.balance as balance, a.freeze as freeze, a.sign1 as sign1, a.sign2 as sign2) from t_users as a where a.id = ?";
		
		try{
			userMap = t_users.find(sql, id).first();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("根据用户id查找用户信息时："+e.getMessage());
			this._id = -1;
			JPA.setRollbackOnly();
			
			return;
		}
		
        String sql2 = "select new Map(a.amount as amount, a.balance as balance, a.freeze as freeze," +
        		" a.recieve_amount as recieveAmount) from t_user_details as a where a.user_id = ? order by id desc";
		try{
			userDetailMap = t_user_details.find(sql2, id).first();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("根据用户id查找用户明细表信息时："+e.getMessage());
			this._id = -1;
			JPA.setRollbackOnly();
			
			return;
		}
		
		this._id = id;
		
		if(userMap != null) {
			this.balance1 = String.valueOf((Double)userMap.get("balance"));
			this.freeze1 = String.valueOf((Double)userMap.get("freeze"));
			this.sign1 = (String)userMap.get("sign1");
			this.sign2 = (String)userMap.get("sign2");
		}
		
		if(userDetailMap != null) {
			this.balance2 = String.valueOf((Double)userDetailMap.get("balance"));
			this.freeze2 = String.valueOf((Double)userDetailMap.get("freeze"));
			this.recieveAmount = String.valueOf((Double)userDetailMap.get("recieveAmount"));
			this.amount = String.valueOf((Double)userDetailMap.get("amount"));
		}else {
			this.balance2 = 0.00+"";
			this.freeze2 = 0.00+"";
			this.recieveAmount = 0.00+"";
			this.amount = 0.00+"";
		}
	}
	
	public void getId(){
		this.id = _id;
	}

	/**
	 * 对比数据库里面的值，判断是否被篡改
	 * @param error
	 * @return false 已被篡改  true 未被篡改
	 */
	public boolean signCheck(ErrorInfo error){
		error.clear();
		
		if(StringUtils.isBlank(this.sign1) || StringUtils.isBlank(this.sign2)){
			error.code = -1;
			error.msg = "尊敬的用户，你的账户资金出现异常变动，请速联系管理员";
			return false;
		}
		
		String userSign1 = com.shove.security.Encrypt.MD5(Long.toString(this._id) + this.balance1 + this.freeze1 + Constants.ENCRYPTION_KEY);
		
		String userSign2 = com.shove.security.Encrypt.MD5(Long.toString(this._id) + this.balance2 + 
				this.freeze2 + this.amount + this.recieveAmount + Constants.ENCRYPTION_KEY);
		
		if(!this.sign1.equalsIgnoreCase(userSign1)){
			error.code = -1;
			error.msg = "尊敬的用户，你的账户资金出现异常变动，请速联系管理员";
			Logger.error("账户资金出现异常变动");
			return false;
		}
		
		if(!this.sign2.equalsIgnoreCase(userSign2)){
			error.code = -1;
			error.msg = "尊敬的用户，你的交易资金出现异常变动，请速联系管理员";
			Logger.error("交易资金出现异常变动");
			return false;
		}
		
		return true;
	} 
	
	/**
	 * MD5生成新的标记
	 * @param error
	 * @return
	 */
	public int updateSign(ErrorInfo error){
		error.clear();
		
		EntityManager em = JPA.em();
		
        String userSign1 = com.shove.security.Encrypt.MD5(Long.toString(this._id) + this.balance1 + this.freeze1 + Constants.ENCRYPTION_KEY);
		
		String userSign2 = com.shove.security.Encrypt.MD5(Long.toString(this._id) + this.balance2 + 
				this.freeze2 + this.amount + this.recieveAmount + Constants.ENCRYPTION_KEY);
		String updateSql = "update t_users set sign1 = ?, sign2 = ? where id = ?";
		Query query = em.createQuery(updateSql).setParameter(1, userSign1).setParameter(2, userSign2).setParameter(3, this._id);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("更改用户防篡改标志时："+e.getMessage());
			error.code = -1;
			error.msg = "更改用户防篡改标志出现错误";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		if(rows < 0){
			error.code = -1;
			error.msg = "更改用户防篡改标志操作没有执行";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		
		return error.code;
	}
	
}
