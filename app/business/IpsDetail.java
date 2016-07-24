package business;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import com.shove.Convert;
import constants.Constants;
import constants.IPSConstants;
import constants.IPSConstants.IPSOperation;
import constants.IPSConstants.Status;
import models.t_bids;
import models.t_invest_transfers;
import models.t_invests;
import models.t_ips_cache;
import models.t_ips_details;
import models.t_ips_sequences;
import models.t_user_recharge_details;
import models.t_user_withdrawals;
import play.Logger;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 资金托管交易
 * @author lzp
 * @version 6.0
 * @created 2014-10-23
 */
public class IpsDetail {
	public long id;
	public long _id;
	public String merBillNo;
	public String userName;
	public Date time;
	public int type;
	public int status;
	public String memo;
	
	public void setId(long id) {
		t_ips_details detail = null;

		try {
			detail = t_ips_details.findById(id);
		} catch (Exception e) {
			this._id = -1;
			Logger.error(e.getMessage());

			return;
		}

		if (null == detail) {
			this._id = -1;

			return;
		}

		setInfomation(detail);
	}

	public long getId() {
		return _id;
	}
	
	/**
	 * 填充数据
	 * @param detail
	 */
	private void setInfomation(t_ips_details detail) {
		if (null == detail) {
			this._id = -1;

			return;
		}
		
		this._id = detail.id;
		this.merBillNo = detail.mer_bill_no;
		this.userName = detail.user_name;
		this.time = detail.time;
		this.type = detail.type;
		this.status = detail.status;
		this.memo = detail.memo;
	}
	
	/**
	 * 查询交易记录
	 * @param currPage
	 * @param pageSize
	 * @param mer_bill_no
	 * @param userName
	 * @param type
	 * @param beginTime
	 * @param endTime
	 * @param status
	 * @param error
	 * @return
	 */
	public static PageBean<t_ips_details> queryDetails(int currPage, int pageSize, String merBillNo, String userName, int type, 
			Date beginTime, Date endTime, int status, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}

		StringBuffer condition = new StringBuffer("1=1");
		Map<String, Object> conditions = new HashMap<String, Object>();
		List<Object> params = new ArrayList<Object>();

		if (StringUtils.isNotBlank(merBillNo)) {
			condition.append(" and mer_bill_no = ?");
			conditions.put("merBillNo", merBillNo);
			params.add(merBillNo);
		}
		
		if (StringUtils.isNotBlank(userName)) {
			condition.append(" and user_name = ?");
			conditions.put("userName", userName);
			params.add(userName);
		}
		
		if (type != 0) {
			condition.append(" and type = ?");
			conditions.put("type", type);
			params.add(type);
		}
		
		if (beginTime != null) {
			condition.append(" and time >= ?");
			conditions.put("beginTime", beginTime);
			params.add(beginTime);
		}
		
		if (endTime != null) {
			condition.append(" and time <= ?");
			conditions.put("endTime", endTime);
			params.add(endTime);
		}

		if (status != 0) {
			condition.append(" and status = ?");
			conditions.put("status", status);
			params.add(status);
		}

		int count = 0;
		List<t_ips_details> page = null;

		try {
			count = (int) t_ips_details.count(condition.toString(), params.toArray());
			page = t_ips_details.find(condition.append(" order by time desc").toString(), params.toArray()).fetch(currPage, pageSize);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		PageBean<t_ips_details> bean = new PageBean<t_ips_details>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.totalCount = count;
		bean.page = page;
		bean.conditions = conditions;
		
		error.code = 0;

		return bean;
	}
	
	/**
	 * 新增交易
	 * @param error
	 */
	public void create(ErrorInfo error) {
		error.clear();
		
		if (StringUtils.isBlank(this.merBillNo)) {
			error.code = -1;
			error.msg = "订单号不能为空";
			
			return;
		}
		
		if (this.time == null) {
			error.code = -1;
			error.msg = "交易时间不能为空";
			
			return;
		}
		
		if (this.type != IPSOperation.REGISTER_SUBJECT && this.type != IPSOperation.REGISTER_CREDITOR && 
			this.type != IPSOperation.REGISTER_CRETANSFER && this.type != IPSOperation.DO_DP_TRADE && 
			this.type != IPSOperation.REPAYMENT_NEW_TRADE && this.type != IPSOperation.DO_DW_TRADE && 
			this.type != IPSOperation.TRANSFER_ONE && this.type != IPSOperation.TRANSFER_FOUR) {
			error.code = -1;
			error.msg = "交易类型有误";
			
			return;
		}
		
		if (status != 1 && status != 2) {
			error.code = -1;
			error.msg = "交易状态有误";
		}
		
		t_ips_details detail = new t_ips_details();
		detail.mer_bill_no = this.merBillNo;
		detail.user_name = this.userName;
		detail.time = this.time;
		detail.type = this.type;
		detail.status = this.status;
		detail.memo = this.memo;
		
		try {
			detail.save();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";

			Logger.error("-----------111IpsDetail242-------------");
			
			return;
		}
		
		error.code = 0;
		error.msg = "交易添加成功";
	}
	
	/**
	 * 更新状态
	 * @param merBillNo
	 * @param status
	 */
	public static void updateStatus(String merBillNo, int status, ErrorInfo error) {
		error.clear();
		
		String sel = "select count(1) from t_ips_details where mer_bill_no = ? and status = 1";
		long rowSel = 0;
		String sql = "update t_ips_details set status = ? where mer_bill_no = ?";
		Query query = JpaHelper.execute(sql).setParameter(1, status).setParameter(2, merBillNo);
		int rows = 0;

		try{
			rowSel = ((BigInteger)JPA.em().createNativeQuery(sel).setParameter(1, merBillNo).getSingleResult()).intValue();
		}catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("更新流水号时："+ e.getMessage());
			error.code = -1;
			error.msg = "更新流水号时失败";
			
			return ;
		}
		
		if(rowSel >= 1) {
			error.code = Constants.ALREADY_RUN; //已执行
			error.msg = "已执行";
			
			return;
		}
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return;
		}
		
		if (rows == 0) {
			error.code = -1;
			error.msg = "数据未更新";
			
			return;
		}
		
		error.code = 0;
	}
	
	/**
	 * 更新t_bids的mer_bill_no，返回
	 * @param merBillNo
	 * @param ipsBillNo
	 * @param error
	 */
	public static void updateBidMer(String merBillNo, String ipsBillNo, ErrorInfo error) {
		error.clear();
		
		String sql = "update t_bids set ips_bill_no = ? where mer_bill_no = ?";
		EntityManager em = JPA.em();
		Query query = em.createQuery(sql).setParameter(1, ipsBillNo).setParameter(2, merBillNo);
		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return;
		}
		
		if (rows == 0) {
			error.code = -1;
			error.msg = "数据未更新";
			
			return;
		}
		
		error.code = 0;
	}
	
	/**
	 * 更新t_invests的mer_bill_no，返回
	 * @param merBillNo
	 * @param ipsBillNo
	 * @param error
	 */
	public static void updateInvestMer(String merBillNo, String ipsBillNo, ErrorInfo error) {
		error.clear();
		
		String sql = "update t_invests set ips_bill_no = ? where mer_bill_no = ?";
		Query query = JpaHelper.execute(sql).setParameter(1, ipsBillNo).setParameter(2, merBillNo);
		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error("-----------111IpsDetail359-------------");
			Logger.info(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return;
		}
		
		if (rows == 0) {
			error.code = -1;
			error.msg = "数据未更新";
			
			return;
		}
		
		error.code = 0;
	}
	
	/**
	 * 更新状态和备注
	 * @param merBillNo
	 * @param status
	 * @param memo
	 * @param error
	 */
	public static void updateStatusAndMemo(String merBillNo, int status, String memo, ErrorInfo error) {
		error.clear();
		
		String sql = "update t_ips_details set status = ?, memo = ? where mer_bill_no = ?";
		EntityManager em = JPA.em();
		Query query = em.createQuery(sql).setParameter(1, status).setParameter(2, memo).setParameter(3, merBillNo);
		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info("更新状态和备注时：%s",e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return;
		}
		
		if (rows == 0) {
			error.code = -1;
			error.msg = "数据未更新";
			
			return;
		}
		
		error.code = 0;
		error.msg = "更新状态和备注成功";
	}
	
	/**
	 * 补单
	 * @param merBillNo
	 * @param type
	 * @param error
	 */
	public void repair(ErrorInfo error) {
		error.clear();
		
		if (StringUtils.isBlank(this.merBillNo)) {
			error.code = -1;
			error.msg = "订单号不能为空";
			
			return;
		}
		
		Integer status = null;
		
		try {
			status = t_ips_details.find("select status from t_ips_details where mer_bill_no = ?", this.merBillNo).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return;
		}
		
		if (status == null) {
			error.code = -1;
			error.msg = "交易不存在";
			
			return;
		}
		
		if (status == Status.SUCCESS) {
			error.code = -1;
			error.msg = "交易已成功";
			
			return;
		}
		
		Date time = null;
		
		try {
			time = t_ips_details.find("select time from t_ips_details where mer_bill_no = ?", this.merBillNo).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return;
		}
		
		JSONObject obj = Payment.queryIpsStatus(this.merBillNo, this.type, time, error);
		
		if (error.code < 0) {
			return;
		}
		
		int ipsStatus = obj.getInt("pTradeStatue");
		String pIpsBillNo = obj.containsKey("pIpsBillNo") ? obj.getString("pIpsBillNo") : "";
		double serviceFee = obj.containsKey("serviceFee") ? Convert.strToDouble(obj.getString("serviceFee").trim(), 0) : 0;
		
		if(obj.containsKey("isPost")){
			//解冻投资金额使用post请求方式
			IPSConstants.IS_WS_UNFREEZE = false;
		}
		
		
		if (ipsStatus == Status.HANDLING) {
			Logger.info("ipsStatus:"+ipsStatus+"---1#成功、2#失败、3#处理中、4#未查询到交易");
			error.code = -1;
			error.msg = "交易正在处理中,请稍后重试";
			
			return;
		}
		
		if (ipsStatus == Status.FAIL) {
			Logger.info("ipsStatus:"+ipsStatus+"---1#成功、2#失败、3#处理中、4#未查询到交易");
			error.code = -1;
			error.msg = "交易在资金托管方就是失败的，不能补单";
			
			return;
		}
		
		if (ipsStatus == Status.NONE) {
			Logger.info("ipsStatus:"+ipsStatus+"---1#成功、2#失败、3#处理中、4#未查询到交易");
			error.code = -1;
			error.msg = "交易不存在，不能补单";
			
			return;
		}
		
		if (ipsStatus != Status.SUCCESS) {
			Logger.info("ipsStatus:"+ipsStatus+"---1#成功、2#失败、3#处理中、4#未查询到交易");
			error.code = -1;
			error.msg = "未知状态";
			
			return;
		}
		
		String memo = "";
		
		switch (this.type) {
		case IPSOperation.REGISTER_SUBJECT:
			this.registerSubject(error, pIpsBillNo);
			memo = "发标补单";
			break;
		case IPSOperation.REGISTER_CREDITOR:
			this.registerCreditor(error, IPSConstants.IS_WS_UNFREEZE, pIpsBillNo);
			memo = "投标补单";
			break;
		case IPSOperation.REGISTER_CRETANSFER:
			this.registerCretansfer(error);
			memo = "债权转让补单";
			break;
		case IPSOperation.DO_DP_TRADE:
			this.doDpTrade(error);
			memo = "充值补单";
			break;
		case IPSOperation.TRANSFER_ONE:
			this.transfer(error);
			memo = "转账(放款)补单";
			break;
		case IPSOperation.TRANSFER_FOUR:
			this.transferForCretransfer(error);
			memo = "转账(债权转让)补单";
			break;
		case IPSOperation.REPAYMENT_NEW_TRADE:
			this.repaymentNewTrade(error);
			memo = "还款补单";
			break;
		case IPSOperation.DO_DW_TRADE:
			this.doDwTrade(serviceFee, error);
			memo = "提现补单";
			break;
		default:
			break;
		}
		
		if (error.code < 0) {
			return;
		}
		
		updateStatusAndMemo(this.merBillNo, Status.SUCCESS, memo, error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return;
		}
		
		error.code = 0;
		error.msg = "补单成功";
	}
	
	/**
	 * 发标补单
	 * @param error
	 */
	private void registerSubject(ErrorInfo error, String pIpsBillNo) {
		error.clear();
		t_bids tbid = null;
		
		try {
			tbid = t_bids.find("mer_bill_no = ?", this.merBillNo).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return;
		}
		
		if (tbid == null) {
			error.code = -1;
			error.msg = "交易不存在";
			
			return;
		}
		
		Bid bid = new Bid();
		bid.createBid = true;
		bid.id = tbid.id;
		bid.afterCreateBid(tbid, tbid.bid_no, error);
		
		if(error.code >= 0 && StringUtils.isNotBlank(pIpsBillNo)){
			//保存乾多多的流水号
			IpsDetail.updateBidMer(this.merBillNo, pIpsBillNo, error);
		}
	}
	
	/**
	 * 投标补单
	 * @param error
	 */
	private void registerCreditor(ErrorInfo error,boolean isWS, String pIpsBillNo) {
		error.clear();
		String memo = null;
		
		try {
			memo = t_ips_details.find("select memo from t_ips_details where mer_bill_no = ?", this.merBillNo).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return;
		}
		
		if (memo == null) {
			error.code = -2;
			error.msg = "交易不存在";
			
			return;
		}
		
		JSONObject jsonObj = JSONObject.fromObject(memo);
		long userId = jsonObj.getLong("userId");
		long bidId = jsonObj.getLong("bidId");
		double pTrdAmt = jsonObj.getDouble("pTrdAmt");
		
		//满标控制
		Map<String, String> bid = Invest.bidMap(bidId, error);
		if(bid != null && !bid.isEmpty()){
			double hasAmt = Convert.strToDouble(bid.get("has_invested_amount").toString(), 0);  
			double amount = Convert.strToDouble(bid.get("amount").toString(),0); 
			//已满标,更新补单状态。
			if(hasAmt+pTrdAmt > amount){
				String _memo = StringUtils.isNotBlank(pIpsBillNo)?pIpsBillNo:"已满标，需解冻";
				IpsDetail.updateStatusAndMemo(this.merBillNo,isWS?Status.UNFREEZING:Status.UNFREEZING, _memo, error);
				
				if(isWS){
					//解冻投资金额(WS)
					Payment.unfreezeInvestAmount(this.merBillNo, pIpsBillNo, isWS, error);
					if(error.code >= 1){
						error.msg = "已满标，执行解冻成功";
						error.code = -1;
						return;
					}
				}
				
				error.msg = "已满标,请解冻投资金额";
				error.code = -2;
				
				return;
			}
		}
		
		Invest.invest(userId, bidId, (int)pTrdAmt, null, false, true, this.merBillNo, error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return;
		}
		
		if(StringUtils.isBlank(pIpsBillNo)){
			//保存乾多多返回的流水号
			pIpsBillNo = this.merBillNo;
		}
		
		IpsDetail.updateInvestMer(this.merBillNo, pIpsBillNo, error);
	}
	
	/**
	 * 债权转让补单
	 * @param error
	 */
	private void registerCretansfer(ErrorInfo error) {
		error.clear();
		t_invest_transfers transfer = null;
		
		try {
			transfer = t_invest_transfers.find("mer_bill_no = ?", this.merBillNo).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return;
		}
		
		if (transfer == null) {
			error.code = -1;
			error.msg = "交易不存在";
			
			return;
		}
		
		updateStatusAndMemo(this.merBillNo, Status.SUCCESS, "债权转让补单", error);
		
		if (error.code < 0) {
			return;
		}
		
		String paymentMerBillNo = "";
		
		if("1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
			
			paymentMerBillNo = Payment.transferForCretransfer(this.merBillNo, transfer.id, error);
			
			if (error.code < 0) {
				return;
			}
			
		}else {
			paymentMerBillNo = this.merBillNo;
		}
		
		if (error.code < 0) {
			return;
		}
		
		Debt.dealDebtTransfer(paymentMerBillNo, transfer.id, null,true,error);
	}
	
	/**
	 * 充值补单
	 * @param error
	 */
	private void doDpTrade(ErrorInfo error) {
		error.clear();
		t_user_recharge_details detail = null;
		
		try {
			detail = t_user_recharge_details.find("pay_number = ?", this.merBillNo).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return;
		}
		
		if (detail == null) {
			error.code = -1;
			error.msg = "交易不存在";
			
			return;
		}
		
		User.recharge(this.merBillNo, detail.amount, error);
	}
	
	/**
	 * 提现补单
	 * @param error
	 */
	private void doDwTrade(double serviceFee, ErrorInfo error) {
		error.clear();
		String memo = null;
		
		try {
			memo = t_ips_details.find("select memo from t_ips_details where mer_bill_no = ?", this.merBillNo).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return;
		}
		
		if (memo == null) {
			error.code = -1;
			error.msg = "交易不存在";
			
			return;
		}
		
		JSONObject jsonMemo = JSONObject.fromObject(memo);
		
		long withdrawId = Convert.strToLong(jsonMemo.getString("withdrawId").trim(), -1);
		serviceFee = serviceFee > 0.0 ? serviceFee : Convert.strToDouble(jsonMemo.getString("serviceFee").trim(),0.0);
		
		t_user_withdrawals detail = null;
		
		try {
			detail = t_user_withdrawals.findById(withdrawId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return;
		}
		
		if (detail == null) {
			error.code = -1;
			error.msg = "交易不存在";
			
			return;
		}
		
		User.withdrawalNotice(detail.user_id, serviceFee, detail.id, "1", true, error);
	}
	
	/**
	 * 还款补单
	 * @param error
	 */
	private void repaymentNewTrade(ErrorInfo error) {
		error.clear();
		String bId = null;
		
		try {
			bId = t_ips_details.find("select memo from t_ips_details where mer_bill_no = ?", this.merBillNo).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return;
		}
		
		if (bId == null) {
			error.code = -1;
			error.msg = "交易不存在";
			
			return;
		}
		
		long billId = Convert.strToLong(bId, -1);
		
		Bill bill = new Bill();
		bill.isRepair = true;
		bill.id = billId;
		bill.repayment(bill.bid.userId, error);
	}
	
	/**
	 * 转账(放款)补单
	 * @param error
	 */
	private void transfer(ErrorInfo error) {
		error.clear();
		String bId = null;
		
		try {
			bId = t_ips_details.find("select memo from t_ips_details where mer_bill_no = ?", this.merBillNo).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return;
		}
		
		if (bId == null) {
			error.code = -1;
			error.msg = "交易不存在";
			
			return;
		}
		
		long bidId = Convert.strToLong(bId, -1);
		
		Bid bid = new Bid();
		bid.auditBid = true;
		bid.isRepair = true;
		bid.id = bidId;
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人
		bid.eaitLoanToRepayment(error);
	}
	
	/**
	 * 转账(债权转让)补单
	 * @param error
	 */
	private void transferForCretransfer(ErrorInfo error) {
		error.clear();
		String dId = null;
		
		try {
			dId = t_ips_details.find("select memo from t_ips_details where mer_bill_no = ?", this.merBillNo).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return;
		}
		
		if (dId == null) {
			error.code = -1;
			error.msg = "交易不存在";
			
			return;
		}
		
		long debtId = Convert.strToLong(dId, -1);
		
		Debt.dealDebtTransfer(this.merBillNo, debtId,null,true, error);
	}
	
	/**
	 * 判断流水号是否已存在
	 * @param pMerBillNo
	 * @param error
	 * @return true 存在 false 不存在
	 */
	public static boolean isMerNoExist(String pMerBillNo, ErrorInfo error) {
		long count = 0;
		
		if(null == pMerBillNo)
			return false;
		
		try{
			count = t_ips_sequences.count("p_mer_bill_no = ? and status = 1", Long.parseLong(pMerBillNo));
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("判断流水号是否已存在时："+ e.getMessage());
			error.code = -1;
			error.msg = "判断流水号是否已存在时失败";
			
			return true;
		}
		
		return count > 0 ? true : false;
	}
	
	/**
	 * 添加流水号
	 * @param pMerBillNo
	 * @param error
	 */
	public static long addMerNo(String pMerBillNo, ErrorInfo error) {
		t_ips_sequences ips = new t_ips_sequences();
		
		ips.time = new Date();
		ips.p_mer_bill_no = Long.parseLong(pMerBillNo);
		ips.status = false;
		
		try{
			ips.save();
		}catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("添加流水号时："+ e.getMessage());
			error.code = -1;
			error.msg = "添加流水号时失败";
			Logger.error("-----------111Ipsdetail944-------------");
			return -1;
		}
		
		error.code = 0;
		
		return ips.id;
	}
	
	/**
	 * 更新流水号状态
	 * @param pMerBillNo
	 * @param error
	 */
	public static void updateMerNo(String pMerBillNo, ErrorInfo error) {
		error.clear();
		
		long merBillNo = Long.parseLong(pMerBillNo);
		
		String sel = "select count(1) from t_ips_sequences where p_mer_bill_no = ? and status = 1";
		String sql = "update t_ips_sequences set status = 1 where p_mer_bill_no = ? and status = 0";
		int row = 0;
		long rowSel = 0;
		
		try{
			rowSel = ((BigInteger)JPA.em().createNativeQuery(sel).setParameter(1, merBillNo).getSingleResult()).intValue();
		}catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("更新流水号时："+ e.getMessage());
			error.code = -1;
			error.msg = "更新流水号时失败";
			
			return ;
		}
		
		if(rowSel >= 1) {
			error.code = Constants.ALREADY_RUN; //已执行
			error.msg = "已执行";
			
			return;
		}
		
		try{
			row = JpaHelper.execute(sql).setParameter(1, merBillNo).executeUpdate();
		}catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("更新流水号时："+ e.getMessage());
			error.code = -1;
			error.msg = "更新流水号时失败";
			
			return ;
		}
		
		if(row <= 0) {
			JPA.setRollbackOnly();
			error.code = Constants.ALREADY_RUN;
			error.msg = "";

			return;
		}
		
		error.code = 1;
	}
	
	/**
	 * 根据放款流水号查询所有投资的流水号
	 * @param pMerBillNo
	 * @param error
	 * @return
	 */
	public static String queryInvestBillNos(String pMerBillNo, ErrorInfo error) {
		String bId = null;
	
		try {
			bId = t_ips_details.find("select memo from t_ips_details where mer_bill_no = ?", pMerBillNo).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "根据放款流水号查询所有投资的流水号失败";
			
			return "";
		}
		
		if (bId == null) {
			error.code = -1;
			error.msg = "ݻ交易不存在";
			
			return "";
		}
		
		long bidId = Convert.strToLong(bId, -1);
		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		List<Invest> invests = Invest.queryAllInvestUser(bid.id);
		StringBuffer bufMerBillNo = new StringBuffer();
		
		for(Invest invest : invests){
			bufMerBillNo.append(invest.merBillNo + ",");
		}
		
		String merBillNos = bufMerBillNo.toString().substring(0,bufMerBillNo.toString().length()-1);
		
		return merBillNos;
	}
	
	/**
	 * 根据流水号查询出投资记录里对应的流水号（易宝放款补单用到）
	 * @param bidId  标id
	 * @param error
	 * @return
	 */
	public static String queryMemberId(String merBillNo, ErrorInfo error){
		error.clear();
        String dId = null;
		
		try {
			dId = t_ips_details.find("select memo from t_ips_details where mer_bill_no = ?", merBillNo).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return null;
		}
		
		if (dId == null) {
			error.code = -1;
			error.msg = "交易不存在";
			
			return null;
		}
		
		String sql = "select mer_bill_no from t_invests where bid_id = ?";
		
		String memberId = null;
		Long bidId = Long.parseLong(dId);
		
		try{
			memberId = t_invests.find(sql, bidId).first();
		}catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("查询投资记录时："+ e.getMessage());
			error.code = -1;
			error.msg = "查询投资记录失败";
			
			return null;
		}
		
		return memberId;
	}
	
	public static String queryMemoByMerBillNo(String merBillNo , ErrorInfo error){
		String memo = "";
		
		try {
			memo = t_ips_details.find("select memo from t_ips_details where mer_bill_no = ?", merBillNo).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return memo;
		}
		
		if (memo == null) {
			error.code = -2;
			error.msg = "交易不存在";
			
			return memo;
		}
		return memo;
	}
	
	/**
	 * 根据流水号查处备注信息
	 * @param merBillNo
	 * @param error
	 * @return
	 */
	public static JSONObject queryMemo(String merBillNo, ErrorInfo error) {
		error.clear();
        String memo = null;
		
		try {
			memo = t_ips_details.find("select memo from t_ips_details where mer_bill_no = ? and status = 2", merBillNo).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return null;
		}
		
		if (memo == null) {
			error.code = -1;
			error.msg = "交易已成功或交易不存在";
			
			return null;
		}
		
		error.code = 1;
		
		JSONObject memoJson = JSONObject.fromObject(memo);
		
		return memoJson;
	}
	
	/**
	 * 存入缓存信息
	 * @param pMerBillNo
	 * @param obj
	 * @param error
	 */
	public static void setIpsInfo(long pMerBillNo, String info, ErrorInfo error) {
		error.clear();
		t_ips_cache ipsCache = new t_ips_cache();
		
		ipsCache.time = new Date();
		ipsCache.p_mer_bill_no = pMerBillNo;
		ipsCache.cache_info = info;
		
		try {
			ipsCache.save();
		}catch(Exception e) {
			Logger.info("存入缓存信息时：%s", e.getMessage());
			error.code = -1;
			error.msg = "存入缓存信息失败";
			
			return ;
		}
		
		error.code = 1;
		
	}
	
	/**
	 * 读取缓存信息
	 * @param pMerBillNo
	 * @param error
	 * @return
	 */
	public static String getIpsInfo(long pMerBillNo, ErrorInfo error) {
		error.clear();
		String sql = "select cache_info from t_ips_cache where p_mer_bill_no = ? limit 1";
		
		String info = null;
		
		try{
			info = JPA.em().createNativeQuery(sql).setParameter(1, pMerBillNo).getSingleResult().toString();
		}catch(Exception e) {
			Logger.info("读取缓存信息时：%s", e.getMessage());
			error.code = -1;
			error.msg = "读取缓存信息失败";
			
			return null;
		}
		
		if(info == null) {
			error.code = -1;
			error.msg = "读取缓存信息不存在";
			return null;
		}
		
		return info;
	}
	
	/**
	 * 读取缓存信息
	 * @param pMerBillNo
	 * @param error
	 * @return
	 */
	public static void deleteIpsInfo(long pMerBillNo, ErrorInfo error) {
		error.clear();
		String sql = "delete from t_ips_cache where p_mer_bill_no = ?";
		
		int rows = 0;
		
		try{
			rows = JpaHelper.execute(sql).setParameter(1, pMerBillNo).executeUpdate();
		}catch(Exception e) {
			Logger.info("删除缓存信息时：%s", e.getMessage());
			error.code = -1;
			error.msg = "删除缓存信息失败";
			
			return;
		}
		
		if(rows <= 0) {
			error.code = -1;
			error.msg = "删除缓存信息不存在";
			return ;
		}
	}
}
