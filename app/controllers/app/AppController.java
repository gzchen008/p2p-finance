package controllers.app;

import com.shove.gateway.GeneralRestGatewayInterface;
import constants.Constants;
import controllers.BaseController;
import controllers.app.common.MessageUtil;
import play.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * app控制器
 * Description:处理app网关传过来的参数并调用对应的处理方法
 * @author zhs
 * vesion: 6.0 
 * @date 2014-10-29 上午11:46:34
 */
public class AppController extends BaseController implements GeneralRestGatewayInterface {

	/**
	 * app端请求服务器的入口
	 * @throws IOException
	 */
	public static void index() throws IOException {
		StringBuilder errorDescription = new StringBuilder();
		AppController app = new AppController();
    	int code = GeneralRestGateway.handle(Constants.APP_ENCRYPTION_KEY, 3000, app, errorDescription);
    	
    	if(code < 0) {
    		Logger.error("%s", errorDescription);
    	}
		Logger.info("调用完成");
		
	}
	
	/**
	 * 根据opt的值调用相对应的方法
	 */
	@Override
	public String delegateHandleRequest(Map<String, String> parameters,
			StringBuilder errorDescription) throws RuntimeException {
		String result = null;
		
		//判断系统是否授权
		// try{
		// 	License.update(BackstageSet.getCurrentBackstageSet().registerCode);
			
		// 	if(!(License.getDomainNameAllow() && License.getWebPagesAllow())) {
		// 		try {
		// 			result = RequestData.checkAuthorize();
		// 			return result;
		// 		} catch (IOException e) {
		// 			Logger.error("进行正版校验时:%s：", e.getMessage());
		// 		}
		// 	}
		// }catch (Exception e) {
		// 	Logger.info("进行正版校验时:%s：" + e.getMessage());
		// }
		
		switch(Integer.valueOf(parameters.get("OPT"))){
		case AppConstants.APP_LOGIN:
			try {
				result = RequestData.login(parameters);
			} catch (IOException e) {
				Logger.error("用户登录时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_REGISTER:
			try {
				result = RequestData.register(parameters);
			} catch (IOException e) {
				Logger.error("注册用户时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_BASEINFO:
			try {
				result = RequestData.queryBaseInfo(parameters);
			} catch (IOException e) {
				Logger.error("查询基本信息时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_FIND_PWD_BY_SMS:
			try {
				result = RequestData.findPwdBySms(parameters);
			} catch (IOException e) {
				Logger.error("根据短信找回密码时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_CONFIRM_CODE:
			try {
				result = RequestData.confirmCode(parameters);
			} catch (IOException e) {
				Logger.error("确认验证码时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_COMMIT_NEW_PWD:
			try {
				result = RequestData.commitPassword(parameters);
			} catch (IOException e) {
				Logger.error("重置密码时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_SAVE_CELLPHONE:
			try {
				result = RequestData.saveCellphone(parameters);
			} catch (IOException e) {
				Logger.error("绑定手机号码时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_SERVICE_AGREEMENT:
			try {
				result = RequestData.ServiceAgreement(parameters);
			} catch (IOException e) {
				Logger.error("查询注册服务协议时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_REPAYMENT_CALCULATOR:
			try {
				result = RequestData.RepaymentCalculator(parameters);
			} catch (IOException e) {
				Logger.error("运行还款计算器时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ALL_BIDS: 
			try { 
			result = RequestData.queryAllbids(parameters); 
			} catch (Exception e) { 
			Logger.error("查询借款标列表时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_BID_DETAIL: 
			try { 
			result = RequestData.bidDetail(parameters); 
			} catch (Exception e) { 
			Logger.error("查询标详情时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_INVEST_BID_RECORD: 
			try { 
			result = RequestData.queryBidInvestRecords(parameters); 
			} catch (Exception e) { 
			Logger.error("查询借款标投标记录时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_ALL_QUESTION: 
			try { 
			result = RequestData.queryAllQuestions(parameters); 
			} catch (Exception e) { 
			Logger.error("查询借款标提问以及回答列表时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_ADD_QUESTIONS: 
			try { 
			result = RequestData.addQuestion(parameters); 
			} catch (Exception e) { 
			Logger.error("查询借款标提问记录时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_INVEST_DETAIL: 
			try { 
			result = RequestData.investDetail(parameters); 
			} catch (Exception e) { 
			Logger.error("查询投标详情时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_INVEST: 
			try { 
			result = RequestData.invest(parameters); 
			} catch (Exception e) { 
			Logger.error("投标操作时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_LOAN_PRODUCT:
			try {
				result = RequestData.loanProduct(parameters);
			} catch (IOException e) {
				Logger.error("查询借款产品列表时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_PRODUCT_INFO:
			try {
				result = RequestData.productInfo(parameters);
			} catch (IOException e) {
				Logger.error("查询借款标产品详情时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_PRODUCT_DETAIL:
			try {
				result = RequestData.productDetails(parameters);
			} catch (IOException e) {
				Logger.error("获取借款产品信息时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_CREATE_BID:
			try {
				result = RequestData.createBid(parameters);
			} catch (IOException e) {
				Logger.error("发布借款时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_USER_STATUS:
			try {
				result = RequestData.UserStatus(parameters);
			} catch (IOException e) {
				Logger.error("获取完善用户资料状态时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_SAVE_BASEINFO:
			try {
				result = RequestData.saveBaseInfo(parameters);
			} catch (IOException e) {
				Logger.error("完善用户资料时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ACTIVE_EMAIL:
			try {
				result = RequestData.activeEmail(parameters);
			} catch (IOException e) {
				Logger.error("通过后台发送激活邮件时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_VIP_APPLY:
			try {
				result = RequestData.vipApply(parameters);
			} catch (IOException e) {
				Logger.error("申请vip时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_VIP_AGREEMENT:
			try {
				result = RequestData.vipAgreement(parameters);
			} catch (IOException e) {
				Logger.error("查询VIP会员服务条款时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_TWO_DIMENSIONANL_CODE:
			try {
				result = RequestData.TwoDimensionalCode(parameters);
			} catch (IOException e) {
				Logger.error("生成二维码时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_SPREAD_USER:
			try {
				result = RequestData.spreadUser(parameters);
			} catch (IOException e) {
				Logger.error("查询推广的会员列表时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ALL_DEBTS: 
			try { 
			result = RequestData.queryAllDebts(parameters); 
			} catch (Exception e) { 
			Logger.error("查询所有债权时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_DEBT_DETAIL: 
			try { 
			result = RequestData.debtDetail(parameters); 
			} catch (Exception e) { 
			Logger.error("查询债权转让标详情时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_DEBTAUCTION_RECORDS: 
			try { 
			result = RequestData.debtAuctionRecords(parameters); 
			} catch (Exception e) { 
			Logger.error("查询债权竞拍记录时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_ACTION_DEBT_DETAIL: 
			try { 
			result = RequestData.auctionDebtDetail(parameters); 
			} catch (Exception e) { 
			Logger.error("获取竞拍相关信息时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_AUCTION: 
			try { 
			result = RequestData.auction(parameters); 
			} catch (Exception e) { 
			Logger.error("债权竞拍时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_INVEST_BILLS: 
			try { 
			result = RequestData.investBills(parameters); 
			} catch (Exception e) { 
			Logger.error("查询理财账单时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_BILL_DETAIL: 
			try { 
			result = RequestData.billDetail(parameters); 
			} catch (Exception e) { 
			Logger.error("查询理财账单时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_CURRENT_BILL_DETAIL: 
			try { 
			result = RequestData.currentBillDetail(parameters); 
			} catch (Exception e) { 
			Logger.error("查询本期账单明细时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_BILL_BID_DETAIL: 
			try { 
			result = RequestData.billBidDetail(parameters); 
			} catch (Exception e) { 
			Logger.error("查询账单借款标详情时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_HISTORY_REPAYMENT: 
			try { 
			result = RequestData.historicalRepayment(parameters); 
			} catch (Exception e) { 
			Logger.error("查询账单历史收款情况时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_INVEST_RECORDS: 
			try { 
			result = RequestData.investRecords(parameters); 
			} catch (Exception e) { 
			Logger.error("查询投标记录时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_LOANING_INVEST_BIDS:
			try {
			result = RequestData.queryUserAllloaningInvestBids(parameters);
			} catch (Exception e) {
			Logger.error("查询等待满标的理财标时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_RECEVING_INVEST_BIDS:
			try {
			result = RequestData.queryUserAllReceivingInvestBids(parameters);
			} catch (Exception e) {
			Logger.error("查询收款中的理财标列表时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_TRANSFER_DEBT:
			try {
			result = RequestData.transferDebt(parameters);
			} catch (Exception e) {
			Logger.error("转让债权时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_SUCCESS_DEBT:
			try {
			result = RequestData.queryUserSuccessInvestBids(parameters);
			} catch (Exception e) {
			Logger.error("查询已成功的理财标时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_DEBT_TRANSFER:
			try {
			result = RequestData.queryUserAllDebtTransfers(parameters);
			} catch (Exception e) {
			Logger.error("债权转让管理时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_DEBT_DETAILS_SUCCESS:
			try {
			result = RequestData.debtDetailsSuccess(parameters);
			} catch (Exception e) {
			Logger.error("查询债权转让成功详情页面时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_SUCCESS_DEBT_DETAILS:
			try {
			result = RequestData.debtDetailsTransfering(parameters);
			} catch (Exception e) {
			Logger.error("查询债权转让中详情页面时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_DEBT_DETAILS_NO_PASS:
			try {
			result = RequestData.debtDetailsNoPass(parameters);
			} catch (Exception e) {
			Logger.error("查询债权转让不通过详情页面时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_DEBT_TRANSFER_DETAIL:
			try {
			result = RequestData.debtTransferDetail(parameters);
			} catch (Exception e) {
			Logger.error("查询债权转让详情时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_DEBT_TRANSFER_BID_DETAIL:
			try {
			result = RequestData.debtTransferBidDetail(parameters);
			} catch (Exception e) {
			Logger.error("查询债权转让借款标详情页面时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_TRANSACT:
			try {
			result = RequestData.transact(parameters);
			} catch (Exception e) {
			Logger.error("成交债权时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ACTION_RECORDS:
			try {
			result = RequestData.queryAuctionRecords(parameters);
			} catch (Exception e) {
			Logger.error("creditorIdStr时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_RECEIVED_DEBT_TRANSFER:
			try {
			result = RequestData.queryUserAllReceivedDebtTransfers(parameters);
			} catch (Exception e) {
			Logger.error("查询用户受让债权管理列表时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_RECEIVE_DEBT_DETAIL_SUCCESS:
			try {
			result = RequestData.receiveDebtDetailSuccess(parameters);
			} catch (Exception e) {
			Logger.error("查询受让债权的详情 [竞拍成功]时：%s：", e.getMessage());
			}
		    break;
		case AppConstants.APP_RECEIVE_DEBT_DETAIL_AUCTION:
			try {
			result = RequestData.receiveDebtDetailAuction(parameters);
			} catch (Exception e) {
			Logger.error("查询受让债权的详情 [竞拍中]时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_RECEIVE_DEBT_DETAIL:
			try {
			result = RequestData.receiveDebtDetail(parameters);
			} catch (Exception e) {
			Logger.error("查询债权受让详情 [竞拍成功,竞拍中,定向转让]时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_RECEIVE_DEBT_BID_DETAIL:
			try {
			result = RequestData.receiveDebtBidDetail(parameters);
			} catch (Exception e) {
			Logger.error("查询受让的借款标详情 [竞拍成功,竞拍中,定向转让]时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_INCREASE_ACTION:
			try {
			result = RequestData.increaseAuction(parameters);
			} catch (Exception e) {
			Logger.error("加价竞拍时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ACCEPT_DEBTS:
			try {
			result = RequestData.acceptDebts(parameters);
			} catch (Exception e) {
			Logger.error("受定向转让债权时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_NOT_ACCEPT:
			try {
			result = RequestData.notAccept(parameters);
			} catch (Exception e) {
			Logger.error("拒绝接受定向债权转让时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_INVEST_STATISTICS:
			try {
			result = RequestData.investStatistics(parameters);
			} catch (Exception e) {
			Logger.error("查询理财情况统计表时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_UPDATE_ROBOTS:
			try {
			result = RequestData.saveOrUpdateRobot(parameters);
			} catch (Exception e) {
			Logger.error("设置投标机器人时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_AUTO_INVEST:
			try {
			result = RequestData.autoInvest(parameters);
			} catch (Exception e) {
			Logger.error("进入自动投标页面时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_CLOSE_ROBOT:
			try {
			result = RequestData.closeRobot(parameters);
			} catch (Exception e) {
			Logger.error("关闭投标机器人时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ATTENTION_DEBTS:
			try {
			result = RequestData.attentionDebts(parameters);
			} catch (Exception e) {
			Logger.error("收藏的债权列表时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ATTENTION_BIDS:
			try {
			result = RequestData.attentionBids(parameters);
			} catch (Exception e) {
			Logger.error("查询收藏的借款标时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ATTENTION_USERS_LSIT:
		    try {
			result = RequestData.myAttentionUser(parameters);
			} catch (Exception e) {
			Logger.error("查询用户关注用户列表时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_BLACK_LIST:
			try {
			result = RequestData.blackList(parameters);
			} catch (Exception e) {
			Logger.error("用户黑名单时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_REPORT_USERS:
			try {
			result = RequestData.reportUser(parameters);
			} catch (Exception e) {
			Logger.error("举报用户时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ADD_BLACK:
			try {
			result = RequestData.addBlack(parameters);
			} catch (Exception e) {
			Logger.error("拉黑对方时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ATTENTION_USERS:
			try {
			result = RequestData.attentionUser(parameters);
			} catch (Exception e) {
			Logger.error("关注用户时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_COLLECT_BID:
			try {
			result = RequestData.collectBid(parameters);
			} catch (Exception e) {
			Logger.error("收藏借款标时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_COLLECT_DEBT:
			try {
			result = RequestData.collectDebt(parameters);
			} catch (Exception e) {
			Logger.error("收藏债权时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_HELP_CENTER: 
			try { 
			result = RequestData.helpCenter(parameters); 
			} catch (Exception e) { 
			Logger.error("进入帮助中心页面时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_HELP_CENTER_CONTENT: 
			try { 
			result = RequestData.helpCenterContent(parameters); 
			} catch (Exception e) { 
			Logger.error("查询帮助中心内容列表时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_HELP_CENTER_DETAIL: 
			try { 
			result = RequestData.helpCenterDetail(parameters); 
			} catch (Exception e) { 
			Logger.error("查询帮助中心列表详情时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_COMPANY_INTRODUCTION: 
			try { 
			result = RequestData.companyIntroduction(parameters); 
			} catch (Exception e) { 
			Logger.error("查询公司介绍时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_MANAGEMENT_TEAM: 
			try { 
			result = RequestData.managementTeam(parameters); 
			} catch (Exception e) { 
			Logger.error("查询管理团队时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_EXPER_ADVISOR: 
			try { 
			result = RequestData.expertAdvisor(parameters); 
			} catch (Exception e) { 
			Logger.error("查询专家顾问时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SEND_STATION: 
			try { 
			result = RequestData.sendStation(parameters); 
			} catch (Exception e) { 
			Logger.error("发送站内信时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SYSTEM_SMS: 
			try { 
			result = RequestData.systemSms(parameters); 
			} catch (Exception e) { 
			Logger.error("查询系统信息时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_DELETE_SYSTEM_SMS: 
			try { 
			result = RequestData.deleteSystemSmgs(parameters); 
			} catch (Exception e) { 
			Logger.error("删除系统信息时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_INBOX_SMGS: 
			try { 
			result = RequestData.inboxMsgs(parameters); 
			} catch (Exception e) { 
			Logger.error("查询收件箱信息时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_DELETE_INBOX_SMGS: 
			try { 
			result = RequestData.deleteInboxMsgs(parameters); 
			} catch (Exception e) { 
			Logger.error("除收件箱信息时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_MARK_MSGS_READED: 
			try { 
			result = RequestData.markMsgsReaded(parameters); 
			} catch (Exception e) { 
			Logger.error("标记为已读时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_MARK_MSGS_UNREAD: 
			try { 
			result = RequestData.markMsgsUnread(parameters); 
			} catch (Exception e) { 
			Logger.error("标记为未读时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_LOAN_BILLS: 
			try { 
			result = RequestData.queryMyLoanBills(parameters); 
			} catch (Exception e) { 
			Logger.error("查询借款账单时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_LOAN_BILL_DETAILS: 
			try { 
			result = RequestData.loanBillDetails(parameters); 
			} catch (Exception e) { 
			Logger.error("查询借款账单详情时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SUBMIT_REPAYMENT: 
			try { 
			result = RequestData.submitRepayment(parameters); 
			} catch (Exception e) { 
			Logger.error("还款时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_AUDITING_LOAN_BIDS: 
			try { 
			result = RequestData.auditingLoanBids(parameters); 
			} catch (Exception e) { 
			Logger.error("查询审核中的借款标列表时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_AUDITING_BIDS: 
			try { 
			result = RequestData.loaningBids(parameters); 
			} catch (Exception e) { 
			Logger.error("查询等待满标的借款标列表时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_REPAYMENT_BIDS: 
			try { 
			result = RequestData.repaymentBids(parameters); 
			} catch (Exception e) { 
			Logger.error("查询还款中的借款标列表时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SUCCESS_BIDS: 
			try { 
			result = RequestData.successBids(parameters); 
			} catch (Exception e) { 
			Logger.error("查询已成功的借款标列表时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_AUDIT_MATERIALS: 
			try { 
			result = RequestData.auditMaterials(parameters); 
			} catch (Exception e) { 
			Logger.error("审核资料认证时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_AUDIT_MATERIALS_SAMEITEM: 
			try { 
			result = RequestData.auditMaterialsSameItem(parameters); 
			} catch (Exception e) { 
			Logger.error("查询审核资料认证详情时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SPREAD_USER_INCOME: 
			try { 
			result = RequestData.spreadUserIncome(parameters); 
			} catch (Exception e) { 
			Logger.error("查询我推广的收入时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_DEAL_RECORD: 
			try { 
			result = RequestData.dealRecord(parameters); 
			} catch (Exception e) { 
			Logger.error("查询交易记录时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_BANK_INFO: 
			try { 
			result = RequestData.bankInfos(parameters); 
			} catch (Exception e) { 
			Logger.error("银行卡管理时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_ADD_BANK: 
			try { 
			result = RequestData.addBank(parameters); 
			} catch (Exception e) { 
			Logger.error("添加银行卡时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_EDIT_BANK: 
			try { 
			result = RequestData.editBank(parameters); 
			} catch (Exception e) { 
			Logger.error("编辑银行卡时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_QUERY_ANSWERS: 
			try { 
			result = RequestData.queryAnswers(parameters); 
			} catch (Exception e) { 
			Logger.error("查询安全问题时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_VERIFYE_QUESTION: 
			try { 
			result = RequestData.verifySafeQuestion(parameters); 
			} catch (Exception e) { 
			Logger.error("校验安全问题时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SAVE_PAY_PWD: 
			try { 
			result = RequestData.savePayPassword(parameters); 
			} catch (Exception e) { 
			Logger.error("保存交易密码时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_EDIT_PAY_PWD: 
			try { 
			result = RequestData.editPayPassword(parameters); 
			} catch (Exception e) { 
			Logger.error("修改交易密码时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SAVE_PWD: 
			try { 
			result = RequestData.savePassword(parameters); 
			} catch (Exception e) { 
			Logger.error("保存登录密码时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_QUESTION_STATUS: 
			try { 
			result = RequestData.questionStatus(parameters); 
			} catch (Exception e) { 
			Logger.error("查询安全问题设置的状态时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_QUESTION_CONTENT: 
			try { 
			result = RequestData.questionContent(parameters); 
			} catch (Exception e) { 
			Logger.error("获取安全问题内容时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SAVE_SAFE_QUESTION: 
			try { 
			result = RequestData.saveSafeQuestion(parameters); 
			} catch (Exception e) { 
			Logger.error("保存安全问题时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_EMAIL_STATUS: 
			try { 
			result = RequestData.emailStatus(parameters); 
			} catch (Exception e) { 
			Logger.error("查询邮箱激活状态时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SAVE_EMAIL: 
			try { 
			result = RequestData.saveEmail(parameters); 
			} catch (Exception e) { 
			Logger.error("修改邮箱时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_PHONE_STATUS: 
			try { 
			result = RequestData.phoneStatus(parameters); 
			} catch (Exception e) { 
			Logger.error("查询安全手机详情及状态时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_MY_CREDIT: 
			try { 
			result = RequestData.myCredit(parameters); 
			} catch (Exception e) { 
			Logger.error("查询我的信用等级时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_VIEW_CREDIT_RULE: 
			try { 
			result = RequestData.viewCreditRule(parameters); 
			} catch (Exception e) { 
			Logger.error("查看信用等级规则时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_CREDIT_INTEGRAL: 
			try { 
			result = RequestData.creditintegral(parameters); 
			} catch (Exception e) { 
			Logger.error("查看信用积分规则时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_AUDIT_ITEM_SCORE: 
			try { 
			result = RequestData.auditItemScore(parameters); 
			} catch (Exception e) { 
			Logger.error("查询审核资料积分明细时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_CREDIT_DETAIL_REPATMENT: 
			try { 
			result = RequestData.creditDetailRepayment(parameters); 
			} catch (Exception e) { 
			Logger.error("查询正常还款积分明细时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_CREDIT_DETAIL_LOAN: 
			try { 
			result = RequestData.creditDetailLoan(parameters); 
			} catch (Exception e) { 
			Logger.error("查询成功借款积分明细时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_CREDIT_DETAIL_INVEST: 
			try { 
			result = RequestData.creditDetailInvest(parameters); 
			} catch (Exception e) { 
			Logger.error("查询成功投标积分明细时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_CREDIT_DETAIL_OVERDUE: 
			try { 
			result = RequestData.creditDetailOverdue(parameters); 
			} catch (Exception e) { 
			Logger.error("查询逾期扣分积分明细时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_APPLY_FOR_OVER_BORROW: 
			try { 
			result = RequestData.applyForOverBorrow(parameters); 
			} catch (Exception e) { 
			Logger.error("申请超额借款时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_OVER_BORROW_LIST: 
			try { 
			result = RequestData.overBorrowLists(parameters); 
			} catch (Exception e) { 
			Logger.error("查询申请超额借款记录列表时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_HOME: 
			try { 
			result = RequestData.home(parameters); 
			} catch (Exception e) { 
			Logger.error("查询首页时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SELECT_AUDIT_ITEMS_INIT: 
			try { 
			result = RequestData.selectAuditItemsInit(parameters); 
			} catch (Exception e) { 
			Logger.error("选择超额借款审核资料库时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_WEALTH_TOOLKIT_CREDIT_CALCULATOR: 
			try { 
			result = RequestData.wealthToolkitCreditCalculator(parameters); 
			} catch (Exception e) { 
			Logger.error("查询信用计算器规则时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_APR_CALCULATOR: 
			try { 
			result = RequestData.aprCalculator(parameters); 
			} catch (Exception e) { 
			Logger.error("查询利率计算器时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_RECRUITMENT: 
			try { 
			result = RequestData.recruitment(parameters); 
			} catch (Exception e) { 
			Logger.error("查询招贤纳士时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_PARTNERS: 
			try { 
			result = RequestData.partners(parameters); 
			} catch (Exception e) { 
			Logger.error("查询合作伙伴时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_VERSION: 
			try { 
			result = RequestData.appVersion(parameters); 
			} catch (Exception e) { 
			Logger.error("获取APP版本时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SERVICE_HOTLINE: 
			try { 
			result = RequestData.serviceHotline(parameters); 
			} catch (Exception e) { 
			Logger.error("获取客服热线时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_NEWS_DETAIL: 
			try { 
			result = RequestData.newsDetail(parameters); 
			} catch (Exception e) { 
			Logger.error("查询财富资讯新闻详情时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_WEALTH_INFO_HOME: 
			try { 
			result = RequestData.wealthinfoHome(parameters); 
			} catch (Exception e) { 
			Logger.error("查询财富资讯首页时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_WEALTH_INFO_NEWS_LIST: 
			try { 
			result = RequestData.wealthinfoNewsList(parameters); 
			} catch (Exception e) { 
			Logger.error("查询财富资讯各个栏目下的新闻列表时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_RESET_SAFE_QUESTION: 
			try { 
				result = RequestData.resetSafeQuestion(parameters); 
				} catch (Exception e) { 
				Logger.error("通过邮箱重置安全问题时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_DELETE_BANK: 
			try { 
				result = RequestData.deleteBank(parameters); 
				} catch (Exception e) { 
				Logger.error("删除银行卡时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_OUTBOX_MSGS: 
			try { 
				result = RequestData.outboxMsgs(parameters); 
				} catch (Exception e) { 
				Logger.error("查询发件箱信息时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_OUTBOX_MSGS_DETAIL: 
			try { 
				result = RequestData.outboxMsgDetail(parameters); 
				} catch (Exception e) { 
				Logger.error("查询发件箱详情信息时：%s：", e.getMessage()); 
				} 
				break;	
		case AppConstants.APP_SYSTEM_MSGS_DETAIL: 
			try { 
				result = RequestData.systemMsgDetail(parameters); 
				} catch (Exception e) { 
				Logger.error("查询系统邮件详情信息时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_INBOX_MSGS_DETAIL: 
			try { 
				result = RequestData.inboxMsgDetail(parameters); 
				} catch (Exception e) { 
				Logger.error("查询收件箱消息详情时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_USER_INFO_STATUS: 
			try { 
				result = RequestData.userInfoStatus(parameters); 
				} catch (Exception e) { 
				Logger.error("查询用户邮箱，手机，安全问题，交易密码状态时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_KITNET_CALCULATOR: 
			try { 
				result = RequestData.kitNetValueCalculator(parameters); 
				} catch (Exception e) { 
				Logger.error("查询净值计算器时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_BID_QUESTIONS: 
			try { 
				result = RequestData.bidQuestions(parameters); 
				} catch (Exception e) { 
				Logger.error("针对当前用户的所有借款提问时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_BID_QUESTIONS_DETAILS: 
			try { 
				result = RequestData.bidQuestionDetail(parameters); 
				} catch (Exception e) { 
				Logger.error("查询提问详情时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_CREDIT_ITEM: 
			try { 
				result = RequestData.creditItem(parameters); 
				} catch (Exception e) { 
				Logger.error("查询审核科目积分明细时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_VIEW_OVER_BORROW: 
			try { 
				result = RequestData.viewOverBorrow(parameters); 
				} catch (Exception e) { 
				Logger.error("查看超额申请详情时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_SUBMIT_WITHDRAWAL: 
			try { 
				result = RequestData.submitWithdrawal(parameters); 
				} catch (Exception e) { 
				Logger.error("申请提现时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_WITHDRAWAL: 
			try { 
				result = RequestData.withdrawal(parameters); 
				} catch (Exception e) { 
				Logger.error("提现初始信息时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_WITHDRAWAL_RECORDS: 
			try { 
				result = RequestData.withdrawalRecords(parameters); 
				} catch (Exception e) { 
				Logger.error("查询提现记录时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_FILE: 
			try { 
				result = RequestData.uploadFile(parameters); 
				} catch (Exception e) { 
				Logger.error("上传文件时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_DELETE_OUTBOX_SMGS: 
			try { 
				result = RequestData.deleteOutboxMsgByUser(parameters); 
				} catch (Exception e) { 
				Logger.error("删除发件箱站内信时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_OFFICIAL_ACTIVITY: 
			try { 
				result = RequestData.queryOfficialActivity(parameters); 
				} catch (Exception e) { 
				Logger.error("查询官方活动时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_CANCEL_ATTENTION_USERS: 
			try { 
				result = RequestData.cancelAttentionUser(parameters); 
				} catch (Exception e) { 
				Logger.error("取消关注用户时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_VIP_FEE: 
			try { 
				result = RequestData.vipInfo(parameters); 
				} catch (Exception e) { 
				Logger.error("获取vip相关信息时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_DELETE_BLACKLIST: 
			try { 
				result = RequestData.deleteBlackList(parameters); 
				} catch (Exception e) { 
				Logger.error("获取vip相关信息时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_DELETE_ATTENTION_BID: 
			try { 
				result = RequestData.deleteAttentionBid(parameters); 
				} catch (Exception e) { 
				Logger.error("获取vip相关信息时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_DELETE_ATTENTION_DEBT: 
			try { 
				result = RequestData.deleteAttentionBebt(parameters); 
				} catch (Exception e) { 
				Logger.error("获取vip相关信息时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_PUSH_SETTINT: 
			try { 
				result = RequestData.pushSetting(parameters); 
				} catch (Exception e) { 
				Logger.error("保存推送设置时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_PUSH_QUERY: 
			try { 
				result = RequestData.queryPushSetting(parameters); 
				} catch (Exception e) { 
				Logger.error("获取推送设置时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_AUDIT_ITEMS: 
			try { 
				result = RequestData.createUserAuditItem(parameters); 
				} catch (Exception e) { 
				Logger.error("提交用户资料时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_FIRST_DEAl_DEBT: 
			try { 
				result = RequestData.firstDealDebt(parameters); 
				} catch (Exception e) { 
				Logger.error("债权用户初步成交债权时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_AUDIT_SUBMIT_UPLOADED_ITEMS: 
			try { 
				result = RequestData.submitUploadedItems(parameters); 
				} catch (Exception e) { 
				Logger.error("提交用户资料时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_CLEAR_AUDIT_ITEMS: 
			try { 
				result = RequestData.clearAuditItem(parameters); 
				} catch (Exception e) { 
				Logger.error("清空用户未付款资料时时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_START_MAP:
			try{
				result = RequestData.getStartMap(parameters);
			} catch(Exception e) {
				Logger.error("APP端启动图时%s", e.getMessage());
			}
			break;
			
		case AppConstants.APP_PROJECT_DETAIL:
			try{
				result = RequestDataExtend.projectDetail(parameters);
			} catch(Exception e) {
				Logger.error("项目详情：%s：", e.getMessage());
			}
			break;	
			
		case AppConstants.APP_FUND_SECURITY:
			try{
				result = RequestDataExtend.fundSecurity(parameters);
			} catch(Exception e) {
				Logger.error("资金安全详情：%s：", e.getMessage());
			}
			break;	
		
		case AppConstants.APP_HOMEPAGE_SHOW_BIDS:
			try{
				result = RequestDataExtend.showP2PProductOnHome(parameters);
			} catch(Exception e) {
				Logger.error("首页展示标的：%s：", e.getMessage());
			}
			break;	
			
		case AppConstants.APP_RETURN_MODE:
			try{
				result = RequestDataExtend.returnMode(parameters);
			} catch(Exception e) {
				Logger.error("收益获取：%s：", e.getMessage());
			}
			break;
            case AppConstants.APP_P2P_AT_TOKEN:
                try{
                    result = RequestDataExtend.getAuthToken(parameters);
                } catch(Exception e) {
                    Logger.error("获取authToken：%s：", e.getMessage());
                }
                break;
            case AppConstants.APP_P2P_AT_TOKEN_CLEAN:
                try{
                    result = RequestDataExtend.removeAuthToken();
                } catch(Exception e) {
                    Logger.error("清除authToken：%s：", e.getMessage());
                }
                break;
            case AppConstants.APP_EDIT_USER_INFO:
                try{
                    Logger.info("保存用户信息====");
                    RequestDataExtend.editUserInfo(parameters);
                    result = MessageUtil.getInstance().toStr();
                    Logger.info("保存用户信息返回：" + result);
                } catch(Exception e) {
                    Logger.error("保存用户信息：%s：", e.getMessage());
                }
                break;
            case AppConstants.APP_QUERY_ACC_BALANCE:
                try{
                    result = RequestDataExtend.queryForAccBalance();
                    Logger.debug("用户余额查询返回：" + result);
                } catch(Exception e) {
                    Logger.error("用户余额查询：%s：", e.getMessage());
                }
                break;
            case AppConstants.APP_SIGN_INVEST:
                try{
                    result = RequestDataExtend.invest(parameters);
                    Logger.debug("获取标的sign返回：" + result);
                } catch(Exception e) {
                    Logger.error("获取标的sign：%s：", e.getMessage());
                }
                break;
            case AppConstants.APP_INVESTBILLS:
                try{
                    result = RequestDataExtend.investBills(parameters);
                    Logger.debug("获取投资记录返回：" + result);
                } catch(Exception e) {
                    Logger.error("获取投资记录：%s：", e.getMessage());
                }
                break;
            case AppConstants.queryUserInvestInfo:
                try{
                    result = RequestDataExtend.queryUserInvestInfo();
                    Logger.debug("获取个人财富返回：" + result);
                } catch(Exception e) {
                    Logger.error("获取个人财富：%s：", e.getMessage());
                }
                break;


            case AppConstants.APP_LIST_BIDS:
                try{
                    result = RequestDataExtend.queryAllbids(parameters);
                    Logger.debug("查询标的列表：" + result);
                } catch(Exception e) {
                    Logger.error("查询标的列表", e.getMessage());
                }
                break;
        }

		return result;
	}
	
}
