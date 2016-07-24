package controllers.front.bid;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import models.t_content_news;
import models.t_dict_ad_citys;
import models.t_dict_ad_provinces;
import models.t_dict_cars;
import models.t_dict_educations;
import models.t_dict_houses;
import models.t_dict_maritals;
import models.t_m_products;
import net.sf.json.JSONObject;
import constants.Constants;
import constants.IPSConstants;
import constants.OptionKeys;
import constants.Constants.PayType;
import constants.Constants.RechargeType;
import constants.IPSConstants.IpsCheckStatus;
import controllers.BaseController;
import controllers.front.account.CheckAction;
import controllers.front.account.FundsManage;
import controllers.front.account.PaymentAction;
import business.Ads;
import business.BackstageSet;
import business.Bid;
import business.MProduct;
import business.MProduct.MainType;
import business.MProduct.SubType;
import business.News;
import business.Payment;
import business.Product;
import business.User;
import business.Bid.Purpose;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.mvc.Before;
import utils.CaptchaUtil;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;
import utils.ServiceFee;

/**
 * 标 Action
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-22 上午09:47:28
 */
public class BidAction extends BaseController {

	/** 
	 * 我要借款首页
	 */
	public static void index(long productId, int code, int status) {
		ErrorInfo error = new ErrorInfo();
		/* 根据排序得到所有的非合作机构产品列表  */
		List<Product> products = Product.queryProduct(Constants.SHOW_TYPE_1, error);
		/* 最新投资资讯 */
		List<Bid> bids = Bid.queryAdvertisement(error);
		/*借款须知*/
		PageBean <t_content_news> pageBean = News.queryNewsByTypeId("14", "1", "5", "", error);
		/*小广告*/
		Ads ads = new Ads();
		ads.id = 13;
		
		renderArgs.put("products", products);
		renderArgs.put("bids", bids);
		renderArgs.put("pageBean", pageBean);
		renderArgs.put("ads", ads);
		renderArgs.put("code", code);
		renderArgs.put("productId", productId);
		renderArgs.put("status", status);
		
		User user = User.currUser();
		
		/* 未邮箱激活 */
		if(code == Constants.NOT_EMAILVERIFIED){
	    	if(null == user)
	    		render(Constants.ERROR_PAGE_PATH_FRONT);
	    	
	    	renderArgs.put("userName", user.name);
	    	renderArgs.put("email", user.email);
	    	
	    	render();
	    }
		
		/* 未完成基本资料 */
		if(code == Constants.NOT_ADDBASEINFO){
			addBaseInfo();
			
			render(user);
	    }
		
		render();
	}
	
	/**
	 * 详情
	 */
	public static void detail(long productId, int code, int status) {
		ErrorInfo error = new ErrorInfo();
		
		Product product = new Product();
		product.id = productId;
		
		if(product.id < 1)
			render(Constants.ERROR_PAGE_PATH_FRONT);

		/* 非合作机构,PC/PC+APP产品列表 */
		List<Product> products = Product.queryProduct(Constants.SHOW_TYPE_1, error);

		/* 手续费常量值 */
	    BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
	    double strfee = backstageSet.borrowFee;
	    double borrowFeeMonth = backstageSet.borrowFeeMonth;
	    double borrowFeeRate = backstageSet.borrowFeeRate;

	    renderArgs.put("product", product);
	    renderArgs.put("productId", productId);
	    renderArgs.put("products", products);
	    renderArgs.put("strfee", strfee);
	    renderArgs.put("borrowFeeMonth", borrowFeeMonth);
	    renderArgs.put("borrowFeeRate", borrowFeeRate);
	    renderArgs.put("code", code);
	    renderArgs.put("status", status);
	    
	    /* 未邮箱激活 */
	    if(code == Constants.NOT_EMAILVERIFIED){
	    	User user = User.currUser();
	    	
	    	if(null == user)
	    		render(Constants.ERROR_PAGE_PATH_FRONT);
	    	
	    	renderArgs.put("userName", user.name);
	    	renderArgs.put("email", user.email);
	    	
	    	render();
	    }
	    
	    /* 未完成基本资料 */
	    if(code == Constants.NOT_ADDBASEINFO){
	    	addBaseInfo();
			
			render();
	    }
	    
	    render();
	}

	/**
	 * 新增母产品
	 */
	public static void createMProductNow (int code) {
        ErrorInfo error = new ErrorInfo();
		
		/* 产品主类型  */
		List<MainType> mainTypes = MainType.queryMainType(error, true);
		/* 产品子类型  */
		List<SubType> subTypes = SubType.querySubType(error, true);
		render(mainTypes, subTypes, code);
	}

	/**
	 * 立即申请
	 */
	public static void applyNow(long productId, int code, int status) {
		if(User.currUser().simulateLogin != null){
	    	if(User.currUser().simulateLogin.equalsIgnoreCase(User.currUser().encrypt())){
	        	flash.error("模拟登录不能进行该操作");
	        	String url = request.headers.get("referer").value();
	        	redirect(url);
	        }else{
	        	flash.error("模拟登录超时，请重新操作");
	        	String url = request.headers.get("referer").value();
	        	redirect(url);
	        }
        }
		
		ErrorInfo error = new ErrorInfo();
		
		/* 借款用途  */
		List<Purpose> purpose = Purpose.queryLoanPurpose(error, true);
		/* 母产品  */
		List<MProduct> mProducts = MProduct.queryMProduct(error);

		/* 产品主类型  */
		List<MainType> mainTypes = MainType.queryMainType(error, true);
		/* 产品子类型  */
		List<SubType> subTypes = SubType.querySubType(error, true);
		if(null == purpose) {
			flash.error("借款用途有误!");
			
			render();
		}
		
		Product product = new Product();
		product.createBid = true;
		product.id = productId; 

		if(product.id < 1)
			render(Constants.ERROR_PAGE_PATH_FRONT);
		
		/* 秒还标未进行自动还款签约 */
		if (Constants.IPS_ENABLE && product.loanType == Constants.S_REPAYMENT_BID && StringUtils.isBlank(User.currUser().ipsRepayAuthNo)) {
			index(productId, Constants.NOT_REPAY_AUTH, status);
		}
		
		String key = "bid_" + session.getId();
		Bid loanBid = (Bid) Cache.get(key);  // 获取用户输入的临时数据
		Cache.delete(key); // 删除缓存中的bid对象
		String uuid = CaptchaUtil.getUUID(); // 防重复提交UUID
		
		render(purpose, product, mProducts, mainTypes, subTypes, code, uuid, loanBid, status);
	}
	/**
	 * 发布母产品
	 */
	public static void createMProduct(MProduct mproduct) {
		checkAuthenticity(); 
		if(User.currUser().simulateLogin != null){
        	if(User.currUser().simulateLogin.equalsIgnoreCase(User.currUser().encrypt())){
            	flash.error("模拟登录不能进行该操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }else{
            	flash.error("模拟登录超时，请重新操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }
        }

		ErrorInfo error = new ErrorInfo();
		t_m_products tproduct = new t_m_products();
		tproduct.capital_usage = mproduct.capital_usage;
		tproduct.time = new Date();
		tproduct.name = mproduct.name;
		tproduct.main_type_id = mproduct.main_type.id + "";
		tproduct.sub_type_id = mproduct.sub_type.id + "";
		tproduct.project_name = mproduct.project_name;
		tproduct.project_code = mproduct.project_code;
		tproduct.total_amount = mproduct.total_amount;
		tproduct.loaner_name = mproduct.loaner_name;
		tproduct.project_introduction = mproduct.project_introduction;
		tproduct.project_detail = mproduct.project_detail;
		tproduct.repayment_res = mproduct.repayment_res;
		tproduct.risk_control = mproduct.risk_control;
		tproduct.security_guarantee = mproduct.security_guarantee;
		
		try {
			/* 新增 */
			tproduct.save();
		} catch (Exception e) {
			Logger.error("新增母产品:" + e.getMessage());
			error.code = -19;
			error.msg = "新增母产品失败!";
			JPA.setRollbackOnly();

			return ;
		}
		error.code = 1;
		error.msg = "新增母产品成功！";
		createMProductNow( error.code);
	}
	/**
	 * 发布借款
	 */
	public static void createBid(Bid bid, String signProductId, String uuid, int status) {
		checkAuthenticity(); 
		String date=bid.presellTime;
        if(User.currUser().simulateLogin != null){
        	if(User.currUser().simulateLogin.equalsIgnoreCase(User.currUser().encrypt())){
            	flash.error("模拟登录不能进行该操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }else{
            	flash.error("模拟登录超时，请重新操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }
        }

		ErrorInfo error = new ErrorInfo();
		long productId = Security.checkSign(signProductId, Constants.PRODUCT_ID_SIGN, Constants.VALID_TIME, error);
		
		if(productId < 1){
			flash.error(error.msg); 

			applyNow(productId, -100, status);
		}
		
		/* 防重复提交 */
		if(!CaptchaUtil.checkUUID(uuid)){
			flash.error("请求已提交或请求超时!");
			
			applyNow(productId, -100, status);
		}
		
		bid.createBid = true; // 优化加载
		bid.productId = productId;  // 填充产品对象
		bid.userId = User.currUser().id; // 填充用户对象
		
		/* 非友好提示 */
		if(	null == bid || 
			null == bid.product || 
			!bid.product.isUse || 
			bid.product.isAgency || 
			bid.user.id < 1 ||
			!bid.user.isEmailVerified ||
			!bid.user.isAddBaseInfo){
			
			render(Constants.ERROR_PAGE_PATH_FRONT); 
		}
		
		/* 秒还标未进行自动还款签约 */
		if (Constants.IPS_ENABLE && bid.product.loanType == Constants.S_REPAYMENT_BID && StringUtils.isBlank(bid.user.ipsRepayAuthNo)) 
			index(productId, Constants.NOT_REPAY_AUTH, status);
		
		/* 发布借款 */
		bid.createBid(error);
		flash.put("msg", error.msg);
		
		if(error.code < 0){
			Cache.set("bid_" + session.getId(), bid); // 缓存用户输入的临时数据
			
			applyNow(productId, error.code, status);
		}
		
		Cache.delete("bid_" + session.getId()); // 删除错误带回页面数据的缓存
		User user = User.currUser();
		
		if(Constants.IPS_ENABLE){
			/*投标奖励*/
			if (bid.bonusType != Constants.NOT_REWARD) {
				if (bid.investBonus > 0) {
					switch (Constants.PAY_TYPE_INVEST) {
					//平台内部进行转账
					case PayType.INNER:
						flash.error("资金托管模式下，不能以平台内部进行转账的方式支付投标奖励费");

						applyNow(productId, -1, status);
						
						break ;
						//通过独立普通网关
					case PayType.INDEPENDENT:
						if (bid.investBonus + bid.bail > user.balanceDetail.user_amount2) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("rechargeType", RechargeType.InvestBonus);
							map.put("fee", bid.investBonus);
							map.put("bid", bid);
							Cache.set("rechargePay"+User.currUser().id, map, IPSConstants.CACHE_TIME);
							flash.error("请支付投标奖励费");
							
							FundsManage.rechargePay();
						} else {
							bid.deductInvestBonus(error);
							
							if (error.code < 0) {
								render(Constants.ERROR_PAGE_PATH_FRONT);
							}
						}
						
						break ;
					//通过共享资金托管账户网关
					case PayType.SHARED:
						if (bid.investBonus + bid.bail > user.balance) {
							flash.error("您可用余额不足支付投标奖励费，请充值后再发布借款标");

							FundsManage.recharge();
						}
						
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("rechargeType", RechargeType.InvestBonus);
						map.put("fee", bid.investBonus);
						map.put("bid", bid);
						Cache.set("rechargePay"+User.currUser().id, map, IPSConstants.CACHE_TIME);
						flash.error("请支付投标奖励费");
						
						FundsManage.rechargePay();
						
						break ;
					//资金托管网关
					case PayType.IPS:
						if (bid.investBonus + bid.bail > user.balance) {
							flash.error("您可用余额不足支付投标奖励费，请充值后再发布借款标");

							FundsManage.recharge();
						}
						
						map = new HashMap<String, Object>();
						map.put("rechargeType", RechargeType.InvestBonus);
						map.put("fee", bid.investBonus);
						map.put("bid", bid);
						Cache.set("rechargePay"+User.currUser().id, map, IPSConstants.CACHE_TIME);
						flash.error("请支付投标奖励费");
						
						PaymentAction.transferUserToMer();
						
						break ;
					}
				}
			}
				
			Map<String, String> args = Payment.registerSubject(IPSConstants.BID_CREATE, bid);
			
			render("@front.account.PaymentAction.registerSubject", args);
		}else{
			flash.put("no", OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, error) + bid.id);
			flash.put("title", bid.title);
			DecimalFormat myformat = new DecimalFormat();
			myformat.applyPattern("##,##0.00");
			flash.put("amount", myformat.format(bid.amount));
			flash.put("status", bid.status);
		}
		
		applyNow(productId, error.code, status);
	}
	
	/**
	 * 查看用户当前的登录状态(异步)
	 */
	public static void checkUserStatus(){
		User user = User.currUser();
		
		JSONObject json = new JSONObject();
		
		/* 是否登录 */
		if(null == user) {
			json.put("status", Constants.NOT_LOGIN);
			
			renderJSON(json);
		}
		
		/* 是否激活 */
		if(!user.isEmailVerified) {
			json.put("userName", user.name);
			json.put("email", user.email);
			json.put("status", Constants.NOT_EMAILVERIFIED);
			
			renderJSON(json);
		}
		
		/* 是否完善基本资料 */
		if (!user.isAddBaseInfo) {
			json.put("status", Constants.NOT_ADDBASEINFO);

			renderJSON(json);
		}
		
		json.put("status", Constants.SUCCESS_STATUS);
		renderJSON(json);
	}
	   
   /**
    * 弹框登录(异步)
    */
   public static void logining(String name, String password, String code, String randomID){
	   
	   business.BackstageSet  currBackstageSet = business.BackstageSet.getCurrentBackstageSet();
	   Map<String,java.util.List<business.BottomLinks>> bottomLinks = business.BottomLinks.currentBottomlinks();
	   
	   if(null != currBackstageSet){
		   Cache.delete("backstageSet");//清除系统设置缓存
	   }
	   
	   if(null != bottomLinks){
		   Cache.delete("bottomlinks");//清除底部连接缓存
	   }
	  
	   ErrorInfo error = new ErrorInfo();
	   
	   if(StringUtils.isBlank(name)) 
		  renderText("请输入用户名!");
	   
	   if(StringUtils.isBlank(password)) 
		   renderText("请输入密码!");
	   
	   if(StringUtils.isBlank(code)) 
		   renderText("请输入验证码");
		   
	   
	   if(StringUtils.isBlank(randomID)) 
		   renderText("请刷新验证码");
	   
	   if(!code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) 
		   renderText("验证码错误");
	   
	   User user = new User();
	   user.name = name;
	   
	   if(user.id < 0) 
		   renderText("该用户名不存在");
	   
	   if(user.login(password,false, error)<0) 
		   renderText(error.msg);
	   
   }
	
	/**
	 * 完善基本资料
	 */
	private static void addBaseInfo(){
		List<t_dict_cars> cars = (List<t_dict_cars>) Cache.get("cars"); // 车子
		List<t_dict_ad_provinces> provinces = (List<t_dict_ad_provinces>) Cache.get("provinces"); // 省
		List<t_dict_educations> educations = (List<t_dict_educations>) Cache.get("educations"); // 教育
		List<t_dict_houses> houses = (List<t_dict_houses>) Cache.get("houses"); // 房子
		List<t_dict_maritals> maritals = (List<t_dict_maritals>) Cache.get("maritals"); // 婚姻
		
		String key = "province" + session.getId();
		Object obj = Cache.get(key);
		Cache.delete(key);
		int province = obj == null ? 1 : Integer.parseInt(obj.toString());
		List<t_dict_ad_citys> cityList = User.queryCity(province); // 市
		
		renderArgs.put("cars", cars);
		renderArgs.put("provinces", provinces);
		renderArgs.put("educations", educations);
		renderArgs.put("houses", houses);
		renderArgs.put("maritals", maritals);
		renderArgs.put("cityList", cityList);
	}
	
	/**
	 * 保存基本信息
	 */
	public static void saveInformation(String realityName, int sex, int age,
			int city, int province, String idNumber, int education,
			int marital, int car, int house, String mobile1, String mobile2,
			String code1, String code2, String email2, long productId, int status) {
		
		User user = User.currUser();
		
		if(User.currUser().simulateLogin != null){
        	if(User.currUser().simulateLogin.equalsIgnoreCase(User.currUser().encrypt())){
            	flash.error("模拟登录不能进行该操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }else{
            	flash.error("模拟登录超时，请重新操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }
        }
		
		if (null == user) {
			if (Constants.APPLY_NOW_INDEX == status)
				index(productId, Constants.NOT_LOGIN, status);
			else
				detail(productId, Constants.NOT_LOGIN, status);
		}
			
		user.id = User.currUser().id; // 及时在抓取一次	
		
		if(!user.isEmailVerified)
			if (Constants.APPLY_NOW_INDEX == status)
				index(productId, Constants.NOT_EMAILVERIFIED, status);
			else
				detail(productId, Constants.NOT_EMAILVERIFIED, status);
		
		if(user.isAddBaseInfo) 
			render(Constants.ERROR_PAGE_PATH_FRONT);
		
		flash.put("realityName", realityName);
		flash.put("sex", sex);
		flash.put("age", age);
		flash.put("city", city);
		flash.put("province", province);
		Cache.set("province" + session.getId(), province);
		flash.put("idNumber", idNumber);
		flash.put("education", education);
		flash.put("marital", marital);
		flash.put("car", car);
		flash.put("house", house);
		flash.put("mobile1", mobile1);
		flash.put("mobile2", mobile2);
		flash.put("email2", email2);
		
		if(Constants.CHECK_CODE) {
			Object cCode1 = Cache.get(mobile1);
			Object cCode2 = Cache.get(mobile2);
			
			if(cCode1 == null) {
				flash.error("验证码已失效，请重新点击发送验证码");
				
				if (Constants.APPLY_NOW_INDEX == status)
					index(productId, Constants.NOT_ADDBASEINFO, status);
				else
					detail(productId, Constants.NOT_ADDBASEINFO, status);
			}

			if(!cCode1.toString().equals(code1)) {
				flash.error("手机验证错误");
				
				if (Constants.APPLY_NOW_INDEX == status)
					index(productId, Constants.NOT_ADDBASEINFO, status);
				else
					detail(productId, Constants.NOT_ADDBASEINFO, status);
			}
			
			if(cCode2 == null || !cCode2.toString().equals(code2)) {
				mobile2 = null;
			}
		}
		
		User newUser = new User();
		newUser.id = user.id;
		
		newUser.realityName = realityName;
		newUser.setSex(sex);
		newUser.age = age;
		newUser.cityId = city;
		newUser.educationId = education;
		newUser.maritalId = marital;
		newUser.carId = car;
		newUser.idNumber = idNumber;
		newUser.houseId = house;
		newUser.mobile1 = mobile1;
		newUser.mobile2 = mobile2;
		newUser.email2 = email2;
		
		ErrorInfo error = new ErrorInfo();
		newUser.edit(user,error);
		
		if(error.code < 0){
			flash.error(error.msg);
			
			if (Constants.APPLY_NOW_INDEX == status)
				index(productId, Constants.NOT_ADDBASEINFO, status);
			else
				detail(productId, Constants.NOT_ADDBASEINFO, status);
		}
			
		applyNow(productId, 0, status);
	}
	
	/**
	 * 最新满标
	 */
	public static void fullBid(int nowPage) {
		ErrorInfo error = new ErrorInfo();
		
		PageBean<Bid> pageBean = new PageBean<Bid>();
		pageBean.currPage = nowPage;
		pageBean.pageSize = Constants.FULL_BID_COUNT;
		pageBean.page = Bid.queryFullBid(pageBean, error);

		render(pageBean);
	}
	
	/* 拦截 createBid, applyNow方法,防止非法数据提交*/
	@Before(only = {"applyNow"})
	static void checkValid(){
		String _status = params.get("status");
		String _productId = params.get("productId");
		
		if(StringUtils.isBlank(_productId) || StringUtils.isBlank(_status))
			render(Constants.ERROR_PAGE_PATH_FRONT); 
		
		long productId = 0;
		int status = 0;
		
		/* 无法转换，跳转至首页 */
		try {
			productId = Long.parseLong(_productId);
			status = Integer.parseInt(_status);
		} catch (Exception e) {
			index(productId, 0, 1);
		}
		
		/* 如果是合作机构标及其未启动 */
		Boolean falg = Product.isAgency(productId);
		
		if(null == falg || falg)
			index(productId, 0, 1);
			
		User user = User.currUser();

		switch (status) {
		/* 首页申请 */
		case Constants.APPLY_NOW_INDEX:
			if(null == user)
				index(productId, Constants.NOT_LOGIN, status);
			
			user.id = User.currUser().id; // 及时在抓取一次	
			
			if(Constants.IPS_ENABLE && (User.currUser().getIpsStatus() != IpsCheckStatus.IPS)){
				CheckAction.approve();
			}
			
			if(!user.isEmailVerified)
				index(productId, Constants.NOT_EMAILVERIFIED, status);
			
			if(!user.isAddBaseInfo){
				
				if(!user.isAddBaseInfo)
					index(productId, Constants.NOT_ADDBASEINFO, status);
			}
			
			break;
		/* 详情申请 */	
		case Constants.APPLY_NOW_DETAIL:
			if(null == user)
				detail(productId, Constants.NOT_LOGIN, status);
			
			user.id = User.currUser().id; // 及时在抓取一次	
			
			if(Constants.IPS_ENABLE && (User.currUser().getIpsStatus() != IpsCheckStatus.IPS)){
				CheckAction.approve();
			}
			
			if(!user.isEmailVerified)
				detail(productId, Constants.NOT_EMAILVERIFIED, status);
			
			if(!user.isAddBaseInfo){
				
				if(!user.isAddBaseInfo)
					detail(productId, Constants.NOT_ADDBASEINFO, status);
			}
			
			break;
			
		default:
			index(productId, 0, 1);
			
			break;
		}
		
		
	}
	
	/**
	* 总付利息
	*/
	public static void planapr(double amount, double apr, int unit, int period, int repayment){
		if (amount <= 0 || apr < 0 || apr > 100 || unit < -1 || unit > 1 || period <= 0 || repayment < 1 || repayment > 3) {
			renderJSON(0);
		}
		
		double lastAmount = ServiceFee.interestCompute(amount, apr, unit, period, repayment);
		
		renderJSON(lastAmount);
		
	}
}
