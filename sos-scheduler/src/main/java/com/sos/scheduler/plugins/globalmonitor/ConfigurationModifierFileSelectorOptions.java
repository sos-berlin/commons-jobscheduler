package com.sos.scheduler.plugins.globalmonitor;

import java.io.File;
import java.util.ArrayList;

public class ConfigurationModifierFileSelectorOptions {
    
    private String configurationDirectory;
    private String regexSelector;
    private String fileExclusions=""; 
    private String directoryExclusions=""; 
    private boolean recursive;
    private ArrayList<String> listOfFileExclusions;
    private ArrayList<String> listOfDirectoryExclusions;
    
    public String getConfigurationDirectory() {
        return configurationDirectory;
    }
    
    public void setConfigurationDirectory(String directory) {
        this.configurationDirectory = directory;
    }
    public String getRegexSelector() {
        return regexSelector;
    }
    public void setRegexSelector(String regexSelector) {
        this.regexSelector = regexSelector;
    }
    public String getfileExclusions() {
        return fileExclusions;
    }
    
    private void fillExclusionsList(){
        listOfFileExclusions = new ArrayList<String>();
        listOfDirectoryExclusions = new ArrayList<String>();
        
        String[] filenames = fileExclusions.split(",");
        for (int i=0;i<filenames.length;i++){
            listOfFileExclusions.add(filenames[i]);
        }
        String[] dirnames = directoryExclusions.split(",");
        for (int i=0;i<dirnames.length;i++){
            listOfDirectoryExclusions.add(dirnames[i]);
        }
    }
    
    public void setFileExclusions(String fileExclusions_) {
        this.fileExclusions = fileExclusions_.replace('\\','/');
        fillExclusionsList();
    }
    
    public boolean isRecursive() {
        return recursive;
    }
    
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public void setRecursive(String recursive) {
        this.recursive = recursive.equalsIgnoreCase("true");
    }

    public void setDirectoryExclusions(String directoryExclusions) {
        this.directoryExclusions = directoryExclusions.replace('\\','/');
        fillExclusionsList();
    }
 
    public boolean isFileExclusions(File file){
        if (listOfFileExclusions == null){
            return false;
        }
        
        String s = file.getAbsolutePath().replace('\\','/');
        s = s.replaceAll("^.*/live/(.*)\\.(job|monitor)\\.xml$","$1");
        
        for(String exclusion:listOfFileExclusions)  {
            if (s.equals(exclusion)){
                return true;
            }
        }
        return false;    
    }
    
    public boolean isDirExclusion(File directory){
        if (listOfDirectoryExclusions == null){
            return false;
        }
        for(String exclusion:listOfDirectoryExclusions)  {
            if (directory.getAbsolutePath().replace('\\', '/').endsWith(exclusion)){
                return true;
            }
        }
        return false;
    }
}
