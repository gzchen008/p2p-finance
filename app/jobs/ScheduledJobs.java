package jobs;

import play.jobs.Job;

/**
 * 周期性执行任务
 * @author lzp
 * @version 6.0
 * @created 2014-7-14
 */

//每天凌晨0点10分执行
//@On("0 10 00 * * ?")
public class ScheduledJobs extends Job {
	
	public void doJob() {
    }
}
