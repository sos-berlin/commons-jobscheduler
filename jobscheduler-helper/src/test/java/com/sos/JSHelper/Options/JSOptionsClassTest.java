package com.sos.JSHelper.Options;

import java.util.HashMap;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author KB */
public class JSOptionsClassTest {
    
      
   
    @Test
    public final void testSettings4StepName() throws Exception {
        JSOptionsClass objOC = new JSOptionsClass();
        HashMap<String, String> objHsh = new HashMap<String, String>();
        objHsh.put("operation", "copy");
        objHsh.put("source/source_host", "wilma.sos");
        objHsh.put("source/source_user", "sos");
        objHsh.put("source/source_port", "22");
        objHsh.put("source/source_protocol", "sftp");
        objHsh.put("source/source_password", "sos");
        objHsh.put("source/source_dir", "/home/sos/setup.scheduler/releases");
        objHsh.put("source/source_ssh_auth_method", "password");
        objHsh.put("test/target_host", "tux.sos");
        objHsh.put("test/target_protocol", "sftp");
        objHsh.put("test/target_port", "22");
        objHsh.put("test/target_password", "sos");
        objHsh.put("test/target_user", "sos");
        objHsh.put("test/target_dir", "/srv/www/htdocs/test");
        objHsh.put("test/target_ssh_auth_method", "password");
        objHsh.put("overwrite_files", "true");
        objHsh.put("check_size", "true");
        objHsh.put("file_spec", "^scheduler_(win32|linux)_joe\\.[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]{4}\\.(tar\\.gz|zip)$");
        objHsh.put("recursive", "false");
        objHsh.put("verbose", "9");
        objHsh.put("buffer_size", "32000");
        objHsh.put("SendTransferHistory", "false");
        String strCmd = "SITE chmod 777 $SourceFileName";
        objHsh.put("source_pre_command", strCmd);
        objHsh.put("target_pre_command", strCmd);
        objOC.setCurrentNodeName("test");
        objOC.setAllOptions(objHsh);
        
        
    }

    
}