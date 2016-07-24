package controllers.front.invest;

import business.*;
import com.shove.security.Encrypt;
import constants.Constants;
import constants.IPSConstants.IpsCheckStatus;
import controllers.BaseController;
import controllers.app.common.Message;
import controllers.app.common.MessageVo;
import controllers.app.common.MsgCode;
import controllers.app.common.Severity;
import controllers.front.account.CheckAction;
import controllers.front.account.LoginAndRegisterAction;
import models.v_front_all_bids;
import models.v_front_user_attention_bids;
import models.v_invest_records;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import utils.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuwenhui
 */
public class InvestAction extends BaseController {

    /**
     * 我要理财首页
     */

    public static void investHome() {

        ErrorInfo error = new ErrorInfo();
        Long totalCount = Invest.getBidCount(error);

        if (error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }

        List<Product> products = Product.queryProductNames(true, error);

        List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);
        int currPage = Constants.ONE;
        int pageSize = Constants.FIVE;

        String currPageStr = params.get("currPage");
        String pageSizeStr = params.get("pageSize");

        if (NumberUtil.isNumericInt(currPageStr)) {
            currPage = Integer.parseInt(currPageStr);
        }

        if (NumberUtil.isNumericInt(pageSizeStr)) {
            pageSize = Integer.parseInt(pageSizeStr);
        }

        String apr = params.get("apr") == null ? "0" : params.get("apr");
        String amount = params.get("amount") == null ? "0" : params.get("amount");
        String loanSchedule = params.get("loanSchedule") == null ? "0" : params.get("loanSchedule");
        String period = params.get("period") == null ? "0" : params.get("period");
        String status = params.get("status") == null ? "0" : params.get("status");

        String startDate = params.get("startDate");
        String endDate = params.get("endDate");
        String loanType = params.get("loanType");
        String minLevel = params.get("minLevel");
        String maxLevel = params.get("maxLevel");
        String orderType = params.get("orderType");
        String keywords = params.get("keywords");

        PageBean<v_front_all_bids> pageBean = new PageBean<v_front_all_bids>();
        pageBean = Invest.queryAllBids(Constants.SHOW_TYPE_1, currPage, pageSize, apr, amount, loanSchedule, startDate, endDate, loanType, minLevel, maxLevel, orderType, keywords, period, status, error);

        if (error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }

        render(totalCount, creditLevels, products, pageBean);
    }


    /**
     * 前台投资首页借款标分页
     *
     * @param pageNum
     */
    public static void homeBids(int pageNum, int pageSize, String apr, String amount, String loanSchedule, String startDate, String endDate, String loanType, String minLevel, String maxLevel, String orderType, String keywords) {

        ErrorInfo error = new ErrorInfo();
        int currPage = pageNum;

        if (params.get("currPage") != null) {
            currPage = Integer.parseInt(params.get("currPage"));
        }

        PageBean<v_front_all_bids> pageBean = new PageBean<v_front_all_bids>();
//        pageBean = Invest.queryAllBids(Constants.SHOW_TYPE_1, currPage, pageSize, apr, amount, loanSchedule, startDate, endDate, loanType, minLevel, maxLevel, orderType, keywords, error);

        if (error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }
        render(pageBean);
    }


    /**
     * 用户查看自己所有的收藏标
     */
    public static void queryUserCollectBids(int pageNum, int pageSize) {

        ErrorInfo error = new ErrorInfo();
        int currPage = pageNum;

        if (params.get("currPage") != null) {
            currPage = Integer.parseInt(params.get("currPage"));
        }
        User user = User.currUser();
        PageBean<v_front_user_attention_bids> pageBean = Invest.queryAllCollectBids(user.id, currPage, pageSize, error);

        if (error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }
        render(pageBean);

    }


    /**
     * 向借款人提问
     *
     */
    public static void questionToBorrower(String toUserIdSign, String bidIdSign, String content, String code, String inputCode) {

        ErrorInfo error = new ErrorInfo();
        User user = User.currUser();
        JSONObject json = new JSONObject();

        long bidId = Security.checkSign(bidIdSign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);

        if (error.code < 0) {
            error.msg = "对不起！非法请求！";
            json.put("error", error);
            renderJSON(json);
        }

        long toUserId = Security.checkSign(toUserIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

        if (error.code < 0) {
            error.msg = "对不起！非法请求！";
            json.put("error", error);
            renderJSON(json);
        }

        BidQuestions question = new BidQuestions();
        question.bidId = bidId;
        question.userId = user.id;
        question.time = new Date();
        question.content = content;
        question.questionedUserId = toUserId;
        String codes = (String) Cache.get(code);

        if (!codes.equalsIgnoreCase(inputCode)) {
            error.msg = "对不起！验证码错误！";
            json.put("error", error);
            renderJSON(json);
        }

        question.addQuestion(user.id, error);

        if (error.code < 0) {
            json.put("content", content);
        }
        json.put("error", error);
        renderJSON(json);
    }


    /**
     * 进入投标页面
     *
     * @param bidId
     */
    public static void invest(long bidId, String showBox) {

        ErrorInfo error = new ErrorInfo();
        Bid bid = new Bid();
        bid.id = bidId;

		/*进入详情页面增加浏览次数*/
        Invest.updateReadCount(bidId, error);


        if (error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }

        Map<String, String> historySituationMap = User.historySituation(bid.userId, error);//借款者历史记录情况
        List<UserAuditItem> uItems = UserAuditItem.queryUserAllAuditItem(bid.userId, bid.mark); // 用户正对产品上传的资料集合

        if (error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }
        User user = User.currUser();
        boolean ipsEnable = Constants.IPS_ENABLE;

        String uuid = CaptchaUtil.getUUID(); // 防重复提交UUID
        boolean flag = false;

        if (StringUtils.isNotBlank(showBox)) {
            showBox = Encrypt.decrypt3DES(showBox, bidId + Constants.ENCRYPTION_KEY);

            if (showBox.equals(Constants.SHOW_BOX))
                flag = true;
        }

        render(bid, flag, historySituationMap, uItems, user, ipsEnable, uuid);
    }


    /**
     * 投标记录分页ajax方法
     *
     * @param pageNum
     * @param pageSize
     */
    public static void viewBidInvestRecords(int pageNum, int pageSize, String bidIdSign) {

        ErrorInfo error = new ErrorInfo();
        int currPage = pageNum;

        if (params.get("currPage") != null) {
            currPage = Integer.parseInt(params.get("currPage"));
        }

        long bidId = Security.checkSign(bidIdSign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);

        if (error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }

        PageBean<v_invest_records> pageBean = new PageBean<v_invest_records>();
        pageBean = Invest.queryBidInvestRecords(currPage, pageSize, bidId, error);

        if (error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }
        render(pageBean);

    }


    /**
     * 查询借款标的所有提问记录ajax分页方法
     *
     * @param pageNum
     * @param pageSize
     */
    public static void viewBidAllQuestion(int pageNum, int pageSize, String bidIdSign) {

        ErrorInfo error = new ErrorInfo();

        long bidId = Security.checkSign(bidIdSign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);

        if (error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }

        PageBean<BidQuestions> page = BidQuestions.queryQuestion(pageNum, pageSize, bidId, "", Constants.SEARCH_ALL, -1, error);

        if (null == page) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }

        render(page);
    }

    /**
     * 确认投标
     *
     */
    public static void confirmInvest(String sign, String uuid) {
        User user = User.currUser();

        if (null == user)
            LoginAndRegisterAction.login();

        if (user.simulateLogin != null) {
            if (User.currUser().simulateLogin.equalsIgnoreCase(user.encrypt())) {
                flash.error("模拟登录不能进行该操作");
                String url = request.headers.get("referer").value();
                redirect(url);
            } else {
                flash.error("模拟登录超时，请重新操作");
                String url = request.headers.get("referer").value();
                redirect(url);
            }
        }
        ErrorInfo error = new ErrorInfo();

        long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);

        if (bidId < 1) {
            flash.error(error.msg);

            invest(bidId, "");
        }
		
		/* 防重复提交 */
        if (!CaptchaUtil.checkUUID(uuid)) {
            flash.error("请求已提交或请求超时!");

            invest(bidId, "");
        }

        if (Constants.IPS_ENABLE && (User.currUser().getIpsStatus() != IpsCheckStatus.IPS)) {
            CheckAction.approve();
        }

        String investAmountStr = params.get("investAmount");
        String dealpwd = params.get("dealpwd");

        if (StringUtils.isBlank(investAmountStr)) {
            error.msg = "投标金额不能为空！";
            flash.error(error.msg);
            invest(bidId, "");
        }

        boolean b = investAmountStr.matches("^[1-9][0-9]*$");
        if (!b) {
            error.msg = "对不起！只能输入正整数!";
            flash.error(error.msg);
            invest(bidId, "");
        }

        int investAmount = Integer.parseInt(investAmountStr);
        Invest.invest(user.id, bidId, investAmount, dealpwd, false, false, null, error);

        if (error.code == Constants.BALANCE_NOT_ENOUGH) {
            flash.put("code", error.code);
            flash.put("msg", error.msg);

            invest(bidId, "");
        }

        if (error.code < 0) {
            flash.error(error.msg);
            invest(bidId, "");
        }

        Map<String, String> bid = Invest.bidMap(bidId, error);

        if (error.code < 0) {
            flash.error("对不起！系统异常！请您联系平台管理员！");
            invest(bidId, "");
        }

        double minInvestAmount = Double.parseDouble(bid.get("min_invest_amount") + "");
        double averageInvestAmount = Double.parseDouble(bid.get("average_invest_amount") + "");

        if (Constants.IPS_ENABLE) {
            if (error.code < 0) {
                flash.error(error.msg);
                invest(bidId, "");
            }

            if (minInvestAmount == 0) {//认购模式
                investAmount = (int) (investAmount * averageInvestAmount);
            }

            String pMerBillNo = Payment.createBillNo(13L, 2);

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("userId", user.id + "");
            map.put("bidId", bidId + "");
            map.put("investAmount", investAmount + "");
            JSONObject info = JSONObject.fromObject(map);
            IpsDetail.setIpsInfo(Long.parseLong(pMerBillNo), info.toString(), error);

            if (error.code < 0) {
                JPA.setRollbackOnly();
                return;
            }

            Map<String, String> args = Payment.registerCreditor(pMerBillNo, user.id, bidId, 1, investAmount, error);

            render("@front.account.PaymentAction.registerCreditor", args);
        }

        if (minInvestAmount == 0) {//认购模式
            investAmount = (int) (investAmount * averageInvestAmount);
        }

        if (error.code > 0) {
            flash.put("amount", NumberUtil.amountFormat(investAmount));
            String showBox = Encrypt.encrypt3DES(Constants.SHOW_BOX, bidId + Constants.ENCRYPTION_KEY);

            invest(bidId, showBox);
        } else {
            flash.error(error.msg);
            invest(bidId, "");
        }
    }
    /**
     * 确认投标--金豆荚
     */
    public static void confirmInvestApp(){
        ErrorInfo error = new ErrorInfo();
        Map<String, String> args = buildConfirmInvestParams(error, ParseClientUtil.PC);
        if (error.code < 0) {
            MessageVo messageVo = new MessageVo(new Message(Severity.ERROR, MsgCode.CONFIRM_INVEST_FAIL, error.msg));
            Logger.info("确认投标返回：" + JSONObject.fromObject(messageVo).toString());
            renderJSON(JSONObject.fromObject(messageVo).toString());
        }
        Logger.info("确认投标成功===");
        renderTemplate("front/account/PaymentAction/registerCreditor.html", args);
    }

    public static Map<String, String> buildConfirmInvestParams(ErrorInfo error, String client) {
        User user = User.currUser();
        if(null == user){
            error.code = -3;
            error.msg = "未获取到当前用户，请登录后再试";
            return null;
        }
        if(User.currUser().ipsAcctNo == null){
            error.code = -1;
            error.msg = "还未开启资金托管，请前去开户!";
            return null;
        }
        String sign = params.get("sign");
        String uuid = params.get("uuid");
        /* 防重复提交 */
        if(!CaptchaUtil.checkUUID(uuid)){
            error.code = -1;
            error.msg = "请求已提交或请求超时!";
            return null;
        }
        if (StringUtils.isEmpty(sign)) {
            error.code = -1;
            error.msg = "请求参数为空!";
            return null;
        }

        long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
        if(bidId < 1){
            return null;
        }

        String investAmountStr = params.get("investAmount");
        if(StringUtils.isBlank(investAmountStr)){
            error.code = -1;
            error.msg = "投标金额不能为空";
            return null;
        }

        boolean b=investAmountStr.matches("^[1-9][0-9]*$");
        if(!b){
            error.code = -1;
            error.msg = "投标金额只能输入正整数!";
            return null;
        }

        int investAmount = Integer.parseInt(investAmountStr);
        Invest.invest(user.id, bidId, investAmount, null, false, false, null, error);
        if (error.code < 0) {
            return null;
        }

        Map<String, String> bid = Invest.bidMap(bidId, error);
        if (error.code < 0) {
            return null;
        }

        double minInvestAmount = Double.parseDouble(bid.get("min_invest_amount") + "");
        double averageInvestAmount = Double.parseDouble(bid.get("average_invest_amount") + "");

        if(minInvestAmount == 0){//认购模式
            investAmount = (int) (investAmount*averageInvestAmount);
        }

        String pMerBillNo = Payment.createBillNo(13L, 2);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userId", user.id+"");
        map.put("bidId", bidId+"");
        map.put("investAmount", investAmount+"");
        JSONObject info = JSONObject.fromObject(map);
        IpsDetail.setIpsInfo(Long.parseLong(pMerBillNo), info.toString(), error);

        if(error.code < 0) {
            JPA.setRollbackOnly();
            return null;
        }

        Map<String, String> args = Payment.registerCreditorCommon(pMerBillNo, user.id, bidId, 1, investAmount, error, client);

        return args;
    }
    /**
     * 确认投标(页面底部投标按钮)
     *
     */
    public static void confirmInvestBottom(String sign, String uuid) {
        User user = User.currUser();

        if (null == user)
            LoginAndRegisterAction.login();

        if (user.simulateLogin != null) {
            if (User.currUser().simulateLogin.equalsIgnoreCase(user.encrypt())) {
                flash.error("模拟登录不能进行该操作");
                String url = request.headers.get("referer").value();
                redirect(url);
            } else {
                flash.error("模拟登录超时，请重新操作");
                String url = request.headers.get("referer").value();
                redirect(url);
            }
        }
        ErrorInfo error = new ErrorInfo();

        long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);

        if (bidId < 1) {
            flash.error(error.msg);

            invest(bidId, "");
        }
		
		/* 防重复提交 */
        if (!CaptchaUtil.checkUUID(uuid)) {
            flash.error("请求已提交或请求超时!");

            invest(bidId, "");
        }

        if (Constants.IPS_ENABLE && (User.currUser().getIpsStatus() != IpsCheckStatus.IPS)) {
            CheckAction.approve();
        }

        String investAmountStr = params.get("investAmountBottom");
        String dealpwd = params.get("dealpwdBottom");

        if (StringUtils.isBlank(investAmountStr)) {
            error.msg = "投标金额不能为空！";
            flash.error(error.msg);
            invest(bidId, "");
        }

        boolean b = investAmountStr.matches("^[1-9][0-9]*$");
        if (!b) {
            error.msg = "对不起！只能输入正整数!";
            flash.error(error.msg);
            invest(bidId, "");
        }

        int investAmount = Integer.parseInt(investAmountStr);
        Invest.invest(user.id, bidId, investAmount, dealpwd, false, false, null, error);

        if (error.code == Constants.BALANCE_NOT_ENOUGH) {
            flash.put("code", error.code);
            flash.put("msg", error.msg);

            invest(bidId, "");
        }

        if (error.code < 0) {
            flash.error(error.msg);
            invest(bidId, "");
        }

        Map<String, String> bid = Invest.bidMap(bidId, error);

        if (error.code < 0) {
            flash.error("对不起！系统异常！请您联系平台管理员！");
            invest(bidId, "");
        }

        double minInvestAmount = Double.parseDouble(bid.get("min_invest_amount") + "");
        double averageInvestAmount = Double.parseDouble(bid.get("average_invest_amount") + "");

        if (Constants.IPS_ENABLE) {
            if (error.code < 0) {
                flash.error(error.msg);
                invest(bidId, "");
            }

            if (minInvestAmount == 0) {//认购模式
                investAmount = (int) (investAmount * averageInvestAmount);
            }

            String pMerBillNo = Payment.createBillNo(13L, 2);

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("userId", user.id + "");
            map.put("bidId", bidId + "");
            map.put("investAmount", investAmount + "");
            JSONObject info = JSONObject.fromObject(map);
            IpsDetail.setIpsInfo(Long.parseLong(pMerBillNo), info.toString(), error);

            if (error.code < 0) {
                JPA.setRollbackOnly();
                return;
            }

            Map<String, String> args = Payment.registerCreditor(pMerBillNo, user.id, bidId, 1, investAmount, error);

            render("@front.account.PaymentAction.registerCreditor", args);
        }

        if (minInvestAmount == 0) {//认购模式
            investAmount = (int) (investAmount * averageInvestAmount);
        }

        if (error.code > 0) {
            flash.put("amount", NumberUtil.amountFormat(investAmount));
            String showBox = Encrypt.encrypt3DES(Constants.SHOW_BOX, bidId + Constants.ENCRYPTION_KEY);

            invest(bidId, showBox);
        } else {
            flash.error(error.msg);
            invest(bidId, "");
        }
    }


    /**
     * 收藏借款标
     *
     * @param bidId
     */
    public static void collectBid(long bidId) {
        if (User.currUser().simulateLogin != null) {
            if (User.currUser().simulateLogin.equalsIgnoreCase(User.currUser().encrypt())) {
                flash.error("模拟登录不能进行该操作");
                String url = request.headers.get("referer").value();
                redirect(url);
            } else {
                flash.error("模拟登录超时，请重新操作");
                String url = request.headers.get("referer").value();
                redirect(url);
            }
        }

        ErrorInfo error = new ErrorInfo();
        User user = User.currUser();

        Bid.collectBid(user.id, bidId, error);

        JSONObject json = new JSONObject();
        json.put("error", error);
        renderJSON(json);
    }


    /**
     * 查看用户当前的登录状态(异步)
     */
    public static void checkUserStatus() {
        User user = User.currUser();

        JSONObject json = new JSONObject();
		
		/* 是否登录 */
        if (null == user) {
            json.put("status", Constants.NOT_LOGIN);

            renderJSON(json);
        }
		
		/* 是否激活 */
        if (!user.isEmailVerified) {
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
     * 查看(异步)
     */
    public static void showitem(String mark, String signUserId) {
		/* 解密userId */
        ErrorInfo error = new ErrorInfo();
        long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

        if (userId < 1) {
            renderText(error.msg);
        }

        UserAuditItem item = new UserAuditItem();
        item.lazy = true;
        item.userId = userId;
        item.mark = mark;

        render(item);
    }


    /**
     * 取消关注借款标
     *
     * @param attentionId
     */
    public static void cancleBidAttention(Long attentionId) {

        ErrorInfo error = new ErrorInfo();
        Invest.canaleBid(attentionId, error);

        JSONObject json = new JSONObject();

        json.put("error", error);
        renderJSON(json);
    }
}
