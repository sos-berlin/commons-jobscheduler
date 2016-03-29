package sos.scheduler.job;

import sos.scheduler.command.SOSSchedulerCommand;
import sos.spooler.Variable_set;
import sos.xml.SOSXMLXPath;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerRemoteCommandJob extends JobSchedulerJobAdapter {

    String host = "";
    int port = 0;
    int timeout = 0;
    String protocol = "";
    String command = "";
    String jobName = "";
    String orderId = "";
    String at = "";
    String webService = "";
    String after = "";
    boolean replace = true;
    String jobChain = "";
    String priority = "";
    String state = "";
    String title = "";
    String runTime = "";

    public void initParameters() {
        this.setHost("localhost");
        this.setPort(4444);
        this.setProtocol("tcp");
        this.setTimeout(60);
        this.setJobName("");
        this.setOrderId("");
        this.setCommand("");
        this.setAt("");
        this.setWebService("");
        this.setAfter("");
        this.setReplace(true);
        this.setJobChain("");
        this.setPriority("");
        this.setState("");
        this.setTitle("");
        this.setRunTime("");
    }

    public void getTaskParameters(final boolean logValue) {
        try {
            if (spooler_task.params().var("scheduler_remote_host") != null && !spooler_task.params().var("scheduler_remote_host").isEmpty()) {
                this.setHost(spooler_task.params().var("scheduler_remote_host"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_host]: " + this.getHost());
                }
            }
            if (spooler_task.params().var("scheduler_remote_port") != null && !spooler_task.params().var("scheduler_remote_port").isEmpty()) {
                try {
                    this.setPort(Integer.parseInt(spooler_task.params().var("scheduler_remote_port")));
                    if (logValue) {
                        spooler_log.info(".. job parameter [scheduler_remote_port]: " + this.getPort());
                    }
                } catch (Exception e) {
                    throw new JobSchedulerException("illegal value specified for parameter [scheduler_remote_port], numeric value expected, found: "
                            + spooler_task.params().var("scheduler_remote_port"), e);
                }
            }
            if (spooler_task.params().var("scheduler_remote_protocol") != null && !spooler_task.params().var("scheduler_remote_protocol").isEmpty()) {
                if (!"tcp".equalsIgnoreCase(spooler_task.params().var("scheduler_remote_protocol"))
                        && !"udp".equalsIgnoreCase(spooler_task.params().var("scheduler_remote_protocol"))) {
                    throw new JobSchedulerException("illegal value specified for parameter [scheduler_remote_protocol], \"tcp\" or \"udp\" expected, found: "
                            + spooler_task.params().var("scheduler_remote_protocol"));
                }
                this.setProtocol(spooler_task.params().var("scheduler_remote_protocol"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_protocol]: " + this.getProtocol());
                }
            }
            if (spooler_task.params().var("scheduler_remote_timeout") != null && !spooler_task.params().var("scheduler_remote_timeout").isEmpty()) {
                try {
                    this.setTimeout(Integer.parseInt(spooler_task.params().var("scheduler_remote_timeout")));
                    if (logValue) {
                        spooler_log.info(".. job parameter [scheduler_remote_timeout]: " + this.getTimeout());
                    }
                } catch (Exception e) {
                    throw new JobSchedulerException("illegal value specified for parameter [scheduler_remote_timeout], numeric value expected, found: "
                            + spooler_task.params().var("scheduler_remote_timeout"), e);
                }
            }
            if (spooler_task.params().var("scheduler_remote_job") != null && !spooler_task.params().var("scheduler_remote_job").isEmpty()) {
                this.setJobName(spooler_task.params().var("scheduler_remote_job"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_job]: " + this.getJobName());
                }
            }
            if (spooler_task.params().var("scheduler_remote_order") != null && !spooler_task.params().var("scheduler_remote_order").isEmpty()) {
                this.setOrderId(spooler_task.params().var("scheduler_remote_order"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_order]: " + this.getOrderId());
                }
            }
            if (spooler_task.params().var("scheduler_remote_command") != null && !spooler_task.params().var("scheduler_remote_command").isEmpty()) {
                this.setCommand(spooler_task.params().var("scheduler_remote_command"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_command]: " + this.getCommand());
                }
            }
            if (spooler_task.params().var("scheduler_remote_start_at") != null && !spooler_task.params().var("scheduler_remote_start_at").isEmpty()) {
                this.setAt(spooler_task.params().var("scheduler_remote_start_at"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_start_at]: " + this.getAt());
                }
            }
            if (spooler_task.params().var("scheduler_remote_web_service") != null && !spooler_task.params().var("scheduler_remote_web_service").isEmpty()) {
                this.setWebService(spooler_task.params().var("scheduler_remote_web_service"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_web_service]: " + this.getWebService());
                }
            }
            if (spooler_task.params().var("scheduler_remote_job_start_after") != null
                    && !spooler_task.params().var("scheduler_remote_job_start_after").isEmpty()) {
                this.setAfter(spooler_task.params().var("scheduler_remote_job_start_after"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_job_start_after]: " + this.getAfter());
                }
            }
            if (spooler_task.params().var("scheduler_remote_order_replace") != null && !spooler_task.params().var("scheduler_remote_order_replace").isEmpty()) {
                if ("yes".equalsIgnoreCase(spooler_task.params().var("scheduler_remote_order_replace"))
                        || "true".equalsIgnoreCase(spooler_task.params().var("scheduler_remote_order_replace"))
                        || "1".equals(spooler_task.params().var("scheduler_remote_order_replace"))) {
                    this.setReplace(true);
                } else {
                    this.setReplace(false);
                }
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_order_replace]: " + this.isReplace());
                }
            }
            if (spooler_task.params().var("scheduler_remote_order_job_chain") != null
                    && !spooler_task.params().var("scheduler_remote_order_job_chain").isEmpty()) {
                this.setJobChain(spooler_task.params().var("scheduler_remote_order_job_chain"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_order_job_chain]: " + this.getJobChain());
                }
            }
            if (spooler_task.params().var("scheduler_remote_order_priority") != null && !spooler_task.params().var("scheduler_remote_order_priority").isEmpty()) {
                this.setPriority(spooler_task.params().var("scheduler_remote_order_priority"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_order_priority]: " + this.getPriority());
                }
            }
            if (spooler_task.params().var("scheduler_remote_order_state") != null && !spooler_task.params().var("scheduler_remote_order_state").isEmpty()) {
                this.setState(spooler_task.params().var("scheduler_remote_order_state"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_order_state]: " + this.getState());
                }
            }
            if (spooler_task.params().var("scheduler_remote_order_title") != null && !spooler_task.params().var("scheduler_remote_order_title").isEmpty()) {
                this.setTitle(spooler_task.params().var("scheduler_remote_order_title"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_order_title]: " + this.getTitle());
                }
            }
            if (spooler_task.params().var("scheduler_remote_order_run_time") != null && !spooler_task.params().var("scheduler_remote_order_run_time").isEmpty()) {
                this.setRunTime(spooler_task.params().var("scheduler_remote_order_run_time"));
                if (logValue) {
                    spooler_log.info(".. job parameter [scheduler_remote_order_run_time]: " + this.getRunTime());
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException("error occurred processing task parameters: " + e.getMessage(), e);
        }
    }

    public void getOrderParameters(final Variable_set params, final boolean logValue) {
        try {
            if (params.var("scheduler_remote_host") != null && !params.var("scheduler_remote_host").isEmpty()) {
                this.setHost(params.var("scheduler_remote_host"));
                if (logValue) {
                    spooler_log.info(".. order parameter [scheduler_remote_host]: " + this.getHost());
                }
            }
            if (params.var("scheduler_remote_port") != null && !params.var("scheduler_remote_port").isEmpty()) {
                try {
                    this.setPort(Integer.parseInt(params.var("scheduler_remote_port")));
                    if (logValue) {
                        spooler_log.info(".. order parameter [scheduler_remote_port]: " + this.getPort());
                    }
                } catch (Exception e) {
                    throw new JobSchedulerException("illegal value specified for parameter [scheduler_remote_port], numeric value expected, found: "
                            + params.var("scheduler_remote_port"), e);
                }
            }
            if (params.var("scheduler_remote_protocol") != null && !params.var("scheduler_remote_protocol").isEmpty()) {
                if (!"tcp".equalsIgnoreCase(params.var("scheduler_remote_protocol")) && !"udp".equalsIgnoreCase(params.var("scheduler_remote_protocol"))) {
                    throw new JobSchedulerException("illegal value specified for parameter [scheduler_remote_protocol], \"tcp\" or \"udp\" expected, found: "
                            + spooler_task.params().var("scheduler_remote_protocol"));
                }
                this.setProtocol(params.var("scheduler_remote_protocol"));
                if (logValue) {
                    spooler_log.info(".. order parameter [scheduler_remote_protocol]: " + this.getProtocol());
                }
            }
            if (params.var("scheduler_remote_timeout") != null && !params.var("scheduler_remote_timeout").isEmpty()) {
                try {
                    this.setTimeout(Integer.parseInt(params.var("scheduler_remote_timeout")));
                    if (logValue) {
                        spooler_log.info(".. order parameter [scheduler_remote_timeout]: " + this.getTimeout());
                    }
                } catch (Exception e) {
                    throw new JobSchedulerException("illegal value specified for parameter [scheduler_remote_timeout], numeric value expected, found: "
                            + params.var("scheduler_remote_timeout"), e);
                }
            }
            if (params.var("scheduler_remote_job") != null && !params.var("scheduler_remote_job").isEmpty()) {
                this.setJobName(params.var("scheduler_remote_job"));
                if (logValue) {
                    spooler_log.info(".. order parameter [scheduler_remote_job]: " + this.getJobName());
                }
            }
            if (params.var("scheduler_remote_order") != null && !params.var("scheduler_remote_order").isEmpty()) {
                this.setOrderId(params.var("scheduler_remote_order"));
                if (logValue) {
                    spooler_log.info(".. order parameter [scheduler_remote_order]: " + this.getOrderId());
                }
            }
            if (params.var("scheduler_remote_command") != null && !params.var("scheduler_remote_command").isEmpty()) {
                this.setCommand(params.var("scheduler_remote_command"));
                if (logValue) {
                    spooler_log.info(".. order parameter [scheduler_remote_command]: " + this.getCommand());
                }
            }
            if (params.var("scheduler_remote_start_at") != null && !params.var("scheduler_remote_start_at").isEmpty()) {
                this.setAt(params.var("scheduler_remote_start_at"));
                if (logValue) {
                    spooler_log.info(".. order parameter [scheduler_remote_start_at]: " + this.getAt());
                }
            }
            if (params.var("scheduler_remote_web_service") != null && !params.var("scheduler_remote_web_service").isEmpty()) {
                this.setWebService(params.var("scheduler_remote_web_service"));
                if (logValue) {
                    spooler_log.info(".. order parameter [scheduler_remote_web_service]: " + this.getWebService());
                }
            }
            if (params.var("scheduler_remote_job_start_after") != null && !params.var("scheduler_remote_job_start_after").isEmpty()) {
                this.setAfter(params.var("scheduler_remote_job_start_after"));
                if (logValue) {
                    spooler_log.info(".. order parameter [scheduler_remote_job_start_after]: " + this.getAfter());
                }
            }
            if (params.var("scheduler_remote_order_replace") != null && !params.var("scheduler_remote_order_replace").isEmpty()) {
                if ("yes".equalsIgnoreCase(params.var("scheduler_remote_order_replace"))
                        || "true".equalsIgnoreCase(params.var("scheduler_remote_order_replace")) || "1".equals(params.var("scheduler_remote_order_replace"))) {
                    this.setReplace(true);
                } else {
                    this.setReplace(false);
                }
                if (logValue) {
                    spooler_log.info(".. order parameter [scheduler_remote_order_replace]: " + this.isReplace());
                }
            }
            if (params.var("scheduler_remote_order_job_chain") != null && !params.var("scheduler_remote_order_job_chain").isEmpty()) {
                this.setJobChain(params.var("scheduler_remote_order_job_chain"));
                if (logValue) {
                    spooler_log.info(".. order parameter [scheduler_remote_order_job_chain]: " + this.getJobChain());
                }
            }
            if (params.var("scheduler_remote_order_priority") != null && !params.var("scheduler_remote_order_priority").isEmpty()) {
                this.setPriority(params.var("scheduler_remote_order_priority"));
                if (logValue) {
                    spooler_log.info(".. order parameter [scheduler_remote_order_priority]: " + this.getPriority());
                }
            }
            if (params.var("scheduler_remote_order_state") != null && !params.var("scheduler_remote_order_state").isEmpty()) {
                this.setState(params.var("scheduler_remote_order_state"));
                if (logValue) {
                    spooler_log.info(".. order parameter [scheduler_remote_order_state]: " + this.getState());
                }
            }
            if (params.var("scheduler_remote_order_title") != null && !params.var("scheduler_remote_order_title").isEmpty()) {
                this.setTitle(params.var("scheduler_remote_order_title"));
                if (logValue) {
                    spooler_log.info(".. order parameter [scheduler_remote_order_title]: " + this.getTitle());
                }
            }
            if (params.var("scheduler_remote_order_run_time") != null && !params.var("scheduler_remote_order_run_time").isEmpty()) {
                this.setRunTime(params.var("scheduler_remote_order_run_time"));
                if (logValue) {
                    spooler_log.info(".. order parameter [scheduler_remote_order_run_time]: " + this.getRunTime());
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException("error occurred processing order parameters: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean spooler_init() {
        try {
            this.initParameters();
            this.getTaskParameters(true);
            if (spooler_job.order_queue() == null) {
                if (spooler_task.params().var("scheduler_remote_host") == null || spooler_task.params().var("scheduler_remote_host").isEmpty()) {
                    throw new JobSchedulerException("no host name parameter [scheduler_remote_host] was specified for remote job scheduler");
                }
                if ((spooler_task.params().var("scheduler_remote_job") == null || spooler_task.params().var("scheduler_remote_job").isEmpty())
                        && (spooler_task.params().var("scheduler_remote_order_job_chain") == null || spooler_task.params()
                                .var("scheduler_remote_order_job_chain").isEmpty())
                        && (spooler_task.params().var("scheduler_remote_command") == null || spooler_task.params().var("scheduler_remote_command").isEmpty())) {
                    throw new JobSchedulerException(
                            "one of the parameters [scheduler_remote_job, scheduler_remote_order_job_chain, scheduler_remote_command] must be specified");
                }
            }
            return true;
        } catch (Exception e) {
            spooler_log.error("error occurred initializing job: " + e.getMessage());
            return false;
        }
    }

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
            oneOfUs += this.getJobName() == null || this.getJobName().isEmpty() ? 0 : 1;
            oneOfUs += this.getJobChain() == null || this.getJobChain().isEmpty() ? 0 : 1;
            oneOfUs += this.getCommand() == null || this.getCommand().isEmpty() ? 0 : 1;
            if (oneOfUs == 0) {
                throw new JobSchedulerException(
                        "one of the parameters [scheduler_remote_job, scheduler_remote_order_job_chain, scheduler_remote_command] must be specified");
            } else if (oneOfUs > 1) {
                throw new JobSchedulerException(
                        "one of the parameters [scheduler_remote_job, scheduler_remote_order_job_chain, scheduler_remote_command] must be specified, "
                                + oneOfUs + " were given");
            }
            if (this.getJobChain() != null && !this.getJobChain().isEmpty()) {
                request = "<add_order";
                request += " replace=\"" + (this.isReplace() ? "yes" : "no") + "\"";
                if (this.getOrderId() != null && !this.getOrderId().isEmpty()) {
                    request += " id=\"" + this.getOrderId() + "\"";
                }
                if (this.getAt() != null && !this.getAt().isEmpty()) {
                    request += " at=\"" + this.getAt() + "\"";
                }
                if (this.getJobChain() != null && !this.getJobChain().isEmpty()) {
                    request += " job_chain=\"" + this.getJobChain() + "\"";
                }
                if (this.getPriority() != null && !this.getPriority().isEmpty()) {
                    request += " priority=\"" + this.getPriority() + "\"";
                }
                if (this.getState() != null && !this.getState().isEmpty()) {
                    request += " state=\"" + this.getState() + "\"";
                }
                if (this.getTitle() != null && !this.getTitle().isEmpty()) {
                    request += " title=\"" + this.getTitle() + "\"";
                }
                if (this.getWebService() != null && !this.getWebService().isEmpty()) {
                    request += " web_service=\"" + this.getWebService() + "\"";
                }
                request += ">";
                request += writeParamsXML(parameters);
                if (this.getRunTime() != null && !this.getRunTime().isEmpty()) {
                    request += this.getRunTime();
                }
                request += "</add_order>";
            } else if (this.getJobName() != null && !this.getJobName().isEmpty()) {
                request = "<start_job job=\"" + this.getJobName() + "\"";
                if (this.getAfter() != null && !this.getAfter().isEmpty()) {
                    request += " after=\"" + this.getAfter() + "\"";
                }
                if (this.getAt() != null && !this.getAt().isEmpty()) {
                    request += " at=\"" + this.getAt() + "\"";
                }
                if (this.getWebService() != null && !this.getWebService().isEmpty()) {
                    request += " web_service=\"" + this.getWebService() + "\"";
                }
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
            if ("tcp".equalsIgnoreCase(this.getProtocol())) {
                response = remoteCommand.getResponse();
                SOSXMLXPath xpath = new SOSXMLXPath(new StringBuffer(response));
                String errCode = xpath.selectSingleNodeValue("//ERROR/@code");
                String errMessage = xpath.selectSingleNodeValue("//ERROR/@text");
                spooler_log.info("remote job scheduler response: " + response);
                if (errCode != null && !errCode.isEmpty() || errMessage != null && !errMessage.isEmpty()) {
                    throw new JobSchedulerException(String.format("remote JobScheduler response reports error message: %1$s [%2$s]", errMessage, errCode));
                }
            }
            return spooler_job.order_queue() != null;
        } catch (Exception e) {
            spooler_log.error("error occurred for remote execution: " + e.getMessage());
            return false;
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

    private String writeParamsXML(final Variable_set parameters) {
        String paramsXml = "<params>";
        String[] params = parameters.names().split(";");
        for (int i = 0; i < params.length; i++) {
            if (!params[i].startsWith("scheduler_remote_")) {
                paramsXml += "<param name=\"" + params[i] + "\" value=\"" + parameters.var(params[i]).replaceAll("&", "&amp;") + "\"/>";
            }
        }
        paramsXml += "</params>";
        return paramsXml;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(final String after) {
        this.after = after;
    }

    public String getAt() {
        return at;
    }

    public void setAt(final String at) {
        this.at = at;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(final String command) {
        this.command = command;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public String getJobChain() {
        return jobChain;
    }

    public void setJobChain(final String jobChain) {
        this.jobChain = jobChain;
    }

    @Override
    public String getJobName() {
        return jobName;
    }

    @Override
    public void setJobName(final String jobName) {
        this.jobName = jobName;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(final String orderId) {
        this.orderId = orderId;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(final String priority) {
        this.priority = priority;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        if (protocol == null || protocol.isEmpty()) {
            throw new JobSchedulerException("no value was given for protocol [tcp, udp]");
        }
        if (!"tcp".equalsIgnoreCase(protocol) && !"udp".equalsIgnoreCase(protocol)) {
            throw new JobSchedulerException("illegal value specified for protocol [tcp, udp], found: " + protocol);
        }
        this.protocol = protocol.toLowerCase();
    }

    public String getRunTime() {
        return runTime;
    }

    public void setRunTime(final String runTime) {
        this.runTime = runTime;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getWebService() {
        return webService;
    }

    public void setWebService(final String webService) {
        this.webService = webService;
    }

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(final boolean replace) {
        this.replace = replace;
    }

}