package sos.scheduler.job;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.connection.SOSConnection;
import sos.settings.SOSProfileSettings;

public class JobSchedulerDatabaseMonitor extends JobSchedulerJob {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerDatabaseMonitor.class); 

    public boolean spooler_init() {
        try {
            this.setJobSettings(new SOSProfileSettings(spooler.ini_path()));
            this.setJobProperties(this.getJobSettings().getSection("job " + spooler_job.name()));
            return true;
        } catch (Exception e) {
            LOGGER.error("error occurred in spooler_init(): " + e.getMessage());
            return false;
        }
    }

    public boolean spooler_process() throws Exception {
        boolean new_connection = false;
        SOSConnection connection = this.getConnection();
        try {
            if (getJobProperties().getProperty("config") != null) {
                connection = sos.connection.SOSConnection.createInstance(getJobProperties().getProperty("config"));
                connection.connect();
                new_connection = true;
            } else {
                throw new Exception("no database connection has been configured by parameter [config]");
            }
        } catch (Exception e) {
            LOGGER.error("error occurred checking database connection: " + e.getMessage());
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