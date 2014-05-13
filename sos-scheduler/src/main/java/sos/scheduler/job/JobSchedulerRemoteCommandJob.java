package sos.scheduler.job;


import org.apache.log4j.Logger;

import sos.scheduler.command.SOSSchedulerCommand;
import sos.spooler.Variable_set;
import sos.xml.SOSXMLXPath;

import com.sos.JSHelper.Exceptions.JobSchedulerException;


public class JobSchedulerRemoteCommandJob extends JobSchedulerJobAdapter {

	@SuppressWarnings("unused")
	private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String conSVNVersion = "$Id$";
	@SuppressWarnings("unused")
	private final Logger logger = Logger.getLogger(this.getClass());
    /* remote execution parameters */
    String host = "";
    int port = 0;
    int timeout = 0;
    String protocol = "";

    /* routing parameters */    
    String command = "";
    String jobName = "";
    String orderId = "";
    
    /* parameters for jobs and orders */
    String at = "";
    String webService = "";
    
    /* parameters for jobs only */
    String after = "";
   
    /* parameters for orders only */
    boolean replace = true;
    String jobChain = "";
    String priority = "";
    String state = "";
    String title = "";
    String runTime = "";
   
    
    
    public void initParameters() {
        
        /* remote execution parameters */
        this.setHost("localhost");
        this.setPort(4444);
        this.setProtocol("tcp");
        this.setTimeout(60);

        /* routing parameters */
        this.setJobName("");
        this.setOrderId("");
        this.setCommand("");

        /* parameters for jobs and orders */
        this.setAt("");
        this.setWebService("");
        
        /* parameters for jobs only */
        this.setAfter("");
       
        /* parameters for orders only */
        this.setReplace(true);
        this.setJobChain("");
        this.setPriority("");
        this.setState("");
        this.setTitle("");
        this.setRunTime("");
    }
    
    
    public void getTaskParameters(final boolean logValue) {
        
        try { 
            if (spooler_task.params().var("scheduler_remote_host") != null && spooler_task.params().var("scheduler_remote_host").length() > 0) {
                this.setHost( spooler_task.params().var("scheduler_remote_host"));
                if (logValue) spooler_log.info(".. job parameter [scheduler_remote_host]: " + this.getHost());
            }

            if (spooler_task.params().var("scheduler_remote_port") != null && spooler_task.params().var("scheduler_remote_port").length() > 0) {
                try {
                  this.setPort(Integer.parseInt(spooler_task.params().var("scheduler_remote_port")));
                  if (logValue) spooler_log.info(".. job parameter [scheduler_remote_port]: " + this.getPort());
                } catch (Exception e) {
                    throw new JobSchedulerException("illegal value specified for parameter [scheduler_remote_port], numeric value expected, found: " + spooler_task.params().var("scheduler_remote_port"), e);
                }
            }
                    

            if (spooler_task.params().var("scheduler_remote_protocol") != null && spooler_task.params().var("scheduler_remote_protocol").length() > 0) {
                if (!spooler_task.params().var("scheduler_remote_protocol").equalsIgnoreCase("tcp") && !spooler_task.params().var("scheduler_remote_protocol").equalsIgnoreCase("udp")) {
                    throw new JobSchedulerException("illegal value specified for parameter [scheduler_remote_protocol], \"tcp\" or \"udp\" expected, found: " + spooler_task.params().var("scheduler_remote_protocol"));
                }
                this.setProtocol(spooler_task.params().var("scheduler_remote_protocol"));
                if (logValue) spooler_log.info(".. job parameter [scheduler_remote_protocol]: " + this.getProtocol());
            }

            if (spooler_task.params().var("scheduler_remote_timeout") != null && spooler_task.params().var("scheduler_remote_timeout").length() > 0) {
                try {
                    this.setTimeout(Integer.parseInt(spooler_task.params().var("scheduler_remote_timeout")));
                    if (logValue) spooler_log.info(".. job parameter [scheduler_remote_timeout]: " + this.getTimeout());
                } catch (Exception e) {
                    throw new JobSchedulerException("illegal value specified for parameter [scheduler_remote_timeout], numeric value expected, found: " + spooler_task.params().var("scheduler_remote_timeout"),e);
                }
            }

            
            /* routing parameters */
            
            if (spooler_task.params().var("scheduler_remote_job") != null && spooler_task.params().var("scheduler_remote_job").length() > 0) {
                this.setJobName(spooler_task.params().var("scheduler_remote_job"));
                if (logValue) spooler_log.info(".. job parameter [scheduler_remote_job]: " + this.getJobName());
            }

            if (spooler_task.params().var("scheduler_remote_order") != null && spooler_task.params().var("scheduler_remote_order").length() > 0) {
                this.setOrderId(spooler_task.params().var("scheduler_remote_order"));
                if (logValue) spooler_log.info(".. job parameter [scheduler_remote_order]: " + this.getOrderId());
            }

            if (spooler_task.params().var("scheduler_remote_command") != null && spooler_task.params().var("scheduler_remote_command").length() > 0) {
                this.setCommand(spooler_task.params().var("scheduler_remote_command"));
                if (logValue) spooler_log.info(".. job parameter [scheduler_remote_command]: " + this.getCommand());
            }

            
            /* parameters for jobs and orders */
            
            if (spooler_task.params().var("scheduler_remote_start_at") != null && spooler_task.params().var("scheduler_remote_start_at").length() > 0) {
                this.setAt(spooler_task.params().var("scheduler_remote_start_at"));
                if (logValue) spooler_log.info(".. job parameter [scheduler_remote_start_at]: " + this.getAt());
            }

            if (spooler_task.params().var("scheduler_remote_web_service") != null && spooler_task.params().var("scheduler_remote_web_service").length() > 0) {
                this.setWebService(spooler_task.params().var("scheduler_remote_web_service"));
                if (logValue) spooler_log.info(".. job parameter [scheduler_remote_web_service]: " + this.getWebService());
            }
            
            
            /** parameters for jobs */
            if (spooler_task.params().var("scheduler_remote_job_start_after") != null && spooler_task.params().var("scheduler_remote_job_start_after").length() > 0) {
                this.setAfter(spooler_task.params().var("scheduler_remote_job_start_after"));
                if (logValue) spooler_log.info(".. job parameter [scheduler_remote_job_start_after]: " + this.getAfter());
            }
            
            
            /* parameters for orders */
            
            if (spooler_task.params().var("scheduler_remote_order_replace") != null && spooler_task.params().var("scheduler_remote_order_replace").length() > 0) {
                if (spooler_task.params().var("scheduler_remote_order_replace").equalsIgnoreCase("yes") 
                    || spooler_task.params().var("scheduler_remote_order_replace").equalsIgnoreCase("true")
                    || spooler_task.params().var("scheduler_remote_order_replace").equals("1")) {
                    this.setReplace(true);
                } else {
                    this.setReplace(false);
                }
                if (logValue) spooler_log.info(".. job parameter [scheduler_remote_order_replace]: " + this.isReplace());
            }
            
            if (spooler_task.params().var("scheduler_remote_order_job_chain") != null && spooler_task.params().var("scheduler_remote_order_job_chain").length() > 0) {
                this.setJobChain(spooler_task.params().var("scheduler_remote_order_job_chain"));
                if (logValue) spooler_log.info(".. job parameter [scheduler_remote_order_job_chain]: " + this.getJobChain());
            }
            
            if (spooler_task.params().var("scheduler_remote_order_priority") != null && spooler_task.params().var("scheduler_remote_order_priority").length() > 0) {
                this.setPriority(spooler_task.params().var("scheduler_remote_order_priority"));
                if (logValue) spooler_log.info(".. job parameter [scheduler_remote_order_priority]: " + this.getPriority());
            }

            if (spooler_task.params().var("scheduler_remote_order_state") != null && spooler_task.params().var("scheduler_remote_order_state").length() > 0) {
                this.setState(spooler_task.params().var("scheduler_remote_order_state"));
                if (logValue) spooler_log.info(".. job parameter [scheduler_remote_order_state]: " + this.getState());
            }

            if (spooler_task.params().var("scheduler_remote_order_title") != null && spooler_task.params().var("scheduler_remote_order_title").length() > 0) {
                this.setTitle(spooler_task.params().var("scheduler_remote_order_title"));
                if (logValue) spooler_log.info(".. job parameter [scheduler_remote_order_title]: " + this.getTitle());
            }

            if (spooler_task.params().var("scheduler_remote_order_run_time") != null && spooler_task.params().var("scheduler_remote_order_run_time").length() > 0) {
                this.setRunTime(spooler_task.params().var("scheduler_remote_order_run_time"));
                if (logValue) spooler_log.info(".. job parameter [scheduler_remote_order_run_time]: " + this.getRunTime());
            }
            
        } catch (Exception e) {
            throw new JobSchedulerException("error occurred processing task parameters: " + e.getMessage(), e);
        }
    }
    
    
    public void getOrderParameters(final Variable_set params, final boolean logValue) {
        
        try { 
            if (params.var("scheduler_remote_host") != null && params.var("scheduler_remote_host").length() > 0) {
                this.setHost( params.var("scheduler_remote_host"));
                if (logValue) spooler_log.info(".. order parameter [scheduler_remote_host]: " + this.getHost());
            }

            if (params.var("scheduler_remote_port") != null && params.var("scheduler_remote_port").length() > 0) {
                try {
                  this.setPort(Integer.parseInt(params.var("scheduler_remote_port")));
                  if (logValue) spooler_log.info(".. order parameter [scheduler_remote_port]: " + this.getPort());
                } catch (Exception e) {
                    throw new JobSchedulerException("illegal value specified for parameter [scheduler_remote_port], numeric value expected, found: " + params.var("scheduler_remote_port"), e);
                }
            }
                    
            if (params.var("scheduler_remote_protocol") != null && params.var("scheduler_remote_protocol").length() > 0) {
                if (!params.var("scheduler_remote_protocol").equalsIgnoreCase("tcp") && !params.var("scheduler_remote_protocol").equalsIgnoreCase("udp")) {
                    throw new JobSchedulerException("illegal value specified for parameter [scheduler_remote_protocol], \"tcp\" or \"udp\" expected, found: " + spooler_task.params().var("scheduler_remote_protocol"));
                }
                this.setProtocol(params.var("scheduler_remote_protocol"));
                if (logValue) spooler_log.info(".. order parameter [scheduler_remote_protocol]: " + this.getProtocol());
            }

            if (params.var("scheduler_remote_timeout") != null && params.var("scheduler_remote_timeout").length() > 0) {
                try {
                    this.setTimeout(Integer.parseInt(params.var("scheduler_remote_timeout")));
                    if (logValue) spooler_log.info(".. order parameter [scheduler_remote_timeout]: " + this.getTimeout());
                } catch (Exception e) {
                    throw new JobSchedulerException("illegal value specified for parameter [scheduler_remote_timeout], numeric value expected, found: " + params.var("scheduler_remote_timeout"), e);
                }
            }

            
            /* routing parameters */
            
            if (params.var("scheduler_remote_job") != null && params.var("scheduler_remote_job").length() > 0) {
                this.setJobName(params.var("scheduler_remote_job"));
                if (logValue) spooler_log.info(".. order parameter [scheduler_remote_job]: " + this.getJobName());
            }

            if (params.var("scheduler_remote_order") != null && params.var("scheduler_remote_order").length() > 0) {
                this.setOrderId(params.var("scheduler_remote_order"));
                if (logValue) spooler_log.info(".. order parameter [scheduler_remote_order]: " + this.getOrderId());
            }

            if (params.var("scheduler_remote_command") != null && params.var("scheduler_remote_command").length() > 0) {
                this.setCommand(params.var("scheduler_remote_command"));
                if (logValue) spooler_log.info(".. order parameter [scheduler_remote_command]: " + this.getCommand());
            }

            
            /* parameters for jobs and orders */
            
            if (params.var("scheduler_remote_start_at") != null && params.var("scheduler_remote_start_at").length() > 0) {
                this.setAt(params.var("scheduler_remote_start_at"));
                if (logValue) spooler_log.info(".. order parameter [scheduler_remote_start_at]: " + this.getAt());
            }

            if (params.var("scheduler_remote_web_service") != null && params.var("scheduler_remote_web_service").length() > 0) {
                this.setWebService(params.var("scheduler_remote_web_service"));
                if (logValue) spooler_log.info(".. order parameter [scheduler_remote_web_service]: " + this.getWebService());
            }
            
            
            /* parameters for jobs */
            
            if (params.var("scheduler_remote_job_start_after") != null && params.var("scheduler_remote_job_start_after").length() > 0) {
                this.setAfter(params.var("scheduler_remote_job_start_after"));
                if (logValue) spooler_log.info(".. order parameter [scheduler_remote_job_start_after]: " + this.getAfter());
            }
            
            
            /* parameters for orders */
            
            if (params.var("scheduler_remote_order_replace") != null && params.var("scheduler_remote_order_replace").length() > 0) {
                if (params.var("scheduler_remote_order_replace").equalsIgnoreCase("yes") 
                    || params.var("scheduler_remote_order_replace").equalsIgnoreCase("true")
                    || params.var("scheduler_remote_order_replace").equals("1")) {
                    this.setReplace(true);
                } else {
                    this.setReplace(false);
                }
                if (logValue) spooler_log.info(".. order parameter [scheduler_remote_order_replace]: " + this.isReplace());
            }
            
            if (params.var("scheduler_remote_order_job_chain") != null && params.var("scheduler_remote_order_job_chain").length() > 0) {
                this.setJobChain(params.var("scheduler_remote_order_job_chain"));
                if (logValue) spooler_log.info(".. order parameter [scheduler_remote_order_job_chain]: " + this.getJobChain());
            }
            
            if (params.var("scheduler_remote_order_priority") != null && params.var("scheduler_remote_order_priority").length() > 0) {
                this.setPriority(params.var("scheduler_remote_order_priority"));
                if (logValue) spooler_log.info(".. order parameter [scheduler_remote_order_priority]: " + this.getPriority());
            }

            if (params.var("scheduler_remote_order_state") != null && params.var("scheduler_remote_order_state").length() > 0) {
                this.setState(params.var("scheduler_remote_order_state"));
                if (logValue) spooler_log.info(".. order parameter [scheduler_remote_order_state]: " + this.getState());
            }

            if (params.var("scheduler_remote_order_title") != null && params.var("scheduler_remote_order_title").length() > 0) {
                this.setTitle(params.var("scheduler_remote_order_title"));
                if (logValue) spooler_log.info(".. order parameter [scheduler_remote_order_title]: " + this.getTitle());
            }

            if (params.var("scheduler_remote_order_run_time") != null && params.var("scheduler_remote_order_run_time").length() > 0) {
                this.setRunTime(params.var("scheduler_remote_order_run_time"));
                if (logValue) spooler_log.info(".. order parameter [scheduler_remote_order_run_time]: " + this.getRunTime());
            }
            
        } catch (Exception e) {
            throw new JobSchedulerException("error occurred processing order parameters: " + e.getMessage(),e);
        }
    }
    
    
    /**
     * Job Scheduler API implementation.
     * 
     * Initializiation.
     * 
     * @return boolean
     */
    @Override
	public boolean spooler_init() {
        
        try {

            this.initParameters();
            this.getTaskParameters(true);
            
            if (spooler_job.order_queue() == null) {
                if (spooler_task.params().var("scheduler_remote_host") == null || spooler_task.params().var("scheduler_remote_host").length() == 0) {
                    throw new JobSchedulerException("no host name parameter [scheduler_remote_host] was specified for remote job scheduler");
                }
                    
                if ((spooler_task.params().var("scheduler_remote_job") == null || spooler_task.params().var("scheduler_remote_job").length() == 0)
                    && (spooler_task.params().var("scheduler_remote_order_job_chain") == null || spooler_task.params().var("scheduler_remote_order_job_chain").length() == 0)
                    && (spooler_task.params().var("scheduler_remote_command") == null || spooler_task.params().var("scheduler_remote_command").length() == 0)) {
                    throw new JobSchedulerException("one of the parameters [scheduler_remote_job, scheduler_remote_order_job_chain, scheduler_remote_command] must be specified");
                }
            }
            
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
    @Override
	public boolean spooler_process() {
    
        String request = "";
        String response = "";
        Variable_set parameters = null;
        SOSSchedulerCommand remoteCommand = null;
        
        
        try {

            parameters = spooler_task.params();
            
            if (spooler_job.order_queue() != null) {
                parameters.merge(spooler_task.order().params());
                this.initParameters();
                this.getTaskParameters(false);
                this.getOrderParameters(spooler_task.order().params(), true);
            }
            
            
            int oneOfUs = 0;
            oneOfUs += this.getJobName() == null || this.getJobName().length() == 0 ? 0 : 1;
            oneOfUs += this.getJobChain() == null || this.getJobChain().length() == 0 ? 0 : 1;
            oneOfUs += this.getCommand() == null || this.getCommand().length() == 0 ? 0 : 1;
            
            if (oneOfUs == 0) {
                throw new JobSchedulerException("one of the parameters [scheduler_remote_job, scheduler_remote_order_job_chain, scheduler_remote_command] must be specified");
            } else if (oneOfUs > 1) {
                throw new JobSchedulerException("one of the parameters [scheduler_remote_job, scheduler_remote_order_job_chain, scheduler_remote_command] must be specified, " + oneOfUs + " were given");
            }
            
            
            if (this.getJobChain() != null && this.getJobChain().length() > 0) {
                request  = "<add_order";
                request += " replace=\"" + (this.isReplace() ? "yes" : "no") + "\"";
                if (this.getOrderId() != null && this.getOrderId().length() > 0) request += " id=\"" + this.getOrderId() + "\"";
                if (this.getAt() != null && this.getAt().length() > 0) request += " at=\"" + this.getAt() + "\"";
                if (this.getJobChain() != null && this.getJobChain().length() > 0) request += " job_chain=\"" + this.getJobChain() + "\"";
                if (this.getPriority() != null && this.getPriority().length() > 0) request += " priority=\"" + this.getPriority() + "\"";
                if (this.getState() != null && this.getState().length() > 0) request += " state=\"" + this.getState() + "\"";
                if (this.getTitle() != null && this.getTitle().length() > 0) request += " title=\"" + this.getTitle() + "\"";
                if (this.getWebService() != null && this.getWebService().length() > 0) request += " web_service=\"" + this.getWebService() + "\"";
                request += ">";
                request += writeParamsXML(parameters);
                if (this.getRunTime() != null && this.getRunTime().length() > 0) request += this.getRunTime();
                request += "</add_order>";

            } else if (this.getJobName() != null && this.getJobName().length() > 0) {
                request  = "<start_job job=\"" + this.getJobName() + "\"";
                if (this.getAfter() != null && this.getAfter().length() > 0) request += " after=\"" + this.getAfter() + "\"";
                if (this.getAt() != null && this.getAt().length() > 0) request += " at=\"" + this.getAt() + "\"";
                if (this.getWebService() != null && this.getWebService().length() > 0) request += " web_service=\"" + this.getWebService() + "\"";
                request += ">";
                request += writeParamsXML(parameters);
                request += "</start_job>";

            } else {
                request = this.getCommand();
            }
            
            
            remoteCommand = new SOSSchedulerCommand(this.getHost(), this.getPort(), this.getProtocol());
            remoteCommand.setTimeout(timeout);
            remoteCommand.connect();
            

            spooler_log.info("sending request to remote Job Scheduler [" + this.getHost() + ":" + this.getPort() + "]: " + request);
            remoteCommand.sendRequest(request);
            
            
            if (this.getProtocol().equalsIgnoreCase("tcp")) { // no response is returned for UDP messages
                response = remoteCommand.getResponse();
                SOSXMLXPath xpath = new SOSXMLXPath(new StringBuffer(response));
                String errCode = xpath.selectSingleNodeValue("//ERROR/@code");
                String errMessage = xpath.selectSingleNodeValue("//ERROR/@text");
                spooler_log.info("remote job scheduler response: " + response);

                if (errCode != null && errCode.length() > 0 || errMessage != null && errMessage.length() > 0) {
                    //spooler_log.warn("remote Job Scheduler response reports error message: " + errMessage + " [" + errCode + "]");
                    throw new JobSchedulerException(String.format("remote JobScheduler response reports error message: %1$s [%2$s]",errMessage,errCode));
                }
            }
            
            return spooler_job.order_queue() != null;
            
        } catch (Exception e) {
            spooler_log.error("error occurred for remote execution: " + e.getMessage());
            return false;
        } finally {
            if (remoteCommand != null) { try { remoteCommand.disconnect(); } catch (Exception x) {} } // gracefully ignore this error
        }
        
    }
    
    
    /**
     * @return Returns the params xml.
     */
    private String writeParamsXML(final Variable_set parameters) {
    	String paramsXml = "<params>";
    	String[] params = parameters.names().split(";");
        for(int i=0; i<params.length; i++) {
            if (!params[i].startsWith("scheduler_remote_")) {
//            	paramsXml += "<param name=\"" + params[i] + "\" value=\"" + JSToolBox.escapeHTML(parameters.var(params[i])) + "\"/>";
            	paramsXml += "<param name=\"" + params[i] + "\" value=\"" + parameters.var(params[i]).replaceAll("&","&amp;") + "\"/>";
            }
        }
        paramsXml += "</params>";
    	return paramsXml;
    }
    
    
    /**
     * @return Returns the after.
     */
    public String getAfter() {
        return after;
    }



    /**
     * @param after The after to set.
     */
    public void setAfter(final String after) {
        this.after = after;
    }



    /**
     * @return Returns the at.
     */
    public String getAt() {
        return at;
    }



    /**
     * @param at The at to set.
     */
    public void setAt(final String at) {
        this.at = at;
    }



    /**
     * @return Returns the command.
     */
    public String getCommand() {
        return command;
    }



    /**
     * @param command The command to set.
     */
    public void setCommand(final String command) {
        this.command = command;
    }



    /**
     * @return Returns the host.
     */
    public String getHost() {
        return host;
    }



    /**
     * @param host The host to set.
     */
    public void setHost(final String host) {
        this.host = host;
    }



    /**
     * @return Returns the jobChain.
     */
    public String getJobChain() {
        return jobChain;
    }



    /**
     * @param jobChain The jobChain to set.
     */
    public void setJobChain(final String jobChain) {
        this.jobChain = jobChain;
    }



    /**
     * @return Returns the jobName.
     */
    @Override
	public String getJobName() {
        return jobName;
    }



    /**
     * @param jobName The jobName to set.
     */
    @Override
	public void setJobName(final String jobName) {
        this.jobName = jobName;
    }



    /**
     * @return Returns the orderId.
     */
    public String getOrderId() {
        return orderId;
    }



    /**
     * @param orderId The orderId to set.
     */
    public void setOrderId(final String orderId) {
        this.orderId = orderId;
    }



    /**
     * @return Returns the port.
     */
    public int getPort() {
        return port;
    }



    /**
     * @param port The port to set.
     */
    public void setPort(final int port) {
        this.port = port;
    }



    /**
     * @return Returns the priority.
     */
    public String getPriority() {
        return priority;
    }



    /**
     * @param priority The priority to set.
     */
    public void setPriority(final String priority) {
        this.priority = priority;
    }



    /**
     * @return Returns the protocol.
     */
    public String getProtocol() {
        return protocol;
    }



    /**
     * @param protocol The protocol to set.
     */
    public void setProtocol(final String protocol) {
        
        if ( protocol == null || protocol.length() == 0)
            throw new JobSchedulerException("no value was given for protocol [tcp, udp]");

        if (!protocol.equalsIgnoreCase("tcp") && !protocol.equalsIgnoreCase("udp"))
            throw new JobSchedulerException ("illegal value specified for protocol [tcp, udp], found: " + protocol);
        
        this.protocol = protocol.toLowerCase();
    }



    /**
     * @return Returns the runTime.
     */
    public String getRunTime() {
        return runTime;
    }



    /**
     * @param runTime The runTime to set.
     */
    public void setRunTime(final String runTime) {
        this.runTime = runTime;
    }



    /**
     * @return Returns the state.
     */
    public String getState() {
        return state;
    }



    /**
     * @param state The state to set.
     */
    public void setState(final String state) {
        this.state = state;
    }



    /**
     * @return Returns the timeout.
     */
    public int getTimeout() {
        return timeout;
    }



    /**
     * @param timeout The timeout to set.
     */
    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }



    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }



    /**
     * @param title The title to set.
     */
    public void setTitle(final String title) {
        this.title = title;
    }



    /**
     * @return Returns the webService.
     */
    public String getWebService() {
        return webService;
    }



    /**
     * @param webService The webService to set.
     */
    public void setWebService(final String webService) {
        this.webService = webService;
    }


    /**
     * @return Returns the replace.
     */
    public boolean isReplace() {
        return replace;
    }


    /**
     * @param replace The replace to set.
     */
    public void setReplace(final boolean replace) {
        this.replace = replace;
    }
 
}
