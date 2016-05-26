package com.sos.scheduler.model.objects;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
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
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{}include"/>
 *       &lt;/choice>
 *       &lt;attribute name="language" type="{}Name" />
 *       &lt;attribute name="use_engine">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *             &lt;enumeration value="task"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="com_class" type="{}String" />
 *       &lt;attribute name="filename" type="{}File" />
 *       &lt;attribute name="java_class" type="{}String" />
 *       &lt;attribute name="java_class_path" type="{}String" />
 *       &lt;attribute name="recompile" type="{}Yes_no" />
 *       &lt;attribute name="encoding" type="{}Code_page_encoding" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre> */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "content" })
@XmlRootElement(name = "script")
public class Script extends JSObjBase {

    @XmlElementRef(name = "include", type = Include.class)
    @XmlMixed
    protected List<Object> content;
    @XmlAttribute(name = "language")
    protected String language;
    @XmlAttribute(name = "use_engine")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String useEngine;
    @XmlAttribute(name = "com_class")
    protected String comClass;
    @XmlAttribute(name = "filename")
    protected String filename;
    @XmlAttribute(name = "java_class")
    protected String javaClass;
    @XmlAttribute(name = "java_class_path")
    protected String javaClassPath;
    @XmlAttribute(name = "recompile")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String recompile;
    @XmlAttribute(name = "encoding")
    protected CodePageEncoding encoding;

    public List<Object> getContent() {
        if (content == null) {
            content = new ArrayList<Object>();
        }
        return this.content;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String value) {
        this.language = value;
    }

    public String getUseEngine() {
        return useEngine;
    }

    public void setUseEngine(String value) {
        this.useEngine = value;
    }

    public String getComClass() {
        return comClass;
    }

    public void setComClass(String value) {
        this.comClass = value;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String value) {
        this.filename = value;
    }

    public String getJavaClass() {
        return javaClass;
    }

    public String getJavaClassPath() {
        return javaClassPath;
    }

    public void setJavaClass(String value) {
        this.javaClass = value;
    }

    public void setJavaClassPath(String value) {
        this.javaClassPath = value;
    }

    public String getRecompile() {
        return recompile;
    }

    public void setRecompile(String value) {
        this.recompile = value;
    }

    public CodePageEncoding getEncoding() {
        return encoding;
    }

    public void setEncoding(CodePageEncoding value) {
        this.encoding = value;
    }

}