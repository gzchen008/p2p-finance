package jobs;

import business.Invest;
import business.Vip;
import play.jobs.Job;
import play.jobs.On;
import reports.StatisticAuditItems;
import reports.StatisticBorrow;
import reports.StatisticInvest;
import reports.StatisticMember;
import reports.StatisticProduct;
import reports.StatisticRecharge;
import reports.StatisticSecurity;
import utils.ErrorInfo;


/**
 * 每天定时定点任务
 * @author lwh
 *
 */

//每天23:50执行


@On("0 50 23 * * ?")
public class EveryDayJob extends Job{
	
	public void doJob() {
		ErrorInfo error = new ErrorInfo();
        
      	StatisticAuditItems.executeUpdate(error);//审核科目库统计
		StatisticProduct.executeUpdate(error);//借款标产品销售情况
		StatisticBorrow.executeUpdate(error);//借款情况统计
		StatisticInvest.investSituationStatistic();//理财情况统计表
		
		StatisticInvest.platformIncomeStatistic();//平台收入
		StatisticInvest.platformWithdrawStatistic();//系统提现
		StatisticInvest.platformFloatstatistics();//平台浮存金统计
		
		StatisticRecharge.executeUpdate(error);//充值统计
		StatisticMember.executeUpdate(error);//会员数据统计分
		StatisticSecurity.executeUpdate(error);//本金保障统计
		
		Vip.vipExpiredJob(); //vip过期处理
		
		//生成借款理财债权协议
		Invest.creatBidPactJob();
		Invest.creatDebtPactJob();
	}
}
