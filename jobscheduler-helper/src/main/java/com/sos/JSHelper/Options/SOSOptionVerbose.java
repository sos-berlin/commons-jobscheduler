package com.sos.JSHelper.Options;

import java.util.Enumeration;

import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.layout.PatternLayout;

/** @author KB */
public class SOSOptionVerbose extends SOSOptionInteger {

    private static final long serialVersionUID = -5484261268617623809L;
    private static final Logger LOGGER = LogManager.getLogger(SOSOptionVerbose.class);

    public SOSOptionVerbose(JSOptionsClass pPobjParent, String pPstrKey, String pPstrDescription, String pPstrValue, String pPstrDefaultValue,
            boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
    }

//    public void initializeLog4jLevels() {
//        PatternLayout layout = new PatternLayout("[%-5p] %d{ABSOLUTE} (%F:%L) - %m%n");
//        int intVerbose = this.value();
//        switch (intVerbose) {
//        case -1:
//            setLoggerLevel(Level.ERROR);
//            LOGGER.error("set loglevel to ERROR due to option verbose = " + intVerbose);
//            break;
//        case 0:
//        case 1:
//            setLoggerLevel(Level.INFO);
//            layout = new PatternLayout("[%-p] %d{ABSOLUTE} - %m%n");
//            LOGGER.info("set loglevel to INFO due to option verbose = " + intVerbose);
//            break;
//        case 9:
//            setLoggerLevel(Level.TRACE);
//            LOGGER.trace("set loglevel to TRACE due to option verbose = " + intVerbose);
//            break;
//        default:
//            setLoggerLevel(Level.DEBUG);
//            LOGGER.debug("set loglevel to DEBUG due to option verbose = " + intVerbose);
//            break;
//        }
//        for (Enumeration appenders = LogManager.getRootLogger().getAllAppenders(); appenders.hasMoreElements();) {
//            Appender appender = (Appender) appenders.nextElement();
//            appender.setLayout(layout);
//        }
//    }

//    private void setLoggerLevel(final Level pintLevel) {
//        LogManager.getRootLogger().setLevel(pintLevel);
//        LOGGER.setLevel(pintLevel);
//    }

    @Override
    public void setValue(final String pstrValue) {
        super.setValue(pstrValue);
    }

}