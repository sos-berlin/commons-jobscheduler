package com.sos.VirtualFileSystem.Factory;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.VirtualFileSystem.FTPS.SOSVfsFtpS;
import com.sos.VirtualFileSystem.HTTP.SOSVfsHTTP;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.JCIFS.SOSVfsJCIFS;
import com.sos.VirtualFileSystem.JMS.SOSVfsJms;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.VirtualFileSystem.WebDAV.SOSVfsWebDAV;
import com.sos.VirtualFileSystem.common.SOSVfsConstants;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.VirtualFileSystem.local.SOSVfsLocal;
import com.sos.VirtualFileSystem.zip.SOSVfsZip;
import com.sos.i18n.Msg;
import com.sos.i18n.Msg.BundleBaseName;
import com.sos.i18n.annotation.I18NMessage;
import com.sos.i18n.annotation.I18NMessages;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class VFSFactory extends SOSVfsMessageCodes {

    private static Logger logger = Logger.getRootLogger();
    protected static Msg objMsg = new Msg(new BundleBaseName("SOSVirtualFileSystem"));
    private static final String USE_TRILEAD = ".TRILEAD";
    private static final String USE_JSCH = ".JSCH";
    private static SOSConnection2OptionsAlternate objConnectionOptions = null;
    private static boolean useTrilead = true;
    @I18NMessages(value = { @I18NMessage("%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "en_UK", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s liefert eine Instanz der Klasse %2$s", locale = "de", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "es", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "fr", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "it", explanation = "%1$s returns instance of %2$s") }, msgnum = "SOSVfs-D-0201", msgurl = "SOSVfs-D-0201")
    private static String strParentLoggerName = "";
    private static ClassLoader classLoader = null;
    public static String sFTPHandlerClassName = "com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft";

    public VFSFactory() {
        super(SOSVfsConstants.strBundleBaseName);
    }

    public static void setParentLogger(final String pstrParentLoggerName) {
        strParentLoggerName = pstrParentLoggerName;
    }

    public static String getLoggerName() {
        String strT = SOSVfsConstants.strVFSLoggerName;
        if (!strParentLoggerName.isEmpty()) {
            strT = strParentLoggerName + "." + strT;
        }
        return strT;
    }

    public static ISOSVFSHandler getHandler(final SOSOptionTransferType.enuTransferTypes penuTType) throws Exception {
        return getHandler(penuTType.Text());
    }

    public static ISOSVFSHandler getHandler(String pstrWhatURL) throws Exception {
        final String methodName = "VFSFactory::getHandler";
        boolean authenticate = true;
        ISOSVFSHandler objC = null;
        URL objURL = null;
        // Possible Elements of an URL are:
        //
        // http://hans:geheim@www.example.org:80/demo/example.cgi?land=de&stadt=aa#geschichte
        // | | | | | | | |
        // | | | host | url-path searchpart fragment
        // | | password port
        // | user
        // protocol
        if (pstrWhatURL.startsWith("local:")) {
            pstrWhatURL = pstrWhatURL.replace("local:", "file:");
        }
        if (pstrWhatURL.contains(USE_TRILEAD)) {
            useTrilead = true;
            pstrWhatURL = pstrWhatURL.replace(USE_TRILEAD, "");
        } else if (pstrWhatURL.contains(USE_JSCH)) {
            useTrilead = false;
            pstrWhatURL = pstrWhatURL.replace(USE_JSCH, "");
        }
        String strWhatSystem = pstrWhatURL;
        try {
            objURL = new URL(pstrWhatURL);
            strWhatSystem = objURL.getProtocol();
            logger.info(objURL.getFile());
            logger.info(objURL.getPath());
        } catch (MalformedURLException e) {
        }
        classLoader = Thread.currentThread().getContextClassLoader();
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.ssh2.Text())) {
            Class objA;
            if (useTrilead) {
                objA = classLoader.loadClass("com.sos.VirtualFileSystem.SSH.SOSSSH2TriLeadImpl");
            } else {
                objA = classLoader.loadClass("com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft");
            }
            ISOSVFSHandler objD = (ISOSVFSHandler) objA.newInstance();
            logger.trace(SOSVfs_D_0201.params(methodName, objD.toString()));
            if (objD instanceof ISOSVFSHandler) {
                logger.trace("ISOSVFSHandler is part of class   ...  " + objA.toString());
                objC = objD;
            } else {
                logger.error("ISOSVFSHandler not part of class");
            }
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.ftp.Text())) {
            objC = getDynamicVFSHandler("com.sos.VirtualFileSystem.FTP.SOSVfsFtp");
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.ftps.Text())) {
            objC = new SOSVfsFtpS();
            logger.trace(SOSVfs_D_0201.params(methodName, SOSVfsFtpS.class.toString()));
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.sftp.Text())) {
            objC = getDynamicVFSHandler(sFTPHandlerClassName);
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.local.Text())
                || strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.file.Text())) {
            objC = new SOSVfsLocal();
            logger.trace(SOSVfs_D_0201.params(methodName, SOSVfsLocal.class.toString()));
            authenticate = false;
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.zip.Text())) {
            objC = new SOSVfsZip();
            logger.trace(SOSVfs_D_0201.params(methodName, SOSVfsZip.class.toString()));
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.webdav.Text())) {
            objC = new SOSVfsWebDAV();
            logger.trace(SOSVfs_D_0201.params(methodName, SOSVfsWebDAV.class.toString()));
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.http.Text())) {
            objC = new SOSVfsHTTP();
            logger.trace(SOSVfs_D_0201.params(methodName, SOSVfsHTTP.class.toString()));
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.smb.Text())) {
            objC = new SOSVfsJCIFS();
            logger.trace(SOSVfs_D_0201.params(methodName, SOSVfsJCIFS.class.toString()));
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.mq.Text())) {
            objC = new SOSVfsJms();
            logger.trace(SOSVfs_D_0201.params(methodName, SOSVfsJms.class.toString()));
        }
        if (objC == null) {
            throw new Exception(SOSVfs_E_0203.params(strWhatSystem));
        }
        if (objURL != null && authenticate) {
            String strHost = objURL.getHost();
            if (strHost != null) {
                int intPort = objURL.getPort();
                objC.Connect(strHost, intPort);
                ISOSAuthenticationOptions objAO = new SOSFTPOptions();
                String strUserInfo = objURL.getUserInfo();
                // JITL-145: can contain password, therefore shouldn't be logged
                // logger.info("User-Info = " + strUserInfo);
                String[] strUI = strUserInfo.split(":");
                objAO.getUser().Value(strUI[0]);
                objAO.getPassword().Value("");
                if (strUI.length > 1) {
                    objAO.getPassword().Value(strUI[1]);
                }
                objC.Authenticate(objAO);
                objAO = null;
                logger.info("objURL.getAuthority() : " + objURL.getAuthority());
                logger.info("objURL.getFile()" + objURL.getFile());
            }
        }
        return objC;
    }

    public static void setConnectionOptions(final SOSConnection2OptionsAlternate pobjConnectionOptions) {
        objConnectionOptions = pobjConnectionOptions;
    }

    /** Load a specified Class of type ISOSVFSHandlerInterface for the logical
     * data provider
     *
     * @param pstrLoadClassNameDefault
     * @return */
    private static ISOSVFSHandler getDynamicVFSHandler(final String pstrLoadClassNameDefault) {
        String strLoadClassName = pstrLoadClassNameDefault;
        if (objConnectionOptions != null && objConnectionOptions.loadClassName.isDirty()) {
            strLoadClassName = objConnectionOptions.loadClassName.Value();
            if (strLoadClassName.isEmpty()) {
                strLoadClassName = pstrLoadClassNameDefault;
            } else {
                logger.trace(String.format("loadClassName changed from '%1$s' to '%2$s'", pstrLoadClassNameDefault, strLoadClassName));
            }
        }
        ISOSVFSHandler objC = null;
        try {
            Class objA = null;
            if (objConnectionOptions != null && objConnectionOptions.javaClassPath.isDirty()) {
                String[] strJars = objConnectionOptions.javaClassPath.Value().split(";");
                for (String strJarFileName : strJars) {
                    File objF = new File(strJarFileName);
                    if (objF.isFile() && objF.canExecute()) {
                        addJarsToClassPath(Thread.currentThread().getContextClassLoader(), new File[] { objF });
                    } else {
                        throw new JobSchedulerException(String.format("ClasspathElement '%1$s' not found or not accessible", strJarFileName));
                    }
                }
            }
            objA = classLoader.loadClass(strLoadClassName);
            objC = (ISOSVFSHandler) objA.newInstance();
            if (objC instanceof ISOSVFSHandler) {
                logger.trace("ISOSVFSHandler is part of class   ...  " + objA.toString());
            } else {
                logger.error("ISOSVFSHandler not part of class" + objA.toString());
            }
        } catch (Exception e) {
            throw new JobSchedulerException(String.format("Class with Name '%1$s' not found and not loaded", strLoadClassName), e);
        }
        return objC;
    }

    private static void addJarsToClassPath(final ClassLoader classLoader1, final File[] jars) {
        if (classLoader1 instanceof URLClassLoader) {
            try {
                Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
                if (null != addUrlMethod) {
                    addUrlMethod.setAccessible(true);
                    for (File jar : jars) {
                        try {
                            addUrlMethod.invoke(classLoader1, jar.toURI().toURL());
                        } catch (Exception e) {
                            logger.error(e.getLocalizedMessage());
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        }
    }

}
