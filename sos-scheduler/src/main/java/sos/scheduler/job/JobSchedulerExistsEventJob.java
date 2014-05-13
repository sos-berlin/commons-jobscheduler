/*
 * JobSchedulerEventExistsJob.java
 * Created on 25.06.2008
 *
 */
package sos.scheduler.job;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/**
 *
 * @author andreas.liebert@sos-berlin.com
 *
 *    This job is used to check if certain events exist
 *
 */
public class JobSchedulerExistsEventJob extends JobSchedulerJob {

	@Override
	public boolean spooler_process() throws JobSchedulerException {
		boolean rc = true;
		try {

			// merge params
			Variable_set params = spooler.create_variable_set();
			if (spooler_task.params() != null)
				params.merge(spooler_task.params());
			if (spooler_job.order_queue() != null && spooler_task.order().params() != null)
				params.merge(spooler_task.order().params());

			String eventSpec = "";
			if (params.var("scheduler_event_spec") != null && params.var("scheduler_event_spec").length() > 0) {
				eventSpec = params.var("scheduler_event_spec");
			}
			else {
				throw new JobSchedulerException("parameter scheduler_event_spec is missing");
			}
			getLogger().debug3(".. job parameter [scheduler_event_spec]: " + eventSpec);

			getLogger().debug("Checking events for: " + eventSpec);

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document eventDocument = docBuilder.newDocument();
			eventDocument.appendChild(eventDocument.createElement("events"));

			JobSchedulerEventJob.readEventsFromDB(getConnection(), spooler, eventDocument, getLogger());

			NodeList nodes = XPathAPI.selectNodeList(eventDocument, eventSpec);
			if (nodes == null || nodes.getLength() == 0) {
				getLogger().info("No matching events were found.");
				rc = false;
			}
			else {
				getLogger().info("Matching events were found.");
				rc = true;
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException("Error checking events: " + e, e);
		}
		return rc;
	}
}
