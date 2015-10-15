package com.sos.scheduler.model;

import static com.sos.scheduler.model.messages.JSMessages.JOM_D_0010;
import static com.sos.scheduler.model.messages.JSMessages.JOM_D_0020;
import static com.sos.scheduler.model.messages.JSMessages.JOM_D_0030;
import static com.sos.scheduler.model.messages.JSMessages.JOM_D_0040;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import sos.spooler.Variable_set;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.scheduler.model.answers.Answer;
import com.sos.scheduler.model.answers.ERROR;
import com.sos.scheduler.model.answers.HistoryEntry;
import com.sos.scheduler.model.answers.JSCmdBase;
import com.sos.scheduler.model.answers.Job;
import com.sos.scheduler.model.answers.Task;
import com.sos.scheduler.model.commands.JSCmdAddJobs;
import com.sos.scheduler.model.commands.JSCmdAddOrder;
import com.sos.scheduler.model.commands.JSCmdCheckFolders;
import com.sos.scheduler.model.commands.JSCmdClusterMemberCommand;
import com.sos.scheduler.model.commands.JSCmdCommands;
import com.sos.scheduler.model.commands.JSCmdEventsGet;
import com.sos.scheduler.model.commands.JSCmdJobChainModify;
import com.sos.scheduler.model.commands.JSCmdJobChainNodeModify;
import com.sos.scheduler.model.commands.JSCmdKillTask;
import com.sos.scheduler.model.commands.JSCmdLicenceUse;
import com.sos.scheduler.model.commands.JSCmdLockRemove;
import com.sos.scheduler.model.commands.JSCmdModifyHotFolder;
import com.sos.scheduler.model.commands.JSCmdModifyJob;
import com.sos.scheduler.model.commands.JSCmdModifyOrder;
import com.sos.scheduler.model.commands.JSCmdModifySpooler;
import com.sos.scheduler.model.commands.JSCmdParamGet;
import com.sos.scheduler.model.commands.JSCmdProcessClassRemove;
import com.sos.scheduler.model.commands.JSCmdRemoteSchedulerRemoteTaskClose;
import com.sos.scheduler.model.commands.JSCmdRemoteSchedulerStartRemoteTask;
import com.sos.scheduler.model.commands.JSCmdRemoveJobChain;
import com.sos.scheduler.model.commands.JSCmdRemoveOrder;
import com.sos.scheduler.model.commands.JSCmdScheduleRemove;
import com.sos.scheduler.model.commands.JSCmdSchedulerLogLogCategoriesReset;
import com.sos.scheduler.model.commands.JSCmdSchedulerLogLogCategoriesSet;
import com.sos.scheduler.model.commands.JSCmdShowCalendar;
import com.sos.scheduler.model.commands.JSCmdShowHistory;
import com.sos.scheduler.model.commands.JSCmdShowJob;
import com.sos.scheduler.model.commands.JSCmdShowJobChain;
import com.sos.scheduler.model.commands.JSCmdShowJobChains;
import com.sos.scheduler.model.commands.JSCmdShowJobs;
import com.sos.scheduler.model.commands.JSCmdShowOrder;
import com.sos.scheduler.model.commands.JSCmdShowState;
import com.sos.scheduler.model.commands.JSCmdShowTask;
import com.sos.scheduler.model.commands.JSCmdStartJob;
import com.sos.scheduler.model.commands.JSCmdSubsystemShow;
import com.sos.scheduler.model.commands.JSCmdSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles;
import com.sos.scheduler.model.commands.JSCmdTerminate;
import com.sos.scheduler.model.exceptions.JSCommandErrorException;
import com.sos.scheduler.model.objects.JSObjCluster;
import com.sos.scheduler.model.objects.JSObjCommands;
import com.sos.scheduler.model.objects.JSObjConfigurationDirectory;
import com.sos.scheduler.model.objects.JSObjConfigurationFile;
import com.sos.scheduler.model.objects.JSObjEnvironment;
import com.sos.scheduler.model.objects.JSObjHoliday;
import com.sos.scheduler.model.objects.JSObjHolidays;
import com.sos.scheduler.model.objects.JSObjInclude;
import com.sos.scheduler.model.objects.JSObjJob;
import com.sos.scheduler.model.objects.JSObjJobChain;
import com.sos.scheduler.model.objects.JSObjJobChainNodeEnd;
import com.sos.scheduler.model.objects.JSObjJobChainNodeJobChain;
import com.sos.scheduler.model.objects.JSObjJobChains;
import com.sos.scheduler.model.objects.JSObjJobSettings;
import com.sos.scheduler.model.objects.JSObjJobs;
import com.sos.scheduler.model.objects.JSObjLock;
import com.sos.scheduler.model.objects.JSObjMonthdays;
import com.sos.scheduler.model.objects.JSObjOrder;
import com.sos.scheduler.model.objects.JSObjParam;
import com.sos.scheduler.model.objects.JSObjParams;
import com.sos.scheduler.model.objects.JSObjProcessClass;
import com.sos.scheduler.model.objects.JSObjProcessClasses;
import com.sos.scheduler.model.objects.JSObjSchedule;
import com.sos.scheduler.model.objects.JSObjScript;
import com.sos.scheduler.model.objects.JSObjSecurity;
import com.sos.scheduler.model.objects.JSObjSpooler;
import com.sos.scheduler.model.objects.ObjectFactory;
import com.sos.scheduler.model.objects.Param;
import com.sos.scheduler.model.objects.Params;
import com.sos.scheduler.model.objects.Spooler;

/**
* \class SchedulerObjectFactory
*
* \brief SchedulerObjectFactory -
*
* \details
*
* \code
*   .... code goes here ...
* \endcode
*
* <p style="text-align:center">
* <br />---------------------------------------------------------------------------
* <br /> APL/Software GmbH - Berlin
* <br />##### generated by ClaviusXPress (http://www.sos-berlin.com) #########
* <br />---------------------------------------------------------------------------
* </p>
* \author KB
* @version $Id$
* \see reference
*
* Created on 18.01.2011 12:02:31
 */
/**
 * @author KB
 *
 */
public class SchedulerObjectFactory extends ObjectFactory implements Runnable {
	public static enum enu4What {
		remote_schedulers, all, folders, job_chains, job_chain_jobs, /* job-chains and jobs are requested */
		running, no_subfolders, log, task_queue
		/**/;
		public String Text() {
			String strT = this.name();
			return strT;
		}
	}
	private final String					conClassName			= "SchedulerObjectFactory";
	private LiveConnector					liveConnector			= null;
	private SchedulerHotFolder				liveFolder;
	private static final Class<Spooler>		conDefaultMarshaller	= Spooler.class;
	private static final Logger				logger					= Logger.getLogger(SchedulerObjectFactory.class);
	private SchedulerObjectFactoryOptions	objOptions				= new SchedulerObjectFactoryOptions();
	// static is due to:  http://jaxb.java.net/guide/Performance_and_thread_safety.html
	private static JAXBContext				jc;
	private static JAXBContext				jc4Answers				= null;

	private Unmarshaller					u;
	private Marshaller						objM;
	private SchedulerSocket					objSchedulerSocket		= null;
	private Unmarshaller					u4Answers				= null;
	private Marshaller						objM4Answers			= null;
	
	private String							strLastAnswer			= null;

	private final JSJobUtilities			objJSJobUtilities		= null;											// this;
	private boolean							useDefaultPeriod;

	public SchedulerObjectFactory() {
		super("com_sos_scheduler_model");
		initMarshaller(conDefaultMarshaller);
		useDefaultPeriod = false;
	}

	public SchedulerObjectFactory(final LiveConnector liveConnector) {
		this();
		this.liveConnector = liveConnector;
		liveFolder = createSchedulerHotFolder(liveConnector.getHotFolderHandle());
	}

	public String getLastAnswer() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getLastAnswer";

		return strLastAnswer;
	} // private String getLastAnswer

	@Override
	public void run() {
		// TODO Auto-generated method stub
	}

	private boolean isJSJobUtilitiesChanged() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::isJSJobUtilitiesChanged";

		boolean flgRet = false;
		if (objJSJobUtilities != this && objJSJobUtilities != null) {
			flgRet = true;
		}

		return flgRet;
	} // private boolean isJSJobUtilitiesChanged

	/**
	 *
	 * \brief run
	 *
	 * \details
	 *
	 * \return Answer
	 *
	 * @param pobjJSCmd
	 * @return
	 */
	public Answer run(final JSCmdBase pobjJSCmd) {
		final String conMethodName = conClassName + "::run";
		Answer objAnswer = null;
		String strT = pobjJSCmd.toXMLString();
		String strA = "";
		try {
			//logger.trace(Messages.getMsg("JOM_D_0010", conMethodName, strT)); // JOM_D_0010=%1$s: Request: %n%2$s
			logger.trace(JOM_D_0010.get(conMethodName, strT)); // JOM_D_0010=%1$s: Request: %n%2$s
			if (isJSJobUtilitiesChanged()) {
				// TODO Ausführen des Kommandos über das interne API
				// IJSCommands.
			}
			else {
				if (objOptions.TransferMethod.isTcp()) {
					// setting timeout to prevent hang up if Server is not available [SP]
					this.getSocket().setSoTimeout(30000);
					this.getSocket().sendRequest(strT);
					logger.trace("Request sended to JobScheduler:\n" + strT);
					strA = getSocket().getResponse();
					strLastAnswer = strA;
					logger.trace("Answer from JobScheduler:\n" + strLastAnswer);
					objAnswer = getAnswer(strA);
				}
				else
					if (objOptions.TransferMethod.isUdp()) {
						DatagramSocket udpSocket = null;
						int intPortNumber = 0;
						try {
							udpSocket = new DatagramSocket();
							intPortNumber = objOptions.PortNumber.value();
							InetAddress objInetAddress = objOptions.ServerName.getInetAddress();
							udpSocket.connect(objInetAddress, intPortNumber);
							if (strT.indexOf("<?xml") == -1) {
								// TODO Encoding variable and utf-8 as default
								strT = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + strT;
							}
							byte[] btyBuffer = strT.getBytes();
							udpSocket.send(new DatagramPacket(btyBuffer, btyBuffer.length, objInetAddress, intPortNumber));
						}
						catch (Exception e) {
							e.printStackTrace();
							throw e;
						}
						finally {
							if (udpSocket != null) {
//								logger.trace(Messages.getMsg("JOM_D_0020", objOptions.ServerName.toString(), intPortNumber)); // JOM_D_0020=Command
								logger.trace(JOM_D_0020.get(objOptions.ServerName.toString(), intPortNumber)); // JOM_D_0020=Command
								udpSocket.disconnect();
								udpSocket = null;
							}
						}
					}
					else {
						throw new JobSchedulerException(String.format("TransferMethod '%1$s' not supported (yet)", objOptions.TransferMethod.Value()));
					}
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException(String.format("%1$s: %2$s", conMethodName, e.getMessage()), e);
		}
		if (objAnswer != null) {
			// TODO in Answer den original-response des js als string ablegen.
			// TODO eigenstï¿½ndige BasisKlasse JSAnswerBase bauen. ableiten von JSCmdBase.
			if (objAnswer.getERROR() != null) {
				throw new JobSchedulerException(Messages.getMsg("JOM_E_0010") + "\n" +
						"command:\n" + strT + "\n" +
						"answer:\n" + strLastAnswer
						); // JOM_E_0010=JobScheduler responds an error due to
																						// an invalid or wrong command\n
			}
		}
		return objAnswer;
	} // private Object run
	
	public Answer getAnswerFromSpooler(sos.spooler.Spooler spooler, JSCmdBase pobjJSCmd) {
		String command = pobjJSCmd.toXMLString();
		strLastAnswer = spooler.execute_xml(command);
		logger.trace("Answer from JobScheduler:\n" + strLastAnswer);
		Answer objAnswer = getAnswer(strLastAnswer);
		if (objAnswer != null) {
		 
			if (objAnswer.getERROR() != null) {
				throw new JobSchedulerException(Messages.getMsg("JOM_E_0010") + "\n" + "command:\n" + command + "\n" + "answer:\n" + strLastAnswer);  
			}
		}
		return objAnswer;
		
	}

	/**
	 *
	 * \brief getAnswer
	 *
	 * \details
	 *
	 * \return Answer
	 *
	 * @param pXMLStr
	 * @return
	 */
	public Answer getAnswer(final String pXMLStr) {
		final String conMethodName = conClassName + "::getAnswer";
		if (JSCmdBase.flgLogXML == true) {
			logger.trace(String.format("%1$s: Response: %n%2$s", conMethodName, pXMLStr));
		}
		Answer objAnswer = null;
		com.sos.scheduler.model.answers.Spooler objSpooler = null;
		try {
			/**
			 * den speziellen Context fü½r die Answer brauchen wir, solange die Answer nicht in der scheduler.xsd enthalten ist.
			 * dadurch ist das dann ein anderer Namespace und das passt nicht zusammen.
			 */
			if (jc4Answers == null) {
				jc4Answers = JAXBContext
						.newInstance(com.sos.scheduler.model.answers.Spooler.class);
				// create an Unmarshaller
			}
			if (u4Answers == null) {
				u4Answers = jc4Answers.createUnmarshaller();
			}
			objSpooler = (com.sos.scheduler.model.answers.Spooler) u4Answers.unmarshal(new StringReader(pXMLStr));
			if (objM4Answers == null) {
				objM4Answers = jc4Answers.createMarshaller();
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException(String.format("can't get answer object from %n%1$s", pXMLStr), e);
		}
		if (objSpooler != null) {
			objAnswer = objSpooler.getAnswer();
		}
		return objAnswer;
	} // public Answer getAnswer

	public void initMarshaller(final Class<?> objC) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::initMarshaller";
		try {
			if (jc == null) {
				jc = JAXBContext.newInstance(objC);
			}
			// create an Unmarshaller
			u = jc.createUnmarshaller();
			objM = jc.createMarshaller();
		}
		catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} // private void initMarshaller
	
	
	public void initAnswerMarshaller() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::initAnswerMarshaller";
		try {
			if (jc4Answers == null) {
				jc4Answers = JAXBContext
						.newInstance(com.sos.scheduler.model.answers.Spooler.class);
				// create an Unmarshaller
			}
			if (u4Answers == null) {
				u4Answers = jc4Answers.createUnmarshaller();
			}
			if (objM4Answers == null) {
				objM4Answers = jc4Answers.createMarshaller();
			}
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * \brief unMarshall
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param pobjFile
	 * @return
	 */
	public Object unMarshall(final File pobjFile) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::unMarshall";
		Object objC = null;
		try {
			objC = u.unmarshal(new FileInputStream(pobjFile));
		}
		catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		return objC;
	} // private Object unMarshall

	public Object unMarshall(final ISOSVirtualFile pobjVirtualFile) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::unMarshall";
		Object objC = null;
		if (pobjVirtualFile != null) {
			InputStream objInputStream = pobjVirtualFile.getFileInputStream();
			try {
				if (objInputStream == null) {
					logger.error(String.format("can't get inputstream for file '%1$s'.", pobjVirtualFile.getName()));
					throw new JobSchedulerException(String.format("can't get inputstream for file '%1$s'.", pobjVirtualFile.getName()));
				}
				objC = u.unmarshal(objInputStream);
			}
			catch (JAXBException e) {
				e.printStackTrace();
				throw new JobSchedulerException("", e);
			}
			finally {
				pobjVirtualFile.closeInput();
			}
		}
		else {
			logger.error("pobjVirtualFile is null");
			throw new JobSchedulerException("pobjVirtualFile is null");
		}
		return objC;
	} // private Object unMarshall

	public Object unMarshall(final InputStream pobjInputStream) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::unMarshall";
		Object objC = null;
		try {
			objC = u.unmarshal(pobjInputStream);
		}
		catch (JAXBException e) {
			e.printStackTrace();
			throw new JobSchedulerException("", e);
		}
		return objC;
	} // private Object unMarshall

	public Object unMarshall(final String pstrXMLContent) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::unMarshall";
		Object objC = null;
		try {
			objC = u.unmarshal(new StringReader(pstrXMLContent));
		}
		catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new JobSchedulerException("", e);
		}
		return objC;
	} // private Object unMarshall

	/**
	 * \brief marshal
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param objO
	 * @param objF
	 * @return
	 */
	public Object marshal(final Object objO, final File objF) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::marshal";
		try {
			// get an Apache XMLSerializer configured to generate CDATA
			XMLSerializer serializer = getXMLSerializer();
			FileOutputStream objFOS = new FileOutputStream(objF);
			serializer.setOutputByteStream(objFOS);
			// marshal using the Apache XMLSerializer
			objM.marshal(objO, serializer.asContentHandler());
			// objM.marshal(objO, objF);
		}
		catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new JobSchedulerException("", e);
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new JobSchedulerException("", e);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new JobSchedulerException("", e);
		}
		return objO;
	} // private SchedulerObjectFactoryOptions marshal

	public String marshal(final Object objO) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::marshal";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		try {
			// get an Apache XMLSerializer configured to generate CDATA
			XMLSerializer serializer = getXMLSerializer();

			// FileOutputStream objFOS = new FileOutputStream(objFos);
			serializer.setOutputByteStream(out);
			// marshal using the Apache XMLSerializer
			objM.marshal(objO, serializer.asContentHandler());
			// objM.marshal(objO, objF);
		}
		catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new JobSchedulerException("", e);
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new JobSchedulerException("", e);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new JobSchedulerException("", e);
		}
		return baos.toString();
	} // private SchedulerObjectFactoryOptions marshal

	/**
	 *
	 * \brief toXMLFile
	 *
	 * \details
	 *
	 * \return Object
	 *
	 * @param objO
	 * @param pobjVirtualFile
	 * @return
	 */
	public Object toXMLFile(final Object objO, final ISOSVirtualFile pobjVirtualFile) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::marshal";
		if (pobjVirtualFile != null) {
			OutputStream objOutputStream = pobjVirtualFile.getFileOutputStream();
			try {
				if (objOutputStream == null) {
					logger.error(String.format("can't get outputstream for file '%1$s'.", pobjVirtualFile.getName()));
					throw new JobSchedulerException(String.format("can't get outputstream for file '%1$s'.", pobjVirtualFile.getName()));
				}
				// get an Apache XMLSerializer configured to generate CDATA
				XMLSerializer serializer = getXMLSerializer();
				serializer.setOutputByteStream(objOutputStream);
				// marshal using the Apache XMLSerializer
				objM.marshal(objO, serializer.asContentHandler());
			}
			catch (JAXBException e) {
				e.printStackTrace();
				throw new JobSchedulerException("", e);
			}
			catch (IOException e) {
				e.printStackTrace();
				throw new JobSchedulerException("", e);
			}
			finally {
				pobjVirtualFile.closeOutput();
			}
		}
		else {
			logger.error("pobjVirtualFile is null");
			throw new JobSchedulerException("pobjVirtualFile is null");
		}
		return objO;
	} // private SchedulerObjectFactoryOptions marshal

	/**
	 * \brief toXMLString
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param objO
	 * @return
	 */
	private String toXMLString(final Object objO, final Marshaller objMarshaller) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::toXMLString";
		String strT = "";
		try {
			// get an Apache XMLSerializer configured to generate CDATA
			XMLSerializer serializer = getXMLSerializer();
			OutputFormat objOutputFormat = new OutputFormat();
			// TODO in die Optionclass
			objOutputFormat.setEncoding("utf-8");
			objOutputFormat.setIndenting(true);
			objOutputFormat.setIndent(4);
			objOutputFormat.setLineWidth(80);
			serializer.setOutputFormat(objOutputFormat);
			StringWriter objSW = new StringWriter();
			serializer.setOutputCharStream(objSW);
			// marshal using the Apache XMLSerializer
			objMarshaller.marshal(objO, serializer.asContentHandler());
			// objM.marshal(objO, objSW);
			strT = objSW.getBuffer().toString();
		}
		catch (JAXBException e) {
			e.printStackTrace();
			throw new JobSchedulerException("", e);
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new JobSchedulerException("", e);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new JobSchedulerException("", e);
		}
		return strT;
	} // private SchedulerObjectFactoryOptions marshal
	
	
	/**
	 * \brief toXMLString
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param objO
	 * @return
	 */
	public String toXMLString(final Object objO) {
		if(objO.getClass().getName().startsWith("com.sos.scheduler.model.answers.")) {
			return answerToXMLString(objO);
		}
		return toXMLString(objO, objM);
	} // public SchedulerObjectFactoryOptions toXMLString
	
	
	public String answerToXMLString(final Object objO) {
		initAnswerMarshaller();
		return toXMLString(objO, objM4Answers);
	} // public SchedulerObjectFactoryOptions answerToXMLString

	/**
	 * \brief getXMLSerializer
	 *
	 * \details
	 * configure an OutputFormat to handle CDATA and indenting
	 *
	 * \return XMLSerializer
	 */
	private XMLSerializer getXMLSerializer() {
		OutputFormat of = new OutputFormat();
		of.setCDataElements(new String[] { "^description", "^script", "^scheduler_script", "^log_mail_to", "^log_mail_cc", "^log_mail_bcc" });
		// TODO setIndenting should be an option
		of.setIndenting(true);
		XMLSerializer serializer = new XMLSerializer(of);
		return serializer;
	} // private XMLSerializer getXMLSerializer

	/**
	 *
	 * \brief Options
	 *
	 * \details
	 *
	 * \return SchedulerObjectFactoryOptions
	 *
	 * @return
	 */
	public SchedulerObjectFactoryOptions Options() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options";
		if (objOptions == null) {
			objOptions = new SchedulerObjectFactoryOptions();
		}
		return objOptions;
	} // private SchedulerObjectFactoryOptions Options

	/**
	 *
	 * \brief SchedulerObjectFactory
	 *
	 * \details
	 *
	 *
	 * @param pstrServerName
	 * @param pintPort
	 */
	public SchedulerObjectFactory(final String pstrServerName, final int pintPort) {
		this();
		this.Options().ServerName.Value(pstrServerName);
		this.Options().PortNumber.value(pintPort);
		initMarshaller(conDefaultMarshaller);
	}

	public SchedulerObjectFactory(final String pstrServerName, final int pintPort, final LiveConnector liveConnector) {
		this(pstrServerName, pintPort);
		this.liveConnector = liveConnector;
		liveFolder = createSchedulerHotFolder(liveConnector.getHotFolderHandle());
	}

	public SchedulerSocket getSocket() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getSocket";
		if (objSchedulerSocket == null) {
			try {
				logger.debug(objOptions.toXML());
				objSchedulerSocket = new SchedulerSocket(objOptions);
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new JobSchedulerException(e.getMessage(), e);
			}
		}
		return objSchedulerSocket;
	} // private SchedulerSocket getSocket

	public void closeSocket() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::closeSocket";

		if (objSchedulerSocket != null) {
			try {
				objSchedulerSocket.close();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			objSchedulerSocket = null;
		}

	} // private void closeSocket

	public Params setParams(final Variable_set pobjProperties) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setParams";

		String strParamValue = "";
		Params objParams = super.createParams();

		for (String strKey : pobjProperties.names().split(";")) {
			strParamValue = pobjProperties.value(strKey);
			Param objP = this.createParam(strKey, strParamValue);
			objParams.getParamOrCopyParamsOrInclude().add(objP);
		}
		return objParams;
	}
	/**
	 *
	 * \brief setParams
	 *
	 * \details
	 *
	 * \return Params
	 *
	 * @param pobjProperties
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Params setParams(final Properties pobjProperties) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setParams";

		String strParamValue = "";
		Params objParams = super.createParams();

		for (final Entry element : pobjProperties.entrySet()) {
			final Map.Entry<String, String> mapItem = element;
			String key = mapItem.getKey().toString();
			strParamValue = mapItem.getValue();
			Param objP = this.createParam(key, strParamValue);
			objParams.getParamOrCopyParamsOrInclude().add(objP);
		}

		return objParams;
	} // private Params setParams
	
	public Params setParams(final Map<String,String> params) {

		Params objParams = super.createParams();
		for (Entry<String,String> element : params.entrySet()) {
			Param param = this.createParam(element.getKey(), element.getValue());
			objParams.getParamOrCopyParamsOrInclude().add(param);
		}
		return objParams;
	} 

	/**
	 *
	 * \brief setParams
	 *
	 * \details
	 *
	 * \return Params
	 *
	 * @param pstrParamArray
	 * @return
	 */
	public Params setParams(final String[] pstrParamArray) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setParams";
		String strParamValue = "";
		Params objParams = super.createParams();
		for (int i = 0; i < pstrParamArray.length; i += 2) {
			String strParamName = pstrParamArray[i];
			if (i + 1 >= pstrParamArray.length) {
				strParamValue = "";
			}
			else {
				strParamValue = pstrParamArray[i + 1];
			}
			Param objP = this.createParam(strParamName, strParamValue);
			objParams.getParamOrCopyParamsOrInclude().add(objP);
		}
		return objParams;
	} // private Params setParams

	/**
	 * \creator JSObjParam
	 * \type Obj
	 * \brief JSObjParam
	 *
	 * \details
	 *
	 *
	 * \created 10.02.2011 10:39:56 by oh
	 */
	@Override
	public JSObjParam createParam() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createParam";
		JSObjParam objParam = new JSObjParam(this);
		return objParam;
	} // JSObjParam createParam()

	/**
	 * \creator JSObjParam
	 * \type Obj
	 * \brief JSObjParam
	 *
	 * \details
	 *
	 *
	 * \created 23.02.2012 10:39:56 by ss
	 */
	public JSObjParam createParam(final Param param) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createParam";
		JSObjParam objParam = new JSObjParam(this, param);
		return objParam;
	} // JSObjParam createParam()

	/**
	 *
	 * \brief createParam
	 *
	 * \details
	 *
	 * \return Param
	 *
	 * @param pstrParamName
	 * @param pstrParamValue
	 * @return
	 */
	public Param createParam(final String pstrParamName, final String pstrParamValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createParam";
		Param objP = super.createParam();
		objP.setName(pstrParamName);
		objP.setValue(pstrParamValue);
		return objP;
	} // private Param createParam

	/**
	 * \creator JSConfiguration
	 * \brief JSConfiguration - create empty JobScheduler configuration
	 *
	 * \details
	 * Create empty JobScheduler configuration
	 *
	 * \created 02.03.2011 10:31:30 by oh
	 *
	 * \return JSConfiguration
	 *
	 * @param pobjVirtualFile
	 * @return
	 */
	public JSConfiguration createJSConfiguration() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createJSConfiguration";
		JSConfiguration objJSC = new JSConfiguration(this);
		return objJSC;
	} // public JSConfiguration newJSConfiguration

	/**
	 * \creator JSConfiguration
	 * \brief JSConfiguration - load JobScheduler configuration from file
	 *
	 * \details
	 * Load JobScheduler configuration from file
	 *
	 * \created 02.03.2011 10:31:30 by oh
	 *
	 * \return JSConfiguration
	 *
	 * @param pobjVirtualFile
	 * @return
	 */
	public JSConfiguration createJSConfiguration(final ISOSVirtualFile pobjVirtualFile) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createJSConfiguration";
		JSConfiguration objJSC = new JSConfiguration(this, pobjVirtualFile);
		return objJSC;
	} // public JSConfiguration newJSConfiguration

	/**
	 * \creator SchedulerHotFolder
	 * \brief SchedulerHotFolder -
	 *
	 * \details
	 * Load Hot Folder objects
	 *
	 * \created 15.02.2011 17:16:36 by oh
	 */
	public SchedulerHotFolder createSchedulerHotFolder(final ISOSVirtualFile objDir) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createSchedulerHotFolder";
		SchedulerHotFolder objSchedulerHotFolder = new SchedulerHotFolder(this, objDir);
		return objSchedulerHotFolder;
	} // SchedulerHotFolder createSchedulerHotFolder()

	/**
	 * \creator JSObjJob
	 * \type Obj
	 * \brief JSObjJob - Create empty Job
	 *
	 * \details
	 * CreatorDescription
	 *
	 * \created 15.02.2011 17:01:37 by oh
	 */
	@Override
	public JSObjJob createJob() {
		JSObjJob objJob = new JSObjJob(this);
		return objJob;
	}

	public JSObjJob createJob(final String pstrJobName) {
		JSObjJob objJob = new JSObjJob(this);
		objJob.setName(pstrJobName);
		return objJob;
	}

	public JSObjJob createStandAloneJob(final String pstrJobName) {
		JSObjJob objJob = new JSObjJob(this);
		objJob.setName(pstrJobName);
		objJob.setOrder(false);
		return objJob;
	}

	/**
	 * \creator JSObjJob
	 * \type Obj
	 * \brief JSObjJob - Create Job from (virtual) file
	 *
	 * \details
	 * CreatorDescription
	 *
	 * \created 15.02.2011 17:01:37 by oh
	 */
	public JSObjJob createJob(final ISOSVirtualFile pobjVirtualFile) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createJob";
		JSObjJob objJob = new JSObjJob(this, pobjVirtualFile);
		return objJob;
	} // JSObjJob createJob()

	/**
	 * \creator JSObjParams
	 * \type Obj
	 * \brief JSObjParams - creates empty Params object
	 *
	 * \details
	 * Creates empty Params object which contains Param objects.
	 *
	 * \created 10.02.2011 10:44:29 by oh
	 */
	public JSObjParams createParams(final ISOSVirtualFile pobjVirtualFile) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createParams";
		JSObjParams objParams = new JSObjParams(this, pobjVirtualFile);
		return objParams;
	} // JSObjParams createParams()

	/**
	 * \creator JSObjParams
	 * \type Obj
	 * \brief JSObjParams - creates Params object from (virtual) file
	 *
	 * \details
	 * Creates Params object from (virtual) file containing Param objects.
	 *
	 * \created 10.02.2011 10:44:29 by oh
	 */
	@Override
	public JSObjParams createParams() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createParams";
		JSObjParams objParams = new JSObjParams(this);
		return objParams;
	} // JSObjParams createParams()

	/**
	 * \creator JSObjParams
	 * \type Obj
	 * \brief JSObjParams - creates Params object from (virtual) file
	 *
	 * \details
	 * Creates Params object from (virtual) file containing Param objects.
	 *
	 * \created 23.02.2012 10:44:29 by ss
	 */
	public JSObjParams createParams(final Params params) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createParams";
		JSObjParams objParams = new JSObjParams(this, params);
		return objParams;
	} // JSObjParams createParams()

	/**
	 * \creator JSObjJobChain
	 * \type Obj
	 * \brief JSObjJobChain - CreatorTitle
	 *
	 * \details
	 * CreatorDescription
	 *
	 * \created 15.02.2011 16:58:47 by oh
	 */
	@Override
	public JSObjJobChain createJobChain() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createJobChain";
		JSObjJobChain objJobChain = new JSObjJobChain(this);
		return objJobChain;
	} // JSObjJobChain createJobChain()

	/**
	 * \creator JSObjJobChain
	 * \type Obj
	 * \brief JSObjJobChain - CreatorTitle
	 *
	 * \details
	 * CreatorDescription
	 *
	 * \created 15.02.2011 17:01:37 by oh
	 */
	public JSObjJobChain createJobChain(final ISOSVirtualFile pobjVirtualFile) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createJobChain";
		JSObjJobChain objJobChain = new JSObjJobChain(this, pobjVirtualFile);
		return objJobChain;
	} // JSObjJobChain createJobChain()

	/**
	 * \creator JSObjJobChains
	 * \type Obj
	 * \brief JSObjJobChains - contains job chain objects
	 *
	 * \details
	 * Contains job chain objects
	 *
	 * \created 11.02.2011 13:10:31 by oh
	 */
	@Override
	public JSObjJobChains createJobChains() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createJobChains";
		JSObjJobChains objJobChains = new JSObjJobChains(this);
		return objJobChains;
	} // JSObjJobChains createJobChains()

	/**
	 * \creator JSObjJobChain
	 * \type Obj
	 * \brief JSObjJobChain - contains jobs or job chains
	 *
	 * \details
	 * contains jobs as jobChainNode, fileOrderSource or fileOrderSink objects or further job chain objects
	 *
	 * \created 10.02.2011 08:50:04 by oh
	 */
	// public JSObjJobChain createJobChain() {
	// @SuppressWarnings("unused")
	// final String conMethodName = conClassName + "::createJobChain";
	// JSObjJobChain objJobChain = new JSObjJobChain(this);
	// return objJobChain;
	// } // JSObjJobChain createJobChain()
	/**
	 * \creator JSObjJobChainNodeEnd
	 * \type Obj
	 * \brief JSObjJobChainNodeEnd - defines a final node of a job chain
	 *
	 * \details
	 * Defines a final node of a job chain. It is the same like a normal JobChainNode object without a job and nextState.
	 *
	 * \created 10.02.2011 08:59:04 by oh
	 */
	@Override
	public JSObjJobChainNodeEnd createJobChainNodeEnd() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createJobChainNodeEnd";
		JSObjJobChainNodeEnd objJobChainNodeEnd = new JSObjJobChainNodeEnd(this);
		return objJobChainNodeEnd;
	} // JSObjJobChainNodeEnd createJobChainNodeEnd()

	/**
	 * \creator JSObjJobChainNodeJobChain
	 * \type Obj
	 * \brief JSObjJobChainNodeJobChain - a job chain node which linked an other job chain
	 *
	 * \details
	 * A job chain node which linked an other job chain.
	 *
	 * \created 10.02.2011 09:02:48 by oh
	 */
	@Override
	public JSObjJobChainNodeJobChain createJobChainNodeJobChain() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createJobChainNodeJobChain";
		JSObjJobChainNodeJobChain objJobChainNodeJobChain = new JSObjJobChainNodeJobChain(this);
		return objJobChainNodeJobChain;
	} // JSObjJobChainNodeJobChain createJobChainNodeJobChain()

	/**
	 * \creator JSObjJobs
	 * \type Obj
	 * \brief JSObjJobs - contains job objects
	 *
	 * \details
	 * Contains job objects. This is not a Hot Folder object.
	 *
	 * \created 10.02.2011 09:05:51 by oh
	 */
	@Override
	public JSObjJobs createJobs() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createJobs";
		JSObjJobs objJobs = new JSObjJobs(this);
		return objJobs;
	} // JSObjJobs createJobs()

	/**
	 * \creator JSObjJobSettings
	 * \type Obj
	 * \brief JSObjJobSettings - contains email, logging and history settings
	 *
	 * \details
	 * Contains email, logging and history settings.
	 *
	 * \created 10.02.2011 09:17:01 by oh
	 */
	@Override
	public JSObjJobSettings createJobSettings() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createJobSettings";
		JSObjJobSettings objJobSettings = new JSObjJobSettings(this);
		return objJobSettings;
	} // JSObjJobSettings createJobSettings()

	/**
	 * \creator JSObjHolidays
	 * \type Obj
	 * \brief JSObjHolidays - contains holiday objects
	 *
	 * \details
	 * Contains holiday objects - days on which the JobScheduler should not run a job.
	 *
	 * \created 09.02.2011 17:34:25 by oh
	 */
	@Override
	public JSObjHolidays createHolidays() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createHolidays";
		JSObjHolidays objHolidays = new JSObjHolidays(this);
		return objHolidays;
	} // JSObjHolidays createHolidays()

	/**
	 * \creator JSObjHoliday
	 * \type Obj
	 * \brief JSObjHoliday - defines a holiday
	 *
	 * \details
	 * Defines a holiday - a day on which the JobScheduler should not run a job.
	 *
	 * \created 09.02.2011 17:35:25 by oh
	 */
	@Override
	public JSObjHoliday createHoliday() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createHoliday";
		JSObjHoliday objHoliday = new JSObjHoliday(this);
		return objHoliday;
	} // JSObjHoliday createHoliday()

	/**
	 * \creator JSObjMonthdays
	 * \type Obj
	 * \brief JSObjMonthdays - sets the operating period for a particular day of the month
	 *
	 * \details
	 * Sets the operating period for a particular day of the month.
	 *
	 * \created 10.02.2011 09:28:19 by oh
	 */
	@Override
	public JSObjMonthdays createMonthdays() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createMonthdays";
		JSObjMonthdays objMonthdays = new JSObjMonthdays(this);
		return objMonthdays;
	} // JSObjMonthDays createMonthDays()

	/**
	 * \creator JSObjLock
	 * \type Obj
	 * \brief JSObjLock - stops two tasks from running at the same time
	 *
	 * \details
	 * Stops two tasks from running at the same time
	 *
	 * \created 10.02.2011 09:23:39 by oh
	 */
	@Override
	public JSObjLock createLock() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createLock";
		JSObjLock objLock = new JSObjLock(this);
		return objLock;
	} // JSObjLock createLock()

	/**
	 * \creator JSObjLock
	 * \type Obj
	 * \brief JSObjLock - Create Lock from (virtual) file
	 *
	 * \details
	 * CreatorDescription
	 *
	 * \created 15.02.2011 17:01:37 by oh
	 */
	public JSObjLock createLock(final ISOSVirtualFile pobjVirtualFile) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createLock";
		JSObjLock objLock = new JSObjLock(this, pobjVirtualFile);
		return objLock;
	} // JSObjLock createLock()

	/**
	 * Create an instance of {@link JSObjCluster }
	 *
	 */
	@Override
	public JSObjCluster createCluster() {
		JSObjCluster objCluster = new JSObjCluster(this);
		return objCluster;
	}

	/**
	 * Create an instance of {@link JSCmdClusterMemberCommand }
	 *
	 */
	@Override
	public JSCmdClusterMemberCommand createClusterMemberCommand() {
		JSCmdClusterMemberCommand objCMC = new JSCmdClusterMemberCommand(this);
		return objCMC;
	}

	/**
	 * Create an instance of {@link JSObjSecurity }
	 *
	 */
	@Override
	public JSObjSecurity createSecurity() {
		JSObjSecurity objSecurity = new JSObjSecurity(this);
		return objSecurity;
	}

	/**
	 * \creator JSObjSpooler
	 * \type Obj
	 * \brief JSObjSpooler - creates empty spooler object
	 *
	 * \details
	 * Creates empty spooler object
	 *
	 * \created 02.03.2011 09:13:13 by oh
	 */
	@Override
	public JSObjSpooler createSpooler() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createSpooler";
		JSObjSpooler objSpooler = new JSObjSpooler(this);
		return objSpooler;
	} // JSObjSpooler createSpooler()

	/**
	 * \creator JSObjSpooler
	 * \type Obj
	 * \brief JSObjSpooler - Create spooler object from (virtual) file
	 *
	 * \details
	 * Create spooler object from (virtual) file
	 *
	 * \created 02.03.2011 09:13:13 by oh
	 */
	public JSObjSpooler createSpooler(final ISOSVirtualFile pobjVirtualFile) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createSpooler";
		JSObjSpooler objSpooler = new JSObjSpooler(this, pobjVirtualFile);
		return objSpooler;
	} // JSObjSpooler createSpooler()

	// **
	// * Create an instance of {@link RegisterRemoteScheduler }
	// *
	// */
	// public RegisterRemoteScheduler createRegisterRemoteScheduler() {
	// return new RegisterRemoteScheduler();
	// }
	//
	// /**
	// * Create an instance of {@link ServiceRequest }
	// *
	// */
	// public ServiceRequest createServiceRequest() {
	// return new ServiceRequest();
	// }
	//
	// /**
	// * Create an instance of {@link Ultimos }
	// *
	// */
	// public Ultimos createUltimos() {
	// return new Ultimos();
	// }
	//
	// /**
	// * Create an instance of {@link Monthdays }
	// *
	// */
	// public Monthdays createMonthdays() {
	// return new Monthdays();
	// }
	//
	// /**
	// * Create an instance of {@link com.sos.scheduler.model.objects.Weekdays }
	// *
	// */
	// public com.sos.scheduler.model.objects.Weekdays createWeekdays() {
	// return new com.sos.scheduler.model.objects.Weekdays();
	// }
	//
	// /**
	// * Create an instance of {@link Spooler.Config }
	// *
	// */
	// public Spooler.Config createSpoolerConfig() {
	// return new Spooler.Config();
	// }
	//
	// /**
	// * Create an instance of {@link Holidays.Weekdays }
	// *
	// */
	// public Holidays.Weekdays createHolidaysWeekdays() {
	// return new Holidays.Weekdays();
	// }
	//
	// /**
	// * Create an instance of {@link HttpAuthentication }
	// *
	// */
	// public HttpAuthentication createHttpAuthentication() {
	// return new HttpAuthentication();
	// }
	// /**
	// * Create an instance of {@link HttpAuthentication.HttpUsers }
	// *
	// */
	// public HttpAuthentication.HttpUsers createHttpAuthenticationHttpUsers() {
	// return new HttpAuthentication.HttpUsers();
	// }
	//
	/**
	 * \creator JSObjOrder
	 * \type Obj
	 * \brief JSObjOrder - creates empty Order
	 *
	 * \details
	 * Creates empty Order.
	 *
	 * \created 10.02.2011 10:28:05 by oh
	 */
	@Override
	public JSObjOrder createOrder() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createOrder";
		JSObjOrder objOrder = new JSObjOrder(this);
		return objOrder;
	} // JSObjOrder createOrder()

	/**
	 * \creator JSObjOrder
	 * \type Obj
	 * \brief JSObjOrder - creates Order from (virtual) file
	 *
	 * \details
	 * Creates Order from (virtual) file
	 *
	 * \created 15.02.2011 17:01:37 by oh
	 */
	public JSObjOrder createOrder(final ISOSVirtualFile pobjVirtualFile) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createOrder";
		JSObjOrder objOrder = new JSObjOrder(this, pobjVirtualFile);
		String s;
        try {
            s = pobjVirtualFile.getFile().getName();
            s = new File(s).getName();
            s = s.replaceFirst(".*,(.*)\\.order\\.xml","$1");
            
            objOrder.setId(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
		return objOrder;
	} // JSObjJob JSObjOrder()

	public JSObjHolidays createHolidays(final ISOSVirtualFile pobjVirtualFile) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createHolidays";
		JSObjHolidays objHolidays = new JSObjHolidays(this, pobjVirtualFile);
		return objHolidays;
	} // JSObjJob JSObjOrder()

	/**
	 * \creator Schedule
	 * \type Obj
	 * \brief JSObjSchedule - creates empty Schedule
	 *
	 * \details
	 * Creates empty Schedule
	 *
	 * \created 22.02.2011 12:01:58 by oh
	 */
	public JSObjSchedule createSchedule() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createSchedule";
		JSObjSchedule objSchedule = new JSObjSchedule(this);
		return objSchedule;
	} // JSObjSchedule createSchedule()

	/**
	 * \creator Schedule
	 * \type Obj
	 * \brief JSObjSchedule - creates Schedule from (virtual) file
	 *
	 * \details
	 * Creates Schedule from (virtual) file
	 *
	 * \created 22.02.2011 12:01:58 by oh
	 */
	public JSObjSchedule createSchedule(final ISOSVirtualFile pobjVirtualFile) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createSchedule";
		JSObjSchedule objSchedule = new JSObjSchedule(this, pobjVirtualFile);
		return objSchedule;
	} // JSObjSchedule createSchedule()

	/**
	 * \creator JSObjProcessClass
	 * \type Obj
	 * \brief JSObjProcessClass - defines an empty process class
	 *
	 * \details
	 * Defines a process class in which jobs can run.
	 *
	 * \created 10.02.2011 11:24:12 by oh
	 */
	@Override
	public JSObjProcessClass createProcessClass() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createProcessClass";
		JSObjProcessClass objProcessClass = new JSObjProcessClass(this);
		return objProcessClass;
	} // JSObjProcessClass createProcessClass()

	/**
	 * \creator JSObjProcessClass
	 * \type Obj
	 * \brief JSObjProcessClass - Create process class from (virtual) file
	 *
	 * \details
	 * Defines a process class from file in which jobs can run.
	 *
	 * \created 22.02.2011 11:21:31 by oh
	 */
	public JSObjProcessClass createProcessClass(final ISOSVirtualFile pobjVirtualFile) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createProcessClass";
		JSObjProcessClass objProcessClass = new JSObjProcessClass(this, pobjVirtualFile);
		return objProcessClass;
	} // JSObjProcessClass createProcessClass()

	/**
	 * \creator ProcessClasses
	 * \type Obj
	 * \brief JSObjProcessClasses - contains ProcessClass objects
	 *
	 * \details
	 * Contains ProcessClass objects. It is not a Hot Folder object.
	 *
	 * \created 10.02.2011 11:26:44 by oh
	 */
	@Override
	public JSObjProcessClasses createProcessClasses() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createProcessClasses";
		JSObjProcessClasses objProcessClasses = new JSObjProcessClasses(this);
		return objProcessClasses;
	} // JSObjProcessClasses createProcessClasses()

	// /**
	// * Create an instance of {@link RunTime }
	// *
	// */
	// public RunTime createRunTime() {
	// return new RunTime();
	// }
	//
	/**
	 * Create an instance of {@link JSObjEnvironment }
	 *
	 */
	@Override
	public JSObjEnvironment createEnvironment() {
		JSObjEnvironment objEnvironment = new JSObjEnvironment(this);
		return objEnvironment;
	}

	/**
	 * Create an instance of {@link JSCmdScheduleRemove }
	 *
	 */
	@Override
	public JSCmdScheduleRemove createScheduleRemove() {
		JSCmdScheduleRemove objScheduleRemove = new JSCmdScheduleRemove(this);
		return objScheduleRemove;
	}

	/**
	 * Create an instance of {@link JSCmdRemoteSchedulerStartRemoteTask }
	 *
	 */
	@Override
	public JSCmdRemoteSchedulerStartRemoteTask createRemoteSchedulerStartRemoteTask() {
		JSCmdRemoteSchedulerStartRemoteTask objRemoteSchedulerStartRemoteTask = new JSCmdRemoteSchedulerStartRemoteTask(this);
		return objRemoteSchedulerStartRemoteTask;
	}

	/**
	 * Create an instance of {@link JSCmdKillTask }
	 *
	 */
	@Override
	public JSCmdKillTask createKillTask() {
		JSCmdKillTask objKillTask = new JSCmdKillTask(this);
		return objKillTask;
	}

	// /**
	// * Create an instance of {@link Job.Description }
	// *
	// */
	// public Job.Description createJobDescription() {
	// return new Job.Description();
	// }
	//
	// /**
	// * Create an instance of {@link Job.LockUse }
	// *
	// */
	// public Job.LockUse createJobLockUse() {
	// return new Job.LockUse();
	// }
	//
	// /**
	// * Create an instance of {@link Params.CopyParams }
	// *
	// */
	// public Params.CopyParams createParamsCopyParams() {
	// return new Params.CopyParams();
	// }
	//
	// /**
	// * Create an instance of {@link Params.Include }
	// *
	// */
	// public Params.Include createParamsInclude() {
	// return new Params.Include();
	// }
	//
	/**
	 * Create an instance of {@link JSObjScript }
	 *
	 */
	@Override
	public JSObjScript createScript() {
		JSObjScript objScript = new JSObjScript(this);
		return objScript;
	}

	/**
	 * \creator JSObjInclude
	 * \type Obj
	 * \brief JSObjInclude - linked file for its parent object
	 *
	 * \details
	 * Linked file for its parent object like Script, Description, Holidays and Params
	 *
	 * \created 10.02.2011 08:40:51 by oh
	 */
	@Override
	public JSObjInclude createInclude() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createInclude";
		JSObjInclude objInclude = new JSObjInclude(this);
		return objInclude;
	} // JSObjInclude createInclude()

	// /**
	// * Create an instance of {@link Job.Process }
	// *
	// */
	// public Job.Process createJobProcess() {
	// return new Job.Process();
	// }
	//
	// /**
	// * Create an instance of {@link Job.Monitor }
	// *
	// */
	// public Job.Monitor createJobMonitor() {
	// return new Job.Monitor();
	// }
	//
	// /**
	// * Create an instance of {@link Job.StartWhenDirectoryChanged }
	// *
	// */
	// public Job.StartWhenDirectoryChanged createJobStartWhenDirectoryChanged() {
	// return new Job.StartWhenDirectoryChanged();
	// }
	//
	// /**
	// * Create an instance of {@link Job.DelayAfterError }
	// *
	// */
	// public Job.DelayAfterError createJobDelayAfterError() {
	// return new Job.DelayAfterError();
	// }
	//
	// /**
	// * Create an instance of {@link Job.DelayOrderAfterSetback }
	// *
	// */
	// public Job.DelayOrderAfterSetback createJobDelayOrderAfterSetback() {
	// return new Job.DelayOrderAfterSetback();
	// }
	//
	/**
	 * \creator JSObjCommands
	 * \type Obj
	 * \brief JSObjCommands - contains exit code dependent commands
	 *
	 * \details
	 * contains exit code dependent commands
	 *
	 * \created 09.02.2011 17:22:01 by oh
	 */
	@Override
	public JSObjCommands createCommands() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createCommands";
		JSObjCommands objCommands = new JSObjCommands(this);
		return objCommands;
	} // JSObjCommands createCommands()

	/**
	 * \creator JSObjConfigurationDirectory
	 * \type Obj
	 * \brief JSObjConfigurationDirectory - configure the Hot Folder path
	 *
	 * \details
	 * configure the Hot Folder path
	 *
	 * \created 09.02.2011 17:26:18 by oh
	 */
	@Override
	public JSObjConfigurationDirectory createConfigurationDirectory() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createConfigurationDirectory";
		JSObjConfigurationDirectory objConfigurationDirectory = new JSObjConfigurationDirectory(this);
		return objConfigurationDirectory;
	} // JSObjConfigurationDirectory createConfigurationDirectory()

	/**
	 * \creator JSObjConfigurationFile
	 * \type Obj
	 * \brief JSObjConfigurationFile
	 *
	 * \details
	 *
	 *
	 * \created 09.02.2011 17:31:59 by oh
	 */
	@Override
	public JSObjConfigurationFile createConfigurationFile() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createConfigurationFile";
		JSObjConfigurationFile objConfigurationFile = new JSObjConfigurationFile(this);
		return objConfigurationFile;
	} // JSObjConfigurationFile createConfigurationFile()

	/**
	 * \creator JSCmdSubsystemShow
	 * \type Cmd
	 * \brief JSCmdSubsystemShow - requests subsystems
	 *
	 * \details
	 * Requests subsystems (job, job_chain, order, schedule, lock, cluster, folder)
	 *
	 * \created 09.02.2011 13:51:32 by oh
	 */
	@Override
	public JSCmdSubsystemShow createSubsystemShow() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createSubsystemShow";
		JSCmdSubsystemShow objSubsystemShow = new JSCmdSubsystemShow(this);
		return objSubsystemShow;
	} // JSCmdSubsystemShow createSubsystemShow()

	/**
	 * Create an instance of {@link JSCmdStartJob }
	 *
	 */
	@Override
	public JSCmdStartJob createStartJob() {
		JSCmdStartJob objStartJobCmd = new JSCmdStartJob(this);
		return objStartJobCmd;
	}

	public JSCmdStartJob createStartJob(final String pstrJobName) {
		JSCmdStartJob objStartJobCmd = new JSCmdStartJob(this);
		objStartJobCmd.setJob(pstrJobName);
		return objStartJobCmd;
	}

	public JSCmdStartJob StartJob(final String pstrJobName) {
		JSCmdStartJob objStartJobCmd = new JSCmdStartJob(this);
		objStartJobCmd.setJob(pstrJobName);
		objStartJobCmd.setForce(true);
		objStartJobCmd.run();
		objStartJobCmd.getAnswerWithException();
		return objStartJobCmd;
	}

	public JSCmdStartJob StartJob(final String pstrJobName, final boolean raiseOk) {
		JSCmdStartJob objStartJobCmd = new JSCmdStartJob(this);
		objStartJobCmd.setJob(pstrJobName);
		objStartJobCmd.setForce(true);
		objStartJobCmd.run();
		objStartJobCmd.flgRaiseOKException = raiseOk;
		objStartJobCmd.getAnswerWithException();
		return objStartJobCmd;
	}

	/**
	 * Create an instance of {@link JSCmdShowCalendar }
	 *
	 */
	@Override
	public JSCmdShowCalendar createShowCalendar() {
		JSCmdShowCalendar objShowCalendarCmd = new JSCmdShowCalendar(this);
		return objShowCalendarCmd;
	}

	@Override
	public JSCmdShowState createShowState() {
		JSCmdShowState objShowStateCmd = new JSCmdShowState(this);
		return objShowStateCmd;
	}

	public JSCmdShowState createShowState(final enu4What enuWhat) {
		JSCmdShowState objShowStateCmd = new JSCmdShowState(this);
		objShowStateCmd.setWhat(enuWhat);
		return objShowStateCmd;
	}

	public JSCmdShowState createShowState(final enu4What[] enuWhat) {
		JSCmdShowState objShowStateCmd = new JSCmdShowState(this);
		objShowStateCmd.setWhat(enuWhat);
		return objShowStateCmd;
	}

	@Override
	public JSCmdShowJob createShowJob() {
		JSCmdShowJob objShowJobCmd = new JSCmdShowJob(this);
		return objShowJobCmd;
	}

	public JSCmdShowJob createShowJob(final String pstrJobName) {
		JSCmdShowJob objShowJobCmd = createShowJob();
		objShowJobCmd.setJob(pstrJobName);
		objShowJobCmd.setWhat(new JSCmdShowJob.enu4What[] { JSCmdShowJob.enu4What.log, JSCmdShowJob.enu4What.task_queue });
		objShowJobCmd.MaxTaskHistory(1);
		objShowJobCmd.MaxOrders(1);

		return objShowJobCmd;
	}

	public JSCmdShowJob createShowJob(final String pstrJobName, final JSCmdShowJob.enu4What[] penuWhat) {
		JSCmdShowJob objShowJobCmd = new JSCmdShowJob(this);
		objShowJobCmd.setJob(pstrJobName);
		objShowJobCmd.setWhat(penuWhat);
		objShowJobCmd.MaxTaskHistory(1);
		objShowJobCmd.MaxOrders(1);

		return objShowJobCmd;
	}

	public JSCmdShowJob getTaskQueue(final String pstrJobName) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getTaskQueue";

		JSCmdShowJob objShowJobCmd = createShowJob(pstrJobName, new JSCmdShowJob.enu4What[] { JSCmdShowJob.enu4What.task_queue });
		objShowJobCmd = executeShowJob(objShowJobCmd);
		return objShowJobCmd;

	} // private JSCmdShowJob getTaskQueue

	/**
	 *
	 * \brief isJobRunning
	 *
	 * \details
	 *
	 * \return JSCmdShowJob
	 *
	 * @param pstrJobName
	 * @return
	 */
	public JSCmdShowJob isJobRunning(final String pstrJobName) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::isJobRunning";

		JSCmdShowJob objShowJob = createShowJob(pstrJobName);
		return executeShowJob(objShowJob);
	}


	/**
	 *
	 * \brief getTaskLogFromShowHistory
	 *
	 * \details returns log from given Job/TaskId
	 *          if the job of the first parameter is running
	 *          then the log in the history is empty for the current taskId as second parameter
	 *          In this case use getTaskLogFromShowJob
	 *
	 * \return String log
	 *
	 * @param pstrJobName
	 * @param pintTaskId
	 * @return
	 */
	public String getTaskLogFromShowHistory(final String pstrJobName, final int pintTaskId) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getTaskLogFromHistory";

		String log = null;

		JSCmdShowHistory objHist = this.createShowHistory();
		objHist.setJob(pstrJobName);
		objHist.setId(BigInteger.valueOf(pintTaskId));
		objHist.setWhat("log");
		objHist.run();
		Answer objAnswer = objHist.getAnswer();
		List<HistoryEntry> objEntries = objAnswer.getHistory().getHistoryEntry();
		if (objEntries != null && objEntries.size() > 0) {
			HistoryEntry objEntry = objEntries.get(0);
			if (objEntry != null) {
				log = objEntry.getLog().getContent();
			}
		}

		return log;
	}


	/**
	 *
	 * \brief getTaskLogFromShowJob
	 *
	 * \details returns log from given Job/TaskId
	 *          You should use the current job with the current taskId otherwise
	 *          use getTaskLogFromShowHistory to get the log from other jobs or taskIds.
	 *
	 * \return String log
	 *
	 * @param pstrJobName
	 * @param pintTaskId
	 * @return
	 */
	public String getTaskLogFromShowJob(final String pstrJobName, final int pintTaskId) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getTaskLogFromShowJob";

		String log = null;

		JSCmdShowJob objShowJob = this.createShowJob();
		objShowJob.setJob(pstrJobName);
		objShowJob.setWhat(com.sos.scheduler.model.commands.JSCmdShowJob.enu4What.log);
		objShowJob.run();
		Answer objAnswer = objShowJob.getAnswer();
		Job objJobAnswer = objAnswer.getJob();
		List<Task> tasks = objJobAnswer.getTasks().getTask();
		for (Task task : tasks) {
			if (task.getId().compareTo(BigInteger.valueOf(pintTaskId)) == 0) {
				log = task.getLog().getContent();
				break;
			}
		}

		return log;
	}


	/**
	 *
	 * \brief getTaskLog
	 *
	 * \details returns log from given Job/TaskId
	 *          It looks into the show_history and in the show_job answer
	 *          If bUseCurrentTaskLog = true then at first in the show_job answer
	 *
	 * \return String log
	 *
	 * @param pstrJobName
	 * @param pintTaskId
	 * @param pbUseCurrentTaskLog
	 * @return
	 */
	public String getTaskLog(final String pstrJobName, final int pintTaskId, final boolean pbUseCurrentTaskLog) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getTaskLog";

		String log = null;
		if (pbUseCurrentTaskLog == true) {
			log = this.getTaskLogFromShowJob(pstrJobName, pintTaskId);
			if (log == null) {
				log = this.getTaskLogFromShowHistory(pstrJobName, pintTaskId);
			}
		}
		else {
			log = this.getTaskLogFromShowHistory(pstrJobName, pintTaskId);
			if (log == null) {
				log = this.getTaskLogFromShowJob(pstrJobName, pintTaskId);
			}
		}

		return log;

	} // getTaskLog


	public JSCmdShowJob executeShowJob(final JSCmdShowJob pobjShowJob) {

		boolean flgJobIsRunning = false;

		pobjShowJob.run();

		Answer objAnswer = pobjShowJob.getAnswer();
		if (objAnswer != null) {
			ERROR objError = objAnswer.getERROR();
			if (objError != null) {
				throw new JSCommandErrorException(objError.getText());
			}
			Job objJobAnswer = objAnswer.getJob();
			String strJobState = objJobAnswer.getState();
			String pstrJobName = pobjShowJob.getJobName();
			if (strJobState.equalsIgnoreCase("running") == false) {
//				logger.debug(Messages.getMsg("JOM_D_0030", pstrJobName, strJobState)); // JOM_D_0030=Job '%1$s' is *not* running, state =
				logger.debug(JOM_D_0030.get(pstrJobName, strJobState)); // JOM_D_0030=Job '%1$s' is *not* running, state =
																						// '%2$s'
				flgJobIsRunning = false;
			}
			else {
//				logger.debug(Messages.getMsg("JOM_D_0040", pstrJobName)); // JOM_D_0040=Job '%1$s' is running
				logger.debug(JOM_D_0040.get(pstrJobName)); // JOM_D_0040=Job '%1$s' is running
				int intNoOfTasks = objJobAnswer.getTasks().getCount().intValue();

				if (intNoOfTasks > 0) {
					flgJobIsRunning = true;
					// Task objTask = objJobAnswer.getTasks().getTask().get(0);
					// Log objTaskLog = objTask.getLog();
					// String strT = objTaskLog.getContent();
				}

			}

		}

		if (flgJobIsRunning == false) {
			return null;
		}
		return pobjShowJob;
	} // private boolean isJobRunning

	@Override
	public JSCmdShowJobs createShowJobs() {
		JSCmdShowJobs objShowJobsCmd = new JSCmdShowJobs(this);
		return objShowJobsCmd;
	}

	// public JSCmdShowJobs createShowJobs(enu4What enuWhat) {
	// JSCmdShowJobs objShowJobsCmd = new JSCmdShowJobs(this);
	// objShowJobsCmd.setWhat(enuWhat);
	// return objShowJobsCmd;
	// }
	// public JSCmdShowJobs createShowJobs(enu4What[] enuWhat) {
	// JSCmdShowJobs objShowJobsCmd = new JSCmdShowJobs(this);
	// objShowJobsCmd.setWhat(enuWhat);
	// return objShowJobsCmd;
	// }
	public JSCmdShowJobs createShowJobs(final enu4What... enuWhat) {
		JSCmdShowJobs objShowJobsCmd = new JSCmdShowJobs(this);
		objShowJobsCmd.setWhat(enuWhat);
		return objShowJobsCmd;
	}

	@Override
	public JSCmdShowTask createShowTask() {
		JSCmdShowTask objShowTaskCmd = new JSCmdShowTask(this);
		return objShowTaskCmd;
	}

	public JSCmdShowTask createShowTask(final enu4What enuWhat) {
		JSCmdShowTask objShowTaskCmd = new JSCmdShowTask(this);
		objShowTaskCmd.setWhat(enuWhat);
		return objShowTaskCmd;
	}

	public JSCmdShowTask createShowTask(final enu4What[] enuWhat) {
		JSCmdShowTask objShowTaskCmd = new JSCmdShowTask(this);
		objShowTaskCmd.setWhat(enuWhat);
		return objShowTaskCmd;
	}

	@Override
	public JSCmdShowOrder createShowOrder() {
		JSCmdShowOrder objShowOrderCmd = new JSCmdShowOrder(this);
		return objShowOrderCmd;
	}

	@Override
	public JSCmdShowJobChain createShowJobChain() {
		JSCmdShowJobChain objShowJobChainCmd = new JSCmdShowJobChain(this);
		return objShowJobChainCmd;
	}

	@Override
	public JSCmdShowJobChains createShowJobChains() {
		JSCmdShowJobChains objShowJobChainsCmd = new JSCmdShowJobChains(this);
		return objShowJobChainsCmd;
	}

	@Override
	public JSCmdShowHistory createShowHistory() {
		JSCmdShowHistory objShowHistoryCmd = new JSCmdShowHistory(this);
		return objShowHistoryCmd;
	}

	/**
	 * Create an instance of {@link JSCmdModifyJob }
	 *
	 */
	@Override
	public JSCmdModifyJob createModifyJob() {
		JSCmdModifyJob objModifyJob = new JSCmdModifyJob(this);
		return objModifyJob;
	}

	/**
	 * \creator JSCmdJobChainModify
	 * \type Cmd
	 * \brief JSCmdJobChainModify - stops and unstops a job chain
	 *
	 * \details
	 * Stops and unstops a job chain
	 *
	 * \created 09.02.2011 13:42:11 by oh
	 */
	@Override
	public JSCmdJobChainModify createJobChainModify() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createJobChainModify";
		JSCmdJobChainModify objJobChainModify = new JSCmdJobChainModify(this);
		return objJobChainModify;
	} // JSCmdJobChainModify createJobChainModify()

	/**
	 * Create an instance of {@link JSCmdRemoveJobChain }
	 *
	 */
	@Override
	public JSCmdRemoveJobChain createRemoveJobChain() {
		JSCmdRemoveJobChain objRemoveJobChain = new JSCmdRemoveJobChain(this);
		return objRemoveJobChain;
	}

	/**
	 * Create an instance of {@link JSCmdProcessClassRemove }
	 *
	 */
	@Override
	public JSCmdProcessClassRemove createProcessClassRemove() {
		JSCmdProcessClassRemove objProcessClassRemove = new JSCmdProcessClassRemove(this);
		return objProcessClassRemove;
	}

	/**
	 * Create an instance of {@link JSCmdCheckFolders }
	 *
	 */
	@Override
	public JSCmdCheckFolders createCheckFolders() {
		JSCmdCheckFolders objCheckFolders = new JSCmdCheckFolders(this);
		return objCheckFolders;
	}

	/**
	 * Create an instance of {@link JSCmdModifySpooler }
	 *
	 */
	@Override
	public JSCmdModifySpooler createModifySpooler() {
		JSCmdModifySpooler objModifySpooler = new JSCmdModifySpooler(this);
		return objModifySpooler;
	}

	/**
	 * Create an instance of {@link JSCmdEventsGet }
	 *
	 */
	@Override
	public JSCmdEventsGet createEventsGet() {
		JSCmdEventsGet objEventsGet = new JSCmdEventsGet(this);
		return objEventsGet;
	}

	/**
	 * Create an instance of {@link JSCmdRemoveOrder }
	 *
	 */
	@Override
	public JSCmdRemoveOrder createRemoveOrder() {
		JSCmdRemoveOrder objRemoveOrder = new JSCmdRemoveOrder(this);
		return objRemoveOrder;
	}

	/**
	 * Create an instance of {@link JSCmdSchedulerLogLogCategoriesReset }
	 *
	 */
	@Override
	public JSCmdSchedulerLogLogCategoriesReset createSchedulerLogLogCategoriesReset() {
		JSCmdSchedulerLogLogCategoriesReset objSchedulerLogLogCategoriesReset = new JSCmdSchedulerLogLogCategoriesReset(this);
		return objSchedulerLogLogCategoriesReset;
	}

	/**
	 * \creator JSCmdTerminate
	 * \type Cmd
	 * \brief JSCmdTerminate - terminates one JobScheduler or all of the same cluster
	 *
	 * \details
	 * Terminates the current JobScheduler or all of its cluster.
	 *
	 * \created 09.02.2011 14:00:31 by oh
	 */
	@Override
	public JSCmdTerminate createTerminate() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createTerminate";
		JSCmdTerminate objTerminate = new JSCmdTerminate(this);
		return objTerminate;
	} // JSCmdTerminate createTerminate()

	/**
	 * Create an instance of {@link JSCmdRemoteSchedulerRemoteTaskClose }
	 *
	 */
	@Override
	public JSCmdRemoteSchedulerRemoteTaskClose createRemoteSchedulerRemoteTaskClose() {
		JSCmdRemoteSchedulerRemoteTaskClose objRemoteSchedulerRemoteTaskClose = new JSCmdRemoteSchedulerRemoteTaskClose(this);
		return objRemoteSchedulerRemoteTaskClose;
	}

	/**
	 * Create an instance of {@link JSCmdLockRemove }
	 *
	 */
	@Override
	public JSCmdLockRemove createLockRemove() {
		JSCmdLockRemove objLockRemove = new JSCmdLockRemove(this);
		return objLockRemove;
	}

	/**
	 * \creator JSCmdSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles
	 * \type Cmd
	 * \brief JSCmdSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles - CreatorTitle
	 *
	 * \details
	 * CreatorDescription
	 *
	 * \created 09.02.2011 13:57:39 by oh
	 */
	@Override
	public JSCmdSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles createSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::createSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles";
		JSCmdSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles objSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles = new JSCmdSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles(
				this);
		return objSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles;
	} // JSCmdSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles createSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles()

	/**
	 * Create an instance of {@link JSCmdJobChainNodeModify }
	 *
	 */
	@Override
	public JSCmdJobChainNodeModify createJobChainNodeModify() {
		JSCmdJobChainNodeModify objJobChainNodeModify = new JSCmdJobChainNodeModify(this);
		return objJobChainNodeModify;
	}

	/**
	 * Create an instance of {@link JSCmdModifyOrder }
	 *
	 */
	@Override
	public JSCmdModifyOrder createModifyOrder() {
		JSCmdModifyOrder objModifyOrder = new JSCmdModifyOrder(this);
		return objModifyOrder;
	}

	/**
	 *
	 * \brief StartOrder
	 *
	 * \details
	 *
	 * \return JSCmdModifyOrder
	 *
	 * @param pstrJobChainName
	 * @param pstrOrderName
	 * @return
	 */
	public JSCmdModifyOrder StartOrder(final String pstrJobChainName, final String pstrOrderName) {
		JSCmdModifyOrder objModifyOrder = new JSCmdModifyOrder(this);
		objModifyOrder.setJobChain(pstrJobChainName);
		objModifyOrder.setOrder(pstrOrderName);
		objModifyOrder.setAt("now");
		objModifyOrder.run();
		objModifyOrder.getAnswerWithException();
		return objModifyOrder;
	}

	public JSCmdModifyOrder StartOrder(final String pstrJobChainName, final String pstrOrderName, final boolean raiseOk) {
		JSCmdModifyOrder objModifyOrder = new JSCmdModifyOrder(this);
		objModifyOrder.setJobChain(pstrJobChainName);
		objModifyOrder.setOrder(pstrOrderName);
		objModifyOrder.setAt("now");
		objModifyOrder.run();
		objModifyOrder.flgRaiseOKException = raiseOk;
		objModifyOrder.getAnswerWithException();
		return objModifyOrder;
	}

	/**
	 * Create an instance of {@link JSCmdParamGet }
	 *
	 */
	@Override
	public JSCmdParamGet createParamGet() {
		JSCmdParamGet objParamGet = new JSCmdParamGet(this);
		return objParamGet;
	}

	/**
	 * Create an instance of {@link JSCmdModifyHotFolder }
	 *
	 */
	@Override
	public JSCmdModifyHotFolder createModifyHotFolder() {
		JSCmdModifyHotFolder objModifyHotFolder = new JSCmdModifyHotFolder(this);
		return objModifyHotFolder;
	}

	/**
	 * Create an instance of {@link JSCmdAddJobs }
	 *
	 */
	@Override
	public JSCmdAddJobs createAddJobs() {
		JSCmdAddJobs objAddJobs = new JSCmdAddJobs(this);
		return objAddJobs;
	}

	/**
	 * Create an instance of {@link JSCmdAddOrder }
	 *
	 */
	public JSCmdAddOrder createAddOrder() {
		JSCmdAddOrder objAddOrder = new JSCmdAddOrder(this);
		return objAddOrder;
	}

	/**
	 * Create an instance of {@link JSCmdCommands }
	 *
	 */
	public JSCmdCommands createCmdCommands() {
		JSCmdCommands objCommands = new JSCmdCommands(this);
		return objCommands;
	}

	/**
	 * Create an instance of {@link JSCmdSchedulerLogLogCategoriesSet }
	 *
	 */
	@Override
	public JSCmdSchedulerLogLogCategoriesSet createSchedulerLogLogCategoriesSet() {
		JSCmdSchedulerLogLogCategoriesSet objSchedulerLogLogCategoriesSet = new JSCmdSchedulerLogLogCategoriesSet(this);
		return objSchedulerLogLogCategoriesSet;
	}

	/**
	 * Create an instance of {@link Spooler.Answer }
	 *
	 */
	@Override
	public Spooler.Answer createSpoolerAnswer() {
		return new Spooler.Answer();
	}

	/**
	 * Create an instance of {@link JSCmdLicenceUse }
	 *
	 */
	@Override
	public JSCmdLicenceUse createLicenceUse() {
		JSCmdLicenceUse objLicenceUse = new JSCmdLicenceUse(this);
		return objLicenceUse;
	}

	/**
	 * \brief If period element is missing or incomplete use a default period element
	 * @return
	 */
	public boolean useDefaultPeriod() {
		return useDefaultPeriod;
	}

	public void setUseDefaultPeriod(final boolean useDefaultPeriod) {
		this.useDefaultPeriod = useDefaultPeriod;
	}

	public SchedulerHotFolder getLiveFolderOrNull() {
		return liveFolder;
	}

	public ISOSVirtualFile getFileHandleOrNull(final String pstrFilename) {
		return liveConnector != null ? liveConnector.getFileSystemHandler().getFileHandle(pstrFilename) : null;
	}

}
