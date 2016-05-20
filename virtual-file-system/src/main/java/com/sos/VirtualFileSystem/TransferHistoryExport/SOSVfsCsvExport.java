package com.sos.VirtualFileSystem.TransferHistoryExport;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.apache.log4j.Logger;

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
    private static final Logger LOGGER = Logger.getLogger(SOSVfsCsvExport.class);
    private SOSFTPOptions objOptions = null;
    private JSCsvFile objCSVFile = null;
    private final String strCsvFileName = "c:/temp/jade-csv.csv";

    public SOSVfsCsvExport() {
        //
    }

    @Override
    public void doTransferDetail() {
        final String conMethodName = CLASSNAME + "::doExportDetail";
        if (objCSVFile == null) {
            objCSVFile = new JSCsvFile(strCsvFileName);
            LOGGER.debug(SOSVfs_D_259.params(strCsvFileName));
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
            LOGGER.error(e.getLocalizedMessage());
            throw new JobSchedulerException(SOSVfs_E_260.params(conMethodName), e);
        }
    }

    @Override
    public void doTransferSummary() {
        // TO DO Auto-generated method stub
        return;
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
                LOGGER.error(e.getLocalizedMessage());
            }
            objCSVFile = null;
        }
    }

    @Override
    public void setJadeTransferData(final IJadeTransferHistoryData jadeTransferHistoryExportData) {
        // TO DO Auto-generated method stub
    }

    @Override
    public void setJadeTransferDetailData(final IJadeTransferDetailHistoryData jadeTransferDetailHistoryExportData) {
        // TO DO Auto-generated method stub
    }

    @Override
    public String getFileName() {
        // TO DO Auto-generated method stub
        return null;
    }

}