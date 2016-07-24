package controllers.supervisor.activity.vo;

import models.RedPacketModel;
import play.db.jpa.JPABase;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Yuan on 2015/6/16.
 */
public class RedPacketVo {
    private Long id;
    private BigDecimal balance; //红包余额
    private BigDecimal total; //红包总金额
    private BigDecimal send; //已经发送红包金额
    private Date createTime; //红包创建时间
    private BigDecimal minValue; //红包最小值
    private BigDecimal maxValue; //红包最大值
    private Integer totalNum;  //发送总人数
    private Integer sendNum; //已经发送人数
    private String actName; //活动的名字
    private String remark;  //活动备注
    private String logoImgUrl;  //logo图片
    private String content;  //活动内容
    private String shareImgUrl;   //分享图片链接
    private String shareUrl;  //分享链接
    private String wishing;  //祝福语
    private String status; //红包是否已结1.未结束2.结束
    private Integer couple; //领取次数
    private Integer userCouple; //用户已领取次数

    public RedPacketVo() {
    }

    public RedPacketVo(RedPacketModel redPacket) {
        inRedPacket(redPacket);

    }

    public void inRedPacket(RedPacketModel redPacket) {
        this.setId(redPacket.getId());
        this.setBalance(redPacket.getBalance());
        this.setTotal(redPacket.getTotal());
        this.setSend(redPacket.getSend());
        this.setCreateTime(redPacket.getCreateTime());
        this.setMinValue(redPacket.getMinValue());
        this.setMaxValue(redPacket.getMaxValue());
        this.setTotalNum(redPacket.getTotalNum());
        this.setSendNum(redPacket.getSendNum());
        this.setActName(redPacket.getActName());
        this.setRemark(redPacket.getRemark());
        this.setLogoImgUrl(redPacket.getLogoImgUrl());
        this.setContent(redPacket.getContent());
        this.setShareImgUrl(redPacket.getShareImgUrl());
        this.setShareUrl(redPacket.getShareUrl());
        this.setWishing(redPacket.getWishing());
        this.setStatus(redPacket.getStatus());
        this.setCouple(redPacket.getCouple());
    }

    public RedPacketModel convertToRedPacket() {
        RedPacketModel redPacketModel = null;
        if (this.getId() == null) {
            redPacketModel = new RedPacketModel();
        } else {
            redPacketModel = RedPacketModel.findById(this.getId());
        }
        redPacketModel.setBalance(this.getBalance());
        redPacketModel.setTotal(this.getTotal());
        redPacketModel.setSend(this.getSend());
        redPacketModel.setCreateTime(this.getCreateTime());
        redPacketModel.setMinValue(this.getMinValue());
        redPacketModel.setMaxValue(this.getMaxValue());
        redPacketModel.setTotalNum(this.getTotalNum());
        redPacketModel.setSendNum(this.getSendNum());
        redPacketModel.setActName(this.getActName());
        redPacketModel.setRemark(this.getRemark());
        redPacketModel.setLogoImgUrl(this.getLogoImgUrl());
        redPacketModel.setContent(this.getContent());
        redPacketModel.setShareImgUrl(this.getShareImgUrl());
        redPacketModel.setShareUrl(this.getShareUrl());
        redPacketModel.setWishing(this.getWishing());
        redPacketModel.setStatus(this.getStatus());
        redPacketModel.setCouple(this.getCouple());
        return redPacketModel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getSend() {
        return send;
    }

    public void setSend(BigDecimal send) {
        this.send = send;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
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

    public String getActName() {
        return actName;
    }

    public void setActName(String actName) {
        this.actName = actName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getLogoImgUrl() {
        return logoImgUrl;
    }

    public void setLogoImgUrl(String logoImgUrl) {
        this.logoImgUrl = logoImgUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getShareImgUrl() {
        return shareImgUrl;
    }

    public void setShareImgUrl(String shareImgUrl) {
        this.shareImgUrl = shareImgUrl;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getWishing() {
        return wishing;
    }

    public void setWishing(String wishing) {
        this.wishing = wishing;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCouple() {
        return couple;
    }

    public void setCouple(Integer couple) {
        this.couple = couple;
    }

    public Integer getUserCouple() {
        return userCouple;
    }

    public void setUserCouple(Integer userCouple) {
        this.userCouple = userCouple;
    }
}
