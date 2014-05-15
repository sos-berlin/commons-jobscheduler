package com.sos.jobscheduler.tools.webservices;

 import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

    @XmlRootElement(name = "sos_security_webservice_answer")
    public class SOSCommandSecurityWebserviceAnswer {
    private String message;
    private String jobSchedulerAnswer;
    private String user;
    private String resource;
    private String sessionId;
    private boolean isEnabled;
  
    public SOSCommandSecurityWebserviceAnswer() {
    }
    
    public SOSCommandSecurityWebserviceAnswer(String user, String resource, String message, String sessionId) {
    this.message = message;
    this.user = user;
    this.resource = resource;
    this.sessionId = sessionId;
    }
     
    
    @XmlElement
    public void setMessage(String message) {
    this.message = message;
    }
     
    public String getMessage() {
    return this.message;
    }

    @XmlElement
    public void setJobSchedulerAnswer(String jobSchedulerAnswer) {
    this.jobSchedulerAnswer = jobSchedulerAnswer;
    }
     
    public String getJobSchedulerAnswer() {
    return this.jobSchedulerAnswer;
    }

    @XmlElement
    public void setUser(String user) {
    this.user = user;
    }
     
    public String getUser() {
    return this.user;
    }

    @XmlElement
    public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
    }
     
    public String getSessionId() {
    return this.sessionId;
    }

     
    @XmlElement
    public void setResource(String resource) {
    this.resource = resource;
    }
     
    public String getResource() {
    return this.resource;
    }
    
    @XmlElement
    public void setIsEnabled(boolean isEnabled) {
       this.isEnabled = isEnabled;
    }
     
    public boolean getIsEnabled() {
       return this.isEnabled;
    }
    
    @Override
    public String toString() {
    return new StringBuffer(" message: ").append(this.message)
          .append(" user: ").append(this.user) .append(" sessionId: ").append(this.sessionId).append(" -> ").append(this.resource).toString();
    }
    }