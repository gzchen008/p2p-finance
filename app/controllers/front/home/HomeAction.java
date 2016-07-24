package controllers.front.home;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import business.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import business.Bid.Repayment;
import models.t_content_advertisements;
import models.t_content_news;
import models.v_bill_board;
import models.v_front_all_bids;
import constants.Constants;
import constants.OptionKeys;
import controllers.BaseController;
import play.Logger;
import play.db.jpa.JPABase;
import utils.Arith;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * @author liuwenhui
 */
public class HomeAction extends BaseController {


    /*网站首页*/
    public static void home() {
        Logger.info("HomeAction.home()");

        ErrorInfo error = new ErrorInfo();

        List<t_content_advertisements> homeAds = Ads.queryAdsByLocation(Constants.HOME_PAGE_PC, error); // 广告条

        List<v_front_all_bids> bidList = Invest.queryBids();//首页最新五个借款标

        List<v_front_all_bids> agencyBids = Invest.queryAgencyBids();//机构借款标

        List<v_bill_board> investBillboard = Invest.investBillboard();//理财风云榜

        List<t_content_news> successStorys = News.queryNewForFront(12l, 2, error);//首页成功故事

        List<t_content_news> investSkills = News.queryNewForFront(10l, 5, error);//首页借款技巧

        List<t_content_news> loanSkills = News.queryNewForFront(11l, 5, error);//首页理财技巧

        List<News> mediaReportNews = News.findMediaReportNews(error);//媒体报道
        List<News> latestNews = News.findLatestNews(error);//最新动态

        List<t_content_news> news = News.queryNewForFront(7l, 5, error);//首页官方公告

        List<Bid> bids = Bid.queryAdvertisement(error); // 最新投资资讯

        List<Map<String, String>> maps = Invest.queryNearlyInvest(error);

        List<AdsEnsure> adsEnsure = AdsEnsure.queryEnsureForFront(error); //四大安全保障

        List<AdsPartner> adsPartner = AdsPartner.qureyPartnerForFront(error);//合作伙伴

        List<NewsType> types = NewsType.queryChildTypes(1, error);

        Long userCount = User.findUserCount(error);  //客户数量

        double totalVolume = Bid.findTotalVolumeOfBids(error); //累计成交额

        double loanLossProvision = Bid.findLoanLossProvision(error);

        render(homeAds, bidList, mediaReportNews, latestNews, news, bids, adsEnsure, adsPartner, types, maps, userCount, totalVolume, loanLossProvision);
    }

    public static void banner() {
        ErrorInfo error = new ErrorInfo();
        List<t_content_advertisements> homeAds = Ads.queryAdsByLocation(Constants.HOME_PAGE_PC, error); // 广告条

        renderJSON(homeAds);
    }

    /**
     * 财富工具箱
     */
    public static void wealthToolkit(int key) {
        ErrorInfo error = new ErrorInfo();
        List<Product> products = Product.queryProductNames(true, error);

        List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);

        render(key, products, creditLevels);
    }

    /**
     * 信用计算器
     */
    public static void wealthToolkitCreditCalculator() {
        ErrorInfo error = new ErrorInfo();

        List<AuditItem> auditItems = AuditItem.queryAuditItems(error);

        String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); // 得到积分对应的借款额度值
        double amountKey = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value);

        render(auditItems, amountKey);
    }

    /**
     * 还款计算器
     */
    public static void wealthToolkitRepaymentCalculator() {
        List<Repayment> rtypes = Repayment.queryRepaymentType(null); // 还款类型

        render(rtypes);
    }

    /**
     * 还款明细(异步)
     */
    public static void repaymentCalculate(double amount, double apr, int period, int periodUnit, int repaymentType) {
        List<Map<String, Object>> payList = null;

        payList = Bill.repaymentCalculate(amount, apr, period, periodUnit, repaymentType);

        render(payList);
    }

    /**
     * 净值计算器
     */
    public static void wealthToolkitNetValueCalculator() {
        ErrorInfo error = new ErrorInfo();

        double bailScale = Product.queryNetValueBailScale(error); // 得到净值产品的保证金比例

        render(bailScale);
    }

    /**
     * 利率计算器
     */
    public static void wealthToolkitAPRCalculator() {
        ErrorInfo error = new ErrorInfo();

        List<Repayment> rtypes = Repayment.queryRepaymentType(null); // 还款类型

        String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); // 得到积分对应的借款额度值
        double serviceFee = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value);

        render(rtypes, serviceFee);
    }

    /**
     * 利率计算器,计算年华收益、总利益(异步)
     */
    public static void aprCalculator(double amount, double apr, int repaymentType, double award, int rperiod) {
        ErrorInfo error = new ErrorInfo();
        DecimalFormat df = new DecimalFormat("#.00");

        double managementRate = BackstageSet.getCurrentBackstageSet().investmentFee / 100;//系统管理费费率
        double earning = 0;

        if (repaymentType == 1) {/* 按月还款、等额本息 */
            double monRate = apr / 12;// 月利率
            int monTime = rperiod;
            double val1 = amount * monRate * Math.pow((1 + monRate), monTime);
            double val2 = Math.pow((1 + monRate), monTime) - 1;
            double monRepay = val1 / val2;// 每月偿还金额

            /**
             * 年化收益
             */
            earning = Arith.excelRate((amount - award),
                    Double.parseDouble(df.format(monRepay)), monTime, 200, 15) * 12 * 100;
            earning = Double.parseDouble(df.format(earning) + "");
        }

        if (repaymentType == 2 || repaymentType == 3) { /* 按月付息、一次还款   */
            double monRate = apr / 12;// 月利率
            int monTime = rperiod;// * 12;借款期限填月
            double borrowSum = Double.parseDouble(df.format(amount));
            double monRepay = Double.parseDouble(df.format(borrowSum * monRate));// 每月偿还金额
            double allSum = Double.parseDouble(df.format((monRepay * monTime)))
                    + borrowSum;// 还款本息总额
            earning = Arith.rateTotal(allSum,
                    (borrowSum - award), monTime) * 100;
            earning = Double.parseDouble(df.format(earning) + "");
        }


        JSONObject obj = new JSONObject();
        obj.put("managementRate", managementRate < 0 ? 0 : managementRate);
        obj.put("earning", earning);

        renderJSON(obj);
    }

    /**
     * 服务手续费
     */
    public static void wealthToolkitServiceFee() {
        ErrorInfo error = new ErrorInfo();
        String content = News.queryContent(-1011L, error);
        flash.error(error.msg);

        renderText(content);
    }

    /**
     * 超额借款
     */
    public static void wealthToolkitOverLoad() {
        ErrorInfo error = new ErrorInfo();

        List<AuditItem> auditItems = AuditItem.queryAuditItems(error);

        String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); // 得到积分对应的借款额度值
        double amountKey = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value);

        render(auditItems, amountKey);
    }

    /**
     * 新手入门
     */
    public static void getStart(int id) {
        ErrorInfo error = new ErrorInfo();

        String content = News.queryContent(id, error);

        List<Product> products = Product.queryProductNames(true, error);

        List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);

        render(content, products, creditLevels, id);
    }

    /**
     * 关于我们
     */
    public static void aboutUs(int id) {
        ErrorInfo error = new ErrorInfo();

        List<Product> products = Product.queryProductNames(true, error);
        List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);

        Object[] investData = News.queryInvestDataSum();
        String content = null;
        List<String> contentList = null;

        List<News> mediaReportNews = null;
        List<News> latestNews = null;
        switch (id) {
            case -1010:
                latestNews = News.findLatestNews(error);
                break;
            case -1009:
                mediaReportNews = News.findMediaReportNews(error);
                break;
            case -1006:
                break;
            case -1005:
                contentList = News.queryContentList(id, error);
                break;
            default:
                content = News.queryContentByTypeId(id, error);
                break;
        }

        NewsType parent = new NewsType();
        parent.id = 3;
        List<NewsType> types = NewsType.queryChildTypes(3, error);
        render(content, contentList, investData, products, creditLevels, parent, types, id, mediaReportNews, latestNews);
    }

    /**
     * 媒体报道列表
     */
    public static void newsDetail(Long newsId) {
        ErrorInfo error = new ErrorInfo();
        Object[] investData = News.queryInvestDataSum();
        List<NewsType> types = NewsType.queryChildTypes(3, error);
        t_content_news content = t_content_news.findById(newsId);
        News news = new News();
        news.id = content.id;
        news.time = content.time;
        news.title = content.title;
        news.content = content.content;
        news.readCount = content.read_count;
        news.time = content.time;
        render(types, news, investData);
    }

    /**
     * 理财风云榜（更多）
     */
    public static void moreInvest(int currPage) {
        ErrorInfo error = new ErrorInfo();
        PageBean<v_bill_board> page = Invest.investBillboards(currPage, error);

        if (error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }

        render(page);
    }

    /**
     * 招贤纳士
     */
    public static void careers() {

    }

    /**
     * 管理团队
     */
    public static void managementTeam() {

    }

    /**
     * 专家顾问
     */
    public static void expertAdvisor() {

    }
}
