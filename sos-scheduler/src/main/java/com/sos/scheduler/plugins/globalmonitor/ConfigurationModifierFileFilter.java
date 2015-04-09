package com.sos.scheduler.plugins.globalmonitor;

import java.io.File;
import java.io.FilenameFilter;

public abstract class ConfigurationModifierFileFilter implements FilenameFilter {
    private ConfigurationModifierFileSelectorOptions options;


    abstract public String getXmlRegex(); 
    
    public ConfigurationModifierFileFilter(ConfigurationModifierFileSelectorOptions options) {
        super();
        this.options = options;
    }


    private boolean isConfiguationFile(String filename){
        boolean isJobSchedulerElement = filename.matches(getXmlRegex());
        return (isJobSchedulerElement);
    }

    
    private boolean matchRegex(String filename){
        boolean isMatch = options.getRegexSelector().trim().length() == 0 || filename.matches(options.getRegexSelector());
        return (isMatch);
    }

    @Override
    public boolean accept(File directory, String filename) {
        File f = new File(directory,filename);
        
          if (f.isDirectory()){
              return !options.isDirExclusion(directory);
          }
           
          if (options.isFileExclusions(f)){
              return false;
          }else{
              if (isConfiguationFile(filename) && matchRegex(filename)){
                  return true;
              }else{
                  return false;
              }
          }
    }

}
