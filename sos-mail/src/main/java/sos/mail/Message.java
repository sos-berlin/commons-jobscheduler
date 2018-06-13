package sos.mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;

public class Message {

    private MimeMessage msg;
    private final Properties properties = System.getProperties();
    private Session session = null;
    private byte[] body;
    private final List<BodyPart> attachments = new LinkedList<BodyPart>();
    private final List<InputStream> fileInputStreams = new ArrayList<InputStream>();
    private String smtpUserName = "";
    private String smtpPassword = "";
    @SuppressWarnings("unused")
    private String encoding;
    private String contentType;
    private boolean built = false;
    static final int CURRENT_VERSION = 2;

    public Message() {
        msg = new MimeMessage(getSession());
    }

    abstract class MyDataSource implements DataSource {

        final String name;
        final String content_type;

        public MyDataSource(final File new_filename, final String content_type) {
            name = new_filename.getName();
            this.content_type = content_type;
        }

        @Override
        public String getContentType() {
            return content_type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public OutputStream getOutputStream() {
            throw new RuntimeException(getClass().getName() + " hat keinen OutputStream");
        }
    }

    class ByteArrayDataSource extends MyDataSource {

        final byte[] byte_array;

        public ByteArrayDataSource(final byte[] byte_array, final File new_filename, final String content_type) {
            super(new_filename, content_type);
            this.byte_array = byte_array;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(byte_array);
        }
    }

    class FileDataSource extends MyDataSource {

        final File file;

        public FileDataSource(final File file, final File new_filename, final String content_type) {
            super(new_filename, content_type);
            this.file = file;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            FileInputStream f = new FileInputStream(file);
            fileInputStreams.add(f);
            return f;
        }
    }

    public class MyAuthenticator extends Authenticator {

        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(smtpUserName, smtpPassword);
        }
    }

    public void close() throws Exception {
        Exception exception = null;
        for (InputStream stream : fileInputStreams) {
            try {
                stream.close();
            } catch (Exception x) {
                if (exception == null) {
                    exception = x;
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    public void needVersion(final int version) throws Exception {
        if (version > CURRENT_VERSION) {
            throw new Exception("Class sos.mail.Message (sos.mail.jar) is not up to date");
        }
    }

    private Session getSession() {
        if (session == null) {
            session = Session.getInstance(properties, new MyAuthenticator());
        }
        return session;
    }

    public void set(final String what, final byte[] value) throws AddressException, MessagingException, UnsupportedEncodingException {
        if ("smtp".equals(what)) {
            properties.put("mail.smtp.host", new String(value, "iso8859-1"));
        } else if ("from".equals(what)) {
            InternetAddress[] addr = InternetAddress.parse(new String(value, "iso8859-1"));
            if (addr.length != 0) {
                msg.setFrom(addr[0]);
            }
        } else if ("reply-to".equals(what)) {
            msg.setReplyTo(InternetAddress.parse(new String(value, "iso8859-1")));
        } else if ("to".equals(what)) {
            msg.setRecipients(RecipientType.TO, InternetAddress.parse(new String(value, "iso8859-1")));
        } else if ("cc".equals(what)) {
            msg.setRecipients(RecipientType.CC, InternetAddress.parse(new String(value, "iso8859-1")));
        } else if ("bcc".equals(what)) {
            msg.setRecipients(RecipientType.BCC, InternetAddress.parse(new String(value, "iso8859-1")));
        } else if ("subject".equals(what)) {
            msg.setSubject(new String(value, "iso8859-1"));
        } else if ("body".equals(what)) {
            body = value;
        } else if ("content_type".equals(what)) {
            contentType = new String(value, "iso8859-1");
        } else if ("encoding".equals(what)) {
            encoding = new String(value, "iso8859-1");
        } else if ("send_rfc822".equals(what)) {
            msg = new MimeMessage(getSession(), new ByteArrayInputStream(value));
            send2();
        } else if ("debug".equals(what)) {
            getSession().setDebug("1".equals(new String(value, "iso8859-1")));
        } else {
            throw new RuntimeException("sos.mail.Message.set: what");
        }
    }

    public void setProperty(final String name, final String value) {
        if ("mail.smtp.user".equals(name)) {
            smtpUserName = value;
        } else if ("mail.smtp.password".equals(name)) {
            smtpPassword = value;
        } else {
            properties.put(name, value);
        }
    }

    private String stringFromAddresses(final Address[] addresses) {
        if (addresses == null) {
            return "";
        }
        String result = "";
        for (Address addresse : addresses) {
            if (!"".equals(result)) {
                result = new String(result + ", ");
            }
            result = new String(result + addresse);
        }
        return result;
    }

    public String get(final String what) throws Exception {
        if ("smtp".equals(what)) {
            return (String) properties.get("mail.smtp.host");
        } else if ("from".equals(what)) {
            return stringFromAddresses(msg.getFrom());
        } else if ("reply-to".equals(what)) {
            return stringFromAddresses(msg.getReplyTo());
        } else if ("to".equals(what)) {
            return stringFromAddresses(msg.getRecipients(RecipientType.TO));
        } else if ("cc".equals(what)) {
            return stringFromAddresses(msg.getRecipients(RecipientType.CC));
        } else if ("bcc".equals(what)) {
            return stringFromAddresses(msg.getRecipients(RecipientType.BCC));
        } else if ("subject".equals(what)) {
            return msg.getSubject();
        } else if ("body".equals(what)) {
            return body == null ? "" : new String(body, "iso8859-1");
        } else if ("rfc822_text".equals(what)) {
            build();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            msg.writeTo(os);
            return os.toString();
        } else {
            throw new RuntimeException("sos.mail.Message.get: what=\"" + what + "\" ist unbekannt");
        }
    }

    public void addHeaderField(final String name, final String value) throws MessagingException {
        msg.setHeader(name, value);
    }

    public void addFile(final String realFilename, String newFilename, String contentType, final String encoding) throws Exception {
        if (newFilename == null || newFilename.isEmpty()) {
            newFilename = realFilename;
        }
        if (contentType == null || contentType.isEmpty()) {
            contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(newFilename);
        }
        MimeBodyPart attachment = new MimeBodyPart();
        DataSource data_source = new FileDataSource(new File(realFilename), new File(newFilename), contentType);
        DataHandler data_handler = new DataHandler(data_source);
        attachment.setDataHandler(data_handler);
        attachment.setFileName(data_handler.getName());
        attachments.add(attachment);
    }

    public void addAttachment(final byte[] data, final String filename, String contentType, final String encoding) throws MessagingException {
        if (contentType.isEmpty()) {
            contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(filename);
        }
        MimeBodyPart attachment = new MimeBodyPart();
        DataSource data_source = new ByteArrayDataSource(data, new File(filename), contentType);
        DataHandler data_handler = new DataHandler(data_source);
        attachment.setDataHandler(data_handler);
        attachment.setFileName(data_handler.getName());
        attachments.add(attachment);
    }

    public void send() throws Exception {
        build();
        send2();
    }

    public void build() throws Exception {
        if (built) {
            return;
        }
        msg.setSentDate(new Date());
        if (contentType == null || "".equals(contentType)) {
            contentType = "text/plain";
        }
        if (attachments.isEmpty()) {
            setBodyIn(msg);
        } else {
            MimeMultipart multipart = new MimeMultipart();
            MimeBodyPart b = new MimeBodyPart();
            setBodyIn(b);
            multipart.addBodyPart(b);
            for (BodyPart bodyPart : attachments) {
                multipart.addBodyPart(bodyPart);
            }
            msg.setContent(multipart);
        }
        built = true;
    }

    private void setBodyIn(final MimePart bodyPart) throws Exception {
        bodyPart.setContent(new String(body, "iso8859-1"), contentType);
    }

    protected void send2() throws MessagingException, NoSuchProviderException {
        if (!smtpUserName.isEmpty()) {
            properties.put("mail.smtp.auth", "true");
        }
        Transport.send(msg);
    }

}