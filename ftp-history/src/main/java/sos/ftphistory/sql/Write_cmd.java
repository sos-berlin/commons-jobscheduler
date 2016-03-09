/*
 * $Id: Write_cmd.java,v 1.9 2004/04/01 14:14:27 jz Exp $ Created on 20.10.2003
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

package sos.ftphistory.sql;

import java.util.*;
import java.sql.*;
import java.text.*;

import sos.connection.SOSConnection;
import sos.util.SOSLogger;

/** Baut die SQL-Anweisung INSERT oder UPDATE zusammen.
 * 
 * Oberklasse für {@link Insert_cmd} und {@link Update_cmd}.
 * 
 * @author Joacim Zschimmer */

public abstract class Write_cmd extends Cmd {

    class Field_value {

        public Field_value(String f, Object v, char type) {
            field_name = f;
            value = v;
            this.type = type;
        }

        public String sql_value() {
            switch (type) {
            case type_string:
                return quoted(value.toString());
            case type_direct:
                return value.toString();
            case type_datetime:
                SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                date_format.setTimeZone(new SimpleTimeZone(0, "GMT"));
                return "{ts'" + date_format.format((Timestamp) value) + "'}";
            case type_time:
                return "{t'" + value.toString().replaceAll("'", "''") + "'}";
            default:
                throw new RuntimeException("Stmt.sql_value type=" + type);
            }

        }

        static final char type_string = 'S';
        static final char type_direct = 'I';
        static final char type_datetime = 'D';
        static final char type_time = 't';

        String field_name;
        Object value;
        char type;            // S: String, N: Numerisch, T: Datetime
    }

    public List field_value_list;
    boolean ignore_null = false;

    Write_cmd(SOSConnection conn_, SOSLogger logger_) {
        super(conn_, logger_);
        field_value_list = new ArrayList(50);
    }

    private void set(String field_name, Object value, char type) {
        if (value == null) {
            if (!ignore_null)
                set_direct(field_name, "NULL");       // Das ist rekursiv, aber dann
            // gilt value != null
        } else {
            Field_value f = new Field_value(field_name, value, type);

            for (int i = 0; i < field_value_list.size(); i++) {
                if (((Field_value) field_value_list.get(i)).field_name.equalsIgnoreCase(field_name)) {
                    field_value_list.set(i, f);
                    return;
                }
            }

            field_value_list.add(f);
        }
    }

    // ------------------------------------------------------------------------------------------set
    /** Setzt ein Feld.
     * 
     * Wenn das Feld bereits gesetzt worden ist, wird es überschrieben.
     * 
     * @param field_name
     * @param value Timestamp wird erkannt. Sonst wird toString() gerufen. */

    public void set(String field_name, boolean value) {
        set_num(field_name, value ? 1 : 0);
    }

    public void set(String field_name, Timestamp value) {
        set(field_name, value, Field_value.type_datetime);
    }

    public void set(String field_name, String value) {
        set(field_name, value, Field_value.type_string);
    }

    public void setNull(String field_name, String value) {
        if (value.length() == 0) {
            value = "NULL";
        }
        set(field_name, value, Field_value.type_string);
    }

    /** Schneidet den Wert rechts ab, wenn er länger als field_size ist.
     * 
     * @param field_name
     * @param value
     * @param field_size */

    public void set_truncate(String field_name, String value, int field_size) {
        if (value == null || value.length() <= field_size) {
            set(field_name, value);
        } else {
            set(field_name, value.substring(0, field_size));
        }
    }

    public void set_direct(String field_name, String value) {
        set(field_name, value, Field_value.type_direct);
    }

    public void set_num(String field_name, Object value) {
        set(field_name, value, Field_value.type_direct);
    }

    public void set_numNull(String field_name, String value) {
        if (value.length() == 0) {
            value = "NULL";
        }
        set(field_name, value, Field_value.type_direct);

    }

    public void set_num(String field_name, long value) {
        set(field_name, Long.toString(value), Field_value.type_direct);
    }

    public void set_later(String field_name) {
        set_direct(field_name, "?");
    }

    public void set_datetime(String field_name, Timestamp value) {
        set(field_name, value, Field_value.type_datetime);
    }

    public boolean empty() {
        // return field_value_map.isEmpty();
        return field_value_list.isEmpty();
    }

    String make_insert_cmd() throws Exception {
        return conn.normalizeStatement(make_insert_cmd_());
    }

    String make_insert_cmd_() throws Exception {
        String names = new String();
        String values = new String();
        String q = "";

        if (withQuote) {
            q = "\"";
        }

        for (int i = 0; i < field_value_list.size(); i++) {
            Field_value fv = (Field_value) field_value_list.get(i);

            if (names.length() > 0) {
                names += ',';
                values += ',';
            }

            names += q + fv.field_name + q;
            values += fv.sql_value();
        }

        return "INSERT INTO " + table_name + " (" + names + ") VALUES (" + values + ")";
    }

}
