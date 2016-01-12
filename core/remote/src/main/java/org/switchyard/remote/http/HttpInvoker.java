/*
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.switchyard.remote.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jboss.logging.Logger;
import org.switchyard.Property;
import org.switchyard.common.codec.Base64;
import org.switchyard.remote.RemoteInvoker;
import org.switchyard.remote.RemoteMessage;
import org.switchyard.remote.RemoteMessages;
import org.switchyard.serial.FormatType;
import org.switchyard.serial.Serializer;
import org.switchyard.serial.SerializerFactory;
import org.w3c.dom.Node;

/**
 * Remote service invoker which uses HTTP as a transport.
 */
public class HttpInvoker implements RemoteInvoker {
    
    /**
     * HTTP header used to communicate the domain name for a service invocation.
     */
    public static final String SERVICE_HEADER = "switchyard-service";
    /**
     * HTTP header used to communicate the Web Service Security header.
     */
    public static final String WS_SECURITY_HEADER = "switchyard-webservice-security";

    /** Property name for username used for authentication. */
    public static final String AUTH_USERNAME = "auth.username";
    /** Property name for password used for authentication. */
    public static final String AUTH_PASSWORD = "auth.password";
    /** Property name for Web Service Security header element. */
    public static final String WS_SECURITY = "webservice.security";
    /** Property name represented by QName for Web Service Security header element. */
    public static final QName WS_SECURITY_QNAME = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security");
    
    private static Logger _log = Logger.getLogger(HttpInvoker.class);
    private Serializer _serializer = SerializerFactory.create(FormatType.JSON, null, true);
    private URL _endpoint;
    private Properties _properties = new Properties();
    
    /**
     * Create a new HttpInvoker from the specified URL string.
     * @param endpoint url string
     */
    public HttpInvoker(String endpoint) {
        try {
            _endpoint = new URL(endpoint);
        } catch (MalformedURLException badURL) {
            throw RemoteMessages.MESSAGES.invalidURLForEndpoint(endpoint, badURL);
        }
    }
    
    /**
     * Create a new HttpInvoker with the specified URL.
     * @param endpoint the endpoint URL
     */
    public HttpInvoker(URL endpoint) {
        _endpoint = endpoint;
    }

    @Override
    public RemoteMessage invoke(RemoteMessage request) throws java.io.IOException {
        RemoteMessage reply = null;
        HttpURLConnection conn = null;
        
        if (_log.isDebugEnabled()) {
            _log.debug("Invoking " + request.getService() + " at endpoint " + _endpoint.toString());
        }
        
        // Initialize HTTP connection
        conn = (HttpURLConnection)_endpoint.openConnection();
        conn.setDoOutput(true);
        conn.addRequestProperty(SERVICE_HEADER, request.getService().toString());
        for (Property prop : request.getContext().getProperties(HttpInvokerLabel.HEADER.label())) {
            conn.addRequestProperty(prop.getName(), prop.getValue().toString());
        }
        setupAuthentication(conn);
        
        conn.connect();
        OutputStream os = conn.getOutputStream();
        try {
        // Write the request message
            _serializer.serialize(request, RemoteMessage.class, os);
            os.flush();
        } finally { 
            os.close();
        }
        
        // Check for response and process accordingly
        if (conn.getResponseCode() == 200) {
            if (_log.isDebugEnabled()) {
                _log.debug("Processing reply for service " + request.getService());
            }
            InputStream is = conn.getInputStream();
            try {
                reply = _serializer.deserialize(is, RemoteMessage.class);
            } finally {
                is.close();
            }
        }
        
        return reply;
    }

    private void setupAuthentication(HttpURLConnection conn) {
        if (_properties.getProperty(AUTH_USERNAME) != null) {
            conn.setRequestProperty("Authorization",
                    "Basic " + Base64.encodeFromString(_properties.getProperty(AUTH_USERNAME) + ":" + _properties.getProperty(AUTH_PASSWORD)));
        }
        
        Object wsse = null;
        if (_properties.get(WS_SECURITY_QNAME) != null) {
            wsse = _properties.get(WS_SECURITY_QNAME);
        } else if (_properties.get(WS_SECURITY_QNAME.toString()) != null) {
            wsse = _properties.get(WS_SECURITY_QNAME.toString());
        } else if (_properties.get(WS_SECURITY) != null) {
            wsse = _properties.get(WS_SECURITY);
        }
        
        if (wsse != null) {
            if (wsse instanceof Node) {
                Node wsseNode = Node.class.cast(wsse);
                StringWriter sw = new StringWriter();
                try {
                    Transformer tr = TransformerFactory.newInstance().newTransformer();
                    tr.transform(new DOMSource(wsseNode), new StreamResult(sw));
                } catch (Exception e) {
                    throw RemoteMessages.MESSAGES.invalidWebServiceSecurityHeader(wsse, e);
                }
                wsse = sw.toString();
            } else if (!(wsse instanceof String)) {
                throw RemoteMessages.MESSAGES.unsupportedWebServiceSecurityHeaderType(wsse.getClass().getName());
            }
            conn.setRequestProperty(WS_SECURITY_HEADER, Base64.encodeFromString(wsse.toString()));
        }
    }

    /**
     * Sets invoker property.
     * @param key property key
     * @param value property value
     * @return this instance
     */
    public HttpInvoker setProperty(Object key, Object value) {
        _properties.put(key, value);
        return this;
    }
}
