//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB)
// Reference Implementation, v2.2.3-hudson-jaxb-ri-2.2.3-3-
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source
// schema.
// Generated on: 2014.01.28 at 10:30:50 AM MEZ
//

package com.sos.scheduler.model.answers;

import java.io.Serializable;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
 *         &lt;element ref="{}job_subsystem.statistics"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre> */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "jobSubsystemStatistics" })
@XmlRootElement(name = "job_subsystem")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2014-01-28T10:30:50+01:00", comments = "JAXB RI v2.2.3-hudson-jaxb-ri-2.2.3-3-")
public class JobSubsystem implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 140996747241779817L;
    @XmlElement(name = "job_subsystem.statistics", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2014-01-28T10:30:50+01:00", comments = "JAXB RI v2.2.3-hudson-jaxb-ri-2.2.3-3-")
    protected JobSubsystemStatistics jobSubsystemStatistics;

    /** Gets the value of the jobSubsystemStatistics property.
     * 
     * @return possible object is {@link JobSubsystemStatistics } */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2014-01-28T10:30:50+01:00", comments = "JAXB RI v2.2.3-hudson-jaxb-ri-2.2.3-3-")
    public JobSubsystemStatistics getJobSubsystemStatistics() {
        return jobSubsystemStatistics;
    }

    /** Sets the value of the jobSubsystemStatistics property.
     * 
     * @param value allowed object is {@link JobSubsystemStatistics } */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2014-01-28T10:30:50+01:00", comments = "JAXB RI v2.2.3-hudson-jaxb-ri-2.2.3-3-")
    public void setJobSubsystemStatistics(JobSubsystemStatistics value) {
        this.jobSubsystemStatistics = value;
    }

}
