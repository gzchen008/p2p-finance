package controllers.mobile;

import business.User;
import controllers.app.common.Message;
import controllers.app.common.MessageVo;
import controllers.app.common.MsgCode;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import play.cache.Cache;
import utils.ErrorInfo;
import utils.RegexUtils;
import utils.SMSUtil;
import controllers.BaseController;

/**
 * <p>Project: com.shovesoft.sp2p</p>
 * <p>Title: CheckAction.java</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2014 Sunlights.cc</p>
 * <p>All Rights Reserved.</p>
 *
 * @author <a href="mailto:jiaming.wang@sunlights.cc">wangJiaMing</a>
 */
public class CheckAction extends BaseController {

    /**
     * 发送短信验证码
     */
    public static void sendVerifyCode() {
        JSONObject json = new JSONObject();
        String mobile = params.get("mobile");
        String type = params.get("type");//0 注册 1修改密码
        ErrorInfo error = new ErrorInfo();

        if(StringUtils.isBlank(mobile) ) {
            error.code = -1;
            error.msg = "手机号码不能为空";
            json.put("error",error);
            renderJSON(json);
        }
        if(StringUtils.isBlank(type) ) {
            error.code = -1;
            error.msg = "类型不能为空";
            json.put("error",error);
            renderJSON(json);
        }

        if(!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请输入正确的手机号码";
            json.put("error",error);
            renderJSON(json);
        }

        if ("0".equals(type)) {//注册
            User.isNameExist(mobile, error);
            if (error.code < 0) {
                json.put("error",error);
                renderJSON(json);
            }
        }
        else if ("1".equals(type)) {
            User.isNameExist(mobile, error);
            if (error.code == 0) {
                error.code = -2;
                error.msg = "该用户名不存在";
                json.put("error",error);
                renderJSON(json);
            }
        }

        SMSUtil.sendCode(mobile, error);

        json.put("error",error);
        renderJSON(json);
    }



    public static void searchVerifyCode(String mobile){
        MessageVo messageVo = new MessageVo();
        messageVo.setMessage(new Message(MsgCode.OPERATE_SUCC));
        messageVo.setValue(Cache.get(mobile));

        renderJSON(JSONObject.fromObject(messageVo));
    }

}
