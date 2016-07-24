package business;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_bids;
import models.t_bills;
import models.t_dict_bid_repayment_types;
import models.t_dict_loan_purposes;
import models.t_invests;
import models.t_system_options;
import models.t_user_attention_bids;
import models.v_bid_attention;
import models.v_bid_auditing;
import models.v_bid_bad;
import models.v_bid_fundraiseing;
import models.v_bid_not_through;
import models.v_bid_overdue;
import models.v_bid_release_funds;
import models.v_bid_repayment;
import models.v_bid_repaymenting;
import models.v_bid_wait_verify;
import models.v_bids;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.cache.Cache;
import play.db.jpa.Blob;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import utils.Arith;
import utils.CnUpperCaser;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.NumberUtil;
import utils.PageBean;
import utils.QueryUtil;
import utils.Security;
import utils.ServiceFee;
import bean.FundraiseingBid;
import business.MProduct.MainType;
import business.MProduct.SubType;

import com.google.zxing.BarcodeFormat;
import com.shove.code.Qrcode;

import constants.Constants;
import constants.Constants.PayType;
import constants.Constants.RechargeType;
import constants.DealType;
import constants.IPSConstants;
import constants.IPSConstants.IPSOperation;
import constants.IPSConstants.Status;
import constants.OptionKeys;
import constants.SQLTempletes;
import constants.SupervisorEvent;
import constants.Templets;
import constants.UserEvent;

/**
 * 标
 * 
 * @author bsr
 * @version 6.0
 * @created 2014年3月9日 上午11:56:44
 */
public class Bid implements Serializable{
	
	public long id;
	private long _id;
	public String bidNo;
	public String merBillNo;
	public String ipsBillNo;
	public boolean ips;
	public boolean auditBid; 
	public boolean createBid;
	public boolean bidDetail;
	public String sign; // 加密ID
	
	public String no; // 编号
	public Date time; // 发布时间
	public String presellTime;//预售shi'jian
	public String title; // 标题
	public double amount; // 借款金额
	public int periodUnit; // 借款期限单位
	public String strPeriodUnit; // 期限单位字符串 
	public Date repayDateAbout ;//预计还款日期
	public int period; // 借款期限
	public double apr; // 年利率
	public double aprPlus;//加送年利率，只作为界面显示用
	//public double totalInterest; // 总利息
	public long bankAccountId; // 借款标绑定的银行卡
	public String imageFilename; // 借款图片
	public String description; // 借款描述

	public String project_introduction;//项目简述
	public String company_info;//相关企业信息
	public String repayment_res;//还款来源
	public String about_risk;//风险提示
	public String risk_control;//风控措施
	public double feeType ;//手续费
	public int is_new ;//是否新手标
	public String repayment_tips ;//还款提示
	public Date sell_time ;//开售时间
	public Date qixi_date ;//起息日
	public Date repayall_date ;//还本结息日
	public Date moneyback_time ;//预计资金到账时间
	public int status; // 审核状态
	public String strStatus; // 0审核中 1筹款中（审核通过） 2还款中 3已还款 -1审核不通过 -2流标
	
	public double hasInvestedAmount; // 已投总额
	public double loanSchedule; // 借款进度比例
	public double minInvestAmount; // 最低金额招标
	public double averageInvestAmount; // 平均金额招标
	public double minAllowInvestAmount;// 最低允许投标金额

	public int bonusType; // 奖励方式 0 不奖励 1 固定金额奖励 2 按比例奖励
	public double bonus; // 固定奖金
	public double awardScale; // 奖励百分比
	public double investBonus;//投标资金

	public boolean isSecBid; // 是否秒还
	public boolean isDealPassword; //是否需要交易密码
	public int showType; // 发布方式
	
	public boolean isQuality; // 优质标
	public boolean isHot; // "火"标
	public double bail; // 保证金
	public double serviceFees; // 服务费
	public int readCount; // 浏览量,默认0
	public boolean isAuditmaticInvestBid; // 是否是已经自动投标的标识 0 否 1 是
	public int investPeriod; // 满标期限
	public Date investExpireTime; // 满标日期
	public Date realInvestExpireTime; // 实际满标日期
	
	public Date repaymentTime;  //还款日期
	public Date recentRepayTime;//最近还款时间
	public boolean hasOverdue;//是否有逾期的还款
	
	public long allocationSupervisorId; // 审核人
	public long manageSupervisorId; // 分配审核人
	public Date auditTime; // 审计时间
	public String auditSuggest; // 审核意见
	
	public String mark; // 产品历史资料唯一标示(缓存字段)
	public String qr_code;//二维码标识
	
	public long productId; // 产品ID
	public Product product; // 产品对象
	public MProduct mProduct; //母产品对象
	public long mProductId; //母产品对象id

	public MainType main_type;// 产品主类型
	public SubType sub_type; // 产品子类型

	public long mainTypeId;// 产品主类型id
	public long subTypeId; // 产品子类型id
	public String smallImageFilename; // 产品小图片路径
	
	public long userId; // 用户ID
	private long _userId; // 用户ID
	public String signUserId; // 加密用户ID
	public String userName; // 用户名
	public User user; // 用户实体类

	public boolean isAgency; // 是否是机构标
	public int agencyId; // 机构ID
	public Agency agency; // 合作机构标对象,多对一
	public boolean isShowAgencyName; // 是否显示机构标名称

	public Repayment repayment; // 还款类型
	public Purpose purpose; // 借款用途
	
	public int investCount; // 投标次数
	public int questionCount; // 提问记录次数
	public double userItemPassScale; // 用户资料通过比例(bid详情,在此使用get方法)
	public boolean isReleaseSign = false;
	public boolean isRepair;//是否补单
	
	public boolean auditBidPact; // 借款合同
	public String pact;
	public String intermediary_agreement;
	public String guarantee_bid;
	public boolean isRegisterGuarantor; // 是否已登记担保方
	
	public String name; // 产品简称
	public String project_name; // 项目名称
	public String project_code; // 项目编码
	public String loaner_name; // 项目融资方
	public String project_detail; // 项目详情
	public String capital_usage; // 项目资金用途
	public String security_guarantee; // 安全保障
	public String profit_guarantee; // 收益保障
	public String supervise_bank; // 监管银行
	/**
	 * 获取_id
	 */
	public long getId(){
		return this._id;
	}
	
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		if(null == this.sign)
			this.sign = Security.addSign(this.id, Constants.BID_ID_SIGN);
		
		return this.sign;
	}

	/**
	 * 获取加密用户ID
	 */
	public String getSignUserId() {
		if(null == this.signUserId) 
			this.signUserId = Security.addSign(this.userId, Constants.USER_ID_SIGN);
		
		return this.signUserId;
	}

	/**
	 * 获取用户ID
	 */
	public long getUserId() {
		return this._userId;
	}
	
	/**
	 * 获取用户名
	 */
	public String getUserName(){
		if(null == this.userName) {
			this.userName = User.queryUserNameById(this.userId, new ErrorInfo());
		}
		
		return this.userName;
	}
	
	/**
	 * 获取编号
	 */
	public String getNo() {
		if(null == this.no) {
			ErrorInfo error = new ErrorInfo();

			String _no = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, error); // 编号

			if (null == _no) {
				Logger.error("标->填充自己,获取常量:" + error.msg);
				_no = "JK";
			}
		
			this.no = _no + this.id;
		}
		
		return this.no;
	}

	/**
	 * 获取状态
	 */
	public String getStrStatus() {
		if(null == this.strStatus){
			switch (this.status) {
				case Constants.BID_AUDIT: this.strStatus = "审核中"; break;
				case Constants.BID_ADVANCE_LOAN: this.strStatus = "提前借款"; break;
				case Constants.BID_FUNDRAISE: this.strStatus = "筹款中"; break;
				case Constants.BID_EAIT_LOAN: this.strStatus = "待放款"; break;
				case Constants.BID_REPAYMENT: this.strStatus = "还款中"; break;
				case Constants.BID_REPAYMENTS: this.strStatus = "已还款"; break;
				case Constants.BID_AUDIT_VERIFY:this.strStatus = "审核中待验证"; break;
				case Constants.BID_ADVANCE_LOAN_VERIFY:this.strStatus = "前提借款待验证"; break;
				case Constants.BID_COMPENSATE_REPAYMENT:this.strStatus = "本金垫付还款中(已放款)"; break;
				case Constants.BID_NOT_THROUGH: this.strStatus = "审核不通过"; break;
				case Constants.BID_PEVIEW_NOT_THROUGH: this.strStatus = "借款中不通过"; break;
				case Constants.BID_LOAN_NOT_THROUGH: this.strStatus = "放款不通过"; break;
				case Constants.BID_FLOW: this.strStatus = "流标"; break;
				case Constants.BID_REPEAL: this.strStatus = "撤销"; break;
				default : this.strStatus = "状态有误,谨慎操作!"; break;
			}
		}
		
		return this.strStatus;
	}
	
	/**
	 * 获取期限单位
	 */
	public String getStrPeriodUnit() {
		if(null == this.strPeriodUnit) {
			switch(this.periodUnit){
				case Constants.YEAR: this.strPeriodUnit = "年"; break;
				case Constants.MONTH: this.strPeriodUnit = "个月"; break;
				case Constants.DAY: this.strPeriodUnit = "天"; break;
				default: this.strPeriodUnit = "有误!"; break;
			}
		}
		
		return this.strPeriodUnit;
	}
	/**
	 * 预计还款日期
	 */
	public Date getRepayDateAbout() {
		Calendar cal = Calendar.getInstance();
		this.repayDateAbout = this.time;
		cal.setTime(this.repayDateAbout);
		switch(this.periodUnit){
			case Constants.YEAR: 
				cal.add(Calendar.YEAR,this.period);
				cal.add(Calendar.DATE,this.investPeriod);
				break;
			case Constants.MONTH: 
				cal.add(Calendar.MONTH,this.period);
				cal.add(Calendar.DATE,this.investPeriod);
				break;
			case Constants.DAY: 
				cal.add(Calendar.DATE,this.period);
				cal.add(Calendar.DATE,this.investPeriod);
				break;
		}
		this.repayDateAbout = cal.getTime();
		return this.repayDateAbout;
	}
	/**
	 * 填充自己的用户对象
	 */
	public void setUserId(long userId) {
		this._userId = userId;
		
		if(null == this.user){
			this.user = new User();
			this.user.createBid = this.createBid; 
			this.user.lazy = this.bidDetail; 
			this.user.id = userId; 
		}
	}
	
	/**
	 * 填充自己的产品对象
	 */
	public void setProductId(long productId) {
		this.productId = productId; 
		
		if(null == this.product){
			this.product = new Product();
			this.product.createBid = this.createBid;
			this.product.bidDetail = this.bidDetail; // 借款标详情部分加载
			this.product.id = productId;
		}
	}

	/**
	 * 填充自己的母产品对象
	 */
	public void setMProductId(long mProductId) {
		this.mProductId = mProductId;
		
		if(null == this.mProduct){
			this.mProduct = new MProduct();
			this.mProduct.id = mProductId; 
		}
	}

	/**
	 * 填充自己的产品主类型对象
	 */
	public void setMainTypeId(long main_typeId) {
		this.mainTypeId = main_typeId;
		
		if(null == this.main_type){
			this.main_type = new MainType();
			this.main_type.id = main_typeId; 
		}
	}
	/**
	 * 填充自己的产品子类型对象
	 */
	public void setSubTypeId(long sub_typeId) {
		this.subTypeId = sub_typeId;
		
		if(null == this.sub_type){
			this.sub_type = new SubType();
			this.sub_type.id = sub_typeId; 
		}
	}
	/**
	 * 填充自己的合作机构对象
	 */
	public void setAgencyId(int agencyId) {
		this.agencyId = agencyId;
		
		if(null == this.agency){
			this.agency = new Agency();
			this.agency.id = agencyId; 
		}
	}

	/**
	 * 填充自己
	 */
	public void setId(long id) {
		t_bids tbid = null;

		try {
			tbid = t_bids.findById(id);
		} catch (Exception e) {
			Logger.error("标->标填充自己:" + e.getMessage());
			return;
		}

		if (null == tbid) {
			this._id = -1;
			
			return;
		}
		
		this._id = id;
		this.mark = tbid.mark;
		this.bidNo = tbid.bid_no;
		this.merBillNo = tbid.mer_bill_no;
		this.isRegisterGuarantor = tbid.is_register_guarantor;
		this.ipsBillNo = tbid.ips_bill_no;
		this.aprPlus = tbid.aprPlus;
		
		this.name = tbid.name;
		this.project_name = tbid.project_name;
		this.project_code = tbid.project_code;
		this.loaner_name = tbid.loaner_name;
		this.project_detail = tbid.project_detail;
		this.capital_usage = tbid.capital_usage;
		this.security_guarantee = tbid.security_guarantee;
		this.profit_guarantee = tbid.profit_guarantee;
		this.supervise_bank = tbid.supervise_bank;
		
		this.mainTypeId = tbid.main_type_id;
		this.subTypeId = tbid.sub_type_id;
		this.mProductId = tbid.m_product_id;
		
		
		if (this.ips) {
			this.purpose = new Purpose();
			this.purpose.id = tbid.loan_purpose_id;
			this.amount = tbid.amount;
			this._userId = tbid.user_id;
			this.serviceFees = tbid.service_fees;
			this.apr = tbid.apr;
			this.hasInvestedAmount = tbid.has_invested_amount;
			
			return;
		}
		
		/* 标审核加载项 */
		if(this.auditBid){
			this.title = tbid.title;
			this.amount = tbid.amount;
			this.periodUnit = tbid.period_unit;
			this.period = tbid.period;
			this.isSecBid = tbid.is_sec_bid;
			this.status = tbid.status;
			this.investPeriod = tbid.invest_period;
			this.bail = tbid.bail;
			this.serviceFees= tbid.service_fees;
			this.bonusType = tbid.bonus_type;
			this.bonus = tbid.bonus;
			this.awardScale = tbid.award_scale;
			this.apr = tbid.apr;
			this.hasInvestedAmount = tbid.has_invested_amount;
			this.loanSchedule = tbid.loan_schedule;
			this.repayment = new Repayment();
			this.repayment.id = tbid.repayment_type_id;
			this.createBid = true; // 加载部分用户项
			this.userId = tbid.user_id;
			this.purpose = new Purpose();
			this.purpose.id = tbid.loan_purpose_id;
			
			return;
		}
		
		if(this.auditBidPact){
			this.amount = tbid.amount;
			this.periodUnit = tbid.period_unit;
			this.period = tbid.period;
			this.isSecBid = tbid.is_sec_bid;
			this.apr = tbid.apr;
			this.repayment = new Repayment();
			this.repayment.id = tbid.repayment_type_id;
			this.createBid = true; // 加载部分用户项
			this.userId = tbid.user_id;
			this.purpose = new Purpose();
			this.purpose.id = tbid.loan_purpose_id;
			
			this.pact = tbid.pact;
			this.intermediary_agreement = tbid.intermediary_agreement;
			this.guarantee_bid = tbid.guarantee_bid;
		}
		
		/* 标详情加载项 */
		if(this.bidDetail) {
			this.title = tbid.title;
			this.amount = tbid.amount;
			this.apr = tbid.apr;
			this.periodUnit = tbid.period_unit;
			this.period = tbid.period;
			this.readCount = tbid.read_count;
			this.loanSchedule = tbid.loan_schedule;
			this.description = tbid.description;
			
			this.project_introduction = tbid.project_introduction;
			this.company_info = tbid.company_info;
			this.repayment_res = tbid.repayment_res;
			this.risk_control = tbid.risk_control;
			this.about_risk = tbid.about_risk;
			this.feeType = tbid.feeType;
			this.is_new = tbid.is_new;
			this.repayment_tips = tbid.repayment_tips;
			this.sell_time = tbid.sell_time;
			this.qixi_date = tbid.qixi_date;
			this.repayall_date = tbid.repayall_date;
			this.moneyback_time = tbid.moneyback_time;
			
			this.bonusType = tbid.bonus_type; // 0 不奖励 1 固定金额奖励 2 按比例奖励
			this.bonus = tbid.bonus; // 固定奖金
			this.awardScale = tbid.award_scale; // 奖励百分比
			this.isQuality = tbid.is_quality;
			this.isHot = tbid.is_hot;
			this.investExpireTime = tbid.invest_expire_time;
			this.hasInvestedAmount = tbid.has_invested_amount;
			this.bankAccountId = tbid.bank_account_id;
			this.status = tbid.status;
			this.auditSuggest = tbid.audit_suggest;
			this.investPeriod = tbid.invest_period;
			this.purpose = new Purpose();
			this.purpose.id = tbid.loan_purpose_id;
			this.repayment = new Repayment();
			this.repayment.id = tbid.repayment_type_id;
			this.manageSupervisorId = tbid.manage_supervisor_id;
			
			if(tbid.is_agency) {
				this.isAgency = true;
				this.agencyId = tbid.agency_id;
			}
			
			this.userId = tbid.user_id;
			this.productId = tbid.product_id;
			
			return;
		}

		double _amount = tbid.amount;
		double _hasInvestedAmount = tbid.has_invested_amount;
		
		if (tbid.min_invest_amount > 0) {
			double _minInvestAmount = tbid.min_invest_amount;

			this.minInvestAmount = _minInvestAmount; // 最低金额招标

			/* 算出最低允许投标金额 */
			if (_amount - _hasInvestedAmount < _minInvestAmount) {
				this.minAllowInvestAmount = _amount - _hasInvestedAmount;
			} else {
				this.minAllowInvestAmount = _minInvestAmount;
			}
		}

		if (tbid.average_invest_amount > 0) {
			this.averageInvestAmount = tbid.average_invest_amount; // 平均金额招标
		}

		int bonus_type = tbid.bonus_type; // 0 不奖励 1 固定金额奖励 2 按比例奖励
		 
		if (bonus_type == Constants.FIXED_AMOUNT_REWARD) {
			this.bonus = tbid.bonus; // 固定奖金
		}

		if (bonus_type == Constants.PROPORTIONATELY_REWARD) {
			this.awardScale = tbid.award_scale; // 奖励百分比
		}
		
		boolean _isAgency = tbid.is_agency; // 是否是机构标
		
		/* 如果是合作机构标 */
		if (_isAgency == Constants.ENABLE) {
			this.agencyId = tbid.agency_id; // 合作机构ID
		}
		
		this.userId = tbid.user_id; // 用户ID
		this.productId = tbid.product_id; // 产品ID
		this.isAgency = _isAgency; // 是否是机构标
		this.repayment = new Repayment();
		this.repayment.id = tbid.repayment_type_id; // 返款类型
		this.purpose = new Purpose();
		this.purpose.id = tbid.loan_purpose_id; //借款用途
		this.isShowAgencyName = tbid.is_show_agency_name; // 是否显示机构标名称
		this.time = tbid.time; // 发布时间
		this.title = tbid.title; // 标题
		this.amount = _amount; // 借款金额
		this.periodUnit = tbid.period_unit; // 借款期限单位
		this.period = tbid.period; // 借款期限
		this.investPeriod = tbid.invest_period; // 投标期限
		this.investExpireTime = tbid.invest_expire_time; // 满标日期
		this.realInvestExpireTime = tbid.real_invest_expire_time; // 实际满标日期
		this.apr = tbid.apr; // 年利率
		this.bankAccountId = tbid.bank_account_id; // 借款标绑定的银行卡
		this.bonusType = bonus_type; // 奖励方式
		this.isQuality = tbid.is_quality; // 优质标
		this.isHot = tbid.is_hot; // "火"标
		this.bail = tbid.bail; // 保证金
		this.serviceFees = tbid.service_fees; // 服务费
		this.imageFilename = tbid.image_filename; // 借款图片
		this.description = tbid.description; // 借款描述
		
		this.project_introduction = tbid.project_introduction;
		this.company_info = tbid.company_info;
		this.repayment_res = tbid.repayment_res;
		this.risk_control = tbid.risk_control;
		this.about_risk = tbid.about_risk;
		this.feeType = tbid.feeType;
		this.is_new = tbid.is_new;
		this.repayment_tips = tbid.repayment_tips;
		this.sell_time = tbid.sell_time;
		this.qixi_date = tbid.qixi_date;
		this.repayall_date = tbid.repayall_date;
		this.moneyback_time = tbid.moneyback_time;
		
		
		this.status = tbid.status; // 状态值
		this.readCount = tbid.read_count; // 浏览量
		this.loanSchedule = tbid.loan_schedule; // 借款进度比例
		this.hasInvestedAmount = _hasInvestedAmount; // 已投总额
		this.allocationSupervisorId = tbid.allocation_supervisor_id; // 审核人
		this.manageSupervisorId = tbid.manage_supervisor_id; // 分配审核人
		this.auditTime = tbid.audit_time; // 审核时间
		this.auditSuggest = tbid.audit_suggest;// 审核意见
		this.isAuditmaticInvestBid = tbid.is_auditmatic_invest_bid; // 自动投标的标识 0 // 否 1 是
		this.isSecBid = tbid.is_sec_bid; // 是否秒还
		this.isDealPassword = tbid.is_deal_password; //是否需要交易密码
		this.showType = tbid.show_type; // 发布方式
		this.qr_code = tbid.qr_code;
		
		this.purpose = new Purpose();
		this.purpose.id = tbid.loan_purpose_id;
		
		this.repaymentTime = tbid.repayment_time;
	}
	
	/**
	 * 用户资料针对对产品通过率
	 */
	public double getUserItemPassScale() {
		if(0 == this.userItemPassScale) {
			int result = UserAuditItem.queryUserItemScale(this.userId, this.mark, Constants.AUDITED);
			int size = 0;
			Object ids = null;
			
			String sql = "select count(1) from t_product_audit_items_log where mark = ? and type = 1";
			
			try {
				ids = JPA.em().createNativeQuery(sql).setParameter(1, this.mark).getSingleResult();
			} catch (Exception e) {
				Logger.error("资料->查询产品必选的科目： " + e.getMessage());
				
				return 0;
			}
			
			if (null != ids) {
				size = ((BigInteger) ids).intValue();
			}
			
			if(size == 0 || result / size >= 1)
				this.userItemPassScale = 100;
			else
				this.userItemPassScale = Arith.div(result, size, 4) * 100;
		}
		
		return this.userItemPassScale;
	}
	
	/**
	 * 我要借款,时间最新的未满借款标 
	 * @param error 信息值
	 * @return Bid
	 */
	public static List<Bid> queryAdvertisement(ErrorInfo error) {
		error.clear();
	
		List<t_bids> tbids = null;
		List<Bid> bids = new ArrayList<Bid>();
		
		String hql = "select new t_bids" +
				"(user_id, id, time, amount, apr)" +
				" from t_bids where amount > has_invested_amount and status in (1,2) order by time desc";

		try {
			tbids = t_bids.find(hql).fetch(Constants.NEW_FUNDRAISEING_BID);
		} catch (Exception e) {
			Logger.error("标->我要借款,时间最新的未满借款标 :" + e.getMessage());
			error.msg = "时间最新的未满借款标 ,加载失败!";
			
			return null;
		}
		
		if(tbids == null)
			return bids;
		
		Bid bid = null;
		
		for (t_bids tbid : tbids) {
			bid = new Bid();
			
			bid._id = tbid.id;
			bid._userId = tbid.user_id;
			bid.time = tbid.time;
			bid.amount = tbid.amount;
			bid.apr = tbid.apr;
			
			bids.add(bid);
		}
		
		return bids;
	}

	/**
	 * 我要借款,最新5个满标
	 * @param pageBean 分页对象
	 * @param error 信息值
	 * @return List<Bid> 
	 */
	public static List<Bid> queryFullBid(PageBean<Bid> pageBean, ErrorInfo error) {
		int count = 0;
		String condition = "amount = has_invested_amount and status in (?, ?)";

		try {
			/* 得到总记录数 */
			count = (int) t_bids.count(condition, Constants.BID_ADVANCE_LOAN, Constants.BID_FUNDRAISE);
		} catch (Exception e) {
			Logger.error("标->我要借款,最新5个满标。查询总记录数:" + e.getMessage());
			error.msg = "最新5个满标,加载失败!";

			return null;
		}

		List<Bid> bids = new ArrayList<Bid>();

		if (count < 0)
			return bids;

		pageBean.setTotalNum(count); 

		List<t_bids> tbids = null;
		String hql = "select new t_bids" +
					 "(id, image_filename, amount)" +
					 " from t_bids where " + condition + 
					 " order by time desc";

		try {
			tbids = t_bids.find(hql, Constants.BID_ADVANCE_LOAN, Constants.BID_FUNDRAISE).fetch(pageBean.currPage, pageBean.pageSize);
		} catch (Exception e) {
			Logger.error("标->我要借款,最新" + Constants.FULL_BID_COUNT + "个满标:" + e.getMessage());
			error.msg = "最新满标,加载失败!";

			return null;
		}
		
		if(null == tbids)
			return bids;
		
		Bid bid = null;
		
		for (t_bids tbid : tbids) {
			bid = new Bid();

			bid._id = tbid.id;
			bid.imageFilename = tbid.image_filename;
			bid.amount = tbid.amount;

			bids.add(bid);
		}
		
		return bids;
	}
	
	/**
	 * 查询是否有逾期还款
	 */
	public boolean isHasOverdue() {
		Long count = null;
		String sql = "select count(1) from t_bills where bid_id = ? and overdue_mark <> 0";
		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql).setParameter(1, this.id);
		Object record = null;

		try {
			record = query.getSingleResult();
		} catch (Exception e) {
			Logger.error("标->查询是否有逾期还款:" + e.getMessage());
			
			return false;
		}
		
		if(record == null)
			return false;
		
		count = Long.parseLong(record.toString());
		
		return  count > 0 ? true : false;
	}
	
	/**
	 * 获取近期还款时间
	 */
	public Date getRecentRepayTime() {
		if(null == this.recentRepayTime){
			String hql = "select repayment_time from t_bills where bid_id = ? order by repayment_time";

			try {
				this.recentRepayTime = t_bills.find(hql, this.id).first();
			} catch (Exception e) {
				Logger.error("标->获取近期还款时间:" + e.getMessage());
				
				return null;
			}
		}
		
		return this.recentRepayTime;
	}

	/**
	 * 投标次数
	 */
	public int getInvestCount() {
		if(0 == this.investCount) {
			try {
				this.investCount = (int) t_invests.count("bid_id = ?", this.id);
			} catch (Exception e) {
				Logger.error("标->投标次数:" + e.getMessage());
	
				return 0;
			}
		}
		
		return this.investCount;
	}
	
	/**
	 * 提问次数
	 */
	public int getQuestionCount(){
		return 0;
	}
	
	/**
	 * 设置为优质标
	 * @param bid 标ID
	 * @param error 信息值
	 */
	public static void editQuality(long bid, boolean status, ErrorInfo error){
		error.clear();
		
		String hql="update t_bids set is_quality=? where id=?";
		Query query=JPA.em().createQuery(hql);
		query.setParameter(1, status); 
		query.setParameter(2, bid);
		
		try {
			error.code = query.executeUpdate();
		} catch (Exception e) {
			Logger.error("标->设置为优质标:" + e.getMessage());
			error.msg = "设置优质标失败了!";
			
			return;
		}
		
		if(error.code < 1){
			error.msg = "设置失败!";
			
			return;
		}
		
		/* 添加事件 */
		if(status)
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.SET_QUALITY_BID, "设置优质标", error);
		else
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.SET_QUALITY_BID, "取消优质标", error);
		
		if(error.code < 0){
			JPA.setRollbackOnly();
			error.msg = "设置失败!";
			
			return;
		}
		
		error.msg = "设置成功!";
	}
	
	/**
	 * 设置为"火"标
	 * @param bid 标ID
	 * @param error 信息值
	 */
	public static void editHot(long bid, boolean status, ErrorInfo error){
		error.clear();
		
		String hql="update t_bids set is_hot=? where id=?";
		Query query=JPA.em().createQuery(hql);
		query.setParameter(1, status); 
		query.setParameter(2, bid);
		
		try {
			error.code = query.executeUpdate();
		} catch (Exception e) {
			Logger.error("标->设置为优质标:" + e.getMessage());
			error.msg = "设置优质标失败了!";
			
			return;
		}
		
		if(error.code < 1){
			error.msg = "设置失败!";
			
			return;
		}
		
		/* 添加事件 */
		if(status)
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.SET_HOT_BID, "设置火标", error);
		else
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.SET_HOT_BID, "设置火标", error);
		
		if(error.code < 0){
			JPA.setRollbackOnly();
			error.msg = "设置失败!";
			
			return;
		}
		
		error.msg = "设置成功!";
	}
	
	/**
	 * 检查是否流标(定时任务，基于自动事务)
	 */
	public static void checkBidIsFlow(){
		List<Long> ids = null;
		String hql = "select id from t_bids where status in (?, ?) and now() > invest_expire_time";
		
		try {
			ids = t_bids.find(hql, Constants.BID_ADVANCE_LOAN, Constants.BID_FUNDRAISE).fetch(); 
		} catch (Exception e) {
			Logger.error("标->检查是否流标:" + e.getMessage());
			
			return;
		}
		
		if(null == ids || ids.size() == 0) return;
		
		Bid bid = null;
		ErrorInfo error = new ErrorInfo();
		
		JPAPlugin.closeTx(false);
		
		for (Long id : ids) {
			try{
				JPAPlugin.startTx(false);

				bid = new Bid();
				bid.auditBid = true;
				bid.id = id;
				
				if(null == bid.user)
					continue;
				
				switch (bid.status) {
				case Constants.BID_ADVANCE_LOAN:
					bid.advanceLoanToFlow(error);
					
					if(error.code < 0) 
						Logger.info("自动流标失败编号(提前借款->流标)：" + id);
	
					break ;

				case Constants.BID_FUNDRAISE:
					bid.fundraiseToFlow(error);
					
					if(error.code < 0)
						Logger.info("自动流标失败编号(筹款中->流标)：" + id);

					break ;
				}
			}catch(Exception e){
				Logger.error("自动流标失败编号(筹款中->流标)：" + e.getMessage());
			}finally{
				
				JPAPlugin.closeTx(false);
				Logger.info("自动流标事务正常关闭，id = %s ", id);
			}
		}
	}
	
	/**
	 * 30分钟未回复短信的标的置为失败
	 */
	public static void verifyToFail() {
		List<Object> ids = null;
		String sql = "select id from t_bids where status in (?, ?, ?) and now() > DATE_ADD(time,INTERVAL 30 MINUTE)";
		Query query = JPAUtil.createNativeQuery(sql, Constants.BID_AUDIT_VERIFY, Constants.BID_ADVANCE_LOAN_VERIFY, Constants.BID_FUNDRAISE_VERIFY);		
		
		try {
			ids = query.getResultList();
		} catch (Exception e) {
			Logger.error("30分钟未回复短信的标的置为失败:" + e.getMessage());
			
			return;
		}
		
		if(null == ids || ids.size() == 0) {
			return;
		}
		
		ErrorInfo error = new ErrorInfo();
		
		for (Object id : ids) {
			Bid bid = new Bid();
			bid.id = Long.parseLong(id.toString());
			bid.verifyToFail(error);
		}
	}
	
	/**--------------------------------------------------发布借款-----------------------------------------------------------------*/
	
	/**
	 * 检查数据
	 */
	public void checkBid(ErrorInfo error){
		error.code = -1;
		
		if (StringUtils.isBlank(this.title) || this.title.length() > 24) {
			error.msg = "借款标题有误!";
			
			return;
		}
		
		int _amount = (int) this.amount;
		
		if (this.amount <= 0 || this.amount != _amount || this.amount < this.product.minAmount || this.amount > this.product.maxAmount) {
			error.msg = "借款金额有误!";
			
			return;
		}
		
		if (this.apr <= 0 || this.apr > 100 || this.apr < this.product.minInterestRate || this.apr > this.product.maxInterestRate) {
			error.msg = "年利率有误!";
			
			return;
		}
		
		if (this.product.loanImageType == Constants.USER_UPLOAD && (StringUtils.isBlank(this.imageFilename) || this.imageFilename.contains(Constants.DEFAULT_IMAGE))) {
			error.msg = "借款图片有误!";
			
			return;
		}
		
		if (this.purpose.id < 0) {
			error.msg = "借款用途有误!";
			
			return;
		}
		
		if (this.repayment.id < 0) {
			error.msg = "借款类型有误!";
			
			return;
		}

		if (this.period <= 0) {
			error.msg = "借款期限有误!";
			
			return;
		}
		
		switch (this.periodUnit) {
		case Constants.YEAR:
			
			if (this.period > Constants.YEAR_PERIOD_LIMIT) {
				error.msg = "借款期限超过了" + Constants.YEAR_PERIOD_LIMIT + "年";
				
				return;
			}
			
			break;
		case Constants.MONTH:
			
			if (this.period > Constants.YEAR_PERIOD_LIMIT * 12) {
				error.msg = "借款期限超过了" + Constants.YEAR_PERIOD_LIMIT + "年";
				
				return;
			}
			
			break;
		case Constants.DAY:
			
			if (this.period > Constants.YEAR_PERIOD_LIMIT * 12 * 30) {
				error.msg =  "借款期限超过了" + Constants.YEAR_PERIOD_LIMIT + "年";
				
				return;
			}
			
			if (this.investPeriod > this.period) {
				error.msg = "天标满标期限不能大于借款期限 !";
				
				return;
			}
			
			break;
		default:
			error.msg = "借款期限单位有误!";
			
			return;
		}

		if ((this.minInvestAmount > 0 && this.averageInvestAmount > 0) || (this.minInvestAmount <= 0 && this.averageInvestAmount <= 0)) {
			error.msg = "最低投标金额和平均招标金额有误!";
			
			return;
		}
		
		if (this.averageInvestAmount > 0 && this.amount % this.averageInvestAmount != 0) {
			error.msg = "平均招标金额有误!";
			
			return;
		}
		
		if (this.investPeriod <= 0) {
			error.msg = "投标期限有误!";
			
			return;
		}
		
		if (StringUtils.isBlank(this.description) || this.description.length() > 2000) {
			error.msg = "借款描述有误!";
			
			return;
		}
		
		if (this.bonusType == Constants.FIXED_AMOUNT_REWARD && (this.bonus < 0 || this.bonus > this.amount)) {
			error.msg = "固定奖励大于了借款金额";
			
			return;
		}
		
		if (this.bonusType == Constants.PROPORTIONATELY_REWARD && (this.awardScale < 0 || this.awardScale > 100)) {
			error.msg = "借款奖励比例有误!";
			
			return;
		}
		
		if (this.minInvestAmount > 0 && (this.minInvestAmount < this.product.minInvestAmount)) {
			error.msg = "最低投标金额不能小于产品最低投标金额!";
			
			return;
		}
		
		if (this.averageInvestAmount > 0 && (this.amount / this.averageInvestAmount > this.product.maxCopies)) {
			error.msg = "平均投标份数不能大于产品的最大份数限制 !";
			
			return;
		}

		if(this.product.loanType == Constants.S_REPAYMENT_BID && this.periodUnit != Constants.DAY) {
			error.msg = "秒还标借款期限需为天[标]!";
			
			return;
		}
		
		error.code = 1;
	}
	
	/**
	 * 发布借款标
	 * @param error
	 * @return
	 */
	public void createBid(ErrorInfo error) {
		error.clear();
		
		this.checkBid(error);
		
		if(error.code < 0)
			return;
		
		/* 数据防篡改 */
		DataSafety bidUserData = new DataSafety();
		bidUserData.setId(this.userId);
		bidUserData.signCheck(error);
		
		if(error.code < 0){
			error.code = -1; 
			error.msg = "系统检测到您的资金异常，请及时联系平台管理员!";

			return ;
		}
		
		/* 是否达到了产品的最低借款信用等级 */
		error.code = CreditLevel.compareWith(this.userId, this.product.creditId, error);
		 
		 if(error.code < 0){
			 error.code = -2;
			 error.msg = "您的信用等级不足发布此借款标!";
			 
			 return ;
		 }
		
		/* 算出保证金    金额*比例/100 */
		this.bail = Arith.mul(this.amount, Arith.div(this.product.bailScale, 100, 10));
	
		if(this.bail < 0){
			error.code = -3;
			error.msg = "系统在算保证金的时候出现错误!";
			
			return ;
		}
		
		/* 可用余额需大于或等于保证金 */
		if(this.bail > this.user.balance){
			/* 如果是合作机构标 */
			if(this.agencyId > 0) { 
				error.code = Constants.BAIL_NOT_ENOUGH; 
				error.msg = "直接借款人可用余额为:" + this.user.balance + ",不足发布借款,无法达到系统需冻结的保证金!";
				
				return;
			}
			
			error.code = Constants.BAIL_NOT_ENOUGH; 
			error.msg = "您的可用余额为:" + this.user.balance + ",不足发布借款,无法达到系统需冻结的保证金。请及时充值!";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("rechargeType", RechargeType.CREATBID);
			map.put("fee", this.bail);
			map.put("bid", this);
			map.put("productId", this.productId);
			
			if (Constants.IPS_ENABLE) {
				Cache.set("rechargePayIps"+this.user.id, map, IPSConstants.CACHE_TIME);
			} else {
				Cache.set("rechargePay"+this.user.id, map, IPSConstants.CACHE_TIME);
			}
			
			return ;
		}
		
		/* 得到服务费 */
		this.serviceFees = ServiceFee.loanServiceFee(this.amount, this.period, this.periodUnit, error);
			
		if (this.serviceFees < 0) {
			error.code = -3;
			error.msg = "获取系统管理费失败!";

			return ;
		}

		/* 冻结保证金不能大于借款金额 */
		if(this.serviceFees > this.amount){
			error.code = -13;
			error.msg = "服务费大于了借款金额!";
			JPA.setRollbackOnly();
			
			return ;
		}
		
		/* 所扣除的金额是不允许大于借款金额的 */
		if(this.bonusType == Constants.FIXED_AMOUNT_REWARD){
			
			if(Arith.add(this.serviceFees, this.bonus) > this.amount){
				error.code = -4;
				error.msg = "服务费 + 奖励金额大于了借款金额!";

				return ;
			}
		}
		
		if(this.bonusType == Constants.PROPORTIONATELY_REWARD){
			double scaleAmount =  Arith.div(Arith.mul(this.amount, this.awardScale), 100, 10);
			
			if(Arith.add(this.serviceFees, scaleAmount) > this.amount){
				error.code = -5;
				error.msg = "服务费 + 奖励金额大于了借款金额!";

				return ;
			}
		}
		
		/**
		 * PD说VIP没有服务费折扣!
		 */
		
//		/* 判断是否会员 */
//		if (this.user.vipStatus) {
//			/* 得到服务费折扣 */
//			String vipfees = OptionKeys.getvalue(OptionKeys.VIP_DISCOUNT, error);
//			
//			if (!NumberUtil.isNumericDouble(vipfees)) {
//				error.code = -4;
//				
//				return;
//			}
//			
//			double vipfees2 = Double.parseDouble(vipfees);
//			
//			/* 如果有折扣  */
//			if(vipfees2 > 0){
//				/* 服务费-服务费*比例值/100 */ 
//				this.serviceFees = Arith.mul(this.serviceFees, Arith.div(vipfees2, 10, 20));
//				
//				if (this.serviceFees < 0) {
//					error.code = -5;
//					error.msg = "折扣过后服务费有误!";
//
//					return;
//				}
//			}
//		}
		
		/* 用户是否可以发这个属性的标,返回需冻结金额 */
		this.bail = this.loanTypeIsPassByUser(error);
		
		if(this.bail < 0) {
			error.code = -13;
			JPA.setRollbackOnly();
			
			return ;
		}
		
		/* 冻结保证金不能大于借款金额 */
		if(this.bail > this.amount){
			error.code = -13;
			error.msg = "冻结保证金大于了借款金额!";
			JPA.setRollbackOnly();
			
			return ;
		}
		
		/* 根据审核机制确定发标状态  */
		this.ensureStatusByMechanism(error);

		if(error.code < 0) {
			error.code = -17;
			error.msg = "根据审核机制确定发标状态时出现错误!";
			JPA.setRollbackOnly();
			
			return ;
		}
		
		/* 添加用户针对这个标没有提交的资料 */
		error.code = UserAuditItem.addBidAuditItem(this.userId, this.product.mark, error);
		
		if(error.code < 0) {
			error.code = -18;
			JPA.setRollbackOnly();
			
			return ;
		}
		
		t_bids tbid = new t_bids();
		
		tbid.mark = this.product.mark;
		if(null==this.presellTime){
			tbid.time = new Date(); // 申请时间
		}else{
			DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				tbid.time =format.parse(this.presellTime);//添加了预售逻辑
			} catch (Exception e) {
				tbid.time = new Date(); // 申请时间
			}
		}
		tbid.title = this.title; // 标题
		tbid.amount = this.amount; // 金额
		tbid.period_unit =  this.product.loanType == Constants.S_REPAYMENT_BID ? Constants.DAY : this.periodUnit; // 借款期限单位
		tbid.period = this.period; // 期限
		tbid.apr = this.apr; // 年利率
		tbid.invest_period = this.investPeriod; // 满标期限
		tbid.description = this.description; // 借款描述

		tbid.project_introduction = this.project_introduction;//项目简述
		tbid.company_info = this.company_info;//相关企业信息
		tbid.repayment_res = this.repayment_res;//还款来源
		tbid.risk_control = this.risk_control;//风控措施
		tbid.about_risk = this.about_risk;//风险提示
		tbid.feeType = this.feeType;//手续费
		tbid.is_new = this.is_new;//是否新手标
		tbid.repayment_tips = this.repayment_tips;//还款提示
		tbid.sell_time = this.sell_time;//开售时间
		tbid.qixi_date = this.qixi_date;//起息日
		tbid.repayall_date = this.repayall_date;//还本结息日
		tbid.moneyback_time = this.moneyback_time;//预计资金到账时间
		
		
		tbid.status = this.status;
		tbid.product_id = this.productId; // 产品ID
		tbid.user_id = this.userId; // 用户ID
		tbid.loan_purpose_id = this.purpose.id; // 借款用途
		tbid.repayment_type_id = 
		this.product.loanType == Constants.S_REPAYMENT_BID || this.periodUnit == Constants.DAY ? Constants.ONCE_REPAYMENT : this.repayment.id; // 还款类型
		tbid.bonus_type = this.bonusType; // 奖励方式
		if(this.bonusType == Constants.FIXED_AMOUNT_REWARD) tbid.bonus = this.bonus; // 1 固定金额奖励
		if(this.bonusType == Constants.PROPORTIONATELY_REWARD) tbid.award_scale = this.awardScale; // 按比例奖励
		if(this.minInvestAmount > 0) tbid.min_invest_amount = this.minInvestAmount; // 最低金额招标
		if(this.averageInvestAmount >0) tbid.average_invest_amount = this.averageInvestAmount; // 平均金额招标
		if(this.product.loanType == Constants.S_REPAYMENT_BID) tbid.is_sec_bid = true; // 是否秒还
		
		if(this.product.loanImageType == Constants.PLATFORM_UPLOAD) tbid.image_filename = this.product.loanImageFilename; // 系统借款图片
		else if(StringUtils.isNotBlank(this.imageFilename)) tbid.image_filename = this.imageFilename; // 借款图片
		
		/* 判断是否合作机构发布 */
		if (this.agencyId > 0) {
			tbid.is_agency = Constants.ENABLE; // 标示合作机构状态
			tbid.agency_id = this.agencyId; // 合作机构ID
			tbid.is_show_agency_name = this.isShowAgencyName; // 是否显示机构合作名称
			tbid.status = 0;//2015.3.16机构表改为后台审核
		}
		
		tbid.is_deal_password = this.product.isDealPassword; //是否需要交易密码
		tbid.show_type = this.product.showType; // 发布方式
		tbid.bail = Arith.round(this.bail, 2);// 保证金
		tbid.service_fees = Arith.round(this.serviceFees, 2); // 服务费
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		tbid.invest_rate = backstageSet.investmentFee; // 理财管理费
		tbid.overdue_rate = backstageSet.overdueFee; // 逾期管理费
		
		tbid.is_quality = false; // 优质标(默认false)
		tbid.is_hot = false; // 火标(默认false)
		//tbid.is_auditmatic_invest_bid = false; // 自动投标(默认为false)
		tbid.loan_schedule = 0; // 借款进度比例(默认为0)
		tbid.has_invested_amount = 0; // 已投总额(默认为0)
		tbid.read_count = 0; // 阅读次数(默认为0)
		tbid.bank_account_id = 0; // 绑定银行卡(默认为0)
		tbid.allocation_supervisor_id = 0; // 审核人(默认为0)
		tbid.manage_supervisor_id = 0; //分配审核人
		tbid.audit_time = null; // 审核时间(默认为null)
		tbid.audit_suggest = null; // 审核意见(默认为null)
		tbid.last_repay_time = null; // 最后放款时间(默认为null)
		
		if(Constants.IPS_ENABLE) {
			this.bidNo = Payment.createBillNo(this.userId, IPSOperation.REGISTER_SUBJECT);
			this.merBillNo = Payment.createBillNo(this.userId, IPSOperation.REGISTER_SUBJECT);
//			this.bidNo  = this.bidNo.substring(this.bidNo.length() - 14);
			tbid.bid_no = this.bidNo;
			tbid.mer_bill_no = this.merBillNo;
			tbid.status += 10;//提交资金托管平台处理中
		}
		
		try {
			/* 发布 */
			tbid.save();
		} catch (Exception e) {
			Logger.error("标->发布借款:" + e.getMessage());
			error.code = -19;
			error.msg = "发布失败!";
			JPA.setRollbackOnly();

			return ;
		}
		
		this._id = tbid.id;
		
		if(tbid.id < 1){
			error.code = -21;
			error.msg = "发布失败!";
			JPA.setRollbackOnly();
			
			return ;
		}
		
		if(Constants.IPS_ENABLE) {
			IpsDetail detail = new IpsDetail();
			detail.merBillNo = this.merBillNo;
			detail.userName = this.userName;
			detail.time = new Date();
			detail.type = IPSOperation.REGISTER_SUBJECT;
			detail.status = Status.FAIL;
			detail.create(error);
			
			IpsDetail.setIpsInfo(Long.parseLong(tbid.bid_no), "{\"bidId\":\""+tbid.id+"\"}", error);
//			Cache.set("bid_"+IPSConstants.BID_CREATE+"_"+tbid.bid_no, this, IPSConstants.CACHE_TIME);
			
			return ;
		}
		
		afterCreateBid(tbid, null, error);
	}
	
	/**
	 * 发布借款标后，改变相应的记录
	 * @param error 信息值
	 */
	public void afterCreateBid(t_bids tbid, String bidNo, ErrorInfo error) {
		error.clear();
		
		if (Constants.IPS_ENABLE) {
			if(tbid.status >= 0 && tbid.status < 10) {
				error.code = -5;
				return;
			}
			
			JPAUtil.executeUpdate(error, "update t_bids set status = status - 10 where id = ?", tbid.id);
			
			if (error.code < 0) {
				return;
			}
		}
		
		/* 如果是提前借款，修改满标的具体期限 */
		if(tbid.status != Constants.BID_AUDIT) {
			error.code = Bid.addInvestExpireTime(tbid.id, tbid.invest_period);
			
			if(error.code < 1) {
				error.code = -22;
				error.msg = "修改具体的投标时间失败!";
				JPA.setRollbackOnly();
				
				return;
			}
		}
		
		/* 修改用户为借款会员 */
		User.updateMasterIdentity(this.userId, Constants.LOAN_USER, error);
		
		if(error.code < 0) {
			error.code = -23;
			error.msg = "修改用户为借款会员失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 冻结用户资金(算出来的保证金,秒还还需加上利息) */
	    error.code = DealDetail.freezeFund(this.userId, this.bail);
		
		if (error.code < 1) {
			error.code = -24;
			error.msg = "冻结您的资金失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		DealDetail dealDetail = null;
		Map<String, Double> detail = DealDetail.queryUserFund(this.userId, error);
		double user_amount = detail.get("user_amount");
		double freeze = detail.get("freeze");
		double receive_amount = detail.get("receive_amount");
		
		/* 用户的金额是不能小于0的!  */
		if(user_amount < 0 || freeze < 0){
			error.code = -20;
			error.msg = "资金有误,请联系管理员,给您带来不便敬请原谅!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 伪构记录 */
		dealDetail = new DealDetail(this.userId, DealType.PAY_FREEZE_FUND,
				this.bail, tbid.id, user_amount, freeze - this.bail,
				receive_amount, "发布借款成功，支出借款保证金");
		
		dealDetail.addDealDetail(error);
		
		if (error.code < 0) {
			error.code = -25;
			error.msg = "添加交易记录失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		/* 添加交易记录 */
		if(Constants.S_REPAYMENT_BID == this.product.loanType)
			
			dealDetail = new DealDetail(this.userId, DealType.FREEZE_SBID_BAIL,
					this.bail, tbid.id, user_amount, freeze,
					receive_amount, "冻结秒还标保证金(服务费+利息+保证金)" + this.bail + "元");
		else
			dealDetail = new DealDetail(this.userId, DealType.FREEZE_BID_BAIL,
					this.bail, tbid.id, user_amount, freeze,
					receive_amount, "冻结非秒还标保证金" + this.bail + "元");
		
		dealDetail.addDealDetail(error);
		
		if (error.code < 0) {
			error.code = -25;
			error.msg = "添加交易记录失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		DataSafety bidUserData2 = new DataSafety();
		
		bidUserData2.setId(this.userId);//更新数据防篡改字段
		bidUserData2.updateSign(error);
		
		if (error.code < 0) {
			error.code = -26;
			error.msg = "更新防数据篡改失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		/* 更新用户的缓存余额 */
		//detail = DealDetail.queryUserFund(this.userId, error);
		//user_amount = detail.get("user_amount");
		//freeze = detail.get("freeze");
		//User user = User.currUser();
		//user.balance = user_amount;
		//user.freeze = freeze;
		
		/* 添加事件 */
		if(tbid.is_agency)
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.CREATE_AGENCY_BID, "管理员发布合作机构标", error);
		else
			DealDetail.userEvent(this.userId, UserEvent.ADD_BID, "用户发布借款标", error);
		
		if(error.code < 0){
			error.code = -27;
			error.msg = "发布失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 状态借款,提示语 */
		if(tbid.status == Constants.BID_AUDIT){
			error.msg = "发布成功，待管理员审核!";
		}else if(tbid.status == Constants.BID_ADVANCE_LOAN){
			error.msg = "发布成功，状态为提前借款!";
		
			/* 执行自动投标 */	
			//new Invest().automaticInvest();
		}
		
		/**
		 * 生成二维码代码
		 */
		String uuid = UUID.randomUUID().toString();
		Qrcode code = new Qrcode();
		Blob blob = new Blob();
		String str = Constants.BASE_URL + "front/invest/invest?bidId=" + tbid.id;
		
		try {
			code.create(str, BarcodeFormat.QR_CODE, 100, 100, new File(blob.getStore(), uuid).getAbsolutePath(), "png");
		} catch (Exception e) {
			Logger.info("标->创建二维码图片失败" + e.getMessage());
			error.code = -28;
			error.msg = "创建二维码图片失败!";
			JPA.setRollbackOnly();
			
			return ;
		}
		
		/* 保存二维码标识 */
		Query query = JPA.em().createQuery("update t_bids set qr_code = ? where id = ?");
		query.setParameter(1, uuid);
		query.setParameter(2, tbid.id);
		
		try {
			error.code = query.executeUpdate();
		} catch (Exception e) {
			Logger.info("标->保存二维码标识" + e.getMessage());
			error.code = -29;
			error.msg = "创建二维码图片失败!";
			JPA.setRollbackOnly();
			
			return ;
		}
		
		if(error.code < 1){
			error.code = -30;
			error.msg = "创建二维码图片失败!";
			JPA.setRollbackOnly();
			
			return ;
		}
		
		/* 发送邮件、站内信、短信通知用户 */
		String content = null;
		TemplateStation station = new TemplateStation();
		TemplateEmail email = new TemplateEmail();
		TemplateSms sms = new TemplateSms();
		email.setId(Templets.E_CREATE_BID);
		station.setId(Templets.M_CREATE_BID);
		sms.setId(Templets.S_CREATE_BID);
		
		if(station.status){
			content = this.createBidNotice(station.content);
			station.addMessageTask(this.userId, station.title, content);
		}
		
		if(email.status){
			content = this.createBidNotice(email.content);
			email.addEmailTask(this.user.email, email.title, content);
		}
		
		if(sms.status){
			content = this.createBidNotice(sms.content);
			sms.addSmsTask(this.user.mobile, content);
		}
		
		/* 请置于code的值大于2,避免和标示起冲突，出现非法弹框 */
		error.code = 3; 
		this._id = tbid.id;
		
		if(Constants.IPS_ENABLE) {
			Cache.delete("t_bids_"+tbid.bid_no);
		}
	}
	
	/**
	 * 发布借款
	 */
	private String createBidNotice(String content){
		 content = content.replace("userName", this.user.name); 
		 content = content.replace("date", DateUtil.dateToString((new Date()))); 
		 content = content.replace("loanType", this.product.strLoanType); 
		 content = content.replace("title", this.title); 
		 content = content.replace("amount", this.amount + ""); 
		 content = content.replace("bail", this.bail + ""); 
		 content = content.replace("status", this.strStatus); 
		 
		 return content;
	}
	
	/**
	 * 根据审核机制确定发标状态
	 */
	private void ensureStatusByMechanism(ErrorInfo error) {
		/* 如果是合作机构 */
		if (this.agencyId > 0) {
			Logger.info("---------------------合作机构------------------------");
			/**
			 * 11-21 调整为根据用户提交的资料来确定状态 
			 */
			/* 得到用户未上传资料的ID集合 */
			List<Long> itemid = UserAuditItem.getNotUploadItemId(this.product.requiredAuditItem, this.user.auditItems);
			
			if(null == itemid) {
				this.status = Constants.BID_AUDIT;
				
				return;
			}
				
			if(0 == itemid.size()) 
				this.status = Constants.BID_FUNDRAISE; // 状态为筹款中
			else
				this.status = Constants.BID_ADVANCE_LOAN; // 状态为提前借款
		} else {
			/* 得到当前审核机制 */
			String _value =  OptionKeys.getvalue(OptionKeys.AUDIT_MECHANISM, error); 

			if(StringUtils.isBlank(_value)) {
				error.code = -14;
				
				return;
			}

			/* 声明产品上传资料的集合 */
			List<ProductAuditItem> productItemsId = new ArrayList<ProductAuditItem>();

			switch(Integer.parseInt(_value)) {

			/* 先审后发 */
			case Constants.AUDIT_RELEASE:
				/* 其状态值为审核中 */
				this.status = Constants.BID_AUDIT;

				return;

			/* 先发后审 */
			case Constants.RELEASE_AUDIT:
				/* 其状态值为提前借款 */
				this.status = Constants.BID_ADVANCE_LOAN;

				return;

			/* 边发边审 */
			case Constants.RELEASE_AND_AUDIT:
				/* 得到审计资料比例模式 */
				String _mode = OptionKeys.getvalue(OptionKeys.AUDIT_MODE, error);
				
				if(StringUtils.isBlank(_mode)) {
					error.code = -15;
					
					return;
				}

				/* 得到提交资料比例 */
				String _scale = OptionKeys.getvalue(OptionKeys.AUDIT_SCALE, error);
				
				if(StringUtils.isBlank(_scale)) {
					error.code = -16;
					
					return;
				}
				
				double scale = Double.parseDouble(_scale);

				switch (Integer.parseInt(_mode)) {
				/* 所有 */
				case Constants.AUDIT_SCALE_ALL:
					/* 全部资料 */
					productItemsId = ProductAuditItem.queryAuditByProductMark(this.product.mark, true, true);

					break;

				/* 必须 */
				case Constants.AUDIT_SCALE_NEED:
					/* 必选资料 */
					productItemsId = this.product.requiredAuditItem;

					break;

				/* 可选 */
				case Constants.AUDIT_SCALE_PICK:
					productItemsId = this.product.selectAuditItem;

					break;
				}

				/* 如果提交资料的比例为0 */
				if (0 == scale) {
					/* 其状态值为提前借款 */
					this.status = Constants.BID_ADVANCE_LOAN;

					return;
				}
				
				/* 得到用户未上传资料的ID集合 */
				List<Long> itemid = UserAuditItem.getNotUploadItemId(productItemsId, this.user.auditItems);

				int size = itemid.size(); // 没有上传资料的大小

				/* 如果都已提交(产品审资料为0,也会出现此状况) */
				if (0 == size) {
					/* 其状态值为提前借款 */
					this.status = Constants.BID_ADVANCE_LOAN;

					return;
				}
				
				/* 算出没上传资料和用户有效资料的比例 */
				double scale2 = Arith.mul(Arith.div(size, productItemsId.size(), 10), 100);

				/* 比例大于需提交资料的比例 */
				if ((100 - scale2) > scale) {
					/* 其状态值为提前借款 */
					this.status = Constants.BID_ADVANCE_LOAN;
				} else {
					/* 其状态值为审核中 */
					this.status = Constants.BID_AUDIT;
				}

				return;
			}
		}
	}
	
	/**
	 * 用户的状态是否可以发这种属性的标
	 */
	private double loanTypeIsPassByUser(ErrorInfo error) {
		switch (this.product.loanType) {
		
		/* 秒还 */
		case Constants.S_REPAYMENT_BID:
			/* 获取利息 */
			double interest = ServiceFee.interestCompute(this.amount, this.apr, Constants.DAY, this.period, Constants.ONCE_REPAYMENT);

			if (interest < 0) {
				error.msg = "亲、算取借款利息失败!";
				
				return -6;
			}
			
			double fees = Arith.add(Arith.add(interest, this.serviceFees), this.bail);
			
			/* 所换利息+借款服务费不能大于用户可用资金 */
			if (fees > this.user.balance) {
				error.msg = "亲、您的可用余额还不能发布秒还标，请及时充值哦!";

				return -999;
			}
			
			return fees;
		
		/* 净值 */
		case Constants.NET_VALUE_BID:
			/* 用户待收 */
			double receive = Bill.forReceive(this.userId, error);
			
			if (receive < 0) {
				error.msg = "查询您的待收金额失败!";
				
				return -8;
			}
			
			/* 用户待还 */
			double pay = Bill.forPay(this.userId ,error);
			
			if (pay < 0) { 
				error.msg = "查询您的待还金额失败!";
				
				return -9;
			}

			/* 借款金额+保证金 */
			double netValue = Arith.add(this.amount, this.bail);

			/* 可用余额+待收-待还70% */
			double netValue2 = Arith.mul(Arith.sub(Arith.add(this.user.balance, receive), pay), 0.7);

			/* 借款金额+保证金<(可用余额+待收-待还)* 70% */
			if (netValue > netValue2) {
				error.msg = "亲、您的可用余额还不能发布净值标，请及时充值哦!";

				return -999;
			}
			
			return this.bail;

		/* 信用 */
		case Constants.CREDIT_BID:
			/* 用户信用额度不能超过对应的借款金额 */
			if (this.amount > this.user.creditLine) {
				error.msg = "亲、您所借款的金额超过了自己的信用额度哦!";

				return -11;
			}
			
			return this.bail;
			
		/* 普通 */
		case Constants.GENERAL_BID:
			return this.bail;
		}
		
		return -12;
	}
	
	/**
	 * 计算借款标总利息
	 * @param amount
	 * @param apr
	 * @param period
	 * @param unit
	 * @param repaymentType
	 * @return
	 */
	/*public static double getTotalInterest(double amount, double apr, int period, int unit, long repaymentType) {
		//天标
		if (unit == Constants.DAY) {
			return Arith.mul(Arith.mul(amount, apr / (double)Constants.DAY_INTEREST), period);
		}
		
		int monthNum = (unit == Constants.MONTH) ? period : (period * 12);
		double monthRate = (apr * 0.01) / 12.0;
		
		//按月还款、等额本息
		if (repaymentType == Constants.PAID_MONTH_EQUAL_PRINCIPAL_INTEREST) {
			return monthNum * Arith.mul(amount, monthRate) * Math.pow((1 + monthRate), monthNum) / (Math.pow((1 + monthRate), monthNum) - 1) - amount;
		}
		
		//按月付息、一次还款
		if(repaymentType == Constants.PAID_MONTH_ONCE_REPAYMENT || repaymentType == Constants.ONCE_REPAYMENT){
			return monthNum * Arith.mul(amount, monthRate);
		}

		return 0;
	}*/
	
	/**
	 * 计算理财管理费
	 * @param bidId
	 * @param investAmount
	 * @return
	 */
	public static double getInvestFee(long bidId, double investAmount) {
		/*t_bids tbid = null;

		try {
			tbid = t_bids.findById(bidId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			
			return 0;
		}

		if (null == tbid) {
			return 0;
		}
		
		double amount = tbid.amount;
		double apr = tbid.apr;
		int period = tbid.period;
		int unit = tbid.period_unit;
		long repaymentType = tbid.repayment_type_id;
		double per = investAmount / amount;
		double rate = BackstageSet.getCurrentBackstageSet().investmentFee / 100;
		
		return Arith.round(Arith.mul(per * getTotalInterest(amount, apr, period, unit, repaymentType), rate), 2);*/
		
		return 0;
	}

	/**
	 * 根据期限、年利率、金额、算出利息(秒还)
	 */
	public static double getInterest(int period, int unit, Double apr, double amount) {
		double ymdapr = 0; // 年、月、日 年利率

		switch (unit) {

		case Constants.YEAR: // 年
			// 公式： this.apr * this.amount / 100;
			ymdapr = Arith.div(apr, 1, 10);

			break;

		case Constants.MONTH: // 月
			// 公式： this.apr / 12 * this.period * this.amount / 100;
			ymdapr = Arith.div(apr, 12, 10);

			break;

		case Constants.DAY: // 日
			// 公式： this.apr / 12 / 30 * this.period * this.amount / 100;
			ymdapr = Arith.div(apr, Constants.DAY_INTEREST, 10);

			break;

		default:

			return -1;
		}

		/* 算出利息 */
		return Arith.div(Arith.mul(Arith.mul(ymdapr, period), amount), 100, 2);
	}
	
	/**--------------------------------------------------审核/撤销、流标-----------------------------------------------------------------*/
	
	/**
	 * 检查当前已投金额和借款金额是否相等
	 */
	private static long checkBidStatus(long bidId){
		Long id = null;
		String hql = "select id from t_bids where id = ? and has_invested_amount = amount and loan_schedule = 100";
		
		try {
			id = t_bids.find(hql, bidId).first();
		} catch (Exception e) {
			return 0;
		}
		 
		if(null == id)
			return 0;
		
		return id;
	}
	
	/**
	 * 审核中->提前借款
	 */
	public void auditToadvanceLoan(ErrorInfo error){
		error.code = -1;
		int row = 0;
		
		/* 1.确定当前bid的状态 */
		if(Constants.BID_AUDIT != this.status){
			error.msg = "非法审核!";
			
			return;
		}
		
		if(Constants.BID_ADVANCE_LOAN == this.status){
			error.msg = "审核失败,请确定当前标是否已经被审核为“提前借款”!";
			
			return;
		}
		
		/* 2.修改状态 */
		String hql = "update t_bids set allocation_supervisor_id=?, audit_time=?, status=? where id=? and user_id=? and status=?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, this.allocationSupervisorId);// 审核人
		query.setParameter(2, new Date());// 审核时间
		query.setParameter(3, Constants.BID_ADVANCE_LOAN);// 审核状态为提前借款
		query.setParameter(4, this.id); // 标ID
		query.setParameter(5, this.userId); // 用户ID
		query.setParameter(6, Constants.BID_AUDIT); // 当前状态必须为审核中

		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标->审核中->提前借款:" + e.getMessage());
			error.msg = "修改状态失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		if(row < 1){
			error.msg = "审核失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		this.status = Constants.BID_ADVANCE_LOAN;
		
		/* 3.修改满标期限 */
		row = Bid.addInvestExpireTime(this.id, investPeriod); 
		
		if(row < 1){
			error.msg = "修改满标期限失败!";
			
			return;
		}
		
		/* 4.添加事件 */
		DealDetail.supervisorEvent(this.allocationSupervisorId, SupervisorEvent.AUDIT_BID, "审核标：审核中->提前借款", error);
		
		if(error.code < 0){
			error.msg = "添加事件失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 5.通知 */
		this.auditToadvanceLoanNotice();
		
		error.code = 1;
		error.msg = "审核成功,已将标置为[提前借款]!";
	}
	
	/**
	 * 审核中->提前借款/筹款中,通知
	 */
	private String auditToadvanceLoanNotice(){
		String content = null;
		TemplateStation station = new TemplateStation();
		TemplateEmail email = new TemplateEmail();
		TemplateSms sms = new TemplateSms();
		email.setId(Templets.E_FIRST_SUCCESS);
		station.setId(Templets.M_FIRST_SUCCESS);
		sms.setId(Templets.S_FIRST_SUCCESS);
		
		if(station.status){
			content = this.auditToadvanceLoanNotice(station.content); 
			station.addMessageTask(this.userId, station.title, content);
		}
		
		if(email.status){
			content = this.auditToadvanceLoanNotice(email.content);
			email.addEmailTask(this.user.email, email.title, content);
		}
		
		if(sms.status){
			content = this.auditToadvanceLoanNotice(sms.content);
			sms.addSmsTask(this.user.mobile, content);
		}
		
		return content;
	}
	
	private String auditToadvanceLoanNotice(String content){
		content = content.replace("userName", this.user.name); 
		content = content.replace("date", DateUtil.dateToString((new Date()))); 
		content = content.replace("title", this.title); 
		content = content.replace("status", this.strStatus); 
		
		return content;
	}
	
	/**
	 * 审核中->筹款中
	 */
	public void auditToFundraise(ErrorInfo error){
		error.code = -1;
		int row = 0;
		
		/* 1.确定当前bid的状态 */
		if(Constants.BID_AUDIT != this.status){
			error.msg = "非法审核!";
			
			return;
		}
		
		if(Constants.BID_FUNDRAISE == this.status){
			error.msg = "审核失败,请确定当前标是否已经被审核为“筹款中”!";
			
			return;
		}
		
		/* 2.确定通过资料比例 */
		row = partAuditPassOperate(this.userId, this.mark); 
		
		if(row > 0){
			error.msg = "审核失败,请确定资料是否提交完毕或通过审核!";
			
			return;
		}
		
		/* 3.修改状态 */
		String hql = "update t_bids set allocation_supervisor_id=?, audit_time=?, audit_suggest=?, status=? where id=? and user_id=? and status=?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, this.allocationSupervisorId);// 审核人
		query.setParameter(2, new Date());// 审核时间
		query.setParameter(3, this.auditSuggest);// 审核意见
		query.setParameter(4, Constants.BID_FUNDRAISE);// 审核状态为筹款中
		query.setParameter(5, this.id); // 标ID
		query.setParameter(6, this.userId); // 用户ID
		query.setParameter(7, Constants.BID_AUDIT); // 当前状态必须为审核中

		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标->审核中->筹款中:" + e.getMessage());
			error.msg = "修改状态失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		if(row < 1){
			error.msg = "审核失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		this.status = Constants.BID_FUNDRAISE;
		
		/* 4.修改满标期限 */
		row = Bid.addInvestExpireTime(this.id, investPeriod); 
		
		if(row < 1){
			error.msg = "修改满标期限失败!";
			
			return;
		}
		
		/* 5.添加事件 */
		DealDetail.supervisorEvent(this.allocationSupervisorId, SupervisorEvent.AUDIT_BID, "审核标：审核中->筹款中", error);
		
		if(error.code < 0){
			error.msg = "添加事件失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 6.通知 */
		this.auditToadvanceLoanNotice();
		
		error.code = 1;
		error.msg = "审核成功,已将标置为[筹款中]!";
	} 
	
	/**
	 * 提前借款->筹款中
	 */
	public void advanceLoanToFundraise(ErrorInfo error){
		error.code = -1;
		int row = 0;
		
		/* 1.确定当前bid的状态 */
		if(Constants.BID_ADVANCE_LOAN != this.status){
			error.msg = "非法审核!";
			
			return;
		}
		
		if(Constants.BID_FUNDRAISE == this.status){
			error.msg = "审核失败,请确定当前标是否已经被审核为“筹款中”!";
			
			return;
		}
		
		/* 2.确定通过资料比例 */
		row = partAuditPassOperate(this.userId, this.mark); 
		
		if(row > 0){
			error.msg = "审核失败,请确定资料是否提交完毕或通过审核!";
			
			return;
		}
		
		/* 3.修改状态 */
		String hql = "update t_bids set allocation_supervisor_id=?, audit_time=?, audit_suggest=?, status=? where id=? and user_id=? and status=?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, this.allocationSupervisorId);// 审核人
		query.setParameter(2, new Date());// 审核时间
		query.setParameter(3, this.auditSuggest);// 审核意见
		query.setParameter(4, Constants.BID_FUNDRAISE);// 审核状态为筹款中
		query.setParameter(5, this.id); // 标ID
		query.setParameter(6, this.userId); // 用户ID
		query.setParameter(7, Constants.BID_ADVANCE_LOAN); // 当前状态必须为提前借款

		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标->提前借款->筹款中:" + e.getMessage());
			error.msg = "审核失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		if(row < 1){
			error.msg = "审核失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 4.添加事件 */
		DealDetail.supervisorEvent(this.allocationSupervisorId, SupervisorEvent.AUDIT_BID, "审核标：提前借款->筹款中", error);
		
		if(error.code < 0){
			error.msg = "添加事件失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		error.code = 1;
		error.msg = "审核成功,已将标置为[筹款中]!";
	}
	
	/**
	 * 审核中->审核不通过
	 */
	public void auditToNotThrough(ErrorInfo error){
		error.code = -1;
		
		/* 1.确定当前bid的状态 */
		if(Constants.BID_AUDIT != this.status){
			error.msg = "非法审核!";
			
			return;
		}
		
		if(Constants.BID_NOT_THROUGH == this.status){
			error.msg = "审核失败,请确定当前标是否已经被审核为“审核不通过”!";
			
			return;
		}
		
		if(Constants.IPS_ENABLE) {
			error.code = 0;
			IpsDetail.setIpsInfo(Long.parseLong(this.bidNo), "{\"bidId\":\""+this.id+"\"}", error);
//			Cache.set("bid_"+IPSConstants.BID_CANCEL_S+"_"+this.bidNo, this, IPSConstants.CACHE_TIME);
			
			return ;
		}
		
		error.code = 0;
		auditToNotThroughBC(error);
	}
	
	public void auditToNotThroughBC(ErrorInfo error) {
		error.clear();
		int row = 0;
		
		/* 2.修改状态 */
		String hql = "update t_bids set allocation_supervisor_id=?, audit_time=?, audit_suggest=?, status=? where id=? and user_id=? and status=?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, this.allocationSupervisorId);// 审核人
		query.setParameter(2, new Date());// 审核时间
		query.setParameter(3, this.auditSuggest);// 审核意见
		query.setParameter(4, Constants.BID_NOT_THROUGH);// 审核状态为审核不通过
		query.setParameter(5, this.id); // 标ID
		query.setParameter(6, this.userId); // 用户ID
		query.setParameter(7, Constants.BID_AUDIT); // 当前状态必须为审核中

		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标->审核中->审核不通过:" + e.getMessage());
			error.msg = "审核失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		if(row < 1){
			error.msg = "审核失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		this.status = Constants.BID_NOT_THROUGH;
			
		/* 3.返还冻结保证金 */
		this.relieveUserBailFund("审核中->审核不通过", error);
		
		if(error.code < 1){
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 4.添加事件 */
		DealDetail.supervisorEvent(this.allocationSupervisorId, SupervisorEvent.AUDIT_BID, "审核标：审核中->审核不通过", error);
		
		if(error.code < 0){
			JPA.setRollbackOnly();
			
			return;
		}
		
		if(Constants.IPS_ENABLE) {
			deleteIPSBid(IPSConstants.BID_CANCEL_S);
			refundInvestBonus(error);
			
			if (error.code < 0) {
				JPA.setRollbackOnly();
				
				return;
			}
		}
		
		error.code = 1;
		error.msg = "审核成功,已将标置为[审核不通过]!";
	} 
	
	/**
	 * 提前借款->借款中不通过
	 */
	public void advanceLoanToPeviewNotThrough(ErrorInfo error){
		error.code = -1;
		
		/* 1.确定当前bid的状态 */
		if(Constants.BID_ADVANCE_LOAN != this.status){
			error.msg = "非法审核!";
			
			return;
		}
		
		if(Constants.BID_PEVIEW_NOT_THROUGH == this.status){
			error.msg = "审核失败,请确定当前标是否已经被审核为[借款中不通过]!";
			
			return;
		}
		
		if(Constants.IPS_ENABLE) {
			error.code = 0;
			IpsDetail.setIpsInfo(Long.parseLong(this.bidNo), "{\"bidId\":\""+this.id+"\"}", error);
//			Cache.set("bid_"+IPSConstants.BID_CANCEL_B+"_"+this.bidNo, this, IPSConstants.CACHE_TIME);
			
			return ;
		}
		
		error.code = 0;
		advanceLoanToPeviewNotThroughBC(error);
	}
	
	public void advanceLoanToPeviewNotThroughBC(ErrorInfo error) {
		error.clear();
		int row = 0;
		
		/* 2.修改状态 */
		String hql = "update t_bids set allocation_supervisor_id=?, audit_time=?, audit_suggest=?, status=? where id=? and user_id=? and status=?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, this.allocationSupervisorId);// 审核人
		query.setParameter(2, new Date());// 审核时间
		query.setParameter(3, this.auditSuggest);// 审核意见
		query.setParameter(4, Constants.BID_PEVIEW_NOT_THROUGH);// 审核状态为借款中不通过
		query.setParameter(5, this.id); // 标ID
		query.setParameter(6, this.userId); // 用户ID
		query.setParameter(7, Constants.BID_ADVANCE_LOAN); // 当前状态必须为提前借款

		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标->提前借款->借款中不通过:" + e.getMessage());
			error.msg = "审核失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		if(row < 1){
			error.msg = "审核失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		this.status = Constants.BID_PEVIEW_NOT_THROUGH;
		
		/* 3.返还借款人冻结保证金 */
		this.relieveUserBailFund("提前借款->借款中不通过", error);
		
		if(error.code < 1){
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 4.返还投资人投资金额 */
		this.returnInvestUserFund("提前借款->借款中不通过", error); 
		
		if(error.code < 1){
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 5.添加事件 */
		DealDetail.supervisorEvent(this.allocationSupervisorId, SupervisorEvent.AUDIT_BID, "审核标：提前借款->借款中不通过", error);
		
		if(error.code < 0){
			error.msg = "添加事件失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		if(Constants.IPS_ENABLE) {
			deleteIPSBid(IPSConstants.BID_CANCEL_B);
			refundInvestBonus(error);
			
			if (error.code < 0) {
				JPA.setRollbackOnly();
				
				return;
			}
		}
		
		error.code = 1;
		error.msg = "审核成功,已将标置为[借款中不通过]!";
	} 
	
	/**
	 * 筹款中->借款中不通过
	 */
	public void fundraiseToPeviewNotThrough(ErrorInfo error){
		error.code = -1;
		
		/* 1.确定当前bid的状态 */
		if(Constants.BID_FUNDRAISE != this.status){
			error.msg = "非法审核!";
			
			return;
		}
		
		if(Constants.BID_PEVIEW_NOT_THROUGH == this.status){
			error.msg = "审核失败,请确定当前标是否已经被审核为[借款中不通过]!";
			
			return;
		}
		
		if(Constants.IPS_ENABLE) {
			error.code = 0;
			IpsDetail.setIpsInfo(Long.parseLong(this.bidNo), "{\"bidId\":\""+this.id+"\"}", error);
//			Cache.set("bid_"+IPSConstants.BID_CANCEL_I+"_"+this.bidNo, this, IPSConstants.CACHE_TIME);
			
			return ;
		}
		
		error.code = 0;
		fundraiseToPeviewNotThroughBC(error);
	}	
		
	public void fundraiseToPeviewNotThroughBC(ErrorInfo error) {
		error.clear();
		int row = 0;
		
		/* 2.修改状态 */
		String hql = "update t_bids set allocation_supervisor_id=?, audit_time=?, status=? where id=? and user_id=? and status=?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, this.allocationSupervisorId);// 审核人
		query.setParameter(2, new Date());// 审核时间
		query.setParameter(3, Constants.BID_PEVIEW_NOT_THROUGH);// 审核状态为借款中不通过
		query.setParameter(4, this.id); // 标ID
		query.setParameter(5, this.userId); // 用户ID
		query.setParameter(6, Constants.BID_FUNDRAISE); // 当前状态必须为筹款中

		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标->筹款中->借款中不通过:" + e.getMessage());
			error.msg = "审核失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		if(row < 1){
			error.msg = "审核失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		this.status = Constants.BID_PEVIEW_NOT_THROUGH;
		
		/* 3.返还借款人冻结保证金 */
		this.relieveUserBailFund("筹款中->借款中不通过", error);
		
		if(error.code < 1){
			error.msg = "返还冻结保证金失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 4.返还投资人投资金额 */
		this.returnInvestUserFund("筹款中->借款中不通过", error); 
		
		if(error.code < 1){
			error.msg = "返还投资用户投资金额失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 5.添加事件 */
		DealDetail.supervisorEvent(this.allocationSupervisorId, SupervisorEvent.AUDIT_BID, "审核标：筹款中->借款中不通过", error);
		
		if(error.code < 0){
			error.msg = "添加事件失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		if(Constants.IPS_ENABLE) {
			deleteIPSBid(IPSConstants.BID_CANCEL_I);
			refundInvestBonus(error);
			
			if (error.code < 0) {
				JPA.setRollbackOnly();
				
				return;
			}
		}
		
		error.code = 1;
		error.msg = "审核成功,已将标置为[借款中不通过]!";
	}
	
	/**
	 * 满标->待放款
	 */
	public void fundraiseToEaitLoan(ErrorInfo error){
		error.code = -1;
		int row = 0;
		
		/* 1.确定当前bid的状态 */
		if(Constants.BID_FUNDRAISE != this.status){
			error.msg = "非法审核!";
			
			return;
		}
		
		if(this.hasInvestedAmount < this.amount || this.loanSchedule < 100){
			error.msg = "非法审核!";
			
			return;
		}
		
		if(Constants.BID_EAIT_LOAN == this.status){
			error.msg = "审核失败,请确定当前标是否已经被审核为“待放款”!";
			
			return;
		}
		
		/* 2.确定通过资料比例 */
		row = partAuditPassOperate(this.userId, this.mark); 
		
		if(row > 0){
			error.msg = "审核失败,请确定资料是否提交完毕或通过审核!";
			
			return;
		}
		
		/* 3.修改状态 */
		String hql = "update t_bids set allocation_supervisor_id=?, audit_time=?, status=? where id=? and user_id=? and status=?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, this.allocationSupervisorId);// 审核人
		query.setParameter(2, new Date());// 审核时间
		query.setParameter(3, Constants.BID_EAIT_LOAN);// 审核状态为待放款
		query.setParameter(4, this.id); // 标ID
		query.setParameter(5, this.userId); // 用户ID
		query.setParameter(6, Constants.BID_FUNDRAISE); // 当前状态必须为筹款中

		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标->满标->待放款:" + e.getMessage());
			error.msg = "修改状态失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		if(row < 1){
			error.msg = "审核失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 4.再次确定已投总额和借款金额是否想等 */
		long bidId = Bid.checkBidStatus(this.id);
		
		if(bidId != this.id){
			error.msg = "借款标有误，请确定是否满标!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 5.添加事件 */
		DealDetail.supervisorEvent(this.allocationSupervisorId, SupervisorEvent.AUDIT_BID, "审核标：满标->待放款", error);
		
		if(error.code < 0){
			error.msg = "添加事件失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 6.通知 */
		String content = null;
		TemplateStation station = new TemplateStation();
		TemplateEmail email = new TemplateEmail();
		TemplateSms sms = new TemplateSms();
		email.setId(Templets.E_FULL_SUCCESS);
		station.setId(Templets.M_FULL_SUCCESS);
		sms.setId(Templets.S_FULL_SUCCESS);
		
		if(station.status){
			content = this.fundraiseToEaitLoanNotice(station.content);
			station.addMessageTask(this.userId, station.title, content);
		}
		
		if(email.status){
			content = this.fundraiseToEaitLoanNotice(email.content);
			email.addEmailTask(this.user.email, email.title, content);
		}
		
		if(sms.status){
			content = this.fundraiseToEaitLoanNotice(sms.content);
			sms.addSmsTask(this.user.mobile, content);
		}
		
		error.code = 1;
		error.msg = "审核成功,已将标置为[待放款]!";
	} 
	
	/**
	 * 满标->待放款,通知
	 */
	private String fundraiseToEaitLoanNotice(String content){
		content = content.replace("userName", this.user.name); 
		content = content.replace("date", DateUtil.dateToString((new Date()))); 
		content = content.replace("title", this.title); 
		content = content.replace("amount", this.amount + ""); 
		content = content.replace("servicefees", this.serviceFees + ""); 
		
		double excitationSum = Constants.PROPORTIONATELY_REWARD == this.bonusType ?
							   Arith.div(Arith.mul(this.awardScale, this.amount), 100, 2) :
							   this.bonus;	   
		
		content = content.replace("excitationSum", excitationSum + "");
		content = content.replace("repaymentType", this.repayment.name);
		content = content.replace("period", this.period + "");
		content = content.replace("unit", this.strPeriodUnit);
		content = content.replace("apr", this.apr + "");
		
		return content;
	}
	
	/**
	 * 满标->放款不通过
	 */
	public void fundraiseToLoanNotThrough(ErrorInfo error) {
		error.code = -1;
		
		/* 1.确定当前bid的状态 */
		if(Constants.BID_FUNDRAISE != this.status){
			error.msg = "非法审核!";
			
			return;
		}
		
		if(this.hasInvestedAmount < this.amount || this.loanSchedule < 100){
			error.msg = "非法审核!";
			
			return;
		}
		
		if(Constants.BID_LOAN_NOT_THROUGH == this.status){
			error.msg = "审核失败,请确定当前标是否已经被审核为“放款不通过”!";
			
			return;
		}
		
		if(Constants.IPS_ENABLE) {
			error.code = 0;
			IpsDetail.setIpsInfo(Long.parseLong(this.bidNo), "{\"bidId\":\""+this.id+"\"}", error);
//			Cache.set("bid_"+IPSConstants.BID_CANCEL_M+"_"+this.bidNo, this, IPSConstants.CACHE_TIME);
			
			return ;
		}
		
		error.code = 0;
		fundraiseToLoanNotThroughBC(error);
	}	
	
	public void fundraiseToLoanNotThroughBC(ErrorInfo error) {
		error.clear();
		int row = 0;
		
		/* 2.修改状态 */
		String hql = "update t_bids set allocation_supervisor_id=?, audit_time=?, status=? where id=? and user_id=? and status=?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, this.allocationSupervisorId);// 审核人
		query.setParameter(2, new Date());// 审核时间
		query.setParameter(3, Constants.BID_LOAN_NOT_THROUGH);// 审核状态为放款不通过
		query.setParameter(4, this.id); // 标ID
		query.setParameter(5, this.userId); // 用户ID
		query.setParameter(6, Constants.BID_FUNDRAISE); // 当前状态必须为筹款中

		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标->满标->放款不通过:" + e.getMessage());
			error.msg = "修改状态失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		if(row < 1){
			error.msg = "审核失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		this.status = Constants.BID_LOAN_NOT_THROUGH;
		
		/* 3.再次确定已投总额和借款金额是否想等 */
		long bidId = Bid.checkBidStatus(this.id);
		
		if(bidId != this.id){
			error.msg = "借款标有误，请确定是否满标!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 4.返还借款人冻结保证金 */
		this.relieveUserBailFund("筹款中->借款中不通过", error);
		
		if(error.code < 1){
			error.msg = "返还冻结保证金失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 5.返还投资人投资金额 */
		this.returnInvestUserFund("满标->放款不通过", error); 
		
		if(error.code < 1){
			error.msg = "返还投资用户投资金额失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 6.添加事件 */
		DealDetail.supervisorEvent(this.allocationSupervisorId, SupervisorEvent.AUDIT_BID, "审核标：满标->放款不通过", error);
		
		if(error.code < 0){
			error.msg = "添加事件失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		if(Constants.IPS_ENABLE) {
			deleteIPSBid(IPSConstants.BID_CANCEL_S);
			refundInvestBonus(error);
			
			if (error.code < 0) {
				JPA.setRollbackOnly();
				
				return;
			}
		}
		
		error.code = 1;
		error.msg = "审核成功,已将标置为[放款不通过]!";
	} 
	
	/**
	 * 待验证->失败
	 * @param error
	 */
	public void verifyToFail(ErrorInfo error) {
		error.code = -1;
		
		/* 确定当前bid的状态 */
		if (this.status < Constants.BID_AUDIT_VERIFY || this.status > Constants.BID_FUNDRAISE_VERIFY) {
			error.msg = "标的未处于待验证状态,操作失败";
			
			return;
		}
		
		/*超过30分钟未回复短信才能撤销*/
		if (DateUtil.diffMinutes(this.time, new Date()) < 30) {
			error.msg = "请发标30分钟后再撤销";
			
			return;
		}
		
		int row = JPAUtil.executeUpdate(error, "update t_bids set status = ? where id = ?", Constants.BID_NOT_VERIFY, this.id);
		
		if (row < 1) {
			error.msg = "数据未更新";
			
			return;
		}
		
		/*退回投标奖励 */
		this.refundInvestBonus(error);
		
		if (error.code < 0) {
			return;
		}
		
		error.code = 1;
		error.msg = "撤销待验证借款标成功";
	}

	/**
	 * 待放款->还款中
	 */
	public void eaitLoanToRepayment(ErrorInfo error){
		error.code = -1;
		
		/* 确定当前bid的状态 */
		if(Constants.BID_EAIT_LOAN != this.status){
			error.msg = "非法审核!";
			
			return;
		}
		
		if(this.hasInvestedAmount < this.amount || this.loanSchedule < 100){
			error.msg = "非法审核!";
			
			return;
		}
		
		if(Constants.BID_REPAYMENT == this.status){
			error.msg = "审核失败,请确定当前标是否已经被审核为“还款中”!";
			
			return;
		}
		
		DataSafety bidUserData = new DataSafety(); // 借款会员数据防篡改对象
		bidUserData.setId(this.userId);
		if(!bidUserData.signCheck(error)) {
			error.msg = "借款用户资金有异常,无法放款!";
			
			return;
		}
		
		if (Constants.IPS_ENABLE && !this.isRepair) {
			error.code = 1;
			
			return;
		}
		
		doEaitLoanToRepayment(error);
	}  
	
	public void doEaitLoanToRepayment(ErrorInfo error){
		error.code = -1;
		
		/* 修改状态 */
		String hql = "update t_bids set allocation_supervisor_id=?, audit_time=?, status=? where id=? and user_id=? and status=?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, this.allocationSupervisorId);// 审核人
		query.setParameter(2, new Date());// 审核时间
		query.setParameter(3, Constants.BID_REPAYMENT);// 审核状态为还款中 
		query.setParameter(4, this.id); // 标ID
		query.setParameter(5, this.userId); // 用户ID
		query.setParameter(6, Constants.BID_EAIT_LOAN); // 当前状态必须为待放款

		int row = 0;
		
		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标->待放款->还款中:" + e.getMessage());
			error.msg = "修改状态失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		if(row < 1){
			error.msg = "审核失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 关闭自动投标 */
		row = Invest.closeUserBidRobot(this.userId);
		
		if(row < 1) {
			error.msg = "关闭自动投标失败!";

			return;
		}
		
		DealDetail dealDetail = null;
		String summary = null;
		Map<String, Double> detail = null;
		
		/* 返还保证金 */
		row = DealDetail.relieveFreezeFund(this.userId, this.bail); // 用户对应冻结的资金(保证金)

		if (row < 1) {
			error.msg = "返还保证金失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		if(this.isSecBid)
			summary = "放款、解冻秒还借款冻结保证金";
		else
			summary = "放款、解冻非秒还借款冻结保证金";
		
		detail = DealDetail.queryUserFund(this.userId, error);

		if(null == detail || detail.size() == 0){
			JPA.setRollbackOnly();
			
			return;
		}
		
		double user_amount = detail.get("user_amount");
		double freeze = detail.get("freeze");
		double receive_amount = detail.get("receive_amount");
		
		/* 添加交易记录 */
		dealDetail = new DealDetail(this.userId, DealType.RELIEVE_FREEZE_FUND,
				this.bail, this.id, user_amount - this.bail, freeze,
				receive_amount, summary + this.bail +"元");

		dealDetail.addDealDetail(error);
		
		if (error.code < 0) {
			error.msg = "返还保证金、添加交易记录失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		/* 伪构记录 */
		dealDetail = new DealDetail(this.userId, DealType.REVENUE_FREEZE_FUND,
				this.bail, this.id, user_amount, freeze,
				receive_amount, "返还借款保证金");

		dealDetail.addDealDetail(error);
		
		if (error.code < 0) {
			error.msg = "返还保证金、添加交易记录失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		/* 打钱给用户 */
		row = DealDetail.addUserFund(this.userId, this.amount);

		if (row < 1) {
			error.msg = "增加用户资金失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		detail = DealDetail.queryUserFund(this.userId, error);

		/* 添加交易记录 */
		dealDetail = new DealDetail(this.userId, DealType.ADD_LOAN_FUND,
				this.amount, this.id, detail.get("user_amount"), detail.get("freeze"),
				detail.get("receive_amount"), "增加用户借款金额");

		dealDetail.addDealDetail(error);
		
		if (error.code < 0) {
			error.msg = "打钱给用户、添加交易记录失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		/* 扣服务费  */
		row = DealDetail.minusUserFund(this.userId, this.serviceFees);
		
		if (row < 1) {
			error.msg = "扣服务费失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		detail = DealDetail.queryUserFund(this.userId, error);
		
		/* 添加交易记录 */
		dealDetail = new DealDetail(this.userId, DealType.CHARGE_LOAN_SERVER_FEE,
				this.serviceFees, this.id, detail.get("user_amount"), detail.get("freeze"),
				detail.get("receive_amount"), "财务放款，扣除用户借款管理费");

		dealDetail.addDealDetail(error);
		
		if (error.code < 0) {
			error.msg = "扣服务费、添加交易记录失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		/* 添加风险金 */
		dealDetail.addPlatformDetail(DealType.LOAN_FEE, this.id, this.userId,
				-1, DealType.ACCOUNT, this.serviceFees, 1, "财务放款，扣除用户借款管理费",
				error);
		
		if (error.code < 0) {
			error.msg = "添加风险金失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		/* 减去投资人的投标冻结金额/发奖金 */
		this.investUserAmountProcess(error);
		
		if(error.code < 1){
			JPA.setRollbackOnly();

			return;
		}
		
		/* 更新自己的防篡改 */
		DataSafety bidUserData = new DataSafety(); // 借款会员数据防篡改对象
		bidUserData.setId(this.userId);
		bidUserData.updateSign(error);
		
		/* 生成账单 */
		row = new Bill().addBill(this, error);

		if(error.code < 0){
			error.msg = "生成账单失败!";;
			JPA.setRollbackOnly();

			return;
		}
		
		/* 更新标的还款日期*/
		row = 0;
		String sql = "update t_bids set repayment_time = ? where id = ?";
		query = JPA.em().createQuery(sql).setParameter(1, this.recentRepayTime).setParameter(2, this.id);
		
		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			Logger.error("标->待放款->还款中：" + e.getMessage());
			error.msg = "修改标的还款日期失败！";
			JPA.setRollbackOnly();
			
			return;
		}
		
		if (row < 1) {
			error.msg = "审核失败！";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 添加CPS推广费 */
		User.rewardCPS(this.userId, this.serviceFees, this.id, error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}
		
		/* 再次确定是否满标状态 */
		long bidId = Bid.checkBidStatus(this.id);
		
		if(bidId != this.id){
			error.msg = "借款标有误，请确定是否满标!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 确定操作资金不为负数 */
		detail = DealDetail.queryUserFund(this.userId, error);
		user_amount = detail.get("user_amount");
		freeze = detail.get("freeze");
		
		if(user_amount < 0 || freeze < 0){
			error.msg = "借款用户资金出现负数!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 更新自己的防篡改 */
		bidUserData.setId(this.userId);
		bidUserData.updateSign(error);
		
		if(error.code < 0){
			error.msg = "更新数据防篡改字段异常!";
			JPA.setRollbackOnly();

			return;
		}
		
		/* 生成借款合同(借款人) */
		boolean flag = this.createPact();
		
		if(!flag) {
			error.msg = "生成借款合同(借款人)失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		Invest.creatInvestPact(this.id, error);
		
		if(error.code < 0){
			error.msg = "生成借款合同(投资人)失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		/* 添加事件 */
		DealDetail.supervisorEvent(this.allocationSupervisorId, SupervisorEvent.REPAYMENT_FUND, "审核标：待放款->还款中", error);
		
		if(error.code < 0){
			error.msg = "添加事件失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 通知 */
		String content = null;
		TemplateStation station = new TemplateStation();
		TemplateEmail email = new TemplateEmail();
		TemplateSms sms = new TemplateSms();
		email.setId(Templets.E_FINANC_RELEASE);
		station.setId(Templets.M_FINANC_RELEASE);
		sms.setId(Templets.S_FINANC_RELEASE);
		
		if(station.status){
			content = station.content;
			content = content.replace("userName", this.user.name);
			content = content.replace("date", DateUtil.dateToString((new Date())));
			content = content.replace("title", this.title);

			station.addMessageTask(this.userId, station.title, content);
		}
		
		if(email.status){
			content = email.content;
			content = content.replace("userName", this.user.name);
			content = content.replace("date", DateUtil.dateToString((new Date())));
			content = content.replace("title", this.title);
			
			email.addEmailTask(this.user.email, email.title, content);
		}
		
		if(sms.status){
			content = sms.content;
			content = content.replace("userName", this.user.name);
			content = content.replace("date", DateUtil.dateToString((new Date())));
			content = content.replace("title", this.title);
			
			sms.addSmsTask(this.user.mobile, content);
		}
		
		error.code = 1;
		error.msg = "审核成功,已将标置为[还款中]!";
		
		if (isReleaseSign) {
			/* 秒还则不模拟提现  */
			if(this.isSecBid) {
				error.code = 1;
				error.msg = "放款标记成功!";
				
				return ;
			}
				
			double deductMoney = 0;
			
			/* 求出奖励额度 */
			if (this.bonusType != Constants.NOT_REWARD) {
				switch (this.bonusType) {
				/* 固定奖金 */
				case Constants.FIXED_AMOUNT_REWARD:
					deductMoney = this.bonus; // 固定奖励金额

					break;

				/* 比例奖金 */
				case Constants.PROPORTIONATELY_REWARD:
					deductMoney = Arith.div(Arith.mul(this.awardScale, this.amount), 100, 2); // 比例奖金金额

					break;

				default:
					error.msg = "奖励方式有误!";
					JPA.setRollbackOnly();

					return;
				}
			}
			
			this.amount = Arith.sub(Arith.sub(this.amount, deductMoney), this.serviceFees);
			
			/* 减少借款人的借款金额(模拟提现)  */
			row = DealDetail.minusUserFund(this.userId, this.amount); 
			
			if (row < 1) {
				error.msg = "减少用户资金失败!";
				JPA.setRollbackOnly();

				return;
			}
			
			detail = DealDetail.queryUserFund(this.userId, error);
			
			/* 添加交易记录 */
			dealDetail = new DealDetail(this.userId, DealType.OFFLINE_REPAYMENT,
					this.amount, this.id, detail.get("user_amount"), detail.get("freeze"),
					detail.get("receive_amount"), "线下放款");

			dealDetail.addDealDetail(error);
			
			if (error.code < 0) {
				error.msg = "线下放款,添加交易记录失败!";
				JPA.setRollbackOnly();

				return;
			}
			
			/* 更新自己的防篡改 */
			bidUserData = new DataSafety();
			bidUserData.setId(this.userId);
			bidUserData.updateSign(error);
			
			/* 添加事件 */
			DealDetail.supervisorEvent(this.allocationSupervisorId, SupervisorEvent.REPAYMENT_FUND_SIGN, "放款标记", error);
			
			if(error.code < 0){
				error.msg = "线下放款,添加事件失败!";;
				JPA.setRollbackOnly();
				
				return;
			}
			
			error.code = 1;
			error.msg = "放款标记成功!";
		}
	}
	
	/**
	 * 扣除投资人的投资冻结金额/发奖金
	 */
	private void investUserAmountProcess(ErrorInfo error){
		error.code = -1;
		int row = 0;
		
		List<Invest> invests = Invest.queryAllInvestUser(this.id); // 获得用户对这个标所投的金额

		if (null == invests || invests.size() == 0) {
			error.msg = "获得用户对标所投的金额有误!";
			JPA.setRollbackOnly();

			return;
		}
		
		User invest_user = null;
		String content = null;
		TemplateStation station = new TemplateStation();
		TemplateEmail email = new TemplateEmail();
		TemplateSms sms = new TemplateSms();
		email.setId(Templets.E_INVEST_DEDUCT);
		station.setId(Templets.M_INVEST_DEDUCT);
		sms.setId(Templets.S_INVEST_DEDUCT);
		
		DataSafety bidUserData = new DataSafety();
		DealDetail dealDetail = null;
		Map<String, Double> detail = null;
		
		double cps = 0; // CPS产生费
		double fund = 0; // 投资人奖金
		double funds = 0; // 已发资金
		double deductMoney = 0; // 借款人奖金
		boolean flag = false; // 防篡改标示
		String _round = null; // 金额字符串形式
		
		/* 求出奖励额度 */
		if (this.bonusType != Constants.NOT_REWARD) {
			switch (this.bonusType) {
			/* 固定奖金 */
			case Constants.FIXED_AMOUNT_REWARD:
				deductMoney = this.bonus; // 固定奖励金额

				break;

			/* 比例奖金 */
			case Constants.PROPORTIONATELY_REWARD:
				deductMoney = Arith.div(Arith.mul(this.awardScale, this.amount), 100, 2); // 比例奖金金额

				break;

			default:
				error.msg = "奖励方式有误!";
				JPA.setRollbackOnly();

				return;
			}
		}
		
		/* 循环处理,涉及操作过多性能消耗较大 */
		for (int i = 0; i < invests.size(); i++) {
			Invest invest = invests.get(i);
			bidUserData.id = invest.investUserId;
			flag = bidUserData.signCheck(error);
			
			row = DealDetail.minusUserFreezeFund(invest.investUserId, invest.investAmount); // 减去用户冻结的投标资金
			
			if (row < 1) {
				error.msg = "减少投资用户的投资冻结金额金失败!";
				JPA.setRollbackOnly();

				return;
			}
			
			detail = DealDetail.queryUserFund(invest.investUserId, error);
			
			/* 添加交易记录 */
			dealDetail = new DealDetail(invest.investUserId, DealType.CHARGE_INVEST_FUND,
					invest.investAmount, this.id, detail.get("user_amount"), detail.get("freeze"),
					detail.get("receive_amount"), "扣除投标冻结金额");

			dealDetail.addDealDetail(error);
			
			if (error.code < 0) {
				error.msg = "扣除投标冻结金额、添加交易记录失败!";
				JPA.setRollbackOnly();

				return;
			}
			
			/* 10.存在奖励的情况 */
			if(this.bonusType != Constants.NOT_REWARD){
				if (i == invests.size() - 1) {
					fund = deductMoney - funds;
				} else {
					fund = Arith.round(Arith.mul(Arith.div(invest.investAmount, this.amount, 10), deductMoney), 2); // 根据固定金额奖励来算
					funds = Arith.add(funds, fund);
				}
				
				if (Constants.PAY_TYPE_FUND == PayType.INDEPENDENT || Constants.PAY_TYPE_FUND == PayType.SHARED) {
					row = DealDetail.addUserFund2(invest.investUserId, fund); // 增加用户的金额
				} else {
					row = DealDetail.addUserFund(invest.investUserId, fund); // 增加用户的金额
				}
				
				if (row < 1) {
					error.msg = "增加用户资金失败!";

					return;
				}
				
				detail = DealDetail.queryUserFund(invest.investUserId, error);
				
				/* 添加交易记录 */
				if (Constants.IPS_ENABLE) {
					dealDetail = new DealDetail(invest.investUserId, DealType.ADD_LOAN_BONUS,
							fund, this.id, detail.get("user_amount2"),  detail.get("freeze"),
							detail.get("receive_amount"), "增加用户投标奖励");

					dealDetail.addDealDetail2(error);
				} else {
					dealDetail = new DealDetail(invest.investUserId, DealType.ADD_LOAN_BONUS,
							fund, this.id, detail.get("user_amount"),  detail.get("freeze"),
							detail.get("receive_amount"), "增加用户投标奖励");

					dealDetail.addDealDetail(error);
				}
				
				if (error.code < 0) {
					error.msg = "增加用户投标奖励,添加交易记录失败!";

					return;
				}
			}
			
			bidUserData.setId(invest.investUserId); // 加防篡改标示(投资会员,只更新)
			/* 被动修改状态：只有在签名正确的情况下才能继续更新签名  */
			if(flag){
				bidUserData.id = invest.investUserId;
				bidUserData.updateSign(error);
			
				if (error.code < 0) {
					error.msg = "防篡改更新失败!";
					JPA.setRollbackOnly();

					return;
				}
			}
			
			invest_user = new User();
			invest_user.createBid = true;
			invest_user.id = invest.investUserId;
			
			_round = Arith.round(fund, 2) + "";
			
			if(station.status){
				content = station.content;
				content = content.replace("userName", invest_user.name); 
				content = content.replace("date", DateUtil.dateToString((new Date()))); 
				content = content.replace("title", this.title); 
			    content = content.replace("rebackAmount", invest.investAmount + ""); 
				content = content.replace("bonus", _round); 
				station.addMessageTask(invest_user.id, station.title, content);
			}
			
			if(email.status){
				content = email.content;
				content = content.replace("userName", invest_user.name); 
				content = content.replace("date", DateUtil.dateToString((new Date()))); 
				content = content.replace("title", this.title); 
			    content = content.replace("rebackAmount", invest.investAmount + ""); 
				content = content.replace("bonus", _round); 
				email.addEmailTask(invest_user.email, email.title, content);
			}
			
			if(sms.status){
				content = sms.content;
				content = content.replace("userName", invest_user.name); 
				content = content.replace("date", DateUtil.dateToString((new Date()))); 
				content = content.replace("title", this.title); 
			    content = content.replace("rebackAmount", invest.investAmount + ""); 
				content = content.replace("bonus", _round); 
				sms.addSmsTask(invest_user.mobile, content);
			}
		}
		
		if(this.bonusType != Constants.NOT_REWARD && !Constants.IPS_ENABLE){
			/* 减少借款人的奖励金额  */
			row = DealDetail.minusUserFund(this.userId, deductMoney);
			
			if (row < 1) {
				error.msg = "减少用户资金失败!";

				return;
			}

			detail = DealDetail.queryUserFund(this.userId, error);
			
			/* 添加交易记录 */
			dealDetail = new DealDetail(this.userId, DealType.CHARGE_BONUS_FEE,
					deductMoney, this.id, detail.get("user_amount"), detail.get("freeze"),
					detail.get("receive_amount"), "扣除用户投标奖励");

			dealDetail.addDealDetail(error);
			
			if (error.code < 0) {
				error.msg = "扣除用户投标奖励,添加交易记录失败!";

				return;
			}
		}
		
		error.code = 1;
	}
	
	/**
	 * 查询投标奖励信息
	 * @return
	 */
	public List<Map<String, Object>> queryInvestFunds(ErrorInfo error) {
		error.clear();
		
		if (this.bonusType == Constants.NOT_REWARD) {
			error.code = -1;
			error.msg = "该标的未设置投标奖励";
			
			return null;
		}
		
		double deductMoney = 0; // 借款人奖金
		double fund = 0; // 投资人奖金
		double funds = 0; // 已发资金
		
		/* 求出奖励额度 */
		if (this.bonusType != Constants.NOT_REWARD) {
			switch (this.bonusType) {
			/* 固定奖金 */
			case Constants.FIXED_AMOUNT_REWARD:
				deductMoney = this.bonus; // 固定奖励金额
				break;
			/* 比例奖金 */
			case Constants.PROPORTIONATELY_REWARD:
				deductMoney = Arith.div(Arith.mul(this.awardScale, this.amount), 100, 2); // 比例奖金金额
				break;
			default:
				error.code = -1;
				error.msg = "奖励方式未知";
				
				return null;
			}
		}
		
		List<Map<Object, Object>> invests = Invest.queryInvestInfo(this.id, error);
		
		if (error.code < 0) {
			return null;
		}
		
		List<Map<String, Object>> res = new ArrayList<Map<String,Object>>();
		
		for (int i = 0; i < invests.size(); i++) {
			Map<Object, Object> invest = invests.get(i);
			Map<String, Object> map = new HashMap<String, Object>();
			
			if (i == invests.size() - 1) {
				fund = deductMoney - funds;
			} else {
				fund = Arith.round(Arith.mul(Arith.div((Double)invest.get("amount"), this.amount, 10), deductMoney), 2);
				funds = Arith.add(funds, fund);
			}
			
			map.put("inCustId", invest.get("ipsAcctNo"));
			map.put("transAmt", String.format("%.2f", fund));
			res.add(map);
		}
		
		return res;
	}
	
	/**
	 * 放款标记
	 */
	public void releaseSign(ErrorInfo error){
		error.code = -1;
		//int row = 0;
		
		/* 1.放款 */
		this.eaitLoanToRepayment(error);
	}
	
	/**
	 * 提前借款->流标
	 */
	public void advanceLoanToFlow(ErrorInfo error){
		error.code = -1;
		
		/* 1.确定当前bid的状态 */
		if(Constants.BID_ADVANCE_LOAN != this.status){
			error.msg = "非法审核!";
			
			return;
		}
		
		if(Constants.BID_FLOW == this.status){
			error.msg = "审核失败,请确定当前标是否已经被审核为“流标”!";
			
			return;
		}	
	
		if(Constants.IPS_ENABLE) {
			error.code = 0;
			Cache.set("bid_"+IPSConstants.BID_ADVANCE_LOAN+"_"+this.bidNo, this, IPSConstants.CACHE_TIME);
			boolean flag = Payment.flow(IPSConstants.BID_ADVANCE_LOAN, this, this.userId, error);
			
			if(!flag) {
				error.code = -1;
				return ;
			}		
		}
		
		error.code = 1;
		advanceLoanToFlowBC(error);
	}
	
	public void advanceLoanToFlowBC(ErrorInfo error) {
		error.code = -1;
		int row = 0;
		
		/* 2.修改状态 */
		String hql = "update t_bids set status=? where id=? and user_id = ? and status =?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, Constants.BID_FLOW); // 审核状态为流标
		query.setParameter(2, this.id); // 标ID
		query.setParameter(3, this.userId); // 用户ID
		query.setParameter(4, Constants.BID_ADVANCE_LOAN); // 当前状态必须为提前借款
		
		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标->提前借款->流标:" + e.getMessage());
			error.msg = "审核失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		if(row < 1){
			error.msg = "审核失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		this.status = Constants.BID_FLOW;
		
		/* 3.返还借款人冻结保证金 */
		this.relieveUserBailFund("提前借款->流标", error);
		
		if(error.code < 1){
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 4.返还投资人投资金额 */
		this.returnInvestUserFund("提前借款->流标", error); 
		
		if(error.code < 1){
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 5.添加事件 */
		DealDetail.userEvent(this.userId, UserEvent.REPEAL_BID, "审核标：提前借款->流标", error);

		if(error.code < 0){
			error.msg = "添加事件失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		if(Constants.IPS_ENABLE) {
			deleteIPSBid(IPSConstants.BID_ADVANCE_LOAN);
			refundInvestBonus(error);
			
			if (error.code < 0) {
				JPA.setRollbackOnly();
				
				return;
			}
		}

		error.code = 1;
		error.msg = "审核成功,已将标置为[流标]!";
	} 
	
	/**
	 * 筹款中->流标
	 */
	public void fundraiseToFlow(ErrorInfo error){
		error.code = -1;
		
		/* 1.确定当前bid的状态 */
		if(Constants.BID_FUNDRAISE != this.status){
			error.msg = "非法审核!";
			
			return;
		}
		
		if(Constants.BID_FLOW == this.status){
			error.msg = "审核失败,请确定当前标是否已经被审核为“流标”!";
			
			return;
		}
		
		if(Constants.IPS_ENABLE) {
			error.code = 0;
			Cache.set("bid_"+IPSConstants.BID_FUNDRAISE+"_"+this.bidNo, this, IPSConstants.CACHE_TIME);
			boolean flag = Payment.flow(IPSConstants.BID_FUNDRAISE, this, this.userId, error);
			
			if(!flag) {
				error.code = -1;
				return ;
			}
		}
		
		error.code = 0;
		fundraiseToFlowBC(error);
	}	
	
	public void fundraiseToFlowBC(ErrorInfo error) {
		error.clear();
		int row = 0;
		
		/* 2.修改状态 */
		String hql = "update t_bids set status=? where id=? and user_id = ? and status =?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1,  Constants.BID_FLOW); // 审核状态为流标
		query.setParameter(2, this._id); // 标ID
		query.setParameter(3, this._userId); // 用户ID
		query.setParameter(4, Constants.BID_FUNDRAISE); // 当前状态必须为筹款中
		
		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			Logger.error("标->筹款中->流标:" + e.getMessage());
			JPA.setRollbackOnly();
			
			error.code = -1;
			error.msg = "流标，修改标的状态，数据库异常";

			return;
		}
		
		if(row < 1){
			error.code = -2;
			error.msg = "修改标的状态失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		this.status = Constants.BID_FLOW;
		
		/* 3.返还借款人冻结保证金 */
		this.relieveUserBailFund("筹款中->流标", error);
		
		if(error.code < 1){
			Logger.info("标->筹款中->流标，返还借款人冻结保证金失败，事务回滚");
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 4.返还投资人投资金额 */
		this.returnInvestUserFund("筹款中->流标", error); 
		
		if(error.code < 1){
			Logger.info("标->筹款中->流标，返还投资人投资金额失败，事务回滚");
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 5.添加事件 */
		DealDetail.userEvent(this.userId, UserEvent.FLOW_BID, "审核标：筹款中->流标", error);

		if(error.code < 0){
			error.msg = "添加事件失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		if(Constants.IPS_ENABLE) {
			deleteIPSBid(IPSConstants.BID_FUNDRAISE);
			refundInvestBonus(error);
			
			if (error.code < 0) {
				Logger.info("标->筹款中->流标，退回投标奖励失败，事务回滚");
				JPA.setRollbackOnly();
				
				return;
			}
		}

		error.code = 1;
		error.msg = "审核成功,已将标置为[流标]!";
	}
	
//	/**
//	 * 构造资金托管需要的t_bids，同时加入缓存
//	 * @param operation
//	 * @return
//	 */
//	public t_bids getIPSBid(String operation) {
//		t_bids tbid =  new t_bids();
//		
//		tbid.bid_no = this.bidNo;
//		tbid.mer_bill_no = this.merBillNo;
//		tbid.amount = this.amount;
//		tbid.bail = this.bail;
//		tbid.apr = this.apr;
//		tbid.period_unit = this.periodUnit;
//		tbid.period = this.period;
//		tbid.loan_purpose_id = this.purpose.id;
//		tbid.repayment_type_id = this.repayment.id;
//		tbid.service_fees = this.serviceFees;
//		
//		Cache.set("bid_"+operation+"_"+this.bidNo, this, IPSConstants.CACHE_TIME);
//		
//		return tbid;
//	}
	
	public void deleteIPSBid(String operation) {
		Cache.delete("bid_"+operation+"_"+this.bidNo);
	}
	
	
	/**
	 * 审核中->撤销
	 */
	public void auditToRepeal(ErrorInfo error){
		error.code = -1;
		
		/* 1.确定当前bid的状态 */
		if(Constants.BID_AUDIT != this.status){
			error.msg = "非法审核!";
			
			return ;
		}
		
		if(Constants.BID_REPEAL == this.status){
			error.msg = "审核失败,请确定当前标是否已经被审核为“流标”!";
			
			return ;
		}
		
		if(Constants.IPS_ENABLE) {
			error.code = 0;
			
			IpsDetail.setIpsInfo(Long.parseLong(this.bidNo), "{\"bidId\":\""+this.id+"\"}", error);
//			Cache.set("bid_"+IPSConstants.BID_CANCEL+"_"+this.bidNo, this, IPSConstants.CACHE_TIME);
			
			return ;
		}
		
		error.code = 0;
		auditToRepealBC(error);
	}
	
	public void auditToRepealBC(ErrorInfo error) {
		error.clear();
		int row = 0;
		
		/* 2.修改状态 */
		String hql = "update t_bids set status=? where id=? and user_id = ? and status =?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, Constants.BID_REPEAL); // 审核状态为撤销
		query.setParameter(2, this.id); // 标ID
		query.setParameter(3, this.userId); // 用户ID
		query.setParameter(4, Constants.BID_AUDIT); // 当前状态必须为审核中
		
		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标->审核中->撤销:" + e.getMessage());
			error.msg = "审核失败!";
			JPA.setRollbackOnly();

			return ;
		}
		
		if(row < 1){
			error.msg = "审核失败!";
			JPA.setRollbackOnly();
			
			return ;
		}
		
		this.status = Constants.BID_REPEAL;
		
		/* 3.返还借款人冻结保证金 */
		this.relieveUserBailFund("审核中->撤销", error);
		
		if(error.code < 1){
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 4.添加事件 */
		DealDetail.userEvent(this.userId, UserEvent.REPEAL_BID, "审核标：审核中->撤销", error);

		if(error.code < 0){
			error.msg = "添加事件失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		if(Constants.IPS_ENABLE) {
			deleteIPSBid("cancel");
			refundInvestBonus(error);
			
			if (error.code < 0) {
				JPA.setRollbackOnly();
				
				return;
			}
		}

		error.code = 1;
		error.msg = "已将标置为[撤销]!";
	} 
	
	/**
	 * 提前借款->撤销
	 */
	public void advanceLoanToRepeal(ErrorInfo error){
		error.code = -1;
		
		/* 1.确定当前bid的状态 */
		if(Constants.BID_ADVANCE_LOAN != this.status){
			error.msg = "非法审核!";
			
			return;
		}
		
		if(Constants.BID_REPEAL == this.status){
			error.msg = "审核失败,请确定当前标是否已经被审核为“流标”!";
			
			return;
		}
		
		if(Constants.IPS_ENABLE) {
			error.code = 0;
			
			IpsDetail.setIpsInfo(Long.parseLong(this.bidNo), "{\"bidId\":\""+this.id+"\"}", error);
//			Cache.set("bid_"+IPSConstants.BID_CANCEL_F+"_"+this.bidNo, this, IPSConstants.CACHE_TIME);
			
			return ;
		}
		
		error.code = 0;
		advanceLoanToRepealBC(error);
	}	
	
	public void advanceLoanToRepealBC(ErrorInfo error) {
		error.clear();
		int row = 0;
		
		/* 2.修改状态 */
		String hql = "update t_bids set status=? where id=? and user_id = ? and status =?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, Constants.BID_REPEAL); // 审核状态为撤销
		query.setParameter(2, this.id); // 标ID
		query.setParameter(3, this.userId); // 用户ID
		query.setParameter(4, Constants.BID_ADVANCE_LOAN); // 当前状态必须为提前借款
		
		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标->提前借款->撤销:" + e.getMessage());
			error.msg = "审核失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		if(row < 1){
			error.msg = "审核失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		this.status = Constants.BID_REPEAL;
		
		/* 3.返还借款人冻结保证金 */
		this.relieveUserBailFund("提前借款->撤销", error);
		
		if(error.code < 1){
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 4.返还投资人投资金额 */
		this.returnInvestUserFund("提前借款->撤销", error); 
		
		if(error.code < 1){
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 5.添加事件 */
		DealDetail.userEvent(this.userId, UserEvent.REPEAL_BID, "审核标：提前借款->撤销", error);

		if(error.code < 0){
			error.msg = "添加事件失败!";
			JPA.setRollbackOnly();
			
			return;
		}

		if(Constants.IPS_ENABLE) {
			deleteIPSBid(IPSConstants.BID_CANCEL_F);
			refundInvestBonus(error);
			
			if (error.code < 0) {
				JPA.setRollbackOnly();
				
				return;
			}
		}
		
		error.code = 1;
		error.msg = "已将标置为[撤销]!";
	} 

	/**
	 * 筹款中->撤销
	 */
	public void fundraiseToRepeal(ErrorInfo error){
		error.code = -1;
		
		/* 1.确定当前bid的状态 */
		if(Constants.BID_FUNDRAISE != this.status){
			error.msg = "非法审核!";
			
			return;
		}
		
		if(Constants.BID_REPEAL == this.status){
			error.msg = "审核失败,请确定当前标是否已经被审核为“流标”!";
			
			return;
		}
		
		if(Constants.IPS_ENABLE) {
			error.code = 0;
			IpsDetail.setIpsInfo(Long.parseLong(this.bidNo), "{\"bidId\":\""+this.id+"\"}", error);
//			Cache.set("bid_"+IPSConstants.BID_CANCEL_N+"_"+this.bidNo, this, IPSConstants.CACHE_TIME);
			
			return ;
		}
		
		error.code = 0;
		fundraiseToRepealBC(error);
	}	
		
	public void fundraiseToRepealBC(ErrorInfo error) {
		error.clear();
		int row = 0;
		
		/* 2.修改状态 */
		String hql = "update t_bids set status=? where id=? and user_id = ? and status =?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, Constants.BID_REPEAL); // 审核状态为撤销
		query.setParameter(2, this.id); // 标ID
		query.setParameter(3, this.userId); // 用户ID
		query.setParameter(4, Constants.BID_FUNDRAISE); // 当前状态必须为筹款中
		
		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标->筹款中->撤销:" + e.getMessage());
			error.msg = "审核失败!";
			JPA.setRollbackOnly();

			return;
		}
		
		if(row < 1){
			error.msg = "审核失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		this.status = Constants.BID_REPEAL;
		
		/* 3.返还借款人冻结保证金 */
		this.relieveUserBailFund("筹款中->撤销", error);
		
		if(error.code < 1){
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 4.返还投资人投资金额 */
		this.returnInvestUserFund("筹款中->撤销", error); 
		
		if(error.code < 1){
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 5.添加事件 */
		DealDetail.userEvent(this.userId, UserEvent.REPEAL_BID, "审核标：筹款中->撤销", error);

		if(error.code < 0){
			error.msg = "添加事件失败!";
			JPA.setRollbackOnly();
			
			return;
		}

		if(Constants.IPS_ENABLE) {
			deleteIPSBid(IPSConstants.BID_CANCEL_N);
			refundInvestBonus(error);
			
			if (error.code < 0) {
				JPA.setRollbackOnly();
				
				return;
			}
		}
		
		error.code = 1;
		error.msg = "已将标置为[撤销]!";
	} 
	
	/**
	 * 资料针对产品通过比例
	 */
	private static int partAuditPassOperate(long userId, String mark) {
		List<ProductAuditItem> pitems = ProductAuditItem.queryAuditByProductMark(mark, false, true);	//从备份表中得到标实际需要通过资料的ID
		List<Long> uitems = UserAuditItem.queryUserAuditItem(userId, true); // 得到用户已上传的可用资料ID集合
		List<Long> itemId = UserAuditItem.getNotUploadItemId(pitems, uitems); // 调用对比的方法
		
		if(null == itemId)
			return 999;
		
		return itemId.size();
	}
	
	/**
	 * 返还投资用户投资金额
	 */
	private void returnInvestUserFund(String summary, ErrorInfo error) {
		error.code = -1;
		int row = 0;
		
		List<Invest> invests = Invest.queryAllInvestUser(this.id); // 查询用户针对这个标所投的金额
		
		if(null == invests)
			return;
		
		if(invests.size() == 0){
			error.code = 1;
			
			return;
		}
			
		DealDetail dealDetail = null;
		Map<String, Double> detail = null;
		DataSafety dataSafety = new DataSafety();//数据防篡改
		User user = new User();
		boolean falg = false;
		
		for (Invest invest : invests) {
			dataSafety.setId(invest.investUserId);//更新数据防篡改字段
			falg = dataSafety.signCheck(error);
			
			row = DealDetail.relieveFreezeFund(invest.investUserId, invest.investAmount); // 返还投标用户冻结资金
			
			if(row < 1) 
				return;
				
			detail = DealDetail.queryUserFund(invest.investUserId, error);
			double user_amount = detail.get("user_amount");
			double freeze = detail.get("freeze");
			double receive_amount = detail.get("receive_amount");
			
			/* 用户的金额是不能小于0的!  */
			if(user_amount < 0 || freeze < 0){
				error.msg = "投资用户资金有误!";
				
				return;
			}
			
			/* 添加交易记录 */
			dealDetail = new DealDetail(invest.investUserId,
					DealType.THAW_FREEZE_INVESTAMOUNT, invest.investAmount,
					this.id, user_amount + invest.investAmount, freeze, receive_amount,
					summary + "解冻投资金额" + invest.investAmount + "元");

			dealDetail.addDealDetail(error);
			
			if (error.code < 0)  
				return;
			
			/* 伪构记录 */
			dealDetail = new DealDetail(invest.investUserId,
					DealType.REVENUE_FREEZE_FUND, invest.investAmount,
					this.id, user_amount, freeze, receive_amount,
					summary + "返还投资金额");

			dealDetail.addDealDetail(error);
			
			if (error.code < 0)  
				return;
			
			/* 被动修改状态：只有在签名正确的情况下才能继续更新签名 */
			if(falg) {
				dataSafety.setId(invest.investUserId);//更新数据防篡改字段
				dataSafety.updateSign(error);
				
				if (error.code < 0) {
					error.msg = "更新数据防篡改字段系统异常!";

					return;
				}
			}
			
			/* 通知 */
			String content = null;
			TemplateStation station = new TemplateStation();
			TemplateEmail email = new TemplateEmail();
			TemplateSms sms = new TemplateSms();
			email.setId(Templets.E_TENDER_OVER);
			station.setId(Templets.M_TENDER_OVER);
			sms.setId(Templets.S_TENDER_OVER);
			
			user.createBid = true;
			user.id = invest.investUserId;
			
			String userName = User.queryUserNameById(invest.investUserId, error);
			
			if(station.status){
				content = station.content;
				content = content.replace("userName", userName == null ? "" : userName);
				content = content.replace("date", DateUtil.dateToString((new Date())));
				content = content.replace("title", this.title);
				content = content.replace("fee", invest.investAmount + "");

				station.addMessageTask(invest.investUserId, station.title, content);
			}
			
			if(email.status){
				content = email.content;
				content = content.replace("userName", userName == null ? "" : userName);
				content = content.replace("date", DateUtil.dateToString((new Date())));
				content = content.replace("title", this.title);
				content = content.replace("fee", invest.investAmount + "");
				
				email.addEmailTask(user.email, email.title, content);
			}
			
			if(sms.status){
				content = sms.content;
				content = content.replace("userName", userName == null ? "" : userName);
				content = content.replace("date", DateUtil.dateToString((new Date())));
				content = content.replace("title", this.title);
				content = content.replace("fee", invest.investAmount + "");
				
				sms.addSmsTask(user.mobile, content);
			}
		}
		
		error.code = 1;
	}
	
	/**
	 * 返还借款人保证金
	 */
	public void relieveUserBailFund(String summary, ErrorInfo error){
		error.code = -1;
		int row = 0;
		
		DataSafety dataSafety = new DataSafety();//数据防篡改
		dataSafety.setId(this.userId);//更新数据防篡改字段
		boolean falg = dataSafety.signCheck(error);
		
		/* 返回用户资金 */
		row = DealDetail.relieveFreezeFund(this.userId, this.bail); // 用户对应冻结的资金(保证金)

		if (row < 1) {
			error.msg = "返还保证金有误!";

			return;
		}

		Map<String, Double> detail = DealDetail.queryUserFund(this.userId, error);
		double user_amount = detail.get("user_amount");
		double freeze = detail.get("freeze");
		double receive_amount = detail.get("receive_amount");
		String summary2 = null;
		
		/* 用户的金额是不能小于0的!  */
		if(user_amount < 0 || freeze < 0){
			error.msg = "借款人资金有误!";
			
			return;
		}
		
		if(this.isSecBid)
			summary2 = " 解冻秒还借款冻结保证金";
		else
			summary2 = " 解冻非秒还借款冻结保证金";
		
		/* 添加交易记录 */
		DealDetail dealDetail = new DealDetail(this.userId,
				DealType.RELIEVE_FREEZE_FUND, this.bail, this.id, user_amount - this.bail,
				freeze, receive_amount, summary + summary2 + this.bail + "元");

		dealDetail.addDealDetail(error);
		
		if (error.code < 0) {
			error.msg = "返还保证金，添加交易记录失败!";

			return;
		}
		
		/* 伪构记录 */
		dealDetail = new DealDetail(this.userId, DealType.REVENUE_FREEZE_FUND,
				this.bail, this.id, user_amount, freeze,
				receive_amount, summary + " 返还冻结保证金");

		dealDetail.addDealDetail(error);
		
		if (error.code < 0) {
			error.msg = "返还保证金，添加交易记录失败!";

			return;
		}
		
		/* 被动修改状态：只有在签名正确的情况下才能继续更新签名 */
		if(falg) {
			dataSafety.setId(this.userId);//更新数据防篡改字段
			dataSafety.updateSign(error);

			if (error.code < 0) {
				error.msg = "更新数据防篡改字段系统异常!";

				return;
			}
		}
		
		/* 通知 */
		String content = null;
		TemplateStation station = new TemplateStation();
		TemplateEmail email = new TemplateEmail();
		TemplateSms sms = new TemplateSms();
		email.setId(Templets.E_FIRST_FAIL);
		station.setId(Templets.M_FIRST_FAIL);
		sms.setId(Templets.S_FIRST_FAIL);
		
		if(station.status){
			content = station.content; 
			content = content.replace("userName", this.user.name == null ? "" : this.user.name); 
			content = content.replace("date", DateUtil.dateToString((new Date()))); 
			content = content.replace("title", this.title); 
			content = content.replace("fee", this.bail + ""); 
			content = content.replace("status", this.strStatus); 
			station.addMessageTask(this.userId, station.title, content);
		}
		
		if(email.status){
			content = email.content; 
			content = content.replace("userName", this.user.name == null ? "" : this.user.name); 
			content = content.replace("date", DateUtil.dateToString((new Date()))); 
			content = content.replace("title", this.title); 
			content = content.replace("fee", this.bail + ""); 
			content = content.replace("status", this.strStatus); 
			email.addEmailTask(this.user.email, email.title, content);
		}
		
		if(sms.status){
			content = sms.content; 
			content = content.replace("userName", this.user.name == null ? "" : this.user.name); 
			content = content.replace("date", DateUtil.dateToString((new Date()))); 
			content = content.replace("title", this.title); 
			content = content.replace("fee", this.bail + ""); 
			content = content.replace("status", this.strStatus); 
			sms.addSmsTask(this.user.mobile, content);
		}
		
		error.code = 1;
	}
	
	/**
	 * 修改满标期限
	 */
	private static int addInvestExpireTime(long bid, int investPeriod) {
		String hql = "update t_bids set invest_expire_time = ? where id = ?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, DateUtil.dateAddDay(new Date(), investPeriod));
		query.setParameter(2, bid);

		try {
			return query.executeUpdate();
		} catch (Exception e) {
			Logger.error("标->修改满标期限:" + e.getMessage());

			return -1;
		}
	}
	
	/**--------------------------------------------------关联管理员-----------------------------------------------------------------*/
	
	/**
	 * 分配某个标给某个管理员
	 * @param supervisorId 管理员ID
	 * @param biddId 标ID
	 * @param error 信息值
	 * @return -1:失败; >0:成功;
	 */
	public static void assignBidToSupervisor(long supervisorId, String typeStr, long bidId, ErrorInfo error) {
		error.clear();
 		
 		if(!NumberUtil.isNumericInt(typeStr)) {
 			error.code = -1;
 			error.msg = "传入的类型参数有误";
 			
 			return;
 		}
 		
 		error.code = Integer.parseInt(typeStr);
 		
 		if(error.code != 1 && error.code !=2) {
 			error.code = -1;
 			error.msg = "传入的类型参数有误";
 			
 			return;
 		}
 		
		String hql = "update t_bids set manage_supervisor_id = ? where id = ?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, supervisorId);
		query.setParameter(2, bidId);

		try {
			error.code = query.executeUpdate();
		} catch (Exception e) {
			Logger.error("标->分配某个标给某个管理员:" + e.getMessage());
			error.msg = "分配失败!";

			return;
		}
		
		if(error.code < 1){
			error.msg = "分配失败!";
			
			return;
		}
		
		/* 添加事件 */
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ASSIGN_BID, "分配某个标给某个管理员", error);
		
		if(error.code < 0){
			error.msg = "分配失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		error.code = 0;
		error.msg = "分配成功";
	}
	
	/**
	 * 查询借款会员列表借款标详情	
	 * @param userId
	 * @param type
	 * @param supervisor_id
	 * @param keywords
	 * @param status
	 * @param pageNum
	 * @param pageSize
	 * @param error
	 * @return
	 */
	public static PageBean<t_bids> queryUserInfoBillDetail(int pageNum,int pageSize,long userId,int type,long supervisor_id,String keywords,String status,ErrorInfo error){
		String no = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, error);
		
		StringBuffer conditions = new StringBuffer(" ");
 		List<Object> values = new ArrayList<Object>();
		
 		if(type == 1){
 			conditions.append("select id from t_bids where user_id = ?");
 			values.add(userId);
 		} else if(type == 2){
 			conditions.append("select id from t_bids where user_id = ? and manage_supervisor_id = ? ");
 			values.add(userId);
 			values.add(supervisor_id);
 		} else {
 			conditions.append("select id from t_bids where 1 = 1 ");
 		}
 		
 		if(StringUtils.isNotBlank(keywords)){
 			conditions.append(" and title like ? ");
 			values.add("%"+keywords+"%");
 		}
 		
 		if(NumberUtil.isNumericInt(status)){
 			conditions.append(" and status = ?");
 			values.add(Integer.parseInt(status));
 		}
 		PageBean<t_bids> page = new PageBean<t_bids>();
 		page.currPage = pageNum;
		page.pageSize = pageSize;
		page.totalCount = t_bids.find(conditions.toString(), values.toArray()).fetch().size();
		List<Long> ids = new ArrayList<Long>();
		List<t_bids> bidList = new ArrayList<t_bids>();
 		
		try {
			
			ids = t_bids.find(conditions.toString(), values.toArray()).fetch(page.currPage,page.pageSize);
		} catch (Exception e) {
			e.printStackTrace();
			
			return page;
		}
		for(long id : ids){
			t_bids bids = t_bids.findById(id);
			t_bids bid = new t_bids(id,no+bids.id, bids.title,bids.amount, bids.status);
					
			bidList.add(bid);
		}
 		
 		page.page = bidList;
 		
		return page;
		
	}
	
	/**
	 * 查询部门账单账单借款会员列表借款标详情	
	 * @param userId
	 * @param type
	 * @param supervisor_id
	 * @param keywords
	 * @param status
	 * @param pageNum
	 * @param pageSize
	 * @param error
	 * @return
	 */
	public static PageBean<t_bids> queryDeptUserInfoBillDetail(int pageNum,
			int pageSize, long userId, String keywords, String status,
			ErrorInfo error) {
		
		String no = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, error);
		
		StringBuffer conditions = new StringBuffer(" ");
		List<Object> values = new ArrayList<Object>();

		conditions.append("select id from t_bids where user_id = ?");
	    values.add(userId);
		
		
		if(StringUtils.isNotBlank(keywords)){
			conditions.append(" and CONCAT("+"'"+no+"'" +",id) like ? or title like ? ");
			values.add("%"+keywords+"%");
			values.add("%"+keywords+"%");
		}
		
		if(NumberUtil.isNumericInt(status)){
			conditions.append(" and status = ?");
			values.add(Integer.parseInt(status));
		}
		
		PageBean<t_bids> page = new PageBean<t_bids>();
		page.currPage = pageNum;
		page.pageSize = pageSize;
		page.totalCount = t_bids.find(conditions.toString(), values.toArray()).fetch().size();
		List<Long> ids = new ArrayList<Long>();
		List<t_bids> bidList = new ArrayList<t_bids>();
			
		try {
			ids = t_bids.find(conditions.toString(), values.toArray()).fetch(page.currPage,page.pageSize);
		} catch (Exception e) {
			e.printStackTrace();
			
			return page;
		}
		
		for(long id : ids){
			t_bids bids = t_bids.findById(id);
			t_bids bid = new t_bids(id,no+bids.id, bids.title,bids.amount, bids.status);
					
			bidList.add(bid);
		}
			
		page.page = bidList;
			
		return page;
		
	  }
	
	/**--------------------------------------------------收藏标-----------------------------------------------------------------*/
	
	/**
	 * 收藏某个标
	 * @param userId 用户ID
	 * @param bidId 标ID
	 * @param error 错误信息
	 */
	public static long collectBid(long userId, long bidId, ErrorInfo error) {
		error.clear();
		 
		error.code = isCollect(userId, bidId);
		
		/* 表示已经收藏,或异常 */
		if (error.code == Constants.COLLECT) {
			error.code = -1;
			error.msg = "标已收藏!";
			
			return -1; 
		}
		
		long bidUserId = 0l;
		
		try {
			bidUserId = t_bids.find("select user_id from t_bids where id = ?", bidId).first();
		} catch (Exception e) {
			Logger.error("标->收藏某个标:" + e.getMessage());
			error.code = -2;
			error.msg = "收藏失败!";
			
			return -1; 
		}
		
		if(bidUserId == userId){
			error.code = -3;
			error.msg = "您不能收藏自己发布的借款标!";
			
			return -1;
		}
		
		t_user_attention_bids bid = new t_user_attention_bids();

		bid.time = new Date();
		bid.user_id = userId;
		bid.bid_id = bidId;

		try {
			bid.save();
		} catch (Exception e) {
			Logger.error("标->收藏某个标:" + e.getMessage());
			error.code = -4;
			error.msg = "收藏失败!";
			
			return -1; 
		}
		
		if(bid.id < 1){
			error.code = -5;
			error.msg = "收藏失败!";
			
			return -1;
		}
		
		/* 添加事件 */
		DealDetail.userEvent(userId, UserEvent.COLLECT_BID, "收藏标", error);
		
		if(error.code < 0){
			error.msg = "收藏失败!";
			JPA.setRollbackOnly();
			
			return -1;
		}
		
		error.msg = "收藏成功!";
		
		return bid.id;
	}

	/**
	 * 查询这个标是否已经被收藏
	 * @param userId 用户ID
	 * @param bidId 标ID
	 * @param error 错误信息
	 * @return 1:已收藏  0:未收藏
	 */
	private static int isCollect(long userId, long bidId) {
		String hql = "select id from t_user_attention_bids where user_id=? and bid_id=?";
		Long id = null;
		
		try {
			id = t_user_attention_bids.find(hql, userId, bidId).first();
		} catch (Exception e) {
			Logger.error("标->查询这个标是否已经被收藏:"+ e.getMessage());

			return Constants.COLLECT; // 表示已收藏
		}
		
		if (id == null || id == 0) return Constants.NOT_COLLECT; // 表示未收藏

		return Constants.COLLECT; // 表示已收藏
	}
	
	/**
	 * 判断标是否收藏，并返回收藏id
	 * @param userId
	 * @param bidId
	 * @return
	 */
	public static long isAttentionBid(long userId, long bidId) {
		String hql = "select id from t_user_attention_bids where user_id=? and bid_id=?";
		Long id = null;
		
		try {
			id = t_user_attention_bids.find(hql, userId, bidId).first();
		} catch (Exception e) {
			Logger.error("标->查询这个标是否已经被收藏:"+ e.getMessage());

			return Constants.COLLECT; // 表示已收藏
		}
		
		if (id == null || id == 0) return Constants.NOT_COLLECT; // 表示未收藏

		return id; // 表示已收藏
	}

	/**--------------------------------------------------借款常量-----------------------------------------------------------------*/
	
	/**
	 * 查询审核机制
	 */
	public static List<t_system_options> getAuditMechanism(ErrorInfo error) {
		error.clear();

		try {
			return t_system_options.find(" _key in(?, ?, ?)", 
					OptionKeys.AUDIT_MECHANISM, 
					OptionKeys.AUDIT_MODE,
					OptionKeys.AUDIT_SCALE).fetch();
		} catch (Exception e) {
			Logger.error("标->获取所有编号字母:" + e.getMessage());
			error.msg = "获取所有编号字母失败!";
			
			return null;
		}
	}
	
	/**
	 * 设置审核机制
	 * @param auditMechanism 机制值
	 * @param auditItem 边发边审中的资料项
	 * @param passRate 资料比例值
	 */
	public static void setAuditMechanism(String auditMechanism, String auditItem, String passRate, ErrorInfo error){
		error.clear();

		if (StringUtils.isNotBlank(auditMechanism))
			OptionKeys.siteValue(OptionKeys.AUDIT_MECHANISM, auditMechanism, error);

		if (StringUtils.isNotBlank(auditItem))
			OptionKeys.siteValue(OptionKeys.AUDIT_MODE, auditItem, error);

		if (StringUtils.isNotBlank(passRate))
			OptionKeys.siteValue(OptionKeys.AUDIT_SCALE, passRate, error);
		
		if(error.code < 1)
			return;
		
		/* 添加事件 */
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.SET_AUDIT_MECHANISM, "设置审核机制", error);
		
		if(error.code < 0){
			error.msg = "设置失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		error.msg = "设置成功!";
	}
	
	/**
	 * 查询所有编号字母
	 */
	public static List<t_system_options> getNumberList(ErrorInfo error){
		error.clear();

		try {
			return t_system_options.find(" _key in(?, ?, ?, ?, ?, ?)", 
					OptionKeys.LOAN_NUMBER,
					OptionKeys.LOAN_BILL_NUMBER, 
					OptionKeys.INVEST_BILL_NUMBER,
					OptionKeys.AGENCIES_NUMBER, 
					OptionKeys.TRANFER_NUMBER,
					OptionKeys.AUDIT_ITEM_NUMBER).fetch();
		} catch (Exception e) {
			Logger.error("标->查询所有编号字母:" + e.getMessage());
			error.msg = "查询所有编号字母失败!";

			return null;
		}
	}
	
	/**
	 * 设置编号字母
	 * @param key
	 * @param value
	 * @param error
	 */
	public static void setNumber(String key, String value, ErrorInfo error){
		OptionKeys.siteValue(key, value, error);
		
		if(error.code < 1)
			return;
		
		/* 添加事件 */
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.SET_NUMBER, "设置编号字母", error);
		
		if(error.code < 0){
			error.msg = "设置失败!";
			JPA.setRollbackOnly();
			
			return;
		}
	}
	
	/**--------------------------------------------------视图查询-----------------------------------------------------------------*/
	
	/**
	 * 构建搜索条件
	 * @param str ...可变参数条件(请遵循：用户ID, 条件, 关键词, 开始时间, 结束时间, 排序数组索引, 排序升降序说明)
	 * @return String
	 */
	public static void getCondition(PageBean pageBean, StringBuffer conditions, List<Object> values, String time, String ... str) {
		if(str == null || str.length == 0)
			return;
		
		Map<String, Object> conditionmap = new HashMap<String, Object>(); 
		
		/* 用户ID */
		if (str.length > 0 && NumberUtil.isNumericInt(str[0])) {
			conditions.append(" AND user_id = ?");
			values.add(Long.parseLong(str[0]));
		}
		
		/* 条件 */
		if (str.length > 1 && NumberUtil.isNumericInt(str[1])) {
			int c = Integer.parseInt(str[1]);
			
			conditions.append(Constants.BID_SEARCH[c]);
			
			if(0 == c) {
				values.add("%" + str[2] + "%");
				values.add("%" + str[2] + "%");
				values.add("%" + str[2] + "%");
				values.add("%" + str[2] + "%");
			}else {
				values.add("%" + str[2] + "%");
			}
			
			conditionmap.put("condition", str[1]);
			conditionmap.put("keyword", str[2]);
		}
		
		/* 借款标管理(或单限制于标题/编号搜索查询) */
		if(str.length > 3) {
			/* 开始时间 */
			if (str.length > 3 && StringUtils.isNotBlank(str[3])) {
				conditions.append(" AND ");
				conditions.append(time);
				conditions.append(" >= ?");
				values.add(DateUtil.strDateToStartDate(str[3]));
				conditionmap.put("startDate", str[3]);
			}
			
			/* 结束时间 */
			if (str.length > 4 && StringUtils.isNotBlank(str[4])) {
				conditions.append(" AND ");
				conditions.append(time);
				conditions.append(" <= ?");
				values.add(DateUtil.strDateToEndDate(str[4]));
				conditionmap.put("endDate", str[4]);
			}
			
			/* 排序 */
			if (str.length > 5 && NumberUtil.isNumericInt(str[5])) {
				int _order = Integer.parseInt(str[5]);
				conditions.append(Constants.BID_SEARCH_ORDER[_order]);
				
				conditionmap.put("orderIndex", str[5]);
				
				/* 升降序 */
				if(str.length > 6 && NumberUtil.isNumericInt(str[6]) && _order > 0){
					
					if(Integer.parseInt(str[6]) == 1)
						conditions.append("ASC");
					else
						conditions.append("DESC");
					
					/* 保存当前索引值 + 升降值 */
					conditionmap.put("orderStatus", str[6]);
				}
			}
		}
		
		 /* 第一次让它为ID降序搜索 */ 
		if(StringUtils.isBlank(str[5])) 
			conditions.append(Constants.BID_SEARCH_ORDER[0]); 

		/* 保存搜索条件集合 */
		pageBean.conditions = conditionmap;
	}
	
	/**
	 * 待验证的借款标 
	 */
	public static List<v_bid_wait_verify> queryBidWaitVerify(PageBean<v_bid_wait_verify> pageBean, ErrorInfo error, String ... str) {
		error.clear();
		
		int count = -1;
		StringBuffer conditions = new StringBuffer(" where 1 = 1"); 
		List<Object> values = new ArrayList<Object>(); 
		
		/* 组装条件 */
		Bid.getCondition(pageBean, conditions, values, "time", str);
		
		StringBuffer sql = new StringBuffer();
		sql.append("select count(t.id) from (");
		sql.append(SQLTempletes.V_BID_WAIT_VERIFY);
		sql.append(")");
		sql.append(SQLTempletes.TABLE_NAME);
		sql.append(conditions);
		
		EntityManager em = JPA.em();
        Query query = em.createNativeQuery(sql.toString());

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }

        List<?> list = null;
        
        try {
        	list = query.getResultList();
		} catch (Exception e) {
			Logger.error("标->审核中的标列表,查询总记录数:" + e.getMessage());
			error.msg = "加载筹款中的借款标列表失败!";
			
			return null;
		}
		
		count = list == null ? 0 : Integer.parseInt(list.get(0).toString());
		
		if(count < 1)
			return new ArrayList<v_bid_wait_verify>();
			
		pageBean.totalCount = count;
		
		sql = new StringBuffer();
		sql.append("select t.* from (");
		sql.append(SQLTempletes.V_BID_WAIT_VERIFY);
		sql.append(")");
		sql.append(SQLTempletes.TABLE_NAME);
		sql.append(conditions);

		query = em.createNativeQuery(sql.toString(), v_bid_wait_verify.class);

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }
	        
        query.setFirstResult((pageBean.currPage - 1) * pageBean.pageSize);
        query.setMaxResults(pageBean.pageSize);
        
		try {
			return query.getResultList();
		} catch (Exception e) {
			Logger.error("标->审核中的标列表,查询列表:" + e.getMessage());
			error.msg = "加载审核中的借款标列表失败!";
			
			return null;
		}
	}
	
	/**
	 * 审核中的标列表
	 * @param pageBean 分页对象
	 * @param error 信息值
	 * @param uid >0:根据用户ID查询,<0:查询所有
	 * @param str 可变参数值
	 * @return List<v_bid_auditing>
	 */
	public static List<v_bid_auditing> queryBidAuditing(PageBean<v_bid_auditing> pageBean, ErrorInfo error, String ... str) {
		error.clear();

		int count = -1;
		StringBuffer conditions = new StringBuffer(" where 1 = 1"); 
		List<Object> values = new ArrayList<Object>(); 
		
		/* 组装条件 */
		Bid.getCondition(pageBean, conditions, values, "time", str);
		
		StringBuffer sql = new StringBuffer();
		sql.append("select count(t.id) from (");
		sql.append(SQLTempletes.V_BID_AUDITING);
		if(Constants.IS_BIDS_NEED_FILTER){
			sql.append(" and b.time > '"+Constants.BIDS_CREATETIME +"' and u.name = '" + Constants.BIDS_MOBILE +"' ");
		}
		sql.append(")");
		sql.append(SQLTempletes.TABLE_NAME);
		sql.append(conditions);
		
		EntityManager em = JPA.em();
        Query query = em.createNativeQuery(sql.toString());

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }

        List<?> list = null;
        
        try {
        	list = query.getResultList();
		} catch (Exception e) {
			Logger.error("标->审核中的标列表,查询总记录数:" + e.getMessage());
			error.msg = "加载筹款中的借款标列表失败!";
			
			return null;
		}
		
		count = list == null ? 0 : Integer.parseInt(list.get(0).toString());
		
		if(count < 1)
			return new ArrayList<v_bid_auditing>();
			
		pageBean.totalCount = count;
		
		sql = new StringBuffer();
		sql.append("select t.* from (");
		sql.append(SQLTempletes.V_BID_AUDITING);
		sql.append(")");
		sql.append(SQLTempletes.TABLE_NAME);
		sql.append(conditions);

		query = em.createNativeQuery(sql.toString(), v_bid_auditing.class);

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }
	        
        query.setFirstResult((pageBean.currPage - 1) * pageBean.pageSize);
        query.setMaxResults(pageBean.pageSize);
        
		try {
			return query.getResultList();
		} catch (Exception e) {
			Logger.error("标->审核中的标列表,查询列表:" + e.getMessage());
			error.msg = "加载审核中的借款标列表失败!";
			
			return null;
		}
	}
	
	/**
	 * 筹款中的标列表
	 * @param pageBean 
	 * @param error 信息值
	 * @return List<v_bid_fundraiseing>
	 */
	public static List<v_bid_fundraiseing> queryBidFundraiseing(PageBean<v_bid_fundraiseing> pageBean, int status,  ErrorInfo error, String ... str) {
		error.clear();

		int count = -1;
		StringBuffer sqlCount = new StringBuffer("");
		StringBuffer sql = new StringBuffer("");
		StringBuffer conditions = new StringBuffer("");
		StringBuffer conditionsCount = new StringBuffer("");
		List<Object> values = new ArrayList<Object>();
		List<Object> valuesCount = new ArrayList<Object>();
		
		sql.append("SELECT concat(`e`.`_value`,cast(`b`.`id` AS CHAR charset utf8)) AS `bid_no`,`b`.`id` AS `id`,`b`.`title` AS `title`,`b`.`real_invest_expire_time` AS `real_invest_expire_time`,`b`.`repayment_type_id` AS `repaymentId`,`u`.`name` AS `user_name`,`b`.`amount` AS `amount`,`b`.has_invested_amount AS has_invested_amount,`p`.`small_image_filename` AS `small_image_filename`,`b`.`apr` AS `apr`,`b`.`period_unit` AS `period_unit`,`b`.`period` AS `period`,`b`.`time` AS `time`,`b`.`status` AS `status`,`b`.`loan_schedule` AS `loan_schedule`,c.image_filename AS credit_level_image_filename,`f_user_audit_item` (`u`.`id`, `b`.`mark`, 2) AS `user_item_count_true`,`f_user_audit_item` (`u`.`id`, `b`.`mark`, -(1)) AS `user_item_count_false`,(to_days(`b`.`real_invest_expire_time`) - to_days((CASE WHEN (`b`.`status` = 1) THEN `b`.`time` WHEN (`b`.`status` = 2) THEN `b`.`audit_time` END ))) AS `full_days`,(SELECT count(`pail`.`id`) AS `product_item_count` FROM `t_product_audit_items_log` `pail` WHERE ((`pail`.`mark` = `b`.`mark`) AND (`pail`.`type` = 1))) AS `product_item_count` FROM (((((`t_bids` `b` LEFT JOIN `t_users` `u` ON ((`b`.`user_id` = `u`.`id`))) LEFT JOIN t_credit_levels c ON ((u.credit_level_id = c.id))) LEFT JOIN `t_products` `p` ON ((`b`.`product_id` = `p`.`id`)))JOIN `t_system_options` `e` )) WHERE ((`b`.`status` IN(?, ?)) AND (`e`.`_key` = 'loan_number'))");
		if(Constants.IS_BIDS_NEED_FILTER){
			sql.append(" and b.time > '"+Constants.BIDS_CREATETIME +"' and u.name = '" + Constants.BIDS_MOBILE +"' ");
		}
		sqlCount.append("SELECT COUNT(b.id) FROM ((`t_bids` `b` LEFT JOIN `t_users` `u` ON ((`b`.`user_id` = `u`.`id`))) JOIN `t_system_options` `e`) WHERE (`b`.`status` IN(?, ?)) AND (`e`.`_key` = 'loan_number')");
		if(Constants.IS_BIDS_NEED_FILTER){
			sql.append(" and b.time > '"+Constants.BIDS_CREATETIME +"' and u.name = '" + Constants.BIDS_MOBILE +"' ");
		}
		values.add(Constants.V_FUNDRAISEING);
		values.add(Constants.V_FULL);
		valuesCount.add(Constants.V_FUNDRAISEING);
		valuesCount.add(Constants.V_FULL);
		
		String[] BID_SEARCH = {" AND (u.name LIKE ? OR concat(`e`.`_value`,cast(`b`.`id` as char charset utf8)) LIKE ?)"," AND concat(`e`.`_value`,cast(`b`.`id` as char charset utf8)) LIKE ?"," AND u.name LIKE ?"};
		
		/* 筹款中/满标 */
		if(status == Constants.V_FULL) {
			conditions.append(" and amount = has_invested_amount");
			conditionsCount.append(" and amount = has_invested_amount");
		}
		else if(status == Constants.V_FUNDRAISEING) {
			conditions.append(" and amount > has_invested_amount");
			conditionsCount.append(" and amount > has_invested_amount");
		}
		
		if (str != null && str.length > 0) {
			Map<String, Object> conditionMaps = new HashMap<String, Object>();
			
			/* 用户id */
			if (NumberUtil.isNumericInt(str[0])) {
				conditions.append(" AND u.id = ?");
				conditionsCount.append(" AND u.id = ?");
				values.add(str[0]);
				valuesCount.add(str[0]);
			}
			
			/* 条件  */
			if (str.length > 2 && NumberUtil.isNumericInt(str[1]) && StringUtils.isNotBlank(str[2])) {
				int c = Integer.parseInt(str[1].toString());
				
				conditions.append(BID_SEARCH[c]);
				conditionsCount.append(BID_SEARCH[c]);
				
				if (0 == c) {
					values.add("%" + str[2] + "%");
					values.add("%" + str[2] + "%");
					valuesCount.add("%" + str[2] + "%");
					valuesCount.add("%" + str[2] + "%");
				}else {
					values.add("%" + str[2] + "%");
					valuesCount.add("%" + str[2] + "%");
				}
				
				conditionMaps.put("condition", str[1]);
				conditionMaps.put("keyword", str[2]);
			}
			
			/* 开始时间  */
			if (str.length > 3 && StringUtils.isNotBlank(str[3])) {
				if (status == Constants.V_FULL) {
					conditions.append(" AND b.real_invest_expire_time >= ?");
					conditionsCount.append(" AND b.real_invest_expire_time >= ?");
					values.add(DateUtil.strDateToStartDate(str[3]));
					valuesCount.add(DateUtil.strDateToStartDate(str[3]));
					
				}else if (status == Constants.V_FUNDRAISEING) {
					conditions.append(" AND b.time >= ?");
					conditionsCount.append(" AND b.time >= ?");
					values.add(DateUtil.strDateToStartDate(str[3]));
					valuesCount.add(DateUtil.strDateToStartDate(str[3]));
				}
				conditionMaps.put("startDate", str[3]);
			}
			
			/* 结束时间  */
			if (str.length > 4 && StringUtils.isNotBlank(str[4])) {
				if (status == Constants.V_FULL) {
					conditions.append("AND b.real_invest_expire_time <= ?");
					conditionsCount.append(" AND b.real_invest_expire_time <= ?");
					values.add(DateUtil.strDateToStartDate(str[4]));
					valuesCount.add(DateUtil.strDateToStartDate(str[4]));	
					
				}else if (status == Constants.V_FUNDRAISEING) {
					conditions.append("AND b.time <= ?");
					conditionsCount.append(" AND b.time <= ?");
					values.add(DateUtil.strDateToStartDate(str[4]));
					valuesCount.add(DateUtil.strDateToStartDate(str[4]));
				}
				conditionMaps.put("endDate", str[4]);
			}
			
			/* 排序  */
			if (str.length > 5 && StringUtils.isNotBlank(str[5]) && NumberUtil.isNumericInt(str[5])) {
				int _order = Integer.parseInt(str[5].toString());
				conditions.append(Constants.BID_SEARCH_ORDER[_order]);
				
				conditionMaps.put("orderIndex", _order);
				/* 升降序  */
				if (str.length > 6 && StringUtils.isNotBlank(str[6]) && NumberUtil.isNumericInt(str[6]) && 0 != _order) {
					
					if (Integer.parseInt(str[6]) == 1) {
						conditions.append("DESC");
					}else {
						conditions.append("ASC");
					}
					
					/* 保存当前索引值 + 升降值 */
					conditionMaps.put("orderStatus", str[6]);
				}
			}
			pageBean.conditions = conditionMaps;
		}
		
		sqlCount.append(conditionsCount);
		
		EntityManager em = JPA.em();
        Query query = em.createNativeQuery(sqlCount.toString());

        for(int n = 1; n <= valuesCount.size(); n++){
            query.setParameter(n, valuesCount.get(n-1));
        }

        List<?> list = null;
		
		try {
			list = query.getResultList();
		} catch (Exception e) {
			Logger.error("标->筹款中的标列表,查询总记录数:" + e.getMessage());
			error.msg = "加载筹款中的借款标列表失败!";
			
			return null;
		}
		
		count = list == null ? 0 : Integer.parseInt(list.get(0).toString());
		
		if(count < 1)
			return new ArrayList<v_bid_fundraiseing>();
			
		pageBean.totalCount = count;
		
		sql.append(conditions);
		
		query = em.createNativeQuery(sql.toString(), FundraiseingBid.class);

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }
	        
        query.setFirstResult((pageBean.currPage - 1) * pageBean.pageSize);
        query.setMaxResults(pageBean.pageSize);
		
		try {
			return query.getResultList();
		} catch (Exception e) {
			Logger.error("标->筹款中的标列表:" + e.getMessage());
			error.msg = "加载筹款中的借款标列表失败!";
			
			return null;
		}
	}
	
	/**
	 * 最新筹款中满标倒计时提醒(借款子账户首页)
	 * @param error 信息值
	 * @return List<Bid>
	 */
	public static List<v_bid_fundraiseing> queryFundraiseingBid(long userId, ErrorInfo error){
		try {
			return v_bid_fundraiseing.find("user_id = ? and amount > has_invested_amount order by time desc", userId).fetch(Constants.FULL_REMIND_BID_COUNT);
		} catch (Exception e) {
			Logger.error("标->最新筹款中满标倒计时提醒:" + e.getMessage()); 
			error.msg = "加载筹款中的借款标列表失败!";
			
			return null;
		}
	}
	
	/**
	 * 最新欠缺资料的借款标(借款子账户首页)
	 * @param error 信息值
	 * @return List<Bid>
	 */
	public static List<v_bids> queryToSubmitItemBid(long userId, ErrorInfo error){
		error.clear();

		try {
			return v_bids.find("status in(?, ?, ?) and product_item_count > user_item_count_true and user_id = ? ", 
					Constants.BID_AUDIT, Constants.BID_ADVANCE_LOAN, Constants.BID_FUNDRAISE,userId).fetch();
		} catch (Exception e) {
			Logger.error("标->最新欠缺资料的借款标提醒:" + e.getMessage());
			error.msg = "加载借款标列表失败!";
			
			return null;
		}
	}
	
	/**
	 * 坏账借款标列表
	 * @param pageBean 
	 * @param error 信息值
	 * @return List<v_bid_bad>
	 */
	public static List<v_bid_bad> queryBidBad(PageBean<v_bid_bad> pageBean, long supId, ErrorInfo error, String ... str) {
		error.clear();
		
		int count = -1;
		StringBuffer conditions = new StringBuffer(" where 1 = 1"); 
		List<Object> values = new ArrayList<Object>(); 
		conditions.append(" and manage_supervisor_id = ?");
		
		/* 我/部门 的会员账单 */
		if(supId > 0) values.add(supId);
		else values.add(0);
		
		/* 组装条件 */
		Bid.getCondition(pageBean, conditions, values, "mark_bad_time", str);
		
		StringBuffer sql = new StringBuffer();
		sql.append("select count(t.id) from (");
		sql.append(SQLTempletes.V_BID_BAD);
		sql.append(")");
		sql.append(SQLTempletes.TABLE_NAME);
		sql.append(conditions);
		
		EntityManager em = JPA.em();
        Query query = em.createNativeQuery(sql.toString());

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }

        List<?> list = null;
        
		try {
			list = query.getResultList();
		} catch (Exception e) {
			Logger.error("标->坏账中的标列表,查询总记录数:" + e.getMessage());
			error.msg = "加载审核中的借款标列表失败!";
			
			return null;
		}
		
		count = list == null ? 0 : Integer.parseInt(list.get(0).toString());
		
		if(count < 1)
			return new ArrayList<v_bid_bad>();
			
		pageBean.totalCount = count;
		
		sql = new StringBuffer();
		sql.append("select t.* from (");
		sql.append(SQLTempletes.V_BID_BAD);
		sql.append(")");
		sql.append(SQLTempletes.TABLE_NAME);
		sql.append(conditions);

		query = em.createNativeQuery(sql.toString(), v_bid_bad.class);

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }
	        
        query.setFirstResult((pageBean.currPage - 1) * pageBean.pageSize);
        query.setMaxResults(pageBean.pageSize);
		
		try {
			return query.getResultList();
		} catch (Exception e) {
			Logger.error("标->坏账借款标列表:" + e.getMessage());
			error.msg = "加载坏账借款标列表失败!";
			
			return null;
		}
	}
	
	/**
	 * 逾期的借款标列表
	 * @param pageBean 
	 * @param error 信息值
	 * @return List<v_bid_overdue>
	 */
	public static List<v_bid_overdue> queryBidOverdue(PageBean<v_bid_overdue> pageBean, ErrorInfo error, String ... str) {
		error.clear();
		
		int count = -1;
		StringBuffer conditions = new StringBuffer(" where 1 = 1"); 
		List<Object> values = new ArrayList<Object>(); 
		
		/* 组装条件 */
		Bid.getCondition(pageBean, conditions, values, "audit_time", str);
		
		StringBuffer sql = new StringBuffer();
		sql.append("select count(t.id) from (");
		sql.append(SQLTempletes.V_BID_OVERDUE);
		sql.append(")");
		sql.append(SQLTempletes.TABLE_NAME);
		sql.append(conditions);
		
		EntityManager em = JPA.em();
        Query query = em.createNativeQuery(sql.toString());

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }

        List<?> list = null;
        
		try {
			list = query.getResultList();
		} catch (Exception e) {
			Logger.error("标->逾期的借款标列表,查询总记录数:" + e.getMessage());
			error.msg = "加载逾期的借款标列表失败!";
			
			return null;
		}
		
		count = list == null ? 0 : Integer.parseInt(list.get(0).toString());
		
		if(count < 1)
			return new ArrayList<v_bid_overdue>();
			
		pageBean.totalCount = count;
		
		sql = new StringBuffer();
		sql.append("select t.* from (");
		sql.append(SQLTempletes.V_BID_OVERDUE);
		sql.append(")");
		sql.append(SQLTempletes.TABLE_NAME);
		sql.append(conditions);

		query = em.createNativeQuery(sql.toString(), v_bid_overdue.class);

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }
	        
        query.setFirstResult((pageBean.currPage - 1) * pageBean.pageSize);
        query.setMaxResults(pageBean.pageSize);
		
		try {
			return query.getResultList();
		} catch (Exception e) {
			Logger.error("标->逾期的借款标列表:" + e.getMessage());
			error.msg = "加载逾期的借款标列表失败!";
			
			return null;
		}
	}
	
	/**
	 * 还款中的借款标列表
	 * @param pageBean 
	 * @param error 信息值
	 * @return List<v_bid_repaymenting>
	 */
	public static List<v_bid_repaymenting> queryBidRepaymenting(PageBean<v_bid_repaymenting> pageBean, long supId, ErrorInfo error, String ... str) {
		error.clear();
		
		int count = -1;
		StringBuffer conditions = new StringBuffer(" where 1 = 1"); 
		List<Object> values = new ArrayList<Object>(); 
		conditions.append(" and manage_supervisor_id = ?");
		
		/* 我/部门 的会员账单 */
		if(supId > 0) values.add(supId);
		else values.add(0);
		
		/* 组装条件 */
		Bid.getCondition(pageBean, conditions, values, "audit_time", str);
			
		StringBuffer sql = new StringBuffer();
		sql.append("select count(t.id) from (");
		sql.append(SQLTempletes.V_BID_REPAYMENTING);
		sql.append(")");
		sql.append(SQLTempletes.TABLE_NAME);
		sql.append(conditions);
		
		EntityManager em = JPA.em();
        Query query = em.createNativeQuery(sql.toString());

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }

        List<?> list = null;
        
		try {
			list = query.getResultList();
		} catch (Exception e) {
			Logger.error("标->还款中的借款标列表,查询总记录数:" + e.getMessage());
			error.msg = "加载还款中的借款标列表失败!";
			
			return null;
		}
		
		count = list == null ? 0 : Integer.parseInt(list.get(0).toString());
		
		if(count < 1)
			return new ArrayList<v_bid_repaymenting>();
			
		pageBean.totalCount = count;
		
		sql = new StringBuffer();
		sql.append("select t.* from (");
		sql.append(SQLTempletes.V_BID_REPAYMENTING);
		sql.append(")");
		sql.append(SQLTempletes.TABLE_NAME);
		sql.append(conditions);

		query = em.createNativeQuery(sql.toString(), v_bid_repaymenting.class);

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }
	        
        query.setFirstResult((pageBean.currPage - 1) * pageBean.pageSize);
        query.setMaxResults(pageBean.pageSize);
		
		try {
			return query.getResultList();
		} catch (Exception e) {
			Logger.error("标->还款中的借款标列表:" + e.getMessage());
			error.msg = "加载还款中的借款标列表失败!";
			
			return null;
		}
	}
	
	/**
	 * 已完成的借款标列表
	 * @param pageBean 
	 * @param error 信息值
	 * @return List<v_bid_repayment>
	 */
	public static List<v_bid_repayment> queryBidRepayment(PageBean<v_bid_repayment> pageBean, long supId, ErrorInfo error, String ... str) {
		error.clear();
		
		int count = -1;
		StringBuffer conditions = new StringBuffer(" where 1 = 1"); 
		List<Object> values = new ArrayList<Object>(); 
		conditions.append(" and manage_supervisor_id = ?");
		
		/* 我/部门 的会员账单 */
		if(supId > 0) values.add(supId);
		else values.add(0);
		
		/* 组装条件 */
		Bid.getCondition(pageBean, conditions, values, "audit_time", str);
			
		StringBuffer sql = new StringBuffer();
		sql.append("select count(t.id) from (");
		sql.append(SQLTempletes.V_BID_REPAYMENT);
		sql.append(")");
		sql.append(SQLTempletes.TABLE_NAME);
		sql.append(conditions);
		
		EntityManager em = JPA.em();
        Query query = em.createNativeQuery(sql.toString());

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }

        List<?> list = null;
        
		try {
			list = query.getResultList();
		} catch (Exception e) {
			Logger.error("标->已完成的借款标列表,查询总记录数:" + e.getMessage());
			error.msg = "加载已完成的借款标列表失败!";
			
			return null;
		}
		
		count = list == null ? 0 : Integer.parseInt(list.get(0).toString());
		
		if(count < 1)
			return new ArrayList<v_bid_repayment>();
			
		pageBean.totalCount = count;
		
		sql = new StringBuffer();
		sql.append("select t.* from (");
		sql.append(SQLTempletes.V_BID_REPAYMENT);
		sql.append(")");
		sql.append(SQLTempletes.TABLE_NAME);
		sql.append(conditions);

		query = em.createNativeQuery(sql.toString(), v_bid_repayment.class);

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }
	        
        query.setFirstResult((pageBean.currPage - 1) * pageBean.pageSize);
        query.setMaxResults(pageBean.pageSize);
		
		try {
			return query.getResultList();
		} catch (Exception e) {
			Logger.error("标->已完成的借款标列表:" + e.getMessage());
			error.msg = "加载已完成的借款标列表失败!";
			
			return null;
		}
	}
	
	/**
	 * 未通过的借款标列表
	 * @param pageBean 
	 * @param error 信息值
	 * @return List<v_bid_not_through>
	 */
	public static List<v_bid_not_through> queryBidNotThrough(PageBean<v_bid_not_through> pageBean, ErrorInfo error, String ... str) {
		error.clear();
		
		int count = -1;
		StringBuffer sql = new StringBuffer("");
		StringBuffer sqlCount = new StringBuffer();
		StringBuffer conditions = new StringBuffer("");
		StringBuffer conditionsCount = new StringBuffer("");
		List<Object> values = new ArrayList<Object>(); 
		List<Object> valuesCount = new ArrayList<Object>();
		Map<String, Object> conditiosMap = new HashMap<String, Object>();
		
		sql.append("SELECT concat(`e`.`_value`,cast(`b`.`id` AS CHAR charset utf8)) AS `bid_no`,`b`.`id` AS `id`,`b`.`title` AS `title`,`b`.`user_id` AS `user_id`,`u`.`name` AS `user_name`,`b`.`product_id` AS `product_id`,`p`.`small_image_filename` AS `small_image_filename`,`p`.`name` AS `product_name`,`b`.`apr` AS `apr`,`b`.`period_unit` AS `period_unit`,`b`.`period` AS `period`,`b`.`time` AS `time`,(CASE b. STATUS WHEN - 10 THEN DATE_ADD(b.time, INTERVAL 30 MINUTE) ELSE `b`.audit_time END) AS audit_time,`b`.`amount` AS `amount`,`b`.`status` AS `status`,`b`.`mark` AS `mark`,c.image_filename AS credit_level_image_filename,`f_user_audit_item` (`u`.`id`, `b`.`mark`, 2) AS `user_item_count_true`,b.repayment_type_id AS repaymentId,(SELECT count(`pail`.`id`) AS `product_item_count` FROM `t_product_audit_items_log` `pail` WHERE ((`pail`.`mark` = `b`.`mark`) AND (`pail`.`type` = 1))) AS `product_item_count` FROM ((((`t_bids` `b` LEFT JOIN `t_users` `u` ON ((`b`.`user_id` = `u`.`id`))) LEFT JOIN t_credit_levels c ON ((u.credit_level_id = c.id))) LEFT JOIN `t_products` `p` ON ((`b`.`product_id` = `p`.`id`))) JOIN `t_system_options` `e`) WHERE ((`b`.`status` IN (? ,? ,? ,? ,? ,?)) AND (`e`.`_key` = 'loan_number'))");
		sqlCount.append("SELECT COUNT(b.id) FROM (`t_bids` `b` LEFT JOIN `t_users` `u` ON ((`b`.`user_id` = `u`.`id`)) JOIN `t_system_options` `e`) WHERE (`b`.`status` IN (? ,? ,? ,? ,? ,?)) AND (`e`.`_key` = 'loan_number')");
		
		values.add(Constants.BID_NOT_THROUGH);
		values.add(Constants.BID_PEVIEW_NOT_THROUGH);
		values.add(Constants.BID_LOAN_NOT_THROUGH);
		values.add(Constants.BID_FLOW);
		values.add(Constants.BID_REPEAL);
		values.add(Constants.BID_NOT_VERIFY);
		
		valuesCount.add(Constants.BID_NOT_THROUGH);
		valuesCount.add(Constants.BID_PEVIEW_NOT_THROUGH);
		valuesCount.add(Constants.BID_LOAN_NOT_THROUGH);
		valuesCount.add(Constants.BID_FLOW);
		valuesCount.add(Constants.BID_REPEAL);
		valuesCount.add(Constants.BID_NOT_VERIFY);
		
		String[] BID_SEARCH = {" AND (u.name like ? or concat(`e`.`_value`,cast(`b`.`id` AS CHAR charset utf8)) like ?)", " AND concat(`e`.`_value`,cast(`b`.`id` AS CHAR charset utf8)) like ?", " AND u.name like ?"};
		
		if (null != str && str.length > 0) {
			int len = str.length;
			/* 用户id */
			if (NumberUtil.isNumericInt(str[0])) {
				conditions.append(" AND u.id = ?");
				conditionsCount.append(" AND u.id = ?");
				values.add(Integer.parseInt(str[0]));
				valuesCount.add(Integer.parseInt(str[0]));
				
				conditiosMap.put("user_id", Integer.parseInt(str[0]));
			}
			
			/* 条件  */
			if (len > 2 && NumberUtil.isNumericInt(str[1]) && StringUtils.isNotBlank(str[2])) {
				int c = Integer.parseInt(str[1]);
				conditions.append(BID_SEARCH[c]);
				conditionsCount.append(BID_SEARCH[c]);
				if (0 == c) {
					values.add("%" + str[2] + "%");
					values.add("%" + str[2] + "%");
					valuesCount.add("%" + str[2] + "%");
					valuesCount.add("%" + str[2] + "%");
				}else {
					values.add("%" + str[2] + "%");
					valuesCount.add("%" + str[2] + "%");
				}
				
				conditiosMap.put("condition", Integer.parseInt(str[1]));
				conditiosMap.put("keyword", str[2]);
			}
			
			/* 开始日期  */
			if (len > 3 && StringUtils.isNotBlank(str[3])) {
				conditions.append(" AND (CASE b.STATUS WHEN - (10) THEN DATE_ADD(b.time, INTERVAL 30 MINUTE) ELSE `b`.audit_time END) >= ?");
				conditionsCount.append(" AND (CASE b.STATUS WHEN - (10) THEN DATE_ADD(b.time, INTERVAL 30 MINUTE) ELSE `b`.audit_time END) >= ?");
				values.add(DateUtil.strDateToStartDate(str[3]));
				valuesCount.add(DateUtil.strDateToStartDate(str[3]));
				
				conditiosMap.put("startDate", str[3]);
			}
			
			/* 结束日期  */
			if (len > 4 && StringUtils.isNotBlank(str[4])) {
				conditions.append(" AND (CASE b.STATUS WHEN - (10) THEN DATE_ADD(b.time, INTERVAL 30 MINUTE) ELSE `b`.audit_time END) <= ?");
				conditionsCount.append(" AND (CASE b.STATUS WHEN - (10) THEN DATE_ADD(b.time, INTERVAL 30 MINUTE) ELSE `b`.audit_time END) <= ?");
				values.add(DateUtil.strDateToEndDate(str[4]));
				valuesCount.add(DateUtil.strDateToEndDate(str[4]));
				
				conditiosMap.put("endDate", str[4]);
			}
			
			/* 升降序  */
			if (len > 5 && NumberUtil.isNumericInt(str[5])) {
				int _order = Integer.parseInt(str[5]);
				conditions.append(Constants.BID_SEARCH_ORDER[_order]);
				
				conditiosMap.put("orderIndex", str[5]);
				
				/* 升降序 */
				if(str.length > 6 && NumberUtil.isNumericInt(str[6]) && _order > 0){
					
					if(Integer.parseInt(str[6]) == 1)
						conditions.append("ASC");
					else
						conditions.append("DESC");
					
					/* 保存当前索引值 + 升降值 */
					conditiosMap.put("orderStatus", str[6]);
				}
			}
			
			 /* 第一次让它为ID降序搜索 */ 
			if(StringUtils.isBlank(str[5])) 
				conditions.append(Constants.BID_SEARCH_ORDER[0]);
			
			pageBean.conditions = conditiosMap;
		}
		
		sqlCount.append(conditionsCount);
		
		EntityManager em = JPA.em();
        Query query = em.createNativeQuery(sqlCount.toString());

        for(int n = 1; n <= valuesCount.size(); n++){
            query.setParameter(n, valuesCount.get(n-1));
        }

        List<?> list = null;
        
		try {
			list = query.getResultList();
		} catch (Exception e) {
			Logger.error("标->未通过的借款标列表,查询总记录数:" + e.getMessage());
			error.msg = "加载未通过的借款标列表失败!";
			
			return null;
		}
		
		count = list == null ? 0 : Integer.parseInt(list.get(0).toString());
		
		if(count < 1)
			return new ArrayList<v_bid_not_through>();
			
		pageBean.totalCount = count;
		
		sql.append(conditions);

		query = em.createNativeQuery(sql.toString(), v_bid_not_through.class);

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }
	        
        query.setFirstResult((pageBean.currPage - 1) * pageBean.pageSize);
        query.setMaxResults(pageBean.pageSize);
		
		try {
			return query.getResultList();
		} catch (Exception e) {
			Logger.error("标->未通过的借款标列表:" + e.getMessage());
			error.msg = "加载未通过的借款标列表失败!";
			
			return null;
		}
	}
	
	/**
	 * 待放款/已放款 的标列表
	 * @param pageBean 分页对象
	 * @param error 信息值
	 * @return List<v_bid_already_release>
	 */
	public static List<v_bid_release_funds> queryReleaseFunds(PageBean<v_bid_release_funds> pageBean, int flag, ErrorInfo error, String ... str) {
		error.clear();
		
		int count = -1;
		StringBuffer conditions = new StringBuffer(" where 1 = 1"); 
		List<Object> values = new ArrayList<Object>(); 
		
		/* 待放款或已放款 */
		conditions.append(" and status = ?");
		values.add(flag);
		
		/* 组装条件 */
		Bid.getCondition(pageBean, conditions, values, "audit_time", str);
		
		StringBuffer sql = new StringBuffer();
		sql.append("select count(t.id) from (");
		sql.append(SQLTempletes.V_BID_RELEASE_FUNDS);
		sql.append(")");
		sql.append(SQLTempletes.TABLE_NAME);
		sql.append(conditions);
		
		EntityManager em = JPA.em();
        Query query = em.createNativeQuery(sql.toString());

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }

        List<?> list = null;
		
		try {
			list = query.getResultList();
		} catch (Exception e) {
			Logger.error("标->待放款/已放款 的标列表,查询总记录数:" + e.getMessage());
			error.msg = "加载标列表失败!";
			
			return null;
		}
		
		count = list == null ? 0 : Integer.parseInt(list.get(0).toString());
		
		if(count < 1)
			return new ArrayList<v_bid_release_funds>();
			
		pageBean.totalCount = count;
		
		sql = new StringBuffer();
		sql.append("select t.* from (");
		sql.append(SQLTempletes.V_BID_RELEASE_FUNDS);
		sql.append(")");
		sql.append(SQLTempletes.TABLE_NAME);
		sql.append(conditions);

		query = em.createNativeQuery(sql.toString(), v_bid_release_funds.class);

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }
	        
        query.setFirstResult((pageBean.currPage - 1) * pageBean.pageSize);
        query.setMaxResults(pageBean.pageSize);
		
		try {
			return query.getResultList();
		} catch (Exception e) {
			Logger.error("标->待放款/已放款 的标列表:" + e.getMessage());
			error.msg = "加载标列表失败!";
			
			return null;
		}
	}
	
	/**
	 * 用户关注的借款标列表
	 * @param pageBean 
	 * @param error 信息值
	 * @return List<v_bid_attention>
	 */
	public static List<v_bid_attention> queryBidAttention(PageBean<v_bid_attention> pageBean, ErrorInfo error) {
		error.clear();
		
		StringBuffer sql = new StringBuffer();
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_BID_ATTENTION);
		
		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql.toString(), v_bid_attention.class);
		query.setFirstResult((pageBean.currPage - 1) * pageBean.pageSize);
        query.setMaxResults(pageBean.pageSize);
        
		try {
			pageBean.totalCount = QueryUtil.getQueryCount(em);
	            
			return query.getResultList();
		} catch (Exception e) {
			Logger.error("标->用户关注的借款标列表:" + e.getMessage());
			error.msg = "加载用户关注的借款标列表失败!";
			
			return null;
		}
	}
	
	/**
	 * 标列表
	 */
	private static List<v_bids> queryBids(PageBean<v_bids> pageBean, String condition, List<Object> values) {
		int count = -1;
		StringBuffer sql = new StringBuffer();
		sql.append("select count(t.id) from (");
		sql.append(SQLTempletes.V_BIDS);
		sql.append(")");
		sql.append(SQLTempletes.TABLE_NAME);
		sql.append(condition);
		
		EntityManager em = JPA.em();
        Query query = em.createNativeQuery(sql.toString());

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }

        List<?> list = null;
		
		try {
			list = query.getResultList();
		} catch (Exception e) {
			Logger.error("标->标列表,查询总记录数:" + e.getMessage());
			
			return null;
		}

        count = list == null ? 0 : Integer.parseInt(list.get(0).toString());
		
		if(count < 1)
			return new ArrayList<v_bids>();
			
		pageBean.totalCount = count;
		
		sql = new StringBuffer();
		sql.append("select t.* from (");
		sql.append(SQLTempletes.V_BIDS);
		sql.append(")");
		sql.append(SQLTempletes.TABLE_NAME);
		sql.append(condition);

		query = em.createNativeQuery(sql.toString(), v_bids.class);

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }
	        
        query.setFirstResult((pageBean.currPage - 1) * pageBean.pageSize);
        query.setMaxResults(pageBean.pageSize);
		
		try {
			return query.getResultList();
		} catch (Exception e) {
			Logger.error("标->标列表:" + e.getMessage());

			return null;
		}
	}

	/**
	 * 最新优质标
	 * @param pageBean 分页对象
	 * @param error 信息值
	 * @return List<v_bids>
	 */
	public static List<v_bids> queryQualityBid(int size,ErrorInfo error) {
		error.clear();

		String condition = "is_quality = 1 and amount > has_invested_amount and status in (?, ?)  order by time desc";

		try {
			return v_bids.find(condition, Constants.BID_FUNDRAISE, Constants.BID_EAIT_LOAN).fetch(size);
		} catch (Exception e) {
			Logger.error("标->标列表:" + e.getMessage());
			error.msg = "加载标列表失败!";

			return null;
		}
	}
	
	

	/**
	 * 借款中的机构合作标
	 * @param pageBean 分页对象
	 * @param error 信息值
	 * @return List<v_bids>
	 */
	public static List<v_bids> queryAuditingAgencyBid(PageBean<v_bids> pageBean, ErrorInfo error) {
		error.clear();
		
		String condition = " where is_agency = 1 and status in (1,2)";
		List<v_bids> bids = Bid.queryBids(pageBean, condition, null);
		
		if(null == bids) {
			error.msg = "加载标列表失败!";
			
			return null;
		}
		
		return bids;
	}
	
	/**
	 * 全部机构合作标
	 * @param pageBean 分页对象
	 * @param error 信息值
	 * @return List<v_bids>
	 */
	public static List<v_bids> queryAllAgencyBid(PageBean<v_bids> pageBean, ErrorInfo error, String ... str) {
		error.clear();
		
		StringBuffer conditions = new StringBuffer(" where 1 = 1"); 
		List<Object> values = new ArrayList<Object>(); 

		/* 合作机构 */
		conditions.append(" and is_agency = ?");
		values.add(Constants.ENABLE);
		
		/* 组装条件 */
		Bid.getCondition(pageBean, conditions, values, "time", str);
		
		List<v_bids> bids = Bid.queryBids(pageBean, conditions.toString(), values);
		
		if(null == bids) {
			error.msg = "加载标列表失败!";
			
			return new ArrayList<v_bids>();
		}
		
		return bids;
	}
	
	/**
	 * 借款中的标
	 * @param pageBean 分页对象
	 * @param error 信息值
	 * @return List<v_bids>
	 */
	public static List<v_bids> queryFundraiseingBid(PageBean<v_bids> pageBean, ErrorInfo error) {
		error.clear();
		
		StringBuffer conditions = new StringBuffer(" where 1 = 1"); 
		List<Object> values = new ArrayList<Object>(); 
		
		/* 借款中的条件 */
		conditions.append(" and status in (?, ?) and amount > has_invested_amount and loan_schedule < 100");
		values.add(1);
		values.add(2);
		
		List<v_bids> bids = Bid.queryBids(pageBean, conditions.toString(), values);
		
		if(null == bids) {
			error.msg = "加载标列表失败!";
			
			return new ArrayList<v_bids>();
		}
		
		return bids;
	}
	
	/**
	 * 合作结构详情列表
	 * @param pageBean 分页对象
	 * @param agencyId 机构ID
	 * @param error 信息值
	 * @return List<v_bids>
	 */ 
	public static List<v_bids> queryAgencyBid(PageBean<v_bids> pageBean, long agencyId, ErrorInfo error, String ... str){
		error.clear();
		
		StringBuffer conditions = new StringBuffer(" where agency_id = ?"); 
		List<Object> values = new ArrayList<Object>(); 
		/* 借款中的条件 */
		values.add(agencyId);
		
		/* 组装条件 */
		Bid.getCondition(pageBean, conditions, values, "time", str);
		
		List<v_bids> bids = Bid.queryBids(pageBean, conditions.toString(), values);
		
		if(null == bids) {
			error.msg = "加载标列表失败!";
			
			return new ArrayList<v_bids>();
		}
		
		return bids;
	}
	
	/**
	 * 我的收藏标列表
	 * @param pageBean
	 * @param error
	 * @return List<v_bid_attention>
	 */
	public static List<v_bid_attention> queryAttentionBid(PageBean<v_bid_attention> pageBean, ErrorInfo error, String ... str){
		error.clear();
		
		int count = -1;
		StringBuffer conditions = new StringBuffer(" 1 = 1"); 
		List<Object> values = new ArrayList<Object>(); 
		
		/* 组装条件 */
		Bid.getCondition(pageBean, conditions, values, "audit_time", str);
		
		try {
			count = (int)v_bid_attention.count(conditions.toString(), values.toArray());
		} catch (Exception e) {
			Logger.error("标->我的收藏标列表,查询总记录数:" + e.getMessage());
			error.msg = "加载您收藏的借款标列表失败!";
			
			return null;
		}
		
		if(count < 1)
			return new ArrayList<v_bid_attention>();
			
		pageBean.totalCount = count;
		
		try {
			return v_bid_attention.find(conditions.toString(), values.toArray()).fetch(pageBean.currPage, pageBean.pageSize);
		} catch (Exception e) {
			Logger.error("标->我的收藏标列表:" + e.getMessage());
			error.msg = "加载您收藏的借款标列表失败!";
			
			return null;
		}
	}
	
	/**
	 * 某个用户的借款列表
	 * @param currPage 当前页
	 * @param pageSize 页码条目
	 * @param userId 用户ID
	 * @param error 信息值
	 * @return PageBean<v_bids> 
	 */
	public static PageBean<v_bids> queryBidByUser(long userId, String currPageStr, String pageSizeStr, ErrorInfo error){
		error.clear();
		
		int currPage = Constants.ONE;
 		int pageSize = Constants.TWO;
		
 		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		
		PageBean<v_bids> page = new PageBean<v_bids>();
		page.currPage = currPage;
		page.pageSize = pageSize;
		StringBuffer conditions = new StringBuffer(" where 1 = 1"); 
		List<Object> values = new ArrayList<Object>(); 
		
		/* 借款中的条件 */
		conditions.append(" and user_id = ?");
		values.add(userId);
		
		/* 组装条件 */
		
		List<v_bids> bids = Bid.queryBids(page, conditions.toString(), values);
		
		if(null == bids) 
			error.msg = "加载标列表失败!";
		
		page.page = bids;
		
		return page;
	} 
	
	/**---------------------------------------------报表字段查询方法------------------------------------------------------*/
	
	/**
	 * 查询借款标申请总数
	 * @param error
	 * @return
	 */
	public static long queryTotalBidCount(ErrorInfo error) {
		error.clear();
		Object count = 0;
		String sql = "SELECT COUNT(id) FROM t_bids WHERE `status` IN (?, ?, ?, ?, ?, ?)";
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, Constants.BID_ADVANCE_LOAN).setParameter(2, Constants.BID_FUNDRAISE).setParameter(3, Constants.BID_EAIT_LOAN).setParameter(4, Constants.BID_REPAYMENT).setParameter(5, Constants.BID_REPAYMENTS).setParameter(6, Constants.BID_COMPENSATE_REPAYMENT);

		try {
			count = query.getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			error.code = -1;
			error.msg = "查询借款标申请总数失败";
			
			return -1;
		}
		
		return Long.parseLong(count.toString());
	}
	
	/**
	 * 查询借款标交易总金额
	 * @param error
	 * @return
	 */
	public static double queryTotalDealAmount(ErrorInfo error) {
		error.clear();
		Double count = null;

		try {
			count = t_bids.find("select SUM(amount) from t_bids WHERE status IN (?1, ?2, ?3, ?4, ?5, ?6)").setParameter("1", Constants.BID_ADVANCE_LOAN).setParameter("2", Constants.BID_FUNDRAISE).setParameter("3", Constants.BID_EAIT_LOAN).setParameter("4", Constants.BID_REPAYMENT).setParameter("5", Constants.BID_REPAYMENTS).setParameter("6", Constants.BID_COMPENSATE_REPAYMENT).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			error.code = -1;
			error.msg = "查询借款标交易总金额失败";
			
			return -1;
		}
		
		return null == count ? 0 : count;
	}
	
	/**
	 * 查询今日新增借款标申请数
	 * @param error
	 * @return
	 */
	public static long queryTodayBidCount(ErrorInfo error) {
		error.clear();
		long count = 0;

		try {
			count = t_bids.count("DATEDIFF(NOW(), time) < 1");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			error.code = -1;
			error.msg = "今日新增借款标申请失败";
			
			return -1;
		}
		
		return count;
	}
	
	/**--------------------------------------------上一个、下一个-------------------------------------------------*/
	
	public long upId; // 上个ID
	public long nextId; // 下个ID 
	public int countUpId; // 上个ID
	public int countNextId; // 下个ID 
	public int upNextFlag; // 当前上一个下一个列表项
	
	/**
	 * 获取上个Id
	 */
	public long getUpId() {
		if(0 == this.upId) {
			String sql = "select MAX(id) from t_bids where id < ?";
			String sql2 = "select MAX(a.id) from t_bids a left join t_bills b on a.id = b.bid_id where a.id < ?";
			String sql3 = "select MAX(a.id) from t_bids a left join t_bill_invests b on a.id = b.bid_id where a.id < ?";
			
			switch (this.upNextFlag) {
			case 2: sql += " and status in(1, 2) and amount > has_invested_amount"; break;
			case 3: sql += " and status in(1, 2) and amount = has_invested_amount"; break;
			case 1: case 4: case 5: case 6: case 8: default: sql += " and status = " + this.status; break;
			case 7: sql = sql2; sql += " and a.status = 4 and b.overdue_mark in(-1, -2, -3)"; break;
			case 9: sql += " and status <= -1 and status >= -10"; break;
			case 10: sql = sql2; sql += " and a.status in(4, 5) and b.overdue_mark = -3"; break;
			
			/* 账单催收，我的账单 */
			case 11: sql = sql2; sql += " and year(b.repayment_time)= year(now()) and month(b.repayment_time)= month(now()) and b.overdue_mark not in(- 2, - 3) and a.manage_supervisor_id = " + this.manageSupervisorId; break; 
			case 12: sql = sql2; sql += " and b.overdue_mark in(-1, -2) and a.manage_supervisor_id = " + this.manageSupervisorId; break;
			case 13: sql = sql2; sql += " and b.status in (0, -3) and a.manage_supervisor_id = " + this.manageSupervisorId; break; 
			/* 账单催收，部门的账单 */
			case 14: sql = sql2; sql += " and year(b.repayment_time)= year(now()) and month(b.repayment_time)= month(now()) and b.overdue_mark not in(- 2, - 3) and a.manage_supervisor_id = 0"; break; 
			case 15: sql = sql2; sql += " and b.overdue_mark in(-1, -2) and a.manage_supervisor_id = 0"; break; 
			case 16: sql = sql2; sql += " and b.status in (0, -3) and a.manage_supervisor_id = 0"; break; 
			/* 账单催收，我的标 */
			case 106: case 108: sql += " and status = " + this.status + " and manage_supervisor_id = " + this.manageSupervisorId; break; 
			case 110: sql = sql2; sql += " and a.status in(4, 5) and b.overdue_mark = -3 and a.manage_supervisor_id = " + this.manageSupervisorId; break; 
			/* 账单催收，部门的标 */
			case 206: case 208: sql += " and status = " + this.status + " and manage_supervisor_id = 0"; break; 
			case 210: sql = sql2; sql += " and a.status in(4, 5) and b.overdue_mark = -3 and a.manage_supervisor_id = 0"; break; 
			case 211: sql += " and manage_supervisor_id > 0"; break;
				
			/* 合作机构 */
			case 300: sql += " and is_agency = 1"; break;
			
			/* 财务 */
			case 400: sql = sql2; sql += " and b.status in (-1, -2)"; break; //400.待收款借款账单。
			case 401: sql = sql2; sql += " and b.status in (-1, -2) and b.overdue_mark in (-1, -2, -3)"; break; //401.逾期账单列表。
			case 402: sql = sql2; sql += " and b.status in (0, -3)"; break; //402.已收款借款账单列表。
			case 403: sql += " and status = 5"; break; //403.已完成借款标列表。
			case 500: sql = sql3; sql += " and b.status in (-1, -2, -5, -6)"; break; //500.待付款理财账单列表。
			case 501: sql = sql3; sql += " and b.status in (-2)"; break; //501.逾期未付理财账单列表。
			case 502: sql = sql3; sql += " and b.status in (-4, 0)"; break; //502.已付款理财账单列表。
			case 503: sql = sql3; sql += " and b.status in (-3)"; break; //503.本金垫付理财账单列表。
			case 504: sql = sql2; sql += " and a.status in(4, 5) and b.overdue_mark = -3"; break; //504.坏账借款标列表。
			}
			
			Query query = JPA.em().createNativeQuery(sql);
			query.setParameter(1, this.id);
			List<Object> obj = null;
			
			try {
				obj = query.getResultList();
			} catch (Exception e) {
				return 0;
			}
				
			if(null == obj || obj.size() == 0)
				return this.id;
			
			Object count = obj.get(0);
		
			this.upId = count == null ? this.id : Long.parseLong(count.toString());
		}
		
		return this.upId;
	}
	
	/**
	 * 下个Id
	 */
	public long getNextId() {
		if(0 == this.nextId) { 
			String sql = "select MIN(id) from t_bids where id > ?";
			String sql2 = "select MIN(a.id) from t_bids a left join t_bills b on a.id = b.bid_id where a.id > ?";
			String sql3 = "select MIN(a.id) from t_bids a left join t_bill_invests b on a.id = b.bid_id where a.id > ?";
			
			switch (this.upNextFlag) {
			case 2: sql += " and status in(1, 2) and amount > has_invested_amount"; break;
			case 3: sql += " and status in(1, 2) and amount = has_invested_amount"; break;
			case 1: case 4: case 5: case 6: case 8: sql += " and status = " + this.status; break;
			case 7: sql = sql2; sql += " and a.status = 4 and b.overdue_mark in(-1, -2, -3)"; break;
			case 9: sql += " and status <= -1 and status >= -10"; break;
			case 10: sql = sql2; sql += " and a.status in(4, 5) and b.overdue_mark = -3"; break;
			
			/* 账单催收，我的账单 */
			case 11: sql = sql2; sql += " and year(b.repayment_time)= year(now()) and month(b.repayment_time)= month(now()) and b.overdue_mark not in(- 2, - 3) and a.manage_supervisor_id = " + this.manageSupervisorId; break; 
			case 12: sql = sql2; sql += " and b.overdue_mark in(-1, -2) and a.manage_supervisor_id = " + this.manageSupervisorId; break;
			case 13: sql = sql2; sql += " and b.status in (0, -3) and a.manage_supervisor_id = " + this.manageSupervisorId; break; 
			/* 账单催收，部门的账单 */
			case 14: sql = sql2; sql += " and year(b.repayment_time)= year(now()) and month(b.repayment_time)= month(now()) and b.overdue_mark not in(- 2, - 3) and a.manage_supervisor_id = 0"; break; 
			case 15: sql = sql2; sql += " and b.overdue_mark in(-1, -2) and a.manage_supervisor_id = 0"; break; 
			case 16: sql = sql2; sql += " and b.status in (0, -3) and a.manage_supervisor_id = 0"; break; 
			/* 账单催收，我的标 */
			case 106: case 108: sql += " and status = " + this.status + " and manage_supervisor_id = " + this.manageSupervisorId; break; 
			case 110: sql = sql2; sql += " and a.status in(4, 5) and b.overdue_mark = -3 and a.manage_supervisor_id = " + this.manageSupervisorId; break; 
			/* 账单催收，部门的标 */
			case 206: case 208: sql += " and status = " + this.status + " and manage_supervisor_id = 0"; break; 
			case 210: sql = sql2; sql += " and a.status in(4, 5) and b.overdue_mark = -3 and a.manage_supervisor_id = 0"; break; 
			case 211: sql += " and manage_supervisor_id > 0"; break;
			
			/* 合作机构 */
			case 300: sql += " and is_agency = 1"; break;
			
			/* 财务 */
			case 400: sql = sql2; sql += " and b.status in (-1, -2)"; break; //400.待收款借款账单。
			case 401: sql = sql2; sql += " and b.status in (-1, -2) and b.overdue_mark in (-1, -2, -3)"; break; //401.逾期账单列表。
			case 402: sql = sql2; sql += " and b.status in (0, -3)"; break; //402.已收款借款账单列表。
			case 403: sql += " and status = 5"; break; //403.已完成借款标列表。
			case 500: sql = sql3; sql += " and b.status in (-1, -2, -5, -6)"; break; //500.待付款理财账单列表。
			case 501: sql = sql3; sql += " and b.status in (-2)"; break; //501.逾期未付理财账单列表。
			case 502: sql = sql3; sql += " and b.status in (-4, 0)"; break; //502.已付款理财账单列表。
			case 503: sql = sql3; sql += " and b.status in (-3)"; break; //503.本金垫付理财账单列表。
			case 504: sql = sql2; sql += " and a.status in(4, 5) and b.overdue_mark = -3"; break; //504.坏账借款标列表。
			}
			
			Query query = JPA.em().createNativeQuery(sql);
			query.setParameter(1, this.id);
			List<Object> obj = null;
			
			try {
				obj = query.getResultList();
			} catch (Exception e) {
				return 0;
			}
				
			if(null == obj || obj.size() == 0)
				return this.id;
			
			Object count = obj.get(0);
			
			this.nextId = count == null ? this.id : Long.parseLong(count.toString());
		}
		
		return this.nextId;
	}

	/**
	 * 上一个ID统计
	 */
	public int getCountUpId() {
		if(0 == this.countUpId) {
			String sql = "id < ?";
			String sql2 = "select count(distinct a.id) from t_bids a left join t_bills b on a.id = b.bid_id where a.id < ?";
			String sql3 = "select count(distinct a.id) from t_bids a left join t_bill_invests b on a.id = b.bid_id where a.id < ?";
			
			switch (this.upNextFlag) {
			case 2: sql += " and status in(1, 2) and amount > has_invested_amount"; break;
			case 3: sql += " and status in(1, 2) and amount = has_invested_amount"; break;
			case 1: case 4: case 5: case 6: case 8: sql += " and status = " + this.status; break;
			case 7: sql = sql2; sql += " and a.status = 4 and b.overdue_mark in(-1, -2, -3)"; break;
			case 9: sql += " and status <= -1 and status >= -10"; break;
			case 10: sql = sql2; sql += " and a.status in(4, 5) and b.overdue_mark = -3"; break;
			
			/* 账单催收，我的账单 */
			case 11: sql = sql2; sql += " and year(b.repayment_time)= year(now()) and month(b.repayment_time)= month(now()) and b.overdue_mark not in(- 2, - 3) and a.manage_supervisor_id = " + this.manageSupervisorId; break; 
			case 12: sql = sql2; sql += " and b.overdue_mark in(-1, -2) and a.manage_supervisor_id = " + this.manageSupervisorId; break;
			case 13: sql = sql2; sql += " and b.status in (0, -3) and a.manage_supervisor_id = " + this.manageSupervisorId; break; 
			/* 账单催收，部门的账单 */
			case 14: sql = sql2; sql += " and year(b.repayment_time)= year(now()) and month(b.repayment_time)= month(now()) and b.overdue_mark not in(- 2, - 3) and a.manage_supervisor_id = 0"; break; 
			case 15: sql = sql2; sql += " and b.overdue_mark in(-1, -2) and a.manage_supervisor_id = 0"; break; 
			case 16: sql = sql2; sql += " and b.status in (0, -3) and a.manage_supervisor_id = 0"; break; 
			/* 账单催收，我的标 */
			case 106: case 108: sql += " and status = " + this.status + " and manage_supervisor_id = " + this.manageSupervisorId; break; 
			case 110: sql = sql2; sql += " and a.status in(4, 5) and b.overdue_mark = -3 and a.manage_supervisor_id = " + this.manageSupervisorId; break; 
			/* 账单催收，部门的标 */
			case 206: case 208: sql += " and status = " + this.status + " and manage_supervisor_id = 0"; break; 
			case 210: sql = sql2; sql += " and a.status in(4, 5) and b.overdue_mark = -3 and a.manage_supervisor_id mkmk  = 0"; break; 
			case 211: sql += " and manage_supervisor_id > 0"; break;
			
			/* 合作机构 */
			case 300: sql += " and is_agency = 1"; break;
			
			/* 财务 */
			case 400: sql = sql2; sql += " and b.status in (-1, -2)"; break; //400.待收款借款账单。
			case 401: sql = sql2; sql += " and b.status in (-1, -2) and b.overdue_mark in (-1, -2, -3)"; break; //401.逾期账单列表。
			case 402: sql = sql2; sql += " and b.status in (0, -3)"; break; //402.已收款借款账单列表。
			case 403: sql += " and status = 5"; break; //403.已完成借款标列表。
			case 500: sql = sql3; sql += " and b.status in (-1, -2, -5, -6)"; break; //500.待付款理财账单列表。
			case 501: sql = sql3; sql += " and b.status in (-2)"; break; //501.逾期未付理财账单列表。
			case 502: sql = sql3; sql += " and b.status in (-4, 0)"; break; //502.已付款理财账单列表。
			case 503: sql = sql3; sql += " and b.status in (-3)"; break; //503.本金垫付理财账单列表。
			case 504: sql = sql2; sql += " and a.status in(4, 5) and b.overdue_mark = -3"; break; //504.坏账借款标列表。
			}
			
			/* count 语句单独处理 */
			if( this.upNextFlag == 7 || 
				this.upNextFlag == 110 ||
				this.upNextFlag == 210 ||
			    (this.upNextFlag <= 16 && this.upNextFlag >= 10) ||
			    (this.upNextFlag <= 402 && this.upNextFlag >= 400) ||
			    (this.upNextFlag <= 504 && this.upNextFlag >= 500) 
			   ){
				Query query = JPA.em().createNativeQuery(sql);
				query.setParameter(1, this.id);
				List<Object> obj = null;
				
				try {
					obj = query.getResultList();
				} catch (Exception e) {
					return 0;
				}
					
				if(null == obj || obj.size() == 0)
					return 0;
				
				Object count = obj.get(0);
				
				this.countUpId = count == null ? 0 : Integer.parseInt(count.toString());
			}else {
				try {
					this.countUpId = (int)t_bids.count(sql, this.id);
				} catch (Exception e) {
					return 0;
				}
			}
		}
		
		return this.countUpId;
	}

	/**
	 * 下一个ID统计
	 */
	public int getCountNextId() {
		if(0 == this.countNextId) {
			String sql = "id > ?";
			String sql2 = "select count(distinct a.id) from t_bids a left join t_bills b on a.id = b.bid_id where a.id > ?";
			String sql3 = "select count(distinct a.id) from t_bids a left join t_bill_invests b on a.id = b.bid_id where a.id > ?";
			
			switch (this.upNextFlag) {
			case 2: sql += " and status in(1, 2) and amount > has_invested_amount"; break;
			case 3: sql += " and status in(1, 2) and amount = has_invested_amount"; break;
			case 1: case 4: case 5: case 6: case 8: sql += " and status = " + this.status; break;
			case 7: sql = sql2; sql += " and a.status = 4 and b.overdue_mark in(-1, -2, -3)"; break;
			case 9: sql += " and status <= -1 and status >= -10"; break;
			case 10: sql = sql2; sql += " and a.status in(4, 5) and b.overdue_mark = -3"; break;
			
			/* 账单催收，我的账单 */
			case 11: sql = sql2; sql += " and year(b.repayment_time)= year(now()) and month(b.repayment_time)= month(now()) and b.overdue_mark not in(- 2, - 3) and a.manage_supervisor_id = " + this.manageSupervisorId; break; 
			case 12: sql = sql2; sql += " and b.overdue_mark in(-1, -2) and a.manage_supervisor_id = " + this.manageSupervisorId; break;
			case 13: sql = sql2; sql += " and b.status in (0, -3) and a.manage_supervisor_id = " + this.manageSupervisorId; break; 
			/* 账单催收，部门的账单 */
			case 14: sql = sql2; sql += " and year(b.repayment_time)= year(now()) and month(b.repayment_time)= month(now()) and b.overdue_mark not in(- 2, - 3) and a.manage_supervisor_id = 0"; break; 
			case 15: sql = sql2; sql += " and b.overdue_mark in(-1, -2) and a.manage_supervisor_id = 0"; break; 
			case 16: sql = sql2; sql += " and b.status in (0, -3) and a.manage_supervisor_id = 0"; break; 
			/* 账单催收，我的标 */
			case 106: case 108: sql += " and status = " + this.status + " and manage_supervisor_id = " + this.manageSupervisorId; break; 
			case 110: sql = sql2; sql += " and a.status in(4, 5) and b.overdue_mark = -3 and a.manage_supervisor_id = " + this.manageSupervisorId; break; 
			/* 账单催收，部门的标 */
			case 206: case 208: sql += " and status = " + this.status + " and manage_supervisor_id = 0"; break; 
			case 210: sql = sql2; sql += " and a.status in(4, 5) and b.overdue_mark = -3 and a.manage_supervisor_id mkmk  = 0"; break; 
			case 211: sql += " and manage_supervisor_id > 0"; break;
			
			/* 合作机构 */
			case 300: sql += " and is_agency = 1"; break;
			
			/* 财务 */
			case 400: sql = sql2; sql += " and b.status in (-1, -2)"; break; //400.待收款借款账单。
			case 401: sql = sql2; sql += " and b.status in (-1, -2) and b.overdue_mark in (-1, -2, -3)"; break; //401.逾期账单列表。
			case 402: sql = sql2; sql += " and b.status in (0, -3)"; break; //402.已收款借款账单列表。
			case 403: sql += " and status = 5"; break; //403.已完成借款标列表。
			case 500: sql = sql3; sql += " and b.status in (-1, -2, -5, -6)"; break; //500.待付款理财账单列表。
			case 501: sql = sql3; sql += " and b.status in (-2)"; break; //501.逾期未付理财账单列表。
			case 502: sql = sql3; sql += " and b.status in (-4, 0)"; break; //502.已付款理财账单列表。
			case 503: sql = sql3; sql += " and b.status in (-3)"; break; //503.本金垫付理财账单列表。
			case 504: sql = sql2; sql += " and a.status in(4, 5) and b.overdue_mark = -3"; break; //504.坏账借款标列表。
			}
			
			/* count 语句单独处理 */
			if( this.upNextFlag == 7 || 
				this.upNextFlag == 110 ||
				this.upNextFlag == 210 ||
			    (this.upNextFlag <= 16 && this.upNextFlag >= 10) ||
			    (this.upNextFlag <= 402 && this.upNextFlag >= 400) ||
			    (this.upNextFlag <= 504 && this.upNextFlag >= 500) 
			   ){
				Query query = JPA.em().createNativeQuery(sql);
				query.setParameter(1, this.id);
				List<Object> obj = null;
				
				try {
					obj = query.getResultList();
				} catch (Exception e) {
					return 0;
				}
					
				if(null == obj || obj.size() == 0)
					return 0;
				
				Object count = obj.get(0);
				
				this.countNextId = count == null ? 0 : Integer.parseInt(count.toString());
			}else {
				try {
					this.countNextId = (int)t_bids.count(sql, this.id);
				} catch (Exception e) {
					return 0;
				}
			}
		}
		
		return this.countNextId;
	}
	
	/**--------------------------------------------------还款类型-----------------------------------------------------------------*/
	
	public static class Repayment implements Serializable{
		public long id; // 还款类型ID
		public long _id;
		public String name; // 还款类型名称
		public boolean isUse; // 是否启用
		
		/**
		 * 获取还款类型名称
		 */
		public String getName() {
			if(null == this.name) {
				String hql = "select name from t_dict_bid_repayment_types where id = ?";
				
				try {
					this.name = t_dict_loan_purposes.find(hql, this.id).first();
				} catch (Exception e) {
					Logger.error("标->获取还款用途名称:" + e.getMessage());
					
					return null;
				}
			}
			
			return this.name;
		}
		
		/**
		 * 获取 还款类型列表
		 * @param info 错误信息
		 * @param id 还款类型ID
		 * @return 标对象集合
		 */
		public static List<Repayment> queryRepaymentType(String [] repaymentTypeId) {
			List<Repayment> repayments = new ArrayList<Repayment>();
			List<t_dict_bid_repayment_types> tbids = null;

			String hql = "select new t_dict_bid_repayment_types(id, name, is_use) "
					+ "from t_dict_bid_repayment_types";

			if (repaymentTypeId != null && repaymentTypeId.length > 0) {
				hql += " where id in(";

				for (String id : repaymentTypeId) {
					hql += id + ","; // 还款类型数量很少，直接用String拼接
				}

				hql = hql.substring(0, hql.length() - 1);
				hql += ")";
			}

			try {
				tbids = t_dict_bid_repayment_types.find(hql).fetch();
			} catch (Exception e) {
				Logger.error("标->获取还款类型列表:" + e.getMessage());
				
				return null;
			}
			
			Repayment repayment = null;
			
			for (t_dict_bid_repayment_types type : tbids) {
				repayment = new Repayment();

				repayment.id = type.id;
				repayment.name = type.name;
				repayment.isUse = type.is_use;

				repayments.add(repayment);
			}
			
			return repayments;
		}
		
		/**
		 * 显示/隐藏 还款类型
		 * @param id 还款类型ID
		 * @param isUse 状态值
		 * @param error 错误信息
		 */
		public static void editRepaymentType(long rid, boolean isUse, ErrorInfo error) {
			error.clear();
			
			String hql = "update t_dict_bid_repayment_types set is_use=? where id=?";
			Query query = JPA.em().createQuery(hql);
			query.setParameter(1, isUse);
			query.setParameter(2, rid);
			
			try {
				error.code = query.executeUpdate();
			} catch (Exception e) {
				Logger.error("标->显示/隐藏 :" + e.getMessage());
				error.msg = "设置失败!";

				return;
			}
			
			if(error.code < 1){
				error.msg = "设置失败!";
				
				return;
			}
			
			/* 添加事件 */
			if(isUse)
				DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ENABLE_REPAYMENT_TYPE, "启用还款类型", error);
			else
				DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.NOT_ENABLE_REPAYMENT_TYPE, "不启用还款类型", error);
			
			if(error.code < 0){
				error.msg = "设置失败!";
				JPA.setRollbackOnly();
				
				return;
			}
		}
	}

	
	/**--------------------------------------------------借款用途-----------------------------------------------------------------*/

	public static class Purpose implements Serializable{
		public long id; // 借款目的ID
		public String name; // 用途名称
		public boolean isUse; // 是否启用
		public int order; // 排序

		// public String code; //编码
		// public String description; //描述
		
		/**
		 * 获取借款用途名称
		 */
		public String getName() {
			if(null == this.name) {
				String hql = "select name from t_dict_loan_purposes where id = ?";
				
				try {
					this.name = t_dict_loan_purposes.find(hql, this.id).first();
				} catch (Exception e) {
					Logger.error("标->获取还款用途名称:" + e.getMessage());
					
					return null;
				}
			}
			
			return this.name;
		}

		/**
		 * 查询借款用途列表
		 * @param info 错误信息
		 * @return 标对象集合
		 */
		public static List<Purpose> queryLoanPurpose(ErrorInfo error, boolean isUse) {
			error.clear();
			
			List<Purpose> purposes = new ArrayList<Purpose>();
			List<t_dict_loan_purposes> tpurposes = null;

			String hql = "select new t_dict_loan_purposes(id, name, is_use, _order) "
					+ "from t_dict_loan_purposes";

			if(isUse)
				hql += " where is_use = 1";
					
			hql += " order by _order, id";
			
			try {
				tpurposes = t_dict_loan_purposes.find(hql).fetch();
			} catch (Exception e) {
				Logger.error("标->获取借款用途列表:" + e.getMessage());
				error.msg = "获取借款用途列表有误!";
				
				return null;
			}

			if(null == tpurposes) return purposes;
			
			Purpose purpose = null;
			
			for (t_dict_loan_purposes tpurpose : tpurposes) {
				purpose = new Purpose();

				purpose.id = tpurpose.id;
				purpose.name = tpurpose.name;
				purpose.isUse = tpurpose.is_use;
				purpose.order = tpurpose._order;

				purposes.add(purpose);
			}
			
			return purposes;
		}

		/**
		 * 显示/隐藏借款用途
		 * @param isUse 状态值
		 * @param id 借款目的ID
		 * @param error 错误信息
		 */
		public static void editLoanPurposeStatus(long productId, boolean isUse, ErrorInfo error) {
			error.clear();
			
			String hql = "update t_dict_loan_purposes set is_use=? where id=?";
			Query query = JPA.em().createQuery(hql);
			query.setParameter(1, isUse);
			query.setParameter(2, productId);
			
			try {
				error.code = query.executeUpdate();
			} catch (Exception e) {
				Logger.error("标->显示/隐藏借款用途:" + e.getMessage());
				error.msg = "设置失败!";

				return;
			}
			
			if(error.code < 1){
				error.msg = "设置失败!";
				
				return;
			}
			
			/* 添加事件 */
			if(isUse)
				DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ENABLE_PURPOSE, "启用借款用途", error);
			else
				DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.NOT_ENABLE_PURPOSE, "不启用借款用途", error);
			
			if(error.code < 0){
				error.msg = "设置失败!";
				JPA.setRollbackOnly();
				
				return;
			}
		}
		
		/**
		 * 添加 借款用途
		 * @param error 错误信息
		 */
		public static void addLoanPurpose(String name, int order, ErrorInfo error) {
			error.code = -1;
			
			t_dict_loan_purposes loanPurpose = new t_dict_loan_purposes();
			loanPurpose.is_use = Constants.ENABLE; // 默认为启用
			loanPurpose.name = name;
			loanPurpose._order = order;
			
			try {
				loanPurpose.save();
			} catch (Exception e) {
				Logger.error("标->添加/编辑借款用途:" + e.getMessage());
				error.msg = "添加借款用途失败!";
				
				return;
			}
			
			/* 添加事件 */
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.CREATE_PURPOSE, "添加借款用途", error);
			
			if(error.code < 0){
				error.msg = "添加失败!";
				JPA.setRollbackOnly();
				
				return;
			}
			
			error.code = 1;
			error.msg = "保存成功!";
		}
		
		/**
		 * 编辑借款用途
		 * @param id 借款用途ID
		 * @param error 错误信息
		 */
		public static void editLoanPurpose(long id, String name, int order, ErrorInfo error) {
			error.code = -1;
			int row = 0;
			
			String hql = "update t_dict_loan_purposes set name = ?, _order = ? where id = ?";
			Query query = JPA.em().createQuery(hql);
			query.setParameter(1, name);
			query.setParameter(2, order);
			query.setParameter(3, id);
			
			try {
				row = query.executeUpdate();
			} catch (Exception e) {
				Logger.error("标->编辑借款用途:" + e.getMessage());
				error.msg = "编辑失败!";

				return;
			}
			
			if(row < 1)
				error.msg = "编辑失败!";
			
			/* 添加事件 */
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.EDIT_PURPOSE, "编辑借款用途", error);
			
			if(error.code < 0){
				error.msg = "编辑失败!";
				JPA.setRollbackOnly();
				
				return;
			}
			
			error.code = 1;
			error.msg = "保存成功!";
		}
		
		/**
		 * 查询借款目的
		 * @param purposeId
		 * @param error
		 * @return
		 */
		public static String queryPurpose(long purposeId, ErrorInfo error) {
			error.clear();
			
			String sql = "select name from t_dict_loan_purposes where id = ? limit 1";
			String purpose = null;
			
			try {
				purpose = JPA.em().createNativeQuery(sql).setParameter(1, purposeId).getSingleResult().toString();
			} catch (Exception e) {
				Logger.error("查询借款用途失败:" + e.getMessage());
				error.msg = "编辑失败!";

				return null;
			}
			
			return purpose;
		}
	}
	
	public double getInvestBonus() {
		if(this.bonusType == Constants.FIXED_AMOUNT_REWARD){
			return this.bonus;
		}
		
		if(this.bonusType == Constants.PROPORTIONATELY_REWARD){
			return Arith.div(Arith.mul(this.amount, this.awardScale), 100, 10);
		}
		
		return 0;
	}
	
	/**
	 * 扣除投标奖励
	 * @param error
	 * @return
	 */
	public int deductInvestBonus(ErrorInfo error) {
		error.clear();
		DataSafety data = new DataSafety();
		data.id = user.id;
		
		if(!data.signCheck(error)){
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "资金异常，不能扣除投标奖励";
			
			return error.code;
		}
		
		//更新用户资金
		if (Constants.PAY_TYPE_INVEST == PayType.INDEPENDENT) {
			error.code = DealDetail.minusUserFund2(user.id, this.investBonus);
		} else {
			error.code = DealDetail.minusUserFund(user.id, this.investBonus);
		}
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		/*添加交易记录*/
		Map<String, Double> detail = DealDetail.queryUserFund(user.id, error);
		DealDetail dealDetail = new DealDetail(this.userId, DealType.CHARGE_BONUS_FEE,
				this.investBonus, this.id, detail.get("user_amount2"), detail.get("freeze"),
				detail.get("receive_amount"), "扣除用户投标奖励");

		dealDetail.addDealDetail2(error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}
		
		data.id = user.id;
		data.updateSign(error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			error.msg = "扣除用户投标奖励失败";
			
			return error.code;
		}
		
		if (Constants.PAY_TYPE_INVEST == PayType.INDEPENDENT) {
			user.balance2 -= this.investBonus;
		} else {
			user.balance -= this.investBonus;
		}
		
		/*后台发布机构合作标，User.currUser()可能为null*/
		User currUser = User.currUser();
		
		if (currUser != null && currUser.id == this.userId) {
			User.setCurrUser(user);
		}
		
		error.msg = "扣除投标奖励";
		
		return error.code;
	}
	
	/**
	 * 退回投标奖励
	 * @param error
	 * @return
	 */
	public int refundInvestBonus(ErrorInfo error) {
		error.clear();
		
		if (this.investBonus == 0) {
			error.msg = "未设置投标奖励，不必退回投标奖励";
			
			return error.code;
		}
		
		DataSafety data = new DataSafety();
		data.id = this.userId;
		
		if(!data.signCheck(error)){
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "资金异常，不能退回投标奖励";
			
			return error.code;
		}
		
		//增加用户资金
		if (Constants.PAY_TYPE_REFUND == PayType.IPS) {
			Payment.transferMerToUser(this.userId, this.investBonus, error);
			
			if (error.code < 0 && error.code != Constants.ALREADY_RUN) {
				return error.code;
			}
			
			error.code = DealDetail.addUserFund(this.userId, this.investBonus);
		} else {
			error.code = DealDetail.addUserFund2(this.userId, this.investBonus);
		}
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		/*添加交易记录*/
		Map<String, Double> detail = DealDetail.queryUserFund(this.userId, error);
		double balance = Constants.PAY_TYPE_REFUND == PayType.IPS ? detail.get("user_amount") : detail.get("user_amount2");
		DealDetail dealDetail = new DealDetail(this.userId, DealType.REFUND_BONUS_FEE,
				this.investBonus, this.id, balance, detail.get("freeze"),
				detail.get("receive_amount"), "退回投标奖励");

		if (Constants.PAY_TYPE_REFUND == PayType.IPS) {
			dealDetail.addDealDetail(error);
		} else {
			dealDetail.addDealDetail2(error);
		}
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		data.id = userId;
		data.updateSign(error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			error.msg = "退回投标奖励失败";
			
			return error.code;
		}
		
		User user = User.currUser();
		
		if (user != null) {
			if (Constants.PAY_TYPE_REFUND == PayType.IPS) {
				user.balance += this.investBonus;
			} else {
				user.balance2 += this.investBonus;
			}
			
			User.setCurrUser(user);
		}
		
		error.msg = "退回投标奖励成功";
		
		return error.code;
	}
	
	/* 2014-11-14 */
	/**
	 * 借款合同
	 */
	public static String queryPact(long id, long userId) {
		if(id < 1)
			return "";
		
		try {
			return t_bids.find("select pact from t_bids where id = ? and user_id = ?", id, userId).first();
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	/**
	 * 居间服务协议
	 * @param id
	 * @return
	 */
	public static String queryIntermediaryAgreement(long id, long userId) {
		if(id < 1)
			return "";
		
		try {
			return t_bids.find("select intermediary_agreement from t_bids where id = ? and user_id = ?", id, userId).first();
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	/**
	 * 保障涵
	 * @param id
	 * @return
	 */
	public static String queryGuaranteeBid(long id, long userId) {
		if(id < 1)
			return "";
		
		try {
			return t_bids.find("select guarantee_bid from t_bids where id = ? and user_id = ?", id, userId).first();
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}
	
	/**
	 * 生成借款合同
	 */
	public boolean createPact(){
		/*1.生成借款合同*/
		TemplatePact pact = new TemplatePact();
		pact.id = Templets.BID_PACT_BID;
		String content = null;
		Date date = new Date();
		
		if(pact.is_use) {
			content = pact.content;
			
			if(StringUtils.isBlank(content))
				return false;
			
			BackstageSet set = BackstageSet.getCurrentBackstageSet();
			DecimalFormat myformat = new DecimalFormat();
			myformat.applyPattern("##,##0.00");
			String pact_no = this.id + DateUtil.simple(date);
			Object _amount = 0;
			
			String sql = "select sum(receive_corpus + receive_interest) from t_bill_invests where bid_id = ?";
			Query query = JPA.em().createNativeQuery(sql);
			query.setParameter(1, this.id);
			
			try {
				_amount = query.getSingleResult();
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("生成借款合同失败(查询借款信息)!" + e.getMessage());
				
				return false;
			}
			
			if(null == _amount)
				return false;
				
			String repayTime = this.isSecBid ? 
					           DateUtil.simple(date) : 
					           ServiceFee.repayTime(this.periodUnit, 
					           this.period, (int)this.repayment.id);
				
			content = content.replace("pact_no", pact_no).
					  replace("date", DateUtil.dateToString(date)).
					  replace("loan_name", this.user.realityName == null ? "" : this.user.realityName).
					  replace("id_number", this.user.idNumber == null ? "" : this.user.idNumber).
					  replace("company_name", set.companyName).
					  replace("purpose_name", this.purpose.name == null ? "" : this.purpose.name).
					  replace("amount", myformat.format(this.amount)).
					  replace("apr", this.apr + "").
					  replace("period", this.period + "").
					  replace("unit", this.strPeriodUnit).
					  replace("capital_interest_sum", _amount.toString()).
					  replace("repayment_name", this.repayment.name == null ? "" : this.repayment.name).
					  replace("repayment_time", repayTime);
			
			/* 查询借款信息，事务还没有提交，一定要用原生SQL查询  */
			/*
			 sql = "SELECT u.`name` AS user_name, "+
						 "t3.amount AS amount, "+
						 "t3.time AS invest_time, "+
						 "MAX(t.receive_time)AS receive_time, "+
						 "sum(t.receive_interest + t.receive_corpus + t.overdue_fine)AS total_amount "+
						 "FROM t_bill_invests t "+
						 "LEFT JOIN t_invests t3 ON t.bid_id = t3.bid_id AND t.user_id = t3.user_id "+
						 "LEFT JOIN t_users u ON u.id = t3.user_id "+
						 "WHERE t.bid_id = ? "+
						 "GROUP BY t.user_id";
			*/
		
			/* 1-16,问题：一个人对同一标投多次,生成合同的时候金额统计错误 */
			sql = "select u.`name`, i.amount, i.time, tmp.receive_time, tmp.total_amount "
					+ "from t_invests i join(select invest_id, receive_time, "
					+ "sum(receive_interest + receive_corpus + overdue_fine) as total_amount "
					+ "from t_bill_invests where bid_id = ? GROUP BY invest_id) tmp "
					+ "on i.id = tmp.invest_id join t_users u on i.user_id = u.id and i.bid_id = ?";
			
			
			query = JPA.em().createNativeQuery(sql);
			query.setParameter(1, this.id);
			query.setParameter(2, this.id);
			List<Object[]> lists = null;
			
			try {
				lists = query.getResultList();
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("生成借款合同失败(查询借款信息)!" + e.getMessage());
				
				return false;
			}
			
			if(null == lists || lists.size() == 0)
				return false;
			
			StringBuffer buffer = new StringBuffer("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
			buffer.append("<tr height=\"36\"><td>投资人名称</td><td>投资金额(RMB)</td><td>年利率</td><td>投资日期</td><td>还款截止日</td><td>本息合计总金额(RMB)</td></tr>");
			
			for (Object[] pa : lists) {
				buffer.append("<tr height=\"30\"><td>")
					  .append(pa[0])
					  .append("</td><td>")
				 	  .append(pa[1])
				 	  .append("</td><td>")
				 	  .append(this.apr)
				 	  .append("</td><td>")
				 	  .append(DateUtil.dateToString1((Date)pa[2]))
				 	  .append("</td><td>")
				 	  .append(DateUtil.dateToString1((Date)pa[3]))
				 	  .append("</td><td>")
				 	  .append(pa[4])
				  	  .append("</td></tr>");
			}
			
			buffer.append("</table>");
			content = content.replace("invest_list", buffer.toString());
			
			/* 查询还款信息，事务还没有提交，一定要用原生SQL查询  */
			sql = "SELECT (SELECT CONCAT(CONVERT(t.periods, char),'/',CONVERT(max(t2.periods), char)) as period FROM t_bills t2 WHERE t2.bid_id = t.bid_id) as period, "+ 
				  "t.repayment_time as repayment_time, "+
				  "t.repayment_corpus as repayment_corpus, "+
				  "t.repayment_interest as repayment_interest, "+
                  "(SELECT SUM(t2.repayment_corpus + t2.repayment_interest) as total_amount FROM t_bills t2 WHERE t2.bid_id = t.bid_id and t2.id = t.id) as perioda "+
				  "from t_bills t "+
				  "where t.bid_id = ?"; 
			
			query = JPA.em().createNativeQuery(sql);
			query.setParameter(1, this.id);
			
			try {
				lists = query.getResultList();
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("生成借款合同失败(查询还款信息)!" + e.getMessage());
				
				return false;
			}
			
			if(null == lists || lists.size() == 0)
				return false;
			
			buffer = new StringBuffer("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tbody>");
			buffer.append("<tr height=\"36\"><td>期数</td><td>年利率</td><td>应还时间</td><td>应还本金</td><td>还款利息</td><td>应还本息合计</td></tr>");
			
			for (Object[] pa : lists) {
				buffer.append("<tr height=\"30\"><td>")
					  .append(pa[0])
					  .append("</td><td>")
				 	  .append(this.apr)
				 	  .append("</td><td>")
				 	  .append(DateUtil.dateToString1((Date)pa[1]))
				 	  .append("</td><td>")
				 	  .append(pa[2])
				 	  .append("</td><td>")
				 	  .append(pa[3])
				 	  .append("</td><td>")
				 	  .append(pa[4])
				  	  .append("</td></tr>");
			}
			
		    buffer.append("</tbody></table>");
		    content = content.replace("repayment_list", buffer.toString());
			
			sql = "update from t_bids set pact = ? where id = ?";
			query = JPA.em().createQuery(sql);
			query.setParameter(1, content);
			query.setParameter(2, this.id);
			int row = 0;
			
			try {
				row = query.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("生成借款合同失败(修改)!" + e.getMessage());
				
				return false;
			}
			
			if(row < 1)
				return false;
			
			/*2.生成保障函(需先生成借款合同) */
			TemplatePact guarantee = new TemplatePact();
			guarantee.id = Templets.GUARANTEE_BID;
			
			DecimalFormat df = new DecimalFormat();
			df.applyPattern("###.00");
			String dxMoney = new CnUpperCaser(df.format(this.amount)).getCnString();
			
			if(guarantee.is_use) {
				content = guarantee.content;
				content = content.replace("loan_name", this.user.name)
								 .replace("loan_reality_name", this.user.realityName == null ? "" : this.user.realityName)
								 .replace("pact_no", pact_no)
								 .replace("chinese_amount", dxMoney)
								 .replace("period", this.period + this.strPeriodUnit)
						         .replace("company_name", set.companyName)
						         .replace("date", DateUtil.dateToString(date));
				
				sql = "update from t_bids set guarantee_bid = ? where id = ?";
				query = JPA.em().createQuery(sql);
				query.setParameter(1, content);
				query.setParameter(2, this.id);
				
				try {
					row = query.executeUpdate();
				} catch (Exception e) {
					e.printStackTrace();
					Logger.error("生成保障函失败(修改)!" + e.getMessage());
					
					return false;
				}
				
				if(row < 1)
					return false;
			}
		}
		
		/* 3.生成居间服务协议 */
		TemplatePact intermediary = new TemplatePact();
		intermediary.id = Templets.INTERMEDIARY_AGREEMENT_BID;
		
		if(intermediary.is_use){
			content = intermediary.content;
			content = content.replace("loan_name", this.user.name)
					  .replace("loan_reality_name", this.user.realityName == null ? "" : this.user.realityName)
					  .replace("id_number", this.user.idNumber == null ? "" : this.user.idNumber)
					  .replace("date", DateUtil.dateToString(date));
			
			String sql = "update from t_bids set intermediary_agreement = ? where id = ?";
			Query query = JPA.em().createQuery(sql);
			query.setParameter(1, content);
			query.setParameter(2, this.id);
			int row = 0;
			
			try {
				row = query.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("生成居间服务协议(修改)!" + e.getMessage());
				
				return false;
			}
			
			if(row < 1)
				return false;
		}

		return true;
	}

	/**
	 * 查询理财费率
	 * @param id
	 * @return
	 */
	public static double queryInvestRate(long id) {
		String sql = "select invest_rate from t_bids where id = ?";
		Double fee = null;
		
		try {
			fee = t_bids.find(sql, id).first();
		} catch (Exception e) {
			e.printStackTrace();
			
			return 0;
		}
		
		return fee == null ? 0 : fee;
	}
	
	/**
	 * 查询逾期费率
	 * @param id
	 * @return
	 */
	public static double queryOverdueRate(long id) {
		String sql = "select overdue_rate from t_bids where id = ?";
		Double fee = null;
		
		try {
			fee = t_bids.find(sql, id).first();
		} catch (Exception e) {
			e.printStackTrace();
			
			return 0;
		}
		
		return fee == null ? 0 : fee;
	}
	
	/**
	 * 根据理财ID查询费率
	 * @param id
	 * @param status 1:理财费率；2：逾期费率
	 * @return
	 */
	public static double queryRateByInvestId(long id, int status) {
		String sql = null;
		
		if(1 == status)
			sql = "select invest_rate from t_bids where id = (select bid_id from t_invests where id = ?)";
		else
			sql = "select overdue_rate from t_bids where id = (select bid_id from t_invests where id = ?)";
		
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, id);
		List<?> list = null;
		Object fee = null;
		
		try {
			list = query.getResultList();
			fee = list == null ? null : list.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			
			return 0;
		}
		
		if(null == fee)
			return 0;
		
		return Double.parseDouble(fee.toString());
	}
	
	/**
	 * 根据借款账单ID查询费率
	 * @param id
	 * @param status 1:理财费率；2：逾期费率
	 * @return
	 */
	public static double queryRateByBillId(long id, int status) {
		String sql = null;
		
		if(1 == status)
			sql = "select invest_rate from t_bids where id = (select bid_id from t_bills where id = ?)";
		else
			sql = "select overdue_rate from t_bids where id = (select bid_id from t_bills where id = ?)";
		
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, id);
		List<?> list = null;
		Object fee = null;
		
		try {
			list = query.getResultList();
			fee = list == null ? null : list.get(0);
		} catch (Exception e) {
			e.printStackTrace();
			
			return 0;
		}
		
		if(null == fee)
			return 0;
		
		return Double.parseDouble(fee.toString());
	}
	
	public static boolean queryIsRegisterGuarantor(long id) {
		try {
			return t_bids.find("select is_register_guarantor from t_bids where id = ?", id).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			
			return false;
		}
	}
	
	/**
	 * 查询标的信息用于投资
	 * @param bidId
	 * @param error
	 * @return
	 */
	public static Bid queryBidForInvest(long bidId, ErrorInfo error) {
		error.clear();
		String sql = "select id, user_id, bid_no, loan_purpose_id, amount, has_invested_amount, service_fees, "
				+ "apr from t_bids where id = ? limit 1";
		Object[] obj = null;
		try {
			obj = (Object[]) JPA.em().createNativeQuery(sql).setParameter(1, bidId).getSingleResult();
		} catch (Exception e) {
			Logger.error("查询标的信息失败：%s", e.getMessage());
			error.code = -1;
			error.msg = "查询标的信息失败";
			
			return null;
		}
		
		if(obj == null || obj.length < 8) {
			error.code = -1;
			error.msg = "查询标的信息失败";
			
			return null;
		}
		
		Bid bid = new Bid();
		
		bid.id = ((BigInteger) obj[0]).longValue();
		bid.userId = ((BigInteger) obj[1]).longValue();
		bid.bidNo =  obj[2] == null ? null : obj[2].toString();
		bid.purpose.id = ((Integer) obj[3]).longValue();
		bid.amount =  ((BigDecimal) obj[4]).doubleValue();
		bid.hasInvestedAmount = ((BigDecimal) obj[5]).doubleValue();
		bid.serviceFees = ((BigDecimal) obj[6]).doubleValue();
		bid.apr = ((BigDecimal) obj[7]).doubleValue();
		
		error.code = 1;
		
		return bid;
	}

	/**
	 * 累计成交额
	 *
	 * @param error
	 * @return
	 */
	public static double findTotalVolumeOfBids(ErrorInfo error) {
		error.clear();
		try {
			String sql = " select sum(b.amount) from t_bids b where b.amount = b.has_invested_amount";
			BigDecimal totalVolume = (BigDecimal) JPA.em().createNativeQuery(sql).getSingleResult();
			return formatMillon((totalVolume == null ? 0 : totalVolume.doubleValue()) + Constants.BASE_TOTAL_VOLUME);
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起！系统异常！请您联系平台管理员！";
			error.code = -2;
		}
		return 0;
	}

	public static double findLoanLossProvision(ErrorInfo error) {
		error.clear();
		try {
			String sql = " select " +
					"        sum(CASE" +
					"            WHEN (b.period_unit = 1)" +
					"            THEN b.amount * b.apr/100 * b.period/365" +
					"            WHEN (b.period_unit = 0)" +
					"            THEN b.amount * b.apr/100 * b.period/12" +
					"            WHEN (b.period_unit = -1)" +
					"            THEN b.amount * b.apr/100 * b.period/1" +
					"            ELSE 0" +
					"        END) from t_bids b where b.amount = b.has_invested_amount";
			BigDecimal loanLossProvision = (BigDecimal) JPA.em().createNativeQuery(sql).getSingleResult();
			return formatMillon((loanLossProvision == null ? 0 : loanLossProvision.doubleValue()) + Constants.BASE_TOTAL_VOLUME * 0.09 * 6 / 12);
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起！系统异常！请您联系平台管理员！";
			error.code = -2;
		}
		return 0;
	}

	/**
	 * 取万元
	 * @param value
	 * @return
	 */
	private static double formatMillon(double value) {
		BigDecimal bigDecimal = new BigDecimal(value / 10000);
		return bigDecimal.setScale(0, BigDecimal.ROUND_CEILING).doubleValue();
	}
}