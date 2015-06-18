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
			//JITL-145
//			spooler_log.info("Job params: \n" + params.xml());
			spooler_log.info("Job params: \n" + getCleanedParams(params).xml());
		}
		Order order = spooler_task.order();
		Variable_set payload = null;
		if (order != null) {
			Object oPayload = order.payload();
			if (oPayload != null)
				payload = (Variable_set) oPayload;
		}
		if (payload != null) {
			//JITL-145
//			spooler_log.info("Order payload: \n" + payload.xml());
			spooler_log.info("Order payload: \n" + getCleanedParams(payload).xml());
		}
		else
			spooler_log.info("No order payload.");
		return rc;
	}
	
	/**
	 * This method returns the params Variable_set with cleaned up password parameters to prevent security Issues
	 * 
	 * @param originalParams the Variable_set which can contain clear type passwords
	 * @return the new cleansed Variable_set
	 * @author SP
	 */
	private Variable_set getCleanedParams(Variable_set originalParams){
		Variable_set cleanedParams = spooler.create_variable_set();
		String names = originalParams.names();
		java.util.StringTokenizer tokenizer = new java.util.StringTokenizer( originalParams.names(), ";" ); 
		while (tokenizer.hasMoreTokens()){
		    String name = tokenizer.nextToken();
		    if(name.contains("password")){
		    	cleanedParams.set_value(name, "*****");
		    }else{
		    	cleanedParams.set_value(name, originalParams.value(name));
		    }
		}
		return cleanedParams;
	}
}
