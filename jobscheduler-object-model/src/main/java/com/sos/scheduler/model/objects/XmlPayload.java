//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB)
// Reference Implementation, v2.2.3-hudson-jaxb-ri-2.2.3-3-
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source
// schema.
// Generated on: 2011.01.17 at 03:00:56 PM MEZ
//

package com.sos.scheduler.model.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlType;

import org.w3c.dom.Element;

/** <p>
 * Java class for Xml_payload complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="Xml_payload">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;any processContents='skip'/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre> */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Xml_payload", propOrder = { "any" })
public class XmlPayload extends JSObjBase {

    @XmlAnyElement
    protected Element any;

    /** Gets the value of the any property.
     * 
     * @return possible object is {@link Element } */
    public Element getAny() {
        return any;
    }

    /** Sets the value of the any property.
     * 
     * @param value allowed object is {@link Element } */
    public void setAny(Element value) {
        this.any = value;
    }

}
