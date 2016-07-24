package controllers.supervisor.activity.vo;

import models.RedPacketBillModel;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Yuan on 2015/6/16.
 */
public class RedPacketBillVo {
    private Long id;
    private String billNo; //红包订单号
    private String openId; //领取红包的openid
    private BigDecimal amount; //领取红包的金额
    private Date addTime; //红包领取时间
    private Integer returnCode; //红包领取结果1.失败2.成功
    private String remark; //备注用于记录微信返回json
    private Long redPacketId; //红包id
    private Integer returnMessage;

    public RedPacketBillVo() {
    }

    public RedPacketBillVo(RedPacketBillModel redPacketBillModel) {
        inRedPacketBill(redPacketBillModel);
    }

    public void inRedPacketBill(RedPacketBillModel redPacketBillModel) {
        this.setId(redPacketBillModel.id);
        this.setBillNo(redPacketBillModel.getBillNo());
        this.setOpenId(redPacketBillModel.getOpenId());
        this.setAmount(redPacketBillModel.getAmount());
        this.setAddTime(redPacketBillModel.getAddTime());
        this.setReturnCode(redPacketBillModel.getReturnCode());
        this.setRemark(redPacketBillModel.getRemark());
        this.setRedPacketId(redPacketBillModel.getRedPacketId());
        this.setReturnMessage(redPacketBillModel.getReturnMessage());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
