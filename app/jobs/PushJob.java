package jobs;

import constants.Constants;
import business.Bill;
import play.jobs.Job;

/**
 * 每天定时定点任务
 * @author lwh
 *
 */
/*正式测试或上线请打开此任务*/
	// TODO
//@On("0 30 12 * * ?")
public class PushJob extends Job{
	
	public void doJob() {
		Bill.queryRecentlyBillsForCast(Constants.PUSH_BILL);
	}
}
