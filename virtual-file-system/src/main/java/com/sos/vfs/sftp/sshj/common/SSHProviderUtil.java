package com.sos.vfs.sftp.sshj.common;

import com.sos.keepass.SOSKeePassDatabase;
import com.sos.keepass.SOSKeePassPath;
import com.sos.vfs.common.options.SOSProviderOptions;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import sos.util.SOSString;

public class SSHProviderUtil {

    public static KeyProvider getKeyProviderFromKeepass(SSHClient sshClient, SOSProviderOptions args) throws Exception {
        SOSKeePassDatabase kd = (SOSKeePassDatabase) args.keepass_database.value();
        if (kd == null) {
            throw new Exception("[keepass]keepass_database property is null");
        }
        org.linguafranca.pwdb.Entry<?, ?, ?, ?> ke = (org.linguafranca.pwdb.Entry<?, ?, ?, ?>) args.keepass_database_entry.value();
        if (ke == null) {
            throw new Exception(String.format("[keepass][can't find database entry]attachment property name=%s", args.keepass_attachment_property_name
                    .getValue()));
        }
        try {
            String pk = new String(kd.getHandler().getAttachment(ke, args.keepass_attachment_property_name.getValue()), "UTF-8");
            return sshClient.loadKeys(pk, null, SOSString.isEmpty(args.passphrase.getValue()) ? null : getPasswordFinder(args.passphrase.getValue()));
        } catch (Exception e) {
            String keePassPath = ke.getPath() + SOSKeePassPath.PROPERTY_PREFIX + args.keepass_attachment_property_name.getValue();
            throw new Exception(String.format("[keepass][%s]%s", keePassPath, e.toString()), e);
        }
    }

    public static PasswordFinder getPasswordFinder(String password) {
        return new PasswordFinder() {

            @Override
            public char[] reqPassword(Resource<?> resource) {
                return password.toCharArray().clone();
            }

            @Override
            public boolean shouldRetry(Resource<?> resource) {
                return false;
            }

        };
    }
}
