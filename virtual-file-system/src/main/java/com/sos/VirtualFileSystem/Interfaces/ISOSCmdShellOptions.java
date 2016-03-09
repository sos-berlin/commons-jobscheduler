package com.sos.VirtualFileSystem.Interfaces;

import com.sos.JSHelper.Options.SOSOptionCommandString;
import com.sos.JSHelper.Options.SOSOptionString;

public interface ISOSCmdShellOptions {

    /** \brief getcommand_script_file : Script file name to Execute The va
     *
     * \details The value of this parameter contains the file-name (and
     * path-name, if needed) of a local (script-)file, which will be transferred
     * to the remote host and will then be executed there. The script can access
     * job- and order-parameters by environment variables. The names of the
     * environment variables are in upper case and have the string
     * "SCHEDULER_PARAM_" as a prefix. Order parameters with the same name
     * overwrite task parameters. This parameter can be used as an alternative
     * to command , command_delimiter and command_script .
     *
     * \return Script file name to Execute The va */
    public SOSOptionCommandString getcommand_script_file();

    /** \brief setcommand_script_file : Script file name to Execute The va
     *
     * \details The value of this parameter contains the file-name (and
     * path-name, if needed) of a local (script-)file, which will be transferred
     * to the remote host and will then be executed there. The script can access
     * job- and order-parameters by environment variables. The names of the
     * environment variables are in upper case and have the string
     * "SCHEDULER_PARAM_" as a prefix. Order parameters with the same name
     * overwrite task parameters. This parameter can be used as an alternative
     * to command , command_delimiter and command_script .
     *
     * @param command_script_file : Script file name to Execute The va */
    public void setcommand_script_file(SOSOptionCommandString p_command_script_file);

    /** \brief getCommand_Line_options : Command_Line_options
     *
     * \details
     *
     *
     * \return Command_Line_options */
    public SOSOptionString getCommand_Line_options();

    /** \brief setCommand_Line_options : Command_Line_options
     *
     * \details
     *
     *
     * @param CommandLineOptions : Command_Line_options */
    public void setCommand_Line_options(SOSOptionString p_Command_Line_options);

    /** \brief getshell_command :
     *
     * \details
     *
     *
     * \return */
    public SOSOptionString getshell_command();

    /** \brief setshell_command :
     *
     * \details
     *
     *
     * @param shell_command : */
    public void setshell_command(SOSOptionString p_shell_command);

    public SOSOptionString getStart_Shell_command();

    public SOSOptionString getShell_command_Parameter();

    public void setShell_command_Parameter(final SOSOptionString pstrValue);

    public void setStart_Shell_command(final SOSOptionString pstrValue);

    public SOSOptionString getOS_Name();

    public void setOS_Name(final SOSOptionString pstrValue);

    public SOSOptionString getStart_Shell_command_Parameter();

    public void setStart_Shell_command_Parameter(final SOSOptionString pstrValue);

}