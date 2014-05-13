/*
 * Created on 07.05.2009
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sos.scheduler.logMessage;

import java.io.File;
import java.util.HashMap;

import sos.connection.SOSConnection;
import sos.util.SOSLogger;
  
public class SchedulerMessage {
	  protected int message_id;
	  protected String scheduler_id;	
	  protected String cluster_member_id="nil"; 	
	  protected String order_id="0";
	  protected String job_chain="nil";
	  protected String job_name="nil";
	  protected int task;
	  protected String logtime;
	  protected String severity="nil";  
	  private String log;
 	  protected int status=0;
	  protected String created; 
      protected File logFile=null;
	  protected SOSConnection conn = null;
	  protected SOSLogger sosLogger=null; 
	  protected boolean isNew;
	  
	  public SchedulerMessage( final File logFile_, final SOSConnection conn_,final SOSLogger sosLogger_, final String scheduler_id_ ) {
			conn = conn_;  
	        sosLogger = sosLogger_;
	        logFile = logFile_;

			scheduler_id = scheduler_id_;
			cluster_member_id = "nil";
		  }
	  


	private String getFieldValue(final HashMap hash, final String fieldname) {
	   if (hash == null) return "";
	   if (hash.get(fieldname) != null) {
			return hash.get(fieldname).toString();
	   }else {
		   return "";
	   }
	  }
	
	private String getFieldValue(final HashMap hash, final String fieldname,final String default_value) {
	   if (hash == null) return default_value;
	   if (hash.get(fieldname) != null) {
			return hash.get(fieldname).toString();
	   }else {
		   return default_value;
	   }
	  }
	
	public SchedulerMessage( final File logFile_, final HashMap rec, final SOSConnection conn_, final SOSLogger sosLogger_ ) throws Exception {
            sosLogger = sosLogger_;
	        conn = conn_;
	        logFile = logFile_;
	        message_id = Integer.parseInt(getFieldValue(rec,"message_id","0"));
			scheduler_id = getFieldValue(rec,"scheduler_id");;
			cluster_member_id = "nil";

		    order_id = getFieldValue(rec,"order_id");
			job_chain = getFieldValue(rec,"job_chain");
			job_name = getFieldValue(rec,"job_name");
			task = Integer.parseInt(getFieldValue(rec,"task"));
			logtime = getFieldValue(rec,"logtime");
			logtime = logtime.replaceFirst("^([^\\.]+).*", "$1"); 
			severity = getFieldValue(rec,"severity");
			status = Integer.parseInt(getFieldValue(rec,"status","0"));
 			created = getFieldValue(rec,"created");

			log = conn.getClob("select \"LOG\" from SCHEDULER_MESSAGES where \"MESSAGE_ID\" = " + message_id);
 	 	  }
	
	public SchedulerMessage( final HashMap rec, final SOSConnection conn_, final SOSLogger sosLogger_ ) throws Exception {
       sosLogger = sosLogger_;
       conn = conn_;
      
       message_id = Integer.parseInt(getFieldValue(rec,"message_id","0"));
	   scheduler_id = getFieldValue(rec,"scheduler_id");;
	   cluster_member_id = "nil";

	   order_id = getFieldValue(rec,"order_id");
	   job_chain = getFieldValue(rec,"job_chain");
	   job_name = getFieldValue(rec,"job_name");
	   task = Integer.parseInt(getFieldValue(rec,"task"));
	   logtime = getFieldValue(rec,"logtime");
	   logtime = logtime.replaceFirst("^([^\\.]+).*", "$1"); 
	   severity = getFieldValue(rec,"severity");
	   status = Integer.parseInt(getFieldValue(rec,"status","0"));
	   created = getFieldValue(rec,"created");

	   log = conn.getClob("select \"LOG\" from SCHEDULER_MESSAGES where \"MESSAGE_ID\" = " + message_id);
 	  }



		 
	 
	  public SchedulerMessage(final File logFile_, final String severity_, final String log_, final String scheduler_id_, final SOSConnection conn_,final SOSLogger sosLogger_ ) {
		//2009-04-21 12:32:01.536 [ERROR]  (Job_chain /blacklist) SCHEDULER-459  Error in dependant after 'Job job1' has been loaded:
		//2009-04-21 12:32:03.083 [ERROR]  (Order put2eventlog:2) SCHEDULER-459  Error in dependant after 'Job_chain /put2eventlog' has been loaded:
		//2009-04-21 12:32:01.068 [ERROR]  (Job zeppelin/check_file_exist) LIBXML2-007  XML document does not conform to schema [Element 'settings': This element is not expected. Expected is one of ( description, lock.use, params, environment, script, process, monitor, start_when_directory_changed, delay_after_error, delay_order_after_setback ).]		  logtime = log_.replaceFirst("^([^\\.]^+).*", "$1"); 
         sosLogger = sosLogger_;
		 logFile = logFile_;
	      severity = severity_;			
		  setLog(log_);
		  conn = conn_;  
		  scheduler_id = scheduler_id_;
		  cluster_member_id = "nil";
	 	  }	  

	  public SchedulerMessage( final File logFile_, final String log_, final SOSConnection conn_,final SOSLogger sosLogger_ ) {
	         sosLogger = sosLogger_;
	         logFile = logFile_;
		     setLog(log_);
			 cluster_member_id = "nil";
      }
	  
	  public void clear() {
		    message_id = -1;
		    scheduler_id="";	
		    cluster_member_id=""; 	
		    order_id="";
		    job_chain="";
		    job_name="";
		    task=0;
		    logtime="";
		    severity="";
		    log="";
 
		    status=0;
		    created="";
		 
	  }
	  
	  public void save() throws Exception {
 	   conn.execute("update SCHEDULER_MESSAGES set \"STATUS\"=" + status +  " where " + key());
	  }
	  
	  private void setLog(final String log_) {
			//2009-04-21 12:32:01.536 [ERROR]  (Job_chain /blacklist) SCHEDULER-459  Error in dependant after 'Job job1' has been loaded:
		 log=log_;
		 
		  logtime = log_.replaceFirst("^([^\\.]+).*", "$1"); 
		  order_id = log_.replaceFirst(".*\\(Order\\s+[^:]+:([^\\)]+).*","$1");

		  if (order_id.equals(log)) {
			  order_id = log_.replaceFirst(".*started - cause: Order\\s+[^:]+:([^\\)]+).*","$1");
			  if (order_id.equals(log)) order_id = "nil";
		  }

		  
          job_chain = log_.replaceFirst(".*\\(Job_chain\\s+([^\\)]+).*","$1");
          if (job_chain.equals(log_)){
        	 job_chain = log_.replaceFirst(".*\\(Order\\s+([^:)]+).*","$1");
             if (job_chain.equals(log_)){
           	    job_chain = log_.replaceFirst(".*started - cause: Order\\s+([^:)]+).*","$1");
            	if (job_chain.equals(log))job_chain = "nil";
	 	        
             }
          }
          
          //JITL-73
//          if (!job_chain.equals(log_) && !job_chain.equals("nil")){
//        	 System.out.println("");
//          }
       
          String s = "";
          int i = 0;
	      if (task == 0 ){
		     i = getTaskFromLog();
	   	     task = i;  
	      }
	  
		  s = getCurrentJobname();
		  if (!s.equals("")){
		 	 job_name = s;  
		  }
	      if (job_name == null  || job_name.equals("")) job_name = "nil";
		 
      }
	  
 
	  
	  @Override
	public boolean equals(final Object o) {
		 SchedulerMessage m = (SchedulerMessage)o;
		  
		 return 
		 m.scheduler_id.equals(scheduler_id) &&
		 m.cluster_member_id.equals(cluster_member_id) &&
		 m.job_chain.equals(job_chain) &&
		 m.job_name.equals(job_name) &&
		 m.order_id.equals(order_id) &&
		 m.getLogFilename().equals(this.getLogFilename()) &&
		 m.logtime.equals(logtime) &&
		 m.log.equals(log);
	  }

	  private String  normalizeLog(final String s) {
		 String retVal = "";
		 retVal = s.replaceAll("&gt;",">");
		 retVal = retVal.replaceAll("&lt;","<");
		 retVal = retVal.replaceAll("'", "''"); 
		 return retVal;
	  }
	  
   private String key() {
 	 String s = 
 		"\"SCHEDULER_ID\" = '" + scheduler_id + "' and " +  	
 	    "\"CLUSTER_MEMBER_ID\" = '" + cluster_member_id + "' and " + 	
		    "\"ORDER_ID\" = '" + order_id +  "' and " +
		    "\"JOB_CHAIN\" = '" + job_chain + "' and " +
		    "\"JOB_NAME\" = '" + job_name +  "' and " +
		    "\"TASK\" = " + task +  " and " +
		    "\"LOGTIME\" = " +  "%timestamp_iso('" + logtime  + "') and " +
		    "\"SEVERITY\" = '" + severity +"'";
 	 
 	 return s;
   }
	  public void insert() throws Exception {
		String sql="";
     
		isNew = false;
		try {
		   if (status == 0) {// Hier müsste scheduler_message eigentlich einen primary Key bekommen und keine Message_id. Das sollte laut ap aber nicht gemacht werden.
			  int anzahl = 0;
			  String cnt = conn.getSingleValue("select count(*) as anzahl from SCHEDULER_MESSAGES where  " + key());
			  try{
				 anzahl = Integer.parseInt(cnt);
			  }catch (NumberFormatException e) {anzahl = 0;}
			  
			  if (anzahl == 0){
				isNew = true;
				sql = "insert into SCHEDULER_MESSAGES ";
		        sql += "(\"SCHEDULER_ID\",\"CLUSTER_MEMBER_ID\",\"LOGFILE\",\"LOGTIME\",\"SEVERITY\",\"LOG\",\"JOB_NAME\",\"TASK\",\"JOB_CHAIN\",\"ORDER_ID\",\"CNT\",\"STATUS\",\"CREATION_DATE\")";
		        sql += " values ";
		        sql += "(";                                                     
		        sql += "'"+ scheduler_id  + "','" + 
		        cluster_member_id   + "','" +
		        this.getLogFilename()   + "'," +
		        "%timestamp_iso('" + logtime  + "'),'" +  
		        severity + "','" + 
		        normalizeLog(log) + "','" + 
 		        job_name + "'," +
		        task + ",'" +
		        job_chain + "','" +
		        order_id + "'," +
		        0 + "," +
 		        status + "," +
		        "%now";
           
		        sql += ")";		
		        sosLogger.debug3(sql);
		        conn.execute(sql);
 		        sosLogger.debug3("... executed");
		  	  }else {
		  		 sosLogger.debug3("Message " + this.getLog() + " already in Database");
		  	  }
		   }
		 
		}catch (Exception e) {
		   sosLogger.debug3("Message:" + log + "ignored. Already in list --->");
		}
	  }
	 
		/**
		 * Liefert den Task 
		 */

	  	private int getTaskFromLog() {
	  	   
	  		String retVal = log.replaceFirst(".*SCHEDULER-930\\s+Task\\s+([^\\s]+)\\s+started\\s+-\\s+cause:.*","$1");
	  		int erg = 0;
		    if (retVal.equals(log)){
				   retVal = log.replaceFirst(".*\\(Task[^:]+:([^\\)]+).*","$1");
		    }
		    if (retVal.equals(log)){
		       retVal = "";
		    }
		  try {
			 erg = Integer.parseInt(retVal);
		  }catch (NumberFormatException e) {erg = 0;}
		  return erg;
	   	}
	  	
	 	/**
	     * Extrahiert den Jobname und Taskid von der Logzeile
	  	 * 
	  	 * @param logLine
	  	 * @return String
	  	 * @throws Exception
	  	 */
	  	private String getCurrentJobname() {
	  		String retVal = log.replaceFirst(".*\\(Task\\s+([^:]+).*","$1");
		    if (retVal.equals(log)){
				   retVal = log.replaceFirst(".*\\(Job\\s+([^\\)]+).*","$1");
		    }
	    
		    if (retVal.equals(log)){
		       retVal = "";
		    }
		    
		  return retVal;
	  	 }	  	
	  	

   public String transformString(final SchedulerMessage h){
	String retVal        = "";
	
 	if(h.order_id != null && !h.order_id.equals("")) {
		retVal = "[Timestamp: " + h.logtime + "] " +"["+ h.severity + "] " +
		"[Job Chain:" + h.job_chain + ", " +
    	"Job: " + h.job_name + ":" + h.task + "] " + h.log;
	} else {
		retVal = "[Timestamp: " + h.logtime + "] ["+ h.severity + "] [Job:" + h.job_name + "] " + h.log;
	}
			
	return retVal;

   }
   
   private String getLogFilename() {
	  String filename = "";
	  if (logFile != null)filename = logFile.getAbsolutePath();
	  return filename;
   }

   public int getMessage_id() {
      return message_id;
   }

   public String getScheduler_id() {
      return scheduler_id;
   }

   public String getCluster_member_id() {
      return cluster_member_id;
   }

   public String getOrder_id() {
      return order_id;
   }

   public String getJob_chain() {
      return job_chain;
   }

   public String getJob_name() {
      return job_name;
   }

   public int getTask() {
      return task;
   }

   public String getLogtime() {
      return logtime;
   }

   public String getSeverity() {
      return severity;
   }

   public String getLog() {
      return log;
   }

   public String getTransformed_log() {
      return transformString(this);
   }

   public int getStatus() {
      return status;
   }
}