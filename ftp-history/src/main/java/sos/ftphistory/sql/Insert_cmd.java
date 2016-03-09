/*
 * $Id: Insert_cmd.java,v 1.2 2003/11/20 16:33:45 jz Exp $ Created on 20.10.2003
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package sos.ftphistory.sql;

import sos.connection.SOSConnection;
import sos.util.SOSLogger;

/** Baut die SQL-Anweisung INSERT zusammen.
 * 
 * @author Joacim Zschimmer */

public class Insert_cmd extends Write_cmd {

    public Insert_cmd(SOSConnection conn_, SOSLogger logger_) {
        super(conn_, logger_);
        ignore_null = true;         // null-Parameter werden ignoriert (macht die
        // Inserts übersichtlicher)
    }

    public Insert_cmd(SOSConnection conn_, SOSLogger logger_, String table_name) {
        this(conn_, logger_);
        set_table_name(table_name);
        ignore_null = true;         // null-Parameter werden ignoriert (macht die
        // Inserts übersichtlicher)
    }

    String make_cmd_() throws Exception {
        return make_insert_cmd_();
    }
}
