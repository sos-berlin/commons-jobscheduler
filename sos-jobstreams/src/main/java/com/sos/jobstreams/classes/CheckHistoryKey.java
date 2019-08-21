package com.sos.jobstreams.classes;

 
public class CheckHistoryKey {
    private String type;
    private String name;
    private String query;
    
    
    public CheckHistoryKey(String type, String name, String query) {
        super();
        this.type = type;
        this.name = name;
        this.query = query;
    }

    public String getName() {
        return name;
    }
    
    public void setJob(String name) {
        this.name = name;
    }
    
    public String getQuery() {
        return query.toLowerCase();
    }
    
    public void setQuery(String query) {
        this.query = query.toLowerCase();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof CheckHistoryKey) {
            CheckHistoryKey checkHistoryKey = (CheckHistoryKey) obj;
            return name.equals(checkHistoryKey.name) && query.equals(checkHistoryKey.query);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (name + "." + query).hashCode();
    }

    
    public String getType() {
        return type;
    }
}
