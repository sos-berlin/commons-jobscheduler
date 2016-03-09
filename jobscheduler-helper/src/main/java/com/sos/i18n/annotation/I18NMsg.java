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
package com.sos.i18n.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotates a field as an I18N resource bundle message key. You define the
 * locale the message is in and that message's text value translated in the
 * locale. If a locale is not specified, it will be in the default locale
 * specified in its {@link I18NResourceBundle resource bundle}. All fields
 * annotated with this annotation must be within a scope of a
 * {@link I18NResourceBundle resource bundle} - if one is not defined, a default
 * resource bundle definition should be assumed by the annotation processor
 * (e.g. an appropriate default resource bundle definition would be a default
 * base bundle name of "messages" with a default locale being the VM default
 * locale). You can annotate the same field with multiple {@link I18NMsg}
 * annotations via {@link I18NMessages} if you want to define multiple
 * translations for a single message key.
 *
 * <p>
 * The additional "help" attribute can be used when generating help
 * documentation - it is a string that can further describe the message or
 * explain under the conditions by which this message appears and may also
 * include things, as an example, like correction procedures (if the message is
 * an error message).
 * </p>
 *
 * @author <a href="mailto:jmazzitelli@users.sourceforge.net">John
 *         Mazzitelli</a> */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface I18NMsg {

    /** The actual message text in the annotation's {@link #locale()}. This is
     * the value that is stored in the resource bundle properties file (on the
     * right side of the equals sign).
     *
     * @return the message text */
    String value() default "";

    /** The locale of the {@link #value() message text}. If this is not
     * specified, the {@link I18NResourceBundle#defaultLocale()} for the
     * resource bundle annotation in scope is used.
     *
     * @return the message locale */
    String locale() default "";

    /** An optional help string that can further describe this message - it can
     * explain under what conditions this message will appear and (if this
     * message is an error message) can include correction procedures that can
     * be followed to fix the error. This attribute is merely for informational
     * purposes only and is generally only used when generating help
     * documentation.
     *
     * @return a help description that further explains the meaning of this
     *         message */
    String help() default "";

    /** \fn explanation \brief explanation
     * 
     * \details
     *
     * \return String
     *
     * @return a short explanation, why this message occured */
    String explanation() default "";

}