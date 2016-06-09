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

package org.switchyard.transform.jaxb.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

import javax.activation.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.switchyard.common.codec.Base64;
import org.switchyard.common.type.Classes;
import org.switchyard.config.model.ModelPuller;
import org.switchyard.config.model.switchyard.SwitchYardModel;
import org.switchyard.config.model.transform.TransformModel;
import org.switchyard.internal.DefaultMessage;
import org.switchyard.metadata.JavaTypes;
import org.switchyard.transform.AbstractTransformerTestCase;
import org.switchyard.transform.Transformer;
import org.switchyard.transform.config.model.JAXBTransformModel;
import org.switchyard.transform.jaxb.internal.Items.Item;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class JAXBTransformerTest extends AbstractTransformerTestCase {

    private static final Logger logger = Logger.getLogger(JAXBTransformerTest.class);
    private static final String IMG_FILE_PATH = "org/switchyard/transform/jaxb/internal/coverphoto.jpg";

    @Test
    public void test_createFromClass() throws Exception {
        JAXBUnmarshalTransformer unmarshalTransformer = new JAXBUnmarshalTransformer(
                new QName("purchaseOrder"), JavaTypes.toMessageType(POType.class), null, true);

        JAXBMarshalTransformer marshalTransformer = new JAXBMarshalTransformer(
                JavaTypes.toMessageType(POType.class), new QName("purchaseOrder"), null, true, true);

        DefaultMessage message = new DefaultMessage();
        message.setContent(new StreamSource(new StringReader(PO_XML)));
        message.addAttachment("cid:coverphoto.jpg", new DataSource() {
            @Override
            public InputStream getInputStream() throws IOException {
                return Classes.getResourceAsStream(IMG_FILE_PATH);
            }
            @Override
            public OutputStream getOutputStream() throws IOException {
                throw new IOException("Not supported");
            }
            @Override
            public String getContentType() {
                return "application/octet-stream";
            }
            @Override
            public String getName() {
                return "cid:coverphoto.jpg";
            }
        });

        // Transform XML to Java POType
        unmarshalTransformer.transform(message);
        
        // Check if the attachment is successfully mapped into JAXB object
        Object unmarshalledPOType = message.getContent();
        Assert.assertEquals(POType.class, unmarshalledPOType.getClass());
        for (Item i : ((POType)unmarshalledPOType).getItems().getItem()) {
            if (i.getPartNumber().equals("242-GZ")) {
                Assert.assertNotNull("A coverPhoto for 242-GZ must not be null", i.getCoverPhoto());
                compareCoverPhoto(Classes.getResourceAsStream(IMG_FILE_PATH), i.getCoverPhoto().getInputStream());
            } else {
                Assert.assertNull("A coverPhoto for " + i.getPartNumber() + " must be null, but was :" + i.getCoverPhoto(), i.getCoverPhoto());
            }
        }
        
        // Transform Java POType back to XML
        logger.info("Attempting JAVA2XML transformation with attachment enabled");
        marshalTransformer.transform(message);
        String resultXML = message.getContent(String.class);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.compareXML(PO_XML, resultXML);
        //logger.info(String.format("Response:[\n%s]", resultXML));
        XpathEngine xpath = XMLUnit.newXpathEngine();
        HashMap<String,String> ncm = new HashMap<String,String>();
        ncm.put("xop", "http://www.w3.org/2004/08/xop/include");
        xpath.setNamespaceContext(new SimpleNamespaceContext(ncm));
        String href = xpath.evaluate("//purchaseOrder/items/item[@partNum=\"242-GZ\"]/coverPhoto/xop:Include/@href", XMLUnit.buildControlDocument(resultXML));
        DataSource attachment = message.getAttachment(href);
        Assert.assertNotNull(String.format("Attachment '%s' must not be null", href), attachment);
        logger.info(String.format("Found an attachment '%s'", href));
        compareCoverPhoto(Classes.getResourceAsStream(IMG_FILE_PATH), attachment.getInputStream());
        
        // Try POType > XML again with attachment disabled - coverPhoto should be inlined
        message.removeAttachment(href);
        message.setContent(unmarshalledPOType);
        JAXBMarshalTransformer marshalTransformerInline = new JAXBMarshalTransformer(
                JavaTypes.toMessageType(POType.class), new QName("purchaseOrder"), null, false, true);
        logger.info("Attempting JAVA2XML transformation with attachment disabled");
        marshalTransformerInline.transform(message);
        resultXML = message.getContent(String.class);
        //logger.info(String.format("Response:[\n%s]", resultXML));
        Document resultDoc = XMLUnit.buildControlDocument(resultXML);
        href = xpath.evaluate("//purchaseOrder/items/item[@partNum=\"242-GZ\"]/coverPhoto/xop:Include", resultDoc);
        Assert.assertTrue(String.format("xop:Include should not appear, but got [%s]", href), href.trim().isEmpty());
        String coverPhotoB64 = xpath.evaluate("//purchaseOrder/items/item[@partNum=\"242-GZ\"]/coverPhoto", resultDoc);
        Assert.assertTrue(String.format("Inlined coverPhoto data is invalid:[%s]", coverPhotoB64), coverPhotoB64 != null && !coverPhotoB64.trim().isEmpty());
        logger.info(String.format("Found inlined coverPhoto:[%s]", coverPhotoB64));
        compareCoverPhoto(Classes.getResourceAsStream(IMG_FILE_PATH), new ByteArrayInputStream(Base64.decode(coverPhotoB64)));
    }

    private void compareCoverPhoto(InputStream expected, InputStream actual) throws Exception {
        int expectedByte = -1;
        int position = 0;
        while ((expectedByte = expected.read()) != -1) {
            Assert.assertEquals("coverPhoto is corrupted at " + position, expectedByte, actual.read());
            position++;
        }
    }
    @Test 
    public void testOrderMarshal() throws Exception {
        QName FROM_TYPE =
                new QName("urn:switchyard-quickstart:transform-jaxb:1.0", "order");

        Order order = new Order();
        order.setItemId("BUTTER");
        order.setOrderId("PO-19838-XYZ");
        order.setQuantity(200);

        JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { Order.class });
        StringWriter resultWriter = new StringWriter();
        Marshaller marshaller = jaxbContext.createMarshaller();
        JAXBElement<Order> jaxbOrder = new JAXBElement<Order>(FROM_TYPE, Order.class, order);

        marshaller.marshal(jaxbOrder, resultWriter);
        //        _testKit.compareXMLToResource(resultWriter.toString(), ORDER_XML);
    }

    @Test
    public void test_configRead() throws IOException {
        Transformer unmarshalingTransformer = getTransformer("switchyard-config-01.xml");
        Assert.assertEquals("A", unmarshalingTransformer.getFrom().toString());
        Assert.assertEquals("java:org.switchyard.transform.jaxb.internal.POType", unmarshalingTransformer.getTo().toString());

        Transformer marshalingTransformer = getTransformer("switchyard-config-02.xml");
        Assert.assertEquals("java:org.switchyard.transform.jaxb.internal.POType", marshalingTransformer.getFrom().toString());
        Assert.assertEquals("A", marshalingTransformer.getTo().toString());
    }

    @Test
    public void test_configReadContextPath() throws IOException {
        SwitchYardModel config = new ModelPuller<SwitchYardModel>().pull("org/switchyard/transform/jaxb/internal/switchyard-config-03.xml", JAXBTransformerTest.class);
        for (TransformModel tm : config.getTransforms().getTransforms()) {
            Assert.assertTrue(tm instanceof JAXBTransformModel);
            JAXBTransformModel jaxbModel = (JAXBTransformModel)tm;
            Assert.assertEquals("org.switchyard.transform.jaxb.internal", jaxbModel.getContextPath());
            Assert.assertEquals(true, jaxbModel.isXOPPackageEnabled());
            if (jaxbModel.getTo().getLocalPart().equals("A")) {
                Assert.assertEquals(true, jaxbModel.isAttachmentEnabled());
            } else {
                Assert.assertEquals(false, jaxbModel.isAttachmentEnabled());
            }
        }
    }

    @Test
    public void test_createMissingFactoryMethodMessage() throws IOException, SAXException {
        String message = JAXBTransformerFactory.createMissingFactoryMethodMessage(
                USAddress.class,
                ObjectFactory.class);
        boolean messageContains = message.contains("SWITCHYARD016840");
        Assert.assertTrue(messageContains);
    }

    private static final String ORDER_XML = "<?xml version=\"1.0\"?>\n" +
            "<order>\n" +
            "<orderId>PO-19838-XYZ</orderId>\n" +
            "<itemId>BUTTER</itemId>\n" +
            "<quantity>200</quantity>\n" +
            "</order>";


    private static final String PO_XML = "<?xml version=\"1.0\"?>\n" +
            "<purchaseOrder orderDate=\"1999-10-20\">\n" +
            "    <shipTo country=\"US\">\n" +
            "        <name>Alice Smith</name>\n" +
            "        <street>123 Maple Street</street>\n" +
            "        <city>Cambridge</city>\n" +
            "        <state>MA</state>\n" +
            "        <zip>12345</zip>\n" +
            "    </shipTo>\n" +
            "    <billTo country=\"US\">\n" +
            "        <name>Robert Smith</name>\n" +
            "        <street>8 Oak Avenue</street>\n" +
            "        <city>Cambridge</city>\n" +
            "        <state>MA</state>\n" +
            "        <zip>12345</zip>\n" +
            "    </billTo>\n" +
            "    <items>\n" +
            "        <item partNum=\"242-NO\" >\n" +
            "            <productName>Nosferatu - Special Edition (1929)</productName>\n" +
            "            <quantity>5</quantity>\n" +
            "            <USPrice>19.99</USPrice>\n" +
            "        </item>\n" +
            "        <item partNum=\"242-MU\" >\n" +
            "            <productName>The Mummy (1959)</productName>\n" +
            "            <quantity>3</quantity>\n" +
            "            <USPrice>19.98</USPrice>\n" +
            "        </item>\n" +
            "        <item partNum=\"242-GZ\" >\n" +
            "            <productName>Godzilla and Mothra: Battle for Earth/Godzilla vs. King Ghidora</productName>\n" +
            "            <quantity>3</quantity>\n" +
            "            <USPrice>27.95</USPrice>\n" +
            "            <coverPhoto><xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:coverphoto.jpg\"/></coverPhoto>\n" +
            "        </item>\n" +
            "    </items>\n" +
            "</purchaseOrder>";
}
