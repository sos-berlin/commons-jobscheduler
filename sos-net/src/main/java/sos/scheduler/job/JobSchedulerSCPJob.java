package sos.scheduler.job;

import com.sos.JSHelper.Basics.VersionInfo;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.SOSFileSystemOperations;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.StreamGobbler;

import sos.spooler.Order;
import sos.spooler.Variable_set;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.regex.Pattern;

/** @author andreas pueschel
 * @author ghassan beydoun */
public class JobSchedulerSCPJob extends JobSchedulerSSHBaseJob {

    protected String action = "";
    protected String fileList = "";
    protected String fileSpec = "";
    protected String localDir = "";
    protected String remoteDir = "";
    protected boolean createDir = true;
    protected boolean recursive = false;
    protected String permissions = "0600";
    private Vector<File> filenames = null;

    @Override
    public boolean spooler_process() {
        spooler_log.info(VersionInfo.VERSION_STRING);
        Order order = null;
        Variable_set params = null;
        SCPClient scpClient = null;
        try {
            try {
                params = spooler_task.params();
                if (spooler_task.job().order_queue() != null) {
                    order = spooler_task.order();
                    if (order.params() != null) {
                        params.merge(order.params());
                    }
                }
                this.getBaseParameters();
                if (params.value("action") != null && !params.value("action").isEmpty()) {
                    if (!"get".equalsIgnoreCase(params.value("action")) && !"put".equalsIgnoreCase(params.value("action"))) {
                        throw new JobSchedulerException("invalid action parameter [action] specified, expected get, put: " + params.value("action"));
                    }
                    this.setAction(params.value("action").toLowerCase());
                    spooler_log.info(".. parameter [action]: " + this.getAction());
                } else {
                    throw new JobSchedulerException("no action parameter [action] was specified");
                }
                if (params.value("file_list") != null && !params.value("file_list").isEmpty()) {
                    this.setFileList(params.value("file_list"));
                    spooler_log.info(".. parameter [file_list]: " + this.getFileList());
                } else {
                    if ("get".equals(this.getAction())) {
                        throw new JobSchedulerException("action [get] requires filenames being specified as parameter [file_list]");
                    }
                    this.setFileList("");
                }
                if (params.value("file_spec") != null && !params.value("file_spec").isEmpty()) {
                    this.setFileSpec(params.value("file_spec"));
                    spooler_log.info(".. parameter [file_spec]: " + this.getFileSpec());
                } else {
                    this.setFileSpec("^(.*)$");
                }
                if (params.value("local_dir") != null && !params.value("local_dir").isEmpty()) {
                    this.setLocalDir(this.normalizePath(params.value("local_dir")));
                    spooler_log.info(".. parameter [local_dir]: " + params.value("local_dir"));
                } else {
                    this.setLocalDir(".");
                }
                if (params.value("remote_dir") != null && !params.value("remote_dir").isEmpty()) {
                    this.setRemoteDir(this.normalizePath(params.value("remote_dir")));
                    spooler_log.info(".. parameter [remote_dir]: " + params.value("remote_dir"));
                } else {
                    this.setRemoteDir(".");
                }
                if (params.value("create_dir") != null && !params.value("create_dir").isEmpty()) {
                    if ("true".equalsIgnoreCase(params.value("create_dir")) || "yes".equalsIgnoreCase(params.value("create_dir"))
                            || "1".equals(params.value("create_dir"))) {
                        this.setCreateDir(true);
                    } else {
                        this.setCreateDir(false);
                    }
                    spooler_log.info(".. parameter [create_dir]: " + this.isCreateDir());
                } else {
                    this.setCreateDir(true);
                }
                if (params.value("recursive") != null && !params.value("recursive").isEmpty()) {
                    if ("true".equalsIgnoreCase(params.value("recursive")) || "yes".equalsIgnoreCase(params.value("recursive"))
                            || "1".equals(params.value("recursive"))) {
                        this.setRecursive(true);
                    } else {
                        this.setRecursive(false);
                    }
                    spooler_log.info(".. parameter [recursive]: " + this.isRecursive());
                } else {
                    this.setRecursive(false);
                }
                if (params.value("permissions") != null && !params.value("permissions").isEmpty()) {
                    String perms = "0000" + params.value("permissions");
                    perms = perms.substring(perms.length() - 4);
                    if (!perms.matches("^[0-7]{4}$")) {
                        throw new JobSchedulerException("illegal octal value for parameter [permissions]: " + perms);
                    }
                    this.setPermissions(perms);
                    spooler_log.info(".. parameter [permissions]: " + this.getPermissions());
                } else {
                    this.setPermissions("0600");
                }
            } catch (JobSchedulerException e) {
                throw e;
            } catch (Exception e) {
                throw new JobSchedulerException("error occurred processing parameters: " + e.getMessage());
            }
            try {
                this.getBaseAuthentication();
                scpClient = new SCPClient(this.getSshConnection());
                File localCheckDir = new File(this.getLocalDir());
                if (!localCheckDir.exists()) {
                    if (this.isCreateDir() && "get".equals(this.getAction())) {
                        try {
                            if (!localCheckDir.mkdirs()) {
                                throw new JobSchedulerException("directory [" + this.getLocalDir() + "] couldn't created.");
                            }
                        } catch (Exception e) {
                            throw new JobSchedulerException("error occurred creating directory [" + this.getLocalDir() + "]: " + e.getMessage(), e);
                        }
                    } else if (!this.isCreateDir()) {
                        throw new JobSchedulerException("directory does not exist: " + this.getLocalDir());
                    }
                }
                if (this.getFileList() != null && !this.getFileList().isEmpty()) {
                    this.setRecursive(false);
                    Vector<File> v = new Vector<File>();
                    for (String filename : this.getFileList().split(";")) {
                        if ("get".equals(this.getAction())) {
                            v.add(new File(filename));
                        } else {
                            filename = this.normalizePath(filename);
                            if (this.getLocalDir() != null && !".".equals(this.getLocalDir()) && !filename.startsWith("/") && !filename.startsWith(":/", 1)) {
                                filename = this.getLocalDir() + "/" + filename;
                            }
                            File fobj = new File(filename);
                            if (fobj.exists()) {
                                if (fobj.isDirectory()) {
                                    spooler_log.info("file [" + fobj.getPath() + "] from filelist is a directory! Transfer will be skipped.");
                                } else {
                                    v.add(fobj);
                                }
                            } else {
                                spooler_log.info("file [" + fobj.getPath() + "] from filelist doesn't exist! Transfer will be skipped.");
                            }
                        }
                    }
                    this.setFilenames(v);
                } else {
                    this.setFilenames(new SOSFileSystemOperations().listFiles(this.getLocalDir(), this.getFileSpec(), Pattern.MULTILINE, this.isRecursive()));
                }
                int count = 0;
                String[] files = new String[this.getFilenames().size()];
                for (File file : this.getFilenames()) {
                    String filename = null;
                    if ("get".equals(this.getAction())) {
                        filename = this.normalizePath(file.getPath());
                        if (this.getRemoteDir() != null && !".".equals(this.getRemoteDir()) && !filename.startsWith("/") && !filename.startsWith(":/", 1)) {
                            filename = this.getRemoteDir() + "/" + filename;
                        }
                        spooler_log.info("file to receive: " + filename);
                    } else {
                        filename = file.getCanonicalPath();
                        spooler_log.info("file to send: " + filename);
                    }
                    files[count] = filename;
                    count++;
                }
                if ("get".equals(this.getAction())) {
                    scpClient.get(files, this.getLocalDir());
                } else {
                    if (this.isCreateDir()) {
                        execCommand("if [ ! -d " + this.getRemoteDir() + " ]; then mkdir -p " + this.getRemoteDir() + "; fi");
                    }
                    if (isRecursive()) {
                        scp_recursive(localCheckDir, this.getRemoteDir(), scpClient, this.getPermissions());
                    } else {
                        scpClient.put(files, this.getRemoteDir(), this.getPermissions());
                    }
                }
                switch (count) {
                case 0:
                    if (this.getFileList() != null && !this.getFileList().isEmpty()) {
                        throw new JobSchedulerException("no files found to transfer");
                    } else {
                        throw new JobSchedulerException("no matching files found for filter (file_spec) = " + fileSpec);
                    }
                case 1:
                    spooler_log.info("1 file transferred");
                    break;
                default:
                    spooler_log.info(count + " files transferred");
                    break;
                }
            } catch (JobSchedulerException e) {
                throw e;
            } catch (Exception e) {
                throw new JobSchedulerException("error occurred processing files: " + e.getMessage());
            } finally {
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

    private void scp_recursive(final File sourceFile, String targetFile, final SCPClient scpClient, final String perms) throws Exception {
        scp_recursive(sourceFile, targetFile, scpClient, perms, true);
    }

    private void scp_recursive(final File sourceFile, String targetFile, final SCPClient scpClient, final String perms, boolean firstLevel) throws Exception {
        if (sourceFile.isDirectory()) {
            if (!firstLevel) {
                targetFile += "/" + sourceFile.getName();
                execCommand("if [ ! -d " + targetFile + " ]; then mkdir " + targetFile + "; fi");
            }
            String[] children = sourceFile.list();
            for (String element : children) {
                scp_recursive(new File(sourceFile, element), targetFile, scpClient, perms, false);
            }
        } else {
            if (this.getFileSpec() != null && !this.getFileSpec().isEmpty()) {
                if (sourceFile.getName().matches(this.getFileSpec())) {
                    scpClient.put(sourceFile.getAbsolutePath(), targetFile, perms);
                }
            } else {
                scpClient.put(sourceFile.getAbsolutePath(), targetFile, perms);
            }
        }
    }

    private void execCommand(final String command) throws Exception {
        BufferedReader stdoutReader = null;
        BufferedReader stderrReader = null;
        try {
            Integer exitStatus = null;
            String exitSignal = null;
            spooler_log.debug5("executing remote command: " + command);
            this.setSshSession(this.getSshConnection().openSession());
            this.getSshSession().execCommand(command);
            spooler_log.debug5("output to stdout for remote command: " + command);
            InputStream stdout = new StreamGobbler(this.getSshSession().getStdout());
            stdoutReader = new BufferedReader(new InputStreamReader(stdout));
            while (true) {
                String line = stdoutReader.readLine();
                if (line == null) {
                    break;
                }
                spooler_log.info(line);
            }
            spooler_log.debug5("output to stderr for remote command: " + command);
            InputStream stderr = new StreamGobbler(this.getSshSession().getStderr());
            stderrReader = new BufferedReader(new InputStreamReader(stderr));
            String stderrOutput = "";
            while (true) {
                String line = stderrReader.readLine();
                if (line == null) {
                    break;
                }
                spooler_log.debug5(line);
                stderrOutput += line + "\n";
            }
            if (stderrOutput != null && !stderrOutput.isEmpty()) {
                throw new JobSchedulerException("remote execution reports error: " + stderrOutput);
            }
            try {
                exitStatus = this.getSshSession().getExitStatus();
            } catch (Exception e) {
                spooler_log.info("could not retrieve exit status, possibly not supported by remote ssh server");
            }
            if (exitStatus != null && !exitStatus.equals(new Integer(0))) {
                throw new JobSchedulerException("remote command terminated with exit status: " + exitStatus);
            }
            try {
                exitSignal = this.getSshSession().getExitSignal();
            } catch (Exception e) {
                spooler_log.info("could not retrieve exit signal, possibly not supported by remote ssh server");
            }
            if (exitSignal != null && !exitSignal.isEmpty()) {
                throw new JobSchedulerException("remote command terminated with exit signal: " + exitSignal);
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e.getMessage(), e);
        } finally {
            if (stdoutReader != null) {
                try {
                    stdoutReader.close();
                } catch (Exception e) {
                }
            }
            if (stderrReader != null) {
                try {
                    stderrReader.close();
                } catch (Exception e) {
                }
            }
            if (this.getSshSession() != null) {
                try {
                    this.getSshSession().close();
                    this.setSshSession(null);
                } catch (Exception ex) {
                    // gracefully ignore this error
                } 
            }
        }
    }

    public String getFileSpec() {
        return fileSpec;
    }

    public void setFileSpec(final String fileSpec) {
        this.fileSpec = fileSpec;
    }

    public String getLocalDir() {
        return localDir;
    }

    public void setLocalDir(final String localDir) {
        this.localDir = localDir;
    }

    public String getRemoteDir() {
        return remoteDir;
    }

    public void setRemoteDir(final String remoteDir) {
        this.remoteDir = remoteDir;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(final String permissions) {
        this.permissions = permissions;
    }

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public String getFileList() {
        return fileList;
    }

    public void setFileList(final String fileList) {
        this.fileList = fileList;
    }

    public boolean isCreateDir() {
        return createDir;
    }

    public void setCreateDir(final boolean createDir) {
        this.createDir = createDir;
    }

    public Vector<File> getFilenames() {
        return filenames;
    }

    public void setFilenames(final Vector<File> filenames) {
        this.filenames = filenames;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(final boolean recursive) {
        this.recursive = recursive;
    }

}