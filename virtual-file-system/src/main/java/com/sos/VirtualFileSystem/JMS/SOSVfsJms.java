package com.sos.VirtualFileSystem.JMS;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Options.SOSDestinationOptions;
import com.sos.VirtualFileSystem.common.SOSFileEntry;
import com.sos.VirtualFileSystem.common.SOSVfsTransferBaseClass;

public class SOSVfsJms extends SOSVfsTransferBaseClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsJms.class);
    private ConnectionFactory factory = null;
    private Connection jmsConnection = null;
    private Session session = null;

    @Override
    public ISOSConnection connect() {
        this.connect(destinationOptions);
        return this;
    }

    @Override
    public ISOSConnection connect(final SOSDestinationOptions pConnection2OptionsAlternate) {
        destinationOptions = pConnection2OptionsAlternate;
        if (destinationOptions == null) {
            throw new JobSchedulerException(SOSVfs_E_190.params("destinationOptions"));
        }
        this.doConnect(destinationOptions.host.getValue(), destinationOptions.port.value());
        return this;
    }

    private void doConnect(final String host, final int port) {
        if (!this.isConnected()) {
            try {
                this.port = port;
                this.host = host;
                if (host.toLowerCase().startsWith("tcp://")) {
                    URL url = new URL(host);
                    this.port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
                    this.host = url.getHost();
                }
                LOGGER.info(SOSVfs_D_0101.params(host, port));
                this.logReply();
            } catch (Exception ex) {
                throw new JobSchedulerException(ex);
            }
        } else {
            LOGGER.warn(SOSVfs_D_0103.params(host, port));
        }
    }

    public String createConnectionUrl(String protocol, String hostName, String port) {
        StringBuilder strb = new StringBuilder();
        strb.append(protocol).append("://").append(hostName).append(":").append(port);
        return strb.toString();
    }

    @Override
    public void disconnect() {
        try {
            jmsConnection.close();
        } catch (JMSException e) {
            LOGGER.error("Error occured closing the jms connection! ", e);
        }
    }

    @Override
    public boolean isConnected() {
        return jmsConnection != null;
    }

    public Connection createConnection(String uri) {
        factory = new ActiveMQConnectionFactory(uri);
        jmsConnection = null;
        try {
            jmsConnection = factory.createConnection();
        } catch (JMSException e) {
            LOGGER.error("JMSException occurred while trying to connect: ", e);
        }
        return jmsConnection;
    }

    public Session createSession(Connection jmsConnection) {
        session = null;
        try {
            session = jmsConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        } catch (JMSException e) {
            LOGGER.error("JMSException occurred while trying to create Session: ", e);
        }
        return session;
    }

    public Destination createDestination(Session session, String queueName) {
        Destination destination = null;
        try {
            destination = session.createQueue(queueName);
        } catch (JMSException e) {
            LOGGER.error("JMSException occurred while trying to create Destination: ", e);
        }
        return destination;
    }

    public MessageProducer createMessageProducer(Session session, Destination destination) {
        MessageProducer producer = null;
        try {
            producer = session.createProducer(destination);
        } catch (JMSException e) {
            LOGGER.error("JMSException occurred while trying to create MessageProducer: ", e);
        }
        return producer;
    }

    public void write(String text, String connectionUrl, String queueName) {
        Connection jmsConnection = createConnection(connectionUrl);
        Session session = createSession(jmsConnection);
        Destination destination = createDestination(session, queueName);
        MessageProducer producer = createMessageProducer(session, destination);
        Message message = null;
        try {
            message = session.createTextMessage(text);
            producer.send(message);
        } catch (JMSException e) {
            LOGGER.error("JMSException occurred in ProducerJob while trying to write Message to Destination: ", e);
        } finally {
            if (jmsConnection != null) {
                try {
                    jmsConnection.close();
                } catch (JMSException e) {
                    LOGGER.error("JMSException occurred in ProducerJob while trying to close the connection: ", e);
                }
            }
        }
    }

    public MessageConsumer createMessageConsumer(Session session, Destination destination) {
        MessageConsumer consumer = null;
        try {
            consumer = session.createConsumer(destination);
        } catch (JMSException e) {
            LOGGER.error("JMSException occurred while trying to create MessageConsumer: ", e);
        }
        return consumer;
    }

    public String read(Connection jmsConnection, String queueName, Boolean closeMessage) {
        String messageText = null;
        try {
            Session session = createSession(jmsConnection);
            Destination destination = createDestination(session, queueName);
            jmsConnection.start();
            MessageConsumer consumer = createMessageConsumer(session, destination);
            Message receivedMessage = null;
            while (true) {
                receivedMessage = consumer.receive(1);
                if (receivedMessage != null) {
                    if (receivedMessage instanceof TextMessage) {
                        TextMessage message = (TextMessage) receivedMessage;
                        messageText = message.getText();
                        LOGGER.debug("Reading message from queue: " + messageText);
                        break;
                    } else {
                        break;
                    }
                }
            }
            if (closeMessage) {
                receivedMessage.acknowledge();
            }
        } catch (JMSException e) {
            LOGGER.error("JMSException occurred while trying to read from Destination: ", e);
        } finally {
            if (jmsConnection != null) {
                try {
                    jmsConnection.close();
                } catch (JMSException e) {
                    LOGGER.error("JMSException occurred while trying to close the connection: ", e);
                }
            }
        }
        return messageText;
    }

    @Override
    public OutputStream getOutputStream() {
        // not implemented, no need
        return null;
    }

    @Override
    public InputStream getInputStream() {
        // not implemented, no need
        return null;
    }
   
    @Override
    public boolean isSimulateShell() {
        return false;
    }

    @Override
    public void setSimulateShell(boolean simulateShell) {
        // not implemented, no need
    }

    @Override
    public OutputStream getOutputStream(String fileName) {
        // not implemented, no need
        return null;
    }
    

    @Override
    public SOSFileEntry getFileEntry(String path) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
