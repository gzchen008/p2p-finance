package business;

import constants.Constants;
import controllers.supervisor.activity.RedPacketController;
import controllers.supervisor.activity.service.RedPacketService;
import controllers.supervisor.activity.vo.RedPacketVo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by libaozhong on 2015/6/11.
 */
public class RedPacket {
    private static RedPacketService redPacketService = new RedPacketService();
    private Long id;
    private Integer total;     //   红包总金额  单位分
    private Integer balance;   //   红包余额 单位分
    private Integer send;      //   已经发送红包金额 单位分
    private Date time;      //   红包创建时间
    private Integer minValue;      //  红包最小值
    private Integer maxValue;      //   红包最大值
    private Integer totalNum;      //   发送总人数
    private Integer sendNum;      //   已发送人数
    private Integer activityId; //活动的id
    private String actName; //活动的名字
    private String remark; //活动备注
    private String logo_imgurl ; //活动备注
    private String content ; //活动备注
    private String share_url ; //分享链接
    private String share_imgurl  ; //分享链接
    private String wishing  ; //祝福语1
    private Integer over  ; //红包是否结束
    private Integer couple  ; //红包可以重复领取1.可以 2不可以
    public Integer getCouple() {
        return couple;
    }

    public void setCouple(Integer couple) {
        this.couple = couple;
    }

    public Integer getOver() {
        return over;
    }

    public void setOver(Integer over) {
        this.over = over;
    }



    public Integer getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    public String getWishing() {
        return wishing;
    }

    public void setWishing(String wishing) {
        this.wishing = wishing;
    }



    public String getLogo_imgurl() {
        return logo_imgurl;
    }

    public void setLogo_imgurl(String logo_imgurl) {
        this.logo_imgurl = logo_imgurl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getShare_url() {
        return share_url;
    }

    public void setShare_url(String share_url) {
        this.share_url = share_url;
    }

    public String getShare_imgurl() {
        return share_imgurl;
    }

    public void setShare_imgurl(String share_imgurl) {
        this.share_imgurl = share_imgurl;
    }


    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


    public Integer getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(Integer totalNum) {
        this.totalNum = totalNum;
    }

    public Integer getSendNum() {
        return sendNum;
    }

    public void setSendNum(Integer sendNum) {
        this.sendNum = sendNum;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }


    public String getActName() {
        return actName;
    }

    public void setActName(String actName) {
        this.actName = actName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public Integer getSend() {
        return send;
    }

    public void setSend(Integer send) {
        this.send = send;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public RedPacket queryRedPacket(Long redPacketId,String opend) {
        RedPacketVo redPacketVo = redPacketService.findRedPacketVoBy(redPacketId,opend);
        if(redPacketVo!=null){
            this.activityId=redPacketVo.getId().intValue() ;
            this.actName=redPacketVo.getActName();
            this.balance=redPacketVo.getBalance().multiply(new BigDecimal(100)).intValue();
//            this.content="registertogit";
//            this.logo_imgurl= "https://api-2.sunlights.me/resources/img/banner/banner_register.png";
            this.maxValue=redPacketVo.getMaxValue().multiply(new BigDecimal(100)).intValue();
            this.minValue=redPacketVo.getMinValue().multiply(new BigDecimal(100)).intValue();
            this.remark=redPacketVo.getRemark();
            this.send=redPacketVo.getSend().multiply(new BigDecimal(100)).intValue();
            this.sendNum=redPacketVo.getSendNum();
//            this.share_url="https://api-2.sunlights.me/activity/register.html";
//            this.share_imgurl= "https://api-2.sunlights.me/resources/img/banner/banner_register.png";
            this.time=new Date();
            this.total=redPacketVo.getTotal().multiply(new BigDecimal(100)).intValue();
            this.totalNum=redPacketVo.getTotalNum();
        }
        return this;
    }
}
