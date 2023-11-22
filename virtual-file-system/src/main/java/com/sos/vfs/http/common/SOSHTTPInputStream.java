package com.sos.vfs.http.common;

import java.io.FilterInputStream;
import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;

public class SOSHTTPInputStream extends FilterInputStream {

    private CloseableHttpResponse response;

    public SOSHTTPInputStream(final CloseableHttpResponse response) throws IOException {
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
