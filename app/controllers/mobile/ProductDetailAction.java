package controllers.mobile;

import business.Bid;
import business.Invest;
import controllers.BaseController;
import controllers.app.RequestData;
import controllers.app.common.Message;
import controllers.app.common.MsgCode;
import controllers.app.common.Severity;
import models.v_invest_records;
import models.y_subject_url;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.PageBean;

import javax.persistence.Query;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>Project: com.shovesoft.sp2p</p>
 * <p>Title: ProductAction.java</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2014 Sunlights.cc</p>
 * <p>All Rights Reserved.</p>
 *
 * @author <a href="mailto:jiaming.wang@sunlights.cc">wangJiaMing</a>
 */
public class ProductDetailAction extends BaseController {

    /**
     * 投标记录
     * @param pageNum(为分页预留字段)
     * @param pageSize(为分页预留字段)
     * @param bidId
     */
    public static void bidRecords(int pageNum, int pageSize,String bidId){
        ErrorInfo error = new ErrorInfo();
        int currPage = 1;
        pageSize = 50;
        if(null==bidId){
            LoginAction.login();
            return;
        }
        long bidid = Long.parseLong(bidId);
        PageBean<v_invest_records> pageBean = new PageBean<v_invest_records>();
        pageBean = Invest.queryBidInvestRecords(currPage, pageSize, bidid, error);
        render(pageBean,bidid);
    }

    /**
     * 项目详情
     * @param borrowId
     * @return
     */
    public static void projectDetail(String borrowId) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        long bidId = 0;
        try {
            bidId = Long.parseLong(borrowId);
            if(bidId==0){
                LoginAction.login();
                return;
            }
        }catch (Exception e){
            LoginAction.login();
            return;
        }
        Bid bid = buildBid(bidId);
        jsonMap.put("error", -1);
        jsonMap.put("msg", "查询成功");
        if(true==bid.isAgency){
            jsonMap.put("project_introduction",bid.description);
            if(null!=bid.description && bid.description.length()>100){
                    jsonMap.put("project_introduction_short",bid.description.substring(0,100)+"......");
            }else{
                jsonMap.put("project_introduction_short",bid.description);
            }
            jsonMap.put("company_info", bid.company_info);
            jsonMap.put("borrowId", bid.id);
            jsonMap.put("isAgency", bid.isAgency);
            jsonMap.put("bidType", 1);//0表示个人标,1表示机构标
        }else{
            jsonMap.put("project_introduction",bid.description);
            if(null!=bid.description && bid.description.length()>100){
                jsonMap.put("project_introduction_short",bid.description.substring(0,100)+"......");
            }else{
                jsonMap.put("project_introduction_short",bid.description);
            }
            jsonMap.put("borrowId", bid.id);
            jsonMap.put("isAgency", bid.isAgency);
            jsonMap.put("bidType", 0);//0表示个人标,1表示机构标
            jsonMap.put("personInfo", bid.project_introduction);//项目简述字段替换成personInfo
            jsonMap.put("realityName",bid.user.realityName.substring(0,1)+"**");
            jsonMap.put("sex", bid.user.sex);
            jsonMap.put("idNumber", bid.user.idNumber.substring(0,4)+"***");
            jsonMap.put("cityName", bid.user.provinceName+bid.user.cityName);
            jsonMap.put("educationName", bid.user.educationName);
            jsonMap.put("maritalName", bid.user.maritalName);
            jsonMap.put("houseName", bid.user.houseName);
            jsonMap.put("carName", bid.user.carName);

        }
        render(jsonMap);
    }

    /**
     * 资金安全
     *
     * @param borrowId
     * @return
     */
    public static void fundSecurity(String borrowId)  {
        List<y_subject_url> userAuditList = new ArrayList<y_subject_url>();
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        long bidId = 0;
        try {
            bidId = Long.parseLong(borrowId);
            if(bidId==0){
                LoginAction.login();
                return;
            }
        }catch (Exception e){
            LoginAction.login();
            return;
        }
        Bid bid = buildBid(bidId);

        String sql="select	a.id,b.name ,a.image_file_name from t_user_audit_items a ,t_dict_audit_items b  where a.mark=b.mark and a.status=2 and a.user_id=?";
        try{
            Query query = JPA.em().createNativeQuery(sql,y_subject_url.class);
            query.setParameter(1, bid.userId);
            userAuditList=query.getResultList();
        }catch (Exception e) {
            e.printStackTrace();
        }
        jsonMap.put("error", -1);
        jsonMap.put("userAuditList", userAuditList);
        jsonMap.put("msg", "查询成功");
        jsonMap.put("repayment_res", bid.repayment_res);
        if(null!=bid.repayment_res && bid.repayment_res.length()>100){
            jsonMap.put("repayment_res_short",bid.repayment_res.substring(0,100)+"......");
        }else{
            jsonMap.put("repayment_res_short",bid.repayment_res);
        }
        //jsonMap.put("about_risk", bid.about_risk); jsonMap.put("repayment_res", bid.repayment_res+ "/r/n" + bid.risk_control);
        jsonMap.put("borrowId", bid.id);

        render(jsonMap);
    }

    /**
     * 收益方式接口
     * @param borrowId
     * @return
     */
    public static void returnMode(String borrowId){
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        Map<String, Object> timeNode1 = new HashMap<String, Object>();//开售时间
        Map<String, Object> timeNode2 = new HashMap<String, Object>();//起息时间
        Map<String, Object> timeNode3 = new HashMap<String, Object>();//还本结息时间
        DateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
        List timeNodeList=new LinkedList();
        long bidId = 0;
        try {
            bidId = Long.parseLong(borrowId);
            if(bidId==0){
                LoginAction.login();
                return;
            }
        }catch (Exception e){
            LoginAction.login();
            return;
        }
        Bid bid = buildBid(bidId);
        jsonMap.put("error", -1);
        jsonMap.put("msg", "查询成功");
        jsonMap.put("feeType", "无");//手续费
        jsonMap.put("repayment_tips", "本项目只支持提前还款,到期后本金和收益自动归还到余额帐户");//还款提示
        jsonMap.put("paymentType", bid.repayment.id);
        jsonMap.put("paymentMode", bid.repayment.name);//还款方式
        timeNode1.put("nodeTime", dft.format(bid.time));//发布时间  开售时间//备注：添加预计时间字段后需要增加completeInd状态逻辑
        timeNode1.put("nodeName", "开售时间");
        timeNode1.put("imgUrl", "/public/images/h5icon/sale_time_01.png");
        timeNode1.put("completeInd", 1);//1表示有效，0表示无效
        timeNodeList.add(timeNode1);
        if(null!=bid.realInvestExpireTime){
            timeNode2.put("nodeTime", dft.format(bid.realInvestExpireTime));//实际满标时间
            timeNode2.put("nodeName", "起息日");
            timeNode2.put("completeInd", 1);
            timeNode2.put("imgUrl", "/public/images/h5icon/qixi_date_01.png");
            timeNodeList.add(timeNode2);
        }else{
            timeNode2.put("nodeTime", dft.format(bid.investExpireTime));//预计满标时间 =起息日
            timeNode2.put("nodeName", "起息日");
            timeNode2.put("completeInd", 0);
            timeNode2.put("imgUrl", "/public/images/h5icon/qixi_date_02.png");
            timeNodeList.add(timeNode2);
        }
        if(null!=bid.recentRepayTime){
            timeNode3.put("nodeTime", dft.format(bid.recentRepayTime));//还本结息日    //还款日   recentRepayTime 	period//借款期限      periodUnit //-1 年 0月  1日
            timeNode3.put("nodeName", "还本结息日");
            if(5==bid.status){
                timeNode3.put("completeInd", 1);
                timeNode3.put("imgUrl", "/public/images/h5icon/jiexi_finish_01.png");
            }else{
                timeNode3.put("completeInd", 0);
                timeNode3.put("imgUrl", "/public/images/h5icon/jiexi_finish_02.png");
            }

            timeNodeList.add(timeNode3);
        }else{

            Date begindate=bid.investExpireTime;
            Calendar date = Calendar.getInstance();
            date.setTime(begindate);
            if(-1==bid.periodUnit){
                date.add(date.YEAR,bid.period);
                timeNode3.put("nodeTime", dft.format(date.getTime()));
            }else if(0==bid.periodUnit){
                date.add(date.MONTH,bid.period);
                timeNode3.put("nodeTime", dft.format(date.getTime()));
            }else if(1==bid.periodUnit){
                date.add(date.DAY_OF_YEAR,bid.period);
                timeNode3.put("nodeTime", dft.format(date.getTime()));
            }else{
                date.add(date.DAY_OF_YEAR,bid.period);
                timeNode3.put("nodeTime", dft.format(date.getTime()));
            }
            timeNode3.put("nodeName", "还本结息日");
            timeNode3.put("completeInd", 0);
            timeNode3.put("imgUrl", "/public/images/h5icon/jiexi_finish_02.png");
            timeNodeList.add(timeNode3);
        }
        jsonMap.put("timeNodeList", timeNodeList);
        jsonMap.put("borrowId",borrowId);
        render(jsonMap);;
    }


    public static void changePassWord(String borrowId) {
        render();
    }
    static Bid buildBid(long bidId) {
        Bid bid = new Bid();
        bid.id = bidId;
        return bid;
    }
}
