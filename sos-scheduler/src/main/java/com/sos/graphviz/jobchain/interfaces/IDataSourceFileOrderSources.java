package com.sos.graphviz.jobchain.interfaces;

import com.sos.scheduler.model.objects.JobChain;

public interface IDataSourceFileOrderSources{
    public void getList();
    public boolean hasNext();
    public JobChain.FileOrderSource next();
    public void reset();
}
    
