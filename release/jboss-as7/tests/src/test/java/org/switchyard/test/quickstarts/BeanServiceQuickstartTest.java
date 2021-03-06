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
package org.switchyard.test.quickstarts;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.common.type.Classes;
import org.switchyard.component.test.mixins.http.HTTPMixIn;
import org.switchyard.test.ArquillianUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

@RunWith(Arquillian.class)
public class BeanServiceQuickstartTest {

    @Deployment(testable = false)
    public static JavaArchive createDeployment() {
        return ArquillianUtil.createJarQSDeployment("switchyard-bean-service");
    }

    @Test
    public void testOrders() throws Exception {
        HTTPMixIn httpMixIn = new HTTPMixIn();

        httpMixIn.initialize();
        try {
            XMLUnit.setIgnoreWhitespace(true);
            XMLUnit.setIgnoreAttributeOrder(true);
            String wsdl = httpMixIn.sendString("http://localhost:8080/quickstart-bean/OrderService?wsdl", "", HTTPMixIn.HTTP_GET);
            compareWSDL(new InputStreamReader(Classes.getResourceAsStream("OrderService.wsdl")), new StringReader(wsdl));
            String response = httpMixIn.postString("http://localhost:8080/quickstart-bean/OrderService", SOAP_REQUEST);
            XMLAssert.assertXpathEvaluatesTo("PO-19838-XYZ", "//orderAck/orderId", response);
            XMLAssert.assertXpathEvaluatesTo("true", "//orderAck/accepted", response);
            XMLAssert.assertXpathEvaluatesTo("Order Accepted [intercepted]", "//orderAck/status", response);
        } finally {
            httpMixIn.uninitialize();
        }
    }

    @Test
    public void testGatewayRestart(@ArquillianResource ManagementClient client) throws Exception {
        HTTPMixIn httpMixIn = new HTTPMixIn();

        httpMixIn.initialize();
        try {
            XMLUnit.setIgnoreWhitespace(true);
            XMLUnit.setIgnoreAttributeOrder(true);
            String wsdl = httpMixIn.sendString("http://localhost:8080/quickstart-bean/OrderService?wsdl", "", HTTPMixIn.HTTP_GET);
            compareWSDL(new InputStreamReader(Classes.getResourceAsStream("OrderService.wsdl")), new StringReader(wsdl));
            String response = httpMixIn.postString("http://localhost:8080/quickstart-bean/OrderService", SOAP_REQUEST);
            XMLAssert.assertXpathEvaluatesTo("PO-19838-XYZ", "//orderAck/orderId", response);
            XMLAssert.assertXpathEvaluatesTo("true", "//orderAck/accepted", response);
            XMLAssert.assertXpathEvaluatesTo("Order Accepted [intercepted]", "//orderAck/status", response);

            final String namespace = "urn:switchyard-quickstart:bean-service:0.1.0";
            final String application = "orders";
            final String service = "OrderService";
            final String bindingType = "soap";
            final ModelNode operation = new ModelNode();
            operation.get(ModelDescriptionConstants.OP_ADDR).add("subsystem", "switchyard");
            operation.get(ModelDescriptionConstants.NAME).set("_" + service + "_" + bindingType + "_1");
            operation.get("service-name").set(new QName(namespace, service).toString());
            operation.get("application-name").set(new QName(namespace, application).toString());

            // stop the gateway
            operation.get(ModelDescriptionConstants.OP).set("stop-gateway");
            ModelNode result = client.getControllerClient().execute(operation);
            Assert.assertEquals("Failed to stop gateway: " + result.toString(), ModelDescriptionConstants.SUCCESS,
                    result.get(ModelDescriptionConstants.OUTCOME).asString());
            int status = httpMixIn.sendStringAndGetStatus("http://localhost:8080/quickstart-bean/OrderService",
                    SOAP_REQUEST, HTTPMixIn.HTTP_POST);
            Assert.assertEquals("Unexpected status for disabled gateway.", 404, status);

            // restart the gateway
            operation.get(ModelDescriptionConstants.OP).set("start-gateway");
            result = client.getControllerClient().execute(operation);
            Assert.assertEquals("Failed to restart gateway" + result.toString(), ModelDescriptionConstants.SUCCESS,
                    result.get(ModelDescriptionConstants.OUTCOME).asString());
            wsdl = httpMixIn.sendString("http://localhost:8080/quickstart-bean/OrderService?wsdl", "", HTTPMixIn.HTTP_GET);
            compareWSDL(new InputStreamReader(Classes.getResourceAsStream("OrderService.wsdl")), new StringReader(wsdl));
            response = httpMixIn.postString("http://localhost:8080/quickstart-bean/OrderService", SOAP_REQUEST);
            XMLAssert.assertXpathEvaluatesTo("PO-19838-XYZ", "//orderAck/orderId", response);
            XMLAssert.assertXpathEvaluatesTo("true", "//orderAck/accepted", response);
            XMLAssert.assertXpathEvaluatesTo("Order Accepted [intercepted]", "//orderAck/status", response);
        } finally {
            httpMixIn.uninitialize();
        }
    }

    /**
     * Due to the different behavior on Element ordering between JDK8 and older,
     * assertXMLEqual() doesn't work OOTB. Instead just verify some parts of WSDL using XPath.
     */
    private void compareWSDL(Reader expected, Reader actual) throws Exception {
        Schema wsdlSchema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                                         .newSchema(new URL("http://schemas.xmlsoap.org/wsdl/"));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setSchema(wsdlSchema);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document expectedDoc = builder.parse(new InputSource(expected));
        Document actualDoc = builder.parse(new InputSource(actual));

        XMLAssert.assertXpathsEqual(
                "//definitions/types", expectedDoc,
                "//definitions/types", actualDoc);
        XMLAssert.assertXpathsEqual(
                "//definitions/message[@name=submitOrder]", expectedDoc,
                "//definitions/message[@name=submitOrder]", actualDoc);
        XMLAssert.assertXpathsEqual(
                "//definitions/message[@name=submitOrderResponse]", expectedDoc,
                "//definitions/message[@name=submitOrderResponse]", actualDoc);
        XMLAssert.assertXpathsEqual(
                "//definitions/portType", expectedDoc,
                "//definitions/portType", actualDoc);
        XMLAssert.assertXpathsEqual(
                "//definitions/binding", expectedDoc,
                "//definitions/binding", actualDoc);
        XMLAssert.assertXpathsEqual(
                "//definitions/service", expectedDoc,
                "//definitions/service", actualDoc);
    }

    private static final String SOAP_REQUEST = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
            "    <soap:Body>\n" +
            "        <orders:submitOrder xmlns:orders=\"urn:switchyard-quickstart:bean-service:1.0\">\n" +
            "            <order>\n" +
            "                <orderId>PO-19838-XYZ</orderId>\n" +
            "                <itemId>BUTTER</itemId>\n" +
            "                <quantity>200</quantity>\n" +
            "            </order>\n" +
            "        </orders:submitOrder>\n" +
            "    </soap:Body>\n" +
            "</soap:Envelope>";
}
