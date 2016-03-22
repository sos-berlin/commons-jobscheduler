package sos.scheduler.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

/** @author SGLO111 */
public class SOSXMLHelper {

    private static final Logger LOGGER = Logger.getLogger(SOSXMLHelper.class);
    private int intIndent = 0;
    private String fleXMLFileName = "";
    private String fleXSLTFileName = "";
    private String strXMLDirective = "?xml version='1.0' encoding='ISO-8859-1' ?";
    private String strXSLDirective = "?xml-stylesheet href='$FileName$' type='text/sos.scheduler.xsl' ?";
    private PrintWriter fleOut = null;
    private long lngNoOfLinesWritten = 0;

    public SOSXMLHelper() {

    }

    public SOSXMLHelper(String pstrXMLFileName) {
        this.fleXMLFileName = pstrXMLFileName;
        LOGGER.debug(this.fleXMLFileName);
        this.fleXSLTFileName = "http://be-list-viewer.eu.schering.net/systems/sos.scheduler.xsl/SOSFolderTree.xsl";
    }

    public SOSXMLHelper(String pstrXMLFileName, String pstrXSLTFileName) {
        this.fleXMLFileName = pstrXMLFileName;
        LOGGER.debug("XML-File Name: " + this.fleXMLFileName);
        this.fleXSLTFileName = pstrXSLTFileName;
        LOGGER.debug("XSLT-File Name: " + this.fleXSLTFileName);
    }

    public void XMLTagV(String strTagName, String strTagValue) throws Exception {
        Out(this.getStartTag(strTagName) + strTagValue + this.getEndTag(strTagName));
    }

    public long NoOfLinesWritten() {
        return this.lngNoOfLinesWritten;
    }

    public void XMLTagV(String strTagName, int intTagValue) throws Exception {
        this.XMLTagV(strTagName, String.valueOf(intTagValue));
    }

    public void XMLTagV(String strTagName, long lngTagValue) throws Exception {
        this.XMLTagV(strTagName, String.valueOf(lngTagValue));
    }

    public void XMLTag(String strTagName) throws Exception {
        Out(getStartTag(strTagName));
        this.intIndent++;
    }

    public void XMLTagE(String strTagName) throws Exception {
        this.intIndent--;
        Out(getEndTag(strTagName));
    }

    public String getStartTag(String strTagName) {
        return "<" + strTagName + ">";
    }

    public String getEndTag(String strTagName) {
        return "</" + strTagName + ">";
    }

    public void Out(String strBuffer) throws IOException, Exception {
        if (fleOut == null) {
            if (this.fleXMLFileName.length() <= 0) {
                throw new Exception("no output-file-name given");
            }
            File fl = new File(this.fleXMLFileName);
            fl.createNewFile();
            fleOut = new PrintWriter(new FileWriter(fl));
            fleOut.println(getStartTag(strXMLDirective));
            if (this.fleXSLTFileName.length() > 0) {
                String strT = strXSLDirective.replaceAll("\\$FileName\\$", this.fleXSLTFileName);
                fleOut.println(getStartTag(strT));
            }
        }
        if (fleOut == null) {
            throw new Exception("no output-file specfied");
        }
        for (int i = 0; i < this.intIndent; i++) {
            fleOut.print(" ");
        }
        fleOut.println(strBuffer);
        lngNoOfLinesWritten++;
    }

    public int getIndent() {
        return this.intIndent;
    }

    public void setXMLFileName(String pfleXMLFileName) throws IOException {
        this.fleXMLFileName = pfleXMLFileName;
    }

    public void close() {
        if (fleOut != null) {
            fleOut.close();
            fleOut = null;
        }
    }

}
