package com.sos.VirtualFileSystem.Interfaces;

import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionCommandString;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionIntegerArray;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;

/** @author KB */
public interface ISOSShellOptions {

    public abstract SOSOptionCommandString getCommand();

    public abstract void setCommand(SOSOptionCommandString command);

    public abstract SOSOptionRegExp getCommandDelimiter();

    public abstract void setCommandDelimiter(SOSOptionRegExp commandDelimiter);

    public abstract SOSOptionCommandString getCommandScript();

    public abstract void setCommandScript(SOSOptionCommandString commandScript);

    public abstract SOSOptionInFileName getCommandScriptFile();

    public abstract void setCommandScriptFile(SOSOptionInFileName commandScriptFile);

    public abstract SOSOptionString getCommandScriptParam();

    public abstract void setCommandScriptParam(SOSOptionString commandScriptParam);

    public abstract SOSOptionBoolean getIgnoreError();

    public abstract void setIgnoreError(SOSOptionBoolean ignoreError);

    public abstract SOSOptionIntegerArray getIgnoreExitCode();

    public abstract void setIgnoreExitCode(SOSOptionIntegerArray ignoreExitCode);

    public abstract SOSOptionBoolean getIgnoreSignal();

    public abstract void setIgnoreSignal(SOSOptionBoolean ignoreSignal);

    public abstract SOSOptionBoolean getIgnoreStderr();

    public abstract void setIgnoreStderr(SOSOptionBoolean ignoreStderr);

    public abstract SOSOptionBoolean getSimulateShell();

    public abstract void setSimulateShell(SOSOptionBoolean simulateShell);

    public abstract SOSOptionInteger getSimulateShellInactivityTimeout();

    public abstract void setSimulateShellInactivityTimeout(SOSOptionInteger simulateShellInactivityTimeout);

    public abstract SOSOptionInteger getSimulateShellLoginTimeout();

    public abstract void setSimulateShellLoginTimeout(SOSOptionInteger simulateShellLoginTimeout);

    public abstract SOSOptionString getSimulateShellPromptTrigger();

    public abstract void setSimulateShellPromptTrigger(SOSOptionString simulateShellPromptTrigger);

    public abstract SOSOptionBoolean getIgnoreHangupSignal();

    public abstract void setIgnoreHangupSignal(SOSOptionBoolean ignoreHangupSignal);

}