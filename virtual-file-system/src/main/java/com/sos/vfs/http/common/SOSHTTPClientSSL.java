package com.sos.vfs.http.common;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

import sos.util.SOSKeyStoreReader;

public class SOSHTTPClientSSL {

    static final TrustStrategy TRUST_ALL_STRATEGY = new TrustStrategy() {

        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            return true;
        }
    };

    private final SOSKeyStoreReader keyStoreReader;
    private final SOSKeyStoreReader trustStoreReader;
    private final boolean checkHostname;
    private final boolean acceptUntrustedCertificate;

    public SOSHTTPClientSSL(SOSKeyStoreReader keyStoreReader, SOSKeyStoreReader trustStoreReader, boolean checkHostname,
            boolean acceptUntrustedCertificate) {
        this.keyStoreReader = keyStoreReader;
        this.trustStoreReader = trustStoreReader;
        this.checkHostname = checkHostname;
        this.acceptUntrustedCertificate = acceptUntrustedCertificate;
    }

    public HostnameVerifier getHostnameVerifier() {
        return checkHostname ? null : NoopHostnameVerifier.INSTANCE;
    }

    public boolean getCheckHostname() {
        return checkHostname;
    }

    public SSLContext getSSLContext() throws Exception {
        if (acceptUntrustedCertificate) {
            SSLContextBuilder builder = SSLContexts.custom();
            builder.loadTrustMaterial(TRUST_ALL_STRATEGY);
            return builder.build();
        }

        if (keyStoreReader != null || trustStoreReader != null) {
            try {
                SSLContextBuilder builder = SSLContexts.custom();
                KeyStore commonKeyStore = null;
                if (keyStoreReader != null && trustStoreReader != null) {
                    if ((keyStoreReader.getPath() == null && trustStoreReader.getPath() == null) || keyStoreReader.getPath().equals(trustStoreReader
                            .getPath())) {
                        commonKeyStore = keyStoreReader.read();
                        builder.loadKeyMaterial(commonKeyStore, keyStoreReader.getPassword());
                        builder.loadTrustMaterial(commonKeyStore, null);
                    }
                }
                if (commonKeyStore == null) {
                    if (keyStoreReader != null) {
                        builder.loadKeyMaterial(keyStoreReader.read(), keyStoreReader.getPassword());
                    }
                    if (trustStoreReader != null) {
                        builder.loadTrustMaterial(trustStoreReader.read(), null);
                    }
                }
                return builder.build();
            } catch (GeneralSecurityException e) {
                throw new Exception(e);
            }
        }
        return null;
    }
}
