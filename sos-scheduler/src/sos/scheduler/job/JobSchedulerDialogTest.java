package sos.scheduler.job;

import sos.settings.SOSProfileSettings;
import sos.settings.SOSSettings;
import sos.spooler.Job_impl;
import sos.spooler.Order;
import sos.spooler.Variable_set;

import sos.stresstest.dialogtest.SOSDialogTest;
import sos.util.SOSSchedulerLogger;


/**
 * @author andreas.pueschel@sos-berlin.com
 *
 * process sos dialog test
 */
public class JobSchedulerDialogTest extends Job_impl {

    private String configFile = "./config/sos_dialogtest.ini";
    
    
	public boolean spooler_process() {
	    
	    boolean rc = false;
    	
	    try {
	    
	        // process job parameters
			if (spooler_task.params().var("config_file") != null
			&& spooler_task.params().var("config_file").length() > 0) {
				this.configFile = spooler_task.params().var("config_file").toString();
				spooler_log.debug3(".. job parameter [config_file]: " + this.configFile);
			}

			// order driven or job driven?
			if (spooler_job.order_queue() != null) {
			    rc = true;
			    Order order = spooler_task.order();
			    Variable_set payload = (sos.spooler.Variable_set) order.payload();
			    if (payload.var("config_file") != null && payload.var("config_file").length() > 0) {
			        this.configFile = payload.var("config_file");
					spooler_log.debug3(".. order parameter [config_file]: " + this.configFile);
			    }
			}
			
			spooler_log.info(".. parameter [config_file]: " + this.configFile);
			SOSSchedulerLogger logger = new SOSSchedulerLogger(spooler_log);
			
			SOSDialogTest dialogTest = new SOSDialogTest(logger);
	        /* if (dialogTest.initialize(this.configFile)) {
	            throw new Exception("error occurred initializing SOSDialogTest");
	        }*/
			//dialogTest.initialize(this.configFile);
			SOSSettings settings = new SOSProfileSettings(configFile);
			dialogTest.initialize(settings);
	        dialogTest.execute();
	        
	    } catch (Exception e) {
	        spooler_log.warn("error occurred in JobSchedulerDialogTest: " + e.getMessage());
	        rc = false;
	    }
		
		return rc;
	}

}