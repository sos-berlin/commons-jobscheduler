package com.sos.vfs.http.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HTTP;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSHTTPOutputStream extends ByteArrayOutputStream {

    private CloseableHttpClient client;
    private HttpPut request;
    private URI uri;

    public SOSHTTPOutputStream(CloseableHttpClient client, HttpPut request, URI uri) {
        super();
        this.client = client;
        this.request = request;
        this.uri = uri;
    }

    @Override
    public void close() throws IOException {
        try {
            executePutMethod(this.toByteArray());
        } catch (Throwable e) {
            // throw run time exception
            throw new JobSchedulerException(e);
        } finally {
            super.close();
        }
    }

    private void executePutMethod(byte[] data) throws Exception {
        request.addHeader(HTTP.EXPECT_DIRECTIVE, HTTP.EXPECT_CONTINUE);
        // p.addHeader(HTTP.CONTENT_TYPE,HTTP.DEF_CONTENT_CHARSET.name());
        request.setEntity(new ByteArrayEntity(data));

        try (CloseableHttpResponse response = client.execute(request)) {
            StatusLine sl = response.getStatusLine();
            if (!SOSHTTPClient.isSuccessStatusCode(sl)) {
                throw new Exception(SOSHTTPClient.getResponseStatus(uri, sl));
            }
        }
    }
}
