package com.sos.graphviz.jobchain.interfaces;

import com.sos.scheduler.model.objects.JSObjJobChain;
import com.sos.scheduler.model.objects.JSObjOrder;

public interface IDataSourceOrders{
    public void getList();
    public boolean hasNext();
    public JSObjOrder next();
    public void reset();
    public JSObjJobChain getJobChain();
    public String getName();
}
    
