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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;
import java.util.ResourceBundle;

import com.sos.JSHelper.Options.SOSOptionLocale;
import com.sos.i18n.exception.LocalizedException;
import com.sos.i18n.exception.LocalizedRuntimeException;

/** Retrieves messages from a message {@link java.util.ResourceBundle} and
 * formats them as appropriate using {@link java.text.MessageFormat}. Instances
 * of this object are mostly read-only - the only thing you can change is the
 * locale. Changing the locale will attempt to convert the message to that new
 * locale's format.
 *
 * <p>
 * I18N <code>Msg</code> objects are serializable and will be re-translated once
 * it is deserialized. This means that a message could be localized in the
 * English language, serialized and sent over the wire to another JVM with a
 * German locale and when the deserialized message is retrieved again, it will
 * be in German. This feature assumes a resource bundle with the same base
 * bundle name exists in the JVM that deserialized the <code>Msg</code>. If it
 * does not, then the original message will be used (in the previous example, it
 * would mean the English message would be retrieved in the JVM, even though its
 * locale is German).
 * </p>
 *
 * <p>
 * There is a method to the madness to some of the methods' parameter ordering.
 * Anytime you want to specify a localized message, you always specify the base
 * bundle name first, followed by the locale, the bundle key and the variable
 * list of arguments that go with the keyed message (all in that order). Bundle
 * name and locale are both optional. This is consistent with the way localized
 * messages are specified in constructors for {@link LocalizedException} and
 * {@link LocalizedRuntimeException}. When you need to specify a <code>
 * Throwable</code> with your localized message, it is specified before those
 * parameters. Again, this is consistent both in this class and the localized
 * exception classes (see
 * {@link #createMsg(com.sos.i18n.Msg.BundleBaseName, Locale, String, Object[])}
 * and
 * {@link LocalizedException#LocalizedException(Throwable, com.sos.i18n.Msg.BundleBaseName, Locale, String, Object[])}
 * as examples).
 * </p>
 *
 * @author <a href="mailto:jmazzitelli@users.sourceforge.net">John
 *         Mazzitelli</a>
 * @version $Revision: 1.3 $
 * @see LocalizedException
 * @see LocalizedRuntimeException */
public class Msg implements Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 1L;

    /** Default resource bundle base name. This is not final - you can change
     * this via {@link #setBundleBaseNameDefault(Msg.BundleBaseName)}. Because
     * it is static, this value is not serialized when the object is serialized.
     * This is probably OK; we can assume if we move over to another VM, we
     * probably will want to go back to the original bundle name default. */
    private static BundleBaseName s_bundleBasenameDefault = new BundleBaseName("messages");

    /** The resource bundle's base name, used in conjunction with the locale to
     * determine which actual resource bundle to find the message in. */
    private BundleBaseName m_bundleBaseName;

    /** The locale used to determine which actual resource bundle to find the
     * message in. */
    private static Locale m_locale;

    /** The bundle message that was found last. */
    private String m_lastMessage;

    /** The last resource bundle key that was used to get the last message. */
    private String m_lastKey;

    /** The last set of variable arguments that was used to get the last message.
     * This may be <code>null</code> if this instance never retreived a message
     * or this instance was serialized and one or more of the vararg objects was
     * not serializable. */
    private Object[] m_lastVarargs;

    /** Localized resource bundle used to look up messages */
    private transient ResourceBundle m_bundle;

    /** A flag used to indicate that the last call to
     * {@link #getMsg(String, Object[])} failed to obtain the message
     * successfully from the resource bundle. Its an internal flag that is
     * allowed to be transient, no need to serialize it. */
    private transient boolean m_getFailed;

    /** Creates a {@link Msg message} object and automatically looks up the given
     * resource bundle message. The caller need only call {@link Msg#toString()}
     * to get the resource bundle message after this method returns.
     *
     * @param basename the base name of the resource bundle
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg createMsg(BundleBaseName basename, Locale locale, String key, Object... varargs) {
        Msg msg = new Msg(basename, locale);
        msg.getMsg(key, varargs);
        // Not found in Locale. Trying with "en".
        if (msg.m_getFailed) {
            msg = new Msg(basename, new Locale("en"));
            msg.getMsg(key, varargs);
        }
        return msg;
    }

    /** Creates a {@link Msg message} object and automatically looks up the given
     * resource bundle message. The caller need only call {@link Msg#toString()}
     * to get the resource bundle message after this method returns. A default
     * basename is used along with the given locale.
     *
     * @param locale the locale to determine what bundle to use
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg createMsg(Locale locale, String key, Object... varargs) {
        Msg msg = new Msg(locale);
        msg.getMsg(key, varargs);
        return msg;
    }

    /** Creates a {@link Msg message} object and automatically looks up the given
     * resource bundle message. The caller need only call {@link Msg#toString()}
     * to get the resource bundle message after this method returns.
     *
     * @param basename the base name of the resource bundle
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg createMsg(BundleBaseName basename, String key, Object... varargs) {
        Msg msg = new Msg(basename);
        msg.getMsg(key, varargs);
        return msg;
    }

    /** Creates a {@link Msg message} object and automatically looks up the given
     * resource bundle message. The caller need only call {@link Msg#toString()}
     * to get the resource bundle message after this method returns. A default
     * basename and the default locale is used to determine what resource bundle
     * to use.
     *
     * @param key the resource bundle key name
     * @param varargs arguments to help fill in the resource bundle message
     *
     * @return if the message was logged, a non-<code>null</code> Msg object is
     *         returned */
    public static Msg createMsg(String key, Object... varargs) {
        Msg msg = new Msg();
        msg.getMsg(key, varargs);
        return msg;
    }

    /** Returns the default bundle base name that all instances of this class
     * will use when no basename is provided.
     *
     * @return the bundle base name default (e.g. "com.abc.messages") */
    public static BundleBaseName getBundleBaseNameDefault() {
        return s_bundleBasenameDefault;
    }

    /** Sets the default bundle base name that all instances of this class will
     * use when no basename is provided.
     *
     * @param newDefault the new bundle base name default (e.g.
     *            "com.abc.messages") */
    public static void setBundleBaseNameDefault(BundleBaseName newDefault) {
        s_bundleBasenameDefault = newDefault;
    }

    /** Initializes the message repository with the appropriate resource bundle.
     *
     * @param basename resource bundle to use, if <code>null</code> uses the
     *            {@link #getBundleBaseNameDefault() default}
     * @param locale locale used to determine proper resource bundle to use, if
     *            <code>null</code> uses the default locale of the JVM */
    public Msg(BundleBaseName basename, Locale locale) {
        if (basename == null) {
            basename = s_bundleBasenameDefault;
        }

        if (locale == null) {
            locale = Locale.getDefault();
        }

        m_getFailed = false;
        m_bundleBaseName = basename;
        m_locale = locale;
        m_bundle = null; // will be lazily set the next time we call getMsg() or
                         // getLastMessage()
        m_lastMessage = null;
        m_lastKey = null;
        m_lastVarargs = null;

        return;
    }

    /** Initializes the message repository using the default resource bundle and
     * given locale.
     *
     * @param locale locale used to determine proper resource bundle to use
     *
     * @see Msg#Msg(com.sos.i18n.Msg.BundleBaseName, Locale) */
    public Msg(Locale locale) {
        this(s_bundleBasenameDefault, locale);
    }

    /** Initializes the message repository using the default locale and given
     * resource bundle base name.
     *
     * @param basename resource bundle to use
     *
     * @see Msg#Msg(com.sos.i18n.Msg.BundleBaseName, Locale) */
    public Msg(BundleBaseName basename) {
        // this(basename, Locale.getDefault());
        this(basename, SOSOptionLocale.i18nLocale);
    }

    /** Initializes the message repository using the default, localized resource
     * bundle. */
    public Msg() {
        this(s_bundleBasenameDefault, Locale.getDefault());
    }

    /** Sets a new locale. This allows this class to change the message it
     * returns after this instance has already been constructed.
     *
     * @return the current locale used by this class to determine which bundle
     *         to find the message in */
    public Locale getLocale() {
        return m_locale;
    }

    /** Sets a new locale. This allows this class to change the message it
     * returns after this instance has already been constructed. The side effect
     * of calling this method is that the next time {@link #getLastMessage()} is
     * called, it may return a different string if the locale was set to a
     * different locale than what it was before.
     *
     * @param locale the new locale to set, if <code>null</code>, the default
     *            locale will be set */
    public void setLocale(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }

        if (!locale.equals(getLocale())) {
            m_locale = locale;
            m_bundle = null; // the locale changed so the bundle we used before
                             // is no longer valid
        }

        return;
    }

    /** Returns the base name of the resource bundles to be used when looking for
     * messages.
     *
     * @return the resource bundles' base name */
    public BundleBaseName getBundleBaseName() {
        return m_bundleBaseName;
    }

    /** Returns the last message that this instance read from a resource bundle.
     *
     * <p>
     * This object can change the messages it finds by simply
     * {@link #setLocale(Locale) setting the locale}. When you change the local,
     * the message is retrieved again when this method is called and is stored
     * as the last message.
     *
     * @return the last retrieved resource bundle message */
    public String getLastMessage() {
        // If bundle was not yet set, we've either never retreived a message or
        // we've been serialized to another VM or someone set a new locale.
        // In either of these cases, we need to get the message again.
        // Note that if the last varargs is null, that means the
        // deserialization of the varargs
        // failed. In this case, we don't want to get the message since
        // we've now lost some of the
        // message data. We will rely on the last message that hopefully
        // contains all the data, albeit
        // in a different locale (but at least the data isn't lost).
        if (m_bundle == null && m_lastVarargs != null) {
            String lastMessageBackup = m_lastMessage;
            getMsg(m_lastKey, m_lastVarargs);
            if (m_getFailed) {
                m_lastMessage = lastMessageBackup;
            }
        }
        return m_lastMessage;
    }

    /** Returns the message string identified with the given key. The additional
     * arguments replace any placeholders found in the message. This sets the
     * {@link #getLastMessage()} when it returns.
     *
     * @param key identifies the message to be retrieved
     * @param varargs arguments to replace placeholders in message
     *
     * @return localized and formatted message
     *
     * @see java.text.MessageFormat */
    public String getMsg(String key, Object... varargs) {
        String retMessage = null;

        // See if we can find the bundle that has our new locale's messages. If
        // we can't,
        // this usually means this object was serialized and sent to another VM
        // that doesn't have the resource bundles.
        // Any exception in here will fall back to using the key and varargs as
        // the last message unless the resource
        // message was found (but failed to be formatted) - in that case, the
        // resource message will be used.
        // In either case, the varargs will be returned in the standard Java
        // List toString format.
        try {
            if (m_bundle == null || m_locale.equals(SOSOptionLocale.i18nLocale) == false) {
                m_bundle = getResourceBundle();
            }

            retMessage = m_bundle.getString(key);

            if (varargs.length > 0) {
                MessageFormat mf = new MessageFormat(retMessage, m_locale);
                retMessage = mf.format(varargs);
            }

            // remember these in case we are asked to get the message again in a
            // different locale or
            // we need to reconsitute the message after being serialized.
            m_lastKey = key;
            m_lastVarargs = varargs;

            // everything is OK
            m_getFailed = false;
        } catch (Exception e) {
            m_getFailed = true;

            if (retMessage == null) {
                retMessage = key;
            }

            Formatter formatter = new Formatter();
            formatter.format("!!! missing resource message key=[%s] args=%s", retMessage, Arrays.asList(varargs));

            retMessage = formatter.toString();
        }

        m_lastMessage = retMessage;

        return retMessage;
    }

    /** Same as {@link #getLastMessage()}.
     *
     * @see java.lang.Object#toString() */
    public String toString() {
        return getLastMessage();
    }

    /** Gets the resource bundle to use, based on the current values of
     * {@link #getBundleBaseName() the base name} and the {@link #getLocale()
     * locale}.
     *
     * @return the resource bundle to be used by this object when looking up
     *         messages */
    protected ResourceBundle getResourceBundle() {
        // We want to use the actual setted locale
        // setLocale(SOSOptionLocale.i18nLocale);
        Locale locale = getLocale();
        BundleBaseName basename = getBundleBaseName();
        ResourceBundle bundle = ResourceBundle.getBundle(basename.getBundleBaseName(), locale);

        return bundle;
    }

    /** Given a serializable object, this will return the object's serialized
     * byte array representation.
     *
     * @param object the object to serialize
     *
     * @return the serialized bytes
     *
     * @throws Exception if failed to serialize the object */
    protected static byte[] serialize(Serializable object) throws Exception {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream oos;

        oos = new ObjectOutputStream(byteStream);
        oos.writeObject(object);
        oos.close();

        return byteStream.toByteArray();
    }

    /** Deserializes the given serialization data and returns the object.
     *
     * @param serializedData the serialized data as a byte array
     *
     * @return the deserialized object
     *
     * @throws Exception if failed to deserialize the object */
    protected static Object deserialize(byte[] serializedData) throws Exception {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(serializedData);
        ObjectInputStream ois;
        Object retObject;

        ois = new ObjectInputStream(byteStream);
        retObject = ois.readObject();
        ois.close();

        return retObject;
    }

    /** <code>ResourceBundle</code> is not serializable so this serializes the
     * base bundle name and the locale with the hopes that this will be enough
     * to look up the message again when this instance is deserialized. This
     * assumes the new place where this object was deserialized has the resource
     * bundle available. If it does not, the original message will be reused.
     *
     * @param out where to write the serialized stream
     *
     * @throws IOException */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(m_bundleBaseName);
        out.writeObject(m_locale);
        out.writeObject(m_lastMessage);
        out.writeObject(m_lastKey);

        byte[] varargs;
        int varargsLength;

        try {
            // Nothing we can do about it if one or more args aren't
            // serializable.
            // We'll just have to rely on the last message when we get to the
            // other side.
            // We do our own serialization here because the writeObject docs
            // says that if a write
            // fails, the whole output stream is corrupted and in an
            // indeterminate state. Since
            // it is completely valid that some vararg objects may not be
            // serializable, we have
            // to take into account that we may not be able to serialize the
            // varargs.
            varargs = serialize(m_lastVarargs);
            varargsLength = varargs.length;
        } catch (Exception e) {
            varargs = null;
            varargsLength = -1;
        }

        out.writeInt(varargsLength);
        if (varargsLength != -1) {
            out.write(varargs);
        }

        return;
    }

    /** <code>ResourceBundle</code> is not serializable so this deserializes the
     * base bundle name and the locale with the hopes that this will be enough
     * to look up the message. This assumes the new place where this object is
     * being deserialized has the resource bundle available. If it does not, the
     * original message will be reused.
     *
     * @param in where to read the serialized stream
     *
     * @throws IOException
     * @throws ClassNotFoundException */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        // set our transient fields
        m_bundle = null;
        m_getFailed = false;

        // now read in our serialized object
        m_bundleBaseName = (BundleBaseName) in.readObject();
        m_locale = (Locale) in.readObject();
        m_lastMessage = (String) in.readObject();
        m_lastKey = (String) in.readObject();
        m_lastVarargs = null;

        int varargsLength = in.readInt();

        if (varargsLength != -1) {
            byte[] varargs = new byte[varargsLength];

            try {
                in.readFully(varargs);
                m_lastVarargs = (Object[]) deserialize(varargs);
            } catch (Exception e) {
                m_lastVarargs = null;
            }
        }

        return;
    }

    /** The purpose of this class is to offer a strongly typed object (more
     * strongly than String) so we can pass bundle base names to our vararg
     * methods and not have this be confused with a key or arg parameter. */
    public static final class BundleBaseName implements Serializable {

        private static final long serialVersionUID = 1L;
        private final String m_name;

        /** Creates a new {@link BundleBaseName} object.
         *
         * @param bundleBaseName the bundle base name string */
        public BundleBaseName(String bundleBaseName) {
            m_name = bundleBaseName;
        }

        public BundleBaseName(String bundleBaseName, final String pstrLocale) {
            m_name = bundleBaseName;
            m_locale = new Locale(pstrLocale);

        }

        /** Returns the bundle base name.
         *
         * @return the bundle base name. */
        public String getBundleBaseName() {
            return m_name;
        }

        /** @see java.lang.Object#toString() */
        public String toString() {
            return getBundleBaseName();
        }
    }
}