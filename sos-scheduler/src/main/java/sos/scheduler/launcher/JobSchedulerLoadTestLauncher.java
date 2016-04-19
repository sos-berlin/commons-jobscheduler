package sos.scheduler.launcher;

import java.util.HashMap;

import org.w3c.dom.Node;

import sos.scheduler.command.SOSSchedulerCommand;
import sos.util.SOSClassUtil;
import sos.util.SOSLogger;
import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

/** Klasse JobSchedulerLoadTestLauncher Diese Klasse kann die Ausführung
 * paralleler Jobs skalieren und beliebig viele Jobs können parallel laufen. Die
 * Klasse kann beliebig viele individuelle Jobs starten und kann mit dem Namen
 * eines anderen Jobs oder Auftrages parametrisiert werden, die gestartet werden
 * soll. Die Anzahl zu startenden Jobs in konfigurierbaren Zeitabständen sowie
 * die Erhöhung der Anzahl zu startenden Jobs beim Erreichen jedes Intervalls
 * sind konfigurierbar.
 * 
 * resourcen: * sos.mail.jar, sos.util.jar, sos.xml.jar, xercesImpl.jar,
 * xml-apis.jar, xalan.jar
 *
 * 
 *
 * a) Aufruf über Kommandozeile
 *
 * java -cp=. JobSchedulerLoadTestLauncher -config=<Konfigurationsdatei> -job=
 * -host= -port=
 *
 * Wenn host= bzw. port= gesetzt sind, dann überschreiben sie die Werte der
 * Konfigurationsdatei Ist ein Job-Name angegeben, dann wird versucht aus der
 * Konfigurationsdatei die Parameter dieses Jobs zu extrahieren. Ist kein
 * Job-Name angegeben, dann wird das erste Element <params> aus der
 * Konfigurationsdatei ausgelesen.
 *
 * Inhalt der Konfigurationsdatei. Es kann auch hier ein XML-Konfigurationsdatei
 * eines Schedulers sein. <params> <param name="" value=""/> </params>
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
 * Die minimale Anzahl von Jobs oder Aufträgen, die gleichzeitig gestartet
 * werden. Default=1
 *
 * d) <param name="scheduler_launcher_max_starts" value="100"/>
 *
 * Die maximale Anzahl von Jobs oder Aufträgen, die gleichzeitig gestartet
 * werden.
 *
 * e) <param name="scheduler_launcher_start_increment" value="+10|*2"/>
 *
 * Die Anzahl zu startender Job oder Aufträge wird pro Start um diese Zahl
 * erhöht. Ein Wert 3 ist gleichbedeutend mit +3, d.h. die Zahl wird um 3
 * erhöht. Ein Wert *2 bedeutet, dass sich die Anzahl verdoppelt. Das Inkrement
 * gilt nicht beim ersten Start. Die mit dem Parameter max_starts gesetzte
 * Anzahl darf nicht überschritten werden. Wird sie überschritten, dann werden
 * alle weiteren Starts mit dem Wert von max_starts ausgeführt.
 *
 * f) alle Parameter, deren Namen nicht mit scheduler_launcher_ beginnen, werden
 * an den Job oder Auftrag durchgereicht.
 * 
 * g) <param name="scheduler_launcher_duration" value="120"/> Kriterium um das
 * Programm zu beenden
 * 
 *
 * 2.2 Spezielle Parameter für Jobs, siehe Beispiel <param
 * name="scheduler_launcher_job" value="job_name"/> Wenn dieser Parameter
 * übergeben wurde, dann handelt es sich um einen Job. Der hier übergebene
 * Job-Name ist der Wert des Attributs job= in <start_job job="..."/>
 *
 * In diesem Fall werden ausgewertet:
 *
 * a) mandatory keine
 *
 * b) optional, d.h. bitte keine eigenen Defaults, sondern im Fall des Fehlens
 * einfach nicht übergeben <param name="scheduler_launcher_job_after"
 * value="..."/> <param name="scheduler_launcher_job_at" value="..."/> <param
 * name="scheduler_launcher_job_web_service" value="..."/>
 *
 * 2.3 Spezielle Parameter für Aufträge, siehe Beispiel <param
 * name="scheduler_launcher_order" value="order_id"/> Wenn dieser Parameter
 * übergeben wurde, dann handelt es sich um einen Auftrag. Die hier übergebene
 * Auftragskennung ist der Wert des Attributs id= in <add_order id="..."/>
 *
 * In diesem Fall werden ausgewertet:
 *
 * a) mandatory <param name="scheduler_launcher_order_job_chain" value="..."/>
 *
 * b) optional, d.h. bitte keine eigenen Defaults, sondern im Fall des Fehlens
 * einfach nicht übergeben <param name="scheduler_launcher_order_replace"
 * value="yes|no"/> <param name="scheduler_launcher_order_state" value="..."/>
 * <param name="scheduler_launcher_order_title" value="..."/> <param
 * name="scheduler_launcher_order_at" value="..."/> <param
 * name="scheduler_launcher_order_priority" value="..."/> <param
 * name="scheduler_launcher_order_web_service" value="..."/> <param
 * name="scheduler_launcher_interval" value="..."/>
 * 
 *
 * 2.4 Alle anderen Parameter werden durchgereicht. Genauer: alle Parameter, die
 * nicht mit scheduler_launcher_ beginnen, werden den Jobs oder Aufträgen
 * durchgereicht, die gestartet werden sollen.
 *
 *
 * @author Mürüvet Öksüz
 * 
 *         mueruevet.oeksuez@sos-berlin.com */

public class JobSchedulerLoadTestLauncher {

    private String schedulerLauncherHost = "localhost";
    private int schedulerLauncherPort = 4444;
    private String schedulerLauncherProtocol = "tcp";
    private int schedulerLauncherMinStarts = 1;
    private int schedulerLauncherMaxStarts = -1;
    private int schedulerLauncherStartIncrement = 1;
    private String schedulerLauncherStartIncrementFactor = "+";
    private int schedulerLauncherInterval = 0;
    private long schedulerLauncherDuration = 120;
    private long terminateTimeInSec = 0;
    private String schedulerLauncherJob = "";
    private String schedulerLauncherJobAfter = "";
    private String schedulerLauncherJobAt = "";
    private String schedulerLauncherJobWebService = "";
    private String schedulerLauncherOrder = "";
    private String schedulerLauncherOrderJobChain = "";
    private boolean schedulerLauncherOrderReplace = true;
    private String schedulerLauncherOrderState = "";
    private String schedulerLauncherOrderTitle = "";
    private String schedulerLauncherOrderAt = "";
    private String schedulerLauncherOrderPriority = "";
    private String schedulerLauncherOrderWebService = "";
    private SOSLogger sosLogger = null;
    private SOSString sosString = null;
    private HashMap allParam = new HashMap();
    private String configFile = "";
    private String stateText = "";
    private String jobname = "";

    public JobSchedulerLoadTestLauncher(SOSLogger sosLogger_) throws Exception {
        try {
            this.sosLogger = sosLogger_;
            init();
        } catch (Exception e) {
            throw new Exception("..error in " + SOSClassUtil.getMethodName() + ": " + e.getMessage(), e);
        }
    }

    public JobSchedulerLoadTestLauncher(SOSLogger sosLogger_, String configFile_, String jobname_, String host_, int port_) throws Exception {
        try {
            this.sosLogger = sosLogger_;
            this.configFile = configFile_;
            if (jobname_ != null && !jobname_.isEmpty()) {
                this.jobname = jobname_;
                sosLogger.debug3("..argument[job] = " + jobname);
            }
            init();
            if (!sosString.parseToString(host_).isEmpty()) {
                this.schedulerLauncherHost = host_;
                sosLogger.debug3("..argument[host] = " + schedulerLauncherHost);
            }
            if ((port_ != -1) && (!sosString.parseToString(String.valueOf(port_)).isEmpty())) {
                this.schedulerLauncherPort = port_;
                sosLogger.debug3("..argument[port] = " + schedulerLauncherPort);
            }
        } catch (Exception e) {
            throw new Exception("..error in " + SOSClassUtil.getMethodName() + ": " + e.getMessage(), e);
        }
    }

    private void init() throws Exception {
        try {
            sosString = new SOSString();
            if (!sosString.parseToString(jobname).isEmpty()) {
                extractParameters(jobname);
            } else {
                extractParameters();
            }
        } catch (Exception e) {
            throw new Exception("..error in " + SOSClassUtil.getMethodName() + ": " + e.getMessage(), e);
        }
    }

    private void extractParameters() throws Exception {
        sosLogger.debug1("..reading parameters ...");
        String paramName = "";
        String paramValue = "";
        try {
            if (configFile.isEmpty()) {
                return;
            }
            allParam = new HashMap();
            SOSXMLXPath xpath = new SOSXMLXPath(this.configFile);
            org.w3c.dom.NodeList nl = xpath.selectNodeList(xpath.getDocument().getElementsByTagName("param").item(0), "//param");
            for (int i = 0; i < nl.getLength(); i++) {
                Node nParam = nl.item(i);
                org.w3c.dom.NamedNodeMap map = nParam.getAttributes();
                for (int j = 0; j < map.getLength(); j++) {
                    paramName = map.item(j).getNodeValue();
                    if (map.getLength() > 1) {
                        j++;
                        paramValue = map.item(j).getNodeValue();
                    }
                    sosLogger.debug6(".. parameter: " + paramName + "=" + paramValue);
                    allParam.put(paramName, paramValue);
                }
            }
            getParameters();
        } catch (Exception e) {
            throw new Exception("..error occurred processing job parameters: " + e.getMessage(), e);
        }
    }

    private void extractParameters(String jobname) throws Exception {
        String paramName = "";
        String paramValue = "";
        boolean existJob = false;
        try {
            if (configFile.isEmpty()) {
                return;
            }
            allParam = new HashMap();
            SOSXMLXPath xpath = new SOSXMLXPath(this.configFile);
            org.w3c.dom.NodeList nl = xpath.selectNodeList("//param");
            for (int i = 0; i < nl.getLength(); i++) {
                Node nParam = nl.item(i);
                if ("job".equals(nParam.getParentNode().getParentNode().getNodeName())
                        && nParam.getParentNode().getParentNode().getAttributes().getNamedItem("name").getNodeValue().equals(jobname)) {
                    existJob = true;
                    org.w3c.dom.NamedNodeMap map = nParam.getAttributes();
                    for (int j = 0; j < map.getLength(); j++) {
                        paramName = map.item(j).getNodeValue();
                        if (map.getLength() > 1) {
                            j++;
                            paramValue = map.item(j).getNodeValue();
                        }
                        sosLogger.debug6(".. attribute: " + paramName + "=" + paramValue);
                        allParam.put(paramName, paramValue);
                    }
                }
            }
            if (!existJob) {
                throw new Exception("..job [" + jobname + "] not found in configuration file: " + configFile);
            }
            getParameters();
        } catch (Exception e) {
            throw new Exception("..error occurred processing job parameters: " + e.getMessage(), e);
        }
    }

    public void setParameters(HashMap allParams_) throws Exception {
        try {
            this.allParam = allParams_;
            getParameters();
        } catch (Exception e) {
            throw new Exception("..error occurred processing job parameters: " + e.getMessage(), e);
        }
    }

    private void getParameters() throws Exception {
        sosLogger.debug3("get parameters ...");
        try {

            if (sosString.parseToString(schedulerLauncherHost).isEmpty()) {
                if (!sosString.parseToString(allParam, "scheduler_launcher_host").isEmpty()) {
                    schedulerLauncherHost = sosString.parseToString(allParam, "scheduler_launcher_host");
                    sosLogger.debug3("..parameter[scheduler_launcher_host] = " + schedulerLauncherHost);
                }
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_port").isEmpty()) {
                schedulerLauncherPort = Integer.parseInt(sosString.parseToString(allParam, "scheduler_launcher_port"));
                sosLogger.debug3("..parameter[scheduler_launcher_port] = " + schedulerLauncherPort);
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_protocol").isEmpty()) {
                schedulerLauncherProtocol = sosString.parseToString(allParam, "scheduler_launcher_protocol");
                sosLogger.debug3("..parameter[scheduler_launcher_protocol] = " + schedulerLauncherProtocol);
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_min_starts").isEmpty()) {
                schedulerLauncherMinStarts = Integer.parseInt(sosString.parseToString(allParam, "scheduler_launcher_min_starts"));
                sosLogger.debug3("..parameter[scheduler_launcher_min_starts] = " + schedulerLauncherMinStarts);
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_start_increment").isEmpty()) {
                sosLogger.debug3("..parameter[scheduler_launcher_start_increment] = " + schedulerLauncherStartIncrement);
                if (sosString.parseToString(allParam, "scheduler_launcher_start_increment").trim().startsWith("+")
                        || sosString.parseToString(allParam, "scheduler_launcher_start_increment").trim().startsWith("*")) {
                    schedulerLauncherStartIncrement = Integer.parseInt(sosString.parseToString(allParam, "scheduler_launcher_start_increment").trim()
                            .substring(1));
                    schedulerLauncherStartIncrementFactor = sosString.parseToString(allParam, "scheduler_launcher_start_increment").trim().substring(0, 1);
                } else {
                    schedulerLauncherStartIncrement = Integer.parseInt(sosString.parseToString(allParam, "scheduler_launcher_start_increment"));
                }
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_max_starts").isEmpty()) {
                this.schedulerLauncherMaxStarts = Integer.parseInt(sosString.parseToString(allParam, "scheduler_launcher_max_starts"));
                sosLogger.debug3("..parameter[scheduler_launcher_max_starts] = " + schedulerLauncherMaxStarts);
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_interval").isEmpty()) {
                schedulerLauncherInterval = Integer.parseInt(sosString.parseToString(allParam, "scheduler_launcher_interval"));
                sosLogger.debug3("..parameter[scheduler_launcher_interval] = " + schedulerLauncherInterval);
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_job").isEmpty()) {
                schedulerLauncherJob = sosString.parseToString(allParam, "scheduler_launcher_job");
                sosLogger.debug3("..parameter[scheduler_launcher_job] = " + schedulerLauncherJob);
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_job_after").isEmpty()) {
                schedulerLauncherJobAfter = sosString.parseToString(allParam, "scheduler_launcher_job_after");
                sosLogger.debug3("..parameter[scheduler_launcher_job_after] = " + schedulerLauncherJobAfter);
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_job_at").isEmpty()) {
                schedulerLauncherJobAt = sosString.parseToString(allParam, "scheduler_launcher_job_at");
                sosLogger.debug3("..parameter[scheduler_launcher_job_at] = " + schedulerLauncherJobAt);
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_duration").isEmpty()) {
                schedulerLauncherDuration = Long.parseLong(sosString.parseToString(allParam, "scheduler_launcher_duration"));
                sosLogger.debug3("..parameter[scheduler_launcher_duration] = " + schedulerLauncherDuration);
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_job_web_service").isEmpty()) {
                schedulerLauncherJobWebService = sosString.parseToString(allParam, "scheduler_launcher_job_web_service");
                sosLogger.debug3("..parameter[scheduler_launcher_web_service] = " + schedulerLauncherJobWebService);
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_order").isEmpty()) {
                schedulerLauncherOrder = sosString.parseToString(allParam, "scheduler_launcher_order");
                sosLogger.debug3("..parameter[scheduler_launcher_order] = " + schedulerLauncherOrder);
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_order_replace").isEmpty()) {
                schedulerLauncherOrderReplace = sosString.parseToBoolean(sosString.parseToString(allParam, "scheduler_launcher_order_replace"));
                sosLogger.debug3("..parameter[scheduler_launcher_order_replace] = " + schedulerLauncherOrderReplace);
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_order_job_chain").isEmpty()) {
                schedulerLauncherOrderJobChain = sosString.parseToString(allParam, "scheduler_launcher_order_job_chain");
                sosLogger.debug3("..parameter[scheduler_launcher_order_job_chain] = " + schedulerLauncherOrderJobChain);
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_order_state").isEmpty()) {
                schedulerLauncherOrderState = sosString.parseToString(allParam, "scheduler_launcher_order_state");
                sosLogger.debug3("..parameter[scheduler_launcher_order_state] = " + schedulerLauncherOrderState);
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_order_title").isEmpty()) {
                schedulerLauncherOrderTitle = sosString.parseToString(allParam, "scheduler_launcher_order_title");
                sosLogger.debug3("..parameter[scheduler_launcher_title] = " + schedulerLauncherOrderTitle);
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_order_at").isEmpty()) {
                schedulerLauncherOrderAt = sosString.parseToString(allParam, "scheduler_launcher_order_at");
                sosLogger.debug3("..parameter[scheduler_launcher_order_at] = " + schedulerLauncherOrderAt);
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_order_priority").isEmpty()) {
                schedulerLauncherOrderPriority = sosString.parseToString(allParam, "scheduler_launcher_order_priority");
                sosLogger.debug3("..parameter[scheduler_launcher_order_priority] = " + schedulerLauncherOrderPriority);
            }
            if (!sosString.parseToString(allParam, "scheduler_launcher_order_web_service").isEmpty()) {
                schedulerLauncherOrderWebService = sosString.parseToString(allParam, "scheduler_launcher_order_web_service");
                sosLogger.debug3("..parameter[scheduler_launcher_order_web_service] = " + schedulerLauncherOrderWebService);
            }
        } catch (Exception e) {
            throw new Exception("..error occurred processing job parameters: " + e.getMessage(), e);
        }
    }

    private String getRequest() throws Exception {
        String request = "";
        try {
            if (!sosString.parseToString(schedulerLauncherOrder).isEmpty()) {
                request = "<add_order";
                request += " replace=\"" + (this.schedulerLauncherOrderReplace ? "yes" : "no") + "\"";
                if (!sosString.parseToString(this.schedulerLauncherOrder).isEmpty()) {
                    request += " id=\"" + schedulerLauncherOrder + "\"";
                }
                if (!sosString.parseToString(this.schedulerLauncherOrderAt).isEmpty()) {
                    request += " at=\"" + this.schedulerLauncherOrderAt + "\"";
                } else {
                    request += " at=\"now + " + this.schedulerLauncherInterval + "\"";
                }
                this.schedulerLauncherJobAt = schedulerLauncherOrderAt;
                if (!sosString.parseToString(this.schedulerLauncherOrderJobChain).isEmpty()) {
                    request += " job_chain=\"" + this.schedulerLauncherOrderJobChain + "\"";
                }
                if (!sosString.parseToString(this.schedulerLauncherOrderPriority).isEmpty()) {
                    request += " priority=\"" + this.schedulerLauncherOrderPriority + "\"";
                }
                if (!sosString.parseToString(this.schedulerLauncherOrderState).isEmpty()) {
                    request += " state=\"" + this.schedulerLauncherOrderState + "\"";
                }
                if (!sosString.parseToString(this.schedulerLauncherOrderTitle).isEmpty()) {
                    request += " title=\"" + this.schedulerLauncherOrderTitle + "\"";
                }
                if (!sosString.parseToString(this.schedulerLauncherOrderWebService).isEmpty()) {
                    request += " web_service=\"" + schedulerLauncherOrderWebService + "\"";
                }
                request += ">";
                request += "<params>";
                Object[] params = this.allParam.entrySet().toArray();
                for (int i = 0; i < params.length; i++) {
                    if (!sosString.parseToString(params[i]).startsWith("scheduler_launcher_")) {
                        request +=
                                "<param name=\"" + sosString.parseToString(params[i]).split("=")[0] + "\" value=\""
                                        + sosString.parseToString(params[i]).split("=")[1] + "\"/>";
                    }
                }
                request += "</params>";
                request += "</add_order>";
            } else if (!sosString.parseToString(this.schedulerLauncherJob).isEmpty()) {
                request = "<start_job job=\"" + schedulerLauncherJob + "\"";
                if (!sosString.parseToString(schedulerLauncherJobAfter).isEmpty()) {
                    request += " after=\"" + this.schedulerLauncherJobAfter + "\"";
                }
                if (!sosString.parseToString(schedulerLauncherJobAt).isEmpty()) {
                    request += " at=\"" + schedulerLauncherJobAt + "\"";
                } else {
                    request += " at=\"now + " + this.schedulerLauncherInterval + "\"";
                }
                if (!sosString.parseToString(this.schedulerLauncherJobWebService).isEmpty()) {
                    request += " web_service=\"" + schedulerLauncherJobWebService + "\"";
                }
                request += ">";
                request += "<params>";
                Object[] params = this.allParam.entrySet().toArray();
                for (int i = 0; i < params.length; i++) {
                    if (!sosString.parseToString(params[i]).startsWith("scheduler_launcher_")) {
                        request +=
                                "<param name=\"" + sosString.parseToString(params[i]).split("=")[0] + "\" value=\""
                                        + sosString.parseToString(params[i]).split("=")[1] + "\"/>";
                    }
                }
                request += "</params>";
                request += "</start_job>";
            }
            sosLogger.debug("request: " + request);
            return request;
        } catch (Exception e) {
            throw new Exception("..error in " + SOSClassUtil.getClassName() + ": " + e.getMessage(), e);
        }
    }

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
            while (loop) {
                sosLogger.info("..sending request to job scheduler [" + this.schedulerLauncherHost + ":" + this.schedulerLauncherPort + "]: "
                        + request);
                if (System.currentTimeMillis() > this.terminateTimeInSec) {
                    sosLogger.debug("..time until termination: " + terminateTimeInSec);
                    loop = false;
                    break;
                }
                for (int i = 0; i < schedulerLauncherMinStarts; i++) {
                    remoteCommand.sendRequest(request);
                    counter++;
                    if ("tcp".equalsIgnoreCase(this.schedulerLauncherProtocol)) {
                        response = remoteCommand.getResponse();
                        SOSXMLXPath xpath = new SOSXMLXPath(new StringBuffer(response));
                        String errCode = xpath.selectSingleNodeValue("//ERROR/@code");
                        String errMessage = xpath.selectSingleNodeValue("//ERROR/@text");
                        sosLogger.info("..job scheduler response: " + response);
                        if ((errCode != null && !errCode.isEmpty()) || (errMessage != null && !errMessage.isEmpty())) {
                            sosLogger.warn("..job scheduler response reports error message: " + errMessage + " [" + errCode + "]");
                            counterError++;
                        }
                    }
                }
                schedulerLauncherMinStarts = this.startIncrement(schedulerLauncherMinStarts);
            }
            sosLogger.info("..number of jobs launched: " + counter);
            stateText = "..number of jobs launched: " + (counter) + "(error=" + (counterError) + ";success=" + (counter - counterError) + ")";
            showSummary(counter, counterError, timeInSec);
        } catch (Exception e) {
            stateText =
                    "..number of jobs launched: " + (counter) + "(error=" + counterError + ";success=" + (counter - counterError) + ")"
                            + e.getMessage();
            sosLogger.info("..error in " + SOSClassUtil.getClassName() + ": " + e.getMessage());
            throw new Exception("..error in " + SOSClassUtil.getClassName() + ": " + e.getMessage(), e);
        } finally {
            if (remoteCommand != null) {
                try {
                    remoteCommand.disconnect();
                } catch (Exception x) {
                    // gracefully ignore this error
                }
            }
        }
    }

    private void checkParams() throws Exception {
        int oneOfUs = 0;
        try {
            oneOfUs += sosString.parseToString(schedulerLauncherJob).isEmpty() ? 0 : 1;
            oneOfUs += sosString.parseToString(this.schedulerLauncherOrder).isEmpty() ? 0 : 1;
            if (oneOfUs == 0) {
                throw new Exception("one of the parameters [scheduler_launcher_job, scheduler_launcher_order] must be specified");
            } else if (oneOfUs > 1) {
                throw new Exception("only one of the parameters [scheduler_launcher_job, scheduler_launcher_order] must be specified, " + oneOfUs
                        + " were given");
            }
        } catch (Exception e) {
            throw new Exception("..error in " + SOSClassUtil.getClassName() + ": " + e.getMessage(), e);
        }
    }

    private int startIncrement(int i) throws Exception {
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
            throw new Exception("..error in " + SOSClassUtil.getClassName() + ": " + e.getMessage(), e);
        }
    }

    public void showSummary(int counter, int counterError, long timeInSec) throws Exception {
        try {
            sosLogger.debug5("..end time in miliseconds: " + System.currentTimeMillis());
            sosLogger.info("---------------------------------------------------------------");
            sosLogger.info("..number of job starts                            : " + counter);
            sosLogger.info("..number of jobs processed successfully           : " + (counter - counterError));
            sosLogger.info("..number of jobs processed with errors            : " + counterError);
            sosLogger.info("..time elapsed in seconds                         : " + Math.round((System.currentTimeMillis() - timeInSec) / 1000) + "s");
            sosLogger.info("---------------------------------------------------------------");
        } catch (Exception e) {
            throw new Exception("..error occurred in " + SOSClassUtil.getMethodName() + ": " + e.getMessage(), e);
        }
    }

    public String getStateText() {
        return stateText;
    }

    /** Diese Klasse kann über die Kommadozeile aufgerufen werden, z.B. kann die
     * Kommando-Datei wie folgt aussehen:
     * 
     * *************************************************************************
     * ********* rem @echo off rem
     * ----------------------------------------------
     * ----------------------------- rem Start script for the
     * sos.scheduler.launcher.JobSchedulerLoadTestLauncher rem
     * ------------------
     * ---------------------------------------------------------
     *
     * set LIB_PATH=C:\scheduler.launcher set
     * CLASS_PATH=.;%LIB_PATH%;%LIB_PATH%\
     * lib\sos.mail.jar;%LIB_PATH%\lib\sos.util
     * .jar;%LIB_PATH%\lib\sos.xml.jar;%LIB_PATH
     * %\lib\xercesImpl.jar;%LIB_PATH%\lib\xml-apis.jar;%LIB_PATH%\lib\xalan.jar
     * 
     * rem run the command java -cp "%CLASS_PATH%"
     * sos.scheduler.launcher.JobSchedulerLoadTestLauncher
     * -config=J:\E\java\mo\sos.scheduler.job\src\test_xmls\job.xml
     * -host=localhost -port=4373 > launcher.log
     * 
     * *************************************************************************
     * *********
     * 
     * @param args */
    public static void main(String[] args) {
        String configFile = "";
        String host = "";
        int port = -1;
        String job = "";
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
                System.err.println("java -cp=. sos.scheduler.launcher.JobSchedulerLoadTestLauncher -config=<xml configuration file> -host=<host> "
                        + "-port=<port> -job=<job name>");
                System.exit(0);
            }
            sos.util.SOSStandardLogger sosLogger = new sos.util.SOSStandardLogger(9);
            JobSchedulerLoadTestLauncher launcher = null;
            for (int i = 0; i < args.length; i++) {
                String[] currArg = args[i].split("=");
                if ("-config".equalsIgnoreCase(currArg[0])) {
                    configFile = currArg[1];
                } else if ("-host".equalsIgnoreCase(currArg[0])) {
                    host = currArg[1];
                } else if ("-port".equalsIgnoreCase(currArg[0])) {
                    if (!sosString.parseToString(currArg[1]).isEmpty()) {
                        port = Integer.parseInt(currArg[1]);
                    }
                } else if ("-job".equalsIgnoreCase(currArg[0])) {
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
