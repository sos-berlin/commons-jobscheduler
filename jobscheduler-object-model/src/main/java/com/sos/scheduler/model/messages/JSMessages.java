/**
 *
 */
package com.sos.scheduler.model.messages;

import com.sos.i18n.annotation.I18NMsg;

/** @author KB */
public class JSMessages {

    /** JSJ_F_107=%1$s - abended with sever errors */
    @I18NMsg
    public final static JSMsg JOM_F_107 = new JSMsg("JOM_F_107");
    @I18NMsg
    public final static JSMsg JOM_I_110 = new JSMsg("JOM_I_110");
    /** %1$s - ended without errors */
    @I18NMsg
    public final static JSMsg JOM_I_111 = new JSMsg("JOM_I_111");

    /** %1$s: Request: %n%2$s */
    @I18NMsg
    public final static JSMsg JOM_D_0010 = new JSMsg("JOM_D_0010");

    /** Command sent using UDP to host '%1$s' at port '%2$d' */
    @I18NMsg
    public final static JSMsg JOM_D_0020 = new JSMsg("JOM_D_0020");

    /** Job '%1$s' is *not* running, state = '%2$s' */
    @I18NMsg
    public final static JSMsg JOM_D_0030 = new JSMsg("JOM_D_0030");

    /** Job '%1$s' is running */
    @I18NMsg
    public final static JSMsg JOM_D_0040 = new JSMsg("JOM_D_0040");

    /** JobScheduler responds an error due to an invalid or wrong command */
    @I18NMsg
    public final static JSMsg JOM_E_0010 = new JSMsg("JOM_E_0010");

    /**
	 *
	 */
    private JSMessages() {
        // TODO Auto-generated constructor stub
    }

}
