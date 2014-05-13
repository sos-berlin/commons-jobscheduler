package sos.scheduler.launcher;


import java.util.HashMap;
import org.w3c.dom.Node;
import sos.scheduler.command.SOSSchedulerCommand;
import sos.util.SOSClassUtil;
import sos.util.SOSLogger;
import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

/**
 * 
 * Klasse JobSchedulerLoadTestLauncher
 * Diese Klasse kann die Ausführung paralleler Jobs skalieren und beliebig viele Jobs können parallel laufen.
 * Die Klasse kann beliebig viele individuelle Jobs starten und kann mit dem Namen eines anderen Jobs oder
 * Auftrages parametrisiert werden, die gestartet werden soll.
 * Die Anzahl zu startenden Jobs in konfigurierbaren Zeitabständen sowie die Erhöhung der Anzahl zu startenden Jobs beim Erreichen
 * jedes Intervalls sind konfigurierbar.
 * 
 * resourcen: * sos.mail.jar, sos.util.jar, sos.xml.jar, xercesImpl.jar, xml-apis.jar, xalan.jar 
 *
 * 
 *
 * a) Aufruf über Kommandozeile
 *
 * java -cp=. JobSchedulerLoadTestLauncher -config=<Konfigurationsdatei> -job= -host= -port=
 *
 * Wenn host= bzw. port= gesetzt sind, dann überschreiben sie die Werte der Konfigurationsdatei
 * Ist ein Job-Name angegeben, dann wird versucht aus der Konfigurationsdatei die Parameter dieses 
 * Jobs zu extrahieren. Ist kein Job-Name angegeben, dann wird das erste Element <params> aus der Konfigurationsdatei ausgelesen. 
 *
 * Inhalt der Konfigurationsdatei. Es kann auch hier ein XML-Konfigurationsdatei eines Schedulers sein.
 * <params>
 *  <param name="" value=""/>
 * </params>
 *
 *
 * 2. Parameter
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
 * Die minimale Anzahl von Jobs oder Aufträgen, die gleichzeitig gestartet werden. Default=1
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
 *  g) <param name="scheduler_launcher_duration" value="120"/>
 *  Kriterium um das Programm zu beenden
 *  
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
 *  scheduler_launcher_ beginnen, werden den Jobs oder Aufträgen durchgereicht, die gestartet werden sollen.
 *
 *
 * @author Mürüvet Öksüz
 * 
 * mueruevet.oeksuez@sos-berlin.com
 *
 * 
 */

public class JobSchedulerLoadTestLauncher {
	
	/** Allgemeine Parameter für den Launcher */
	/** Der Host des Job Schedulers, der den Job ausführt, Default: localhost*/
	private String schedulerLauncherHost                = "localhost";
	
	/** Der Port des Job Schedulers, der den Job ausführt, Default: 4444 */
	private int schedulerLauncherPort                   = 4444;
	
	/** Das Protokoll zum Versenden via JobSchedulerCommand tcp oder udp, Default: tcp */
	private String schedulerLauncherProtocol            = "tcp";
	
	/** Die minimale Anzahl von Jobs oder Aufträgen, die gleichzeitig gestartet wird. Default=1 */
	private int schedulerLauncherMinStarts              = 1;
	
	/** Die maximale Anzahl von Jobs oder Aufträgen, die gleichzeitig gestartet werden.
	 * -1 bedeutet, das kein Parameterwert angegeben ist
	 */
	private int schedulerLauncherMaxStarts              = -1;
	
	/**
	 * Die Anzahl zu startender Job oder Aufträge wird pro Start um diese Zahl erhöht.
	 * Ein Wert 3 ist gleichbedeutend mit +3, d.h. die Zahl wird um 3 erhöht.
	 * Ein Wert *2 bedeutet, dass sich die Anzahl verdoppelt.
	 * Das Inkrement gilt nicht beim ersten Start. Die mit dem Parameter max_starts gesetzte Anzahl darf nicht überschritten werden.
	 * Wird sie überschritten, dann werden alle weiteren Starts mit dem Wert von max_starts ausgeführt.
	 * Z.B. value="+10" oder "*2"/>
	 * Diese Wert wird in zwei Variablen gesplittet: 
	 * schedulerLauncherStartIncrement und schedulerLauncherStartIncrementFactor.
	 */
	private int schedulerLauncherStartIncrement           = 1;
	private String schedulerLauncherStartIncrementFactor  = "+";
	
	/** Verzögerung bis zum nächsten Job Starts*/
	private int schedulerLauncherInterval                 = 0; 
	
	/** Abbruchbedingung. Beim Erreichen der Anzahl Sekunden wird das Programm beendet. 
	 * Ist das Programm mitten im Launchen, dann wird so lange darauf gewartet, bis das Ende des Launchens erreicht ist.
	 * Default ist 120 sek.
	 *  */
	private long schedulerLauncherDuration                = 120;
	
	/**
	 * Wird berechnet aus der aktuellen Uhrzeit in Milisekunden + Abbruchbedingung (=>schedulerLauncherDuration * 1000)
	 */
	private long terminateTimeInSec                       = 0;
	
	/** Spezielle Parameter für Jobs
	 * Wenn dieser Parameter übergeben wurde, dann handelt es sich um einen Job.
	 * Der hier übergebene Job-Name ist der Wert des Attributs job= in <start_job job="..."/>
	 */
	private String schedulerLauncherJob                 = "";
	
	/** Spezielle Parameter für Jobs, mandatory*/
	private String schedulerLauncherJobAfter            = "";
	private String schedulerLauncherJobAt               = "";
	private String schedulerLauncherJobWebService       = "";
	
	/**
	 * Spezielle Parameter für Aufträge.
	 * Wenn dieser Parameter übergeben wurde, dann handelt es sich um einen Auftrag.
	 * Die hier übergebene Auftragskennung ist der Wert des Attributs id= in <add_order id="..."/>
	 */
	private String schedulerLauncherOrder               = "";
	
	/** Spezielle Parameter für Aufträge. mandatory, wenn Parameter schedulerLauncher_order angegeben ist */
	private String schedulerLauncherOrderJobChain       = "";
	
	/** Spezielle Parameter für Aufträge.
	 * Optional, d.h. bitte keine eigenen Defaults, sondern im Fall des Fehlens einfach nicht übergeben
	 */
	private boolean schedulerLauncherOrderReplace        = true;
	private String schedulerLauncherOrderState          = "";
	private String schedulerLauncherOrderTitle          = "";
	private String schedulerLauncherOrderAt             = "";
	private String schedulerLauncherOrderPriority       = "";
	private String schedulerLauncherOrderWebService     = "";
	
	/** Das sos.util.SOSLogger Objekt*/
	private SOSLogger sosLogger                         = null;
	
	/** sos.util.SOSString Objekt */
	private SOSString sosString                         = null;
	
	/** Hilfsparameter, alle aus der Konfigurationsdatei gelesene Parametern werden hier geschrieben  */
	private HashMap allParam                            = new HashMap();
	
	/** Argument-Parameter: XML-Konfigurationsdatei
	 * Inhalt der Konfigurationsdatei
	 * <params>
	 *   <param name="" value=""/>
	 * </params>
	 * */
	private String configFile                           = "";		
	
	/** Status Text */
	private String stateText                            = "";
	
	/** Name des Jobs, dessen Parameter aus der XML-Konfigurationsdatei extrahiert werden sollen.
	 *  Gilt nur, wenn diese Klasse über die Kommandozeile aufgerufen wird*/
	private String jobname                              = "";
	
    
	/**
	 * Konstruktor
	 *
	 */
	public JobSchedulerLoadTestLauncher(SOSLogger sosLogger_) throws Exception {
		try {
			this.sosLogger = sosLogger_;
			init();
		} catch (Exception e) {
			throw new Exception ("..error in " + SOSClassUtil.getMethodName() + ": " + e.getMessage());
		}
	}

	
	/**
	 * Konstruktor
	 *
	 */
	public JobSchedulerLoadTestLauncher(SOSLogger sosLogger_, String configFile_, String jobname_, String host_, int port_) throws Exception {
		try {
			this.sosLogger = sosLogger_;
			this.configFile = configFile_;
			
			// Wenn das configFile_ mehrere Job-Definitionen enthält, dann sollen die Parameter diese jobname extrahiert werden			
			if (jobname_ != null && jobname_.length() > 0) {
				this.jobname = jobname_;
				sosLogger.debug3("..argument[job] = " + jobname);
			}
			
			init();
			
			//Das Argument host_ überschreibt eventuell vorhandene Parameter aus der Konfigurationsdatei
			if (sosString.parseToString(host_).length() > 0) {
				this.schedulerLauncherHost = host_;			
				sosLogger.debug3("..argument[host] = " + schedulerLauncherHost);
			}
			
			//Das Argument port_ überschreibt eventuell vorhandene Parameter aus der Konfigurationsdatei			
			if ((port_ != -1) 					
					&& (sosString.parseToString(String.valueOf(port_)).length() > 0)) {
				this.schedulerLauncherPort = port_;
				sosLogger.debug3("..argument[port] = " + schedulerLauncherPort);
			}						
			
		} catch (Exception e) {
			throw new Exception ("..error in " + SOSClassUtil.getMethodName() + ": " + e.getMessage());
		}
	}
	
	private void init() throws Exception{
		try {
			sosString = new SOSString();
			if (sosString.parseToString(jobname).length() > 0) {
				extractParameters(jobname);
			} else {
				extractParameters();
			}
		} catch (Exception e) {
			throw new Exception ("..error in " + SOSClassUtil.getMethodName() + ": " + e.getMessage());
		}
	}
	
	/**
	 * Extrahiert die Parameters aus einer Konfigurationsdatei.
	 * Das ist der Fall, wenn diese Klasse über eine Kommandozeile aufgerufen wurde und 
	 * die Konfigurationsdateiname als Argument übergeben wurde.
	 * 
	 * Wenn keine Konfigurationsdatei übergeben wurde, dann springt er raus.
	 * 
	 * @throws Exception
	 */
	private void extractParameters() throws Exception{
		
		sosLogger.debug1("..reading parameters ...");		
		String paramName = "";
		String paramValue = "";
        
		try { 
			if (configFile.length() == 0)
				return;
			
			allParam= new HashMap(); //Hilfsvariaable
			
			SOSXMLXPath xpath = new SOSXMLXPath(this.configFile);
						
			org.w3c.dom.NodeList nl = xpath.selectNodeList(xpath.document.getElementsByTagName("param").item(0),"//param");
			
			for(int i =0; i < nl.getLength(); i++) {
				Node nParam = nl.item(i);
				org.w3c.dom.NamedNodeMap map = nParam.getAttributes();
				for (int j = 0; j < map.getLength(); j++) {
					//sosLogger.debug6(".. attributes: " + map.item(j).getNodeName()+ "="+  map.item(j).getNodeValue());
					paramName = map.item(j).getNodeValue();
					if (map.getLength() > 1) {
						j++;
						//sosLogger.debug6(" ..parameter: " + map.item(j).getNodeName()+ "="+  map.item(j).getNodeValue());
						paramValue = map.item(j).getNodeValue();
					}
					sosLogger.debug6(".. parameter: " + paramName+ "="+  paramValue);
					allParam.put(paramName, paramValue);
				}
			}	
			getParameters();
			
		} catch (Exception e) {
			throw new Exception("..error occurred processing job parameters: " + e.getMessage());
		}
	}
	
	/**
	 * Extrahiert die Parameters aus einer Konfigurationsdatei.
	 * Das ist der Fall, wenn diese Klasse über eine Kommandozeile aufgerufen wurde und 
	 * die Konfigurationsdateiname als Argument übergeben wurde.
	 * 
	 * Wenn keine Konfigurationsdatei übergeben wurde, dann springt er raus.
	 * 
	 * @throws Exception
	 */
	private void extractParameters(String jobname) throws Exception{
		String paramName = "";
		String paramValue = "";
		boolean existJob = false;
        
		try {
			if (configFile.length() == 0)
				return;
			
			allParam= new HashMap(); //Hilfsvariaable
			
			SOSXMLXPath xpath = new SOSXMLXPath(this.configFile);						
			org.w3c.dom.NodeList nl = xpath.selectNodeList("//param");									
			for(int i =0; i < nl.getLength(); i++) {
				Node nParam = nl.item(i);
				
				if (nParam.getParentNode().getParentNode().getNodeName().equals("job") &&
						nParam.getParentNode().getParentNode().getAttributes().getNamedItem("name").getNodeValue().equals(jobname)) {
					existJob = true;
					org.w3c.dom.NamedNodeMap map = nParam.getAttributes();
					for (int j = 0; j < map.getLength(); j++) {
						paramName = map.item(j).getNodeValue();
						if (map.getLength() > 1) {
							j++;						
							paramValue = map.item(j).getNodeValue();
						}
						sosLogger.debug6(".. attribute: " + paramName+ "="+  paramValue);
						allParam.put(paramName, paramValue);
					}
				}
			}
			if (!existJob) {
				throw new Exception("..job [" + jobname + "] not found in configuration file: " + configFile);
			}
			getParameters();
		} catch (Exception e) {
			throw new Exception("..error occurred processing job parameters: " + e.getMessage());
		}
	}
	
	/**
	 * Setzen alle Parametern
	 * @param allParams_
	 * @throws Exception
	 */
	public void setParameters (HashMap allParams_) throws Exception{
		try {
			this.allParam = allParams_;
			getParameters();
		} catch (Exception e) {
			throw new Exception("..error occurred processing job parameters: " + e.getMessage());
		}
	}
	
	private void getParameters() throws Exception{
		
		sosLogger.debug3("get parameters ...");
		
		try { 
			
			if (sosString.parseToString(schedulerLauncherHost).length() == 0) {
				if (sosString.parseToString(allParam, "scheduler_launcher_host").length() > 0) {
					schedulerLauncherHost = sosString.parseToString(allParam, "scheduler_launcher_host");
					sosLogger.debug3("..parameter[scheduler_launcher_host] = " + schedulerLauncherHost);
				}
			}
			
			if (sosString.parseToString(allParam, "scheduler_launcher_port").length() > 0) {
				schedulerLauncherPort = Integer.parseInt(sosString.parseToString(allParam, "scheduler_launcher_port"));
				sosLogger.debug3("..parameter[scheduler_launcher_port] = " + schedulerLauncherPort);
			}
			
			if (sosString.parseToString(allParam, "scheduler_launcher_protocol").length() > 0) {
				schedulerLauncherProtocol = sosString.parseToString(allParam, "scheduler_launcher_protocol");
				sosLogger.debug3("..parameter[scheduler_launcher_protocol] = " + schedulerLauncherProtocol);
			}
			
			if (sosString.parseToString(allParam, "scheduler_launcher_min_starts").length() > 0)  {
				schedulerLauncherMinStarts = Integer.parseInt(sosString.parseToString(allParam, "scheduler_launcher_min_starts"));
				sosLogger.debug3("..parameter[scheduler_launcher_min_starts] = " + schedulerLauncherMinStarts);
			}
			
			if (sosString.parseToString(allParam, "scheduler_launcher_start_increment").length() > 0) {
				sosLogger.debug3("..parameter[scheduler_launcher_start_increment] = " + schedulerLauncherStartIncrement);
				if (sosString.parseToString(allParam, "scheduler_launcher_start_increment").trim().startsWith("+") ||
						sosString.parseToString(allParam, "scheduler_launcher_start_increment").trim().startsWith("*")) {
					schedulerLauncherStartIncrement = Integer.parseInt(sosString.parseToString(allParam, "scheduler_launcher_start_increment").trim().substring(1));
					schedulerLauncherStartIncrementFactor = sosString.parseToString(allParam, "scheduler_launcher_start_increment").trim().substring(0, 1); 
				} else {
					schedulerLauncherStartIncrement = Integer.parseInt(sosString.parseToString(allParam, "scheduler_launcher_start_increment"));
				}							
			}
			
			if (sosString.parseToString(allParam, "scheduler_launcher_max_starts").length() > 0) {
				this.schedulerLauncherMaxStarts= Integer.parseInt(sosString.parseToString(allParam, "scheduler_launcher_max_starts"));
				sosLogger.debug3("..parameter[scheduler_launcher_max_starts] = " + schedulerLauncherMaxStarts);
			}						
			
			if (sosString.parseToString(allParam, "scheduler_launcher_interval").length() > 0) {
				schedulerLauncherInterval = Integer.parseInt(sosString.parseToString(allParam, "scheduler_launcher_interval"));
				sosLogger.debug3("..parameter[scheduler_launcher_interval] = " + schedulerLauncherInterval);
			}
			
			if (sosString.parseToString(allParam, "scheduler_launcher_job").length() > 0) {
				schedulerLauncherJob = sosString.parseToString(allParam, "scheduler_launcher_job");
				sosLogger.debug3("..parameter[scheduler_launcher_job] = " + schedulerLauncherJob);
			}
			
			if (sosString.parseToString(allParam, "scheduler_launcher_job_after").length() > 0) {
				schedulerLauncherJobAfter = sosString.parseToString(allParam, "scheduler_launcher_job_after");
				sosLogger.debug3("..parameter[scheduler_launcher_job_after] = " + schedulerLauncherJobAfter);
			}
			
			if (sosString.parseToString(allParam, "scheduler_launcher_job_at").length() > 0) {
				schedulerLauncherJobAt = sosString.parseToString(allParam, "scheduler_launcher_job_at");
				sosLogger.debug3("..parameter[scheduler_launcher_job_at] = " + schedulerLauncherJobAt);
			}
			
			if (sosString.parseToString(allParam, "scheduler_launcher_duration").length() > 0) {
				schedulerLauncherDuration = Long.parseLong(sosString.parseToString(allParam, "scheduler_launcher_duration"));
				sosLogger.debug3("..parameter[scheduler_launcher_duration] = " + schedulerLauncherDuration);
			}
			
			
			if (sosString.parseToString(allParam, "scheduler_launcher_job_web_service").length() > 0) {
				schedulerLauncherJobWebService = sosString.parseToString(allParam, "scheduler_launcher_job_web_service");
				sosLogger.debug3("..parameter[scheduler_launcher_web_service] = " + schedulerLauncherJobWebService);
			}
			
			if (sosString.parseToString(allParam, "scheduler_launcher_order").length() > 0) {
				schedulerLauncherOrder = sosString.parseToString(allParam, "scheduler_launcher_order");
				sosLogger.debug3("..parameter[scheduler_launcher_order] = " + schedulerLauncherOrder);
			}
			
			if (sosString.parseToString(allParam, "scheduler_launcher_order_replace").length() > 0) {
				schedulerLauncherOrderReplace = sosString.parseToBoolean(sosString.parseToString(allParam, "scheduler_launcher_order_replace"));
				sosLogger.debug3("..parameter[scheduler_launcher_order_replace] = " + schedulerLauncherOrderReplace);
			}
			
			if (sosString.parseToString(allParam, "scheduler_launcher_order_job_chain").length() > 0) {
				schedulerLauncherOrderJobChain = sosString.parseToString(allParam, "scheduler_launcher_order_job_chain");
				sosLogger.debug3("..parameter[scheduler_launcher_order_job_chain] = " + schedulerLauncherOrderJobChain);
			}
			
			if (sosString.parseToString(allParam, "scheduler_launcher_order_state").length() > 0) {
				schedulerLauncherOrderState = sosString.parseToString(allParam, "scheduler_launcher_order_state");
				sosLogger.debug3("..parameter[scheduler_launcher_order_state] = " + schedulerLauncherOrderState);
			}
			
			if (sosString.parseToString(allParam, "scheduler_launcher_order_title").length() > 0) {
				schedulerLauncherOrderTitle = sosString.parseToString(allParam, "scheduler_launcher_order_title");
				sosLogger.debug3("..parameter[scheduler_launcher_title] = " + schedulerLauncherOrderTitle);
			}
			
			if (sosString.parseToString(allParam, "scheduler_launcher_order_at").length() > 0) {
				schedulerLauncherOrderAt = sosString.parseToString(allParam, "scheduler_launcher_order_at");
				sosLogger.debug3("..parameter[scheduler_launcher_order_at] = " + schedulerLauncherOrderAt);
			}
			
			if (sosString.parseToString(allParam, "scheduler_launcher_order_priority").length() > 0) {
				schedulerLauncherOrderPriority = sosString.parseToString(allParam, "scheduler_launcher_order_priority");
				sosLogger.debug3("..parameter[scheduler_launcher_order_priority] = " + schedulerLauncherOrderPriority);
			}
			
			if (sosString.parseToString(allParam, "scheduler_launcher_order_web_service").length() > 0) {
				schedulerLauncherOrderWebService = sosString.parseToString(allParam, "scheduler_launcher_order_web_service");
				sosLogger.debug3("..parameter[scheduler_launcher_order_web_service] = " + schedulerLauncherOrderWebService);
			}	
			
			
		} catch (Exception e) {
			throw new Exception("..error occurred processing job parameters: " + e.getMessage());
		}
	}
	
	/**
	 * Erstellt das XML-String zusammen zu einem Request.
	 * @return
	 * @throws Exception
	 */
	private String getRequest() throws Exception {
		String request = "";
        
		try {
			if (sosString.parseToString(schedulerLauncherOrder).length() > 0) {
				request  = "<add_order";
				request += " replace=\"" + (this.schedulerLauncherOrderReplace ? "yes" : "no") + "\"";
				
				if (sosString.parseToString(this.schedulerLauncherOrder).length() > 0)
					request += " id=\"" + schedulerLauncherOrder + "\"";
				
				if (sosString.parseToString(this.schedulerLauncherOrderAt).length() > 0)
					request += " at=\"" + this.schedulerLauncherOrderAt + "\"";
				else {					
					request += " at=\"now + " + this.schedulerLauncherInterval +"\"";
				}
				//will hier mit einer Varaiable arbeiten. 
				this.schedulerLauncherJobAt = schedulerLauncherOrderAt;
				
				if (sosString.parseToString(this.schedulerLauncherOrderJobChain).length() > 0)
					request += " job_chain=\"" + this.schedulerLauncherOrderJobChain + "\"";
				
				if (sosString.parseToString(this.schedulerLauncherOrderPriority).length() > 0)
					request += " priority=\"" + this.schedulerLauncherOrderPriority + "\"";
				
				if (sosString.parseToString(this.schedulerLauncherOrderState).length() > 0)
					request += " state=\"" + this.schedulerLauncherOrderState + "\"";
				
				if (sosString.parseToString(this.schedulerLauncherOrderTitle).length() > 0)
					request += " title=\"" + this.schedulerLauncherOrderTitle + "\"";
				
				if (sosString.parseToString(this.schedulerLauncherOrderWebService).length() > 0)
					request += " web_service=\"" + schedulerLauncherOrderWebService + "\"";
				
				request += ">";
				request += "<params>";
				
				Object[] params = this.allParam.entrySet().toArray();
				for(int i=0; i<params.length; i++) {
					if (!sosString.parseToString(params[i]).startsWith("scheduler_launcher_")) {
						request += "<param name=\"" + sosString.parseToString(params[i]).split("=")[0] + "\" value=\"" + sosString.parseToString(params[i]).split("=")[1] + "\"/>";
					}
				}
				request += "</params>";
				
				request += "</add_order>";
				
			} else if (sosString.parseToString(this.schedulerLauncherJob).length() > 0) {
				request  = "<start_job job=\"" + schedulerLauncherJob + "\"";
				if (sosString.parseToString(schedulerLauncherJobAfter).length() > 0)
					request += " after=\"" + this.schedulerLauncherJobAfter + "\"";
				
				if (sosString.parseToString(schedulerLauncherJobAt).length() > 0)
					request += " at=\"" + schedulerLauncherJobAt + "\"";
				else {					
					request += " at=\"now + " + this.schedulerLauncherInterval + "\"";
				}
				if (sosString.parseToString(this.schedulerLauncherJobWebService).length() > 0)
					request += " web_service=\"" + schedulerLauncherJobWebService + "\"";
				
				request += ">";
				request += "<params>";
				Object[] params = this.allParam.entrySet().toArray();
				for(int i=0; i<params.length; i++) {
					if (!sosString.parseToString(params[i]).startsWith("scheduler_launcher_")) {
						request += "<param name=\"" + sosString.parseToString(params[i]).split("=")[0] + "\" value=\"" + sosString.parseToString(params[i]).split("=")[1] + "\"/>";
					}
				}
				request += "</params>";
				request += "</start_job>";
				
			} 
			sosLogger.debug("request: " + request);
			return request;
			
		} catch (Exception e) {
			throw new Exception("..error in "+ SOSClassUtil.getClassName() + ": " + e.getMessage());
		}
	}
	
	/**
	 * In dieser Methode wird eine Verbindung zu der Scheduler aufgebaut und
	 * das Request wird an den Job bzw. Order gesendet. 
	 * 
	 * @throws Exception
	 */
	public void process() throws Exception {
		String request = "";
		String response = "";
		int counter = 0;
		int counterError = 0;		
		long timeInSec = 0;
		SOSSchedulerCommand remoteCommand = null;
        
		try {
			timeInSec = System.currentTimeMillis();
			
			checkParams();
			
			request = getRequest();
			
			remoteCommand = new SOSSchedulerCommand();
			
			remoteCommand.setProtocol(this.schedulerLauncherProtocol);
			remoteCommand.connect(this.schedulerLauncherHost, this.schedulerLauncherPort);
						
			boolean loop = true;
			terminateTimeInSec = System.currentTimeMillis() + (this.schedulerLauncherDuration * 1000);			
			sosLogger.debug("..time until termination: " + terminateTimeInSec);
			//soll so lange wiederholt werden, bis aktuelle Zeit = terminateTimeInSec erreicht hat			
			while(loop) {								
				sosLogger.info("..sending request to job scheduler [" + this.schedulerLauncherHost + ":" + this.schedulerLauncherPort + "]: " + request);
				if (System.currentTimeMillis() > this.terminateTimeInSec) {
					//Abbruchbedingungen erreicht
					sosLogger.debug("..time until termination: " + terminateTimeInSec);
					loop = false;
					break; //abbruchbedingung
				}
				//loop wird nur auf false gesetzt, wenn scheduler_launcher_duration erreicht wurde.
				for (int i =0; i < schedulerLauncherMinStarts ; i++) {            		
					remoteCommand.sendRequest(request);
					counter++;
//					hier die Fehlerhandhabung
					if (this.schedulerLauncherProtocol.equalsIgnoreCase("tcp")) { // no response is returned for UDP messages
						response = remoteCommand.getResponse();
						SOSXMLXPath xpath = new SOSXMLXPath(new StringBuffer(response));
						String errCode = xpath.selectSingleNodeValue("//ERROR/@code");
						String errMessage = xpath.selectSingleNodeValue("//ERROR/@text");
						sosLogger.info("..job scheduler response: " + response);            		
						if ((errCode != null && errCode.length() > 0) || (errMessage != null && errMessage.length() > 0)) {
							sosLogger.warn("..job scheduler response reports error message: " + errMessage + " [" + errCode + "]");
							counterError++;
						}
					}
				}
								
				schedulerLauncherMinStarts = this.startIncrement(schedulerLauncherMinStarts);
								
			}
			sosLogger.info("..number of jobs launched: " + counter);
			stateText = "..number of jobs launched: " + (counter) + "(error="+(counterError) +  ";success="+(counter - counterError) +")";
			showSummary(counter, counterError, timeInSec);
			
			
			
		} catch (Exception e) {
			stateText = "..number of jobs launched: " + (counter) + "(error=" + counterError +  ";success="+(counter - counterError) +")" + e.getMessage();
			sosLogger.info("..error in "+ SOSClassUtil.getClassName() + ": " + e.getMessage());
			throw new Exception("..error in "+ SOSClassUtil.getClassName() + ": " + e.getMessage());
			
		} finally {
			if (remoteCommand != null) { try { remoteCommand.disconnect(); } catch (Exception x) {} } // gracefully ignore this error
		}
	}
	
	/**
	 * Überprüfungen:
	 * Die Parameter scheduler_launcher_job und scheduler_launcher_order dürfen nicht gleichzeitig leer sein bzw. gleichzeitig angegeben sein
	 * @throws Exception
	 */
	private void checkParams() throws Exception {
		int oneOfUs = 0;
		try {		
			oneOfUs += (sosString.parseToString(schedulerLauncherJob).length() == 0) ? 0 : 1;
			oneOfUs += (sosString.parseToString(this.schedulerLauncherOrder).length() == 0) ? 0 : 1;			
			
			if (oneOfUs == 0) {
				throw new Exception("one of the parameters [scheduler_launcher_job, scheduler_launcher_order] must be specified");
			} else if (oneOfUs > 1) {
				throw new Exception("only one of the parameters [scheduler_launcher_job, scheduler_launcher_order] must be specified, " + oneOfUs + " were given");
			}
			
		} catch (Exception e) {
			throw new Exception("..error in "+ SOSClassUtil.getClassName() + ": " + e.getMessage());
		}
	}
	
	
	/**
	 * Incrementiert um den Factory 
	 * @param i
	 * @return
	 * @throws Exception
	 */
	private int startIncrement(int i) throws Exception{
		int retVal = 0;
		try {
			if (schedulerLauncherStartIncrementFactor.startsWith("*")) {
				retVal = i * schedulerLauncherStartIncrement;				
			} else {
				retVal = i + schedulerLauncherStartIncrement;
			}
			
			if (retVal > this.schedulerLauncherMaxStarts) {
				retVal = schedulerLauncherMaxStarts;
				sosLogger.debug4("..maximum number of jobs to be launched is reached: " + schedulerLauncherMaxStarts);	
			}
			
			if (schedulerLauncherInterval > 0) {
				sosLogger.debug3("..delay " + schedulerLauncherInterval + " sec.");
				Thread.sleep(schedulerLauncherInterval * 1000);
			}
			
			sosLogger.debug5("..next start increment from " + i + " to " + retVal);			
			return retVal;	
		} catch (Exception e) {
			throw new Exception("..error in "+ SOSClassUtil.getClassName() + ": " + e.getMessage());
		}
		
	}
	
	
	/**
	 * Nach Programmausführung können folgende Ausgaben erfolgen:
	 * --------------------------------------------------------------- 
	 * ..number of job starts :  
	 * ..number of jobs processed successfully : 
	 * ..number of jobs processed with errors : 
	 * ..time elapsed in seconds :
	 * ---------------------------------------------------------------
	 */
	public void showSummary(int counter, int counterError, long timeInSec) throws Exception {
		
		try {
			sosLogger.debug5("..end time in miliseconds: "+ System.currentTimeMillis());
			sosLogger.info("---------------------------------------------------------------");
			sosLogger.info("..number of job starts                            : "+ counter);            
			sosLogger.info("..number of jobs processed successfully           : "+ (counter - counterError));
			sosLogger.info("..number of jobs processed with errors            : "+ counterError);
			sosLogger.info("..time elapsed in seconds                         : "+ Math.round((System.currentTimeMillis() - timeInSec) / 1000)+ "s");
			sosLogger.info("---------------------------------------------------------------");
		} catch (Exception e) {
			throw new Exception("..error occurred in "
					+ SOSClassUtil.getMethodName() + ": " + e.getMessage());
		}
	}
	
	/**
	 * Liefert den Status
	 * @return String
	 */
	public String getStateText() {
		return stateText;
	}
	
	/**
	 * 
	 * Diese Klasse kann über die Kommadozeile aufgerufen werden, z.B. kann die 
	 * Kommando-Datei wie folgt aussehen:
	 * 
	 * **********************************************************************************
	 * rem @echo off
	 * rem ---------------------------------------------------------------------------
	 * rem Start script for the sos.scheduler.launcher.JobSchedulerLoadTestLauncher
	 * rem ---------------------------------------------------------------------------
	 *
	 * set LIB_PATH=C:\scheduler.launcher
	 * set CLASS_PATH=.;%LIB_PATH%;%LIB_PATH%\lib\sos.mail.jar;%LIB_PATH%\lib\sos.util.jar;%LIB_PATH%\lib\sos.xml.jar;%LIB_PATH%\lib\xercesImpl.jar;%LIB_PATH%\lib\xml-apis.jar;%LIB_PATH%\lib\xalan.jar
	 * 
	 * rem run the command
	 * java -cp "%CLASS_PATH%" sos.scheduler.launcher.JobSchedulerLoadTestLauncher -config=J:\E\java\mo\sos.scheduler.job\src\test_xmls\job.xml -host=localhost -port=4373 > launcher.log
	 * 
	 * ********************************************************************************** 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
        
		String configFile = "";
		String host       = "";
		int port          = -1; //es wurde kein Argument -port= übergeben
		String job        = "";
		SOSString sosString = null;
        
		try {
			sosString = new SOSString();						
			
			if (args.length == 0) {
				System.err.println("Usage: ");
				System.out.println("sos.scheduler.launcher.JobSchedulerLoadTestLauncher");
				System.out.println("     -config=         xml configuration file");				
				System.out.println("     -host=           host (optional)");
				System.out.println("     -port=           port (optional)");
				System.out.println("     -job=            job  (optional)");
				System.out.println();
				System.out.println("for example:");
				System.err.println("java -cp=. sos.scheduler.launcher.JobSchedulerLoadTestLauncher -config=<xml configuration file> -host=<host> -port=<port> -job=<job name>");
				System.exit(0);
			}
			
			sos.util.SOSStandardLogger sosLogger = new sos.util.SOSStandardLogger(9);
			JobSchedulerLoadTestLauncher launcher = null;
			for (int i = 0; i < args.length; i++) {
				String[] currArg = args[i].split("=");
				if (currArg[0].equalsIgnoreCase("-config")) {
					configFile = currArg[1]; 
				} else if (currArg[0].equalsIgnoreCase("-host")) {
					host = currArg[1]; 
				} else if (currArg[0].equalsIgnoreCase("-port")) {
					if (sosString.parseToString(currArg[1]).length() > 0) {
						port = Integer.parseInt(currArg[1]); 
					}
				} else if (currArg[0].equalsIgnoreCase("-job")) {
					job = currArg[1]; 
				}
			}
			
			launcher = new JobSchedulerLoadTestLauncher(sosLogger, configFile, job, host, port); 	
			launcher.process();
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
	}
}
