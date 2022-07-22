package com.sos.vfs.sftp.sshj;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod.enuAuthenticationMethods;
import com.sos.JSHelper.Options.SOSOptionTransferType.TransferTypes;
import com.sos.credentialstore.options.SOSCredentialStoreOptions;
import com.sos.vfs.common.options.SOSProviderOptions;

public class SOSSFTPSSHJTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSFTPSSHJTest.class);

    @Ignore
    @Test
    public void testExecuteMethods() {
        SOSSFTPSSHJ p = new SOSSFTPSSHJ();
        try {
            SOSProviderOptions o = new SOSProviderOptions();
            o.protocol.setValue(TransferTypes.sftp);
            o.host.setValue("localhost");
            o.port.setValue("22");
            o.authMethod.setValue(enuAuthenticationMethods.password);

            o.user.setValue("user");
            o.password.setValue("password");

            p.connect(o);

            p.mkdir("/home/sos/yade/test/a/b/c");
            p.rmdir("/home/sos/yade/test/a");

        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            p.disconnect();
        }
    }

    @Ignore
    @Test
    public void testPassword() {
        SOSProviderOptions po = new SOSProviderOptions();
        po.host.setValue("localhost");
        po.port.setValue("22");
        po.authMethod.setValue(enuAuthenticationMethods.password);

        po.user.setValue("test");
        po.password.setValue("test");

        connect(po);
    }

    @Ignore
    @Test
    public void testPublicKey() {
        SOSProviderOptions po = new SOSProviderOptions();
        po.host.setValue("localhost");
        po.port.setValue("22");
        po.authMethod.setValue(enuAuthenticationMethods.publicKey);

        po.user.setValue("test");
        po.authFile.setValue("/tmp/localhost_id_ed25519.ppk");

        connect(po);
    }

    @Ignore
    @Test
    public void testPublicKeyFromCredentialStore() {
        SOSProviderOptions po = new SOSProviderOptions();
        po.host.setValue("localhost");
        po.port.setValue("22");
        po.authMethod.setValue(enuAuthenticationMethods.publicKey);
        po.user.setValue("cs://@user");
        po.authFile.setValue("cs://@attachment");

        SOSCredentialStoreOptions cso = new SOSCredentialStoreOptions();
        cso.useCredentialStore.value(true);
        cso.credentialStoreFileName.setValue("/tmp/kdbx-p.kdbx");
        cso.credentialStorePassword.setValue("test");
        cso.credentialStoreKeyPath.setValue("/server/SFTP/localhost");

        po.setCredentialStore(cso);
        po.checkCredentialStoreOptions();

        connect(po);
    }

    private void connect(SOSProviderOptions po) {
        SOSSFTPSSHJ ssh = new SOSSFTPSSHJ();
        try {
            ssh.connect(po);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            ssh.disconnect();
        }
    }
}
