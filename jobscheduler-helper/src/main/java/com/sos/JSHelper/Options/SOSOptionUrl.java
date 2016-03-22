package com.sos.JSHelper.Options;

import java.net.MalformedURLException;
import java.net.URL;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.interfaces.ISOSDataProviderOptions;
import com.sos.JSHelper.interfaces.ISOSFtpOptions;

public class SOSOptionUrl extends SOSOptionString {

    /**
	 * 
	 */
    private static final long serialVersionUID = 4655546707861016058L;
    /** FTP URL syntax is described in RFC1738,[13] taking the form:
     * 
     * ftp://[<user>[:<password>]@]<host>[:<port>]/<url-path>
     * 
     * (see: http://www.rfc-base.org/txt/rfc-1738.txt) The bracketed parts are
     * optional.
     * 
     * For example, the URL
     * 
     * ftp://public.ftp-servers.example.com/mydirectory/myfile.txt
     * 
     * represents the file myfile.txt from the directory mydirectory on the
     * server public.ftp-servers.example.com as an FTP resource. The URL
     * 
     * ftp://user001:secretpassword@private.ftp-servers.example.com/mydirectory/
     * myfile.txt
     * 
     * adds a specification of the username and password that must be used to
     * access this resource.
     * 
     * More details on specifying a username and password may be found in the
     * browsers' documentation, such as, for example, Firefox[14] and Internet
     * Explorer.[15] By default, most web browsers use passive (PASV) mode,
     * which more easily traverses end-user firewalls. */
    private URL objURL = null;

    public SOSOptionUrl(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        // TODO Auto-generated constructor stub
    }

    public SOSOptionUrl(final String pstrUrl) {
        super(null, "", "", pstrUrl, "", false);
    }

    public URL getUrl() {
        return objURL;
    }

    /**
	 * 
	 */
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
            String strUrl = pstrUrl; //
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

    /** \brief getOptions
     *
     * \details
     * 
     * \return void */
    public void getOptions(final ISOSDataProviderOptions pobjO) {
        if (objURL == null) {
            throw new JobSchedulerException("no URL speficied");
        }
        setIfNotDirty(pobjO.getHost(), objURL.getHost());
        String strPort = String.valueOf(objURL.getPort());
        if (isEmpty(strPort) || strPort.equals("-1")) {
            strPort = String.valueOf(objURL.getDefaultPort());
        }
        setIfNotDirty(pobjO.getport(), strPort);
        // setIfNotDirty(pobjO.getprotocol(), objURL.getProtocol());
        pobjO.getprotocol().Value(objURL.getProtocol());
        String strUserInfo = objURL.getUserInfo();
        String[] strU = strUserInfo.split(":");
        setIfNotDirty(pobjO.getUser(), strU[0]);
        if (strU.length > 1) {
            setIfNotDirty(pobjO.getPassword(), strU[1]);
        }
    }

    /** \brief getJadeOptions
     *
     * \details
     * 
     * \return void */
    public void getJadeOptions(final ISOSFtpOptions pobjO) {
        if (objURL == null) {
            throw new JobSchedulerException("no URL speficied");
        }
        String strPath = objURL.getPath();
        // setIfNotDirty(pobjO.getfile_path(), strPath);
        // setIfNotDirty(pobjO.getdir(), strPath);
        String strAuthority = objURL.getAuthority();
        String[] strA = strAuthority.split("@"); // user:pw host
    }

    public String getFolderName() {
        if (objURL == null) {
            throw new JobSchedulerException("no URL speficied");
        }
        String strPath = objURL.getPath();
        return strPath;
    }

    /** \brief setIfNotDirty
     *
     * \details
     * 
     * \return void */
    private void setIfNotDirty(final SOSOptionElement objOption, final String pstrValue) {
        if (objOption.isNotDirty() && isNotEmpty(pstrValue)) {
            logger.trace("setValue = " + pstrValue);
            objOption.Value(pstrValue);
        }
    }
}
