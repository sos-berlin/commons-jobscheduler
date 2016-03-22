package com.sos.scheduler.model;

public interface ISOSSchedulerSocket {

    public abstract int getTCPTimeoutValue(); // public int gettcp_time_out

    public abstract ISOSSchedulerSocket setTCPTimeoutValue(String pstrValue); // public
                                                                              // SchedulerObjectFactoryOptions
                                                                              // settcp_time_out

    public abstract int getUDPPortNumber(); // public int getUDPPortNumber

    public abstract ISOSSchedulerSocket setUDPPortNumber(String pstrValue); // public
                                                                            // SchedulerObjectFactoryOptions
                                                                            // setUDPPortNumber

    public abstract int getPortNumber(); // public int getPortNumber

    public abstract ISOSSchedulerSocket setPortNumber(String pstrValue); // public
                                                                         // SchedulerObjectFactoryOptions
                                                                         // setPortNumber

    public abstract String getTransferMethod(); // public String
                                                // getTransferMethod

    public abstract ISOSSchedulerSocket setTransferMethod(String pstrValue); // public
                                                                             // SchedulerObjectFactoryOptions
                                                                             // setTransferMethod

    public abstract String getServerName(); // public String getServerName

    public abstract ISOSSchedulerSocket setServerName(String pstrValue); // public
                                                                         // SchedulerObjectFactory
                                                                         // setServerName

}