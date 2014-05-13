/**
 * I18N Messages and Logging
 * Copyright (C) 2006 John J. Mazzitelli
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA 
 */
package com.sos.i18n;

import java.util.Locale;

/**
 * A utility that provides a locale that you can use for your loggers. Sometimes, you want your log messages to be
 * logged in one locale while your user interfaces use another locale. For example, a customer may be running in a
 * German locale, however, the team that supports the software may be located in France and only speaks and reads
 * French. Therefore, they will want to see the logs dumped in French so they can read the logs and help decipher
 * problems that their German customer is having.
 *
 * <p>This class provides a method ({@link #getLogLocale()})to give you the locale that your log messages should be
 * logged with. It may or may not be different than the JVM's default locale.</p>
 *
 * <p>To define a separate log locale, this class looks for a system property called <code>
 * i18nlog.log-locale</code>. It must be in the form of "language_country_variant" where country and variant are
 * optional. For example, the system property can be "it", "en_US" or "fr_FR_EURO".</p>
 *
 * <p>If you want the log locale to be the same as the default VM locale, set the system property <code>
 * i18nlog.log-locale-is-default</code> to <code>true</code>. This will be ignored if the <code>
 * i18nlog.log-locale</code> system property is set.</p>
 *
 * <p>If neither system property is set, the log locale is set to <code>Locale.ENGLISH</code>.</p>
 *
 * <p>You can also programatically set the locale via the methods {@link #setLogLocale(Locale)} or
 * {@link #setLogLocale(String)}.</p>
 *
 * <p>This utility is a singleton. If you change the system properties during runtime and wish for this singleton
 * to pick up the change (and thus change its log locale value), you must call {@link #reset()} after the change in
 * system properties is made. It is preferrable to call one of the setter methods to affect a change rather that
 * using the system properties.</p>
 *
 * @author  <a href="mailto:jmazzitelli@users.sourceforge.net">John Mazzitelli</a>
 * @version $Revision: 1.1 $
 * @see     Locale
 */
public class LoggerLocale
{
   /**
    * The name of the system property that contains the log locale.
    */
   public static final String LOG_LOCALE_SYSTEM_PROPERTY = "i18nlog.log-locale";

   /**
    * If this system property is set to <code>true</code>, the log locale will be set to the same as the default
    * JVM locale.
    */
   public static final String LOG_LOCALE_IS_DEFAULT_SYSTEM_PROPERTY = "i18nlog.log-locale-is-default";

   /**
    * The locale that loggers can use to log their messages.
    */
   private static Locale s_logLocale = null;

   /**
    * If the log locale has not been set yet, this will examines the system properties
    * {@link #LOG_LOCALE_SYSTEM_PROPERTY} and {@link #LOG_LOCALE_IS_DEFAULT_SYSTEM_PROPERTY} to determine what the
    * log locale should be.
    *
    * @return the log locale to be used by loggers
    */
   public static Locale getLogLocale()
   {
      if ( s_logLocale != null )
      {
         return s_logLocale;
      }

      String  locale_prop            = null;
      boolean locale_is_default_prop = false;

      try
      {
         locale_prop            = System.getProperty( LOG_LOCALE_SYSTEM_PROPERTY );
         locale_is_default_prop = Boolean.getBoolean( LOG_LOCALE_IS_DEFAULT_SYSTEM_PROPERTY );
      }
      catch ( Throwable t )
      { // probably due to a security manager not allowing us to see the properties
      }

      s_logLocale = parseLocaleString( locale_prop, locale_is_default_prop );

      return s_logLocale;
   }

   /**
    * This method allows you to explicitly define what locale all loggers should use when logging messages.
    *
    * @param locale the new locale
    */
   public static void setLogLocale( Locale locale )
   {
      s_logLocale = locale;
   }

   /**
    * Parses the locale string, which must be in the form of "language_country_variant" where country and variant
    * are optional, and returns that locale. If <code>locale_str</code> is <code>null</code>, then <code>
    * Locale.ENGLISH</code> is returned. If you want the default VM to be used as the new log locale, call
    * {@link #setLogLocale(Locale) setLogLocale}(Locale.getDefault())</code>.
    *
    * @param locale_str the locale string in the form of "language_country_variant" or <code>null</code>
    */
   public static void setLogLocale( String locale_str )
   {
      s_logLocale = parseLocaleString( locale_str, false );
   }

   /**
    * Resets what this class thinks should be the log locale. Call this if you change the system properties and
    * want this class to re-read those system properties to determine the new log locale. Or call this method if
    * you want to undo any changes made programatically via the setter methods and want to return to the locale
    * that is specified by the system properties.
    */
   public static void reset()
   {
      s_logLocale = null;
      getLogLocale();

      return;
   }

   /**
    * Parses the locale string, which must be in the form of "language_country_variant" where country and variant
    * are optional, and returns that locale. If <code>locale_str</code> is <code>null</code>, then if <code>
    * locale_is_default</code> is <code>true</code>, the VM's default locale will be returned. Otherwise, <code>
    * Locale.ENGLISH</code> is returned.
    *
    * @param  locale_str        the locale string in the form of "language_country_variant"
    * @param  locale_is_default if <code>locale_str</code> is <code>null</code>, this indicates if the VM's default
    *                           locale is used
    *
    * @return the locale
    */
   private static Locale parseLocaleString( String  locale_str,
                                            boolean locale_is_default )
   {
      Locale log_locale;

      if ( locale_str != null )
      {
         String[] locale_specs = locale_str.split( "_" );
         String   language     = ( locale_specs.length > 0 ) ? locale_specs[0] : "";
         String   country      = ( locale_specs.length > 1 ) ? locale_specs[1] : "";
         String   variant      = ( locale_specs.length > 2 ) ? locale_specs[2] : "";

         log_locale = new Locale( language, country, variant );
      }
      else if ( locale_is_default )
      {
         log_locale = Locale.getDefault();
      }
      else
      {
         log_locale = Locale.ENGLISH;
      }

      return log_locale;
   }
}