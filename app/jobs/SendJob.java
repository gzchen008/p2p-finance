package jobs;

import business.TemplateEmail;
import business.TemplateSms;
import business.TemplateStation;
import play.jobs.Every;
import play.jobs.Job;

/**
 * 每天定时定点任务
 * @author lwh
 *
 */
/*正式测试或上线请打开此任务*/
@Every("5min")
public class SendJob extends Job{
	
	public void doJob() {
		 
		TemplateStation.dealStationTask();
		TemplateEmail.dealEmailTask();
		TemplateSms.dealSmsTask();
		
	}
}
