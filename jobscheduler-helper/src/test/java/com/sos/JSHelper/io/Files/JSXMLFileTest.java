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
import org.apache.log4j.Logger;
import org.junit.Before;
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

public class JSXMLFileTest extends JSToolBox {

    private static final Logger LOGGER = Logger.getLogger(JSXMLFileTest.class);
    private static final String XSLT_PARM_EXTENDS_CLASSNAME = "ExtendsClassName";
    private static final String XSLT_PARM_CLASSNAME_EXTENSION = "ClassNameExtension";
    private static final String XSLT_PARM_VERSION = "version";
    private static final String XSLT_PARM_SOURCE_TYPE = "sourcetype";
    private static final String XSLT_PARM_CLASSNAME = "ClassName";
    private static final String XSLT_PARM_WORKER_CLASSNAME = "WorkerClassName";
    private HashMap<String, String> pobjHshMap = null;
    String strBaseFolder = "R:/backup/sos/";
    String strBaseDirName = strBaseFolder + "java/development/com.sos.scheduler/src/sos/scheduler/jobdoc";
    String strXSLTFile = "";
    String strXMLFileName = "";

    public JSXMLFileTest() {
        BasicConfigurator.configure();
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("user.dir", strBaseDirName);
        pobjHshMap = new HashMap<String, String>();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testLoadXMLAsDocument() throws Exception {
        JSXMLFile fleFile = new JSXMLFile(strBaseFolder + "java/development/com.sos.scheduler/src/sos/scheduler/jobdoc/SOSSSHJob2JSAdapter.xml");
        Document objDoc = fleFile.getDomDocument();
        LOGGER.info("objDoc has " + objDoc.getChildNodes().getLength() + " childs");
        NodeList config = objDoc.getElementsByTagName("param");
        int intLength = config.getLength();
        LOGGER.info("Length is " + intLength);
        for (int i = 0; i < config.getLength(); i++) {
            Element objNode = (Element) config.item(i);
            LOGGER.info(objNode.getAttribute("name"));
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
        String strClassNameExtension = "OptionsSuperClass";
        String strWorkerClassName = "SOSSSHJob2JSAdapter";
        setXSLTParameter(XSLT_PARM_CLASSNAME_EXTENSION, strClassNameExtension);
        setXSLTParameter(XSLT_PARM_EXTENDS_CLASSNAME, "JSOptionsClass");
        setXSLTParameter(XSLT_PARM_VERSION, "version");
        setXSLTParameter(XSLT_PARM_SOURCE_TYPE, "options");
        setXSLTParameter(XSLT_PARM_CLASSNAME, strWorkerClassName);
        setXSLTParameter(XSLT_PARM_WORKER_CLASSNAME, strWorkerClassName);
        setXSLTParameter("XMLDocuFilename", fleFile.getAbsolutePath());
        setXSLTParameter("XSLTFilename", objXSLTFile.getAbsolutePath());
        fleFile.setParameters(pobjHshMap);
        JSTextFile objOutputFile = new JSTextFile("c:/temp/test.java");
        fleFile.Transform(objXSLTFile, objOutputFile);
        LOGGER.info(objOutputFile.getContent());
    }

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
        LOGGER.info("objDoc has " + objDoc.getChildNodes().getLength() + " childs");
        JSTextFile objOutputFile = new JSTextFile("c:/temp/test.mediaWiki");
        fleXMLFile.Transform(objXSLTFile, objOutputFile);
        LOGGER.info(objOutputFile.getContent());
    }

    private void setXSLTParameter(final String strVarName, final String strVarValue) {
        pobjHshMap.put(strVarName, strVarValue);
    }

    private void setGeneralParameters() {
        setXSLTParameter("package_name", "package");
        setXSLTParameter("XSLTFileName", strXSLTFile);
        setXSLTParameter("XMLDocuFilename", strXMLFileName);
        setXSLTParameter("default_lang", "en");
        setXSLTParameter("standalone", "false");
    }

    @Test
    public void XIncludeExample() throws Exception {
        final String XML =
                "<?xml version=\"1.0\"?>\n" + "<data xmlns=\"foo\" xmlns:xi=\"http://www.w3.org/2001/XInclude\">\n"
                        + "<xi:include href=\"include.txt\" parse=\"text\"/>\n" + "</data>\n";
        final String INCLUDE = "Hello, World!";
        final InputStream xmlStream = new ByteArrayInputStream(XML.getBytes("UTF-8"));
        final InputStream includeStream = new ByteArrayInputStream(INCLUDE.getBytes("UTF-8"));
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
        Source source = new DOMSource(doc);
        Result result = new StreamResult(System.out);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(source, result);
    }

}