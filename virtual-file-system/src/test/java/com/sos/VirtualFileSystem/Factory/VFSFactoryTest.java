package com.sos.VirtualFileSystem.Factory;

import org.apache.log4j.Logger;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;

/** @author KB */
public class VFSFactoryTest {

    private static final Logger LOGGER = Logger.getLogger(VFSFactoryTest.class);

    public VFSFactoryTest() {
        //
    }

    @Test
    public void testVFSFactory() {
        ISOSVFSHandler objVFS = null;
        try {
            objVFS = VFSFactory.getHandler("ftp://kb:kb@wilma.sos:21");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertNotNull(objVFS);
    }

}
