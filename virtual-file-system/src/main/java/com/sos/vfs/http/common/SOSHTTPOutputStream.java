package com.sos.vfs.http.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HTTP;

public class SOSHTTPOutputStream extends ByteArrayOutputStream {

    private CloseableHttpClient client;
    private HttpPut request;

    public SOSHTTPOutputStream(CloseableHttpClient client, HttpPut request) {
        super();
        this.client = client;
        this.request = request;
    }

    @Override
    public void close() throws IOException {
        try {
            executePutMethod(this.toByteArray());
        } finally {
            super.close();
        }
    }

    private boolean executePutMethod(byte[] data) throws IOException {
        request.addHeader(HTTP.EXPECT_DIRECTIVE, HTTP.EXPECT_CONTINUE);
        // p.addHeader(HTTP.CONTENT_TYPE,HTTP.DEF_CONTENT_CHARSET.name());
        request.setEntity(new ByteArrayEntity(data));

        try (CloseableHttpResponse response = client.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            int status = statusLine.getStatusCode();
            if (status / 100 != 2) {
                return false;
            }
        }
        return true;
    }
}
