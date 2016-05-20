package com.sos.VirtualFileSystem.Interfaces;

import com.sos.JSHelper.Options.SOSOptionCommandString;
import com.sos.JSHelper.Options.SOSOptionString;

public interface ISOSCmdShellOptions {

    public SOSOptionCommandString getCommandScriptFile();

    public void setCommandScriptFile(SOSOptionCommandString pCommandScriptFile);

    public SOSOptionString getCommandLineOptions();

    public void setCommandLineOptions(SOSOptionString pCommandLineOptions);

    public SOSOptionString getShellCommand();

    public void setShellCommand(SOSOptionString pShellCommand);

    public SOSOptionString getStartShellCommand();

    public SOSOptionString getShellCommandParameter();

    public void setShellCommandParameter(final SOSOptionString pstrValue);

    public void setStartShellCommand(final SOSOptionString pstrValue);

    public SOSOptionString getOSName();

    public void setOSName(final SOSOptionString pstrValue);

    public SOSOptionString getStartShellCommandParameter();

    public void setStartShellCommandParameter(final SOSOptionString pstrValue);

}