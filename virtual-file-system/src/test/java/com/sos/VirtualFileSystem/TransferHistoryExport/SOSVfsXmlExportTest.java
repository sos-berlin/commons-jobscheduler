package com.sos.VirtualFileSystem.TransferHistoryExport;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;

/** @author oh */
public class SOSVfsXmlExportTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsXmlExportTest.class);
    private SOSVfsXmlExport objExp = null;
    private SOSFTPOptions objO = null;
    private JadeTransferDetailHistoryExportDataTest objJadeDetail = null;
    private JadeTransferHistoryExportDataTest objJade = null;
    private JSFile objF = null;

    @Before
    public void setUp() throws Exception {
        objExp = new SOSVfsXmlExport();
        objO = new SOSFTPOptions();
        objJadeDetail = new JadeTransferDetailHistoryExportDataTest();
        objJade = new JadeTransferHistoryExportDataTest();
    }

    public void close() {
        objF = new JSFile(objExp.getFileName());
        objExp.close();
        LOGGER.debug(objF.getContent());
    }

    @Test
    public final void testDoExportDetail() throws Exception {
        objExp.setData(objO);
        objExp.setJadeTransferData(objJade);
        objExp.doTransferSummary();
        for (int i = 0; i < 2; i++) {
            objExp.setJadeTransferDetailData(objJadeDetail);
            objExp.doTransferDetail();
        }
        close();
    }

    @Test
    public final void testDoExportSummary() {
        objExp.setData(objO);
        objExp.setJadeTransferData(objJade);
        objExp.doTransferSummary();
        close();
    }

}