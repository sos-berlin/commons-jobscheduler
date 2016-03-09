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

/** Simply an annotation that allows for multiple {@link I18NMessage} annotations
 * to be defined.
 *
 * @author <a href="mailto:jmazzitelli@users.sourceforge.net">John
 *         Mazzitelli</a> */
@Documented
@Retention(RetentionPolicy.RUNTIME)
// @Target(ElementType.LOCAL_VARIABLE)
@Target({ ElementType.FIELD, ElementType.LOCAL_VARIABLE })
public @interface I18NMessages {

    /** The individual messages being defined. */
    I18NMessage[] value();

    /** \fn msgnum \brief msgnum
     * 
     * \details
     *
     * \return String
     *
     * @return the number of the message, if available */
    String msgnum() default "";

    String msgurl() default "";

}