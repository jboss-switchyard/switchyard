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
 
package org.switchyard.component.soap.endpoint;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import org.switchyard.ServiceDomain;
import org.switchyard.component.common.Endpoint;
import org.switchyard.component.soap.AddressingInterceptor;
import org.switchyard.component.soap.InboundHandler;
import org.switchyard.component.soap.WebServicePublishException;
import org.switchyard.component.soap.config.model.SOAPBindingModel;

import org.switchyard.common.type.Classes;
import org.switchyard.deploy.internal.Deployment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Handles publishing of Webservice Endpoints on CXF JAX-WS implementations.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2012 Red Hat Inc.
 */
public class CXFJettyEndpointPublisher extends AbstractEndpointPublisher {

    private static final String HTTP_SCHEME = "http";

    /**
     * {@inheritDoc}
     */
    public synchronized Endpoint publish(ServiceDomain domain, final SOAPBindingModel config, final String bindingId, final InboundHandler handler, WebServiceFeature... features) {
        CXFJettyEndpoint wsEndpoint = null;
        try {
            initialize(config);
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(javax.xml.ws.Endpoint.WSDL_SERVICE, config.getPort().getServiceQName());
            properties.put(javax.xml.ws.Endpoint.WSDL_PORT, config.getPort().getPortQName());
            properties.put(MessageContext.WSDL_DESCRIPTION, getWsdlLocation());

            String publishUrl = HTTP_SCHEME + "://" + config.getSocketAddr().getHost() + ":" + config.getSocketAddr().getPort() + "/" + getContextPath();

            String configFile = null;
            wsEndpoint = new CXFJettyEndpoint(bindingId, config, handler, new AddressingInterceptor(), features);
            //wsEndpoint.getEndpoint().setProperties(properties);

            if (config.getEndpointConfig() != null) {
                String endpointFile = config.getEndpointConfig().getConfigFile();
                wsEndpoint = parseJaxWSConfig(endpointFile, wsEndpoint);
            }

            wsEndpoint.getEndpoint().setWsdlURL(getWsdlLocation());
            wsEndpoint.getEndpoint().setServiceName(config.getPort().getServiceQName());
            wsEndpoint.publish(publishUrl);
        } catch (MalformedURLException e) {
            throw new WebServicePublishException(e);
        }
        return wsEndpoint;
    }

    /**
     * ENTESB-6418 : parse the endpoint file for properties and attach those
     * properties to the endpoint we publish.  Note that we are only supporting
     * properties here and not pre/post handler chains.
     * @param fileName jaxws-endpoint-config.xml to parse for properties
     * @return list of properties
     */
    private CXFJettyEndpoint parseJaxWSConfig(String fileName, CXFJettyEndpoint endpoint) {
        Map<String, Object> props = new HashMap<String, Object>();
        List<Handler> handlerList = new ArrayList<Handler>();
        try {
            URL url = Classes.getResource(fileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(url.openStream());
            doc.getDocumentElement().normalize();

            NodeList endpointConfigs = doc.getElementsByTagName("endpoint-config");
            for (int temp = 0; temp < endpointConfigs.getLength(); temp++) {
                Node node = endpointConfigs.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    NodeList propertyNodeList = element.getElementsByTagName("property");
                    for (int count = 0; count < propertyNodeList.getLength(); count++) {
                        Node propNode = propertyNodeList.item(count);
                        String propertyName = element.getElementsByTagName("property-name").item(0).getTextContent();
                        String propertyValue = element.getElementsByTagName("property-value").item(0).getTextContent();
                        props.put(propertyName, propertyValue);
                    }

                    // TODO : run through the list of pre-handler-chains and post-handler-chains
                    // and add the handlers to the endpoint
                }
            }
        } catch (IOException ioe) {
        } catch (ParserConfigurationException pce) {
        } catch (SAXException se) {
        }
        endpoint.getEndpoint().setProperties(props);
        return endpoint;
    }
}
