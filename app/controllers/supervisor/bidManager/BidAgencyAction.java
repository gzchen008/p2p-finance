package controllers.supervisor.bidManager;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import constants.Constants;
import constants.IPSConstants;
import constants.Constants.PayType;
import controllers.supervisor.SupervisorController;
import models.v_agencies;
import models.v_bids;
import models.v_user_info;
import bean.AgencyBid;
import business.Agency;
import business.Bid;
import business.MProduct;
import business.Optimization;
import business.Payment;
import business.Product;
import business.User;
import business.Bid.Purpose;
import business.MProduct.MainType;
import business.MProduct.SubType;
import play.cache.Cache;
import utils.CaptchaUtil;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;

/**
 * 机构合作/机构标管理
 * @author bsr
 * @version 6.0
 * @created 2014-6-26 下午01:52:09
 */
public class BidAgencyAction extends SupervisorController{
	
	/**
	 * 合作结构标列表
	 */
	public static void agencyBidList(){
		ErrorInfo error = new ErrorInfo();
		PageBean<AgencyBid> pageBean = new PageBean<AgencyBid>();
		pageBean.page = Optimization.BidOZ.queryAgencyBids(pageBean, error, BidPlatformAction.getParameter(pageBean, null));

		render(pageBean);
	}
	

	/**
	 * 发布合作机构标 页面跳转
	 */
	public static void addAgencyBid(){
		ErrorInfo error = new ErrorInfo();
		
		/* 得到所有借款用途  */
		List<Purpose> purpose = Purpose.queryLoanPurpose(error, true);
		
		/* 机构标产品 */
		Product product = Product.queryAgencyProduct(error);
		/* 母产品  */
		List<MProduct> mProducts = MProduct.queryMProduct(error);

		/* 产品主类型  */
		List<MainType> mainTypes = MainType.queryMainType(error, true);
		/* 产品子类型  */
		List<SubType> subTypes = SubType.querySubType(error, true);
		if(null == product){
			flash.error(error.msg);
			
			agencyBidList();
		}
		
		/* 机构列表 */
		List<Agency> agencys = Agency.queryAgencys(error);
		
		String key = "bid_" + session.getId();
		Bid loanBid = (Bid) Cache.get(key);  // 获取用户输入的临时数据
		Cache.delete(key); // 删除缓存中的bid对象
		String uuid = CaptchaUtil.getUUID(); // 防重复提交UUID
		int once_repayment = Constants.ONCE_REPAYMENT;  //一次性还款方式

		render(purpose, product, mProducts, mainTypes, subTypes, agencys, uuid, loanBid, once_repayment);
	}
	
	/**
	 * 发布合作机构标
	 */
	public static void addingAgencyBid(Bid bid, long productId, String uuid){
		/* 有效表单验证  */
		checkAuthenticity(); 

		if(!CaptchaUtil.checkUUID(uuid)){
			flash.error("请求已提交或请求超时!");
			
			addAgencyBid();
		}
		
		if(bid.agencyId <= 0) { 
			flash.error("机构名称有误!"); 
			
			addAgencyBid();
		}
		
		String userName = params.get("userName");
		String signUserId = params.get("sign");
		
		if(StringUtils.isBlank(signUserId) && StringUtils.isBlank(userName)){
			flash.error("直接借款人有误!");
			
			addAgencyBid();
		}
		
		ErrorInfo error = new ErrorInfo();
		long userId = 0;
		
		if(StringUtils.isNotBlank(userName)){
			userId = User.queryIdByUserName(userName, error);
		}else{
			userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		}
		
		if(userId < 1){
			flash.error(error.msg);
			
			addAgencyBid();
		}
		
		bid.createBid = true;
		bid.productId = productId;  // 填充产品对象
		bid.userId = userId;

		/* 非友好提示 */
		if( bid.user.id < 1 ||
			null == bid.product || 
			!bid.product.isUse || 
			!bid.product.isAgency 
		){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR); 
		}
		
		if(!bid.user.isEmailVerified){
			flash.error("借款人未激活!");
			
			addAgencyBid();
		}
		
		/* 需要填写基本资料 */
		if(!bid.user.isAddBaseInfo) {
			flash.error("借款人未填写基本资料!");
			
			addAgencyBid();
		}
		
		/* 秒还标未进行自动还款签约 */
		if (Constants.IPS_ENABLE && bid.product.loanType == Constants.S_REPAYMENT_BID && StringUtils.isBlank(bid.user.ipsRepayAuthNo)) {
			flash.error("直接借款人未自动还款签约!");
			
			addAgencyBid();
		}
		
		/* 发布借款 */
		bid.createBid(error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			
			addAgencyBid();
		}
		
		if(Constants.IPS_ENABLE){
			/*投标奖励*/
			if (bid.bonusType != Constants.NOT_REWARD) {
				switch (Constants.PAY_TYPE_INVEST) {
				//平台内部进行转账
				case PayType.INNER:
					flash.error("资金托管模式下，不能以平台内部进行转账的方式支付投标奖励费");

					addAgencyBid();
					//通过独立普通网关
				case PayType.INDEPENDENT:
					if (bid.investBonus > bid.user.balanceDetail.user_amount2) {
						flash.error("直接借款人账户余额不足以扣除投标奖励，发布借款标失败");
						
						addAgencyBid();
					} else {
						bid.deductInvestBonus(error);
						
						if (error.code < 0) {
							render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
						}
					}
				//通过共享资金托管账户网关
				case PayType.SHARED:
				//资金托管网关
				case PayType.IPS:
					flash.error("发布机构合作标，请不要设置投标奖励");

					addAgencyBid();
				}
			}
				
			Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CREATE, bid);
			
			render("@front.account.PaymentAction.registerSubject", args);
		}
		
		flash.error(bid.id > 0 ? "发布成功!" : error.msg);
		
		agencyBidList();
	}

	/**
	 * 检查数据
	 */
	@Deprecated
	public static boolean checkAgencyBid(Bid bid){
		if(bid.agencyId <= 0) { flash.error("机构名称有误!"); return true; }
		if(StringUtils.isBlank(bid.title) || bid.title.length() > 24){ flash.error("借款标题有误!"); return true; }
		int _amount = (int)bid.amount;
		if(bid.amount <= 0 || bid.amount != _amount || bid.amount < bid.product.minAmount || bid.amount > bid.product.maxAmount){flash.error("借款金额有误!"); return true; }
		if (bid.apr <= 0 || bid.apr > 100 || bid.apr < bid.product.minInterestRate || bid.apr > bid.product.maxInterestRate) { flash.error("年利率有误!"); return true; }
		if (bid.product.loanImageType == Constants.USER_UPLOAD && (StringUtils.isBlank(bid.imageFilename) || bid.imageFilename.contains(Constants.DEFAULT_IMAGE))) { flash.error("借款图片有误!"); return true; }
		if(bid.purpose.id < 0){ flash.error("借款用途有误!"); return true; }
		if(bid.repayment.id < 0){ flash.error("借款类型有误!"); return true; }
		if(bid.period <= 0){ flash.error("借款期限有误!"); return true; }
		switch (bid.periodUnit) {
		case Constants.YEAR:
			if(bid.period > Constants.YEAR_PERIOD_LIMIT){ flash.error("借款期限超过了" + Constants.YEAR_PERIOD_LIMIT + "年"); return true; }
			break;
		case Constants.MONTH:
			if(bid.period > Constants.YEAR_PERIOD_LIMIT * 12){ flash.error("借款期限超过了" + Constants.YEAR_PERIOD_LIMIT + "年"); return true; }
			break;
		case Constants.DAY:
			if(bid.period > Constants.YEAR_PERIOD_LIMIT * 12 * 30){ flash.error("借款期限超过了" + Constants.YEAR_PERIOD_LIMIT + "年"); return true; }
			if(bid.investPeriod > bid.period){ flash.error("天标满标期限不能大于借款期限 !"); return true; }
			break;
		default: flash.error("借款期限单位有误!"); return true;
		}
		
		if((bid.minInvestAmount > 0 && bid.averageInvestAmount > 0) || (bid.minInvestAmount <= 0 && bid.averageInvestAmount <= 0)){ flash.error("最低投标金额和平均招标金额有误!"); return true; }
		if(bid.averageInvestAmount > 0 && bid.amount % bid.averageInvestAmount != 0){ flash.error("平均招标金额有误!"); return true;}
		if(bid.investPeriod <= 0) { flash.error("投标期限有误!"); return true; }
		if(StringUtils.isBlank(bid.description)) { flash.error("借款描述有误!"); return true; }
		if(bid.bonusType == Constants.FIXED_AMOUNT_REWARD && (bid.bonus < 0 || bid.bonus > bid.amount)){ flash.error("固定奖励大于了借款金额"); return true; }
		if(bid.bonusType == Constants.PROPORTIONATELY_REWARD && (bid.awardScale < 0 || bid.awardScale > 100)){ flash.error("借款奖励比例有误!"); return true; }
		if (bid.minInvestAmount > 0 && (bid.minInvestAmount < bid.product.minInvestAmount)){ flash.error("最低投标金额不能小于产品最低投标金额!"); return true; }
		if (bid.averageInvestAmount > 0 && (bid.amount / bid.averageInvestAmount > bid.product.maxCopies)){ flash.error("平均投标份数不能大于产品的最大份数限制 !"); return true; }
	
		return false;
	}
	
	/**
	 * 异步选择用户
	 */
	public static void selectUsersInit(String currPage, String pageSize, String keyword) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_info> pageBean = User.queryUserBySupervisor(null, null, null, null, keyword, "0", currPage, Constants.PAGE_SIZE_EIGHT+"", error);
		
		if(error.code < 0) 
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		
		render(pageBean);
	}
	/**
	 * 选择母产品
	 */
	public static void getMProductById(String id) {
		ErrorInfo error = new ErrorInfo();
		MProduct mProdutE = MProduct.getMProductById(Long.valueOf(id), error);
		JSONObject json = new JSONObject();
		
		json.put("error", error);
		json.put("mProdutE", mProdutE);
		
		renderJSON(json);
	}
	/**
	 * 合作结构列表
	 */
	public static void agencyList() {
		ErrorInfo error = new ErrorInfo();
		
		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数
		String condition = params.get("condition"); // 条件
		String keyword = params.get("keyword"); // 关键词
		
		PageBean<v_agencies> pageBean = new PageBean<v_agencies>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		pageBean.page = Agency.queryAgencies(pageBean, error, condition, keyword);

		if (null == pageBean.page) render(Constants.ERROR_PAGE_PATH_SUPERVISOR);  

		render(pageBean);
	}
	
	/**
	 * 启用合作机构
	 */
	public static void enanleAgency(long aid){
		ErrorInfo error = new ErrorInfo();
	    Agency.editStatus(aid, Constants.ENABLE, error);
		flash.error(error.msg);
	    
		agencyList();
	}
	
	/**
	 * 暂停合作机构
	 */
	public static void notEnanleAgency(long aid){
		ErrorInfo error = new ErrorInfo();
		Agency.editStatus(aid, Constants.NOT_ENABLE, error);
		flash.error(error.msg);
	    
		agencyList();
	}
	
	/**
	 * 添加合作机构 页面跳转
	 */
	public static void addAgency(){
		//ErrorInfo error = new ErrorInfo();
		
		/* 信用等级名称 */
		//List<CreditLevel> creditLevels = CreditLevel.queryCreditName(error);
		
		render();
	}
	
	/**
	 * 添加合作机构
	 */
	public static void addingAgency(Agency agency){

		if( StringUtils.isBlank(agency.name) ||
			Agency.checkName(agency.name) ||
			Constants.AGENCY_NAME_REPEAT.equals(agency.name) ||
			StringUtils.isBlank(agency.introduction) ||
			StringUtils.isBlank(agency.id_number) ||
			Constants.SEAL_NAME_REPEAT.equals(agency.id_number) ||
			Agency.checkIdNumber(agency.id_number) ||
			StringUtils.isBlank(agency.imageFilenames) ||
			agency.creditLevel <= 0 
		  ){
			flash.error("数据有误!");
			
			agencyList();
		}
		
		ErrorInfo error = new ErrorInfo();
		agency.createAgency(error);
		flash.error(error.msg);
		
		agencyList();
	}
	
	/**
	 * 检查名称是否唯一
	 */
	public static void checkName(String name){
		renderJSON(Agency.checkName(name));
	}
	
	/**
	 * 检查营业执照号是否唯一
	 */
	public static void checkIdNumber(String idNumber){
	    renderJSON(Agency.checkIdNumber(idNumber));
	}
	
	/**
	 * 合作机构标详情(审核操作等,需要去借款标管理中进行)
	 */
	public static void detail(long bidid, int flag) { 
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = flag;
		bid.id = bidid;
		
		render(bid, flag);
	}
	
	/**
	 * 合作机构详情(对应的标列表)
	 */
	public static void agencyDetail(long agencyId){
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bids> pageBean = new PageBean<v_bids>();
		pageBean.page = Bid.queryAgencyBid(pageBean, agencyId, error, BidPlatformAction.getParameter(pageBean, null));

		if (null == pageBean.page) render(Constants.ERROR_PAGE_PATH_SUPERVISOR);  

		render(pageBean, agencyId);
	}
}
