/*
 * JobSchedulerEventExistsJob.java Created on 25.06.2008
 */
package sos.scheduler.job;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import sos.connection.SOSConnection;
import sos.spooler.Spooler;
import sos.spooler.Variable_set;
import sos.util.SOSLogger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author andreas.liebert@sos-berlin.com
 *
 *         This job is used to check if certain events exist */
public class JobSchedulerExistsEventJob extends JobSchedulerJob {

    private final String tableEvents = "SCHEDULER_EVENTS";
    private String scheduler_event_service_id = "";

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
            } else {
                throw new JobSchedulerException("parameter scheduler_event_spec is missing");
            }

            if (params.var("scheduler_event_service_id") != null && params.var("scheduler_event_service_id").length() > 0) {
                scheduler_event_service_id = params.var("scheduler_event_service_id");
            } else {
                scheduler_event_service_id = spooler.id();
            }

            getLogger().debug3(".. job parameter [scheduler_event_service_id]: " + scheduler_event_service_id);
            getLogger().debug3(".. job parameter [scheduler_event_spec]: " + eventSpec);

            getLogger().debug("Checking events for: " + eventSpec);

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document eventDocument = docBuilder.newDocument();
            eventDocument.appendChild(eventDocument.createElement("events"));

            readEventsFromDB(getConnection(), spooler, eventDocument, getLogger());

            NodeList nodes = XPathAPI.selectNodeList(eventDocument, eventSpec);
            if (nodes == null || nodes.getLength() == 0) {
                getLogger().info("No matching events were found.");
                rc = false;
            } else {
                getLogger().info("Matching events were found.");
                rc = true;
            }
        } catch (Exception e) {
            throw new JobSchedulerException("Error checking events: " + e, e);
        }
        return rc;
    }

    private void readEventsFromDB(final SOSConnection conn, final Spooler spooler, final Document eventsDoc, final SOSLogger log) throws Exception {
        try {
            conn.executeUpdate("DELETE FROM " + tableEvents
                    + " WHERE \"EXPIRES\"<=%now AND (\"SPOOLER_ID\" IS NULL OR \"SPOOLER_ID\"='' OR \"SPOOLER_ID\"='" + scheduler_event_service_id
                    + "')");
            conn.commit();
            Vector<?> vEvents = conn.getArrayAsVector("SELECT \"SPOOLER_ID\", \"REMOTE_SCHEDULER_HOST\", \"REMOTE_SCHEDULER_PORT\", \"JOB_CHAIN\", \"ORDER_ID\", \"JOB_NAME\", \"EVENT_CLASS\", \"EVENT_ID\", \"EXIT_CODE\", \"CREATED\", \"EXPIRES\", \"PARAMETERS\" FROM "
                    + tableEvents
                    + " WHERE (\"SPOOLER_ID\" IS NULL OR \"SPOOLER_ID\"='' OR \"SPOOLER_ID\"='"
                    + scheduler_event_service_id
                    + "') ORDER BY \"ID\" ASC");
            Iterator<?> vIterator = vEvents.iterator();
            int vCount = 0;
            while (vIterator.hasNext()) {
                HashMap<?, ?> record = (HashMap<?, ?>) vIterator.next();
                Element event = eventsDoc.createElement("event");
                event.setAttribute("scheduler_id", record.get("spooler_id").toString());
                event.setAttribute("remote_scheduler_host", record.get("remote_scheduler_host").toString());
                event.setAttribute("remote_scheduler_port", record.get("remote_scheduler_port").toString());
                event.setAttribute("job_chain", record.get("job_chain").toString());
                event.setAttribute("order_id", record.get("order_id").toString());
                event.setAttribute("job_name", record.get("job_name").toString());
                event.setAttribute("event_class", record.get("event_class").toString());
                event.setAttribute("event_id", record.get("event_id").toString());
                event.setAttribute("exit_code", record.get("exit_code").toString());
                event.setAttribute("expires", record.get("expires").toString());
                event.setAttribute("created", record.get("created").toString());
                if (record.get("parameters").toString().length() > 0) {
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    Document eventParameters = docBuilder.parse(new InputSource(new StringReader(record.get("parameters").toString())));
                    log.debug9("Importing params node...");
                    Node impParameters = eventsDoc.importNode(eventParameters.getDocumentElement(), true);
                    log.debug9("appending params child...");
                    event.appendChild(impParameters);
                }
                eventsDoc.getLastChild().appendChild(event);
                vCount++;
            }
            log.info(vCount + " events restored from database");
        } catch (Exception e) {
            throw new JobSchedulerException("Failed to read events from database: " + e, e);
        }
    }

}
