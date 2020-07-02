package sos.scheduler.live;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class JobSchedulerMetadataElement {

    public String pkid = "";
    public String parent_id = "";
    public String element_path = "";
    public String element_name = "";
    public String table_name = "";
    public Map<String, String> attributes;
    public String attribute = "";
    public int nesting = 0;
    public List<String> elements = null;

    public JobSchedulerMetadataElement(Map<String, String> rec) {
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
        elements = new ArrayList<String>();
        while (t.hasMoreTokens()) {
            String token = t.nextToken().trim();
            elements.add(token);
        }
    }

    public String fieldnames() {
        String erg = "";
        for (String key : attributes.keySet()) {
            erg += "\"" + key.toUpperCase() + "\",";
        }
        if ("script".equals(element_name.toLowerCase())) {
            erg += "\"CDATA\",";
        }
        if (!erg.isEmpty()) {
            erg = "," + erg.substring(0, erg.length() - 1);
        }
        return erg;
    }

    public String fieldvalues() {
        String erg = "";
        for (String val : attributes.values()) {
            erg += "'" + val + "',";
        }
        if ("script".equals(element_name.toLowerCase())) {
            erg += "'',";
        }
        if (!erg.isEmpty()) {
            erg = "," + erg.substring(0, erg.length() - 1);
        }
        return erg;
    }

    private String getValue(Map<String, String> h, String k) {
        String erg = "";
        try {
            if (h.containsKey(k) && h.get(k) == null) {
                erg = "";
            } else {
                erg = h.get(k);
            }
            return erg;
        } catch (Exception e) {
            return "";
        }
    }

}