package controllers.app.common;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuan on 9/22/14.
 */
public class Message {
  private Severity severity = Severity.INFO;
  private String code = null;
  private String summary = null;
  private String detail = null;
  private Map<String, String> fields = new HashMap<String, String>();

  public Message() {
  }

  public Message(MsgCode msgCode, Object... params) {
    this.code = msgCode.getCode();
    this.summary = msgCode.getMessage();
    String detailMsg = msgCode.getDetail();
      if (params != null && params.length != 0) {
          if (StringUtils.isEmpty(msgCode.getDetail())) {
              detailMsg = params[0] == null ? "" : (String)params[0];
          }else {
              detailMsg = MessageFormat.format(msgCode.getDetail(), params);
          }
      }
    this.detail = detailMsg;
  }

  public Message(Severity severity, MsgCode msgCode, Object... params) {
    this(msgCode, params);
    this.severity = severity;
  }

  public Message(Severity severity, String errorCode, String errorMessage, String detailMsg) {
    this.severity = severity;
    this.code = errorCode;
    this.summary = errorMessage;
    this.detail = detailMsg;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getDetail() {
    if (this.detail == null) {
      return (this.summary);
    } else {
      return (this.detail);
    }
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }


  public Severity currentSeverity() {
    return severity;
  }

  public int getSeverity() {
    return severity.getLevel();
  }

  public Map<String, String> getFields() {
    return fields;
  }

  public void put(String key, String value) {
    fields.put(key, value);
  }

  public void remove(String key) {
    fields.remove(key);
  }

  public void clear() {
    fields.clear();
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setSeverity(Severity severity) {
    this.severity = severity;
  }

  public void setFields(Map<String, String> fields) {
    this.fields = fields;
  }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[code = ").append(code)
                .append("; summary = ").append(summary)
                .append("; detail = ").append(detail)
                .append("]");

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;

        Message message = (Message) o;

        if (code != null ? !code.equals(message.code) : message.code != null) return false;
        if (detail != null ? !detail.equals(message.detail) : message.detail != null) return false;
        if (fields != null ? !fields.equals(message.fields) : message.fields != null) return false;
        if (severity != message.severity) return false;
        if (summary != null ? !summary.equals(message.summary) : message.summary != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = severity != null ? severity.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (summary != null ? summary.hashCode() : 0);
        result = 31 * result + (detail != null ? detail.hashCode() : 0);
        result = 31 * result + (fields != null ? fields.hashCode() : 0);
        return result;
    }
}
