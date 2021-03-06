package sos.scheduler.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sos.util.SOSFile;

public class JobSchedulerProcessCheckFileMailJob extends JobSchedulerProcessSendMailJob {

    private String fileSpec;
    private String filePath;
    private String fileContent;

    protected boolean doSendMail() {
        boolean found = false;
        fileSpec = "";
        fileContent = "";
        filePath = "";
        try {
            if (this.getParameters().value("file_spec") != null && !this.getParameters().value("file_spec").isEmpty()) {
                fileSpec = this.getParameters().value("file_spec");
            }
            if (this.getParameters().value("file_path") != null && !this.getParameters().value("file_path").isEmpty()) {
                filePath = this.getParameters().value("file_path");
            }
            if (this.getParameters().value("file_content") != null && !this.getParameters().value("file_content").isEmpty()) {
                fileContent = this.getParameters().value("file_content");
            }
        } catch (Exception e) {
            spooler_log.warn("error occurred checking parameters: " + e.getMessage());
        }
        if (fileSpec.isEmpty() && fileContent.isEmpty() && filePath.isEmpty()) {
            spooler_log.info("JobSchedulerProcessCheckFileMailJob is not configured, suppressing mail.");
            return false;
        }
        try {
            Vector fileList = new Vector();
            if (fileSpec.isEmpty() && spooler_job.order_queue() != null) {
                File triggeredFile = new File(spooler_task.order().id());
                fileList.add(triggeredFile);
            } else {
                fileList = SOSFile.getFilelist(filePath, fileSpec, 0);
            }
            spooler_log.debug3("Lenght of filelist: " + fileList.size());
            Iterator iter = fileList.iterator();
            Pattern pattern = null;
            if (!fileContent.isEmpty()) {
                pattern = Pattern.compile(fileContent);
            }
            while (iter.hasNext()) {
                File curFile = (File) iter.next();
                if (pattern == null && curFile.exists()) {
                    spooler_log.info("Found file " + curFile.getAbsolutePath() + ", sending mail.");
                    found = true;
                    break;
                }
                if (pattern != null && curFile.exists() && grep(curFile, pattern)) {
                    found = true;
                    break;
                }
            }
        } catch (Exception e) {
            spooler_log.warn("error occurred checking file(s): " + e.getMessage());
        }
        return found;
    }

    private boolean grep(File file, Pattern regex) throws Exception {
        boolean found = false;
        try {
            spooler_log.debug7("Grepping file " + file.getAbsolutePath() + " for regex: " + regex.pattern());
            BufferedReader in = new BufferedReader(new FileReader(file));
            String currentLine = "";
            while ((currentLine = in.readLine()) != null) {
                Matcher matcher = regex.matcher(currentLine);
                if (matcher.find()) {
                    String match = matcher.group();
                    spooler_log.debug1("Found String '" + match + "' in file " + file.getAbsolutePath() + ", sending mail.");
                    found = true;
                    String body = "";
                    if (this.getParameters().value("body") != null && !this.getParameters().value("body").isEmpty()) {
                        body = this.getParameters().value("body") + "\n";
                    }
                    body += "'" + match + "' was found in file " + file.getAbsolutePath();
                    this.getParameters().set_var("body", body);
                    break;
                }
            }
            in.close();
        } catch (Exception e) {
            throw new Exception("Error occurde grepping file: " + e);
        }
        return found;
    }

}