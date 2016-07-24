package controllers.supervisor.activity.service;

import business.PageVo;
import business.RedPacketBill;
import controllers.supervisor.activity.vo.RedPacketBillVo;
import models.RedPacketBillModel;
import play.db.jpa.JPA;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Yuan on 2015/6/16.
 */
public class RedPacketBillService {

    public RedPacketBillModel save(RedPacketBill redPacketBill) {
        try {
            RedPacketBillModel redPacketBillModel = new RedPacketBillModel();
            redPacketBillModel.setAddTime(redPacketBill.getAddTime());
            redPacketBillModel.setAmount(new BigDecimal(redPacketBill.getAmount()).divide(new BigDecimal(100)));
            redPacketBillModel.setBillNo(redPacketBill.getBillNo());
            redPacketBillModel.setOpenId(redPacketBill.getOpenid());
            redPacketBillModel.setRedPacketId(Long.valueOf(redPacketBill.getRedPackId()));
            redPacketBillModel.setRemark(redPacketBill.getRemark());
            //        redPacketBillModel.setReturnMessage(redPacketBill.getReturnMsg());
            redPacketBillModel.setReturnCode(redPacketBill.getResult());
            redPacketBillModel.save();
            return redPacketBillModel;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<RedPacketBillVo> findRedPacketBillVosBy(PageVo pageVo) {
        StringBuffer jpql = new StringBuffer();
        jpql.append(" select new ");
        jpql.append(RedPacketBillVo.class.getName() + "(r)");
        jpql.append(" from RedPacketBillModel r");
        jpql.append(" where 1=1");
        List<RedPacketBillVo> resultList = JPA.em().createQuery(jpql.toString()).getResultList();
        return resultList;
    }

}
