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
package com.sos.i18n.logging.log4j;

import com.sos.i18n.Logger;
import com.sos.i18n.Msg;
import com.sos.i18n.Msg.BundleBaseName;
import com.sos.i18n.exception.LocalizedException;
import com.sos.i18n.exception.LocalizedRuntimeException;
import java.util.Locale;

/** <p>
 * This class provides alot of static methods to assist in logging localized
 * messages via a Log4J logging log object.
 * </p>
 *
 * <p>
 * There is a method to the madness in the parameter ordering. Anytime you want
 * to specify a localized message, you always specify the base bundle name
 * first, followed by the locale, the bundle key and the variable list of
 * arguments that go with the keyed message (all in that order). Bundle name and
 * locale are both optional. This is consistent with the way localized messages
 * are specified in constructors for {@link LocalizedException} and
 * {@link LocalizedRuntimeException}. When you need to specify a
 * <code>Throwable</code> with your localized message, it is specified before
 * those parameters. Again, this is consistent both in this class and the
 * localized exception classes (see
 * {@link Log4jLogMsg#fatal(org.apache.logging.log4j.Logger, Throwable, Msg.BundleBaseName, Locale, String, Object[])}
 * and
 * {@link LocalizedException#LocalizedException(Throwable, Msg.BundleBaseName, Locale, String, Object[])}
 * as examples).
 * </p>
 *
 * <p>
 * These static utility methods respect the settings defined by
 * {@link Logger#setDumpStackTraces(boolean)} and
 * {@link Logger#setDumpLogKeys(boolean)}.
 * </p>
 *
 * @author <a href="mailto:jmazzitelli@users.sourceforge.net">John
 *         Mazzitelli</a>
 * @version $Revision: 1.1 $
 * @see Msg
 * @see LocalizedException
 * @see LocalizedRuntimeException */
public class Log4jLogMsg {

    /** Prevents instantiation. */
    private Log4jLogMsg() {
    }

    /** Logs the given message to the log at the fatal level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param basename the base name of the resource bundle
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned
     *
     * @see Msg#createMsg(com.sos.i18n.Msg.BundleBaseName, Locale, String,
     *      Object[]) */
    public static Msg fatal(org.apache.logging.log4j.Logger log, BundleBaseName basename, Locale locale, String key, Object... varargs) {
        Msg msg = Msg.createMsg(basename, locale, key, varargs);
        log.fatal((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
        return msg;
    }

    /** Logs the given message to the log at the fatal level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param basename the base name of the resource bundle
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned
     *
     * @see Msg#createMsg(com.sos.i18n.Msg.BundleBaseName, Locale, String,
     *      Object[]) */
    public static Msg fatal(org.apache.logging.log4j.Logger log, Throwable throwable, BundleBaseName basename, Locale locale, String key, Object... varargs) {
        Msg msg = Msg.createMsg(basename, locale, key, varargs);
        logFatalWithThrowable(log, key, msg, throwable);
        return msg;
    }

    /** Logs the given message to the log at the fatal level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg fatal(org.apache.logging.log4j.Logger log, Locale locale, String key, Object... varargs) {
        Msg msg = Msg.createMsg(locale, key, varargs);
        log.fatal((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
        return msg;
    }

    /** Logs the given message to the log at the fatal level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg fatal(org.apache.logging.log4j.Logger log, Throwable throwable, Locale locale, String key, Object... varargs) {
        Msg msg = Msg.createMsg(locale, key, varargs);
        logFatalWithThrowable(log, key, msg, throwable);
        return msg;
    }

    /** Logs the given message to the log at the fatal level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param basename the base name of the resource bundle
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg fatal(org.apache.logging.log4j.Logger log, BundleBaseName basename, String key, Object... varargs) {
        Msg msg = Msg.createMsg(basename, key, varargs);
        log.fatal((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
        return msg;
    }

    /** Logs the given message to the log at the fatal level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param basename the base name of the resource bundle
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg fatal(org.apache.logging.log4j.Logger log, Throwable throwable, BundleBaseName basename, String key, Object... varargs) {
        Msg msg = Msg.createMsg(basename, key, varargs);
        logFatalWithThrowable(log, key, msg, throwable);
        return msg;
    }

    /** Logs the given message to the log at the fatal level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg fatal(org.apache.logging.log4j.Logger log, String key, Object... varargs) {
        Msg msg = Msg.createMsg(key, varargs);
        log.fatal((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
        return msg;
    }

    /** Logs the given message to the log at the fatal level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg fatal(org.apache.logging.log4j.Logger log, Throwable throwable, String key, Object... varargs) {
        Msg msg = Msg.createMsg(key, varargs);
        logFatalWithThrowable(log, key, msg, throwable);
        return msg;
    }

    /** Logs the given message to the log at the error level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param basename the base name of the resource bundle
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg error(org.apache.logging.log4j.Logger log, BundleBaseName basename, Locale locale, String key, Object... varargs) {
        Msg msg = Msg.createMsg(basename, locale, key, varargs);
        log.error((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
        return msg;
    }

    /** Logs the given message to the log at the error level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param basename the base name of the resource bundle
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg error(org.apache.logging.log4j.Logger log, Throwable throwable, BundleBaseName basename, Locale locale, String key, Object... varargs) {
        Msg msg = Msg.createMsg(basename, locale, key, varargs);
        logErrorWithThrowable(log, key, msg, throwable);
        return msg;
    }

    /** Logs the given message to the log at the error level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg error(org.apache.logging.log4j.Logger log, Locale locale, String key, Object... varargs) {
        Msg msg = Msg.createMsg(locale, key, varargs);
        log.error((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
        return msg;
    }

    /** Logs the given message to the log at the error level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg error(org.apache.logging.log4j.Logger log, Throwable throwable, Locale locale, String key, Object... varargs) {
        Msg msg = Msg.createMsg(locale, key, varargs);
        logErrorWithThrowable(log, key, msg, throwable);
        return msg;
    }

    /** Logs the given message to the log at the error level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param basename the base name of the resource bundle
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg error(org.apache.logging.log4j.Logger log, BundleBaseName basename, String key, Object... varargs) {
        Msg msg = Msg.createMsg(basename, key, varargs);
        log.error((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
        return msg;
    }

    /** Logs the given message to the log at the error level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param basename the base name of the resource bundle
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg error(org.apache.logging.log4j.Logger log, Throwable throwable, BundleBaseName basename, String key, Object... varargs) {
        Msg msg = Msg.createMsg(basename, key, varargs);
        logErrorWithThrowable(log, key, msg, throwable);
        return msg;
    }

    /** Logs the given message to the log at the error level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg error(org.apache.logging.log4j.Logger log, String key, Object... varargs) {
        Msg msg = Msg.createMsg(key, varargs);
        log.error((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
        return msg;
    }

    /** Logs the given message to the log at the error level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg error(org.apache.logging.log4j.Logger log, Throwable throwable, String key, Object... varargs) {
        Msg msg = Msg.createMsg(key, varargs);
        logErrorWithThrowable(log, key, msg, throwable);
        return msg;
    }

    /** Logs the given message to the log at the warn level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param basename the base name of the resource bundle
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg warn(org.apache.logging.log4j.Logger log, BundleBaseName basename, Locale locale, String key, Object... varargs) {
        Msg msg = Msg.createMsg(basename, locale, key, varargs);
        log.warn((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
        return msg;
    }

    /** Logs the given message to the log at the warn level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param basename the base name of the resource bundle
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg warn(org.apache.logging.log4j.Logger log, Throwable throwable, BundleBaseName basename, Locale locale, String key, Object... varargs) {
        Msg msg = Msg.createMsg(basename, locale, key, varargs);
        logWarnWithThrowable(log, key, msg, throwable);
        return msg;
    }

    /** Logs the given message to the log at the warn level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg warn(org.apache.logging.log4j.Logger log, Locale locale, String key, Object... varargs) {
        Msg msg = Msg.createMsg(locale, key, varargs);
        log.warn((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
        return msg;
    }

    /** Logs the given message to the log at the warn level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg warn(org.apache.logging.log4j.Logger log, Throwable throwable, Locale locale, String key, Object... varargs) {
        Msg msg = Msg.createMsg(locale, key, varargs);
        logWarnWithThrowable(log, key, msg, throwable);
        return msg;
    }

    /** Logs the given message to the log at the warn level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param basename the base name of the resource bundle
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg warn(org.apache.logging.log4j.Logger log, BundleBaseName basename, String key, Object... varargs) {
        Msg msg = Msg.createMsg(basename, key, varargs);
        log.warn((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
        return msg;
    }

    /** Logs the given message to the log at the warn level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param basename the base name of the resource bundle
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg warn(org.apache.logging.log4j.Logger log, Throwable throwable, BundleBaseName basename, String key, Object... varargs) {
        Msg msg = Msg.createMsg(basename, key, varargs);
        logWarnWithThrowable(log, key, msg, throwable);
        return msg;
    }

    /** Logs the given message to the log at the warn level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg warn(org.apache.logging.log4j.Logger log, String key, Object... varargs) {
        Msg msg = Msg.createMsg(key, varargs);
        log.warn((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
        return msg;
    }

    /** Logs the given message to the log at the warn level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg warn(org.apache.logging.log4j.Logger log, Throwable throwable, String key, Object... varargs) {
        Msg msg = Msg.createMsg(key, varargs);
        logWarnWithThrowable(log, key, msg, throwable);
        return msg;
    }

    /** Logs the given message to the log at the info level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param basename the base name of the resource bundle
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg info(org.apache.logging.log4j.Logger log, BundleBaseName basename, Locale locale, String key, Object... varargs) {
        if (log.isInfoEnabled()) {
            Msg msg = Msg.createMsg(basename, locale, key, varargs);
            log.info((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the info level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param basename the base name of the resource bundle
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg info(org.apache.logging.log4j.Logger log, Throwable throwable, BundleBaseName basename, Locale locale, String key, Object... varargs) {
        if (log.isInfoEnabled()) {
            Msg msg = Msg.createMsg(basename, locale, key, varargs);
            logInfoWithThrowable(log, key, msg, throwable);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the info level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg info(org.apache.logging.log4j.Logger log, Locale locale, String key, Object... varargs) {
        if (log.isInfoEnabled()) {
            Msg msg = Msg.createMsg(locale, key, varargs);
            log.info((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the info level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg info(org.apache.logging.log4j.Logger log, Throwable throwable, Locale locale, String key, Object... varargs) {
        if (log.isInfoEnabled()) {
            Msg msg = Msg.createMsg(locale, key, varargs);
            logInfoWithThrowable(log, key, msg, throwable);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the info level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param basename the base name of the resource bundle
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg info(org.apache.logging.log4j.Logger log, BundleBaseName basename, String key, Object... varargs) {
        if (log.isInfoEnabled()) {
            Msg msg = Msg.createMsg(basename, key, varargs);
            log.info((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the info level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param basename the base name of the resource bundle
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg info(org.apache.logging.log4j.Logger log, Throwable throwable, BundleBaseName basename, String key, Object... varargs) {
        if (log.isInfoEnabled()) {
            Msg msg = Msg.createMsg(basename, key, varargs);
            logInfoWithThrowable(log, key, msg, throwable);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the info level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg info(org.apache.logging.log4j.Logger log, String key, Object... varargs) {
        if (log.isInfoEnabled()) {
            Msg msg = Msg.createMsg(key, varargs);
            log.info((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the info level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg info(org.apache.logging.log4j.Logger log, Throwable throwable, String key, Object... varargs) {
        if (log.isInfoEnabled()) {
            Msg msg = Msg.createMsg(key, varargs);
            logInfoWithThrowable(log, key, msg, throwable);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the debug level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param basename the base name of the resource bundle
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg debug(org.apache.logging.log4j.Logger log, BundleBaseName basename, Locale locale, String key, Object... varargs) {
        if (log.isDebugEnabled()) {
            Msg msg = Msg.createMsg(basename, locale, key, varargs);
            log.debug((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the debug level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param basename the base name of the resource bundle
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg debug(org.apache.logging.log4j.Logger log, Throwable throwable, BundleBaseName basename, Locale locale, String key, Object... varargs) {
        if (log.isDebugEnabled()) {
            Msg msg = Msg.createMsg(basename, locale, key, varargs);
            logDebugWithThrowable(log, key, msg, throwable);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the debug level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg debug(org.apache.logging.log4j.Logger log, Locale locale, String key, Object... varargs) {
        if (log.isDebugEnabled()) {
            Msg msg = Msg.createMsg(locale, key, varargs);
            log.debug((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the debug level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg debug(org.apache.logging.log4j.Logger log, Throwable throwable, Locale locale, String key, Object... varargs) {
        if (log.isDebugEnabled()) {
            Msg msg = Msg.createMsg(locale, key, varargs);
            logDebugWithThrowable(log, key, msg, throwable);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the debug level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param basename the base name of the resource bundle
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg debug(org.apache.logging.log4j.Logger log, BundleBaseName basename, String key, Object... varargs) {
        if (log.isDebugEnabled()) {
            Msg msg = Msg.createMsg(basename, key, varargs);
            log.debug((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the debug level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param basename the base name of the resource bundle
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg debug(org.apache.logging.log4j.Logger log, Throwable throwable, BundleBaseName basename, String key, Object... varargs) {
        if (log.isDebugEnabled()) {
            Msg msg = Msg.createMsg(basename, key, varargs);
            logDebugWithThrowable(log, key, msg, throwable);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the debug level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg debug(org.apache.logging.log4j.Logger log, String key, Object... varargs) {
        if (log.isDebugEnabled()) {
            Msg msg = Msg.createMsg(key, varargs);
            log.debug((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the debug level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg debug(org.apache.logging.log4j.Logger log, Throwable throwable, String key, Object... varargs) {
        if (log.isDebugEnabled()) {
            Msg msg = Msg.createMsg(key, varargs);
            logDebugWithThrowable(log, key, msg, throwable);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the trace level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param basename the base name of the resource bundle
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg trace(org.apache.logging.log4j.Logger log, BundleBaseName basename, Locale locale, String key, Object... varargs) {
        if (log.isTraceEnabled()) {
            Msg msg = Msg.createMsg(basename, locale, key, varargs);
            log.trace((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the trace level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param basename the base name of the resource bundle
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg trace(org.apache.logging.log4j.Logger log, Throwable throwable, BundleBaseName basename, Locale locale, String key, Object... varargs) {
        if (log.isTraceEnabled()) {
            Msg msg = Msg.createMsg(basename, locale, key, varargs);
            logTraceWithThrowable(log, key, msg, throwable);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the trace level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg trace(org.apache.logging.log4j.Logger log, Locale locale, String key, Object... varargs) {
        if (log.isTraceEnabled()) {
            Msg msg = Msg.createMsg(locale, key, varargs);
            log.trace((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the trace level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg trace(org.apache.logging.log4j.Logger log, Throwable throwable, Locale locale, String key, Object... varargs) {
        if (log.isTraceEnabled()) {
            Msg msg = Msg.createMsg(locale, key, varargs);
            logTraceWithThrowable(log, key, msg, throwable);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the trace level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param basename the base name of the resource bundle
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg trace(org.apache.logging.log4j.Logger log, BundleBaseName basename, String key, Object... varargs) {
        if (log.isTraceEnabled()) {
            Msg msg = Msg.createMsg(basename, key, varargs);
            log.trace((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the trace level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param basename the base name of the resource bundle
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg trace(org.apache.logging.log4j.Logger log, Throwable throwable, BundleBaseName basename, String key, Object... varargs) {
        if (log.isTraceEnabled()) {
            Msg msg = Msg.createMsg(basename, key, varargs);
            logTraceWithThrowable(log, key, msg, throwable);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the trace level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * @param log the log where the messages will go
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg trace(org.apache.logging.log4j.Logger log, String key, Object... varargs) {
        if (log.isTraceEnabled()) {
            Msg msg = Msg.createMsg(key, varargs);
            log.trace((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg);
            return msg;
        }

        return null;
    }

    /** Logs the given message to the log at the trace level. If the log level is
     * not enabled, this method does nothing and returns <code>null</code>. If a
     * message was logged, its {@link Msg} will be returned.
     *
     * <p>
     * The given Throwable will be passed to the logger so its stack can be
     * dumped when appropriate.
     * </p>
     *
     * @param log the log where the messages will go
     * @param throwable the throwable associated with the log message
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg trace(org.apache.logging.log4j.Logger log, Throwable throwable, String key, Object... varargs) {
        if (log.isTraceEnabled()) {
            Msg msg = Msg.createMsg(key, varargs);
            logTraceWithThrowable(log, key, msg, throwable);
            return msg;
        }

        return null;
    }

    /** Logs the message, along with the given <code>throwable</code>, at the
     * fatal level. If {@link Logger#getDumpStackTraces() stack traces are not
     * to be dumped}, it doesn't matter. FATAL log level messages should be very
     * rare and normally indicate a very bad condition. In this case, we should
     * always dump the stack trace no matter how we are configured.
     *
     * @param log where to log the message
     * @param key the resource key that is associated with the message
     * @param msg the message to log
     * @param throwable the throwable associated with the message */
    private static void logFatalWithThrowable(org.apache.logging.log4j.Logger log, String key, Msg msg, Throwable throwable) {
        log.fatal(((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg), throwable);
    }

    /** Logs the message, along with the given <code>throwable</code>, at the
     * error level. If {@link Logger#getDumpStackTraces() stack traces are not
     * to be dumped}, the logged message will be appended with the throwable's
     * <code>Throwable.toString()</code> contents.
     *
     * @param log where to log the message
     * @param key the resource key that is associated with the message
     * @param msg the message to log
     * @param throwable the throwable associated with the message */
    private static void logErrorWithThrowable(org.apache.logging.log4j.Logger log, String key, Msg msg, Throwable throwable) {
        if (Logger.getDumpStackTraces()) {
            log.error(((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg), throwable);
        } else {
            log.error(((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg) + ". Cause: " + throwable.toString());
        }
    }

    /** Logs the message, along with the given <code>throwable</code>, at the
     * warn level. If {@link Logger#getDumpStackTraces() stack traces are not to
     * be dumped}, the logged message will be appended with the throwable's
     * <code>Throwable.toString()</code> contents.
     *
     * @param log where to log the message
     * @param key the resource key that is associated with the message
     * @param msg the message to log
     * @param throwable the throwable associated with the message */
    private static void logWarnWithThrowable(org.apache.logging.log4j.Logger log, String key, Msg msg, Throwable throwable) {
        if (Logger.getDumpStackTraces()) {
            log.warn(((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg), throwable);
        } else {
            log.warn(((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg) + ". Cause: " + throwable.toString());
        }
    }

    /** Logs the message, along with the given <code>throwable</code>, at the
     * info level. If {@link Logger#getDumpStackTraces() stack traces are not to
     * be dumped}, the logged message will be appended with the throwable's
     * <code>Throwable.toString()</code> contents.
     *
     * @param log where to log the message
     * @param key the resource key that is associated with the message
     * @param msg the message to log
     * @param throwable the throwable associated with the message */
    private static void logInfoWithThrowable(org.apache.logging.log4j.Logger log, String key, Msg msg, Throwable throwable) {
        if (Logger.getDumpStackTraces()) {
            log.info(((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg), throwable);
        } else {
            log.info(((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg) + ". Cause: " + throwable.toString());
        }
    }

    /** Logs the message, along with the given <code>throwable</code>, at the
     * debug level. If {@link Logger#getDumpStackTraces() stack traces are not
     * to be dumped}, the logged message will be appended with the throwable's
     * <code>Throwable.toString()</code> contents.
     *
     * @param log where to log the message
     * @param key the resource key that is associated with the message
     * @param msg the message to log
     * @param throwable the throwable associated with the message */
    private static void logDebugWithThrowable(org.apache.logging.log4j.Logger log, String key, Msg msg, Throwable throwable) {
        if (Logger.getDumpStackTraces()) {
            log.debug(((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg), throwable);
        } else {
            log.debug(((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg) + ". Cause: " + throwable.toString());
        }
    }

    /** Logs the message, along with the given <code>throwable</code>, at the
     * trace level. If {@link Logger#getDumpStackTraces() stack traces are not
     * to be dumped}, the logged message will be appended with the throwable's
     * <code>Throwable.toString()</code> contents.
     *
     * @param log where to log the message
     * @param key the resource key that is associated with the message
     * @param msg the message to log
     * @param throwable the throwable associated with the message */
    private static void logTraceWithThrowable(org.apache.logging.log4j.Logger log, String key, Msg msg, Throwable throwable) {
        if (Logger.getDumpStackTraces()) {
            log.trace(((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg), throwable);
        } else {
            log.trace(((Logger.getDumpLogKeys()) ? ('{' + key + '}' + msg) : msg) + ". Cause: " + throwable.toString());
        }
    }
}