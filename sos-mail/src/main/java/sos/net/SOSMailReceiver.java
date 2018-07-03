package sos.net;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSMailReceiver {

	protected SOSMailAuthenticator authenticator = null;
	protected int timeout = 5000;
	private static final Logger LOGGER = LoggerFactory.getLogger(SOSMailReceiver.class);
	private Store store;
	private Folder folder = null;
	private String protocol = "POP3";
	private String folderName = "INBOX";
	private final String host;
	private final String port;
	private final String user;
	private Session session;
	private boolean ssl;
	private final String password;
	public int READ_ONLY = Folder.READ_ONLY;
	public int READ_WRITE = Folder.READ_WRITE;

	public SOSMailReceiver(final String host, final String port, final String user, final String password, boolean ssl,
			String protocol) throws Exception {
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.protocol = protocol;
		this.ssl = ssl;
		createSession();
	}

	public SOSMailReceiver(String host, String port, String user, String password) throws Exception {
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.protocol = "POP3";
		this.ssl = false;
		createSession();	}

	public Session createSession() throws Exception {
		Properties props = System.getProperties();

		if ("imap".equalsIgnoreCase(protocol)) {
			props.put("mail.imap.host", host);
			props.put("mail.imap.port", port);
			if (ssl) {
				props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				props.setProperty("mail.imap.socketFactory.fallback", "false");
				props.setProperty("mail.imap.ssl.trust", host);
				props.setProperty("mail.imap.socketFactory.port", port);
			}
		} else {
			if ("pop3".equalsIgnoreCase(protocol)) {
				props.put("mail.host", host);
				props.put("mail.port", port);
				props.put("mail.pop3.host", host);
				props.put("mail.pop3.port", port);
				if (ssl) {
					props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
					props.setProperty("mail.pop3.socketFactory.fallback", "false");
					props.setProperty("mail.pop3.ssl.trust", host);
					props.setProperty("mail.pop3.socketFactory.port", port);
				}
			} else {
				LOGGER.warn("unknown protocol: " + protocol);
			}
		}
		authenticator = new SOSMailAuthenticator(user, password);
		session = Session.getInstance(props, authenticator);

		return session;
	}

	public void connect(String mailProtocolName) throws Exception {
		mailProtocolName = mailProtocolName.toLowerCase();
		session.getProperties().put("mail." + mailProtocolName + ".timeout", String.valueOf(timeout));
		store = session.getStore(mailProtocolName);
		store.connect(host, user, password);
		LOGGER.debug("..connected to host [" + host + ":" + port + "] successfully done.");
	}

	/**
	 * opens the given folder
	 * 
	 * @param folderName1
	 * @param mode
	 *            The open mode of this folder. The open mode is Folder.READ_ONLY,
	 *            Folder.READ_WRITE, or -1 if not known.
	 * @return folder
	 * @throws Exception
	 */
	public Folder openFolder(final String folderName1, final int mode) throws Exception {
		folder = store.getFolder(folderName1);
		if (folder == null) {
			throw new JobSchedulerException("An error occured opening [" + folderName1 + "]");
		}
		folder.open(mode);
		folderName = folderName1;
		return folder;
	}

	/**
	 * opens the default folder
	 *
	 * @param mode
	 *            The open mode of this folder. The open mode is Folder.READ_ONLY,
	 *            Folder.READ_WRITE, or -1 if not known.
	 * @return folder
	 * @throws Exception
	 */
	public Folder openFolder(final int mode) throws Exception {
		folder = store.getDefaultFolder();
		if (folder == null) {
			throw new JobSchedulerException("An error occured opening default folder");
		}
		folderName = folder.getName();
		folder.open(mode);
		return folder;
	}

	/**
	 * @param expunge
	 *            expunges all deleted messages if this flag is true.
	 * @throws MessagingException
	 */
	public void closeFolder(final boolean expunge) throws MessagingException {
		if (folder != null && folder.isOpen()) {
			folder.close(expunge);
			folder = null;
		}
	}

	public void disconnect() throws MessagingException {
		if (store != null) {
			store.close();
			store = null;
		}
	}

	public String getProtocol() {
		return protocol;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(final String folderName) {
		this.folderName = folderName;
	}

	public Session getSession() {
		return session;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(final int timeout) {
		this.timeout = timeout;
	}

}