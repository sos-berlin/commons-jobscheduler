package com.sos.JSHelper.io.Files;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.DataElements.JSDataElementDate;
import com.sos.JSHelper.DataElements.JSDateFormat;

/**
* \class JSXMLFileTest
*
* \brief JSXMLFileTest -
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
* @version $Id$24.09.2009
* \see reference
*
* Created on 24.09.2009 11:27:15
 */

public class JSXMLFileTest extends JSToolBox {

	@SuppressWarnings("unused")
	private final String			conClassName					= "JSXMLFileTest";

	private HashMap<String, String>	pobjHshMap						= null;

	private final String			conXsltParmExtendsClassName		= "ExtendsClassName";
	private final String			conXsltParmClassNameExtension	= "ClassNameExtension";
	private final String			conXsltParmVersion				= "version";
	private final String			conXsltParmSourceType			= "sourcetype";
	private final String			conXsltParmClassName			= "ClassName";
	private final String			conXsltParmWorkerClassName		= "WorkerClassName";

	String strBaseFolder = "R:/backup/sos/";
	String strBaseDirName = strBaseFolder + "java/development/com.sos.scheduler/src/sos/scheduler/jobdoc";

	public JSXMLFileTest() {
		BasicConfigurator.configure();
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		System.setProperty("user.dir", strBaseDirName);
		pobjHshMap = new HashMap<String, String>();

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testLoadXMLAsDocument() throws Exception {
		JSXMLFile fleFile = new JSXMLFile(strBaseFolder + "java/development/com.sos.scheduler/src/sos/scheduler/jobdoc/SOSSSHJob2JSAdapter.xml");
		Document objDoc = fleFile.getDomDocument();
		System.out.println("objDoc has " + objDoc.getChildNodes().getLength() + " childs");

		NodeList config = objDoc.getElementsByTagName("param");

		int intLength = config.getLength();
		System.out.println("Length is " + intLength);
		for (int i = 0; i < config.getLength(); i++) {
			Element objNode = (Element) config.item(i);
			System.out.println(objNode.getAttribute("name"));
		}
	}

	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testXSLTTransformWithParams() throws Exception {

		JSXMLFile fleFile = new JSXMLFile(strBaseDirName + "/SOSSSHJob2JSAdapter.xml");

		strXSLTFile = strBaseFolder + "java/development/com.sos.scheduler.editor/src/xsl/JSJobDoc2JSOptionSuperClass.xsl";
		JSXSLTFile objXSLTFile = new JSXSLTFile(strXSLTFile);

		setGeneralParameters();

		JSToolBox objTools = new JSToolBox();
		JSDataElementDate objDate = new JSDataElementDate(objTools.Now());
		objDate.setFormatPattern(JSDateFormat.dfTIMESTAMPS24);
		objDate.setParsePattern(JSDateFormat.dfTIMESTAMPS24);
		String strTimeStamp = objDate.FormattedValue();
		setXSLTParameter("timestamp", strTimeStamp);
		String strClassNameExtension = "OptionsSuperClass"; //$NON-NLS-1$

		String strWorkerClassName = "SOSSSHJob2JSAdapter";
		setXSLTParameter(conXsltParmClassNameExtension, strClassNameExtension);
		setXSLTParameter(conXsltParmExtendsClassName, "JSOptionsClass");
		setXSLTParameter(conXsltParmVersion, "version");
		setXSLTParameter(conXsltParmSourceType, "options"); //$NON-NLS-1$
		setXSLTParameter(conXsltParmClassName, strWorkerClassName);
		setXSLTParameter(conXsltParmWorkerClassName, strWorkerClassName);
		setXSLTParameter("XMLDocuFilename", fleFile.getAbsolutePath());
		setXSLTParameter("XSLTFilename", objXSLTFile.getAbsolutePath());

		fleFile.setParameters(pobjHshMap);

		JSTextFile objOutputFile = new JSTextFile("c:/temp/test.java");
		fleFile.Transform(objXSLTFile, objOutputFile);
		System.out.println(objOutputFile.getContent());
	}

	String strXSLTFile = "";
	String strXMLFileName = "";

	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testXSLTTransform() throws Exception {

		strXMLFileName = strBaseDirName + "/JobSchedulerPLSQLJob.xml";
		JSXMLFile fleXMLFile = new JSXMLFile(strXMLFileName);

		strXSLTFile = strBaseDirName + "/xsl/CreateMediaWikiFromSOSDoc.xsl";
		JSXSLTFile objXSLTFile = new JSXSLTFile(strXSLTFile);

		setGeneralParameters();
		fleXMLFile.setParameters(pobjHshMap);

		Document objDoc = fleXMLFile.getDomDocument();
		System.out.println("objDoc has " + objDoc.getChildNodes().getLength() + " childs");

		JSTextFile objOutputFile = new JSTextFile("c:/temp/test.mediaWiki");
		fleXMLFile.Transform(objXSLTFile, objOutputFile);
		 System.out.println(objOutputFile.getContent());

		 /*
		strXSLTFile = strBaseDirName + "/xsl/JSJobDoc2JSOptionClass.xsl";
		objXSLTFile = new JSXSLTFile(strXSLTFile);
		objXSLTFile.MustExist();

		objOutputFile = new JSTextFile("c:/temp/test.mm");
		fleXMLFile.Transform(objXSLTFile, objOutputFile);
		// System.out.println(objOutputFile.getContent());
		  */
	}

	private void setXSLTParameter(final String strVarName, final String strVarValue) {
		final String conMethodName = conClassName + "::setXSLTParameter";

		String strV = strVarValue;
		String strX = String.format("%3$s: Set parameter '%1$s' to Value %2$s.", strVarName, strV, conMethodName);
//		logger.info(strX);
		pobjHshMap.put(strVarName, strV);
	}

	private void setGeneralParameters () {
		setXSLTParameter("package_name", "package");
		setXSLTParameter("XSLTFileName", strXSLTFile);
		setXSLTParameter("XMLDocuFilename", strXMLFileName);

		setXSLTParameter("default_lang", "en");
		setXSLTParameter("standalone", "false");

	}
	// from: http://stackoverflow.com/questions/581939/default-support-for-xinclude-in-java-6

	@Test
	public void XIncludeExample() throws Exception {

		final String XML = "<?xml version=\"1.0\"?>\n" + "<data xmlns=\"foo\" xmlns:xi=\"http://www.w3.org/2001/XInclude\">\n"
				+ "<xi:include href=\"include.txt\" parse=\"text\"/>\n" + "</data>\n";

		final String INCLUDE = "Hello, World!";

		// data
		final InputStream xmlStream = new ByteArrayInputStream(XML.getBytes("UTF-8"));
		final InputStream includeStream = new ByteArrayInputStream(INCLUDE.getBytes("UTF-8"));
		// document parser
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setXIncludeAware(true);
		factory.setNamespaceAware(true);
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		if (!docBuilder.isXIncludeAware()) {
			throw new IllegalStateException();
		}
		docBuilder.setEntityResolver(new EntityResolver() {
			@Override
			public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
				if (systemId.endsWith("include.txt")) {
					return new InputSource(includeStream);
				}
				return null;
			}
		});
		Document doc = docBuilder.parse(xmlStream);
		// print result
		Source source = new DOMSource(doc);
		Result result = new StreamResult(System.out);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.transform(source, result);
	}

}
