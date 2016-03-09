package sos.net;

import java.io.OutputStreamWriter;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import sos.util.NullBufferedWriter;
import sos.util.SOSLogger;
import sos.util.SOSStandardLogger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** Mail Receiver (POP3, IMAP)
 *
 * @version $Id: SOSMailReceiver.java 18712 2013-01-02 21:27:15Z kb $ */

public class SOSMailReceiver {

    private final String conSVNVersion = "$Id: SOSMailReceiver.java 18712 2013-01-02 21:27:15Z kb $";

    /** */
    private Store store;

    public int READ_ONLY = Folder.READ_ONLY;
    public int READ_WRITE = Folder.READ_WRITE;
    private String folderName = "INBOX";
    private Folder folder = null;
    private String protocol = "POP3";
    // Vector<SOSMailAttachment> sosMailAttachmentList = new
    // Vector<SOSMailAttachment>();
    // Vector<?> sosMailAttachmentList = new Vector();
    SOSLogger logger = null;

    protected SOSMailAuthenticator authenticator = null;
    private final String host;
    private final String port;
    private final String user;
    private Session session;
    private final String password;
    protected int timeout = 5000;

    /** @param host
     * @param port
     * @param user
     * @param password
     * @throws Exception */
    public SOSMailReceiver(final String host, final String port, final String user, final String password) throws Exception {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        createSession();
        setStubLogger();
        logger.debug(conSVNVersion);
    }

    /** @throws Exception */
    private void setStubLogger() throws Exception {
        if (logger == null)
            logger = new SOSStandardLogger(new NullBufferedWriter(new OutputStreamWriter(System.out)), SOSStandardLogger.DEBUG1);
        return;
    }

    /** @return
     * @throws Exception */
    public Session createSession() throws Exception {
        Properties props = System.getProperties();
        props.put("mail.host", host);
        props.put("mail.port", port);
        if (!user.equals("") && !password.equals("")) {
            authenticator = new SOSMailAuthenticator(user, password);
            session = Session.getInstance(props, authenticator);
        } else {
            authenticator = new SOSMailAuthenticator(user, password);
            session = Session.getInstance(props, authenticator);
        }
        return session;
    } // createSession

    /** @param pstrMailProtocolName the protocol to be used: "imap" for IMAP and
     *            pop3 for POP3
     *
     * @throws MessagingException */
    public void connect(final String pstrMailProtocolName) throws Exception {
        // set timeout
        String strMailProtocolName = pstrMailProtocolName.toLowerCase();
        session.getProperties().put("mail." + strMailProtocolName + ".timeout", String.valueOf(timeout));
        store = session.getStore(strMailProtocolName);
        store.connect(host, user, password);
        logger.debug5("..connected to host [" + host + ":" + port + "] successfully done.");
    } // connect

    /** @param folderName1
     * @param mode The open mode of this folder. The open mode is
     *            Folder.READ_ONLY, Folder.READ_WRITE, or -1 if not known.
     * @return folder
     * @throws Exception */
    public Folder openFolder(final String folderName1, final int mode) throws Exception {

        folder = store.getFolder(folderName1);

        if (folder == null)
            throw new JobSchedulerException("An error occured opening [" + folderName1 + "]");

        folder.open(mode);
        folderName = folderName1;
        return folder;
    }// openFolder

    /** opens the default folder
     *
     * @param mode The open mode of this folder. The open mode is
     *            Folder.READ_ONLY, Folder.READ_WRITE, or -1 if not known.
     * @return folder
     * @throws Exception */
    public Folder openFolder(final int mode) throws Exception {

        folder = store.getDefaultFolder();

        if (folder == null) {
            throw new JobSchedulerException("An error occured opening default folder");
        }

        // folder = folder.getFolder(folderName);
        folderName = folder.getName();
        folder.open(mode);
        return folder;
    }// openFolder

    /** @param expunge expunges all deleted messages if this flag is true.
     * @throws MessagingException */
    public void closeFolder(final boolean expunge) throws MessagingException {
        if (folder != null && folder.isOpen()) {
            folder.close(expunge);
            folder = null;
        }
    }// closeFolder

    /** @throws MessagingException */
    public void disconnect() throws MessagingException {
        if (store != null) {
            store.close();
            store = null;
        } // if
    }// disconnect

    /** @return Returns the protocol. */
    public String getProtocol() {
        return protocol;
    }

    /** @param protocol The protocol to set. */
    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    /** @return Returns the folderName. */
    public String getFolderName() {
        return folderName;
    }

    /** @param folderName The folderName to set. */
    public void setFolderName(final String folderName) {
        this.folderName = folderName;
    }

    /** @return Returns the logger. */
    public SOSLogger getLogger() {
        return logger;
    }

    /** @param logger The logger to set. */
    public void setLogger(final SOSLogger logger) {
        this.logger = logger;
    }

    /** @return Returns the session. */
    public Session getSession() {
        return session;
    }

    /** @param session The session to set. */
    public void setSession(final Session session) {
        this.session = session;
    }

    /** @return Returns the timeout. */
    public int getTimeout() {
        return timeout;
    }

    /** @param timeout The timeout to set. */
    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }
}
