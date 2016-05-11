package com.sos.JSHelper.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.xalan.xslt.EnvironmentCheck;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sos.JSHelper.DataElements.JSDataElementDateISO;
import com.sos.JSHelper.Options.SOSOptionFileName;

/** @author kb */
public class JSXMLFile extends JSTextFile {

    protected final String conMissingDate = "0000-00-00";
    protected final String conMissingDate2 = "--";
    protected HashMap<String, String> hsmParameters = null;
    private static final Logger LOGGER = Logger.getLogger(JSXMLFile.class);
    private static final long serialVersionUID = 1L;
    private Boolean flgFileIsOpen = false;
    private int intIndent = 0;
    private Boolean flgDecrIndent = false;
    private Boolean flgIncrIndent = false;
    private Vector<SOSXMLAttribute> lstAttributes = null;
    private int intNumberOfXMLDeclarations = 0;
    public static final String conORG_APACHE_XERCES_JAXP_DOCUMENT_BUILDER_FACTORY_IMPL = "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl";
    Document document = null;

    public JSXMLFile() {
        super(null);
    }

    public JSXMLFile(final SOSOptionFileName pobjOptionElementFileName) throws Exception {
        super(pobjOptionElementFileName.Value());
        strFileName = pobjOptionElementFileName.Value();
    }

    public JSXMLFile(final String pstrFileName) throws Exception {
        super(pstrFileName);
        strFileName = pstrFileName;
    }

    public void open(final String pstrOutputFileName) throws Exception {
        //
    }

    public JSXMLFile XMLTagV(final String pstrTagName, final String pstrTagValue) throws Exception {
        return this.newTag(pstrTagName, pstrTagValue);
    }

    private String getStartTag(final String strTagName) {
        final String strT = addBraces(adjustTagName(strTagName) + BuildAttributesString());
        if (strTagName.startsWith("/")) {
            flgDecrIndent = true;
        } else {
            flgIncrIndent = true;
        }
        return strT;
    }

    private String getEmptyTag(final String strTagName) {
        final String strT = addBraces(adjustTagName(strTagName) + BuildAttributesString() + " /");
        flgDecrIndent = true;
        flgIncrIndent = true;
        return strT;
    }

    private String BuildAttributesString() {
        String strT = "";
        if (lstAttributes != null) {
            for (int i = 0; i < lstAttributes.size(); i++) {
                strT += " " + lstAttributes.elementAt(i).toString();
            }
            lstAttributes = null;
        }
        return strT;
    }

    private String adjustTagName(final String pstrTagName) {
        return pstrTagName;
    }

    private String addBraces(final String pstrParam) {
        return "<" + pstrParam + ">";
    }

    private String getEndTag(final String strTagName) {
        flgDecrIndent = true;
        return addBraces("/" + adjustTagName(strTagName));
    }

    public String tag(final String pstrTagName, final String pstrTagValue) {
        String strValue = pstrTagValue;
        if (strValue == null) {
            strValue = "";
        } else {
            if (strValue.matches(".*[&<>].*") && !pstrTagValue.contains("![CDATA[")) {
                strValue = MakeCData(strValue);
            }
        }
        String strTemp = "";
        if (strValue.isEmpty()) {
            strTemp = String.format("%1$s", getEmptyTag(pstrTagName));
        } else {
            strTemp = String.format("%1$s%2$s%3$s", getStartTag(pstrTagName), strValue, getEndTag(pstrTagName));
        }
        return strTemp;
    }

    public JSXMLFile newTag(final String pstrTagName, final String pstrTagValue) throws Exception {
        String strValue = pstrTagValue;
        if (strValue == null) {
            strValue = "";
        } else if (strValue.matches(".*[&<>].*") && !pstrTagValue.contains("![CDATA[")) {
            strValue = MakeCData(strValue);
        }
        String strTemp = "";
        if (strValue.isEmpty()) {
            strTemp = String.format("%1$s", getEmptyTag(pstrTagName));
        } else {
            strTemp = String.format("%1$s%2$s%3$s", getStartTag(pstrTagName), strValue, getEndTag(pstrTagName));
        }
        Write2File(strTemp);
        return this;
    }

    public JSXMLFile Write2File(final String pstrS) throws Exception {
        String strT = "";
        if (!flgFileIsOpen) {
            flgFileIsOpen = true;
        }
        if (flgIncrIndent) {
            strT = getIndent();
            intIndent++;
            flgIncrIndent = false;
        }
        if (flgDecrIndent) {
            intIndent--;
            flgDecrIndent = false;
            strT = getIndent();
        }
        super.Write(strT + pstrS);
        return this;
    }

    private String getIndent() {
        String strT = "";
        if (intIndent > 0) {
            strT = "";
            for (int i = 0; i < intIndent; i++) {
                strT += "\t";
            }
        }
        return strT;

    }

    public boolean isOpened() {
        return flgFileIsOpen;
    }

    public JSXMLFile writeXMLDeclaration(final String pstrCharset) throws Exception {
        String strCharset = pstrCharset;
        if (strCharset == null || strCharset.isEmpty()) {
            strCharset = "ISO-8859-1";
        }
        Write2File(addBraces("?xml version='1.0' encoding='" + strCharset + "' ?"));
        intNumberOfXMLDeclarations++;
        return this;
    }

    public JSXMLFile writeXMLDeclaration() throws Exception {
        writeXMLDeclaration(null);
        return this;
    }

    public JSXMLFile comment(final String pstrComment) throws Exception {
        Write2File(addBraces("!-- " + pstrComment + " --"));
        return this;
    }

    public String tag(final String pstrTagName) {
        return getStartTag(pstrTagName);
    }

    public JSXMLFile newTag(final String pstrTagName) throws Exception {
        Write2File(getStartTag(pstrTagName));
        return this;
    }

    public JSXMLFile endTag(final String pstrTagName) throws Exception {
        Write2File(getEndTag(pstrTagName));
        return this;
    }

    public JSXMLFile newCDataTag(final String pstrTagName) throws Exception {
        Write2File(getStartTag(pstrTagName) + "<![CDATA[");
        return this;
    }

    public JSXMLFile endCDataTag(final String pstrTagName) throws Exception {
        Write2File("]]>" + getEndTag(pstrTagName));
        return this;
    }

    public JSXMLFile newCDataTag(final String pstrTagName, final String pstrTagValue) throws Exception {
        if (pstrTagValue != null && pstrTagName != null && !pstrTagValue.isEmpty()) {
            this.newTag(pstrTagName, MakeCData(pstrTagValue));
        }
        return this;
    }

    public String MakeCData(final String pstrValue) {
        return addBraces("![CDATA[" + pstrValue + "]]");
    }

    public JSXMLFile newDateTag(final String pstrTagName, final String pstrTagValue) throws Exception {
        if (!(pstrTagValue != null && (conMissingDate.equals(pstrTagValue) || "".equals(pstrTagValue) || conMissingDate2.equals(pstrTagValue.trim())))) {
            this.newTag(pstrTagName, pstrTagValue);
        }
        return this;
    }

    public JSXMLFile addAttribute(final String pstrAttributeName, final String pstrAttributeValue) {
        final SOSXMLAttribute objAtt = new SOSXMLAttribute(pstrAttributeName, pstrAttributeValue);
        if (lstAttributes == null) {
            lstAttributes = new Vector<SOSXMLAttribute>();
        }
        lstAttributes.add(objAtt);
        return this;
    }

    public void Transform(final File xslFile, final File outputFile) throws TransformerException, TransformerConfigurationException,
            FileNotFoundException, Exception {
        Transformer transformer = null;
        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
        try {
            final TransformerFactory tFactory = TransformerFactory.newInstance();
            transformer = tFactory.newTransformer(new StreamSource(xslFile));
        } catch (TransformerFactoryConfigurationError e) {
            throw e;
        } catch (TransformerException e) {
            throw e;
        }
        DOMSource objDS = new DOMSource(this.getDomDocument());
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transf = tf.newTransformer();
        if (hsmParameters != null) {
            for (String strKey : hsmParameters.keySet()) {
                String strValue = hsmParameters.get(strKey);
                LOGGER.debug(String.format("Set XSLT-Parameter '%1$s' to value '%2$s'", strKey, strValue));
                transf.setParameter(strKey, strValue);
                transformer.setParameter(strKey, strValue);
            }
        }
        JSDataElementDateISO objISODate = new JSDataElementDateISO();
        objISODate.Now();
        transf.setParameter("sos.timestamp", objISODate.Now());
        transformer.setParameter("sos.timestamp", objISODate.Now());
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transf.transform(objDS, result);
        LOGGER.debug(writer.toString());
        if (outputFile == null) {
            transformer.transform(objDS, new StreamResult(new java.io.OutputStreamWriter(System.out)));
        } else {
            transformer.transform(objDS, new StreamResult(new FileOutputStream(outputFile)));
        }
    }

    @Override
    public String getContent() {
        String strT = "";
        try {
            DOMSource objDS = new DOMSource(this.getDomDocument());
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transf = tf.newTransformer();
            transf.transform(objDS, result);
            strT = writer.toString();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return strT;
    }

    public void Validate() throws SAXException, IOException {
        if (intNumberOfXMLDeclarations > 1) {
            return;
        }
    }

    class SOSXMLAttribute {

        private String strAttributeName = null;
        private String strAttributeValue = null;

        public SOSXMLAttribute() {
            super();
        }

        public SOSXMLAttribute(final String pstrAttributeName, final String pstrAttributeValue) {
            strAttributeName = pstrAttributeName;
            strAttributeValue = pstrAttributeValue;
        }

        @Override
        public String toString() {
            return strAttributeName + "='" + strAttributeValue + "'";
        }
    }

    class SOSXMLTag {

        private String Name;
        private String Value;
        private Boolean isCdata;
        private int DataType;

        public SOSXMLTag() {
            //
        }

        @Override
        public String toString() {
            return "not implemented";
        }

        public SOSXMLTag(final String pstrTagName) {
            this(pstrTagName, "");
        }

        public SOSXMLTag(final String pstrTagName, final String pstrTagValue) {
            Name = pstrTagName;
            Value = pstrTagValue;
        }

        public int getDataType() {
            return DataType;
        }

        public void setDataType(final int dataType) {
            DataType = dataType;
        }

        public Boolean getIsCdata() {
            return isCdata;
        }

        public void setIsCdata(final Boolean isCdata1) {
            isCdata = isCdata1;
        }

        public String getName() {
            return Name;
        }

        public void setName(final String name) {
            Name = name;
        }

        public String getValue() {
            return Value;
        }

        public void setValue(final String value) {
            Value = value;
        }

    }

    public void error(final SAXParseException pobjException) throws SAXException {
        throw pobjException;
    }

    public void fatalError(final SAXParseException arg0) throws SAXException {
        //
    }

    public void warning(final SAXParseException arg0) throws SAXException {
        //
    }

    public Document getDomDocument() throws ParserConfigurationException {
        if (document == null) {
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory", conORG_APACHE_XERCES_JAXP_DOCUMENT_BUILDER_FACTORY_IMPL);
            DocumentBuilderFactory builderFactory =
                    DocumentBuilderFactory.newInstance(conORG_APACHE_XERCES_JAXP_DOCUMENT_BUILDER_FACTORY_IMPL, this.getClass().getClassLoader());
            builderFactory.setNamespaceAware(true);
            builderFactory.setXIncludeAware(true);
            String strUserDir = System.getProperty("user.dir");
            try {
                String strPath = new File(strFileName).getParent();
                if (strPath != null) {
                    System.setProperty("user.dir", strPath);
                }
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                LOGGER.debug("Namespace aware:" + builder.isNamespaceAware());
                LOGGER.debug("XInclude aware:" + builder.isXIncludeAware());
                Reader objR = new InputStreamReader(new FileInputStream(strFileName), "UTF-8");
                InputSource inS = new InputSource(objR);
                document = builder.parse(inS);
            } catch (ParserConfigurationException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (FileNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (SAXException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                System.setProperty("user.dir", strUserDir);
                LOGGER.debug(document.toString());
            }
        }
        return document;
    }

    public void writeDocument(final String pstrFileName) throws Exception {
        getDomDocument();
        this.writeXmlFile(document, pstrFileName);
    }

    public void writeXmlFile(final Document doc, final String filename) throws Exception {
        try {
            Source source = new DOMSource(doc);
            File file = new File(filename);
            Result result = new StreamResult(file.toURI().getPath());
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            throw e;
        } catch (TransformerException e) {
            throw e;
        }
    }

    public void setParameters(final HashMap<String, String> pobjHshMap) {
        hsmParameters = pobjHshMap;
    }

    public void EnvironmentCheck() throws Exception {
        EnvironmentCheck ec = new EnvironmentCheck();
        StringWriter sWri = new StringWriter();
        PrintWriter pWri = new PrintWriter(new StringWriter());
        ec.checkEnvironment(pWri);
        pWri.close();
        try {
            LOGGER.debug("Checking Xalan environment...");
            LOGGER.debug(sWri.toString());
        } catch (Exception ex) {
            throw ex;
        }
    }

}