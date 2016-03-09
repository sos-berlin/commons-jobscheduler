package com.sos.jobscheduler.tools.webservices.globals;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "myAnswer")
public class MyWebserviceAnswer {

    private String name;
    private String email;
    private String telephone;

    public MyWebserviceAnswer() {
    }

    public MyWebserviceAnswer(String name) {
        this.name = name;
    }

    @XmlAttribute
    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    @XmlAttribute
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }

    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return new StringBuffer(" name: ").append(this.name).append(" email: ").append(this.email).append(" tel : ").append(this.telephone).toString();
    }
}