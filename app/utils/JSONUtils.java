package utils;

import controllers.app.common.Message;
import controllers.app.common.MessageVo;
import controllers.app.common.MsgCode;
import controllers.app.common.Severity;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class JSONUtils {

	/** 
	* @Description: 将对象以json格式输出
	* @Author Yang Cheng
	* @Date: Feb 9, 2012 1:53:58 AM  
	* @param obj
	* @throws Exception  
	* @return void    
	 * @throws IOException 
	*/ 
	public static String printObject(Object obj) throws IOException {
		JSONObject jsObject =JSONObject.fromObject(obj);
		return jsObject.toString();
	}
	
	 /***
     * 将List对象序列化为JSON文本
     */
    public static <T> String toJSONString(List<T> list)
    {
        JSONArray jsonArray = JSONArray.fromObject(list);

        return jsonArray.toString();
    }


    public static JSONObject toJSONString(ErrorInfo errorInfo, MsgCode msgCode){
        return toJSONString(errorInfo, msgCode, null);
    }

    public static JSONObject toJSONString(ErrorInfo errorInfo, MsgCode msgCode, Object value){
        MessageVo messageVo = new MessageVo();

        if (errorInfo.code == 0) {
            messageVo.setMessage(new Message(Severity.INFO, msgCode, errorInfo.msg));
        }else{
            messageVo.setMessage(new Message(Severity.ERROR, msgCode, errorInfo.msg));
        }
        messageVo.setValue(value);

        return JSONObject.fromObject(messageVo);
    }
}
