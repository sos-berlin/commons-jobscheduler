package com.sos.VirtualFileSystem.WebDAV;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.httpclient.HttpException;
import org.apache.webdav.lib.WebdavResource;

public class SOSVfsWebDAVOutputStream extends ByteArrayOutputStream {

    private final WebdavResource resource;

    public SOSVfsWebDAVOutputStream(final WebdavResource res) {
        super();
        resource = res;
    }

    public WebdavResource getResource() {
        return resource;
    }

    public void put() throws HttpException, IOException {
        resource.putMethod(this.toByteArray());
        if (resource.exists() == false) {
            throw new HttpException(String.format("%1$s: %2$s", resource.getPath(), resource.getStatusMessage()));
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        resource.close();
    }
}
