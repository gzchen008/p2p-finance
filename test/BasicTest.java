import net.sf.json.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.SQLQuery;
import org.hibernate.engine.ExecuteUpdateResultCheckStyle;
import org.hibernate.transform.Transformers;
import org.hibernate.usertype.UserType;
import org.junit.*;

import com.google.zxing.BarcodeFormat;
import com.mysql.jdbc.Connection;
import com.shove.code.Qrcode;
import com.shove.security.Encrypt;

import constants.Constants;


import business.BackstageSet;
import business.Bid;
import business.Bill;
import business.BillInvests;
import business.BottomLinks;
import business.OverBorrow;
import business.StatisticalReport;
import business.Supervisor;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import constants.Constants;
import constants.OptionKeys;
import constants.Constants.*;
import business.Bid;
import business.CreditLevel;
import business.DataSafety;
import business.Debt;
import business.Invest;
import business.News;
import business.NewsType;
import business.OverBorrow;
import business.Product;
import business.Right;
import business.RightGroup;
import business.StationLetter;
import business.Supervisor;
import business.User;
import business.UserAuditItem;
import business.Bid.Purpose;
import business.Bid.Repayment;

import java.math.BigDecimal;
import java.net.ServerSocket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import play.db.DB;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;



import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.jobs.Job;
import play.jobs.On;
import play.test.*;
import reports.StatisticMember;
import reports.StatisticRecharge;
import reports.StatisticSecurity;
import utils.Arith;
import utils.DataUtil;
import utils.DateUtil;
import utils.ErrorInfo;
import reports.StatisticAuditItems;
import reports.StatisticBorrow;
import reports.StatisticInvest;
import reports.StatisticProduct;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.PageBean;
import utils.RegexUtils;
import models.*;

public class BasicTest extends UnitTest {
	
	@Test
	public void systemMakeOverdue(){
		ErrorInfo error = new ErrorInfo();
		
		new Bill().systemMakeOverdue(error); //系统标记逾期
		
		System.out.println(error.code);
	}
	
	@Test
	public void aa(){
		JPAPlugin plugin = new JPAPlugin(); // 事务工具
		plugin.startTx(false);
		
		String sql = "update t_supervisors set name = ? where id = 3";
		EntityManager entityManager = JPA.em();
		entityManager.setFlushMode(FlushModeType.COMMIT);
		Query query = entityManager.createNativeQuery(sql);
		
		query.setParameter(1, "你好22");
		int row = query.executeUpdate();
		
		System.out.println(row);
		
	}
	
	@Test
	public void aaa(){
		//Bid.checkBidIsFlow();
		//Bid.checkBidIsFlow();
		
		java.sql.Connection conn = DB.getConnection();
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	
	@Test
	public void aaaa(){
		ErrorInfo error = new ErrorInfo();
		
		Debt.auditDebtTransferNoPass(9l, 1, "123", error);
		
		System.out.println(error.msg);
	}
	
	@Test
	public void aaaaa(){
		String sql = "select id from t_users where 1 = 1";
		//sql += " and id = 7";
		List<Long> ids = t_users.find(sql).fetch();
		DataSafety dataSafety = new DataSafety();
		ErrorInfo error = new ErrorInfo();
		
		for (Long id : ids) {
			dataSafety.id = id;
			dataSafety.updateSign(error);
			
			if(error.code < 0)
				System.out.println(id);
		}
	}
	
	public static void main(String[] args) {
		// 创建一个固定大小的线程池
        ExecutorService service = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 10; i++) {
            System.out.println("创建线程" + i);
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    System.out.println("启动线程");
                }
            };
            // 在未来某个时间执行给定的命令
            service.execute(run);
        }
        // 关闭启动线程
        service.shutdown();
        // 等待子线程结束，再继续执行下面的代码
        try {
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
        System.out.println("all thread complete");
	}

	@Test
	public void testEncry(){
		String pwd = com.shove.security.Encrypt.MD5("123" + Constants.ENCRYPTION_KEY);
		System.out.print(pwd);
	}
}
