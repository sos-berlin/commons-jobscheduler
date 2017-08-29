package com.sos.VirtualFileSystem.SFTP;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class SOSVfsSFtpJCraftLogger implements com.jcraft.jsch.Logger {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsSFtpJCraftLogger.class);
    private Marker fatal = MarkerFactory.getMarker("FATAL");

    public boolean isEnabled(int level) {
        switch (level) {
        case com.jcraft.jsch.Logger.DEBUG:
            return LOGGER.isDebugEnabled();
        case com.jcraft.jsch.Logger.ERROR:
            return LOGGER.isErrorEnabled();
        case com.jcraft.jsch.Logger.FATAL:
            return LOGGER.isErrorEnabled();
        case com.jcraft.jsch.Logger.INFO:
            return LOGGER.isInfoEnabled();
        case com.jcraft.jsch.Logger.WARN:
            return LOGGER.isWarnEnabled();
        default:
            return false;
        }
    }

    public void log(int level, String message) {
        switch (level) {
        case com.jcraft.jsch.Logger.DEBUG:
            LOGGER.debug(message);
            break;
        case com.jcraft.jsch.Logger.ERROR:
            LOGGER.error(message);
            break;
        case com.jcraft.jsch.Logger.FATAL:
            LOGGER.error(fatal, message);
            break;
        case com.jcraft.jsch.Logger.INFO:
            LOGGER.debug(message);// debug instead of info
            break;
        case com.jcraft.jsch.Logger.WARN:
            LOGGER.warn(message);
            break;
        default:
            break;
        }
    }
}
