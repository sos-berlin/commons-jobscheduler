package com.sos.vfs.webdav.jackrabbit.common;

import java.io.FilterInputStream;
import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;

public class SOSWebDAVInputStream extends FilterInputStream {

    private CloseableHttpResponse response;

    public SOSWebDAVInputStream(final CloseableHttpResponse response) throws IOException {
        super(response.getEntity().getContent());
        this.response = response;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Throwable e) {

                }
            }
        }
    }

}
