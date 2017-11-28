package com.sos.graphviz.jobchain.datasource.jobscheduler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sos.graphviz.jobchain.interfaces.IDataSourceFileOrderSinks;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.JSObjJobChain;
import com.sos.scheduler.model.objects.JobChain;
import com.sos.scheduler.model.objects.JobChain.FileOrderSink;

public class DataSourceFileOrderSinks implements IDataSourceFileOrderSinks {
    private String firstNode;
    private Map<String, JobChain.FileOrderSink> listOfFileSinks;
    private Iterator<Map.Entry<String, JobChain.FileOrderSink>> entries;
    private JSObjJobChain jobChain;
    

    public DataSourceFileOrderSinks(SchedulerObjectFactory schedulerObjectFactory, String xml) {
        super();
        jobChain = schedulerObjectFactory.createJobChain();
        jobChain.loadObject(xml);
        listOfFileSinks = new HashMap<String, JobChain.FileOrderSink>();
    }

    public DataSourceFileOrderSinks(SchedulerObjectFactory schedulerObjectFactory, JSObjJobChain jobChain) {
        super();
        this.jobChain = jobChain;
        listOfFileSinks = new HashMap<String, JobChain.FileOrderSink>();
    }

    public void getList() {

        for (Object jobChainNodeItem : jobChain.getJobChainNodeOrFileOrderSinkOrJobChainNodeEnd()) {
            if (jobChainNodeItem instanceof FileOrderSink) {
                FileOrderSink fileOrderSink = (FileOrderSink) jobChainNodeItem;
                String state = fileOrderSink.getState();
                if (listOfFileSinks.get(state) == null) {
                    listOfFileSinks.put(state, fileOrderSink);
                    if (firstNode == null) {
                        firstNode = state;
                    }

                }
            }
        }
    }

    public boolean hasNext() {
        if (entries == null) {
            entries = listOfFileSinks.entrySet().iterator();
        }

        return entries.hasNext();
    }

    public JobChain.FileOrderSink next() {
        return entries.next().getValue();
    }

    public void reset() {
        entries = null;
    }

    @Override
    public FileOrderSink get(String key) {
        return listOfFileSinks.get(key);
    }

    public JSObjJobChain getJobChain() {
        return jobChain;
    }

    public String getFirstNode() {
        return firstNode;
    }

    public void setFirstNode(String firstNode) {
        this.firstNode = firstNode;
    }

}
