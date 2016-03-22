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

/** @author andreas pueschel */
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerSynchronizeJobChains1 extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = Logger.getLogger(JobSchedulerSynchronizeJobChains1.class);
    private static final String PARAMETER_SETBACK_COUNT = "setback_count";
    private static final String PARAMETER_SETBACK_INTERVAL = "setback_interval";
    private static final String PARAMETER_SETBACK_TYPE = "setback_type";
    private static final String SETBACK_TYPE_SETBACK = "setback";
    private static final String SETBACK_TYPE_SUSPEND = "suspend";
    private static final String PARAMETER_MINIMUM_SYNC_HITS = "minimum_sync_hits";
    private static final String PARAMETER_REQUIRED_ORDERS = "required_orders";
    private static final String PARAMETER_SCHEDULER_SYNC_READY = "scheduler_sync_ready";
    private static final String PARAMETER_SYNC_SESSION_ID = "sync_session_id";
    private int setbackInterval = 600;
    private int setbackCount = 0;
    private int intMinimumSyncHits = 0;
    private boolean syncReady = false;
    private String setbackType = SETBACK_TYPE_SUSPEND;
    private String JSJ_SYNC_0010 = "this job cannot be used standalone but requires being operated by a job chain";
    private String JSJ_SYNC_0020 = "allowed values are '%1$s' and '%2$s'";
    private String JSJ_SYNC_0030 = "illegal value for parameter '%1$s' specified '%2$s': %3$s";

    public boolean spooler_init() {
        try {
            String strSetbackT = spooler_task.params().value(PARAMETER_SETBACK_TYPE);
            try {
                if (isNotEmpty(strSetbackT)) {
                    this.setbackType = strSetbackT;
                    if (!SETBACK_TYPE_SETBACK.equalsIgnoreCase(setbackType) && !SETBACK_TYPE_SUSPEND.equalsIgnoreCase(setbackType)) {
                        throw new JobSchedulerException(Messages.getMsg(JSJ_SYNC_0020, SETBACK_TYPE_SETBACK, SETBACK_TYPE_SUSPEND));
                    }
                }
            } catch (Exception ex) {
                throw new JobSchedulerException(Messages.getMsg(JSJ_SYNC_0030, PARAMETER_SETBACK_TYPE, strSetbackT, ex.getMessage()), ex);
            }
            String strMinimumSyncHits = spooler_task.params().value(PARAMETER_MINIMUM_SYNC_HITS);
            try {
                if (isNotEmpty(strMinimumSyncHits)) {
                    this.intMinimumSyncHits = Integer.parseInt(strMinimumSyncHits);
                }
            } catch (NumberFormatException nx) {
                throw new JobSchedulerException(Messages.getMsg(JSJ_SYNC_0030, PARAMETER_MINIMUM_SYNC_HITS, strMinimumSyncHits, nx.getMessage()), nx);
            }
            String strSetBackInterval = spooler_task.params().value(PARAMETER_SETBACK_INTERVAL);
            try {
                if (isNotEmpty(strSetBackInterval)) {
                    this.setbackInterval = Integer.parseInt(strSetBackInterval);
                }
            } catch (Exception ex) {
                throw new JobSchedulerException(Messages.getMsg(JSJ_SYNC_0030, PARAMETER_SETBACK_INTERVAL, strSetBackInterval, ex.getMessage()), ex);
            }
            String strSetbackCount = spooler_task.params().value(PARAMETER_SETBACK_COUNT);
            try {
                if (isNotEmpty(strSetbackCount)) {
                    this.setbackCount = Integer.parseInt(strSetbackCount);
                }
            } catch (Exception ex) {
                throw new JobSchedulerException(Messages.getMsg(JSJ_SYNC_0030, PARAMETER_SETBACK_COUNT, strSetbackCount, ex.getMessage()), ex);
            }
            if (spooler_job.setback_max() <= 0) {
                spooler_job.set_delay_order_after_setback(1, this.setbackInterval);
                if (this.setbackCount > 0) {
                    spooler_job.set_max_order_setbacks(this.setbackCount);
                }
            }
            return signalSuccess();
        } catch (Exception e) {
            LOGGER.error("error occurred: " + e.getMessage(), e);
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
            if (orderParams.var(PARAMETER_SYNC_SESSION_ID) != null) {
                syncSessionId = orderParams.var(PARAMETER_SYNC_SESSION_ID);
            }
            if (!isJobchain()) {
                throw new JobSchedulerException(Messages.getMsg(JSJ_SYNC_0010));
            }
            if (orderParams.var(PARAMETER_SCHEDULER_SYNC_READY) != null) {
                syncReady = "true".equals(orderParams.var(PARAMETER_SCHEDULER_SYNC_READY));
            }
            if (syncReady) {
                LOGGER.info("js-461: Sync skipped");
                Order order = spooler_task.order();
                Variable_set resultParameters = spooler.create_variable_set();
                String[] parameterNames = order.params().names().split(";");
                for (int i = 0; i < parameterNames.length; i++) {
                    if (!PARAMETER_SCHEDULER_SYNC_READY.equals(parameterNames[i])) {
                        resultParameters.set_var(parameterNames[i], order.params().value(parameterNames[i]));
                    }
                }
                order.set_params(resultParameters);
                return true;
            }
            String JSJ_SYNC_0040 = "JobScheduler reports error in status response: %1$s &2$s";
            String JSJ_SYNC_0050 = "could not process JobScheduler status response: %1$s";
            try {
                String what = "job_chains,job_chain_orders";
                what += ",payload";
                currentStatusAnswer = spooler.execute_xml(String.format("<show_state what='%1$s' />", what));
                currentStatusXPath = new SOSXMLXPath(new StringBuffer(currentStatusAnswer));
                String errorCode = currentStatusXPath.selectSingleNodeValue("/spooler/answer/ERROR/@code");
                String errorText = currentStatusXPath.selectSingleNodeValue("/spooler/answer/ERROR/@text");
                if ((errorCode != null && !errorCode.isEmpty()) || (errorText != null && !errorText.isEmpty())) {
                    throw new JobSchedulerException(Messages.getMsg(JSJ_SYNC_0040, errorCode, errorText));
                }
            } catch (Exception ex) {
                throw new JobSchedulerException(Messages.getMsg(JSJ_SYNC_0050, ex.getMessage()), ex);
            }
            try {
                LOGGER.debug("---> /spooler/answer/state/job_chains/job_chain[@name = '" + currentJobChainName + "' and job_chain_node/@job = '"
                        + currentJobPath + "']");
                NodeList verifyNodes = currentStatusXPath.selectNodeList("/spooler/answer/state/job_chains/job_chain[@name = '" + currentJobChainName
                        + "' and job_chain_node/@job = '" + currentJobPath + "']");
                if (verifyNodes == null || verifyNodes.getLength() < 1) {
                    LOGGER.info("could not identify the current job and job chain in JobScheduler status response");
                }
                if (verifyNodes.getLength() != 1) {
                    LOGGER.warn("possible problem detected: current job chain name [" + currentJobChainName + "] and job path [" + currentJobPath
                            + "] are not unique in JobScheduler status response");
                }
                currentJobChainPath = verifyNodes.item(0).getAttributes().getNamedItem("path").getNodeValue();
                currentJobChainPath = (currentJobChainPath.startsWith("/") ? currentJobChainPath : "/" + currentJobChainPath);
                LOGGER.info("synchronizing order [" + spooler_task.order().id() + ", " + spooler_task.order().title() + "] of job chain ["
                        + currentJobChainPath + "]");
            } catch (Exception ex) {
                throw new JobSchedulerException(ex.getMessage(), ex);
            }

            try {
                String signalVariable = currentJobChainPath + ";" + currentNodeName + "_pass_through_next_order";
                String strSignalVariable = spooler.variables().value(signalVariable);
                if (isNotEmpty(strSignalVariable)) {
                    int numOfSignalledOrders = Integer.parseInt(strSignalVariable);
                    if (numOfSignalledOrders > 0) {
                        numOfSignalledOrders--;
                        spooler.variables().set_var(signalVariable, Integer.toString(numOfSignalledOrders));
                        LOGGER.info("current order is being passed through due to global signal: " + signalVariable);
                        LOGGER.info(numOfSignalledOrders + " left for pass through operation");
                        return signalSuccess();
                    }
                }
            } catch (Exception ex) {
                throw new JobSchedulerException("could not pass through current order: " + ex.getMessage(), ex);
            }
            try {
                nodes = currentStatusXPath.selectNodeList("/spooler/answer/state/job_chains/job_chain/job_chain_node[@job = '" + currentJobPath + "']");
                if (!(nodes != null && nodes.getLength() == 1)) {
                    LOGGER.info("no additional pending orders found");
                    if (SETBACK_TYPE_SETBACK.equalsIgnoreCase(this.setbackType) || spooler_task.order().job_chain_node().next_node().next_node() == null) {
                        spooler_task.order().setback();
                    } else if (!spooler_task.order().suspended()) {
                        spooler_task.order().set_suspended(true);
                    }
                    LOGGER.info("order is set on hold: " + spooler_task.order().id() + ", " + spooler_task.order().title());
                    return true;
                }
            } catch (Exception ex) {
                throw new JobSchedulerException("could not process JobScheduler status response: " + ex.getMessage(), ex);
            }
            try {
                dependentJobChainNodes = new HashMap();
                int satisfiedNode = 0;
                for (int i = 0; i < nodes.getLength(); i++) {
                    dependentJobChainName = nodes.item(i).getParentNode().getAttributes().getNamedItem("name").getNodeValue();
                    dependentJobChainPath = nodes.item(i).getParentNode().getAttributes().getNamedItem("path").getNodeValue();
                    dependentNodeName = nodes.item(i).getAttributes().getNamedItem("state").getNodeValue();
                    if (!syncSessionId.isEmpty()) {
                        syncSessionCondition = " and payload/params/param[@name='sync_session_id' and @value='" + syncSessionId + "']";
                        syncSessionDebug = ", sync_session [" + syncSessionId + "]";
                    } else {
                        syncSessionCondition = " and not(payload/params/param[@name='sync_session_id'])";
                    }
                    String nodeCheck = "(order_queue/order[((@setback_count > 0 and @setback != '') or @suspended = 'yes')" + syncSessionCondition
                            + "]) | (../job_chain_node[@state='" + dependentNodeName + "']/order_queue/order[@suspended = 'yes'" + syncSessionCondition + "])";
                    LOGGER.debug(nodeCheck);
                    NodeList orderNodes = currentStatusXPath.selectNodeList(nodes.item(i), nodeCheck);
                    dependentJobChainOrders = orderNodes.getLength();
                    LOGGER.debug("node list length for currently suspended/set back orders in job chain [" + dependentJobChainPath + "], state ["
                            + dependentNodeName + "]" + syncSessionDebug + ": " + dependentJobChainOrders);
                    LOGGER.debug("dependentJobChainPath=" + dependentJobChainPath + " currentJobChainPath=" + currentJobChainPath + " dependentNodeName="
                            + dependentNodeName + " currentNodeName=" + currentNodeName);
                    if (dependentJobChainPath.equalsIgnoreCase(currentJobChainPath) && dependentNodeName.equalsIgnoreCase(currentNodeName)) {
                        dependentJobChainOrders++;
                    }
                    LOGGER.debug(dependentJobChainOrders + " pending orders found for job chain [" + dependentJobChainPath + "], state [" + dependentNodeName
                            + "]" + syncSessionDebug + "");
                    int requiredOrders = 1;
                    if (spooler_task.params().value(dependentJobChainName + ";" + dependentNodeName + "_required_orders") != null
                            && !spooler_task.params().value(dependentJobChainName + ";" + dependentNodeName + "_required_orders").isEmpty()) {
                        requiredOrders = Integer.parseInt(spooler_task.params().value(dependentJobChainName + ";" + dependentNodeName + "_required_orders"));
                    } else if (spooler_task.params().value(dependentJobChainName + "_required_orders") != null
                            && !spooler_task.params().value(dependentJobChainName + "_required_orders").isEmpty()) {
                        String s = spooler_task.params().value(dependentJobChainName + "_required_orders");
                        if ("job_chain.order_count".equalsIgnoreCase(s)) {
                            Job_chain jobChain = spooler.job_chain(dependentJobChainPath);
                            LOGGER.debug("Waiting for all orders in job_chain " + dependentJobChainPath + " ...");
                            if (jobChain != null) {
                                requiredOrders = jobChain.order_count();
                            } else {
                                LOGGER.warn(dependentJobChainPath + "does not exist");
                                requiredOrders = 1;
                            }
                        } else {
                            requiredOrders = Integer.parseInt(spooler_task.params().value(dependentJobChainName + "_required_orders"));
                        }
                    } else if (spooler_task.params().value(PARAMETER_REQUIRED_ORDERS) != null
                            && !spooler_task.params().value(PARAMETER_REQUIRED_ORDERS).isEmpty()) {
                        requiredOrders = Integer.parseInt(spooler_task.params().value(PARAMETER_REQUIRED_ORDERS));
                    }
                    if (requiredOrders > dependentJobChainOrders) {
                        passOrders = false;
                    } else if (intMinimumSyncHits > 0) {
                        satisfiedNode++;
                    }
                    LOGGER.info("job chain [" + dependentJobChainPath + "], state [" + dependentNodeName + "]" + syncSessionDebug + " requires "
                            + requiredOrders + " orders being present, " + dependentJobChainOrders + " orders have been enqueued");
                    int[] orders = new int[2];
                    orders[0] = dependentJobChainOrders;
                    orders[1] = requiredOrders;
                    dependentJobChainNodes.put(dependentJobChainPath + ";" + dependentNodeName, orders);
                }
                LOGGER.info("Synchits:" + satisfiedNode);
                passOrders = (passOrders || satisfiedNode > 0 && satisfiedNode >= intMinimumSyncHits);
                if (!passOrders) {
                    if (SETBACK_TYPE_SETBACK.equalsIgnoreCase(this.setbackType)) {
                        spooler_task.order().setback();
                    } else if (!spooler_task.order().suspended()) {
                        spooler_task.order().set_state(spooler_task.order().state());
                        spooler_task.order().set_suspended(true);
                    }
                    LOGGER.info("order is set on hold: " + spooler_task.order().id() + ", " + spooler_task.order().title());
                }
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
                    LOGGER.info("synchronized job chain: " + dependentJobChainPath + ", state [" + dependentNodeName + "]" + syncSessionDebug
                            + ", required orders: " + orders[1] + ", pending orders: " + orders[0]);
                }
            } catch (Exception ex) {
                throw new JobSchedulerException("could not check pending orders in dependent job chains: " + ex.getMessage(), ex);
            }
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
                    LOGGER.debug("checking job chain: " + dependentJobChainPath + ", state [" + dependentNodeName + "]");
                    if (SETBACK_TYPE_SETBACK.equalsIgnoreCase(this.setbackType)) {
                        String signalVariable = keyName + "_pass_through_next_order";
                        if (dependentJobChainPath.equals(currentJobChainPath) && dependentNodeName.equals(currentNodeName)) {
                            if (orders[1] - 1 > 0) {
                                LOGGER.debug("setting signal variable: " + signalVariable + "=" + Integer.toString(orders[1] - 1));
                                spooler.set_var(signalVariable, Integer.toString(orders[1] - 1));
                            }
                        } else {
                            LOGGER.debug("setting signal variable: " + signalVariable + "=" + Integer.toString(orders[1]));
                            spooler.set_var(signalVariable, Integer.toString(orders[1]));
                        }
                        LOGGER.info("signalling next " + orders[1] + " orders to be passed through by job chain: " + dependentJobChainPath + ", state ["
                                + dependentNodeName + "]");
                    }
                    try {
                        NodeList signalNodes = null;
                        if (this.setbackType.equalsIgnoreCase(SETBACK_TYPE_SETBACK)) {
                            signalNodes = currentStatusXPath.selectNodeList("/spooler/answer/state/job_chains/job_chain[@path = '" + dependentJobChainPath
                                    + "']/job_chain_node[@state = '" + dependentNodeName + "']/order_queue/order[@setback_count > 0 and @setback != '' "
                                    + syncSessionCondition + "]");
                            LOGGER.debug("orders being set back: " + signalNodes.getLength() + " /spooler/answer/state/job_chains/job_chain[@path = '"
                                    + dependentJobChainPath + "']/job_chain_node[@state = '" + dependentNodeName
                                    + "']/order_queue/order[@setback_count > 0 and @setback != ''" + syncSessionCondition + "]");
                        } else {
                            signalNodes = currentStatusXPath.selectNodeList("/spooler/answer/state/job_chains/job_chain[@path = '" + dependentJobChainPath
                                    + "']/job_chain_node[@state = '" + dependentNodeName + "']/order_queue/order[@suspended = 'yes' " + syncSessionCondition
                                    + "]");
                            LOGGER.debug("orders being suspended: " + signalNodes.getLength() + " /spooler/answer/state/job_chains/job_chain[@path = '"
                                    + dependentJobChainPath + "']/job_chain_node[@state = '" + dependentNodeName + "']/order_queue/order[@suspended = 'yes' "
                                    + syncSessionCondition + "]");
                        }
                        if (signalNodes == null || signalNodes.getLength() < 1) {
                            LOGGER.info("no suspended or set back orders found in dependent job chain: " + dependentJobChainPath);
                            continue;
                        }
                        SOSXMLXPath xPath = null;
                        String answer = "";
                        int requiredOrders = 1;
                        if (dependentJobChainPath.lastIndexOf("/") > -1) {
                            dependentJobChainName = dependentJobChainPath.substring(dependentJobChainPath.lastIndexOf("/") + 1);
                        } else {
                            dependentJobChainName = dependentJobChainPath;
                        }
                        if (spooler_task.params().value(dependentJobChainName + "_required_orders") != null
                                && !spooler_task.params().value(dependentJobChainName + "_required_orders").isEmpty()) {
                            requiredOrders = Integer.parseInt(spooler_task.params().value(dependentJobChainName + "_required_orders"));
                        } else if (spooler_task.params().value(PARAMETER_REQUIRED_ORDERS) != null
                                && !spooler_task.params().value(PARAMETER_REQUIRED_ORDERS).isEmpty()) {
                            requiredOrders = Integer.parseInt(spooler_task.params().value(PARAMETER_REQUIRED_ORDERS));
                        }
                        for (int i = 0; i < signalNodes.getLength(); i++) {
                            if (i >= requiredOrders) {
                                LOGGER.debug("maximum number of orders for reactivation reached: found " + i + ", required " + requiredOrders);
                                break;
                            }
                            LOGGER.info("signalling order [" + signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue()
                                    + "] to be continued for job chain [" + dependentJobChainPath + "]");
                            if (signalNodes.item(i).getAttributes().getNamedItem(SETBACK_TYPE_SETBACK) != null) {
                                if (!this.setbackType.equalsIgnoreCase(SETBACK_TYPE_SETBACK)) {
                                    String signalVariable = keyName + "_pass_through_next_order";
                                    if (dependentJobChainPath.equals(currentJobChainPath) && dependentNodeName.equals(currentNodeName)) {
                                        if (orders[1] - 1 > 0) {
                                            spooler.set_var(signalVariable, Integer.toString(orders[1] - 1));
                                        }
                                    } else {
                                        spooler.set_var(signalVariable, Integer.toString(orders[1]));
                                    }
                                    LOGGER.info("signalling next " + orders[1] + " orders to be passed through by job chain: " + dependentJobChainPath
                                            + ", state [" + dependentNodeName + "]");
                                }
                                String strM = "<modify_order job_chain='" + dependentJobChainPath + "' order='"
                                        + signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue() + "' setback='no'/>";
                                LOGGER.debug(String.format("activate order after setback: %1$s", strM));
                                answer = spooler.execute_xml(strM);
                            } else {
                                Job_chain j = spooler.job_chain(dependentJobChainPath);
                                LOGGER.debug("---> dependentJobChainPath:" + dependentJobChainPath);
                                Job_chain_node n = j.node(signalNodes.item(i).getAttributes().getNamedItem("state").getNodeValue());
                                Job_chain_node next_n = n.next_node();
                                if (next_n.job() == null) {
                                    LOGGER.info("end state reached");
                                }
                                String next_state = n.next_state();
                                LOGGER.debug("---> state:" + signalNodes.item(i).getAttributes().getNamedItem("state").getNodeValue());
                                LOGGER.debug("---> state:" + next_state);
                                LOGGER.debug("activate suspended order: <modify_order job_chain='" + dependentJobChainPath + "' order='"
                                        + signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue() + "' state='" + next_state
                                        + "' suspended='no'/>");
                                if (next_n.job() == null) {
                                    answer = spooler.execute_xml("<modify_order job_chain='" + dependentJobChainPath + "' order='"
                                            + signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue()
                                            + "' suspended='no'><params><param name='scheduler_sync_ready' value='true'></param></params></modify_order>");
                                } else {
                                    answer = spooler.execute_xml("<modify_order job_chain='" + dependentJobChainPath + "' order='"
                                            + signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue() + "' state='" + next_state
                                            + "' suspended='no'/>");
                                }
                            }
                            xPath = new SOSXMLXPath(new StringBuffer(answer));
                            String errorCode = xPath.selectSingleNodeValue("/spooler/answer/ERROR/@code");
                            String errorText = xPath.selectSingleNodeValue("/spooler/answer/ERROR/@text");
                            if ((errorCode != null && !errorCode.isEmpty()) || (errorText != null && !errorText.isEmpty())) {
                                LOGGER.warn("JobScheduler reports error when signalling order ["
                                        + signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue() + "] to pass through job chain ["
                                        + dependentJobChainPath + "]: " + errorCode + " " + errorText);
                            }
                        }
                    } catch (Exception ex) {
                        throw new JobSchedulerException("could not signal orders to be passed through by job chains: " + ex.getMessage(), ex);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("error occurred: " + e.getMessage(), e);
            return false;
        }
    }

}