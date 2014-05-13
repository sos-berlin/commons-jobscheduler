package sos.scheduler.launcher;


import java.util.HashMap;

import sos.scheduler.launcher.JobSchedulerLoadTestLauncher;
import sos.spooler.Job_impl;
import sos.spooler.Variable_set;
import sos.util.SOSClassUtil;
import sos.util.SOSSchedulerLogger;
import sos.util.SOSString;


/**
 * 
 * Klasse JobSchedulerLoadTestLauncherJob
 * Diese Job kann die Ausführung paralleler Jobs skalieren und beliebig viele Jobs können parallel laufen.
 * Der Job kann beliebig viele individuelle Jobs startet und kann mit dem Namen eines anderen Jobs oder
 * Auftrags parametrisiert werden, die gestartet werden soll.
 * Die Anzahl zu startender Jobs in konfigurierbaren Zeitabständen sowie die Erhöhung der Anzahl zu startenden Jobs beim Erreichen
 * jedes Intervalls sind konfigurierbar.
 *
 *
 * @author Mürüvet Öksüz
 * @version 1.0
 * 
 * email: mueruevet.oeksuez@sos-berlin.com
 * 
 * resourcen: * sos.mail.jar, sos.util.jar, sos.xml.jar, xercesImpl.jar, xml-apis.jar, xalan.jar
 * 
 *  ********************************************************************************************
 *  
 *   * 2. Parameter
 *
 * 2.1 Allgemeine Parameter für den Launcher
 *
 * a) <param name="scheduler_launcher_host" value="localhost"/>
 *
 * Der Host des Job Schedulers, der den Job ausführt, Default: localhost
 *
 * b) <param name="scheduler_launcher_port" value="4444"/>
 *
 * Der Port des Job Schedulers, der den Job ausführt, Default: 4444
 *
 * c) <param name="scheduler_launcher_protocol" value="tcp|udp"/>
 *
 * Das Protokoll zum Versenden via JobSchedulerCommand, Default: tcp
 *
 * c) <param name="scheduler_launcher_min_starts" value="5"/>
 *
 * Die minimale Anzahl von Jobs oder Aufträgen, die gleichzeitig gestartet wird. Default=1
 *
 * d) <param name="scheduler_launcher_max_starts" value="100"/>
 *
 * Die maximale Anzahl von Jobs oder Aufträgen, die gleichzeitig gestartet werden.
 *
 *  e) <param name="scheduler_launcher_start_increment" value="+10|*2"/>
 *
 *  Die Anzahl zu startender Job oder Aufträge wird pro Start um diese Zahl erhöht. Ein Wert 3 ist gleichbedeutend mit +3, d.h. die Zahl wird um 3 erhöht.
 *  Ein Wert *2 bedeutet, dass sich die Anzahl verdoppelt. Das Inkrement gilt nicht beim ersten Start. Die mit dem Parameter max_starts gesetzte Anzahl darf nicht überschritten werden. Wird sie überschritten, dann werden alle weiteren Starts mit dem Wert von max_starts ausgeführt.
 *
 *  f) alle Parameter, deren Namen nicht mit scheduler_launcher_ beginnen, werden an den Job oder Auftrag durchgereicht.
 *
 *  2.2 Spezielle Parameter für Jobs, siehe Beispiel
 *  <param name="scheduler_launcher_job" value="job_name"/>
 *  Wenn dieser Parameter übergeben wurde, dann handelt es sich um einen Job. Der hier übergebene 
 *  Job-Name ist der Wert des Attributs job= in <start_job job="..."/>
 *
 *  In diesem Fall werden ausgewertet:
 *
 *  a) mandatory
 *  keine
 *
 *  b) optional, d.h. bitte keine eigenen Defaults, sondern im Fall des Fehlens einfach nicht übergeben
 *  <param name="scheduler_launcher_job_after" value="..."/>
 *  <param name="scheduler_launcher_job_at" value="..."/>
 *  <param name="scheduler_launcher_job_web_service" value="..."/>
 *
 *  2.3 Spezielle Parameter für Aufträge, siehe Beispiel
 *  <param name="scheduler_launcher_order" value="order_id"/>
 *  Wenn dieser Parameter übergeben wurde, dann handelt es sich um einen Auftrag. Die hier übergebene Auftragskennung ist der Wert des Attributs id= in <add_order id="..."/>
 *
 *  In diesem Fall werden ausgewertet:
 *
 *  a) mandatory
 *  <param name="scheduler_launcher_order_job_chain" value="..."/>
 *
 *  b) optional, d.h. bitte keine eigenen Defaults, sondern im Fall des Fehlens einfach nicht übergeben
 *  <param name="scheduler_launcher_order_replace" value="yes|no"/>
 *  <param name="scheduler_launcher_order_state" value="..."/>
 *  <param name="scheduler_launcher_order_title" value="..."/>
 *  <param name="scheduler_launcher_order_at" value="..."/>
 *  <param name="scheduler_launcher_order_priority" value="..."/>
 *  <param name="scheduler_launcher_order_web_service" value="..."/>
 *  <param name="scheduler_launcher_interval" value="..."/>
 *  
 *
 *  2.4 Alle anderen Parameter werden durchgereicht. Genauer: alle Parameter, die nicht mit
 *  scheduler_launcher_ beginnen, werden den Jobs übergeben, die gestartet werden sollen.
 * 
 */

public class JobSchedulerLoadTestLauncherJob extends Job_impl {
	
	 private SOSString sosString          = null;
	 private SOSSchedulerLogger sosLogger = null;

     public boolean spooler_init() {
	        
	        try {
                sosString = new SOSString();	            
	            //this.getParameters();
	            sosLogger = new SOSSchedulerLogger(spooler_log);	           	            
	            return true;
	            
	        } catch (Exception e) {
	            spooler_log.error("error occurred initializing job: " + e.getMessage());
	            return false;
	        }
	    }
	    
	    
	    
	    /**
	     * Job Scheduler API implementation.
	     * 
	     * Method is executed once per job or repeatedly per order.
	     * 
	     * @return boolean
	     */
	    public boolean spooler_process() {
	    	        
	        Variable_set parameters = null;
	        HashMap allParams       = null;
	        JobSchedulerLoadTestLauncher launcher = null;
	        try {
	        	sosLogger = new SOSSchedulerLogger(spooler_log);
	        	sosLogger.debug3(".. calling " + SOSClassUtil.getMethodName());
	            parameters = spooler_task.params();
	            
	            if (spooler_job.order_queue() != null) {
	                parameters.merge(spooler_task.order().params());	                         
	            }
	            
	            allParams = this.getParameters();	      
	            
	            launcher = new JobSchedulerLoadTestLauncher(sosLogger);	            
	            launcher.setParameters(allParams);
	            launcher.process();
	            
	            spooler_job.set_state_text(launcher.getStateText()); 
	            
	            return (spooler_job.order_queue() != null);
	            
	        } catch (Exception e) {
	            spooler_log.error("error occurred in execution: " + e.getMessage());
	            return false;
	        }
	        
	    }

	    /**
	     * Liest alle Job-Parameter aus und schreibt diese in ein HahpMap Objekt.
	     * 
	     * @return
	     * @throws Exception
	     */
	  	public HashMap getParameters() throws Exception {
	  		Variable_set parameters = null;
	  		String[] names = null;
	  		HashMap allParam = new HashMap();
	        try { 	        	
	        	sosLogger.debug3(".. calling " + SOSClassUtil.getMethodName());
	        	parameters = spooler_task.params();
	        	if (parameters.count() > 0) {
	        		names = parameters.names().split(";");
	        		for(int i = 0; i < parameters.count(); i++) {
	        			if (sosString.parseToString(parameters.var(names[i])).length() > 0) {
	        				allParam.put(names[i], parameters.var(names[i]));
	        			}
	        		}	
	        	}
	        	 return allParam;	        		        				        		          
	            
	        } catch (Exception e) {
	            throw new Exception("error occurred processing task parameters: " + e.getMessage());
	        }
	    }	    	    	    

}
