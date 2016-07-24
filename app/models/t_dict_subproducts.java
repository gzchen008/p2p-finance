/*
 * Project Name:sp2ponline
 * File Name:t_dict_subproducts.java
 * Package Name:models
 * Date:2015-6-10下午2:13:31
 * Copyright (c) 2015, ZhanYi Company All Rights Reserved.
*/
package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**  
 * ClassName:   t_dict_subproducts
 * Description: TODO ADD Description.
 * Date:        2015-6-10 下午2:13:31
 * @author      john woo  
 * @version     1.0
 * @since       JDK 1.6
 */
@Entity
public class t_dict_subproducts  extends Model {
	public long main_id;
	public String name;
	public String code;
	public String description;
	public boolean is_use;
	public t_dict_subproducts(){
		
	}
	
	/**
	 * 产品子类型列表
	 * @param id ID
	 * @param name 名称
	 * @param is_use 是否启用
	 */
	public t_dict_subproducts(long id, String name, boolean is_use, long main_id) {
		this.id = id;
		this.name = name;
		this.is_use = is_use;
		this.main_id = main_id;
	}
}
