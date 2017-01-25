package com.sos.hibernate.classes;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.hibernate.SQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSHibernateConnectionTest {
	final static Logger LOGGER = LoggerFactory.getLogger(SOSHibernateConnectionTest.class);

	public void withTransaction(SOSHibernateConnection conn) throws Exception {
		//conn.setAutoCommit(false);
		conn.connect();

		boolean delete = false;
		SQLQuery q = null;

		LOGGER.info("autocommit 1= " + conn.getAutoCommit());

		conn.beginTransaction();
		LOGGER.info("autocommit 1.1= " + conn.getAutoCommit());
		q = conn.createSQLQuery("select * from SCHEDULER_VARIABLES where \"NAME\"='test'");
		LOGGER.info("autocommit 1.2= " + conn.getAutoCommit());
		List result = q.list();
		LOGGER.info("autocommit 1.3= " + conn.getAutoCommit());
		conn.commit();

		LOGGER.info("autocommit 2= " + conn.getAutoCommit());

		if (result == null || result.size() == 0) {
			conn.beginTransaction();
			LOGGER.info("autocommit 2.1= " + conn.getAutoCommit());
			q = conn.createSQLQuery(
					"insert into SCHEDULER_VARIABLES(\"NAME\",\"WERT\",\"TEXTWERT\") values('test',0,'test')");
			LOGGER.info("autocommit 2.2= " + conn.getAutoCommit());
			q.executeUpdate();
			LOGGER.info("autocommit 2.3= " + conn.getAutoCommit());
			conn.commit();
		}

		LOGGER.info("autocommit 3= " + conn.getAutoCommit());

		conn.beginTransaction();
		LOGGER.info("autocommit 3.1= " + conn.getAutoCommit());
		q = conn.createSQLQuery("update SCHEDULER_VARIABLES set \"WERT\" = 1 where \"NAME\"='test'");
		LOGGER.info("autocommit 3.2= " + conn.getAutoCommit());
		q.executeUpdate();
		LOGGER.info("autocommit 3.3= " + conn.getAutoCommit());
		conn.commit();

		LOGGER.info("autocommit 4= " + conn.getAutoCommit());

		if (delete) {
			conn.beginTransaction();
			LOGGER.info("autocommit 4.1= " + conn.getAutoCommit());
			q = conn.createSQLQuery("delete from SCHEDULER_VARIABLES where \"NAME\"='test'");
			LOGGER.info("autocommit 4.2= " + conn.getAutoCommit());
			q.executeUpdate();
			LOGGER.info("autocommit 4.3= " + conn.getAutoCommit());
			conn.commit();

			// TimeUnit.SECONDS.sleep(5);
			LOGGER.info("autocommit 6= " + conn.getAutoCommit());
		}

	}

	public void withoutTransaction(SOSHibernateConnection conn) throws Exception {
		//conn.setAutoCommit(true);
		conn.connect();

		SQLQuery q = null;

		LOGGER.info("autocommit 1 = " + conn.getAutoCommit());
		q = conn.createSQLQuery(
				"insert into SCHEDULER_VARIABLES(\"NAME\",\"WERT\",\"TEXTWERT\") values('test',0,'test')");
		LOGGER.info("autocommit 1.1= " + conn.getAutoCommit());
		q.executeUpdate();

		LOGGER.info("autocommit 2= " + conn.getAutoCommit());

		q = conn.createSQLQuery("select * from SCHEDULER_VARIABLES");
		LOGGER.info("autocommit 2.1= " + conn.getAutoCommit());
		q.list();

		LOGGER.info("autocommit 3= " + conn.getAutoCommit());

		q = conn.createSQLQuery("update SCHEDULER_VARIABLES set \"WERT\" = 1 where \"NAME\"='test'");
		LOGGER.info("autocommit 3.1= " + conn.getAutoCommit());
		q.executeUpdate();

		LOGGER.info("autocommit 4= " + conn.getAutoCommit());
		/**
		 * q = conn.createSQLQuery(
		 * "delete from SCHEDULER_VARIABLES where \"NAME\"='test'");
		 * LOGGER.info("autocommit 4.1= "+conn.getAutoCommit());
		 * q.executeUpdate();
		 */
		// TimeUnit.SECONDS.sleep(5);
		LOGGER.info("autocommit 5= " + conn.getAutoCommit());

	}

	
	
	public static void main(String[] args) throws Exception {
		String hibernateConfigFile = "D:/Arbeit/scheduler/jobscheduler_data/re-dell_4444_jobscheduler.1.11x64-snapshot/config/hibernate.cfg.xml";
		SOSHibernateFactory factory = new SOSHibernateFactory(hibernateConfigFile);
		factory.build();
		
		LOGGER.info(factory.getDialect().toString());
		
		/**
		try {
			conn = new SOSHibernateConnection(hibernateConfigFile);
			Properties p = new Properties();
			// p.put("hibernate.current_session_context_class","jta");
			// conn.setConfigurationProperties(p);
			// conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

			SOSHibernateConnectionTest t = new SOSHibernateConnectionTest();
			t.withTransaction(conn);

		} catch (Exception e) {
			if (conn != null) {
				conn.rollback();
			}
			throw e;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}*/

	}

}
