package sos.scheduler.job;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import sos.configuration.SOSConfiguration;
import sos.scheduler.managed.JobSchedulerManagedObject;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSSchedulerLogger;

import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

@Deprecated
public class JobSchedulerSSHJob_deprecated extends JobSchedulerSSHBaseJob {

    protected InputStream stdout;
    protected InputStream stderr;
    protected StringBuffer stderrOutput;
    protected Variable_set params = null;

    private class PsEfLine implements Comparable {

        public String user = null;
        public int pid;
        public int parentPid;
        public String commandLine = null;
        public ArrayList children = new ArrayList();

        @Override
        public int compareTo(final Object o) {
            PsEfLine other = (PsEfLine) o;
            return pid - other.pid;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

    }

    public void initKillJob() {
        try {
            try {
                spooler.job("scheduler_ssh_kill");
            } catch (Exception e) {
                spooler_log.debug1("initializing ssh kill job");
                spooler.execute_xml("<job name=\"scheduler_ssh_kill\" title=\"Kill processes launched by SSH\"" + " order=\"no\" stop_on_error=\"no\">"
                        + "<description>" + "<include file=\"jobs/JobSchedulerSSHJob.xml\"/>" + "</description>" + "<params>"
                        + "<param name=\"is_kill_job\" value=\"true\"/>" + "</params>" + "<script language=\"java\""
                        + " java_class=\"sos.scheduler.job.JobSchedulerSSHJob\"/>" + "</job>");
            }
        } catch (Exception e) {
            spooler_log.warn("Failed to initialize kill job: " + e);
        }
    }

    @Override
    public boolean spooler_process() {
        String sshKillPid = "";
        boolean isSSHKillJob = false;
        sos.net.sosftp.SOSFTPCommandSSH ftpCommand = null;
        Properties schedulerParameter = null;
        try {
            try {
                params = getParameters();
                String commandScript = "";
                String commandScriptFileName = "";
                schedulerParameter = getSchedulerParameterAsProperties(params);
                if (params.value("is_kill_job") != null && params.value("is_kill_job").length() > 0) {
                    if ("true".equalsIgnoreCase(params.value("is_kill_job")) || "yes".equalsIgnoreCase(params.value("is_kill_job"))
                            || "1".equals(params.value("is_kill_job"))) {
                        isSSHKillJob = true;
                    } else {
                        isSSHKillJob = false;
                    }
                    spooler_log.info(".. parameter [is_kill_job]: " + isSSHKillJob);
                }
                if (isSSHKillJob) {
                    if (params.value("job_scheduler_ssh_kill_pid") != null && params.value("job_scheduler_ssh_kill_pid").length() > 0) {
                        sshKillPid = params.value("job_scheduler_ssh_kill_pid");
                        spooler_log.debug(".. parameter [job_scheduler_ssh_kill_pid]: " + sshKillPid);
                    } else {
                        spooler_log.info("Process doesn't need to be killed. Doing nothing");
                        return false;
                    }
                    if (sshKillPid.equals("0")) {
                        spooler_log.info("Process doesn't need to be killed. Doing nothing");
                        return false;
                    }
                }
                if (params.value("command_script") != null && params.value("command_script").length() > 0) {
                    commandScript = params.value("command_script");
                    if (JobSchedulerManagedObject.isHex(commandScript)) {
                        commandScript = new String(JobSchedulerManagedObject.fromHexString(commandScript), "US-ASCII");
                        schedulerParameter.put("command_script", commandScript);
                    }
                    spooler_log.info(".. parameter [command_script]: " + commandScript);
                }
                if (params.value("command") != null && params.value("command").length() > 0) {
                    String sCommand = params.value("command");
                    if (JobSchedulerManagedObject.isHex(sCommand)) {
                        sCommand = new String(JobSchedulerManagedObject.fromHexString(sCommand), "US-ASCII");
                        schedulerParameter.put("jump_command", sCommand);
                    }
                    spooler_log.info(".. parameter [command]: " + sCommand);
                } else if (commandScriptFileName.length() == 0 && commandScriptFileName.length() == 0) {
                    throw new Exception("no command (or command_script or command_script_file) has been specified for parameter [command]");
                }
            } catch (Exception e) {
                throw new Exception("error occurred processing parameters: " + e.getMessage());
            }
            try {
                if (isSSHKillJob) {
                    this.getBaseParameters();
                    this.getBaseAuthentication();
                    if (sshKillPid.length() > 0) {
                        remoteKill(sshKillPid);
                    }
                } else {
                    SOSConfiguration con = new SOSConfiguration(null, schedulerParameter, null, null, "sos/net/sosftp/Configuration.xml", new SOSSchedulerLogger(spooler_log));
                    con.checkConfigurationItems();
                    ftpCommand = new sos.net.sosftp.SOSFTPCommandSSH(con, new SOSSchedulerLogger(spooler_log));
                    ftpCommand.setSchedulerJob(this);
                    boolean rc = ftpCommand.transfer();
                    return spooler_task.job().order_queue() == null ? false : rc;
                }
            } catch (Exception e) {
                throw new Exception("error occurred processing ssh command: " + e.getMessage());
            } finally {
                if (ftpCommand != null) {
                    createOrderParameter(ftpCommand.getExitStatus());
                }
                if (this.getSshConnection() != null) {
                    try {
                        this.getSshConnection().close();
                        this.setSshConnection(null);
                    } catch (Exception ex) {
                        // gracefully ignore this error
                    }
                }
            }
            return spooler_task.job().order_queue() != null;
        } catch (Exception e) {
            spooler_log.error(e.getMessage());
            return false;
        }
    }

    public static String myReplaceAll(final String source, final String what, final String replacement) {
        String newReplacement = replacement.replaceAll("\\$", "\\\\\\$");
        return source.replaceAll("(?m)" + what, newReplacement);
    }

    public void remoteKill(final String pid) {
        try {
            HashMap pidMap = new HashMap();
            spooler_log.debug3("killing remote process...");
            this.setSshSession(this.getSshConnection().openSession());
            spooler_log.debug5("fetching children");
            this.getSshSession().execCommand("/bin/ps -ef");
            spooler_log.debug9("output to stdout for ps command: ");
            stdout = new StreamGobbler(this.getSshSession().getStdout());
            stderr = new StreamGobbler(this.getSshSession().getStderr());
            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdout));
            String line = stdoutReader.readLine();
            spooler_log.debug9(line);
            while (true) {
                line = stdoutReader.readLine();
                if (line == null) {
                    break;
                }
                spooler_log.debug9(line);
                line = line.trim();
                String[] fields = line.split(" +", 8);
                PsEfLine pel = new PsEfLine();
                pel.user = fields[0];
                pel.pid = Integer.parseInt(fields[1]);
                pel.parentPid = Integer.parseInt(fields[2]);
                pel.commandLine = fields[7];
                pidMap.put(new Integer(pel.pid), pel);
            }
            spooler_log.debug9("output to stderr for ps command: ");
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));
            stderrOutput = new StringBuffer();
            while (true) {
                line = stderrReader.readLine();
                if (line == null) {
                    break;
                }
                spooler_log.debug9(line);
                stderrOutput.append(line + "\n");
            }
            // get children of parent pid
            Iterator pelIter = pidMap.values().iterator();
            while (pelIter.hasNext()) {
                PsEfLine current = (PsEfLine) pelIter.next();
                PsEfLine parent = (PsEfLine) pidMap.get(new Integer(current.parentPid));
                if (parent != null) {
                    spooler_log.debug9("Child of " + parent.pid + " is " + current.pid);
                    parent.children.add(new Integer(current.pid));
                }
            }
            spooler_log.info("killing pid " + pid);
            executeCommand("kill -9 " + pid, -9);
            Integer parentPid = new Integer(pid);
            if (pidMap.containsKey(parentPid)) {
                PsEfLine pel = (PsEfLine) pidMap.get(parentPid);
                if (pel.children.size() > 0) {
                    spooler_log.debug("killing children of pid " + pid);
                    killChildrenOfPid(pel, pidMap);
                }
            } else {
                spooler_log.debug("remote process pid=" + pid + " not exist.");
            }
        } catch (Exception e) {
            try {
                spooler_log.warn("failed to kill remote process: " + e);
            } catch (Exception ex) {
            }
        } finally {
            getSshSession().close();
        }
    }

    private void killChildrenOfPid(final PsEfLine pel, final HashMap pidMap) throws Exception {
        try {
            if (pel.children.size() == 0) {
                return;
            }
            Iterator iter = pel.children.iterator();
            while (iter.hasNext()) {
                Integer childPid = (Integer) iter.next();
                PsEfLine child = (PsEfLine) pidMap.get(childPid);
                spooler_log.debug("killing child pid " + child.pid);
                executeCommand("kill -9 " + child.pid, -9);
                killChildrenOfPid(child, pidMap);
            }
        } catch (Exception e) {
            throw new Exception("Failed to kill children of pid " + pel.pid + ": " + e, e);
        }
    }

    private void executeCommand(final String command, final int logLevel) throws Exception {
        Session session = getSshConnection().openSession();
        try {
            session.execCommand(command);
            spooler_log.log(logLevel, "output to stdout for remote command: " + command);
            stdout = new StreamGobbler(this.getSshSession().getStdout());
            stderr = new StreamGobbler(this.getSshSession().getStderr());
            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdout));
            while (true) {
                String line = stdoutReader.readLine();
                if (line == null) {
                    break;
                }
                spooler_log.log(logLevel, line);
            }
            spooler_log.log(logLevel, "output to stderr for remote command: " + command);
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));
            stderrOutput = new StringBuffer();
            while (true) {
                String line = stderrReader.readLine();
                if (line == null) {
                    break;
                }
                spooler_log.log(logLevel, line);
                stderrOutput.append(line + "\n");
            }
        } catch (Exception e) {
            throw new Exception("Error executing command \"" + command + "\": " + e, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private Variable_set getParameters() throws Exception {
        Order order = null;
        try {
            Variable_set params = spooler.create_variable_set();
            params = spooler_task.params();
            if (spooler_task.job().order_queue() != null) {
                order = spooler_task.order();
                if (order.params() != null) {
                    params.merge(order.params());
                }
            }
            return params;
        } catch (Exception e) {
            throw new Exception("error occurred reading Patameter: " + e.getMessage());
        }
    }

    private Properties getSchedulerParameterAsProperties(final Variable_set params) throws Exception {
        Properties schedulerParams = new Properties();
        try {
            if (params == null) {
                return new Properties();
            }
            String[] names = params.names().split(";");
            spooler_log.debug9("names " + params.names());
            for (String name : names) {
                String key = name;
                String val = params.var(name);
                if (key.length() > 0) {
                    key = "jump_" + key;
                }
                spooler_log.debug1("param [" + key + "=" + val + "]");
                schedulerParams.put(key, val);
            }
            // Einige Defaults hinzufügen
            schedulerParams.put("operation", "execute");
            schedulerParams.put("check_params_names", "no");
            return schedulerParams;
        } catch (Exception e) {
            throw new Exception("error occurred reading Patameter: " + e.getMessage());
        }
    }

    public void setSchedulerSSHKillPid(final int pid) {
        spooler_task.params().set_var("job_scheduler_ssh_kill_pid", "" + pid);
    }

    private void createOrderParameter(final Integer exitstatus) throws Exception {
        try {
            // return the number of transferred files and filenames
            if (spooler_job.order_queue() != null) {
                if (spooler_task.order() != null && spooler_task.order().params() != null) {
                    spooler_task.order().params().set_var("job_scheduler_ssh_exit_code", Integer.toString(exitstatus.intValue()));
                }
            }
        } catch (Exception e) {
            throw new Exception("error occurred creating order Parameter: " + e.getMessage());
        }
    }

}
