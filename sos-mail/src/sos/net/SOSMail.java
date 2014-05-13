package sos.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import sos.settings.SOSSettings;
import sos.util.SOSClassUtil;
import sos.util.SOSDate;
import sos.util.SOSLogger;
import sos.util.SOSStandardLogger;

import com.sos.JSHelper.Exceptions.JSNotImplementedException;
import com.sos.JSHelper.interfaces.ISOSSmtpMailOptions;

/**
 * https://blogs.oracle.com/apanicker/entry/java_code_for_smtp_server
 *
 * @version $Id: SOSMail.java 22027 2014-01-08 15:38:00Z kb $
 */

public class SOSMail {

	@SuppressWarnings("unused")
	private final String				conSVNVersion		= "$Id: SOSMail.java 22027 2014-01-08 15:38:00Z kb $";

	abstract class My_data_source implements DataSource {
		final String	name;
		final String	content_type;

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

	// --------------------------------------------------------------------------------class File_data_source

	// Eigene Klasse für Attachments, damit close müglich ist.
	class File_data_source extends My_data_source {
		final File	file;

		public File_data_source(final File file, final String content_type) {
			super(file, content_type);
			this.file = file;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			FileInputStream f = new FileInputStream(file);
			file_input_streams.add(f); // wird von Message.close() geschlossen
			return f;
		}
	}

	/** Attribut: host:
	 * - Wird im Konstruktor als Parameter übergeben.
	 * - Kann über Settings-Objekt gesetzt werden.
	 * - mit .setHost kann Host gesetzt werden. Es wird dann eine auch eine neue Message-id vergeben.
	*/
	protected String				host							= "";

	/** Attribut: port: Default=25
	 * - Wird im Konstruktor als Parameter übergeben.
	 * - Kann über Settings-Objekt gesetzt werden.
	 * - mit .setPort kann Port gesetzt werden. Es wird dann eine auch eine neue Message-id vergeben.
	*/
	protected String				port							= "25";

	/** Attribut: user:
	 * Zur Verwendung bei SMTP_Server, die eine Autentifizierung verlangen
	 * - Wird im Konstruktor als Parameter übergeben.
	 * - Kann über Settings-Objekt gesetzt werden.
	 * - mit .setUser kann User gesetzt werden. Es wird dann eine auch eine neue Message-id vergeben.
	*/

	protected String				user							= "";

	/** Attribut: password:
	 * Zur Verwendung bei SMTP_Server, die eine Autentifizierung verlangen
	 * - Wird im Konstruktor als Parameter übergeben.
	 * - Kann über Settings-Objekt gesetzt werden.
	 * - mit .setPassword kann User gesetzt werden. Es wird dann eine auch eine neue Message-id vergeben.
	*/
	protected String				password						= "";

	/** Attribut: timeout: Default=30000
	 *
	 * - Wird im Konstruktor als Parameter übergeben.
	 * - Kann über Settings-Objekt gesetzt werden. Dort werden Sekunden angegeben
	 * - mit .setTimeout kann Timeout gesetzt werden. Es wird dann eine auch eine neue Message-id vergeben.
	*/
	protected int					timeout							= 30000;

	/** Attribut: subject
	 *
	 * Kann Variable enthalten, die substituiert werden z.B. &(var).
	 * Siehe Methode substitute
	 *
	 *
	*/
	protected String				subject							= "";
	protected String				from							= "";
	protected String				fromName						= "";
	protected String				replyTo							= "";
	protected String				queueDir						= "";
	protected String				body							= "";
	protected String				alternativeBody					= "";
	protected String				language						= "de";
	protected String				dateFormat						= "dd.MM.yyyy";
	protected String				datetimeFormat					= "dd.MM.yyyy HH:mm";
	protected HashMap				dateFormats						= new HashMap();
	protected HashMap				datetimeFormats					= new HashMap();
	/** Attribut: charSet: iso-8859-1, us-ascii für Attachments. Wenn leer
	    Wird charset verwendet.
	*/
	protected String				attachmentCharset				= "iso-8859-1";
	/** Attribut: charSet: iso-8859-1, us-ascii für Body.*/
	protected String				charset							= "iso-8859-1";
	protected String				alternativeCharset				= "iso-8859-1";

	/** Mime-Type der Nachricht: text/plain, text/html etc.*/
	protected String				contentType						= "text/plain";
	protected String				alternativeContentType			= "text/html";

	/** Attribut: encoding: Codierung des Nachrichteninhalts (Quoted-Printable, Base64) */
	protected String				encoding						= "7bit";
	protected String				alternativeEncoding				= "7bit";

	/** Attribut: encoding: Codierung des Anhanges (Quoted-Printable, Base64) */
	protected String				attachmentEncoding				= "Base64";

	/** Mime-Type der Nachricht: application/octet-stream, application/pdf etc. */
	protected String				attachmentContentType			= "application/octet-stream";

	/** recipient Liste */
	protected LinkedList			toList							= new LinkedList();

	/** cc Liste */
	protected LinkedList			ccList							= new LinkedList();

	/** bcc Liste */
	protected LinkedList			bccList							= new LinkedList();

	/** attachment Liste */
	protected TreeMap				attachmentList					= new TreeMap();

	/** Muster für eMail-Texte */
	protected Properties			templates						= new Properties();

	/** sos settings object */
	protected SOSSettings			sosSettings						= null;

	/** Tabelle für Einstellungen */
	protected String				tableSettings					= "SETTINGS";

	/** Tabelle für eMail Auftrüge und Historie */
	public static String			tableMails						= "MAILS";

	/** Tabelle für eMail Anhünge */
	public static String			tableMailAttachments			= "MAIL_ATTACHMENTS";

	/** Sequenzname der IDs für die MAILS Tablle*/
	public static String			mailsSequence					= "MAILS_ID_SEQ";

	/** Applikationsname für eMail-Einstellungen */
	protected String				applicationMail					= "email";

	/** Sektionsname für eMail-Einstellungen */
	protected String				sectionMail						= "mail_server";

	/** Applikationsname für eMail-Templates in Settings */
	protected String				applicationMailTemplates		= "email_templates";

	/** Sektionsname für eMail-Templates in Settings */
	protected String				sectionMailTemplates			= "mail_templates";

	/** Applikationsname für eMail-Scripts in Settings */
	protected String				applicationMailScripts			= "email";

	/** Sektionsname für eMail-Scripts in Settings */
	protected String				sectionMailScripts				= "mail_start_scripts_factory";

	/** Applikationsname für eMail-Templates der Document Factory in Settings */
	protected String				applicationMailTemplatesFactory	= "email_templates_factory";

	/** Sektionsname für eMail-Templates der Document Factory in Settings */
	protected String				sectionMailTemplatesFactory		= "mail_templates";

	/** Email als byteArray verfügbar machen */
	private boolean					sendToOutputStream				= false;
	private byte[]					messageBytes;
	private MimeMessage				message							= null;
	private SOSMailAuthenticator	authenticator					= null;

	private final ArrayList				file_input_streams				= new ArrayList();				// Alle offenen Attachments, werden von
																									// close() geschlossen
	protected SOSLogger				sosLogger						= null;

	private ByteArrayOutputStream	raw_email_byte_stream			= null;
	private String					lastError						= "";
	private boolean					changed							= false;
	private final String					queuePattern					= "yyyy-MM-dd.HHmmss.S";
	private String					queuePraefix					= "sos.";
	private String					lastGeneratedFileName			= "";
	private String					loadedMessageId					= "";
	private boolean					messageReady					= false;
	private int						priority						= -1;
	private Session					session							= null;

	// Konstanten für Prioritüten (in Settings und MAILS Tabelle)
	public static final int			PRIORITY_HIGHEST				= 1;
	public static final int			PRIORITY_HIGH					= 2;
	public static final int			PRIORITY_NORMAL					= 3;
	public static final int			PRIORITY_LOW					= 4;
	public static final int			PRIORITY_LOWEST					= 5;

	// Depricated
	private String					filename;

	// --------------------------------------------------------------------------------Konstruktor

	/**
	 * Konstruktor
	 * @param host string Hostname oder IP-Adresse des Mail-Servers
	 * Wird verwendet bei smtp-server ohne Autentifierung
	 * Standardport 25 wird verwendet
	 * @throws java.lang.Exception
	 */

	public SOSMail(final String host) throws Exception {
		if (host != null) {
			this.host = host;
		}
		this.init();

	}

	/**
	 * Konstruktor
	 * @param host String Hostname oder IP-Adresse des Mail-Servers
	 * @param user String Name des SMTP-Benutzers
	 * @param pass String Kennwort des SMTP-Benutzers
	 * Wird verwendet bei smtp-server mit Autentifierung
	 * Standardport 25 wird verwendet
	 *
	 * @throws java.lang.Exception
	 */
	public SOSMail(final String host_, final String user_, final String password_) throws Exception {

		if (host_ != null) {
			host = host_;
		}
		if (user_ != null) {
			user = user_;
		}
		if (password_ != null) {
			password = password_;
		}
		this.init();
	}

	/**
	 * Konstruktor
	 * @param host String Hostname oder IP-Adresse des Mail-Servers
	 * @param user String Name des SMTP-Benutzers
	 * @param pass String Kennwort des SMTP-Benutzers
	 * Wird verwendet bei smtp-server mit Autentifierung ,wenn der Port festgelegt werden soll
	 *
	 * @throws java.lang.Exception
	 */
	public SOSMail(final String host_, final String port_, final String user_, final String password_) throws Exception {

		if (host_ != null) {
			host = host_;
		}

		if (port_ != null) {
			port = port_;
		}

		if (user_ != null) {
			user = user_;
		}

		if (password_ != null) {
			password = password_;
		}

		this.init();
	}

	/**
	 * Konstruktor
	  * @param sosSettings SOSSettings Einstellungen aus Profile Settings oder Connection Settings
	 * @throws java.lang.Exception
	 */
	public SOSMail(final SOSSettings sosSettings) throws Exception {

		this.getSettings(sosSettings);
		this.init();

	}

	/**
	 * Konstruktor
	 * @param sosSettings SOSSettings Einstellungen aus Profile Settings oder Connection Settings
	 * @param language Sprache für Einstellungen
	 * @throws java.lang.Exception
	 */
	public SOSMail(final SOSSettings sosSettings, final String language) throws Exception {

		this.getSettings(sosSettings, language);
		this.init();

	}

	/**
	 * Initialisierungen
	 * -ruft initMessage()
	 * -ruf initLanguage()
	 * Wird im Konstruktor gerufen.
	 * @throws MessagingException
	 *
	 * @throws java.lang.Exception
	 */

	// --------------------------------------------------------------------------------init

	private void initPriority() throws MessagingException {
		switch (priority) {
			case PRIORITY_HIGHEST:
				this.setPriorityHighest();
				break;
			case PRIORITY_HIGH:
				this.setPriorityHigh();
				break;
			case PRIORITY_LOW:
				this.setPriorityLow();
				break;
			case PRIORITY_LOWEST:
				this.setPriorityLowest();
				break;
			default:
				break;
		// default: this.setPriorityNormal(); break;
		}

	}

	public void init() throws Exception {

		dateFormats.put("de", "dd.MM.yyyy");
		dateFormats.put("en", "MM/dd/yyyy");

		datetimeFormats.put("de", "dd.MM.yyyy HH:mm");
		datetimeFormats.put("en", "MM/dd/yyyy HH:mm");

		this.initLanguage();
		this.initMessage();

		clearRecipients();
		clearAttachments();
	}

	// --------------------------------------------------------------------------------initMessage
	/**
	 * Initialisierungen
	 * -Autorisierung für smtp-Server
	 * -Anlegen des Message-Objekts
	 * -Lüschen der Empfänger- und der Attachmentliste
	 * Wenn ein sosmail-Objekt mehrmach wiederverwendet werden sollen , muss für jede Nachricht
	 * .init() gerufen.
	 *
	 * @throws java.lang.Exception
	 */

	public void initMessage() throws Exception {
		createMessage(createSession());
		initPriority();
	}

	// --------------------------------------------------------------------------------createSession
	/**
	 * Initialisierungen
	 * -Autorisierung für smtp-Server
	 * -Anlegen des Message-Objekts
	 * -Lüschen der Empfünger- und der Attachmentliste
	 * Wenn ein sosmail-Objekt mehrmach wiederverwendet werden sollen , muss für jede Nachricht
	 * .init() gerufen.
	 *
	 * @throws java.lang.Exception
	 */

	public Session createSession() throws Exception {

		Properties props = System.getProperties();
		props.put("mail.host", host);
		props.put("mail.port", port);
		props.put("mail.smtp.timeout", String.valueOf(timeout));
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.class", "com.sun.mail.SMTPTransport");

		if (user.length() > 0) {
			props.put("mail.smtp.auth", "true");
		}
		else {
			props.put("mail.smtp.auth", "false");
		}
		authenticator = new SOSMailAuthenticator(user, password);
		session = Session.getInstance(props, authenticator);
		return session;

	}

	// --------------------------------------------------------------------------------createMessage
	public void createMessage(final Session session) throws Exception {
		message = new MimeMessage(session);
	}

	// --------------------------------------------------------------------------------initLanguage
	/**
	 * Initialisierungen
	 * -Datumsformat setzen
	 *
	 * @throws java.lang.Exception
	 */
	public void initLanguage() throws Exception {

		if (dateFormats.containsKey(this.getLanguage()) && datetimeFormats.containsKey(this.getLanguage())) {
			this.setDateFormat(dateFormats.get(this.getLanguage()).toString());
			this.setDatetimeFormat(datetimeFormats.get(this.getLanguage()).toString());
		}
		else {
			this.setDateFormat(dateFormats.get("de").toString());
			this.setDatetimeFormat(datetimeFormats.get("de").toString());
		}
	}

	// --------------------------------------------------------------------------------log
	public void log(String s, final int level) throws Exception {
		s = "SOSMail." + s;
		// if (sosLogger != null && Math.abs(logLevel) >= Math.abs(level)){
		if (sosLogger != null) {

			switch (level) {
				case SOSLogger.DEBUG1:
					sosLogger.debug1(s);
					break;
				case SOSLogger.DEBUG2:
					sosLogger.debug2(s);
					break;
				case SOSLogger.DEBUG3:
					sosLogger.debug3(s);
					break;
				case SOSLogger.DEBUG4:
					sosLogger.debug4(s);
					break;
				case SOSLogger.DEBUG5:
					sosLogger.debug5(s);
					break;
				case SOSLogger.DEBUG6:
					sosLogger.debug6(s);
					break;
				case SOSLogger.DEBUG7:
					sosLogger.debug7(s);
					break;
				case SOSLogger.DEBUG8:
					sosLogger.debug8(s);
					break;
				case SOSLogger.DEBUG9:
					sosLogger.debug9(s);
					break;
				case SOSLogger.INFO:
					sosLogger.info(s);
					break;
				case SOSLogger.WARN:
					sosLogger.warn(s);
					break;
				case SOSLogger.ERROR:
					sosLogger.error(s);
					break;
			}

		}
	}

	// --------------------------------------------------------------------------------getSettings
	/**
	 * Einstellungen für eMail-Versand aus Settings
	 *
	 * @param sosSettings SOSSettings Einstellungen aus Profile Settings oder Connection Settings
	 * @param language Sprache fr Einstellungen
	 * @throws java.lang.Exception
	 */
	private void getSettings(final SOSSettings sosSettings, final String language) throws Exception {

		if (language != null) {
			this.setLanguage(language);
		}
		this.getSettings(sosSettings);
	}

	// --------------------------------------------------------------------------------getEntry

	private String getEntry(final String val, final Properties entries, final String key) {
		String erg = val; // Do nothing if not exist

		if (entries.containsKey(key)) {
			if (entries.getProperty(key).length() > 0)
				erg = entries.getProperty(key);
		}
		return erg;
	}

	// --------------------------------------------------------------------------------getSettings
	/**
	* Einstellungen fr eMail-Versand aus Settings
	*
	 * @param sosSettings SOSSettings Einstellungen aus Profile Settings oder Connection Settings
	 * @throws java.lang.Exception
	 */
	private void getSettings(final SOSSettings sosSettings) throws Exception {

		if (sosSettings == null)
			throw new Exception(SOSClassUtil.getMethodName() + ": missing settings object.");

		this.sosSettings = sosSettings;

		Properties entries = this.sosSettings.getSection(applicationMail, sectionMail);

		if (entries.size() == 0)
			throw new Exception(SOSClassUtil.getMethodName() + ": missing settings entries in section \"" + sectionMail + "\".");

		host = getEntry(host, entries, "host");
		port = getEntry(port, entries, "port");
		user = getEntry(user, entries, "smtp_user");
		password = getEntry(password, entries, "smtp_pass");

		from = getEntry(from, entries, "from");
		from = getEntry(from, entries, "mail_from");
		fromName = getEntry(fromName, entries, "from_name");
		fromName = getEntry(fromName, entries, "mail_from_name");

		replyTo = getEntry(replyTo, entries, "reply_to");
		replyTo = getEntry(replyTo, entries, "mail_reply_to");

		queueDir = getEntry(queueDir, entries, "queue_directory");
		queueDir = getEntry(queueDir, entries, "mail_queue_directory");

		String priorityStr = new String("1");
		priority = Integer.parseInt(getEntry(priorityStr, entries, "priority"));

		language = getEntry(language, entries, "language");
		subject = getEntry(subject, entries, "subject");

		contentType = getEntry(contentType, entries, "content_type");
		charset = getEntry(charset, entries, "charset");
		encoding = getEntry(encoding, entries, "encoding");

		attachmentEncoding = getEntry(attachmentEncoding, entries, "attachment_encoding");
		attachmentEncoding = getEntry(attachmentEncoding, entries, "file_encoding");

		if (entries.getProperty("smtp_timeout") != null) {
			if (entries.getProperty("smtp_timeout").length() > 0)
				timeout = 1000 * Integer.parseInt(entries.getProperty("smtp_timeout"));
		}

		if (from == null && entries.containsKey("mail_from")) {
			if (entries.getProperty("mail_from").length() > 0)
				from = entries.getProperty("mail_from");
		}
	}

	// --------------------------------------------------------------------------------getTemplates
	/**
	 * Muster für eMail-Texte
	 *
	 * @param sosSettings SOSSettings Einstellungen aus Profile Settings oder Connection Settings
	 * @throws java.lang.Exception
	 */
	public boolean getTemplates(final SOSSettings sosSettings, final String language) throws Exception {

		if (language != null)
			this.setLanguage(language);
		return this.getTemplates(sosSettings);
	}

	// --------------------------------------------------------------------------------getTemplates
	/**
	 * Muster für eMail-Texte
	 *
	 * @param sosSettings SOSSettings Einstellungen aus Profile Settings oder Connection Settings
	 * @throws java.lang.Exception
	 */
	public boolean getTemplates(final SOSSettings sosSettings) throws Exception {

		if (sosSettings == null)
			throw new Exception(SOSClassUtil.getMethodName() + ": missing settings object.");

		this.sosSettings = sosSettings;
		templates = this.sosSettings.getSection(this.getApplicationMailTemplates(), this.getSectionMailTemplates() + "_" + language);

		if (templates.size() == 0)
			throw new Exception(SOSClassUtil.getMethodName() + ": missing settings entries for application \"" + applicationMailTemplates
					+ "\" in section \"" + sectionMailTemplates + "\".");
		return true;
	}

	// --------------------------------------------------------------------------------substituteSubject
	/**
	 *
	 * @param template Name des Templates
	 * @param replacements HashMap mit Name/Wert-Paaren, der Name wird im Template durch den Wert ersetzt
	 * @param nl2br NewLines durch HTML Breaks ersetzen
	 * @throws java.lang.Exception
	 */
	public String substituteSubject(final String template, final HashMap replacements) throws Exception {

		if (!templates.containsKey(template + "_subject")) {
			throw new Exception("substituteSubject(): template does not exist: " + template + "_subject");
		}

		return substitute(templates.get(template + "_subject").toString(), replacements, false);
	}

	// --------------------------------------------------------------------------------substituteBody
	/**
	 *
	 * @param template Name des Templates
	 * @param replacements HashMap mit Name/Wert-Paaren, der Name wird im Template durch den Wert ersetzt
	 * @param nl2br NewLines durch HTML Breaks ersetzen
	 * @throws java.lang.Exception
	 */
	public String substituteBody(final String template, final HashMap replacements, final boolean nl2br) throws Exception {

		if (!templates.containsKey(template + "_body")) {
			throw new Exception("substituteBody(): template does not exist: " + template + "_body");
		}

		return substitute(templates.get(template + "_body").toString(), replacements, nl2br);
	}

	// --------------------------------------------------------------------------------substituteBody
	/**
	 *
	 * @param template Name des Templates
	 * @param replacements HashMap mit Name/Wert-Paaren, der Name wird im Template durch den Wert ersetzt
	 * @param nl2br NewLines durch HTML Breaks ersetzen
	 * @throws java.lang.Exception
	 */
	public String substituteBody(final String template, final HashMap replacements) throws Exception {

		if (!templates.containsKey(template + "_body")) {
			throw new Exception("substituteBody(): template does not exist: " + template + "_body");
		}

		return substitute(templates.get(template + "_body").toString(), replacements, false);
	}

	// -------------------------------------------------------------------------------------substitute
	/**
	 *
	 * @param content String mit beliebigem Inhalt
	 * @param replacements HashMap mit Name/Wert-Paaren, der Name wird im content durch den Wert ersetzt
	 * @param nl2br NewLines durch HTML Breaks ersetzen
	 * @throws java.lang.Exception
	 * @deprecated
	 */
	@Deprecated
	public String substitute(String content, final HashMap replacements, final boolean nl2br) throws Exception {

		Object key = null;
		Object value = null;

		if (this.getLanguage().equalsIgnoreCase("de")) {
			this.setDateFormat("dd.MM.yyyy");
			this.setDatetimeFormat("dd.MM.yyyy HH:mm");
		}
		else
			if (this.getLanguage().equalsIgnoreCase("en")) {
				this.setDateFormat("MM/dd/yyyy");
				this.setDatetimeFormat("MM/dd/yyyy HH:mm");
			}
		content = content.replaceAll("&\\(date\\)", SOSDate.getCurrentDateAsString(this.getDateFormat()));
		content = content.replaceAll("&\\(datetime\\)", SOSDate.getCurrentTimeAsString(this.getDatetimeFormat()));
		content = content.replaceAll("&\\#\\(date\\)", SOSDate.getCurrentDateAsString(this.getDateFormat()));
		content = content.replaceAll("&\\#\\#\\(datetime\\)", SOSDate.getCurrentTimeAsString(this.getDatetimeFormat()));

		if (nl2br) {
			content = content.replaceAll("\n", "<br/>");
		}

		if (replacements != null) {
			Iterator keys = replacements.keySet().iterator();
			while (keys.hasNext()) {
				key = keys.next();
				if (key != null) {
					value = replacements.get(key.toString());
					if (value != null) {
						try {
							content = content.replaceAll("&\\#\\(" + key.toString() + "\\)",
									SOSDate.getDateAsString(SOSDate.getDate(value.toString()), this.getDateFormat()));
							content = content.replaceAll("&\\#\\#\\(" + key.toString() + "\\)",
									SOSDate.getDateTimeAsString(SOSDate.getDate(value.toString()), this.getDatetimeFormat()));
						}
						catch (Exception ex) {
						} // ignore this error: replacement is not convertible to date

						Locale defaultLocale = Locale.getDefault();
						try {
							double doubleValue = Double.parseDouble(value.toString());
							if (this.getLanguage().equalsIgnoreCase("de")) {
								Locale.setDefault(Locale.GERMAN);
							}
							else
								if (this.getLanguage().equalsIgnoreCase("en")) {
									Locale.setDefault(Locale.US);
								}
							DecimalFormat formatter = new DecimalFormat("#,###.00");
							content = content.replaceAll("&\\$\\(" + key.toString() + "\\)", formatter.format(doubleValue).toString());
						}
						catch (Exception ex) {
						}
						finally {
							Locale.setDefault(defaultLocale);
						}

						content = content.replaceAll("&\\(" + key.toString() + "\\)", value.toString());
					}
				}
			}
		}

		// remove all variables that were not substituted
		content = content.replaceAll("&\\#\\(.*\\)", "");
		content = content.replaceAll("&\\#\\#\\(.*\\)", "");
		content = content.replaceAll("&\\$\\(.*\\)", "");
		return content.replaceAll("&\\(.*\\)", "");
	}

	// --------------------------------------------------------------------------------addRecipient
	/**
	 *
	 * @param recipient eMail-Adresse des Empfüngers
	 * @throws java.lang.Exception
	 */
	public void addRecipient(String recipient) throws Exception {
		String token = "";

		warn("addRecipient", recipient);

		if (recipient == null)
			throw new Exception(SOSClassUtil.getMethodName() + ": recipient has no value.");

		recipient = recipient.replace(',', ';');
		StringTokenizer t = new StringTokenizer(recipient, ";");

		while (t.hasMoreTokens()) {
			token = t.nextToken();
			if (!toList.contains(token)) {
				toList.add(token);
			}
			log(SOSClassUtil.getMethodName() + "-->" + token, SOSLogger.DEBUG9);
		}
		changed = true;
	}

	// --------------------------------------------------------------------------------addCC
	/**
	 *
	 * @param recipient eMail-Adresse des cc Empfüngers
	 * @throws java.lang.Exception
	 */
	public void addCC(String recipient) throws Exception {
		String token = "";
		warn("addCC", recipient);
		if (recipient == null)
			throw new Exception(SOSClassUtil.getMethodName() + ": CC recipient has no value.");
		recipient = recipient.replace(',', ';');
		StringTokenizer t = new StringTokenizer(recipient, ";");

		while (t.hasMoreTokens()) {
			token = t.nextToken();
			if (!toList.contains(token) && !ccList.contains(token)) {
				ccList.add(token);
				log(SOSClassUtil.getMethodName() + "-->" + token, SOSLogger.DEBUG9);
			}
			else {
				log(SOSClassUtil.getMethodName() + "--> Ignored:" + token, SOSLogger.DEBUG9);
			}
		}
		changed = true;
	}

	// --------------------------------------------------------------------------------addBCC
	/**
	 *
	 * @param recipient eMail-Adresse des bcc Empfüngers
	 * @throws java.lang.Exception
	 */
	public void addBCC(String recipient) throws Exception {
		String token = "";
		warn("addBCC", recipient);
		if (recipient == null)
			throw new Exception(SOSClassUtil.getMethodName() + ": BCC recipient has no value.");
		recipient = recipient.replace(',', ';');
		StringTokenizer t = new StringTokenizer(recipient, ";");

		while (t.hasMoreTokens()) {
			token = t.nextToken();
			if (!ccList.contains(token) && !toList.contains(token) && !bccList.contains(token)) {
				bccList.add(token);
				log(SOSClassUtil.getMethodName() + "-->" + token, SOSLogger.DEBUG9);
			}
			else {
				log(SOSClassUtil.getMethodName() + "--> Ignored:" + token, SOSLogger.DEBUG9);
			}
		}
		changed = true;
	}

	// --------------------------------------------------------------------------------closeAttachments
	private void closeAttachments() throws Exception {
		Exception exception = null;

		for (int i = 0; i < file_input_streams.size(); i++) {
			try {
				((FileInputStream) file_input_streams.get(i)).close();
			}
			catch (Exception x) {
				if (exception == null)
					exception = x;
			}
		}

		if (exception != null)
			throw exception;
	}

	// --------------------------------------------------------------------------------addAttachment
	/**
	*
	* @param filename Datei fr Anhang
	* @throws java.lang.Exception
	*/
	public void addAttachment(final SOSMailAttachment att) throws Exception {

		warn("addAttachment", att.getFile().getAbsolutePath());
		attachmentList.put(att.getFile().getAbsolutePath(), att);
		changed = true;
	}

	// --------------------------------------------------------------------------------addAttachment
	/**
	*
	* @param filename Datei fr Anhang
	* @throws java.lang.Exception
	*/
	public void addAttachment(final String filename) throws Exception {

		warn("addAttachment", filename);
		File f = new File(filename);
		SOSMailAttachment att = new SOSMailAttachment(this, f);
		att.setCharset(getAttachmentCharset());
		att.setEncoding(getAttachmentEncoding());
		att.setContentType(getAttachmentContentType());
		attachmentList.put(filename, att);
		changed = true;
	}

	// --------------------------------------------------------------------------------addAttachment
	/**
	 *
	 * @param filename Datei fr Anhang
	 * @param contentType Content-Type dieses Anhangs
	 * @throws java.lang.Exception
	 */
	public void addAttachment(final String filename, final String params) throws Exception {
		String name = "";
		String value = "";
		String token = "";
		int counter = 0;

		warn("addAttachment", filename + "(" + params + ")");
		StringTokenizer t = new StringTokenizer(params, ",");
		File f = new File(filename);
		SOSMailAttachment att = new SOSMailAttachment(this, f);

		while (t.hasMoreTokens()) {
			token = t.nextToken();
			StringTokenizer vv = new StringTokenizer(token, "=");
			if (vv.countTokens() == 1) {
				name = "content-type";
				value = vv.nextToken();
				counter += 1;
			}
			else {
				name = vv.nextToken().trim();
				try {
					value = vv.nextToken().trim();
				}
				catch (NoSuchElementException e) {
					value = "";
				}
			}
			if (name.equalsIgnoreCase("content-type")) {
				att.setContentType(value);
			}
			else
				if (name.equalsIgnoreCase("charset")) {
					att.setCharset(value);
				}
				else
					if (name.equalsIgnoreCase("encoding")) {
						att.setEncoding(value);
					}
					else {
						throw new Exception("USING of .addAttachment is wrong. ==> " + params
								+ ", rigth using is: [content-type-value],[content-type=<value>],[charset=<value>],[encoding=<value>]");
					}

			if (counter > 1) {
				throw new Exception("USING of .addAttachment is wrong. ==> " + params
						+ ", rigth using is: [content-type-value],[content-type=<value>],[charset=<value>],[encoding=<value>]");
			}
		}

		attachmentList.put(filename, att);
		changed = true;

	}

	// --------------------------------------------------------------------------------add_file
	private void add_file(final SOSMailAttachment att) throws Exception {
		if (!att.getFile().exists())
			throw new Exception("Datei " + att.getFile().getAbsolutePath() + " fehlt");

		MimeBodyPart attachment = new MimeBodyPart();

		DataSource data_source = new File_data_source(att.getFile(), att.getContentType());
		DataHandler data_handler = new DataHandler(data_source);

		attachment.setDataHandler(data_handler);
		attachment.setFileName(att.getFile().getName());

		// Charset des Attachments setzen, wenn content_type text/ ist
		if (att.getContentType().startsWith("text/")) {
			// Charset macht nur Sinn bei text/

			String s = "";
			FileReader fr = new FileReader(att.getFile());

			for (int c; (c = fr.read()) != -1;)
				s += (char) c;
			attachment.setText(s, att.getCharset());
			fr.close();
		}

		Object m = message.getContent();
		if (!(m instanceof MimeMultipart))
			throw new RuntimeException(getClass().getName() + "mime_message.getContent() liefert nicht MimeMultiPart");
		((MimeMultipart) m).addBodyPart(attachment);
		attachment.setHeader("Content-Transfer-Encoding", att.getEncoding());
	}

	// --------------------------------------------------------------------------------loadFile

	public void loadFile(final File messageFile) throws Exception {
		FileInputStream messageInputStream = null;
		try {
			messageInputStream = new FileInputStream(messageFile);
			message = new MimeMessage(createSession(), messageInputStream);
			loadedMessageId = message.getMessageID();

			raw_email_byte_stream = new ByteArrayOutputStream();
			message.writeTo(raw_email_byte_stream);
			messageBytes = raw_email_byte_stream.toByteArray();

			messageReady = true;
		}
		catch (Exception x) {
			throw new Exception("Fehler beim Lesen der eMail. " + messageFile);
		}
		finally {
			if (messageInputStream != null) {
				messageInputStream.close();
			}
		}
	}

	public void unloadMessage() {
		messageReady = false;
		loadedMessageId = "";
		message = null;
	}

	// --------------------------------------------------------------------------------send
	/**
	 * Nachricht versenden
	 *
	 * @throws java.lang.Exception
	 */
	public boolean send() throws Exception {
		return sendJavaMail();
	}

	// --------------------------------------------------------------------------------send

	/**
	    * Nachricht vorbereiten: Nicht senden
	    *
	    * @throws java.lang.Exception
	    */
	public boolean send(final boolean send) throws Exception {
		if (send) {
			return send();
		}
		else {
			return prepareJavaMail();
		}
	}

	// --------------------------------------------------------------------------------sendJavaMail
	/**
	 * Nachricht mit JavaMail-Funktionen versenden
	 *
	 * @throws java.lang.Exception
	 */
	private boolean sendJavaMail() throws Exception {
		try {

			prepareJavaMail();

			String sTO = this.getRecipientsAsString();
			String logMessage = SOSClassUtil.getMethodName() + "-->" + "sending email:" + "   host:port=" + host + ":" + port + "   to=" + sTO;

			String sCC = this.getCCsAsString();
			if (!sCC.equals("")) {
				logMessage += "   sCC=" + sCC;
			}

			String sBCC = this.getBCCsAsString();
			if (!sBCC.equals("")) {
				logMessage += "   sBCC=" + sBCC;
			}

			log(logMessage, SOSLogger.INFO);

			log(SOSClassUtil.getMethodName() + "-->" + "Subject=" + subject, SOSLogger.DEBUG6);
			log(SOSClassUtil.getMethodName() + "-->" + dumpHeaders(), SOSLogger.DEBUG6);
			log(SOSClassUtil.getMethodName() + "-->" + dumpMessageAsString(false), SOSLogger.DEBUG9);
			// --------------------------------------------------------------
			if (!sendToOutputStream) {
				// Hier kann nicht die statische Methode Transport.send verwendet werden, da diese ein
				// implizites saveChanges macht. Das führt zu einer neuen Vergabe einer Message-Id, so dass diese
				// möglicherweise nicht mit der aus dumpMessageToFile übereinstimmt.

				Transport t = session.getTransport("smtp");
				message.setSentDate(new Date());
				System.setProperty("mail.smtp.port", port);
				System.setProperty("mail.smtp.host", host);
				if (user.length() == 0) {
					t.connect();
				}
				else {
					t.connect(host, user, password);
				}
				t.sendMessage(message, message.getAllRecipients());
				t.close();

				raw_email_byte_stream = new ByteArrayOutputStream();
				message.writeTo(raw_email_byte_stream);
				messageBytes = raw_email_byte_stream.toByteArray();
				changed = true;
			}
			// --------------------------------------------------------------

			return true;
		}
		catch (javax.mail.AuthenticationFailedException ee) {
			lastError = "AuthenticationFailedException while connecting to " + host + ":" + port + " " + user + "/******** -->" + ee.getMessage();

			try {
				dumpMessageToFile(true);
			}
			catch (Exception e) {
				log(SOSClassUtil.getMethodName() + ":" + e.getMessage(), SOSLogger.WARN);
			}
			;

			return false;

		}
		catch (javax.mail.MessagingException e) {
			// ist ein Fehler, bei dem es lohnt, zwischenzuspeichern?
			if (queueDir.length() > 0 && e.getMessage().startsWith("Could not connect to SMTP host") || e.getMessage().startsWith("Unknown SMTP host")
					|| e.getMessage().startsWith("Read timed out") || e.getMessage().startsWith("Exception reading response")) {
				lastError = e.getMessage() + " ==> " + host + ":" + port + " " + user + "/********";
				try {
					dumpMessageToFile(true);
				}
				catch (Exception ee) {
					log(SOSClassUtil.getMethodName() + ":" + e.getMessage(), SOSLogger.WARN);
				}
				;
				return false;
			}
			else {
				throw new Exception(SOSClassUtil.getMethodName() + ": error occurred on send: " + e.toString());
			}
		}
		catch (SocketTimeoutException e) {
			if (queueDir.length() > 0) {
				lastError = e.getMessage() + " ==> " + host + ":" + port + " " + user + "/********";
				try {
					dumpMessageToFile(true);
				}
				catch (Exception ee) {
					log(SOSClassUtil.getMethodName() + ":" + e.getMessage(), SOSLogger.WARN);
				}
				;
				return false;
			}
			else {
				throw new Exception(SOSClassUtil.getMethodName() + ": error occurred on send: " + e.toString());
			}
		}
		catch (Exception e) {
			throw new Exception(SOSClassUtil.getMethodName() + ": error occurred on send: " + e.toString());
		}

	}

	// --------------------------------------------------------------------------------haveAlternative
	private boolean haveAlternative() {
		return !alternativeBody.equals("") && attachmentList.isEmpty();
	}

	// --------------------------------------------------------------------------------prepareJavaMail
	/**
	 * Nachricht mit JavaMail-Funktionen versenden
	 *
	 * @throws java.lang.Exception
	 */
	protected boolean prepareJavaMail() throws Exception {
		try {
			if (messageReady) {
				message.saveChanges();
				return true;
			}

			if (!changed) {
				return true;
			}
			changed = false;

			if (this.getContentType().equals("text/html")) {
				body = body.replaceAll("\\\\n", "<br>");
			}
			else {
				body = body.replaceAll("\\\\n", "\n");
			}

			String t = "";

			if (toList.isEmpty()) {
				throw new Exception(SOSClassUtil.getMethodName() + ": no recipient specified.");
			}

			if (from == null || from.length() == 0) {
				throw new Exception(SOSClassUtil.getMethodName() + ": no sender specified.");
			}
			if (fromName != null && fromName.length() > 0) {
				message.setFrom(new InternetAddress(from, fromName));
			}
			else {
				message.setFrom(new InternetAddress(from));
			}

			message.setSentDate(new Date());

			if (replyTo != null && replyTo.length() > 0) {
				InternetAddress fromAddrs[] = new InternetAddress[1];
				fromAddrs[0] = new InternetAddress(replyTo);
				message.setReplyTo(fromAddrs);
			}

			if (!toList.isEmpty()) {
				InternetAddress toAddrs[] = new InternetAddress[toList.size()];
				int i = 0;

				for (ListIterator e = toList.listIterator(); e.hasNext();) {
					t = e.next().toString();
					toAddrs[i++] = new InternetAddress(t);
				}

				message.setRecipients(MimeMessage.RecipientType.TO, toAddrs);
			}

			// if (!this.ccList.isEmpty()) {
			// Sonst wirkt clearReceipients nicht.
			InternetAddress toAddrs[] = new InternetAddress[ccList.size()];
			int i = 0;
			for (ListIterator e = ccList.listIterator(); e.hasNext();) {
				t = e.next().toString();
				toAddrs[i++] = new InternetAddress(t);
			}
			message.setRecipients(MimeMessage.RecipientType.CC, toAddrs);
			// }

			// if (!this.bccList.isEmpty()) {
			toAddrs = new InternetAddress[bccList.size()];
			i = 0;
			for (ListIterator e = bccList.listIterator(); e.hasNext();) {
				t = e.next().toString();
				toAddrs[i++] = new InternetAddress(t);
			}
			message.setRecipients(MimeMessage.RecipientType.BCC, toAddrs);
			// }

			if (subject != null) {
				message.setSubject(subject);
			}

			// send the attachments

			if (!attachmentList.isEmpty() || !alternativeBody.equals("")) {

				// Multipart nur bei Attachments!

				// send the body
				MimeBodyPart bodypart = null;
				MimeBodyPart alternativeBodypart = null;
				MimeMultipart multipart = null;
				if (this.haveAlternative()) {
					multipart = new MimeMultipart("alternative");
				}
				else {
					multipart = new MimeMultipart();
				}

				bodypart = new MimeBodyPart();
				if (contentType.startsWith("text/")) {
					bodypart.setContent(body, contentType + ";charset= " + charset);
				}
				else {
					bodypart.setContent(body, contentType);
				}

				multipart.addBodyPart(bodypart);

				// Alternativer Body gesetzt? Nur wenn keine Attachments vorhanden!!!

				if (this.haveAlternative()) {
					alternativeBodypart = new MimeBodyPart();
					if (contentType.startsWith("text/")) {
						alternativeBodypart.setContent(alternativeBody, alternativeContentType + ";charset= " + alternativeCharset);
					}
					else {
						alternativeBodypart.setContent(alternativeBody, alternativeContentType);
					}
					multipart.addBodyPart(alternativeBodypart);
				}

				message.setContent(multipart);
				// Encoding nur für Bodypart setzen
				bodypart.setHeader("Content-Transfer-Encoding", encoding);
				if (alternativeBodypart != null) {
					alternativeBodypart.setHeader("Content-Transfer-Encoding", alternativeEncoding);
				}

				for (Iterator iter = attachmentList.values().iterator(); iter.hasNext();) {

					SOSMailAttachment attachment = (SOSMailAttachment) iter.next();
					String content_type = attachment.getContentType();

					if (content_type == null)
						throw new Exception("content_type ist null");
					log(SOSClassUtil.getMethodName() + "-->" + "Attachment=" + attachment.getFile(), SOSLogger.DEBUG6);
					add_file(attachment);
				}
			}
			else {
				message.setHeader("Content-Transfer-Encoding", encoding);
				// Wenn content_type des body <> text_plain, dann content_type setzen
				if (contentType.startsWith("text/")) {
					message.setContent(body, contentType + "; charset=" + charset);
				}
				else {
					message.setContent(body, contentType);
				}
			}

			message.saveChanges();
			closeAttachments();

			return true;

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new Exception(SOSClassUtil.getMethodName() + ": error occurred on send: " + e.toString());
		}
		finally {
		}
	}

	// --------------------------------------------------------------------------------dumpHeaders
	public String dumpHeaders() throws IOException, MessagingException {
		String s = "";

		for (Enumeration e = message.getAllHeaders(); e.hasMoreElements();) {
			Header header = (Header) e.nextElement();
			s += "\n" + header.getName() + ": " + header.getValue();
		}
		return s;

	}

	// --------------------------------------------------------------------------------messageRemoveAttachments
	private ByteArrayOutputStream messageRemoveAttachments() throws Exception {
		ByteArrayOutputStream raw_email_byte_stream_without_attachment = new ByteArrayOutputStream();

		// Attachments entfernen
		MimeMessage mm = new MimeMessage(message);
		Object mmpo = mm.getContent();
		if (mmpo instanceof MimeMultipart) {
			MimeMultipart mmp = (MimeMultipart) mmpo;

			if (mm.isMimeType("text/plain")) {
			}
			else
				if (mm.isMimeType("multipart/*")) {
					mmp = (MimeMultipart) mm.getContent();
					for (int i = 1; i < mmp.getCount(); i++) {
						BodyPart part = mmp.getBodyPart(i);
						mmp.removeBodyPart(i);
						i--;
					}

				}

			//
			mm.setContent(mmp);
			mm.saveChanges();
		}
		mm.writeTo(raw_email_byte_stream_without_attachment);
		return raw_email_byte_stream_without_attachment;

	}

	// --------------------------------------------------------------------------------dumpMessageAsString
	public String dumpMessageAsString() throws Exception {
		return dumpMessageAsString(false);
	}

	// --------------------------------------------------------------------------------dumpMessageToFile
	private void dumpMessageToFile(final boolean withAttachment) throws Exception {
		Date d = new Date();
		StringBuffer bb = new StringBuffer();
		SimpleDateFormat s = new SimpleDateFormat(queuePattern);
		FieldPosition fp = new FieldPosition(0);
		StringBuffer b = s.format(d, bb, fp);
		lastGeneratedFileName = queueDir + "/" + queuePraefix + b + ".email";
		File f = new File(lastGeneratedFileName);
		while (f.exists()) {
			b = s.format(d, bb, fp);
			lastGeneratedFileName = queueDir + "/" + queuePraefix + b + ".email";
			f = new File(lastGeneratedFileName);
		}
		dumpMessageToFile(f, withAttachment);
	}

	// --------------------------------------------------------------------------------dumpMessageToFile
	public void dumpMessageToFile(final String filename, final boolean withAttachment) throws Exception {
		dumpMessageToFile(new File(filename), withAttachment);
	}

	// --------------------------------------------------------------------------------dumpMessageToFile
	public void dumpMessageToFile(final File file, final boolean withAttachment) throws Exception {
		try {
			this.prepareJavaMail();

			File myFile = new File(file.getAbsolutePath() + "~");

			FileOutputStream out = new FileOutputStream(myFile, true);
			out.write(dumpMessage(withAttachment));
			out.close();

			String newFilename = myFile.getAbsolutePath().substring(0, myFile.getAbsolutePath().length() - 1);
			File f = new File(newFilename);
			f.delete();

			myFile.renameTo(f);

		}
		catch (Exception e) {
			throw new Exception(SOSClassUtil.getMethodName() + ": error occurred on dump: " + e.toString());
		}
	}

	// --------------------------------------------------------------------------------dumpMessageAsString
	public String dumpMessageAsString(final boolean withAttachment) throws Exception {
		byte[] bytes;
		ByteArrayOutputStream raw_email_byte_stream_without_attachment = null;

		this.prepareJavaMail();
		if (!withAttachment) {
			raw_email_byte_stream_without_attachment = messageRemoveAttachments();
		}

		raw_email_byte_stream = new ByteArrayOutputStream();
		message.writeTo(raw_email_byte_stream);

		if (withAttachment || raw_email_byte_stream_without_attachment == null) {
			bytes = raw_email_byte_stream.toByteArray();
		}
		else {
			bytes = raw_email_byte_stream_without_attachment.toByteArray();
		}
		String s = new String(bytes);

		return s;

	}

	public byte[] dumpMessage() throws Exception {
		return dumpMessage(true);
	}

	// --------------------------------------------------------------------------------dumpMessage
	public byte[] dumpMessage(final boolean withAttachment) throws Exception {
		byte[] bytes;
		ByteArrayOutputStream raw_email_byte_stream_without_attachment = null;

		this.prepareJavaMail();
		if (!withAttachment) {
			raw_email_byte_stream_without_attachment = messageRemoveAttachments();
		}

		raw_email_byte_stream = new ByteArrayOutputStream();
		message.writeTo(raw_email_byte_stream);

		if (withAttachment || raw_email_byte_stream_without_attachment == null) {
			bytes = raw_email_byte_stream.toByteArray();
		}
		else {
			bytes = raw_email_byte_stream_without_attachment.toByteArray();
		}
		return bytes;
	}

	// --------------------------------------------------------------------------------Getter/Setter
	public LinkedList getRecipients() {
		return toList;
	}

	// Liefert die Recipients aus dem Message-Objekt, wenn das Message-Objekt mit loadFile erzeugt wurde.
	// Sonst werden die mit addRecipients hinzugefügten Empfünger geliefert.

	public String getRecipientsAsString() throws MessagingException {
		String s = " ";

		if (messageReady) {
			Address[] addresses = message.getRecipients(MimeMessage.RecipientType.TO);
			if (addresses != null) {
				for (Address aktAddress : addresses) {
					s += aktAddress.toString() + ",";
				}
			}
		}
		else {
			for (Iterator i = toList.listIterator(); i.hasNext();) {
				s += i.next() + ",";
			}
		}
		return s.substring(0, s.length() - 1).trim();
	}

	public LinkedList getCCs() {
		return ccList;
	}

	// Liefert die Recipients aus dem Message-Objekt, wenn das Message-Objekt mit loadFile erzeugt wurde.
	// Sonst werden die mit addRecipients hinzugefügten Empfünger geliefert.

	public String getCCsAsString() throws MessagingException {
		String s = " ";

		if (messageReady) {
			Address[] addresses = message.getRecipients(MimeMessage.RecipientType.CC);
			if (addresses != null) {
				for (Address aktAddress : addresses) {
					s += aktAddress.toString() + ",";
				}
			}
		}
		else {
			for (Iterator i = ccList.listIterator(); i.hasNext();) {
				s += i.next() + ",";
			}
		}
		return s.substring(0, s.length() - 1).trim();
	}

	public LinkedList getBCCs() {
		return bccList;
	}

	// Liefert die Recipients aus dem Message-Objekt, wenn das Message-Objekt mit loadFile erzeugt wurde.
	// Sonst werden die mit addRecipients hinzugefügten Empfünger geliefert.

	public String getBCCsAsString() throws MessagingException {
		String s = " ";

		if (messageReady) {
			Address[] addresses = message.getRecipients(MimeMessage.RecipientType.BCC);
			if (addresses != null) {
				for (Address aktAddress : addresses) {
					s += aktAddress.toString() + ",";
				}
			}
		}
		else {
			for (Iterator i = bccList.listIterator(); i.hasNext();) {
				s += i.next() + ",";
			}
		}
		return s.substring(0, s.length() - 1).trim();
	}

	/**
	 * setzt die eMail-Empfünger zurück
	 */
	public void clearRecipients() throws Exception {
		log(SOSClassUtil.getMethodName(), SOSLogger.DEBUG9);
		toList.clear();
		ccList.clear();
		bccList.clear();

		changed = true;
	}

	/**
	 * setzt die eMail-Attachments zurück
	 */
	public void clearAttachments() {
		attachmentList.clear();
		changed = true;
	}

	/**
	 * sendet eine Nachricht in den Ausgabestrom
	 * @param out Ausgabestrom
	 * @param s   beliebige Zeichenfolge
	 */
	private void sendLine(final BufferedReader in, final BufferedWriter out, String s) throws Exception {

		try {
			out.write(s + "\r\n");
			out.flush();
			s = in.readLine();
		}
		catch (Exception e) {
			throw new Exception(SOSClassUtil.getMethodName() + ": error occurred on sendLine: " + e.toString());
		}
	}

	/**
	 * sendet eine Nachricht in den Ausgabestrom
	 * @param out Ausgabestrom
	 * @param s   beliebige Zeichenfolge
	 */
	private void sendLine(final BufferedWriter out, final String s) throws Exception {

		try {
			out.write(s + "\r\n");
			out.flush();
		}
		catch (Exception e) {
			throw new Exception(SOSClassUtil.getMethodName() + ": error occurred on sendLine: " + e.toString());
		}
	}

	/**
	 * liefert einen mit &lt; und &gt; quotierten String
	 * @param host Hostname bzw. IP-Adresse des Mail-Servers
	 */
	public String getQuotedName(final String name) {

		if (name.indexOf('<') > -1 && name.indexOf('>') > -1) {
			return name;
		}
		else {
			return '<' + name + '>';
		}
	}

	/**
	* setzt den Timeout fr den Verbindungsaufbau zum Mail-Server
	 * @param timeout fr Verbindungsaufbau zum Mail-Server (inaktiv)
	 */
	public void setTimeout(final int timeout) throws Exception {
		this.timeout = timeout;
		this.initMessage();

	}

	/**
	 * liefert den Timeout fr den Verbindungsaufbau zum Mail-Server
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	* liefert die Sprache fr eMail-Mustertexte und Datums-/Zeitformate
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * setzt die Sprache fr eMail-Mustertexte und Datums-/Zeitformate
	 * @param language Sprache fr eMail-Muster (en, de, default:de)
	 */
	public void setLanguage(final String language) throws Exception {
		this.language = language;
		this.initLanguage();
	}

	/**
	 * liefert das Datumsformat fr Datumsersetzungen im Text der eMail
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * setzt das Datumsformat fr Datumsersetzungen im Text der eMail
	 * @param dateFormat Datumsformat, z.B. dd.MM.yyyy
	 */
	public void setDateFormat(final String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * liefert das Datums- und Zeitformat fr Datumsersetzungen im Text der eMail
	 */
	public String getDatetimeFormat() {
		return datetimeFormat;
	}

	/**
	 * setzt das Datums- und Zeitformat fr Datumsersetzungen im Text der eMail
	 * @param datetimeFormat Datumsformat, z.B. dd.MM.yyyy HH:mm:ss
	 */
	public void setDatetimeFormat(final String datetimeFormat) {
		this.datetimeFormat = datetimeFormat;
	}

	/**
	 * setzt das Encoding der eMail
	 * @param encoding
	 */
	public void setEncoding(final String encoding) {
		this.encoding = encoding;
		warn("encoding", encoding);
		changed = true;
	}

	/**
	 * liefert das Encoding der eMail
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	* setzt das Character-Set der eMail
	 * @param charset
	 */
	public void setCharset(final String charset) {
		this.charset = charset;
		warn("charset", charset);
		changed = true;
	}

	/**
	 * liefert das Character-Set der eMail
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * setzt den Content-Type der eMail
	 * @param contentType Content-Type der eMail
	 */
	public void setContentType(final String contentType) {
		this.contentType = contentType;
		warn("contentType", contentType);
		changed = true;
	}

	/**
	 * liefert den Content-Type der eMail
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * setzt den Content-Type fr Attachments
	 * @param attachmentContentType Default fr Content-Type der Attachments
	 */
	public void setAttachmentContentType(final String attachmentContentType) {
		this.attachmentContentType = attachmentContentType;
		warn("attachmentContentType", attachmentContentType);
		changed = true;

	}

	/**
	 * liefert den Content-Type fr Attachments
	 */
	public String getAttachmentContentType() {
		return attachmentContentType;
	}

	/**
	* @return Returns the host.
	*/
	public String getHost() {
		return host;
	}

	/**
	 * @return Returns the port.
	 */
	public String getPort() {
		return port;
	}

	/**
	 * setzt den Namen des Verzeichnisses für zwischengespeicherte eMails
	 * @param queueDir String Verzeichnis zum Zwischenspeichern von eMails
	 */
	public void setQueueDir(final String queueDir) {
		this.queueDir = queueDir;
	}

	/**
	 * liefert den Namen des Verzeichnisses für zwischengespeicherte eMails
	 */
	public String getQueueDir() {
		return queueDir;
	}

	/**
	 * setzt den Inhalt des Betreffs
	 * @param subject Betreff
	 */
	public void setSubject(final String subject) {
		this.subject = subject;
		warn("subject", subject);
		changed = true;

	}

	/**
	 * liefert den Inhalt des Betreffs
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * setzt die eMail-Adresse des Absenders
	 * @param from Absender der eMail
	 */
	public void setFrom(final String from) {
		this.from = from;
		warn("from", from);
		changed = true;

	}

	/**
	 * liefert die eMail-Adresse des Absenders
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * liefert den Namen des Absenders
	 */
	public String getFromName() {
		return fromName;
	}

	/**
	 * setzt den Namen des Absenders
	 * @param fromName Absender der eMail
	 */
	public void setFromName(final String fromName) {
		this.fromName = fromName;
		warn("fromName", fromName);
		changed = true;

	}

	/**
	 * setzt die eMail-Adresse für Rückantworten
	 * @param replyTo Empfünger für Rückantworten
	 */
	public void setReplyTo(final String replyTo) {
		this.replyTo = replyTo;
		warn("replyTo", replyTo);
		changed = true;

	}

	/**
	 * liefert die eMail-Adresse für Rückantworten
	 */
	public String getReplyTo() {
		return replyTo;
	}

	public void setBody(final String body) {
		this.body = body;
		warn("body", body);
		changed = true;

	}

	/**
	 * liefert den Inhalt des eMail-Texts
	 */
	public String getBody() {
		return body;
	}

	/**
	 * setzt den Namen der Applikation fr eMail-Einstellungen in der Tabelle der Einstellungen
	 * @param applicationMail Name der Applikation fr eMail-Einstellungen in der Einstellungstabelle
	 */
	public void setApplicationMail(final String applicationMail) {
		this.applicationMail = applicationMail;
	}

	/**
	 * liefert den Namen der Applikation fr eMail-Einstellungen in der Tabelle der Einstellungen
	 */
	public String getApplicationMail() {
		return applicationMail;
	}

	/**
	 * setzt den Namen der Sektion fr eMail-Einstellungen in der Tabelle der Einstellungen
	 * @param sectionMail Name der Sektion fr eMail-Einstellungen in der Einstellungstabelle
	 */
	public void setSectionMail(final String sectionMail) {
		this.sectionMail = sectionMail;
	}

	/**
	 * liefert den Namen der Sektion fr eMail-Einstellungen in der Tabelle der Einstellungen
	 */
	public String getSectionMail() {
		return sectionMail;
	}

	/**
	 * setzt den Namen der Applikation fr Mustertexte in der Tabelle der Einstellungen
	 * @param applicationMailTemplates Name der Applikation fr eMail-Muster in der Einstellungstabelle
	 */
	public void setApplicationMailTemplates(final String applicationMailTemplates) {
		this.applicationMailTemplates = applicationMailTemplates;
	}

	/**
	 * liefert den Namen der Applikation fr Mustertexte in der Tabelle der Einstellungen
	 */
	public String getApplicationMailTemplates() {
		return applicationMailTemplates;
	}

	/**
	 * setzt den Namen der Applikation fr eMail Vorlagen der Factory in der Tabelle der Einstellungen
	 * @param applicationMailTemplates Name der Applikation fr eMail Vorlagen der Factory in der Einstellungstabelle
	 */
	public void setApplicationMailTemplatesFactory(final String applicationMailTemplates) {
		applicationMailTemplatesFactory = applicationMailTemplates;
	}

	/**
	 * liefert den Namen der Applikation für eMail Vorlagen der Factory in der Tabelle der Einstellungen
	 */
	public String getApplicationMailTemplatesFactory() {
		return applicationMailTemplatesFactory;
	}

	/**
	 * setzt den Namen der Sektion fr Mustertexte in der Tabelle der Einstellungen
	 * @param sectionMailTemplates Name der Sektion fr eMail-Muster in der Einstellungstabelle
	 */
	public void setSectionMailTemplates(final String sectionMailTemplates) {
		this.sectionMailTemplates = sectionMailTemplates;
	}

	/**
	 * liefert den Namen der Sektion fr Mustertexte in der Tabelle der Einstellungen
	 */
	public String getSectionMailTemplates() {
		return sectionMailTemplates;
	}

	/**
	 * setzt den Namen der Sektion für eMail Vorlagen der Factory in der Tabelle der Einstellungen
	 * @param sectionMailTemplates Name der Sektion für eMail Vorlagen der Factory in der Einstellungstabelle
	 */
	public void setSectionMailTemplatesFactory(final String sectionMailTemplatesFactory) {
		this.sectionMailTemplatesFactory = sectionMailTemplatesFactory;
	}

	/**
	 * liefert den Namen der Sektion für eMail Vorlagen der Factory in der Tabelle der Einstellungen
	 */
	public String getSectionMailTemplatesFactory() {
		return sectionMailTemplatesFactory;
	}

	/**
	 * setzt den Namen der Sektion für Scripte in der Tabelle der Einstellungen
	 * @param sectionMailTemplates Name der Sektion für eMail-Scripte in der Einstellungstabelle
	 */
	public void setSectionMailScripts(final String sectionMailScripts) {
		this.sectionMailScripts = sectionMailScripts;
	}

	/**
	 * liefert den Namen der Sektion für eMail Scripte in der Tabelle der Einstellungen
	 */
	public String getSectionMailScripts() {
		return sectionMailScripts;
	}

	/**
	 * setzt den Namen der Applikation für Scripte in der Tabelle der Einstellungen
	 * @param sectionMailTemplates Name der Applikation für eMail-Scripte in der Einstellungstabelle
	 */
	public void setApplicationMailScripts(final String applicationMailScripts) {
		this.applicationMailScripts = applicationMailScripts;
	}

	/**
	 * liefert den Namen der Applikation für eMail Scripte in der Tabelle der Einstellungen
	 */
	public String getApplicationMailScripts() {
		return applicationMailScripts;
	}

	/**
	 * liefert den Namen der Tabelle mit Einstellungen
	 * @param tableSettings
	 */
	public void setTableSettings(final String tableSettings) {
		this.tableSettings = tableSettings;
	}

	/**
	 * liefert den Namen der Tabelle mit Einstellungen
	 */
	public String getTableSettings() {
		return tableSettings;
	}

	/**
	 * @return Returns the messageBytes.
	 */
	public byte[] getMessageBytes() {
		return messageBytes;
	}

	/**
	 * Email nicht senden sondern als bytes [] verfügbar machen (message_bytes)
	 * @param sendToOutputStream The sendToOutputStream to set.
	 */
	public void setSendToOutputStream(final boolean sendToOutputStream) {
		this.sendToOutputStream = sendToOutputStream;
	}

	/**
	 * @param attachmentEncoding The attachmentEncoding to set.
	 */
	public void setattachmentEncoding(final String attachmentEncoding) {
		this.attachmentEncoding = attachmentEncoding;
		warn("attachmentEncoding", attachmentEncoding);
		changed = true;

	}

	/**
	 * @return Returns the message.
	 */
	public MimeMessage getMessage() {
		return message;
	}

	/**
	* @return Returns the lastError.
	*/
	public String getLastError() {
		return lastError;
	}

	/**
	* @return Returns the attachmentEncoding.
	*/
	public String getAttachmentEncoding() {
		return attachmentEncoding;
	}

	/**
	* @return Returns the attachmentCharset.
	*/
	public String getAttachmentCharset() {
		return attachmentCharset;
	}

	/**
	 * @param attachmentCharset The attachmentCharset to set.
	 */
	public void setAttachmentCharset(final String attachmentCharset) {
		this.attachmentCharset = attachmentCharset;
		warn("attachmentCharset", attachmentCharset);
		changed = true;

	}

	/**
	 * @param attachmentEncoding The attachmentEncoding to set.
	 */
	public void setAttachmentEncoding(final String attachmentEncoding) {
		this.attachmentEncoding = attachmentEncoding;
		warn("attachmentEncoding", attachmentEncoding);
		changed = true;

	}

	public void setHost(final String host) throws Exception {
		this.host = host;
		this.initMessage();
	}

	public void setPassword(final String password) throws Exception {
		this.password = password;
		this.initMessage();
	}

	public void setUser(final String user) throws Exception {
		this.user = user;
		this.initMessage();
	}

	public void setPort(final String port) throws Exception {
		this.port = port;
		this.initMessage();
	}

	public void setPriorityHighest() throws MessagingException {
		message.setHeader("Priority", "urgent");
		message.setHeader("X-Priority", "1 (Highest)");
		message.setHeader("X-MSMail-Priority", "Highest");
		changed = true;

	}

	public void setPriorityHigh() throws MessagingException {
		message.setHeader("Priority", "urgent");
		message.setHeader("X-Priority", "2 (High)");
		message.setHeader("X-MSMail-Priority", "Highest");
		changed = true;
	}

	public void setPriorityNormal() throws MessagingException {
		message.setHeader("Priority", "normal");
		message.setHeader("X-Priority", "3 (Normal)");
		message.setHeader("X-MSMail-Priority", "Normal");
		changed = true;
	}

	public void setPriorityLow() throws MessagingException {
		message.setHeader("Priority", "non-urgent");
		message.setHeader("X-Priority", "4 (Low)");
		message.setHeader("X-MSMail-Priority", "Low");
		changed = true;
	}

	public void setPriorityLowest() throws MessagingException {
		message.setHeader("Priority", "non-urgent");
		message.setHeader("X-Priority", "5 (Lowest)");
		message.setHeader("X-MSMail-Priority", "Low");
		changed = true;
	}

	public void setAlternativeBody(final String alternativeBody) {
		this.alternativeBody = alternativeBody;
	}

	public void setAlternativeCharset(final String alternativeCharset) {
		this.alternativeCharset = alternativeCharset;
	}

	public void setAlternativeContentType(final String alternativeContentType) {
		this.alternativeContentType = alternativeContentType;
	}

	public String getQueuePraefix() {
		return queuePraefix;
	}

	public String getLastGeneratedFileName() {
		return lastGeneratedFileName;
	}

	public void setQueuePraefix(final String queuePraefix) {
		this.queuePraefix = queuePraefix;
	}

	public String getLoadedMessageId() {
		return loadedMessageId;
	}

	/**
	 * @param sosLogger The sosLogger to set.
	 */
	public void setSOSLogger(final SOSLogger sosLogger) {
		this.sosLogger = sosLogger;
	}

	private void warn(final String n, final String v) {
		if (messageReady) {
			try {
				log("...setting of " + n + "=" + v + " will not be used. Loaded Message will be sent unchanged.", SOSStandardLogger.WARN);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Nachricht mit nativen Mail-Funktionen versenden
	 *
	 * @throws java.lang.Exception
	 *  @deprecated Not further supported
	 */
	@Deprecated
	public String sendNative() throws Exception {
		Socket socket = null;
		String boundary = "DataSeparatorString";
		StringBuffer sb = new StringBuffer("");

		try {

			if (host == null || host.length() == 0) {
				throw new Exception(SOSClassUtil.getMethodName() + ": host has no value.");
			}

			if (port == null || port.length() == 0) {
				throw new Exception(SOSClassUtil.getMethodName() + ": port has no value.");
			}

			if (toList.isEmpty()) {
				throw new Exception(SOSClassUtil.getMethodName() + ": no recipient specified.");
			}

			if (from == null || from.length() == 0) {
				throw new Exception(SOSClassUtil.getMethodName() + ": no sender specified.");
			}

			socket = new Socket(host, Integer.parseInt(port));
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "8859_1"));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "8859_1"));

			sendLine(in, out, "HELO " + host);

			if (fromName != null && fromName.length() > 0 && from != null && from.length() > 0) {
				sendLine(in, out, "MAIL FROM: " + fromName + getQuotedName(from));
			}
			else
				if (from != null && from.length() > 0) {
					sendLine(in, out, "MAIL FROM: " + getQuotedName(from));
				}

			// if (!this.toList.isEmpty()) {
			// for (Enumeration e = this.toList.elements(); e.hasMoreElements();) {
			// sendLine(in, out, "RCPT TO: " + getQuotedName(e.nextElement().toString()) );
			// }
			// }

			sendLine(in, out, "DATA");
			sendLine(out, "MIME-Version: 1.0");

			if (fromName != null && fromName.length() > 0 && from != null && from.length() > 0) {
				sendLine(out, "From: " + fromName + getQuotedName(from));
			}
			else
				if (from != null && from.length() > 0) {
					sendLine(out, "From: " + getQuotedName(from));
				}

			if (replyTo != null && replyTo.length() > 0) {
				sendLine(out, "Reply-To: " + getQuotedName(replyTo));
			}

			if (!toList.isEmpty()) {
				sb = new StringBuffer("");
				for (ListIterator e = toList.listIterator(); e.hasNext();) {
					sb.append(getQuotedName(e.next().toString()));
					if (e.hasNext()) {
						sb.append(",");
					}
				}
				sendLine(out, "To: " + sb);
			}

			if (!ccList.isEmpty()) {
				sb = new StringBuffer("");
				for (ListIterator e = ccList.listIterator(); e.hasNext();) {
					sb.append(getQuotedName(e.next().toString()));
					if (e.hasNext()) {
						sb.append(",");
					}
				}
				sendLine(out, "Cc: " + sb);
			}

			if (!bccList.isEmpty()) {
				sb = new StringBuffer("");
				for (ListIterator e = bccList.listIterator(); e.hasNext();) {
					sb.append(getQuotedName(e.next().toString()));
					if (e.hasNext()) {
						sb.append(",");
					}
				}
				sendLine(out, "Bcc: " + sb);
			}

			if (subject != null) {
				sendLine(out, "Subject: " + subject);
			}

			sendLine(out, "Content-Type: multipart/mixed; boundary=\"" + boundary + "\"");
			sendLine(out, "\r\n--" + boundary);

			// send the body

			if (contentType != null && contentType.length() > 0) {
				sendLine(out, "Content-Type: text/html; charset=\"" + charset + "\"");
			}

			if (encoding != null) {
				sendLine(out, "Content-Transfer-Encoding: " + encoding);
			}

			sendLine(out, "\r\n" + body + "\r\n\r\n");

			// send the attachments

			if (!attachmentList.isEmpty()) {
				for (Iterator i = attachmentList.values().iterator(); i.hasNext();) {
					String[] attachment = new String[2];
					attachment = (String[]) i.next();

					sendLine(out, "\r\n--" + boundary);
					sendLine(out, "Content-Type: " + attachment[1] + "; name=\"" + new File(attachment[0]).getName() + "\"");
					sendLine(out, "Content-Disposition: attachment; filename=\"" + attachment[0] + "\"");
					sendLine(out, "Content-Transfer-Encoding: " + attachmentEncoding + "\r\n");
					SOSMimeBase64.encode(attachment[0], out);
				}
			}

			sendLine(out, "\r\n\r\n--" + boundary + "--\r\n");
			sendLine(in, out, ".");
			sendLine(in, out, "QUIT");

		}
		catch (Exception e) {
			throw new Exception(SOSClassUtil.getMethodName() + ": error occurred on send: " + e.toString());
		}
		finally {
			if (socket != null) {
				socket.close();
			}
		}

		return sb.toString();
	}

	/**
	 * Nachricht versenden mit hostWare
	 *
	 *  @deprecated Not further supported
	  * @throws java.lang.Exception
	 */
	@Deprecated
	public String sendHostware() throws Exception {

//		sos.hostware.File file = new sos.hostware.File();
		StringBuffer sb;
throw new JSNotImplementedException("hostware is no longer supported");
	}

	public static void main(final String[] args) throws Exception {
		// SOSMail sosMail = new SOSMail("dod.sos","25","sos","sos");

		SOSMail sosMail = new SOSMail("smtp.sos");

		sosMail.setPriorityLowest();

		sosMail.setQueueDir("c:/");

		sosMail.setFrom("xyz@sos-berlin.com");
		// sosMail.setContentType("text/plain");
		sosMail.setEncoding("8bit");
		sosMail.setattachmentEncoding("Base64");

		// sosMail.addRecipient("xyz@sos-berlin.com");

		// sosMail.setCharset("us-ascii");
		sosMail.setSubject("Betreff");
		sosMail.setReplyTo("xyz@sos-berlin.com");
		String s = "Hello\\nWorld";
		sosMail.setBody(s);
		sosMail.addRecipient("xyz@sos-berlin.com");
		// sosMail.addAttachment("c:\\windows\\sos.ini");

		// sosMail.setAlternativeBody("Nachricht2");

		// sosMail.dumpMessageToFile(new File("c:/3.msg"),true);
		SOSStandardLogger sosLogger = new SOSStandardLogger(SOSStandardLogger.DEBUG9);
		sosMail.setSOSLogger(sosLogger);

		sosMail.setPriorityLowest();
		// sosMail.getMessage().setHeader("Return-Path","<>");
		// sosMail.getMessage().setHeader("X-SOSMAIL-delivery-counter","1");

		if (!sosMail.send()) {
			sosMail.log(SOSClassUtil.getMethodName() + "-->" + sosMail.getLastError(), SOSLogger.WARN);
		}
		else {
			// sosMail.dumpMessageToFile(new File("c:/4.msg"),false);
			// sosMail.log("==>Original-Message-id Vergleiche 4.msg:" + sosMail.getMessage().getMessageID(),SOSStandardLogger.INFO);
		}

		sosMail.clearRecipients();

	}

	/**
	 * send mail
	 *
	 * @param recipient
	 * @param recipientCC carbon copy recipient
	 * @param recipientBCC blind carbon copy recipient
	 * @param subject
	 * @param body
	 * @throws Exception
	 */
	public void sendMail(final ISOSSmtpMailOptions pobjO) throws Exception {

		final String strDelims = ",|;";
		try {
			SOSMail sosMail = this;
			sosMail.init();
			sosMail.setHost(pobjO.gethost().Value()); // = new SOSMail(pobjO.gethost().Value());
			sosMail.setPort(pobjO.getport().Value());

			sosMail.setQueueDir(pobjO.getqueue_directory().Value());
			sosMail.setFrom(pobjO.getfrom().Value());
			sosMail.setContentType(pobjO.getcontent_type().Value());
			sosMail.setEncoding(pobjO.getencoding().Value());

			String recipient = pobjO.getto().Value();
			String recipients[] = recipient.trim().split(strDelims);
			for (String recipient2 : recipients) {
				sosMail.addRecipient(recipient2.trim());
			}

			String recipientCC = pobjO.getcc().Value();
			if (recipientCC.trim().length() > 0) {
				String recipientsCC[] = recipientCC.trim().split(strDelims);
				for (String element : recipientsCC) {
					sosMail.addCC(element.trim());
				}
			}

			String recipientBCC = pobjO.getbcc().Value().trim();
			if (recipientBCC.length() > 0) {
				String recipientsBCC[] = recipientBCC.trim().split(strDelims);
				for (String element : recipientsBCC) {
					sosMail.addBCC(element.trim());
				}
			}

			String strAttachments = pobjO.getattachment().Value().trim();
			if (strAttachments.length() > 0) {
				String strAttachmentsA[] = strAttachments.trim().split(strDelims);
				for (String element : strAttachmentsA) {
					sosMail.addAttachment(element.trim());
				}
			}

			sosMail.setSubject(pobjO.getsubject().Value());
			sosMail.setBody(pobjO.getbody().Value());

			// SOSStandardLogger sosLogger = new SOSStandardLogger(SOSStandardLogger.DEBUG9);
			// sosMail.setSOSLogger(sosLogger);

			if (sosLogger != null) {
				sosLogger.debug("sending mail: \n" + sosMail.dumpMessageAsString());
			}
			if (!sosMail.send()) {
				if (sosLogger != null) {
					sosLogger.warn("mail server is unavailable, mail for recipient [" + recipient + "] is queued in local directory [" + sosMail.getQueueDir()
							+ "]:" + sosMail.getLastError());
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new Exception("error occurred sending mail: " + e.getMessage());
		}
	}

}
