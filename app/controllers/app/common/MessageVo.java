package controllers.app.common;


import play.i18n.Messages;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuan on 9/22/14.
 */
public class MessageVo<V> {

  private Message message;
  private V value;

  public MessageVo() {
  }

  public MessageVo(Message message) {
    this.message = message;
  }

  public Message getMessage() {
    return message;
  }

  public void setMessage(Message message) {
    this.message = message;
  }

  public void setValue(V value) {
    this.value = value;
  }

  public V getValue() {
    return this.value;
  }


}
