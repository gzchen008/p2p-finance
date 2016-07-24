package business;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.*;
import org.apache.commons.lang.StringUtils;
import com.shove.Convert;
import com.shove.security.Encrypt;
import net.sf.json.JSONObject;
import constants.Constants;
import constants.DealType;
import constants.IPSConstants;
import constants.OptionKeys;
import constants.SQLTempletes;
import constants.Templets;
import constants.UserEvent;
import constants.IPSConstants.IPSOperation;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.libs.WS;
import utils.Arith;
import utils.CnUpperCaser;
import utils.Converter;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import utils.QueryUtil;
import utils.PushMessage;
import utils.Security;
import utils.ServiceFee;

/**
 * 投资业务实体类
 *
 * @author lwh
 * @version 6.0
 * @created 2014-3-27 下午03:31:06
 */
public class Invest implements Serializable {
    private long _id;
    public long id;
    public String merBillNo;
    public String ipsBillNo;
    public long userId;
    public String userIdSign; // 加密ID
    public Date time;
    public long bidId;
    public double amount;
    public double fee;
    public int transferStatus;
    public String status;
    public long transfersId;
    public boolean isAutomaticInvest;

    public User user;
    public Bid bid;


    /**
     * 获取加密投资者ID
     *
     * @return
     */
    public String getUserIdSign() {
        return Security.addSign(this.userId, Constants.USER_ID_SIGN);
    }

    public void setUserId(long userId) {
        this.userId = userId;
        this.user = new User();
        this.user.id = userId;
    }

    public void setBidId(long bidId) {
        this.bidId = bidId;
        this.bid = new Bid();
        this.bid.id = bidId;
    }


    public long getId() {
        return _id;
    }

    public void setId(long id) {

        t_invests invests = null;
        try {
            invests = t_invests.findById(id);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.info(e.getMessage());
        }

        if (null == invests) {
            this._id = -1;

            return;
        }
        this._id = invests.id;
        this.userId = invests.user_id;
        this.time = invests.time;
        this.bidId = invests.bid_id;
        this.amount = invests.amount;
        this.fee = invests.fee;
        this.transferStatus = invests.transfer_status;

        if (invests.transfer_status == 0) {
            this.status = "正常";
        }

        if (invests.transfer_status == -1) {
            this.status = "已转让出";
        }

        if (invests.transfer_status == 0) {
            this.status = "转让中";
        }

        this.transfersId = invests.transfers_id;
        this.isAutomaticInvest = invests.is_automatic_invest;
    }


    public Invest() {

    }

    /**
     * 针对某个标的投标记录
     *
     * @return
     */
    public static PageBean<v_invest_records> queryBidInvestRecords(int currPage, int pageSize, long bidId, ErrorInfo error) {

        PageBean<v_invest_records> pageBean = new PageBean<v_invest_records>();
        List<v_invest_records> list = new ArrayList<v_invest_records>();
        pageBean.currPage = currPage;
        pageBean.pageSize = pageSize;
        StringBuffer sql = new StringBuffer("");
        sql.append(SQLTempletes.PAGE_SELECT);
        sql.append(SQLTempletes.V_INVEST_RECORDS);
        sql.append(" and bid_id = ?");
        sql.append(" order by time desc");
        List<Object> params = new ArrayList<Object>();
        params.add(bidId);

        try {
            EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(), v_invest_records.class);
            for (int n = 1; n <= params.size(); n++) {
                query.setParameter(n, params.get(n - 1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            list = query.getResultList();

            pageBean.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

        } catch (Exception e) {
            e.printStackTrace();
            Logger.info(e.getMessage());
            error.code = -1;

            return pageBean;
        }

        pageBean.page = list;
        error.code = 1;
        return pageBean;

    }


    /**
     * @param showType
     * @param currPage
     * @param pageSize
     * @param _apr
     * @param _amount
     * @param _loanSchedule
     * @param _startDate
     * @param _endDate
     * @param _loanType
     * @param minLevelStr
     * @param maxLevelStr
     * @param _orderType
     * @param _keywords
     * @param _period
     * @param _status
     * @param error
     * @return
     */
    public static PageBean<v_front_all_bids> queryAllBids(int showType, int currPage, int pageSize, String _apr, String _amount, String _loanSchedule, String _startDate, String _endDate, String _loanType, String minLevelStr, String maxLevelStr, String _orderType, String _keywords, String _period, String _status, ErrorInfo error) {

//        int apr = 0;
//        int amount = 0;
//        int loan_schedule = 0;
//        int orderType = 0;
//        int product_id = 0;
//        int minLevel = 0;
//        int maxLevel = 0;
//
//        int period = 0;
//        int status = 0;

        List<v_front_all_bids> bidList = new ArrayList<v_front_all_bids>();
        PageBean<v_front_all_bids> page = new PageBean<v_front_all_bids>();

        EntityManager em = JPA.em();
//        String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
//        obj = obj == null ? "" : obj;

//        Map<String, Object> conditionMap = new HashMap<String, Object>();
//        conditionMap.put("keywords", _keywords);

        page.pageSize = pageSize;
        page.currPage = currPage;

        StringBuffer sql = new StringBuffer("");
        sql.append("select * from v_bids_info");
//        sql.append(SQLTempletes.SELECT);
//        sql.append(SQLTempletes.V_FRONT_ALL_BIDS_CREDIT);

        List<Object> params = new ArrayList<Object>();

//        if (NumberUtil.isNumericInt(_apr)) {
//            apr = Integer.parseInt(_apr);
//        }
//
//        if (apr < 0 || apr > 4) {
//            sql.append(SQLTempletes.BID_APR_CONDITION[0]);// 全部范围
//        } else {
//            sql.append(SQLTempletes.BID_APR_CONDITION[apr]);
//        }
//
//        if (NumberUtil.isNumericInt(_amount)) {
//            amount = Integer.parseInt(_amount);
//        }
//
//        if (!StringUtils.isBlank(_keywords)) {
//            sql.append(" and t_bids.title like ? or t_bids.id like ? ");
//            params.add("%" + _keywords + "%");
//            _keywords = _keywords.replace(obj + "", "");
//            params.add("%" + _keywords + "%");
//        }
//
//        if (amount < 0 || amount > 5) {
//            sql.append(SQLTempletes.BID_MIN_INVEST_AMOUNT_CONDITION[0]);// 全部范围
//        } else {
//            sql.append(SQLTempletes.BID_MIN_INVEST_AMOUNT_CONDITION[amount]);
//        }
//
//        if (NumberUtil.isNumericInt(_loanSchedule)) {
//            loan_schedule = Integer.parseInt(_loanSchedule);
//        }
//
//        if (loan_schedule < 0 || loan_schedule > 4) {
//            sql.append(SQLTempletes.BID_LOAN_SCHEDULE_CONDITION[0]);//全部范围
//        } else {
//            sql.append(SQLTempletes.BID_LOAN_SCHEDULE_CONDITION[loan_schedule]);
//        }
//
//        if (NumberUtil.isNumericInt(_loanType)) {
//            product_id = Integer.parseInt(_loanType);
//            if (product_id > 0) {
//                sql.append(" and t_products.id = ? ");
//                params.add(product_id);
//            }
//
//        }
//
//        if (NumberUtil.isNumericInt(minLevelStr)) {
//            minLevel = Integer.parseInt(minLevelStr);
//            if (minLevel > 0) {
//                sql.append(" AND t_users.credit_level_id = ?");
//                params.add(minLevel);
//            }
//
//        }
//
//
//        if (NumberUtil.isNumericInt(maxLevelStr)) {
//            maxLevel = Integer.parseInt(maxLevelStr);
//            if (maxLevel > 0) {
//                sql.append(" and ? <= `f_credit_levels`(`t_bids`.`user_id`)");
//                params.add(maxLevel);
//            }
//
//        }
//
//        if (!StringUtils.isBlank(_startDate) && !StringUtils.isBlank(_endDate)) {
//            sql.append(" and t_bids.repayment_time >= ? and  t_bids.repayment_time <= ? ");
//            params.add(DateUtil.strDateToStartDate(_startDate));
//            params.add(DateUtil.strDateToEndDate(_endDate));
//        }
//
//        if (showType == Constants.SHOW_TYPE_1) {
//            sql.append(" and t_bids.show_type in (1,3) ");
//        } else {
//            sql.append(" and t_bids.show_type in (2,3) ");
//        }
//        if (Constants.IS_BIDS_NEED_FILTER) {
//            sql.append(" and t_bids.time > '" + Constants.BIDS_CREATETIME + "' and t_users.name = '" + Constants.BIDS_MOBILE + "' ");
//        }
//
//
//        if (NumberUtil.isNumericInt(_period)) {
//            period = Integer.parseInt(_period);
//            if (period > 0 && period < 6) {
//                sql.append("and ((CASE WHEN (t_bids.period_unit = 1) THEN t_bids.period WHEN (t_bids.period_unit = 0) THEN t_bids.period * 30 WHEN (t_bids.period_unit = -1) THEN t_bids.period * 365 ELSE 0 END) " + SQLTempletes.BID_PERIOD_CONDITION[period] + ")");
//            } else {
//                sql.append(SQLTempletes.BID_PERIOD_CONDITION[0]);
//            }
//
//        }
//
//        if (NumberUtil.isNumericInt(_status)) {
//            status = Integer.parseInt(_status);
//            if (status > 0 && period < 6) {
//                sql.append(SQLTempletes.BID_STATUS_CONDITION[status]);
//            } else {
//                sql.append(SQLTempletes.BID_STATUS_CONDITION[0]);
//            }
//
//        }
//
//        if (NumberUtil.isNumericInt(_orderType)) {
//            orderType = Integer.parseInt(_orderType);
//        }
//
//        if (orderType < 0 || orderType > 5) {
//            sql.append(Constants.BID_ORDER_CONDITION[0]);
//        } else {
//            sql.append(Constants.BID_ORDER_CONDITION[orderType]);
//        }
//
//        conditionMap.put("apr", apr);
//        conditionMap.put("amount", amount);
//        conditionMap.put("loanSchedule", loan_schedule);
//        conditionMap.put("startDate", _startDate);
//        conditionMap.put("endDate", _endDate);
//        conditionMap.put("minLevel", minLevel);
//        conditionMap.put("maxLevel", maxLevel);
//        conditionMap.put("orderType", orderType);
//        conditionMap.put("loanType", product_id);
//
//        conditionMap.put("period", period);
//        conditionMap.put("status", status);

        try {
            Query query = em.createNativeQuery(sql.toString(), v_front_all_bids.class);
            for (int n = 1; n <= params.size(); n++) {
                query.setParameter(n, params.get(n - 1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bidList = query.getResultList();

            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

        } catch (Exception e) {
            e.printStackTrace();
            error.msg = "系统异常，给您带来的不便敬请谅解！";
            error.code = -2;
        }

        error.code = 1;
        error.msg = "查询成功";
        page.page = bidList;
        // page.conditions = conditionMap;

        return page;
    }

    public static PageBean<v_bids_info> queryAllBids1(int showType, int currPage, int pageSize, String _apr, String _amount, String _loanSchedule, String _startDate, String _endDate, String _loanType, String minLevelStr, String maxLevelStr, String _orderType, String _keywords, String _period, String _status, ErrorInfo error) {
        List<v_bids_info> bidList = new ArrayList<v_bids_info>();
        PageBean<v_bids_info> page = new PageBean<v_bids_info>();
        EntityManager em = JPA.em();

        page.pageSize = pageSize;
        page.currPage = currPage;

        StringBuffer sql = new StringBuffer("");
        sql.append("select * from v_bids_info");

        List<Object> params = new ArrayList<Object>();
        try {
            Query query = em.createNativeQuery(sql.toString(), v_bids_info.class);
            for (int n = 1; n <= params.size(); n++) {
                query.setParameter(n, params.get(n - 1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bidList = query.getResultList();

            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

        } catch (Exception e) {
            e.printStackTrace();
            error.msg = "系统异常，给您带来的不便敬请谅解！";
            error.code = -2;
        }

        error.code = 1;
        error.msg = "查询成功";
        page.page = bidList;
        // page.conditions = conditionMap;

        return page;
    }

    /**
     * 理财首页用户收藏的所有借款标
     */
    public static PageBean<v_front_user_attention_bids> queryAllCollectBids(long userId, int currPage, int pageSize, ErrorInfo error) {

        List<v_front_user_attention_bids> bidList = new ArrayList<v_front_user_attention_bids>();
        PageBean<v_front_user_attention_bids> page = new PageBean<v_front_user_attention_bids>();
        page.pageSize = pageSize;
        page.currPage = currPage;

        StringBuffer sql = new StringBuffer("");
        sql.append(SQLTempletes.PAGE_SELECT);
        sql.append(SQLTempletes.V_FRONT_USER_ATTENTION_BIDS);
        sql.append(" and t_user_attention_bids.user_id = ?");

        List<Object> params = new ArrayList<Object>();
        params.add(userId);

        try {
            EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(), v_front_user_attention_bids.class);
            for (int n = 1; n <= params.size(); n++) {
                query.setParameter(n, params.get(n - 1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bidList = query.getResultList();

            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

        } catch (Exception e) {
            e.printStackTrace();
            Logger.info(e.getMessage());
            error.code = -1;

            return page;
        }
        error.code = 1;
        page.page = bidList;

        return page;
    }


    /**
     * 查询用户所有投资记录
     *
     * @param userId
     * @return
     */
    public static PageBean<v_invest_records> queryUserInvestRecords(long userId, String currPageStr, String pageSizeStr, String typeStr, String paramter, ErrorInfo error) {

        int type = 0;
        String[] typeCondition = {" and (t_invests.bid_id like ? or bid_user.name like ?)", " and  t_invests.bid_id like ? ", " and bid_user.name like ? "};

        List<v_invest_records> investRecords = new ArrayList<v_invest_records>();
        PageBean<v_invest_records> page = new PageBean<v_invest_records>();
        int currPage = Constants.ONE;
        int pageSize = Constants.TEN;

        if (NumberUtil.isNumericInt(currPageStr)) {
            currPage = Integer.parseInt(currPageStr);
        }

        if (NumberUtil.isNumericInt(pageSizeStr)) {
            pageSize = Integer.parseInt(pageSizeStr);
        }

        if (NumberUtil.isNumericInt(typeStr)) {
            type = Integer.parseInt(typeStr);
        }

        StringBuffer sql = new StringBuffer("");
        sql.append(SQLTempletes.PAGE_SELECT);

        //在嵌套查询语句中，后者查询的结果必须要有关键字select
        sql.append(SQLTempletes.V_INVEST_RECORDS);
        sql.append(" and t_invests.user_id=? ");
        List<Object> params = new ArrayList<Object>();
        params.add(userId);

        EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;

        Map<String, Object> conditionMap = new HashMap<String, Object>();
        conditionMap.put("keyWords", paramter);

        if (typeStr == null && paramter == null) {
            sql.append(" order by time desc");
            try {
                Query query = em.createNativeQuery(sql.toString(), v_invest_records.class);
                for (int n = 1; n <= params.size(); n++) {
                    query.setParameter(n, params.get(n - 1));
                }
                query.setFirstResult((currPage - 1) * pageSize);
                query.setMaxResults(pageSize);
                investRecords = query.getResultList();

                page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

            } catch (Exception e) {
                e.printStackTrace();

                return page;
            }
            page.page = investRecords;
            page.pageSize = pageSize;
            page.currPage = currPage;
            error.code = 1;
            return page;
        }

        if (StringUtils.isNotBlank(typeStr)) {
            type = Integer.parseInt(typeStr);
        }

        if (type < 0 || type > 2) {
            type = 0;
        }

        if (type == 0) {
            sql.append(typeCondition[0]);
            params.add("%" + paramter + "%");
            paramter = paramter.replace(obj + "", "");
            params.add("%" + paramter + "%");
        } else {
            sql.append(typeCondition[type]);
            if (type == 1) {
                paramter = paramter.replace(obj + "", "");
            }
            params.add("%" + paramter + "%");
        }

        sql.append(" order by time desc");
        try {
            Query query = em.createNativeQuery(sql.toString(), v_invest_records.class);
            for (int n = 1; n <= params.size(); n++) {
                query.setParameter(n, params.get(n - 1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            investRecords = query.getResultList();

            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

        } catch (Exception e) {
            e.printStackTrace();
            error.code = -1;
            return page;
        }
        conditionMap.put("type", type);

        page.conditions = conditionMap;
        page.page = investRecords;
        page.pageSize = pageSize;
        page.currPage = currPage;
        error.code = 1;
        return page;
    }

    /**
     * 查询用户所有投资记录(AJAX)
     *
     * @param userId
     * @return
     */
    public static PageBean<v_invest_records> queryInvestRecords(long userId, String currPageStr, String pageSizeStr, ErrorInfo error) {
        error.clear();

        int currPage = Constants.ONE;
        int pageSize = Constants.TWO;

        if (NumberUtil.isNumericInt(currPageStr)) {
            currPage = Integer.parseInt(currPageStr);
        }

        if (NumberUtil.isNumericInt(pageSizeStr)) {
            pageSize = Integer.parseInt(pageSizeStr);
        }

        PageBean<v_invest_records> page = new PageBean<v_invest_records>();
        page.currPage = currPage;
        page.pageSize = pageSize;

        List<v_invest_records> recordDetails = new ArrayList<v_invest_records>();
        StringBuffer sql = new StringBuffer("");
        sql.append(SQLTempletes.PAGE_SELECT);
        sql.append(SQLTempletes.V_INVEST_RECORDS);
        sql.append(" and `t_invests`.user_id  = ? ");
        sql.append(" order by time desc");
        List<Object> params = new ArrayList<Object>();
        params.add(userId);
        try {
            EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(), v_invest_records.class);
            for (int n = 1; n <= params.size(); n++) {
                query.setParameter(n, params.get(n - 1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            recordDetails = query.getResultList();

            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("查询查询用户所有投资记录时：" + e.getMessage());
            error.code = -1;
            error.msg = "用户所有投资记录查询失败";
        }

        page.page = recordDetails;
        error.code = 1;
        return page;
    }

    /**
     * 查询理财交易总数
     *
     * @param error
     * @return
     */
    public static long queryTotalInvestCount(ErrorInfo error) {
        error.clear();
        Object[] objs;
        long count = 0;
        Object investCount;
        Object transferCount;

        String sql = "SELECT investCount,transferCount FROM((SELECT COUNT(id) AS investCount FROM t_invest_transfers WHERE `status` = ?) AS investCount,(SELECT COUNT(id) AS transferCount FROM t_invests WHERE bid_id IN (?, ?, ?, ?, ?, ?)) AS transferCount)";
        Query query = JPA.em().createNativeQuery(sql).setParameter(1, Constants.DEBT_SUCCESS).setParameter(2, Constants.BID_ADVANCE_LOAN).setParameter(3, Constants.BID_FUNDRAISE).setParameter(4, Constants.BID_EAIT_LOAN).setParameter(5, Constants.BID_REPAYMENT).setParameter(6, Constants.BID_REPAYMENTS).setParameter(7, Constants.BID_COMPENSATE_REPAYMENT);

        try {
            objs = (Object[]) query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.info("数据库异常");
            error.code = -1;
            error.msg = "查询理财交易总数失败";

            return -1;
        }

        if (null != objs && objs.length > 1) {
            investCount = objs[0];
            transferCount = objs[1];

            if (null != investCount && null != transferCount) {
                count = Long.parseLong(investCount.toString()) + Long.parseLong(transferCount.toString());
            }
        }

        error.code = 1;
        return count;
    }

    /**
     * 查询理财交易总金额
     *
     * @param error
     * @return
     */
    public static double queryTotalDealAmount(ErrorInfo error) {
        error.clear();

        Object[] objs;
        Object investAmount;
        Object transferAmount;
        double amount = 0;

        String sql = "SELECT transferAmount,investAmount FROM((SELECT SUM(debt_amount) AS transferAmount FROM t_invest_transfers WHERE `status` = ?) AS transferAmount,(SELECT SUM(amount) AS investAmount FROM t_invests WHERE bid_id IN (?, ?, ?, ?, ?, ?)) AS investAmount)";
        Query query = JPA.em().createNativeQuery(sql).setParameter(1, Constants.DEBT_SUCCESS).setParameter(2, Constants.BID_ADVANCE_LOAN).setParameter(3, Constants.BID_FUNDRAISE).setParameter(4, Constants.BID_EAIT_LOAN).setParameter(5, Constants.BID_REPAYMENT).setParameter(6, Constants.BID_REPAYMENTS).setParameter(7, Constants.BID_COMPENSATE_REPAYMENT);

        try {
            objs = (Object[]) query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.info("数据库异常");
            error.code = -1;
            error.msg = "查询理财交易总金额失败";

            return -1;
        }

        if (null != objs && objs.length > 1) {
            transferAmount = objs[0];
            investAmount = objs[1];

            if (null != transferAmount && null != investAmount) {
                amount = Double.parseDouble(transferAmount.toString()) + Double.parseDouble(investAmount.toString());
            }
        }

        error.code = 1;
        return amount;
    }


    /**
     * 关闭投标机器人
     *
     * @param robotId
     * @param error
     * @return
     */
    public static int closeRobot(long userId, long robotId, ErrorInfo error) {

        EntityManager em = JPA.em();
//		User user = User.currUser();

        try {
            int rows = em.createQuery(" update t_user_automatic_invest_options set status = 0 where id = ?").setParameter(1, robotId).executeUpdate();

            if (rows == 0) {
                JPA.setRollbackOnly();
                error.msg = "关闭投标机器人失败！";
                error.code = -1;

                return error.code;
            }
        } catch (Exception e) {
            e.printStackTrace();
            error.msg = "关闭投标机器人失败！";
            error.code = -1;

            return error.code;
        }

        DealDetail.userEvent(userId, UserEvent.CLOSE_ROBOT, "关闭投标机器人", error);

        if (error.code < 0) {
            JPA.setRollbackOnly();

            return error.code;
        }

        error.msg = "关闭投标机器人成功！";
        error.code = 1;

        return error.code;

    }


    /**
     * 设置或修改自动投标机器人
     *
     * @param userId
     * @param bidAmount
     * @param rateStart
     * @param rateEnd
     * @param deadlineStart
     * @param deadlineEnd
     * @param creditStart
     * @param creditEnd
     * @param remandAmount
     * @param borrowWay
     */
    public static int saveOrUpdateRobot(long userId, int validType, int validDate, double minAmount, double maxAmount, String bidAmount, String rateStart, String rateEnd, String deadlineStart, String deadlineEnd, String creditStart, String creditEnd,
                                        String remandAmount, String borrowWay, ErrorInfo error) {

        error.clear();
        String sql = "select balance from t_users where id = ?";
        Double balance = 0.0;

        t_user_automatic_invest_options robot = null;

        try {
            balance = t_users.find(sql, userId).first();

			/*查询用户是否设置过自动投标*/
            robot = t_user_automatic_invest_options.find(" user_id = ? ", userId).first();
        } catch (Exception e) {
            error.msg = "对不起！系统异常!";
            error.code = -1;

            return error.code;
        }

        if (validType != 0 && validType != 1) {
            error.msg = "非法参数";
            error.code = -1;

            return error.code;
        }

        if (validDate <= 0) {
            error.msg = "请选择有效期";
            error.code = -1;

            return error.code;
        }

        if (minAmount < IPSConstants.MIN_AMOUNT) {
            error.msg = "借款额度必须大于" + IPSConstants.MIN_AMOUNT;
            error.code = -1;

            return error.code;
        }

        if (minAmount > maxAmount) {
            error.msg = "最高借款额度不能小于最低借款额度";
            error.code = -1;

            return error.code;
        }

        if (robot == null) {
            t_user_automatic_invest_options robotNew = new t_user_automatic_invest_options();
            robotNew.user_id = userId;
            robotNew.min_interest_rate = Double.parseDouble(rateStart);
            robotNew.max_interest_rate = Double.parseDouble(rateEnd);

            if (Double.parseDouble(rateEnd) < Double.parseDouble(rateStart)) {
                error.msg = "对不起！您设置的利率上限不能小于利率下限！";
                error.code = -1;

                return error.code;
            }

            if (null != deadlineStart) {
                robotNew.min_period = Integer.parseInt(deadlineStart);
            }

            if (null != deadlineEnd) {
                robotNew.max_period = Integer.parseInt(deadlineEnd);

                if (Integer.parseInt(deadlineEnd) < Integer.parseInt(deadlineStart)) {
                    error.msg = "对不起！您设置的借款期限上限不能小于借款期限下限！";
                    error.code = -2;

                    return error.code;
                }
            }

            if (null != creditStart) {
                robotNew.min_credit_level_id = Integer.parseInt(creditStart);
            }

            if (null != creditEnd) {
                robotNew.max_credit_level_id = Integer.parseInt(creditEnd);

                if (Integer.parseInt(creditEnd) >= Integer.parseInt(creditStart)) {
                    error.msg = "对不起！您设置的最高信用等级不能低于最低信用等级！";
                    error.code = -3;
                }
            }

            if (balance < Double.parseDouble(remandAmount)) {
                error.msg = "对不起！您预留金额不能大于您的可用余额！";
                error.code = -4;
                return error.code;
            }

            if (Double.parseDouble(bidAmount) > balance) {
                error.msg = "对不起！您设置的投标金额不能大于您的可用余额！";
                error.code = -5;
                return error.code;
            }

            if (Double.parseDouble(bidAmount) + Double.parseDouble(remandAmount) > balance) {
                error.msg = "对不起！您设置的投标金额和投标金额总和不能大于您的可用余额！";
                error.code = -5;
                return error.code;
            }

            if (null == remandAmount) {
                error.msg = "对不起！您预留金额不能为空！";
                error.code = -6;
                return error.code;
            }

            if (null == bidAmount) {
                error.msg = "对不起！每次投标金额不能为空！";
                error.code = -7;
                return error.code;
            }

            if (null == borrowWay) {
                error.msg = "对不起！借款类型不能为空！";
                error.code = -8;

                return error.code;
            }

            if (Double.parseDouble(bidAmount) < 0) {
                error.msg = "对不起！您设置的投标金额应该大于0！";
                error.code = -9;
                return error.code;
            }

            if (0 > Double.parseDouble(remandAmount)) {
                error.msg = "对不起！您预留金额不能小于0！";
                error.code = -10;
                return error.code;
            }

            robotNew.retention_amout = Double.parseDouble(remandAmount);
            robotNew.amount = Double.parseDouble(bidAmount);

            robotNew.status = Constants.IPS_ENABLE ? false : true;
            robotNew.loan_type = borrowWay;
            robotNew.time = new Date();
            robotNew.valid_type = validType;
            robotNew.valid_date = validDate;
            robotNew.min_amount = minAmount;
            robotNew.max_amount = maxAmount;

            try {
                robotNew.save();
            } catch (Exception e) {
                error.msg = "对不起！本次设置投标机器人失败！请您重试！";
                error.code = -9;
                return error.code;
            }

        } else {

            robot.user_id = userId;
            robot.min_interest_rate = Double.parseDouble(rateStart);
            robot.max_interest_rate = Double.parseDouble(rateEnd);

            if (Double.parseDouble(rateEnd) < Double.parseDouble(rateStart)) {
                error.msg = "对不起！您设置的利率上限不能小于利率下限！";
                error.code = -1;

                return error.code;
            }

            if (null != deadlineStart) {
                robot.min_period = Integer.parseInt(deadlineStart);
            }

            if (null != deadlineEnd) {
                robot.max_period = Integer.parseInt(deadlineEnd);

                if (Integer.parseInt(deadlineEnd) < Integer.parseInt(deadlineStart)) {
                    error.msg = "对不起！您设置的借款期限上限不能小于借款期限下限！";
                    error.code = -2;

                    return error.code;
                }
            }

            if (null != creditStart) {
                robot.min_credit_level_id = Integer.parseInt(creditStart);
            }

            if (null != creditEnd) {
                robot.max_credit_level_id = Integer.parseInt(creditEnd);

                if (Integer.parseInt(creditEnd) >= Integer.parseInt(creditStart)) {
                    error.msg = "对不起！最高信用等级不能低于最低信用等级！";
                    error.code = -3;
                    return error.code;
                }
            }

            if (balance < Double.parseDouble(remandAmount)) {
                error.msg = "对不起！您预留金额不能大于您的可用余额！";
                error.code = -4;
                return error.code;
            }

            if (Double.parseDouble(bidAmount) > balance) {
                error.msg = "对不起！您设置的投标金额不能大于您的可用余额！";
                error.code = -5;
                return error.code;
            }

            if (null == remandAmount) {
                error.msg = "对不起！您预留金额不能为空！";
                error.code = -6;
                return error.code;
            }

            if (null == bidAmount) {
                error.msg = "对不起！每次投标金额不能为空！";
                error.code = -7;
                return error.code;
            }

            if (null == borrowWay) {
                error.msg = "对不起！借款类型不能为空！";
                error.code = -8;

                return error.code;
            }

            if (Double.parseDouble(bidAmount) < 0) {
                error.msg = "对不起！您设置的投标金额应该大于0！";
                error.code = -9;
                return error.code;
            }

            if (0 > Double.parseDouble(remandAmount)) {
                error.msg = "对不起！您预留金额不能小于0！";
                error.code = -10;
                return error.code;
            }

            robot.retention_amout = Double.parseDouble(remandAmount);
            robot.amount = Double.parseDouble(bidAmount);

            robot.status = Constants.IPS_ENABLE ? false : true;
            robot.loan_type = borrowWay;
            robot.time = new Date();
            robot.valid_type = validType;
            robot.valid_date = validDate;
            robot.min_amount = minAmount;
            robot.max_amount = maxAmount;

            try {
                robot.save();
            } catch (Exception e) {
                error.msg = "对不起！本次设置投标机器人失败！请您重试！";
                error.code = -9;
                return error.code;
            }


        }

        DealDetail.userEvent(userId, UserEvent.OPEN_ROBOT, "开启投标机器人", error);

        if (error.code < 0) {
            JPA.setRollbackOnly();

            return error.code;
        }

        error.msg = "设置成功";
        error.code = 1;
        return 1;

    }

    /**
     * 获取用户投标机器人
     *
     * @param userId
     * @return
     */
    public static t_user_automatic_invest_options getUserRobot(long userId, ErrorInfo error) {

        t_user_automatic_invest_options robot = null;

        try {
            robot = t_user_automatic_invest_options.find(" user_id = ? ", userId).first();
        } catch (Exception e) {
            e.printStackTrace();
            error.code = -1;
            return null;
        }

        error.code = 1;
        return robot;
    }


    public static double getUserBalance(long userId) {

        double balance = 0;

        try {
            balance = t_users.find(" select balance from t_users where id = ? ", userId).first();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return balance;
    }


    /**
     * 检查贷款用户是否开启了投标机器人，贷款用户在获得贷款时会自动关闭自动投标，以避免借款被用作自动投标资金
     *
     * @param bidId
     */
    public static int closeUserBidRobot(long userId) {

        if (0 == userId) return -1;

        ErrorInfo error = new ErrorInfo();
        Boolean status = null;
        String hql1 = "select status from t_user_automatic_invest_options  where user_id = ?";

        try {
            status = t_user_automatic_invest_options.find(hql1, userId).first();
        } catch (Exception e) {
            Logger.error("理财->查询开启自动投标状态:" + e.getMessage());

            return -2;
        }

        if (null == status) return 1;

		/* 表示投标机器人开启,关闭投标机器人 */
        if (status) {
            String hql2 = "update t_user_automatic_invest_options set status = ? where user_id = ?";

            Query query = JPA.em().createQuery(hql2);
            query.setParameter(1, Constants.NOT_ENABLE);
            query.setParameter(2, userId);

            try {
                return query.executeUpdate();
            } catch (Exception e) {
                Logger.error(e.getMessage());

                return -3;
            }
        }

        DealDetail.userEvent(userId, UserEvent.CLOSE_ROBOT, "关闭投标机器人", error);

        if (error.code < 0) {
            JPA.setRollbackOnly();

            return error.code;
        }

        return 1;
    }

    public long investUserId;
    public double investAmount;

    /**
     * 查询对应标的的所有投资者以及投资金额
     *
     * @param bidId
     * @return
     */
    public static List<Invest> queryAllInvestUser(long bidId) {
        List<Map<Long, Object>> tamounts = null;
        List<Invest> amounts = new ArrayList<Invest>();

        String hql = "select new Map(i.user_id as userId, i.amount as amount, i.mer_bill_no as mer_bill_no, i.ips_bill_no as ips_bill_no, i.fee as fee) from t_invests i where i.bid_id=?  order by time";

        try {
            tamounts = t_invests.find(hql, bidId).fetch();
        } catch (Exception e) {
            Logger.error("查询对应标的的所有投资者以及投资金额:" + e.getMessage());

            return null;
        }

        if (null == tamounts)
            return null;

        if (tamounts.size() == 0) {
            return amounts;
        }

        Invest invest = null;

        for (Map<Long, Object> map : tamounts) {
            invest = new Invest();

            invest.investUserId = Long.parseLong(map.get("userId") + "");
            invest.investAmount = Double.parseDouble(map.get("amount") + "");
            invest.merBillNo = (String) map.get("mer_bill_no");
            invest.ipsBillNo = (String) map.get("ips_bill_no");
            invest.fee = Convert.strToDouble("" + map.get("fee"), 0);

            amounts.add(invest);
        }

        return amounts;
    }

    /**
     * 查询投标信息
     *
     * @param bidId
     * @return
     */
    public static List<Map<Object, Object>> queryInvestInfo(long bidId, ErrorInfo error) {
        error.clear();
        String sql = "select new Map(u.id as userId, u.ips_acct_no as ipsAcctNo, i.amount as amount) from t_invests i, t_users u where i.bid_id=? and u.id = i.user_id order by i.time";

        try {
            return t_invests.find(sql, bidId).fetch();
        } catch (Exception e) {
            Logger.error(e.getMessage());
            error.code = -1;
            error.msg = "数据库异常";

            return null;
        }
    }

    /**
     * 更新标的浏览次数
     *
     * @param bidId
     */
    public static void updateReadCount(long bidId, ErrorInfo error) {
        EntityManager em = JPA.em();
        /*增加该借款标浏览次数*/
        int rows = em.createQuery("update t_bids set read_count = read_count + 1 where id = ?").setParameter(1, bidId).executeUpdate();

        if (rows == 0) {
            JPA.setRollbackOnly();
            error.code = -1;
        }

        error.code = 1;
    }


    /**
     * 等待满标的理财标
     *
     * @param userId
     * @param type     1:全部 2：标题 3：借款标编号
     * @param params
     * @param currPage
     * @return
     */
    public static PageBean<v_user_waiting_full_invest_bids> queryUserWaitFullBids(long userId, String typeStr, String param, int currPage, int pageSize, ErrorInfo error) {
        PageBean<v_user_waiting_full_invest_bids> page = new PageBean<v_user_waiting_full_invest_bids>();
        List<v_user_waiting_full_invest_bids> bidList = new ArrayList<v_user_waiting_full_invest_bids>();
        StringBuffer sql = new StringBuffer("");
        sql.append(SQLTempletes.SELECT);
        sql.append(SQLTempletes.V_USER_WAITING_FULL_INVEST_BIDS);
        sql.append(" and t_invests.user_id = ? ");
        List<Object> params = new ArrayList<Object>();
        params.add(userId);

        EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;

        Map<String, Object> conditionMap = new HashMap<String, Object>();
        conditionMap.put("keyWords", param);
        int type = 0;

        String[] typeCondition = {" and ( t_bids.title like ? or t_invests.bid_id like ?) ", " and t_bids.title like ? ", " and t_invests.bid_id like ? "};

        if (StringUtils.isNotBlank(typeStr)) {
            type = Integer.parseInt(typeStr);
        }

        if (type < 0 || type > 2) {
            type = 0;
        }

        if (type == 0) {
            param = param == null ? "" : param;
            sql.append(typeCondition[0]);
            params.add("%" + param + "%");
            param = param.replace(obj + "", "");
            params.add("%" + param + "%");
        } else {
            sql.append(typeCondition[type]);
            if (type == 2) {
                param = param.replace(obj + "", "");
            }
            params.add("%" + param + "%");
        }

        try {
            Query query = em.createNativeQuery(sql.toString(), v_user_waiting_full_invest_bids.class);
            for (int n = 1; n <= params.size(); n++) {
                query.setParameter(n, params.get(n - 1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bidList = query.getResultList();

            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

        } catch (Exception e) {
            e.printStackTrace();
            error.code = -1;
            return page;
        }

        conditionMap.put("type", type);

        page.page = bidList;
        page.currPage = currPage;
        page.pageSize = pageSize;
        page.conditions = conditionMap;
        error.code = 1;
        return page;
    }

    /**
     * 等待放款的理财标
     *
     * @param userId
     * @param typeStr
     * @param param
     * @param currPage
     * @param pageSize
     * @param error
     * @return
     */
    public static PageBean<v_user_waiting_full_invest_bids> queryUserReadyReleaseBid(long userId, String typeStr, String param, int currPage, int pageSize, ErrorInfo error) {
        PageBean<v_user_waiting_full_invest_bids> page = new PageBean<v_user_waiting_full_invest_bids>();
        List<v_user_waiting_full_invest_bids> bidList = new ArrayList<v_user_waiting_full_invest_bids>();
        StringBuffer sql = new StringBuffer("");
        sql.append(SQLTempletes.SELECT);
        sql.append(SQLTempletes.V_USER_INVEST_READY_RELEASE_BID);
        sql.append(" and t_invests.user_id = ? ");
        List<Object> params = new ArrayList<Object>();
        params.add(userId);

        EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;

        Map<String, Object> conditionMap = new HashMap<String, Object>();
        conditionMap.put("keyWords", param);
        int type = 0;

        String[] typeCondition = {" and ( t_bids.title like ? or t_invests.bid_id like ?) ", " and t_bids.title like ? ", " and t_invests.bid_id like ? "};

        if (StringUtils.isNotBlank(typeStr)) {
            type = Integer.parseInt(typeStr);
        }

        if (type < 0 || type > 2) {
            type = 0;
        }

        if (type == 0) {
            param = param == null ? "" : param;
            sql.append(typeCondition[0]);
            params.add("%" + param + "%");
            param = param.replace(obj + "", "");
            params.add("%" + param + "%");
        } else {
            sql.append(typeCondition[type]);
            if (type == 2) {
                param = param.replace(obj + "", "");
            }
            params.add("%" + param + "%");
        }

        try {
            Query query = em.createNativeQuery(sql.toString(), v_user_waiting_full_invest_bids.class);
            for (int n = 1; n <= params.size(); n++) {
                query.setParameter(n, params.get(n - 1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bidList = query.getResultList();

            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

        } catch (Exception e) {
            e.printStackTrace();
            error.code = -1;
            return page;
        }

        conditionMap.put("type", type);

        page.page = bidList;
        page.currPage = currPage;
        page.pageSize = pageSize;
        page.conditions = conditionMap;
        error.code = 1;
        return page;
    }


    /**
     * 查询用户所有投资成功的借款标
     *
     * @param userId
     * @param type
     * @param params
     * @param currPage
     * @return
     */
    public static PageBean<v_user_success_invest_bids> queryUserSuccessInvestBids(long userId, String typeStr, String param, int currPage, int pageSize, ErrorInfo error) {

        int type = 0;
        String[] typeCondition = {" and ( t_bids.title like ? or t_invests.bid_id like ?) ", " and t_bids.title like ? ", " and t_invests.bid_id like ? "};

        PageBean<v_user_success_invest_bids> page = new PageBean<v_user_success_invest_bids>();
        List<v_user_success_invest_bids> list = new ArrayList<v_user_success_invest_bids>();

        page.pageSize = pageSize;
        page.currPage = currPage;

        StringBuffer sql = new StringBuffer("");
        sql.append(SQLTempletes.SELECT);
        sql.append(SQLTempletes.V_USER_SUCCESS_INVEST_BIDS);
        sql.append(" and t_invests.user_id=? ");
        List<Object> params = new ArrayList<Object>();
        params.add(userId);

        EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;

        Map<String, Object> conditionMap = new HashMap<String, Object>();
        conditionMap.put("keyWords", param);

        if (typeStr == null && param == null) {
            sql.append(" order by id desc");
            try {
                Query query = em.createNativeQuery(sql.toString(), v_user_success_invest_bids.class);
                for (int n = 1; n <= params.size(); n++) {
                    query.setParameter(n, params.get(n - 1));
                }
                query.setFirstResult((currPage - 1) * pageSize);
                query.setMaxResults(pageSize);
                list = query.getResultList();

                page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

            } catch (Exception e) {
                e.printStackTrace();
                error.code = -1;
                return page;
            }

            page.page = list;
            error.code = 1;
            return page;
        }

        if (StringUtils.isNotBlank(typeStr)) {
            type = Integer.parseInt(typeStr);
        }

        if (type < 0 || type > 2) {
            type = 0;

        }

        if (type == 0) {
            param = param == null ? "" : param;
            sql.append(typeCondition[0]);
            params.add("%" + param + "%");
            param = param.replace(obj + "", "");
            params.add("%" + param + "%");
        } else {
            sql.append(typeCondition[type]);
            if (type == 2) {
                param = param.replace(obj + "", "");
            }
            params.add("%" + param + "%");
        }
        sql.append(" order by id desc");

        try {
            Query query = em.createNativeQuery(sql.toString(), v_user_success_invest_bids.class);
            for (int n = 1; n <= params.size(); n++) {
                query.setParameter(n, params.get(n - 1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            list = query.getResultList();

            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

        } catch (Exception e) {
            e.printStackTrace();
            error.code = -2;
            return page;
        }

        conditionMap.put("type", type);

        page.conditions = conditionMap;
        page.page = list;
        error.code = 1;
        return page;
    }


    /**
     * 查询用户收款中的理财标
     *
     * @param userId
     * @param type     1:全部 2：标题 3：借款标编号
     * @param params
     * @param currPage
     * @return
     */

    public static PageBean<v_receiving_invest_bids> queryUserAllReceivingInvestBids(long userId, String typeStr, String param, int currPage, int pageSize, ErrorInfo error) {

        int type = 0;
        String[] typeCondition = {" and ( t_bids.title like ? or t_invests.bid_id like ?) ", " and t_bids.title like ? ", " and t_invests.bid_id like ? "};
        PageBean<v_receiving_invest_bids> page = new PageBean<v_receiving_invest_bids>();
        List<v_receiving_invest_bids> bidList = new ArrayList<v_receiving_invest_bids>();

        page.pageSize = pageSize;
        page.currPage = currPage;

        StringBuffer sql = new StringBuffer("");
        sql.append(SQLTempletes.SELECT);
        sql.append(SQLTempletes.V_RECEIVING_INVEST_BIDS);
        sql.append(" and t_invests.user_id = ?");
        List<Object> params = new ArrayList<Object>();
        params.add(userId);

        EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;

        Map<String, Object> conditionMap = new HashMap<String, Object>();
        conditionMap.put("keyWords", param);

        if (typeStr == null && param == null) {
            try {
                sql.append(" order by id desc");
                Query query = em.createNativeQuery(sql.toString(), v_receiving_invest_bids.class);
                for (int n = 1; n <= params.size(); n++) {
                    query.setParameter(n, params.get(n - 1));
                }
                query.setFirstResult((currPage - 1) * pageSize);
                query.setMaxResults(pageSize);
                bidList = query.getResultList();

                page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

            } catch (Exception e) {
                e.printStackTrace();
                error.code = -1;
                return page;
            }
            page.page = bidList;
            error.code = 1;
            return page;
        }

        if (StringUtils.isNotBlank(typeStr)) {
            type = Integer.parseInt(typeStr);
        }

        if (type < 0 || type > 2) {
            type = 0;
        }

        if (type == 0) {
            param = param == null ? "" : param;
            sql.append(typeCondition[0]);
            params.add("%" + param + "%");
            param = param.replace(obj + "", "");
            params.add("%" + param + "%");
        } else {
            sql.append(typeCondition[type]);
            if (type == 2) {
                param = param.replace(obj + "", "");
            }
            params.add("%" + param + "%");
        }

        try {
            sql.append(" order by id desc");
            Query query = em.createNativeQuery(sql.toString(), v_receiving_invest_bids.class);
            for (int n = 1; n <= params.size(); n++) {
                query.setParameter(n, params.get(n - 1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bidList = query.getResultList();

            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

        } catch (Exception e) {
            e.printStackTrace();
            error.code = -2;
            return page;
        }

        conditionMap.put("type", type);

        page.conditions = conditionMap;
        page.page = bidList;
        error.code = 1;
        return page;
    }

    /**
     * 获取t_bids表特定标版本号
     *
     * @param bidId
     * @param error
     * @return
     */
    public static int getBidVersion(long bidId, ErrorInfo error) {

        int version = 0;
        String sql = "select version from t_bids where id = ?";

        try {
            version = t_bids.find(sql, bidId).first();
        } catch (Exception e) {
            e.printStackTrace();
            error.msg = "对不起！系统异常！请您联系平台管理员！";
            error.code = -1;
            return -1;
        }
        error.code = 1;
        return version;
    }


    /**
     * 已投总额增加,投标进度增加
     *
     * @param bidId
     * @param amount
     * @param schedule
     * @param error
     * @return
     */
    public static int updateBidschedule(long bidId, double amount, double schedule, ErrorInfo error) {
        EntityManager em = JPA.em();
        int rows = 0;

        try {
            rows = em.createQuery("update t_bids set loan_schedule=?,has_invested_amount= has_invested_amount + ? where id=? and amount >= has_invested_amount + ?")
                    .setParameter(1, schedule).setParameter(2, amount).setParameter(3, bidId).setParameter(4, amount).executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            error.code = -1;
            return error.code;
        }

        if (rows == 0) {
            JPA.setRollbackOnly();
            error.code = -1;
            return error.code;
        }


        error.code = 1;

        return 1;
    }


    /**
     * 更新借款标满标时间
     *
     * @param bidId
     * @param error
     * @return
     */
    public static int updateBidExpiretime(long bidId, ErrorInfo error) {
        EntityManager em = JPA.em();
        try {
            int rows = em.createQuery("update t_bids set real_invest_expire_time = ? where id=?").setParameter(1, new Date()).setParameter(2, bidId).executeUpdate();

            if (rows == 0) {
                JPA.setRollbackOnly();
                error.code = -1;
                return error.code;
            }
        } catch (Exception e) {
            e.printStackTrace();
            error.code = -1;
            return error.code;
        }
        error.code = 1;
        return 1;
    }


    /**
     * 根据投资ID获取对应bidId,userId
     *
     * @param investId
     * @param error
     * @return
     */
    public static t_invests queryUserAndBid(long investId) {
        t_invests invest = null;
        try {
            invest = t_invests.find("select new t_invests(user_id,bid_id) from t_invests where id = ?", investId).first();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return invest;
    }

    /**
     * 即时查询借款标对象
     *
     * @param bidId
     * @return
     */
    public static Map<String, String> bidMap(long bidId, ErrorInfo error) {
        error.clear();

        String sql = "select id, title, min_invest_amount, average_invest_amount, amount, status, "
                + "invest_expire_time, has_invested_amount, user_id, product_id, version from t_bids where id = ? limit 1";

        Object[] obj = null;

        try {
            obj = (Object[]) JPA.em().createNativeQuery(sql).setParameter(1, bidId).getSingleResult();

        } catch (Exception e) {
            e.printStackTrace();
            error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
            error.code = -11;

        }

        if (obj == null || obj.length == 0) {
            error.msg = "标的信息不存在";
            error.code = -11;

            return null;
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("id", obj[0] + "");
        map.put("title", obj[1] + "");
        map.put("min_invest_amount", obj[2] + "");
        map.put("average_invest_amount", obj[3] + "");
        map.put("amount", obj[4] + "");
        map.put("status", obj[5] + "");
        map.put("invest_expire_time", obj[6] + "");
        map.put("has_invested_amount", obj[7] + "");
        map.put("user_id", obj[8] + "");
        map.put("product_id", obj[9] + "");
        map.put("version", obj[10] + "");

        error.code = 1;
        return map;
    }

    /**
     * 投标操作
     *
     * @param userId
     * @param bidId
     * @param investTotal
     * @param dealpwdStr
     * @param isAuto      是否自动投标
     * @param isRepair    是否补单
     * @param error
     */
    public static void invest(long userId, long bidId, int investTotal, String dealpwdStr, boolean isAuto, boolean isRepair, String repairBillNo, ErrorInfo error) {
        error.clear();

        if (investTotal <= 0) {
            error.msg = "对不起！请输入正确格式的数字!";
            error.code = -10;

            return;
        }

        t_users user1 = User.queryUserforInvest(userId, error);

        if (error.code < 0) {
            return;
        }

        if (user1.balance <= 0) {
            error.msg = "对不起！您余额不足，请及时充值！";
            error.code = -999;

            return;
        }

        double balance = user1.balance;
        boolean black = user1.is_blacklist;
        String dealpwd = user1.pay_password;

        if (black) {
            error.msg = "对不起！您已经被平台管理员限制操作！请您与平台管理员联系！";
            error.code = -1;

            return;
        }

        Map<String, String> bid = bidMap(bidId, error);

        if (error.code < 0) {
            error.msg = "对不起！系统异常！请您联系平台管理员！";
            error.code = -2;

            return;
        }

        double minInvestAmount = Double.parseDouble(bid.get("min_invest_amount") + "");
        double averageInvestAmount = Double.parseDouble(bid.get("average_invest_amount") + "");
        double amount = Double.parseDouble(bid.get("amount") + "");
        int status = Integer.parseInt(bid.get("status") + "");

        Date invest_expire_time = DateUtil.strToDate(bid.get("invest_expire_time").toString());

        double hasInvestedAmount = Double.parseDouble(bid.get("has_invested_amount") + "");
        long bidUserId = Long.parseLong(bid.get("user_id") + "");// 借款者
        long product_id = Long.parseLong(bid.get("product_id") + "");
        long time = new Date().getTime();
        long time2 = invest_expire_time.getTime();

        if (time > time2) {
            error.msg = "对不起！此借款标已经不处于招标状态，请投资其他借款标！谢谢！";
            error.code = -2;
            JPA.setRollbackOnly();
            Logger.error("-----------111Incest1767bidId:%s,time:%s ,invest_expire_time.getTime:%s-------------", bidId, time, time2);

            return;
        }

        if (userId == bidUserId) {
            error.msg = "对不起！您不能投自己的借款标!";
            error.code = -10;

            return;
        }

        if (User.isInMyBlacklist(bidUserId, userId, error) < 0) {
            error.msg = "对不起！您已经被对方拉入黑名单，您被限制投资此借款标！";
            error.code = -2;

            return;
        }

        if (status != Constants.BID_ADVANCE_LOAN
                && status != Constants.BID_FUNDRAISE) {
            error.msg = "对不起！此借款标已经不处于招标状态，请投资其他借款标！谢谢！";
            error.code = -2;

            return;
        }

        if (amount <= hasInvestedAmount) {
            error.msg = "对不起！此借款标已经不处于招标状态，请投资其他借款标！谢谢！";
            error.code = -2;

            return;
        }

        DataSafety data = new DataSafety();// 数据防篡改(针对当前投标会员)
        data.setId(userId);
        boolean sign = data.signCheck(error);

        if (error.code < 0) {
            error.msg = "对不起！尊敬的用户，你的账户资金出现异常变动，请速联系管理员！";
            error.code = -2;
            JPA.setRollbackOnly();

            Logger.error("-----------111Invest1810-------------");

            return;
        }

        if (!sign) {// 数据被异常改动
            error.msg = "对不起！尊敬的用户，你的账户资金出现异常变动，请速联系管理员！";
            error.code = -2;
            JPA.setRollbackOnly();

            Logger.error("-----------111Invest1820-------------");

            return;
        }

        String sqlProduct = "select is_deal_password as is_deal_password from t_products where id = ?";
        boolean is_deal_password = (Boolean) JPA.em().createNativeQuery(sqlProduct).setParameter(1, product_id).getSingleResult();

        if (is_deal_password == true && !isRepair) {
            if (StringUtils.isBlank(dealpwdStr)) {
                error.msg = "对不起！请输入交易密码!";
                error.code = -12;

                return;
            }

            if (!Encrypt.MD5(dealpwdStr + Constants.ENCRYPTION_KEY).equals(
                    dealpwd)) {
                error.msg = "对不起！交易密码错误!";
                error.code = -13;
                return;
            }
        }

		/* 普通模式 */
        if (averageInvestAmount == 0) {

            if (amount - hasInvestedAmount >= minInvestAmount) {

                if (investTotal < minInvestAmount) {
                    error.msg = "对不起！您最少要投" + minInvestAmount + "元";
                    error.code = -3;

                    return;
                }
            } else {

                if (investTotal < amount - hasInvestedAmount) {
                    double money = amount - hasInvestedAmount;
                    error.msg = "对不起！您最少要投" + money + "元";
                    error.code = -4;

                    return;
                }
            }

            if (balance < investTotal) {
                error.msg = "对不起！您可用余额不足！根据您的余额您最多只能投" + balance + "元";
                error.code = Constants.BALANCE_NOT_ENOUGH;

                return;
            }

            if (investTotal > (amount - hasInvestedAmount)) {
                double money = amount - hasInvestedAmount;
                error.msg = "对不起！您的投资金额超过了该标的剩余金额,您最多只能投" + money + "元！";
                error.code = -6;

                return;
            }
        }

		/* 认购模式 */
        if (minInvestAmount == 0) {
            if (investTotal <= 0) {
                error.msg = "对不起！您至少应该买一份！";
                error.code = -7;

                return;
            }
            if (investTotal > ((amount - hasInvestedAmount) / averageInvestAmount)) {
                error.msg = "对不起！您最多只能购买" + (amount - hasInvestedAmount)
                        / averageInvestAmount + "份！";
                error.code = -8;

                return;
            }

            investTotal = (int) (investTotal * averageInvestAmount);

            if (balance < investTotal) {
                error.msg = "对不起！您余额不足！您最多只能购买"
                        + (int) (balance / averageInvestAmount) + "份！";
                error.code = Constants.BALANCE_NOT_ENOUGH;

                return;
            }

        }

        if (error.code < 0) {
            error.msg = "对不起！系统异常！请您联系平台管理员！";
            error.code = -2;

            return;
        }

        String pMerBillNo = null;
        double pFee = 0;
        String pIpsBillNo = "";

        if (Constants.IPS_ENABLE && !isRepair) {
            if (!isAuto) {
                return;
            }

            //自动投标
            pMerBillNo = Payment.createBillNo(userId, IPSOperation.REGISTER_CREDITOR);

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("userId", userId);
            map.put("bidId", bidId);
            map.put("investAmount", investTotal);
            Cache.set(pMerBillNo, map, IPSConstants.CACHE_TIME);

            Map<String, String> args = Payment.registerCreditor(pMerBillNo, userId, bidId, 2, investTotal, error);
            String str = WS.url(IPSConstants.ACTION).setParameters(args).post().getString();
            Logger.info("自动投标接口输出参数\n" + str);

            JSONObject jsonObj = JSONObject.fromObject(str);
            String pMerCode = jsonObj.getString("pMerCode");
            String pErrCode = jsonObj.getString("pErrCode");
            String pErrMsg = jsonObj.getString("pErrMsg");
            String p3DesXmlPara = jsonObj.getString("p3DesXmlPara");
            String pSign = jsonObj.getString("pSign");

            if (!Payment.checkSign(pMerCode + pErrCode + pErrMsg + p3DesXmlPara, pSign)) {
                error.code = -1;
                error.msg = "sign校验失败";

                return;
            }

            if (!"MG00000F".equals(pErrCode)) {
                error.code = -1;
                error.msg = pErrMsg;

                return;
            }

            p3DesXmlPara = Encrypt.decrypt3DES(p3DesXmlPara, Constants.ENCRYPTION_KEY);

            if (StringUtils.isNotBlank(p3DesXmlPara)) {
                if (p3DesXmlPara.startsWith("?")) {
                    p3DesXmlPara = p3DesXmlPara.substring(1);
                }
            }

            pFee = ((JSONObject) Converter.xmlToObj(p3DesXmlPara)).getDouble("pFee");
            pIpsBillNo = ((JSONObject) Converter.xmlToObj(p3DesXmlPara)).getString("pP2PBillNo");
        } else if (Constants.IPS_ENABLE && isRepair) {
            pMerBillNo = repairBillNo;
        }

        doInvest(user1, bid, investTotal, pMerBillNo, pFee, error);

        if (Constants.IPS_ENABLE && !isRepair) {
            if (!isAuto) {
                return;
            }
            //保存第三方返回流水号
            IpsDetail.updateInvestMer(pMerBillNo, pIpsBillNo, error);
        }
    }

    /**
     * 投标操作(写入数据库)
     *
     * @param user1
     * @param bid
     * @param investTotal
     * @param pMerBillNo
     * @param fee
     * @param error
     */
    public static void doInvest(t_users user1, Map<String, String> bid, int investTotal, String pMerBillNo, double pFee, ErrorInfo error) {
        error.clear();
        long userId = user1.id;
        long bidId = Long.parseLong(bid.get("id") + "");
        double amount = Double.parseDouble(bid.get("amount") + "");
        double hasInvestedAmount = Double.parseDouble(bid.get("has_invested_amount") + "");
        long bidUserId = Long.parseLong(bid.get("user_id") + "");// 借款者

        double schedule = Arith.divDown(hasInvestedAmount + investTotal, amount, 4) * 100;//

		/* 已投总额增加,投标进度增加 */
        int result = updateBidschedule(bidId, investTotal, schedule, error);

        if (result < 0) {
            error.msg = "对不起！系统异常！对此造成的不便敬请谅解！";
            error.code = -8;
            JPA.setRollbackOnly();

            Logger.error("-----------111Invest2018-------------");
            return;
        }

		/* 满标 */
        if (amount == (hasInvestedAmount + investTotal)) {

            // 更新满标时间
            int resulta = updateBidExpiretime(bidId, error);

            if (resulta < 0) {
                error.msg = "对不起！系统异常！对此造成的不便敬请谅解！";
                error.code = -8;
                JPA.setRollbackOnly();
                Logger.error("-----------111Invest2032-------------");

                return;
            }

            // 成功满标，增加信用积分
            DealDetail.addCreditScore(bidUserId, 3, 1, bidId, "成功满标，借款人添加信用积分", error);
            if (error.code < 0) {
                JPA.setRollbackOnly();
                Logger.error("-----------111Invest2042-------------");
                return;
            }

            //更新用户的信用等级
            User.updateCreditLevel(bidUserId, error);
            if (error.code < 0) {
                JPA.setRollbackOnly();

                return;
            }
        }

        if (error.code < 0) {
            error.msg = "对不起！系统异常！对此造成的不便敬请谅解！";
            error.code = -8;
            JPA.setRollbackOnly();

            Logger.error("-----------111Invest2262-------------");
            return;
        }

		/* 可用金额减少,冻结资金增加 */
        int result5 = DealDetail.freezeFund(userId, investTotal);

        if (result5 <= 0) {
            error.msg = "对不起！系统异常！请您重试或联系平台管理员！";
            error.code = -9;
            JPA.setRollbackOnly();

            Logger.error("-----------111Invest2274------------");

            return;
        }

        // 更新会员性质
        User.updateMasterIdentity(userId, Constants.INVEST_USER, error);

        if (error.code < 0) {
            JPA.setRollbackOnly();
            Logger.error("-----------111Invset2284-------------");
            return;
        }

//		Map<String, Double> funds = DealDetail.queryUserFund(userId, error);
        v_user_for_details forDetail = DealDetail.queryUserBalance(userId, error);

        if (error.code < 0) {
            JPA.setRollbackOnly();
            Logger.error("-----------111Ivsert2293-------------");
            return;
        }

        double user_amount = forDetail.user_amount;
        double freeze = forDetail.freeze;
        double receive_amount = forDetail.receive_amount;

		/* 伪构记录 */
        DealDetail dealDetail = new DealDetail(userId,
                DealType.PAY_FREEZE_FUND, investTotal, bidId, user_amount,
                freeze - investTotal, receive_amount, "投标成功，支出投标冻结金额");
        dealDetail.addDealDetail(error);

        if (error.code < 0) {
            error.code = -25;
            error.msg = "添加交易记录失败!";
            JPA.setRollbackOnly();
            Logger.error("-----------111Invest2311-------------");
            return;
        }

        // 添加交易记录
        dealDetail = new DealDetail(userId, DealType.FREEZE_INVEST, investTotal, bidId, user_amount,
                freeze, receive_amount, "投标成功，冻结投标金额" + investTotal + "元");
        dealDetail.addDealDetail(error);

        if (error.code < 0) {
            JPA.setRollbackOnly();
            Logger.error("-----------111Invest2322-------------");
            return;
        }

        // 投标用户增加系统积分
        DealDetail.addScore(userId, 1, investTotal, bidId, "投标成功，添加系统积分", error);

        if (error.code < 0) {
            JPA.setRollbackOnly();
            Logger.error("-----------111Invest2331-------------");
            return;
        }


        DealDetail.userEvent(userId, UserEvent.INVEST, "成功投标", error);

        if (error.code < 0) {
            JPA.setRollbackOnly();
            Logger.error("-----------111Invest2340-------------");
            return;
        }

        t_invests invest = new t_invests();
        invest.user_id = userId;
        invest.time = new Date();
        invest.bid_id = bidId;
        /* 0 正常(转让入的也是0) */
        invest.transfer_status = 0;
        invest.amount = investTotal;
        invest.fee = pFee;
        invest.mer_bill_no = pMerBillNo;

        try {
            invest.save();
        } catch (Exception e) {
            error.msg = "对不起！您此次投资失败！请您重试或联系平台管理员！";
            error.code = -10;
            JPA.setRollbackOnly();

            Logger.error("-----------111Invest2351-------------");

            return;
        }

        // 投标一次增加信用积分
        DealDetail.addCreditScore(userId, 4, 1, invest.id, "成功投标一次，投资人添加信用积分",
                error);

        if (error.code < 0) {
            JPA.setRollbackOnly();
            Logger.error("-----------111Invest2362-------------");
            return;
        }


        v_user_for_details forDetail2 = DealDetail.queryUserBalance(userId, error);

        if (error.code < 0) {
            JPA.setRollbackOnly();
            Logger.error("-----------111Invest2371-------------");
            return;
        }

        if (forDetail2.user_amount < 0) {
            error.msg = "对不起！您账户余额不足，请及时充值！";
            error.code = -10;
            JPA.setRollbackOnly();

            Logger.error("-----------111Invest2380-------------");
            return;
        }

        DataSafety data = new DataSafety();
        data.setId(userId);// 更新数据防篡改字段
        data.updateSign(error);

        if (error.code < 0) {
            error.msg = "对不起！系统异常！请您重试或联系平台管理员！";
            error.code = -9;
            JPA.setRollbackOnly();

            Logger.error("-----------111Invest2393-------------");
            return;
        }

        // 发送消息
        String username = user1.name;
        String title = bid.get("title") + "";

        TemplateEmail email = TemplateEmail.getEmailTemplate(Templets.E_INVEST, error);//发送邮件

        if (error.code < 0) {
            email = new TemplateEmail();
        }

        if (email.status) {
            String econtent = email.content;
            econtent = econtent.replace("date", DateUtil.dateToString((new Date())));
            econtent = econtent.replace("userName", username);
            econtent = econtent.replace("title", title);
            econtent = econtent.replace("investAmount", investTotal + "");

            TemplateEmail.addEmailTask(user1.email, email.title, econtent);
        }


        TemplateStation station = TemplateStation.getStationTemplate(Templets.M_INVEST, error);//发送站内信

        if (error.code < 0) {
            station = new TemplateStation();
        }

        if (station.status) {
            String stationContent = station.content;
            stationContent = stationContent.replace("date", DateUtil.dateToString((new Date())));
            stationContent = stationContent.replace("userName", username);
            stationContent = stationContent.replace("title", title);
            stationContent = stationContent.replace("investAmount", investTotal + "");

            TemplateStation.addMessageTask(userId, station.title, stationContent);
        }

        TemplateSms sms = TemplateSms.getSmsTemplate(Templets.S_INVEST, error);//发送短信

        if (error.code < 0) {
            sms = new TemplateSms();
        }

        if (sms.status) {
            String smscontent = sms.content;
            smscontent = smscontent.replace("date", DateUtil.dateToString((new Date())));
            smscontent = smscontent.replace("userName", username);
            smscontent = smscontent.replace("title", title);
            smscontent = smscontent.replace("investAmount", investTotal + "");
            TemplateSms.addSmsTask(user1.mobile, smscontent);
        }

        if (amount == (hasInvestedAmount + investTotal)) {

            List<Invest> investUser = Invest.queryAllInvestUser(bidId);

            if (investUser != null && investUser.size() > 0) {
                for (Invest userInvest : investUser) {
                    t_users user = t_users.find("select new t_users(id, device_user_id, channel_id, device_type, is_bill_push, is_invest_push, is_activity_push) from t_users where id = ?",
                            userInvest.investUserId).first();

                    if (user.is_invest_push) {
                        String device = user.device_type == 1 ? "\"custom_content\":{\"bidId\":\"" + bidId + "\",\"type\":\"3\"}" : "\"aps\": {\"alert\":\"test\",\"sound\":\"1\",\"badge\":\"1\"},\"bidId\":\"" + bidId + "\",\"type\":\"3\"";
                        device = "{\"title\":\"理财满标提醒通知\",\"description\":\"你有一条新的理财满标\"," + device + "}";
//						PushMessage.pushUnicastMessage(bill.device_user_id, bill.channel_id, bill.device_type, device);
                        PushMessage.pushUnicastMessage(user.device_user_id, user.channel_id, user.device_type, device);
                    }
                }
            }
            //send email to supervisor that the bid amount is enough
            String supervisorEmail = Constants.SUPERVISOR_MAIL;
            String checkTitle = "满标请审核";
            String checkContent = "尊敬的管理员：\n        标的" + title + "已经成功满标，请及时审核，谢谢！";
            ErrorInfo emailError = new ErrorInfo();
            TemplateEmail.sendEmail(0, supervisorEmail, checkTitle, checkContent, emailError);
        }

        error.msg = "投资成功！";
        error.code = 1;

    }

    /**
     * 获取用户减掉预留金额后的可用金额
     *
     * @param userId
     * @param remandAmount
     * @return
     */
    public static double queryAutoUserBalance(long userId, double remandAmount) {

        Double balance = null;
        String sql = "select balance from t_users where id = ?";

        try {
            balance = t_users.find(sql, userId).first();
        } catch (Exception e) {
            e.printStackTrace();

        }

        if (null == balance)
            return 0;

        return balance < remandAmount ? 0 : balance - remandAmount;
    }

    /**
     * 按时间倒序排序查出所有开启了投标机器人的用户ID
     *
     * @return
     */
    public static List<Object> queryAllAutoUser() {

        List<Object> list = null;

        try {
            list = t_user_automatic_invest_options.find("select user_id from t_user_automatic_invest_options where status = 1 order by time desc").fetch();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    /**
     * 将用户排到自动投标队尾
     *
     * @param user_id
     */
    public static void updateUserAutoBidTime(long user_id) {

        EntityManager em = JPA.em();
        em.createQuery("update t_user_automatic_invest_options set time = ? where user_id = ?").setParameter(1, new Date()).setParameter(2, user_id).executeUpdate();
    }

    /**
     * 查询符合用户设置条件的标的ID
     *
     * @param autoOptions
     * @param unit
     * @param bidId
     * @return
     */
    public static Map<String, Object> queryBiderByParam(t_user_automatic_invest_options autoOptions, int unit, long bidId) {
        int min_period = 0;
        int max_period = 0;

        if (unit == -1) {//单位为年
            min_period = autoOptions.min_period * 12;
            max_period = autoOptions.max_period * 12;
        }

        if (unit == 0) {
            min_period = autoOptions.min_period;
            max_period = autoOptions.max_period;
        }

        StringBuffer condition = new StringBuffer();
        condition.append("select new Map(id as id) from v_confirm_autoinvest_bids where apr >= "
                + autoOptions.min_interest_rate + " and apr <= " + autoOptions.max_interest_rate + " and min_invest_amount <= " + autoOptions.amount);

        if (autoOptions.min_period > 0 && autoOptions.max_period > 0) {
            condition.append(" and period >=" + min_period + " and period <=" + max_period);
        }

        if (autoOptions.min_credit_level_id > 0 && autoOptions.max_credit_level_id > 0) {
            condition.append(" and num >= " + autoOptions.max_credit_level_id + "  and num <= " + autoOptions.min_credit_level_id);
        }

        condition.append(" and  loan_type in ( " + autoOptions.loan_type + " )  and id=?");
        Map<String, Object> map = v_confirm_autoinvest_bids.find(condition.toString(), bidId).first();
        return map;
    }


    /**
     * 扣除保留金额后，计算最后投标金额
     *
     * @param bidAmount
     * @param schedule
     * @param amount
     * @param hasInvestedAmount
     * @return
     */
    public static int calculateBidAmount(double bidAmount, double schedule, double amount, double hasInvestedAmount) {

        int maxBidAmount = (int) (amount * 0.2);
        int invesAmount = 0;

        if (schedule < 95) {
            while (bidAmount > maxBidAmount) {
                bidAmount = bidAmount - 50;
            }

            do {
                invesAmount = (int) (hasInvestedAmount + bidAmount);
                schedule = invesAmount / amount;
                if (schedule > 95) {
                    bidAmount = bidAmount - 50;
                }
            } while (schedule > 95);
        }

        return (int) bidAmount;
    }


    /**
     * 计算自动投标份数
     *
     * @param amount
     * @param averageAmount
     * @return
     */
    public static int calculateFinalInvestAmount(double amount, double averageAmount) {
        int temp = 0;
        temp = (int) (amount / averageAmount);
        return temp;
    }

    /**
     * 增加用户自动投标记录
     *
     * @param userId
     * @param bidId
     */
    public static void addAutoBidRecord(long userId, long bidId) {

        t_user_automatic_bid bid = new t_user_automatic_bid();

        bid.bid_id = bidId;
        bid.time = new Date();
        bid.user_id = userId;

        bid.save();
    }

    /**
     * 判断用户是否已经自动投过当前标
     *
     * @param bidId
     * @param userId
     * @return
     */
    public static boolean hasAutoInvestTheBid(long bidId, long userId) {

        boolean flag = false;
        t_user_automatic_bid bid = t_user_automatic_bid.find("user_id=? and bid_id =?", userId, bidId).first();
        if (bid != null) {
            flag = true;
        }
        return flag;
    }

    public Map<String, Object> queryParamByBidId(long bidId) {
        String sql = "select new Map(user_id as user_id,amount as amount,min_invest_amount as min_invest_amount,average_invest_amount as average_invest_amount," +
                "has_invested_amount as has_invested_amount) from t_bids where id=?";
        return t_bids.find(sql, bidId).first();
    }

    /**
     * 查询所有投标进度小于且进入招标中十五分钟后的所有标的
     *
     * @return
     * @throws ParseException
     */
    public static List<Map<String, Object>> queryAllBider() {

        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        List<v_confirm_autoinvest_bids> bidList = null;
//		String dateTime = "";
//		try {
//			dateTime = DateUtil.getDateMinusMinutes(15);
//		} catch (ParseException e1) {
//			e1.printStackTrace();
//		}//当前时间减去15分钟的时间
//		
//		Date date = DateUtil.strToDate(dateTime);
        StringBuffer sql = new StringBuffer("");
        sql.append(SQLTempletes.SELECT);
        sql.append(SQLTempletes.V_CONFIRM_AUTOINVEST_BIDS);

        try {
            EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(), v_confirm_autoinvest_bids.class);
            bidList = query.getResultList();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Object> bidMap = null;

        for (v_confirm_autoinvest_bids bid : bidList) {
            bidMap = new HashMap<String, Object>();

            bidMap.put("bid_id", bid.id);
            bidMap.put("user_id", bid.user_id);
            bidMap.put("period_unit", bid.period_unit);
            bidMap.put("amount", bid.amount);
            bidMap.put("has_invested_amount", bid.has_invested_amount);
            bidMap.put("loan_schedule", bid.loan_schedule);
            bidMap.put("has_invested_amount", bid.has_invested_amount);
            bidMap.put("loan_schedule", bid.loan_schedule);
            bidMap.put("average_invest_amount", bid.average_invest_amount);

            mapList.add(bidMap);
        }

        return mapList;
    }


    /**
     * 判断该借款标是否超过95%
     *
     * @param bidId
     * @return
     */
    public static boolean judgeSchedule(long bidId) {

        boolean flag = false;
        Double schedule = 0.0;

        String sql = "select loan_schedule from t_bids where id = ? ";

        try {
            schedule = t_bids.find(sql, bidId).first();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (schedule == null) {
            schedule = 0.0;
        }

        if (schedule >= 95) {
            flag = true;
        }

        return flag;
    }


    /**
     * 资金托管模式下自动投标时额外条件判断
     *
     * @param userIdStr
     * @param bidIdStr
     * @return
     */
    public static boolean additionalJudgment(String userIdStr, String bidIdStr) {

        boolean flag = false;

        long userId = Long.parseLong(userIdStr);
        long bidId = Long.parseLong(bidIdStr);

        t_user_automatic_invest_options robot = null;
        double bidAmount = 0;

        try {
            robot = t_user_automatic_invest_options.find(" user_id = ? ", userId).first();
            bidAmount = t_bids.find(" select amount from t_bids where id = ? ", bidId).first();
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }

        if (null != robot && bidAmount > 0) {
            int dateType = robot.valid_type;
            int date = robot.valid_date;
            double minAmount = robot.min_amount;
            double maxAmount = robot.max_amount;
            Date time = robot.time;
            Date overTime = null;
            if (dateType == 0) {
                overTime = DateUtil.dateAddDay(time, date);
            } else {
                overTime = DateUtil.dateAddMonth(time, date);
            }

            boolean isOverTime = overTime.getTime() >= new Date().getTime() ? true : false;
            boolean isOverAmount = false;
            if (bidAmount >= minAmount && bidAmount <= maxAmount) {
                isOverAmount = true;
            }
            if (isOverTime && isOverAmount) {
                flag = true;
            }

        }


        return flag;
    }


    /**
     * 自动投标
     *
     * @throws ParseException
     */
    public static void automaticInvest() {

        int unit = -2;//标产品期限单位 -1：年  0：月   1：天
        long userId = -1;
        long bidId = -1;
        t_user_automatic_invest_options userParam = null;
        List<Map<String, Object>> biderList = Invest.queryAllBider();//查出所有符合自动投标条件的标的
        ErrorInfo error = new ErrorInfo();


        if (null != biderList && biderList.size() > 0) {

            OK:

            if (null != Invest.queryAllAutoUser() && Invest.queryAllAutoUser().size() > 0) {

                List<Object> userIds = Invest.queryAllAutoUser();
                //遍历所有的符合条件进度低于95%的招标中的借款
                for (Map<String, Object> map : biderList) {

                    //遍历所有设置了投标机器人用户ID
                    for (Object o : userIds) {

                        boolean over = judgeSchedule(Long.parseLong(map.get("bid_id") + ""));

                        if (over) {
                            break OK;
                        }

                        if (!over) {//借款标投标进度没有超过95%
                            userId = Long.parseLong(o.toString());
                            //资金托管模式下的额外判断
                            boolean overTime = additionalJudgment(userId + "", map.get("bid_id").toString());
                            if (map.get("user_id").toString().equals(userId + "") || !overTime) { // 如果该借款是发布者的标,则发布者不能投标,用户自动排队到后面
                                Invest.updateUserAutoBidTime(userId);//将该用户排到队尾
                            } else {
                                //获取用户设置的投标机器人参数
                                userParam = t_user_automatic_invest_options.find("from t_user_automatic_invest_options where user_id=?", userId).first();

                                if (null != userParam) {
                                    bidId = Long.parseLong(map.get("bid_id").toString());
                                    boolean flag = hasAutoInvestTheBid(bidId, userId);

                                    if (flag) {//该用户已经投过该标的
                                        Invest.updateUserAutoBidTime(userId);//将该用户排到队尾
                                    } else {
                                        unit = Integer.parseInt(map.get("period_unit").toString());
                                        Map m = Invest.queryBiderByParam(userParam, unit, bidId);//查询符合用户条件的标的

                                        if (null == m) {//没有找到符合条件的标的
                                            Invest.updateUserAutoBidTime(userId);//将该用户排到队尾
                                        } else {//找到了符合条件的标的，现在开始计算投资额
                                            double amount = Double.parseDouble(map.get("amount").toString());//标的借款总额
                                            double has_invested_amount = Double.parseDouble(map.get("has_invested_amount").toString());//标的已投金额
                                            double balance = Invest.queryAutoUserBalance(userId, userParam.retention_amout);//减去用户设置的保留余额后的用户可用余额
                                            double setAmount = userParam.amount;//用户设置的每次投标金额
                                            double loan_schedule = Double.parseDouble(map.get("loan_schedule").toString());
                                            double averageAmount = Double.parseDouble(map.get("average_invest_amount").toString());
                                            int bidAmount = Invest.calculateBidAmount(setAmount, loan_schedule, amount, has_invested_amount);//计算出投标金额

                                            if (balance < bidAmount) {//用户余额不足
                                                Invest.updateUserAutoBidTime(userId);//排到队尾
                                            } else {

                                                if (averageAmount > 0) {
                                                    bidAmount = calculateFinalInvestAmount(bidAmount, averageAmount);
                                                }
                                                invest(userId, bidId, bidAmount, "", true, false, null, error);
                                                Invest.addAutoBidRecord(userId, bidId);//添加自动投标记录
                                                Invest.updateUserAutoBidTime(userId);//排到队尾
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * 前台显示的机构借款标
     *
     * @return
     */
    public static List<v_front_all_bids> queryAgencyBids() {

        List<v_front_all_bids> agencyBids = null;
        StringBuffer sql = new StringBuffer("");
        sql.append(SQLTempletes.SELECT);
        sql.append(SQLTempletes.V_FRONT_ALL_BIDS_CREDIT);
        if (Constants.IS_BIDS_NEED_FILTER) {
            sql.append(" and t_bids.is_agency = 1");
            sql.append(" and t_bids.time > '" + Constants.BIDS_CREATETIME + "' and t_users.name = '" + Constants.BIDS_MOBILE + "' ");
        } else {
            sql.append(" and t_bids.is_agency = 1");
        }

        sql.append(" order by t_bids.time desc");

        List<Object> params = new ArrayList<Object>();

        try {
            EntityManager em = JPA.em();

            Query query = em.createNativeQuery(sql.toString(), v_front_all_bids.class);
            for (int n = 1; n <= params.size(); n++) {
                query.setParameter(n, params.get(n - 1));
            }
            query.setMaxResults(Constants.HOME_BID_COUNT);
            agencyBids = query.getResultList();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return agencyBids;
    }


    /**
     * 前台显示的普通借款标
     *
     * @return
     */
    public static List<v_front_all_bids> queryBids() {

        List<v_front_all_bids> bids = null;
        StringBuffer sql = new StringBuffer("");
//		sql.append(SQLTempletes.SELECT);
//		sql.append(SQLTempletes.V_FRONT_ALL_BIDS_CREDIT);
//		if(Constants.IS_BIDS_NEED_FILTER){
//			sql.append(" and t_bids.is_agency = 0 and t_bids.time > '"+Constants.BIDS_CREATETIME +"' and t_users.name = '"+Constants.BIDS_MOBILE +"' and t_bids.status in(?, ?) order by t_bids.id desc");
//		}else {
//			sql.append(" and t_bids.is_agency = 0 and t_bids.status in(?, ?) order by t_bids.id desc");
//		}
//		
//		List<Object> params = new ArrayList<Object>();
//		params.add(Constants.BID_ADVANCE_LOAN);
//		params.add(Constants.BID_FUNDRAISE);

        sql.append(" (SELECT " + SQLTempletes.V_FRONT_ALL_BIDS_LIST1 + " WHERE t_bids.is_hot=1 AND t_bids.STATUS = 2 ORDER BY t_bids.TIME DESC LIMIT 1) ");
        sql.append("UNION (SELECT " + SQLTempletes.V_FRONT_ALL_BIDS_LIST1 + " WHERE t_bids.is_quality = 1 AND t_bids.STATUS = 2 ORDER BY t_bids.TIME DESC  LIMIT 8) ");
        sql.append("UNION (SELECT " + SQLTempletes.V_FRONT_ALL_BIDS_LIST1 + " WHERE t_bids.TIME > DATE_ADD(NOW(), INTERVAL 3 HOUR) AND t_bids.STATUS = 2 ORDER BY t_bids.TIME DESC LIMIT 2) ");
        sql.append("UNION (SELECT " + SQLTempletes.V_FRONT_ALL_BIDS_LIST1 + " WHERE t_bids.STATUS = 2 and t_bids.real_invest_expire_time IS NULL ORDER BY t_bids.TIME  DESC LIMIT 8) ");
        sql.append("UNION (SELECT " + SQLTempletes.V_FRONT_ALL_BIDS_LIST1 + " WHERE t_bids.STATUS = 2 and t_bids.real_invest_expire_time IS NOT NULL ORDER BY t_bids.TIME DESC LIMIT 8) ");
        sql.append("UNION (SELECT " + SQLTempletes.V_FRONT_ALL_BIDS_LIST1 + " WHERE t_bids.STATUS = 4 ORDER BY t_bids.TIME DESC LIMIT 8) ");

        Logger.info("sql:" + sql.toString());
        try {
            EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(), v_front_all_bids.class);
//            for(int n = 1; n <= params.size(); n++){
//                query.setParameter(n, params.get(n-1));
//            }
            query.setMaxResults(Constants.HOME_BID_COUNT);//返回的条数
            bids = query.getResultList();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bids;
    }

    /**
     * 投资记录
     *
     * @param pageBean 分页对象
     * @param bidId    标ID
     */
    public static List<v_invest_records> bidInvestRecord(
            PageBean<v_invest_records> pageBean,
            long bidId,
            ErrorInfo error) {
        error.clear();

        int count = -1;
        List<v_invest_records> record_list = null;
        StringBuffer sql = new StringBuffer("");
        sql.append(SQLTempletes.PAGE_SELECT);
        sql.append(SQLTempletes.V_INVEST_RECORDS);
        sql.append(" and bid_id = ?");
        List<Object> params = new ArrayList<Object>();
        params.add(bidId);

        try {
            //count = (int) v_invest_records.count("bid_id = ?", bidId);
            EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(), v_invest_records.class);
            for (int n = 1; n <= params.size(); n++) {
                query.setParameter(n, params.get(n - 1));
            }
            query.setFirstResult((pageBean.currPage - 1) * pageBean.pageSize);
            query.setMaxResults(pageBean.pageSize);
            record_list = query.getResultList();

            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

        } catch (Exception e) {
            Logger.error("理财->标投资记录,查询总记录数:" + e.getMessage());
            error.msg = error.FRIEND_INFO + "加载投资记录失败!";

            return null;
        }

        if (count < 1)
            return new ArrayList<v_invest_records>();

        pageBean.totalCount = count;

        return record_list;
    }


    /**
     * 理财风云榜
     *
     * @return
     */
    public static List<v_bill_board> investBillboard() {

        List<v_bill_board> investBillboard = new ArrayList<v_bill_board>();
        StringBuffer sql = new StringBuffer("");
        sql.append(SQLTempletes.SELECT);
        sql.append(SQLTempletes.V_BILL_BOARD);
        sql.append(" group by t_bill_invests.user_id ");
        sql.append("order by sum((t_bill_invests.receive_corpus + t_bill_invests.receive_interest)) desc");

        try {
            //investBillboard = v_bill_board.find("").fetch(5);
            EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(), v_bill_board.class);
            query.setMaxResults(5);
            investBillboard = query.getResultList();

        } catch (Exception e) {
            e.printStackTrace();

            return investBillboard;
        }

        return investBillboard;
    }

    /**
     * 理财风云榜(更多)
     *
     * @return
     */
    public static PageBean<v_bill_board> investBillboards(int currPage, ErrorInfo error) {
        error.clear();

        if (currPage < 1) {
            currPage = 1;
        }

        if (currPage > 5) {
            currPage = 5;
        }

        List<v_bill_board> investBillboard = new ArrayList<v_bill_board>();
        StringBuffer sql = new StringBuffer("");
        sql.append(SQLTempletes.PAGE_SELECT);
        sql.append(SQLTempletes.V_BILL_BOARD);
        sql.append(" group by t_bill_invests.user_id ");
        sql.append("order by sum((t_bill_invests.receive_corpus + t_bill_invests.receive_interest)) desc");

        int count = 0;

        try {
            EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(), v_bill_board.class);
            query.setFirstResult((currPage - 1) * 10);
            query.setMaxResults(10);
            investBillboard = query.getResultList();

            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), new ArrayList<Object>());

        } catch (Exception e) {
            e.printStackTrace();
            Logger.info("查询Top50投资金额排行时：" + e.getMessage());

            error.code = 0;
            error.msg = "查询Top50投资金额排行失败";

            return null;
        }

        count = count > 50 ? 50 : count;

        PageBean<v_bill_board> page = new PageBean<v_bill_board>();

        page.pageSize = 10;
        page.currPage = currPage;
        page.totalCount = count;

        page.page = investBillboard;

        error.code = 0;

        return page;
    }


    /**
     * 根据标产品资料ID查出用户提交的对应资料
     *
     * @param itemId
     * @return
     */
    public static UserAuditItem getAuditItem(long itemId, long userId) {

        String hql = "select audit_item_id from t_product_audit_items where id = ?";
        String sql = "select id from t_user_audit_items where user_id = ? and audit_item_id = ?";
        Long userItemId = 0l;
        Long productItemId = 0l;

        try {
            productItemId = t_product_audit_items.find(hql, itemId).first();
            userItemId = t_user_audit_items.find(sql, userId, productItemId).first();
        } catch (Exception e) {
            e.printStackTrace();
        }


        UserAuditItem item = new UserAuditItem();
        if (null != userItemId) {
            item.id = userItemId;
        }
        return item;
    }


    /**
     * 根据投资ID查询账单
     *
     * @param investId
     * @param error
     * @return
     */
    public static Long queryBillByInvestId(long investId, ErrorInfo error) {

        Long billId = 0l;

        try {
            billId = t_bills.find("select id from t_bill_invests where invest_id = ? ", investId).first();
        } catch (Exception e) {
            e.printStackTrace();
        }

        error.code = 1;
        return billId;
    }


    /**
     * ajax分页查询债权竞拍记录
     *
     * @param debtId
     */
    public static PageBean<v_debt_auction_records> viewAuctionRecords(int pageNum, int pageSize, long debtId, ErrorInfo error) {

        PageBean<v_debt_auction_records> page = new PageBean<v_debt_auction_records>();

        int currPage = pageNum;

        page.currPage = currPage;
        page.pageSize = pageSize;

        List<v_debt_auction_records> list = new ArrayList<v_debt_auction_records>();

        StringBuffer sql = new StringBuffer("");
        sql.append(SQLTempletes.PAGE_SELECT);
        sql.append(SQLTempletes.V_DEBT_AUCTION_RECORDS);
        sql.append(" and t_invest_transfer_details.transfer_id=?");

        List<Object> params = new ArrayList<Object>();
        params.add(debtId);

        try {
            EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(), v_debt_auction_records.class);
            for (int n = 1; n <= params.size(); n++) {
                query.setParameter(n, params.get(n - 1));
            }
            query.setFirstResult((page.currPage - 1) * page.pageSize);
            query.setMaxResults(page.pageSize);
            list = query.getResultList();

            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

        } catch (Exception e) {
            e.printStackTrace();
            Logger.info(e.getMessage());
            error.code = -1;
        }

        page.page = list;
        error.code = 1;
        return page;
    }


    /**
     * 获取理财首页所有借款标数目
     *
     * @param error
     * @return
     */
    public static Long getBidCount(ErrorInfo error) {

        Long count = 0l;

        try {
            count = v_front_all_bids.count();
        } catch (Exception e) {
            e.printStackTrace();
            error.code = -1;
        }

        if (null == count) {
            count = 0l;
        }

        error.code = 1;
        return count;
    }

    /**
     * 取消债权关注
     *
     * @param debtId
     * @param error
     */
    public static void canaleBid(Long attentionId, ErrorInfo error) {

        t_user_attention_bids attentionBid = null;

        try {
            attentionBid = t_user_attention_bids.findById(attentionId);
        } catch (Exception e) {
            Logger.error("查询关注的借款标时：" + e.getMessage());
            error.code = -1;
            error.msg = "查询关注的借款标异常";

            return;
        }

        if (null != attentionBid) {
            attentionBid.delete();
            error.code = 1;
            error.msg = "取消收藏借款标成功";

            return;
        }

        error.code = -2;
        error.msg = "查询关注的借款标异常";

        return;
    }

	/* 2014-11-15 */

    /**
     * 借款合同
     */
    public static String queryPact(long id) {
        if (id < 1)
            return "查看失败!";

        try {
            return t_invests.find("select pact from t_invests where id = ?", id).first();
        } catch (Exception e) {
            e.printStackTrace();

            return "查看失败!";
        }
    }

    /**
     * 居间服务协议
     */
    public static String queryIntermediaryAgreement(long id) {
        if (id < 1)
            return "查看失败!";

        try {
            return t_invests.find("select intermediary_agreement from t_invests where id = ?", id).first();
        } catch (Exception e) {
            e.printStackTrace();

            return "查看失败!";
        }
    }

    /**
     * 保障涵
     */
    public static String queryGuaranteeBid(long id) {
        if (id < 1)
            return "查看失败!";

        try {
            return t_invests.find("select guarantee_invest from t_invests where id = ?", id).first();
        } catch (Exception e) {
            e.printStackTrace();

            return "查看失败!";
        }
    }


    /**
     * 生成借款合同（理财人）
     *
     * @param bidId
     * @param error
     */
    public static void creatInvestPact(long bidId, ErrorInfo error) {

        TemplatePact pact = new TemplatePact();
        pact.id = Templets.BID_PACT_INVEST;
        if (pact.is_use) {
            List<Long> investIds = new ArrayList<Long>();
            String sql = "select id from t_invests where bid_id = ? and transfer_status <> -1";

            try {
                investIds = t_invests.find(sql, bidId).fetch();

            } catch (Exception e) {
                e.printStackTrace();
                error.msg = "系统异常";
                error.code = -1;
                return;
            }


            if (investIds.size() > 0) {
                for (Long investId : investIds) {
                    String pact_no = investId + DateUtil.simple(new Date());
                    creatSingleInvestPact(investId, error);
                    if (error.code < 0) {
                        JPA.setRollbackOnly();
                        error.msg = "创建平台协议失败";
                        error.code = -1;
                        return;
                    }
                    creatSingleGuaranteeInvest(investId, pact_no, error);
                    if (error.code < 0) {
                        JPA.setRollbackOnly();
                        error.msg = "创建平台协议失败";
                        error.code = -1;
                        return;
                    }
                    creatSingleIntermediaryAgreement(investId, error);
                    if (error.code < 0) {
                        JPA.setRollbackOnly();
                        error.msg = "创建平台协议失败";
                        error.code = -1;
                        return;
                    }
                }
            }
            error.msg = "创建成功";
            error.code = 1;
            return;
        } else {
            error.msg = "平台协议未开启";
            error.code = 1;
            return;
        }

    }


    /**
     * 根据单个投资记录生成理财合同
     *
     * @param investId
     * @param error
     */
    public static void creatSingleInvestPact(long investId, ErrorInfo error) {

        TemplatePact pact = new TemplatePact();
        pact.id = Templets.BID_PACT_INVEST;

        t_users investUser = new t_users();
        t_users bidUser = new t_users();
        t_invests invest = new t_invests();
        t_bids bid = new t_bids();
        Double amount = 0.0;
        String company_name = "";
        Double sum = 0.0;

        String hql = "select sum(receive_corpus + receive_interest) from t_bill_invests where invest_id = ?";
        String sql1 = "select _value from t_system_options where _key = ?";
        String sql2 = "select sum(repayment_corpus + repayment_interest) from t_bills where bid_id = ? ";
        try {
            invest = t_invests.findById(investId);
            bid = t_bids.findById(invest.bid_id);
            investUser = t_users.findById(invest.user_id);
            bidUser = t_users.findById(bid.user_id);
            amount = t_bill_invests.find(hql, investId).first();
            company_name = t_system_options.find(sql1, "company_name").first();
            sum = t_bills.find(sql2, bid.id).first();
        } catch (Exception e) {
            error.msg = "系统异常";
            error.code = -1;
            return;
        }

        if (null == amount) {
            amount = 0.0;
        }

        String no = investId + DateUtil.simple(new Date());
        StringBuffer investTable = new StringBuffer(" <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"> <tr height=\"36\"><td>投资人名称</td><td>投资金额(人民币)</td><td>年利率</td>" +
                "<td>投资日期</td><td>本息合计总金额(人民币)</td></tr>");

        investTable.append("<tr height=\"30\">");
        investTable.append("<td>" + investUser.name + "</td>");
        investTable.append("<td>￥" + invest.amount + "</td>");
        investTable.append("<td>" + bid.apr + "%</td>");
        investTable.append("<td>" + DateUtil.dateToString1(invest.time) + "</td>");
        investTable.append("<td>" + amount + "</td>");
        investTable.append("</tr></table>");

        String content = pact.content;
        content = content.replace(Templets.INVEST_NAME, investUser.name)
                .replace(Templets.LOAN_NAME, bidUser.reality_name)
                .replace(Templets.ID_NUMBER, bidUser.id_number)
                .replace(Templets.PACT_NO, no)
                .replace(Templets.COMPANY_NAME, company_name)
                .replace(Templets.DATE, DateUtil.dateToString(new Date()))
                .replace(Templets.INVEST_LIST, investTable.toString());

        Bid bidbusiness = new Bid();
        bidbusiness.auditBid = true;
        bidbusiness.id = bid.id;


        String repayTime = bidbusiness.isSecBid ?
                DateUtil.simple(new Date()) :
                ServiceFee.repayTime(bidbusiness.periodUnit, bidbusiness.period,
                        (int) bidbusiness.repayment.id);

        content = content.replace(Templets.PURPOSE_NAME, bidbusiness.purpose.name)
                .replace(Templets.AMOUNT, bidbusiness.amount + "")
                .replace(Templets.APR, bidbusiness.apr + "%")
                .replace(Templets.PERIOD, bidbusiness.period + "")
                .replace(Templets.PERIOD_UNIT, bidbusiness.strPeriodUnit)
                .replace(Templets.REPAYMENT_NAME, bidbusiness.repayment.name)
                .replace(Templets.CAPITAL_INTEREST_SUM, sum + "")
                .replace(Templets.REPAYMENT_TIME, repayTime);


        StringBuffer billTable = new StringBuffer("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"> <tr height=\"36\"><td>期数</td><td>应还时间</td>" +
                "<td>应还本金</td><td>应还利息</td><td>应还本息合计</td></tr>");

        List<t_bill_invests> bills = new ArrayList<t_bill_invests>();
        String strsql = " invest_id = ? ";
        long periodCount = 0;

        try {
            bills = t_bill_invests.find(strsql, investId).fetch();
            periodCount = t_bill_invests.count(strsql, investId);
        } catch (Exception e) {
            e.printStackTrace();
            error.msg = "系统异常";
            error.code = -1;
            return;
        }
        DecimalFormat myformat = new DecimalFormat();
        myformat.applyPattern("##,##0.00");

        if (bills.size() > 0) {
            for (t_bill_invests bill : bills) {
                billTable.append("<tr height=\"30\">");
                billTable.append("<td>" + bill.periods + "/" + periodCount + "</td>");
                billTable.append("<td>" + DateUtil.dateToString1(bill.receive_time) + "</td>");
                billTable.append("<td>" + myformat.format(bill.receive_corpus) + "</td>");
                billTable.append("<td>" + myformat.format(bill.receive_interest) + "</td>");
                String temp = myformat.format(bill.receive_corpus + bill.receive_interest);
                billTable.append("<td>" + temp + "</td>");
                billTable.append("</tr>");
            }

        }
        billTable.append("</table>");

        content = content.replace(Templets.INVEST_BILL_LIST, billTable);
        hql = "update t_invests set pact = ? where id = ? ";
        EntityManager em = JPA.em();
        int rows = 0;
        try {
            rows = em.createQuery(hql).setParameter(1, content).setParameter(2, investId).executeUpdate();
        } catch (Exception e) {
            error.msg = "系统异常";
            error.code = -1;
            return;
        }

        if (rows == 0) {
            error.msg = "系统异常";
            error.code = -1;
            return;
        }

        error.msg = "生成协议成功";
        error.code = 1;
        return;

    }


    /**
     * 针对单挑投标记录创建居间服务协议（投资人）
     *
     * @param investId
     * @param error
     */
    public static void creatSingleIntermediaryAgreement(long investId, ErrorInfo error) {

        TemplatePact pact = new TemplatePact();
        pact.id = Templets.INTERMEDIARY_AGREEMENT_INVEST;
        t_users investUser = new t_users();
        t_invests invest = new t_invests();

        try {
            invest = t_invests.findById(investId);
            investUser = t_users.findById(invest.user_id);
        } catch (Exception e) {
            error.msg = "系统异常";
            error.code = -1;
            return;
        }

        String investRealityName = investUser.reality_name == null ? "" : investUser.reality_name;
        String investRealityIdno = investUser.id_number == null ? "" : investUser.id_number;

        String content = pact.content;
        content = content.replace(Templets.INVEST_NAME, investUser.name)
                .replace(Templets.ID_NUMBER, investRealityIdno)
                .replace(Templets.INVEST_REALITY_NAME, investRealityName)
                .replace(Templets.DATE, DateUtil.dateToString1(new Date()));

        String hql = "update t_invests set intermediary_agreement = ? where id = ? ";
        EntityManager em = JPA.em();
        int rows = 0;
        try {
            rows = em.createQuery(hql).setParameter(1, content).setParameter(2, investId).executeUpdate();
        } catch (Exception e) {
            error.msg = "系统异常";
            error.code = -1;
            return;
        }

        if (rows == 0) {
            error.msg = "系统异常";
            error.code = -1;
            return;
        }
        error.msg = "生成协议成功";
        error.code = 1;
        return;
    }


    /**
     * 针对单挑记录生成对应保障函
     *
     * @param investId
     * @param error
     */
    public static void creatSingleGuaranteeInvest(long investId, String pact_no, ErrorInfo error) {
        TemplatePact pact = new TemplatePact();
        pact.id = Templets.GUARANTEE_INVEST;
        t_users investUser = new t_users();
        t_bids bid = new t_bids();
        t_invests invest = new t_invests();
        String company_name = "";
        String sql1 = "select _value from t_system_options where _key = ?";
        try {
            invest = t_invests.findById(investId);
            bid = t_bids.findById(invest.bid_id);
            investUser = t_users.findById(invest.user_id);
            company_name = t_system_options.find(sql1, "company_name").first();
        } catch (Exception e) {
            error.msg = "系统异常";
            error.code = -1;
            return;
        }

        int period = bid.period;
        int periodUnit = bid.period_unit;
        String periodStr = "";
        if (periodUnit == -1) {
            periodStr = period + "年";
        } else if (periodUnit == 1) {
            periodStr = "1个月";
        } else {
            periodStr = period + "个月";
        }

        String investRealityName = "";

        if (investUser.is_add_base_info) {
            investRealityName = investUser.reality_name;
        }


        String content = pact.content;
        DecimalFormat df = new DecimalFormat();
        df.applyPattern("###.00");
        content = content.replace(Templets.INVEST_NAME, investUser.name)
                .replace(Templets.INVEST_REALITY_NAME, investRealityName)
                .replace(Templets.CHINESE_AMOUNT, new CnUpperCaser(df.format(invest.amount)).getCnString())
                .replace(Templets.COMPANY_NAME, company_name)
                .replace(Templets.DATE, DateUtil.dateToString(new Date()))
                .replace(Templets.PACT_NO, pact_no)
                .replace(Templets.PERIOD, periodStr);

        String hql = "update t_invests set guarantee_invest = ? where id = ? ";
        EntityManager em = JPA.em();
        int rows = 0;
        try {
            rows = em.createQuery(hql).setParameter(1, content).setParameter(2, investId).executeUpdate();
        } catch (Exception e) {
            error.msg = "系统异常";
            error.code = -1;
            return;
        }

        if (rows == 0) {
            error.msg = "系统异常";
            error.code = -1;
            return;
        }

        error.msg = "生成协议成功";
        error.code = 1;
        return;
    }


    /**
     * 定时执行生成借款合同，理财合同等协议
     */
    public static void creatBidPactJob() {
        ErrorInfo error = new ErrorInfo();
        List<Object> bidIds = new ArrayList<Object>();
        String sql = "select id from t_bids where status in (4,5) and (ISNULL(pact) or ISNULL(guarantee_bid) or ISNULL(intermediary_agreement))";
        Query query = JPA.em().createNativeQuery(sql);

        try {
            bidIds = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (null == bidIds || bidIds.size() == 0)
            return;

        Bid bid = null;
        long _bidId = 0;

        for (Object bidId : bidIds) {
            bid = new Bid();
            bid.auditBidPact = true;
            _bidId = Long.parseLong(bidId.toString());

            try {
                bid.id = _bidId;

                bid.createPact();//生成借款合同
                Invest.creatInvestPact(_bidId, error);//生成理财合同
            } catch (Exception e) {
                continue;
            }
        }
    }


    /**
     * 定时执行生成债权协议
     */
    public static void creatDebtPactJob() {

        ErrorInfo error = new ErrorInfo();
        List<Object> investIds = new ArrayList<Object>();
        String sql = "select id from t_invests where transfers_id > 0 and ISNULL(pact) and ISNULL(guarantee_invest) and ISNULL(intermediary_agreement) ";
        t_invests invest = new t_invests();
        t_invests originalInvest = new t_invests();
        t_invest_transfers debt = new t_invest_transfers();
        Query query = JPA.em().createNativeQuery(sql);
        try {
            investIds = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (investIds.size() > 0) {
            for (Object invesId : investIds) {
                invest = t_invests.findById(Long.parseLong(invesId.toString()));
                debt = t_invest_transfers.findById(invest.transfers_id);
                originalInvest = t_invests.findById(debt.invest_id);
                long presentInvestUserId = invest.user_id;
                long investId = invest.id;
                long debtId = invest.transfers_id;
                long originalInvestUserId = originalInvest.user_id;

                Debt.creatDebtAgreement(originalInvestUserId, presentInvestUserId, debtId, investId, error);
            }
            return;
        } else {
            return;
        }
    }


    /**
     * 查询前台最新三条理财资讯
     *
     * @return
     */
    public static List<Map<String, String>> queryNearlyInvest(ErrorInfo error) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        List<t_invests> invests = new ArrayList<t_invests>();
        invests = t_invests.find(" order by id desc").fetch(Constants.NEW_FUNDRAISEING_BID);

        String userName = "";
        Long count = 0l;
        Double apr = 0.0;
        Map<String, String> map = null;
        for (t_invests invest : invests) {
            map = new HashMap<String, String>();
            try {
                userName = t_users.find("select name from t_users where id = ? ", invest.user_id).first();
                apr = t_bids.find("select apr from t_bids where id = ? ", invest.bid_id).first();
                count = t_invests.find("select count(*) from t_invests where user_id = ? ", invest.user_id).first();
            } catch (Exception e) {
                e.printStackTrace();
                error.msg = "查询最新理财资讯异常";
                error.code = -1;

                return null;
            }

            map = new HashMap<String, String>();
            map.put("id", invest.bid_id + "");
            map.put("userName", userName);
            map.put("count", count + "");
            map.put("apr", apr + "");
            map.put("amount", invest.amount + "");
            list.add(map);
        }

        return list;
    }

    /**
     * 根据订单流水号查询交易记录是否存在
     *
     * @param pMerBillNo
     * @param error
     */
    public static long queryIsInvest(String pMerBillNo, ErrorInfo error) {
        error.clear();

        String sql = "select count(1) from t_invests where mer_bill_no = ? limit 1";
        long rows = 0;

        try {
            rows = ((BigInteger) JPA.em().createNativeQuery(sql).setParameter(1, pMerBillNo).getSingleResult()).longValue();
        } catch (Exception e) {
            Logger.error("根据订单流水号查询交易记录是否存在时：" + e.getMessage());

            error.code = -1;
            error.msg = "根据订单流水号查询交易记录是否存在时";

            return -1;
        }

        error.code = 1;
        return rows;
    }

    /**
     * 解冻投资金额
     *
     * @param pMerBillNo
     * @param error
     */
    public static void unfreezeInvest(String pMerBillNo, ErrorInfo error) {
        error.clear();

        long row = queryIsInvest(pMerBillNo, error);

        if (error.code < 0) {
            return;
        }

        if (row == 0) {
            error.code = 2;
            error.msg = "记录不存在，无需解冻";
            return;
        }

        t_invests invest = null;

        try {
            invest = t_invests.find("mer_bill_no = ?", pMerBillNo).first();
        } catch (Exception e) {
            Logger.error("解冻投资保证金失败" + e.getMessage());

            error.code = -1;
            error.msg = "查询投资金额失败";

            return;
        }

        DataSafety data = new DataSafety();

        data.id = invest.user_id;

        if (!data.signCheck(error)) {
            JPA.setRollbackOnly();

            return;
        }

        v_user_for_details forDetail = DealDetail.queryUserBalance(invest.user_id, error);

        if (error.code < 0) {
            JPA.setRollbackOnly();

            return;
        }

        double balance = forDetail.user_amount;
        double freeze = forDetail.freeze;
        double receiveAmount = forDetail.receive_amount;

        DealDetail.updateUserBalance(invest.user_id, balance + invest.amount, freeze - invest.amount, error);

        if (error.code < 0) {
            JPA.setRollbackOnly();

            return;
        }

        if (error.code < 0) {
            JPA.setRollbackOnly();

            return;
        }

        DealDetail dealDetail = null;

		/* 添加交易记录 */
        dealDetail = new DealDetail(invest.user_id,
                DealType.THAW_FREEZE_INVESTAMOUNT, invest.amount,
                invest.bid_id, balance + invest.amount, freeze - invest.amount, receiveAmount,
                "解冻投资金额" + invest.amount + "元");

        dealDetail.addDealDetail(error);

        if (error.code < 0) {
            return;
        }

        data.id = invest.user_id;
        data.updateSign(error);

        if (error.code < 0) {
            return;
        }

        error.code = 1;
        error.msg = "解冻金额成功";
    }


    public static List<ProductVo> findProductsBy(PageVo<ProductVo> pageVo) {
        int index = pageVo.getIndex();
        int pageSize = pageVo.getPageSize();

        StringBuffer jpql = new StringBuffer();
        jpql.append(" select ");
        jpql.append(" p.id as prodId,");
        jpql.append(" p.title as \"prodName\",");
        jpql.append(" p.apr as \"interestRate\",");
        jpql.append(" CASE WHEN p.period < 100 THEN CONCAT(p.period,'天') ELSE CONCAT(floor(p.period /30),'个月') END as \"deadline\",");
        jpql.append(" p.min_invest_amount as \"bidMoney\",");
        jpql.append(" p.is_new as \"isNewUser\",");
        jpql.append(" p.amount-p.has_invested_amount as \"remainingAvailableMoney\",");
        jpql.append(" p.amount as \"availableMoney\",");
        jpql.append(" p.time as \"sellStartTime\",");
        jpql.append(" p.invest_expire_time as \"sellEndTime\",");
        jpql.append(" DATE_ADD(p.time,INTERVAL p.period DAY) as \"predictDeadline\",");
        jpql.append(" p.amount as \"totalBidMoney\",");
        jpql.append(" p.status as \"prodStatus\"");
        jpql.append(" from");
        jpql.append(" v_bids_info p");
        jpql.append(" where 1=1");

        String querySql = "select count(1) from (" + jpql.toString() + ") as rs";

        int count = Integer.valueOf(JPA.em().createNativeQuery(querySql).getSingleResult().toString());
        pageVo.setCount(count);
        Query nativeQuery = JPA.em().createNativeQuery(jpql.toString());
        nativeQuery.setFirstResult(index);
        if (pageSize > 0) {
            nativeQuery.setMaxResults(pageSize);
        }
        nativeQuery.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        List<Map<String, Object>> resultList = nativeQuery.getResultList();

        List<ProductVo> productVos = new ArrayList<ProductVo>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (Map<String, Object> row : resultList) {
            productVos.add(objectMapper.convertValue(row, ProductVo.class));
        }
        return productVos;
    }
}
