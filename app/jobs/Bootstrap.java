package jobs;
import business.BackstageSet;
import play.*;
import play.jobs.*;

@OnApplicationStart
public class Bootstrap extends Job {
 	@Override
    public void doJob() {
	     new BackstageSet();
	     
	     BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
	     
	     Play.configuration.setProperty("mail.smtp.host",backstageSet.emailWebsite);
	     Play.configuration.setProperty("mail.smtp.user",backstageSet.mailAccount);
	     Play.configuration.setProperty("mail.smtp.pass",backstageSet.mailPassword);
    }
 
}