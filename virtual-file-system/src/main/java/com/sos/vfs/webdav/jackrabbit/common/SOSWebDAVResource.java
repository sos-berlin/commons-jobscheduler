package com.sos.vfs.webdav.jackrabbit.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.http.HttpStatus;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.w3c.dom.Node;

import sos.util.SOSDate;

public class SOSWebDAVResource {

    private String href;
    private String name;
    private Date lastModified;
    private boolean isDirectory;
    private long size;

    public SOSWebDAVResource(MultiStatusResponse response) {
        this(response, false);
    }

    public SOSWebDAVResource(MultiStatusResponse response, boolean onlyLastModified) {
        isDirectory = false;
        size = 0;

        DavPropertySet found = response.getProperties(HttpStatus.SC_OK);// 200
        if (onlyLastModified) {
            lastModified = getLastModified(found);
        } else {
            href = getHref(response);
            name = getName(href);
            isDirectory = isDirectory(found);
            if (!isDirectory) {
                size = getSize(found);
            }
        }
    }

    private Date getLastModified(DavPropertySet set) {
        Date result = null;
        DavProperty<?> p = set.get(DavConstants.PROPERTY_GETLASTMODIFIED);
        if (p != null) {
            try {
                result = DateUtil.parseDate(p.getValue().toString());
            } catch (Throwable e) {
            }
        }
        return result;
    }

    private String getHref(MultiStatusResponse response) {
        try {
            return URLDecoder.decode(response.getHref(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return response.getHref();
        }
    }

    private boolean isDirectory(DavPropertySet set) {
        DavProperty<?> p = set.get(DavConstants.PROPERTY_RESOURCETYPE);
        if (p != null) {
            Node node = (Node) p.getValue();
            if (node != null && node.getLocalName() != null) {
                return node.getLocalName().equals(DavConstants.XML_COLLECTION);
            }
        }
        return false;
    }

    private long getSize(DavPropertySet set) {
        DavProperty<?> p = set.get(DavConstants.PROPERTY_GETCONTENTLENGTH);
        return p == null ? 0 : Long.parseLong(p.getValue().toString());
    }

    private String getName(final String href) {
        if (href.equals("/")) {
            return "";
        }
        String path = href;
        if (href.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        final int i = path.lastIndexOf("/");
        return ((i >= 0) ? path.substring(i + 1) : path);
    }

    public String getHref() {
        return href;
    }

    public String getName() {
        return name;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public String getLastModifiedAsString() {
        try {
            return SOSDate.getDateTimeAsString(lastModified, "yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            return null;
        }
    }

    public long getLastModifiedAsLong() {
        return lastModified == null ? -1 : lastModified.getTime();
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public long getSize() {
        return size;
    }

}
