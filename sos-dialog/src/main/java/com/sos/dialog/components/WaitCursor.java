/**
 * 
 */
package com.sos.dialog.components;

public class WaitCursor implements AutoCloseable {

    SOSCursor objC = null;

    public WaitCursor() {
        objC = new SOSCursor();
        objC.showWait();
    }

    @Override
    public void close() throws Exception {
        if (objC != null) {
            objC.close();
        }
    }

}
