package com.sos.jobscheduler.tools.webservices;

 import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

    @XmlRootElement(name = "sos_security_webservice_answer")
    public class SOSSecurityWebserviceAnswer {
    private String message;
    private String user;
    private String resource;
  
    public SOSSecurityWebserviceAnswer() {
    }
    
    public SOSSecurityWebserviceAnswer(String user, String resource, String message) {
    this.message = message;
    this.user = user;
    this.resource = resource;
    }
     
    @XmlElement
    public void setMessage(String message) {
    this.message = message;
    }
     
    public String getMessage() {
    return this.message;
    }

    @XmlElement
    public void setUser(String user) {
    this.user = user;
    }
     
    public String getUser() {
    return this.user;
    }
     
    @XmlElement
    public void setResource(String resource) {
    this.resource = resource;
    }
     
    public String getResource() {
    return this.resource;
    }
    
    
    @Override
    public String toString() {
    return new StringBuffer(" user: ").append(this.user)
    .append(" resource: ").append(this.resource).append(" -> ").append(this.message).toString();
    }
    }