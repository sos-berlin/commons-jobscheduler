/*
 * JobSchedulerDatabaseMonitor.java
 * Created on 30.05.2005
 * 
 */
package sos.scheduler.job;

import sos.connection.SOSConnection;
import sos.scheduler.job.JobSchedulerJob;
import sos.settings.SOSProfileSettings;
import sos.util.SOSSchedulerLogger;


public class JobSchedulerDatabaseMonitor extends JobSchedulerJob {

    public boolean spooler_init() {

        try {
            this.setLogger(new SOSSchedulerLogger(spooler_log));
            this.setJobSettings(new SOSProfileSettings(spooler.ini_path()));
            this.setJobProperties(this.getJobSettings().getSection("job " + spooler_job.name()));

            return true;
        } catch (Exception e) {
            try {
                this.getLogger().error("error occurred in spooler_init(): " + e.getMessage());
            } catch (Exception ex) {} // gracefully ignore this error
            return false;
        }
    }
	
	public boolean spooler_process() throws Exception {
        
		boolean new_connection=false;
		SOSConnection connection = this.getConnection();
		
		this.setLogger(new SOSSchedulerLogger(spooler_log));
        
		try{
			if (getJobProperties().getProperty("config") != null) {
	    	    connection = sos.connection.SOSConnection.createInstance(getJobProperties().getProperty("config"));
	            connection.connect();
	    	    new_connection = true;
	        } else {
                throw new Exception("no database connection has been configured by parameter [config]");
            }

		} catch (Exception e) {
			this.getLogger().error("error occurred checking database connection: "+e.getMessage());
			return false;
		} finally {
	        if (new_connection && (connection != null)) {
	            connection.disconnect();
	            connection = null;
	        }
	    }

	    return false;
	}
}
