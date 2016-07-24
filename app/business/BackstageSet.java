package business;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang.StringUtils;
import constants.Constants;
import constants.OptionKeys;
import constants.SupervisorEvent;
import models.t_dict_ad_citys;
import models.t_system_options;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.RegexUtils;

public class BackstageSet implements Serializable{

	public Date platformStartupTime;//平台启动时间
	public long platformRunDays;//平台运行天数
	
	public String serviceEmailServer;
	public String serviceEmailAccount;
	public String serviceEmailPassword;
	
	public double initialAmount;//初始信用额度
	public int normalPayPoints;//正常还款积分
	public int fullBidPoints;//成功借款积分
	public int investpoints;//成功投标积分
	public int moneyToSystemScore;//每投标1元积多少分
	public int overDuePoints;//账单逾期扣分（负数表示扣分）
	public double creditToMoney;//信用积分兑换信用额度
	
	public double borrowFee;//借款管理费本金百分比
	public double borrowFeeDay;//天标管理费本金百分比
	public int borrowFeeMonth;//借款管理费月
	public double borrowFeeRate;//借款管理费利率
	public double investmentFee;//理财管理费
	public double debtTransferFee;//债权转让管理费
	public double overdueFee;//逾期管理费
	public double withdrawFee;//提现管理费x1基础金额（x1基础金额,x2超出金额收取的百分比）
	public double withdrawRate;//x2超出金额收取的百分比
	public double rechargeFee;//充值手续费
	public double vipFee;//VIP服务费
	public int vipTimeType;//VIP服务时间类型(年 0, 月 1)
	public int vipTimeLength;//VIP服务时长(单位[年,月]由vipTimeType确定)
	public int vipMinTimeType;//VIP最少开通时间类型(年 0, 月 1)
	public int vipMinTimeLength;//VIP最少开通时长
	public int vipDiscount;//VIP折扣
	public int vipAuditPeriod;//VIP审核周期
	
	public String investIntegration;//理财积分
	public String repayType;//应付账单还款方式
	public String platformName;//平台名称
	public String platformLogoFilename;//LOGO路径
	public String companyName;//公司名称
	public String companyProvince;//省
	public String companyCity;//市
	public String companyAddress;//详细地址
	public String companyTelephone;//联系电话
	public String contactMobile1;//手机1
	public String contactMobile2;//手机2
	public String companyFax;//传真
	public String companyEmail;//邮箱
	public String companyContact_name;//联系人
	public String companyQQ1;//联系QQ1
	public String companyQQ2;//联系QQ2
	public String siteIcpNumber;//网站备案号
	public String platformTelephone;//客服电话
	public String workStatrTime;//工作开始时间
	public String workEndTime;//工作结束时间
	
	public String lableBeginnerIntroduction;//新手入门
	public String lableAboutLoan;//我要借款
	public String lableAboutFinancing;//我要理财
	public String lableAboutUs;//关于我们
	public String lableHelpCentre;//帮助中心
	public String lableCustomerSupport;//客服与支持
	
	public String paymentId;//支付方式id
	public String auditMechanism;//审核机制(0.先审后发；1.先发后审；2.边发边审)
	public String audit_mode;//边发边审的模式(0,全部,1.必须,2.可选)
	public String audit_scale;//提交资料的比例
	
	public String loanNumber;//借款标编号代码
	public String loanBillNumber;//借款账单编号代码
	public String investsBillNumber;//理财账单编号代码
	public String agenciesNumber;//债权转让编号代码
	public String auditItemNumber;//审计资料编号代码
	
	public String smsAccount;//短信通道账号
	public String smsPassword;//短信通道密码
	public String mailAccount;//邮件通道账号
	public String mailPassword;//邮件通道密码
	public String emailWebsite;//邮箱登录网址
	public String POP3Server;//POP3服务器
	public String STMPServer;//STMP服务器
	public String isChargesChannels;//是否开启收费邮件广告通道
	
	public String seoTitle;//SEO标题
	public String seoDescription;//SEO描述
	public String seoKeywords;//SEO关键词
	
	public String versionName;
	public String version;
	public String dbVersion;
	
	public int cpsRewardType;//结算方式（1 按会员数；2 按交易额）
	public double rewardForCounts;//按会员数，每个的金额
	public double rewardForRate;//按交易额的比例
	
	public int isOpenPasswordErrorLimit;//是否启用密码错误次数超限锁定(0不启用，1启用)
	public int passwordErrorCounts;//密码连续错误次数时锁定用户
	public int lockingTime;//密码连续错误次数时锁定用户时长
	public String isOpenKeywordLimit;//是否启用用户名注册关键字安全设置
	
	public String userNameMinLength;//最小用户名长度
	public String userNameMaxLength;//最大用户名长度
	public String passwordMinLength;//最小密码名长度
	public String passwordMaxLength;//最小密码长度
	public String password_regex;//密码复杂度正则表达式
	
	public String keywords;//注册关键字否定词
	
	public String baiduAccount;//百度流量统计账号
	public String baiduPassword;//百度流量统计密码
	public String baiduCode;//百度流量统计代码
	
	public String companyNameService;//公司名称（后台）
	public String companyProvinceService;//省
	public String companyCityeService;//市
	public String companyDomain;//域名
	public String registerCode;//注册码
	public String supervisorPlatformLog; //管理者平台LOG
	public String entrustVersion; //资金托管版本
	
	public String androidVersion; //android版本号
	public String androidCode; //android编号
	public String iosVersion; //ios版本号
	public String iosCode; //ios编号
	public String iosMsg; //ios升级信息
	public String androidMsg; //android升级信息
	
	static {
		BackstageSet backstageSet = BackstageSet.queryBackstageSet();
		setCurrentBackstageSet(backstageSet);
		
		BottomLinks.setCurrentLinks();
		User.queryBasic();
	}
	
	public static void setCurrentBackstageSet(BackstageSet backstageSet) {
		
		Cache.set("backstageSet", backstageSet);
	}
	
	public static BackstageSet getCurrentBackstageSet() {
		BackstageSet backstageSet = (BackstageSet) Cache.get("backstageSet");
		
		if(backstageSet == null || StringUtils.isBlank(backstageSet.keywords)) {
			backstageSet = BackstageSet.queryBackstageSet();
		}
		
		return backstageSet;
	}
	
	
	/**
	 * 平台运行天数
	 * @return
	 */
	public long getPlatformRunDays() {
		if (this.platformStartupTime == null) {
			try {
				platformStartupTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(OptionKeys.getvalue(OptionKeys.PLATFORM_STARTUP_TIME, new ErrorInfo()));
			} catch (ParseException e) {
				Logger.error("查询平台启动时间出现异常：", e.getMessage());
				
				return 0;
			}
		}
		
		return DateUtil.diffDays(platformStartupTime, new Date());
	}

	/**
	 * 根据市id获取省
	 * @return
	 */
	public static long getProvince(String cityId, ErrorInfo error) {
		t_dict_ad_citys citys = null;
		if(cityId == null){
			return 0;
		}
		try {
		     citys = t_dict_ad_citys.findById(Long.parseLong(cityId));
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("根据市id获取省时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致根据市id获取省失败！";
			
		}
		
		error.code = 0;
		
		return citys.province_id;
	}
	
	/**
	 * 平台服务费设置
	 * @param error
	 * @return -1失败  0成功
	 */
	public int setPlatformFee(ErrorInfo error){
		
		error.clear();
		
		int rows = 0;
		String sql = "update t_system_options set _value = case _key " +
                   " when 'borrow_fee' then ?" +
                   " when 'borrow_fee_month' then ?" +
                   " when 'borrow_fee_rate' then ?" +
                   " when 'investment_fee' then ?" +
                   " when 'debt_transfer_fee' then ?" + 
                   " when 'overdue_fee' then ?" +
                   " when 'withdraw_fee' then ?" +
                   " when 'withdraw_fee_rate' then ?" +
                   " when 'vip_fee' then ?" +
                   " when 'vip_time_type' then ?" +
                   " when 'vip_min_time_type' then ?" +
                   " when 'vip_min_time_lenght' then ?" +
                   " when 'vip_discount' then ?" +
                   " when 'vip_audit_period' then ?" +
                   " when 'borrow_fee_day' then ?" +
                   " end where id in(2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2010, 2011, 2013, 2014, 2015, 2016, 9001)";
		try {
			rows = JPA.em().createNativeQuery(sql).setParameter(1, this.borrowFee)
				   .setParameter(2, this.borrowFeeMonth).setParameter(3, this.borrowFeeRate).setParameter(4, this.investmentFee).setParameter(5, this.debtTransferFee)
				   .setParameter(6, this.overdueFee).setParameter(7, this.withdrawFee).setParameter(8, this.withdrawRate)
				   .setParameter(9, this.vipFee).setParameter(10, this.vipTimeType).setParameter(11, this.vipMinTimeType)
				   .setParameter(12, this.vipMinTimeLength).setParameter(13, this.vipDiscount).setParameter(14, this.vipAuditPeriod)
				   .setParameter(15, this.borrowFeeDay).executeUpdate();
			
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改平台服务费设置时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致平台服务费设置失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.FEE_SETTINT, 
				"修改服务费设置", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		BackstageSet currentBackstageSet = BackstageSet.getCurrentBackstageSet();
		currentBackstageSet.borrowFee = borrowFee;
		currentBackstageSet.borrowFeeDay = borrowFeeDay;
		currentBackstageSet.borrowFeeMonth = borrowFeeMonth;
		currentBackstageSet.borrowFeeRate = borrowFeeRate;
		currentBackstageSet.investmentFee = investmentFee;
		currentBackstageSet.debtTransferFee = debtTransferFee;
		currentBackstageSet.overdueFee = overdueFee;
		currentBackstageSet.withdrawFee = withdrawFee;
		currentBackstageSet.withdrawRate = withdrawRate;
		currentBackstageSet.vipFee = vipFee;
		currentBackstageSet.vipTimeType = vipTimeType;
		currentBackstageSet.vipMinTimeType = vipMinTimeType;
		currentBackstageSet.vipMinTimeLength = vipMinTimeLength;
		currentBackstageSet.vipDiscount = vipDiscount;
		currentBackstageSet.vipAuditPeriod = vipAuditPeriod;
		setCurrentBackstageSet(currentBackstageSet);
		
		error.code = 0;
		error.msg = "平台服务费设置成功";
		
		return 0;
	}
	
	/**
	 * 信用积分规则设置
	 * @param error
	 * @return-1失败  0成功
	 */
	public int creditRuleSet(ErrorInfo error){
		
		error.clear();
		
		int rows = 0;
		
		try {
			rows = JPA.em().createNativeQuery("update t_system_options set _value = case _key " +
					"when 'credit_initial_amount' then ?" + 
				   " when 'normal_repayment_score' then ?" + 
				   " when 'successful_borrow_score' then ?" +
				   " when 'successful_invest_score' then ?" +
				   " when 'overdue_bill_score' then ?" +
				   " when 'credit_limit' then ?" +
				   " end where id in(3006, 3001, 3002, 3003, 3004, 3005)").setParameter(1, this.initialAmount).setParameter(2, this.normalPayPoints)
				   .setParameter(3, this.fullBidPoints).setParameter(4, this.investpoints).setParameter(5, this.overDuePoints)
				   .setParameter(6, this.creditToMoney).executeUpdate();
			
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改信用规则设置时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致修改信用规则设置失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.EDIT_CREDIT_SCORE_RULE, "设置信用积分规则", error);
		
		if (error.code < 0) {
			return error.code;
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		backstageSet.initialAmount = initialAmount;
		backstageSet.normalPayPoints = normalPayPoints;
		backstageSet.fullBidPoints = fullBidPoints;
		backstageSet.investpoints = investpoints;
		backstageSet.overDuePoints = overDuePoints;
		backstageSet.creditToMoney = creditToMoney;
		setCurrentBackstageSet(backstageSet);
		
		error.code = 0;
		error.msg = "信用规则设置成功";
		
		return 0;
		
	}
	
	/**
	 * 系统积分规则设置
	 * @param error
	 * @return
	 */
	public int setSystemScoreRule(ErrorInfo error) {
		error.clear();
		
		int rows = 0;
		
		try {
			rows = JpaHelper.execute("update t_system_options set _value = ? where _key = 'money_to_system_score'", this.moneyToSystemScore+"").executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("系统积分规则设置时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致系统积分规则设置失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.SCORE_RULE, 
				"修改积分规则设置", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		backstageSet.moneyToSystemScore = moneyToSystemScore;
		setCurrentBackstageSet(backstageSet);
		
		error.code = 0;
		error.msg = "系统积分规则设置成功";
		
		return error.code;
	}
	
	/**
	 * 理财积分设置
	 * @param error
	 * @return -1失败  0成功
	 */
	public int investIntegrationSet(ErrorInfo error){
		error.clear();
		
		if (!RegexUtils.isNumber(this.investIntegration)) {
			error.code = -1;
			error.msg = "理财积分必须是数字";
			
			return error.code;
		}
		
		int rows = 0;
		
		try {
			rows = JpaHelper.execute("update t_system_options set _value = ? where _key = 'invest_integration'",this.investIntegration).executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改理财积分设置时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致修改理财积分设置失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		error.code = 0;
		
		return 0;
	}
	
	/**
	 * 应付账单还款方式
	 * @param error
	 * @return -1失败  0成功
	 */
	public int setBillsRepayType(ErrorInfo error){
		error.clear();
		
		if (!RegexUtils.isNumber(this.repayType)) {
			error.code = -1;
			error.msg = "付款方式必须是数字";
			
			return error.code;
		}
		
		int rows = 0;
		
		try {
			rows = JpaHelper.execute("update t_system_options set _value = ? where _key = 'bill_repayment_type'",this.repayType).executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改应付账单还款方式时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致修改应付账单还款方式失败！";
			
			return error.code; 
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.REPAY_TYPE, 
				"修改应付账单设置", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		backstageSet.repayType = repayType;
		setCurrentBackstageSet(backstageSet);
		
		error.code = 0;
		error.msg = "应付账单还款方式设置成功";
		
		return 0;
	}
	
	/**
	 * 网贷软件系统基本资料设置
	 * @param error
	 * @return -1失败  0成功
	 */
	public int plateDataSet(ErrorInfo error){
		error.clear();
		
		EntityManager em = JPA.em();
		
		String sql ="update t_system_options set _value = case _key " +
				   "when 'platform_name' then ?" +
				   " when 'platform_logo_filename' then ?" + 
				   " when 'company_name' then ?" +
				   " when 'company_city' then ?" +
				   " when 'company_address' then ?" +
				   " when 'company_telephone' then ?" +
				   " when 'contact_mobile1' then ?" +
				   " when 'contact_mobile2' then ?" +
				   " when 'company_fax' then ?" +
				   " when 'company_email' then ?" +
				   " when 'company_contact_name' then ?" +
				   " when 'company_qq1' then ?" +
				   " when 'company_qq2' then ?" +
				   " when 'site_icp_number' then ?" +
				   " when 'platform_telephone' then ?" +
				   " when 'work_start_time' then ?" +
				   " when 'work_end_time' then ?" +
				   " when 'supervisor_platform_log' then ?" +
				   " end where id in(100, 101, 102, 103,"
				   + " 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 7008)";
		
		Query query = em.createNativeQuery(sql);
					query.setParameter(1, this.platformName).setParameter(2, this.platformLogoFilename)
					.setParameter(3, this.companyName)
					.setParameter(4, this.companyCity).setParameter(5, this.companyAddress)
					.setParameter(6, this.companyTelephone).setParameter(7, this.contactMobile1)
					.setParameter(8, this.contactMobile2).setParameter(9, this.companyFax)
					.setParameter(10, this.companyEmail).setParameter(11, this.companyContact_name)
				    .setParameter(12, this.companyQQ1).setParameter(13, this.companyQQ2)
				    .setParameter(14, this.siteIcpNumber).setParameter(15, this.platformTelephone)
				    .setParameter(16, this.workStatrTime).setParameter(17, this.workEndTime)
				    .setParameter(18, this.supervisorPlatformLog);
		
		
		/*String sql ="update t_system_options set _value = case _key " +
		   "when 'platform_name' then ?" +
		   " when 'platform_logo_filename' then ?" + 
		   " when 'company_name' then ?" +
		   " when 'company_city' then ?" +
		   " when 'company_address' then ?" +
		   " when 'company_qq1' then ?" +
		   " when 'company_qq2' then ?" +
		   " when 'site_icp_number' then ?" +
		   " when 'platform_telephone' then ?" +
		   " when 'work_start_time' then ?" +
		   " when 'work_end_time' then ?" +
		   " when 'supervisor_platform_log' then ?" +
		   " end where id in(100, 101, 102, 103,"
		   + " 105, 106, 107, 108, 109, 110, 111, " +
		   		"112, 113, 114, 115, 116, 117, 7008)";

		Query query = em.createNativeQuery(sql);
			query.setParameter(1, this.platformName)
			.setParameter(2, this.platformLogoFilename)
			.setParameter(3, this.companyName)
			.setParameter(4, this.companyCity)
			.setParameter(5, this.companyAddress)
		    .setParameter(6, this.companyQQ1)
		    .setParameter(7, this.companyQQ2)
		    .setParameter(8, this.siteIcpNumber)
		    .setParameter(9, this.platformTelephone)
		    .setParameter(10, this.workStatrTime)
		    .setParameter(11, this.workEndTime)
			.setParameter(12, this.supervisorPlatformLog);*/
			
		int rows = 0;
					
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改网贷软件系统基本资料设置时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致修改网贷软件系统基本资料设置失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.PLAT_DATA, 
				"修改系统基本资料", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		backstageSet.platformName = this.platformName;
		backstageSet.platformLogoFilename = this.platformLogoFilename;
		backstageSet.companyName = this.companyName;
		backstageSet.companyCity = this.companyCity;
		backstageSet.companyAddress = this.companyAddress;
		backstageSet.companyTelephone = this.companyTelephone;
		backstageSet.contactMobile1 = this.contactMobile1;
		backstageSet.contactMobile2 = this.contactMobile2;
		backstageSet.companyFax = this.companyFax;
		backstageSet.companyEmail = this.companyEmail;
		backstageSet.companyContact_name = this.companyContact_name;
		backstageSet.companyQQ1 = this.companyQQ1;
		backstageSet.companyQQ2 = this.companyQQ2;
		backstageSet.siteIcpNumber = this.siteIcpNumber;
		backstageSet.platformTelephone = this.platformTelephone;
		backstageSet.workStatrTime = this.workStatrTime;
		backstageSet.workEndTime = this.workEndTime;
		backstageSet.supervisorPlatformLog = this.supervisorPlatformLog;
		setCurrentBackstageSet(backstageSet);
		
		error.code = 0;
		error.msg = "系统基本资料修改成功！";
		
		return 0;
	}
	
	/**
	 * 资金托管账户设置(该方法暂未使用)
	 * @param error
	 * @return -1失败  0成功
	 */
	public int fundEscrowAccount(ErrorInfo error){
		error.clear();
		
		int rows = 0;
		
		try {
			JpaHelper.execute("update t_dict_payment_gateways set is_use = ?",1).executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改资金托管账户设置时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致修改资金托管账户设置失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		try {
			rows = JpaHelper.execute("update t_dict_payment_gateways set is_use = 0 where id = ?",this.paymentId).executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改资金托管账户设置时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致修改资金托管账户设置失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		error.code = 0;
		
		return 0;
	}
	
	/**
	 * 支付方式(待定)
	 * @param error
	 * @return -1失败  0成功
	 */
	public int paymentWays(ErrorInfo error){
		
		return 0;
	}
	
	/**
	 * 短信通道设置
	 * @param error
	 * @return -1失败  0成功
	 */
	public int SMSChannels(ErrorInfo error){
		error.clear();
		
		int rows = 0;
		
		try {
			rows = JPA.em().createNativeQuery("update t_system_options set _value = case _key " +
				   "when 'service_sms_account' then ?" +
				   " when 'service_sms_password' then ?" + 
				   " end where id in(4, 5)").setParameter(1, this.smsAccount)
				   .setParameter(2, this.smsPassword).executeUpdate();
			
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改短信通道设置时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致修改短信通道设置失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.SMS_CHANNEL, 
				"修改短信通道设置", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		backstageSet.smsAccount = this.smsAccount;
		backstageSet.smsPassword = this.smsPassword;
		
		error.code = 0;
		error.msg = "修改短信通道设置成功！";
		
		return 0;
	}
	
	/**
	 * 邮件通道设置
	 * @param error
	 * @return -1失败  0成功
	 */
	public int MAILChannels(ErrorInfo error){
		error.clear();
		
		if(StringUtils.isBlank(emailWebsite)) {
			error.code = -1;
			error.msg = "请输入邮箱登录网址";
			
			return error.code;
		}
		
		if(StringUtils.isBlank(mailAccount)) {
			error.code = -1;
			error.msg = "请填写邮箱地址";
			
			return error.code;
		}
		
		if(!RegexUtils.isEmail(mailAccount)) {
			error.code = -1;
			error.msg = "请填写正确的邮箱地址";
			
			return error.code;
		}
		
		if(StringUtils.isBlank(mailPassword)) {
			error.code = -1;
			error.msg = "请填写邮箱密码";
			
			return error.code;
		}
		
		if(StringUtils.isBlank(POP3Server) || StringUtils.isBlank(STMPServer)) {
			error.code = -1;
			error.msg = "请填写邮箱服务器地址";
			
			return error.code;
		}
		
		Query query = JPA.em().createNativeQuery("update t_system_options set _value = case _key " +
				   "when 'service_mail_account' then ?" +
				   " when 'service_mail_password' then ?" + 
				   " when 'email_website' then ?" + 
				   " when 'POP3_server' then ?" + 
				   " when 'STMP_server' then ?" + 
				   " when 'is_charges_channels' then ?" + 
				   " end where id in(6, 7, 8, 9, 10, 11)").setParameter(1, this.mailAccount).setParameter(2, this.mailPassword)
				   .setParameter(3, this.emailWebsite).setParameter(4, this.POP3Server)
				   .setParameter(5, this.STMPServer).setParameter(6, this.isChargesChannels);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改邮件通道设置时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致修邮件通道设置失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.MAIL_CHANNEL, 
				"修改邮件通道设置", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		backstageSet.emailWebsite = emailWebsite;
		backstageSet.mailAccount = mailAccount;
		backstageSet.mailPassword = mailPassword;
		backstageSet.STMPServer = STMPServer;
		backstageSet.POP3Server = POP3Server;
		backstageSet.isChargesChannels = isChargesChannels;
		setCurrentBackstageSet(backstageSet);
		
		Play.configuration.setProperty("mail.smtp.host",backstageSet.emailWebsite);
	    Play.configuration.setProperty("mail.smtp.user",backstageSet.mailAccount);
	    Play.configuration.setProperty("mail.smtp.pass",backstageSet.mailPassword);
	    
	    error.code = 0;
		error.msg = "修改邮件通道设置成功！";
		
		return 0;
	}
	
	/**
	 * SEO优化设置
	 * @param error
	 * @return -1失败  0成功
	 */
	public int SEOSet(ErrorInfo error){
		error.clear();
		
		if(StringUtils.isBlank(seoTitle)) {
			error.code = -1;
			error.msg = "请输入首页标题";
			
			return error.code;
		}
		
		if(StringUtils.isBlank(seoDescription)) {
			error.code = -1;
			error.msg = "请输入描述内容";
			
			return error.code;
		}
		
		if(StringUtils.isBlank(seoKeywords)) {
			error.code = -1;
			error.msg = "请设置关键字";
			
			return error.code;
		}
		
		Query query = JPA.em().createNativeQuery("update t_system_options set _value = case _key " +
				   "when 'seo_title' then ?" +
				   " when 'seo_description' then ?" + 
				   " when 'seo_keywords' then ?" +
				   " end where id in(701, 702, 703)").setParameter(1, this.seoTitle).setParameter(2, this.seoDescription)
				   .setParameter(3, this.seoKeywords);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改SEO优化设置时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致修改SEO优化设置失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.SEC, 
				"修改SEO优化设置", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		backstageSet.seoTitle = seoTitle;
		backstageSet.seoDescription = seoDescription;
		backstageSet.seoKeywords = seoKeywords;
		setCurrentBackstageSet(backstageSet);
		
		error.code = 0;
		error.msg = "SEO优化设置保存成功！";
		
		return 0;
	}
	
	/**
	 * CPS推广规则设置
	 * @param error
	 * @return -1失败  0成功
	 */
	public int CPSPromotion(ErrorInfo error){
		error.clear();
		
		if(cpsRewardType != 1 && cpsRewardType != 2) {
			error.code = -1;
			error.msg = "请选择推广设置";
			
			return error.code;
		}
		
		if(cpsRewardType == 1) {
			if(rewardForCounts == 0) {
				error.code = -1;
				error.msg = "请输入有效会员奖金";
				
				return error.code;
			}
		}else if(cpsRewardType == 2) {
			if(rewardForRate < 0 || rewardForRate > 100) {
				error.code = -1;
				error.msg = "管理费结算比例有误，有效范围0~100";
				
				return error.code;
			}
		}
		
		java.text.DecimalFormat dFormat =new   java.text.DecimalFormat("#.##");
		EntityManager em = JPA.em();
		
		String sql = null;
		Query query = null;
		if(cpsRewardType == 1) {
			sql = "update t_system_options set _value = case _key " +
					   "when 'cps_reward_type' then ?" +
					   " when 'cps_reward_type_1' then ?"+ 
					   " end where id in(4001, 4002)";
			
			query = em.createNativeQuery(sql).setParameter(1, this.cpsRewardType)
					.setParameter(2, dFormat.format(this.rewardForCounts));
		}
		
		if(cpsRewardType == 2) {
			sql = "update t_system_options set _value = case _key " +
					   "when 'cps_reward_type' then ?" +
					   " when 'cps_reward_type_2' then ?"+ 
					   " end where id in(4001, 4003)";
			
			query = em.createNativeQuery(sql).setParameter(1, this.cpsRewardType)
					.setParameter(2, dFormat.format(this.rewardForRate));
		}
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改CPS推广规则设置时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致修改CPS推广规则设置失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.CPS_SETTING, 
				"修改cps推广设置", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		if(cpsRewardType == 1) {
			backstageSet.cpsRewardType = cpsRewardType;
			backstageSet.rewardForCounts = Double.parseDouble(dFormat.format(this.rewardForCounts));
		}
		
		if(cpsRewardType == 2) {
			backstageSet.cpsRewardType = cpsRewardType;
			backstageSet.rewardForRate = Double.parseDouble(dFormat.format(this.rewardForRate));
		}
		
		setCurrentBackstageSet(backstageSet);
		
		error.code = 0;
		error.msg = "推广规则设置保存成功！";
		
		return 0;
	}
	
	/**
	 * 系统安全参数设置
	 * @param error
	 * @return
	 */
	public int editSystemParameter(ErrorInfo error){
		error.clear();
		
		EntityManager em = JPA.em();
		
		String sql = null;
		Query query = null;
		
		if(isOpenPasswordErrorLimit == Constants.OPEN_LOCK) {
			sql = "update t_system_options set _value = case _key " +
					   "when 'isopen_password_error_limit' then ?" +
					   " when 'security_lock_at_password_continuous_errors' then ?" + 
					   " when 'security_lock_time' then ?" +
					   " when 'register_neg' then ?" + 
					   " end where id in(5000, 5001, 5002, 5009)";
			
			query = em.createNativeQuery(sql).setParameter(1, this.isOpenPasswordErrorLimit)
					.setParameter(2, this.passwordErrorCounts).setParameter(3, this.lockingTime)
					.setParameter(4, this.keywords);
		}
		
		if(isOpenPasswordErrorLimit == Constants.CLOSE_LOCK) {
			sql = "update t_system_options set _value = case _key " +
					   "when 'isopen_password_error_limit' then ?" +
					   " when 'register_neg' then ?" + 
					   " end where id in(5000, 5009)";
			
			query = em.createNativeQuery(sql).setParameter(1, this.isOpenPasswordErrorLimit)
					.setParameter(2, this.keywords);
		}
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改系统安全参数设置时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致修改系统安全参数设置失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.SAFE_PARAMETER, 
				"修改系统安全参数设置", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		if(isOpenPasswordErrorLimit == Constants.OPEN_LOCK) {
			backstageSet.isOpenPasswordErrorLimit = isOpenPasswordErrorLimit;
			backstageSet.passwordErrorCounts = passwordErrorCounts;
			backstageSet.lockingTime = lockingTime;
			backstageSet.keywords = keywords;
		}else{
			backstageSet.isOpenPasswordErrorLimit = isOpenPasswordErrorLimit;
			backstageSet.keywords = keywords;
		}
		
		setCurrentBackstageSet(backstageSet);
		
		error.code = 0;
		error.msg = "安全参数设置成功！";
		
		return 0;
	}
	
	/**
	 * 软件授权
	 * @param error
	 * @return
	 */
	public int authorize(ErrorInfo error){
		error.clear();
		
		EntityManager em = JPA.em();
		
		String sql = "update t_system_options set _value = case _key " +
				   "when 'company_name_service' then ?" +
				   " when 'company_province_service' then ?" + 
				   " when 'company_city_service' then ?" +
				   " when 'company_domain' then ?" + 
				   " when 'register_code' then ?" + 
				   " end where id in(7001, 7002, 7003, 7004, 7005)";
		
		Query query = em.createNativeQuery(sql).setParameter(1, this.companyNameService)
					.setParameter(2, this.companyProvinceService).setParameter(3, this.companyCityeService)
					.setParameter(4, this.companyDomain).setParameter(5, this.registerCode);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("正版授权时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，正版授权保存失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.AUTHORIZE, 
				"正版授权", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		
		backstageSet.companyNameService = companyNameService;
		backstageSet.companyProvinceService = companyProvinceService;
		backstageSet.companyCityeService = companyCityeService;
		backstageSet.companyDomain = companyDomain;
		backstageSet.registerCode = registerCode;
		
		setCurrentBackstageSet(backstageSet);
		
		error.code = 0;
		error.msg = "正版授权保存成功！";
		
		return 0;
	}
	
	/**
	 * 查询系统设置
	 * @return
	 */
	public static BackstageSet queryBackstageSet() {
		List<t_system_options> options = new ArrayList<t_system_options>();
		
		try{
			options = t_system_options.find("order by id").fetch();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询系统设置时："+e.getMessage());
			
			return null;
		}
		
		BackstageSet backstageSet = new BackstageSet();
		
		backstageSet.serviceEmailServer = options.get(0)._value;
		backstageSet.serviceEmailAccount = options.get(1)._value;
		backstageSet.serviceEmailPassword = options.get(2)._value;
		
		backstageSet.smsAccount = options.get(3)._value;//短信通道账号
		backstageSet.smsPassword = options.get(4)._value;//短信通道密码
		backstageSet.mailAccount = options.get(5)._value;//邮件通道账号
		backstageSet.mailPassword = options.get(6)._value;//邮件通道密码
		backstageSet.emailWebsite = options.get(7)._value;//邮箱登录网址
		backstageSet.POP3Server = options.get(8)._value;//POP3服务器
		backstageSet.STMPServer = options.get(9)._value;//STMP服务器
		backstageSet.isChargesChannels = options.get(10)._value;//是否开启收费邮件广告通道
		
		backstageSet.platformTelephone = options.get(11)._value;//客服电话
		backstageSet.platformName = options.get(12)._value;//平台名称
		backstageSet.platformLogoFilename = options.get(13)._value;//LOGO路径
		backstageSet.companyName = options.get(14)._value;//公司名称
		backstageSet.companyProvince = options.get(15)._value;//省
		backstageSet.companyCity = options.get(16)._value;//市
		backstageSet.companyAddress = options.get(17)._value;//详细地址
		backstageSet.companyTelephone = options.get(18)._value;//联系电话
		backstageSet.contactMobile1 = options.get(19)._value;//手机1
		backstageSet.contactMobile2 = options.get(20)._value;//手机2
		backstageSet.companyFax = options.get(21)._value;//传真
		backstageSet.companyEmail = options.get(22)._value;//邮箱
		backstageSet.companyContact_name = options.get(23)._value;//联系人
		backstageSet.companyQQ1 = options.get(24)._value;//联系QQ1
		backstageSet.companyQQ2 = options.get(25)._value;//联系QQ2
		backstageSet.siteIcpNumber = options.get(26)._value;//网站备案号
		backstageSet.workStatrTime = options.get(27)._value;//工作开始时间
		backstageSet.workEndTime = options.get(28)._value;//工作结束时间
		
		backstageSet.lableBeginnerIntroduction = options.get(29)._value;//新手入门
		backstageSet.lableAboutLoan = options.get(30)._value;//我要借款
		backstageSet.lableAboutFinancing = options.get(31)._value;//我要理财
		backstageSet.lableAboutUs = options.get(32)._value;//关于我们
		backstageSet.lableHelpCentre = options.get(33)._value;//帮助中心
		backstageSet.lableCustomerSupport = options.get(34)._value;//客服与支持
		
		backstageSet.repayType = options.get(35)._value;//账单返款方式(0.自动付款；1.手动付款)
		backstageSet.paymentId = options.get(36)._value;//支付方式id
		backstageSet.auditMechanism = options.get(37)._value;//审核机制(0.先审后发；1.先发后审；2.边发边审)
		backstageSet.audit_mode = options.get(38)._value;//边发边审的模式(0,全部,1.必须,2.可选)
		backstageSet.audit_scale = options.get(39)._value;//提交资料的比例
		
		backstageSet.loanNumber = options.get(40)._value;//借款标编号代码
		backstageSet.loanBillNumber = options.get(41)._value;//借款账单编号代码
		backstageSet.investsBillNumber = options.get(42)._value;//理财账单编号代码
		backstageSet.agenciesNumber = options.get(43)._value;//债权转让编号代码
		backstageSet.auditItemNumber = options.get(44)._value;//审计资料编号代码
		
		backstageSet.seoTitle = options.get(45)._value;//SEO标题
		backstageSet.seoDescription = options.get(46)._value;//SEO描述
		backstageSet.seoKeywords = options.get(47)._value;//SEO关键词
		
		backstageSet.versionName = options.get(48)._value;
		backstageSet.version = options.get(49)._value;
		backstageSet.dbVersion = options.get(50)._value;
		
		backstageSet.borrowFee = Double.parseDouble(options.get(51)._value);//借款管理费本金
		backstageSet.borrowFeeMonth = Integer.parseInt(options.get(52)._value);//借款管理费月
		backstageSet.borrowFeeRate = Double.parseDouble(options.get(53)._value);//借款管理费利率
		backstageSet.investmentFee = Double.parseDouble(options.get(54)._value);//理财管理费
		backstageSet.debtTransferFee = Double.parseDouble(options.get(55)._value);//债权转让管理费
		backstageSet.overdueFee = Double.parseDouble(options.get(56)._value);//逾期管理费
		backstageSet.withdrawFee = Double.parseDouble(options.get(57)._value);//提现管理费
		backstageSet.withdrawRate = Double.parseDouble(options.get(58)._value);//提现管理费
//		backstageSet.rechargeFee = Double.parseDouble(options.get(59)._value);//充值手续费
		backstageSet.vipFee = Double.parseDouble(options.get(60)._value);//VIP服务费
		backstageSet.vipTimeType = Integer.parseInt(options.get(61)._value);//VIP服务时间类型(年 0, 月 1)
//		backstageSet.vipTimeLength = Integer.parseInt(options.get(62)._value);//VIP服务时长(单位[年,月]由vipTimeType确定)
		backstageSet.vipMinTimeType = Integer.parseInt(options.get(63)._value);//VIP最少开通时间类型(年 0, 月 1)
		backstageSet.vipMinTimeLength = Integer.parseInt(options.get(64)._value);//VIP最少开通时长
		backstageSet.vipDiscount = Integer.parseInt(options.get(65)._value);//VIP折扣
		backstageSet.vipAuditPeriod = Integer.parseInt(options.get(66)._value);//VIP审核周期
		
		
		backstageSet.normalPayPoints = Integer.parseInt(options.get(67)._value);//正常还款积分
		backstageSet.fullBidPoints = Integer.parseInt(options.get(68)._value);//成功借款积分
		backstageSet.investpoints = Integer.parseInt(options.get(69)._value);//成功投标积分
		backstageSet.overDuePoints = Integer.parseInt(options.get(70)._value);//账单逾期扣分（负数表示扣分）
		backstageSet.creditToMoney = Double.parseDouble(options.get(71)._value);//信用积分兑换信用额度
		backstageSet.initialAmount = Double.parseDouble(options.get(72)._value);//初始信用额度
		backstageSet.investIntegration = options.get(73)._value;//理财积分
		backstageSet.moneyToSystemScore = Integer.parseInt(options.get(74)._value);//每投标1元积多少分
		
		backstageSet.cpsRewardType = Integer.parseInt(options.get(75)._value);//结算方式（1 按会员数；2 按交易额）
		backstageSet.rewardForCounts = Double.parseDouble(options.get(76)._value);//按会员数，每个的金额
		backstageSet.rewardForRate = Double.parseDouble(options.get(77)._value);//按交易额的比例
		
		backstageSet.isOpenPasswordErrorLimit = Integer.parseInt(options.get(78)._value);//是否启用密码错误次数超限锁定(0不启用，1启用)
		backstageSet.passwordErrorCounts = Integer.parseInt(options.get(79)._value);//密码连续错误次数时锁定用户
		backstageSet.lockingTime = Integer.parseInt(options.get(80)._value);//密码连续错误次数时锁定用户时长
		backstageSet.isOpenKeywordLimit = options.get(81)._value;//是否启用用户名注册关键字安全设置
		
		backstageSet.userNameMinLength = options.get(82)._value;//最小用户名长度
		backstageSet.userNameMaxLength = options.get(83)._value;//最大用户名长度
		backstageSet.passwordMinLength = options.get(84)._value;//最小密码名长度
		backstageSet.passwordMaxLength = options.get(85)._value;//最小密码长度
		backstageSet.password_regex = options.get(86)._value;//密码复杂度正则表达式
		
		backstageSet.keywords = options.get(87)._value;//注册关键字否定词
		
		backstageSet.baiduAccount = options.get(89)._value;//百度流量统计账号
		backstageSet.baiduPassword = options.get(90)._value;//百度流量统计密码
		backstageSet.baiduCode = options.get(91)._value;//百度流量统计代码
		
		backstageSet.companyNameService = options.get(92)._value;//公司名称
		backstageSet.companyProvinceService = options.get(93)._value;//省
		backstageSet.companyCityeService = options.get(94)._value;//市
		backstageSet.companyDomain = options.get(95)._value;//域名
		backstageSet.registerCode = options.get(96)._value;//注册码
		
		backstageSet.supervisorPlatformLog = options.get(98)._value;//管理者平台LOG
		backstageSet.entrustVersion = options.get(99)._value;//资金托管版本
		
		backstageSet.androidVersion = options.get(97)._value;//android版本号
		backstageSet.androidCode = options.get(100)._value;//android编号
		backstageSet.iosVersion = options.get(101)._value;//ios版本号
		backstageSet.iosCode = options.get(102)._value;//ios编号
		backstageSet.iosMsg = options.get(103)._value;//ios升级信息
		backstageSet.androidMsg = options.get(104)._value;//android升级信息
		backstageSet.borrowFeeDay = Double.parseDouble(options.get(105)._value);//天标借款管理费本金
		
		try {
			backstageSet.platformStartupTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(options.get(88)._value);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return backstageSet;
	}
	
	/**
	 * 保存百度流量统计代码
	 * @param error
	 * @return
	 */
	public int saveBaiduCode(String code, ErrorInfo error){
		error.clear();
		
		Query query = JPA.em().createNativeQuery("update t_system_options set _value = ? where id = 6003")
		.setParameter(1, code);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("保存百度流量统计代码时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致保存百度流量统计代码失败！";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		backstageSet.baiduCode = this.baiduCode;
		setCurrentBackstageSet(backstageSet);
		
		error.code = 0;
		error.msg = "保存成功";
		
		return 0;
	}
	
	public String sellService; // 售后服务到期时间
	
	public String getSellService(){
		Date date =  DateUtil.dateAddYear(this.platformStartupTime, Constants.SELL_SERVICE_DATE);
		
		if(DateUtil.daysBetween(new Date(), date) > 0)
			return DateUtil.dateToString1(date) + "(服务中)";
		
		return DateUtil.dateToString1(date) + "(已过期)";
	}
	
	/**
	 * 保存APP版本设置
	 */
	public int appVersionSet(){
		int row = 0;
		String sql = "update t_system_options set _value = case _key " +
        " when 'android_version' then ?" +
        " when 'android_code' then ?" +
        " when 'ios_version' then ?" +
        " when 'ios_code' then ?" +
        " when 'ios_msg' then ?" + 
        " when 'android_msg' then ?" +
        " end where id in(7006, 8002, 8003, 8004, 8005, 8006)";
		
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, this.androidVersion);
		query.setParameter(2, this.androidCode);
		query.setParameter(3, this.iosVersion);
		query.setParameter(4, this.iosCode);
		query.setParameter(5, this.iosMsg);
		query.setParameter(6, this.androidMsg);
		
		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			Logger.error("保存APP版本设置错误!");
			
			return -1;
		}
		
		if(row > 0) {
			BackstageSet set = BackstageSet.getCurrentBackstageSet();
			set.androidVersion = androidVersion;
			set.androidCode = androidCode;
			set.iosVersion = iosVersion;
			set.iosCode = iosCode;
			set.iosMsg = iosMsg;
			set.androidMsg = androidMsg;
			setCurrentBackstageSet(set);
		}
		
		return row;
	}
}
