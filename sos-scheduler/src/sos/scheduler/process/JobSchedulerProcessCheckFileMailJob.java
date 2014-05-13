/*
 * JobSchedulerProcessCheckFileMailJob.java
 * Created on 05.06.2007
 * 
 */
package sos.scheduler.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sos.util.SOSFile;
import sos.util.SOSLogger;

public class JobSchedulerProcessCheckFileMailJob extends
		JobSchedulerProcessSendMailJob {
	
	// regex for file to search for
	private String fileSpec;
	
    //	path for files
	private String filePath;
	
	// file content regex to search for in file
	private String fileContent;

	/* 
	 * @see sos.scheduler.process.JobSchedulerProcessSendMailJob#doSendMail()
	 */
	protected boolean doSendMail() {
		boolean found = false;
		fileSpec = "";
		fileContent = "";
		filePath ="";
		
		try {
    		if (this.getParameters().value("file_spec") != null && this.getParameters().value("file_spec").length() > 0) {
    			fileSpec = this.getParameters().value("file_spec");
    		}    		
    		if (this.getParameters().value("file_path") != null && this.getParameters().value("file_path").length() > 0) {
    			filePath = this.getParameters().value("file_path");
    		}
    		if (this.getParameters().value("file_content") != null && this.getParameters().value("file_content").length() > 0) {
    			fileContent = this.getParameters().value("file_content");
    		}
		} catch (Exception e) {
			try{
				this.getLogger().warn("error occurred checking parameters: " + e.getMessage());
			} catch(Exception ex){}
    	}
		if (fileSpec.length()==0 && fileContent.length()==0 && filePath.length()==0){
			try{
				getLogger().info("JobSchedulerProcessCheckFileMailJob is not configured, suppressing mail.");
			} catch (Exception e){}
			return false;
		}
		try{
			Vector fileList = new Vector();
			if (fileSpec.length()==0){
				if (spooler_job.order_queue() != null){
					File triggeredFile = new File(spooler_task.order().id());					
					fileList.add(triggeredFile);
				}
			} else{
				fileList = SOSFile.getFilelist(filePath, fileSpec,0);
			}
			getLogger().debug3("Lenght of filelist: "+fileList.size());
			Iterator iter = fileList.iterator();
			Pattern pattern = null;
			if (fileContent.length()>0) pattern = Pattern.compile(fileContent);
			
			while (iter.hasNext()){
				File curFile = (File) iter.next();
				if(pattern==null && curFile.exists()) {
					getLogger().info("Found file "+curFile.getAbsolutePath()+", sending mail.");
					found = true;
					break;
				}
				if(pattern!=null && curFile.exists()) {
					if (grep(curFile,pattern)){
						found = true;
						break;
					}
				}
			}
		}catch (Exception e) {
			try{
				this.getLogger().warn("error occurred checking file(s): " + e.getMessage());
			} catch(Exception ex){}
    	}
		return found;
	}

	private boolean grep(File file, Pattern regex) throws Exception{
		boolean found = false;
		try{
			getLogger().debug7("Grepping file "+file.getAbsolutePath()+" for regex: "+regex.pattern());
			BufferedReader in = new BufferedReader ( new FileReader (file) );
			String currentLine ="";
			while( (currentLine = in.readLine()) != null ) {
				Matcher matcher = regex.matcher(currentLine);
				//getLogger().debug9(currentLine);
				if (matcher.find()){
					String match = matcher.group();
					getLogger().debug1("Found String '"+match+"' in file "+file.getAbsolutePath()+", sending mail.");
					found = true;
					String body = "";
					if (this.getParameters().value("body") != null && this.getParameters().value("body").length() > 0) {
            			body = this.getParameters().value("body")+"\n";
            		}
					body+="'"+match+"' was found in file "+file.getAbsolutePath();
					this.getParameters().set_var("body",body);
					break;
				}
			}
			in.close();
		} catch(Exception e){
			throw new Exception ("Error occurde grepping file: "+e);
		}
		return found;
	}
}
