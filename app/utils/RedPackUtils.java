package utils;

import business.RedPacketBill;

import java.util.Date;

/**
 * Created by libaozhong on 2015/6/11.
 */
public class RedPackUtils {
    /**
     * 生成随机红包金额
     * @param openid
     * @param billNo
     * @return
     */
    public synchronized RedPacketBill getAmount(String openid,String billNo){
        //该用户获取的随机红包金额
//        int amount = (int) Math.round(Math.random()*(HongBaoUtil.MAX_VALUE-HongBaoUtil.MIN_VALUE)+HongBaoUtil.MIN_VALUE);
//        StringBuffer hql = new StringBuffer("from CHongbao");
//        //商户的红包总余额
//        CHongbao po = service.queryOneByHQL(hql.toString());
//        //如果此次随机金额比商户红包余额还要大,则返回商户红包余额
//        if(amount > po.getBalance()){
//            amount =  po.getBalance();
//        }
//        Hongbao hongbao = new Hongbao();
//        hongbao.setAddTime(new Date());
//        hongbao.setAmount(amount);
//        hongbao.setOpenid(openid);
//        hongbao.setResult(HongBaoUtil.LOCK);
//        hongbao.setBillNo(billNo);
//        //先锁定用户领取的金额,防止领取金额超过预算金额
//        service.save(hongbao);
//        return hongbao;
        return null;
   }
}
