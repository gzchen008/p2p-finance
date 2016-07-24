package controllers.supervisor.activity;

import business.PageVo;
import business.RedPacketBill;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.BaseController;
import controllers.supervisor.SupervisorController;
import controllers.supervisor.activity.service.RedPacketBillService;
import controllers.supervisor.activity.service.RedPacketService;
import controllers.supervisor.activity.vo.RedPacketBillVo;
import controllers.supervisor.activity.vo.RedPacketVo;
import models.RedPacketBillModel;
import play.Logger;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Yuan on 2015/6/16.
 */
public class RedPacketBillController extends SupervisorController {

    private static RedPacketBillService redPacketBillService = new RedPacketBillService();

    public static void index() {
        PageVo pageVo = new PageVo();
        List<RedPacketBillVo> billVos = redPacketBillService.findRedPacketBillVosBy(pageVo);
        render(billVos);
    }


    public static void saveRedPacketBill() {
        RedPacketBill redPacketBill = new RedPacketBill();
        redPacketBill.setAddTime(new Date());
        redPacketBill.setAmount(100);
        redPacketBill.setBillNo("21212121");
        redPacketBill.setOpenid("111111");
        redPacketBill.setRedPackId(1L);
        redPacketBill.setRemark("11111111");
        redPacketBill.setResult(1);
        RedPacketBillModel billModel = redPacketBillService.save(redPacketBill);
        renderJSON(billModel);
    }

}
