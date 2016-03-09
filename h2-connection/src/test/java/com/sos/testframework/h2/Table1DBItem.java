package com.sos.testframework.h2;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Table(name = "TABLE_1")
public class Table1DBItem {

    private long id;
    private long referenceId;
    private String name;
    private DateTime datefield;
    private Table2DBItem table2Item;

    public Table1DBItem() {
        super();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    @Column(name = "ID")
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "REFERENCE_ID")
    public Long getReferenceId() {
        return referenceId;
    }

    @Column(name = "REFERENCE_ID")
    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    @ManyToOne(optional = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "`REFERENCE_ID`", insertable = false, updatable = false)
    public Table2DBItem getTable2DBItem() {
        return table2Item;
    }

    public void setTable2DBItem(Table2DBItem item) {
        this.table2Item = item;
    }

    @Column(name = "FIELD1", nullable = false)
    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "FIELD1", nullable = false)
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

    @Transient
    public String getTransientData() {
        return "transient";
    }
}
