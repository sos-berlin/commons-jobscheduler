package com.sos.graphviz.jobchain.datasource.jobscheduler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sos.graphviz.jobchain.interfaces.IDataSourceJobChainNodes;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.JSObjJobChain;
import com.sos.scheduler.model.objects.JobChain;
import com.sos.scheduler.model.objects.JobChain.JobChainNode;

public class DataSourceJobChainNodes implements IDataSourceJobChainNodes {
    private String firstNode;
    private Map<String, JobChain.JobChainNode> listOfJobChainNodes;
    private Iterator<Map.Entry<String, JobChain.JobChainNode>> entries;
    private JSObjJobChain jobChain;
 
    public DataSourceJobChainNodes(SchedulerObjectFactory schedulerObjectFactory, String xml) {
        super();
        jobChain = schedulerObjectFactory.createJobChain();
        jobChain.loadObject(xml);
        listOfJobChainNodes = new HashMap<String, JobChain.JobChainNode>();
        firstNode = null;
    }

    public DataSourceJobChainNodes(SchedulerObjectFactory schedulerObjectFactory, JSObjJobChain jobChain) {
        super();
        this.jobChain = jobChain;
        listOfJobChainNodes = new HashMap<String, JobChain.JobChainNode>();
    }

    public void getList() {
        for (Object jobChainNodeItem : jobChain.getJobChainNodeOrFileOrderSinkOrJobChainNodeEnd()) {
            if (jobChainNodeItem instanceof JobChainNode) {
                JobChainNode jobChainNode = (JobChainNode) jobChainNodeItem;
                if (firstNode == null) {
                    firstNode = jobChainNode.getState();
                }
                if (listOfJobChainNodes.get(jobChainNode.getState()) == null) {
                    listOfJobChainNodes.put(jobChainNode.getState(),jobChainNode);
                }
            }
        }
    }

    public boolean hasNext() {
        if (entries == null) {
            entries = listOfJobChainNodes.entrySet().iterator();
        }
            
        return entries.hasNext();
    }

    public JobChain.JobChainNode next() {
        return entries.next().getValue();
    }

    public String getFirstNode() {
        return firstNode;
    }

    public void reset() {
        entries = null;
    }

    @Override
    public JobChainNode get(String key) {
        return listOfJobChainNodes.get(key);
    }

    public JSObjJobChain getJobChain() {
        return jobChain;
    }

    public void setFirstNode(String firstNode) {
        this.firstNode = firstNode;
    }

}
