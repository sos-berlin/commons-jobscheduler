package sos.scheduler.managed.configuration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.net.SOSMail;
import sos.scheduler.misc.ParameterSubstitutor;
import sos.spooler.Monitor_impl;
import sos.spooler.Variable_set;
import sos.util.SOSFile;
import sos.util.SOSLogger;
import sos.util.SOSSchedulerLogger;
import sos.xml.SOSXMLXPath;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;

/**
 * <p>
 * ConfigurationBaseMonitor provides functions for Monitor-Scripts
 * </p>
 *
 * !!!! Attention: this program is using the xml-paylod for making the changes
 * persistent !!!! 
 * 
 * die payload wird u.A. verwendet, um die configuration auch
 * für agenten verfügbar zu machen
 *
 *
 * @author andreas.pueschel@sos-berlin.com
 * @since 1.0 2006-10-05
 */

// TODO wo ist die doku der Parameter?
// TODO Options klasse verwenden
// TODO Parameter in der Form
// [<step>[@(task_before|task_after|process_before|process_after]/]name
// ebenfalls verarbeiten. Alternativ zur config-Datei
// TODO JobSchedulerJobAdapter als Basis#Klasse verwenden
// TODO log4j verwenden
// TODO das Variable_set serialisieren und in eine Variable stecken. Payload nicht mehr verändern
// TODO Den Konfiguration-Monitor in die JobAdapterKlasse übernehmen, damit kann der Monitor dann entfallen für die API-Jobs

public class ConfigurationBaseMonitor extends Monitor_impl {

	public static final String		conVariableStartString								= "${";
	public static final String		conTypeBASENAME										= "basename:";
	public static final String		conVariableTypeBASENAME								= conVariableStartString + conTypeBASENAME;						// "${basename:";
	public static final String		conTypeFILE_CONTENT									= "file_content:";
	public static final String		conVariableTypeFILE_CONTENT							= conVariableStartString + conTypeFILE_CONTENT;					// "${file_content:";

	private static final String		conClassName										= "ConfigurationBaseMonitor";

	private static final String		XML_VERSION_1_0_ENCODING_ISO_8859_1					= "<?xml version='1.0' encoding='ISO-8859-1'?>";
	private static final String		conAttributeNameHIDDEN								= "hidden";
	private static final String		conVariableSCHEDULER_ORDER_ADDITIONAL_ENVVARS		= "scheduler_order_additional_envvars";
	private static final String		conSETTINGS_LOG_LEVEL								= "//settings/log_level";
	private static final String		conSystemPropertyOS_NAME							= "os.name";
	private static final String		conTRUE												= "true";
	private static final String		conVariableNameSCHEDULER_ORDER_CONFIGURATION_LOADED	= "ConfigurationBaseMonitor.scheduler_order_configuration_loaded";
	protected static final String	conParamNameCONFIGURATION_FILE						= "ConfigurationBaseMonitor.configuration_file";
	protected static final String	conParamNameCONFIGURATION_PATH						= "ConfigurationBaseMonitor.configuration_path";
	private static final String		conVariableNameGLOBAL_CONFIGURATION_PARAMS			= "global_configuration_params";
	private static final String		conNamedItemENV										= "env";
	private static final String		conTagNamePARAM										= "param";
	private static final String		conXPathPARAMS										= "/params";
	private static final String		conAttributeNameNAME								= "name";
	private static final String		conAttributeNameVALUE								= "value";
	private static final String		conXPathPARAMS_PARAM								= conXPathPARAMS + "/" + conTagNamePARAM;
	protected static final String	conFileNameExtensionCONFIG_XML						= ".config.xml";
	protected static final String	conDefaultFileName4CACHE							= "cache";
    
	/** Logging */  
	private SOSSchedulerLogger		sosLogger											= null;

	/** Attribut: configuration: Job-Konfiguration im XML-Format */
	private Document				configuration										= null;

//	private String					configurationBuffer									= null;
	private String					configurationPath									= "";
	private String					configurationFilename								= "";

	/** Liste der Parameternamen */
	private Vector<String>			orderParameterKeys									= null;
	private TreeMap<String, String>	envvars												= null;
	private TreeMap<String, String>	additional_envvars									= null;
	private boolean					envVarsCaseSensitive								= true;

	private String getOrderParam(final String pstrParamName) {
		if (spooler_task.order() != null) {
			String strT = spooler_task.order().params().value(pstrParamName);
            spooler_log.debug9(String.format("...getting %1$s = %2$s", pstrParamName, strT));
			if (strT != null) {
				return strT;
			}
		}
		return null;
	}

	private String setOrderParam(final String pstrParamName, final String pstrParamValue) {
		if (spooler_task.order() != null) {
			spooler_task.order().params().set_var(pstrParamName, pstrParamValue);
			spooler_log.debug3(String.format("...Setting %1$s = %2$s", pstrParamName, pstrParamValue));

		}
		return pstrParamValue;
	}

	/** 
	 * Initialize order configuration
	 */
	public void initConfiguration() throws Exception {

	    spooler_log.debug9("..initConfiguration");
	    String paramNameCONFIGURATION_PATH = getOrderParam(conParamNameCONFIGURATION_PATH);
	    String paramNameCONFIGURATION_FILE = getOrderParam(conParamNameCONFIGURATION_FILE);
	    
	    if (paramNameCONFIGURATION_PATH != null && paramNameCONFIGURATION_PATH.length() > 0) {
		   this.setConfigurationPath(paramNameCONFIGURATION_PATH);
	    }
	    
	    if (paramNameCONFIGURATION_FILE != null && paramNameCONFIGURATION_FILE.length() > 0) {
		   this.setConfigurationFilename(paramNameCONFIGURATION_FILE);
	    }

		this.initConfiguration(this.getConfigurationPath(), this.getConfigurationFilename());
	}

	/**
	 * Initialize order configuration
	 */
	private void initConfiguration(final String configurationPath1, final String configurationFilename1) throws Exception {

		this.setConfigurationPath(getOrderParam(conParamNameCONFIGURATION_PATH));
		spooler_log.debug3(String.format("--> configurationFilename1:%s ",configurationFilename1));
		spooler_log.debug3(String.format("--> configurationPath1:%s ",configurationPath1));
 

		if (configurationFilename1.startsWith(".") || configurationFilename1.startsWith("/") || configurationFilename1.startsWith("\\")
				|| configurationFilename1.indexOf(":") > -1 || configurationPath1 == null || configurationPath1.length() == 0) {
			this.initConfiguration(configurationFilename1);
		}
		else {
			this.initConfiguration(configurationPath1 + (!configurationPath1.endsWith("/") && !configurationPath1.endsWith("\\") ? "/" : "")
					+ configurationFilename1);
		}
	}

	/**
	 * Initialize order configuration
	 */
	public void initConfiguration(final String configurationFilename1) throws Exception {

		FileInputStream fis = null;

		try { // to retrieve configuration from file
			if (configurationFilename1 == null || configurationFilename1.length() == 0)
				throw new JobSchedulerException(conClassName + ": no configuration filename was specified");

			// TODO use object "jobChainConfig" for this purpose
			JSFile objConfigFile = new JSFile(configurationFilename1);
			
			spooler_log.debug7(String.format("opened config file: %s",configurationFilename1));
			if (objConfigFile.exists()){
    			objConfigFile.canRead();
    			String configurationBuffer = objConfigFile.getContent();
    	        spooler_log.debug9(String.format("...with content %s",configurationBuffer));
    
    			objConfigFile.close();
    
    			// Damit die Agenten auch eine Konfiguration haben. Evtl.
    			// Dummy-Step als ersten Node einfügen.
    			spooler_task.order().set_xml_payload(configurationBuffer);
			}else {
	            // Es ist ok, dass die Datei fehlt
	            this.getLogger().debug3(String.format(conClassName + ": error occurred initializing configuration: File %s not found", configurationFilename1));
			}

		/*	To avoid side effects e.g. when cloning the order for other job chains or in nested job chains, this should not be set in the order parameter.  
		    setOrderParam(conVariableNameSCHEDULER_ORDER_CONFIGURATION_LOADED, conTRUE);
			setOrderParam(conParamNameCONFIGURATION_FILE, configurationFilename1);
			*/
		}
		catch (Exception e) {
 			this.getLogger().warn(conClassName + ": error occurred initializing configuration: " + e.getMessage());
            e.printStackTrace();
		}
	}

	private String getJobChainName() {

		@SuppressWarnings("unused")
		final String	conMethodName	= conClassName + "::getJobChainName";

		String strT = spooler_task.order().job_chain().name();

		return strT;
	} // private String getJobChainName
	/**
	 * Initialize order configuration
	 */
	public Document prepareConfiguration() throws Exception {

		String nodeQuery = "";
		String payload = "";
		additional_envvars = null;

		try { // to fetch the order configuration
			orderParameterKeys = new Vector<String>();

			if (spooler_task.job().order_queue() != null) {

				this.setEnvVars();
				String env = "";
				boolean globalEnv = false;

				if (spooler_task.order().xml_payload() == null || spooler_task.order().xml_payload().length() == 0 || spooler_task.order().params() == null
						|| getOrderParam(conVariableNameSCHEDULER_ORDER_CONFIGURATION_LOADED) == null
						|| getOrderParam(conVariableNameSCHEDULER_ORDER_CONFIGURATION_LOADED).length() == 0)
				    spooler_log.debug3(".. call init from prepare");
					this.initConfiguration();

				if (spooler_task.order().xml_payload() == null)
					throw new JobSchedulerException(conClassName + ": monitor: no configuration was specified for this order: " + spooler_task.order().id());

				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				docFactory.setNamespaceAware(false);
				docFactory.setValidating(false);
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				// this.setConfiguration(docBuilder.parse(new
				// ByteArrayInputStream(spooler_task.order().xml_payload().getBytes())));
				payload = spooler_task.order().xml_payload();


				if (!payload.startsWith("<?xml ")) {
	                debugx(3, "Payload not starting with <?xml: " + payload);
                    debugx(3, String.format("Will add %s to payload",XML_VERSION_1_0_ENCODING_ISO_8859_1));
					payload = XML_VERSION_1_0_ENCODING_ISO_8859_1 + payload;
				}

				debugx(3, "Payload: " + payload);

				if (!payload.equals(XML_VERSION_1_0_ENCODING_ISO_8859_1)) {

					this.setConfiguration(docBuilder.parse(new ByteArrayInputStream(payload.getBytes())));
					SOSXMLXPath xpath = new SOSXMLXPath(new StringBuffer(payload));
					NodeList nodeList = null;
					NamedNodeMap nodeMapSettings = null;

					nodeQuery = conSETTINGS_LOG_LEVEL;
					Node nodeSettings = xpath.selectSingleNode(nodeQuery);
					if (nodeSettings != null) {
						nodeMapSettings = nodeSettings.getAttributes();
						if (nodeMapSettings != null && nodeMapSettings.getNamedItem(conAttributeNameVALUE) != null) {
							debugx(1, "Log Level is: " + nodeMapSettings.getNamedItem(conAttributeNameVALUE).getNodeValue());
							this.getLogger().setLogLevel(this.logLevel2Int(nodeMapSettings.getNamedItem(conAttributeNameVALUE).getNodeValue()));
						}
					}

					// add attributes from configuration
					// this.getLogger().debug7("adding parameters from configuration: "
					// + spooler_task.order().xml_payload());

					// look up the configuration for the all states
					nodeQuery = "//job_chain[@name='" + getJobChainName() + "']/order";
					debugx(9, "monitor: lookup order for job chain: " + nodeQuery + conXPathPARAMS_PARAM);
					Node nodeParams = xpath.selectSingleNode(nodeQuery + conXPathPARAMS);
					if (nodeParams == null || !nodeParams.hasChildNodes()) {
						nodeQuery = "//application[@name='" + getJobChainName() + "']/order";
						debugx(9, "lookup order query for application: " + nodeQuery);
						nodeParams = xpath.selectSingleNode(nodeQuery + conXPathPARAMS);
					}

					if (nodeParams != null && nodeParams.hasAttributes()) {
						NamedNodeMap nodeMapParams = nodeParams.getAttributes();
						if (nodeMapParams != null && nodeMapParams.getNamedItem(conNamedItemENV) != null) {
							env = nodeMapParams.getNamedItem(conNamedItemENV).getNodeValue();
							debugx(3, ".. parameter section with env=" + env + " found");
							// globalEnv = (env.equalsIgnoreCase("yes") ||
							// env.equals("1") || env.equalsIgnoreCase("on") ||
							// env.equalsIgnoreCase(conTRUE));
							globalEnv = isTrue(env);
						}
					}

					nodeList = xpath.selectNodeList(nodeQuery + conXPathPARAMS_PARAM);
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node node = nodeList.item(i);
						String nodeValue = "";
						String nodeName = "";
						if (node.getNodeName().equalsIgnoreCase(conTagNamePARAM)) {
							NamedNodeMap nodeMap = node.getAttributes();
							if (nodeMap != null && nodeMap.getNamedItem(conAttributeNameNAME) != null) {
								nodeName = nodeMap.getNamedItem(conAttributeNameNAME).getNodeValue();
								if (nodeMap.getNamedItem(conAttributeNameVALUE) != null) {
									nodeValue = nodeMap.getNamedItem(conAttributeNameVALUE).getNodeValue();
								}
								else {
									NodeList children = node.getChildNodes();
									for (int j = 0; j < children.getLength(); j++) {
										Node item = children.item(j);
										switch (item.getNodeType()) {
											case Node.TEXT_NODE:
											case Node.CDATA_SECTION_NODE:
												nodeValue += item.getNodeValue();
										}
									}
								}
								debugx(3, ".. monitor: global configuration parameter [" + nodeName + "]: " + nodeValue);
								setOrderParam(nodeName, nodeValue);

								if (globalEnv || nodeMap.getNamedItem(conNamedItemENV) != null) {
									if (nodeMap.getNamedItem(conNamedItemENV) != null)
										env = nodeMap.getNamedItem(conNamedItemENV).getNodeValue();

									boolean setEnv = globalEnv || isTrue(env); // (env.equalsIgnoreCase("yes")
																				// ||
																				// env.equals("1")
																				// ||
																				// env.equalsIgnoreCase("on")
																				// ||
																				// env.equalsIgnoreCase(conTRUE));
									if (setEnv) {
										if (additional_envvars == null)
											additional_envvars = new TreeMap<String, String>();
										additional_envvars.put(nodeName, nodeValue);
									}
								}
							}
						}
					}

					// add additional envvars parameter
					if (additional_envvars != null) {
						Iterator iter = additional_envvars.keySet().iterator();
						String envNames = "";
						while (iter.hasNext()) {
							String envName = (String) iter.next();
							envNames += envName;
							if (iter.hasNext())
								envNames += ";";
						}
						orderParameterKeys.add(conVariableSCHEDULER_ORDER_ADDITIONAL_ENVVARS);
						setOrderParam(conVariableSCHEDULER_ORDER_ADDITIONAL_ENVVARS, envNames);
					}

					// look up the configuration for the order state
					nodeQuery = "//job_chain[@name='" + spooler_task.order().job_chain().name() + "']/order/process[@state='" + spooler_task.order().state()
							+ "']";
					debugx(9, "monitor: lookup order node query for job chain: " + nodeQuery + conXPathPARAMS_PARAM);
					nodeList = xpath.selectNodeList(nodeQuery + conXPathPARAMS_PARAM);
					nodeParams = xpath.selectSingleNode(nodeQuery + conXPathPARAMS);
					if (nodeList == null || nodeList.getLength() == 0) {
						nodeQuery = "//application[@name='" + spooler_task.order().job_chain().name() + "']/order/process[@state='"
								+ spooler_task.order().state() + "']";
						debugx(9, "monitor: lookup order node query for application: " + nodeQuery + conXPathPARAMS_PARAM);
						nodeList = xpath.selectNodeList(nodeQuery + conXPathPARAMS_PARAM);
						nodeParams = xpath.selectSingleNode(nodeQuery + conXPathPARAMS);
					}

					// look for global env at this state
					globalEnv = false;
					if (nodeParams != null && nodeParams.hasAttributes()) {
						NamedNodeMap nodeMapParams = nodeParams.getAttributes();
						if (nodeMapParams != null && nodeMapParams.getNamedItem(conNamedItemENV) != null) {
							env = nodeMapParams.getNamedItem(conNamedItemENV).getNodeValue();
							debugx(3, ".. parameter section with env=" + env + " found");
							globalEnv = isTrue(env); // (env.equalsIgnoreCase("yes")
														// || env.equals("1") ||
														// env.equalsIgnoreCase("on")
														// ||
														// env.equalsIgnoreCase(conTRUE));
						}
					}
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node node = nodeList.item(i);
						this.getLogger().debug7("---->" + node.getNodeName());
						if (node.getNodeName().equalsIgnoreCase(conTagNamePARAM)) {
							NamedNodeMap nodeMap = node.getAttributes();
							boolean hidden = false;
							if (nodeMap.getNamedItem(conAttributeNameHIDDEN) != null
									&& nodeMap.getNamedItem(conAttributeNameHIDDEN).getNodeValue().equalsIgnoreCase(conTRUE))
								hidden = true;
							if (nodeMap != null && nodeMap.getNamedItem(conAttributeNameNAME) != null) {
								String value = "";
								if (nodeMap.getNamedItem(conAttributeNameVALUE) != null) {
									value = nodeMap.getNamedItem(conAttributeNameVALUE).getNodeValue();
								}
								else {
									NodeList children = node.getChildNodes();
									for (int j = 0; j < children.getLength(); j++) {
										Node item = children.item(j);
										switch (item.getNodeType()) {
											case Node.TEXT_NODE:
											case Node.CDATA_SECTION_NODE:
												value += item.getNodeValue();
										}
									}
								}
								if (hidden) {
									debugx(3, ".. configuration parameter [" + nodeMap.getNamedItem(conAttributeNameNAME).getNodeValue() + "]: *****");
								}
								else {
									debugx(3, ".. configuration parameter [" + nodeMap.getNamedItem(conAttributeNameNAME).getNodeValue() + "]: " + value);
								}
								setOrderParam(nodeMap.getNamedItem(conAttributeNameNAME).getNodeValue(), value);
								orderParameterKeys.add(nodeMap.getNamedItem(conAttributeNameNAME).getNodeValue());

								// set additional environment variables for
								// parameters of this state
								if (globalEnv || nodeMap.getNamedItem(conNamedItemENV) != null) {
									if (nodeMap.getNamedItem(conNamedItemENV) != null)
										env = nodeMap.getNamedItem(conNamedItemENV).getNodeValue();

									boolean setEnv = globalEnv || isTrue(env); // (env.equalsIgnoreCase("yes")
																				// ||
																				// env.equals("1")
																				// ||
																				// env.equalsIgnoreCase("on")
																				// ||
																				// env.equalsIgnoreCase(conTRUE));
									if (setEnv) {
										if (additional_envvars == null)
											additional_envvars = new TreeMap<String, String>();
										additional_envvars.put(nodeMap.getNamedItem(conAttributeNameNAME).getNodeValue(), value);
									}
								}
							}
						}
					}
				}

				// add parameters from order with name state/name=value
				debugx(3, "Looking for order state parameters from order params");

				String state = spooler_task.order().state();
				String[] parameterNames = spooler_task.order().params().names().split(";");
				for (String parameterName : parameterNames) {
					if (parameterName.startsWith(state + "/")) {
						String parName = parameterName.replaceAll("^" + state + "/", "");
						setOrderParam(parName, getOrderParam(parameterName));
						orderParameterKeys.add(parName);
					}
				}

				debugx(3, "Looking for global parameters from scheduler variables");

				Variable_set globalVariables = spooler.create_variable_set();
				globalVariables.merge(spooler.variables());
				globalVariables.merge(spooler_task.params());
				if (globalVariables.value(conVariableNameGLOBAL_CONFIGURATION_PARAMS) != null
						&& globalVariables.value(conVariableNameGLOBAL_CONFIGURATION_PARAMS).length() > 0) {
					String globalFile = globalVariables.value(conVariableNameGLOBAL_CONFIGURATION_PARAMS);
					JSFile objFile = new JSFile(globalFile);
					if (objFile.canRead()) {
						getLogger().debug3("Reading global parameters from " + globalFile);

						SOSXMLXPath globalXPath = new SOSXMLXPath(globalFile);
						NodeList globalParams = globalXPath.selectNodeList("//params/param");
						for (int i = 0; i < globalParams.getLength(); i++) {
							Node node = globalParams.item(i);
							if (node.getNodeName().equalsIgnoreCase(conTagNamePARAM)) {
								NamedNodeMap nodeMap = node.getAttributes();
								boolean hidden = false;
								if (nodeMap.getNamedItem(conAttributeNameHIDDEN) != null
										&& nodeMap.getNamedItem(conAttributeNameHIDDEN).getNodeValue().equalsIgnoreCase(conTRUE))
									hidden = true;
								if (nodeMap != null && nodeMap.getNamedItem(conAttributeNameNAME) != null) {
									String value = "";
									if (nodeMap.getNamedItem(conAttributeNameVALUE) != null) {
										value = nodeMap.getNamedItem(conAttributeNameVALUE).getNodeValue();
									}
									else {
										NodeList children = node.getChildNodes();
										for (int j = 0; j < children.getLength(); j++) {
											Node item = children.item(j);
											switch (item.getNodeType()) {
												case Node.TEXT_NODE:
												case Node.CDATA_SECTION_NODE:
													value += item.getNodeValue();
											}
										}
									}
									String strT = nodeMap.getNamedItem(conAttributeNameNAME).getNodeValue(); // name="strT"
									if (hidden) {
										debugx(3, ".. configuration parameter [" + strT + "]: *****");
									}
									else {
										debugx(3, ".. configuration parameter [" + strT + "]: " + value);
									}
									globalVariables.set_var(strT, value);

									// set additional environment variables for
									// parameters of this state
									if (globalEnv || nodeMap.getNamedItem(conNamedItemENV) != null) {
										if (nodeMap.getNamedItem(conNamedItemENV) != null)
											env = nodeMap.getNamedItem(conNamedItemENV).getNodeValue();

										boolean setEnv = globalEnv || isTrue(env); // (env.equalsIgnoreCase("yes")
																					// ||
																					// env.equals("1")
																					// ||
																					// env.equalsIgnoreCase("on")
																					// ||
																					// env.equalsIgnoreCase(conTRUE));
										if (setEnv) {
											if (additional_envvars == null)
												additional_envvars = new TreeMap();
											additional_envvars.put(nodeMap.getNamedItem(conAttributeNameNAME).getNodeValue(), value);
										}
									}
								}
							}
						}
					}
					// globalParams
				}

				substituteOrderParamsInTaskParams();
				substitute(spooler_task.params(), globalVariables);
				substitute(spooler_task.order().params(), globalVariables);

				debugx(1, "Merged order parameters after substitutions:");
				ConfigurationBaseMonitor.logParameters(spooler_task.order().params(), this.getLogger());
			}

			return this.getConfiguration();
		}
		catch (Exception e) {
			this.getLogger().warn(conClassName + ": Monitor: error occurred preparing configuration: " + e.getMessage());
			throw new JobSchedulerException(this.getLogger().getWarning(), e);
		}
	}

	private void substituteOrderParamsInTaskParams() {
		ParameterSubstitutor parameterSubstitutor = new ParameterSubstitutor();
		String[] parameterNames = spooler_task.order().params().names().split(";");
		for (String paramName : parameterNames) {
			String paramValue = String.valueOf(getOrderParam(paramName));
			debugx(3, "addKey " + paramName + "=" + paramValue);
			parameterSubstitutor.addKey(paramName, paramValue);
		}
		parameterNames = spooler_task.params().names().split(";");
		for (String parameterName : parameterNames) {
			String paramName = parameterName;
			String paramValue = String.valueOf(spooler_task.params().value(paramName));
			paramValue = parameterSubstitutor.replace(paramValue);
			if (!paramName.trim().equals("")) {
				debugx(3, "set " + paramName + "=" + paramValue);
				spooler_task.params().set_var(paramName, paramValue);
			}
		}
	}

	// TODO allow additional parameter patterns: %...%, $...
	private void substitute(final Variable_set objParams, final Variable_set globalVariables) throws Exception {

		String[] parameterNames = objParams.names().split(";");
		Pattern paramPattern = Pattern.compile("(?m)^.*\\$\\{.*\\}.*$");
		int metaTrials = 0;
		while (metaTrials <= 1) {
			for (int i = 0; i < parameterNames.length; i++) {// iterate over
				String currParamValue = String.valueOf(objParams.value(parameterNames[i]));
				// if ( currParam != null &&
				// currParam.matches("^.*\\$\\{.*\\}.*$")) {
				if (currParamValue != null && paramPattern.matcher(currParamValue).find()) {
					boolean parameterFound = false;
					boolean globalParameterFound = false;
					boolean envFound = false;
					boolean additionalEnvFound = false;
					String parameterValue = objParams.value(parameterNames[i]);
					int trials = 0;
					while (parameterValue.indexOf(conVariableStartString) != -1 && trials <= 1) {
						debugx(6, "substitution trials: " + trials + " --> " + parameterValue);
						for (int j = 0; j < parameterNames.length; j++) {// iterate
							this.getLogger().debug9(
									"parameterNames[j]=" + parameterNames[j] + " -->"
											+ contains(parameterValue, conVariableStartString + parameterNames[j] + "}", false));

							if (!parameterNames[i].equals(parameterNames[j])
									&& (contains(parameterValue, conVariableStartString + parameterNames[j].toUpperCase() + "}", false)
											|| contains(parameterValue, conVariableTypeBASENAME + parameterNames[j] + "}", false) || contains(parameterValue,
												conVariableTypeFILE_CONTENT + parameterNames[j] + "}", false))) {
								String jParameterValue = objParams.value(parameterNames[j]);
								if (parameterValue.indexOf(conVariableTypeBASENAME + parameterNames[j] + "}") != -1) {
									parameterValue = myReplaceAll(parameterValue, "(?i)\\$\\{basename:" + parameterNames[j] + "\\}",
											new File(objParams.value(parameterNames[j])).getName().replaceAll("[\\\\]", "\\\\\\\\"));
									parameterFound = true;
									trials = 0;
								}
								else
									if (parameterValue.indexOf(conVariableTypeFILE_CONTENT + parameterNames[j] + "}") != -1) {
										if (jParameterValue.indexOf(conVariableStartString) != -1) {
											getLogger().debug9("file_content parameter still contains other parameters.");
											metaTrials = 0;
										}
										else {
											File contentFile = new File(objParams.value(parameterNames[j]));
											String fileContent = "";
											try {
												fileContent = SOSFile.readFile(contentFile);
											}
											catch (Exception e) {
												getLogger().warn(conClassName + ": Failed to read file: " + contentFile.getAbsolutePath());
											}
											parameterValue = myReplaceAll(parameterValue, "(?i)\\$\\{file_content:" + parameterNames[j] + "\\}",
													fileContent.replaceAll("[\\\\]", "\\\\\\\\"));
											parameterFound = true;
											trials = 0;
										}
									}
									else {
										parameterValue = myReplaceAll(parameterValue, "(?i)\\$\\{" + parameterNames[j] + "\\}",
												objParams.value(parameterNames[j]).replaceAll("[\\\\]", "\\\\\\\\"));
										parameterFound = true;
										trials = 0;
									}

							}
						}
						trials++;
					}

					if (globalVariables.count() > 0) {
						String[] globalNames = globalVariables.names().split(";");
						for (String globalName : globalNames) {
							debugx(9, "globalNames[j]=" + globalName + " -->" + contains(parameterValue, conVariableStartString + globalName + "}", false));
							String jParameterValue = globalVariables.value(globalName);
							if (contains(parameterValue, conVariableStartString + globalName.toUpperCase() + "}", false)
									|| contains(parameterValue, conVariableTypeBASENAME + globalName + "}", false)
									|| contains(parameterValue, conVariableTypeFILE_CONTENT + globalName + "}", false)) {
								if (parameterValue.indexOf(conVariableTypeBASENAME) != -1) {
									parameterValue = myReplaceAll(parameterValue, "(?i)\\$\\{basename:" + globalName + "\\}",
											new File(globalVariables.value(globalName)).getName().replaceAll("[\\\\]", "\\\\\\\\"));
								}
								else
									if (parameterValue.indexOf(conVariableTypeFILE_CONTENT + globalName + "}") != -1) {
										if (jParameterValue.indexOf(conVariableStartString) != -1) {
											getLogger().debug9("file_content parameter still contains other parameters.");
											metaTrials = 0;
										}
										else {
											File contentFile = new File(globalVariables.value(globalName));
											String fileContent = "";
											try {
												fileContent = SOSFile.readFile(contentFile);
											}
											catch (Exception e) {
												getLogger().warn(conClassName + ": Failed to read file: " + contentFile.getAbsolutePath());
											}
											parameterValue = myReplaceAll(parameterValue, "(?i)\\$\\{file_content:" + globalName + "\\}",
													fileContent.replaceAll("[\\\\]", "\\\\\\\\"));
										}
									}
									else {
										parameterValue = myReplaceAll(parameterValue, "(?i)\\$\\{" + globalName + "\\}", globalVariables.value(globalName)
												.replaceAll("[\\\\]", "\\\\\\\\"));
									}
								globalParameterFound = true;
							}
						}

					}

					String casePrefix = "";
					if (!envVarsCaseSensitive)
						casePrefix = "(?i)";
					if (envvars != null) {
						Iterator envIterator = envvars.keySet().iterator();

						while (envIterator.hasNext()) {
							try {
								Object envName = envIterator.next();
								Object envValue = envvars.get(envName.toString());

								if (contains(parameterValue, conVariableStartString + envName.toString() + "}", envVarsCaseSensitive)) {
									parameterValue = myReplaceAll(parameterValue, casePrefix + "\\$\\{" + envName.toString() + "\\}", envValue.toString()
											.replaceAll("[\\\\]", "\\\\\\\\"));
									envFound = true;
								}
								else
									if (contains(parameterValue, conVariableTypeBASENAME + envName.toString() + "}", envVarsCaseSensitive)) {
										parameterValue = myReplaceAll(parameterValue, casePrefix + "\\$\\{basename:" + envName.toString() + "\\}", new File(
												envValue.toString()).getName().replaceAll("[\\\\]", "\\\\\\\\"));
										envFound = true;
									}
							}
							catch (Exception e) {
								getLogger().warn(conClassName + ": Error reading envar");
							}

						}
					}

					if (additional_envvars != null) {
						Iterator envIterator = additional_envvars.keySet().iterator();
						while (envIterator.hasNext()) {
							Object envName = envIterator.next();
							Object envValue = additional_envvars.get(envName.toString());
							if (contains(parameterValue, conVariableStartString + envName + "}", envVarsCaseSensitive)) {
								parameterValue = myReplaceAll(parameterValue, casePrefix + "\\$\\{" + envName.toString() + "\\}", envValue.toString()
										.replaceAll("[\\\\]", "\\\\\\\\"));
								additionalEnvFound = true;
							}
							else
								if (contains(parameterValue, conVariableTypeBASENAME + envName.toString() + "}", envVarsCaseSensitive)) {
									parameterValue = myReplaceAll(parameterValue, casePrefix + "\\$\\{basename:" + envName.toString() + "\\}", new File(
											envValue.toString()).getName().replaceAll("[\\\\]", "\\\\\\\\"));
									additionalEnvFound = true;
								}
								else
									if (contains(parameterValue, conVariableTypeFILE_CONTENT + envName.toString() + "}", envVarsCaseSensitive)) {
										if (envValue.toString().indexOf(conVariableStartString) != -1) {
											getLogger().debug9("file_content parameter still contains other parameters.");
											metaTrials = 0;
										}
										else {
											File contentFile = new File(envValue.toString());
											String fileContent = "";
											try {
												fileContent = SOSFile.readFile(contentFile);
											}
											catch (Exception e) {
												getLogger().warn(conClassName + ": Failed to read file: " + contentFile.getAbsolutePath());
											}
											parameterValue = myReplaceAll(parameterValue, casePrefix + "\\$\\{file_content:" + envName.toString() + "\\}",
													fileContent.replaceAll("[\\\\]", "\\\\\\\\"));
											additionalEnvFound = true;
										}
									}
						}
					}

					Variable_set objOrderParams = objParams;
					if (parameterFound) {
						debugx(3, "parameter substitution [" + parameterNames[i] + "]: " + parameterValue);
						objOrderParams.set_var(parameterNames[i], parameterValue);
					}

					if (globalParameterFound) {
						debugx(3, "global parameter substitution [" + parameterNames[i] + "]: " + parameterValue);
						objOrderParams.set_var(parameterNames[i], parameterValue);
					}

					if (envFound) {
						debugx(3, "environment variable substitution [" + parameterNames[i] + "]: " + parameterValue);
						objOrderParams.set_var(parameterNames[i], parameterValue);
					}

					if (additionalEnvFound) {
						debugx(3, "additional environment variable substitution [" + parameterNames[i] + "]: " + parameterValue);
						objOrderParams.set_var(parameterNames[i], parameterValue);
					}
				}
			}
			metaTrials++;
		}
	}

	// TODO use the base class JobSchedulerJobAdapter
	public static void logParameters(final Variable_set params, final SOSLogger logger) {
		String[] names = params.names().split(";");
		for (String name : names) {
			try {
				logger.debug1(".. parameter [" + name + "]: " + params.value(name));
			}
			catch (Exception e) {
				// no exception handling here
			}
		}
	}

	/**
	 * cleanup order parameters
	 */
	public void cleanupConfiguration() throws Exception {

		try {
			if (orderParameterKeys != null) {
				Variable_set resultParameters = spooler.create_variable_set();
				String[] parameterNames = spooler_task.order().params().names().split(";");
				for (String strParamName : parameterNames) {
					if (!orderParameterKeys.contains(strParamName) && !strParamName.equals("")) {
						String strValue = getOrderParam(strParamName);
//						assert strValue != null;
						debugx(9, String.format("set '%1$s' to value '%2$s'", strParamName, strValue));
						resultParameters.set_var(strParamName, strValue );
					}
					else {
						// bug oder feature? how to handle?
					}
				}
				if (spooler_task.order() != null) {
					spooler_task.order().set_params(resultParameters);
				}
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException(conClassName + ": error occurred in monitor on cleanup: " + e.getMessage(), e);
		}
	}

	/**
	 * sendet Mail mit den Einstellungen des Job Schedulers
	 *
	 * @param recipient
	 * @param subject
	 * @param body
	 * @throws Exception
	 */
	public void sendMail(final String recipient, final String recipientCC, final String recipientBCC, final String subject, final String body) throws Exception {
		// TODO use mail options
		try {
			SOSMail sosMail = new SOSMail(spooler_log.mail().smtp());

			sosMail.setQueueDir(spooler_log.mail().queue_dir());
			sosMail.setFrom(spooler_log.mail().from());
			sosMail.setContentType("text/plain");
			sosMail.setEncoding("Base64");

			String recipients[] = recipient.split(",");
			for (int i = 0; i < recipients.length; i++) {
				if (i == 0)
					sosMail.setReplyTo(recipients[i].trim());
				sosMail.addRecipient(recipients[i].trim());
			}

			String recipientsCC[] = recipientCC.split(",");
			for (String element : recipientsCC) {
				sosMail.addCC(element.trim());
			}

			String recipientsBCC[] = recipientBCC.split(",");
			for (String element : recipientsBCC) {
				sosMail.addBCC(element.trim());
			}

			sosMail.setSubject(subject);
			sosMail.setBody(body);
			sosMail.setSOSLogger(this.getLogger());

			this.getLogger().info("sending mail: \n" + sosMail.dumpMessageAsString());

			if (!sosMail.send()) {
				this.getLogger().warn(
						"mail server is unavailable, mail for recipient [" + recipient + "] is queued in local directory [" + sosMail.getQueueDir() + "]:"
								+ sosMail.getLastError());
			}

			sosMail.clearRecipients();
		}
		catch (Exception e) {
			throw new JobSchedulerException(conClassName + ": error occurred in monitor sending mail: " + e.getMessage(), e);
		}
	}

	/**
	 * @return Returns the sosLogger.
	 */
	public SOSSchedulerLogger getLogger() {
		return sosLogger;
	}

	/**
	 * @param sosLogger1
	 *            The sosLogger to set.
	 */
	public void setLogger(final SOSSchedulerLogger sosLogger1) {
		sosLogger = sosLogger1;
	}

	/**
	 * @return Returns the configuration.
	 */
	public Document getConfiguration() {
		return configuration;
	}

	/**
	 * @param configuration1
	 *            The configuration to set.
	 */
	public void setConfiguration(final Document configuration1) {
		configuration = configuration1;
	}

	/**
	 * @return Returns the configurationPath.
	 */
	public String getConfigurationPath() {

		return configurationPath;
	}

	/**
	 * @param configurationPath
	 *            The configurationPath to set.
	 */
	public void setConfigurationPath(final String configurationPath) {
		if (configurationPath != null) {
			this.configurationPath = configurationPath;
		}
	}

	/**
	 * @return Returns the configurationFilename.
	 */
	public String getConfigurationFilename() {

		return configurationFilename;
	}

	/**
	 * @param configurationFilename
	 *            The configurationFilename to set.
	 */
	public void setConfigurationFilename(final String configurationFilename) {
		if (configurationFilename != null) {
		    spooler_log.debug9("...setConfigurationFilename:" + configurationFilename);
			this.configurationFilename = configurationFilename;
		}
	}

	private void setEnvVars() throws Exception {

		String OS = System.getProperty(conSystemPropertyOS_NAME).toLowerCase();
		boolean win = false;
		envvars = new TreeMap();

		if (OS.indexOf("nt") > -1 || OS.indexOf("windows") > -1) {
			win = true;
			envVarsCaseSensitive = false;
		}

		Variable_set env = spooler_task.create_subprocess().env();
		debugx(9, "environment variable names: " + env.names());
		StringTokenizer t = new StringTokenizer(env.names(), ";");
		while (t.hasMoreTokens()) {
			String envname = t.nextToken();
			if (envname != null) {
				String envvalue = env.value(envname);
				if (win) {
					debugx(9, "set environment variable: " + envname.toUpperCase() + "=" + envvalue);
					envvars.put(envname.toUpperCase(), envvalue);
				}
				else {
					debugx(9, "set environment variable: " + envname + "=" + envvalue);
					envvars.put(envname, envvalue);
				}
			}
		}
	}

	// TODO move to base class
	private int logLevel2Int(final String l) {
		HashMap<String, String> levels = new HashMap<String, String>();
		if (l == null) {
			return this.getLogger().getLogLevel();
		}
		else {
			levels.put("info", "10");
			levels.put("warn", "11");
			levels.put("error", "12");
			levels.put("debug1", "1");
			levels.put("debug2", "2");
			levels.put("debug3", "3");
			levels.put("debug4", "4");
			levels.put("debug5", "5");
			levels.put("debug6", "6");
			levels.put("debug7", "7");
			levels.put("debug8", "8");
			levels.put("debug9", "9");

			if (levels.get(l) != null) {
				return Integer.parseInt(levels.get(l).toString());
			}
			else {
				return this.getLogger().getLogLevel();
			}
		}
	}

	public static String myReplaceAll(final String source, final String what, final String replacement) {

		String newReplacement = replacement.replaceAll("\\$", "\\\\\\$");
		newReplacement = newReplacement.replaceAll("\"", "");
		return source.replaceAll("(?m)" + what, newReplacement);
	}

	public static boolean contains(final String searchString, final String otherString, final boolean caseSensitive) {
		if (caseSensitive)
			return searchString.indexOf(otherString) != -1;
		else
			return searchString.toUpperCase().indexOf(otherString.toUpperCase()) != -1;
	}

	@SuppressWarnings("unused")
	private void debug3(final String pstrDebugMessage) {

		final String conMethodName = conClassName + "::debug3";

		debugx(3, pstrDebugMessage);

	} // private void debug3

	private void debugx(final int intDebugLevel, final String pstrMsg) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::debug3";

		try {
			switch (intDebugLevel) {
				case 1:
					this.getLogger().debug1(conClassName + ": " + pstrMsg);
					break;

				case 2:
					this.getLogger().debug2(conClassName + ": " + pstrMsg);
					break;

				case 3:
					this.getLogger().debug3(conClassName + ": " + pstrMsg);
					break;

				case 6:
					this.getLogger().debug6(conClassName + ": " + pstrMsg);
					break;

				case 9:
					this.getLogger().debug9(conClassName + ": " + pstrMsg);
					break;

				default:
					this.getLogger().debug(conClassName + ": " + pstrMsg);
					break;
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}

	} // private void debug3

	private boolean isTrue(final String pstrValue) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::isTrue";

		boolean flgRC = pstrValue.equalsIgnoreCase("yes") || pstrValue.equals("1") || pstrValue.equalsIgnoreCase("on") || pstrValue.equalsIgnoreCase(conTRUE);

		return flgRC;
	} // private boolean isTrue
}
