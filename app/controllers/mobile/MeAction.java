package controllers.mobile;

import business.Bid;
import business.Invest;
import business.User;
import constants.Constants;
import controllers.BaseController;
import controllers.SubmitRepeat;
import controllers.front.account.LoginAndRegisterAction;
import controllers.interceptor.H5Interceptor;
import models.v_invest_records;
import models.y_subject_url;
import net.sf.json.JSONObject;
import play.db.jpa.JPA;
import play.mvc.With;
import utils.ErrorInfo;
import utils.PageBean;

import javax.persistence.Query;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * <p>Project: com.shovesoft.sp2p</p>
 * <p>Title: ProductAction.java</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2014 Sunlights.cc</p>
 * <p>All Rights Reserved.</p>
 *
 * @author <a href="mailto:jiaming.wang@sunlights.cc">wangJiaMing</a>
 */
@With({H5Interceptor.class, SubmitRepeat.class})
public class MeAction extends BaseController {
    public static void MeLogout() {
        User user = User.currUser();
        if (user == null) {
            LoginAction.login();
        }
        ErrorInfo error = new ErrorInfo();
        user.logout(error);
        if (error.code < 0) {
            LoginAction.login();
        }
        LoginAction.login();
    }

    public static void accountSafe() {
        User user = User.currUser();
        Map map = new HashMap();
        if(null!=user && null!=user.idNumber) {
            String idNumber = user.idNumber.substring(0, 6) + "****";
            String name = user.name.substring(0, 3) + "***" + user.name.substring(7, 11);
            map.put("idNumber",idNumber);
            map.put("name",name);
        }
        render(map);
    }

    public static void changePassWord() {

        String name=params.get("name");
        if (null!=name){
            User user =new User();
            user.setName(name);
            render(user);
        }else {
            User user = User.currUser();
            render(user);
        }

    }
    /**
     * 保存重设的密码
     */
    public static void modifyPassWord() {
        User user = User.currUser();
        JSONObject json = new JSONObject();
        ErrorInfo error = new ErrorInfo();
        String mobile = params.get("name");//the user name is mobile
        String password = params.get("password");
        String code = params.get("verifyCode");
        String confirmPassword = password;
        User.updatePasswordByMobile(mobile, code, password, confirmPassword,error);
        if (error.code < 0) {
            json.put("error",error);
            renderJSON(json);
        }
//        if (error.code < 0) {
//            flash.put("mobile", mobile);
//            flash.put("code", code);
//            flash.put("password", password);
//            flash.put("confirmPassword", confirmPassword);
//            flash.error(error.msg);
//        }
        json.put("error",error);
        renderJSON(json);

    }
    public static void aboutOur() {
        render();
    }
    public static void safety() {
        render();
    }

    static Bid buildBid(long bidId) {
        Bid bid = new Bid();
        bid.id = bidId;
        return bid;
    }
}
