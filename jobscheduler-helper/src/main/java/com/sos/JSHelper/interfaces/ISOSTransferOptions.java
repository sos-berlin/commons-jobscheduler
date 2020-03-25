package com.sos.JSHelper.interfaces;

import java.util.HashMap;

import com.sos.JSHelper.Options.JSJobChain;
import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionCommandScript;
import com.sos.JSHelper.Options.SOSOptionCommandScriptFile;
import com.sos.JSHelper.Options.SOSOptionFileName;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionJadeOperation;
import com.sos.JSHelper.Options.SOSOptionLogFileName;
import com.sos.JSHelper.Options.SOSOptionOutFileName;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionProcessID;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTime;
import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionUserName;

public interface ISOSTransferOptions {

    public abstract SOSOptionString getAccount();

    public abstract void setAccount(SOSOptionString pAccount);

    public abstract SOSOptionString getAlternativeAccount();

    public abstract void setAlternativeAccount(SOSOptionString pAlternativeAccount);

    public abstract SOSOptionHostName getAlternativeHost();

    public abstract void setAlternativeHost(SOSOptionHostName pAlternativeHost);

    public abstract SOSOptionString getAlternativePassiveMode();

    public abstract void setAlternativePassiveMode(SOSOptionString pAlternativePassiveMode);

    public abstract SOSOptionPassword getAlternativePassword();

    public abstract void setAlternativePassword(SOSOptionPassword pAlternativePassword);

    public abstract SOSOptionPortNumber getAlternativePort();

    public abstract void setAlternativePort(SOSOptionPortNumber pAlternativePort);

    public abstract SOSOptionString getAlternativeRemoteDir();

    public abstract void setAlternativeRemoteDir(SOSOptionString pAlternativeRemoteDir);

    public abstract SOSOptionString getAlternativeTransferMode();

    public abstract void setAlternativeTransferMode(SOSOptionString pAlternativeTransferMode);

    public abstract SOSOptionUserName getAlternativeUser();

    public abstract void setAlternativeUser(SOSOptionUserName pAlternativeUser);

    public abstract SOSOptionBoolean getAppendFiles();

    public abstract void setAppendFiles(SOSOptionBoolean pAppendFiles);

    public abstract SOSOptionString getAtomicPrefix();

    public abstract void setAtomicPrefix(SOSOptionString pAtomicPrefix);

    public abstract SOSOptionString getAtomicSuffix();

    public abstract void setAtomicSuffix(SOSOptionString pAtomicSuffix);

    public abstract SOSOptionInFileName getBannerFooter();

    public abstract void setBannerFooter(SOSOptionInFileName pBannerFooter);

    public abstract SOSOptionInFileName getBannerHeader();

    public abstract void setBannerHeader(SOSOptionInFileName pBannerHeader);

    public abstract SOSOptionInteger getCheckInterval();

    public abstract void setCheckInterval(SOSOptionInteger pCheckInterval);

    public abstract SOSOptionInteger getCheckRetry();

    public abstract void setCheckRetry(SOSOptionInteger pCheckRetry);

    public abstract SOSOptionFolderName getClasspathBase();

    public abstract void setClasspathBase(SOSOptionFolderName pClasspathBase);

    public abstract SOSOptionProcessID getCurrentPid();

    public abstract void setCurrentPid(SOSOptionProcessID pCurrentPid);

    public abstract SOSOptionFileName getFilePath();

    public abstract void setFilePath(SOSOptionFileName pFilePath);

    public abstract SOSOptionRegExp getFileSpec();

    public abstract void setFileSpec(SOSOptionRegExp pFileSpec);

    public abstract SOSOptionRegExp getFileSpec2();

    public abstract void setFileSpec2(SOSOptionRegExp pFileSpec2);

    public abstract SOSOptionBoolean getForceFiles();

    public abstract void setForceFiles(SOSOptionBoolean pForceFiles);

    public abstract SOSOptionOutFileName getHistory();

    public abstract void setHistory(SOSOptionOutFileName pHistory);

    public abstract SOSOptionInteger getHistoryRepeat();

    public abstract void setHistoryRepeat(SOSOptionInteger pHistoryRepeat);

    public abstract SOSOptionInteger getHistoryRepeatInterval();

    public abstract void setHistoryRepeatInterval(SOSOptionInteger pHistoryRepeatInterval);

    public abstract SOSOptionHostName getHost();

    public abstract void setHost(SOSOptionHostName pHost);

    public abstract SOSOptionString getHttpProxyHost();

    public abstract void setHttpProxyHost(SOSOptionString pHttpProxyHost);

    public abstract SOSOptionString getHttpProxyPort();

    public abstract void setHttpProxyPort(SOSOptionString pHttpProxyPort);

    public abstract SOSOptionString getJumpCommand();

    public abstract void setJumpCommand(SOSOptionString pJumpCommand);

    public abstract SOSOptionString getJumpCommandDelimiter();

    public abstract void setJumpCommandDelimiter(SOSOptionString pJumpCommandDelimiter);

    public abstract SOSOptionCommandScript getJumpCommandScript();

    public abstract void setJumpCommandScript(SOSOptionCommandScript pJumpCommandScript);

    public abstract SOSOptionCommandScriptFile getJumpCommandScriptFile();

    public abstract void setJumpCommandScriptFile(SOSOptionCommandScriptFile pJumpCommandScriptFile);

    public abstract SOSOptionHostName getJumpHost();

    public abstract void setJumpHost(SOSOptionHostName pJumpHost);

    public abstract SOSOptionBoolean getJumpIgnoreError();

    public abstract void setJumpIgnoreError(SOSOptionBoolean pJumpIgnoreError);

    public abstract SOSOptionBoolean getJumpIgnoreSignal();

    public abstract void setJumpIgnoreSignal(SOSOptionBoolean pJumpIgnoreSignal);

    public abstract SOSOptionBoolean getJumpIgnoreStderr();

    public abstract void setJumpIgnoreStderr(SOSOptionBoolean pJumpIgnoreStderr);

    public abstract SOSOptionPassword getJumpPassword();

    public abstract void setJumpPassword(SOSOptionPassword pJumpPassword);

    public abstract SOSOptionPortNumber getJumpPort();

    public abstract void setJumpPort(SOSOptionPortNumber pJumpPort);

    public abstract SOSOptionString getJumpProtocol();

    public abstract void setJumpProtocol(SOSOptionString pJumpProtocol);

    public abstract SOSOptionString getJumpProxyHost();

    public abstract void setJumpProxyHost(SOSOptionString pJumpProxyHost);

    public abstract SOSOptionString getJumpProxyPassword();

    public abstract void setJumpProxyPassword(SOSOptionString pJumpProxyPassword);

    public abstract SOSOptionString getJumpProxyPort();

    public abstract void setJumpProxyPort(SOSOptionString pJumpProxyPort);

    public abstract SOSOptionUserName getJumpProxyUser();

    public abstract void setJumpProxyUser(SOSOptionUserName pJumpProxyUser);

    public abstract SOSOptionBoolean getJumpSimulateShell();

    public abstract void setJumpSimulateShell(SOSOptionBoolean pJumpSimulateShell);

    public abstract SOSOptionInteger getJumpSimulateShellInactivityTimeout();

    public abstract void setJumpSimulateShellInactivityTimeout(SOSOptionInteger pJumpSimulateShellInactivityTimeout);

    public abstract SOSOptionInteger getJumpSimulateShellLoginTimeout();

    public abstract void setJumpSimulateShellLoginTimeout(SOSOptionInteger pJumpSimulateShellLoginTimeout);

    public abstract SOSOptionString getJumpSimulateShellPromptTrigger();

    public abstract void setJumpSimulateShellPromptTrigger(SOSOptionString pJumpSimulateShellPromptTrigger);

    public abstract SOSOptionInFileName getJumpSshAuthFile();

    public abstract void setJumpSshAuthFile(SOSOptionInFileName pJumpSshAuthFile);

    public abstract SOSOptionAuthenticationMethod getJumpSshAuthMethod();

    public abstract void setJumpSshAuthMethod(SOSOptionAuthenticationMethod pJumpSshAuthMethod);

    public abstract SOSOptionUserName getJumpUser();

    public abstract void setJumpUser(SOSOptionUserName pJumpUser);

    public abstract SOSOptionFolderName getLocalDir();

    public abstract SOSOptionFolderName sourceDir();

    public abstract SOSOptionFolderName targetDir();

    public abstract void setLocalDir(SOSOptionFolderName pLocalDir);

    public abstract SOSOptionLogFileName getLogFilename();

    public abstract void setLogFilename(SOSOptionLogFileName pLogFilename);

    public abstract SOSOptionString getMandator();

    public abstract void setMandator(SOSOptionString pMandator);

    public abstract SOSOptionJadeOperation getOperation();

    public abstract void setOperation(SOSOptionJadeOperation pOperation);

    public abstract SOSOptionBoolean getOverwriteFiles();

    public abstract void setOverwriteFiles(SOSOptionBoolean pOverwriteFiles);

    public abstract SOSOptionBoolean getPassiveMode();

    public abstract void setPassiveMode(SOSOptionBoolean pPassiveMode);

    public abstract SOSOptionPassword getPassword();

    public abstract void setPassword(SOSOptionPassword pPassword);

    public abstract SOSOptionTime getPollInterval();

    public abstract void setPollInterval(SOSOptionTime pPollInterval);

    public abstract SOSOptionInteger getPollMinfiles();

    public abstract void setPollMinfiles(SOSOptionInteger pPollMinfiles);

    public abstract SOSOptionInteger getPollTimeout();

    public abstract void setPollTimeout(SOSOptionInteger pPollTimeout);

    public abstract SOSOptionPortNumber getPort();

    public abstract void setPort(SOSOptionPortNumber pPort);

    public abstract SOSOptionProcessID getPpid();

    public abstract void setPpid(SOSOptionProcessID pPpid);

    public abstract SOSOptionString getProfile();

    public abstract void setProfile(SOSOptionString pProfile);

    public abstract SOSOptionTransferType getProtocol();

    public abstract void setProtocol(SOSOptionTransferType pProtocol);

    public abstract SOSOptionBoolean getRecursive();

    public abstract void setRecursive(SOSOptionBoolean pRecursive);

    public abstract SOSOptionFolderName getRemoteDir();

    public abstract void setRemoteDir(SOSOptionFolderName pRemoteDir);

    public abstract SOSOptionBoolean getRemoveFiles();

    public abstract void setRemoveFiles(SOSOptionBoolean pRemoveFiles);

    public abstract SOSOptionString getReplacement();

    public abstract void setReplacement(SOSOptionString pReplacement);

    public abstract SOSOptionRegExp getReplacing();

    public abstract void setReplacing(SOSOptionRegExp pReplacing);

    public abstract SOSOptionFolderName getRoot();

    public abstract void setRoot(SOSOptionFolderName pRoot);

    public abstract SOSOptionHostName getSchedulerHost();

    public abstract void setSchedulerHost(SOSOptionHostName pSchedulerHost);

    public abstract JSJobChain getSchedulerJobChain();

    public abstract void setSchedulerJobChain(JSJobChain pSchedulerJobChain);

    public abstract SOSOptionPortNumber getSchedulerPort();

    public abstract void setSchedulerPort(SOSOptionPortNumber pSchedulerPort);

    public abstract SOSOptionBoolean getSkipTransfer();

    public abstract void setSkipTransfer(SOSOptionBoolean pSkipTransfer);

    public abstract SOSOptionInFileName getSshAuthFile();

    public abstract void setSshAuthFile(SOSOptionInFileName pSshAuthFile);

    public abstract SOSOptionAuthenticationMethod getSshAuthMethod();

    public abstract void setSshAuthMethod(SOSOptionAuthenticationMethod pSshAuthMethod);

    public abstract SOSOptionString getSshProxyHost();

    public abstract void setSshProxyHost(SOSOptionString pSshProxyHost);

    public abstract SOSOptionString getSshProxyPassword();

    public abstract void setSshProxyPassword(SOSOptionString pSshProxyPassword);

    public abstract SOSOptionString getSshProxyPort();

    public abstract void setSshProxyPort(SOSOptionString pSshProxyPort);

    public abstract SOSOptionString getSshProxyUser();

    public abstract void setSshProxyUser(SOSOptionString pSshProxyUser);

    public abstract SOSOptionBoolean getTransactional();

    public abstract void setTransactional(SOSOptionBoolean pTransactional);

    public abstract SOSOptionTransferMode getTransferMode();

    public abstract void setTransferMode(SOSOptionTransferMode pTransferMode);

    public abstract SOSOptionUserName getUser();

    public abstract void setUser(SOSOptionUserName pUser);

    public abstract SOSOptionInteger getVerbose();

    public abstract void setVerbose(SOSOptionInteger pVerbose);

    public abstract void setAllOptions(HashMap<String, String> pobjJSSettings) throws Exception;

    public abstract void checkMandatory() throws com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;

    public abstract void commandLineArgs(String[] pstrArgs) throws Exception;

    public abstract SOSOptionString getProxyHost();

    public abstract SOSOptionPassword getProxyPassword();

    public abstract SOSOptionPortNumber getProxyPort();

    public abstract SOSOptionUserName getProxyUser();

    public abstract void setProxyHost(SOSOptionString proxyHost);

    public abstract void setProxyPassword(SOSOptionPassword proxyPassword);

    public abstract void setProxyPort(SOSOptionPortNumber proxyPort);

    public abstract void setProxyUser(SOSOptionUserName proxyUser);

    public abstract SOSOptionInFileName getAuthFile();

    public abstract SOSOptionAuthenticationMethod getAuthMethod();

    public abstract void setAuthFile(SOSOptionInFileName authFile);

    public abstract void setAuthMethod(SOSOptionAuthenticationMethod authMethod);

    public abstract boolean isAtomicTransfer();

}