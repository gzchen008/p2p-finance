package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 标
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:32:02
 */
@Entity
public class t_bids extends Model {
	public long user_id;
	public long m_product_id;//母产品id
	public long main_type_id;// 产品主类型,字典类型
	public long sub_type_id; // 产品子类型,字典类型
	public Date time;
	public String bid_no;
	public String mer_bill_no;
	public String ips_bill_no;
	public long product_id;                 // 产品ID
	public String title;                   //标题
	public long loan_purpose_id;            // 借款用途
	public long repayment_type_id;          // 还款类型
	public double amount;                  //金额
	public int period;                     //期限
	public double min_invest_amount;        // 最低金额招标
	public double average_invest_amount;     // 平均金额招标
	public int invest_period;              //满标期限
	public Date invest_expire_time;        // 满标日期
	public Date real_invest_expire_time;    // 实际满标日期
	public double apr;                     //年利率
	@Transient
	public double aprPlus;//加送年利率，只作为界面显示用
	public long bank_account_id;            // 绑定银行卡(默认为0)
	public int bonus_type;                  // 奖励方式:1 固定金额奖励 2按比例奖励
	public double bonus;                    // 固定奖金
	public double award_scale;              // 奖励百分比
	public String image_filename;            // 借款图片
	public boolean is_quality;                // 优质标(默认false)
	public boolean is_hot;                    // 火标(默认false)
	public String description;             //借款描述
	public String project_introduction;//项目简述
	public String company_info;//相关企业信息
	public String repayment_res;//还款来源
	public String risk_control;//风控措施
	public String about_risk;//风险提示
	public double feeType ;//手续费
	public int is_new ;//是否新手标
	public String repayment_tips ;//还款提示
	public Date sell_time ;//开售时间
	public Date qixi_date ;//起息日
	public Date repayall_date ;//还本结息日
	public Date moneyback_time ;//预计资金到账时间
	public double bail;                    // 保证金
	public double service_fees;             // 服务费
	public boolean is_agency;             // 标示合作机构状态
	public int agency_id;                  // 合作机构ID
	public boolean is_show_agency_name;     //是否显示机构合作名称
	public int status;               //审核状态:0审核中 1募集中（审核通过） 2还款中 3已还款 -1审核不通过 -2流标
	public double loan_schedule;                    // 借款进度比例
	public double has_invested_amount;            // 已投总额
	public int read_count;                         // 阅读次数(默认为0)
	public long allocation_supervisor_id;            // 审核人(默认为0)
	public long manage_supervisor_id;                //分配审核人
	public Date audit_time;                           // 审核时间(默认为null)
	public String audit_suggest;                      // 审核意见(默认为null)
	public Date repayment_time;                        //还款日期
	public Date last_repay_time;                      // 最后放款时间(默认为null)
	public boolean is_auditmatic_invest_bid;     // 自动投标(默认为false)
	
	public int period_unit;                  //借款期限单位
	public boolean is_sec_bid;                // 是否秒还
	public boolean is_deal_password;          //是否需要交易密码
	public int show_type;                     // 发布方式
	public String mark;                       // 产品历史资料唯一标示(缓存字段)
	public int version;
	public String qr_code;//二维码标识
	
	public double invest_rate; // 理财管理费，利息费率
	public double overdue_rate; // 逾期费率
	public boolean is_register_guarantor; // 是否已登记担保方
	
	public String name; // 产品简称
	public String project_name; // 项目名称
	public String project_code; // 项目编码
	public String loaner_name; // 项目融资方
	public String project_detail; // 项目详情
	public String capital_usage; // 项目资金用途
	public String security_guarantee; // 安全保障
	public String profit_guarantee; // 收益保障
	public String supervise_bank; // 监管银行
	
	public t_bids() {

	}

	/**
	 * 我要借款,时间最新的未满借款标
	 * @param user_id
	 * @param id
	 * @param time
	 * @param amount
	 * @param apr
	 */
	public t_bids(long user_id, long id, Date time, double amount, double apr) {
		this.user_id = user_id;
		this.id = id;
		this.time = time;
		this.amount = amount;
		this.apr = apr;
	}

	/**
	 * 我要借款,最新5个满标
	 * @param id 标ID
	 * @param image_filename 借款图片
	 * @param amount 金额
	 */
	public t_bids(long id, String image_filename, double amount) {
		this.id = id;
		this.image_filename = image_filename;
		this.amount = amount;
	}

	/**
	 * 放款审核后续处理,部分查询项
	 * @param service_fees 服务费
	 * @param amount 金额
	 */
	public t_bids(double service_fees, double amount) {
		this.service_fees = service_fees;
		this.amount = amount;
		//this.bail = bail;
	}

	/**
	 * 放款审核后续处理,部分查询项
	 * @param bonus_type 奖励方式
	 * @param bonus 固定奖金
	 * @param award_scale 奖励百分比
	 */
	public t_bids(int bonus_type, double bonus, double award_scale) {
		this.bonus_type = bonus_type;
		this.bonus = bonus;
		this.award_scale = award_scale;
	}
	
	/**
	 * 账户满标倒计时提醒
	 * @param id 标ID
	 * @param amount 金额
	 * @param time 时间
	 * @param invest_expire_time 满标时间
	 */
	public t_bids(long id, double amount, Date time, Date invest_expire_time) {
		this.id = id;
		this.amount = amount;
		this.time = time;
		this.invest_expire_time = invest_expire_time;
	}
	
	/**
	 * 财务放款
	 * @param amount 金额
	 * @param apr 年利率
	 * @param period 期限
	 * @param repayment_type_id 还款方式
	 * @param service_fees 服务费
	 * @param bonus_type 奖励方式
	 * @param bonus 固定奖励
	 * @param award_scale 比例奖励
	 */
	public t_bids(double amount, double apr, int period, long repayment_type_id,
			double service_fees, int bonus_type, double bonus,
			double award_scale) {
		this.amount = amount;
		this.apr = apr;
		this.period = period;
		this.repayment_type_id = repayment_type_id;
		this.service_fees = service_fees;
		this.bonus_type = bonus_type;
		this.bonus = bonus;
		this.award_scale = award_scale;
	}
	
	/**
	 * 解除秒还标操作
	 * @param amount 金额
	 * @param apr 年利率
	 * @param period 期限
	 * @param service_fees 服务费
	 * @param bail 保证金
	 */
	public t_bids(double amount, double apr, int period, double service_fees,
			double bail) {
		this.amount = amount;
		this.apr = apr;
		this.period = period;
		this.service_fees = service_fees;
		this.bail = bail;
	}
	
	/* 辉哥  */
	@Transient
	public String no;
	
	/**
	 * 我的会员账单-借款会员管理
	 * @param id
	 * @param no
	 * @param title
	 * @param amount
	 * @param status
	 */
	public t_bids(long id,String no,String title,double amount,int status){
		this.id = id;
		this.no = no;
		this.title = title;
		this.amount = amount;
		this.status = status;
	}
	
	/**
	 * 会员管理，根据用户ID查询数据
	 * @param id ID
	 * @param title 标题
	 * @param amount 金额
	 * @param status 状态
	 */
	public t_bids(long id, String title, double amount, int status) {
		this.id = id;
		this.title = title;
		this.amount = amount;
		this.status = status;
	}
	
	@Transient
	public String sign;
	
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
	}
	
	/* 2014-11-14 */
	public String pact; // 借款合同
	public String intermediary_agreement; // 居间服务协议
	public String guarantee_bid; // 保障函
}
