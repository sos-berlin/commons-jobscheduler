package sos.scheduler.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSFile;

/** @author andreas.pueschel */
public class JobSchedulerHttpPostJob extends JobSchedulerJob {

    protected Vector inputFileList = null;
    protected Iterator inputFileListIterator = null;
    protected String inputDirectory = "";
    protected String inputFileSpec = "^(.*)$";
    protected String outputDirectory = "";
    protected String url = "";
    protected String contentType = "";
    protected int timeout = 0;
    Variable_set mergedVariables;

    public boolean spooler_init() {
        try {
            if (!super.spooler_init()) {
                return false;
            }
            try {
                if (spooler_task != null) {
                    if (spooler_task.params().var("input") != null && !spooler_task.params().var("input").isEmpty()) {
                        this.setInputDirectory(spooler_task.params().var("input"));
                        spooler_log.info(".. job parameter [input]: " + this.getInputDirectory());
                    }
                    if (spooler_task.params().var("input_directory") != null && !spooler_task.params().var("input_directory").isEmpty()) {
                        this.setInputDirectory(spooler_task.params().var("input_directory"));
                        spooler_log.info(".. job parameter [input_directory]: " + this.getInputDirectory());
                    }
                    if (spooler_task.params().var("input_filespec") != null && !spooler_task.params().var("input_filespec").isEmpty()) {
                        this.setInputFileSpec(spooler_task.params().var("input_filespec"));
                        spooler_log.info(".. job parameter [input_filespec]: " + this.getInputFileSpec());
                    }
                    if (spooler_task.params().var("output") != null && !spooler_task.params().var("output").isEmpty()) {
                        this.setOutputDirectory(spooler_task.params().var("output"));
                        spooler_log.info(".. job parameter [output]: " + this.getOutputDirectory());
                    }
                    if (spooler_task.params().var("output_directory") != null && !spooler_task.params().var("output_directory").isEmpty()) {
                        this.setOutputDirectory(spooler_task.params().var("output_directory"));
                        spooler_log.info(".. job parameter [output_directory]: " + this.getOutputDirectory());
                    }
                    if (spooler_task.params().var("url") != null && !spooler_task.params().var("url").isEmpty()) {
                        this.setUrl(spooler_task.params().var("url"));
                        spooler_log.info(".. job parameter [url]: " + this.getUrl());
                    }
                    if (spooler_task.params().var("content_type") != null && !spooler_task.params().var("content_type").isEmpty()) {
                        this.setContentType(spooler_task.params().var("content_type"));
                        spooler_log.info(".. job parameter [content_type]: " + this.getContentType());
                    }
                }
                System.setProperty("org.apache.commons.logging.log", "sos.util.SOSJCLNullLogger");
            } catch (Exception e) {
                throw (new Exception("an error occurred processing job parameters: " + e.getMessage()));
            }
            return true;
        } catch (Exception e) {
            spooler_log.warn("failed to initialize job: " + e.getMessage());
            return false;
        }
    }

    public boolean spooler_open() {
        try {
            if (this.getInputDirectory() == null || this.getInputDirectory().isEmpty()) {
                return true;
            }
            File inputFile = new File(this.getInputDirectory());
            if (inputFile.isDirectory()) {
                if (!inputFile.canRead()) {
                    throw new Exception("input directory is not accessible: " + inputFile.getCanonicalPath());
                }
                spooler_log.debug6("retrieving files from directory: " + this.getInputDirectory() + " for file specification: " + this.getInputFileSpec());
                this.inputFileList = SOSFile.getFilelist(this.getInputDirectory(), this.getInputFileSpec(), 0);
            } else {
                if (!inputFile.canRead()) {
                    throw new Exception("input file is not accessible: " + inputFile.getCanonicalPath());
                }
                this.inputFileList = new Vector();
                this.inputFileList.add(inputFile);
            }
            if (!this.inputFileList.isEmpty()) {
                spooler_log.info(this.inputFileList.size() + " input files found");
            }
            this.inputFileListIterator = this.inputFileList.iterator();
            return this.inputFileListIterator.hasNext();
        } catch (Exception e) {
            spooler_log.warn("failed to retrieve input files from directory [" + this.getInputDirectory() + ", " + this.getInputFileSpec() + "]: "
                    + e.getMessage());
            return false;
        }
    }

    public boolean spooler_process() {
        Order order = null;
        File inputFile = null;
        File outputFile = null;
        String inputFileSpec = "";
        String url = "";
        String contentType = "";
        boolean rc = true;
        try {
            if (this.getOutputDirectory() != null && !this.getOutputDirectory().isEmpty()) {
                outputFile = new File(this.getOutputDirectory());
            }
            getLogger().debug8("merging parameters...");
            mergedVariables = spooler.create_variable_set();
            Variable_set env = spooler_task.create_subprocess().env();
            String[] envVars = env.names().split(";");
            for (int i = 0; i < envVars.length; i++) {
                String currentName = envVars[i];
                mergedVariables.set_var(currentName, env.value(currentName));
            }
            mergedVariables.merge(spooler_task.params());
            if (spooler_task.job().order_queue() == null) {
                if (!this.inputFileListIterator.hasNext()) {
                    return true;
                }
                inputFile = (File) this.inputFileListIterator.next();
                rc = this.inputFileListIterator.hasNext();
                if (inputFile == null) {
                    throw new Exception("empty input file item was found.");
                }
            } else {
                if (this.inputFileListIterator != null && this.inputFileListIterator.hasNext()) {
                    inputFile = (File) this.inputFileListIterator.next();
                }
                order = spooler_task.order();
                mergedVariables.merge(order.params());
                if (order.params() != null) {
                    if (order.params().value("input") != null && !order.params().value("input").toString().isEmpty()) {
                        inputFile = new File(order.params().value("input").toString());
                        spooler_log.info(".. order parameter [input]: " + order.params().value("input").toString());
                    } else if (order.params().value("input_directory") != null && !order.params().value("input_directory").toString().isEmpty()) {
                        inputFile = new File(order.params().value("input_directory").toString());
                        spooler_log.info(".. order parameter [input_directory]: " + order.params().value("input_directory").toString());
                    }
                    if (order.params().value("input_filespec") != null && !order.params().value("input_filespec").toString().isEmpty()) {
                        inputFileSpec = order.params().value("input_filespec").toString();
                        spooler_log.info(".. order parameter [input_filespec]: " + inputFileSpec);
                    }
                    if (order.params().value("output") != null && !order.params().value("output").toString().isEmpty()) {
                        outputFile = new File((order.params().value("output").toString()));
                        spooler_log.info(".. order parameter [output]: " + order.params().value("output").toString());
                    } else if (order.params().value("output_directory") != null && !order.params().value("output_directory").toString().isEmpty()) {
                        outputFile = new File((order.params().value("output_directory").toString()));
                        spooler_log.info(".. order parameter [output_directory]: " + order.params().value("output_directory").toString());
                    }
                    if (order.params().value("url") != null && !order.params().value("url").toString().isEmpty()) {
                        url = order.params().value("url").toString();
                        spooler_log.info(".. order parameter [url]: " + url);
                    }
                    if (order.params().value("content_type") != null && !order.params().value("content_type").toString().isEmpty()) {
                        contentType = order.params().value("content_type").toString();
                        spooler_log.info(".. order parameter [content_type]: " + contentType);
                    }
                }
            }
            if (url == null || url.isEmpty()) {
                url = this.getUrl();
            }
            if (inputFileSpec == null || inputFileSpec.isEmpty()) {
                inputFileSpec = this.getInputFileSpec();
            }
            if (!inputFile.exists()) {
                throw new Exception("input file [" + inputFile.getCanonicalPath() + "] does not exist.");
            }
            if (url == null || url.isEmpty()) {
                throw new Exception("no URL was given to post files.");
            }
            if (contentType == null || contentType.isEmpty()) {
                contentType = this.getContentType();
            }
            if (contentType == null || !contentType.isEmpty()) {
                if (inputFile.getName().endsWith(".xml")) {
                    contentType = "text/xml";
                } else if (inputFile.getName().endsWith(".htm") || inputFile.getName().endsWith(".html")) {
                    contentType = "text/html";
                }
                if ("text/html".equals(contentType) || "text/xml".equals(contentType)) {
                    BufferedReader br = new BufferedReader(new FileReader(inputFile));
                    String buffer = "";
                    String line = null;
                    int c = 0;
                    while ((line = br.readLine()) != null || ++c > 5) {
                        buffer += line;
                    }
                    Pattern p = Pattern.compile("encoding[\\s]*=[\\s]*['\"](.*?)['\"]", Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(buffer);
                    if (m.find()) {
                        contentType += "; charset=" + m.group(1);
                    }
                    br.close();
                }
            }
            int responseCode = postFile(inputFile, this.getInputFileSpec(), outputFile, contentType, url);
            spooler_log.info("input file [" + inputFile.getCanonicalPath() + "] processed for URL [" + url + "] with response code " + responseCode);
            if (spooler_task.job().order_queue() != null) {
                return rc;
            } else {
                return this.inputFileListIterator.hasNext();
            }
        } catch (Exception e) {
            String message = "error occurred processing";
            if (inputFile != null) {
                message += " file [" + inputFile.getAbsolutePath() + "]";
            }
            if (url != null && !url.isEmpty()) {
                message += " for url [" + url + "]";
            }
            spooler_log.warn(message + ": " + e);
            if (spooler_task.job().order_queue() != null) {
                spooler_task.end();
                return false;
            } else {
                return this.inputFileListIterator.hasNext();
            }
        }
    }

    public int postFile(File inputFile, String inputFileSpec, File outputFile, String contentType, String url) throws Exception {
        int rc = 0;
        try {
            if (inputFile.isDirectory()) {
                Vector filelist = SOSFile.getFilelist(inputFile.getCanonicalPath(), inputFileSpec, 0);
                Iterator iterator = filelist.iterator();
                while (iterator.hasNext()) {
                    rc = this.postFile((File) iterator.next(), inputFileSpec, outputFile, contentType, url);
                }
                return rc;
            } else {
                PostMethod post = new PostMethod(url);
                String content = SOSFile.readFile(inputFile);
                getLogger().debug9("post before replacements: " + content);
                content = mergedVariables.substitute(content);
                getLogger().debug5("Posting: " + content);
                StringRequestEntity req = new StringRequestEntity(content);
                post.setRequestEntity(req);
                post.setRequestHeader("Content-type", contentType);
                HttpClient httpClient = new HttpClient();
                if (this.getTimeout() > 0) {
                    HttpConnectionManager httpManager = httpClient.getHttpConnectionManager();
                    HttpConnectionManagerParams httpParams = new HttpConnectionManagerParams();
                    httpParams.setConnectionTimeout(this.getTimeout() * 1000);
                    httpManager.setParams(httpParams);
                }
                rc = httpClient.executeMethod(post);
                if (outputFile != null) {
                    logResponse(inputFile, outputFile, post.getResponseBodyAsStream());
                }
                return rc;
            }
        } catch (Exception e) {
            throw new Exception("error occurred in HTTP POST: " + e.getMessage());
        }
    }

    private void logResponse(File inputFile, File outputFile, InputStream responseStream) throws Exception {
        if (outputFile == null) {
            throw new Exception("cannot write response: output file is null");
        }
        if (responseStream == null) {
            throw new Exception("cannot write response: response is null");
        }
        if (!outputFile.canRead()) {
            File path = new File(outputFile.getParent());
            if (!path.canRead()) {
                path.mkdirs();
            }
            outputFile.createNewFile();
        }
        if (!outputFile.canWrite()) {
            throw new Exception("cannot write to file: " + outputFile.getCanonicalPath());
        }
        if (outputFile.isDirectory()) {
            outputFile = new File(outputFile.getCanonicalPath() + "/" + inputFile.getName());
        }
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        try {
            byte buffer[] = new byte[1000];
            int numOfBytes = 0;
            while ((numOfBytes = responseStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, numOfBytes);
            }
        } catch (Exception e) {
            throw new Exception("error occurred while logging to file [" + outputFile.getCanonicalPath() + "]: " + e.getMessage());
        } finally {
            try {
                if (responseStream != null) {
                    responseStream.close();
                }
            } catch (Exception ex) {
                // gracefully ignore this error
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception ex) {
                // gracefully ignore this error
            }
        }
    }

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public String getInputFileSpec() {
        return inputFileSpec;
    }

    public void setInputFileSpec(String inputFileSpec) {
        this.inputFileSpec = inputFileSpec;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

}