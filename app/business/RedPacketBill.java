package business;

import controllers.supervisor.activity.service.RedPacketBillService;

import java.util.Date;

/**
 * Created by libaozhong on 2015/6/11.
 */
public class RedPacketBill {
    private RedPacketBillService redPacketBillService = new RedPacketBillService();
    private Long id;    //
    private String billNo;     // 红包订单号
    private String openid;     // 领取红包的openID
    private Integer amount;    // 领取红包金额 单位分
    private Date addTime;      // 添加时间
    private Integer result;    // 领取红包结果 0失败 1成功 2锁定
    private String remark;     // 备注  用于保存微信返回的json
    private Long redPackId;    //红包 id
    private String returnMsg;  //返回消息
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

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getRedPackId() {
        return redPackId;
    }

    public void setRedPackId(Long redPackId) {
        this.redPackId = redPackId;
    }


    public void saveRedPacketBill(RedPacketBill redPacketBill) {
        redPacketBillService.save(redPacketBill);
    }

    public String getReturnMsg() {
        return returnMsg;
    }

    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
    }
}
