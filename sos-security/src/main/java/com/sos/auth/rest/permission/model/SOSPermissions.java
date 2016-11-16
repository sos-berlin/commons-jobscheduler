//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.11.15 um 11:35:56 AM CET 
//


package com.sos.auth.rest.permission.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
     * Ruft den Wert der sosPermissionJoc-Eigenschaft ab.
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
     * Legt den Wert der sosPermissionJoc-Eigenschaft fest.
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
     * Ruft den Wert der sosPermissionJoe-Eigenschaft ab.
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
     * Legt den Wert der sosPermissionJoe-Eigenschaft fest.
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
     * Ruft den Wert der sosPermissionJid-Eigenschaft ab.
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
     * Legt den Wert der sosPermissionJid-Eigenschaft fest.
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
