package controllers.supervisor.activity;

import business.PageVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.BaseController;
import controllers.supervisor.SupervisorController;
import controllers.supervisor.activity.service.RedPacketService;
import controllers.supervisor.activity.vo.RedPacketVo;
import play.Logger;
import play.data.validation.Validation;

import java.util.List;
import java.util.Map;

/**
 * Created by Yuan on 2015/6/16.
 */
public class RedPacketController extends SupervisorController {

    private static RedPacketService redPacketService = new RedPacketService();

    public static void index() {
        PageVo pageVo = new PageVo();
        List<RedPacketVo> redPacketVos = redPacketService.findRedPacketVos(pageVo);
        render(redPacketVos);
    }

    public static void detail(Long id) {
        RedPacketVo redPacketVo = redPacketService.findRedPacketDetailById(id);
        render(redPacketVo);
    }

    public static void save() {
        Map<String, String> map = params.allSimple();
        map.remove("body");
        map.remove("authenticityToken");
        Long id = "".equals(map.get("id")) ? 0 : Long.valueOf(map.get("id"));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            RedPacketVo redPacketVo = objectMapper.convertValue(map, RedPacketVo.class);
            validation.required("actName", redPacketVo.getActName());
            validation.required("content", redPacketVo.getContent());
            validation.required("total", redPacketVo.getTotal());
            validation.required("minValue", redPacketVo.getMinValue());
            validation.required("maxValue", redPacketVo.getMaxValue());
            validation.min("total", redPacketVo.getTotal(), 0.00);
            validation.min("minValue", redPacketVo.getMinValue(), 0.00);
            validation.min("maxValue", redPacketVo.getMaxValue(), 0.00);
            validation.max("maxValue", redPacketVo.getMaxValue(), 200.00);
            if (redPacketVo.getMinValue() != null) {
                validation.min("maxValue", redPacketVo.getMaxValue(), redPacketVo.getMinValue().doubleValue());
            }
            if (redPacketVo.getTotal() != null) {
                validation.max("maxValue", redPacketVo.getMaxValue(), redPacketVo.getTotal().doubleValue());
            }
            validation.min("totalNum", redPacketVo.getTotalNum(), 0);
            validation.min("couple", redPacketVo.getCouple(), 0);
            if (validation.hasErrors()) {
                params.flash();
                validation.keep();
                detail(id);
            }
            redPacketService.save(redPacketVo);
            index();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(e.getMessage(), e);
            Validation.addError("failure", "%s:请重新操作");
            validation.keep();
            detail(id);
        }

    }

    public static void findRedPacket() {
        RedPacketVo redPacketVo = redPacketService.findRedPacketVoBy(1L, "111111");
        renderJSON(redPacketVo);
    }


}
