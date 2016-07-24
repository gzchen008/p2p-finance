package controllers.interceptor;

import com.shove.security.License;
import business.BackstageSet;
import business.User;
import constants.Constants;
import controllers.BaseController;
import controllers.front.account.LoginAndRegisterAction;
import play.Logger;
import play.mvc.Before;
import utils.ErrorInfo;
import utils.Security;

public class FInterceptor extends BaseController{
	
	@Before(unless={"front.account.FundsManage.gCallback",
			"front.account.FundsManage.callback"
	})
	public static void checkLogin(){
		/*try{
			License.update(BackstageSet.getCurrentBackstageSet().registerCode);
			if(!(License.getDomainNameAllow() && License.getWebPagesAllow())) {
				flash.put("error", "此版本非正版授权，请联系晓风软件购买正版授权！");
				LoginAndRegisterAction.login();
			}
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("进行正版校验时：" + e.getMessage());
			flash.put("error", "此版本非正版授权，请联系晓风软件购买正版授权！");
			LoginAndRegisterAction.login();
		} */
		
		String sign = params.get("id");
		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, 60*60*12, new ErrorInfo());
		User user = null;
		if(id > 0){
			user = User.currAppUser(id+"");
			//来自于APP的webview访问并设更新户信息
			User.setCurrUser(user);
		}else{
		    user = User.currUser();
			
			if(user == null){
				LoginAndRegisterAction.login();
			}
		}
		
		renderArgs.put("user", user);
	}
	
	/**
	 * 模拟登录拦截
	 */
	@Before (only = {
			"front.account.AccountHome.uploadPhoto",
			"front.account.AccountHome.applyForOverBorrow",
			"front.account.AccountHome.vipApply",
			"front.account.AccountHome.setNoteName",
			"front.account.AccountHome.sendMessage",
			"front.account.Message.deleteSystemMsgs",
			"front.account.Message.markMsgsReaded",
			"front.account.Message.markMsgsUnread",
			"front.account.Message.deleteInboxMsgs",
			"front.account.Message.deleteOutboxMsgs",
			"front.account.Message.replyMsg",
			"front.account.Message.createAnswers",
			"front.account.Message.deleteBidQuestion",
			"front.account.AccountHome.attentionUser",
			"front.account.AccountHome.cancelAttentionUser",
			"front.account.AccountHome.vipMoney",
			"front.account.AccountHome.vipApply",
			"front.account.AccountHome.submitRepayment",
			"front.account.AccountHome.repealLoaningBid",
			"front.account.AccountHome.repealAuditingBid",
			"front.account.AccountHome.deleteAuditItem",
			"front.account.AccountHome.createUserAuditItem",
			"front.account.BasicInformation.saveInformation",
			"front.account.BasicInformation.verifySafeQuestion",
			"front.account.BasicInformation.saveSafeQuestion",
			"front.account.BasicInformation.resetSafeQuestion",
			"front.account.BasicInformation.saveSafeQuestionByEmail",
			"front.account.BasicInformation.saveEmail",
			"front.account.BasicInformation.savePassword",
			"front.account.BasicInformation.editPayPassword",
			"front.account.BasicInformation.editPayPassword",
			"front.account.BasicInformation.savePayPassword",
			"front.account.BasicInformation.resetPayPassword",
			"front.account.BasicInformation.saveMobile",
			"front.account.BasicInformation.bindMobile",
			"front.account.FundsManage.addBank",
			"front.account.FundsManage.editBank",
			"front.account.FundsManage.deleteBank",
			"front.account.FundsManage.userAuditItem",
			"front.account.FundsManage.submitWithdrawal",
			"front.account.FundsManage.exportDealRecords",
			"front.account.FundsManage.submitRecharge",
			"front.account.InvestAccount.cancleBid",
			"front.account.InvestAccount.increaseAuction",
			"front.account.InvestAccount.transact",
			"front.account.InvestAccount.acceptDebts",
			"front.account.InvestAccount.notAccept",
			"front.account.InvestAccount.addBlack",
			"front.account.InvestAccount.removeBlacklist",
			"front.account.InvestAccount.closeRobot",
			"front.account.InvestAccount.saveOrUpdateRobot",
			"front.account.LoginAndRegisterAction.saveUsernameByTele",
			"front.account.LoginAndRegisterAction.savePasswordByMobile",
			"front.account.LoginAndRegisterAction.sendResetEmail",
			"front.account.LoginAndRegisterAction.savePasswordByEmail",
			"front.bid.BidAction.createBid",
			"front.bid.BidAction.saveInformation",
			"front.debt.DebtAction.confirmTransfer",
			"front.debt.DebtAction.auction",
			"front.debt.DebtAction.reportUser",
			"front.help.HelpCenterAction.support",
			"front.help.HelpCenterAction.opposition ",
			"front.invest.InvestAction.confirmInvest",
			"front.invest.InvestAction.confirmInvestBottom",
			"front.invest.InvestAction.collectBid",
			"front.account.Message.sendMsg",
			"Application.dlImages"})
	 static void simulateLogin(String encryString) {
        if(User.currUser().simulateLogin != null){
        	if(User.currUser().simulateLogin.equalsIgnoreCase(User.currUser().encrypt())){
            	flash.error("模拟登录不能进行该操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }else{
            	flash.error("模拟登录超时，请重新操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }
        }
	}
}
