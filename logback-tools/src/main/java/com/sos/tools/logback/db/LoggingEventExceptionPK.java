package com.sos.tools.logback.db;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @version 17.01.14 09:40
 * @author stefan.schaedlich@sos-berlin.com
 */
@Embeddable
public class LoggingEventExceptionPK implements Serializable {

    private Long eventId;
    private Long i;

    // do not delete - it is for hibernate
    public LoggingEventExceptionPK() {}

    public LoggingEventExceptionPK(Long eventId, Long i) {
        this.eventId = eventId;
        this.i = i;
    }

    @Column(name="EVENT_ID")
    public Long getEventId() {
        return eventId;
    }

    @Column(name="EVENT_ID")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    @Column(name="I")
    public Long getI() {
        return i;
    }

    @Column(name="I")
    public void setI(Long i) {
        this.i = i;
    }

    public boolean equals(Object o) {
        return ((o instanceof LoggingEventExceptionPK) &&
                eventId == ((LoggingEventExceptionPK)o).eventId &&
                i == ((LoggingEventExceptionPK) o).i);
    }

    public int hashCode() {
        return eventId.hashCode() + i.hashCode();
    }
}
