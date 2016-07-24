package models;

import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by libaozhong on 2015/6/15.
 */
@Entity
@Table(name = "t_red_packet_bill")
public class RedPacketBillModel extends Model {

    @Column(name = "bill_no")
    private String billNo; //红包订单号
    @Column(name = "open_id")
    private String openId; //领取红包的openid
    @Column(name = "amount")
    private BigDecimal amount; //领取红包的金额
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "add_time")
    private Date addTime; //红包领取时间
    @Column(name = "return_code")
    private Integer returnCode; //红包领取结果1.失败2.成功
    @Column(name = "remark")
    private String remark; //备注用于记录微信返回json
    @Column(name = "red_packet_id")
    private Long redPacketId; //红包id
    @Column(name = "return_message")
    private Integer returnMessage;

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(Integer returnCode) {
        this.returnCode = returnCode;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getRedPacketId() {
        return redPacketId;
    }

    public void setRedPacketId(Long redPacketId) {
        this.redPacketId = redPacketId;
    }

    public Integer getReturnMessage() {
        return returnMessage;
    }

    public void setReturnMessage(Integer returnMessage) {
        this.returnMessage = returnMessage;
    }
}
