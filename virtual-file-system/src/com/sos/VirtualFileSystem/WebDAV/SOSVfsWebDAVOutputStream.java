package com.sos.VirtualFileSystem.WebDAV;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.apache.webdav.lib.WebdavResource;


public class SOSVfsWebDAVOutputStream extends ByteArrayOutputStream {
	
	@SuppressWarnings("unused")
	private final String	conClassName		= "SOSVfsWebDAV";
	
	@SuppressWarnings("unused")
	private final Logger	logger		= Logger.getLogger(SOSVfsWebDAVOutputStream.class);
	private final WebdavResource	resource;

	/**
	 *
	 * \brief SOSVfsWebDAVOutputStream
	 *
	 * \details
	 *
	 * @param res
	 */
	public SOSVfsWebDAVOutputStream(final WebdavResource res) {
		super();
		resource = res;
	}
	
	/**
	 *
	 * \brief getResource
	 *
	 * \details
	 *
	 * \return WebdavResource
	 */
	public WebdavResource getResource() {
		return resource;
	}
	
	/**
	 *
	 * \brief put
	 *
	 * \details
	 *
	 * @throws HttpException, IOException
	 */
	public void put() throws HttpException, IOException {
		resource.putMethod(this.toByteArray());
		if(resource.exists() == false) {
			String msg = String.format("%1$s: %2$s", resource.getPath(), resource.getStatusMessage());
			throw new HttpException(msg);
		}
	}

	/**
	 *
	 * \brief close
	 *
	 * \details
	 *
	 * \return
	 *
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException {
		super.close();
		resource.close();
	}
}
