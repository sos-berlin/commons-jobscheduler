/** I18N Messages and Logging Copyright (C) 2006 John J. Mazzitelli
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA */
package com.sos.i18n;

import com.sos.i18n.logging.commons.CommonsLogger;
import com.sos.i18n.logging.jdk.JDKLogger;
import com.sos.i18n.logging.log4j.Log4jLogger;

import java.util.Locale;

/** Factory used to create the I18N {@link Logger} objects. Note that if you pass
 * to a method a <code>null</code> value for a logger's locale (or you call a
 * method where Locale is not specified as an argument), then the default locale
 * to be used will be determined by the {@link LoggerLocale} object.
 *
 * <p>
 * To tell this factory what types of loggers to create, set the system property
 * <i>i18nlog.logger-type</i> to one of the following:
 * </p>
 *
 * <ul>
 * <li><b>log4j</b></li>
 * <li><b>commons</b></li>
 * <li><b>jdk</b></li>
 * </ul>
 *
 * <p>
 * <b>log4j</b> is to indicate that Apache Log4J is to be used directly as the
 * underlying logging framework; <b> commons</b> is to indicate the use of
 * Apache Commons Logging and <b>jdk</b> is to indicate that the JDK Logging API
 * is to be used.
 * </p>
 *
 * <p>
 * If this system property is not set, Apache Log4J will be used directly if it
 * is found in this class's class loader. If that is not found, Apache Commons
 * Logging will be used if it is found in this class's class loader. Otherwise,
 * JDK Logging will be used.
 * </p>
 *
 * <p>
 * You can programatically set the logger type this factory will use by calling
 * {@link #resetLoggerType(com.sos.i18n.LoggerFactory.LoggerType)}.
 * </p>
 *
 * @author <a href="mailto:jmazzitelli@users.sourceforge.net">John
 *         Mazzitelli</a>
 * @version $Revision: 1.3 $
 * @see Logger
 * @see LoggerLocale */
public class LoggerFactory {

    /** The system property whose value determines the type of loggers this
     * factory creates. */
    public static final String SYSPROP_LOGGER_TYPE = "i18nlog.logger-type";

    /** Enum of the different types of loggers. */
    public static enum LoggerType {
        /** Apache Log4J Logging */
        LOG4J,
        /** Apache Commons Logging */
        COMMONS,
        /** JDK Logging */
        JDK
    }

    /** This static enum indicates what kind of loggers this factory will create. */
    private static LoggerType LOGGER_TYPE = resetLoggerType(null);

    /** Prevents instantiation. */
    private LoggerFactory() {
    }

    /** Creates a new {@link Logger} object.
     *
     * @param name the name to give to the logger (this is usually a class name
     *            of the object that will be logging messages)
     * @param basename the base bundle name used to identify the resource bundle
     *            to use (may be <code>null</code>)
     * @param locale the locale of the messages that will be logged (may be
     *            <code>null</code>)
     *
     * @return the new logger that was created
     *
     * @see Msg#Msg(Msg.BundleBaseName, Locale) */
    public static Logger getLogger(String name, Msg.BundleBaseName basename, Locale locale) {
        switch (LOGGER_TYPE) {
        case LOG4J: {
            return new Log4jLogger(name, basename, locale);
        }

        case COMMONS: {
            return new CommonsLogger(name, basename, locale);
        }

        case JDK:
        default: {
            return new JDKLogger(name, basename, locale);
        }
        }
    }

    /** Creates a new {@link Logger} object.
     *
     * @param clazz the class of the object that will be logging messages
     * @param basename the base bundle name used to identify the resource bundle
     *            to use
     * @param locale the locale of the messages that will be logged (used to
     *            further identify the resource bundle)
     *
     * @return the new logger that was created
     *
     * @see Msg#Msg(Msg.BundleBaseName, Locale) */
    public static Logger getLogger(Class clazz, Msg.BundleBaseName basename, Locale locale) {
        switch (LOGGER_TYPE) {
        case LOG4J: {
            return new Log4jLogger(clazz, basename, locale);
        }

        case COMMONS: {
            return new CommonsLogger(clazz, basename, locale);
        }

        case JDK:
        default: {
            return new JDKLogger(clazz, basename, locale);
        }
        }
    }

    /** Creates a new {@link Logger} object using the JVM's default locale.
     *
     * @param name the name to give to the logger (this is usually a class name
     *            of the object that will be logging messages)
     * @param basename the base bundle name used to identify the resource bundle
     *            to use
     *
     * @return the new logger that was created
     *
     * @see Msg#Msg(Msg.BundleBaseName) */
    public static Logger getLogger(String name, Msg.BundleBaseName basename) {
        switch (LOGGER_TYPE) {
        case LOG4J: {
            return new Log4jLogger(name, basename);
        }

        case COMMONS: {
            return new CommonsLogger(name, basename);
        }

        case JDK:
        default: {
            return new JDKLogger(name, basename);
        }
        }
    }

    /** Creates a new {@link Logger} object using the JVM's default locale.
     *
     * @param clazz the class of the object that will be logging messages
     * @param basename the base bundle name used to identify the resource bundle
     *            to use
     *
     * @return the new logger that was created
     *
     * @see Msg#Msg(Msg.BundleBaseName) */
    public static Logger getLogger(Class clazz, Msg.BundleBaseName basename) {
        switch (LOGGER_TYPE) {
        case LOG4J: {
            return new Log4jLogger(clazz, basename);
        }

        case COMMONS: {
            return new CommonsLogger(clazz, basename);
        }

        case JDK:
        default: {
            return new JDKLogger(clazz, basename);
        }
        }
    }

    /** Creates a new {@link Logger} object using a default resource bundle.
     *
     * @param name the name to give to the logger (this is usually a class name
     *            of the object that will be logging messages)
     * @param locale the locale of the messages that will be logged (used to
     *            further identify the resource bundle)
     *
     * @return the new logger that was created
     *
     * @see Msg#Msg(Locale) */
    public static Logger getLogger(String name, Locale locale) {
        switch (LOGGER_TYPE) {
        case LOG4J: {
            return new Log4jLogger(name, locale);
        }

        case COMMONS: {
            return new CommonsLogger(name, locale);
        }

        case JDK:
        default: {
            return new JDKLogger(name, locale);
        }
        }
    }

    /** Creates a new {@link Logger} object using a default resource bundle.
     *
     * @param clazz the class of the object that will be logging messages
     * @param locale the locale of the messages that will be logged (used to
     *            further identify the resource bundle)
     *
     * @return the new logger that was created
     *
     * @see Msg#Msg(Locale) */
    public static Logger getLogger(Class clazz, Locale locale) {
        switch (LOGGER_TYPE) {
        case LOG4J: {
            return new Log4jLogger(clazz, locale);
        }

        case COMMONS: {
            return new CommonsLogger(clazz, locale);
        }

        case JDK:
        default: {
            return new JDKLogger(clazz, locale);
        }
        }
    }

    /** Creates a new {@link Logger} object using a default resource bundle and
     * the JVM's default locale.
     *
     * @param name the name to give to the logger (this is usually a class name
     *            of the object that will be logging messages)
     *
     * @return the new logger that was created
     *
     * @see Msg#Msg() */
    public static Logger getLogger(String name) {
        switch (LOGGER_TYPE) {
        case LOG4J: {
            return new Log4jLogger(name);
        }

        case COMMONS: {
            return new CommonsLogger(name);
        }

        case JDK:
        default: {
            return new JDKLogger(name);
        }
        }
    }

    /** Creates a new {@link Logger} object using a default resource bundle and
     * the JVM's default locale.
     *
     * @param clazz the class of the object that will be logging messages
     *
     * @return the new logger that was created
     *
     * @see Msg#Msg() */
    public static Logger getLogger(Class clazz) {
        switch (LOGGER_TYPE) {
        case LOG4J: {
            return new Log4jLogger(clazz);
        }

        case COMMONS: {
            return new CommonsLogger(clazz);
        }

        case JDK:
        default: {
            return new JDKLogger(clazz);
        }
        }
    }

    /** Resets the logger type that this factory will use. If <code>type</code>
     * is <code>null</code>, the system property {@link #SYSPROP_LOGGER_TYPE}
     * will be examined to determine what logger type to use (and if that isn't
     * set, JDK logging will be used unless Apache Log4J or Apache Commons
     * logging is found in the class's classloader, checked in that order).
     *
     * @param type the new logger type that will be used when creating loggers;
     *            if <code>null</code> will use heuristics to guess what logger
     *            type to use
     *
     * @return the logger type that is to be used (useful if <code>null</code>
     *         was passed in and the caller wants to know what will be used) */
    public static LoggerType resetLoggerType(LoggerType type) {
        if (type == null) {
            String property = System.getProperty(SYSPROP_LOGGER_TYPE);

            if (property != null) {
                if (LoggerType.LOG4J.toString().equalsIgnoreCase(property)) {
                    type = LoggerType.LOG4J;
                } else if (LoggerType.COMMONS.toString().equalsIgnoreCase(property)) {
                    type = LoggerType.COMMONS;
                } else if (LoggerType.JDK.toString().equalsIgnoreCase(property)) {
                    type = LoggerType.JDK;
                } else {
                    System.err.println(SYSPROP_LOGGER_TYPE + " != [log4j | commons | jdk ]");
                }
            }

            // if the logger type wasn't explicitly defined or was incorrectly
            // defined, then let's guess what to use
            // first see if we can load in Log4J; if not, try Commons; if
            // neither then use JDK logging
            if (type == null) {
                try {
                    Class.forName("org.apache.log4j.Logger", false, LoggerFactory.class.getClassLoader());
                    type = LoggerType.LOG4J;
                } catch (Exception e1) {
                    try {
                        Class.forName("org.apache.commons.logging.Log", false, LoggerFactory.class.getClassLoader());
                        type = LoggerType.COMMONS;
                    } catch (Exception e2) {
                        type = LoggerType.JDK;
                    }
                }
            }
        }

        LOGGER_TYPE = type;

        return LOGGER_TYPE;
    }
}