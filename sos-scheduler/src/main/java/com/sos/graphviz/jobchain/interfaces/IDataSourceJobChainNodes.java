package com.sos.graphviz.jobchain.interfaces;

import com.sos.scheduler.model.objects.JobChain;

public interface IDataSourceJobChainNodes {
    public void getList();
    public boolean hasNext();
    public JobChain.JobChainNode next();
    public void reset();
    public JobChain.JobChainNode get(String key);
    public String getFirstNode();
    public void setFirstNode(String firstNode);

}
    
