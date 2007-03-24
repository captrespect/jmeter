/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.jmeter.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Derived from EasySSLProtocolFactory
 * 
 * Used by JsseSSLManager to set up the HttpClient and Java https socket handling
 */

public class HttpSSLProtocolSocketFactory 
    extends SSLSocketFactory // for java sockets
    implements SecureProtocolSocketFactory { // for httpclient sockets

	private static final Logger log = LoggingManager.getLoggerForClass();
	
	private SSLSocketFactory sslfac;

    private HttpSSLProtocolSocketFactory(){
    }
    
    public HttpSSLProtocolSocketFactory(SSLContext context) {
        super();
		sslfac=context.getSocketFactory();
    }
    
    private static final String protocolList = JMeterUtils.getPropDefault("https.socket.protocols", "");
    
    static {
    	if (protocolList.length()>0){
    		log.info("Using protocol list: "+protocolList);
    	}
    }
    private static final String[] protocols = protocolList.split(" ");

    private void setSocket(Socket sock){
    	if (protocolList.length() <= 0) return;
    	if (sock instanceof SSLSocket){
    		try {
				((SSLSocket) sock).setEnabledProtocols(protocols);
			} catch (IllegalArgumentException e) {
				log.warn("Could not set protocol list: "+protocolList+".");
				log.warn("Valid protocols are: "+join(((SSLSocket) sock).getSupportedProtocols()));
			}
    	} else {
    		throw new IllegalArgumentException("Expecting only SSL socket; found "+sock.getClass().getName());
    	}
    }

    private String join(String[] strings) {
		StringBuffer sb = new StringBuffer();
    	for (int i=0;i<strings.length;i++){
    		if (i>0) sb.append(" ");
    		sb.append(strings[i]);
    	}
    	return sb.toString();
	}

	/**
     * Attempts to get a new socket connection to the given host within the given time limit.
     *  
     * @param host the host name/IP
     * @param port the port on the host
     * @param clientHost the local host name/IP to bind the socket to
     * @param clientPort the port on the local machine
     * @param params {@link HttpConnectionParams Http connection parameters}
     * 
     * @return Socket a new socket
     * 
     * @throws IOException if an I/O error occurs while creating the socket
     * @throws UnknownHostException if the IP address of the host cannot be
     * determined
     */
    public Socket createSocket(
        final String host,
        final int port,
        final InetAddress localAddress,
        final int localPort,
        final HttpConnectionParams params
    ) throws IOException, UnknownHostException, ConnectTimeoutException {
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        int timeout = params.getConnectionTimeout();
        Socket socket;
        if (timeout == 0) {
        	socket = sslfac.createSocket(host, port, localAddress, localPort);
        } else {
            socket = sslfac.createSocket();
            SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
            SocketAddress remoteaddr = new InetSocketAddress(host, port);
            socket.bind(localaddr);
            socket.connect(remoteaddr, timeout);
        }
        setSocket(socket);
        return socket;
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
     */
    public Socket createSocket(String host, int port)
        throws IOException, UnknownHostException {
    	Socket sock = sslfac.createSocket(
            host,
            port
        );
        setSocket(sock);
    	return sock;
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
     */
    public Socket createSocket(
        Socket socket,
        String host,
        int port,
        boolean autoClose)
        throws IOException, UnknownHostException {
    	Socket sock = sslfac.createSocket(
            socket,
            host,
            port,
            autoClose
        );
        setSocket(sock);
    	return sock;
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
     */
    public Socket createSocket(
        String host,
        int port,
        InetAddress clientHost,
        int clientPort)
        throws IOException, UnknownHostException {

        Socket sock = sslfac.createSocket(
            host,
            port,
            clientHost,
            clientPort
        );
        setSocket(sock);
        return sock;
    }

	public Socket createSocket(InetAddress host, int port) throws IOException {
		Socket sock=sslfac.createSocket(host,port);
        setSocket(sock);
		return sock;
	}

	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
		Socket sock=sslfac.createSocket(address, port, localAddress, localPort);
        setSocket(sock);
		return sock;
	}

	public String[] getDefaultCipherSuites() {
		return sslfac.getDefaultCipherSuites();
	}

	public String[] getSupportedCipherSuites() {
		return sslfac.getSupportedCipherSuites();
	}
}