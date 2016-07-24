package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang.StringUtils;
import business.Optimization.AuditItemOZ;
import com.shove.Convert;
import constants.Constants;
import constants.SQLTempletes;
import constants.DealType;
import constants.IPSConstants;
import constants.SupervisorEvent;
import constants.UserEvent;
import constants.Constants.PayType;
import constants.Constants.RechargeType;
import play.Logger;
import play.cache.Cache;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.PageBean;
import utils.QueryUtil;
import models.t_dict_audit_items;
import models.t_user_audit_items;
import models.t_user_over_borrows;
import models.v_user_audit_items;
import models.v_user_over_borrows;

/**
 * 超额借款
 * @author cp
 * @version 6.0
 * @created 2014年3月25日 上午11:28:56
 */
public class OverBorrow implements Serializable{

	public long id;
	public long _id = -1;
	
	public long userId;
	public Date time;
	public double amount;
	public String reason;
	public int status;
    public double passAmount;
	public long auditSupervisorId;
	public Date auditTime;
	public String auditOpinion;
	
	public boolean isPay;//是否已支付资料审核费用
	
	public void setId(long id){
        t_user_over_borrows overBorrow = null;

		try {
			overBorrow = t_user_over_borrows.findById(id);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			this._id = -1;

			return;
		}

		if (null == overBorrow) {
			this._id = -1;

			return;
		}
        
        this._id = overBorrow.id;
        this.userId = overBorrow.user_id;
        this.time = overBorrow.time;
        this.amount = overBorrow.amount;
        this.reason = overBorrow.reason;
        this.status = overBorrow.status;
        this.passAmount = overBorrow.pass_amount;
        this.auditSupervisorId = overBorrow.audit_supervisor_id;
        this.auditTime = overBorrow.audit_time;
        this.auditOpinion = overBorrow.audit_opinion;
	}
	
	public long getId() {
 		return _id;
 	}
	
	/**
	 * 是否有未通过审核的超额借款
	 * @return
	 */
	public static boolean haveAuditingOverBorrow(long userId, ErrorInfo error) {
		int count = 0;
		
		try {
			count = (int) t_user_over_borrows.count("user_id = ? and status = 0", userId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return true;
		}
		
		error.code = 0;
		
		if (count > 0) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 申请超额借款
	 * @param user
	 * @param amount
	 * @param reason
	 * @param auditItems
	 * @param error
	 * @return
	 */
	public int applyFor(User user, int amount, String reason, List<Map<String,String>> auditItems, ErrorInfo error) {
		error.clear();
		long userId = user.id;
		
		if (amount <= 0) {
			error.code = -1;
			error.msg = "金额必须是正数";
			
			return error.code;
		}
		
		if (amount > 10000000) {
			error.code = -1;
			error.msg = "超额借款最多只能申请100000000元";
			
			return error.code;
		}
		
		if (StringUtils.isBlank(reason)) {
			error.code = -1;
			error.msg = "原因不能为空";
			
			return error.code;
		}
		
		if (null == auditItems || 0 == auditItems.size()) {
			error.code = -1;
			error.msg = "审核资料不能为空";
			
			return error.code;
		}
		
		if (haveAuditingOverBorrow(userId, error) && 0 == error.code) {
			error.code = -1;
			error.msg = "您还有未审核的超额借款申请，不能再次申请";
			
			return error.code;
		}
		
		/**
		 * 超额借款审核资料费
		 */
		Double fees = 0.0;
		String sql = "select sum(audit_fee) from t_dict_audit_items where id in (";
		
		for (Map<String, String> item : auditItems) {
			long itemId = Convert.strToLong(item.get("id"), 0);
			String filename = item.get("filename");
			
			if (StringUtils.isBlank(filename)) {
				error.code = -1;
				error.msg = Constants.IPS_ENABLE ? "您还有未上传的资料，申请超额借款失败" : "您还有未提交的资料，申请超额借款失败";
				
				return error.code;
			}
			
			sql += itemId + ",";
		}
		
		sql = sql.substring(0, sql.length() - 1);
		sql += ")";
		
		try {
			fees = (Double) t_dict_audit_items.find(sql).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";

			return 0;
		}
		
		double user_amount = 0;
		
		if (Constants.PAY_TYPE_ITEM == PayType.INDEPENDENT) 
			user_amount = user.balance2;
		else
			user_amount = user.balance;
		
		
		switch (Constants.PAY_TYPE_VIP) {
		//平台内部进行转账
		case PayType.INNER:
		//通过独立普通网关
		case PayType.INDEPENDENT:
			if (fees > user_amount && fees > 0) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("rechargeType", RechargeType.UploadItemsOB);
				map.put("fee", fees);
				map.put("amount", amount);
				map.put("reason", reason);
				map.put("auditItems", auditItems);
				Cache.set("rechargePay"+userId, map, IPSConstants.CACHE_TIME);
				
				error.code = Constants.BALANCE_NOT_ENOUGH;
				error.msg = "您可用余额不足,请支付资料审核费";
				
				return error.code;
			}
			break;
		//通过共享资金托管账户网关
		case PayType.SHARED:
		//资金托管网关
		case PayType.IPS:
			if (this.isPay) {
				break;
			}
			
			if (fees > user.balance) {
				error.code = Constants.BALANCE_NOT_ENOUGH;
				error.msg = "对不起，您可用余额不足";
				
				return error.code;
			}
			
			if (fees > 0) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("rechargeType", RechargeType.UploadItemsOB);
				map.put("fee", fees);
				map.put("amount", amount);
				map.put("reason", reason);
				map.put("auditItems", auditItems);
				Cache.set("rechargePay"+userId, map, IPSConstants.CACHE_TIME);
				
				error.code = Constants.BALANCE_PAY_ENOUGH;
				error.msg = "请前去支付资料审核费用";
				
				return error.code;
			}
		}
		
		/**
		 * 扣资料费
		 */
		DataSafety data = new DataSafety();
		data.id = userId;
		
		if(!data.signCheck(error)){
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		if (Constants.PAY_TYPE_ITEM == PayType.INDEPENDENT) 
			error.code = DealDetail.minusUserFund2(userId, fees);
		else
			error.code = DealDetail.minusUserFund(userId, fees);

		if(error.code < 1){
			JPA.setRollbackOnly();
			error.msg = "提交超额借款失败";
			
			return error.code;
		}
		
		Map<String, Double> detail = DealDetail.queryUserFund(userId, error);
		
		if (Constants.PAY_TYPE_ITEM == PayType.INDEPENDENT) 
			user_amount = detail.get("user_amount2");
		else
			user_amount = detail.get("user_amount");
		
		DealDetail dealDetail = new DealDetail(userId,
				DealType.CHARGE_AUDIT_ITEM, fees,
				userId, user_amount, detail.get("freeze"),
				detail.get("receive_amount"), "审核用户资料扣除审核费用");

		if (Constants.PAY_TYPE_ITEM == PayType.INDEPENDENT) 
			dealDetail.addDealDetail2(error);
		else
			dealDetail.addDealDetail(error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			error.msg = "提交超额借款失败";

			return error.code;
		}
		
		data.id = userId;
		data.updateSign(error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			error.msg = "提交超额借款失败";
			
			return error.code;
		}
		
		/* 添加风险金 */
		dealDetail.addPlatformDetail(DealType.AUDIT_FEE, userId, userId, -1,
				DealType.ACCOUNT, fees, 1,
				"扣除审核资料费", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			error.msg = "提交超额借款失败";
			
			return error.code;
		}
		
		if (Constants.PAY_TYPE_ITEM == PayType.INDEPENDENT) 
			user.balance2 -= fees;
		else
			user.balance -= fees;
			
		User.setCurrUser(user);
		
		/**
		 * 添加超额借款记录
		 */
		t_user_over_borrows overBorrow = new t_user_over_borrows(); 
		overBorrow.user_id = userId;
		overBorrow.time = new Date();
		overBorrow.amount = amount;
		overBorrow.reason = reason;
		overBorrow.credit_line = User.queryCreditLineById(userId, error);
		overBorrow.status = 0;
		
		try {
			overBorrow.save();
		}catch (Exception e) {
			Logger.info("申请超额借款时："+e.getMessage());
			error.code = -1;
			error.msg = "申请超额借款失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		long overBorrowId = overBorrow.id;
		
		/**
		 * 添加超额借款审核资料
		 */
		for (Map<String, String> item : auditItems) {
			long itemId = Convert.strToLong(item.get("id"), 0);
			String filename = item.get("filename");
			int status = Constants.AUDITING;
			
			if (StringUtils.isBlank(filename)) {
				error.code = -1;
				error.msg = "您还有未上传的资料，申请超额借款失败";
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			/* 2014-11-10 修复资料状态不一致的BUG*/
			UserAuditItem.editOverBorrowId(userId, itemId, overBorrowId, status, error);
			
			if(error.code < 0){
				error.code = -1;
				error.msg = "申请超额借款失败!";
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			String[] names = filename.split(",");
			
			for (int i = 0; i < names.length; i++) {
				t_user_audit_items tItem = new t_user_audit_items();
				tItem.user_id = userId;
				tItem.time = new Date();
				tItem.audit_item_id = itemId;
				tItem.image_file_name = names[i];
				tItem.status = status;
				tItem.is_over_borrow = true;
				tItem.over_borrow_id = overBorrowId;
				tItem.is_visible = true;
				tItem.mark = AuditItem.queryMark(itemId);
				
				try {
					tItem.save();
				} catch (Exception e) {
					Logger.error("添加超额借款审核资料时:" + e.getMessage());
					error.code = -1;
					error.msg = "申请超额借款失败!";
					JPA.setRollbackOnly();
					
					return error.code; 
				}
			}
		}
		
		DealDetail.userEvent(userId, UserEvent.APPLY_FOR_OVER_BORROW, "申请超额借款", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		AuditItemOZ.createItemStatistic(this.userId); // 更新通过资料数
		
		error.code = 0;
		error.msg = "您的超额借款申请已提交，请耐心等待审核结果。";
		
		return error.code;
	}
	
	/**
	 * 审核超额借款
	 * @param supervisorId
	 * @param overBorrowId
	 * @param status
	 * @param passAmount
	 * @param auditOpinion
	 * @param error
	 * @return
	 */
	public static int audit(long supervisorId, long overBorrowId, int status, int passAmount, String auditOpinion, ErrorInfo error) {
		error.clear();
		
		/**
		 * 判断审核资料是否全部提交完毕
		 */
		List<v_user_audit_items> items = queryAuditItems(overBorrowId, error);
		
		if (error.code < 0) {
			return error.code;
		}
		
		if(status == 1){
			for (v_user_audit_items item : items) {
				if (item.status != 2) {
					error.code = -1;
					error.msg = "该超额借款还有未审核通过的资料，审核失败";
					
					return error.code;
				}
			}
			
			if (passAmount < 0) {
				error.code = -1;
				error.msg = "通过的额度必须是正数";
			}
		}
		
		if (StringUtils.isBlank(auditOpinion)) {
			error.code = -1;
			error.msg = "审核意见不能为空";
			
			return error.code;
		}
		
		if (status != 1 && status != 2) {
			error.code = -1;
			error.msg = "请选择审核状态";
			
			return error.code;
		}
		
		t_user_over_borrows overBorrow = null;

		try {
			overBorrow = t_user_over_borrows.findById(overBorrowId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == overBorrow) {
			error.code = -2;
			error.msg = "审核的超额借款不存在";

			return error.code;
		}
		
		if (overBorrow.status != 0) {
			error.code = -3;
			error.msg = "超额借款已审核";

			return error.code;
		}
		
		overBorrow.status = status;
		overBorrow.audit_supervisor_id = supervisorId;
		overBorrow.audit_time = new Date();
		overBorrow.pass_amount = passAmount;
		overBorrow.audit_opinion = auditOpinion;
		
		try {
			overBorrow.save();
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("update t_user_over_borrows："+e.getMessage());
			error.code = -1;
			error.msg = "审核超额借款失败, 请稍后重试";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		/**
		 * 不通过
		 */
		if (status == 2) {
			DealDetail.supervisorEvent(supervisorId, SupervisorEvent.AUDIT_OVER_BORROW, "审核超额借款，不通过", error);
			
			if (error.code < 0) {
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			error.msg = "审核超额借款不通过";
			
			return error.code;
		}
		
		/**
		 * 通过
		 */
		long userId = overBorrow.user_id;
		String sql = "update t_users set credit_line = credit_line + ? where id = ?";
		int rows = 0;
		
		try {
			rows = JpaHelper.execute(sql, (double)passAmount, userId).executeUpdate();
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("update t_users："+e.getMessage());
			error.code = -1;
			error.msg = "审核超额借款失败, 请稍后重试";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.AUDIT_OVER_BORROW, "审核超额借款，通过", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "审核超额借款成功";
		
		return error.code;
	}
	
	/**
	 * 超额借款管理列表
	 * @param currPage
	 * @param pageSize
	 * @param keywordType
	 * @param keyword
	 * @param orderType
	 * @param error
	 * @return
	 */
	public static PageBean<v_user_over_borrows> queryOverBorrows(int currPage, int pageSize, 
			int keywordType, String keyword, int orderType, String startDateStr, String endDateStr, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		if (keywordType < 0 || keywordType > 2) {
			keywordType = 0;
		}
		
		if (orderType < 0 || orderType > 9) {
			orderType = 0;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_OVER_BORROWS);
		
		String orderCondition = SQLTempletes.OVER_BORROWS_ORDER_CONDITION[orderType];
		List<Object> params = new ArrayList<Object>();

		if (StringUtils.isNotBlank(keyword)) {
			if (1 == keywordType) {
				sql.append("  and (u.name like ?) ");
				params.add("%" + keyword + "%");
			} else if (2 == keywordType) {
				sql.append("  and (u.email like ?) ");
				params.add("%" + keyword + "%");
			} else {
				sql.append("  and ((u.name like ?) or (u.email like ?)) ");
				params.add("%" + keyword + "%");
				params.add("%" + keyword + "%");
			}
		}
		
		if (StringUtils.isNotBlank(startDateStr)) {
			sql.append(" AND uob.time >= ?");
			params.add(DateUtil.strDateToStartDate(startDateStr));
		}
		
		if (StringUtils.isNotBlank(endDateStr)) {
			sql.append(" AND uob.time <= ?");
			params.add(DateUtil.strDateToEndDate(endDateStr));
		}
		
		sql.append(orderCondition);
		
		List<v_user_over_borrows> page = null;
		int count = 0;

		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_user_over_borrows.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            page = query.getResultList();
            
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("keywordType", keywordType);
		map.put("orderType", orderType);
		map.put("startDateStr", startDateStr);
		map.put("endDateStr", endDateStr);
		
		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}
		
		PageBean<v_user_over_borrows> bean = new PageBean<v_user_over_borrows>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.page = page;
		bean.totalCount = (int) count;
		bean.conditions = map;
		
		error.code = 0;

		return bean;
	}
	
	
	
	/**
	 * 查询超额借款补提交的资料
	 * @param overBorrowId
	 * @return
	 */
	public static List<v_user_audit_items> queryAuditItems(long overBorrowId, ErrorInfo error) {
		error.clear();
		List<v_user_audit_items> items = null;
		
		try {
			items = v_user_audit_items.find("over_borrow_id = ? group by audit_item_id", overBorrowId).fetch();
			//items = v_user_audit_items.find("user_id = (select distinct(user_id) from t_user_audit_items where over_borrow_id = ?) and audit_item_id in (select audit_item_id from t_user_audit_items where over_borrow_id = ?) and id in (select max(id) from t_user_audit_items group by audit_item_id)", overBorrowId, overBorrowId).fetch();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		error.code = 0;
		error.msg = "查询超额借款补提交的资料成功";
		
		return items;
	}
	
	/**
	 * 查询历史申请记录
	 * @param userId
	 * @param error
	 * @return
	 */
	public static List<v_user_over_borrows> queryHistoryOverBorrows(long overBorrowId, ErrorInfo error) {
		error.clear();
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_OVER_BORROWS);
		sql.append(" and uob.user_id = (select user_id from t_user_over_borrows where id = ?) and uob.status != 0");
		List<v_user_over_borrows> overBorrows = null;
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_user_over_borrows.class);
            query.setParameter(1, overBorrowId);
            overBorrows = query.getResultList();
            
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		error.code = 0;
		error.msg = "查询历史申请记录成功";
		
		return overBorrows;
	}
	
	/**
	 * 查询审核中的超额借款
	 * @param userId
	 * @param error
	 * @return
	 */
	public static v_user_over_borrows queryAuditingOverBorrow(long overBorrowId, ErrorInfo error) {
		error.clear();
		StringBuffer sql = new StringBuffer();
		List<v_user_over_borrows> v_user_over_borrows_list = null;
		
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_OVER_BORROWS);
		sql.append(" and uob.id = ?");
		
		v_user_over_borrows overBorrow = null;
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_user_over_borrows.class);
            query.setParameter(1, overBorrowId);
            query.setMaxResults(1);
            v_user_over_borrows_list = query.getResultList();
            
            if(v_user_over_borrows_list.size() > 0){
            	overBorrow = v_user_over_borrows_list.get(0);
            }
            
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		error.code = 0;
		
		return overBorrow;
	}
	
	/**
	 * 查询上一个超额借款id
	 * @param overBorrowId
	 * @return
	 */
	public static long queryPreOverBorrowId(long overBorrowId) {
		Long id = null;
		
		try {
			id = t_user_over_borrows.find("select MAX(id) from t_user_over_borrows where id < ?", overBorrowId).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();

			return -1;
		}
		
		return (null == id) ? -1 : id.longValue();
	}
	
	/**
	 * 查询下一个超额借款id
	 * @param overBorrowId
	 * @return
	 */
	public static long queryNextOverBorrowId(long overBorrowId) {
		Long id = null;
		
		try {
			id = t_user_over_borrows.find("select MIN(id) from t_user_over_borrows where id > ?", overBorrowId).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();

			return -1;
		}
		
		return (null == id) ? -1 : id.longValue();
	}
	
	/**
	 * 查询之前的超额借款数
	 * @param overBorrowId
	 * @return
	 */
	public static long queryPreOverBorrowCount(long overBorrowId) {
		long count = 0;
		
		try {
			count = t_user_over_borrows.count("id < ?", overBorrowId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();

			return 0;
		}
		
		return count;
	}
	
	/**
	 * 查询之后的超额借款数
	 * @param overBorrowId
	 * @return
	 */
	public static long queryLaterOverBorrowCount(long overBorrowId) {
		long count = 0;
		
		try {
			count = t_user_over_borrows.count("id > ?", overBorrowId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();

			return 0;
		}
		
		return count;
	}
	
	/**
	 * 查询超额借款补提交的资料通过数
	 * @param overBorrowId
	 * @return
	 */
	public static int queryPassedAuditItemsCount(long overBorrowId, ErrorInfo error) {
		error.clear();
		int count = 0;
		
		try {
			count = (int) t_user_audit_items.count("over_borrow_id = ? and status = 2", overBorrowId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return 0;
		}
		
		error.code = 0;
		
		return count;
	}
	
	/**
	 * 查询信用积分
	 * @param overBorrowId
	 * @return
	 */
	public static int queryCreditScore(long overBorrowId, ErrorInfo error) {
		error.clear();
		Long sum = null;
		String sql = "select sum(credit_score) from v_user_audit_items where over_borrow_id = ? and status = ?";
		
		try {
			sum = (Long) v_user_audit_items.find(sql, overBorrowId, Constants.AUDITED).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return 0;
		}
		
		error.code = 0;
		
		return (sum == null) ? 0 : sum.intValue();
	}
	
	/**
	 * 查询userId通过overBorrowId
	 * @param overBorrowId
	 * @return
	 */
	public static long queryUserId(long overBorrowId, ErrorInfo error) {
		error.clear();
		Long userId = null;
		String sql = "select `uob`.`user_id` AS `user_id` from `t_user_over_borrows` `uob` left join `t_users` `u` on `uob`.`user_id` = `u`.`id` where uob.id = ?";
		List<Long> longList = null;
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString());
            query.setParameter(1, overBorrowId);
            query.setMaxResults(1);
            longList = query.getResultList();
            
            if(longList.size() > 0){
              userId = Convert.strToLong(longList.get(0)+"", -1);
            }
            
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return 0;
		}
		
		error.code = 0;
		
		return (userId == null) ? 0 : userId.longValue();
	}
	
	/**
	 * 查询超额借款记录
	 * @param userId
	 * @return
	 */
	public static List<t_user_over_borrows> queryUserOverBorrows(long userId, ErrorInfo error){
		error.clear();
		
		List<t_user_over_borrows> overBorrows = new ArrayList<t_user_over_borrows>();
		
		String sql = "select new t_user_over_borrows(id, amount, reason, time, status) from t_user_over_borrows"
				+ " where user_id=?";
		
		try{
			overBorrows = t_user_over_borrows.find(sql, userId).fetch();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询超额借款申请记录时："+e.getMessage());
			
			error.code = -1;
			error.msg = "查询用户超额借款记录出现异常";
			
			return null;
		}
		
		error.code = 0;
		
		return overBorrows;
	} 
	
	public static t_user_over_borrows queryOverBorrowById(long overBorrowId, ErrorInfo error){
		error.clear();
		
		t_user_over_borrows overBorrows = new t_user_over_borrows();
		
		String sql = "select new t_user_over_borrows(id, amount, reason, time, status) from t_user_over_borrows"
				+ " where id=?";
		
		try{
			overBorrows = t_user_over_borrows.find(sql, overBorrowId).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询超额借款申请记录时："+e.getMessage());
			
			error.code = -1;
			error.msg = "查询用户超额借款记录出现异常";
			
			return null;
		}
		
		error.code = 0;
		
		return overBorrows;
	} 
	
}
