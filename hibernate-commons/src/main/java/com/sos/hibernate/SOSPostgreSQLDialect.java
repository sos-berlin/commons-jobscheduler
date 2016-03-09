package com.sos.hibernate;

import java.sql.Types;

import org.hibernate.dialect.PostgreSQLDialect;

public class SOSPostgreSQLDialect extends PostgreSQLDialect {

    public SOSPostgreSQLDialect() {
        super();

        registerColumnType(Types.BLOB, "bytea");
    }
}