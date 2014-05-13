/*
 * JobSchedulerCheckBlacklist.java
 * Created on 22.11.2005
 * 
 */
package sos.scheduler.job;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.spooler.Job;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Variable_set;

/**
 * Dieser Job prüft. ob es Jobketten gibt, die Aufträge in der Blacklist haben. 
 * @author Uwe Risse 
 */

public class JobSchedulerCheckBlacklist extends JobSchedulerJob {

	private class BlackList {
		protected String	id;
		protected String	job_chain;
		protected String	created;
	}

	private DocumentBuilder	docBuilder;
	private String			level		= "info";
	private String			job			= "";
	private String			job_chain	= "";
	private String			granuality	= "blacklist";	//Posible values: blacklist|job_chain|order

	@Override
	public boolean spooler_init() {
		boolean rc = super.spooler_init();

		Variable_set params = spooler_task.params();
		if (spooler_task.order() != null)
			params.merge(spooler_task.order().params());

		if (params.var("level") != null) {
			level = params.var("level").toString().trim();
			spooler_log.info("... job setting [level]: " + level);
		}
		if (params.var("job") != null) {
			job = params.var("job").toString().trim();
			spooler_log.info("... job setting [job]: " + job);
		}
		if (params.var("job_chain") != null) {
			job_chain = params.var("job_chain").toString().trim();
			spooler_log.info("... job setting [job_chain]: " + job_chain);
		}

		if (params.var("granuality") != null) {
			granuality = params.var("granuality").toString().trim();
			spooler_log.info("... job setting [granuality]: " + granuality);
		}

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docFactory.newDocumentBuilder();
			return rc;
		}
		catch (Exception e) {
			try {
				getLogger().error("Error occured during initialisation: " + e);
			}
			catch (Exception ex) {
			}
			return false;
		}
	}

	@Override
	public boolean spooler_process() throws Exception {
		checkBlacklist();
		return false;
	}

	private void checkBlacklist() throws Exception {
		try {
			if (spooler != null) {
				getLogger().info("Sending show_state command.....");
				String answer = spooler.execute_xml("<show_state what=\"job_chain_orders,blacklist\"/>");
				getLogger().debug9("answer from Scheduler: " + answer);
				Document spoolerDocument = docBuilder.parse(new ByteArrayInputStream(answer.getBytes()));
				Element spoolerElement = spoolerDocument.getDocumentElement();
				Node answerNode = spoolerElement.getFirstChild();
				while (answerNode != null && answerNode.getNodeType() != Node.ELEMENT_NODE)
					answerNode = answerNode.getNextSibling();
				if (answerNode == null) {
					throw new Exception("answer contains no xml elements");
				}
				Element answerElement = (Element) answerNode;
				if (!answerElement.getNodeName().equals("answer"))
					throw new Exception("element <answer> is missing");
				NodeList schedulerNodes = answerElement.getElementsByTagName("blacklist");
				getLogger().debug3(schedulerNodes.getLength() + " blacklists found.");

				if (granuality.equalsIgnoreCase("blacklist")) {
					execute("There are orders in " + schedulerNodes.getLength() + " blacklists", null);
				}
				else {
					for (int i = 0; i < schedulerNodes.getLength(); i++) {
						Node blacklistNode = schedulerNodes.item(i);
						if (blacklistNode != null && blacklistNode.getNodeType() == Node.ELEMENT_NODE) {
							Element blacklist = (Element) blacklistNode;
							handleBlacklistEntry(blacklist);
						}
					}
				}
			}
		}
		catch (Exception e) {
			throw new Exception("Error occured checking blacklists: " + e, e);
		}
	}

	private void handleBlacklistEntry(final Element blacklist) throws Exception {

		NodeList blacklistOrders = blacklist.getElementsByTagName("order");
		getLogger().debug3(blacklistOrders.getLength() + " orders in blacklists found.");
		for (int i = 0; i < blacklistOrders.getLength(); i++) {
			Node orderNode = blacklistOrders.item(i);
			if (orderNode != null && orderNode.getNodeType() == Node.ELEMENT_NODE) {

				Element order = (Element) orderNode;
				BlackList b = new BlackList();
				b.job_chain = order.getAttribute("job_chain");
				b.id = "";
				b.created = "";
				if (granuality.equalsIgnoreCase("order")) {
					b.id = order.getAttribute("id");
					b.created = order.getAttribute("created");
					execute("Blacklist found for job_chain:" + b.job_chain + " file=" + b.id + "; created:" + b.created, b);
				}
				else {
					execute(blacklistOrders.getLength() + " order found in Blacklist for job_chain:" + job_chain, b);
					break;
				}
			}
		}
	}

	private void execute(final String s, final BlackList b) throws Exception {
		if (level.equalsIgnoreCase("info"))
			getLogger().info(s);
		if (level.equalsIgnoreCase("warning"))
			getLogger().warn(s);
		if (level.equalsIgnoreCase("error"))
			getLogger().error(s);

		if (!job.equals("")) {
			Job j = spooler.job(job);
			if (j != null) {
				if (b != null) {
					Variable_set p = spooler.create_variable_set();
					p.merge(spooler_task.params());
					p.set_var("filename", b.id);
					p.set_var("blacklist_job_chain", b.job_chain);
					p.set_var("created", b.created);
					j.start(p);
				}
				else {
					j.start();
				}
			}
			else {
				getLogger().warn("Job: " + job + " unknown");
			}
		}

		if (!job_chain.equalsIgnoreCase("")) {
			Job_chain jc = spooler.job_chain(job_chain);
			if (jc != null) {
				Order o = spooler.create_order();
				if (b != null) {
					o.params().merge(spooler_task.params());
					o.params().set_var("filename", b.id);
					o.params().set_var("blacklist_job_chain", b.job_chain);
					o.params().set_var("created", b.created);
				}
				jc.add_order(o);
			}
			else {
				getLogger().warn("Job_chain: " + job_chain + " unknown");
			}

		}
	}

}
