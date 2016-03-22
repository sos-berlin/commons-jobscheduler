//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.6 generiert
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas
// verloren.
// Generiert: 2014.05.09 um 02:56:23 PM CEST
//

package com.sos.scheduler.model.objects;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/** <p>
 * Java-Klasse für job_chain element declaration.
 * 
 * <p>
 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser
 * Klasse enthalten ist.
 * 
 * <pre>
 * &lt;element name="job_chain">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;choice maxOccurs="unbounded">
 *           &lt;element name="job_chain_node">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element ref="{job-chain-extensions}params"/>
 *                   &lt;/sequence>
 *                   &lt;attribute ref="{job-chain-extensions}previous_state"/>
 *                   &lt;attribute ref="{job-chain-extensions}node_type"/>
 *                   &lt;attribute ref="{job-chain-extensions}title"/>
 *                   &lt;attribute ref="{job-chain-extensions}seq_number"/>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/choice>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre> */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "jobChainNode" })
@XmlRootElement(name = "job_chain", namespace = "job-chain-extensions")
public class JobChainExtended extends JSObjBase {

    @XmlElement(name = "job_chain_node", namespace = "job-chain-extensions")
    protected List<JobChainNode> jobChainNode;

    /** Gets the value of the jobChainNode property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the jobChainNode property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getJobChainNode().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JobChainExtended.JobChainNode } */
    public List<JobChainNode> getJobChainNode() {
        if (jobChainNode == null) {
            jobChainNode = new ArrayList<JobChainNode>();
        }
        return this.jobChainNode;
    }

    /** <p>
     * Java-Klasse für anonymous complex type.
     * 
     * <p>
     * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser
     * Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element ref="{job-chain-extensions}params"/>
     *       &lt;/sequence>
     *       &lt;attribute ref="{job-chain-extensions}previous_state"/>
     *       &lt;attribute ref="{job-chain-extensions}node_type"/>
     *       &lt;attribute ref="{job-chain-extensions}title"/>
     *       &lt;attribute ref="{job-chain-extensions}seq_number"/>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre> */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "params" })
    public static class JobChainNode extends JSObjBase {

        @XmlElement(namespace = "job-chain-extensions", required = true)
        protected ParamsExtended params;
        @XmlAttribute(name = "previous_state", namespace = "job-chain-extensions")
        protected String previousState;
        @XmlAttribute(name = "node_type", namespace = "job-chain-extensions")
        protected String nodeType;
        @XmlAttribute(name = "title", namespace = "job-chain-extensions")
        protected String title;
        @XmlAttribute(name = "seq_number", namespace = "job-chain-extensions")
        @XmlJavaTypeAdapter(Adapter1.class)
        @XmlSchemaType(name = "nonNegativeInteger")
        protected Integer seqNumber;

        /** Ruft den Wert der params-Eigenschaft ab.
         * 
         * @return possible object is {@link ParamsExtended } */
        public ParamsExtended getParams() {
            return params;
        }

        /** Legt den Wert der params-Eigenschaft fest.
         * 
         * @param value allowed object is {@link ParamsExtended } */
        public void setParams(ParamsExtended value) {
            this.params = value;
        }

        /** Ruft den Wert der previousState-Eigenschaft ab.
         * 
         * @return possible object is {@link String } */
        public String getPreviousState() {
            return previousState;
        }

        /** Legt den Wert der previousState-Eigenschaft fest.
         * 
         * @param value allowed object is {@link String } */
        public void setPreviousState(String value) {
            this.previousState = value;
        }

        /** Ruft den Wert der nodeType-Eigenschaft ab.
         * 
         * @return possible object is {@link String } */
        public String getNodeType() {
            return nodeType;
        }

        /** Legt den Wert der nodeType-Eigenschaft fest.
         * 
         * @param value allowed object is {@link String } */
        public void setNodeType(String value) {
            this.nodeType = value;
        }

        /** Ruft den Wert der title-Eigenschaft ab.
         * 
         * @return possible object is {@link String } */
        public String getTitle() {
            return title;
        }

        /** Legt den Wert der title-Eigenschaft fest.
         * 
         * @param value allowed object is {@link String } */
        public void setTitle(String value) {
            this.title = value;
        }

        /** Ruft den Wert der seqNumber-Eigenschaft ab.
         * 
         * @return possible object is {@link String } */
        public Integer getSeqNumber() {
            return seqNumber;
        }

        /** Legt den Wert der seqNumber-Eigenschaft fest.
         * 
         * @param value allowed object is {@link String } */
        public void setSeqNumber(Integer value) {
            this.seqNumber = value;
        }

    }

}
