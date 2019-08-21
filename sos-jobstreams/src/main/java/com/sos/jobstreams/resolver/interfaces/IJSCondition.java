package com.sos.jobstreams.resolver.interfaces;

public interface IJSCondition {

    public String getJobSchedulerId();

    public String getJob();

    public String getExpression();
    
    public String getJobStream();
 
}
