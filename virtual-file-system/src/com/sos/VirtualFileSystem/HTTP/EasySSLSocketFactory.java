package com.sos.VirtualFileSystem.HTTP;


/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.SchemeLayeredSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * This socket factory will create ssl socket that accepts self signed
 * certificate
 * 
 * @author re (diverse Anpassungen für httpclient 4.2.x)
 * @author olamy
 * @version $Id: EasySSLSocketFactory.java 765355 2009-04-15 20:59:07Z evenisse
 *          $
 * @since 1.2.3
 */
public class EasySSLSocketFactory implements SchemeLayeredSocketFactory  {

	private SSLContext sslcontext = null;

	private static SSLContext createEasySSLContext() throws IOException {
		try {
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new TrustManager[] { new EasyX509TrustManager(
					null) }, null);
			return context;
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	private SSLContext getSSLContext() throws IOException {
		if (this.sslcontext == null) {
			this.sslcontext = createEasySSLContext();
		}
		return this.sslcontext;
	}
	
	@Override
	public Socket connectSocket(Socket sock, InetSocketAddress remoteAddress,
			InetSocketAddress localAddress, HttpParams params) throws IOException,
			UnknownHostException, ConnectTimeoutException {
		
		int connTimeout = HttpConnectionParams.getConnectionTimeout(params); 
	    int soTimeout = HttpConnectionParams.getSoTimeout(params); 
	    SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock : createSocket(params)); 
	    if (localAddress != null) { 
	    	// we need to bind explicitly 
	        sslsock.bind(localAddress); 
	    } 
	 
	    sslsock.connect(remoteAddress, connTimeout); 
	    sslsock.setSoTimeout(soTimeout); 
	    return sslsock; 
	}

	@Override
	public Socket createSocket(HttpParams arg0) throws IOException {
		return this.createSocket();
	}

	/**
	 * @see org.apache.http.conn.scheme.SocketFactory#createSocket()
	 */
	public Socket createSocket() throws IOException {
		return getSSLContext().getSocketFactory().createSocket();
	}

	/**
	 * @see org.apache.http.conn.scheme.SocketFactory#isSecure(java.net.Socket)
	 */
	public boolean isSecure(Socket socket) throws IllegalArgumentException {
		return true;
	}

	/**
	 * @see org.apache.http.conn.scheme.LayeredSocketFactory#createSocket(java.net.Socket,
	 *      java.lang.String, int, boolean)
	 */
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
        return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
	}

	// -------------------------------------------------------------------
	// javadoc in org.apache.http.conn.scheme.SocketFactory says :
	// Both Object.equals() and Object.hashCode() must be overridden
	// for the correct operation of some connection managers
	// -------------------------------------------------------------------

	public boolean equals(Object obj) {
		return ((obj != null) && obj.getClass().equals(
				EasySSLSocketFactory.class));
	}

	public int hashCode() {
		return EasySSLSocketFactory.class.hashCode();
	}

	@Override
	public Socket createLayeredSocket(Socket arg0, String arg1, int arg2,
			HttpParams arg3) throws IOException, UnknownHostException {
		// TODO Auto-generated method stub
		return null;
	}

	

}

