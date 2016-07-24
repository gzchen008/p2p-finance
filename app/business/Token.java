package business;

import play.mvc.Http;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by libaozhong on 2015/6/4.
 */
public class Token implements Serializable{
    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private Date expireDate;
    private String value;


}
