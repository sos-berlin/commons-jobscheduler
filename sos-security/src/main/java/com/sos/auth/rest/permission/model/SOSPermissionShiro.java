//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.08.29 um 04:30:14 PM CEST 
//


package com.sos.auth.rest.permission.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element ref="{}SOSPermissionRoles"/>
 *         &lt;element ref="{}SOSPermissions"/>
 *       &lt;/sequence>
 *       &lt;attribute name="authenticated" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="user" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="access_token" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="security_server_url" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sosPermissionRoles",
    "sosPermissions"
})
@XmlRootElement(name = "SOSPermissionShiro")
public class SOSPermissionShiro {

    @XmlElement(name = "SOSPermissionRoles", required = true)
    protected SOSPermissionRoles sosPermissionRoles;
    @XmlElement(name = "SOSPermissions", required = true)
    protected SOSPermissions sosPermissions;
    @XmlAttribute(name = "authenticated")
    protected Boolean authenticated;
    @XmlAttribute(name = "user")
    protected String user;
    @XmlAttribute(name = "access_token")
    protected String accessToken;
    @XmlAttribute(name = "security_server_url")
    protected String securityServerUrl;

    /**
     * Ruft den Wert der sosPermissionRoles-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SOSPermissionRoles }
     *     
     */
    public SOSPermissionRoles getSOSPermissionRoles() {
        return sosPermissionRoles;
    }

    /**
     * Legt den Wert der sosPermissionRoles-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SOSPermissionRoles }
     *     
     */
    public void setSOSPermissionRoles(SOSPermissionRoles value) {
        this.sosPermissionRoles = value;
    }

    /**
     * Ruft den Wert der sosPermissions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SOSPermissions }
     *     
     */
    public SOSPermissions getSOSPermissions() {
        return sosPermissions;
    }

    /**
     * Legt den Wert der sosPermissions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SOSPermissions }
     *     
     */
    public void setSOSPermissions(SOSPermissions value) {
        this.sosPermissions = value;
    }

    /**
     * Ruft den Wert der authenticated-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Legt den Wert der authenticated-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAuthenticated(Boolean value) {
        this.authenticated = value;
    }

    /**
     * Ruft den Wert der user-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUser() {
        return user;
    }

    /**
     * Legt den Wert der user-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUser(String value) {
        this.user = value;
    }

    /**
     * Ruft den Wert der accessToken-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Legt den Wert der accessToken-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccessToken(String value) {
        this.accessToken = value;
    }

    /**
     * Ruft den Wert der securityServerUrl-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSecurityServerUrl() {
        return securityServerUrl;
    }

    /**
     * Legt den Wert der securityServerUrl-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSecurityServerUrl(String value) {
        this.securityServerUrl = value;
    }

}
