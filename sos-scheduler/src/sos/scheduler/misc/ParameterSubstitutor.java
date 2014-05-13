package sos.scheduler.misc;

import java.util.HashMap;

import org.apache.commons.lang3.text.StrSubstitutor;

public class ParameterSubstitutor {
private  HashMap<String,String> keylist;

    public void addKey(String k,String v){
	     if (keylist == null){
		     keylist = new HashMap<String,String>();
		 }
		 keylist.put(k,v);
	}

    public String replaceEnvVars(String source){
        StrSubstitutor strSubstitutor = new StrSubstitutor(System.getenv());
        return strSubstitutor.replace(source);
    }
    
    public String replaceSystemProperties(String source){
        return StrSubstitutor.replaceSystemProperties(source);
    }
    
    public String replace(final String source) {
        StrSubstitutor strSubstitutor = new StrSubstitutor(keylist);
        return strSubstitutor.replace(source);
    }
}