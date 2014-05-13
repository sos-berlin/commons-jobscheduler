/*
 * ManagedJobChainExport.java
 * Created on 14.10.2005
 * 
 */
package sos.scheduler.managed;

import sos.connection.SOSConnection;
import sos.util.SOSArguments;
import sos.marshalling.SOSExport;
import sos.util.SOSStandardLogger;

/**
 * This class exports managed job chains to xml
 *
 * @author Andreas Liebert 
 */
public class ManagedJobChainExport {

	private static SOSConnection conn;

	private static SOSStandardLogger sosLogger = null;
	
	public static void main(String[] args) {
		if(args.length==0 || args[0].equals("-?") || args[0].equals("/?") || args[0].equals("-h")){
			showUsage();
			System.exit(0);
		}
		try {
			SOSArguments arguments = new SOSArguments(args);
			
			String xmlFile="";
			String logFile="";
			int logLevel=0;
			String settingsFile="";
			String modelID="";
			String packageName="";
			try {
				xmlFile = arguments.as_string("-file=","job_export.xml");
				logLevel = arguments.as_int("-v=",SOSStandardLogger.INFO);
				logFile = arguments.as_string("-log=","");
				modelID = arguments.as_string("-jobchain=","");
				packageName = arguments.as_string("-package=","");
				settingsFile = arguments.as_string("-settings=","../config/factory.ini");
			} catch (Exception e1) {
				System.out.println(e1.getMessage());
				showUsage();
				System.exit(0);
			}
			if(packageName.length()>0 && modelID.length()>0){
				System.out.println("jobchain und package dürfen nicht zusammen angegeben werden.");
				showUsage();
				System.exit(0);
			}
			if(packageName.length()==0 && modelID.length()==0){
				System.out.println("Entweder jobchain oder package muss angegeben werden.");
				showUsage();
				System.exit(0);
			}
			if (logFile.length()>0)	sosLogger = new SOSStandardLogger(logFile, logLevel);
			else sosLogger = new SOSStandardLogger(logLevel);
			
			ManagedJobExport.setSosLogger(sosLogger);
			conn = ManagedJobExport.getDBConnection(settingsFile);			
						
			conn.connect();
			
			arguments.check_all_used();
			export(xmlFile,modelID, packageName);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.disconnect();
			} catch (Exception e) {
			}
		}
	}
	
	public static void showUsage(){
		System.out.println("usage:ManagedJobExport ");
		System.out.println("Argumente:");
		System.out.println("     -jobchain=       Name der zu kopierenden jobchain(s) (jobchain[+jobchain[+...]])");
		System.out.println("   oder");
		System.out.println("     -package=        Paketname(n) zur Gruppierung mehrerer jobchains (package[+package...])");
		System.out.println("     -v=              Loglevel (optional)");
		System.out.println("     -log=            LogDatei (optional)");
		System.out.println("     -settings=       factory.ini Datei (default:../config/factory.ini)");
		System.out.println("     -file=           Exportdatei (default:job_export.xml)");
	}
	
	private static void export(String xmlFile, String modelID, String packageName) throws Exception{
		String selManagedModel="";
		if(modelID.length()>0){
			if (modelID.indexOf("+")>0){
				String models = "('"+modelID.replaceAll("\\+","','")+"')";
				selManagedModel = "SELECT * FROM "+JobSchedulerManagedObject.getTableManagedModels()+ " WHERE \"NAME\" IN "+models;
			}else
			selManagedModel = "SELECT * FROM "+JobSchedulerManagedObject.getTableManagedModels()+ " WHERE \"NAME\"='"+modelID+"'";
		}
		if(packageName.length()>0){
			if (packageName.indexOf("+")>0){
				String packages = "('"+packageName.replaceAll("\\+","','")+"')";
				selManagedModel = "SELECT * FROM "+JobSchedulerManagedObject.getTableManagedModels()+ " WHERE \"PACKAGE\" IN "+packages;
			}else
			selManagedModel = "SELECT * FROM "+JobSchedulerManagedObject.getTableManagedModels()+ " WHERE \"PACKAGE\"='"+packageName+"'";
		}
		String selManagedJobs = "SELECT * FROM "+JobSchedulerManagedObject.getTableManagedJobs()+" WHERE \"MODEL\"=?";
		String selJobTypes = "SELECT * FROM "+JobSchedulerManagedObject.getTableManagedJobTypes()+" WHERE \"TYPE\"='?'";
		String selSettings = "SELECT * FROM "+JobSchedulerManagedObject.getTableSettings()+" WHERE \"APPLICATION\" IN ('job_type/local/?', 'job_type/global/?', 'job_type/mixed/?')";
		String selSettingsOrders = "SELECT * FROM "+JobSchedulerManagedObject.getTableSettings()+" WHERE \"APPLICATION\" IN ('order_type/local/?', 'order_type/global/?', 'order_type/mixed/?')";
		String selOrders = "SELECT * FROM "+JobSchedulerManagedObject.getTableManagedOrders()+" WHERE \"JOB_CHAIN\"='?'";
		
		//TODO: wofuer ist 3. Parameter?		
		SOSExport export = new SOSExport(conn,xmlFile, "DOCUMENT", sosLogger);
		int model = export.query(JobSchedulerManagedObject.getTableManagedModels(),"ID", selManagedModel);
		int job = export.query(JobSchedulerManagedObject.getTableManagedJobs(),"ID",selManagedJobs,"ID", model);
		int orders = export.query(JobSchedulerManagedObject.getTableManagedOrders(),"ID",selOrders,"NAME",model);
		int jobTypes = export.query(JobSchedulerManagedObject.getTableManagedJobTypes(),"TYPE", selJobTypes,"JOB_TYPE", job);
		int jobTypes2 = export.query(JobSchedulerManagedObject.getTableManagedJobTypes(),"TYPE", selJobTypes,"JOB_TYPE", orders);
		int settings = export.query(JobSchedulerManagedObject.getTableSettings(), "APPLICATION,SECTION,NAME",selSettings,"TYPE,TYPE,TYPE",jobTypes);
		int settingsOrders = export.query(JobSchedulerManagedObject.getTableSettings(), "APPLICATION,SECTION,NAME",selSettings,"TYPE,TYPE,TYPE",jobTypes2);
		
		
		
		export.doExport();
		
		
	}
}
