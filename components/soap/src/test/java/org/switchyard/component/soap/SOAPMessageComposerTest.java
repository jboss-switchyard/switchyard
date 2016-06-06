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
package org.switchyard.component.soap;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.switchyard.Context;
import org.switchyard.Exchange;
import org.switchyard.Message;
import org.switchyard.common.xml.XMLHelper;
import org.switchyard.component.soap.composer.SOAPBindingData;
import org.switchyard.component.soap.composer.SOAPContextMapper;
import org.switchyard.component.soap.composer.SOAPMessageComposer;
import org.switchyard.component.soap.util.SOAPUtil;
import org.switchyard.internal.DefaultMessage;
import org.switchyard.internal.ExchangeImpl;
import org.w3c.dom.Node;

/**
 * SOAPMessageComposerTest.
 */
public class SOAPMessageComposerTest {

    private static final Logger logger = Logger.getLogger(SOAPMessageComposerTest.class);
    
    private Exchange _exchange;
    private SOAPMessageComposer _composer;
    private Message _message;
    
    @Before
    public void before() {
        _exchange = Mockito.mock(ExchangeImpl.class, Mockito.RETURNS_DEEP_STUBS);
        _message = new DefaultMessage();
        Mockito.when(_exchange.createMessage()).thenReturn(_message);
        _composer = new SOAPMessageComposer();
        _composer.setContextMapper(new SOAPContextMapper() {
            public void mapFrom(SOAPBindingData sbd, Context context) {
                return;
            }
        });
    }
    
    @Test
    public void testCopyNamespacesSOAP11() throws Exception {
        _composer.setCopyNamespaces(true);
        SOAPMessage soapMessage = SOAPUtil.createMessage(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING);
        soapMessage.getSOAPPart().getEnvelope().addNamespaceDeclaration("foobarns", "urn:foobarns");
        soapMessage.getSOAPBody().addBodyElement(new QName("urn:test", "foobar"));
        logger.info(String.format("before compose:[\n%s]", XMLHelper.toPretty(soapMessage.getSOAPPart())));
        
        SOAPBindingData sbd = new SOAPBindingData(soapMessage);
        Node res = _composer.compose(sbd, _exchange).getContent(Node.class);
        logger.info(String.format("after compose:[\n%s]", XMLHelper.toPretty(res)));
        String envPrefix = soapMessage.getSOAPPart().getEnvelope().getPrefix();
        String envNS = soapMessage.getSOAPPart().getEnvelope().getNamespaceURI(envPrefix);
        String resNS = res.lookupNamespaceURI(envPrefix);
        Assert.assertEquals(envNS, resNS);
        Assert.assertEquals("urn:foobarns", res.lookupNamespaceURI("foobarns"));
    }
    
    @Test
    public void testCopyNamespacesOnFaultSOAP11() throws Exception {
        _composer.setCopyNamespaces(true);
        SOAPMessage soapMessage = SOAPUtil.createMessage(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING);
        soapMessage.getSOAPPart().getEnvelope().addNamespaceDeclaration("foobarns", "urn:foobarns");
        soapMessage.getSOAPBody().addFault(new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Server"), "my fault string");
        logger.info(String.format("before compose:[\n%s]", XMLHelper.toPretty(soapMessage.getSOAPPart())));
        
        SOAPBindingData sbd = new SOAPBindingData(soapMessage);
        Node res = _composer.compose(sbd, _exchange).getContent(Node.class);
        logger.info(String.format("after compose:[\n%s]", XMLHelper.toPretty(res)));
        String envPrefix = soapMessage.getSOAPPart().getEnvelope().getPrefix();
        String envNS = soapMessage.getSOAPPart().getEnvelope().getNamespaceURI(envPrefix);
        String resNS = res.lookupNamespaceURI(envPrefix);
        Assert.assertEquals(envNS, resNS);
        Assert.assertEquals("urn:foobarns", res.lookupNamespaceURI("foobarns"));
    }
    
    @Test
    public void testCopyNamespacesSOAP12() throws Exception {
        _composer.setCopyNamespaces(true);
        SOAPMessage soapMessage = SOAPUtil.createMessage(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING);
        soapMessage.getSOAPPart().getEnvelope().addNamespaceDeclaration("foobarns", "urn:foobarns");
        soapMessage.getSOAPBody().addBodyElement(new QName("urn:test", "foobar"));
        logger.info(String.format("before compose:[\n%s]", XMLHelper.toPretty(soapMessage.getSOAPPart())));
        
        SOAPBindingData sbd = new SOAPBindingData(soapMessage);
        Node res = _composer.compose(sbd, _exchange).getContent(Node.class);
        logger.info(String.format("after compose:[\n%s]", XMLHelper.toPretty(res)));
        String envPrefix = soapMessage.getSOAPPart().getEnvelope().getPrefix();
        String envNS = soapMessage.getSOAPPart().getEnvelope().getNamespaceURI(envPrefix);
        String resNS = res.lookupNamespaceURI(envPrefix);
        Assert.assertEquals(envNS, resNS);
        Assert.assertEquals("urn:foobarns", res.lookupNamespaceURI("foobarns"));
    }
    
    @Test
    public void testCopyNamespacesOnFaultSOAP12() throws Exception {
        _composer.setCopyNamespaces(true);
        SOAPMessage soapMessage = SOAPUtil.createMessage(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING);
        soapMessage.getSOAPPart().getEnvelope().addNamespaceDeclaration("foobarns", "urn:foobarns");
        soapMessage.getSOAPBody().addFault(new QName(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, "Receiver"), "my fault string");
        logger.info(String.format("before compose:[\n%s]", XMLHelper.toPretty(soapMessage.getSOAPPart())));
        
        SOAPBindingData sbd = new SOAPBindingData(soapMessage);
        Node res = _composer.compose(sbd, _exchange).getContent(Node.class);
        logger.info(String.format("after compose:[\n%s]", XMLHelper.toPretty(res)));
        String envPrefix = soapMessage.getSOAPPart().getEnvelope().getPrefix();
        String envNS = soapMessage.getSOAPPart().getEnvelope().getNamespaceURI(envPrefix);
        String resNS = res.lookupNamespaceURI(envPrefix);
        Assert.assertEquals(envNS, resNS);
        Assert.assertEquals("urn:foobarns", res.lookupNamespaceURI("foobarns"));
    }
}
