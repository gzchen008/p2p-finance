package controllers.supervisor.systemSettings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import models.t_right_groups;
import models.v_right_groups;
import models.v_supervisors;
import business.Right;
import business.RightGroup;
import business.Supervisor;
import play.Logger;
import utils.ErrorInfo;
import utils.PageBean;
import utils.RegexUtils;
import utils.Security;

/**
 * 管理员管理
 * @author lzp
 * @version 6.0
 * @created 2014-5-28
 */
public class supervisorAction extends SupervisorController {
	/**
	 * 管理员列表(首页)
	 * @param currPage
	 * @param pageSize
	 * @param keyword
	 */
	public static void list(int currPage, int pageSize, String keyword) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_supervisors> pageBean = 
				Supervisor.querySupervisors(currPage, pageSize, keyword, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(pageBean);
	}
	
	/**
	 * 添加管理员初始化
	 */
	public static void createSupervisorInit() {
		render();
	}
	
	/**
	 * 添加管理员
	 */
	public static void createSupervisor(int level, String groupIds, String realityName, int sex,
			String birthday, String mobile1, String mobile2, String email) {
		ErrorInfo error = new ErrorInfo();
		
		if (StringUtils.isBlank(birthday)) {
			error.code = -1;
			error.msg = "出生日期不能为空";
			
			renderJSON(error);
		}
		
		birthday = birthday.replaceAll("\\s+", "");
		
		if (!RegexUtils.isDate(birthday)) {
			error.code = -1;
			error.msg = "出生日期格式不正确，正确的格式如：2008-08-08";
			
			renderJSON(error);
		}
		
		Date date = null;
		
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(birthday);
		} catch (ParseException e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据异常，请重试";
			
			renderJSON(error);
		}
		
		Supervisor supervisor = new Supervisor();
		supervisor.password = Constants.SUPERVISOR_INITIAL_PASSWORD;
		supervisor.level = level;
		supervisor.realityName = realityName;
		supervisor.sex = sex;
		supervisor.birthday = date;
		supervisor.mobile1 = mobile1;
		supervisor.mobile2 = mobile2;
		supervisor.email = email;
		supervisor.isAllowLogin = true;
		supervisor.isErased = false;
		supervisor.create(error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		if (StringUtils.isBlank(groupIds)) {
			renderJSON(error);
		}
		
		supervisor.editGroups(groupIds, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		error.msg = "添加管理员成功";
		
		renderJSON(error);
	}
	
	/**
	 * 设置管理员的权限组初始化
	 * @param currPage
	 * @param pageSize
	 */
	public static void selectGroupsOfSupervisorInit(int currPage, int pageSize) {
		ErrorInfo error = new ErrorInfo();
		
		PageBean<v_right_groups> pageBean = RightGroup.queryRightGroups(currPage, pageSize, null, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render(pageBean);
	}
	
	/**
	 * 设置管理员的权限组
	 * @param sign
	 * @param groupIds
	 */
	public static void setGroupsOfSupervisor(String sign, String groupIds) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		Supervisor supervisor = new Supervisor();
		supervisor.id = supervisorId;
		supervisor.editGroups(groupIds, error);
		
		renderJSON(error);
	}
	
	/**
	 * 编辑管理员初始化
	 */
	public static void editSupervisorInit(String sign) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		Supervisor supervisor = new Supervisor();
		supervisor.id = supervisorId;
		
		String groupIds = Supervisor.queryGroupIds(supervisorId, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render(supervisor, groupIds);
	}
	
	/**
	 * 编辑管理员
	 */
	public static void editSupervisor(String sign, int level, String realityName, int sex,
			String birthday, String mobile1, String mobile2, String email) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		if (StringUtils.isBlank(birthday)) {
			error.code = -1;
			error.msg = "出生日期不能为空";
			
			renderJSON(error);
		}
		
		birthday = birthday.replaceAll("\\s+", "");
		
		if (!RegexUtils.isDate(birthday)) {
			error.code = -1;
			error.msg = "出生日期格式不正确，正确的格式如：2008-08-08";
			
			renderJSON(error);
		}
		
		Date date = null;
		
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(birthday);
		} catch (ParseException e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据异常，请重试";
			
			renderJSON(error);
		}
		
		Supervisor supervisor = new Supervisor();
		supervisor.id = supervisorId;
		supervisor.level = level;
		supervisor.realityName = realityName;
		supervisor.sex = sex;
		supervisor.birthday = date;
		supervisor.mobile1 = mobile1;
		supervisor.mobile2 = mobile2;
		supervisor.email = email;
		supervisor.edit(error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		renderJSON(error);
	}

	/**
	 * 查看详情
	 * @param id
	 */
	public static void detail(String sign) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}

		v_supervisors supervisor = Supervisor.detail(supervisorId, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render(supervisor);
	}

	/**
	 * 设置权限初始化
	 * @param sign
	 */
	public static void setRightsInit(String sign) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		List<t_right_groups> groups = Supervisor.queryGroups(supervisorId, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		List<Map<String, Object>> rightMapList = Right.queryRightMap(error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		String rightIds = Supervisor.queryRightIds(supervisorId, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		String groupName = "";
		String groupDescription = "";
		
		for (t_right_groups group : groups) {
			groupName += group.name+",";
			groupDescription += group.description+",";
		}
		
		if (groups.size() > 0) {
			groupName = groupName.substring(0, groupName.length()-1);
			groupDescription = groupDescription.substring(0, groupDescription.length()-1);
		}
		
		render(groupName, groupDescription, sign, rightMapList, rightIds);
	}
	
	/**
	 * 设置权限
	 * @param sign
	 * @param rightIds
	 */
	public static void setRights(String sign, String rightIds) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		new Supervisor(supervisorId).editRights(rightIds, error);
		
		renderJSON(error);
	}
	
	/**
	 * 锁定/启用
	 * @param sign
	 * @param isAllowLogin
	 */
	public static void enable(String sign, boolean isAllowLogin) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		new Supervisor(supervisorId).enable(isAllowLogin, error);

		renderJSON(error);
	}
	
	/**
	 * 删除管理员
	 * @param sign
	 */
	public static void deleteSupervisor(String sign) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		Supervisor.delete(supervisorId, error);
		
		renderJSON(error);
	}
	
	/**
	 * 重置管理员密码
	 */
	public static void resetPassword(String sign, int currPage, int pageSize) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			flash.error(error.msg);
			
			list(currPage, pageSize, "");
		}
		
		Supervisor.resetPassword(supervisorId, error);
		flash.error(error.msg);
		
		list(currPage, pageSize, "");
	}
}
