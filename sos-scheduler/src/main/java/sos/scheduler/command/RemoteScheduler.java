/*
 * RemoteScheduler.java Created on 30.05.2008
 */
package sos.scheduler.command;

/*
 * @Deprecated use SOSJobSchedulerModel
 */
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
            }
        }
    }

    /** @return the host */
    public String getHost() {
        return host;
    }

    /** @param host the host to set */
    public void setHost(final String host) {
        this.host = host;
    }

    /** @return the tcpPort */
    public int getTcpPort() {
        return tcpPort;
    }

    /** @param tcpPort the tcpPort to set */
    public void setTcpPort(final int tcpPort) {
        this.tcpPort = tcpPort;
    }

    /** @return the udpPort */
    public int getUdpPort() {
        return udpPort;
    }

    /** @param udpPort the udpPort to set */
    public void setUdpPort(final int udpPort) {
        this.udpPort = udpPort;
    }

    @Override
    public String toString() {
        return host + ":" + tcpPort;
    }
}
