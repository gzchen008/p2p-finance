package controllers.mobile;

import business.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import constants.Constants;
import constants.Templets;
import controllers.BaseController;
import controllers.interceptor.H5Interceptor;
import models.t_bids;
import models.t_system_options;
import models.t_users;
import net.sf.json.JSONObject;
import play.Logger;
import play.mvc.Scope;
import play.mvc.With;
import utils.CaptchaUtil;
import utils.DateUtil;
import utils.ErrorInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>Project: com.shovesoft.sp2p</p>
 * <p>Title: ProductAction.java</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2014 Sunlights.cc</p>
 * <p>All Rights Reserved.</p>
 *
 * @author <a href="mailto:jiaming.wang@sunlights.cc">wangJiaMing</a>
 */
@With(H5Interceptor.class)
public class ProductAction extends BaseController {

    public static void productDetail(){
        if (params.get("bidId") == null) {
            MainContent.moneyMatters();
        }
        Long bidId = Long.valueOf(params.get("bidId"));
        Logger.info(">>bidId:" + bidId);
        Bid bid = new Bid();
        bid.id = bidId;
        if (bid.getId() == -1) {
            MainContent.moneyMatters();
        }

        JSONObject jsonMap = new JSONObject();
        if(bid.repayment_res != null && bid.repayment_res.length() > 44){
            try{
                String project = bid.repayment_res.split(";")[0];
                jsonMap.put("repayment_res_short", project.substring(0,44) + "...");//短的资金安全
            }catch (Exception e) {
                jsonMap.put("repayment_res_short", bid.repayment_res.substring(0,44) + "...");//短的资金安全
            }
        }else{
            jsonMap.put("repayment_res_short", bid.repayment_res);
        }

        if(bid.description != null && bid.description.length() > 44){
            jsonMap.put("project_introduction_short", bid.description.substring(0,44) + "...");
        }else{
            jsonMap.put("project_introduction_short", bid.description);
        }

        boolean bidCanBuyFlag = false;//是否可以购买
        if (bid.status == 1 || bid.status == 2) {//提前借款  筹款中
            Long balanceTime = (bid.investExpireTime.getTime() - new Date().getTime()) / 1000;
            jsonMap.put("balanceTime", balanceTime);//倒计时时间

            double canInvestAmount = bid.amount - bid.hasInvestedAmount;
            if (canInvestAmount > 0) {
                bidCanBuyFlag = true;
            }
        }
        jsonMap.put("bidCanBuyFlag", bidCanBuyFlag);

        Logger.info(">>current bid status:" + bid.status);

        render(bid, jsonMap);
    }


    public static void productBid(String bidId){
        Logger.info("current bid :" + bidId);
        if (bidId == null) {
            MainContent.moneyMatters();
        }
        Long newBidId = Long.valueOf(bidId);
        Bid bid = new Bid();
        bid.id = newBidId;

        if (bid.getId() == -1) {
            MainContent.moneyMatters();
        }

        String sign = bid.getSign();
        String uuid = CaptchaUtil.getUUID(); // 防重复提交UUID

        JSONObject map = new JSONObject();
        double availavleInvestedAmount = bid.amount - bid.hasInvestedAmount;
        map.put("availavleInvestedAmount", availavleInvestedAmount);
        map.put("currentUser", User.currUser());
        map.put("uuid", uuid);
        map.put("sign", sign);
        map.put("userId", "front_" + Scope.Session.current().getId());

        ProductAction.render(bid, map);
    }
    
    public static void bidAgreement (String sign, int type) {
    	render (sign, type);
    }
    
    public static void getAgreementContent(String sign, int type){
		ErrorInfo error = new ErrorInfo();
		
		TemplatePact pact = new TemplatePact();
		pact.id = Templets.BID_PACT_INVEST;
		
		t_bids bid = new t_bids();
		t_users bidUser = new t_users();
		String company_name = "";
		String sql1 = "select _value from t_system_options where _key = ?";
		try {
			bid = t_bids.findById(Long.parseLong(sign));
			bidUser = t_users.findById(bid.user_id);
			company_name = t_system_options.find(sql1, "company_name").first();
		} catch (Exception e) {
			error.msg = "系统异常";
			error.code = -1;
			e.printStackTrace();
			return;
		}
		
		Date date = new Date();
		String pact_no = sign + DateUtil.simple(date);
		String content = pact.content;
		content = content.replace(Templets.PACT_NO,pact_no)
		.replace(Templets.LOAN_NAME, bidUser.reality_name)
		.replace(Templets.ID_NUMBER, bidUser.id_number)
		.replace(Templets.COMPANY_NAME,company_name)
		.replace(Templets.DATE,DateUtil.dateToString(new Date()));	
		if(error.code < 0){
			renderJSON(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		renderText(content);
	}
    
    public static void productList() {
    	render();
    };
    

    public static void findProductsBy() {
        Map<String, String> map = params.allSimple();
        map.remove("body");
        ObjectMapper objectMapper = new ObjectMapper();
        PageVo pageVo = objectMapper.convertValue(map, PageVo.class);
        List<ProductVo> productVos = Invest.findProductsBy(pageVo);
        pageVo.setList(productVos);
        renderJSON(pageVo);
    }
    
    public static void productDetail_v1() {
    	render();
    }
}
