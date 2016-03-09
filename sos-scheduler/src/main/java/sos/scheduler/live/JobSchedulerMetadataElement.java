package sos.scheduler.live;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

public class JobSchedulerMetadataElement {

    public String pkid = "";
    public String parent_id = "";
    public String element_path = "";
    public String element_name = "";
    public String table_name = "";
    public HashMap attributes;
    public String attribute = "";
    public int nesting = 0;
    public ArrayList elements = null;

    public JobSchedulerMetadataElement(HashMap rec) {
        super();
        pkid = getValue(rec, "pk_id");
        parent_id = getValue(rec, "parent_id");
        try {
            nesting = Integer.parseInt(getValue(rec, "nesting"));
        } catch (NumberFormatException n) {
            nesting = 0;
        }
        element_name = getValue(rec, "element_name");
        table_name = getValue(rec, "table_name");
        element_path = getValue(rec, "element_path");
        StringTokenizer t = new StringTokenizer(element_path, "/");
        elements = new ArrayList();
        while (t.hasMoreTokens()) {
            String token = t.nextToken().trim();
            elements.add(token);
        }
    }

    public String fieldnames() {
        String erg = "";

        Iterator i = attributes.keySet().iterator();
        while (i.hasNext()) {
            erg += "\"" + i.next().toString().toUpperCase() + "\",";
        }

        if (element_name.toLowerCase().equals("script")) {
            erg += "\"CDATA\",";
        }

        if (erg.length() > 0)
            erg = "," + erg.substring(0, erg.length() - 1);

        return erg;
    }

    public String fieldvalues() {
        String erg = "";
        Iterator i = attributes.values().iterator();
        while (i.hasNext()) {
            erg += "'" + i.next().toString() + "',";
        }

        if (element_name.toLowerCase().equals("script")) {
            erg += "'',";
        }

        if (erg.length() > 0)
            erg = "," + erg.substring(0, erg.length() - 1);
        return erg;
    }

    private String getValue(HashMap h, String k) {
        String erg = "";
        try {
            if (h.containsKey(k) && h.get(k) == null) {
                erg = "";
            } else {
                erg = h.get(k).toString();
            }
            return erg;
        } catch (Exception e) {
            return "";
        }
    }

}
