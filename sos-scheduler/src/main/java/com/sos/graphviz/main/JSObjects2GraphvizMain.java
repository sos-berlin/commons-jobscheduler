package com.sos.graphviz.main;

import com.sos.JSHelper.Basics.JSToolBox;
import org.apache.log4j.Logger;

public class JSObjects2GraphvizMain extends JSToolBox {

    protected JSObjects2GraphvizOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JSObjects2GraphvizMain.class);

    public final static void main(final String[] pstrArgs) {
        final String conMethodName = "JSObjects2GraphvizMain::Main";
        LOGGER.info("JSObjects2Graphviz - Main");
        try {
            JSObjects2Graphviz jsObjects2Graphviz = new JSObjects2Graphviz();
            JSObjects2GraphvizOptions jsObjects2GraphvizOptions = jsObjects2Graphviz.getOptions();
            jsObjects2GraphvizOptions.commandLineArgs(pstrArgs);
            jsObjects2Graphviz.execute();
        } catch (Exception e) {
            LOGGER.error(conMethodName + ": " + "Error occured ..." + e.getMessage(), e);
            int intExitCode = 99;
            LOGGER.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", conMethodName, intExitCode), e);
            System.exit(intExitCode);
        }
        LOGGER.info(String.format("JSJ-I-106: %1$s - ended without errors", conMethodName));
        System.exit(0);
    }

}
