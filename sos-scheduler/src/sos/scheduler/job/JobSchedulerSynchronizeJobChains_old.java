/*
 * Synchronization of job chains: split and merge
 *
 * @author andreas.pueschel@sos-berlin.com
 * @version   2009-09-12
 *
 * This job can be used - with the same job name - in multiple job chains and would require each chain
 * to have the required number of active orders (default to 1) enqueued. Thus all orders from all chains
 * are set back if for one of these chains the number of required orders has not yet been reached.
 * As soon as all required orders are present then the current order is passed through this chain and
 * all other chains are signalled to make pass through the respective number of required orders.
 */
package sos.scheduler.job;
import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.NodeList;

import sos.spooler.Job_chain;
import sos.spooler.Job_chain_node;
import sos.spooler.Job_impl;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.xml.SOSXMLXPath;

public class JobSchedulerSynchronizeJobChains_old extends Job_impl {
	// delay order after setback: default interval if not specified by job configuration
	private int		setbackInterval	= 600;
	// delay order after setback: max. number of setbacks or unbounded if not specified by job configuration
	private int		setbackCount	= 0;
	// use "setback" or "suspend" for waiting orders
	private String	setbackType		= "suspend";
	// when reached, release orders
	private int		minimumSyncHits	= 0;
	private boolean	syncReady		= false;		//siehe js-461

	@Override public boolean spooler_init() {
		try {
			// modify default setback type
			try {
				if (spooler_task.params().value("setback_type") != null && spooler_task.params().value("setback_type").length() > 0) {
					setbackType = spooler_task.params().value("setback_type");
					if (!setbackType.equalsIgnoreCase("setback") && !setbackType.equalsIgnoreCase("suspend")) {
						throw new Exception("allowed values are \"setback\" and \"suspend\"");
					}
				}
			}
			catch (Exception ex) {
				throw new Exception("illegal value for parameter [setback_type] specified [" + spooler_task.params().value("setback_type") + ": "
						+ ex.getMessage());
			}
			try {
				if (spooler_task.params().value("minimumSyncHits") != null && spooler_task.params().value("minimum_sync_hits").length() > 0) {
					minimumSyncHits = Integer.parseInt(spooler_task.params().value("minimum_sync_hits"));
				}
			}
			catch (NumberFormatException nx) {
				throw new Exception("illegal, non-numeric parameter [minimumSyncHits] specified [" + spooler_task.params().value("minimumSyncHits") + ": "
						+ nx.getMessage());
			}
			// modify default setback interval from job parameter
			try {
				if (spooler_task.params().value("setback_interval") != null && spooler_task.params().value("setback_interval").length() > 0) {
					setbackInterval = Integer.parseInt(spooler_task.params().value("setback_interval"));
				}
			}
			catch (Exception ex) {
				throw new Exception("illegal, non-numeric parameter [setback_interval] specified [" + spooler_task.params().value("setback_interval") + ": "
						+ ex.getMessage());
			}
			// modify default number of setbacks from job parameter
			try {
				if (spooler_task.params().value("setback_count") != null && spooler_task.params().value("setback_count").length() > 0) {
					setbackCount = Integer.parseInt(spooler_task.params().value("setback_count"));
				}
			}
			catch (Exception ex) {
				throw new Exception("illegal, non-numeric parameter [setback_count] specified [" + spooler_task.params().value("setback_count") + ": "
						+ ex.getMessage());
			}
			// by precedence we use the configuration specified by <delay_order_after_setback/>
			if (spooler_job.setback_max() <= 0) {
				spooler_job.set_delay_order_after_setback(1, setbackInterval);
				if (setbackCount > 0) {
					spooler_job.set_max_order_setbacks(setbackCount);
				}
			}
			return true;
		}
		catch (Exception e) {
			spooler_log.error("error occurred: " + e.getMessage());
			return false;
		}
	}

	private int getRequiredOrder(final String dependentJobChainPath, final String dependentJobChainName, final String dependentNodeName) {
		int requiredOrders = 1;
		if (spooler_task.params().value(dependentJobChainName + ";" + dependentNodeName + "_required_orders") != null
				&& spooler_task.params().value(dependentJobChainName + ";" + dependentNodeName + "_required_orders").length() > 0) {
			requiredOrders = Integer.parseInt(spooler_task.params().value(dependentJobChainName + ";" + dependentNodeName + "_required_orders"));
		}
		else
			if (spooler_task.params().value(dependentJobChainName + "_required_orders") != null
					&& spooler_task.params().value(dependentJobChainName + "_required_orders").length() > 0) {
				String s = spooler_task.params().value(dependentJobChainName + "_required_orders");
				if (s.equalsIgnoreCase("job_chain.order_count")) {
					Job_chain j = spooler.job_chain(dependentJobChainPath);
					spooler_log.debug9("Waiting for all orders in job_chain " + dependentJobChainPath + " ...");
					if (j != null) {
						requiredOrders = j.order_count();
					}
					else {
						spooler_log.warn(dependentJobChainPath + "does not exist");
						requiredOrders = 1;
					}
				}
				else {
					requiredOrders = Integer.parseInt(spooler_task.params().value(dependentJobChainName + "_required_orders"));
				}
			}
			else
				if (spooler_task.params().value("required_orders") != null && spooler_task.params().value("required_orders").length() > 0) {
					requiredOrders = Integer.parseInt(spooler_task.params().value("required_orders"));
				}
		return requiredOrders;
	}

	@Override public boolean spooler_process() {
		boolean passOrders = true;
		NodeList nodes = null;
		SOSXMLXPath currentStatusXPath = null;
		String currentStatusAnswer = "";
		HashMap dependentJobChainNodes = null;
		String currentJobChainName = "";
		String currentJobChainPath = "";
		String currentJobPath = "";
		String currentNodeName = "";
		String dependentJobChainName = "";
		String dependentJobChainPath = "";
		String dependentNodeName = "";
		String syncSessionId = "";
		String syncSessionDebug = "";
		String syncSessionCondition = "";
		int dependentJobChainOrders = 0;
		try {
			currentJobChainName = spooler_task.order().job_chain().name();
			currentJobPath = spooler_job.name();
			currentJobPath = currentJobPath.startsWith("/") ? currentJobPath : "/" + currentJobPath;
			currentNodeName = spooler_task.order().state();
			Variable_set orderParams = spooler_task.order().params();
			if (orderParams.var("sync_session_id") != null)
				syncSessionId = orderParams.var("sync_session_id");
			if (spooler_task.job().order_queue() == null) {
				throw new Exception("this job cannot be used standalone but requires being operated by a job chain");
			}
			//siehe js-461
			if (orderParams.var("scheduler_sync_ready") != null)
				syncReady = orderParams.var("scheduler_sync_ready").equals("true");
			if (syncReady) {
				spooler_log.info("js-461: Sync skipped");
				Order o = spooler_task.order();
				Variable_set resultParameters = spooler.create_variable_set();
				String[] parameterNames = o.params().names().split(";");
				for (int i = 0; i < parameterNames.length; i++) {
					if (!parameterNames[i].equals("scheduler_sync_ready")) {
						resultParameters.set_var(parameterNames[i], o.params().value(parameterNames[i]));
					}
				}
				o.set_params(resultParameters);
				return true;
			}
			try { // to get the status information on current job chains
				String what = "job_chains,job_chain_orders";
				what += ",payload";
				currentStatusAnswer = spooler.execute_xml("<show_state what='" + what + "'/>");
				currentStatusXPath = new SOSXMLXPath(new StringBuffer(currentStatusAnswer));
				//spooler_log.debug6("Job Scheduler status response: " + currentStatusAnswer);
				String errorCode = currentStatusXPath.selectSingleNodeValue("/spooler/answer/ERROR/@code");
				String errorText = currentStatusXPath.selectSingleNodeValue("/spooler/answer/ERROR/@text");
				if (errorCode != null && errorCode.length() > 0 || errorText != null && errorText.length() > 0) {
					throw new Exception("Job Scheduler reports error in status response: " + errorCode + " " + errorText);
				}
			}
			catch (Exception ex) {
				throw new Exception("could not process Job Scheduler status response: " + ex.getMessage());
			}
			try { // to verify the information on the current order and current job chain
				spooler_log.debug9("---> /spooler/answer/state/job_chains/job_chain[@name = '" + currentJobChainName + "' and job_chain_node/@job = '"
						+ currentJobPath + "']");
				NodeList verifyNodes = currentStatusXPath.selectNodeList("/spooler/answer/state/job_chains/job_chain[@name = '" + currentJobChainName
						+ "' and job_chain_node/@job = '" + currentJobPath + "']");
				if (verifyNodes == null || verifyNodes.getLength() < 1) {
					spooler_log.info("could not identify the current job and job chain in Job Scheduler status response");
				}
				if (verifyNodes.getLength() != 1) {
					spooler_log.warn("possible problem detected: current job chain name [" + currentJobChainName + "] and job path [" + currentJobPath
							+ "] are not unique in Job Scheduler status response");
				}
				currentJobChainPath = verifyNodes.item(0).getAttributes().getNamedItem("path").getNodeValue();
				currentJobChainPath = currentJobChainPath.startsWith("/") ? currentJobChainPath : "/" + currentJobChainPath;
				spooler_log.info("synchronizing order [" + spooler_task.order().id() + ", " + spooler_task.order().title() + "] of job chain ["
						+ currentJobChainPath + "]");
			}
			catch (Exception ex) {
				throw new Exception(ex.getMessage());
			}
			try { // to pass the current order if previously signalled by a global Job Scheduler variable
				String signalVariable = currentJobChainPath + ";" + currentNodeName + "_pass_through_next_order";
				if (spooler.variables().value(signalVariable) != null && spooler.variables().value(signalVariable).length() > 0) {
					int numOfSignalledOrders = Integer.parseInt(spooler.variables().value(signalVariable));
					if (numOfSignalledOrders > 0) {
						numOfSignalledOrders--;
						spooler.variables().set_var(signalVariable, Integer.toString(numOfSignalledOrders));
						spooler_log.info("current order is being passed through due to global signal: " + signalVariable);
						spooler_log.info(numOfSignalledOrders + " left for pass through operation");
						return true;
					}
				}
			}
			catch (Exception ex) {
				throw new Exception("could not pass through current order: " + ex.getMessage());
			}
			try { // to retrieve the list of dependent job chains nodes
				nodes = currentStatusXPath.selectNodeList("/spooler/answer/state/job_chains/job_chain/job_chain_node[@job = '" + currentJobPath + "']");
				//nodes = currentStatusXPath.selectNodeList("/spooler/answer/state/job_chains/job_chain[( job_chain_node/@state = job_chain_node[@job = '" + currentJobPath + "']/@next_state or job_chain_node/@state = job_chain_node[@job = '" + currentJobPath + "']/@state) and ( (job_chain_node/order_queue/order/@setback_count > 0 and job_chain_node/order_queue/order/@setback != '') or job_chain_node/order_queue/order/@suspended = 'yes')]");
				// if there is only one job chain with one synchronization job node then multiple orders for the same chain get synchronized
				if (nodes != null && nodes.getLength() == 1) {
					// handle one chain in the same way as multiple job chains
					// otherwise, if there are multiple job chains
				}
				else
					if (nodes == null || nodes.getLength() < 1) {
						spooler_log.info("no additional pending orders found");
						// suspend/setback the current order for this job chain: if this is the last node of a job chain then the order is being set back, otherwise it will be suspended
						if (setbackType.equalsIgnoreCase("setback") || spooler_task.order().job_chain_node().next_node().next_node() == null) {
							spooler_task.order().setback();
						}
						else {
							if (!spooler_task.order().suspended()) {
								spooler_task.order().set_suspended(true);
							}
						}
						spooler_log.info("order is set on hold: " + spooler_task.order().id() + ", " + spooler_task.order().title());
						return true;
					}
			}
			catch (Exception ex) {
				throw new Exception("could not process Job Scheduler status response: " + ex.getMessage());
			}
			try { // to check in dependent job chain nodes if the required number of orders has been enqueued
				dependentJobChainNodes = new HashMap();
				int satisfiedNode = 0;
				for (int i = 0; i < nodes.getLength(); i++) {
					dependentJobChainName = nodes.item(i).getParentNode().getAttributes().getNamedItem("name").getNodeValue();
					dependentJobChainPath = nodes.item(i).getParentNode().getAttributes().getNamedItem("path").getNodeValue();
					dependentNodeName = nodes.item(i).getAttributes().getNamedItem("state").getNodeValue();
					// spooler_log.debug9("node check: self::node()[( job_chain_node/@state = job_chain_node[@job = '" + currentJobPath + "']/@next_state or job_chain_node/@state = job_chain_node[@job = '" + currentJobPath + "']/@state) and ( (job_chain_node/order_queue/order/@setback_count > 0 and job_chain_node/order_queue/order/@setback != '') or job_chain_node/order_queue/order/@suspended = 'yes')]/job_chain_node/order_queue[@length > 0]/order");
					// NodeList orderNodes = currentStatusXPath.selectNodeList(nodes.item(i), "self::node()[( job_chain_node/@state = job_chain_node[@job = '" + currentJobPath + "']/@next_state or job_chain_node/@state = job_chain_node[@job = '" + currentJobPath + "']/@state) and ( (job_chain_node/order_queue/order/@setback_count > 0 and job_chain_node/order_queue/order/@setback != '') or job_chain_node/order_queue/order/@suspended = 'yes')]/job_chain_node/order_queue[@length > 0]/order");
					if (syncSessionId.length() > 0) {
						syncSessionCondition = " and payload/params/param[@name='sync_session_id' and @value='" + syncSessionId + "']";
						syncSessionDebug = ", sync_session [" + syncSessionId + "]";
					}
					else {
						syncSessionCondition = " and not(payload/params/param[@name='sync_session_id'])";
					}
					String nodeCheck = "(order_queue/order[((@setback_count > 0 and @setback != '') or @suspended = 'yes')" + syncSessionCondition
							+ "]) | (../job_chain_node[@state='" + dependentNodeName + "']/order_queue/order[@suspended = 'yes'" + syncSessionCondition + "])";
					spooler_log.debug9(nodeCheck);
					NodeList orderNodes = currentStatusXPath.selectNodeList(nodes.item(i), nodeCheck);
					//dependentJobChainOrders = Integer.parseInt(nodes.item(i).getAttributes().getNamedItem("orders").getNodeValue());
					dependentJobChainOrders = orderNodes.getLength();
					spooler_log.debug9("node list length for currently suspended/set back orders in job chain [" + dependentJobChainPath + "], state ["
							+ dependentNodeName + "]" + syncSessionDebug + ": " + dependentJobChainOrders);
					// add the currently processed order to the number of pending orders
					spooler_log.debug9("dependentJobChainPath=" + dependentJobChainPath + " currentJobChainPath=" + currentJobChainPath + " dependentNodeName="
							+ dependentNodeName + " currentNodeName=" + currentNodeName);
					if (dependentJobChainPath.equalsIgnoreCase(currentJobChainPath) && dependentNodeName.equalsIgnoreCase(currentNodeName)) {
						dependentJobChainOrders++;
					}
					spooler_log.debug3(dependentJobChainOrders + " pending orders found for job chain [" + dependentJobChainPath + "], state ["
							+ dependentNodeName + "]" + syncSessionDebug + "");
					int requiredOrders = getRequiredOrder(dependentJobChainPath, dependentJobChainName, dependentNodeName);
					// should any of the dependent job chains have less orders enqueued than required, then the current order will be set on hold
					if (requiredOrders > dependentJobChainOrders) {
						passOrders = false;
					}
					else {
						if (minimumSyncHits > 0)
							satisfiedNode++;
					}
					spooler_log.info("job chain [" + dependentJobChainPath + "], state [" + dependentNodeName + "]" + syncSessionDebug + " requires "
							+ requiredOrders + " orders being present, " + dependentJobChainOrders + " orders have been enqueued");
					// move this information to a hashmap for further use
					int[] orders = new int[2];
					orders[0] = dependentJobChainOrders;
					orders[1] = requiredOrders;
					dependentJobChainNodes.put(dependentJobChainPath + ";" + dependentNodeName, orders);
				}
				spooler_log.info("Synchits:" + satisfiedNode);
				passOrders = passOrders || satisfiedNode > 0 && satisfiedNode >= minimumSyncHits;
				if (!passOrders) {
					// suspend/setback the current order for this job chain: it will be suspended
					if (setbackType.equalsIgnoreCase("setback")) {
						spooler_task.order().setback();
					}
					else {
						if (!spooler_task.order().suspended()) {
							spooler_task.order().set_state(spooler_task.order().state()); //Damit der Suspend auf den sync-Knoten geht und nicht auf den nächsten.
							spooler_task.order().set_suspended(true);
						}
					}
					spooler_log.info("order is set on hold: " + spooler_task.order().id() + ", " + spooler_task.order().title());
				}
				// display an overview of all dependent job chains
				Iterator jobChainNodesIterator = dependentJobChainNodes.keySet().iterator();
				while (jobChainNodesIterator.hasNext()) {
					String keyName = (String) jobChainNodesIterator.next();
					dependentJobChainPath = keyName.split(";")[0];
					dependentNodeName = keyName.split(";")[1];
					if (dependentJobChainPath == null) {
						continue;
					}
					if (dependentNodeName == null) {
						continue;
					}
					int[] orders = (int[]) dependentJobChainNodes.get(keyName);
					if (orders == null) {
						continue;
					}
					spooler_log.info("synchronized job chain: " + dependentJobChainPath + ", state [" + dependentNodeName + "]" + syncSessionDebug
							+ ", required orders: " + orders[1] + ", pending orders: " + orders[0]);
				}
			}
			catch (Exception ex) {
				throw new Exception("could not check pending orders in dependent job chains: " + ex.getMessage());
			}
			// signal to other job chains that the next order should be passed through to its next state
			if (passOrders) {
				Iterator jobChainNodesIterator = dependentJobChainNodes.keySet().iterator();
				while (jobChainNodesIterator.hasNext()) {
					String keyName = (String) jobChainNodesIterator.next();
					dependentJobChainPath = keyName.split(";")[0];
					dependentNodeName = keyName.split(";")[1];
					if (dependentJobChainPath == null) {
						continue;
					}
					if (dependentNodeName == null) {
						continue;
					}
					int[] orders = (int[]) dependentJobChainNodes.get(keyName);
					if (orders == null) {
						continue;
					}
					spooler_log.debug6("checking job chain: " + dependentJobChainPath + ", state [" + dependentNodeName + "]");
					if (setbackType.equalsIgnoreCase("setback")) {
						String signalVariable = keyName + "_pass_through_next_order";
						// signal current job chain to pass through the required number of orders minus the current one
						if (dependentJobChainPath.equals(currentJobChainPath) && dependentNodeName.equals(currentNodeName)) {
							if (orders[1] - 1 > 0) {
								spooler_log.debug6("setting signal variable: " + signalVariable + "=" + Integer.toString(orders[1] - 1));
								spooler.set_var(signalVariable, Integer.toString(orders[1] - 1));
							}
						}
						else {
							// make other job chain pass through the required number of orders
							spooler_log.debug6("setting signal variable: " + signalVariable + "=" + Integer.toString(orders[1]));
							spooler.set_var(signalVariable, Integer.toString(orders[1]));
						}
						spooler_log.info("signalling next " + orders[1] + " orders to be passed through by job chain: " + dependentJobChainPath + ", state ["
								+ dependentNodeName + "]");
					}
					try { // to signal waiting orders to start immediately
						NodeList signalNodes = null;
						if (setbackType.equalsIgnoreCase("setback")) {
							signalNodes = currentStatusXPath.selectNodeList("/spooler/answer/state/job_chains/job_chain[@path = '" + dependentJobChainPath
									+ "']/job_chain_node[@state = '" + dependentNodeName + "']/order_queue/order[@setback_count > 0 and @setback != '' "
									+ syncSessionCondition + "]");
							spooler_log.debug9("orders being set back: " + signalNodes.getLength() + " /spooler/answer/state/job_chains/job_chain[@path = '"
									+ dependentJobChainPath + "']/job_chain_node[@state = '" + dependentNodeName
									+ "']/order_queue/order[@setback_count > 0 and @setback != ''" + syncSessionCondition + "]");
						}
						else {
							signalNodes = currentStatusXPath.selectNodeList("/spooler/answer/state/job_chains/job_chain[@path = '" + dependentJobChainPath
									+ "']/job_chain_node[@state = '" + dependentNodeName + "']/order_queue/order[@suspended = 'yes' " + syncSessionCondition
									+ "]");
							spooler_log.debug9("orders being suspended: " + signalNodes.getLength() + " /spooler/answer/state/job_chains/job_chain[@path = '"
									+ dependentJobChainPath + "']/job_chain_node[@state = '" + dependentNodeName + "']/order_queue/order[@suspended = 'yes' "
									+ syncSessionCondition + "]");
						}
						if (signalNodes == null || signalNodes.getLength() < 1) {
							spooler_log.info("no suspended or set back orders found in dependent job chain: " + dependentJobChainPath);
							continue;
						}
						SOSXMLXPath xPath = null;
						String answer = "";
						int requiredOrders = 1;
						if (dependentJobChainPath.lastIndexOf("/") > -1) {
							dependentJobChainName = dependentJobChainPath.substring(dependentJobChainPath.lastIndexOf("/") + 1);
						}
						else {
							dependentJobChainName = dependentJobChainPath;
						}
						/*  if (spooler_task.params().value(dependentJobChainName + "_required_orders") != null && spooler_task.params().value(dependentJobChainName + "_required_orders").length() > 0) {
						       requiredOrders = Integer.parseInt(spooler_task.params().value(dependentJobChainName + "_required_orders"));
						   } else if (spooler_task.params().value("required_orders") != null && spooler_task.params().value("required_orders").length() > 0) {
						       requiredOrders = Integer.parseInt(spooler_task.params().value("required_orders"));
						   }*/
						requiredOrders = getRequiredOrder(dependentJobChainPath, dependentJobChainName, dependentNodeName);
						for (int i = 0; i < signalNodes.getLength(); i++) {
							if (i >= requiredOrders) {
								spooler_log.debug9("maximum number of orders for reactivation reached: found " + i + ", required " + requiredOrders);
								break;
							}
							spooler_log.info("signalling order [" + signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue()
									+ "] to be continued for job chain [" + dependentJobChainPath + "]");
							if (signalNodes.item(i).getAttributes().getNamedItem("setback") != null) {
								if (!setbackType.equalsIgnoreCase("setback")) {
									String signalVariable = keyName + "_pass_through_next_order";
									// signal current job chain to pass through the required number of orders minus the current one
									if (dependentJobChainPath.equals(currentJobChainPath) && dependentNodeName.equals(currentNodeName)) {
										if (orders[1] - 1 > 0) {
											spooler.set_var(signalVariable, Integer.toString(orders[1] - 1));
										}
									}
									else {
										// make other job chains pass through the required number of orders
										spooler.set_var(signalVariable, Integer.toString(orders[1]));
									}
									spooler_log.info("signalling next " + orders[1] + " orders to be passed through by job chain: " + dependentJobChainPath
											+ ", state [" + dependentNodeName + "]");
								}
								spooler_log.debug9("activate order after setback: <modify_order job_chain='" + dependentJobChainPath + "' order='"
										+ signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue() + "' setback='no'/>");
								answer = spooler.execute_xml("<modify_order job_chain='" + dependentJobChainPath + "' order='"
										+ signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue() + "' setback='no'/>");
							}
							else {
								Job_chain j = spooler.job_chain(dependentJobChainPath);
								spooler_log.debug9("---> dependentJobChainPath:" + dependentJobChainPath);
								Job_chain_node n = j.node(signalNodes.item(i).getAttributes().getNamedItem("state").getNodeValue());
								Job_chain_node next_n = n.next_node();
								if (next_n.job() == null) {
									spooler_log.info("end state reached");
								}
								String next_state = n.next_state();
								spooler_log.debug9("---> state:" + signalNodes.item(i).getAttributes().getNamedItem("state").getNodeValue());
								spooler_log.debug9("---> state:" + next_state);
								spooler_log.debug9("activate suspended order: <modify_order job_chain='" + dependentJobChainPath + "' order='"
										+ signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue() + "' state='" + next_state
										+ "' suspended='no'/>");
								if (next_n.job() == null) { //siehe js-461
									answer = spooler.execute_xml("<modify_order job_chain='" + dependentJobChainPath + "' order='"
											+ signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue()
											+ "' suspended='no'><params><param name='scheduler_sync_ready' value='true'></param></params></modify_order>");
								}
								else {
									answer = spooler.execute_xml("<modify_order job_chain='" + dependentJobChainPath + "' order='"
											+ signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue() + "' state='" + next_state
											+ "' suspended='no'/>");
								}
							}
							xPath = new SOSXMLXPath(new StringBuffer(answer));
							String errorCode = xPath.selectSingleNodeValue("/spooler/answer/ERROR/@code");
							String errorText = xPath.selectSingleNodeValue("/spooler/answer/ERROR/@text");
							if (errorCode != null && errorCode.length() > 0 || errorText != null && errorText.length() > 0) {
								spooler_log.warn("Job Scheduler reports error when signalling order ["
										+ signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue() + "] to pass through job chain ["
										+ dependentJobChainPath + "]: " + errorCode + " " + errorText);
							}
						}
					}
					catch (Exception ex) {
						throw new Exception("could not signal orders to be passed through by job chains: " + ex.getMessage());
					}
				}
			}
			return true;
		}
		catch (Exception e) {
			spooler_log.error("error occurred: " + e.getMessage());
			return false;
		}
	}
}
