package com.sos.tools.logback;

import ch.qos.logback.classic.db.DBAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version 14.01.14 15:57
 * @uthor stefan.schaedlich@sos-berlin.com
 *
 * This class enables to use ojdbc.jar with the logback DBAppender.
 * see http://jira.qos.ch/browse/LOGBACK-145 for details.
 */
public class OracleDBAppender extends DBAppender {

    private Logger logger = LoggerFactory.getLogger(OracleDBAppender.class);

    @Override
    public void start() {
        super.start();
        cnxSupportsGetGeneratedKeys = false;
        logger.info("DBAppender for Oracle ojdbc6 driver does not support the getGeneratedKeys method - com.sos.tools.logback.OracleDBAppender is used instead.");
    }

}
