package sos.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import sos.util.NullBufferedWriter;
import sos.util.SOSClassUtil;
import sos.util.SOSDate;
import sos.util.SOSLogger;
import sos.util.SOSStandardLogger;

/** help class for processing mime message
 *
 * @version $Id: SOSMimeMessage.java 18745 2013-01-09 21:16:22Z kb $ */

public class SOSMimeMessage {

    /** attachment list of the current message */
    Vector<SOSMailAttachment> sosMailAttachmentList = new Vector<SOSMailAttachment>();
    // Vector sosMailAttachmentList = new Vector();

    private MimeMessage mimeMessage = null;
    private String localDateFormat = "dd.MM.yyyy HH:mm:ss";
    private String sentDateAsString;
    private String from;
    private String fromName;
    private String fromAddress;
    private SOSLogger logger = null;
    protected ByteArrayOutputStream raw_email_byte_stream = null;
    private final String queuePattern = "yyyy-MM-dd.HHmmss.S";
    private final String queuePraefix = "sos.";
    protected String queueDir = "";
    private String dumpedFileName = "";

    /** @param message */
    public SOSMimeMessage(final Message message) throws Exception {
        this((MimeMessage) message);
        setDefaultLogger();
    }

    /** populate the sosMimeMessage object
     *
     *
     * @param message */
    public SOSMimeMessage(final MimeMessage mimeMessage) throws Exception {
        this.mimeMessage = mimeMessage;
        setSentDateAsString();
        setFrom();
        setFromName();
        setFromAddress();
        // TODO resolve attachment if needed only, not in advance

        processAttachment(this.mimeMessage);
        setDefaultLogger();
    }

    /**
	 *
	 */
    public SOSMimeMessage(final Message message, final SOSLogger logger) throws Exception {
        this((MimeMessage) message);
        this.logger = logger;
    }

    /** populate the sosMimeMessage object
     *
     *
     * @param message */
    public SOSMimeMessage(final MimeMessage mimeMessage, final SOSLogger logger) throws Exception {
        this(mimeMessage);
        this.logger = logger;
    }

    /** @param message
     * @param logger
     * @throws Exception */
    public MimeMessage getMessage() {
        return mimeMessage;
    }

    /** @throws Exception */
    private void setDefaultLogger() throws Exception {
        if (logger == null)
            logger = new SOSStandardLogger(new NullBufferedWriter(new OutputStreamWriter(System.out)), SOSStandardLogger.DEBUG1);
        return;
    }

    /** returns the personal name of the from attribute
     *
     * @return the personal name
     * @throws Exception */
    public String getFrom() throws Exception {
        return from;
    } // gerFrom

    /** sets the personal name of the from attribute
     *
     * @throws Exception */
    private final void setFrom() throws Exception {
        from = mimeMessage.getFrom()[0].toString();
    } // gerFrom

    /** sets the personal name of the from attribute
     *
     * @throws Exception */
    public void setFromName() throws Exception {
        fromName = ((InternetAddress) mimeMessage.getFrom()[0]).getPersonal();
    } // gerFromName

    /** returns the personal name of the from attribute
     *
     * @param message
     * @return the personal name
     * @throws Exception */
    public String getFromName() throws Exception {
        return fromName;
    } // gerFromName

    /** sets the email address
     *
     * @throws Exception */
    private void setFromAddress() throws Exception {
        fromAddress = ((InternetAddress) mimeMessage.getFrom()[0]).getAddress();
    } // setrFromAddress

    /** returns the email address
     *
     * @return the the email address
     * @throws Exception */
    public String getFromAddress() throws Exception {
        return fromAddress;
    } // gerFromAddress

    /** @return
     * @throws Exception */
    public String getSubject() throws Exception {
        String subject = "";
        if (mimeMessage.getSubject() != null)
            subject = MimeUtility.decodeText(mimeMessage.getSubject());
        return subject;
    }

    /** @param message
     * @return
     * @throws Exception */
    public String getSentDateAsString(final String dateFormat) throws Exception {
        return SOSDate.getDateAsString(mimeMessage.getSentDate(), dateFormat);
    }

    /** @return
     * @throws Exception */
    private final void setSentDateAsString() throws Exception {
        if (mimeMessage.getSentDate() != null)
            sentDateAsString = SOSDate.getDateAsString(mimeMessage.getSentDate(), localDateFormat);
    }

    /** @return
     * @throws Exception
     * @throws MessagingException
     * @throws Exception */
    public String getSentDateAsString() {
        return sentDateAsString;
    }

    /** delete the message.
     *
     * @throws MessagingException */
    public void deleteMessage() throws Exception {
        mimeMessage.setFlag(Flags.Flag.DELETED, true);
        logger.debug5("[" + mimeMessage.getMessageID() + "] marked for delete.");
    }

    // Set the specified flag on this message to the specified value.

    /** @param message
     * @param dateFormat
     * @return
     * @throws MessagingException
     * @throws Exception */
    public Date getSentDate() throws MessagingException {
        return mimeMessage.getSentDate();
    }

    public int getMessageNumber() {
        return mimeMessage.getMessageNumber();
    }

    public String getMessageId() throws MessagingException {
        return mimeMessage.getMessageID().replaceAll("(^<?)|(>?$)", "");
    }

    /** @param recipientType sample: to,cc,bcc
     * @return
     * @throws Exception */
    public List<String> getRecipientAddress(String recipientType) throws Exception {
        List<String> recipientAddressList = new Vector<String>();
        InternetAddress[] internetAddress = null;

        recipientType = recipientType.toUpperCase();

        if (recipientType.equals("TO")) {
            internetAddress = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.TO);
        } else if (recipientType.equals("CC")) {
            internetAddress = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.CC);
        } else if (recipientType.equals("BCC")) {
            internetAddress = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.BCC);
        } else {
            throw new Exception("Invalid recipient type!!");
        }
        if (internetAddress != null) {
            for (InternetAddress internetAddres : internetAddress) {
                if (internetAddres.getAddress() != null) {
                    recipientAddressList.add(MimeUtility.decodeText(internetAddres.getAddress()));
                }
            }// for
        }
        return recipientAddressList;
    }

    public String getLocalDateFormat() {
        return localDateFormat;
    }

    public void setLocalDateFormat(final String localDateFormat) throws Exception {
        this.localDateFormat = localDateFormat;
        setSentDateAsString();
    }

    public List<String> getBodyText() throws MessagingException, IOException {
        return getBodyText(mimeMessage);
    }

    /** @param part
     * @return
     * @throws MessagingException
     * @throws IOException */
    private List<String> getBodyText(final Part part) throws MessagingException, IOException {

        Object content = part.getContent();
        String disposition = part.getDisposition();
        List<String> textContent = new Vector<String>();

        if (content instanceof String && disposition != null && !disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
            textContent.addAll(toList((String) content));
            return toList((String) content);
        } else if (content instanceof Multipart) {
            int count = ((Multipart) content).getCount();
            for (int i = 0; i < count; i++) {

                BodyPart bodyPart = ((Multipart) content).getBodyPart(i);
                textContent.addAll(getBodyText(bodyPart));
            } // for
            return textContent;
        } else {
            return new Vector<String>();
        }
    }// getBodyText

    /** @return
     * @throws MessagingException
     * @throws Exception */
    public String getPlainTextBody() throws Exception {
        Object content = mimeMessage.getContent();

        if (mimeMessage.isMimeType("text/plain")) {
            return (String) content;

        } else if (mimeMessage.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) mimeMessage.getContent();
            int numParts = mp.getCount();

            for (int i = 0; i < numParts; ++i) {

                if (mp.getBodyPart(i).isMimeType("text/plain")) {
                    return (String) mp.getBodyPart(i).getContent();

                } else if (mp.getBodyPart(i).isMimeType("multipart/*")) {
                    MimeMultipart mmp = (MimeMultipart) mp.getBodyPart(i).getContent();
                    int numBodyParts = mmp.getCount();

                    for (int j = 0; j < numBodyParts; ++j) {
                        if (mmp.getBodyPart(j).isMimeType("text/plain")) {
                            return (String) mmp.getBodyPart(j).getContent();
                        }
                    }
                }
            }

            return "";

        } else { // unknown content type
            return "";
        }
    }// getPlainTextBody()

    /** bounced message has an empty return-path and multipart/report header
     *
     * @param message
     * @return
     * @throws Exception */
    public boolean isBounce() throws Exception {
        boolean emptyReturnPath = false;
        if (mimeMessage.getHeader("Return-Path") != null) {
            emptyReturnPath = mimeMessage.getHeader("Return-Path")[0].matches("(.*)(\\s*\\<\\s*\\>\\s*)(.*)");
        }
        return mimeMessage.isMimeType("multipart/report") && emptyReturnPath;
    }

    /** returns comma separated string
     * 
     * @param array
     * @return string */
    public static String arrayToString(final String[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1)
                sb.append(",");
        }
        return sb.toString();
    }

    /** @param session
     * @param messageContent
     * @throws Exception */
    public SOSMimeMessage getAttachedSosMimeMessage(final Session session, final byte[] messageContent) throws Exception {
        if (messageContent == null)
            throw new Exception("messageContent has null value!!");
        InputStream messageInputStream = new ByteArrayInputStream(messageContent);
        return new SOSMimeMessage(new MimeMessage(session, messageInputStream), this.getLogger());
    }

    /** Returns a list of all sosMailAttachments in the message.
     *
     * @return the sosMailAttachment in the message
     * @throws Exception */
    public Vector<SOSMailAttachment> getSosMailAttachments() throws Exception {
        return sosMailAttachmentList;
    }

    /** Returns the number of attachments in the message.
     *
     * @return the number of attachments in the message
     * @throws Exception */
    public int getSosMailAttachmentsCount() throws Exception {
        return sosMailAttachmentList.size();
    }

    /** @param in
     * @return
     * @throws Exception */
    private byte[] getByteArray(final InputStream in) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[8192];
            int count = 0;
            while ((count = in.read(buffer)) >= 0)
                bos.write(buffer, 0, count);
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (Exception e) {
            }
            try {
                if (bos != null)
                    bos.close();
            } catch (Exception e) {
            }
        }
        return bos.toByteArray();
    }// getByteArray

    /** @param part
     * @throws Exception */
    public void processAttachment(final Part part) throws Exception {
        String extension = "";
        boolean hasAttachment = false;
        String fileName = null;

        if (part.isMimeType("text/plain")) {
            // body
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int count = multipart.getCount();
            for (int i = 0; i < count; i++) {
                processAttachment(multipart.getBodyPart(i));
            }
        } else if (part.isMimeType("message/rfc822")) { // attachment is a
                                                        // message
            hasAttachment = true;
            extension = ".eml";
        } else if (part.isMimeType("text/html")) { // HTML?
            extension = ".html";
            hasAttachment = true;
        } else { // Content-Type: message/delivery-status on bounce message
            hasAttachment = true;
        }

        if (hasAttachment) {
            SOSMailAttachment sosMailAttachment = new SOSMailAttachment();
            String disposition = part.getDisposition();
            //
            if (disposition == null || disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
                fileName = part.getFileName();
            }

            if (fileName != null) {
                sosMailAttachment.setFilename(MimeUtility.decodeText(part.getFileName()));
            } else {
                sosMailAttachment.setFilename(fileName);
            }

            sosMailAttachment.setContentType(part.getContentType());
            sosMailAttachment.setContent(getByteArray(part.getInputStream()));
            sosMailAttachment.setFileExtension(extension);
            sosMailAttachmentList.add(sosMailAttachment);
        } // hasAttachment
    } // processAttachment

    /** @param string
     * @return */
    private static List<String> toList(final String string) {
        List<String> list = new Vector<String>();
        list.add(string);
        return list;
    } //

    /** Saves the byte array to a file.
     * 
     * @param file
     * @param input
     * @throws Exception */
    public void saveFile(File file, final byte[] input) throws Exception {

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {

            for (int i = 0; file.exists(); i++) {
                file = new File(file.getAbsolutePath() + i);
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);

            bos.write(input);
        } finally {
            if (bos != null)
                try {
                    bos.flush();
                } catch (Exception e) {
                }
            if (bos != null)
                try {
                    bos.close();
                    bos = null;
                } catch (Exception e) {
                }
        }
    }// saveFile

    /** Saves the specified attachment.
     *
     * @param sosMailAttachment the attachment to be saved.
     * @param targetFolderName
     * @throws Exception */
    public void saveAttachment(final SOSMailAttachment sosMailAttachment, final String targetFolderName) throws Exception {

        String attachmentFileName = null;

        logger.debug9("Calling " + SOSClassUtil.getMethodName());

        if (!new File(targetFolderName).exists())
            throw new Exception("File [" + targetFolderName + "] does not exists!");

        attachmentFileName = sosMailAttachment.getFilename();

        if (attachmentFileName == null)
            attachmentFileName = "attach";

        attachmentFileName = targetFolderName + File.separator + attachmentFileName;

        saveFile(new File(attachmentFileName), sosMailAttachment.getContent());

        logger.debug5(".. saving attachment file [" + sosMailAttachment.getFilename() + " successfully done.");

    }// saveAttachment

    /** @param targetFolderName
     * @throws Exception */
    public void saveAttachments(final String targetFolderName) throws Exception {
        String attachmentFileName = null;
        File fileToSave;
        SOSMailAttachment attachment;

        logger.debug9("Calling " + SOSClassUtil.getMethodName());
        if (!new File(targetFolderName).exists())
            throw new Exception("File [" + targetFolderName + "] does not exists!!");

        Iterator<SOSMailAttachment> it = sosMailAttachmentList.iterator();

        for (; it.hasNext();) {
            attachment = it.next();
            logger.debug9(".. attachment count byte [" + attachment.getContent().length + "]");
            attachmentFileName = attachment.getFilename();
            if (attachmentFileName == null)
                attachmentFileName = "attach";
            fileToSave = new File(targetFolderName + File.separator + attachmentFileName);
            saveFile(fileToSave, attachment.getContent());
            logger.debug9(".. attachment file [" + attachment.getFilename() + "] successfully saved.");
        }
        logger.debug9(".. [" + sosMailAttachmentList.size() + "] Attachment(s) saved to " + targetFolderName);
    } // saveAttachments

    /** @return Returns the logger. */
    public SOSLogger getLogger() {
        return logger;
    }

    /** @param logger The logger to set. */
    public void setLogger(final SOSLogger logger) {
        this.logger = logger;
    }

    /** dump the current message to the spicified file
     *
     * @param file
     * @param append */
    public void dumpMessage(final File file, final boolean append) throws Exception {
        dumpFile(file, mimeMessage.getInputStream(), append);
    }

    /** Set the specified flag on this message to the specified value.
     *
     * @throws MessagingException */
    public void setFlag(final Flags.Flag flag, final boolean set) throws MessagingException {
        mimeMessage.setFlag(flag, set);
    }

    /** return the content of the current message.
     *
     * @throws MessagingException
     * @throws IOException
     * @throws MessagingException */
    public Object getContent() throws IOException, MessagingException {
        return mimeMessage.getContent();
    }

    /** dumps the input stream to the specified file
     *
     * @param file
     * @param input
     * @param append
     * @throws Exception */
    private final void dumpFile(final File file, final InputStream input, final boolean append) throws Exception {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        int b;
        try {
            fos = new FileOutputStream(file, append);
            bos = new BufferedOutputStream(fos);
            bis = new BufferedInputStream(input);
            while ((b = bis.read()) != -1) {
                bos.write(b);
            }

        } finally {
            if (bos != null)
                bos.flush();
            if (bos != null)
                bos.close();
            if (fos != null)
                fos.close();
            if (bis != null)
                bis.close();
        }
    }// dumpFile

    /** @return
     * @throws Exception */
    public Enumeration<Header> getHeaders() throws Exception {

        return mimeMessage.getAllHeaders();
    }

    /** @return all message headers as a string
     * @throws IOException
     * @throws MessagingException */
    public String dumpHeaders() throws IOException, MessagingException {
        StringBuffer sb = new StringBuffer();
        Header header = null;
        for (Enumeration<Header> e = mimeMessage.getAllHeaders(); e.hasMoreElements();) {
            header = e.nextElement();
            sb.append("\n").append(header.getName()).append(": ").append(header.getValue());
        }
        return sb.toString();

    }

    /** @param file the file to be opened for writing
     * @param append
     * @throws Exception */
    public void dumpMessageToFile(final File file, final boolean withAttachment, final boolean append) throws Exception {

        FileOutputStream out = null;
        File tmpFile, targetFile;
        try {
            // TODO Implement parameter atomic_suffix and atomic_prefix to
            // protect the file against file watchers
            tmpFile = new File(file.getAbsolutePath() + "~");
            out = new FileOutputStream(file, append);

            out.write(dumpMessage(withAttachment));

            targetFile = new File(file.getAbsolutePath().replaceFirst("~$", ""));
            tmpFile.delete();

            tmpFile.renameTo(targetFile);

        } catch (Exception e) {
            throw new Exception(SOSClassUtil.getMethodName() + ": An error occurred on dumping: " + e.toString());
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (Exception e) {
                }
        }
    } // dumpMessageToFile

    /** @return Returns the mimeMessage. */
    public final MimeMessage getMimeMessage() {
        return mimeMessage;
    }

    /** @return
     * @throws Exception */
    private ByteArrayOutputStream messageRemoveAttachments() throws Exception {
        ByteArrayOutputStream raw_email_byte_stream_without_attachment = new ByteArrayOutputStream();

        Object mmpo = mimeMessage.getContent();
        if (mmpo instanceof MimeMultipart) {
            MimeMultipart mmp = (MimeMultipart) mmpo;

            if (mimeMessage.isMimeType("text/plain")) {
            } else if (mimeMessage.isMimeType("multipart/*")) {
                mmp = (MimeMultipart) mimeMessage.getContent();
                for (int i = 1; i < mmp.getCount(); i++) {
                    @SuppressWarnings("unused")
                    BodyPart part = mmp.getBodyPart(i);
                    mmp.removeBodyPart(i);
                    i--;
                }
            }
            mimeMessage.setContent(mmp);
        }// if
        mimeMessage.writeTo(raw_email_byte_stream_without_attachment);
        return raw_email_byte_stream_without_attachment;

    }

    /** @return
     * @throws Exception */
    public String dumpMessageAsString() throws Exception {
        return dumpMessageAsString(false);
    }

    /** @param withAttachment
     * @param append
     * @throws Exception */
    public void dumpMessageToFile(final boolean withAttachment, final boolean append) throws Exception {

        Date d = new Date();
        StringBuffer bb = new StringBuffer();
        SimpleDateFormat s = new SimpleDateFormat(queuePattern);

        FieldPosition fp = new FieldPosition(0);
        StringBuffer b = s.format(d, bb, fp);

        dumpedFileName = queueDir + queuePraefix + b + ".email";

        File file = new File(dumpedFileName);
        while (file.exists()) {
            b = s.format(d, bb, fp);
            dumpedFileName = queueDir + "/" + queuePraefix + b + ".email";
            file = new File(dumpedFileName);
        }
        dumpMessageToFile(file, withAttachment, append);
    }

    /** @param filename
     * @param withAttachment
     * @param append
     * @throws Exception */
    public void dumpMessageToFile(final String filename, final boolean withAttachment, final boolean append) throws Exception {
        dumpMessageToFile(new File(filename), withAttachment, append);
    }

    /** @param withAttachment
     * @return
     * @throws Exception */
    public String dumpMessageAsString(final boolean withAttachment) throws Exception {
        byte[] bytes;
        ByteArrayOutputStream raw_email_byte_stream_without_attachment = null;

        if (!withAttachment) {
            raw_email_byte_stream_without_attachment = messageRemoveAttachments();
        }

        raw_email_byte_stream = new ByteArrayOutputStream();
        mimeMessage.writeTo(raw_email_byte_stream);

        if (withAttachment || raw_email_byte_stream_without_attachment == null) {
            bytes = raw_email_byte_stream.toByteArray();
        } else {
            bytes = raw_email_byte_stream_without_attachment.toByteArray();
        }
        String s = new String(bytes);

        return s;

    }

    public byte[] dumpMessage() throws Exception {
        return dumpMessage(true);
    }

    /** @param withAttachment
     * @return
     * @throws Exception */
    public byte[] dumpMessage(final boolean withAttachment) throws Exception {
        byte[] bytes;
        ByteArrayOutputStream raw_email_byte_stream_without_attachment = null;

        if (!withAttachment) {
            raw_email_byte_stream_without_attachment = messageRemoveAttachments();
        }

        raw_email_byte_stream = new ByteArrayOutputStream();
        mimeMessage.writeTo(raw_email_byte_stream);

        if (withAttachment || raw_email_byte_stream_without_attachment == null) {
            bytes = raw_email_byte_stream.toByteArray();
        } else {
            bytes = raw_email_byte_stream_without_attachment.toByteArray();
        }
        return bytes;
    }

    /** @return Returns the queueDir. */
    public String getQueueDir() {
        return queueDir;
    }

    /** @param queueDir The queueDir to set. */
    public void setQueueDir(String queueDir) {
        if (!queueDir.endsWith("/")) {
            queueDir += "/";
        }

        /*
         * if ( !queueDir.endsWith( File.separator)) { queueDir +=
         * File.separator; }
         */
        this.queueDir = queueDir;
    }

    /** increments the specified header if and only if its value is a number.
     *
     * @param headerName
     * @return integer value represents the current header value, otherwise -1
     * @throws MessagingException */
    public int incrementHeader(final String headerName) throws MessagingException {
        Header header = null;
        int value = -1;
        for (Enumeration<Header> e = mimeMessage.getAllHeaders(); e.hasMoreElements();) {
            header = e.nextElement();
            if (header.getName().equalsIgnoreCase(headerName)) {
                if (header.getValue() != null && header.getValue().trim().matches("^[0-9]+$")) {
                    value += Integer.parseInt(header.getValue());
                    mimeMessage.setHeader(headerName, "" + value);
                }// if
            }// if
        }// for
        return value;
    }// incrementHeader

    /** @param headerName
     * @param HeaderValue
     * @throws MessagingException */
    public void setHeader(final String headerName, final String HeaderValue) throws MessagingException {
        mimeMessage.setHeader(headerName, HeaderValue);
    }

    /** returns the value of the specified header if header exists otherwise
     * null.
     *
     * @param headerName
     * @return String represents the value of the specified header otherwise
     *         null.
     * @throws MessagingException */
    public String getHeaderValue(final String headerName) throws Exception {
        Header header = null;
        for (Enumeration<Header> e = mimeMessage.getAllHeaders(); e.hasMoreElements();) {
            header = e.nextElement();
            if (header.getName().equalsIgnoreCase(headerName)) {
                return header.getValue();
            }// if
        }// for
        return null;
    }// getHeaderValue

    /** @return the dumpedFileName. */
    public String getDumpedFileName() {
        return dumpedFileName;
    }

    /** @param dumpedFileName The dumpedFileName to set. */
    public void setDumpedFileName(final String dumpedFileName) {
        this.dumpedFileName = dumpedFileName;
    }

}
