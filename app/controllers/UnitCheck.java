package controllers;

import constants.Constants;
import controllers.front.account.AccountHome;
import controllers.supervisor.managementHome.HomeAction;
import play.Logger;
import play.mvc.Before;

public class UnitCheck extends BaseController{

	@Before
	static void checkAccess() {
		
		Unit unit = getActionAnnotation(Unit.class);
		
		if(unit != null && !Constants.DEBT_USE) {
			Logger.info("unitCheck 执行");
			
			int value = unit.value();
			
			if(value == 1) {
				AccountHome.home();
			}else if(value == 2) {
				HomeAction.showHome();
			}
			
		}
	}
}
