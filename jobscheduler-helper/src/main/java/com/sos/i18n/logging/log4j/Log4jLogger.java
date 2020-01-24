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
import com.sos.i18n.LoggerLocale;
import com.sos.i18n.Msg;

import java.util.Locale;

/** Similar in functionality to the {@link Log4jLogMsg} utility class, this
 * provides the ability to log messages that are retrieved from a resource
 * bundle with the additional capability of being able to set the base bundle
 * name and locale that this logger will use to log the message. If the locale
 * is not specified or <code>
 * null</code>, the {@link LoggerLocale} will be used to determine what locale
 * will be used.
 *
 * @author <a href="mailto:jmazzitelli@users.sourceforge.net">John
 *         Mazzitelli</a>
 * @version $Revision: 1.2 $ */
public class Log4jLogger extends Logger {

    private org.apache.logging.log4j.Logger m_log;

    /** @see Logger#Logger(String, Msg.BundleBaseName, Locale) */
    public Log4jLogger(String name, Msg.BundleBaseName basename, Locale locale) {
        super(name, basename, locale);
    }

    /** @see Logger#Logger(Class, Msg.BundleBaseName, Locale) */
    public Log4jLogger(Class clazz, Msg.BundleBaseName basename, Locale locale) {
        super(clazz, basename, locale);
    }

    /** @see Logger#Logger(String, Msg.BundleBaseName) */
    public Log4jLogger(String name, Msg.BundleBaseName basename) {
        super(name, basename);
    }

    /** @see Logger#Logger(Class, Msg.BundleBaseName) */
    public Log4jLogger(Class clazz, Msg.BundleBaseName basename) {
        super(clazz, basename);
    }

    /** @see Logger#Logger(String, Locale) */
    public Log4jLogger(String name, Locale locale) {
        super(name, locale);
    }

    /** @see Logger#Logger(Class, Locale) */
    public Log4jLogger(Class clazz, Locale locale) {
        super(clazz, locale);
    }

    /** @see Logger#Logger(String) */
    public Log4jLogger(String name) {
        super(name);
    }

    /** @see Logger#Logger(Class) */
    public Log4jLogger(Class clazz) {
        super(clazz);
    }

    /** Log4J's design philosophy is that this method should never be needed.
     * This method is here to comply with i18nlog's API contract; however, this
     * method will always return <code>true</code>.
     *
     * @see Logger#isFatalEnabled() */
    public boolean isFatalEnabled() {
        return true;
    }

    /** Log4J's design philosophy is that this method should never be needed.
     * This method is here to comply with i18nlog's API contract; however, this
     * method will always return <code>true</code>.
     *
     * @see Logger#isErrorEnabled() */
    public boolean isErrorEnabled() {
        return true;
    }

    /** Log4J's design philosophy is that this method should never be needed.
     * This method is here to comply with i18nlog's API contract; however, this
     * method will always return <code>true</code>.
     *
     * @see Logger#isWarnEnabled() */
    public boolean isWarnEnabled() {
        return true;
    }

    /** @see Logger#isInfoEnabled() */
    public boolean isInfoEnabled() {
        return m_log.isInfoEnabled();
    }

    /** @see Logger#isDebugEnabled() */
    public boolean isDebugEnabled() {
        return m_log.isDebugEnabled();
    }

    /** @see Logger#isTraceEnabled() */
    public boolean isTraceEnabled() {
        return m_log.isTraceEnabled();
    }

    /** Calls
     * {@link Log4jLogMsg#fatal(org.apache.log4j.Logger, Throwable, Msg.BundleBaseName, Locale, String, Object[])}
     * to log the message.
     *
     * @see Logger#fatal(Throwable, String, Object[]) */
    public Msg fatal(Throwable throwable, String key, Object... varargs) {
        return Log4jLogMsg.fatal(m_log, throwable, getBaseBundleName(), getLocale(), key, varargs);
    }

    /** Calls
     * {@link Log4jLogMsg#fatal(org.apache.log4j.Logger, Msg.BundleBaseName, Locale, String, Object[])}
     * to log the message.
     *
     * @see Logger#fatal(String, Object[]) */
    public Msg fatal(String key, Object... varargs) {
        return Log4jLogMsg.fatal(m_log, getBaseBundleName(), getLocale(), key, varargs);
    }

    /** Calls
     * {@link Log4jLogMsg#error(org.apache.log4j.Logger, Throwable, Msg.BundleBaseName, Locale, String, Object[])}
     * to log the message.
     *
     * @see Logger#error(Throwable, String, Object[]) */
    public Msg error(Throwable throwable, String key, Object... varargs) {
        return Log4jLogMsg.error(m_log, throwable, getBaseBundleName(), getLocale(), key, varargs);
    }

    /** Calls
     * {@link Log4jLogMsg#error(org.apache.log4j.Logger, Msg.BundleBaseName, Locale, String, Object[])}
     * to log the message.
     *
     * @see Logger#error(String, Object[]) */
    public Msg error(String key, Object... varargs) {
        return Log4jLogMsg.error(m_log, getBaseBundleName(), getLocale(), key, varargs);
    }

    /** Calls
     * {@link Log4jLogMsg#warn(org.apache.log4j.Logger, Throwable, Msg.BundleBaseName, Locale, String, Object[])}
     * to log the message.
     *
     * @see Logger#warn(Throwable, String, Object[]) */
    public Msg warn(Throwable throwable, String key, Object... varargs) {
        return Log4jLogMsg.warn(m_log, throwable, getBaseBundleName(), getLocale(), key, varargs);
    }

    /** Calls
     * {@link Log4jLogMsg#warn(org.apache.log4j.Logger, Msg.BundleBaseName, Locale, String, Object[])}
     * to log the message.
     *
     * @see Logger#warn(String, Object[]) */
    public Msg warn(String key, Object... varargs) {
        return Log4jLogMsg.warn(m_log, getBaseBundleName(), getLocale(), key, varargs);
    }

    /** Calls
     * {@link Log4jLogMsg#info(org.apache.log4j.Logger, Throwable, Msg.BundleBaseName, Locale, String, Object[])}
     * to log the message.
     *
     * @see Logger#info(Throwable, String, Object[]) */
    public Msg info(Throwable throwable, String key, Object... varargs) {
        return Log4jLogMsg.info(m_log, throwable, getBaseBundleName(), getLocale(), key, varargs);
    }

    /** Calls
     * {@link Log4jLogMsg#info(org.apache.log4j.Logger, Msg.BundleBaseName, Locale, String, Object[])}
     * to log the message.
     *
     * @see Logger#info(String, Object[]) */
    public Msg info(String key, Object... varargs) {
        return Log4jLogMsg.info(m_log, getBaseBundleName(), getLocale(), key, varargs);
    }

    /** Calls
     * {@link Log4jLogMsg#debug(org.apache.log4j.Logger, Throwable, Msg.BundleBaseName, Locale, String, Object[])}
     * to log the message.
     *
     * @see Logger#debug(Throwable, String, Object[]) */
    public Msg debug(Throwable throwable, String key, Object... varargs) {
        return Log4jLogMsg.debug(m_log, throwable, getBaseBundleName(), getLocale(), key, varargs);
    }

    /** Calls
     * {@link Log4jLogMsg#debug(org.apache.log4j.Logger, Msg.BundleBaseName, Locale, String, Object[])}
     * to log the message.
     *
     * @see Logger#debug(String, Object[]) */
    public Msg debug(String key, Object... varargs) {
        return Log4jLogMsg.debug(m_log, getBaseBundleName(), getLocale(), key, varargs);
    }

    /** Calls
     * {@link Log4jLogMsg#trace(org.apache.log4j.Logger, Throwable, Msg.BundleBaseName, Locale, String, Object[])}
     * to log the message.
     *
     * @see Logger#trace(Throwable, String, Object[]) */
    public Msg trace(Throwable throwable, String key, Object... varargs) {
        return Log4jLogMsg.trace(m_log, throwable, getBaseBundleName(), getLocale(), key, varargs);
    }

    /** Calls
     * {@link Log4jLogMsg#trace(org.apache.log4j.Logger, Msg.BundleBaseName, Locale, String, Object[])}
     * to log the message.
     *
     * @see Logger#trace(String, Object[]) */
    public Msg trace(String key, Object... varargs) {
        return Log4jLogMsg.trace(m_log, getBaseBundleName(), getLocale(), key, varargs);
    }

    /** Creates the Commons-Logging log object.
     *
     * @see Logger#createLogObject(Class) */
    protected void createLogObject(Class clazz) {
        m_log = org.apache.logging.log4j.LogManager.getLogger(clazz);
    }

    /** Creates the Commons-Logging log object.
     *
     * @see Logger#createLogObject(String) */
    protected void createLogObject(String name) {
        m_log = org.apache.logging.log4j.LogManager.getLogger(name);
    }
}