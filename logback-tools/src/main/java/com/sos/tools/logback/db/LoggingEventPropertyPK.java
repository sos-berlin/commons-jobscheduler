package com.sos.tools.logback.db;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/** @version 17.01.14 09:40
 * @author stefan.schaedlich@sos-berlin.com */
@Embeddable
public class LoggingEventPropertyPK implements Serializable {

    private Long eventId;
    private String mappedKey;

    // do not delete - it is for hibernate
    public LoggingEventPropertyPK() {
    }

    public LoggingEventPropertyPK(Long eventId, String mappedKey) {
        this.eventId = eventId;
        this.mappedKey = mappedKey;
    }

    @Column(name = "EVENT_ID")
    public Long getEventId() {
        return eventId;
    }

    @Column(name = "EVENT_ID")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    @Column(name = "MAPPED_KEY")
    public String getMappedKey() {
        return mappedKey;
    }

    @Column(name = "MAPPED_KEY")
    public void setMappedKey(String mappedKey) {
        this.mappedKey = mappedKey;
    }

    public boolean equals(Object o) {
        return ((o instanceof LoggingEventPropertyPK) && eventId == ((LoggingEventPropertyPK) o).eventId && mappedKey == ((LoggingEventPropertyPK) o).mappedKey);
    }

    public int hashCode() {
        return eventId.hashCode() + mappedKey.hashCode();
    }
}
