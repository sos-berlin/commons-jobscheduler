package sos.net.ssh;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import sos.scheduler.managed.JobSchedulerManagedObject;
import sos.spooler.Job_impl;
import sos.spooler.Order;
import sos.spooler.Variable_set;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.Session;

public class SOSSSHPidMonitorJobJcraft extends Job_impl {
  /** default command delimiter */
  final static String DEFAULT_COMMAND_DELIMITER = "%%";
  /**
   * regular expression for delimiter of multiple commands specified as job or
   * order parameter
   */
  protected String commandDelimiter = DEFAULT_COMMAND_DELIMITER;
  /** array of commands that have been separated by the commandDelimiter */
  protected String[] commands = {};
  /** Script for a command which will be submitted and then executed **/
  protected String commandScript = "";
  /** File containing a command which will be submitted and then executed **/
  protected String commandScriptFileName = "";
  /** Parameter for the commandScriptFile **/
  protected String commandScriptParam = "";
  /** ignore errors reported by the exit status of commands */
  protected boolean ignoreError = false;
  /** ignore signals terminating remote execution */
  protected boolean ignoreSignal = false;
  /** ignore output to stderr */
  protected boolean ignoreStderr = false;
  /** ignore exit codes **/
  protected Vector ignoreExitCodes = new Vector();
  /** Simulate a shell instead of just passing commands **/
  protected boolean simulateShell = false;
  /** Trigger for this string to know that the shell is waiting **/
  protected String promptTrigger = "";
  /** Inputstreams for stdout and stderr **/
  protected InputStream stdoutInputStream;
  protected InputStream stderrInputStream;
  /** timestamp of the last text from stdin or stderr **/
  protected long lasttime = 0;
  /** time to wait if anything more is coming from stdout or stderr when logging in **/
  protected long loginTimeout = 0;
  /** time to wait if anything more is coming from stdout or stderr when executing commands **/
  protected long commandTimeout = 0;
  /** Output from stdout and stderr **/
  protected StringBuffer stdoutOutput;
  protected StringBuffer stderrOutput;
  /** Line currently being displayed on the shell **/
  protected String currentLine = "";
  protected Variable_set params = null;
  protected String host = null;
  protected String user = null;
  protected Integer port = null;
  protected String passwd = null;
  protected String authenticationMethod = null;
  protected String authenticationFilename = null;
  protected String proxyHost = null;
  protected String proxyUser = null;
  protected Integer proxyPort = null;
  protected String proxyPasswd = null;

  private ChannelExec executeChannel = null;
//  private ChannelShell shellChannel = null;
  private Session sshSession = null;
  private JSch secureChannel = null;

  private int exitCode = -1;
  private boolean isWindows = false;

  private void initKillJob() {
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

  public boolean spooler_process() {
    stdoutOutput = new StringBuffer();
    stderrOutput = new StringBuffer();
    currentLine = "";
    simulateShell = false;
    promptTrigger = "";
    loginTimeout = 0;
    commandTimeout = 0;
    commandScript = "";
    commandScriptFileName = "";
    commandScriptParam = "";
    String sshKillPid = "";
    boolean isSSHKillJob = false;
    Order order = null;
    try {
      try {
        // to fetch parameters, order parameters have precedence to job
        // parameters
        params = spooler_task.params();
        if (spooler_task.job().order_queue() != null) {
          order = spooler_task.order();
          if (order.params() != null) params.merge(order.params());
        }
        getBaseParameters();
        if (params.value("is_kill_job") != null && params.value("is_kill_job").length() > 0) {
          if (params.value("is_kill_job").equalsIgnoreCase("true") || params.value("is_kill_job").equalsIgnoreCase("yes")
              || params.value("is_kill_job").equals("1")) {
            isSSHKillJob = true;
          } else {
            isSSHKillJob = false;
          }
          spooler_log.info(".. parameter [is_kill_job]: " + isSSHKillJob);
        }
        if (isSSHKillJob) {
          if (params.value("job_scheduler_ssh_kill_pid") != null && params.value("job_scheduler_ssh_kill_pid").length() > 0) {
            sshKillPid = (params.value("job_scheduler_ssh_kill_pid"));
            spooler_log.info(".. parameter [job_scheduler_ssh_kill_pid]: " + sshKillPid);
          } else {
            spooler_log.info("Process doesn't need to be killed. Doing nothing");
            return false;
          }
          if (sshKillPid.equals("0")) {
            spooler_log.info("Process doesn't need to be killed. Doing nothing");
            return false;
          }
        }
        if (params.value("command_delimiter") != null && params.value("command_delimiter").length() > 0) {
          commandDelimiter = params.value("command_delimiter");
          spooler_log.info(".. parameter [command_delimiter]: " + commandDelimiter);
        } else {
          commandDelimiter = DEFAULT_COMMAND_DELIMITER;
        }
        if (params.value("command_script") != null && params.value("command_script").length() > 0) {
          commandScript = params.value("command_script");
          if (JobSchedulerManagedObject.isHex(commandScript)) commandScript = new String(JobSchedulerManagedObject.fromHexString(commandScript), "US-ASCII");
          spooler_log.info(".. parameter [command_script]: " + commandScript);
        }
        if (params.value("command_script_file") != null && params.value("command_script_file").length() > 0) {
          commandScriptFileName = params.value("command_script_file");
          spooler_log.info(".. parameter [command_script_file]: " + commandScriptFileName);
        }
        if (params.value("command_script_param") != null && params.value("command_script_param").length() > 0) {
          commandScriptParam = params.value("command_script_param");
          spooler_log.info(".. parameter [command_script_param]: " + commandScriptParam);
        }
        if (params.value("command") != null && params.value("command").length() > 0) {
          String sCommand = params.value("command");
          if (JobSchedulerManagedObject.isHex(sCommand)) sCommand = new String(JobSchedulerManagedObject.fromHexString(sCommand), "US-ASCII");
          commands = sCommand.split(commandDelimiter);
          spooler_log.info(".. parameter [command]: " + sCommand);
        } else if (commandScript.length() == 0 && commandScriptFileName.length() == 0) {
          throw new Exception("no command (or command_script or command_script_file) has been specified for parameter [command]");
        }
        if (params.value("ignore_error") != null && params.value("ignore_error").length() > 0) {
          if (params.value("ignore_error").equalsIgnoreCase("true") || params.value("ignore_error").equalsIgnoreCase("yes")
              || params.value("ignore_error").equals("1")) {
            ignoreError = true;
          } else {
            ignoreError = false;
          }
          spooler_log.info(".. parameter [ignore_error]: " + ignoreError);
        } else {
          ignoreError = false;
        }
        if (params.value("ignore_signal") != null && params.value("ignore_signal").length() > 0) {
          if (params.value("ignore_signal").equalsIgnoreCase("true") || params.value("ignore_signal").equalsIgnoreCase("yes")
              || params.value("ignore_signal").equals("1")) {
            ignoreSignal = true;
          } else {
            ignoreSignal = false;
          }
          spooler_log.info(".. parameter [ignore_signal]: " + ignoreSignal);
        } else {
          ignoreSignal = false;
        }
        if (params.value("ignore_stderr") != null && params.value("ignore_stderr").length() > 0) {
          if (params.value("ignore_stderr").equalsIgnoreCase("true") || params.value("ignore_stderr").equalsIgnoreCase("yes")
              || params.value("ignore_stderr").equals("1")) {
            ignoreStderr = true;
          } else {
            ignoreStderr = false;
          }
          spooler_log.info(".. parameter [ignore_stderr]: " + ignoreStderr);
        } else {
          ignoreStderr = false;
        }
        if (params.value("ignore_exit_code") != null && params.value("ignore_exit_code").length() > 0) {
          String codesToIgnore = params.value("ignore_exit_code");
          ignoreExitCodes = getVectorFromRange(codesToIgnore);
          spooler_log.info(".. parameter [ignore_exit_code]: " + codesToIgnore);
        } else {
          ignoreExitCodes = new Vector();
        }
        if (params.value("simulate_shell") != null && params.value("simulate_shell").length() > 0) {
          if (params.value("simulate_shell").equalsIgnoreCase("true") || params.value("simulate_shell").equalsIgnoreCase("yes")
              || params.value("simulate_shell").equals("1")) {
            simulateShell = true;
          } else {
            simulateShell = false;
          }
          spooler_log.info(".. parameter [simulate_shell]: " + simulateShell);
        } else {
          simulateShell = false;
        }
        if (params.value("simulate_shell_prompt_trigger") != null && params.value("simulate_shell_prompt_trigger").length() > 0) {
          promptTrigger = params.value("simulate_shell_prompt_trigger");
          spooler_log.info(".. parameter [simulate_shell_prompt_trigger]: " + promptTrigger);
        } else {
          promptTrigger = "";
        }
        if (params.value("simulate_shell_login_timeout") != null && params.value("simulate_shell_login_timeout").length() > 0) {
          this.loginTimeout = Long.parseLong(params.value("simulate_shell_login_timeout"));
          spooler_log.info(".. parameter [simulate_shell_login_timeout]: " + this.loginTimeout);
        } else {
          loginTimeout = 0;
        }
        if (params.value("simulate_shell_inactivity_timeout") != null && params.value("simulate_shell_inactivity_timeout").length() > 0) {
          this.commandTimeout = Long.parseLong(params.value("simulate_shell_inactivity_timeout"));
          spooler_log.info(".. parameter [simulate_shell_inactivity_timeout]: " + this.commandTimeout);
        } else {
          commandTimeout = 0;
        }
        if (simulateShell && promptTrigger.length() == 0 && (loginTimeout == 0 || commandTimeout == 0)) {
          throw new Exception("if simulate_shell=true then either simulate_shell_prompt_trigger or "
              + "simulate_shell_login_timeout and simulate_shell_inactivity_timeout need to br set.");
        }
      } catch (Exception e) {
        throw new Exception("error occurred processing parameters: " + e.getMessage());
      }

      RemoteConsumer stdoutConsumer = null;
      RemoteConsumer stderrConsumer = null;

      try { // to connect, authenticate and execute commands
        authenticate();
        OutputStream stdin;
        OutputStreamWriter stdinWriter = null;
        String remoteCommandScriptFileName = "";
        boolean isWindows = remoteIsWindowsShell();
        if (isSSHKillJob) {
          if (sshKillPid.length() > 0) {
            remoteKill(sshKillPid);
          }
        } else {
          if (commandScript.length() > 0 || commandScriptFileName.length() > 0) {
            // change commands to execute transferred file

            File commandScriptFile = null;
            if (commandScript.length() > 0) {
              commandScriptFile = new File(createScriptFile(commandScript));
            }
            remoteCommandScriptFileName = commandScriptFile.getName();
            commands = new String[1];
            if (isWindows) {
              commands[0] = commandScriptFile.getName();
            } else {
              commands[0] = "./" + commandScriptFile.getName();
            }
            if (commandScriptParam != null && commandScriptParam.length() > 0) {
              commands[0] = commands[0] + " " + commandScriptParam;
            }
            // delete local file
            if (commandScript.length() > 0) {
              commandScriptFile.delete();
            }
          }
          // execute commands
          for (int i = 0; i < commands.length; i++) {
            try {
              Integer exitStatus = null;
              spooler_log.info("executing remote command: " + commands[i]);
              if (simulateShell) {
                stdinWriter.write(commands[i] + "\n");
                stdinWriter.flush();
                boolean prompt = false;
                while (!prompt) {
                  long now = System.currentTimeMillis();
                  if (loginTimeout > 0 && lasttime + loginTimeout < now) {
                    prompt = true;
                  }
                  if (promptTrigger.length() > 0 && currentLine.indexOf(promptTrigger) != -1) {
                    spooler_log.debug3("Found prompt " + promptTrigger);
                    prompt = true;
                  }
                }
                currentLine = "";
                spooler_log.info("output to stdout for remote command: " + commands[i]);
                spooler_log.info(stdoutOutput.toString());
                stdoutOutput = new StringBuffer();
              } else {
                sshSession = secureChannel.getSession(user, host, port);
                executeChannel = (ChannelExec) sshSession.openChannel("exec");
                // int pid =
                // getRemotePid(this.getSshConnection().openSession());
                String currentCommand = commands[i];
                if (!isWindows) {
                  currentCommand = "echo $$ && " + currentCommand;
                  // TODO: initKillJob if Job is running and remote command is not available anymore
                  // initKillJob();
                }
                executeChannel.setCommand(currentCommand);
                executeChannel.setInputStream(System.in, true);
                executeChannel.setOutputStream(System.out);
                executeChannel.setErrStream(System.err, true);
                stdoutInputStream = executeChannel.getInputStream();
                stderrInputStream = executeChannel.getErrStream();
                executeChannel.connect(3000);
                spooler_log.info("output to stdout for remote command: " + commands[i]);
                BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdoutInputStream));
                int pid = 0;
                while (true) {
                  String line = stdoutReader.readLine();
                  if (line == null) break;
                  if (!isWindows && pid == 0) {
                    pid = Integer.parseInt(line);
                    spooler_task.params().set_var("job_scheduler_ssh_kill_pid", "" + pid);
                    spooler_log.debug5("Parent pid: " + pid);
                  } else
                    spooler_log.info(line);
                }
                spooler_task.params().set_var("job_scheduler_ssh_kill_pid", "0");

                spooler_log.info("output to stderr for remote command: " + commands[i]);
                BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderrInputStream));
                stderrOutput = new StringBuffer();
                while (true) {
                  String line = stderrReader.readLine();
                  if (line == null) break;
                  spooler_log.info(line);
                  stderrOutput.append(line + "\n");
                }
              }
              if (stderrOutput != null && stderrOutput.length() > 0) {
                if (ignoreStderr) {
                  spooler_log.info("output to stderr is ignored: " + stderrOutput);
                } else {
                  throw new Exception("remote execution reports error: " + stderrOutput);
                }
              }
              try {
                exitCode = executeChannel.getExitStatus();
              } catch (Exception e) {
                spooler_log.info("could not retrieve exit status, possibly not supported by remote ssh server");
              }
              if (exitCode >= 0) {
                if (exitCode != 0) {
                  if (ignoreError || this.ignoreExitCodes.contains(exitCode)) {
                    spooler_log.info("exit code is ignored: " + exitCode);
                  } else {
                    throw new Exception("remote command terminated with exit code: " + exitCode);
                  }
                }
              }
            } catch (Exception e) {
              throw new Exception(e.getMessage());
            }
          }
        }
      } catch (Exception e) {
        throw new Exception("error occurred processing ssh command: " + e.getMessage());
      } finally {
        if (stderrConsumer != null) stderrConsumer.end();
        if (stdoutConsumer != null) stdoutConsumer.end();
      }
      // return value for classic and order driven processing
      return (spooler_task.job().order_queue() != null);
    } catch (Exception e) {
      spooler_log.error(e.getMessage());
      return false;
    }
  }

  public String createScriptFile(String pstrContent) throws Exception {
    try {
      String commandScript = pstrContent;
      if (isWindows == false) {
        commandScript = commandScript.replaceAll("(?m)\r", "");
      }
      File fleTempScriptFile = File.createTempFile("sos-sshscript", getScriptFileNameSuffix());
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fleTempScriptFile)));
      out.write(commandScript);
      out.flush();
      out.close();
      fleTempScriptFile.deleteOnExit();
      putFile(fleTempScriptFile);
      String strFileName2Return = fleTempScriptFile.getName();
      if (isWindows == false) {
        strFileName2Return = "./" + strFileName2Return;
      }
      return strFileName2Return;
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  private void putFile(File pfleCommandFile) throws Exception {

    String strFileName = pfleCommandFile.getName();
    try {
      if (!sshSession.isConnected()) {
        if (secureChannel == null) {
          secureChannel = new JSch();
        }
        sshSession = secureChannel.getSession(user, host, port);
        sshSession.setPassword(passwd);
        sshSession.connect();
      }
      ChannelSftp channel = (ChannelSftp) sshSession.openChannel("sftp");
      channel.connect();
      ChannelSftp sftp = (ChannelSftp) channel;
      sftp.put(pfleCommandFile.getCanonicalPath(), strFileName);
      sftp.chmod(new Integer(0700), strFileName);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  private String getScriptFileNameSuffix() {
    String strSuffix = isWindows ? ".cmd" : ".sh";
    return strSuffix;
  }

  private int getRemoteShellPid(Session session) {
    int pid = 0;
    try {
      String pidCommand = "echo $$";
      spooler_log.debug9("Executing command " + pidCommand);
      executeChannel.setCommand(pidCommand);
      executeChannel.setInputStream(stdoutInputStream);
      executeChannel.setErrStream(System.err);
      executeChannel.connect();
      spooler_log.debug9("output to stdout for remote command: " + pidCommand);
      BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdoutInputStream));
      String stdOut = "";
      while (true) {
        String line = stdoutReader.readLine();
        if (line == null) break;
        spooler_log.debug9(line);
        stdOut += line;
      }
      spooler_log.debug9("output to stderr for remote command: " + pidCommand);
      stderrInputStream = executeChannel.getErrStream();
      BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderrInputStream));
      while (true) {
        String line = stderrReader.readLine();
        if (line == null) break;
        spooler_log.info(line);
      }
      pid = Integer.parseInt(stdOut);
      spooler_log.debug3("pid: " + pid);
    } catch (Exception e) {
      spooler_log.warn("Failed to check remote pid: " + e);
    } finally {
      session.disconnect();
    }
    return pid;
  }

  /**
   * Parameter-Processing
   * 
   */
  public void getBaseParameters() throws Exception {
    Order order = null;
    Variable_set params = null;
    try { // to fetch parameters, order parameters have precedence to job
          // parameters
      params = spooler_task.params();
      if (spooler_task.job().order_queue() != null) {
        order = spooler_task.order();
        if (order.params() != null) params.merge(order.params());
      }
      if (params.value("host") != null && params.value("host").toString().length() > 0) {
        host = params.value("host");
        spooler_log.info(".. parameter [host]: " + host);
      } else {
        throw new Exception("no host name or ip address was specified as parameter [host]");
      }

      if (params.value("port") != null && params.value("port").length() > 0) {
        try {
          port = Integer.parseInt(params.value("port"));
          spooler_log.info(".. parameter [port]: " + port);
        } catch (Exception ex) {
          throw new Exception("illegal non-numeric value for parameter [port]: " + params.value("port"));
        }
      } else {
        port = 22;
      }
      if (params.value("user") != null && params.value("user").length() > 0) {
        user = params.value("user");
        spooler_log.info(".. parameter [user]: " + user);
      } else {
        throw new Exception("no user name was specified as parameter [user]");
      }
      if (params.value("password") != null && params.value("password").length() > 0) {
        passwd = params.value("password");
        spooler_log.info(".. parameter [password]: ********");
      } else {
        passwd = "";
      }
      if (params.value("proxy_host") != null && params.value("proxy_host").toString().length() > 0) {
        proxyHost = params.value("proxy_host");
        spooler_log.info(".. parameter [proxy_host]: " + proxyHost);
      } else {
        proxyHost = "";
      }
      if (params.value("proxy_port") != null && params.value("proxy_port").length() > 0) {
        try {
          proxyPort = Integer.parseInt(params.value("proxy_port"));
          spooler_log.info(".. parameter [proxy_port]: " + proxyPort);
        } catch (Exception ex) {
          throw new Exception("illegal non-numeric value for parameter [proxy_port]: " + params.value("proxy_port"));
        }
      } else {
        proxyPort = 3128;
      }
      if (params.value("proxy_user") != null && params.value("proxy_user").length() > 0) {
        proxyUser = params.value("proxy_user");
        spooler_log.info(".. parameter [proxy_user]: " + proxyUser);
      } else {
        proxyUser = "";
      }
      if (params.value("proxy_password") != null && params.value("proxy_password").length() > 0) {
        proxyPasswd = params.value("proxy_password");
        spooler_log.info(".. parameter [proxy_password]: ********");
      } else {
        proxyPasswd = "";
      }
      if (params.value("auth_method") != null && params.value("auth_method").length() > 0) {
        if (params.value("auth_method").equalsIgnoreCase("publickey") || params.value("auth_method").equalsIgnoreCase("password")) {
          authenticationMethod = params.value("auth_method");
          spooler_log.info(".. parameter [auth_method]: " + authenticationMethod);
        } else {
          throw new Exception("invalid authentication method [publickey, password] specified: " + params.value("auth_method"));
        }
      } else {
        authenticationMethod = "publickey";
      }
      if (params.value("auth_file") != null && params.value("auth_file").length() > 0) {
        authenticationFilename = params.value("auth_file");
        spooler_log.info(".. parameter [auth_file]: " + authenticationFilename);
      } else {
        if (authenticationMethod.equalsIgnoreCase("publickey")) throw new Exception("no authentication filename was specified as parameter [auth_file]");
      }
    } catch (Exception e) {
      throw new Exception("error occurred processing parameters: " + e.getMessage());
    }
  }

  private Vector getVectorFromRange(String input) {
    String[] elements = input.split(",");
    Vector result = new Vector();
    for (int i = 0; i < elements.length; i++) {
      String element = elements[i].trim();
      if (element.indexOf("-") == -1) {
        result.add(new Integer(element));
      } else {
        String[] range = element.split("-");
        int from = Integer.parseInt(range[0].trim());
        int to = Integer.parseInt(range[1].trim());
        int stepSize = 1;
        for (int j = from; j <= to; j = j + stepSize)
          result.add(new Integer(j));
      }
    }
    return result;
  }

  public void authenticate() throws Exception {
    try { // to connect and authenticate
      boolean isAuthenticated = false;
      secureChannel = new JSch();
      sshSession = secureChannel.getSession(user, host, port);
      sshSession.setConfig("StrictHostKeyChecking", "no");
      if (proxyHost != null && proxyHost.length() > 0 && proxyPort != null && proxyPort > 0) {
        ProxyHTTP proxy = new ProxyHTTP(proxyHost, proxyPort);
        if (proxyUser != null && proxyUser.length() > 0 && proxyPasswd != null && proxyPasswd.length() > 0) {
          proxy.setUserPasswd(proxyUser, proxyPasswd);
        }
        sshSession.setProxy(proxy);
      }
      if (authenticationMethod.equalsIgnoreCase("publickey")) {
        File authenticationFile = new File(authenticationFilename);
        if (!authenticationFile.exists()) throw new Exception("authentication file does not exist: " + authenticationFile.getCanonicalPath());
        if (!authenticationFile.canRead()) {
          throw new Exception("authentication file not accessible: " + authenticationFile.getCanonicalPath());
        }
        secureChannel.addIdentity(authenticationFile.getAbsolutePath());
      }
      sshSession.connect();
      isAuthenticated = sshSession.isConnected();
      if (!isAuthenticated)
        throw new Exception("authentication failed [host=" + host + ", port=" + port + ", user:" + user + ", auth_method=" + authenticationMethod
            + ", auth_file=" + authenticationFilename + "]");
    } catch (Exception e) {
      if (sshSession != null) {
        try {
          sshSession.disconnect();
        } catch (Exception ex) {
        } // gracefully ignore this error
      }
      throw new Exception(e.getMessage());
    }
  }

  public void remoteKill(String pid) {
    try {
      HashMap pidMap = new HashMap();
      spooler_log.debug3("killing remote process...");
      spooler_log.debug5("fetching children");
      String command = "/bin/ps -ef";
      executeCommand(command, -9);
      spooler_log.debug9("output to stdout for ps command: ");
      BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdoutInputStream));
      String line = stdoutReader.readLine(); // Ueberschrift
      spooler_log.debug9(line);
      while (true) {
        line = stdoutReader.readLine();
        if (line == null) break;
        spooler_log.debug9(line);
        line = line.trim();
        String[] fields = line.split(" +", 8);
        PidComparator pidComparator = new PidComparator();
        pidComparator.user = fields[0];
        pidComparator.pid = Integer.parseInt(fields[1]);
        pidComparator.parentPid = Integer.parseInt(fields[2]);
        pidComparator.commandLine = fields[7];
        pidMap.put(new Integer(pidComparator.pid), pidComparator);
      }
      spooler_log.debug9("output to stderr for ps command: ");
      BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderrInputStream));
      stderrOutput = new StringBuffer();
      while (true) {
        line = stderrReader.readLine();
        if (line == null) break;
        spooler_log.debug9(line);
        stderrOutput.append(line + "\n");
      }
      // get children of parent pid
      Iterator pidComparatorIter = pidMap.values().iterator();
      while (pidComparatorIter.hasNext()) {
        PidComparator current = (PidComparator) pidComparatorIter.next();
        PidComparator parent = (PidComparator) pidMap.get(new Integer(current.parentPid));
        if (parent != null) {
          spooler_log.debug9("Child of " + parent.pid + " is " + current.pid);
          parent.children.add(new Integer(current.pid));
        }
      }
      spooler_log.info("killing pid " + pid);
      executeCommand("kill -9 " + pid, -9);
      Integer parentPid = new Integer(pid);
      PidComparator pidComparator = (PidComparator) pidMap.get(parentPid);
      if (pidComparator.children.size() > 0) {
        spooler_log.debug("killing children of pid " + pid);
        killChildrenOfPid(pidComparator, pidMap);
      }
    } catch (Exception e) {
      try {
        spooler_log.warn("failed to kill remote process: " + e);
      } catch (Exception ex) {
      }
    } finally {
      sshSession.disconnect();
    }
  }

  private void killChildrenOfPid(PidComparator pel, HashMap pidMap) throws Exception {
    try {
      if (pel.children.size() == 0) return;
      Iterator iter = pel.children.iterator();
      while (iter.hasNext()) {
        Integer childPid = (Integer) iter.next();
        PidComparator child = (PidComparator) pidMap.get(childPid);
        spooler_log.debug("killing child pid " + child.pid);
        executeCommand("kill -9 " + child.pid, -9);
        killChildrenOfPid(child, pidMap);
      }
    } catch (Exception e) {
      throw new Exception("Failed to kill children of pid " + pel.pid + ": " + e, e);
    }
  }

  private void executeCommand(String command, int logLevel) throws Exception {
    stdoutOutput = new StringBuffer();
    stderrOutput = new StringBuffer();
    if (!sshSession.isConnected()) {
      sshSession = secureChannel.getSession(user, host, port);
      sshSession.setPassword(passwd);
      sshSession.connect();
    }
    sshSession.setConfig("StrictHostKeyChecking", "no");
    executeChannel = (ChannelExec) sshSession.openChannel("exec");
    executeChannel.setCommand(command);
    executeChannel.setInputStream(System.in, true);
    executeChannel.setOutputStream(System.out);
    executeChannel.setErrStream(System.err, true);
    stdoutInputStream = executeChannel.getInputStream();
    stderrInputStream = executeChannel.getErrStream();
    executeChannel.connect(3000);
    spooler_log.log(logLevel, "output to stdout for remote command: " + command);
    spooler_log.log(logLevel, "output to stderr for remote command: " + command);
    String output = "";
    byte[] tmp = new byte[1024];
    while (true) {
      while (stdoutInputStream.available() > 0) {
        int i = stdoutInputStream.read(tmp, 0, 1024);
        if (i < 0) {
          break;
        }
        output += new String(tmp, 0, i);
        spooler_log.log(logLevel, output);
      }
      stdoutOutput.append(output);
      if (executeChannel.isClosed()) {
        if (stdoutInputStream.available() > 0) {
          continue;
        }
        exitCode = executeChannel.getExitStatus();
        break;
      } else if (executeChannel.isEOF()) {
        if (stdoutInputStream.available() > 0) {
          continue;
        }
        exitCode = executeChannel.getExitStatus();
        break;
      }
      try {
        Thread.sleep(1000);
      } catch (Exception ee) {
      }
    }
    tmp = new byte[1024];
    String errorOutput = "";
    while (true) {
      while (stderrInputStream.available() > 0) {
        int i = stderrInputStream.read(tmp, 0, 1024);
        if (i < 0) {
          break;
        }
        errorOutput = new String(tmp, 0, i);
        spooler_log.log(logLevel, errorOutput);
      }
      stderrOutput.append(errorOutput);
      if (executeChannel.isClosed()) {
        if (stderrInputStream.available() > 0) {
          continue;
        }
        exitCode = executeChannel.getExitStatus();
        break;
      }
      try {
        Thread.sleep(1000);
      } catch (Exception ee) {
      }
    }
  }

  private boolean remoteIsWindowsShell() {
    stdoutOutput = new StringBuffer();
    stderrOutput = new StringBuffer();
    ChannelExec channel = null;
    try {
      String checkShellCommand = "echo %ComSpec%";
      channel = (ChannelExec) sshSession.openChannel("exec");
      channel.setCommand(checkShellCommand);
      stdoutInputStream = channel.getInputStream();
      stderrInputStream = channel.getErrStream();
      channel.connect(3000);
      String stdOut = "";
      byte[] tmp = new byte[1024];
      while (true) {
        while (stdoutInputStream.available() > 0) {
          int i = stdoutInputStream.read(tmp, 0, 1024);
          if (i < 0) {
            break;
          }
          stdOut += new String(tmp, 0, i);
        }
        stdoutOutput.append(stdOut);
        if (channel.isClosed()) {
          if (stdoutInputStream.available() > 0) {
            continue;
          }
          exitCode = channel.getExitStatus();
          break;
        } else {
          Thread.sleep(1000);
        }
      }
      String stdErr = "";
      tmp = new byte[1024];
      while (true) {
        while (stderrInputStream.available() > 0) {
          int i = stderrInputStream.read(tmp, 0, 1024);
          if (i < 0) {
            break;
          }
          stdErr += new String(tmp, 0, i);
        }
        stderrOutput.append(stdErr);
        if (channel.isClosed()) {
          if (stderrInputStream.available() > 0) {
            continue;
          }
          exitCode = channel.getExitStatus();
          break;
        } else {
          Thread.sleep(1000);
        }
      }
      if (stdOut.indexOf("cmd.exe") > -1) {
        isWindows = true;
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (channel != null && !channel.isClosed()) {
        try {
          channel.disconnect();
        } catch (Exception e) {
          e.printStackTrace();
          ;
        }
      }
    }
    return false;
  }

  /**
   * This thread consumes output from the remote server puts it into fields of
   * the main class
   */
  class RemoteConsumer extends Thread {
    private StringBuffer sbuf;
    private boolean writeCurrentline = false;
    private InputStream stream;
    boolean end = false;

    private RemoteConsumer(StringBuffer buffer, InputStream str) {
      this.sbuf = buffer;
      this.writeCurrentline = true;
      this.stream = str;
    }

    private void addText(byte[] data, int len) {
      lasttime = System.currentTimeMillis();
      String outstring = new String(data).substring(0, len);
      sbuf.append(outstring);
      if (writeCurrentline) {
        int newlineIndex = outstring.indexOf("\n");
        if (newlineIndex > -1) {
          String stringAfterNewline = outstring.substring(newlineIndex);
          currentLine = stringAfterNewline;
        } else
          currentLine += outstring;
      }
    }

    public void run() {
      byte[] buff = new byte[64];

      try {
        while (!end) {
          buff = new byte[8];
          int len = stream.read(buff);
          if (len == -1) return;
          addText(buff, len);
        }
      } catch (Exception e) {
      }
    }

    public synchronized void end() {
      end = true;
    }
  }

  private class PidComparator implements Comparable {
    public String user;
    public int pid;
    public int parentPid;
    public String commandLine;
    public ArrayList children = new ArrayList();

    /* (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object) */
    public int compareTo(Object o) {
      PidComparator other = (PidComparator) o;
      return pid - other.pid;
    }
  }

}
