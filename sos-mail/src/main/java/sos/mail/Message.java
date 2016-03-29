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
import java.util.ListIterator;
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

    private MimeMessage _msg;
    private final Properties _properties = System.getProperties();
    private Session _session = null;
    private byte[] _body;
    private final List _attachments = new LinkedList();
    private final ArrayList file_input_streams = new ArrayList();
    private String _smtp_user_name = "";
    private String _smtp_password = "";
    private String _encoding;
    private String _content_type;
    private boolean _built = false;
    static final int current_version = 2;

    abstract class My_data_source implements DataSource {

        final String name;
        final String content_type;

        public My_data_source(final File new_filename, final String content_type) {
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

    class Byte_array_data_source extends My_data_source {

        final byte[] byte_array;

        public Byte_array_data_source(final byte[] byte_array, final File new_filename, final String content_type) {
            super(new_filename, content_type);
            this.byte_array = byte_array;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(byte_array);
        }
    }

    class File_data_source extends My_data_source {

        final File file;

        public File_data_source(final File file, final File new_filename, final String content_type) {
            super(new_filename, content_type);
            this.file = file;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            FileInputStream f = new FileInputStream(file);
            file_input_streams.add(f);
            return f;
        }
    }

    public class My_authenticator extends Authenticator {

        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(_smtp_user_name, _smtp_password);
        }
    }

    public Message() {
        _msg = new MimeMessage(get_session());
    }

    public void close() throws Exception {
        Exception exception = null;
        for (int i = 0; i < file_input_streams.size(); i++) {
            try {
                ((FileInputStream) file_input_streams.get(i)).close();
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

    public void need_version(final int version) throws Exception {
        if (version > current_version) {
            throw new Exception("Class sos.mail.Message (sos.mail.jar) is not up to date");
        }
    }

    private Session get_session() {
        if (_session == null) {
            _session = Session.getInstance(_properties, new My_authenticator());
        }
        return _session;
    }

    public void set(final String what, final byte[] value) throws AddressException, MessagingException, UnsupportedEncodingException {
        if ("smtp".equals(what)) {
            _properties.put("mail.smtp.host", new String(value, "iso8859-1"));
        } else if ("from".equals(what)) {
            InternetAddress[] addr = InternetAddress.parse(new String(value, "iso8859-1"));
            if (addr.length != 0) {
                _msg.setFrom(addr[0]);
            }
        } else if ("reply-to".equals(what)) {
            _msg.setReplyTo(InternetAddress.parse(new String(value, "iso8859-1")));
        } else if ("to".equals(what)) {
            _msg.setRecipients(RecipientType.TO, InternetAddress.parse(new String(value, "iso8859-1")));
        } else if ("cc".equals(what)) {
            _msg.setRecipients(RecipientType.CC, InternetAddress.parse(new String(value, "iso8859-1")));
        } else if ("bcc".equals(what)) {
            _msg.setRecipients(RecipientType.BCC, InternetAddress.parse(new String(value, "iso8859-1")));
        } else if ("subject".equals(what)) {
            _msg.setSubject(new String(value, "iso8859-1"));
        } else if ("body".equals(what)) {
            _body = value;
        } else if ("content_type".equals(what)) {
            _content_type = new String(value, "iso8859-1");
        } else if ("encoding".equals(what)) {
            _encoding = new String(value, "iso8859-1");
        } else if ("send_rfc822".equals(what)) {
            _msg = new MimeMessage(get_session(), new ByteArrayInputStream(value));
            send2();
        } else if ("debug".equals(what)) {
            get_session().setDebug(new String(value, "iso8859-1").equals("1"));
        } else {
            throw new RuntimeException("sos.mail.Message.set: what");
        }
    }

    public void set_property(final String name, final String value) {
        if ("mail.smtp.user".equals(name)) {
            _smtp_user_name = value;
        } else if ("mail.smtp.password".equals(name)) {
            _smtp_password = value;
        } else {
            _properties.put(name, value);
        }
    }

    private String string_from_addresses(final Address[] addresses) {
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
            return (String) _properties.get("mail.smtp.host");
        } else if ("from".equals(what)) {
            return string_from_addresses(_msg.getFrom());
        } else if ("reply-to".equals(what)) {
            return string_from_addresses(_msg.getReplyTo());
        } else if ("to".equals(what)) {
            return string_from_addresses(_msg.getRecipients(RecipientType.TO));
        } else if ("cc".equals(what)) {
            return string_from_addresses(_msg.getRecipients(RecipientType.CC));
        } else if ("bcc".equals(what)) {
            return string_from_addresses(_msg.getRecipients(RecipientType.BCC));
        } else if ("subject".equals(what)) {
            return _msg.getSubject();
        } else if ("body".equals(what)) {
            return _body == null ? "" : new String(_body, "iso8859-1");
        } else if ("rfc822_text".equals(what)) {
            build();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            _msg.writeTo(os);
            return os.toString();
        } else {
            throw new RuntimeException("sos.mail.Message.get: what=\"" + what + "\" ist unbekannt");
        }
    }

    public void add_header_field(final String name, final String value) throws MessagingException {
        _msg.setHeader(name, value);
    }

    public void add_file(final String real_filename, String new_filename, String content_type, final String encoding) throws Exception {
        if (new_filename == null || new_filename.isEmpty()) {
            new_filename = real_filename;
        }
        if (content_type == null || content_type.isEmpty()) {
            content_type = FileTypeMap.getDefaultFileTypeMap().getContentType(new_filename);
        }
        MimeBodyPart attachment = new MimeBodyPart();
        DataSource data_source = new File_data_source(new File(real_filename), new File(new_filename), content_type);
        DataHandler data_handler = new DataHandler(data_source);
        attachment.setDataHandler(data_handler);
        attachment.setFileName(data_handler.getName());
        _attachments.add(attachment);
    }

    public void add_attachment(final byte[] data, final String filename, String content_type, final String encoding) throws MessagingException {
        if (content_type.isEmpty()) {
            content_type = FileTypeMap.getDefaultFileTypeMap().getContentType(filename);
        }
        MimeBodyPart attachment = new MimeBodyPart();
        DataSource data_source = new Byte_array_data_source(data, new File(filename), content_type);
        DataHandler data_handler = new DataHandler(data_source);
        attachment.setDataHandler(data_handler);
        attachment.setFileName(data_handler.getName());
        _attachments.add(attachment);
    }

    public void send() throws Exception {
        build();
        send2();
    }

    public void build() throws Exception {
        if (_built) {
            return;
        }
        _msg.setSentDate(new Date());
        if (_content_type == null || "".equals(_content_type)) {
            _content_type = "text/plain";
        }
        if (_attachments.isEmpty()) {
            set_body_in(_msg);
        } else {
            MimeMultipart multipart = new MimeMultipart();
            MimeBodyPart b = new MimeBodyPart();
            set_body_in(b);
            multipart.addBodyPart(b);
            ListIterator i = _attachments.listIterator();
            while (i.hasNext()) {
                multipart.addBodyPart((BodyPart) i.next());
            }
            _msg.setContent(multipart);
        }
        _built = true;
    }

    private void set_body_in(final MimePart body_part) throws Exception {
        body_part.setContent(new String(_body, "iso8859-1"), _content_type);
    }

    protected void send2() throws MessagingException, NoSuchProviderException {
        if (!_smtp_user_name.isEmpty()) {
            _properties.put("mail.smtp.auth", "true");
        }
        Transport.send(_msg);
    }

}