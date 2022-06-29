package com.sos.vfs.webdav.webdavclient4j.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.webdav.lib.WebdavResource;

public class SOSWebDAVOutputStream extends ByteArrayOutputStream {

    private final WebdavResource resource;

    public SOSWebDAVOutputStream(final WebdavResource res) {
        super();
        resource = res;
    }
   
    @Override
    public void close() throws IOException {
        try {
            resource.putMethod(this.toByteArray());
            // if (resource.exists() == false) {
            // throw new HttpException(String.format("%1$s: %2$s", resource.getPath(), resource.getStatusMessage()));
            // }
        } finally {
            try {
                super.close();
            } finally {
                resource.close();
            }
        }
    }
}
