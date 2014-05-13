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
package com.sos.i18n.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that declares a resource bundle. The resource bundle's base name is defined by an annotation
 * attribute. A default locale is also defined by an optional attribute - this denotes the locale for those I18N
 * messages whose annotations do not define a locale attribute. This default locale is only applicable for those
 * {@link I18NMessage} annotations within the same scope of this resource bundle annotation (that is, the default
 * is only for the class or field that this annotation annotates). If the default locale is not specified, the
 * annotation processor should choose an appropriate default (such as the default locale of the JVM).
 *
 * @author <a href="mailto:jmazzitelli@users.sourceforge.net">John Mazzitelli</a>
 */
@Documented
@Retention( RetentionPolicy.RUNTIME )
@Target( {
           ElementType.TYPE,
           ElementType.FIELD
        } )
public @interface I18NResourceBundle
{
   /**
    * The resource bundle's base name (for example, if the resource bundle file name is to be "messages_en.properties",
    * the resource bundle base name is "messages").
    */
   String baseName() default "messages";

   /**
    * Defines the default locale of all I18N messages within the scope of this resource bundle.
    */
   String defaultLocale() default "";
}