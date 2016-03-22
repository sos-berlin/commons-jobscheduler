package sos.scheduler.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Vector;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import sos.spooler.Order;
import sos.spooler.Variable_set;

import sos.util.SOSFile;

/** post files via http, based on Jakarta Commons HTTP Client, see
 * commons-httpclient-3.0.jar
 * 
 * job parameters: input_directory input_filespec output_directory url
 * 
 * @author andreas.pueschel@sos-berlin.com
 * @since 1.0 2006-02-12 */
public class JobSchedulerHttpPostJob extends JobSchedulerJob {

    protected Vector inputFileList = null;

    protected Iterator inputFileListIterator = null;

    /** input directory */
    protected String inputDirectory = "";

    /** regular expression for file list */
    protected String inputFileSpec = "^(.*)$";

    /** output directory */
    protected String outputDirectory = "";

    /** post to url */
    protected String url = "";

    /** content type of input file(s) */
    protected String contentType = "";

    /** timeout for http request */
    protected int timeout = 0;

    /** Set of merged variables for replacements **/
    Variable_set mergedVariables;

    /** Initialisation */
    public boolean spooler_init() {

        try {
            if (!super.spooler_init())
                return false;

            try { // to process job parameters
                if (spooler_task != null) {

                    if (spooler_task.params().var("input") != null && spooler_task.params().var("input").length() > 0) {
                        this.setInputDirectory(spooler_task.params().var("input"));
                        spooler_log.info(".. job parameter [input]: " + this.getInputDirectory());
                    }

                    if (spooler_task.params().var("input_directory") != null && spooler_task.params().var("input_directory").length() > 0) {
                        this.setInputDirectory(spooler_task.params().var("input_directory"));
                        spooler_log.info(".. job parameter [input_directory]: " + this.getInputDirectory());
                    }

                    if (spooler_task.params().var("input_filespec") != null && spooler_task.params().var("input_filespec").length() > 0) {
                        this.setInputFileSpec(spooler_task.params().var("input_filespec"));
                        spooler_log.info(".. job parameter [input_filespec]: " + this.getInputFileSpec());
                    }

                    if (spooler_task.params().var("output") != null && spooler_task.params().var("output").length() > 0) {
                        this.setOutputDirectory(spooler_task.params().var("output"));
                        spooler_log.info(".. job parameter [output]: " + this.getOutputDirectory());
                    }

                    if (spooler_task.params().var("output_directory") != null && spooler_task.params().var("output_directory").length() > 0) {
                        this.setOutputDirectory(spooler_task.params().var("output_directory"));
                        spooler_log.info(".. job parameter [output_directory]: " + this.getOutputDirectory());
                    }

                    if (spooler_task.params().var("url") != null && spooler_task.params().var("url").length() > 0) {
                        this.setUrl(spooler_task.params().var("url"));
                        spooler_log.info(".. job parameter [url]: " + this.getUrl());
                    }

                    if (spooler_task.params().var("content_type") != null && spooler_task.params().var("content_type").length() > 0) {
                        this.setContentType(spooler_task.params().var("content_type"));
                        spooler_log.info(".. job parameter [content_type]: " + this.getContentType());
                    }
                }

                // use a simple Apache Logger
                // System.setProperty("org.apache.commons.logging.Log",
                // "org.apache.commons.logging.impl.SimpleLog");
                // do not log
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

    /** check for files to post */

    public boolean spooler_open() {

        try {
            if (this.getInputDirectory() == null || this.getInputDirectory().length() == 0)
                return true;

            File inputFile = new File(this.getInputDirectory());
            if (inputFile.isDirectory()) {
                if (!inputFile.canRead())
                    throw new Exception("input directory is not accessible: " + inputFile.getCanonicalPath());
                spooler_log.debug6("retrieving files from directory: " + this.getInputDirectory() + " for file specification: " + this.getInputFileSpec());
                this.inputFileList = SOSFile.getFilelist(this.getInputDirectory(), this.getInputFileSpec(), 0);
            } else {
                if (!inputFile.canRead())
                    throw new Exception("input file is not accessible: " + inputFile.getCanonicalPath());
                this.inputFileList = new Vector();
                this.inputFileList.add(inputFile);
            }

            if (this.inputFileList.size() > 0)
                spooler_log.info(this.inputFileList.size() + " input files found");

            this.inputFileListIterator = this.inputFileList.iterator();
            return this.inputFileListIterator.hasNext();

        } catch (Exception e) {
            spooler_log.warn("failed to retrieve input files from directory [" + this.getInputDirectory() + ", " + this.getInputFileSpec() + "]: "
                    + e.getMessage());
            return false;
        }
    }

    /** Process single file */
    public boolean spooler_process() {

        Order order = null;
        File inputFile = null;
        File outputFile = null;
        String inputFileSpec = "";
        String url = "";
        String contentType = "";

        boolean rc = true;

        try {
            if (this.getOutputDirectory() != null && this.getOutputDirectory().length() > 0) {
                outputFile = new File(this.getOutputDirectory());
            }
            getLogger().debug8("merging parameters...");
            mergedVariables = spooler.create_variable_set();

            Variable_set env = spooler_task.create_subprocess().env();
            // this merge doesn't work:
            // mergedVariables.merge(env);
            String[] envVars = env.names().split(";");
            for (int i = 0; i < envVars.length; i++) {
                String currentName = envVars[i];
                mergedVariables.set_var(currentName, env.value(currentName));
            }
            mergedVariables.merge(spooler_task.params());
            // classic or order driven
            if (spooler_task.job().order_queue() == null) {
                if (!this.inputFileListIterator.hasNext())
                    return true;
                inputFile = (File) this.inputFileListIterator.next();
                rc = this.inputFileListIterator.hasNext();

                if (inputFile == null)
                    throw new Exception("empty input file item was found.");

            } else {
                // input file migth be specified on startup for order driven
                // jobs
                if (this.inputFileListIterator != null && this.inputFileListIterator.hasNext()) {
                    inputFile = (File) this.inputFileListIterator.next();
                }

                order = spooler_task.order();
                mergedVariables.merge(order.params());
                if (order.params() != null) {
                    if (order.params().value("input") != null && order.params().value("input").toString().length() > 0) {
                        inputFile = new File(order.params().value("input").toString());
                        spooler_log.info(".. order parameter [input]: " + order.params().value("input").toString());
                    } else if (order.params().value("input_directory") != null && order.params().value("input_directory").toString().length() > 0) {
                        inputFile = new File(order.params().value("input_directory").toString());
                        spooler_log.info(".. order parameter [input_directory]: " + order.params().value("input_directory").toString());
                    }

                    if (order.params().value("input_filespec") != null && order.params().value("input_filespec").toString().length() > 0) {
                        inputFileSpec = order.params().value("input_filespec").toString();
                        spooler_log.info(".. order parameter [input_filespec]: " + inputFileSpec);
                    }

                    if (order.params().value("output") != null && order.params().value("output").toString().length() > 0) {
                        outputFile = new File((order.params().value("output").toString()));
                        spooler_log.info(".. order parameter [output]: " + order.params().value("output").toString());
                    } else if (order.params().value("output_directory") != null && order.params().value("output_directory").toString().length() > 0) {
                        outputFile = new File((order.params().value("output_directory").toString()));
                        spooler_log.info(".. order parameter [output_directory]: " + order.params().value("output_directory").toString());
                    }

                    if (order.params().value("url") != null && order.params().value("url").toString().length() > 0) {
                        url = order.params().value("url").toString();
                        spooler_log.info(".. order parameter [url]: " + url);
                    }

                    if (order.params().value("content_type") != null && order.params().value("content_type").toString().length() > 0) {
                        contentType = order.params().value("content_type").toString();
                        spooler_log.info(".. order parameter [content_type]: " + contentType);
                    }
                }
            }

            if (url == null || url.length() == 0)
                url = this.getUrl();
            if (inputFileSpec == null || inputFileSpec.length() == 0)
                inputFileSpec = this.getInputFileSpec();

            if (!inputFile.exists())
                throw new Exception("input file [" + inputFile.getCanonicalPath() + "] does not exist.");
            if (url == null || url.length() == 0)
                throw new Exception("no URL was given to post files.");

            if (contentType == null || contentType.length() == 0)
                contentType = this.getContentType();
            if (contentType == null || contentType.length() == 0) {
                if (inputFile.getName().endsWith(".xml")) {
                    contentType = "text/xml";
                } else if (inputFile.getName().endsWith(".htm") || inputFile.getName().endsWith(".html")) {
                    contentType = "text/html";
                }

                // encoding ermitteln
                if (contentType.equals("text/html") || contentType.equals("text/xml")) {
                    BufferedReader br = new BufferedReader(new FileReader(inputFile));
                    String buffer = "";
                    String line = null;
                    int c = 0;
                    // die ersten 5 Zeilen nach einem encoding durchsuchen
                    while ((line = br.readLine()) != null || ++c > 5)
                        buffer += line;
                    Pattern p = Pattern.compile("encoding[\\s]*=[\\s]*['\"](.*?)['\"]", Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(buffer);
                    if (m.find())
                        contentType += "; charset=" + m.group(1);
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
            if (inputFile != null)
                message += " file [" + inputFile.getAbsolutePath() + "]";
            if (url != null && url.length() > 0)
                message += " for url [" + url + "]";
            spooler_log.warn(message + ": " + e);

            // restart this order driven task in case of errors to make the task
            // log available by mail
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
                // post.setRequestEntity(new InputStreamRequestEntity(new
                // FileInputStream(inputFile), inputFile.length()));
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

                if (outputFile != null)
                    logResponse(inputFile, outputFile, post.getResponseBodyAsStream());
                return rc;
            }
        } catch (Exception e) {
            throw new Exception("error occurred in HTTP POST: " + e.getMessage());
        }
    }

    private void logResponse(File inputFile, File outputFile, InputStream responseStream) throws Exception {

        if (outputFile == null)
            throw new Exception("cannot write response: output file is null");
        if (responseStream == null)
            throw new Exception("cannot write response: response is null");

        if (!outputFile.canRead()) {
            File path = new File(outputFile.getParent());
            if (!path.canRead())
                path.mkdirs();
            outputFile.createNewFile();
        }

        if (!outputFile.canWrite())
            throw new Exception("cannot write to file: " + outputFile.getCanonicalPath());

        if (outputFile.isDirectory())
            outputFile = new File(outputFile.getCanonicalPath() + "/" + inputFile.getName());

        FileOutputStream outputStream = new FileOutputStream(outputFile);

        try {
            byte buffer[] = new byte[1000];
            int numOfBytes = 0;

            while ((numOfBytes = responseStream.read(buffer)) != -1)
                outputStream.write(buffer, 0, numOfBytes);
        } catch (Exception e) {
            throw new Exception("error occurred while logging to file [" + outputFile.getCanonicalPath() + "]: " + e.getMessage());
        } finally {
            try {
                if (responseStream != null) {
                    responseStream.close();
                }
            } catch (Exception ex) {
            } // gracefully ignore this error
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception ex) {
            } // gracefully ignore this error
        }
    }

    /** @return Returns the inputDirectory. */
    public String getInputDirectory() {
        return inputDirectory;
    }

    /** @param inputDirectory The inputDirectory to set. */
    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    /** @return Returns the inputFileSpec. */
    public String getInputFileSpec() {
        return inputFileSpec;
    }

    /** @param inputFileSpec The inputFileSpec to set. */
    public void setInputFileSpec(String inputFileSpec) {
        this.inputFileSpec = inputFileSpec;
    }

    /** @return Returns the outputDirectory. */
    public String getOutputDirectory() {
        return outputDirectory;
    }

    /** @param outputDirectory The outputDirectory to set. */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /** @return Returns the url. */
    public String getUrl() {
        return url;
    }

    /** @param url The url to set. */
    public void setUrl(String url) {
        this.url = url;
    }

    /** @return Returns the contentType. */
    public String getContentType() {
        return contentType;
    }

    /** @param contentType The contentType to set. */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /** @return Returns the timeout. */
    public int getTimeout() {
        return timeout;
    }

    /** @param timeout The timeout to set. */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

}