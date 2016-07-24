package utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

public class JsonDateValueProcessor implements JsonValueProcessor {

    
    private String datePattern = "yyyy-MM-dd HH:mm:ss";

    
    public JsonDateValueProcessor() {
        super();
    }

    
    public JsonDateValueProcessor(String format) {
        super();
        this.datePattern = format;
    }

    
    public Object processArrayValue(Object value, JsonConfig jsonConfig) {
        return process(value);
    }

    
    public Object processObjectValue(String key, Object value,
            JsonConfig jsonConfig) {
        return process(value);
    }

    
    private Object process(Object value) {
        try {
            if (value instanceof Date) {
                SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
                return sdf.format((Date) value);
            }
            
            return value == null ? "" : value.toString();
        } catch (Exception e) {
            return "";
        }

    }

    
    public String getDatePattern() {
        return datePattern;
    }

    
    public void setDatePattern(String pDatePattern) {
        datePattern = pDatePattern;
    }

}
