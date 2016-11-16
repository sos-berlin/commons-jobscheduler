//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.11.15 um 11:35:56 AM CET 
//


package com.sos.auth.rest.permission.model;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element ref="{}SOSPermissionJoe"/>
 *         &lt;element ref="{}SOSPermissionJoc"/>
 *         &lt;element ref="{}SOSPermissionDashboard"/>
 *         &lt;element ref="{}SOSPermissionEvents"/>
 *         &lt;element ref="{}SOSPermissionWorkingplan"/>
 *         &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{}SOSPermission"/>
 *         &lt;/sequence>
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
    "sosPermissionJoe",
    "sosPermissionJoc",
    "sosPermissionDashboard",
    "sosPermissionEvents",
    "sosPermissionWorkingplan",
    "sosPermission"
})
@XmlRootElement(name = "SOSPermissionJid")
public class SOSPermissionJid {

    @XmlElement(name = "SOSPermissionJoe", required = true)
    protected SOSPermissionJoe sosPermissionJoe;
    @XmlElement(name = "SOSPermissionJoc", required = true)
    protected SOSPermissionJoc sosPermissionJoc;
    @XmlElement(name = "SOSPermissionDashboard", required = true)
    protected SOSPermissionDashboard sosPermissionDashboard;
    @XmlElement(name = "SOSPermissionEvents", required = true)
    protected SOSPermissionEvents sosPermissionEvents;
    @XmlElement(name = "SOSPermissionWorkingplan", required = true)
    protected SOSPermissionWorkingplan sosPermissionWorkingplan;
    @XmlElement(name = "SOSPermission")
    protected List<String> sosPermission;

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
     * Ruft den Wert der sosPermissionDashboard-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SOSPermissionDashboard }
     *     
     */
    public SOSPermissionDashboard getSOSPermissionDashboard() {
        return sosPermissionDashboard;
    }

    /**
     * Legt den Wert der sosPermissionDashboard-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SOSPermissionDashboard }
     *     
     */
    public void setSOSPermissionDashboard(SOSPermissionDashboard value) {
        this.sosPermissionDashboard = value;
    }

    /**
     * Ruft den Wert der sosPermissionEvents-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SOSPermissionEvents }
     *     
     */
    public SOSPermissionEvents getSOSPermissionEvents() {
        return sosPermissionEvents;
    }

    /**
     * Legt den Wert der sosPermissionEvents-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SOSPermissionEvents }
     *     
     */
    public void setSOSPermissionEvents(SOSPermissionEvents value) {
        this.sosPermissionEvents = value;
    }

    /**
     * Ruft den Wert der sosPermissionWorkingplan-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SOSPermissionWorkingplan }
     *     
     */
    public SOSPermissionWorkingplan getSOSPermissionWorkingplan() {
        return sosPermissionWorkingplan;
    }

    /**
     * Legt den Wert der sosPermissionWorkingplan-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SOSPermissionWorkingplan }
     *     
     */
    public void setSOSPermissionWorkingplan(SOSPermissionWorkingplan value) {
        this.sosPermissionWorkingplan = value;
    }

    /**
     * Gets the value of the sosPermission property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sosPermission property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSOSPermission().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSOSPermission() {
        if (sosPermission == null) {
            sosPermission = new ArrayList<String>();
        }
        return this.sosPermission;
    }

}
