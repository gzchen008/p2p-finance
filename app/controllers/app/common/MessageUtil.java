package controllers.app.common;

import controllers.app.common.MsgCode;
import controllers.app.common.Severity;
import controllers.app.common.Message;
import play.Logger;




import java.util.List;

import net.sf.json.JSONObject;

/**
 * Created by yuan on 9/22/14.
 */
public class MessageUtil {

	private MessageVo messageVo;

	private MessageUtil() {
		super();
	}

	private static ThreadLocal<MessageUtil> instance = new ThreadLocal<MessageUtil>() {
		protected MessageUtil initialValue() {
			return (new MessageUtil());
		}
	};
	
	public String toStr() {
		return JSONObject.fromObject(messageVo).toString();
	}

	public static MessageUtil getInstance() {
		return instance.get();
	}

	public void setMessage(Message message) {
		messageVo = new MessageVo(message);
	}

	public void setMessage(Message message, Object value) {
		setMessage(message);
		messageVo.setValue(value);
	}




}
