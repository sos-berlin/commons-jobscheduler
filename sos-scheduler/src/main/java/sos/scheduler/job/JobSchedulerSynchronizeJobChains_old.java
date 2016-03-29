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

/**
 * @author andreas pueschel */
public class JobSchedulerSynchronizeJobChains_old extends Job_impl {

    private int setbackInterval = 600;
    private int setbackCount = 0;
    private String setbackType = "suspend";
    private int minimumSyncHits = 0;
    private boolean syncReady = false;

    @Override
    public boolean spooler_init() {
        try {
            try {
                if (spooler_task.params().value("setback_type") != null && !spooler_task.params().value("setback_type").isEmpty()) {
                    setbackType = spooler_task.params().value("setback_type");
                    if (!"setback".equalsIgnoreCase(setbackType) && !"suspend".equalsIgnoreCase(setbackType)) {
                        throw new Exception("allowed values are \"setback\" and \"suspend\"");
                    }
                }
            } catch (Exception ex) {
                throw new Exception("illegal value for parameter [setback_type] specified [" + spooler_task.params().value("setback_type") + ": "
                        + ex.getMessage(), ex);
            }
            try {
                if (spooler_task.params().value("minimumSyncHits") != null && !spooler_task.params().value("minimum_sync_hits").isEmpty()) {
                    minimumSyncHits = Integer.parseInt(spooler_task.params().value("minimum_sync_hits"));
                }
            } catch (NumberFormatException nx) {
                throw new Exception("illegal, non-numeric parameter [minimumSyncHits] specified [" + spooler_task.params().value("minimumSyncHits") + ": "
                        + nx.getMessage());
            }
            try {
                if (spooler_task.params().value("setback_interval") != null && !spooler_task.params().value("setback_interval").isEmpty()) {
                    setbackInterval = Integer.parseInt(spooler_task.params().value("setback_interval"));
                }
            } catch (Exception ex) {
                throw new Exception("illegal, non-numeric parameter [setback_interval] specified [" + spooler_task.params().value("setback_interval") + ": "
                        + ex.getMessage());
            }
            try {
                if (spooler_task.params().value("setback_count") != null && !spooler_task.params().value("setback_count").isEmpty()) {
                    setbackCount = Integer.parseInt(spooler_task.params().value("setback_count"));
                }
            } catch (Exception ex) {
                throw new Exception("illegal, non-numeric parameter [setback_count] specified [" + spooler_task.params().value("setback_count") + ": "
                        + ex.getMessage(), ex);
            }
            if (spooler_job.setback_max() <= 0) {
                spooler_job.set_delay_order_after_setback(1, setbackInterval);
                if (setbackCount > 0) {
                    spooler_job.set_max_order_setbacks(setbackCount);
                }
            }
            return true;
        } catch (Exception e) {
            spooler_log.error("error occurred: " + e.getMessage());
            return false;
        }
    }

    private int getRequiredOrder(final String dependentJobChainPath, final String dependentJobChainName, final String dependentNodeName) {
        int requiredOrders = 1;
        if (spooler_task.params().value(dependentJobChainName + ";" + dependentNodeName + "_required_orders") != null
                && !spooler_task.params().value(dependentJobChainName + ";" + dependentNodeName + "_required_orders").isEmpty()) {
            requiredOrders = Integer.parseInt(spooler_task.params().value(dependentJobChainName + ";" + dependentNodeName + "_required_orders"));
        } else if (spooler_task.params().value(dependentJobChainName + "_required_orders") != null
                && !spooler_task.params().value(dependentJobChainName + "_required_orders").isEmpty()) {
            String s = spooler_task.params().value(dependentJobChainName + "_required_orders");
            if ("job_chain.order_count".equalsIgnoreCase(s)) {
                Job_chain j = spooler.job_chain(dependentJobChainPath);
                spooler_log.debug9("Waiting for all orders in job_chain " + dependentJobChainPath + " ...");
                if (j != null) {
                    requiredOrders = j.order_count();
                } else {
                    spooler_log.warn(dependentJobChainPath + "does not exist");
                    requiredOrders = 1;
                }
            } else {
                requiredOrders = Integer.parseInt(spooler_task.params().value(dependentJobChainName + "_required_orders"));
            }
        } else if (spooler_task.params().value("required_orders") != null && !spooler_task.params().value("required_orders").isEmpty()) {
            requiredOrders = Integer.parseInt(spooler_task.params().value("required_orders"));
        }
        return requiredOrders;
    }

    @Override
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
            currentJobChainName = spooler_task.order().job_chain().name();
            currentJobPath = spooler_job.name();
            currentJobPath = currentJobPath.startsWith("/") ? currentJobPath : "/" + currentJobPath;
            currentNodeName = spooler_task.order().state();
            Variable_set orderParams = spooler_task.order().params();
            if (orderParams.var("sync_session_id") != null) {
                syncSessionId = orderParams.var("sync_session_id");
            }
            if (spooler_task.job().order_queue() == null) {
                throw new Exception("this job cannot be used standalone but requires being operated by a job chain");
            }
            if (orderParams.var("scheduler_sync_ready") != null) {
                syncReady = "true".equals(orderParams.var("scheduler_sync_ready"));
            }
            if (syncReady) {
                spooler_log.info("js-461: Sync skipped");
                Order o = spooler_task.order();
                Variable_set resultParameters = spooler.create_variable_set();
                String[] parameterNames = o.params().names().split(";");
                for (int i = 0; i < parameterNames.length; i++) {
                    if (!"scheduler_sync_ready".equals(parameterNames[i])) {
                        resultParameters.set_var(parameterNames[i], o.params().value(parameterNames[i]));
                    }
                }
                o.set_params(resultParameters);
                return true;
            }
            try {
                String what = "job_chains,job_chain_orders";
                what += ",payload";
                currentStatusAnswer = spooler.execute_xml("<show_state what='" + what + "'/>");
                currentStatusXPath = new SOSXMLXPath(new StringBuffer(currentStatusAnswer));
                String errorCode = currentStatusXPath.selectSingleNodeValue("/spooler/answer/ERROR/@code");
                String errorText = currentStatusXPath.selectSingleNodeValue("/spooler/answer/ERROR/@text");
                if (errorCode != null && !errorCode.isEmpty() || errorText != null && !errorText.isEmpty()) {
                    throw new Exception("Job Scheduler reports error in status response: " + errorCode + " " + errorText);
                }
            } catch (Exception ex) {
                throw new Exception("could not process Job Scheduler status response: " + ex.getMessage());
            }
            try {
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
            } catch (Exception ex) {
                throw new Exception(ex.getMessage());
            }
            try {
                String signalVariable = currentJobChainPath + ";" + currentNodeName + "_pass_through_next_order";
                if (spooler.variables().value(signalVariable) != null && !spooler.variables().value(signalVariable).isEmpty()) {
                    int numOfSignalledOrders = Integer.parseInt(spooler.variables().value(signalVariable));
                    if (numOfSignalledOrders > 0) {
                        numOfSignalledOrders--;
                        spooler.variables().set_var(signalVariable, Integer.toString(numOfSignalledOrders));
                        spooler_log.info("current order is being passed through due to global signal: " + signalVariable);
                        spooler_log.info(numOfSignalledOrders + " left for pass through operation");
                        return true;
                    }
                }
            } catch (Exception ex) {
                throw new Exception("could not pass through current order: " + ex.getMessage());
            }
            try {
                nodes = currentStatusXPath.selectNodeList("/spooler/answer/state/job_chains/job_chain/job_chain_node[@job = '" + currentJobPath + "']");
                if (nodes == null || nodes.getLength() < 1) {
                    spooler_log.info("no additional pending orders found");
                    if ("setback".equalsIgnoreCase(setbackType) || spooler_task.order().job_chain_node().next_node().next_node() == null) {
                        spooler_task.order().setback();
                    } else if (!spooler_task.order().suspended()) {
                        spooler_task.order().set_suspended(true);
                    }
                    spooler_log.info("order is set on hold: " + spooler_task.order().id() + ", " + spooler_task.order().title());
                    return true;
                }
            } catch (Exception ex) {
                throw new Exception("could not process Job Scheduler status response: " + ex.getMessage());
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
                    spooler_log.debug9(nodeCheck);
                    NodeList orderNodes = currentStatusXPath.selectNodeList(nodes.item(i), nodeCheck);
                    dependentJobChainOrders = orderNodes.getLength();
                    spooler_log.debug9("node list length for currently suspended/set back orders in job chain [" + dependentJobChainPath + "], state ["
                            + dependentNodeName + "]" + syncSessionDebug + ": " + dependentJobChainOrders);
                    spooler_log.debug9("dependentJobChainPath=" + dependentJobChainPath + " currentJobChainPath=" + currentJobChainPath + " dependentNodeName="
                            + dependentNodeName + " currentNodeName=" + currentNodeName);
                    if (dependentJobChainPath.equalsIgnoreCase(currentJobChainPath) && dependentNodeName.equalsIgnoreCase(currentNodeName)) {
                        dependentJobChainOrders++;
                    }
                    spooler_log.debug3(dependentJobChainOrders + " pending orders found for job chain [" + dependentJobChainPath + "], state ["
                            + dependentNodeName + "]" + syncSessionDebug + "");
                    int requiredOrders = getRequiredOrder(dependentJobChainPath, dependentJobChainName, dependentNodeName);
                    if (requiredOrders > dependentJobChainOrders) {
                        passOrders = false;
                    } else if (minimumSyncHits > 0) {
                        satisfiedNode++;
                    }
                    spooler_log.info("job chain [" + dependentJobChainPath + "], state [" + dependentNodeName + "]" + syncSessionDebug + " requires "
                            + requiredOrders + " orders being present, " + dependentJobChainOrders + " orders have been enqueued");
                    int[] orders = new int[2];
                    orders[0] = dependentJobChainOrders;
                    orders[1] = requiredOrders;
                    dependentJobChainNodes.put(dependentJobChainPath + ";" + dependentNodeName, orders);
                }
                spooler_log.info("Synchits:" + satisfiedNode);
                passOrders = passOrders || satisfiedNode > 0 && satisfiedNode >= minimumSyncHits;
                if (!passOrders) {
                    if ("setback".equalsIgnoreCase(setbackType)) {
                        spooler_task.order().setback();
                    } else {
                        if (!spooler_task.order().suspended()) {
                            spooler_task.order().set_state(spooler_task.order().state());
                            spooler_task.order().set_suspended(true);
                        }
                    }
                    spooler_log.info("order is set on hold: " + spooler_task.order().id() + ", " + spooler_task.order().title());
                }
                Iterator jobChainNodesIterator = dependentJobChainNodes.keySet().iterator();
                while (jobChainNodesIterator.hasNext()) {
                    String keyName = (String) jobChainNodesIterator.next();
                    dependentJobChainPath = keyName.split(";")[0];
                    dependentNodeName = keyName.split(";")[1];
                    if (dependentJobChainPath == null || dependentNodeName == null) {
                        continue;
                    }
                    int[] orders = (int[]) dependentJobChainNodes.get(keyName);
                    if (orders == null) {
                        continue;
                    }
                    spooler_log.info("synchronized job chain: " + dependentJobChainPath + ", state [" + dependentNodeName + "]" + syncSessionDebug
                            + ", required orders: " + orders[1] + ", pending orders: " + orders[0]);
                }
            } catch (Exception ex) {
                throw new Exception("could not check pending orders in dependent job chains: " + ex.getMessage());
            }
            if (passOrders) {
                Iterator jobChainNodesIterator = dependentJobChainNodes.keySet().iterator();
                while (jobChainNodesIterator.hasNext()) {
                    String keyName = (String) jobChainNodesIterator.next();
                    dependentJobChainPath = keyName.split(";")[0];
                    dependentNodeName = keyName.split(";")[1];
                    if (dependentJobChainPath == null || dependentNodeName == null) {
                        continue;
                    }
                    int[] orders = (int[]) dependentJobChainNodes.get(keyName);
                    if (orders == null) {
                        continue;
                    }
                    spooler_log.debug6("checking job chain: " + dependentJobChainPath + ", state [" + dependentNodeName + "]");
                    if (setbackType.equalsIgnoreCase("setback")) {
                        String signalVariable = keyName + "_pass_through_next_order";
                        if (dependentJobChainPath.equals(currentJobChainPath) && dependentNodeName.equals(currentNodeName)) {
                            if (orders[1] - 1 > 0) {
                                spooler_log.debug6("setting signal variable: " + signalVariable + "=" + Integer.toString(orders[1] - 1));
                                spooler.set_var(signalVariable, Integer.toString(orders[1] - 1));
                            }
                        } else {
                            spooler_log.debug6("setting signal variable: " + signalVariable + "=" + Integer.toString(orders[1]));
                            spooler.set_var(signalVariable, Integer.toString(orders[1]));
                        }
                        spooler_log.info("signalling next " + orders[1] + " orders to be passed through by job chain: " + dependentJobChainPath + ", state ["
                                + dependentNodeName + "]");
                    }
                    try {
                        NodeList signalNodes = null;
                        if ("setback".equalsIgnoreCase(setbackType)) {
                            signalNodes = currentStatusXPath.selectNodeList("/spooler/answer/state/job_chains/job_chain[@path = '" + dependentJobChainPath
                                    + "']/job_chain_node[@state = '" + dependentNodeName + "']/order_queue/order[@setback_count > 0 and @setback != '' "
                                    + syncSessionCondition + "]");
                            spooler_log.debug9("orders being set back: " + signalNodes.getLength() + " /spooler/answer/state/job_chains/job_chain[@path = '"
                                    + dependentJobChainPath + "']/job_chain_node[@state = '" + dependentNodeName
                                    + "']/order_queue/order[@setback_count > 0 and @setback != ''" + syncSessionCondition + "]");
                        } else {
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
                        } else {
                            dependentJobChainName = dependentJobChainPath;
                        }
                        requiredOrders = getRequiredOrder(dependentJobChainPath, dependentJobChainName, dependentNodeName);
                        for (int i = 0; i < signalNodes.getLength(); i++) {
                            if (i >= requiredOrders) {
                                spooler_log.debug9("maximum number of orders for reactivation reached: found " + i + ", required " + requiredOrders);
                                break;
                            }
                            spooler_log.info("signalling order [" + signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue()
                                    + "] to be continued for job chain [" + dependentJobChainPath + "]");
                            if (signalNodes.item(i).getAttributes().getNamedItem("setback") != null) {
                                if (!"setback".equalsIgnoreCase(setbackType)) {
                                    String signalVariable = keyName + "_pass_through_next_order";
                                    if (dependentJobChainPath.equals(currentJobChainPath) && dependentNodeName.equals(currentNodeName)) {
                                        if (orders[1] - 1 > 0) {
                                            spooler.set_var(signalVariable, Integer.toString(orders[1] - 1));
                                        }
                                    } else {
                                        spooler.set_var(signalVariable, Integer.toString(orders[1]));
                                    }
                                    spooler_log.info("signalling next " + orders[1] + " orders to be passed through by job chain: " + dependentJobChainPath
                                            + ", state [" + dependentNodeName + "]");
                                }
                                spooler_log.debug9("activate order after setback: <modify_order job_chain='" + dependentJobChainPath + "' order='"
                                        + signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue() + "' setback='no'/>");
                                answer = spooler.execute_xml("<modify_order job_chain='" + dependentJobChainPath + "' order='"
                                        + signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue() + "' setback='no'/>");
                            } else {
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
                            if (errorCode != null && !errorCode.isEmpty() || errorText != null && !errorText.isEmpty()) {
                                spooler_log.warn("Job Scheduler reports error when signalling order ["
                                        + signalNodes.item(i).getAttributes().getNamedItem("id").getNodeValue() + "] to pass through job chain ["
                                        + dependentJobChainPath + "]: " + errorCode + " " + errorText);
                            }
                        }
                    } catch (Exception ex) {
                        throw new Exception("could not signal orders to be passed through by job chains: " + ex.getMessage(), ex);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            spooler_log.error("error occurred: " + e.getMessage());
            return false;
        }
    }

}