package models;

import java.util.Date;
import javax.persistence.Entity;
import play.data.validation.Match;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;

/**
 * 用户
 * @author cp
 * @version 6.0
 * @created 2014年4月16日 下午2:14:51
 */
@Entity
public class t_users extends Model {
	
	public Date time;
	
	@Required(message="姓名不能为空")
	@MaxSize(value=20,message="姓名长度为2-20个字符")
	@MinSize(value=2,message="姓名长度为2-20个字符")
	@Match(value="^[\u4E00-\u9FA5A-Za-z0-9_]+$",message="姓名不能含有特殊字符")
	public String name;
	
	@Required(message="邮箱不能为空")
	@Match(value="^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$",message="邮箱格式不正确")
	public String email;
	
	@Required(message="密码不能为空")
	public String password;
	
	public String photo;
	
	public String authentication_id;//注册中心 authentication表的id
	
	public String reality_name;
	
	public int password_continuous_errors;
	
	public boolean is_password_error_locked;
	
	public Date password_error_locked_time;
	
	public String pay_password;
	
	public int pay_password_continuous_errors;
	
	public boolean is_pay_password_error_locked;
	
	public Date pay_password_error_locked_time;
	
	public boolean is_secret_set;
	
	public Date secret_set_time;
	
	public long secret_question_id1;
	public long secret_question_id2;
	public long secret_question_id3;
	
	public String answer1;
	public String answer2;
	public String answer3;
	
	public boolean is_allow_login;
	
	public Date lock_time;
	
	public long login_count;
	
	public Date last_login_time;
	
	public String last_login_ip;
	
	public Date last_logout_time;
	
	public boolean is_email_verified;
	
	public String telephone;
	
	public String mobile;
	
	public boolean is_mobile_verified;
	
	public String mobile2;
	
	public String id_number;
	
	public String address;
	
	public String postcode;
	
	public int sex;
	
	public Date birthday;
	
	public int city_id;
	
	public String family_address;
	
	public String family_telephone;
	
	public String company;
	
	public String company_address;
	
	public String office_telephone;
	
	public String fax_number;
	
	public int education_id;
	
	public int marital_id;
	
	public int house_id;
	
	public int car_id;
	
	/**
	 * 基本信息中的邮箱
	 */
	
	public String email2;
	
	public boolean is_add_base_info;
	
	public boolean is_erased;
	
	public Date recommend_time;
	
	public long recommend_user_id;
	
	public int recommend_reward_type;

    public String recommend_user_code; //个人推荐码
    public String recommend_referee_code;//推荐人推荐码
    public long t_cfp_id;//理财师id
	
	public int master_identity;
	
	public Date master_time_loan;
	
	public Date master_time_invest;
	
	public Date master_time_complex;
	
	public boolean vip_status;
	
	public double balance;
	
	public double balance2;
	
	public double freeze;
	
	public double credit_line;
	
	public double last_credit_line;
	
	public int score;
	
	public int credit_score;
	
	public Long credit_level_id;
	
	public boolean is_refused_receive;
	
	public Date refused_time;
	
	public String refused_reason;
	
	public boolean is_blacklist;
	
	public Date joined_time;
	
	public String joined_reason;
	
	public Date assigned_time;
	
	public long assigned_to_supervisor_id;
	
	public String sign1;
	public String sign2;
	
	public String qq_key;
	public String weibo_key;
	public String qr_code;
	
	public String ips_acct_no;
	public String ips_bid_auth_no;
	public String ips_repay_auth_no;
	
	public String device_user_id;
	public String channel_id;
	public int device_type;
	
	public boolean is_bill_push;
	public boolean is_invest_push;
	public boolean is_activity_push;
	
	/**
	 * 是否时 活跃会员（充值就成为活跃会员）
	 */
	public boolean is_active;

	public t_users() {
	}

	public t_users(String name, String email) {
		this.name = name;
		this.email = email;
	}
	
	public t_users(long id, boolean isActive) {
		this.id = id;
		this.is_active = isActive;
	}
	
	public t_users(double balance, double balance2, double freeze) {
		super();
		this.balance = balance;
		this.balance2 = balance2;
		this.freeze = freeze;
	}

	/**
	 * 用于查询拒收名单
	 */
	public t_users(long id, String name, Date refused_time, boolean is_refused_receive, String refused_reason, boolean is_allow_login) {
		this.id = id;
		this.name = name;
		this.refused_time = refused_time;
		this.is_refused_receive = is_refused_receive;
		this.refused_reason = refused_reason;
		this.is_allow_login = is_allow_login;
	}
	
	/**
	 * 用于查询用户通过名字
	 */
	public t_users(long id, String name, String realityName, String email) {
		this.id = id;
		this.name = name;
		this.reality_name = realityName;
		this.email = email;
	}
	
	/**
	 * 用于查询用户通过名字(发送系统通知)
	 */
	public t_users(String name, String mobile, String email) {
		this.name = name;
		this.mobile = mobile;
		this.email = email;
	}
	
	/**
	 * 查询用户信息(用于资金托管)
	 */
	public t_users(String realityName, String idNumber, String ipsAcctNo, String ipsBidAuthNo) {
		this.reality_name = realityName;
		this.id_number = idNumber;
		this.ips_acct_no = ipsAcctNo;
		this.ips_repay_auth_no = ipsBidAuthNo;
	}
		
	/**
	 * 选择用户
	 * @param id ID
	 * @param name 名称
	 */
	public t_users(long id, String name){
		this.id = id;
		this.name = name;
	}
	
	/**
	 * 查询用户资金
	 * @param id ID
	 * @param name 名称
	 */
	public t_users(double balance, double freeze){
		this.balance = balance;
		this.freeze = freeze;
	}
	
	/**
	 * 百度云推送查询
	 * @param id
	 * @param deviceUserId
	 * @param channelId
	 * @param deviceType
	 */
	public t_users(long id, String deviceUserId, String channelId, int deviceType, boolean isBillPush, boolean isInvestPush, boolean isActivityPush){
		this.id = id;
		this.device_user_id = deviceUserId;
		this.channel_id = channelId;
		this.device_type = deviceType;
		this.is_bill_push = isBillPush;
		this.is_invest_push = isInvestPush;
		this.is_activity_push = isActivityPush;
	}
}
