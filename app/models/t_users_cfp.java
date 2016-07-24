package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import java.util.Date;


@Entity
public class t_users_cfp extends Model{

    public long t_users_id;                     // 用户ID
    public long t_cfp_id;                     // 理财师ID
    public Date createtime;              //日期
    public boolean flag;   //有效标志
}
