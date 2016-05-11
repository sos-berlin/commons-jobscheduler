package com.sos.VirtualFileSystem.TransferHistoryExport;

import org.junit.Before;
import org.junit.Test;

import com.sos.VirtualFileSystem.Options.SOSFTPOptions;

/** @author KB */
public class SOSVfsCsvExportTest {

    private SOSVfsCsvExport objExp = null;
    private SOSFTPOptions objO = null;

    @Before
    public void setUp() throws Exception {
        objExp = new SOSVfsCsvExport();
        objO = new SOSFTPOptions();
    }

    @Test
    public final void testDoExportDetail() {
        objExp.setData(objO);
        objExp.doTransferDetail();
        objExp.close();
    }

    @Test
    public final void testDoExportSummary() {
        objExp.doTransferSummary();
    }

    @Test
    public final void testSetData() {
        objExp.setData(objO);
    }

}