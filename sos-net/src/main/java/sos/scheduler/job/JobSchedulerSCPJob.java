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

/** @author andreas.pueschel@sos-berlin.com
 * @author ghassan.beydoun@sos-berlin.com
 *
 *         see job documentation in the package jobdoc for details */
public class JobSchedulerSCPJob extends JobSchedulerSSHBaseJob {

    /** action specifies the copy direction: get, put */
    protected String action = "";
    /** list of files to be copied */
    protected String fileList = "";
    /** regular expression specifying file names for transfer */
    protected String fileSpec = "";
    /** local directory for files specified with the attribute fileSpec */
    protected String localDir = "";
    /** remote directory */
    protected String remoteDir = "";
    /** create directories if needed */
    protected boolean createDir = true;
    /** enable recursive processing of directories */
    protected boolean recursive = false;
    /** create files with explicit permissions */
    protected String permissions = "0600";
    private Vector<File> filenames = null;

    /** Processing */
    @Override
    public boolean spooler_process() {
        spooler_log.info(VersionInfo.VERSION_STRING);
        Order order = null;
        Variable_set params = null;
        SCPClient scpClient = null;
        try {
            try { // to fetch parameters from orders that have precedence over
                  // job parameters
                params = spooler_task.params();
                if (spooler_task.job().order_queue() != null) {
                    order = spooler_task.order();
                    if (order.params() != null)
                        params.merge(order.params());
                }
                // get basic authentication parameters
                this.getBaseParameters();
                if (params.value("action") != null && params.value("action").length() > 0) {
                    if (!params.value("action").equalsIgnoreCase("get") && !params.value("action").equalsIgnoreCase("put"))
                        throw new JobSchedulerException("invalid action parameter [action] specified, expected get, put: " + params.value("action"));
                    this.setAction(params.value("action").toLowerCase());
                    spooler_log.info(".. parameter [action]: " + this.getAction());
                } else {
                    throw new JobSchedulerException("no action parameter [action] was specified");
                }
                if (params.value("file_list") != null && params.value("file_list").length() > 0) {
                    this.setFileList(params.value("file_list"));
                    spooler_log.info(".. parameter [file_list]: " + this.getFileList());
                } else {
                    if (this.getAction().equals("get"))
                        throw new JobSchedulerException("action [get] requires filenames being specified as parameter [file_list]");
                    this.setFileList("");
                }
                if (params.value("file_spec") != null && params.value("file_spec").length() > 0) {
                    this.setFileSpec(params.value("file_spec"));
                    spooler_log.info(".. parameter [file_spec]: " + this.getFileSpec());
                } else {
                    this.setFileSpec("^(.*)$");
                }
                if (params.value("local_dir") != null && params.value("local_dir").length() > 0) {
                    this.setLocalDir(this.normalizePath(params.value("local_dir")));
                    spooler_log.info(".. parameter [local_dir]: " + params.value("local_dir"));
                } else {
                    this.setLocalDir(".");
                }
                if (params.value("remote_dir") != null && params.value("remote_dir").length() > 0) {
                    this.setRemoteDir(this.normalizePath(params.value("remote_dir")));
                    spooler_log.info(".. parameter [remote_dir]: " + params.value("remote_dir"));
                } else {
                    this.setRemoteDir(".");
                }
                if (params.value("create_dir") != null && params.value("create_dir").length() > 0) {
                    if (params.value("create_dir").equalsIgnoreCase("true") || params.value("create_dir").equalsIgnoreCase("yes")
                            || params.value("create_dir").equals("1")) {
                        this.setCreateDir(true);
                    } else {
                        this.setCreateDir(false);
                    }
                    spooler_log.info(".. parameter [create_dir]: " + this.isCreateDir());
                } else {
                    this.setCreateDir(true);
                }
                if (params.value("recursive") != null && params.value("recursive").length() > 0) {
                    if (params.value("recursive").equalsIgnoreCase("true") || params.value("recursive").equalsIgnoreCase("yes")
                            || params.value("recursive").equals("1")) {
                        this.setRecursive(true);
                    } else {
                        this.setRecursive(false);
                    }
                    spooler_log.info(".. parameter [recursive]: " + this.isRecursive());
                } else {
                    this.setRecursive(false);
                }
                if (params.value("permissions") != null && params.value("permissions").length() > 0) {
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
            try { // to connect, authenticate and process files
                this.getBaseAuthentication();
                scpClient = new SCPClient(this.getSshConnection());
                File localCheckDir = new File(this.getLocalDir());
                if (!localCheckDir.exists()) {
                    if (this.isCreateDir() && this.getAction().equals("get")) {
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
                if (this.getFileList() != null && this.getFileList().length() > 0) {
                    this.setRecursive(false);
                    Vector<File> v = new Vector<File>();
                    for (String filename : this.getFileList().split(";")) {
                        if (this.getAction().equals("get")) {
                            v.add(new File(filename));
                        } else {
                            filename = this.normalizePath(filename);
                            if (this.getLocalDir() != null && !this.getLocalDir().equals(".") && !filename.startsWith("/")
                                    && !filename.startsWith(":/", 1)) {
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
                    // list of files for put operations only
                    this.setFilenames(new SOSFileSystemOperations().listFiles(this.getLocalDir(), this.getFileSpec(), Pattern.MULTILINE, this.isRecursive()));
                }
                int count = 0;
                String[] files = new String[this.getFilenames().size()];
                for (File file : this.getFilenames()) {
                    String filename = null;
                    if (this.getAction().equals("get")) {
                        filename = this.normalizePath(file.getPath());
                        if (this.getRemoteDir() != null && !this.getRemoteDir().equals(".") && !filename.startsWith("/")
                                && !filename.startsWith(":/", 1)) {
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
                if (this.getAction().equals("get")) {
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
                    if (this.getFileList() != null && this.getFileList().length() > 0) {
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
                if (this.getSshConnection() != null)
                    try {
                        this.getSshConnection().close();
                        this.setSshConnection(null);
                    } catch (Exception ex) {
                    } // gracefully ignore this error
            }
            // return value for classic and order driven processing
            return spooler_task.job().order_queue() != null;
        } catch (Exception e) {
            spooler_log.error(e.getMessage());
            return false;
        }
    }

    /** @param sourceFile
     * @param scpClient
     * @throws Exception */
    private void scp_recursive(final File sourceFile, String targetFile, final SCPClient scpClient, final String perms) throws Exception {
        scp_recursive(sourceFile, targetFile, scpClient, perms, true);
    }

    private void scp_recursive(final File sourceFile, String targetFile, final SCPClient scpClient, final String perms, boolean firstLevel)
            throws Exception {
        if (sourceFile.isDirectory()) {
            // create target directory if does not exist
            if (!firstLevel) {
                targetFile += "/" + sourceFile.getName();
                // TODO auweia, das ist aber ein heftiger hack. was ist, wenn
                // keine shell da ist?
                execCommand("if [ ! -d " + targetFile + " ]; then mkdir " + targetFile + "; fi");
            }
            String[] children = sourceFile.list();
            for (String element : children) {
                scp_recursive(new File(sourceFile, element), targetFile, scpClient, perms, false);
            }
        } else {
            if (this.getFileSpec() != null && this.getFileSpec().length() > 0) {
                // copy matching file only if asked
                if (sourceFile.getName().matches(this.getFileSpec()))
                    scpClient.put(sourceFile.getAbsolutePath(), targetFile, perms);
            } else {
                scpClient.put(sourceFile.getAbsolutePath(), targetFile, perms);
            }
        }
    }

    /** executes a command on the remote host
     *
     * @param command
     * @throws Exception */
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
                if (line == null)
                    break;
                spooler_log.info(line);
            }
            spooler_log.debug5("output to stderr for remote command: " + command);
            InputStream stderr = new StreamGobbler(this.getSshSession().getStderr());
            stderrReader = new BufferedReader(new InputStreamReader(stderr));
            String stderrOutput = "";
            while (true) {
                String line = stderrReader.readLine();
                if (line == null)
                    break;
                spooler_log.debug5(line);
                stderrOutput += line + "\n";
            }
            if (stderrOutput != null && stderrOutput.length() > 0) {
                throw new JobSchedulerException("remote execution reports error: " + stderrOutput);
            }
            try {
                exitStatus = this.getSshSession().getExitStatus();
            } catch (Exception e) {
                spooler_log.info("could not retrieve exit status, possibly not supported by remote ssh server");
            }
            if (exitStatus != null) {
                if (!exitStatus.equals(new Integer(0))) {
                    throw new JobSchedulerException("remote command terminated with exit status: " + exitStatus);
                }
            }
            try {
                exitSignal = this.getSshSession().getExitSignal();
            } catch (Exception e) {
                spooler_log.info("could not retrieve exit signal, possibly not supported by remote ssh server");
            }
            if (exitSignal != null) {
                if (exitSignal.length() > 0) {
                    throw new JobSchedulerException("remote command terminated with exit signal: " + exitSignal);
                }
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
            if (this.getSshSession() != null)
                try {
                    this.getSshSession().close();
                    this.setSshSession(null);
                } catch (Exception ex) {
                } // gracefully ignore this error
        }
    }// executeCommand

    /** @return Returns the fileSpec. */
    public String getFileSpec() {
        return fileSpec;
    }

    /** @param fileSpec The fileSpec to set. */
    public void setFileSpec(final String fileSpec) {
        this.fileSpec = fileSpec;
    }

    /** @return Returns the localDir. */
    public String getLocalDir() {
        return localDir;
    }

    /** @param localDir The localDir to set. */
    public void setLocalDir(final String localDir) {
        this.localDir = localDir;
    }

    /** @return Returns the remoteDir. */
    public String getRemoteDir() {
        return remoteDir;
    }

    /** @param remoteDir The remoteDir to set. */
    public void setRemoteDir(final String remoteDir) {
        this.remoteDir = remoteDir;
    }

    /** @return Returns the permissions. */
    public String getPermissions() {
        return permissions;
    }

    /** @param permissions The permissions to set. */
    public void setPermissions(final String permissions) {
        this.permissions = permissions;
    }

    /** @return Returns the action. */
    public String getAction() {
        return action;
    }

    /** @param action The action to set. */
    public void setAction(final String action) {
        this.action = action;
    }

    /** @return Returns the fileList. */
    public String getFileList() {
        return fileList;
    }

    /** @param fileList The fileList to set. */
    public void setFileList(final String fileList) {
        this.fileList = fileList;
    }

    /** @return Returns the createDir. */
    public boolean isCreateDir() {
        return createDir;
    }

    /** @param createDir The createDir to set. */
    public void setCreateDir(final boolean createDir) {
        this.createDir = createDir;
    }

    /** @return Returns the filenames. */
    public Vector<File> getFilenames() {
        return filenames;
    }

    /** @param filenames The filenames to set. */
    public void setFilenames(final Vector<File> filenames) {
        this.filenames = filenames;
    }

    /** @return Returns the recursive. */
    public boolean isRecursive() {
        return recursive;
    }

    /** @param recursive The recursive to set. */
    public void setRecursive(final boolean recursive) {
        this.recursive = recursive;
    }
}
