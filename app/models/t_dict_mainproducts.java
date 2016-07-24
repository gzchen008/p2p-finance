/*
 * Project Name:sp2ponline
 * File Name:t_dict_mainproducts.java
 * Package Name:models
 * Date:2015-6-10下午2:11:36
 * Copyright (c) 2015, ZhanYi Company All Rights Reserved.
*/
package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**  
 * ClassName:   t_dict_mainproducts
 * Description: TODO ADD Description.
 * Date:        2015-6-10 下午2:11:36
 * @author      john woo  
 * @version     1.0
 * @since       JDK 1.6
 */
@Entity
public class t_dict_mainproducts extends Model {
	public String name;
	public String code;
	public String description;
	public boolean is_use;
	public t_dict_mainproducts(){
		
	}
	
	/**
	 * 产品主类型列表
	 * @param id ID
	 * @param name 名称
	 * @param is_use 是否启用
	 */
	public t_dict_mainproducts(long id, String name, boolean is_use) {
		this.id = id;
		this.name = name;
		this.is_use = is_use;
	}
}
