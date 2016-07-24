/*
 * Project Name:sp2ponline
 * File Name:MProduct.java
 * Package Name:business
 * Date:2015-6-10下午6:13:40
 * Copyright (c) 2015, YiYue Company All Rights Reserved.
*/
package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import business.Bid.Purpose;

import models.t_agencies;
import models.t_dict_loan_purposes;
import models.t_dict_mainproducts;
import models.t_dict_subproducts;
import models.t_m_products;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import constants.Constants;
import constants.SupervisorEvent;

/**  
 * ClassName:   MProduct
 * Description: TODO ADD Description.
 * Date:        2015-6-10 下午6:13:40
 * @author      john woo  
 * @version     1.0
 * @since       JDK 1.6
 */
public class MProduct implements Serializable{
	public long id;
	public Date time; // 时间
	public String name; // 名称
	public MainType main_type;// 产品主类型
	public SubType sub_type; // 产品子类型

	public String main_type_id;// 产品主类型id
	public String sub_type_id; // 产品子类型id
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
	
	public MProduct() {

	}
	/**
	 * 填充自己
	 */
	public void setId(long id) {
		 t_m_products tMProduct = null;
		 
		try {
			tMProduct = t_m_products.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("母产品->标填充自己:" + e.getMessage());
			
			return;
		}

		if (null == tMProduct) {
			return;
		}
		
		this.id = tMProduct.id;
		this.name = tMProduct.name;
		this.main_type_id = tMProduct.main_type_id;
		this.sub_type_id = tMProduct.sub_type_id;
		this.project_name = tMProduct.project_name;
		this.project_code = tMProduct.project_code;
		this.total_amount = tMProduct.total_amount;
		this.loaner_name = tMProduct.loaner_name;
		this.project_introduction = tMProduct.project_introduction;
		this.project_detail = tMProduct.project_detail;
		this.capital_usage = tMProduct.capital_usage;
		this.repayment_res = tMProduct.repayment_res;
		this.risk_control = tMProduct.risk_control;
		this.security_guarantee = tMProduct.security_guarantee;
	}
	
	/**
	 * 填充自己
	 */
	public static MProduct getMProductById(long id, ErrorInfo error) {
		 t_m_products tMProduct = null;
		 
		try {
			tMProduct = t_m_products.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("母产品->标填充自己:" + e.getMessage());
			error.msg = "获取母产品有误!";
			error.code = -1;
			return null;
		}

		if (null == tMProduct) {
			return null;
		}
		MProduct mProduct = new MProduct();
		mProduct.id = tMProduct.id;
		mProduct.name = tMProduct.name;
		mProduct.main_type_id = tMProduct.main_type_id;
		mProduct.sub_type_id = tMProduct.sub_type_id;
		mProduct.project_name = tMProduct.project_name;
		mProduct.project_code = tMProduct.project_code;
		mProduct.total_amount = tMProduct.total_amount;
		mProduct.loaner_name = tMProduct.loaner_name;
		mProduct.project_introduction = tMProduct.project_introduction;
		mProduct.project_detail = tMProduct.project_detail;
		mProduct.capital_usage = tMProduct.capital_usage;
		mProduct.repayment_res = tMProduct.repayment_res;
		mProduct.risk_control = tMProduct.risk_control;
		mProduct.security_guarantee = tMProduct.security_guarantee;
		error.msg = "获取母产品成功!";
		error.code = 1;
		return mProduct;
	}

	/**
	 * 查询母产品列表
	 * @param info 错误信息
	 * @return 标对象集合
	 */
	public static List<MProduct> queryMProduct(ErrorInfo error) {
		error.clear();
		
		List<MProduct> mproducts = new ArrayList<MProduct>();
		List<t_m_products> tMproducts = null;

		String hql = "select new t_m_products(id, name, main_type_id, sub_type_id, project_name, project_code, total_amount, loaner_name, project_introduction, project_detail, capital_usage, repayment_res, risk_control, security_guarantee) "
				+ "from t_m_products";
		
		try {
			tMproducts = t_m_products.find(hql).fetch();
		} catch (Exception e) {
			Logger.error("标->获取母产品列表:" + e.getMessage());
			error.msg = "获取母产品列表有误!";
			
			return null;
		}

		if(null == tMproducts) 
			return mproducts;
		
		MProduct mProduct = null;
		
		for (t_m_products tMProduct : tMproducts) {
			mProduct = new MProduct();
			mProduct.id = tMProduct.id;
			mProduct.name = tMProduct.name;
			mProduct.main_type_id = tMProduct.main_type_id;
			mProduct.sub_type_id = tMProduct.sub_type_id;
			mProduct.project_name = tMProduct.project_name;
			mProduct.project_code = tMProduct.project_code;
			mProduct.total_amount = tMProduct.total_amount;
			mProduct.loaner_name = tMProduct.loaner_name;
			mProduct.project_introduction = tMProduct.project_introduction;
			mProduct.project_detail = tMProduct.project_detail;
			mProduct.capital_usage = tMProduct.capital_usage;
			mProduct.repayment_res = tMProduct.repayment_res;
			mProduct.risk_control = tMProduct.risk_control;
			mProduct.security_guarantee = tMProduct.security_guarantee;

			mproducts.add(mProduct);
		}
		
		return mproducts;
	}
	/**--------------------------------------------------产品主类型-----------------------------------------------------------------*/

	public static class MainType implements Serializable{
		public long id; // ID
		public String name; // 用途名称
		public boolean isUse; // 是否启用
		public String code; //编码
		public String description; //描述
		/**
		 * 填充自己
		 */
		public void setId(long id) {
			 t_dict_mainproducts tDMProduct = null;
			 
			try {
				tDMProduct = t_dict_mainproducts.findById(id);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("产品主类型->标填充自己:" + e.getMessage());
				
				return;
			}

			if (null == tDMProduct) {
				return;
			}
			
			this.id = tDMProduct.id;
			this.name = tDMProduct.name;
			this.isUse = tDMProduct.is_use;

		}
		/**
		 * 获取产品主类型名称
		 */
		public String getName() {
			if(null == this.name) {
				String hql = "select name from t_dict_mainproducts where id = ?";
				
				try {
					this.name = t_dict_mainproducts.find(hql, this.id).first();
				} catch (Exception e) {
					Logger.error("标->获取产品主类型名称:" + e.getMessage());
					
					return null;
				}
			}
			
			return this.name;
		}

		/**
		 * 查询产品主类型列表
		 * @param info 错误信息
		 * @return 标对象集合
		 */
		public static List<MainType> queryMainType(ErrorInfo error, boolean isUse) {
			error.clear();
			
			List<MainType> mainTypes = new ArrayList<MainType>();
			List<t_dict_mainproducts> tMainTypes = null;

			String hql = "select new t_dict_mainproducts(id, name, is_use ) "
					+ "from t_dict_mainproducts";

			if(isUse)
				hql += " where is_use = 1";
					
			hql += " order by  id";
			
			try {
				tMainTypes = t_dict_mainproducts.find(hql).fetch();
			} catch (Exception e) {
				Logger.error("标->获取产品主类型列表:" + e.getMessage());
				error.msg = "获取产品主类型列表有误!";
				
				return null;
			}

			if(null == tMainTypes) return mainTypes;
			
			MainType mainType = null;
			
			for (t_dict_mainproducts tMainType : tMainTypes) {
				mainType = new MainType();

				mainType.id = tMainType.id;
				mainType.name = tMainType.name;
				mainType.isUse = tMainType.is_use;

				mainTypes.add(mainType);
			}
			
			return mainTypes;
		}
	}
	
	/**--------------------------------------------------产品子类型-----------------------------------------------------------------*/

	public static class SubType implements Serializable{
		public long id; // ID
		public long mainId; // 主产品ID
		public String name; // 用途名称
		public boolean isUse; // 是否启用
		public String code; //编码
		public String description; //描述
		/**
		 * 填充自己
		 */
		public void setId(long id) {
			 t_dict_subproducts tSMProduct = null;
			 
			try {
				tSMProduct = t_dict_subproducts.findById(id);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("产品子类型->标填充自己:" + e.getMessage());
				
				return;
			}

			if (null == tSMProduct) {
				return;
			}
			
			this.id = tSMProduct.id;
			this.name = tSMProduct.name;
			this.isUse = tSMProduct.is_use;

		}
		/**
		 * 获取产品子类型名称
		 */
		public String getName() {
			if(null == this.name) {
				String hql = "select name from t_dict_subproducts where id = ?";
				
				try {
					this.name = t_dict_subproducts.find(hql, this.id).first();
				} catch (Exception e) {
					Logger.error("标->获取产品主类型名称:" + e.getMessage());
					
					return null;
				}
			}
			
			return this.name;
		}

		/**
		 * 查询产品子类型列表
		 * @param info 错误信息
		 * @return 标对象集合
		 */
		public static List<SubType> querySubType(ErrorInfo error, boolean isUse) {
			error.clear();
			
			List<SubType> subTypes = new ArrayList<SubType>();
			List<t_dict_subproducts> tSubTypes = null;

			String hql = "select new t_dict_subproducts(id, name, is_use, main_id ) "
					+ "from t_dict_subproducts";

			if(isUse)
				hql += " where is_use = 1";
					
			hql += " order by  id";
			
			try {
				tSubTypes = t_dict_subproducts.find(hql).fetch();
			} catch (Exception e) {
				Logger.error("标->获取产品类型列表:" + e.getMessage());
				error.msg = "获取产品主类型列表有误!";
				
				return null;
			}

			if(null == tSubTypes) return subTypes;
			
			SubType subType = null;
			
			for (t_dict_subproducts tSubType : tSubTypes) {
				subType = new SubType();

				subType.id = tSubType.id;
				subType.name = tSubType.name;
				subType.isUse = tSubType.is_use;
				subType.mainId = tSubType.main_id;

				subTypes.add(subType);
			}
			
			return subTypes;
		}
	}
}
