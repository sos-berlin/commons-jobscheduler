package com.sos.VirtualFileSystem.Filter;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.Filter.Options.SOSFilterOptions;

/** @author KB */
public class SOSFilteredFileReader extends JSToolBox implements ISOSFilteredFileReader {

    protected SOSFilterOptions objFilterOptions = null;
    private static final Logger LOGGER = Logger.getLogger(SOSFilteredFileReader.class);
    private Vector<SOSNullFilter> lstFilters = null;
    private JSFile objFile2Read = null;
    private ISOSFilteredFileReader objProcessHandler = this;

    public SOSFilteredFileReader() {
        super();
    }

    public SOSFilteredFileReader(final ISOSFilteredFileReader pobjProcessHandler) {
        this();
        this.setProcesshandler(pobjProcessHandler);
    }

    public SOSFilteredFileReader(final JSFile pobjJSFile) {
        this();
        objFile2Read = pobjJSFile;
    }

    public void Options(final SOSFilterOptions pobjOptions) {
        objFilterOptions = pobjOptions;
    }

    public SOSFilterOptions Options() {
        if (objFilterOptions == null) {
            objFilterOptions = new SOSFilterOptions();
        }
        return objFilterOptions;
    }

    public void setProcesshandler(final ISOSFilteredFileReader pobjProcessHandler) {
        objProcessHandler = pobjProcessHandler;
    }

    public void setFile(final JSFile pobjJSFile) {
        objFile2Read = pobjJSFile;
    }

    public JSFile getCurrentFile() {
        return objFile2Read;
    }

    public void runMultipleFiles(final SOSOptionFolderName pobjFolderName) {
        this.runMultipleFiles(pobjFolderName.JSFile().getAbsolutePath());
    }

    public void runMultipleFiles(final String pstrPathName) {
        SOSOptionFolderName objFN = new SOSOptionFolderName(pstrPathName);
        for (File file : objFN.listFiles()) {
            this.setFile(new JSFile(file.getAbsolutePath()));
            this.run();
        }
    }

    public String getFilteredLine() {
        String strB = null;
        StringBuffer strBF = objFile2Read.GetLine();
        if (strBF != null) {
            strB = strBF.toString();
            for (SOSNullFilter sosNullFilter : lstFilters) {
                sosNullFilter.write(strB);
                strB = sosNullFilter.readString();
            }
        }
        return strB;
    }

    public void run() {
        objProcessHandler.atStartOfNewFile(objFile2Read);
        if (lstFilters == null) {
            lstFilters = objFilterOptions.getFilter();
        }
        StringBuffer strBuffer = null;
        while ((strBuffer = objFile2Read.GetLine()) != null) {
            String strB = strBuffer.toString();
            for (SOSNullFilter sosNullFilter : lstFilters) {
                sosNullFilter.write(strB);
                strB = sosNullFilter.readString();
            }
            if (strB != null) {
                objProcessHandler.processRecord(strB);
            }
        }
        try {
            objFilterOptions.startPostProcessing(null);
            objFile2Read.close();
            objFilterOptions.startCloseProcessing(null);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void processRecord(final String pstrRecord) {
        //
    }

    @Override
    public void atStartOfData() {

    }

    @Override
    public void atEndOfData() {

    }

    @Override
    public void atStartOfNewFile(JSFile file) {
        // TODO Auto-generated method stub
    }

}
