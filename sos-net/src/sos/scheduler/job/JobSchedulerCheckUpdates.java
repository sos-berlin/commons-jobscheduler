package sos.scheduler.job;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import sos.net.SOSFTP;
import sos.net.SOSMail;
import sos.settings.SOSProfileSettings;
import sos.xml.SOSXMLXPath;

/**
 * Checks for newer releases of the job scheduler
 * 
 * job parameters:
 * 
 * @author uwe.risse@sos-berlin.com
 * @since 1.0 2006-06-28
 */

public class JobSchedulerCheckUpdates extends JobSchedulerJob {

	private SchedulerUpdateRequest	updateRequest			= null;
	private SchedulerUpdateAnswer	schedulerUpdateAnswer	= null;
	private String					webserviceUrl			= "http://www.sos-berlin.com/check_for_update";
	private String					host					= "www.sos-berlin.com";
	private int						ftp_port				= -1;
	private String					user					= "anonymous";
	private String					password				= "";
	private boolean					passiveMode				= true;
	private String					transferMode			= "binary";
	private String					product					= "scheduler";
	private String					remoteDir				= "";
	private String					localDir				= "";
	private String					http_proxy				= "";
	private int						http_proxy_port			= -1;
	private String					ftp_proxy				= "";
	private int						ftp_proxy_port			= -1;

	private String					automatic_download		= "0";											//Default für automatic_download!!!!

	private File					transferFile			= null;
	private int						timeout					= 30000;
	// ======================================================
	class SchedulerUpdateRequest {

		SchedulerUpdateRequest() throws Exception {
			Properties se = System.getProperties();

			InetAddress localhost = InetAddress.getLocalHost();
			hostname = localhost.getHostName();
			ip = localhost.getHostAddress();

			os = se.getProperty("os.name");

			getRelease();

		}

		private String	hostname		= "";
		private String	ip				= "0.0.0";
		private String	release			= "";
		private String	new_release		= "";
		private String	downloaded_file	= "";
		private String	os				= "";
		private String	os_install		= "";

		private void getRelease() throws Exception {
			File f = new File("config/.version");
			if (!f.exists()) {
				release = "1.0.0.0";
				new_release = "";
				downloaded_file = "0";
				os_install = "*unknown*";

			}
			else {
				SOSProfileSettings s = new SOSProfileSettings("config/.version", "scheduler");
				os_install = s.getSectionEntry("os_install");
				release = s.getSectionEntry("release");
				new_release = s.getSectionEntry("new_release");
				downloaded_file = s.getSectionEntry("downloaded_file");
			}
		}
	}

	// ======================================================
	class SchedulerUpdateAnswer {
		SchedulerUpdateAnswer(final String xml) throws Exception {
			SOSXMLXPath xpathPayload = new sos.xml.SOSXMLXPath(new java.lang.StringBuffer(xml));
			release = xpathPayload.selectSingleNodeValue("//release");
			new_release = xpathPayload.selectSingleNodeValue("//new_release");
			os = xpathPayload.selectSingleNodeValue("//os");
			os_install = xpathPayload.selectSingleNodeValue("//os_install");
			automatic_download = xpathPayload.selectSingleNodeValue("//automatic_download");
			filename = xpathPayload.selectSingleNodeValue("//filename");
			update_needed = xpathPayload.selectSingleNodeValue("//update_needed");

		}

		private String	release				= "";
		private String	new_release			= "";
		private String	os					= "";
		private String	os_install			= "";
		private String	automatic_download	= "0";
		private String	update_needed		= "";
		private String	filename			= "";

	}

	// ======================================================
	/**
	 * Initialisation
	 */
	@Override
	public boolean spooler_init() {

		try {
			if (!super.spooler_init())
				return false;

			try { // to process job parameters
				if (spooler_task != null) {
					if (spooler_task.params().var("webserviceUrl") != null && spooler_task.params().var("webserviceUrl").length() > 0) {
						webserviceUrl = spooler_task.params().var("webserviceUrl");
					}

					localDir = spooler.directory();
					if (spooler_task.params().var("ftp_host") != null && spooler_task.params().var("ftp_host").length() > 0)
						host = spooler_task.params().var("ftp_host");
					if (spooler_task.params().var("ftp_port") != null && spooler_task.params().var("ftp_port").length() > 0)
						ftp_port = Integer.parseInt(spooler_task.params().var("ftp_port"));

					if (spooler_task.params().var("ftp_user") != null && spooler_task.params().var("ftp_user").length() > 0)
						user = spooler_task.params().var("ftp_user");
					if (spooler_task.params().var("ftp_password") != null && spooler_task.params().var("ftp_password").length() > 0)
						password = spooler_task.params().var("ftp_password");
					if (spooler_task.params().var("ftp_transfer_mode") != null && spooler_task.params().var("ftp_transfer_mode").length() > 0)
						transferMode = spooler_task.params().var("ftp_transfer_mode");
					if (spooler_task.params().var("ftp_passive_mode") != null && spooler_task.params().var("ftp_passive_mode").length() > 0)
						passiveMode = spooler_task.params().var("ftp_passive_mode").equals("1") ? true : false;
					if (spooler_task.params().var("ftp_remote_dir") != null && spooler_task.params().var("ftp_remote_dir").length() > 0)
						remoteDir = spooler_task.params().var("ftp_remote_dir");
					if (spooler_task.params().var("ftp_local_dir") != null && spooler_task.params().var("ftp_local_dir").length() > 0)
						localDir = spooler_task.params().var("ftp_local_dir");
					if (spooler_task.params().var("ftp_automatic_download") != null && spooler_task.params().var("ftp_automatic_download").length() > 0)
						automatic_download = spooler_task.params().var("ftp_automatic_download");
					if (spooler_task.params().var("product") != null && spooler_task.params().var("product").length() > 0)
						product = spooler_task.params().var("product");

					if (spooler_task.params().var("http_proxy") != null && spooler_task.params().var("http_proxy").length() > 0)
						http_proxy = spooler_task.params().var("http_proxy");

					if (spooler_task.params().var("http_proxy_port") != null && spooler_task.params().var("http_proxy_port").length() > 0)
						http_proxy_port = Integer.parseInt(spooler_task.params().var("http_proxy_port"));

					if (spooler_task.params().var("ftp_proxy") != null && spooler_task.params().var("ftp_proxy").length() > 0)
						ftp_proxy = spooler_task.params().var("ftp_proxy");

					if (spooler_task.params().var("ftp_proxy_port") != null && spooler_task.params().var("ftp_proxy_port").length() > 0)
						ftp_proxy_port = Integer.parseInt(spooler_task.params().var("ftp_proxy_port"));

					try {
						if (spooler_task.params().var("timeout") != null && spooler_task.params().var("timeout").length() > 0)
							timeout = Integer.parseInt(spooler_task.params().var("timeout"));
					}
					catch (NumberFormatException e) {
						spooler_log.warn("Value for timeout is not correct. Please set it to an integer");
						timeout = 30000;

					}

				}

			}
			catch (Exception e) {
				throw new Exception("an error occurred processing job parameters: " + e.getMessage());
			}

			return true;
		}
		catch (Exception e) {
			spooler_log_warn("failed to initialize job: " + e.getMessage());
			return false;
		}
	}

	/**
	 * check for files to post
	 */

	@Override
	public boolean spooler_open() {
		return true;
	}

	private void spooler_log_info(final String s) {

		if (spooler_log != null) {
			spooler_log.info(s);
		}
		else {
			System.out.println(s);
		}
	}

	private void spooler_log_info_and_state(final String s) {

		spooler_log_info(s);
		if (spooler != null) {
			spooler_job.set_state_text(s);
		}
	}

	private void spooler_log_warn(final String s) {

		if (spooler_log != null) {
			spooler_log.warn(s);
		}
		else {
			System.out.println(s);
		}
	}

	private void spooler_log_debug3(final String s) {

		if (spooler_log != null) {
			spooler_log.debug3(s);
		}
		else {
			System.out.println(s);
		}
	}

	/**
	 * Process single file
	 * 
	 * @throws Exception
	 * @throws IOException
	 * @throws FactoryConfigurationError
	 * @throws ParserConfigurationException
	 */
	@Override
	public boolean spooler_process() {
		try {
			sendRequest(buildRequest(), webserviceUrl);
			spooler_log_info("OS: " + schedulerUpdateAnswer.os);
			spooler_log_info("OS_INSTALL: " + schedulerUpdateAnswer.os_install);
			spooler_log_info("new_release: " + schedulerUpdateAnswer.new_release);
			spooler_log_info("old_release: " + schedulerUpdateAnswer.release);
			if (schedulerUpdateAnswer.update_needed.equals("1")) {
				spooler_log_info_and_state("You need an update");
				if (schedulerUpdateAnswer.automatic_download.equals("1") || schedulerUpdateAnswer.automatic_download.equalsIgnoreCase("true")) {
					//Avoid double downloads
					if (!schedulerUpdateAnswer.new_release.equals(updateRequest.new_release) || updateRequest.downloaded_file.equals("")) {
						downloadFile();
					}
					else {
						spooler_log_info_and_state("You have already downloaded the new release, but it has not been installed. See file "
								+ updateRequest.downloaded_file);
					}
				}
				else {
					spooler_log_info_and_state("Please download the latest release from http://www.sos-berlin.com/scheduler");
				}
				if (!schedulerUpdateAnswer.new_release.equals(updateRequest.new_release)) {
					sendEmail();
					updateIni();
				}
				else {
					spooler_log_info_and_state("You already have received mail about a new release, but the new release has not been installed");
				}
			}
			else {
				spooler_log_info_and_state("You run the latest release");
			}
			return false;

		}
		catch (Exception e) {
			spooler_log_warn("error while processing request: " + e.getMessage());
			return false;
		}
	}

	private void downloadFile() throws Exception {
		// Die aktuelle Version per FTP herunterladen
		boolean isLoggedIn = false;

		SOSFTP ftpClient = null;
		if (ftp_port == -1) {

			ftpClient = new SOSFTP(host);
		}
		else {
			ftpClient = new SOSFTP(host, ftp_port);
		}

		if (!ftp_proxy.equals("")) {
			System.getProperties().put("socksProxyPort", ftp_proxy_port);
			System.getProperties().put("socksProxyHost", ftp_proxy);
		}

		try {
			spooler_log_info_and_state("connected to host " + host + ", port " + ftp_port);
			spooler_job.set_state_text("connected to host " + host + ", port " + ftp_port);
			isLoggedIn = ftpClient.login(user, password);

			if (passiveMode)
				ftpClient.passive();
			if (transferMode.equalsIgnoreCase("ascii")) {
				ftpClient.ascii();
			}
			else {
				ftpClient.binary();
			}

			if (!remoteDir.equals(""))
				ftpClient.cd(remoteDir);
			ftpClient.cwd(localDir);
			spooler_log_info("get local directory: " + localDir + ", remote directory: " + remoteDir + " Filename:" + schedulerUpdateAnswer.filename);

			String fileName = schedulerUpdateAnswer.filename;
			transferFile = new File(localDir, fileName);
			spooler_log_info("The new release will be automatically downloaded: " + schedulerUpdateAnswer.new_release);
			spooler_log_info("File will be saved to: " + transferFile.getAbsolutePath());
			spooler_log_info_and_state("receiving file: " + transferFile.getAbsolutePath() + " with " + ftpClient.size(transferFile.getName()) + " bytes");

			ftpClient.getFile(transferFile.getName(), transferFile.getAbsolutePath());

			spooler_log_info_and_state("One file received:" + transferFile.getAbsolutePath() + " with " + ftpClient.size(transferFile.getName()) + " bytes");

		}
		catch (Exception e) {
			transferFile = null;
			spooler_log_warn("could not process file transfer: " + e.getMessage());
		}
		finally {
			if (ftpClient != null) {
				if (isLoggedIn)
					try {
						ftpClient.logout();
					}
					catch (Exception e) {
					} // no error handling
				if (ftpClient.isConnected())
					try {
						ftpClient.disconnect();
					}
					catch (Exception e) {
					} // no error handling
			}
		}

	}

	private void updateIni() {
		Writer fw = null;
		try {
			fw = new FileWriter("config/.version");
			fw.write("[scheduler]\n");
			fw.write("release=" + schedulerUpdateAnswer.release + "\n");
			fw.write("new_release=" + schedulerUpdateAnswer.new_release + "\n");
			fw.write("os_install=" + schedulerUpdateAnswer.os_install + "\n");
			if (transferFile != null) {
				fw.write("downloaded_file=" + transferFile.getAbsolutePath() + "\n");
			}
		}
		catch (IOException e) {
			System.err.println("error occurred processing file config/.version");
		}
		finally {
			if (fw != null)
				try {
					fw.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private void sendEmail() {
		// Dem Admin eine email schicken, dass ein neues Release vorliegt
		String emailAddress = "";
		try {
			if (spooler != null) {

				emailAddress = spooler_log.mail().to();

				if (emailAddress == null || emailAddress.equals("")) {
					spooler_log_warn("The  mail address for notification on automatic updates is missing");
				}
				else {
					// ------------------------------------------------------
					String contentType = "text/plain";

					SOSMail sosMail = new SOSMail(spooler_log.mail().smtp());

					sosMail.setFrom(spooler_log.mail().from());
					sosMail.setContentType(contentType);

					sosMail.addRecipient(emailAddress);
					String subject = "New release for Job Scheduler available";
					sosMail.setSubject(subject);
					sosMail.setReplyTo(spooler_log.mail().from());

					String body = "A new release of the Job Scheduler is available.\nYour version is: " + schedulerUpdateAnswer.release
							+ "\nThe new version is: " + schedulerUpdateAnswer.new_release;

					if (transferFile != null && schedulerUpdateAnswer.automatic_download.equals("1")
							|| schedulerUpdateAnswer.automatic_download.equalsIgnoreCase("true")) {
						body += "\nThe update has been downloaded to: " + transferFile.getAbsoluteFile();
					}
					sosMail.setBody(body);
					sosMail.send();
				}
			}
		}
		catch (Exception e) {
			spooler_log_warn("error sending mail to: " + emailAddress + " --> " + e.getMessage());
		}
	}

	/*
	 * <?xml version="1.0" encoding="UTF-8"?> <soapenv:Envelope
	 * xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"> <soapenv:Header>
	 * <wsa:To
	 * xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing">http://localhost:4455/check_for_update</wsa:To>
	 * <wsa:ReplyTo xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing">
	 * <wsa:Address>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</wsa:Address>
	 * </wsa:ReplyTo> </soapenv:Header> <soapenv:Body> <addOrder
	 * xmlns="http://www.sos-berlin.com/scheduler"> <jobchain>check_for_update</jobchain>
	 * <title>Scheduler: Check for Update</title> <xml_payload>
	 * <CheckForUpdateRequest> <hostname>myHost</hostname>  
	 * <release>1.2.3.4</release> <os>winxp</os> <os_install>windows</os_install> <product>scheduler</product>
	 * <automatic_download>1</automatic_download> </CheckForUpdateRequest>
	 * </xml_payload> </addOrder> </soapenv:Body> </soapenv:Envelope>
	 * 
	 */

	private void addNode(final Document answerXml, final Element e, final String field, final String value) {
		Element newElement = answerXml.createElement(field);
		Text textNode = answerXml.createTextNode(value);
		newElement.appendChild(textNode);
		e.appendChild(newElement);
	}

	private void addNodeNS(final String ns, final String uri, final Document answerXml, final Element e, final String field, final String value) {
		Element newElement = answerXml.createElement(field);
		Text textNode = answerXml.createTextNode(value);
		newElement.appendChild(textNode);
		newElement.setAttribute("xmlns:" + ns, uri);
		e.appendChild(newElement);
	}

	private String buildRequest() throws FactoryConfigurationError, Exception {
		spooler_log_debug3("....Building request");
		updateRequest = new SchedulerUpdateRequest();
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document requestXml = docBuilder.newDocument();

		Element envelope = requestXml.createElement("soapenv:Envelope");
		envelope.setAttribute("xmlns:soapenv", "http://schemas.xmlsoap.org/soap/envelope/");

		Element header = requestXml.createElement("soapenv:Header");
		addNodeNS("wsa", "http://schemas.xmlsoap.org/ws/2004/08/addressing", requestXml, header, "To", "value of to");
		addNodeNS("wsa", "http://schemas.xmlsoap.org/ws/2004/08/addressing", requestXml, header, "ReplyTo", "value of replyto");
		addNodeNS("wsa", "http://schemas.xmlsoap.org/ws/2004/08/addressing", requestXml, header, "Address", "value of address");
		envelope.appendChild(header);
		Element body = requestXml.createElement("soapenv:Body");
		envelope.appendChild(body);

		Element addOrder = requestXml.createElement("addOrder");
		addOrder.setAttribute("xmlns", "http://www.sos-berlin.com/scheduler");

		addNode(requestXml, addOrder, "jobchain", "value of jobchain");
		addNode(requestXml, addOrder, "title", "value of title");
		body.appendChild(addOrder);

		Element payload = requestXml.createElement("xml_payload");
		addOrder.appendChild(payload);

		Element request = requestXml.createElement("CheckForUpdateRequest");
		addNode(requestXml, request, "hostname", updateRequest.hostname);
		addNode(requestXml, request, "ip", updateRequest.ip);
		addNode(requestXml, request, "release", updateRequest.release);
		addNode(requestXml, request, "os", updateRequest.os);
		addNode(requestXml, request, "os_install", updateRequest.os_install);
		addNode(requestXml, request, "product", product);
		addNode(requestXml, request, "automatic_download", automatic_download);
		payload.appendChild(request);

		requestXml.appendChild(envelope);

		StringWriter out = new StringWriter();
		XMLSerializer serializer = new XMLSerializer(out, new OutputFormat(requestXml));
		serializer.serialize(requestXml);
		String xmlRequest = out.toString();
		spooler_log_debug3(xmlRequest);
		return xmlRequest;
	}

	private int sendRequest(final String contentType, final String url) throws Exception {
		int rc = 0;

		try {

			spooler_log_debug3("... Send request:" + url);
			PostMethod post = new PostMethod(url);
			post.setRequestEntity(new InputStreamRequestEntity(new ByteArrayInputStream(contentType.getBytes())));
			post.setRequestHeader("Content-type", "xml");

			HttpClient httpClient = new HttpClient();
			if (!http_proxy.equals("")) {
				httpClient.getHostConfiguration().setProxy(http_proxy, http_proxy_port);
			}
			httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
			httpClient.getHttpConnectionManager().getParams().setSoTimeout(timeout);

			// deprecated httpClient.setTimeout(30*1000);
			rc = httpClient.executeMethod(post);
			spooler_log_debug3("... request (flgOperationWasSuccessful):" + rc);

			getResponse(post.getResponseBodyAsStream());
			return rc;

		}
		catch (Exception e) {
			throw new Exception("could not connect to SOS Web Service, an error occurred in HTTP POST: " + e.getMessage());
		}
	}

	private void init_test() throws Exception {
		try {
			schedulerUpdateAnswer = new SchedulerUpdateAnswer("<xml/>");
		}
		catch (Exception e) {
		}
		schedulerUpdateAnswer.new_release = "1.3.6";
		try {
			updateRequest = new SchedulerUpdateRequest();
		}
		catch (Exception e) {
		}
		updateRequest.new_release = "1.3.5";

	}

	private void getResponse(final InputStream responseStream) throws Exception {
		spooler_log_debug3("... get response");
		if (responseStream == null)
			throw new Exception("cannot write response: response is null");

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			byte buffer[] = new byte[1000];
			int numOfBytes = 0;

			while ((numOfBytes = responseStream.read(buffer)) != -1)
				outputStream.write(buffer, 0, numOfBytes);

			spooler_log_info(outputStream.toString());
			schedulerUpdateAnswer = new SchedulerUpdateAnswer(outputStream.toString());

		}
		catch (Exception e) {
			throw new Exception("error occurred while logging: " + e.getMessage());
		}
		finally {
			try {
				if (responseStream != null) {
					responseStream.close();
				}
			}
			catch (Exception ex) {
			} //  ignore this error

		}
	}

	public static void main(final String[] args) throws Exception {
		// Diese Testroutine erzeugt einen Request und schreibt ihn nach Stdout
		JobSchedulerCheckUpdates x = new JobSchedulerCheckUpdates();
		x.init_test();
		if (x.schedulerUpdateAnswer.update_needed.equals("1")) {
			System.out.println("Neue Version da");
		}
		else {
			System.out.println("Version ist aktuell");
		}

		//x.spooler_process();
	}

}
