package com.sos.hibernate.exceptions;

import java.nio.file.Path;
import java.util.Optional;

import javax.persistence.PersistenceException;
import javax.xml.stream.XMLStreamException;

/** can occurs if factory.build() method are called */
public class SOSHibernateFactoryBuildException extends SOSHibernateException {

    private static final long serialVersionUID = 1L;

    public SOSHibernateFactoryBuildException(PersistenceException cause) {
        super(cause);
    }

    public SOSHibernateFactoryBuildException(SOSHibernateConfigurationException cause, Optional<Path> file) {
        super("");
        Throwable e = cause;
        while (e != null) {
            if (e instanceof XMLStreamException) {
                XMLStreamException xe = (XMLStreamException) e;
                initCause(xe);
                setMessage(getErrorMessage(xe.getMessage(), file));
                return;
            }
            e = e.getCause();
        }
        setMessage(getErrorMessage(cause.toString(), file));
        initCause(cause);
    }

    private String getErrorMessage(String err, Optional<Path> file) {
        if (file.isPresent()) {
            return String.format("[%s] %s", file.get(), err);
        }
        return err;
    }
}
