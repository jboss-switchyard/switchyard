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

import java.io.StringWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.Message;
import org.switchyard.ServiceDomain;
import org.switchyard.common.net.SocketAddr;
import org.switchyard.component.soap.PortName;
import org.switchyard.component.soap.config.model.SOAPBindingModel;
import org.switchyard.component.soap.config.model.SOAPNamespace;
import org.switchyard.component.soap.config.model.v1.V1SOAPBindingModel;
import org.switchyard.component.soap.util.WSDLUtil;
import org.switchyard.config.model.ModelPuller;
import org.switchyard.config.model.composite.CompositeModel;
import org.switchyard.config.model.composite.CompositeReferenceModel;
import org.switchyard.config.model.composite.CompositeServiceModel;
import org.switchyard.config.model.composite.v1.V1CompositeReferenceModel;
import org.switchyard.deploy.ServiceDomainManager;
import org.switchyard.metadata.BaseService;
import org.switchyard.metadata.InOutOperation;
import org.switchyard.metadata.ServiceOperation;
import org.switchyard.test.Invoker;
import org.switchyard.test.SwitchYardRunner;
import org.switchyard.test.SwitchYardTestKit;
import org.w3c.dom.Node;

/**
 * Contains tests for standard doclit (non-wrapped doclit).
 *
 * @author Mario Antollini
 */
@RunWith(SwitchYardRunner.class)
public class StandardDocLitTest {

    private SwitchYardTestKit _testKit;
    
    private ServiceDomain _domain = new ServiceDomainManager().createDomain();
    private SOAPBindingModel _config;
    private SOAPBindingModel _config2;
    private static URL _serviceURL;
    private InboundHandler _soapInbound;
    private OutboundHandler _soapOutbound;
    private InboundHandler _soapInbound2;
    private OutboundHandler _soapOutbound2;
    
    @org.switchyard.test.ServiceOperation("{urn:soap:test:1.0}OrderService")
    private Invoker consumerService;

    private static ModelPuller<CompositeModel> _puller;
    
    @Before
    public void setUp() throws Exception {
        
        // Provide a switchyard service
        DoclitSOAPProvider provider = new DoclitSOAPProvider();

        String host = System.getProperty("org.switchyard.test.soap.host", "localhost");
        String port = System.getProperty("org.switchyard.test.soap.port", "48080");

        _puller = new ModelPuller<CompositeModel>();
        CompositeModel composite = _puller.pull("/DoclitSwitchyard.xml", getClass());
        
        CompositeServiceModel compositeService = composite.getServices().get(0);
        _config = (SOAPBindingModel)compositeService.getBindings().get(0);
        _domain.registerService(_config.getServiceName(), new OrderServiceInterface(), provider);
        _domain.registerServiceReference(_config.getServiceName(), new OrderServiceInterface());
        
        _config.setSocketAddr(new SocketAddr(host, Integer.parseInt(port)));
        
        _soapInbound = new InboundHandler(_config, _domain);

        _soapInbound.start();

        _serviceURL = new URL("http://" + host + ":" + port + "/OrderService");

        // A WS Consumer as Service
        SOAPBindingModel outConfig = new V1SOAPBindingModel(SOAPNamespace.DEFAULT.uri()) {
            @Override
            public CompositeReferenceModel getReference() {
                return new V1CompositeReferenceModel();
            }
        };
        outConfig.setWsdl(_serviceURL.toExternalForm() + "?wsdl");
        outConfig.setServiceName(_config.getServiceName());
        outConfig.setName("testGateway");
        _soapOutbound = new OutboundHandler(outConfig);
        _soapOutbound.start();
        _domain.registerService(_config.getServiceName(), new OrderServiceInterface(), _soapOutbound);

        compositeService = composite.getServices().get(1);
        _config2 = (SOAPBindingModel)compositeService.getBindings().get(0);
        _domain.registerService(_config2.getServiceName(), new HeartBeatServiceInterface(), provider);
        _domain.registerServiceReference(_config2.getServiceName(), new HeartBeatServiceInterface());
        _config2.setSocketAddr(new SocketAddr(host, Integer.parseInt(port)));
        _soapInbound2 = new InboundHandler(_config2, _domain);
        _soapInbound2.start();

        _serviceURL = new URL("http://" + host + ":" + port + "/HeartBeatService");
        SOAPBindingModel outConfig2 = new V1SOAPBindingModel(SOAPNamespace.DEFAULT.uri()) {
            @Override
            public CompositeReferenceModel getReference() {
                return new V1CompositeReferenceModel();
            }
        };
        outConfig2.setWsdl(_serviceURL.toExternalForm() + "?wsdl");
        outConfig2.setServiceName(_config2.getServiceName());
        outConfig2.setName("heartBeat");
        _soapOutbound2 = new OutboundHandler(outConfig2);
        _soapOutbound2.start();
        _domain.registerService(_config2.getServiceName(), new HeartBeatServiceInterface(), _soapOutbound2);
        
        XMLUnit.setIgnoreWhitespace(true);
    }

    @After
    public void tearDown() throws Exception {
        // NOOP
        _soapOutbound.stop();
        _soapInbound.stop();
        _soapOutbound2.stop();
        _soapInbound2.stop();
    }

    @Test
    public void standardDocLitOperation() throws Exception {
        
        DOMSource domSource = new DOMSource(_testKit.readResourceDocument("/doclit_request.xml"));
        Message responseMsg = consumerService.operation("submitOrder").sendInOut(toString(domSource.getNode()));
        String response = toString(responseMsg.getContent(Node.class));
        _testKit.compareXMLToResource(response, "/doclit_response.xml");
        
    }

    @Test
    public void standardDocLitNoInputOperation() throws Exception {
        
        try {
            PortName portName = new PortName("HeartBeatService:HeartBeatServicePort");
            WSDLUtil.getOperationByElement(WSDLUtil.getPort(WSDLUtil.getService(WSDLUtil.readWSDL("/DoclitOrderService.wsdl"), portName), portName), QName.valueOf("{urn:soap:test:1.0}ping"), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    private static class OrderServiceInterface extends BaseService {
        private static Set<ServiceOperation> _operations = new HashSet<ServiceOperation>(2);
        static {
            _operations.add(new InOutOperation("submitOrder"));
        }
        public OrderServiceInterface() {
            super(_operations);
        }
    }
    
    private static class HeartBeatServiceInterface extends BaseService {
        private static Set<ServiceOperation> _operations = new HashSet<ServiceOperation>(1);
        static {
            _operations.add(new InOutOperation("heartBeat"));
        }
        public HeartBeatServiceInterface() {
            super(_operations);
        }
    }
    
    private String toString(Node node) throws Exception
    {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();
        StringWriter sw = new StringWriter();
        DOMSource source = new DOMSource(node);
        StreamResult result = new StreamResult(sw);
        transformer.transform(source, result);
        return sw.toString();
    }
    
}
