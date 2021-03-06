//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB)
// Reference Implementation, vJAXB 2.1.3 in JDK 1.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source
// schema.
// Generated on: 2011.03.08 at 02:48:08 PM MEZ
//

package com.sos.scheduler.model.xmldoc;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/** <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="include" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="file" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="language">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="java"/>
 *             &lt;enumeration value="javascript"/>
 *             &lt;enumeration value="perlscript"/>
 *             &lt;enumeration value="vbscript"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="java_class" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="com_class" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="resource" type="{http://www.w3.org/2001/XMLSchema}token" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre> */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "include" })
@XmlRootElement(name = "script")
public class Script {

    protected List<Script.Include> include;
    @XmlAttribute
    protected String language;
    @XmlAttribute(name = "java_class")
    protected String javaClass;
    @XmlAttribute(name = "com_class")
    protected String comClass;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String resource;

    /** Gets the value of the include property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the include property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getInclude().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Script.Include } */
    public List<Script.Include> getInclude() {
        if (include == null) {
            include = new ArrayList<Script.Include>();
        }
        return this.include;
    }

    /** Gets the value of the language property.
     * 
     * @return possible object is {@link String } */
    public String getLanguage() {
        return language;
    }

    /** Sets the value of the language property.
     * 
     * @param value allowed object is {@link String } */
    public void setLanguage(String value) {
        this.language = value;
    }

    /** Gets the value of the javaClass property.
     * 
     * @return possible object is {@link String } */
    public String getJavaClass() {
        return javaClass;
    }

    /** Sets the value of the javaClass property.
     * 
     * @param value allowed object is {@link String } */
    public void setJavaClass(String value) {
        this.javaClass = value;
    }

    /** Gets the value of the comClass property.
     * 
     * @return possible object is {@link String } */
    public String getComClass() {
        return comClass;
    }

    /** Sets the value of the comClass property.
     * 
     * @param value allowed object is {@link String } */
    public void setComClass(String value) {
        this.comClass = value;
    }

    /** Gets the value of the resource property.
     * 
     * @return possible object is {@link String } */
    public String getResource() {
        return resource;
    }

    /** Sets the value of the resource property.
     * 
     * @param value allowed object is {@link String } */
    public void setResource(String value) {
        this.resource = value;
    }

    /** <p>
     * Java class for anonymous complex type.
     * 
     * <p>
     * The following schema fragment specifies the expected content contained
     * within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="file" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre> */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Include {

        @XmlAttribute(required = true)
        protected String file;

        /** Gets the value of the file property.
         * 
         * @return possible object is {@link String } */
        public String getFile() {
            return file;
        }

        /** Sets the value of the file property.
         * 
         * @param value allowed object is {@link String } */
        public void setFile(String value) {
            this.file = value;
        }

    }

}
