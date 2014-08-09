/*
 * JobSchedulerShowParams.java
 * Created on 06.04.2005
 * 
 */
package sos.scheduler.job;
import org.apache.log4j.Logger;

import sos.spooler.Order;
import sos.spooler.Variable_set;

/**
 * logs job and order parameters (good for debugging)
 *
 * @author Andreas Liebert 
 */
public class JobSchedulerShowParams extends JobSchedulerJob {
	
	@SuppressWarnings("unused") private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String conSVNVersion = "$Id$";
	@SuppressWarnings("unused") private final Logger logger = Logger.getLogger(this.getClass());
	
	@Override public boolean spooler_process()  {
		boolean rc = false;
		// classic or order queue driven? return true for order queue driven invocation, return false for classic job start
		rc = !(spooler_task.job().order_queue() == null);
		Variable_set params = spooler_task.params();
		spooler_log.info("Params for task: " + spooler_task.id());
		if (params != null) {
			spooler_log.info("Job params: \n" + params.xml());
		}
		Order order = spooler_task.order();
		Variable_set payload = null;
		if (order != null) {
			Object oPayload = order.payload();
			if (oPayload != null)
				payload = (Variable_set) oPayload;
		}
		if (payload != null) {
			spooler_log.info("Order payload: \n" + payload.xml());
		}
		else
			spooler_log.info("No order payload.");
		return rc;
	}
}
