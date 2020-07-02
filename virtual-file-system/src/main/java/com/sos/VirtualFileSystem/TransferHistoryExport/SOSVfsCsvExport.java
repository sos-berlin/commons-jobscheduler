package com.sos.VirtualFileSystem.TransferHistoryExport;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSCsvFile;
import com.sos.VirtualFileSystem.Interfaces.IJadeTransferDetailHistoryData;
import com.sos.VirtualFileSystem.Interfaces.IJadeTransferHistoryData;
import com.sos.VirtualFileSystem.Interfaces.ISOSTransferHistory;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;

/** @author KB */
public class SOSVfsCsvExport extends SOSVfsMessageCodes implements ISOSTransferHistory {

    private static final String CLASSNAME = "SOSVfsCsvExport";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsCsvExport.class);
    private static final String CSV_FILE_NAME = "c:/temp/jade-csv.csv";
    private SOSFTPOptions objOptions = null;
    private JSCsvFile objCSVFile = null;

    public SOSVfsCsvExport() {
        //
    }

    @Override
    public void doTransferDetail() {
        final String conMethodName = CLASSNAME + "::doExportDetail";
        if (objCSVFile == null) {
            objCSVFile = new JSCsvFile(CSV_FILE_NAME);
            LOGGER.debug(SOSVfs_D_259.params(CSV_FILE_NAME));
        }
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        String strA[] = pid.split("@");
        pid = strA[0];
        String last_error_message = "";
        try {
            objCSVFile.addCellValue(" ").addCellValue(objOptions.mandator.getValue()).addCellValue(" ").addCellValue(pid).addCellValue("0")
                    .addCellValue(objOptions.operation.getValue()).addCellValue(objOptions.getSource().host.getValue())
                    .addCellValue(objOptions.getSource().host.getHostAdress()).addCellValue(objOptions.getSource().user.getValue())
                    .addCellValue(objOptions.getTarget().host.getValue()).addCellValue(objOptions.getTarget().host.getHostAdress())
                    .addCellValue(objOptions.getTarget().user.getValue()).addCellValue(objOptions.protocol.getValue()).addCellValue(objOptions.port.getValue())
                    .addCellValue(objOptions.sourceDir.getValue()).addCellValue(objOptions.targetDir.getValue()).addCellValue(" ")
                    .addCellValue(" ").addCellValue(" ").addCellValue(" ").addCellValue(" ").addCellValue(last_error_message)
                    .addCellValue(objOptions.logFilename.getValue()).addCellValue(" ").addCellValue(" ").addCellValue(" ").addCellValue(" ").addCellValue(" ")
                    .newLine();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new JobSchedulerException(SOSVfs_E_260.params(conMethodName), e);
        }
    }

    @Override
    public void doTransferSummary() {
        //
    }

    @Override
    public void setData(final SOSFTPOptions pobjTransferHistoryData) {
        objOptions = pobjTransferHistoryData;
    }

    @Override
    public void close() {
        if (objCSVFile != null) {
            try {
                objCSVFile.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
            objCSVFile = null;
        }
    }

    @Override
    public void setJadeTransferData(final IJadeTransferHistoryData jadeTransferHistoryExportData) {
        //
    }

    @Override
    public void setJadeTransferDetailData(final IJadeTransferDetailHistoryData jadeTransferDetailHistoryExportData) {
        //
    }

    @Override
    public String getFileName() {
        return null;
    }

}