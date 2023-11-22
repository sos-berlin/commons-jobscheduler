package com.sos.vfs.sftp;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod.enuAuthenticationMethods;
import com.sos.JSHelper.Options.SOSOptionTransferType.TransferTypes;
import com.sos.credentialstore.options.SOSCredentialStoreOptions;
import com.sos.vfs.common.Buffer;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSBaseOptions;
import com.sos.vfs.common.options.SOSProviderOptions;
import com.sos.vfs.local.SOSLocal;

import sos.util.SOSString;

public class SOSSFTPTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSFTPTest.class);

    private SOSBaseOptions baseOptions;
    private SOSProviderOptions providerOptions;

    private SOSBaseOptions getBaseOptions() {
        if (baseOptions == null) {
            baseOptions = new SOSBaseOptions();
            baseOptions.ssh_provider.setValue(SOSSFTP.SSHProvider.SSHJ.name());
        }
        return baseOptions;
    }

    private SOSProviderOptions getProviderOptions() {
        if (providerOptions == null) {
            providerOptions = new SOSProviderOptions();
            providerOptions.protocol.setValue(TransferTypes.sftp);

            providerOptions.host.setValue("localhost");

            usePassword();
            // usePublicKey();
            // usePublicKeyFromCredentialStore();
        }
        return providerOptions;
    }

    private void usePassword() {
        providerOptions.authMethod.setValue(enuAuthenticationMethods.password);
        providerOptions.user.setValue("sos");
        providerOptions.password.setValue("sos");
    }

    @SuppressWarnings("unused")
    private void usePublicKey() {
        providerOptions.authMethod.setValue(enuAuthenticationMethods.publicKey);
        providerOptions.user.setValue("sos");
        providerOptions.authFile.setValue("/tmp/localhost_id_ed25519.ppk");
    }

    @SuppressWarnings("unused")
    private void usePublicKeyFromCredentialStore() {
        providerOptions.authMethod.setValue(enuAuthenticationMethods.publicKey);
        providerOptions.user.setValue("cs://@user");
        providerOptions.authFile.setValue("cs://@attachment");

        SOSCredentialStoreOptions cso = new SOSCredentialStoreOptions();
        cso.useCredentialStore.value(true);
        cso.credentialStoreFileName.setValue("/tmp/kdbx-p.kdbx");
        cso.credentialStorePassword.setValue("test");
        cso.credentialStoreKeyPath.setValue("/server/SFTP/localhost");

        providerOptions.setCredentialStore(cso);
        providerOptions.checkCredentialStoreOptions();
    }

    @Ignore
    @Test
    public void testInfoMethods() {
        SOSSFTP p = new SOSSFTP(getBaseOptions().ssh_provider);
        p.setBaseOptions(getBaseOptions());
        try {
            p.connect(getProviderOptions());

            List<SOSFileEntry> r = p.listNames("/sos/yade/", -1, true, true);
            LOGGER.info(p.getReplyString());
            if (r != null) {
                LOGGER.info("found=" + r.size());
                for (SOSFileEntry e : r) {
                    LOGGER.info("[listNames][entry]" + SOSString.toString(e));
                    String path = e.getFullPath();
                    LOGGER.info("  isDirecory=" + p.isDirectory(path) + ", directoryExists=" + p.directoryExists(path));
                    LOGGER.info("  fileExists=" + p.fileExists(path) + ",  size=" + p.size(path));
                    LOGGER.info("  getModificationDateTime=" + p.getModificationDateTime(path));

                    if (!e.isDirectory()) {
                        SOSFileEntry f = p.getFileEntry(e.getFullPath());
                        LOGGER.info("    [getFileEntry]" + SOSString.toString(f));
                    }
                }
            }

        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            p.disconnect();
        }
    }

    @Ignore
    @Test
    public void testConfigFiles() {
        SOSSFTP p = new SOSSFTP(getBaseOptions().ssh_provider);
        p.setBaseOptions(getBaseOptions());
        try {
            getProviderOptions().configuration_files.setValue("src/test/resources/ssh/sshj.config");
            p.connect(getProviderOptions());
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            p.disconnect();
        }
    }

    @Ignore
    @Test
    public void testExecuteMethods() {
        SOSSFTP p = new SOSSFTP(getBaseOptions().ssh_provider);
        p.setBaseOptions(getBaseOptions());
        try {
            p.connect(getProviderOptions());

            p.mkdir("/sos/yade/test/a/b/c");
            p.rmdir("/sos/yade/test/a");

            // p.delete("/sos/yade/1.txt", false);
            // p.rename("/sos/yade/22.txt", "/sos/yade/test/22.txt");
            // p.rename("/sos/yade/test/22.txt", "/sos/yade/22.txt");
            // p.rename("/sos/yade/22.txt", "/sos/yade/3.txt");
            // p.setModificationTimeStamp("/sos/yade/22.txt", Instant.now().toEpochMilli());

        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            p.disconnect();
        }
    }

    @Ignore
    @Test
    public void testInputStream() {
        SOSSFTP p = new SOSSFTP(getBaseOptions().ssh_provider);
        p.setBaseOptions(getBaseOptions());

        SOSLocal l = new SOSLocal();
        l.setBaseOptions(getBaseOptions());

        ISOSProviderFile sourceFile = null;
        ISOSProviderFile targetFile = null;
        try {
            p.connect(getProviderOptions());

            sourceFile = p.getFile("/sos/yade/source.txt");
            targetFile = l.getFile("/tmp/target.txt");

            byte[] buffer = new byte[getBaseOptions().bufferSize.value()];
            int bytesTransferred;
            while ((bytesTransferred = sourceFile.read(buffer)) != -1) {
                Buffer buf = new Buffer();
                buf.setBytes(buffer);
                buf.setLength(bytesTransferred);

                targetFile.write(buf.getBytes(), 0, buf.getLength());
            }

        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            if (sourceFile != null) {
                sourceFile.closeInput();
            }
            if (targetFile != null) {
                targetFile.closeOutput();
            }
            p.disconnect();
        }
    }

    @Ignore
    @Test
    public void testOutputStream() {
        // getBaseOptions().appendFiles.setValue("true");

        SOSSFTP p = new SOSSFTP(getBaseOptions().ssh_provider);
        p.setBaseOptions(getBaseOptions());

        SOSLocal l = new SOSLocal();
        l.setBaseOptions(getBaseOptions());

        ISOSProviderFile sourceFile = null;
        ISOSProviderFile targetFile = null;
        try {
            p.connect(getProviderOptions());

            sourceFile = l.getFile("/tmp/source.txt");
            targetFile = p.getFile("/sos/yade/target.txt");
            targetFile.setModeAppend(getBaseOptions().appendFiles.value());
            targetFile.setModeRestart(getBaseOptions().resumeTransfer.value());

            byte[] buffer = new byte[getBaseOptions().bufferSize.value()];
            int bytesTransferred;
            while ((bytesTransferred = sourceFile.read(buffer)) != -1) {
                Buffer buf = new Buffer();
                buf.setBytes(buffer);
                buf.setLength(bytesTransferred);

                targetFile.write(buf.getBytes(), 0, buf.getLength());
            }

        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            if (sourceFile != null) {
                sourceFile.closeInput();
            }
            if (targetFile != null) {
                targetFile.closeOutput();
            }
            p.disconnect();
        }
    }

}
