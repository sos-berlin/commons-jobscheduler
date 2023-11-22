package com.sos.vfs.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.entity.InputStreamEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.vfs.common.SOSCommonProvider;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.SOSFileEntry.EntryType;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSProviderOptions;
import com.sos.vfs.http.common.SOSHTTPClient;
import com.sos.vfs.http.common.SOSHTTPInputStream;
import com.sos.vfs.http.common.SOSHTTPOutputStream;

import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSHTTP extends SOSCommonProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHTTP.class);

    private SOSHTTPClient client;

    private HashMap<String, Long> fileSizes = null;
    private LinkedHashMap<String, String> headers;

    public SOSHTTP() {
        super();
        fileSizes = new HashMap<String, Long>();
    }

    @Override
    public void connect(final SOSProviderOptions options) throws Exception {
        super.connect(options);

        client = new SOSHTTPClient(options, false);

        host = client.getBaseURI().getHost();
        port = client.getPort();
        setHeaders();

        checkConnection();

        LOGGER.info(SOSVfs_D_0102.params(host, port));
        this.logReply();
    }

    @Override
    public void disconnect() {
        reply = "disconnect OK";

        fileSizes = new HashMap<>();
        headers = new LinkedHashMap<>();

        if (client != null) {
            try {
                client.close();
                client = null;
            } catch (Throwable ex) {
                reply = "disconnect: " + ex;
            }
        }
        LOGGER.info(reply);
    }

    @Override
    public boolean isConnected() {
        return client != null;
    }

    private void checkConnection() throws Exception {
        URI uri = client.getBaseURI();
        HttpGet request = new HttpGet(uri);
        setHttpHeaders(request);

        try (CloseableHttpResponse response = client.execute(request)) {
            checkConnectResponse(uri, response);
        } catch (Throwable e) {
            throwException(request, e);
        }
    }

    @Override
    public void mkdir(final String pathname) throws IOException {
        LOGGER.debug("[mkdir][not implemented yet]" + pathname);
    }

    @Override
    public void rmdir(final String pathname) throws IOException {
        LOGGER.info("[rmdir][not implemented yet]" + pathname);
    }

    @Override
    public boolean isDirectory(final String path) {
        boolean isDirectory = false;
        try {
            // http://test.sos:9080/my_file.txt/
            URI uri = client.normalizeURI(path.endsWith("/") ? path : (path + "/"));

            HttpTrace request = new HttpTrace(uri);
            setHttpHeaders(request);

            try (CloseableHttpResponse response = client.execute(request)) {
                isDirectory = response.getStatusLine().getStatusCode() <= 400;
            }
        } catch (Throwable e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[" + path + "]" + e.toString(), e);
            }
        }
        return isDirectory;
    }

    @Override
    public SOSFileEntry getFileEntry(String path) throws Exception {
        long size = size(path);
        if (size < 0) {
            return null;
        }

        String fileName = getBaseNameFromPath(path);
        SOSFileEntry entry = new SOSFileEntry(EntryType.HTTP);
        entry.setDirectory(false);
        entry.setFilename(fileName);
        // e.g. for HTTP(s) transfers with the file names like SET-217?filter=13400
        entry.setNormalizedFilename(URLEncoder.encode(fileName, "UTF-8"));
        entry.setFilesize(size);
        // entry.setParentPath(getFullParentFromPath(path));
        // http://test.sos:9080/my_file.txt
        entry.setParentPath(client.getRelativeDirectoryPath(client.normalizeURI(path)));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[getFileEntry=%s]%s", path, SOSString.toString(entry)));
        }
        return entry;
    }

    @Override
    public List<SOSFileEntry> listNames(String path, int maxFiles, boolean checkIfExists, boolean checkIfIsDirectory) throws IOException {
        if (path.isEmpty()) {
            path = "/";
        }
        reply = "ls OK";
        return new ArrayList<SOSFileEntry>();// TODO
    }

    @Override
    public long size(final String path) throws Exception {
        if (fileSizes.containsKey(path)) {
            return fileSizes.get(path);
        }
        long size = -1;

        URI uri = client.normalizeURI(path);
        HttpGet request = new HttpGet(uri);
        setHttpHeaders(request);

        try (CloseableHttpResponse response = client.execute(request)) {
            StatusLine sl = response.getStatusLine();
            boolean success = SOSHTTPClient.isSuccessStatusCode(sl);
            if (success) {
                HttpEntity en = response.getEntity();
                size = en.getContentLength();
                if (size < 0) {// e.g. Transfer-Encoding: chunked
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("[%s][size=%s]use getInputStreamLen", uri, size));
                    }
                    size = getInputStreamLen(en.getContent());
                }
            } else {
                if (sl.getStatusCode() != 404) {
                    throw new Exception(SOSHTTPClient.getResponseStatus(uri, sl));
                }
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][size=%s]%s", uri, size, SOSString.toString(sl)));
            }
        } catch (Throwable e) {
            throwException(request, e);
        } finally {
            fileSizes.put(path, size);
        }
        return size;
    }

    private long getInputStreamLen(InputStream is) throws Exception {
        long total = 0;
        try {
            int readed = 0;
            byte[] buffer = new byte[1024];
            while ((readed = is.read(buffer)) != -1) {
                total += readed;
            }
        } catch (Throwable ex) {
            throw ex;
        } finally {
            try {
                is.close();
            } catch (Throwable exx) {
                //
            }
        }
        return total;
    }

    @Override
    public void delete(String path, boolean checkIsDirectory) {
        if (checkIsDirectory && this.isDirectory(path)) {
            throw new JobSchedulerException(SOSVfs_E_186.params(path));
        }

        URI uri;
        try {
            uri = client.normalizeURI(path);
        } catch (Throwable e) {
            throw new JobSchedulerException(String.format("[%s]%s", path, e.toString()), e);
        }
        HttpDelete request = new HttpDelete(uri);
        setHttpHeaders(request);

        try (CloseableHttpResponse response = client.execute(request)) {
            StatusLine sl = response.getStatusLine();
            if (!SOSHTTPClient.isSuccessStatusCode(sl)) {
                throw new Exception(SOSHTTPClient.getResponseStatus(uri, sl));
            }
        } catch (Throwable e) {
            reply = e.toString();
            throwJobSchedulerException(request, e);
        }
        reply = "rm OK";
        LOGGER.info(getHostID(SOSVfs_D_181.params("delete", uri, getReplyString())));
    }

    @Override
    public void rename(String from, String to) {
        URI uriTo;
        try {
            uriTo = client.normalizeURI(to);
        } catch (Throwable e) {
            throw new JobSchedulerException(String.format("[to=%s]%s", to, e.toString()), e);
        }

        InputStream is = null;

        try {
            is = getInputStream(from);

            HttpPut requestTo = new HttpPut(uriTo);
            setHttpHeaders(requestTo);
            requestTo.setEntity(new InputStreamEntity(is));
            try (CloseableHttpResponse response = client.execute(requestTo)) {
                try {
                    is.close();
                    is = null;
                } catch (Throwable ex) {
                }
                StatusLine sl = response.getStatusLine();
                if (!SOSHTTPClient.isSuccessStatusCode(sl)) {
                    throw new Exception(SOSHTTPClient.getResponseStatus(uriTo, sl));
                }
                delete(from, false);
            } catch (Throwable e) {
                reply = e.toString();
                throwJobSchedulerException(requestTo, e);
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable e) {
                }
            }
        }
        reply = "mv OK";
        LOGGER.info(getHostID(SOSVfs_I_189.params(from, to, getReplyString())));
    }

    @Override
    public ISOSProviderFile getFile(String fileName) {
        String fn = "";
        try {
            fn = adjustFileSeparator(fileName);
            if (!client.isAbsolute(fn)) {
                String tfn = fileName.startsWith("/") ? fn : "/" + fn;
                if (client.getBaseURIPath().endsWith(tfn + "/")) {// workaround host=https://<host>/myfile.txt and /myfile.txt
                    fn = client.getBaseURIPath().substring(0, client.getBaseURIPath().length() - 1);
                } else {
                    fn = (client.getBaseURIPath() + fn).replaceAll("//+", "/");
                }
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[getFile][%s]%s", fileName, fn));
            }

            ISOSProviderFile file = new SOSHTTPFile(fn);
            file.setProvider(this);

            return file;
        } catch (Throwable e) {
            LOGGER.error(String.format("[getFile][%s][%s]%s", fileName, fn, e.toString()), e);
            return null;
        }
    }

    @Override
    public boolean fileExists(final String path) {
        try {
            return size(path) > -1;
        } catch (Throwable e) {
            return false;
        }

    }

    @Override
    public boolean directoryExists(final String path) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]directoryExists", path));
        }
        return isDirectory(path);
    }

    private void setHeaders() throws IOException {
        if (!SOSString.isEmpty(getProviderOptions().http_headers.getValue())) {
            headers = readHeaders(getProviderOptions().http_headers.getValue());
            LOGGER.info(String.format("[HTTPHeaders]%s", StringUtils.stripEnd(StringUtils.stripStart(headers.toString(), "{"), "}")));
        }
    }

    @Override
    public OutputStream getOutputStream(final String path, boolean append, boolean resume) {
        try {
            URI uri = client.normalizeURI(path);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[getOutputStream][path=%s]uri=%s", path, uri));
            }
            HttpPut request = new HttpPut(uri);
            setHttpHeaders(request);

            return new SOSHTTPOutputStream(client.getClient(), request, uri);
        } catch (Throwable ex) {
            throw new JobSchedulerException(SOSVfs_E_193.params("getOutputStream()", path), ex);
        }
    }

    @Override
    public InputStream getInputStream(String path) {
        try {
            URI uri = client.normalizeURI(path);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[getInputStream][path=%s]uri=%s", path, uri));
            }

            CloseableHttpResponse response = null;
            try {
                HttpGet request = new HttpGet(uri);
                setHttpHeaders(request);

                response = client.execute(request);

                StatusLine sl = response.getStatusLine();
                if (!SOSHTTPClient.isSuccessStatusCode(sl)) {
                    throw new Exception(SOSHTTPClient.getResponseStatus(uri, sl));
                }
                return new SOSHTTPInputStream(response);
            } catch (Throwable e) {
                if (response != null) {
                    try {
                        response.close();
                    } catch (Throwable ee) {
                    }
                }
                throw e;
            }
        } catch (Throwable ex) {
            throw new JobSchedulerException(SOSVfs_E_193.params("getInputStream()", path), ex);
        }
    }

    private void checkConnectResponse(URI uri, CloseableHttpResponse response) throws Exception {
        int sc = SOSHTTPClient.checkConnectResponse(uri, response);
        if (sc == 404) {
            client.setBaseUriOnNotExists();
        }
    }

    private void setHttpHeaders(HttpRequestBase request) {
        if (headers != null && headers.size() > 0) {
            headers.forEach((k, v) -> {
                request.addHeader(k.toString(), v.toString());
            });
        }
    }

    private LinkedHashMap<String, String> readHeaders(final String val) throws IOException {
        final LinkedHashMap<String, String> m = new LinkedHashMap<>();
        // see JadeXml2IniConverter DELIMITER_MERGED_CHILDS_HEADERS
        Stream.of(val.split("\\|")).forEach(e -> {
            // https://www.rfc-editor.org/rfc/rfc7230#section-3.2.4
            // No whitespace is allowed between the header field-name and colon.
            String header = e.trim();
            int p = header.indexOf(" ");
            if (p == -1) {
                m.put(header, "");
            } else {
                String name = header.substring(0, p);
                String value = header.substring(p);
                m.put(name, value.trim());
            }
        });
        return m;
    }

    private void throwException(HttpRequestBase request, Throwable e) throws Exception {
        throw new Exception(String.format("[%s]%s", request.getURI(), e.toString()), e);
    }

    private void throwJobSchedulerException(HttpRequestBase request, Throwable e) throws JobSchedulerException {
        throw new JobSchedulerException(String.format("[%s]%s", request.getURI(), e.toString()), e);
    }

    @Override
    public boolean isHTTP() {
        return true;
    }

}