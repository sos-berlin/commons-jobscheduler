package com.sos.VirtualFileSystem.JMS.test;

/** Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License. */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.protobuf.compiler.CommandLineSupport;
import org.apache.log4j.Logger;

public class FileConsumerTool extends Thread implements MessageListener, ExceptionListener {

    private static final Logger LOGGER = Logger.getLogger(FileConsumerTool.class);
    private ActiveMQConnection connection;
    private boolean running;
    private Session session;
    private Destination destination;
    private MessageProducer replyProducer;
    private boolean pauseBeforeShutdown = false;
    private boolean verbose = true;
    private int maxiumMessages;
    private static int parallelThreads = 1;
    private String subject = "SOSDEx.Files";
    private boolean topic;
    private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
    private String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    private boolean transacted;
    private boolean durable;
    private String clientId;
    private int ackMode = Session.AUTO_ACKNOWLEDGE;
    private String consumerName = "James";
    private long sleepTime;
    private long receiveTimeOut;
    private FileOutputStream fos = null;
    private String strTargetFileNameSave = "";

    public static void main(final String[] args) {
        ArrayList<FileConsumerTool> threads = new ArrayList();
        FileConsumerTool consumerTool = new FileConsumerTool();
        String[] unknown = CommandLineSupport.setOptions(consumerTool, args);
        if (unknown.length > 0) {
            LOGGER.debug("Unknown options: " + Arrays.toString(unknown));
            System.exit(-1);
        }
        consumerTool.showParameters();
        for (int threadCount = 1; threadCount <= parallelThreads; threadCount++) {
            consumerTool = new FileConsumerTool();
            CommandLineSupport.setOptions(consumerTool, args);
            consumerTool.start();
            threads.add(consumerTool);
        }
        while (true) {
            Iterator<FileConsumerTool> itr = threads.iterator();
            int running = 0;
            while (itr.hasNext()) {
                FileConsumerTool thread = itr.next();
                if (thread.isAlive()) {
                    running++;
                }
            }
            if (running <= 0) {
                LOGGER.debug("All threads completed their work");
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
        Iterator<FileConsumerTool> itr = threads.iterator();
        while (itr.hasNext()) {
            FileConsumerTool thread = itr.next();
        }
    }

    public void showParameters() {
        LOGGER.info("Connecting to URL: " + url);
        LOGGER.info("Consuming " + (topic ? "topic" : "queue") + ": " + subject);
        LOGGER.info("Using a " + (durable ? "durable" : "non-durable") + " subscription");
        LOGGER.info("Running " + parallelThreads + " parallel threads");
    }

    @Override
    public void run() {
        try {
            running = true;
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
            connection = (ActiveMQConnection) connectionFactory.createConnection();
            if (durable && clientId != null && !clientId.isEmpty() && !"null".equals(clientId)) {
                connection.setClientID(clientId);
            }
            connection.start();
            session = connection.createSession(transacted, ackMode);
            if (topic) {
                destination = session.createTopic(subject);
            } else {
                destination = session.createQueue(subject);
            }
            replyProducer = session.createProducer(null);
            replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            MessageConsumer consumer = null;
            if (durable && topic) {
                consumer = session.createDurableSubscriber((Topic) destination, consumerName);
            } else {
                consumer = session.createConsumer(destination);
            }
            maxiumMessages = 100;
            if (maxiumMessages > 0) {
                consumeMessagesAndClose(connection, session, consumer);
            } else {
                receiveTimeOut = 1000;
                if (receiveTimeOut == 0) {
                    consumer.setMessageListener(this);
                } else {
                    consumeMessagesAndClose(connection, session, consumer, receiveTimeOut);
                }
            }
        } catch (Exception e) {
            LOGGER.error("[" + this.getName() + "] Caught: " + e.getMessage(), e);
        }
    }

    @Override
    public void onMessage(final Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage txtMsg = (TextMessage) message;
                if (verbose) {
                    String msg = txtMsg.getText();
                    int length = msg.length();
                    if (length > 50) {
                        msg = msg.substring(0, 50) + "...";
                    }
                    LOGGER.debug("[" + this.getName() + "] Received: '" + msg + "' (length " + length + ")");
                }
            } else if (message instanceof BytesMessage) {
                LOGGER.debug("[" + this.getName() + "] BytesMessage Received: '" + message + "'");
                String strTargetFileName = message.getStringProperty("targetFileName");
                LOGGER.debug("TargetFileName = " + strTargetFileName);
                try {
                    String messageId = ((ActiveMQBytesMessage) message).getGroupID();
                    if (!strTargetFileName.equalsIgnoreCase(strTargetFileNameSave) && fos != null) {
                        fos.close();
                        fos = null;
                        LOGGER.debug("filename changed " + strTargetFileName + ", " + strTargetFileNameSave);
                    }
                    if (fos == null) {
                        File outFile = new File("c:/temp/", strTargetFileName);
                        fos = new FileOutputStream(outFile);
                        strTargetFileNameSave = strTargetFileName;
                        LOGGER.debug("outfile created " + strTargetFileName);
                    }
                    int intBufSize = 32768;
                    byte[] buf = new byte[intBufSize];
                    int c = 0;
                    InputStream in = connection.createInputStream(destination);
                    LOGGER.debug("inputStream " + in);
                    do {
                        c = in.read(buf, 0, intBufSize);
                        LOGGER.debug("No of bytes read " + c);
                        if (c > 0) {
                            fos.write(buf, 0, c);
                        }
                    } while (c >= 0);
                    in.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new RuntimeException("Problems with inputStream");
                } finally {
                }
            } else {
                if (verbose) {
                    LOGGER.debug("[" + this.getName() + "] Received: '" + message + "'");
                }
            }
            if (message.getJMSReplyTo() != null) {
                replyProducer.send(message.getJMSReplyTo(), session.createTextMessage("Reply: " + message.getJMSMessageID()));
            }
            if (transacted) {
                session.commit();
            } else if (ackMode == Session.CLIENT_ACKNOWLEDGE) {
                message.acknowledge();
            }
        } catch (JMSException e) {
            LOGGER.error("[" + this.getName() + "] Caught: " + e.getMessage(), e);
        } finally {
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    @Override
    public synchronized void onException(final JMSException ex) {
        LOGGER.debug("[" + this.getName() + "] JMS Exception occured.  Shutting down client.");
        running = false;
    }

    synchronized boolean isRunning() {
        return running;
    }

    protected void consumeMessagesAndClose(final ActiveMQConnection connection, final Session session, final MessageConsumer consumer)
            throws JMSException, IOException {
        LOGGER.debug("[" + this.getName() + "] We are about to wait until we consume: " + maxiumMessages + " message(s) then we will shutdown");
        for (int i = 0; i < maxiumMessages && isRunning();) {
            Message message = consumer.receive(1000);
            if (message != null) {
                i++;
                onMessage(message);
            }
        }
        LOGGER.debug("[" + this.getName() + "] Closing connection");
        if (fos != null) {
            fos.close();
            fos = null;
            strTargetFileNameSave = "";
        }
        consumer.close();
        session.close();
        connection.close();
        if (pauseBeforeShutdown) {
            LOGGER.debug("[" + this.getName() + "] Press return to shut down");
            System.in.read();
        }
    }

    protected void consumeMessagesAndClose(final Connection connection, final Session session, final MessageConsumer consumer, final long timeout)
            throws JMSException, IOException {
        LOGGER.debug("[" + this.getName() + "] We will consume messages while they continue to be delivered within: " + timeout
                + " ms, and then we will shutdown");
        Message message;
        while ((message = consumer.receive(timeout)) != null) {
            onMessage(message);
        }
        LOGGER.debug("[" + this.getName() + "] Closing connection");
        consumer.close();
        session.close();
        connection.close();
        if (pauseBeforeShutdown) {
            LOGGER.debug("[" + this.getName() + "] Press return to shut down");
            System.in.read();
        }
    }

    public void setAckMode(final String ackMode) {
        if ("CLIENT_ACKNOWLEDGE".equals(ackMode)) {
            this.ackMode = Session.CLIENT_ACKNOWLEDGE;
        } else if ("AUTO_ACKNOWLEDGE".equals(ackMode)) {
            this.ackMode = Session.AUTO_ACKNOWLEDGE;
        } else if ("DUPS_OK_ACKNOWLEDGE".equals(ackMode)) {
            this.ackMode = Session.DUPS_OK_ACKNOWLEDGE;
        } else if ("SESSION_TRANSACTED".equals(ackMode)) {
            this.ackMode = Session.SESSION_TRANSACTED;
        }
    }

    public void setClientId(final String clientID) {
        clientId = clientID;
    }

    public void setConsumerName(final String consumerName) {
        this.consumerName = consumerName;
    }

    public void setDurable(final boolean durable) {
        this.durable = durable;
    }

    public void setMaxiumMessages(final int maxiumMessages) {
        this.maxiumMessages = maxiumMessages;
    }

    public void setPauseBeforeShutdown(final boolean pauseBeforeShutdown) {
        this.pauseBeforeShutdown = pauseBeforeShutdown;
    }

    public void setPassword(final String pwd) {
        password = pwd;
    }

    public void setReceiveTimeOut(final long receiveTimeOut) {
        this.receiveTimeOut = receiveTimeOut;
    }

    public void setSleepTime(final long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public void setParallelThreads(int parallelThreads) {
        if (parallelThreads < 1) {
            parallelThreads = 1;
        }
        FileConsumerTool.parallelThreads = parallelThreads;
    }

    public void setTopic(final boolean topic) {
        this.topic = topic;
    }

    public void setQueue(final boolean queue) {
        topic = !queue;
    }

    public void setTransacted(final boolean transacted) {
        this.transacted = transacted;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

}
