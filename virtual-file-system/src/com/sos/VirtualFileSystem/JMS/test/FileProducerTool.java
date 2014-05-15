package com.sos.VirtualFileSystem.JMS.test;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.protobuf.compiler.CommandLineSupport;
import org.apache.activemq.util.IndentPrinter;

/**
 * A simple tool for publishing messages
 * 
 * @version $Id$ $Revision: 1.2 $
 */
public class FileProducerTool extends Thread {
	private Destination		destination;
	private int				messageCount	= 10;
	private long			sleepTime;
	private boolean			verbose			= true;
	private int				messageSize		= 255;
	private static int		parallelThreads	= 1;
	private long			timeToLive;
	private String			user			= ActiveMQConnection.DEFAULT_USER;
	private String			password		= ActiveMQConnection.DEFAULT_PASSWORD;
	private String			url				= ActiveMQConnection.DEFAULT_BROKER_URL;
//	private String			subject			= "TOOL.DEFAULT";
	private String			subject			= "SOSDEx.Files";
	private boolean			topic;
	private boolean			transacted;
	private boolean			persistent;
	private static Object	lockResults		= new Object();

	public static void main(String[] args) {
		ArrayList<FileProducerTool> threads = new ArrayList<FileProducerTool>();
		FileProducerTool producerTool = new FileProducerTool();
		String[] unknown = CommandLineSupport.setOptions(producerTool, args);
		if (unknown.length > 0) {
			System.out.println("Unknown options: " + Arrays.toString(unknown));
			System.exit(-1);
		}
		producerTool.showParameters();
		for (int threadCount = 1; threadCount <= parallelThreads; threadCount++) {
			producerTool = new FileProducerTool();
			CommandLineSupport.setOptions(producerTool, args);
			producerTool.start();
			threads.add(producerTool);
		}
		while (true) {
			Iterator<FileProducerTool> itr = threads.iterator();
			int running = 0;
			while (itr.hasNext()) {
				FileProducerTool thread = itr.next();
				if (thread.isAlive()) {
					running++;
				}
			}
			if (running <= 0) {
				System.out.println("All threads completed their work");
				break;
			}
			try {
				Thread.sleep(1000);
			}
			catch (Exception e) {
			}
		}
	}

	public void showParameters() {
		System.out.println("Connecting to URL: " + url);
		System.out.println("Publishing a Message with size " + messageSize + " to " + (topic ? "topic" : "queue") + ": " + subject);
		System.out.println("Using " + (persistent ? "persistent" : "non-persistent") + " messages");
		System.out.println("Sleeping between publish " + sleepTime + " ms");
		System.out.println("Running " + parallelThreads + " parallel threads");
		if (timeToLive != 0) {
			System.out.println("Messages time to live " + timeToLive + " ms");
		}
	}

	public void run() {
		// Connection connection = null;
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
			}
			else {
				destination = session.createQueue(subject);
			}
			// Create the producer.
			MessageProducer producer = session.createProducer(destination);
			if (persistent) {
				producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			}
			else {
				producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			}
			if (timeToLive != 0) {
				producer.setTimeToLive(timeToLive);
			}
			// Start sending messages
			sendLoop4File(connection, destination, session, producer);
			System.out.println("[" + this.getName() + "] Done.");
			synchronized (lockResults) {
				ActiveMQConnection c = (ActiveMQConnection) connection;
				System.out.println("[" + this.getName() + "] Results:\n");
				c.getConnectionStats().dump(new IndentPrinter());
			}
		}
		catch (Exception e) {
			System.out.println("[" + this.getName() + "] Caught: " + e);
			e.printStackTrace();
		}
		finally {
			try {
				connection.close();
			}
			catch (Throwable ignore) {
			}
		}
	}

	protected void sendLoop4File(ActiveMQConnection connection, Destination destination, Session session, MessageProducer producer) throws Exception {
		HashMap<String, Object> properties = new HashMap<String, Object>();
//		String returnFileName = "C:/KB/text.txt";
			String returnFileName = "C:/KB/jerry.log";
//		String returnFileName = "C:/KB/sosftp0211/client/com.sos.net-1.6-1104-7856.jar";
		
		properties.put("sourceFileName", returnFileName);
		File fleFile = new File(returnFileName);
		properties.put("FileSize", String.valueOf(fleFile.length()));
		properties.put("targetFileName", fleFile.getName());
		properties.put("submitterURI", "com.sos.VirtualFileSystem.JMS.ActiveMQImpl");
		properties.put("submitterVersion", "1.0");
		
		int priority = producer.getPriority();
		int intDeliveryMode = producer.getDeliveryMode();
		
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(returnFileName));
		OutputStream out = connection.createOutputStream(destination, properties, intDeliveryMode, priority, timeToLive);
		
		// ActiveMQOutputStream out = new ActiveMQOutputStream(connection, producer., producer.getDestination(), properties,
		// intDeliveryMode, priority, producer.getTimeToLive()); // connection.createOutputStream(destination);
		// StreamMessage objStreamMessage = session.createStreamMessage();
		// objStreamMessage.setStringProperty("FILENAME", "test.txt");
		// producer.send(objStreamMessage);
		
		byte[] buf = new byte[32768]; // 32k buffer
		int c = 0;
		do {
		    c = in.read(buf,0,32768);
		    if (c > 0) out.write(buf,0,c);
		} while (c >= 0);

//		int value = 0;
//		while ((value = in.read()) != -1) {
//			out.write(value);
//		}
//		out.write(-1);
		in.close();
////		out.flush();
		out.close();
	}

	protected void sendLoop4Message(Session session, MessageProducer producer) throws Exception {
		for (int i = 0; i < messageCount || messageCount == 0; i++) {
			TextMessage message = session.createTextMessage(createMessageText(i));
			// StreamMessage message = session.createStreamMessage();
			if (verbose) {
				String msg = message.getText();
				if (msg.length() > 50) {
					msg = msg.substring(0, 50) + "...";
				}
				System.out.println("[" + this.getName() + "] Sending message: '" + msg + "'");
			}
			producer.send(message);
			if (transacted) {
				System.out.println("[" + this.getName() + "] Committing " + messageCount + " messages");
				session.commit();
			}
			Thread.sleep(sleepTime);
		}
	}

	private String createMessageText(int index) {
		StringBuffer buffer = new StringBuffer(messageSize);
		buffer.append("Message: " + index + " sent at: " + new Date());
		if (buffer.length() > messageSize) {
			return buffer.substring(0, messageSize);
		}
		for (int i = buffer.length(); i < messageSize; i++) {
			buffer.append(' ');
		}
		return buffer.toString();
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

	public void setParallelThreads(int parallelThreads) {
		if (parallelThreads < 1) {
			parallelThreads = 1;
		}
		this.parallelThreads = parallelThreads;
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
