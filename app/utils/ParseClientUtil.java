package utils;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.mvc.Http;
import constants.Constants;

/**
 * <p>Project: com.shovesoft.sp2p</p>
 * <p>Title: ParseClientUtil.java</p>
 * <p>Description: </p>
 * <p>Copyright (c) 2014 Sunlights.cc</p>
 * <p>All Rights Reserved.</p>
 *
 * @author <a href="mailto:jiaming.wang@sunlights.cc">wangJiaMing</a>
 */
public class ParseClientUtil {
    public static final String APP = "0";//IOS   ANDROID
    public static final String PC = "1";
    public static final String H5 = "3";

    public static String parseClient(Http.Request request){
        Http.Header userAgentHeader = request.headers.get("user-agent");
        if (userAgentHeader == null) {
            userAgentHeader = request.headers.get("User-Agent");
        }

        if (userAgentHeader != null && StringUtils.isNotEmpty(userAgentHeader.value())){
            String userAgent = userAgentHeader.value();
            Logger.info("User-Agent:" + userAgent);
            if (userAgent.contains("Mobile") || userAgent.contains("mobile")) {
                String appNames = Constants.APP_AGENT_NAMES;
                if (StringUtils.isNotEmpty(appNames)) {
                    String[] appAgents = appNames.split(";");
                    for (String appAgent : appAgents) {
                        if (userAgent.contains(appAgent)) {
                            return APP;
                        }
                    }
                }

                return H5;
            }
        }

        return PC;
    }

    public static boolean isWeiXin(){
        Object agent = Http.Request.current().headers.get("user-agent");
        String agentUrl=agent.toString().toLowerCase();
        if(agentUrl.indexOf("mobile") > 0){
            if(agentUrl.indexOf("micromessenger") > 0){
                return true;
            }
        }
        return false;
    }


}
