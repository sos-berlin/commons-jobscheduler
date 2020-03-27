package com.sos.vfs.jms;

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
import com.sos.vfs.common.options.SOSDestinationOptions;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.SOSCommonTransfer;

public class SOSJMS extends SOSCommonTransfer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSJMS.class);
    private ConnectionFactory factory = null;
    private Connection jmsConnection = null;
    private Session session = null;

    @Override
    public boolean isConnected() {
        return jmsConnection != null;
    }

    @Override
    public void connect(final SOSDestinationOptions options) throws Exception {
        super.connect(options);
        doConnect();
    }

    @Override
    public void disconnect() {
        try {
            if (jmsConnection != null) {
                jmsConnection.close();
            }
        } catch (JMSException e) {
            LOGGER.error("Error occured closing the jms connection! ", e);
        }
    }

    private void doConnect() {
        try {
            if (host.toLowerCase().startsWith("tcp://")) {
                URL url = new URL(host);
                port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
                host = url.getHost();
            }
            LOGGER.info(SOSVfs_D_0101.params(host, port));
            this.logReply();
        } catch (Exception ex) {
            throw new JobSchedulerException(ex);
        }
    }

    public String createConnectionUrl(String protocol, String host, String port) {
        return new StringBuilder(protocol).append("://").append(host).append(":").append(port).toString();
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
    public OutputStream getOutputStream(String fileName, boolean append, boolean resume) {
        // not implemented, no need
        return null;
    }

    @Override
    public SOSFileEntry getFileEntry(String path) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
