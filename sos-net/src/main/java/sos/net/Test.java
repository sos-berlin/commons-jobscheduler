package sos.net;

import org.apache.log4j.Logger;

/** @author KB */
public class Test {

    private static final Logger LOGGER = Logger.getLogger(Test.class);

    Test() {
        //
    }

    public static void main(String[] args) throws Exception {
        SOSSFTP objFtp = new SOSSFTP("wilma.sos", 22);
        objFtp.setAuthenticationFilename("C:\\Users\\KB\\kb-openSSH-private.key");
        objFtp.setAuthenticationMethod("publickey");
        objFtp.setUser("kb");
        objFtp.connect();
        LOGGER.info("Connected");
    }

}