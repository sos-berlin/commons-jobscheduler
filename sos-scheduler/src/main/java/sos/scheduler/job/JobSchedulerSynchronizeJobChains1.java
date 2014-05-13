/*
 * Synchronization of job chains: split and merge
 *
 * @author andreas.pueschel@sos-berlin.com
 * @version   2009-09-12
 * 
 * This class can be used - with the same job name - in multiple job chains and would require each chain
 * to have the required number of active orders (default to 1) enqueued. Thus all orders from all chains
 * are set back if for one of these chains the number of required orders has not yet been reached.
 * As soon as all required orders are present then the current order is passed through this chain and
 * all other chains are signalled to make pass through the respective number of required orders.
 */
package sos.scheduler.job;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;

import sos.spooler.Job_chain;
import sos.spooler.Job_chain_node;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.xml.SOSXMLXPath;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerSynchronizeJobChains1 extends JobSchedulerJobAdapter {
	private static final String	conParameterSETBACK_COUNT			= "setback_count";
	private static final String	conParameterSETBACK_INTERVAL		= "setback_interval";

	private static final String	conParameterSETBACK_TYPE			= "setback_type";
	private static final String	conSetBackTypeSETBACK				= "setback";
	private static final String	conSetBackTypeSUSPEND				= "suspend";

	private static final String	conParameterMINIMUM_SYNC_HITS		= "minimum_sync_hits";
	private static final String	conParameterREQUIRED_ORDERS			= "required_orders";
	private static final String	conParameterSCHEDULER_SYNC_READY	= "scheduler_sync_ready";
	private static final String	conParameterSYNC_SESSION_ID			= "sync_session_id";
	private final String		conClassName						= "JobSchedulerSynchronizeJobChains";												//$NON-NLS-1$
	private static Logger		logger								= Logger.getLogger(JobSchedulerSynchronizeJobChains1.class);
	private final String		conSVNVersion						= "$Id: JobSchedulerSynchronizeJobChains.java 16198 2012-01-11 20:47:52Z kb $";

	// delay order after setback: default interval if not specified by job configuration
	private int					setbackInterval						= 600;
	// delay order after setback: max. number of setbacks or unbounded if not specified by job configuration
	private int					setbackCount						= 0;
	// use "setback" or "suspend" for waiting orders
	private String				setbackType							= conSetBackTypeSUSPEND;
	// when reached, release orders
	private int					intMinimumSyncHits					= 0;
	private boolean				syncReady							= false;																			// siehe js-461

	private String				JSJ_SYNC_0010						= "this job cannot be used standalone but requires being operated by a job chain";
	private String				JSJ_SYNC_0020						= "allowed values are '%1$s' and '%2$s'";
	private String				JSJ_SYNC_0030						= "illegal value for parameter '%1$s' specified '%2$s': %3$s";

	public boolean spooler_init() {
		try {
			// modify default setback type
			String strSetbackT = spooler_task.params().value(conParameterSETBACK_TYPE);
			try {
				if (isNotEmpty(strSetbackT)) {
					this.setbackType = strSetbackT;
					if (!setbackType.equalsIgnoreCase(conSetBackTypeSETBACK) && !setbackType.equalsIgnoreCase(conSetBackTypeSUSPEND)) {
						throw new JobSchedulerException(Messages.getMsg(JSJ_SYNC_0020, conSetBackTypeSETBACK, conSetBackTypeSUSPEND));
					}
				}
			}
			catch (Exception ex) {
				ex.printStackTrace(System.err);
				throw new JobSchedulerException(Messages.getMsg(JSJ_SYNC_0030, conParameterSETBACK_TYPE, strSetbackT, ex.getMessage()));
			}

			String strMinimumSyncHits = spooler_task.params().value(conParameterMINIMUM_SYNC_HITS);
			try {
				if (isNotEmpty(strMinimumSyncHits)) {
					this.intMinimumSyncHits = Integer.parseInt(strMinimumSyncHits);
				}
			}
			catch (NumberFormatException nx) {
				nx.printStackTrace(System.err);
				throw new JobSchedulerException(Messages.getMsg(JSJ_SYNC_0030, conParameterMINIMUM_SYNC_HITS, strMinimumSyncHits, nx.getMessage()));
			}

			// modify default setback interval from job parameter
			String strSetBackInterval = spooler_task.params().value(conParameterSETBACK_INTERVAL);
			try {
				if (isNotEmpty(strSetBackInterval)) {
					this.setbackInterval = Integer.parseInt(strSetBackInterval);
				}
			}
			catch (Exception ex) {
				ex.printStackTrace(System.err);
				throw new JobSchedulerException(Messages.getMsg(JSJ_SYNC_0030, conParameterSETBACK_INTERVAL, strSetBackInterval, ex.getMessage()));
			}

			// modify default number of setbacks from job parameter
			String strSetbackCount = spooler_task.params().value(conParameterSETBACK_COUNT);
			try {
				if (isNotEmpty(strSetbackCount)) {
					this.setbackCount = Integer.parseInt(strSetbackCount);
				}
			}
			catch (Exception ex) {
				ex.printStackTrace(System.err);
				throw new JobSchedulerException(Messages.getMsg(JSJ_SYNC_0030, conParameterSETBACK_COUNT, strSetbackCount, ex.getMessage()));
			}

			// by precedence we use the configuration specified by <delay_order_after_setback/>
			if (spooler_job.setback_max() <= 0) {
				spooler_job.set_delay_order_after_setback(1, this.setbackInterval);
				if (this.setbackCount > 0) {
					spooler_job.set_max_order_setbacks(this.setbackCount);
				}
			}
			return signalSuccess();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error("error occurred: " + e.getMessage());
			return signalFailure();
		}
	}

	public boolean spooler_process() {

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
			currentJobPath = spooler_job.name();
			currentJobPath = (currentJobPath.startsWith("/") ? currentJobPath : "/" + currentJobPath);
			currentNodeName = spooler_task.order().state();
			Variable_set orderParams = spooler_task.order().params();
			if (orderParams.var(conParameterSYNC_SESSION_ID) != null) {
				syncSessionId = orderParams.var(conParameterSYNC_SESSION_ID);
			}

			if (isJobchain() == false) {
				throw new JobSchedulerException(Messages.getMsg(JSJ_SYNC_0010));
			}
			//siehe js-461
			if (orderParams.var(conParameterSCHEDULER_SYNC_READY) != null) {
				syncReady = orderParams.var(conParameterSCHEDULER_SYNC_READY).equals("true");
			}
			if (syncReady) {
				logger.info("js-461: Sync skipped");
				Order o = spooler_task.order();
				Variable_set resultParameters = spooler.create_variable_set();
				String[] parameterNames = o.params().names().split(";");
				for (int i = 0; i < parameterNames.length; i++) {
					if (!parameterNames[i].equals(conParameterSCHEDULER_SYNC_READY)) {
						resultParameters.set_var(parameterNames[i], o.params().value(parameterNames[i]));
					}
				}
				o.set_params(resultParameters);
				return true;
			}

			String JSJ_SYNC_0040 = "JobScheduler reports error in status response: %1$s &2$s";
			String JSJ_SYNC_0050 = "could not process JobScheduler status response: %1$s";
			try { // to get the status information on current job chains
				String what = "job_chains,job_chain_orders";
				what += ",payload";
				currentStatusAnswer = spooler.execute_xml(String.format("<show_state what='%1$s' />", what ));
				currentStatusXPath = new SOSXMLXPath(new StringBuffer(currentStatusAnswer));
				//logger.debug("JobScheduler status response: " + currentStatusAnswer);
				String errorCode = currentStatusXPath.selectSingleNodeValue("/spooler/answer/ERROR/@code");
				String errorText = currentStatusXPath.selectSingleNodeValue("/spooler/answer/ERROR/@text");
				if ((errorCode != null && errorCode.length() > 0) || (errorText != null && errorText.length() > 0)) {
					throw new JobSchedulerException(Messages.getMsg(JSJ_SYNC_0040, errorCode, errorText));
				}
			}
			catch (Exception ex) {
				ex.printStackTrace(System.err);
				throw new JobSchedulerException(Messages.getMsg(JSJ_SYNC_0050, ex.getMessage()));
			}

			try { // to verify the information on the current order and current job chain
				logger.debug("---> /spooler/answer/state/job_chains/job_chain[@name = '" + currentJobChainName + "' and job_chain_node/@job = '"
						+ currentJobPath + "']");
				NodeList verifyNodes = currentStatusXPath.selectNodeList("/spooler/answer/state/job_chains/job_chain[@name = '" + currentJobChainName
						+ "' and job_chain_node/@job = '" + currentJobPath + "']");
				if (verifyNodes == null || verifyNodes.getLength() < 1) {
					logger.info("could not identify the current job and job chain in JobScheduler status response");
				}

				if (verifyNodes.getLength() != 1) {
					logger.warn("possible problem detected: current job chain name [" + currentJobChainName + "] and job path [" + currentJobPath
							+ "] are not unique in JobScheduler status response");
				}

				currentJobChainPath = verifyNodes.item(0).getAttributes().getNamedItem("path").getNodeValue();
				currentJobChainPath = (currentJobChainPath.startsWith("/") ? currentJobChainPath : "/" + currentJobChainPath);
				logger.info("synchronizing order [" + spooler_task.order().id() + ", " + spooler_task.order().title() + "] of job chain ["
						+ currentJobChainPath + "]");
			}
			catch (Exception ex) {
				ex.printStackTrace(System.err);
				throw new JobSchedulerException(ex.getMessage());
			}

			try { // to pass the current order if previously signalled by a global JobScheduler variable
				String signalVariable = currentJobChainPath + ";" + currentNodeName + "_pass_through_next_order";
				String strSignalVariable = spooler.variables().value(signalVariable);
				if (isNotEmpty(strSignalVariable)) {
					int numOfSignalledOrders = Integer.parseInt(strSignalVariable);
					if (numOfSignalledOrders > 0) {
						numOfSignalledOrders--;
						spooler.variables().set_var(signalVariable, Integer.toString(numOfSignalledOrders));
						logger.info("current order is being passed through due to global signal: " + signalVariable);
						logger.info(numOfSignalledOrders + " left for pass through operation");
						//						return true;
						return signalSuccess();
					}
				}
			}
			catch (Exception ex) {
				ex.printStackTrace(System.err);
				throw new JobSchedulerException("could not pass through current order: " + ex.getMessage());
			}

			try { // to retrieve the list of dependent job chains nodes
				nodes = currentStatusXPath.selectNodeList("/spooler/answer/state/job_chains/job_chain/job_chain_node[@job = '" + currentJobPath + "']");
				// if there is only one job chain with one synchronization job node then multiple orders for the same chain get synchronized
				if (nodes != null && nodes.getLength() == 1) {
					// TODO handle one chain in the same way as multiple job chains

					// otherwise, if there are multiple job chains
				}
				else
					if (nodes == null || nodes.getLength() < 1) {
						logger.info("no additional pending orders found");
						// suspend/setback the current order for this job chain: 
						// if this is the last node of a job chain then the order is being set back, 
						// otherwise it will be suspended
						if (this.setbackType.equalsIgnoreCase(conSetBackTypeSETBACK) || spooler_task.order().job_chain_node().next_node().next_node() == null) {
							spooler_task.order().setback();
						}
						else {
							if (!spooler_task.order().suspended()) {
								spooler_task.order().set_suspended(true);
							}
						}
						logger.info("order is set on hold: " + spooler_task.order().id() + ", " + spooler_task.order().title());
						return true;
					}
			}
			catch (Exception ex) {
				ex.printStackTrace(System.err);
				throw new JobSchedulerException("could not process JobScheduler status response: " + ex.getMessage());
			}

			try { // to check in dependent job chain nodes if the required number of orders has been enqueued
				dependentJobChainNodes = new HashMap();
				int satisfiedNode = 0;
				for (int i = 0; i < nodes.getLength(); i++) {
					dependentJobChainName = nodes.item(i).getParentNode().getAttributes().getNamedItem("name").getNodeValue();
					dependentJobChainPath = nodes.item(i).getParentNode().getAttributes().getNamedItem("path").getNodeValue();
					dependentNodeName = nodes.item(i).getAttributes().getNamedItem("state").getNodeValue();

					if (syncSessionId.length() > 0) {
						syncSessionCondition = " and payload/params/param[@name='sync_session_id' and @value='" + syncSessionId + "']";
						syncSessionDebug = ", sync_session [" + syncSessionId + "]";
					}
					else {
						syncSessionCondition = " and not(payload/params/param[@name='sync_session_id'])";
					}
					String nodeCheck = "(order_queue/order[((@setback_count > 0 and @setback != '') or @suspended = 'yes')" + syncSessionCondition
							+ "]) | (../job_chain_node[@state='" + dependentNodeName + "']/order_queue/order[@suspended = 'yes'" + syncSessionCondition + "])";
					logger.debug(nodeCheck);
					NodeList orderNodes = currentStatusXPath.selectNodeList(nodes.item(i), nodeCheck);
					//dependentJobChainOrders = Integer.parseInt(nodes.item(i).getAttributes().getNamedItem("orders").getNodeValue());
					dependentJobChainOrders = orderNodes.getLength();
					logger.debug("node list length for currently suspended/set back orders in job chain [" + dependentJobChainPath + "], state ["
							+ dependentNodeName + "]" + syncSessionDebug + ": " + dependentJobChainOrders);
					// add the currently processed order to the number of pending orders
					logger.debug("dependentJobChainPath=" + dependentJobChainPath + " currentJobChainPath=" + currentJobChainPath + " dependentNodeName="
							+ dependentNodeName + " currentNodeName=" + currentNodeName);
					if (dependentJobChainPath.equalsIgnoreCase(currentJobChainPath) && dependentNodeName.equalsIgnoreCase(currentNodeName)) {
						dependentJobChainOrders++;
					}
					logger.debug(dependentJobChainOrders + " pending orders found for job chain [" + dependentJobChainPath + "], state [" + dependentNodeName
							+ "]" + syncSessionDebug + "");

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
								logger.debug("Waiting for all orders in job_chain " + dependentJobChainPath + " ...");
								if (j != null) {
									requiredOrders = j.order_count();
								}
								else {
									logger.warn(dependentJobChainPath + "does not exist");
									requiredOrders = 1;
								}
							}
							else {
								requiredOrders = Integer.parseInt(spooler_task.params().value(dependentJobChainName + "_required_orders"));
							}
						}
						else
							if (spooler_task.params().value(conParameterREQUIRED_ORDERS) != null
									&& spooler_task.params().value(conParameterREQUIRED_ORDERS).length() > 0) {
								requiredOrders = Integer.parseInt(spooler_task.params().value(conParameterREQUIRED_ORDERS));
							}

					// should any of the dependent job chains have less orders enqueued than required, then the current order will be set on hold
					if (requiredOrders > dependentJobChainOrders) {
						passOrders = false;
					}
					else {
						if (intMinimumSyncHits > 0)
							satisfiedNode++;
					}
					logger.info("job chain [" + dependentJobChainPath + "], state [" + dependentNodeName + "]" + syncSessionDebug + " requires "
							+ requiredOrders + " orders being present, " + dependentJobChainOrders + " orders have been enqueued");

					// move this information to a hashmap for further use
					int[] orders = new int[2];
					orders[0] = dependentJobChainOrders;
					orders[1] = requiredOrders;
					dependentJobChainNodes.put(dependentJobChainPath + ";" + dependentNodeName, orders);
				}

				logger.info("Synchits:" + satisfiedNode);
				passOrders = (passOrders || satisfiedNode > 0 && satisfiedNode >= intMinimumSyncHits);
				if (!passOrders) {
					// suspend/setback the current order for this job chain: it will be suspended
					if (this.setbackType.equalsIgnoreCase(conSetBackTypeSETBACK)) {
						spooler_task.order().setback();
					}
					else {
						if (!spooler_task.order().suspended()) {
							spooler_task.order().set_state(spooler_task.order().state()); // Damit der Suspend auf den sync-Knoten geht und nicht auf den nächsten.
							spooler_task.order().set_suspended(true);
						}
					}
					logger.info("order is set on hold: " + spooler_task.order().id() + ", " + spooler_task.order().title());
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
					logger.info("synchronized job chain: " + dependentJobChainPath + ", state [" + dependentNodeName + "]" + syncSessionDebug
							+ ", required orders: " + orders[1] + ", pending orders: " + orders[0]);
				}

			}
			catch (Exception ex) {
				ex.printStackTrace(System.err);
				throw new JobSchedulerException("could not check pending orders in dependent job chains: " + ex.getMessage());
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
					logger.debug("checking job chain: " + dependentJobChainPath + ", state [" + dependentNodeName + "]");

					if (this.setbackType.equalsIgnoreCase(conSetBackTypeSETBACK)) {
						String signalVariable = keyName + "_pass_through_next_order";
						// signal current job chain to pass through the required number of orders minus the current one                    	
						if (dependentJobChainPath.equals(currentJobChainPath) && dependentNodeName.equals(currentNodeName)) {
							if (orders[1] - 1 > 0) {
								logger.debug("setting signal variable: " + signalVariable + "=" + Integer.toString(orders[1] - 1));
								spooler.set_var(signalVariable, Integer.toString(orders[1] - 1));
							}
						}
						else {
							// make other job chain pass through the required number of orders
							logger.debug("setting signal variable: " + signalVariable + "=" + Integer.toString(orders[1]));
							spooler.set_var(signalVariable, Integer.toString(orders[1]));
						}
						logger.info("signalling next " + orders[1] + " orders to be passed through by job chain: " + dependentJobChainPath + ", state ["
								+ dependentNodeName + "]");
					}

					try { // to signal waiting orders to start immediately
						NodeList signalNodes = null;
						if (this.setbackType.equalsIgnoreCase(conSetBackTypeSETBACK)) {
							signalNodes = currentStatusXPath.selectNodeList("/spooler/answer/state/job_chains/job_chain[@path = '" + dependentJobChainPath
									+ "']/job_chain_node[@state = '" + dependentNodeName + "']/order_queue/order[@setback_count > 0 and @setback != '' "
									+ syncSessionCondition + "]");
							logger.debug("orders being set back: " + signalNodes.getLength() + " /spooler/answer/state/job_chains/job_chain[@path = '"
									+ dependentJobChainPath + "']/job_chain_node[@state = '" + dependentNodeName
									+ "']/order_queue/order[@setback_count > 0 and @setback != ''" + syncSessionCondition + "]");
						}
						else {
							signalNodes = currentStatusXPath.selectNodeList("/spooler/answer/state/job_chains/job_chain[@path = '" + dependentJobChainPath
									+ "']/job_chain_node[@state = '" + dependentNodeName + "']/order_queue/order[@suspended = 'yes' " + syncSessionCondition
									+ "]");
							logger.debug("orders being suspended: " + signalNodes.getLength() + " /spooler/answer/state/job_chains/job_chain[@path = '"
									+ dependentJobChainPath + "']/job_chain_node[@state = '" + dependentNodeName + "']/order_queue/order[@suspended = 'yes' "
									+ syncSessionCondition + "]");
						}
						if (signalNodes == null || signalNodes.getLength() < 1) {
							logger.info("no suspended or set back orders found in dependent job chain: " + dependentJobChainPath);
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

						if (spooler_task.params().value(dependentJobChainName + "_required_orders") != null
								&& spooler_task.params().value(dependentJobChainName + "_required_orders").length() > 0) {
							requiredOrders = Integer.parseInt(spooler_task.params().value(dependentJobChainName + "_required_orders"));
						}
						else
							if (spooler_task.params().value(conParameterREQUIRED_ORDERS) != null
									&& spooler_task.params().value(conParameterREQUIRED_ORDERS).length() > 0) {
								requiredOrders = Integer.parseInt(spooler_task.params().value(conParameterREQUIRED_ORDERS));
							}

						for (int i = 0; i < signalNodes.getLength(); i++) {
							if (i >= requiredOrders) {
								logger.debug("maximum number of orders for reactivation reached: found " + i + ", required " + requiredOrders);
								break;
							}

							logger.info("signalling order [" + signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue()
									+ "] to be continued for job chain [" + dependentJobChainPath + "]");
							if (signalNodes.item(i).getAttributes().getNamedItem(conSetBackTypeSETBACK) != null) {
								if (!this.setbackType.equalsIgnoreCase(conSetBackTypeSETBACK)) {
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
									logger.info("signalling next " + orders[1] + " orders to be passed through by job chain: " + dependentJobChainPath
											+ ", state [" + dependentNodeName + "]");
								}

								String strM = "<modify_order job_chain='" + dependentJobChainPath + "' order='"
										+ signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue() + "' setback='no'/>";
								logger.debug(String.format("activate order after setback: %1$s", strM));
								answer = spooler.execute_xml(strM);
							}
							else {
								Job_chain j = spooler.job_chain(dependentJobChainPath);
								logger.debug("---> dependentJobChainPath:" + dependentJobChainPath);
								Job_chain_node n = j.node(signalNodes.item(i).getAttributes().getNamedItem("state").getNodeValue());
								Job_chain_node next_n = n.next_node();
								if (next_n.job() == null) {
									logger.info("end state reached");
								}
								String next_state = n.next_state();

								logger.debug("---> state:" + signalNodes.item(i).getAttributes().getNamedItem("state").getNodeValue());
								logger.debug("---> state:" + next_state);
								logger.debug("activate suspended order: <modify_order job_chain='" + dependentJobChainPath + "' order='"
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
							if ((errorCode != null && errorCode.length() > 0) || (errorText != null && errorText.length() > 0)) {
								logger.warn("JobScheduler reports error when signalling order ["
										+ signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue() + "] to pass through job chain ["
										+ dependentJobChainPath + "]: " + errorCode + " " + errorText);
							}
						}
					}
					catch (Exception ex) {
						ex.printStackTrace(System.err);
						throw new JobSchedulerException("could not signal orders to be passed through by job chains: " + ex.getMessage());
					}
				}
			}

			return true;
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error("error occurred: " + e.getMessage());
			return false;
		}
	}
}
