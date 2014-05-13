/*
 * JobSchedulerCheckSlaves.java
 * Created on 22.11.2005
 * 
 */
package sos.scheduler.job;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.scheduler.command.SOSSchedulerCommand;
import sos.util.SOSDate;
import sos.util.SOSString;

/**
 * Dieser Job läuft auf einem Master Scheduler und überprüft den
 * Status anderer als Slaves angemeldeter Scheduler
 * Paramter:<br/>
 * <ul>
 *  <li>slave_1 erster zu überprüfender Jobscheduler (host:port)</li>
 *  <li>slave_n n-ter zu überprüfender Jobscheduler</li>
 *  <li>warn_if_not_connected Warnung verschicken wenn Scheduler nicht verbunden
 *   sind? default:true</li>
 *  <li>warn_if_not_registered Warnung verschicken wenn Scheduler nicht registriert
 *   sind? default:true</li>
 *  <li>check_jobs Jobs der verbundenen Scheduler überprüfen? default:true</li>
 *  <li>min_warning_age</li>Mindestalter eines Problems, damit der Job es meldet.
 *     Entweder Sekunden oder hh:mm[:ss]
 * </ul>
 * @author Andreas Liebert 
 */
public class JobSchedulerCheckSlaves extends JobSchedulerJob {
	
	private class SlaveScheduler{
		private String host="";
		private String ip="";
		private int port;
		
		private boolean connected;
		private String disconnectedAt="";
		
		/**
		 * @param host Scheduler host (or ip)
		 * @param port Scheduler port
		 */
		public SlaveScheduler(String host, int port) {
			super();
			if (isIP(host)) this.ip = host;
			else this.host = host;
			this.port = port;
		}

		
		public boolean isConnected() {
			return connected;
		}

		
		public void setConnected(boolean connected) {
			this.connected = connected;
		}

		/**
		 * @param host Scheduler host
		 * @param ip Scheduler ip
		 * @param port Scheduler port
		 */
		public SlaveScheduler(String host, String ip, int port) {
			super();
			this.host = host;
			this.ip = ip;
			this.port = port;
			this.connected = true;
		}

		/**
		 * @param host Scheduler host
		 * @param ip Scheduler ip
		 * @param port Scheduler port
		 */
		public SlaveScheduler(String host, String ip, int port, String disconnectedAt) {
			this(host, ip, port);
			this.connected = false;
			this.disconnectedAt = disconnectedAt;
		}
		
		   // check if server is an ip address.
		   // an ip address is assumed if server is nothing but numbers and dots 
		private boolean isIP(String strServer)
		   {
		      char ch;
		      int i;
		      boolean bIsAnIP;
		      
		      bIsAnIP = true;   // assume is an ip address   
		      
		      for (i = 0; i < strServer.length();i++)
		      {
		         ch = strServer.charAt(i);
		         if ( ((ch >= '0') && (ch <= '9')) || (ch == '.') )
		         {
		            // valid for ip
		         }
		         else
		         {
		            bIsAnIP = false;  // not an ip 
		            break;
		         }
		      }
		      
		      return bIsAnIP;
		}
		   
		public boolean equals(Object other){
			   if (other instanceof SlaveScheduler){
				   SlaveScheduler otherSlave = (SlaveScheduler) other;
				   if(otherSlave.port!=this.port) return false;
				   if(otherSlave.ip.equals(this.ip)) return true;
				   if(otherSlave.host.equals(this.host)) return true;
				   return false;
			   }
			   return super.equals(other);			   
		}
		   
		public String toString(){
			   String displayHost = this.host;
			   if (displayHost == null || displayHost.length() == 0){
				   displayHost = this.ip;
			   }
			   return displayHost + ":" + port;
		}

		
		public String getDisconnectedAt() {
			return disconnectedAt;
		}

		public void setDisconnectedAt(String disconnectedAt) {
			this.disconnectedAt = disconnectedAt;
		}
	}
	
	private Vector requestedSlaves;
	private Vector registeredSlaves;
	private Vector connectedSlaves;
	
	private DocumentBuilder docBuilder;
	
	private boolean warnIfNotConnected = true;
	private boolean warnIfNotRegistered = true;
	private boolean checkJobs = true;
	
	 /** Attribut: maxRetryConnectCount: Maximale Anzahl Versuche für Verbindungsaufbau bis Fehlerzustand erreicht ist */
    private int maxRetry			= 50; 

    /** Attribut: maxRetryConnectInterval: Zeitintervall für Wiederholungsversuche für Verbindungsaufbau wenn Fehlerzustand erreicht ist */
    private int maxRetryInterval			= 14400; // alle 4 Stunden
    
    /** Attribut: min_warning_age: Mindestalter eines Problems, damit der Job es meldet. */
    private int minWarningAge				= 120; // 2 Minuten
    
    /** Zeit des Schedulers aus der XML Antwort */
    private String schedulerDateTime="";
    
    // wurden Scheduler konfiguriert, die auf alle Fälle benötigt werden?
    private boolean hasRequests;
	
	public boolean spooler_init(){
    	boolean rc = super.spooler_init();
    	if (!rc) return false;
    	
    	try{
    		requestedSlaves = getSlaveList();
    		registeredSlaves = new Vector();
    		connectedSlaves = new Vector();
    		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();                
            docBuilder = docFactory.newDocumentBuilder();
            
            if (getJobProperties().get("warn_if_not_connected")!=null){
            	String winc = getJobProperties().get("warn_if_not_connected").toString();
            	if (winc.equalsIgnoreCase("false") || winc.equals("0"))
            		warnIfNotConnected = false;
            	spooler_log.info(".. job setting [warn_if_not_connected]: " + winc);
            }
            if (getJobProperties().get("warn_if_not_registered")!=null){
            	String winr = getJobProperties().get("warn_if_not_registered").toString();
            	if (winr.equalsIgnoreCase("false") || winr.equals("0"))
            		warnIfNotRegistered = false;
            	spooler_log.info(".. job setting [warn_if_not_registered]: " + winr);
            }
            if (getJobProperties().get("check_jobs")!=null){
            	String check = getJobProperties().get("check_jobs").toString();
            	if (check.equalsIgnoreCase("false") || check.equals("0"))
            		checkJobs = false;            	
            	spooler_log.info(".. job setting [checkJobs]: " + check);
            }
            if (getJobProperties().get("delay_after_error") != null) {
                String[] delays = getJobProperties().get("delay_after_error").toString().split(";");
                if (delays.length > 0) spooler_job.clear_delay_after_error();
                for(int i=0; i<delays.length; i++) {                	
                    String[] delay = delays[i].split(":");
                    spooler_job.set_delay_after_error(Integer.parseInt(delay[0]),  delay[1]);
                }
            }else{
            	spooler_job.set_delay_after_error(1, maxRetryInterval );
        		spooler_job.set_delay_after_error(maxRetry, "STOP" );
            }
            if (getJobProperties().get("min_warning_age")!=null){
            	String min_warning_age = getJobProperties().get("min_warning_age").toString();
            	if(min_warning_age.indexOf(":")>-1){
            		minWarningAge = calculateSeconds(min_warning_age);
            	} else minWarningAge = Integer.parseInt(min_warning_age);
            }
    		return true;
    	} catch (Exception e){
    		try{
    			getLogger().error("Error occured during initialisation: "+e);
    		} catch(Exception ex){}
			return false;			
    	}
	}
	
	private Vector getSlaveList() throws Exception{
		try{
			
		Vector slaveList = new Vector();
		int slaveCounter=0;
		SOSString sosString = new SOSString();
		Properties settings = getJobProperties();
        Enumeration keys = settings.keys();
        while (keys.hasMoreElements()){
        	String key =  sosString.parseToString(keys.nextElement());        
        	if (key.startsWith("slave_")){
        		String number = key.substring(6);
        		String sSlave = sosString.parseToString(settings,key);
        		String[] slaveArray = sSlave.split(":");
        		
        		int port;
        		try{
        			if (slaveArray.length!=2) throw new Exception();
        			port = Integer.parseInt(slaveArray[1]);
        		} catch (Exception e){
        			throw new Exception("\""+sSlave+"\" is not a valid value for slave. Values must have" +
    				" the form host:port or ip:port");
        		}
        		slaveList.add(new SlaveScheduler(slaveArray[0], port));
        		slaveCounter++;
        	}
        }
        hasRequests = !slaveList.isEmpty();
		return slaveList;
		} catch(Exception e){
			throw new Exception("Error occured retrieving settings for slaves: "+e,e);
		}
	}
	
	
	
	
	public boolean spooler_process() throws Exception {
		String hasRun = spooler.var("CheckSlavesHasRun");
		if (hasRun.length()==0){
			getLogger().info("This is the first run of JobSchedulerCheckSlaves. Slave Schedulers may " +
					"not have registered yet. Delaying spooler_process() for 120s");
			getLogger().debug6("CheckSlavesHasRun: "+hasRun);
			spooler.set_var("CheckSlavesHasRun","true");
			spooler_task.set_delay_spooler_process(120);
			return true;
		}
		
		checkRegistered();
		reportSlaves();
		if (checkJobs) checkSchedulerJobs();		
		return false;
	}
	
	private void checkRegistered() throws Exception{
		try{
		getLogger().info("Sending show_state command...");
		String answer = spooler.execute_xml("<show_state what=\"remote_schedulers\"/>");
		getLogger().debug9("answer from Scheduler: "+answer);
		
		Document spoolerDocument = docBuilder.parse(new ByteArrayInputStream(answer.getBytes()));
		Element spoolerElement = spoolerDocument.getDocumentElement();
		Node answerNode = spoolerElement.getFirstChild();
		while( answerNode != null  &&  answerNode.getNodeType() != Node.ELEMENT_NODE ) answerNode = answerNode.getNextSibling();
		
        if ( answerNode == null ) {
            throw new Exception("answer contains no xml elements");
        }
        
        Element answerElement = (Element)answerNode;        
        if (!answerElement.getNodeName().equals( "answer" ) )  
            throw new Exception( "element <answer> is missing" );
        schedulerDateTime = answerElement.getAttribute("time");
        
        NodeList schedulerNodes = answerElement.getElementsByTagName("remote_scheduler");
        getLogger().debug3(schedulerNodes.getLength() + " remote_scheduler elements found.");
        
        for(int i=0; i< schedulerNodes.getLength(); i++){
        	Node remoteSchedulerNode = schedulerNodes.item(i);
        	if (remoteSchedulerNode!=null && remoteSchedulerNode.getNodeType()==Node.ELEMENT_NODE){
        		Element remoteScheduler = (Element)remoteSchedulerNode;
        		SlaveScheduler slave;
        		String host = remoteScheduler.getAttribute("hostname");
        		String ip = remoteScheduler.getAttribute("ip");
        		String tcp_port = remoteScheduler.getAttribute("tcp_port");
        		String connected = remoteScheduler.getAttribute("connected");
        		if (connected.equalsIgnoreCase("no")){
        			String disconnectedAt = remoteScheduler.getAttribute("disconnected_at");
        			slave = new SlaveScheduler(host, ip, Integer.parseInt(tcp_port), disconnectedAt);
        		} else {
        			slave = new SlaveScheduler(host, ip, Integer.parseInt(tcp_port));
        		}
        		if (requestedSlaves.contains(slave)){        			
        			requestedSlaves.remove(slave);
        			if (slave.isConnected()){        				
        				connectedSlaves.add(slave);
        			} else registeredSlaves.add(slave);
        		}
        		// Wenn keine Slaves explizit angegeben sind, dann wenigstens
        		// gucken, ob alle registrierten auch connected sind.
        		if (!hasRequests){
        			if (slave.isConnected()){
        				connectedSlaves.add(slave);
        			} else registeredSlaves.add(slave);
        		}
        		
        	}
        }
		} catch (Exception e){
			throw new Exception ("Error occured checking remote schedulers: "+e,e);
		}
        /*Node stateNode = answerElement.getFirstChild();
        while( stateNode != null  &&  stateNode.getNodeType() != Node.ELEMENT_NODE )  stateNode = stateNode.getNextSibling();
        
        if ( stateNode == null ) {
            throw new Exception("element <state> is missing");
        }
        
        Element stateElement = (Element)stateNode;
        if (!stateElement.getNodeName().equals( "state" ) )  
            throw new Exception( "element <state> is missing" );
        */
	}
	
	private void reportSlaves() throws Exception{
		if(!connectedSlaves.isEmpty()){
			getLogger().info("The following slave Job Schedulers are registered and connected:");
			logSlaves(connectedSlaves);
		}
		if(!registeredSlaves.isEmpty()){
			boolean minAgeReached = needsWarning(registeredSlaves);
			String warning = "The following slave Job Schedulers are registered but not connected:";
			if (warnIfNotConnected && minAgeReached){
				getLogger().warn("Some slave Job Schedulers are registered but not connected, see log for details.");								
			}
			getLogger().info(warning);
			logSlaves(registeredSlaves);
		}
		if(!requestedSlaves.isEmpty()){
			String warning = "The following slave Job Schedulers are not registered and not connected:";
			if (warnIfNotRegistered)	{
				getLogger().warn("Some slave Job Schedulers are not registered and not connected, see log for details.");				
			}
			getLogger().info(warning);
			logSlaves(requestedSlaves);
		}
	}
	
	private void logSlaves(Collection slaves) throws Exception{
		Iterator iter = slaves.iterator();
		while (iter.hasNext()){
			getLogger().info("  "+iter.next().toString());
		}
	}
	
	private void checkSchedulerJobs() throws Exception{
		Iterator iter = connectedSlaves.iterator();
		while (iter.hasNext()){
			String hostPort = iter.next().toString();
			getLogger().info("Checking jobs for Scheduler "+hostPort+" ...");
			if (hostPort!=null){
				String[] aHostPort=hostPort.split(":");
				String host=aHostPort[0];
				String port="";
				int iPort=0;
				SOSSchedulerCommand command;
				if(aHostPort.length>1) {
					port=aHostPort[1];
					try{
						iPort=Integer.parseInt(port);
					} catch (Exception e){
						
					}					
				}
				if (iPort>0) command = new SOSSchedulerCommand(host, iPort);
				else command = new SOSSchedulerCommand(host);
				try{
					command.connect();					
					command.sendRequest("<show_state/>");
					String response = command.getResponse().trim();
					getLogger().debug6("Response from Job Scheduler: "+response);
					
					Document spoolerDocument = docBuilder.parse(new ByteArrayInputStream(response.getBytes()));
					Element spoolerElement = spoolerDocument.getDocumentElement();
					Node answerNode = spoolerElement.getFirstChild();
					while( answerNode != null  &&  answerNode.getNodeType() != Node.ELEMENT_NODE ) answerNode = answerNode.getNextSibling();
					
			        if ( answerNode == null ) {
			            throw new Exception("answer contains no xml elements");
			        }
			        
			        Element answerElement = (Element)answerNode;      
			        NodeList jobs = answerElement.getElementsByTagName("job");
			        if (jobs!=null && jobs.getLength()>0){
			        	for (int i=0;i<jobs.getLength();i++){
			        		Node jobNode = jobs.item(i);
			            	if (jobNode!=null && jobNode.getNodeType()==Node.ELEMENT_NODE){
			            		Element jobElement = (Element) jobNode;
			            		String state = jobElement.getAttribute("state");
			            		String jobName = jobElement.getAttribute("job");
			            		if (state.equalsIgnoreCase("stopped")){
			            			
			            			getLogger().warn("Job "+jobName+" on Job Scheduler \""+hostPort+"\" is stopped.");
			            		}
			            		if (state.equalsIgnoreCase("enqueued")){
			            			getLogger().info("Job "+jobName+" on Job Scheduler \""+hostPort+"\" is enqueued.");
			            		}
			            	}
			        	}
			        }
				}catch (Exception e){
					getLogger().warn("Error occured querying Job Scheduler at\""+hostPort+"\": "+e);
				} finally{
					try {
						command.disconnect();
					} catch (Exception ex){}
				}
				
			}
		}
	}
	
	private int calculateSeconds(String hoursMinSec){
		int age=0;
    	String[] timeArray = hoursMinSec.split(":");
    	int hours = Integer.parseInt(timeArray[0]);
    	int minutes = Integer.parseInt(timeArray[1]);
    	int seconds = 0;
    	if (timeArray.length>2){
    		seconds = Integer.parseInt(timeArray[2]);
    	}
    	age = hours*3600+minutes*60+seconds;
    	return age;
	}
	
	private boolean needsWarning(Collection slaves){
		Iterator iter = slaves.iterator();
		while (iter.hasNext()){
			SlaveScheduler slave = (SlaveScheduler) iter.next();
			String eventTime = slave.getDisconnectedAt();
			if (needsWarning(eventTime, schedulerDateTime)) return true;
		}
		return false;
	}
	
	private boolean needsWarning(String eventTime, String schedulerTime){
		try{
			Date eventDate = SOSDate.getTime(eventTime.substring(0,19));
			Date schedulerDate = SOSDate.getTime(schedulerTime.substring(0,19));
			GregorianCalendar eventCal = new GregorianCalendar();
			GregorianCalendar schedulerCal = new GregorianCalendar();
			eventCal.setTime(eventDate);
			schedulerCal.setTime(schedulerDate);
			eventCal.add(java.util.Calendar.SECOND, minWarningAge);
			if (eventCal.before(schedulerCal)) return true;
		} catch (Exception e){
			try{
				getLogger().warn("Failed to convert String to Date: "+e);
			} catch(Exception ex){}
		}
		return false;
	}
}
/* Beispiel remote_schedulers Antwort:
<spooler>
	<answer time="2006-02-02 11:10:32.156">
		<state time="2006-02-02 11:10:32" id="master" spooler_id="master" spooler_running_since="2006-01-27 14:52:12" state="running" log_file="C:/scheduler.master/logs/scheduler-2006-01-27-145212.master.log" version="2.0.144.4023  (2005-11-10 14:42:31) " pid="344" config_file="C:/scheduler.master/config/scheduler.xml" host="8of9" need_db="yes" tcp_port="4477" udp_port="4477" db="jdbc  -id=spooler -class=com.mysql.jdbc.Driver jdbc:mysql://Wilma:3305/scheduler -user=scheduler" cpu_time="22.625" loop="59649" waits="32611">
		    <jobs>...</jobs>			
			<remote_schedulers count="5" connected="5">
				<remote_scheduler ip="192.11.0.57" hostname="sag.sos" tcp_port="4370" scheduler_id="dataswitch_workflow" version="2.0.140.3946  (2005-09-26 10:52:01) " connected="yes" connected_at="2006-01-27 16:23:27.312" disconnected_at="2006-01-27 16:23:10.875"/>
				<remote_scheduler ip="192.11.0.57" hostname="sag.sos" tcp_port="4371" scheduler_id="dataswitch_inbound" version="2.0.140.3946  (2005-09-26 10:52:01) " connected="yes" connected_at="2006-01-27 16:23:23.156" disconnected_at="2006-01-27 16:23:10.687"/>
				<remote_scheduler ip="192.11.0.57" hostname="sag.sos" tcp_port="4372" scheduler_id="dataswitch_outbound" version="2.0.140.3946  (2005-09-26 10:52:01) " connected="yes" connected_at="2006-01-27 16:23:23.234" disconnected_at="2006-01-27 16:23:10.765"/>
				<remote_scheduler ip="192.11.0.61" hostname="wilma.sos" tcp_port="4477" scheduler_id="Wilma" version="2.0.146.4069  (2006-01-30 19:50:11) " connected="yes" connected_at="2006-02-01 16:34:35.187" disconnected_at="2006-02-01 16:33:30.671"/>
				<remote_scheduler ip="192.11.0.61" hostname="wilma.sos" tcp_port="4488" scheduler_id="managed.demo" version="2.0.144.4037  (2005-12-08 11:29:18) " connected="yes" connected_at="2006-02-02 11:01:11.406" disconnected_at="2006-02-02 11:00:08.093"/>
			</remote_schedulers>
		</state>
	</answer>
</spooler>
*/