package sos.scheduler.command;

/** @Deprecated use SOSJobSchedulerModel */
@Deprecated
public class RemoteScheduler {

    private String host;
    private int tcpPort;
    private int udpPort;

    public RemoteScheduler(final String host, final int tcpPort) {
        this.host = host;
        this.tcpPort = tcpPort;
    }

    public RemoteScheduler(final String host, final int tcpPort, final int udpPort) {
        this.host = host;
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
    }

    public String sendCommand(final String command) throws Exception {
        SOSSchedulerCommand schedulerCommand = new SOSSchedulerCommand();
        schedulerCommand.setHost(getHost());
        schedulerCommand.setPort(getTcpPort());
        schedulerCommand.setProtocol("tcp");
        try {
            schedulerCommand.connect();
            schedulerCommand.sendRequest(command);
            String response = schedulerCommand.getResponse();
            return response;
        } catch (Exception e) {
            throw new Exception("Error contacting remote Job Scheduler " + this + ": " + e, e);
        } finally {
            try {
                schedulerCommand.disconnect();
            } catch (Exception ex) {
                //
            }
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(final int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(final int udpPort) {
        this.udpPort = udpPort;
    }

    public String toString() {
        return host + ":" + tcpPort;
    }

}