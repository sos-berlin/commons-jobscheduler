//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.07.09 at 02:24:25 PM CEST 
//


package com.sos.auth.rest.permission.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}SOSPermissionJoc"/>
 *         &lt;element ref="{}SOSPermissionJoe"/>
 *         &lt;element ref="{}SOSPermissionJid"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sosPermissionJoc",
    "sosPermissionJoe",
    "sosPermissionJid"
})
@XmlRootElement(name = "SOSPermissions")
public class SOSPermissions {

    @XmlElement(name = "SOSPermissionJoc", required = true)
    protected SOSPermissionJoc sosPermissionJoc;
    @XmlElement(name = "SOSPermissionJoe", required = true)
    protected SOSPermissionJoe sosPermissionJoe;
    @XmlElement(name = "SOSPermissionJid", required = true)
    protected SOSPermissionJid sosPermissionJid;

    /**
     * Gets the value of the sosPermissionJoc property.
     * 
     * @return
     *     possible object is
     *     {@link SOSPermissionJoc }
     *     
     */
    public SOSPermissionJoc getSOSPermissionJoc() {
        return sosPermissionJoc;
    }

    /**
     * Sets the value of the sosPermissionJoc property.
     * 
     * @param value
     *     allowed object is
     *     {@link SOSPermissionJoc }
     *     
     */
    public void setSOSPermissionJoc(SOSPermissionJoc value) {
        this.sosPermissionJoc = value;
    }

    /**
     * Gets the value of the sosPermissionJoe property.
     * 
     * @return
     *     possible object is
     *     {@link SOSPermissionJoe }
     *     
     */
    public SOSPermissionJoe getSOSPermissionJoe() {
        return sosPermissionJoe;
    }

    /**
     * Sets the value of the sosPermissionJoe property.
     * 
     * @param value
     *     allowed object is
     *     {@link SOSPermissionJoe }
     *     
     */
    public void setSOSPermissionJoe(SOSPermissionJoe value) {
        this.sosPermissionJoe = value;
    }

    /**
     * Gets the value of the sosPermissionJid property.
     * 
     * @return
     *     possible object is
     *     {@link SOSPermissionJid }
     *     
     */
    public SOSPermissionJid getSOSPermissionJid() {
        return sosPermissionJid;
    }

    /**
     * Sets the value of the sosPermissionJid property.
     * 
     * @param value
     *     allowed object is
     *     {@link SOSPermissionJid }
     *     
     */
    public void setSOSPermissionJid(SOSPermissionJid value) {
        this.sosPermissionJid = value;
    }

}
