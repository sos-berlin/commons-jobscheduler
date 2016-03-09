/*
 * $Id: Update_cmd.java,v 1.5 2004/01/23 09:19:42 jz Exp $ Created on 20.10.2003
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

package sos.ftphistory.sql;

/** @author joacim */

// import java.sql.*;
import java.util.ArrayList;

import sos.connection.SOSConnection;
import sos.ftphistory.sql.Write_cmd.Field_value;
import sos.util.SOSLogger;

public class Update_cmd extends Write_cmd {

    public boolean single = false;

    String where;

    // -----------------------------------------------------------------------------------Update_cmd

    public Update_cmd(SOSConnection conn_, SOSLogger logger_) {
        super(conn_, logger_);
    }

    // -----------------------------------------------------------------------------------Update_cmd

    public Update_cmd(SOSConnection conn_, SOSLogger logger_, String table_name) {
        this(conn_, logger_);
        set_table_name(table_name);
    }

    // ------------------------------------------------------------------------------------set_where
    /** Setzt die Bedingung der Where-Klausel. */
    public void set_where(String where_condition) {
        where = where_condition;
    }

    // ------------------------------------------------------------------------------------make_cmd_
    /*
     * (non-Javadoc)
     * @see sos.dod.orderprepare.sql.Cmd#make_stmt()
     */

    String make_cmd_() throws Exception {
        String settings = new String();
        String q = "";

        if (withQuote) {
            q = "\"";
        }

        for (int i = 0; i < field_value_list.size(); i++) {
            Field_value fv = (Field_value) field_value_list.get(i);

            if (settings.length() > 0)
                settings += ", ";
            settings += q + fv.field_name + q + "=" + fv.sql_value();
        }

        return conn.normalizeStatement("UPDATE " + table_name + " SET " + settings + " WHERE " + where);
    }

    public void copyFieldsFrom(Write_cmd cmd) {
        this.field_value_list = new ArrayList(50);
        for (int i = 0; i < cmd.field_value_list.size(); i++) {
            this.field_value_list.add(cmd.field_value_list.get(i));
        }
    }

}
