package com.sos.vfs.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.RequestEntity;

public class SOSHTTPRequestEntity implements RequestEntity {

    private final ByteArrayOutputStream outputStream;
    private final String contentType;

    public SOSHTTPRequestEntity() {
        this(new ByteArrayOutputStream(), null);
    }

    public SOSHTTPRequestEntity(ByteArrayOutputStream baos) {
        this(baos, null);
    }

    public SOSHTTPRequestEntity(ByteArrayOutputStream baos, String contentType) {
        outputStream = baos;
        this.contentType = contentType;
    }

    public boolean isRepeatable() {
        return true;
    }

    public void writeRequest(OutputStream os) throws IOException {
        outputStream.writeTo(os);
    }

    public long getContentLength() {
        return outputStream.size();
    }

    public String getContentType() {
        return contentType;
    }

    public ByteArrayOutputStream getOutputStream() {
        return outputStream;
    }

}
