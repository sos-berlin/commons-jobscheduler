package com.sos.vfs.webdav.jackrabbit.common;

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
import com.sos.vfs.http.common.SOSHTTPClient;

public class SOSWebDAVOutputStream extends ByteArrayOutputStream {

    private CloseableHttpClient client;
    private URI uri;

    public SOSWebDAVOutputStream(CloseableHttpClient client, URI uri) {
        super();
        this.client = client;
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
        HttpPut p = new HttpPut(uri);

        p.addHeader(HTTP.EXPECT_DIRECTIVE, HTTP.EXPECT_CONTINUE);
        // p.addHeader(HTTP.CONTENT_TYPE,HTTP.DEF_CONTENT_CHARSET.name());
        p.setEntity(new ByteArrayEntity(data));

        try (CloseableHttpResponse response = client.execute(p)) {
            StatusLine sl = response.getStatusLine();
            if (!SOSHTTPClient.isSuccessStatusCode(sl)) {
                throw new Exception(SOSHTTPClient.getResponseStatus(uri, sl));
            }
        }
    }
}
