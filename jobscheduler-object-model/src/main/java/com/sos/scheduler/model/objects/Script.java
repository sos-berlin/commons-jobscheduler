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