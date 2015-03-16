package com.sos.hibernate.classes;

import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.jdbc.Work;
import org.hibernate.loader.criteria.CriteriaJoinWalker;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.Transformers;
import org.hibernate.type.NumericBooleanType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

public class SOSHibernateConnection implements Serializable {
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(SOSHibernateConnection.class);

	private File configFile;
	
	private Configuration configuration;
	private SessionFactory sessionFactory;
	private Dialect dialect;
	private Object currentSession;
	private Connection jdbcConnection;
	
	private boolean useOpenStatelessSession;
	private boolean useGetCurrentSession;
	private FlushMode sessionFlushMode;
	
	private Properties defaultConfigurationProperties;
	private Properties configurationProperties;
	private ClassList classMapping;
		
	private boolean useDefaultConfigurationProperties = true;
	private String connectionIdentifier;
	private Enum<SOSHibernateConnection.DBMS> dbms = DBMS.UNKNOWN;
	
	/** bei autoCommit = true beginTransaction(), commit(), rollback() nicht ausführen */
	private boolean ignoreAutoCommitTransactions = false; 
	private String openSessionMethodName;
	private int defaultFetchSize;
	
	/** 
	 * 0 = TRANSACTION_NONE
	 * 1 = TRANSACTION_READ_UNCOMMITTED
	 * 2 = TRANSACTION_READ_COMMITTED
	 * 4 = TRANSACTION_REPEATABLE_READ
	 * 8 = TRANSACTION_SERIALIZABLE
	 */
	public static final String HIBERNATE_PROPERTY_TRANSACTION_ISOLATION = "hibernate.connection.isolation";
	public static final String HIBERNATE_PROPERTY_AUTO_COMMIT = "hibernate.connection.autocommit";
	public static final String HIBERNATE_PROPERTY_USE_SCROLLABLE_RESULTSET = "hibernate.jdbc.use_scrollable_resultset";
	public static final String HIBERNATE_PROPERTY_CURRENT_SESSION_CONTEXT_CLASS = "hibernate.current_session_context_class";
	
	/**
	 * 
	 * @throws Exception
	 */
	public SOSHibernateConnection() throws Exception{
		this(null);
	}
	
	/**
	 * 
	 * @param hibernateConfigFile
	 * @throws Exception
	 */
	public SOSHibernateConnection(String hibernateConfigFile) throws Exception {
		
		initConfigFile(hibernateConfigFile);
		initClassMapping();
		initConfigurationProperties();
		initSessionProperties();
	}

	
	/**
	 * 
	 * @author Robert Ehrlich
	 *
	 */
	public enum DBMS {
		UNKNOWN,
		DB2,
		FBSQL,
		MSSQL,
		MYSQL,
		ORACLE,
		PGSQL,
		SYBASE
	} 
	
	
	/**
	 * 
	 * @throws Exception
	 */
	public void reconnect() throws Exception {
		String method = getMethodName("reconnect");
		
		try {
			logger.info(String.format("%s: useOpenStatelessSession = %s",
					method,useOpenStatelessSession));
			
			disconnect();
			
			if(configuration == null){
				initConfiguration();
			}
			initSessionFactory();
			openSession();
			
		} catch (Exception ex) {
			throw new Exception(String.format("%s: %s", method,
					ex.toString()));
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void connect() throws Exception {

		String method = getMethodName("connect");
				
		try {
			initConfiguration();
			initSessionFactory();
			openSession();
			
			String connFile = (configFile == null) ? "without config file" : configFile.getCanonicalPath();
			int isolationLevel = getTransactionIsolation();
			logger.info(String.format("%s: autocommit = %s, transaction isolation = %s, %s, %s",
						method,
						getAutoCommit(),
						getTransactionIsolationName(isolationLevel),
						openSessionMethodName,
						connFile
						));
						
		} catch (Exception ex) {
			throw new Exception(String.format("%s: %s", method,
					ex.toString()));
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private void initConfiguration() throws Exception{
		String method = getMethodName("initConfiguration");
		
		logger.debug(String.format("%s",method));
		
		configuration = new Configuration();
		setConfigurationClassMapping();
		setDefaultConfigurationProperties();
		configure();
		setConfigurationProperties();
		
		logConfigurationProperties();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private void configure() throws Exception{
		String method = getMethodName("configure");
				
		if(configFile == null){
			logger.debug(String.format("%s: configure connection without the hibernate file",method));
			
			configuration.configure();
		}
		else{
			logger.debug(String.format(
					"%s: configure connection with hibernate file = %s", method,
					configFile.getCanonicalPath()));
			
			configuration.configure(configFile);
		}
	}
	
	/**
	 * 
	 */
	private void initSessionProperties(){
		sessionFlushMode = FlushMode.ALWAYS;
		useOpenStatelessSession = false;
		useGetCurrentSession = false;
	}
	
	/**
	 * 
	 */
	private void openSession(){
		String method = getMethodName("openSession");
		
		logger.debug(String.format("%s: useOpenStatelessSession = %s, useGetCurrentSession = %s",
				method,
				useOpenStatelessSession,
				useGetCurrentSession));
		
		openSessionMethodName = "";
		if(useOpenStatelessSession){
			currentSession = sessionFactory.openStatelessSession(jdbcConnection);
			openSessionMethodName = "openStatelessSession";
		}
		else{
			Session session = null;
			if(useGetCurrentSession){
				session = sessionFactory.getCurrentSession();
				openSessionMethodName = "getCurrentSession";
			}
			else{
				session = sessionFactory.openSession(jdbcConnection);
				openSessionMethodName = "openSession";
			}
			if(sessionFlushMode != null){
				session.setFlushMode(sessionFlushMode);
			}
			currentSession = session;
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	private void initSessionFactory() throws Exception{
		String method = getMethodName("initSessionFactory");
		logger.debug(String.format("%s",method));
		
		if(currentSession != null){
			disconnect();
		}
		sessionFactory = configuration.buildSessionFactory();
		SessionFactoryImpl sf = (SessionFactoryImpl)sessionFactory;
		
		try{
			dialect = sf.getDialect();
		}
		catch(Exception ex){
			throw new Exception(String.format("%s: cannot get dialect : %s",method,ex.toString()));
		}
		try{
			jdbcConnection = sf.getConnectionProvider().getConnection();
		}
		catch(Exception ex){
			throw new Exception(String.format("%s: cannot get jdbcConnection : %s",method,ex.toString()));
		}
		setDbms();
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean getConfiguredAutoCommit() throws Exception{
		if(configuration == null){
			throw new Exception("configuration is NULL");
		}
		String p = configuration.getProperty(HIBERNATE_PROPERTY_AUTO_COMMIT);
		if(SOSString.isEmpty(p)){
			throw new Exception(String.format("\"%s\" property is not configured ",HIBERNATE_PROPERTY_AUTO_COMMIT));
		}
		return Boolean.parseBoolean(p);
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public int getConfiguredTransactionIsolation() throws Exception{
		if(configuration == null){
			throw new Exception("configuration is NULL");
		}
		String p = configuration.getProperty(HIBERNATE_PROPERTY_TRANSACTION_ISOLATION);
		if(SOSString.isEmpty(p)){
			throw new Exception(String.format("\"%s\" property is not configured ",HIBERNATE_PROPERTY_TRANSACTION_ISOLATION));
		}
		return Integer.parseInt(p);
	}
	
	/**
	 * 
	 * @param commit
	 */
	public void setAutoCommit(boolean commit){
		configurationProperties.put(HIBERNATE_PROPERTY_AUTO_COMMIT,String.valueOf(commit));
	}
	
	/**
	 * 
	 * @param level
	 */
	public void setTransactionIsolation(int level){
		configurationProperties.put(HIBERNATE_PROPERTY_TRANSACTION_ISOLATION,String.valueOf(level));
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean getAutoCommit() throws Exception{
		if(jdbcConnection == null){
			throw new Exception("jdbcConnection is NULL");
		}
		return jdbcConnection.getAutoCommit();
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public int getTransactionIsolation() throws Exception{
		if(jdbcConnection == null){
			throw new Exception("jdbcConnection is NULL");
		}
		return jdbcConnection.getTransactionIsolation();
	}
	
	/**
	 * 
	 * @param isolationLevel
	 * @return
	 * @throws Exception
	 */
	public static String getTransactionIsolationName(int isolationLevel) throws Exception{
		
		switch(isolationLevel) {
			case Connection.TRANSACTION_NONE :
		                 return "TRANSACTION_NONE";
		                 
			case Connection.TRANSACTION_READ_UNCOMMITTED :
		                 return "TRANSACTION_READ_UNCOMMITTED";
		                 
			case Connection.TRANSACTION_READ_COMMITTED : 
		                 return "TRANSACTION_READ_COMMITTED";
		                 
			case Connection.TRANSACTION_REPEATABLE_READ :
		                return "TRANSACTION_REPEATABLE_READ";
		                
			case Connection.TRANSACTION_SERIALIZABLE :
		                 return "TRANSACTION_SERIALIZABLE";
		                 
			default :
		                 throw new Exception(String.format("Invalid transaction isolation level = %s.",isolationLevel));
		}
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	private void setDbms(){
		dbms = DBMS.UNKNOWN;
		
		setDbmsFromJdbcConnection();
		if(dbms.equals(DBMS.UNKNOWN)){
			setDbmsFromDialect();
		}
		setDbmsDefaultFetchSize();
	}
	
	/**
	 * 
	 */
	private void setDbmsDefaultFetchSize(){
		defaultFetchSize = 1;
		if(dbms.equals(DBMS.MYSQL)){
			defaultFetchSize = Integer.MIN_VALUE;
		}
	}
	
	/**
	 * 
	 */
	private void setDbmsFromDialect(){
		if(dialect != null){
			String dialectClassName = dialect.getClass().getSimpleName().toLowerCase();
			if(dialectClassName.contains("db2")){
				dbms = DBMS.DB2;
			}
			else if(dialectClassName.contains("firebird")){
				dbms = DBMS.FBSQL;
			}
			else if(dialectClassName.contains("sqlserver")){
				dbms = DBMS.MSSQL;
			}
			else if(dialectClassName.contains("mysql")){
				dbms = DBMS.MYSQL;
			}
			else if(dialectClassName.contains("oracle")){
				dbms = DBMS.ORACLE;
			}
			else if(dialectClassName.contains("postgre")){
				dbms = DBMS.PGSQL;
			}
			else if(dialectClassName.contains("sybase")){
				dbms = DBMS.SYBASE;
			}
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private void setDbmsFromJdbcConnection(){
		String method = "setDbmsFromJdbcConnection";
		try{
			if(jdbcConnection != null){
				String pn = jdbcConnection.getMetaData().getDatabaseProductName();
				if(pn != null){
					pn = pn.toLowerCase();
					
					if(pn.contains("db2")){
						dbms = DBMS.DB2;
					}
					else if(pn.contains("firebird")){
						dbms = DBMS.FBSQL;
					}
					else if(pn.contains("sql server")){
						dbms = DBMS.MSSQL;
					}
					else if(pn.contains("mysql")){
						dbms = DBMS.MYSQL;
					}
					else if(pn.contains("oracle")){
						dbms = DBMS.ORACLE;
					}
					else if(pn.contains("postgre")){
						dbms = DBMS.PGSQL;
					}
					else if(pn.contains("ase")){
						dbms = DBMS.SYBASE;
					}
				}
			}
		}
		catch(Exception ex){
			logger.warn(String.format("%s: cannot set dbms. %s",method,ex.toString()));
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public Enum<SOSHibernateConnection.DBMS> getDbms(){
		return dbms;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Dialect getDialect(){
		return dialect;
	}
	
	/**
	 * 
	 */
	public void disconnect() {
		String method = getMethodName("disconnect");
		logger.info(String.format("%s",method));
		
		closeTransaction();
		closeSession();
		closeSessionFactory();
		closeJdbcConnection();
		dialect = null;
	}
	
	/**
	 * 
	 */
	private void closeJdbcConnection(){
		String method = getMethodName("closeJdbcConnection");
		logger.debug(String.format("%s",method));
		
		if(jdbcConnection != null){
			try{
				jdbcConnection.close();
			}
			catch(Exception ex){}
		}
		jdbcConnection = null;
	}
	
	/**
	 * 
	 */
	public void clearSession() throws Exception{
		String method = getMethodName("clearSession");
		logger.debug(String.format("%s",method));
		
		if(currentSession == null){
			throw new Exception(String.format("currentSession is NULL"));
		}
		
		if(currentSession instanceof Session){
			Session session = (Session)currentSession;
			session.clear();
		}
	}
	
	/**
	 * 
	 * @param work
	 * @throws Exception
	 */
	public void sessionDoWork(Work work) throws Exception{
		String method = getMethodName("sessionDoWork");
		logger.debug(String.format("%s",method));
		
		if(currentSession == null){
			throw new Exception(String.format("currentSession is NULL"));
		}
				
		if(currentSession instanceof Session){
			Session session = (Session)currentSession;
			session.doWork(work);
		}
		else{
			logger.warn(String.format("%s: this method will be ignored for current openSessionMethodName : %s (%s)",
					method,
					openSessionMethodName,
					currentSession.getClass().getSimpleName()
					));
		}
	}
	
	
	/**
	 * 
	 */
	private void closeSession(){
		String method = getMethodName("closeSession");
		logger.debug(String.format("%s",method));
		
		if(currentSession != null){
			if(currentSession instanceof Session){
				if(!useGetCurrentSession){
					Session session = (Session)currentSession;
					if(session.isOpen()){
						session.close();
					}
				}
			}
			else if(currentSession instanceof StatelessSession){
				StatelessSession session = (StatelessSession)currentSession;
				session.close();
			}
		}
		currentSession = null;
	}
	
	/**
	 * 
	 */
	private void closeSessionFactory(){
		String method = getMethodName("closeSessionFactory");
		logger.debug(String.format("%s",method));
		
		if(sessionFactory != null && !sessionFactory.isClosed()){
			sessionFactory.close();
		}
		sessionFactory = null;
	}
	
	/**
	 * 
	 */
	private void closeTransaction(){
		
		String method = getMethodName("closeTransaction");
		logger.debug(String.format("%s",method));
		
		try{
			if(currentSession != null){
				Transaction tr = null;
				if(currentSession instanceof Session){
					Session session = (Session)currentSession;
					tr = session.getTransaction();
				}
				else if(currentSession instanceof StatelessSession){
					StatelessSession session = (StatelessSession)currentSession;
					tr = session.getTransaction();
				}
				
				if(tr != null){
					//@TODO Funktionalität prüfen. Abhängig von autocommit ???
					//if(!tr.wasCommitted() && !tr.wasRolledBack()){
						tr.rollback();
					//}
				}
			}
		}
		catch(Exception ex){}
		
	}
	
	/**
	 * 
	 */
	private void initClassMapping(){
		classMapping = new ClassList();
	}
	
	/**
	 * 
	 */
	private void initConfigurationProperties(){
		defaultConfigurationProperties = new Properties();
		
		defaultConfigurationProperties.put(HIBERNATE_PROPERTY_TRANSACTION_ISOLATION,String.valueOf(Connection.TRANSACTION_READ_COMMITTED));
		defaultConfigurationProperties.put(HIBERNATE_PROPERTY_AUTO_COMMIT,"false");
		defaultConfigurationProperties.put(HIBERNATE_PROPERTY_USE_SCROLLABLE_RESULTSET,"true");
		defaultConfigurationProperties.put(HIBERNATE_PROPERTY_CURRENT_SESSION_CONTEXT_CLASS,"jta");
		
		configurationProperties = new Properties();
	}
	
	/**
	 * 
	 * @param hibernateConfigFile
	 * @throws Exception
	 */
	private void initConfigFile(String hibernateConfigFile) throws Exception{
		File file = null;
		if(hibernateConfigFile != null){
			file = new File(hibernateConfigFile);
			if (!file.exists()) {
				throw new Exception(String.format(
						"hibernate config file not found: %s",
						file.getCanonicalPath()));
			}
		}
		configFile = file;
	}
	
	/**
	 * 
	 */
	private void logConfigurationProperties(){
		String method = getMethodName("logConfigurationProperties");
		
		for (Map.Entry<?,?> entry: configuration.getProperties().entrySet()) {
			String key = (String)entry.getKey();
			String value = (String)entry.getValue();
			logger.debug(String.format(
					"%s: property: %s = %s", method,
					key,value));
		}
	}
	
	/**
	 * 
	 */
	private void setConfigurationClassMapping(){
		String method = getMethodName("setConfigurationClassMapping");
		
		if(classMapping != null){
			for(Class<?> c : classMapping.getClasses()) {
				configuration.addAnnotatedClass(c);
				
				logger.debug(String.format(
						"%s: mapping. class = %s", method,
						c.getCanonicalName()));
        	}
		}
	}
	
	/**
	 * 
	 */
	private void setDefaultConfigurationProperties(){
		String method = getMethodName("setDefaultConfigurationProperties");
		if(useDefaultConfigurationProperties && defaultConfigurationProperties != null){
			for (Map.Entry<?,?> entry: defaultConfigurationProperties.entrySet()) {
				String key = (String)entry.getKey();
				String value = (String)entry.getValue();
				
			    configuration.setProperty(key,value);
			    
			    logger.debug(String.format(
						"%s: default properties. property: %s = %s", method,
						key,value));
			}
		}
	}
	
	
	/**
	 * 
	 */
	private void setConfigurationProperties(){
		String method = getMethodName("setConfigurationProperties");
		if(configurationProperties != null){
			for (Map.Entry<?,?> entry: configurationProperties.entrySet()) {
				String key = (String)entry.getKey();
				String value = (String)entry.getValue();
				
				configuration.setProperty(key,value);
			    
			    logger.debug(String.format(
						"%s: custom properties. property: %s = %s", method,
						key,value));
			}
		}
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public Query createQuery(String query) throws Exception{
		String method = getMethodName("createQuery");
		logger.debug(String.format("%s: query = %s",method,query));
		
		if(currentSession == null){
			throw new Exception(String.format("currentSession is NULL"));
		}
		
		Query q = null;
		if(currentSession instanceof Session){
			q = ((Session)currentSession).createQuery(query);
		}
		else if(currentSession instanceof StatelessSession){
			q = ((StatelessSession)currentSession).createQuery(query);
		}
		
		if(q != null){
			q.setFetchSize(defaultFetchSize);
		}
		
		return q;
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public SQLQuery createSQLQuery(String query) throws Exception{
		return createSQLQuery(query,null);
	}
	
	/**
	 * 
	 * @param query
	 * @param entityClass
	 * @return
	 * @throws Exception
	 */
	public SQLQuery createSQLQuery(String query,Class<?> entityClass) throws Exception{
		String method = getMethodName("createSQLQuery");
		logger.debug(String.format("%s: query = %s",method,query));
		
		if(currentSession == null){
			throw new Exception(String.format("currentSession is NULL"));
		}
		
		SQLQuery q = null;
		if(currentSession instanceof Session){
			q = ((Session)currentSession).createSQLQuery(query);
		}
		else if(currentSession instanceof StatelessSession){
			q = ((StatelessSession)currentSession).createSQLQuery(query);
		}
		
		if(q != null){
			if(entityClass != null){
				q.addEntity(entityClass);
			}
			q.setFetchSize(defaultFetchSize);
		}
		
		return q;
	}
	
	
	
	/**
	 * 
	 * @param cl
	 * @param alias
	 * @return
	 * @throws Exception
	 */
	public Criteria createCriteria(Class<?> cl,String alias) throws Exception{
		String method = getMethodName("createCriteria");
		logger.debug(String.format("%s: class = %s",method,cl.getSimpleName()));
		
		if(currentSession == null){
			throw new Exception(String.format("currentSession is NULL"));
		}
		
		Criteria cr = null;
		if(currentSession instanceof Session){
			cr = ((Session)currentSession).createCriteria(cl,alias);
		}
		else if(currentSession instanceof StatelessSession){
			cr = ((StatelessSession)currentSession).createCriteria(cl,alias);
		}
		
		if(cr != null){ 
			cr.setFetchSize(defaultFetchSize);
		}
		return cr;
	}
	
	/**
	 * 
	 * @param cl
	 * @return
	 * @throws Exception
	 */
	public Criteria createCriteria(Class<?> cl) throws Exception{
		return createCriteria(cl,(String)null);
	}
	
	/**
	 * 
	 * @param cl
	 * @param selectProperties
	 * @return
	 * @throws Exception
	 */
	public  Criteria createCriteria(Class<?> cl, String[] selectProperties) throws Exception{
		return createCriteria(cl,selectProperties,null);
	}
	
	/**
	 * 
	 * @param cl
	 * @param selectProperties
	 * @param transformer
	 * @return
	 * @throws Exception
	 */
	public  Criteria createCriteria(Class<?> cl, String[] selectProperties, ResultTransformer transformer) throws Exception{
		
		Criteria cr = createCriteria(cl);
		if(cr == null){
			throw new Exception("Criteria is NULL");
		}
		
		if(selectProperties != null){
			ProjectionList pl = Projections.projectionList();
			for(String property : selectProperties ){
				pl.add(Projections.property(property),property);
			}
			cr.setProjection(pl);
		}
			
		
		if(transformer != null){
			cr.setResultTransformer(transformer);
		}
		return cr;
	}
	

	
	/**
	 * 
	 * @param cl
	 * @param selectProperty
	 * @return
	 * @throws Exception
	 */
	public Criteria createSingleListTransform2BeanCriteria(Class<?> cl, String selectProperty) throws Exception{
		return createSingleListCriteria(cl, selectProperty,Transformers.aliasToBean(cl));
	}
		
	/**
	 * 
	 * @param cl
	 * @param selectProperty
	 * @return
	 * @throws Exception
	 */
	public Criteria createSingleListCriteria(Class<?> cl, String selectProperty) throws Exception{
		return createSingleListCriteria(cl, selectProperty,null);
	}
	
	
	/**
	 * 
	 * @param cl
	 * @param selectProperty
	 * @param transformer
	 * @return
	 * @throws Exception
	 */
	public Criteria createSingleListCriteria(Class<?> cl, String selectProperty,ResultTransformer transformer) throws Exception{
	return createCriteria(cl,new String[]{selectProperty},transformer);
	}
	
	/**
	 * 
	 * @param cl
	 * @return
	 * @throws Exception
	 */
	public  Criteria createTransform2BeanCriteria(Class<?> cl) throws Exception{
		return createCriteria(cl,null,null);
	}
	
		
	/**
	 * 
	 * @param cl
	 * @param selectProperties
	 * @return
	 * @throws Exception
	 */
	public  Criteria createTransform2BeanCriteria(Class<?> cl, String[] selectProperties) throws Exception{
		return createCriteria(cl,selectProperties,Transformers.aliasToBean(cl));
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void beginTransaction() throws Exception{
		if(ignoreAutoCommitTransactions && this.getAutoCommit()){ return;}
		
		String method = getMethodName("beginTransaction");
		logger.debug(String.format("%s",method));
		
		if(currentSession == null){
			throw new Exception(String.format("currentSession is NULL"));
		}
				
		if(currentSession instanceof Session){
			Session session = ((Session)currentSession);
			session.beginTransaction();
		}
		else if(currentSession instanceof StatelessSession){
			StatelessSession session = ((StatelessSession)currentSession);
			session.beginTransaction();
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void commit() throws Exception{
		if(ignoreAutoCommitTransactions && this.getAutoCommit()){ return;}
		
		String method = getMethodName("commit");
		
		if(currentSession == null){
			throw new Exception(String.format("currentSession is NULL"));
		}
		
		if(currentSession instanceof Session){
			logger.debug(String.format("%s",method));
			
			Session session = ((Session)currentSession);
			Transaction tr = session.getTransaction();
			if(tr == null){
				throw new Exception(String.format("session transaction is NULL"));
			}
			session.flush();
			tr.commit();
		}
		else if(currentSession instanceof StatelessSession){
			logger.debug(String.format("%s",method));
			
			StatelessSession session = ((StatelessSession)currentSession);
			Transaction tr = session.getTransaction();
			if(tr == null){
				throw new Exception(String.format("stateless session transaction is NULL"));
			}
			tr.commit();
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void rollback() throws Exception {
		if(ignoreAutoCommitTransactions && this.getAutoCommit()){ return;}
		
		String method = getMethodName("rollback");
		
		if(currentSession == null){
			throw new Exception(String.format("currentSession is NULL"));
		}
		
		if(currentSession instanceof Session){
			logger.debug(String.format("%s",method));
			
			Session session = ((Session)currentSession);
			Transaction tr = session.getTransaction();
			if(tr == null){
				throw new Exception(String.format("session transaction is NULL"));
			}
			tr.rollback();
		}
		else if(currentSession instanceof StatelessSession){
			logger.debug(String.format("%s",method));
			
			StatelessSession session = ((StatelessSession)currentSession);
			Transaction tr = session.getTransaction();
			if(tr == null){
				throw new Exception(String.format("stateless session transaction is NULL"));
			}
			tr.rollback();
		}
	}

	/**
	 * 
	 * @param item
	 * @throws Exception
	 */
	public void save(Object item) throws Exception{
		String method = getMethodName("save");
		logger.debug(String.format("%s: item = %s",method,item));
		
		if(currentSession == null){
			throw new Exception(String.format("currentSession is NULL"));
		}
		
		if(currentSession instanceof Session){
			Session session = ((Session)currentSession);
			session.save(item);
			session.flush();
		}
		else if(currentSession instanceof StatelessSession){
			StatelessSession session = ((StatelessSession)currentSession);
			session.insert(item);
		}
	}

	/**
	 * 
	 * @param item
	 * @throws Exception
	 */
	public void update(Object item) throws Exception {
		String method = getMethodName("update");
		logger.debug(String.format("%s: item = %s",method,item));
		
		if(currentSession == null){
			throw new Exception(String.format("currentSession is NULL"));
		}
		
		if(currentSession instanceof Session){
			Session session = ((Session)currentSession);
			session.update(item);
			session.flush();
		}
		else if(currentSession instanceof StatelessSession){
			StatelessSession session = ((StatelessSession)currentSession);
			session.update(item);
		}
	}

	/**
	 * @TODO bei StatelessSession
	 * - Exception ?
	 * - delete, insert?
	 * - select, dann insert oder update?
	 * 
	 * @param item
	 * @return
	 * @throws Exception
	 */
	public Object saveOrUpdate(Object item) throws Exception{
		String method = getMethodName("saveOrUpdate");
		logger.debug(String.format("%s: item = %s",method,item));
		
		if(currentSession == null){
			throw new Exception(String.format("currentSession is NULL"));
		}
		
		if(currentSession instanceof Session){
			Session session = ((Session)currentSession);
			session.saveOrUpdate(item);
			session.flush();
		}
		else if(currentSession instanceof StatelessSession){
			//StatelessSession session = ((StatelessSession)currentSession);
			//session.delete(item);
			//session.insert(item);
			throw new Exception(String.format("saveOrUpdate method is not allowed for this session instance: %s",
					currentSession.toString()));
		}
		return item;
	}

	/**
	 * 
	 * @param item
	 * @throws Exception
	 */
	public void delete(Object item) throws Exception {
		String method = getMethodName("delete");
		logger.debug(String.format("%s: item = %s",method,item));
		
		if(currentSession == null){
			throw new Exception(String.format("currentSession is NULL"));
		}
		
		if(currentSession instanceof Session){
			Session session = ((Session)currentSession);
			session.delete(item);
			session.flush();
		}
		else if(currentSession instanceof StatelessSession){
			StatelessSession session = ((StatelessSession)currentSession);
			session.delete(item);
		}
	}

		
	/**
	 * 
	 * @param properties
	 */
	public void setConfigurationProperties(Properties properties){
		if(configurationProperties.size() == 0){
			configurationProperties = properties;
		}
		else{
			if(properties != null){
				for (Map.Entry<?,?> entry: properties.entrySet()) {
					String key = (String)entry.getKey();
					String value = (String)entry.getValue();
					configurationProperties.setProperty(key,value);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param list
	 */
	public void addClassMapping(ClassList list){
		for(Class<?> c : list.getClasses()) {
			classMapping.add(c);
    	}
	}

	
	public File getConfigFile() {
		return configFile;
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Object getCurrentSession() throws Exception {
		return currentSession;
	}
	
	public boolean isUseDefaultConfigurationProperties() {
		return useDefaultConfigurationProperties;
	}

	public void setUseDefaultConfigurationProperties(
			boolean val) {
		useDefaultConfigurationProperties = val;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public Properties getConfigurationProperties() {
		return configurationProperties;
	}

	public ClassList getClassMapping() {
		return classMapping;
	}

	public Properties getDefaultConfigurationProperties() {
		return defaultConfigurationProperties;
	}

	/**
	 * 
	 * @return
	 */
	public Connection getJdbcConnection(){
	 return jdbcConnection;	
	}
	
	/**
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public PreparedStatement jdbcConnectionPrepareStatement(String sql) throws Exception{
		if(jdbcConnection == null){
			throw new Exception("jdbcConnection is NULL");
		}
		return jdbcConnection.prepareStatement(sql);
	} 
	
		
	public FlushMode getSessionFlushMode() {
		return sessionFlushMode;
	}

	public void setSessionFlushMode(FlushMode sessionFlushMode) {
		this.sessionFlushMode = sessionFlushMode;
	}
	
	private String getMethodName(String name){
		String prefix = connectionIdentifier == null ? "" : String.format("[%s] ",connectionIdentifier);
		return String.format("%s%s",prefix,name);
	}
	
	/**
	 * 
	 * @param criteria
	 * @return
	 * @throws Exception
	 */
	public String getSqlStringFromCriteria(Criteria criteria) throws Exception{
		CriteriaImpl criteriaImpl = (CriteriaImpl)criteria;
		SessionImplementor session = criteriaImpl.getSession();
		SessionFactoryImplementor factory = session.getFactory();
		CriteriaQueryTranslator translator = new CriteriaQueryTranslator(factory,criteriaImpl,criteriaImpl.getEntityOrClassName(),CriteriaQueryTranslator.ROOT_SQL_ALIAS);
		String[] implementors = factory.getImplementors( criteriaImpl.getEntityOrClassName() );

		CriteriaJoinWalker walker = new CriteriaJoinWalker((OuterJoinLoadable)factory.getEntityPersister(implementors[0]), 
		                        translator,
		                        factory, 
		                        criteriaImpl, 
		                        criteriaImpl.getEntityOrClassName(), 
		                        session.getLoadQueryInfluencers());

		return walker.getSQLString();
	}
	
	/**
	 * @TODO weitere Types
	 * 
	 * @param type
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public String quote(Type type,Object value) throws Exception{
		if(dialect == null){
			throw new Exception("dialect is NULL");
		}
		
		if(value == null){
			return "NULL";
		}
		
		if(type instanceof org.hibernate.type.NumericBooleanType){
			return NumericBooleanType.INSTANCE.objectToSQLString((Boolean)value,dialect);
		}
		else if(type instanceof org.hibernate.type.LongType){
			return org.hibernate.type.LongType.INSTANCE.objectToSQLString((Long)value,dialect);
		}
		else if(type instanceof org.hibernate.type.StringType){
			return StringType.INSTANCE.objectToSQLString((String)value, dialect);
		}
		else if(type instanceof org.hibernate.type.TimestampType){
			return TimestampType.INSTANCE.objectToSQLString((Date)value,dialect);
		}
		return null;
	}

	/**
	 * Feldname mit oder ohne Alias
	 * - HISTORY_ID oder oh.HISTORY_ID
	 * 
	 * @param columnName
	 * @return
	 */
	public String quoteFieldName(String columnName){
		if(dialect != null && columnName != null){
			String[] arr = columnName.split("\\.");
			if(arr.length == 1){
				columnName = dialect.openQuote()+columnName+dialect.closeQuote();
			}
			else{
				StringBuffer sb = new StringBuffer();
				String cn = arr[arr.length-1];
				for(int i=0;i<arr.length-1;i++){
					sb.append(arr[i]+".");
				}
				sb.append(dialect.openQuote()+cn+dialect.closeQuote());
				columnName = sb.toString();
			}
		}
		return columnName;
	}
	
	public boolean isUseOpenStatelessSession() {
		return useOpenStatelessSession;
	}

	public void setUseOpenStatelessSession(boolean useOpenStatelessSession) {
		this.useOpenStatelessSession = useOpenStatelessSession;
	}

	public boolean isUseGetCurrentSession() {
		return useGetCurrentSession;
	}

	public void setUseGetCurrentSession(boolean useGetCurrentSession) {
		this.useGetCurrentSession = useGetCurrentSession;
	}
	
	
	public String getConnectionIdentifier() {
		return connectionIdentifier;
	}

	public void setConnectionIdentifier(String val) {
		connectionIdentifier = val;
	}
		
	public boolean isIgnoreAutoCommitTransactions() {
		return ignoreAutoCommitTransactions;
	}

	public void setIgnoreAutoCommitTransactions(
			boolean val) {
		ignoreAutoCommitTransactions = val;
	}
	
	public int getDefaultFetchSize() {
		return defaultFetchSize;
	}

	public SessionFactory getSessionFactory(){
		return sessionFactory;
	}

}
