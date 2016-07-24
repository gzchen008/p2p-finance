package controllers.app;

import com.shove.Convert;
import com.shove.gateway.GeneralRestGatewayInterface;
import com.shove.security.Encrypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Scope.Params;

public class GeneralRestGateway
{
  public static int handle(String key, int allowTimespanSeconds, GeneralRestGatewayInterface grgi, StringBuilder errorDescription)
    throws IOException
  {
    errorDescription.delete(0, errorDescription.length());

    if (StringUtils.isBlank(key))
    {
      throw new RuntimeException("在使用 com.shove.gateways.GeneralRestGateway.handle 方法解析参数并处理业务时，必须提供一个用于摘要签名用的 key (俗称 MD5 加盐)。");
    }

    Http.Request request = Http.Request.current();
    Http.Response response = Http.Response.current();

    if ((request == null) || (response == null)) {
      errorDescription.append("Http 上下文错误。");

      return -2;
    }

    request.encoding = "utf-8";
    response.encoding = "utf-8";

    Set<String> keys = request.params.data.keySet();

    if (keys.isEmpty()) {
      errorDescription.append("没有找到 Query 参数，接口数据非法。");

      return -3;
    }

    Map parameters = new HashMap();
    String _s = ""; String _t = "";

    for (String t_key : keys) {
      if (t_key.equals("_s")) {
        _s = request.params.get("_s");
      }
      else
      {
        if (t_key.equals("_t")) {
          _t = URLDecoder.decode(request.params.get("_t"), "utf-8");
        }

        parameters.put(t_key, URLDecoder.decode(request.params.get(t_key), "utf-8"));
      }
    }
//    if ((StringUtils.isBlank(_s)) || (StringUtils.isBlank(_t))) {
//      errorDescription.append("缺少 _s 或 _t 参数，接口数据非法。");
//
//      return -4;
//    }

    List parameterNames = new ArrayList(parameters.keySet());
    Collections.sort(parameterNames);

    StringBuffer signData = new StringBuffer();

    for (int i = 0; i < parameters.size(); i++) {
      signData.append((String)parameterNames.get(i) + "=" + (String)parameters.get(parameterNames.get(i)) + (i < parameters.size() - 1 ? "&" : ""));
    }

    Date timestamp = Convert.strToDate(_t, getLongGoneDate());
    long span = Math.abs((timestamp.getTime() - System.currentTimeMillis()) / 1000L);

//    if ((allowTimespanSeconds > 0) && (span > allowTimespanSeconds)) {
//      errorDescription.append("访问超时。");
//
//      return -5;
//    }

//    if (!_s.equalsIgnoreCase(Encrypt.MD5(signData + key, "utf-8"))) {
//      errorDescription.append("签名错误，接口数据非法。");
//
//      return -6;
//    }

    parameters.remove("_t");

    String result = "";
    try
    {
      result = grgi.delegateHandleRequest(parameters, errorDescription);
    } catch (Exception e) {
      e.printStackTrace();
      errorDescription.append("应用程序的代理回调程序遇到异常，详细原因是：" + e.getMessage());

      return -7;
    }

    if (!StringUtils.isBlank(result)) {
      response.encoding = "UTF-8";

      response.print(result);
      response.out.flush();
      response.out.close();
    }

    return 0;
  }

  public static String buildUrl(String urlBase, String key, Map<String, String> parameters)
    throws UnsupportedEncodingException
  {
    if ((parameters.containsKey("_s")) || (parameters.containsKey("_t")))
    {
      throw new RuntimeException("在使用 com.shove.gateways.GeneralRestGateway.buildUrl 方法构建通用 REST 接口 Url 时，不能使用 _s, _t 此保留字作为参数名。");
    }

    if (StringUtils.isBlank(key))
    {
      throw new RuntimeException("在使用 com.shove.gateways.GeneralRestGateway.buildUrl 方法构建通用 REST 接口 Url 时，必须提供一个用于摘要签名用的 key (俗称 MD5 加盐)。");
    }

    parameters.put("_t", Convert.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss", "1970-01-01 00:00:00"));

    List parameterNames = new ArrayList(parameters.keySet());
    Collections.sort(parameterNames);

    if ((!urlBase.endsWith("?")) && (!urlBase.endsWith("&"))) {
      urlBase = urlBase + (urlBase.indexOf("?") == -1 ? "?" : "&");
    }

    String signData = "";

    for (int i = 0; i < parameters.size(); i++) {
      String _key = (String)parameterNames.get(i);
      String _value = (String)parameters.get(_key);

      signData = signData + _key + "=" + _value;
      urlBase = urlBase + _key + "=" + URLEncoder.encode(_value, "utf-8");

      if (i < parameters.size() - 1) {
        signData = signData + "&";
        urlBase = urlBase + "&";
      }
    }

    urlBase = urlBase + "&_s=" + Encrypt.MD5(new StringBuilder(String.valueOf(signData)).append(key).toString(), "utf-8");

    return urlBase;
  }

  private static Date getLongGoneDate() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(1, -30);

    return calendar.getTime();
  }
}