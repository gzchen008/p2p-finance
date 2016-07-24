/*
 * Project Name:sp2ponline
 * File Name:t_m_products.java
 * Package Name:models
 * Date:2015-6-10下午1:26:27
 * Copyright (c) 2015, ZhanYi Company All Rights Reserved.
*/
package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**  
 * ClassName:   t_m_products
 * Description: TODO ADD Description.
 * Date:        2015-6-10 下午1:26:27
 * @author      john woo  
 * @version     1.0
 * @since       JDK 1.6
 */
@Entity
public class t_m_products  extends Model {
	public Date time; // 时间
	public String name; // 名称
	public String main_type_id;// 产品主类型,字典类型
	public String sub_type_id; // 产品子类型,字典类型
	public String project_name; // 项目名称
	public String project_code; // 项目编码
	public double total_amount; // 项目融资总额
	public String loaner_name; // 项目融资方
	public String project_introduction; // 项目简述
	public String project_detail; // 项目详情
	public String project_image_filename; // 项目名称图片
	public String capital_usage; // 项目资金用途
	public String repayment_res; // 还款来源
	public String risk_control; // 主要风控条款
	public String security_guarantee; // 安全保障
	public t_m_products() {

	}
	public t_m_products(long id, String name, String main_type_id, String sub_type_id, String project_name, String project_code, double total_amount, String loaner_name, String project_introduction, String project_detail, String capital_usage, String repayment_res, String risk_control, String security_guarantee) {

		this.id = id;
		this.name = name;
		this.main_type_id = main_type_id;
		this.sub_type_id = sub_type_id;
		this.project_name = project_name;
		this.project_code = project_code;
		this.total_amount = total_amount;
		this.loaner_name = loaner_name;
		this.project_introduction = project_introduction;
		this.project_detail = project_detail;
		this.capital_usage = capital_usage;
		this.repayment_res = repayment_res;
		this.risk_control = risk_control;
		this.security_guarantee = security_guarantee;
	}
}
