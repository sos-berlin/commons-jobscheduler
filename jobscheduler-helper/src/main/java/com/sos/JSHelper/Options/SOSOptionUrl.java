package com.sos.JSHelper.Options;

import java.net.MalformedURLException;
import java.net.URL;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.interfaces.ISOSDataProviderOptions;
import com.sos.JSHelper.interfaces.ISOSFtpOptions;

public class SOSOptionUrl extends SOSOptionString {

    private static final long serialVersionUID = 4655546707861016058L;
    private URL objURL = null;

    public SOSOptionUrl(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
    }

    public SOSOptionUrl(final String pstrUrl) {
        super(null, "", "", pstrUrl, "", false);
    }

    public URL getUrl() {
        return objURL;
    }

    @Override
    public void Value(final String pstrUrl) {
        if (isNotEmpty(pstrUrl)) {
            // Possible Elements of an URL are:
            //
            // http://hans:geheim@www.example.org:80/demo/example.cgi?land=de&stadt=aa#geschichte
            // | | | | | | | |
            // | | | host | url-path searchpart fragment
            // | | password port
            // | user
            // protocol
            //
            // ftp://<user>:<password>@<host>:<port>/<url-path>;type=<typecode>
            // see http://docs.oracle.com/javase/7/docs/api/java/net/URL.html
            String strUrl = pstrUrl;
            try {
                objURL = new URL(strUrl);
                super.Value(pstrUrl);
            } catch (MalformedURLException e) {
                throw new JobSchedulerException(String.format("invalid url '%1$s' specified", pstrUrl), e);
                // not a valid url. ignore it, because it could be a host name
                // only
            }
        }
    }

    public void getOptions(final ISOSDataProviderOptions pobjO) {
        if (objURL == null) {
            throw new JobSchedulerException("no URL speficied");
        }
        setIfNotDirty(pobjO.getHost(), objURL.getHost());
        String strPort = String.valueOf(objURL.getPort());
        if (isEmpty(strPort) || "-1".equals(strPort)) {
            strPort = String.valueOf(objURL.getDefaultPort());
        }
        setIfNotDirty(pobjO.getPort(), strPort);
        pobjO.getProtocol().Value(objURL.getProtocol());
        String strUserInfo = objURL.getUserInfo();
        String[] strU = strUserInfo.split(":");
        setIfNotDirty(pobjO.getUser(), strU[0]);
        if (strU.length > 1) {
            setIfNotDirty(pobjO.getPassword(), strU[1]);
        }
    }

    public void getJadeOptions(final ISOSFtpOptions pobjO) {
        if (objURL == null) {
            throw new JobSchedulerException("no URL speficied");
        }
        String strPath = objURL.getPath();
        String strAuthority = objURL.getAuthority();
        String[] strA = strAuthority.split("@");
    }

    public String getFolderName() {
        if (objURL == null) {
            throw new JobSchedulerException("no URL speficied");
        }
        String strPath = objURL.getPath();
        return strPath;
    }

    private void setIfNotDirty(final SOSOptionElement objOption, final String pstrValue) {
        if (objOption.isNotDirty() && isNotEmpty(pstrValue)) {
            logger.trace("setValue = " + pstrValue);
            objOption.Value(pstrValue);
        }
    }

}