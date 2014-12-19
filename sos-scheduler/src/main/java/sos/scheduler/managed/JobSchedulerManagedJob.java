package sos.scheduler.managed;

import java.util.HashMap;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Order;
import sos.spooler.Variable_set;

/**
 * Base class for managed jobs. This class can be used to write own mangaged jobs.
 * By calling prepareParams() in spooler_process() task and order parameters
 * will be merged.
 *  
 * @author andreas.pueschel@sos-berlin.com
 * @since 1.0 2005-03-05 
 */
public abstract class JobSchedulerManagedJob extends JobSchedulerJobAdapter {

	/** Attribut: globalSettings: globale Einstellungen des Jobs */
	protected HashMap<String, String>	globalSettings	= null;

	// Payload des Orders oder Parameter des Jobs wenn kein Order
	protected sos.spooler.Variable_set	orderPayload;

	// läuft der Job mit einem order?
	protected boolean					orderJob		= true;

	protected void prepareParams() throws Exception {
		orderJob = !(spooler_task.job().order_queue() == null);
		Order order = null;
		if (orderJob == false) {
			this.getLogger().debug3(spooler_job.name() + " running without order.");
		}
		try {
			Variable_set params = spooler.create_variable_set();

			params.merge(spooler_task.params());
			if (orderJob) {
				order = spooler_task.order();
				params.merge(order.params());
			}
				
	        Variable_set orderPay = clearBlanks(params);
			orderPayload.merge(orderPay);
			getLogger().debug6("Merged Payload: " + orderPayload.xml());
			
		}
		catch (Exception e) {

		}
	}

	private Variable_set clearBlanks(final Variable_set set) throws Exception {
		Variable_set retSet = spooler.create_variable_set();

		String[] keys = set.names().split(";");
		for (String key : keys) {
			String parameterValue = set.var(key);
			getLogger().debug9(key + "=" + parameterValue);
			if (parameterValue != null && parameterValue.length() > 0) {
				retSet.set_var(key, parameterValue);
			}
		}

		return retSet;
	}

	protected void debugParamter(final Variable_set params, final String name) {
		try {
			getLogger().debug6("Parameter: " + name + " value:\"" + params.var(name) + "\"");
		}
		catch (Exception e) {
		}
	}

	/**
	 * @return Returns the orderJob.
	 */
	public boolean isOrderJob() {
		return orderJob;
	}

	/**
	 * @param orderJob The orderJob to set.
	 */
	public void setOrderJob(final boolean pOrderJob) {
		orderJob = pOrderJob;
	}

	/**
	 * @return Returns the orderPayload.
	 */
	public sos.spooler.Variable_set getOrderPayload() {
		return orderPayload;
	}
}
