package models;

import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by libaozhong on 2015/6/15.
 */
@Entity
@Table(name = "t_red_packet")
public class RedPacketModel extends Model {
    @Column(name = "balance")
    private BigDecimal balance; //红包余额
    @Column(name = "total")
    private BigDecimal total; //红包总金额
    @Column(name = "send")
    private BigDecimal send; //(11) NULL已经发送红包金额
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time")
    private Date createTime; //NOT NULL红包创建时间
    @Column(name = "min_value")
    private BigDecimal minValue; //红包最小值
    @Column(name = "max_value")
    private BigDecimal maxValue; //红包最大值
    @Column(name = "total_num")
    private Integer totalNum;  //发送总人数
    @Column(name = "send_num")
    private Integer sendNum; //已经发送人数
    @Column(name = "act_name")
    private String actName; //活动的名字
    @Column(name = "remark")
    private String remark;  //活动备注
    @Column(name = "logo_img_url")
    private String logoImgUrl;  //logo图片
    @Column(name = "content")
    private String content;  //活动内容
    @Column(name = "share_img_url")
    private String shareImgUrl;   //分享图片链接
    @Column(name = "share_url")
    private String shareUrl;  //分享链接
    @Column(name = "wishing")
    private String wishing;  //祝福语
    @Column(name = "status")
    private String status; //红包是否已结1.未结束2.结束
    @Column(name = "couple")
    private Integer couple; //领取次数

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
}
