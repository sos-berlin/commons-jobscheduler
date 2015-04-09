package com.sos.hibernate.classes;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.engine.QueryParameters;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.loader.criteria.CriteriaJoinWalker;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Problem: mariadb Treiber unterstützt keine innere selects innerhalb eines ScrollableResultSets
 * 
 * Klasse verwendet absichtlich Statement statt PreparedStatement
 * 
 * 
 * @author Robert Ehrlich
 *
 */
public class SOSHibernateResultSetProcessor implements Serializable {
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.getLogger(SOSHibernateResultSetProcessor.class);
	
	private SOSHibernateConnection connection;
	private Statement statement;
	private ResultSet resultSet;
	
	private Class<?> entity;
	private HashMap<String,Method> entityGetMethods;
	private HashMap<String,Method> entitySetMethods;
	private String sqlStatement;
	
	public SOSHibernateResultSetProcessor(SOSHibernateConnection conn){
		connection = conn;
	}
	
	/**
	 * 
	 * @param criteria
	 * @param scrollMode
	 * @return
	 * @throws Exception
	 */
	public ResultSet createResultSet(Criteria criteria,ScrollMode scrollMode) throws Exception{
		return createResultSet(criteria, scrollMode,null);
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public ResultSet createResultSet(Criteria criteria,ScrollMode scrollMode, Long fetchSize) throws Exception{
		String method = "createResultSet";
		
		try{
			logger.debug(String.format("%s",method));
			
			CriteriaImpl criteriaImpl = (CriteriaImpl)criteria;
			entity = Class.forName(criteriaImpl.getEntityOrClassName());
			
			SessionImplementor session = criteriaImpl.getSession();
			SessionFactoryImplementor factory = session.getFactory();
			CriteriaQueryTranslator translator = new CriteriaQueryTranslator(factory,criteriaImpl,criteriaImpl.getEntityOrClassName(),CriteriaQueryTranslator.ROOT_SQL_ALIAS);
						
			String[] implementors = factory.getImplementors(criteriaImpl.getEntityOrClassName());
			CriteriaJoinWalker walker = new CriteriaJoinWalker((OuterJoinLoadable)factory.getEntityPersister(implementors[0]), 
	                translator,
	                factory, 
	                criteriaImpl, 
	                criteriaImpl.getEntityOrClassName(), 
	                session.getLoadQueryInfluencers());
			
			String sql = createSqlStatement(translator,walker.getSQLString());
			createMetadata(translator);			
					
			resultSet = createResultSet(sql,scrollMode,criteria.isReadOnly(),fetchSize);
		}
		catch(Exception ex){
			throw new Exception(String.format("%s: %s",method,ex.toString()));
		}
		
		return resultSet;
	}
	
	/**
	 * 
	 * @param sql
	 * @param scrollMode
	 * @param isReadOnly
	 * @return
	 * @throws Exception
	 */
	public ResultSet createResultSet(String sql,ScrollMode scrollMode,boolean isReadOnly) throws Exception{
	
		return createResultSet(sql, scrollMode, isReadOnly,null);
	}
	
	/**
	 * 
	 * @param sql
	 * @param scrollMode
	 * @param fetchSize
	 * @return
	 * @throws Exception
	 */
	public ResultSet createResultSet(String sql,ScrollMode scrollMode,boolean isReadOnly, Long fetchSize) throws Exception{
		String method = "createResultSet";
		
		int fSize = (fetchSize == null) ? connection.getDefaultFetchSize() : fetchSize.intValue();
		sqlStatement = sql;
		
		logger.debug(String.format("%s: sqlStatement = %s, scrollMode = %s, isReadOnly = %s, fetchSize = %s",
				method,
				sqlStatement,
				scrollMode.toString(),
				isReadOnly,
				fSize));
		
		statement = connection.getJdbcConnection().createStatement(
				getResultSetType(scrollMode),
				getConcurrencyMode(isReadOnly));
		resultSet = statement.executeQuery(sqlStatement);
		resultSet.setFetchSize(fSize);
		
		return resultSet;
	}
	
	/**
	 * 
	 * @param translator
	 * @param hibernateSqlString
	 * @return
	 * @throws Exception
	 */
	private String createSqlStatement(CriteriaQueryTranslator translator,String hibernateSqlString) throws Exception{
		String where = translator.getWhereCondition();
		
		QueryParameters qp = translator.getQueryParameters();
		Type[] types = qp.getPositionalParameterTypes();
		Object[] values = qp.getPositionalParameterValues();
		
		for(int i=0;i<values.length;i++){
			int index = where.indexOf("?");
			if(index > 0){
				String val = connection.quote(types[i],values[i]);
				where = where.replaceFirst("\\?",val);
			}
		}
		return hibernateSqlString.replace(translator.getWhereCondition(),where);
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private void createMetadata(CriteriaQueryTranslator translator) throws Exception{
		entityGetMethods = new HashMap<String,Method>();
		entitySetMethods = new HashMap<String,Method>();
		
		String[] properties = translator.getProjectedAliases();
		String[] propertiesColumnAliases = translator.getProjectedColumnAliases();
		for(int i=0; i< properties.length;i++){
			 String property = properties[i];
			 Method getter = new PropertyDescriptor(property,entity).getReadMethod();
			 Method setter = new PropertyDescriptor(property,entity).getWriteMethod();
			 
			 entityGetMethods.put(propertiesColumnAliases[i],getter);
 			 entitySetMethods.put(propertiesColumnAliases[i],setter);
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	private int getConcurrencyMode(boolean readOnly){
		return readOnly ? ResultSet.CONCUR_READ_ONLY : ResultSet.CONCUR_UPDATABLE;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	private int getResultSetType(ScrollMode scrollMode) throws Exception{
		String method = "getResultSetType";
		
		int type = 0;
		if(scrollMode.equals(ScrollMode.FORWARD_ONLY)){
			type = ResultSet.TYPE_FORWARD_ONLY;
		}
		else if(scrollMode.equals(ScrollMode.SCROLL_INSENSITIVE)){
			type = ResultSet.TYPE_SCROLL_INSENSITIVE;
		}
		else if(scrollMode.equals(ScrollMode.SCROLL_SENSITIVE)){
			type = ResultSet.TYPE_SCROLL_SENSITIVE;
		}
		else{
			throw new Exception(String.format("%s: not supported scroll mode = %s",method,scrollMode.toString()));
		}
		return type;
	}
	
	
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Object get() throws Exception{
		if(entity == null){
			throw new Exception("entity is NULL");
		}
		if(entitySetMethods == null){
			throw new Exception("entitySetMethods is NULL");
		}
		if(entityGetMethods == null){
			throw new Exception("entityGetMethods is NULL");
		}
		
		Object bean = entity.newInstance(); 
		for (Map.Entry<String, Method> setters : entitySetMethods.entrySet()) {
		    String field = setters.getKey();
		    Method setter = setters.getValue();
		    Method getter = entityGetMethods.get(field);
		    //else if(getter.getReturnType().equals(Date.class)){
		    String returnTypeName = getter.getReturnType().getSimpleName();
		    		    
		    if(returnTypeName.equalsIgnoreCase("Long")){
		    	setter.invoke(bean,resultSet.getLong(field)); 
			}
			else if(returnTypeName.equals("Timestamp")){
				setter.invoke(bean,resultSet.getTimestamp(field)); 
			}
			else if(returnTypeName.equals("Date")){
				setter.invoke(bean,resultSet.getTimestamp(field)); 
			}
			else if(returnTypeName.equalsIgnoreCase("boolean")){
				org.hibernate.annotations.Type t = getter.getAnnotation(org.hibernate.annotations.Type.class);
				boolean setted = false;
				if(t != null){
					if(t.type().equalsIgnoreCase("numeric_boolean")){
						long val = resultSet.getLong(field);
						setter.invoke(bean,val == 0 ? new Boolean(false) : new Boolean(true));
						setted = true;
					}
				}
				if(!setted){
					setter.invoke(bean,resultSet.getBoolean(field));
				}
			}
			else{
				setter.invoke(bean,resultSet.getString(field)); 
			}
		}
		return bean;
	}
	
	/**
	 * 
	 */
	private void dispose(){
		resultSet = null;
		statement = null;
		entity = null;
		entityGetMethods = null;
		entitySetMethods = null;
		sqlStatement = null;
	}
	
	/**
	 * 
	 */
	public void close(){
		if(resultSet != null){
			try{
				resultSet.close();
			}
			catch(Exception ex){}
		}
		if(statement != null){
			try{
				statement.close();
			}
			catch(Exception ex){}
		}
		dispose();
	}

	/**
	 * 
	 * @return
	 */
	public String getSqlStatement() {
		return sqlStatement;
	}
	
	
	
}
