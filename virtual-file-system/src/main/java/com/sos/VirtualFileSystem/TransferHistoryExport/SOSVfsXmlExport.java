package com.sos.VirtualFileSystem.TransferHistoryExport;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSXMLFile;
import com.sos.VirtualFileSystem.Interfaces.IJadeTransferDetailHistoryData;
import com.sos.VirtualFileSystem.Interfaces.IJadeTransferHistoryData;
import com.sos.VirtualFileSystem.Interfaces.ISOSTransferHistory;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;

/** @author oh */
public class SOSVfsXmlExport extends SOSVfsMessageCodes implements ISOSTransferHistory {

    private static final String CLASSNAME = "SOSVfsXmlExport";
    private static final Logger LOGGER = Logger.getLogger(SOSVfsXmlExport.class);
    private final String rootElementName = "transfer_history";
    private final String exportElementName = "summary";
    private final String exportDetailsElementName = "items";
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SOSFTPOptions objOptions = null;
    private JSXMLFile objXMLFile = null;
    private IJadeTransferHistoryData jadeTransferExportData = null;
    private IJadeTransferDetailHistoryData jadeTransferDetailExportData = null;

    public SOSVfsXmlExport() {
        //
    }

    public SOSVfsXmlExport(final String pstrResourceBundleName) {
        super(pstrResourceBundleName);
    }

    @Override
    public void doTransferDetail() {
        final String conMethodName = CLASSNAME + "::doExportDetail";
        if (isNull(jadeTransferDetailExportData)) {
            throw new JobSchedulerException(SOSVfs_E_261.params(conMethodName));
        }
        open();
        try {
            if (isNotEmpty(exportDetailsElementName)) {
                objXMLFile.newTag(exportDetailsElementName);
            }
            writeItem("transfer_details_id", jadeTransferDetailExportData.getTransferDetailsId());
            writeItem("transfer_id", jadeTransferDetailExportData.getTransferId());
            writeItem("file_size", jadeTransferDetailExportData.getFileSize());
            writeItem("command_type", jadeTransferDetailExportData.getCommandType());
            writeItem("command", jadeTransferDetailExportData.getCommand());
            writeItem("pid", jadeTransferDetailExportData.getPid());
            writeItem("last_error_message", jadeTransferDetailExportData.getLastErrorMessage());
            writeItem("target_filename", jadeTransferDetailExportData.getTargetFilename());
            writeItem("source_filename", jadeTransferDetailExportData.getSourceFilename());
            writeItem("md5", jadeTransferDetailExportData.getMd5());
            writeItem("status", jadeTransferDetailExportData.getStatus());
            writeItem("status_text", jadeTransferDetailExportData.getStatusText());
            writeItem("start_time", jadeTransferDetailExportData.getStartTime());
            writeItem("end_time", jadeTransferDetailExportData.getEndTime());
            writeItem("modified_by", jadeTransferDetailExportData.getModifiedBy());
            writeItem("modified", jadeTransferDetailExportData.getModified());
            writeItem("created_by", jadeTransferDetailExportData.getCreatedBy());
            writeItem("created", jadeTransferDetailExportData.getCreated());
            writeItem("size_value", jadeTransferDetailExportData.getSizeValue());
            if (isNotEmpty(exportDetailsElementName)) {
                objXMLFile.endTag(exportDetailsElementName);
            }
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            throw new JobSchedulerException(SOSVfs_E_260.params(conMethodName), e);
        }
    }

    @Override
    public void doTransferSummary() {
        final String conMethodName = CLASSNAME + "::doExportSummary";
        if (isNull(jadeTransferExportData)) {
            throw new JobSchedulerException(SOSVfs_E_261.params(conMethodName));
        }
        open();
        try {
            if (isNotEmpty(exportElementName)) {
                objXMLFile.newTag(exportElementName);
            }
            writeItem("transfer_id", jadeTransferExportData.getTransferId());
            writeItem("mandator", jadeTransferExportData.getMandator());
            writeItem("file_size", jadeTransferExportData.getFileSize());
            writeItem("source_host", jadeTransferExportData.getSourceHost());
            writeItem("source_host_ip", jadeTransferExportData.getSourceHostIp());
            writeItem("source_user", jadeTransferExportData.getSourceUser());
            writeItem("source_dir", jadeTransferExportData.getSourceDir());
            writeItem("target_host", jadeTransferExportData.getTargetHost());
            writeItem("target_host_ip", jadeTransferExportData.getTargetHostIp());
            writeItem("target_user", jadeTransferExportData.getTargetUser());
            writeItem("target_dir", jadeTransferExportData.getTargetDir());
            writeItem("protocol_type", jadeTransferExportData.getProtocolType());
            writeItem("port", jadeTransferExportData.getPort().toString());
            writeItem("files_count", jadeTransferExportData.getFilesCount());
            writeItem("profile_name", jadeTransferExportData.getProfileName());
            writeItem("profile", jadeTransferExportData.getProfile());
            writeItem("command_type", jadeTransferExportData.getCommandType());
            writeItem("command", jadeTransferExportData.getCommand());
            writeItem("log", jadeTransferExportData.getLog());
            writeItem("status", jadeTransferExportData.getStatus());
            writeItem("status_text", jadeTransferExportData.getStatusText());
            writeItem("start_time", jadeTransferExportData.getStartTime());
            writeItem("end_time", jadeTransferExportData.getEndTime());
            writeItem("last_error_message", jadeTransferExportData.getLastErrorMessage());
            if (isNotEmpty(exportElementName)) {
                objXMLFile.endTag(exportElementName);
            }
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            throw new JobSchedulerException(SOSVfs_E_260.params(conMethodName), e);
        }
    }

    @Override
    public void close() {
        final String conMethodName = CLASSNAME + "::close";
        if (isNotNull(objXMLFile)) {
            try {
                if (objXMLFile.isOpened() == true) {
                    objXMLFile.endTag(rootElementName);
                    objXMLFile.close();
                }
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage());
                throw new JobSchedulerException(SOSVfs_E_260.params(conMethodName), e);
            }
            objXMLFile = null;
        }
    }

    @Override
    public String getFileName() {
        if (isNotNull(objXMLFile)) {
            return objXMLFile.getAbsolutePath();
        } else {
            return null;
        }
    }

    @Override
    public void setData(final SOSFTPOptions pobjOptions) {
        objOptions = pobjOptions;
    }

    @Override
    public void setJadeTransferData(final IJadeTransferHistoryData jadeTransferHistoryExportData) {
        jadeTransferExportData = jadeTransferHistoryExportData;
    }

    @Override
    public void setJadeTransferDetailData(final IJadeTransferDetailHistoryData jadeTransferDetailHistoryExportData) {
        jadeTransferDetailExportData = jadeTransferDetailHistoryExportData;
    }

    private void writeItem(final String name, final String value) throws Exception {
        if (isNotNull(value)) {
            objXMLFile.addAttribute("name", name).addAttribute("value", value).newTag("item", "");
        }
    }

    private void writeItem(final String name, final Integer value) throws Exception {
        if (isNotNull(value)) {
            writeItem(name, value.toString());
        }
    }

    private void writeItem(final String name, final Long value) throws Exception {
        if (isNotNull(value)) {
            writeItem(name, value.toString());
        }
    }

    private void writeItem(final String name, final Date value) throws Exception {
        if (isNotNull(value)) {
            writeItem(name, dateFormatter.format(value));
        }
    }

    private String getXMLFileName() {
        String sectionName = null;
        if (isNotNull(jadeTransferExportData)) {
            sectionName = jadeTransferExportData.getProfile();
        }
        if (isEmpty(sectionName)) {
            sectionName = objOptions.profile.Value().trim();
        }
        if (isNull(sectionName)) {
            sectionName = "";
        }
        if (isNotEmpty(sectionName)) {
            sectionName += "-";
        }
        return objOptions.TempDir() + rootElementName + "-" + sectionName + getDateTimeFormatted("yyyyMMddHHmmssSSSS") + ".xml";
    }

    private void open() {
        final String conMethodName = CLASSNAME + "::open";
        if (objXMLFile == null) {
            try {
                String strXMLFileName = getXMLFileName();
                objXMLFile = new JSXMLFile(strXMLFileName);
                if (objXMLFile.isOpened() == false) {
                    objXMLFile.writeXMLDeclaration().newTag(rootElementName);
                }
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage());
                throw new JobSchedulerException(SOSVfs_E_260.params(conMethodName), e);
            }
        }
    }

}