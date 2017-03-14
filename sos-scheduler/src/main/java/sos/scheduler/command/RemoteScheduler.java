package sos.scheduler.command;

 public class RemoteScheduler {

    private String host;
    private int port;

    public RemoteScheduler(final String host, final int tcpPort) {
        this.host = host;
        this.port = tcpPort;
    }

    public RemoteScheduler(final String host, final int tcpPort, final int udpPort) {
        this.host = host;
        this.port = tcpPort;
    }

    public String sendCommand(final String command) throws Exception {
        SOSSchedulerCommand schedulerCommand = new SOSSchedulerCommand();
        schedulerCommand.setHost(host);
        schedulerCommand.setPort(port);
        try {
            schedulerCommand.connect();
            schedulerCommand.sendRequest(command);
            String response = schedulerCommand.getResponse();
            return response;
        } catch (Exception e) {
            throw new Exception("Error contacting remote Job Scheduler " + this + ": " + e, e);
        }
    }

    public String getHost() {
        return host;
    }

     
    public int getPort() {
        return port;
    }
  
    public String toString() {
        return "http://" + host + ":" + port;
    }

}