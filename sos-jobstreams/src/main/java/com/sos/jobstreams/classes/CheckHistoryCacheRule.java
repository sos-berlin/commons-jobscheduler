package com.sos.jobstreams.classes;

public class CheckHistoryCacheRule {

    private String queryString;
    private boolean validateAlways;
    private boolean validateIfFalse;

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString.toLowerCase();
    }

    public boolean isValidateAlways() {
        return validateAlways;
    }

    public void setValidateAlways(boolean validateAlways) {
        this.validateAlways = validateAlways;
    }

    public boolean isValidateIfFalse() {
        return validateIfFalse;
    }

    public void setValidateIfFalse(boolean validateIfFalse) {
        this.validateIfFalse = validateIfFalse;
    }

}
