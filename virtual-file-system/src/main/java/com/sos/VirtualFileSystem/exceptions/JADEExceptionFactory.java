/**
 * 
 */
package com.sos.VirtualFileSystem.exceptions;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.enums.JADEExitCodes;

/** @author KB */
public final class JADEExceptionFactory {

    /**
	 * 
	 */
    public JADEExceptionFactory() {
    }

    public static void RaiseDeleteException(final String pstrS) {
        RaiseJadeException(pstrS);
    }

    public static void RaiseJadeException(final String pstrS) {
        JADEException objJ = new JADEException(pstrS);
        objJ.setExitCode(JADEExitCodes.someUnspecificError);
        objJ.setMessage(pstrS);
        throw objJ;
    }

    public static void RaiseJadeException(final JADEExitCodes penuExitCode, final String pstrS, final Exception e) {
        JADEException objJ = createJadeException(penuExitCode, e);
        objJ.setMessage(pstrS);
        throw objJ;
    }

    public static void RaiseJadeException(final JADEExitCodes penuExitCode, final Exception e) {
        throw createJadeException(penuExitCode, e);
    }

    public static JADEException createJadeException(final JADEExitCodes penuExitCode, final Exception e) {
        JADEException objJ;
        if (e instanceof JADEException) {
            objJ = (JADEException) e;
            objJ.setExitCode(penuExitCode);
        } else {
            if (e instanceof JobSchedulerException) {
                objJ = new JADEException(e);
            } else {
                objJ = new JADEException(e);
                objJ.setExitCode(penuExitCode);
            }
        }
        return objJ;
    }

    public static void resetLastErrorMessage() {
        JobSchedulerException.LastErrorMessage = "";
    }
}
