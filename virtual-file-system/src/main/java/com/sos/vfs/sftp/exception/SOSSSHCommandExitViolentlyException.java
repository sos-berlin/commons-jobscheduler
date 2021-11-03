package com.sos.vfs.sftp.exception;

import com.sos.vfs.exception.SOSProviderException;

import net.schmizz.sshj.connection.channel.direct.Signal;

public class SOSSSHCommandExitViolentlyException extends SOSProviderException {

    private static final long serialVersionUID = 1L;
    private final Signal signal;

    public SOSSSHCommandExitViolentlyException(Signal signal, String msg) {
        super(String.format("[%s]%s", signal, msg));
        this.signal = signal;
    }

    public Signal getSignal() {
        return signal;
    }
}
