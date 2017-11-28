package com.sos.graphviz.jobchain.interfaces;

import com.sos.scheduler.model.objects.JobChain;

public interface IDataSourceFileOrderSinks{
    public void getList();
    public boolean hasNext();
    public JobChain.FileOrderSink next();
    public void reset();
    public JobChain.FileOrderSink get(String key);
    public String getFirstNode();
    public void setFirstNode(String firstNode);

}
    
