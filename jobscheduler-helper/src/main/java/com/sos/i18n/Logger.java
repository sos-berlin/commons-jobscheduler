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

import java.util.Locale;

/** Superclass to all the loggers that provide the ability to log messages that
 * are retrieved from a resource bundle with the additional capability of being
 * able to set the base bundle name and locale that this logger will use to log
 * the message. Note that if this object is created with a <code>null</code> or
 * unspecified locale, this logger will use {@link LoggerLocale} to determine
 * what locale it should use.
 *
 * <p>
 * This object also provides a convienent way to obtain I18N messages from the
 * resource bundle this logger uses in the locale this logger uses. This is
 * useful for when you need to output messages outside of any log mechanism
 * (e.g. creating exceptions with a message string). See
 * {@link #getMsg(String, Object[])} and {@link #getMsgString(String, Object[])}
 * .
 * </p>
 *
 * <p>
 * This abstraction is to allow subclasses to utilize different logging
 * implementations, like log4j, commons logging, JDK logging, etc. Subclasses
 * should/must override all constructors and simply call the super constructors.
 * Subclasses must implement the {@link #createLogObject(String)}/
 * {@link #createLogObject(Class)} methods in order to create its own log
 * implementation object. That created log object should then be used by the
 * subclass' implementations of the different log methods (e.g.
 * {@link #fatal(String, Object[])}, etc).
 * </p>
 *
 * <p>
 * By default, all loggers will <b>not</b> dump stack traces to the log when
 * throwables are logged. To change this default behavior, set the system
 * property {@link #SYSPROP_DUMP_STACK_TRACES} to <code>true</code> or
 * programatically call {@link #setDumpStackTraces(boolean)} (bear in mind that
 * this will affect all loggers - those that currently exist and those created
 * in the future). Note, however, that this flag has no bearing on Throwables
 * logged at the FATAL level; that is to say, all Throwables that are logged at
 * the FATAL level will always have their stack traces dumped to the log.
 * Additionally, all loggers <b>will</b> dump the resource bundle keys that map
 * to the messages being logged (this is to make it easier to correlate a log
 * message to where it was logged in the code if the message itself is in a
 * language that cannot be read by the person looking at the log). To change
 * this default behavior, set the system property {@link #SYSPROP_DUMP_LOG_KEYS}
 * to <code>false</code> or programatically call
 * {@link #setDumpLogKeys(boolean)}. Again, this affects all loggers.
 * </p>
 *
 * @author <a href="mailto:jmazzitelli@users.sourceforge.net">John
 *         Mazzitelli</a>
 * @version $Revision: 1.2 $
 * @see Msg
 * @see LoggerLocale */
public abstract class Logger {

    /** System property name that is used to define the default dump-stack-trace
     * behavior. */
    public static final String SYSPROP_DUMP_STACK_TRACES = "i18nlog.dump-stack-traces";

    /** System property name that is used to determine if the resource keys are
     * logged along with the log message. */
    public static final String SYSPROP_DUMP_LOG_KEYS = "i18nlog.dump-keys";

    /** Determines the behavior of the log methods when provided a
     * <code>Throwable</code>. If <code>true</code>, stack traces will be dumped
     * to the log; if <code>false</code>, a cause message will be appended to
     * the log message but the stack will not be dumped (except for fatal log
     * level messages, stack dumps will always be dumped for those). The default
     * is specified by the value of the system property
     * {@link #SYSPROP_DUMP_STACK_TRACES}, if that is not specified, the default
     * is <code>false</code>. You can change this value after this class has
     * been loaded by the {@link #setDumpStackTraces(boolean)} method. */
    private static boolean s_dumpStackTraces;

    /** To facilitate easier correlation between a log message (in any locale)
     * with the resource key that is associated with that message, this flag can
     * be <code>true</code> to force the key to be logged with the message
     * itself. This will help a developer or support engineer more easily
     * correlate a log message with the place in the code that generated it,
     * especially if that log message is in a non-native language. */
    private static boolean s_dumpKeys;

    // initialize the static dump flags
    static {
        try {
            s_dumpStackTraces = Boolean.getBoolean(SYSPROP_DUMP_STACK_TRACES);
        } catch (Exception e) { // probably a security error not letting us read
                                // system properties
            s_dumpStackTraces = false;
        }

        try {
            s_dumpKeys = Boolean.parseBoolean(System.getProperty(SYSPROP_DUMP_LOG_KEYS, "true"));
        } catch (Exception e) { // probably a security error not letting us read
                                // system properties
            s_dumpKeys = true;
        }
    }

    /** Identifies the base name of the resource bundle. For example, if the
     * resource bundle to be used is "my-messages_fr_FR.properties", the base
     * bundle name must have a value of "my-messages". */
    private Msg.BundleBaseName m_baseBundleName;

    /** The locale in which the messages will be logged. This helps determine
     * what resource bundle to use. For example, if the resource bundle to be
     * used is "my-messages_fr_FR.properties", the locale must be <code>
     * Locale.FRANCE</code> (fr_FR). If this is <code>null</code>, the locale
     * used will be determined by {@link LoggerLocale}. */
    private Locale m_locale;

    /** Gets the flag this class will use to determine if stack traces should be
     * logged when <code>Throwables</code> are provided to the log methods.
     *
     * @return flag to indicate if stack traces are dumped to the log */
    public static boolean getDumpStackTraces() {
        return s_dumpStackTraces;
    }

    /** Sets the flag this class will use to determine if stack traces should be
     * logged when <code>Throwable</code>s are provided to the log methods.
     *
     * @param flag the new flag to indicate if stack traces are to be dumped to
     *            the log */
    public static void setDumpStackTraces(boolean flag) {
        s_dumpStackTraces = flag;
    }

    /** Gets the flag this class will use to determine if the resource bundle key
     * names should be logged along with the log message itself. When enabled,
     * this helps a person looking at the log messages to more easily determine
     * what a log message is and what code generated it if that person does not
     * understand the language that the log message was written in.
     *
     * @return flag to indicate if resource bundle keys are logged along with
     *         the log message */
    public static boolean getDumpLogKeys() {
        return s_dumpKeys;
    }

    /** Sets the flag this class will use to determine if the resource bundle key
     * names should be logged along with the log message itself. When this is
     * set to <code>true</code>, this helps a person looking at the log messages
     * to more easily determine what a log message is and what code generated it
     * if that person does not understand the language that the log message was
     * written in.
     *
     * @param flag the new flag to indicate if resource bundle keys are logged
     *            along with the log message */
    public static void setDumpLogKeys(boolean flag) {
        s_dumpKeys = flag;
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
     * @see Msg#Msg(Msg.BundleBaseName, Locale) */
    public Logger(String name, Msg.BundleBaseName basename, Locale locale) {
        createLogObject(name);
        m_baseBundleName = basename;
        m_locale = locale;
    }

    /** Creates a new {@link Logger} object.
     *
     * @param clazz the class of the object that will be logging messages
     * @param basename the base bundle name used to identify the resource bundle
     *            to use (may be <code>null</code>)
     * @param locale the locale of the messages that will be logged (may be
     *            <code>null</code>)
     *
     * @see Msg#Msg(Msg.BundleBaseName, Locale) */
    public Logger(Class clazz, Msg.BundleBaseName basename, Locale locale) {
        createLogObject(clazz);
        m_baseBundleName = basename;
        m_locale = locale;
    }

    /** Creates a new {@link Logger} object using the default log locale as
     * determined by {@link LoggerLocale}.
     *
     * @param name the name to give to the logger (this is usually a class name
     *            of the object that will be logging messages)
     * @param basename the base bundle name used to identify the resource bundle
     *            to use (may be <code>null</code>)
     *
     * @see Msg#Msg(Msg.BundleBaseName) */
    public Logger(String name, Msg.BundleBaseName basename) {
        this(name, basename, null);
    }

    /** Creates a new {@link Logger} object using the default log locale as
     * determined by {@link LoggerLocale}.
     *
     * @param clazz the class of the object that will be logging messages
     * @param basename the base bundle name used to identify the resource bundle
     *            to use (may be <code>null</code>)
     *
     * @see Msg#Msg(Msg.BundleBaseName) */
    public Logger(Class clazz, Msg.BundleBaseName basename) {
        this(clazz, basename, null);
    }

    /** Creates a new {@link Logger} object using a default resource bundle.
     *
     * @param name the name to give to the logger (this is usually a class name
     *            of the object that will be logging messages)
     * @param locale the locale of the messages that will be logged (may be
     *            <code>null</code>)
     *
     * @see Msg#Msg(Locale) */
    public Logger(String name, Locale locale) {
        this(name, null, locale);
    }

    /** Creates a new {@link Logger} object using a default resource bundle.
     *
     * @param clazz the class of the object that will be logging messages
     * @param locale the locale of the messages that will be logged (may be
     *            <code>null</code>)
     *
     * @see Msg#Msg(Locale) */
    public Logger(Class clazz, Locale locale) {
        this(clazz, null, locale);
    }

    /** Creates a new {@link Logger} object using a default resource bundle and
     * the default log locale as determined by {@link LoggerLocale}.
     *
     * @param name the name to give to the logger (this is usually a class name
     *            of the object that will be logging messages)
     *
     * @see Msg#Msg() */
    public Logger(String name) {
        this(name, null, null);
    }

    /** Creates a new {@link Logger} object using a default resource bundle and
     * the default log locale as determined by {@link LoggerLocale}.
     *
     * @param clazz the class of the object that will be logging messages
     *
     * @see Msg#Msg() */
    public Logger(Class clazz) {
        this(clazz, null, null);
    }

    /** Returns the base bundle name of the resource bundle that will house this
     * logger's messages.
     *
     * @return base bundle name; if <code>null</code>, the default base bundle
     *         name will be used
     *
     * @see Msg#getBundleBaseNameDefault() */
    public Msg.BundleBaseName getBaseBundleName() {
        return m_baseBundleName;
    }

    /** Returns the locale of the messages that are to be logged. If this object
     * was created with a <code>null</code> or unspecified locale, this method
     * will use {@link LoggerLocale} to determine what locale to return.
     *
     * @return locale that this logger will use to log messages */
    public Locale getLocale() {
        if (m_locale != null) {
            return m_locale;
        }

        return LoggerLocale.getLogLocale();
    }

    /** Returns a localized {@link Msg} object whose message is found in this
     * logger's {@link #getBaseBundleName() resource bundle} in the logger's
     * {@link #getLocale() locale}.
     *
     * @param key the resource bundle key of the message to get
     * @param varargs the resource bundle message arguments
     *
     * @return the localized message object
     *
     * @see #getMsgString(String, Object[]) */
    public Msg getMsg(String key, Object... varargs) {
        return Msg.createMsg(getBaseBundleName(), getLocale(), key, varargs);
    }

    /** Returns a localized message string that is found in this logger's
     * {@link #getBaseBundleName() resource bundle} in the logger's
     * {@link #getLocale() locale}. This is equivalent to
     * <code>{@link #getMsg(String, Object[])}.toString()</code>.
     *
     * @param key the resource bundle key of the message to get
     * @param varargs the resource bundle message arguments
     *
     * @return the localized message string
     *
     * @see #getMsg(String, Object[]) */
    public String getMsgString(String key, Object... varargs) {
        return getMsg(key, varargs).toString();
    }

    /** Returns <code>true</code> if the fatal log level is enabled
     *
     * @return enabled flag */
    public abstract boolean isFatalEnabled();

    /** Returns <code>true</code> if the error log level is enabled
     *
     * @return enabled flag */
    public abstract boolean isErrorEnabled();

    /** Returns <code>true</code> if the warn log level is enabled
     *
     * @return enabled flag */
    public abstract boolean isWarnEnabled();

    /** Returns <code>true</code> if the info log level is enabled
     *
     * @return enabled flag */
    public abstract boolean isInfoEnabled();

    /** Returns <code>true</code> if the debug log level is enabled
     *
     * @return enabled flag */
    public abstract boolean isDebugEnabled();

    /** Returns <code>true</code> if the trace log level is enabled
     *
     * @return enabled flag */
    public abstract boolean isTraceEnabled();

    /** Logs the throwable with the resource bundle message and args.
     *
     * @param throwable the throwable to log
     * @param key the resource bundle key of the message to log
     * @param varargs the resource bundle message arguments
     *
     * @return the resource bundle message that was logged */
    public abstract Msg fatal(Throwable throwable, String key, Object... varargs);

    /** Logs the resource bundle message and args.
     *
     * @param key the resource bundle key of the message to log
     * @param varargs the resource bundle message arguments
     *
     * @return the resource bundle message that was logged */
    public abstract Msg fatal(String key, Object... varargs);

    /** Logs the throwable with the resource bundle message and args.
     *
     * @param throwable the throwable to log
     * @param key the resource bundle key of the message to log
     * @param varargs the resource bundle message arguments
     *
     * @return the resource bundle message that was logged */
    public abstract Msg error(Throwable throwable, String key, Object... varargs);

    /** Logs the resource bundle message and args.
     *
     * @param key the resource bundle key of the message to log
     * @param varargs the resource bundle message arguments
     *
     * @return the resource bundle message that was logged */
    public abstract Msg error(String key, Object... varargs);

    /** Logs the throwable with the resource bundle message and args.
     *
     * @param throwable the throwable to log
     * @param key the resource bundle key of the message to log
     * @param varargs the resource bundle message arguments
     *
     * @return the resource bundle message that was logged */
    public abstract Msg warn(Throwable throwable, String key, Object... varargs);

    /** Logs the resource bundle message and args.
     *
     * @param key the resource bundle key of the message to log
     * @param varargs the resource bundle message arguments
     *
     * @return the resource bundle message that was logged */
    public abstract Msg warn(String key, Object... varargs);

    /** Logs the throwable with the resource bundle message and args.
     *
     * @param throwable the throwable to log
     * @param key the resource bundle key of the message to log
     * @param varargs the resource bundle message arguments
     *
     * @return the resource bundle message that was logged */
    public abstract Msg info(Throwable throwable, String key, Object... varargs);

    /** Logs the resource bundle message and args.
     *
     * @param key the resource bundle key of the message to log
     * @param varargs the resource bundle message arguments
     *
     * @return the resource bundle message that was logged */
    public abstract Msg info(String key, Object... varargs);

    /** Logs the throwable with the resource bundle message and args.
     *
     * @param throwable the throwable to log
     * @param key the resource bundle key of the message to log
     * @param varargs the resource bundle message arguments
     *
     * @return the resource bundle message that was logged */
    public abstract Msg debug(Throwable throwable, String key, Object... varargs);

    /** Logs the resource bundle message and args.
     *
     * @param key the resource bundle key of the message to log
     * @param varargs the resource bundle message arguments
     *
     * @return the resource bundle message that was logged */
    public abstract Msg debug(String key, Object... varargs);

    /** Logs the throwable with the resource bundle message and args.
     *
     * @param throwable the throwable to log
     * @param key the resource bundle key of the message to log
     * @param varargs the resource bundle message arguments
     *
     * @return the resource bundle message that was logged */
    public abstract Msg trace(Throwable throwable, String key, Object... varargs);

    /** Logs the resource bundle message and args.
     *
     * @param key the resource bundle key of the message to log
     * @param varargs the resource bundle message arguments
     *
     * @return the resource bundle message that was logged */
    public abstract Msg trace(String key, Object... varargs);

    /** Creates the actual log object to be used by this logger based on the
     * given class.
     *
     * @param clazz the class to associate with the log object that is to be
     *            created */
    protected abstract void createLogObject(Class clazz);

    /** Creates the actual log object to be used by this logger given the name of
     * the logger as a String.
     *
     * @param name the name to associate with the log object that is to be
     *            created */
    protected abstract void createLogObject(String name);
}
