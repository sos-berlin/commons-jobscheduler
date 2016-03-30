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

/** @author KB */
public class SOSMailReceiver {

    protected SOSMailAuthenticator authenticator = null;
    protected int timeout = 5000;
    private Store store;
    private Folder folder = null;
    private String protocol = "POP3";
    private String folderName = "INBOX";
    private final String host;
    private final String port;
    private final String user;
    private Session session;
    private final String password;
    public int READ_ONLY = Folder.READ_ONLY;
    public int READ_WRITE = Folder.READ_WRITE;
    SOSLogger logger = null;

    public SOSMailReceiver(final String host, final String port, final String user, final String password) throws Exception {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        createSession();
        setStubLogger();
    }

    private void setStubLogger() throws Exception {
        if (logger == null) {
            logger = new SOSStandardLogger(new NullBufferedWriter(new OutputStreamWriter(System.out)), SOSStandardLogger.DEBUG1);
        }
        return;
    }

    public Session createSession() throws Exception {
        Properties props = System.getProperties();
        props.put("mail.host", host);
        props.put("mail.port", port);
        if (!"".equals(user) && !"".equals(password)) {
            authenticator = new SOSMailAuthenticator(user, password);
            session = Session.getInstance(props, authenticator);
        } else {
            authenticator = new SOSMailAuthenticator(user, password);
            session = Session.getInstance(props, authenticator);
        }
        return session;
    }

    public void connect(final String pstrMailProtocolName) throws Exception {
        String strMailProtocolName = pstrMailProtocolName.toLowerCase();
        session.getProperties().put("mail." + strMailProtocolName + ".timeout", String.valueOf(timeout));
        store = session.getStore(strMailProtocolName);
        store.connect(host, user, password);
        logger.debug5("..connected to host [" + host + ":" + port + "] successfully done.");
    }

    /** opens the given folder
     * 
     * @param folderName1
     * @param mode The open mode of this folder. The open mode is
     *            Folder.READ_ONLY, Folder.READ_WRITE, or -1 if not known.
     * @return folder
     * @throws Exception */
    public Folder openFolder(final String folderName1, final int mode) throws Exception {
        folder = store.getFolder(folderName1);
        if (folder == null) {
            throw new JobSchedulerException("An error occured opening [" + folderName1 + "]");
        }
        folder.open(mode);
        folderName = folderName1;
        return folder;
    }

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
        folderName = folder.getName();
        folder.open(mode);
        return folder;
    }

    /** @param expunge expunges all deleted messages if this flag is true.
     * @throws MessagingException */
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

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(final String folderName) {
        this.folderName = folderName;
    }

    public SOSLogger getLogger() {
        return logger;
    }

    public void setLogger(final SOSLogger logger) {
        this.logger = logger;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(final Session session) {
        this.session = session;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

}