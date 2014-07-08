package com.sos.dialog.classes;

import java.net.*;
 
 
public class SOSUrl  {

    private static final String DEFAULT_PROTOCOL = "http";
    private static final int DEFAULT_PORT = 4444;
    private static final String DEFAULT_HOST = "localhost";

    private URL url;    
    private String urlValue;
    private String title;
     
    public SOSUrl(String url_) {
        initUrl(url_);
        this.title = getUrlCaption(); 
    }

    public SOSUrl(String title_,String url_) {
        initUrl(url_);
        if (title_ == null || title_.trim().length() == 0) {
            this.title = getUrlCaption(); 
        }else {
            this.title = title_;
        }
    }
    
    private void initUrl(String url_) {
        try {
            urlValue = url_;
            url = new URL(urlValue);
         }
        catch (MalformedURLException e) {
            //See https://bugs.eclipse.org/bugs/show_bug.cgi?id=324351
            if (!url_.equals("about:blank")) {
                try {
                    urlValue = String.format("%s://%s", DEFAULT_PROTOCOL, url_);
                    url = new URL(urlValue);

                }
                catch (MalformedURLException e1) {
                    try {
                        urlValue = String.format("%s://%s:%s", DEFAULT_PROTOCOL, DEFAULT_HOST, DEFAULT_PORT);
                        url = new URL(urlValue);
                    }
                    catch (MalformedURLException e2) {
                        e2.printStackTrace();
                    }
                }

            }
        }
    }

    public String getHost() {
        return url.getHost();
    }
    
    public String getProtocol() {
        return url.getProtocol();
    }

    public int getPort() {
        if (url.getPort() < 0) {
            return DEFAULT_PORT;
        }else {
            return url.getPort();
        }
    }

    private String getUrlCaption() {
        if (url.getProtocol().equalsIgnoreCase(DEFAULT_PROTOCOL)) {
            if (url.getPort() < 0) {
               return String.format("%s",url.getHost());
            }else {
                return String.format("%s:%s",url.getHost(),url.getPort());
            }
        }else {
            if (url.getPort() < 0) {
                return String.format("%s://%s",url.getProtocol(),url.getHost());
            }else {
                return String.format("%s://%s:%s",url.getProtocol(),url.getHost(),url.getPort());
            }
        }
    }

    public URL getUrl() {
        return url;
    }
    
    public String getUrlValue() {
        return urlValue;
    }

    public String getTitle() {
        return title;
    }

  

}
