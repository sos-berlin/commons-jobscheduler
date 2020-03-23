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

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.sos.xml.SOSXmlCommand;

import sos.spooler.Variable_set;

public class SchedulerObjectFactory extends ObjectFactory implements Runnable {

    private static final int READ_TIME_OUT = 60;
    private static final int WRITE_TIME_OUT = 60;
    private static final Class<Spooler> DEFAULT_MARSHALLER = Spooler.class;
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerObjectFactory.class);
    private SchedulerObjectFactoryOptions objOptions = new SchedulerObjectFactoryOptions();
    private static JAXBContext jc;
    private static JAXBContext jc4Answers = null;
    private final JSJobUtilities objJSJobUtilities = null;
    private LiveConnector liveConnector = null;
    private SchedulerHotFolder liveFolder;
    private Unmarshaller u;
    private Marshaller objM;
    private SchedulerSocket objSchedulerSocket = null;
    private Unmarshaller u4Answers = null;
    private Marshaller objM4Answers = null;
    private String strLastAnswer = null;
    private boolean useDefaultPeriod;
    private boolean ommitXmlDeclaration = false;
    private sos.spooler.Spooler spooler;

    public void setOmmitXmlDeclaration(boolean ommitXmlDeclaration) {
        this.ommitXmlDeclaration = ommitXmlDeclaration;
    }

    public static enum enu4What {
        remote_schedulers, all, folders, job_chains, job_chain_jobs, running, no_subfolders, log, task_queue;

        public String getText() {
            String strT = this.name();
            return strT;
        }
    }

    public SchedulerObjectFactory(sos.spooler.Spooler spooler) {
        super("com_sos_scheduler_model");
        this.getOptions().setTransferMethod("api");
        initMarshaller(DEFAULT_MARSHALLER);
        this.spooler = spooler;
        useDefaultPeriod = false;
    }

    public SchedulerObjectFactory() {
        super("com_sos_scheduler_model");
        initMarshaller(DEFAULT_MARSHALLER);
        useDefaultPeriod = false;
    }

    public SchedulerObjectFactory(final LiveConnector liveConnector) {
        this();
        this.liveConnector = liveConnector;
        liveFolder = createSchedulerHotFolder(liveConnector.getHotFolderHandle());
    }

    public SchedulerObjectFactory(final String pstrServerName, final int pintPort) {
        this();
        this.getOptions().setTransferMethod("tcp");
        this.getOptions().ServerName.setValue(pstrServerName);
        this.getOptions().PortNumber.value(pintPort);
        initMarshaller(DEFAULT_MARSHALLER);
    }

    public SchedulerObjectFactory(String commandUrl) {
        this();
        this.getOptions().setTransferMethod("http");
        this.getOptions().commandUrl.setValue(commandUrl);
        initMarshaller(DEFAULT_MARSHALLER);
    }

    public SchedulerObjectFactory(String commandUrl, String basicAuthorization) {
        this();
        this.getOptions().setTransferMethod("https");
        this.getOptions().commandUrl.setValue(commandUrl);
        this.getOptions().basicAuthorization.setValue(basicAuthorization);
        initMarshaller(DEFAULT_MARSHALLER);
    }

    public SchedulerObjectFactory(final String pstrServerName, final int pintPort, final LiveConnector liveConnector) {
        this(pstrServerName, pintPort);
        this.liveConnector = liveConnector;
        liveFolder = createSchedulerHotFolder(liveConnector.getHotFolderHandle());
    }

    public String getLastAnswer() {
        return strLastAnswer;
    }

    @Override
    public void run() {
        //
    }

    private boolean isJSJobUtilitiesChanged() {
        boolean flgRet = false;
        if (objJSJobUtilities != null && !objJSJobUtilities.equals(this)) {
            flgRet = true;
        }
        return flgRet;
    }

    public Answer run(final JSCmdBase pobjJSCmd) {
        final String conMethodName = "SchedulerObjectFactory::run";
        Answer objAnswer = null;
        String xmlCommand = pobjJSCmd.toXMLString();
        try {
            LOGGER.trace(JOM_D_0010.get(conMethodName, xmlCommand));
            if (!isJSJobUtilitiesChanged()) {

                if (objOptions.TransferMethod.isApi()) {
                    objAnswer = getAnswerFromSpooler(pobjJSCmd);
                } else if (objOptions.TransferMethod.isHttp()) {
                    SOSXmlCommand sosXmlCommand= new SOSXmlCommand(objOptions.getCommandUrl().getValue()); 
                    LOGGER.trace("Request sended to JobScheduler:\n" + xmlCommand);
                    String answer = sosXmlCommand.executeXMLPost(xmlCommand);
                    LOGGER.trace("Answer from JobScheduler:\n" + answer);
                    objAnswer = getAnswer(answer);
                } else if (objOptions.TransferMethod.isHttps()) {
                    SOSXmlCommand sosXmlCommand= new SOSXmlCommand(objOptions.getCommandUrl().getValue()); 
                    LOGGER.trace("Request sended to JobScheduler:\n" + xmlCommand);
                    sosXmlCommand.setBasicAuthorization(objOptions.basicAuthorization.getValue());
                    sosXmlCommand.setConnectTimeout(WRITE_TIME_OUT);
                    sosXmlCommand.setReadTimeout(READ_TIME_OUT);
                    String answer = sosXmlCommand.executeXMLPost(xmlCommand);
                    LOGGER.trace("Answer from JobScheduler:\n" + answer);
                    objAnswer = getAnswer(answer);
                    
                } else if (objOptions.TransferMethod.isTcp()) {
                    this.getSocket().setSoTimeout(30000);
                    this.getSocket().sendRequest(xmlCommand);
                    LOGGER.trace("Request sended to JobScheduler:\n" + xmlCommand);
                    String answer = getSocket().getResponse();
                    LOGGER.trace("Answer from JobScheduler:\n" + answer);
                    objAnswer = getAnswer(answer);
                } else if (objOptions.TransferMethod.isUdp()) {
                    DatagramSocket udpSocket = null;
                    int intPortNumber = 0;
                    try {
                        udpSocket = new DatagramSocket();
                        intPortNumber = objOptions.PortNumber.value();
                        InetAddress objInetAddress = objOptions.ServerName.getInetAddress();
                        udpSocket.connect(objInetAddress, intPortNumber);
                        if (xmlCommand.indexOf("<?xml") == -1) {
                            xmlCommand = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + xmlCommand;
                        }
                        byte[] btyBuffer = xmlCommand.getBytes();
                        udpSocket.send(new DatagramPacket(btyBuffer, btyBuffer.length, objInetAddress, intPortNumber));
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                        throw e;
                    } finally {
                        if (udpSocket != null) {
                            LOGGER.trace(JOM_D_0020.get(objOptions.ServerName.toString(), intPortNumber));
                            udpSocket.disconnect();
                            udpSocket = null;
                        }
                    }
                } else {
                    throw new JobSchedulerException(String.format("TransferMethod '%1$s' not supported (yet)", objOptions.TransferMethod.getValue()));
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(String.format("%1$s: %2$s", conMethodName, e.getMessage()), e);
        }
        if (objAnswer != null && objAnswer.getERROR() != null) {
            throw new JobSchedulerException(Messages.getMsg("JOM_E_0010") + "\n" + "command:\n" + xmlCommand + "\n" + "answer:\n" + strLastAnswer);
        }
        return objAnswer;
    }

    public Answer getAnswerFromSpooler(JSCmdBase pobjJSCmd) {   
        String command = pobjJSCmd.toXMLString();
        strLastAnswer = spooler.execute_xml(command);
        LOGGER.trace("Answer from JobScheduler:\n" + strLastAnswer);
        Answer objAnswer = getAnswer(strLastAnswer);
        return objAnswer;
    }

    public Answer getAnswer(final String pXMLStr) {
        final String conMethodName = "SchedulerObjectFactory::getAnswer";
        if (JSCmdBase.flgLogXML == true) {
            LOGGER.trace(String.format("%1$s: Response: %n%2$s", conMethodName, pXMLStr));
        }
        Answer objAnswer = null;
        com.sos.scheduler.model.answers.Spooler objSpooler = null;
        try {
            if (jc4Answers == null) {
                jc4Answers = JAXBContext.newInstance(com.sos.scheduler.model.answers.Spooler.class);
            }
            if (u4Answers == null) {
                u4Answers = jc4Answers.createUnmarshaller();
            }
            objSpooler = (com.sos.scheduler.model.answers.Spooler) u4Answers.unmarshal(new StringReader(pXMLStr));
            if (objM4Answers == null) {
                objM4Answers = jc4Answers.createMarshaller();
            }
        } catch (Exception e) {
            throw new JobSchedulerException(String.format("can't get answer object from %n%1$s", pXMLStr), e);
        }
        if (objSpooler != null) {
            objAnswer = objSpooler.getAnswer();
        }
        return objAnswer;
    }

    public void initMarshaller(final Class<?> objC) {
        try {
            if (jc == null) {
                jc = JAXBContext.newInstance(objC);
            }
            u = jc.createUnmarshaller();
            objM = jc.createMarshaller();
        } catch (JAXBException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void initMarshaller(String context) {
        try {
            if (jc == null) {
                jc = JAXBContext.newInstance(context);
            }
            u = jc.createUnmarshaller();
            objM = jc.createMarshaller();
        } catch (JAXBException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void initAnswerMarshaller() {
        try {
            if (jc4Answers == null) {
                jc4Answers = JAXBContext.newInstance(com.sos.scheduler.model.answers.Spooler.class);
            }
            if (u4Answers == null) {
                u4Answers = jc4Answers.createUnmarshaller();
            }
            if (objM4Answers == null) {
                objM4Answers = jc4Answers.createMarshaller();
            }
        } catch (JAXBException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public Object unMarshall(final File pobjFile) {
        Object objC = null;
        try {
            try {
                objC = u.unmarshal(new FileInputStream(pobjFile));
            } catch (FileNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } catch (JAXBException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return objC;
    }

    public Object unMarshall(final ISOSVirtualFile pobjVirtualFile) {
        Object objC = null;
        if (pobjVirtualFile != null) {
            InputStream objInputStream = pobjVirtualFile.getFileInputStream();
            try {
                if (objInputStream == null) {
                    LOGGER.error(String.format("can't get inputstream for file '%1$s'.", pobjVirtualFile.getName()));
                    throw new JobSchedulerException(String.format("can't get inputstream for file '%1$s'.", pobjVirtualFile.getName()));
                }
                objC = u.unmarshal(objInputStream);
            } catch (JAXBException e) {
                throw new JobSchedulerException(e.getMessage(), e);
            } finally {
                pobjVirtualFile.closeInput();
            }
        } else {
            LOGGER.error("pobjVirtualFile is null");
            throw new JobSchedulerException("pobjVirtualFile is null");
        }
        return objC;
    }

    public Object unMarshall(final InputStream pobjInputStream) {
        Object objC = null;
        try {
            objC = u.unmarshal(pobjInputStream);
        } catch (JAXBException e) {
            throw new JobSchedulerException(e.getMessage(), e);
        }
        return objC;
    }

    public Object unMarshall(final String pstrXMLContent) {
        Object objC = null;
        try {
            objC = u.unmarshal(new StringReader(pstrXMLContent));
        } catch (JAXBException e) {
            throw new JobSchedulerException(e.getMessage(), e);
        }
        return objC;
    }

    public Object marshal(final Object objO, final File objF) {
        try {
            XMLSerializer serializer = getXMLSerializer();
            FileOutputStream objFOS = new FileOutputStream(objF);
            serializer.setOutputByteStream(objFOS);
            objM.marshal(objO, serializer.asContentHandler());
        } catch (JAXBException e) {
            throw new JobSchedulerException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new JobSchedulerException(e.getMessage(), e);
        } catch (IOException e) {
            throw new JobSchedulerException(e.getMessage(), e);
        }
        return objO;
    }

    public String marshal(final Object objO) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        try {
            XMLSerializer serializer = getXMLSerializer();
            serializer.setOutputByteStream(out);
            objM.marshal(objO, serializer.asContentHandler());
        } catch (JAXBException e) {
            throw new JobSchedulerException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new JobSchedulerException(e.getMessage(), e);
        } catch (IOException e) {
            throw new JobSchedulerException(e.getMessage(), e);
        }
        return baos.toString();
    }

    public Object toXMLFile(final Object objO, final ISOSVirtualFile pobjVirtualFile) {
        if (pobjVirtualFile != null) {
            OutputStream objOutputStream = pobjVirtualFile.getFileOutputStream();
            try {
                if (objOutputStream == null) {
                    LOGGER.error(String.format("can't get outputstream for file '%1$s'.", pobjVirtualFile.getName()));
                    throw new JobSchedulerException(String.format("can't get outputstream for file '%1$s'.", pobjVirtualFile.getName()));
                }
                XMLSerializer serializer = getXMLSerializer();
                serializer.setOutputByteStream(objOutputStream);
                objM.marshal(objO, serializer.asContentHandler());
            } catch (JAXBException e) {
                throw new JobSchedulerException(e.getMessage(), e);
            } catch (IOException e) {
                throw new JobSchedulerException(e.getMessage(), e);
            } finally {
                pobjVirtualFile.closeOutput();
            }
        } else {
            LOGGER.error("pobjVirtualFile is null");
            throw new JobSchedulerException("pobjVirtualFile is null");
        }
        return objO;
    }

    private String toXMLString(final Object objO, final Marshaller objMarshaller) {

        String strT = "";
        try {
            XMLSerializer serializer = getXMLSerializer();
            OutputFormat objOutputFormat = new OutputFormat();
            objOutputFormat.setEncoding("utf-8");
            objOutputFormat.setIndenting(true);
            objOutputFormat.setIndent(4);
            objOutputFormat.setOmitXMLDeclaration(ommitXmlDeclaration);
            objOutputFormat.setLineWidth(80);
            serializer.setOutputFormat(objOutputFormat);
            StringWriter objSW = new StringWriter();
            serializer.setOutputCharStream(objSW);
            objMarshaller.marshal(objO, serializer.asContentHandler());
            strT = objSW.getBuffer().toString();
        } catch (JAXBException e) {
            throw new JobSchedulerException(e.getMessage(), e);
        } catch (IOException e) {
            throw new JobSchedulerException(e.getMessage(), e);
        } catch (Exception e) {
            throw new JobSchedulerException(e.getMessage(), e);
        }
        return strT;
    }

    public String toXMLString(final Object objO) {
        if (objO.getClass().getName().startsWith("com.sos.scheduler.model.answers.")) {
            return answerToXMLString(objO);
        }
        if (objO.getClass().getName().startsWith("com.sos.scheduler.model.commands.")) {
            return cmdToXMLString(objO);
        }
        return toXMLString(objO, objM);
    }

    public String answerToXMLString(final Object objO) {
        initAnswerMarshaller();
        return toXMLString(objO, objM4Answers);
    }

    private String cmdToXMLString(final Object objO) {
        StringWriter s = new StringWriter();
        try {
            objM.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            objM.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            objM.setProperty(Marshaller.JAXB_FRAGMENT, ommitXmlDeclaration);
            objM.marshal(objO, s);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        return s.toString();
    }

    private XMLSerializer getXMLSerializer() {
        OutputFormat of = new OutputFormat();
        of.setCDataElements(new String[] { "^description", "^script", "^scheduler_script", "^log_mail_to", "^log_mail_cc", "^log_mail_bcc" });
        of.setIndenting(true);
        XMLSerializer serializer = new XMLSerializer(of);
        return serializer;
    }

    public SchedulerObjectFactoryOptions getOptions() {
        if (objOptions == null) {
            objOptions = new SchedulerObjectFactoryOptions();
        }
        return objOptions;
    }

    public SchedulerSocket getSocket() {
        if (objSchedulerSocket == null) {
            try {
                objSchedulerSocket = new SchedulerSocket(objOptions);
            } catch (Exception e) {
                throw new JobSchedulerException(e.getMessage(), e);
            }
        }
        return objSchedulerSocket;
    }

    public void closeSocket() {
        if (objSchedulerSocket != null) {
            try {
                objSchedulerSocket.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
            objSchedulerSocket = null;
        }
    }

    public Params setParams(final Variable_set pobjProperties) {
        String strParamValue = "";
        Params objParams = super.createParams();
        for (String strKey : pobjProperties.names().split(";")) {
            strParamValue = pobjProperties.value(strKey);
            Param objP = this.createParam(strKey, strParamValue);
            objParams.getParamOrCopyParamsOrInclude().add(objP);
        }
        return objParams;
    }

    public Params setParams(final Properties pobjProperties) {
        Params objParams = super.createParams();
        for (final Entry<Object, Object> element : pobjProperties.entrySet()) {
            Param objP = this.createParam(element.getKey().toString(), element.getValue().toString());
            objParams.getParamOrCopyParamsOrInclude().add(objP);
        }
        return objParams;
    }

    public Params setParams(final Map<String, String> params) {
        Params objParams = super.createParams();
        for (Entry<String, String> element : params.entrySet()) {
            Param param = this.createParam(element.getKey(), element.getValue());
            objParams.getParamOrCopyParamsOrInclude().add(param);
        }
        return objParams;
    }

    public Params setParams(final String[] pstrParamArray) {
        String strParamValue = "";
        Params objParams = super.createParams();
        for (int i = 0; i < pstrParamArray.length; i += 2) {
            String strParamName = pstrParamArray[i];
            if (i + 1 >= pstrParamArray.length) {
                strParamValue = "";
            } else {
                strParamValue = pstrParamArray[i + 1];
            }
            Param objP = this.createParam(strParamName, strParamValue);
            objParams.getParamOrCopyParamsOrInclude().add(objP);
        }
        return objParams;
    }

    @Override
    public JSObjParam createParam() {
        JSObjParam objParam = new JSObjParam(this);
        return objParam;
    }

    public JSObjParam createParam(final Param param) {
        JSObjParam objParam = new JSObjParam(this, param);
        return objParam;
    }

    public Param createParam(final String pstrParamName, final String pstrParamValue) {
        Param objP = super.createParam();
        objP.setName(pstrParamName);
        objP.setValue(pstrParamValue);
        return objP;
    }

    public JSConfiguration createJSConfiguration() {
        JSConfiguration objJSC = new JSConfiguration(this);
        return objJSC;
    }

    public JSConfiguration createJSConfiguration(final ISOSVirtualFile pobjVirtualFile) {
        JSConfiguration objJSC = new JSConfiguration(this, pobjVirtualFile);
        return objJSC;
    }

    public SchedulerHotFolder createSchedulerHotFolder(final ISOSVirtualFile objDir) {
        SchedulerHotFolder objSchedulerHotFolder = new SchedulerHotFolder(this, objDir);
        return objSchedulerHotFolder;
    }

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

    public JSObjJob createJob(final ISOSVirtualFile pobjVirtualFile) {
        JSObjJob objJob = new JSObjJob(this, pobjVirtualFile);
        return objJob;
    }

    public JSObjParams createParams(final ISOSVirtualFile pobjVirtualFile) {
        JSObjParams objParams = new JSObjParams(this, pobjVirtualFile);
        return objParams;
    }

    @Override
    public JSObjParams createParams() {
        JSObjParams objParams = new JSObjParams(this);
        return objParams;
    }

    public JSObjParams createParams(final Params params) {
        JSObjParams objParams = new JSObjParams(this, params);
        return objParams;
    }

    @Override
    public JSObjJobChain createJobChain() {
        JSObjJobChain objJobChain = new JSObjJobChain(this);
        return objJobChain;
    }

    public JSObjJobChain createJobChain(final ISOSVirtualFile pobjVirtualFile) {
        JSObjJobChain objJobChain = new JSObjJobChain(this, pobjVirtualFile);
        return objJobChain;
    }

    @Override
    public JSObjJobChains createJobChains() {
        JSObjJobChains objJobChains = new JSObjJobChains(this);
        return objJobChains;
    }

    @Override
    public JSObjJobChainNodeEnd createJobChainNodeEnd() {
        JSObjJobChainNodeEnd objJobChainNodeEnd = new JSObjJobChainNodeEnd(this);
        return objJobChainNodeEnd;
    }

    @Override
    public JSObjJobChainNodeJobChain createJobChainNodeJobChain() {
        JSObjJobChainNodeJobChain objJobChainNodeJobChain = new JSObjJobChainNodeJobChain(this);
        return objJobChainNodeJobChain;
    }

    @Override
    public JSObjJobs createJobs() {
        JSObjJobs objJobs = new JSObjJobs(this);
        return objJobs;
    }

    @Override
    public JSObjJobSettings createJobSettings() {
        JSObjJobSettings objJobSettings = new JSObjJobSettings(this);
        return objJobSettings;
    }

    @Override
    public JSObjHolidays createHolidays() {
        JSObjHolidays objHolidays = new JSObjHolidays(this);
        return objHolidays;
    }

    @Override
    public JSObjHoliday createHoliday() {
        JSObjHoliday objHoliday = new JSObjHoliday(this);
        return objHoliday;
    }

    @Override
    public JSObjMonthdays createMonthdays() {
        JSObjMonthdays objMonthdays = new JSObjMonthdays(this);
        return objMonthdays;
    }

    @Override
    public JSObjLock createLock() {
        JSObjLock objLock = new JSObjLock(this);
        return objLock;
    }

    public JSObjLock createLock(final ISOSVirtualFile pobjVirtualFile) {
        JSObjLock objLock = new JSObjLock(this, pobjVirtualFile);
        return objLock;
    }

    @Override
    public JSObjCluster createCluster() {
        JSObjCluster objCluster = new JSObjCluster(this);
        return objCluster;
    }

    @Override
    public JSCmdClusterMemberCommand createClusterMemberCommand() {
        JSCmdClusterMemberCommand objCMC = new JSCmdClusterMemberCommand(this);
        return objCMC;
    }

    @Override
    public JSObjSecurity createSecurity() {
        JSObjSecurity objSecurity = new JSObjSecurity(this);
        return objSecurity;
    }

    @Override
    public JSObjSpooler createSpooler() {
        JSObjSpooler objSpooler = new JSObjSpooler(this);
        return objSpooler;
    }

    public JSObjSpooler createSpooler(final ISOSVirtualFile pobjVirtualFile) {
        JSObjSpooler objSpooler = new JSObjSpooler(this, pobjVirtualFile);
        return objSpooler;
    }

    @Override
    public JSObjOrder createOrder() {
        JSObjOrder objOrder = new JSObjOrder(this);
        return objOrder;
    }

    public JSObjOrder createOrder(final ISOSVirtualFile pobjVirtualFile) {
        JSObjOrder objOrder = new JSObjOrder(this, pobjVirtualFile);
        String s;
        try {
            s = pobjVirtualFile.getFile().getName();
            s = new File(s).getName();
            s = s.replaceFirst(".*,(.*)\\.order\\.xml", "$1");

            objOrder.setId(s);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return objOrder;
    }

    public JSObjHolidays createHolidays(final ISOSVirtualFile pobjVirtualFile) {
        JSObjHolidays objHolidays = new JSObjHolidays(this, pobjVirtualFile);
        return objHolidays;
    }

    public JSObjSchedule createSchedule() {
        JSObjSchedule objSchedule = new JSObjSchedule(this);
        return objSchedule;
    }

    public JSObjSchedule createSchedule(final ISOSVirtualFile pobjVirtualFile) {
        JSObjSchedule objSchedule = new JSObjSchedule(this, pobjVirtualFile);
        return objSchedule;
    }

    @Override
    public JSObjProcessClass createProcessClass() {
        JSObjProcessClass objProcessClass = new JSObjProcessClass(this);
        return objProcessClass;
    }

    public JSObjProcessClass createProcessClass(final ISOSVirtualFile pobjVirtualFile) {
        JSObjProcessClass objProcessClass = new JSObjProcessClass(this, pobjVirtualFile);
        return objProcessClass;
    }

    @Override
    public JSObjProcessClasses createProcessClasses() {
        JSObjProcessClasses objProcessClasses = new JSObjProcessClasses(this);
        return objProcessClasses;
    }

    @Override
    public JSObjEnvironment createEnvironment() {
        JSObjEnvironment objEnvironment = new JSObjEnvironment(this);
        return objEnvironment;
    }

    @Override
    public JSCmdScheduleRemove createScheduleRemove() {
        JSCmdScheduleRemove objScheduleRemove = new JSCmdScheduleRemove(this);
        return objScheduleRemove;
    }

    @Override
    public JSCmdRemoteSchedulerStartRemoteTask createRemoteSchedulerStartRemoteTask() {
        JSCmdRemoteSchedulerStartRemoteTask objRemoteSchedulerStartRemoteTask = new JSCmdRemoteSchedulerStartRemoteTask(this);
        return objRemoteSchedulerStartRemoteTask;
    }

    @Override
    public JSCmdKillTask createKillTask() {
        JSCmdKillTask objKillTask = new JSCmdKillTask(this);
        return objKillTask;
    }

    @Override
    public JSObjScript createScript() {
        JSObjScript objScript = new JSObjScript(this);
        return objScript;
    }

    @Override
    public JSObjInclude createInclude() {
        JSObjInclude objInclude = new JSObjInclude(this);
        return objInclude;
    }

    @Override
    public JSObjCommands createCommands() {
        JSObjCommands objCommands = new JSObjCommands(this);
        return objCommands;
    }

    @Override
    public JSObjConfigurationDirectory createConfigurationDirectory() {
        JSObjConfigurationDirectory objConfigurationDirectory = new JSObjConfigurationDirectory(this);
        return objConfigurationDirectory;
    }

    @Override
    public JSObjConfigurationFile createConfigurationFile() {
        JSObjConfigurationFile objConfigurationFile = new JSObjConfigurationFile(this);
        return objConfigurationFile;
    }

    @Override
    public JSCmdSubsystemShow createSubsystemShow() {
        JSCmdSubsystemShow objSubsystemShow = new JSCmdSubsystemShow(this);
        return objSubsystemShow;
    }

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

    public JSCmdStartJob startJob(final String pstrJobName) {
        JSCmdStartJob objStartJobCmd = new JSCmdStartJob(this);
        objStartJobCmd.setJob(pstrJobName);
        objStartJobCmd.setForce(true);
        objStartJobCmd.run();
        objStartJobCmd.getAnswerWithException();
        return objStartJobCmd;
    }

    public JSCmdStartJob startJob(final String pstrJobName, final boolean raiseOk) {
        JSCmdStartJob objStartJobCmd = new JSCmdStartJob(this);
        objStartJobCmd.setJob(pstrJobName);
        objStartJobCmd.setForce(true);
        objStartJobCmd.run();
        objStartJobCmd.flgRaiseOKException = raiseOk;
        objStartJobCmd.getAnswerWithException();
        return objStartJobCmd;
    }

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
        objShowJobCmd.maxTaskHistory(1);
        objShowJobCmd.maxOrders(1);
        return objShowJobCmd;
    }

    public JSCmdShowJob createShowJob(final String pstrJobName, final JSCmdShowJob.enu4What[] penuWhat) {
        JSCmdShowJob objShowJobCmd = new JSCmdShowJob(this);
        objShowJobCmd.setJob(pstrJobName);
        objShowJobCmd.setWhat(penuWhat);
        objShowJobCmd.maxTaskHistory(1);
        objShowJobCmd.maxOrders(1);
        return objShowJobCmd;
    }

    public JSCmdShowJob getTaskQueue(final String pstrJobName) {
        JSCmdShowJob objShowJobCmd = createShowJob(pstrJobName, new JSCmdShowJob.enu4What[] { JSCmdShowJob.enu4What.task_queue });
        objShowJobCmd = executeShowJob(objShowJobCmd);
        return objShowJobCmd;
    }

    public JSCmdShowJob isJobRunning(final String pstrJobName) {
        JSCmdShowJob objShowJob = createShowJob(pstrJobName);
        return executeShowJob(objShowJob);
    }

    public String getTaskLogFromShowHistory(final String pstrJobName, final int pintTaskId) {
        String log = null;
        JSCmdShowHistory objHist = this.createShowHistory();
        objHist.setJob(pstrJobName);
        objHist.setId(BigInteger.valueOf(pintTaskId));
        objHist.setWhat("log");
        objHist.run();
        Answer objAnswer = objHist.getAnswer();
        List<HistoryEntry> objEntries = objAnswer.getHistory().getHistoryEntry();
        if (objEntries != null && !objEntries.isEmpty()) {
            HistoryEntry objEntry = objEntries.get(0);
            if (objEntry != null) {
                log = objEntry.getLog().getContent();
            }
        }
        return log;
    }

    public String getTaskLogFromShowJob(final String pstrJobName, final int pintTaskId) {
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

    public String getTaskLog(final String pstrJobName, final int pintTaskId, final boolean pbUseCurrentTaskLog) {
        String log = null;
        if (pbUseCurrentTaskLog) {
            log = this.getTaskLogFromShowJob(pstrJobName, pintTaskId);
            if (log == null) {
                log = this.getTaskLogFromShowHistory(pstrJobName, pintTaskId);
            }
        } else {
            log = this.getTaskLogFromShowHistory(pstrJobName, pintTaskId);
            if (log == null) {
                log = this.getTaskLogFromShowJob(pstrJobName, pintTaskId);
            }
        }
        return log;
    }

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
            if (!"running".equalsIgnoreCase(strJobState)) {
                LOGGER.debug(JOM_D_0030.get(pstrJobName, strJobState));
                flgJobIsRunning = false;
            } else {
                LOGGER.debug(JOM_D_0040.get(pstrJobName));
                int intNoOfTasks = objJobAnswer.getTasks().getCount().intValue();
                if (intNoOfTasks > 0) {
                    flgJobIsRunning = true;
                }
            }
        }
        if (!flgJobIsRunning) {
            return null;
        }
        return pobjShowJob;
    }

    @Override
    public JSCmdShowJobs createShowJobs() {
        JSCmdShowJobs objShowJobsCmd = new JSCmdShowJobs(this);
        return objShowJobsCmd;
    }

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

    @Override
    public JSCmdModifyJob createModifyJob() {
        JSCmdModifyJob objModifyJob = new JSCmdModifyJob(this);
        return objModifyJob;
    }

    @Override
    public JSCmdJobChainModify createJobChainModify() {
        JSCmdJobChainModify objJobChainModify = new JSCmdJobChainModify(this);
        return objJobChainModify;
    }

    @Override
    public JSCmdRemoveJobChain createRemoveJobChain() {
        JSCmdRemoveJobChain objRemoveJobChain = new JSCmdRemoveJobChain(this);
        return objRemoveJobChain;
    }

    @Override
    public JSCmdProcessClassRemove createProcessClassRemove() {
        JSCmdProcessClassRemove objProcessClassRemove = new JSCmdProcessClassRemove(this);
        return objProcessClassRemove;
    }

    @Override
    public JSCmdCheckFolders createCheckFolders() {
        JSCmdCheckFolders objCheckFolders = new JSCmdCheckFolders(this);
        return objCheckFolders;
    }

    @Override
    public JSCmdModifySpooler createModifySpooler() {
        JSCmdModifySpooler objModifySpooler = new JSCmdModifySpooler(this);
        return objModifySpooler;
    }

    @Override
    public JSCmdEventsGet createEventsGet() {
        JSCmdEventsGet objEventsGet = new JSCmdEventsGet(this);
        return objEventsGet;
    }

    @Override
    public JSCmdRemoveOrder createRemoveOrder() {
        JSCmdRemoveOrder objRemoveOrder = new JSCmdRemoveOrder(this);
        return objRemoveOrder;
    }

    @Override
    public JSCmdSchedulerLogLogCategoriesReset createSchedulerLogLogCategoriesReset() {
        JSCmdSchedulerLogLogCategoriesReset objSchedulerLogLogCategoriesReset = new JSCmdSchedulerLogLogCategoriesReset(this);
        return objSchedulerLogLogCategoriesReset;
    }

    @Override
    public JSCmdTerminate createTerminate() {
        JSCmdTerminate objTerminate = new JSCmdTerminate(this);
        return objTerminate;
    }

    @Override
    public JSCmdRemoteSchedulerRemoteTaskClose createRemoteSchedulerRemoteTaskClose() {
        JSCmdRemoteSchedulerRemoteTaskClose objRemoteSchedulerRemoteTaskClose = new JSCmdRemoteSchedulerRemoteTaskClose(this);
        return objRemoteSchedulerRemoteTaskClose;
    }

    @Override
    public JSCmdLockRemove createLockRemove() {
        JSCmdLockRemove objLockRemove = new JSCmdLockRemove(this);
        return objLockRemove;
    }

    @Override
    public JSCmdSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles createSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles() {
        JSCmdSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles objSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles =
                new JSCmdSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles(this);
        return objSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles;
    }

    public JSCmdJobChainNodeModify createJobChainNodeModify() {
        JSCmdJobChainNodeModify objJobChainNodeModify = new JSCmdJobChainNodeModify(this);
        return objJobChainNodeModify;
    }

    @Override
    public JSCmdModifyOrder createModifyOrder() {
        JSCmdModifyOrder objModifyOrder = new JSCmdModifyOrder(this);
        return objModifyOrder;
    }

    public JSCmdModifyOrder startOrder(final String pstrJobChainName, final String pstrOrderName) {
        JSCmdModifyOrder objModifyOrder = new JSCmdModifyOrder(this);
        objModifyOrder.setJobChain(pstrJobChainName);
        objModifyOrder.setOrder(pstrOrderName);
        objModifyOrder.setAt("now");
        objModifyOrder.run();
        objModifyOrder.getAnswerWithException();
        return objModifyOrder;
    }

    public JSCmdModifyOrder startOrder(final String pstrJobChainName, final String pstrOrderName, final boolean raiseOk) {
        JSCmdModifyOrder objModifyOrder = new JSCmdModifyOrder(this);
        objModifyOrder.setJobChain(pstrJobChainName);
        objModifyOrder.setOrder(pstrOrderName);
        objModifyOrder.setAt("now");
        objModifyOrder.run();
        objModifyOrder.flgRaiseOKException = raiseOk;
        objModifyOrder.getAnswerWithException();
        return objModifyOrder;
    }

    @Override
    public JSCmdParamGet createParamGet() {
        JSCmdParamGet objParamGet = new JSCmdParamGet(this);
        return objParamGet;
    }

    @Override
    public JSCmdModifyHotFolder createModifyHotFolder() {
        JSCmdModifyHotFolder objModifyHotFolder = new JSCmdModifyHotFolder(this);
        return objModifyHotFolder;
    }

    @Override
    public JSCmdAddJobs createAddJobs() {
        JSCmdAddJobs objAddJobs = new JSCmdAddJobs(this);
        return objAddJobs;
    }

    public JSCmdAddOrder createAddOrder() {
        JSCmdAddOrder objAddOrder = new JSCmdAddOrder(this);
        return objAddOrder;
    }

    public JSCmdCommands createCmdCommands() {
        JSCmdCommands objCommands = new JSCmdCommands(this);
        return objCommands;
    }

    @Override
    public JSCmdSchedulerLogLogCategoriesSet createSchedulerLogLogCategoriesSet() {
        JSCmdSchedulerLogLogCategoriesSet objSchedulerLogLogCategoriesSet = new JSCmdSchedulerLogLogCategoriesSet(this);
        return objSchedulerLogLogCategoriesSet;
    }

    @Override
    public Spooler.Answer createSpoolerAnswer() {
        return new Spooler.Answer();
    }

    @Override
    public JSCmdLicenceUse createLicenceUse() {
        JSCmdLicenceUse objLicenceUse = new JSCmdLicenceUse(this);
        return objLicenceUse;
    }

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

    public void setSpooler(sos.spooler.Spooler spooler) {
       this.spooler = spooler;
    }

}
