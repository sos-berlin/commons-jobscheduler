package com.sos.hibernate.classes;

public class SOSSearchFilterData {

    private String searchfield = "";
    private boolean regularExpression = false;
    private boolean wildcardExpression = true;
    private boolean filtered = false;

    public String getSearchfield() {
        return searchfield;
    }

    public void setSearchfield(String searchfield) {
        this.searchfield = searchfield;
    }

    public boolean isRegularExpression() {
        return regularExpression;
    }

    public void setRegularExpression(boolean regularExpression) {
        this.regularExpression = regularExpression;
        this.wildcardExpression = (!regularExpression);
    }

    public boolean isWildcardExpression() {
        return wildcardExpression;
    }

    public void setWildcardExpression(boolean wildcardExpression) {
        this.wildcardExpression = wildcardExpression;
        this.regularExpression = (!wildcardExpression);
    }

    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

}
