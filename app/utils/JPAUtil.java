package utils;

import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

import play.Logger;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;

public class JPAUtil {
	public static Query createNativeQuery(String sql, Object... params) {
		Query query = JPA.em().createNativeQuery(sql);
		int index = 0;
		
		for (Object param : params) {
			query.setParameter(++index, param);
		}
		
		return query;
	}

	/**
	 * 执行增、删、改语句
	 * @param sql
	 * @param params
	 * @return
	 */
	public static int executeUpdate(ErrorInfo error, String sql, Object... params) {
		error.clear();
		Query query = JpaHelper.execute(sql, params);
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return 0;
		}
		
		if (rows < 1) {
			error.code = -2;
			error.msg = "数据未更新";
		}
		
		return rows;
	}
	
	/**
	 * 执行查询语句，返回一个map对象
	 * @param sql
	 * @param params
	 * @return
	 */
	public static Map<String, Object> getMap(ErrorInfo error, String sql, Object... params) {
		error.clear();
		Query query = createNativeQuery(sql, params);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		
		try {
			return (Map<String, Object>) query.getSingleResult();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return null;
		}
	}
	
	/**
	 * 执行查询语句，返回一个map集合
	 * @param sql
	 * @param params
	 * @return
	 */
	public static List<Map<String, Object>> getList(ErrorInfo error, String sql, Object... params) {
		error.clear();
		Query query = createNativeQuery(sql, params);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		
		try {
			return query.getResultList();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return null;
		}
	}
	
	/**
	 * 执行查询语句，返回一个json对象
	 * @param error
	 * @param sql
	 * @param params
	 * @return
	 */
	public static JSONObject getJSONObject(ErrorInfo error, String sql, Object... params) {
		error.clear();
		Query query = createNativeQuery(sql, params);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		
		try {
			Object obj = query.getSingleResult();
			
			return obj == null ? null : JSONObject.fromObject(obj);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return null;
		}
	}
	
	/**
	 * 执行查询语句，返回一个json数组
	 * @param sql
	 * @param params
	 * @return
	 */
	public static JSONArray getJSONArray(ErrorInfo error, String sql, Object... params) {
		error.clear();
		Query query = createNativeQuery(sql, params);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		
		try {
			List<Map<String, Object>> list = query.getResultList();
			
			return (list == null || list.size() == 0) ? null : JSONArray.fromObject(list);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return null;
		}
	}
}
