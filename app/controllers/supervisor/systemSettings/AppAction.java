package controllers.supervisor.systemSettings;

import org.apache.commons.lang.StringUtils;
import business.BackstageSet;
import controllers.supervisor.SupervisorController;

public class AppAction extends SupervisorController {
	/**
	 * app版本设置页面
	 */
	public static void version() {
		BackstageSet set = BackstageSet.getCurrentBackstageSet();
		
		render(set);
	}
	
	/**
	 * 保存app版本
	 */
	public static void saveVersion(String iosVersion, String iosCode,
			String iosMsg, String androidVersion, String androidCode,
			String androidMsg) {
		if( StringUtils.isBlank(iosVersion) ||
			StringUtils.isBlank(iosMsg) ||
			StringUtils.isBlank(androidVersion) ||
			StringUtils.isBlank(iosVersion) ||
			StringUtils.isBlank(iosCode) ||
			StringUtils.isBlank(androidCode) 
		  ){
			flash.error("请输入正确的数据!");
			
			version();
		}
		
		BackstageSet set = new BackstageSet();
		set.androidVersion = androidVersion;
		set.androidCode = androidCode;
		set.iosVersion = iosVersion;
		set.iosCode = iosCode;
		set.iosMsg = iosMsg;
		set.androidMsg = androidMsg;
		
		if(set.appVersionSet() < 1) 
			flash.error("保存失败!");
		else
			flash.error("保存成功!");
		
		version();
	}
}
