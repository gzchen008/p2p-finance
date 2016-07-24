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
    private BigDecimal balance; //������
    private BigDecimal total; //����ܽ��
    private BigDecimal send; //�Ѿ����ͺ�����
    private Date createTime; //�������ʱ��
    private BigDecimal minValue; //�����Сֵ
    private BigDecimal maxValue; //������ֵ
    private Integer totalNum;  //����������
    private Integer sendNum; //�Ѿ���������
    private String actName; //�������
    private String remark;  //���ע
    private String logoImgUrl;  //logoͼƬ
    private String content;  //�����
    private String shareImgUrl;   //����ͼƬ����
    private String shareUrl;  //��������
    private String wishing;  //ף����
    private String status; //����Ƿ��ѽ�1.δ����2.����
    private Integer couple; //��ȡ����
    private Integer userCouple; //�û�����ȡ����

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
