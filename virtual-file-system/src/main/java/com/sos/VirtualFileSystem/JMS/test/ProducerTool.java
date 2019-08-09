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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.protobuf.compiler.CommandLineSupport;
import org.apache.activemq.util.IndentPrinter;
import org.apache.log4j.Logger;

public class ProducerTool extends Thread {

    private static final Logger LOGGER = Logger.getLogger(ProducerTool.class);
    private Destination destination;
    private int messageCount = 10;
    private long sleepTime;
    private boolean verbose = true;
    private int messageSize = 255;
    private static int parallelThreads = 1;
    private long timeToLive;
    private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
    private String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    private String subject = "TOOL.DEFAULT";
    private boolean topic;
    private boolean transacted;
    private boolean persistent;
    private static Object lockResults = new Object();

    public static void main(String[] args) {
        List<ProducerTool> threads = new ArrayList<ProducerTool>();
        ProducerTool producerTool = new ProducerTool();
        String[] unknown = CommandLineSupport.setOptions(producerTool, args);
        if (unknown.length > 0) {
            LOGGER.debug("Unknown options: " + Arrays.toString(unknown));
            System.exit(-1);
        }
        producerTool.showParameters();
        for (int threadCount = 1; threadCount <= parallelThreads; threadCount++) {
            producerTool = new ProducerTool();
            CommandLineSupport.setOptions(producerTool, args);
            producerTool.start();
            threads.add(producerTool);
        }
        while (true) {
            int running = 0;
            for (ProducerTool thread : threads) {
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
                //
            }
        }
    }

    public void showParameters() {
        LOGGER.debug("Connecting to URL: " + url);
        LOGGER.debug("Publishing a Message with size " + messageSize + " to " + (topic ? "topic" : "queue") + ": " + subject);
        LOGGER.debug("Using " + (persistent ? "persistent" : "non-persistent") + " messages");
        LOGGER.debug("Sleeping between publish " + sleepTime + " ms");
        LOGGER.debug("Running " + parallelThreads + " parallel threads");
        if (timeToLive != 0) {
            LOGGER.debug("Messages time to live " + timeToLive + " ms");
        }
    }

    public void run() {
        ActiveMQConnection connection = null;
        try {
            // Create the connection.
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
            connection = (ActiveMQConnection) connectionFactory.createConnection();
            connection.start();
            // Create the session
            Session session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
            if (topic) {
                destination = session.createTopic(subject);
            } else {
                destination = session.createQueue(subject);
            }
            // Create the producer.
            MessageProducer producer = session.createProducer(destination);
            if (persistent) {
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            } else {
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            }
            if (timeToLive != 0) {
                producer.setTimeToLive(timeToLive);
            }
            // Start sending messages
            sendLoop(session, producer);
            LOGGER.debug("[" + this.getName() + "] Done.");
            synchronized (lockResults) {
                ActiveMQConnection c = (ActiveMQConnection) connection;
                LOGGER.debug("[" + this.getName() + "] Results:\n");
                c.getConnectionStats().dump(new IndentPrinter());
            }
        } catch (Exception e) {
            LOGGER.error("[" + this.getName() + "] Caught: " + e.getMessage(), e);
        } finally {
            try {
                connection.close();
            } catch (Exception ignore) {
                //
            }
        }
    }

    protected void sendLoop(Session session, MessageProducer producer) throws Exception {
        for (int i = 0; i < messageCount || messageCount == 0; i++) {
            TextMessage message = session.createTextMessage(createMessageText(i));
            if (verbose) {
                String msg = message.getText();
                if (msg.length() > 50) {
                    msg = msg.substring(0, 50) + "...";
                }
                LOGGER.debug("[" + this.getName() + "] Sending message: '" + msg + "'");
            }
            producer.send(message);
            if (transacted) {
                LOGGER.debug("[" + this.getName() + "] Committing " + messageCount + " messages");
                session.commit();
            }
            Thread.sleep(sleepTime);
        }
    }

    private String createMessageText(int index) {
        StringBuilder strb = new StringBuilder(messageSize);
        strb.append("Message: " + index + " sent at: " + new Date());
        if (strb.length() > messageSize) {
            return strb.substring(0, messageSize);
        }
        for (int i = strb.length(); i < messageSize; i++) {
            strb.append(' ');
        }
        return strb.toString();
    }

    public void setPersistent(boolean durable) {
        this.persistent = durable;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public void setMessageSize(int messageSize) {
        this.messageSize = messageSize;
    }

    public void setPassword(String pwd) {
        this.password = pwd;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void setParallelThreads(int _parallelThreads) {
        if (_parallelThreads < 1) {
            _parallelThreads = 1;
        }
        parallelThreads = _parallelThreads;
    }

    public void setTopic(boolean topic) {
        this.topic = topic;
    }

    public void setQueue(boolean queue) {
        this.topic = !queue;
    }

    public void setTransacted(boolean transacted) {
        this.transacted = transacted;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
