package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Query;
import constants.OptionKeys;
import constants.SupervisorEvent;
import play.Logger;
import play.cache.Cache;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;
import utils.DateUtil;
import utils.ErrorInfo;
import models.t_content_advertisements_links;

/**
 * 底部链接
 * 
 * @author zhs
 * @version 6.0
 * @created 2014年3月24日 下午4:01:00
 */
public class BottomLinks  implements Serializable{

	public long id; // 底部链接id
	public long _id;

	public Date time;
	public String key;
	
	public String title; // 标题
	public String url; // 广告链接
	public int target;
	public int order; // 排序

	public long getId() {
		return _id;
	}

	public void setId(long id) {
		if(id <= 0) {
			return ;
		}
		
		t_content_advertisements_links bottominfo = t_content_advertisements_links
				.findById(id);
		if ((this.id < 0) || (bottominfo == null)) {
			this._id = -1;
			return;
		}

		this._id = id;
		this.key = bottominfo._key;
		this.title = bottominfo.title;
		this.url = bottominfo.url;
		this.target = bottominfo.target;
		this.order = bottominfo._order;

	}
	
	public static void setCurrentLinks() {
		Cache.set("bottomlinks", queryBottomLinks());
	}
	
	public static Map<String, List<BottomLinks>> currentBottomlinks() {
		Map<String, List<BottomLinks>> BottomLinks = (Map<String, List<business.BottomLinks>>) Cache.get("bottomlinks");
		
		if(BottomLinks == null) {
			BottomLinks = queryBottomLinks();
		}
		
		return BottomLinks;
	}

	public static Map<String, List<BottomLinks>> queryBottomLinks() {
		
		List<t_content_advertisements_links> links = null;
				
		String sql = "select new t_content_advertisements_links(_key, title, url, target)"
				+ " from t_content_advertisements_links link order by link._order";
		
		try {
			links = t_content_advertisements_links.find(sql).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询底部链接时：" + e.getMessage());

			return null;
		}
		
		List<BottomLinks> introductionLinks = new ArrayList<BottomLinks>();
		List<BottomLinks> loanLinks = new ArrayList<BottomLinks>();
		List<BottomLinks> financeLinks = new ArrayList<BottomLinks>();
		List<BottomLinks> usLinks = new ArrayList<BottomLinks>();
		List<BottomLinks> centreLinks = new ArrayList<BottomLinks>();
		List<BottomLinks> supportLinks = new ArrayList<BottomLinks>();
		
		BottomLinks bottomLink = null;
		
		for(t_content_advertisements_links link : links) {
			bottomLink = new BottomLinks();
			
			bottomLink.key = link._key;
			bottomLink.title = link.title;
			bottomLink.url = link.url;
			bottomLink.target = link.target;
			
			if(link._key.equals(OptionKeys.LABLE_BEGINNER_INTRODUCTION)) {
				introductionLinks.add(bottomLink);
			}else if(link._key.equals(OptionKeys.LABLE_ABOUT_LOAN)) {
				loanLinks.add(bottomLink);
			}else if(link._key.equals(OptionKeys.LABLE_ABOUT_FINANCING)) {
				financeLinks.add(bottomLink);
			}else if(link._key.equals(OptionKeys.LABLE_ABOUT_US)) {
				usLinks.add(bottomLink);
			}else if(link._key.equals(OptionKeys.LABLE_HELP_CENTRE)) {
				centreLinks.add(bottomLink);
			}else{
				supportLinks.add(bottomLink);
			}
		}
		
		
		Map<String, List<BottomLinks>> bottomLinks = new HashMap<String, List<BottomLinks>>();
		
		bottomLinks.put("1", introductionLinks);
		bottomLinks.put("2", loanLinks);
		bottomLinks.put("3", financeLinks);
		bottomLinks.put("4", usLinks);
		bottomLinks.put("5", centreLinks);
		bottomLinks.put("6", supportLinks);
		
		return bottomLinks;
	}
	
	public static List<BottomLinks> queryFrontBottomLinks(String target) {
		Map<String, List<BottomLinks>> bottomLinksMap = currentBottomlinks();
		
		return bottomLinksMap.get(target);

	}

	/**
	 * 后台->底部链接管理
	 * 
	 * @param key
	 * @return
	 */
	public static List<t_content_advertisements_links> queryBottomLinksByKey(
			String key) {
		List<t_content_advertisements_links> bottomLinks = null;

		try {
			bottomLinks = t_content_advertisements_links.find("_key = ? order by _order", key).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询底部链接时：" + e.getMessage());

			return null;
		}

		return bottomLinks;
	}

	/**
	 * 查出所有的链接
	 * 
	 * @param obj
	 */
	public List<BottomLinks> queryAllBottomLink(ErrorInfo error) {
		BottomLinks bottomLinks = null;
		List<BottomLinks> buttonList = new ArrayList<BottomLinks>();
		List<Map<String, Object>> BottomLinkList = null;

		String sql = "select new Map(link.id as id, link.title as title, link.url as url, link.target as target,"
				+ " link._order as order) from t_content_advertisements_links as link where type_id = ?";
		try {
			BottomLinkList = t_content_advertisements_links.find(sql, this.key)
					.fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询底部链接时：" + e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致查询底部链接失败！";

			return null;
		}

		for (Map<String, Object> map : BottomLinkList) {
			bottomLinks = new BottomLinks();

			bottomLinks.order = (Integer) map.get("order");
			bottomLinks.url = (String) map.get("url");
			bottomLinks.title = (String) map.get("title");
			bottomLinks.target = (Integer) map.get("target");
			bottomLinks.id = (Long) map.get("id");

			buttonList.add(bottomLinks);

		}
		
		error.code = 0;
		
		return buttonList;

	}

	/**
	 * 编辑底部链接后更新
	 * 
	 * @param error
	 * @return
	 */
	public int updateBottomLink(long id,ErrorInfo error) {
		error.clear();
			
//		try {
//			count =(int) t_content_advertisements_links.count("_order = ?", this.order);
//		} catch (Exception e) {
//			e.printStackTrace();
//			Logger.info("查询底部链接序号是否存在时：" + e.getMessage());
//			error.code = -1;
//			error.msg = "由于数据库异常，导致查询底部链接序号是否存在失败！";
//
//			return error.code;
//		}
		
		verifyOrder(id, key, order, error);
		
		if(error.code < 0){
			error.code = -1;
			error.msg = "该序号已经存在！";

			return error.code;
		}
			
		Query query = JpaHelper.execute("update t_content_advertisements_links set time = ?,"
				+ " title = ?, url = ?, target = ?, _order = ? where id = ?")
				.setParameter(1, DateUtil.currentDate())
				.setParameter(2, this.title).setParameter(3, this.url)
				.setParameter(4, this.target).setParameter(5, this.order)
				.setParameter(6, id);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("编辑底部链接时：" + e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致编辑底部链接失败！";

			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}

		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.EDIT_BOTTOMLINK,
				"编辑底部链接", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;

		}
		
		setCurrentLinks();
		
		error.code = 0;
		error.msg = "底部链接编辑成功！";

		return 0;
	}

	/**
	 * 校验顺序的唯一性
	 * 
	 * @param key
	 * @param order
	 * @param error
	 * @return <0 不唯一或者查询出现错误
	 */
	public static int verifyOrder(long id, String key, int order, ErrorInfo error) {
		error.clear();

		long count = 0;

		try {
			count = t_content_advertisements_links.count(
					"_key = ? and _order = ? and id <> ?", key, order, id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("校验顺序的唯一性：" + e.getMessage());

			error.code = -1;
			error.msg = "由于数据库异常，导致查询底部链接失败！";
		}

		if (count > 0) {
			error.code = -2;
			error.msg = "该顺序已存在！";
			
			return error.code;
		}

		error.code = 0;
		error.msg = "该顺序不存在！";

		return 0;
	}
}
