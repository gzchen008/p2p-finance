package controllers.mobile;

import business.*;
import constants.Constants;
import constants.SQLTempletes;
import controllers.BaseController;
import controllers.SubmitRepeat;
import controllers.interceptor.H5Interceptor;
import models.*;
import play.db.jpa.JPA;
import play.mvc.With;
import utils.ErrorInfo;
import utils.PageBean;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by libaozhong on 2015/5/14.
 */
@With({H5Interceptor.class, SubmitRepeat.class})
public class TradeController extends BaseController {
    public static void tradeHistory(){
        User user =User.currUser();
        Invest invest=new Invest();
        ErrorInfo error = new ErrorInfo();
        PageBean<v_invest_records> invetResult = invest.queryUserInvestRecords(user.getId(), "1", "100", null, null, error);
        if(error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }
        List<v_invest_records> vInvestRecords = invetResult.page;
        for(v_invest_records v:vInvestRecords){
            v.getStrStatus();
        }
        List<t_user_recharge_details> userRechargeDetails = User.queryRechargeRecordByUserId(user.getId());
        java.util.Date date=new  java.util.Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
         Calendar ca=Calendar.getInstance();
        ca.setTime(date);
        ca.set(Calendar.YEAR,ca.get(Calendar.YEAR)-1);
       String now= sdf.format(date);
        String begin=sdf.format(ca.getTime());
        PageBean<v_user_withdrawals> withdrawalRecord = User.queryWithdrawalRecord(user.getId(), "0", begin, now, null, null, error);
        List<v_user_withdrawals> withdrawals = withdrawalRecord.page;
        render(vInvestRecords, userRechargeDetails,withdrawals);
    }

/*****************跳转到待收金额****************************/
    public static void remainMoney(){
        User user =  User.currUser();
        int payType=1;
        long userId=user.getId();
        ErrorInfo error = new ErrorInfo();
        v_user_account_statistics accountStatistics = User.queryAccountStatistics(userId, error);

        if(error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }

        Optimization.UserOZ accountInfo = new Optimization.UserOZ(userId);

        if(error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }

        List<v_user_details> userDetails = User.queryUserDetail(userId, error);

        if(error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }

        List<UserBankAccounts> userBanks = UserBankAccounts.queryUserAllBankAccount(userId);
        BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
        String content = News.queryContent(Constants.NewsTypeId.VIP_AGREEMENT, error);

        List<t_content_news> news = News.queryNewForFront(Constants.NewsTypeId.MONEY_TIPS, 3, error);
         double totalRemain=0;

        boolean isIps = Constants.IPS_ENABLE;
//        Invest invest=new Invest();
//        PageBean<v_invest_records> investRecords = invest.queryInvestRecords(userId, "1", "100", error);
//        List<v_invest_records> investRecord= investRecords.page;
//        List<v_invest_records> investsBill =new ArrayList<v_invest_records>();
//        java.util.Date time=new  java.util.Date();
//        Double apr=0.0;
//        Integer periodunit=0;
//        Integer period=0;
//        for(v_invest_records v:investRecord)
//            if (v.status == 4) {
//
//                String sql = "select new map(t.repayment_time as repaymenttime, t.apr as apr,t.period_unit as periodunit,t.period as period) from t_bids t where t.id = ?";
//                List<Map<?, ?>> maps = null;
//                EntityManager em = JPA.em();
//                Query query = em.createQuery(sql).setParameter(1, v.bid_id);
//                maps = query.getResultList();
//                if (maps != null && maps.size() > 0) {
//
//                    Map<?, ?> map = maps.get(0);
//                    if (map.size() >= 2) {
//                        v.repayment_time = (java.util.Date) map.get("repaymenttime");
//
//                        apr = (Double) map.get("apr");
//                        periodunit = (Integer) map.get("periodunit");
//                        period = (Integer) map.get("period");
//                        int unit=periodunit;
//                        unit= unit==-1?unit*=-365:unit==0?unit*=30:unit;
//
//                        v.Forecast_earnings = v.invest_amount * unit * apr/365 * period/100;
//
//                    }
//                }
//
//                investsBill.add(v);
//            }
        Map<String,Object> conditionMap = new HashMap<String, Object>();
        List<Object> params = new ArrayList<Object>();
        List<v_bill_invest> bills = new ArrayList<v_bill_invest>();
        StringBuffer sql = new StringBuffer("");
        StringBuffer sqlBill=new StringBuffer("");
        sql.append(SQLTempletes.SELECT);
        sql.append(SQLTempletes.V_BILL_INVEST);
        sql.append(SQLTempletes.LOAN_INVESTBILL_RECEIVE[payType]);
        sql.append("and c.id = ?");
        params.add(userId);
        sql.append(" order by receive_time");
        EntityManager em = JPA.em();
        Query query = em.createNativeQuery(sql.toString(),v_bill_invest.class);
        for(int n = 1; n <= params.size(); n++){
            query.setParameter(n, params.get(n-1));
        }
        bills = query.getResultList();
        for(int i=0;i< bills.size();i++){
            StringBuffer conditions = new StringBuffer(" where b.id = ?");
            sql = new StringBuffer();
            sql.append("select b.status from t_bids b");
            sql.append(conditions);
            params.clear();
            params.add(bills.get(i).bid_id);
            Query queryBill = em.createNativeQuery(sql.toString());

            for(int n = 1; n <= params.size(); n++){
                queryBill.setParameter(n, params.get(n-1));
            }

            int status = Integer.parseInt(queryBill.getResultList().get(0).toString());

            bills.get(i).bidStatus=status;
        }

        List<v_bill_invest> billInvestView=new ArrayList<v_bill_invest>();
             for(v_bill_invest b:bills){
                 if(b.bidStatus==4){
                     totalRemain+=b.income_amounts;
                     billInvestView.add(b);
                 }
             }
//       for(int n = 0;n < investsBill.size();n++){
//           totalRemain+= investsBill.get(n).Forecast_earnings;
//        }
        render(user, accountStatistics, accountInfo, userDetails, userBanks,billInvestView, backstageSet,content, isIps,totalRemain);
    }
    /**
     * 跳转到财富页面冻结金额
     */
    public static void tradeList() {

        User user = User.currUser();
        int payType=1;
        long userId=user.getId();
        ErrorInfo error = new ErrorInfo();
        v_user_account_statistics accountStatistics = User.queryAccountStatistics(userId, error);

        if(error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }

        Optimization.UserOZ accountInfo = new Optimization.UserOZ(userId);

        if(error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }

        List<v_user_details> userDetails = User.queryUserDetail(userId, error);

        if(error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }

        List<UserBankAccounts> userBanks = UserBankAccounts.queryUserAllBankAccount(userId);
        BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
        String content = News.queryContent(Constants.NewsTypeId.VIP_AGREEMENT, error);

        List<t_content_news> news = News.queryNewForFront(Constants.NewsTypeId.MONEY_TIPS, 3, error);

        boolean isIps = Constants.IPS_ENABLE;

        Invest invest=new Invest();
        PageBean<v_invest_records> investRecords = invest.queryInvestRecords(userId, "1", "100", error);
        List<v_invest_records> investRecord= investRecords.page;
             List<v_invest_records> investsBill =new ArrayList<v_invest_records>();
        double InvestAmount=0;
        for(v_invest_records v:investRecord){
            if(v.status==2|| v.status==3){
                InvestAmount+=v.invest_amount;
                investsBill.add(v);
            }
        }
//        List<v_bill_invest> resultBills= Collections.emptyList();
//            resultBills= getResultBills(payType,userId,2);

            render(user, accountStatistics, accountInfo, userDetails, userBanks, backstageSet, content, investsBill, isIps,InvestAmount);
    }

    private static double getInvestAmount( List<v_invest_records> invests,ErrorInfo error) {
        double total=0;
        for(v_invest_records vbi:invests){
            Invest invest=new Invest();
            PageBean<v_invest_records> investResult = invest.queryBidInvestRecords(1, 30, vbi.bid_id, error);
            List<v_invest_records> resut = investResult.page;
            for(v_invest_records v:resut){
                if(v.status==2||v.status==3) {
                    total += v.invest_amount;
                }
            }
        }
        return total;
    }

    public static  List<v_bill_invest> getResultBills(int payType,long userId,int bidStatus ) {
        List<v_bill_invest> resultBills= new ArrayList<v_bill_invest>();
        Map<String,Object> conditionMap = new HashMap<String, Object>();
        List<Object> params = new ArrayList<Object>();
        List<v_bill_invest> bills = new ArrayList<v_bill_invest>();
        StringBuffer sql = new StringBuffer("");

        sql.append(SQLTempletes.SELECT);
        sql.append(SQLTempletes.V_BILL_INVEST);
        sql.append(SQLTempletes.LOAN_INVESTBILL_RECEIVE[payType]);
        sql.append("and c.id = ?");
        params.add(userId);
        sql.append(" group by receive_time");
        EntityManager em = JPA.em();
        Query query = em.createNativeQuery(sql.toString(),v_bill_invest.class);
        for(int n = 1; n <= params.size(); n++){
            query.setParameter(n, params.get(n-1));
        }
        bills = query.getResultList();

        for(int i=0;i< bills.size();i++){
            Bid bid=new Bid();
            bid.setId(bills.get(i).bid_id);

            int status =bid.status;
            if(status==bidStatus ){
                bills.get(i).apr=bid.apr;
                bills.get(i).bidStatus=status;
                bills.get(i).bidTime= bid.time;
                bills.get(i).invest_period=bid.investPeriod;
                bills.get(i).period_unit=bid.periodUnit;
                resultBills.add(bills.get(i));
            }

        }
        return resultBills;
    }
}
