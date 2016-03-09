package com.sos.testframework.h2;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Table(name = "TABLE_2")
public class Table2DBItem {

    private long referenceId;
    private String name;
    private DateTime datefield;

    public Table2DBItem() {
        super();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "REFERENCE_ID")
    public Long getId() {
        return referenceId;
    }

    @Column(name = "REFERENCE_ID")
    public void setId(Long referenceId) {
        this.referenceId = referenceId;
    }

    @Column(name = "FIELD2", nullable = false)
    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "FIELD2", nullable = false)
    public String getName() {
        return name;
    }

    @Column(name = "DATEFIELD", nullable = true)
    @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
    public void setDateField(DateTime dateTime) {
        this.datefield = dateTime;
    }

    @Column(name = "DATEFIELD", nullable = true)
    @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
    public DateTime getDateField() {
        return datefield;
    }

}
