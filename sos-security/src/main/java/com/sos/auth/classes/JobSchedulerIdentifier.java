package com.sos.auth.classes;

public class JobSchedulerIdentifier {
    String schedulerId;
    String host;
    Integer port;
    
    
    public String getSchedulerId() {
        return schedulerId;
    }
    public JobSchedulerIdentifier(String schedulerId) {
        super();
        this.schedulerId = schedulerId;
        host="";
        port=0;
    }
    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public Integer getPort() {
        return port;
    }
    public void setPort(Integer port) {
        this.port = port;
    }
    
    public String getId(){
        String s = schedulerId;
        if (!"".equals(host)){
            s = s + ":" + host;
        }
        if (port != null && port > 0){
            s = s + ":" + port;
        }
        return s;
    }
    
}
