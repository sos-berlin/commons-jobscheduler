package com.sos.vfs.sftp.sshj.common;

import java.io.Reader;
import java.io.StringReader;

import com.sos.keepass.SOSKeePassDatabase;
import com.sos.keepass.SOSKeePassPath;
import com.sos.vfs.common.options.SOSProviderOptions;

import net.schmizz.sshj.Config;
import net.schmizz.sshj.common.Factory;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.userauth.keyprovider.FileKeyProvider;
import net.schmizz.sshj.userauth.keyprovider.KeyFormat;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.keyprovider.KeyProviderUtil;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import sos.util.SOSString;

public class SSHProviderUtil {

    public static KeyProvider getKeyProviderFromKeepass(Config config, SOSProviderOptions args) throws Exception {
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
            return getKeyProvider(config, kd.getAttachment(ke, args.keepass_attachment_property_name.getValue()), args.passphrase.getValue());
        } catch (Exception e) {
            String keePassPath = ke.getPath() + SOSKeePassPath.PROPERTY_PREFIX + args.keepass_attachment_property_name.getValue();
            throw new Exception(String.format("[keepass][%s]%s", keePassPath, e.toString()), e);
        }
    }

    public static KeyProvider getKeyProvider(Config config, byte[] privateKey, String passphrase) throws Exception {
        Reader r = null;
        try {
            KeyFormat kf = KeyProviderUtil.detectKeyFileFormat(new StringReader(new String(privateKey, "UTF-8")), false);
            r = new StringReader(new String(privateKey, "UTF-8"));
            FileKeyProvider kp = Factory.Named.Util.create(config.getFileKeyProviderFactories(), kf.toString());
            if (kp == null) {
                throw new SSHException("No provider available for " + kf + " key file");
            }
            kp.init(r, SOSString.isEmpty(passphrase) ? null : getPasswordFinder(passphrase));
            return kp;
        } catch (Throwable e) {
            throw e;
        } finally {
            if (r != null) {
                r.close();
            }
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
