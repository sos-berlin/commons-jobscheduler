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
            objCSVFile.AddCellValue(" ").AddCellValue(objOptions.mandator.Value()).AddCellValue(" ").AddCellValue(pid).AddCellValue("0")
                    .AddCellValue(objOptions.operation.Value()).AddCellValue(objOptions.Source().host.Value())
                    .AddCellValue(objOptions.Source().host.getHostAdress()).AddCellValue(objOptions.Source().user.Value())
                    .AddCellValue(objOptions.Target().host.Value()).AddCellValue(objOptions.Target().host.getHostAdress())
                    .AddCellValue(objOptions.Target().user.Value()).AddCellValue(objOptions.protocol.Value()).AddCellValue(objOptions.port.Value())
                    .AddCellValue(objOptions.sourceDir.Value()).AddCellValue(objOptions.targetDir.Value()).AddCellValue(" ")
                    .AddCellValue(" ").AddCellValue(" ").AddCellValue(" ").AddCellValue(" ").AddCellValue(last_error_message)
                    .AddCellValue(objOptions.log_filename.Value()).AddCellValue(" ").AddCellValue(" ").AddCellValue(" ").AddCellValue(" ").AddCellValue(" ")
                    .NewLine();
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