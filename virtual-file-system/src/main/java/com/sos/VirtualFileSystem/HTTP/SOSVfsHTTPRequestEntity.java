package com.sos.VirtualFileSystem.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.RequestEntity;

public class SOSVfsHTTPRequestEntity implements RequestEntity {

    private final ByteArrayOutputStream outputStream;
    private final String contentType;

    public SOSVfsHTTPRequestEntity() {
        this(new ByteArrayOutputStream(), null);
    }

    public SOSVfsHTTPRequestEntity(ByteArrayOutputStream baos) {
        this(baos, null);
    }

    public SOSVfsHTTPRequestEntity(ByteArrayOutputStream baos, String contentType) {
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
