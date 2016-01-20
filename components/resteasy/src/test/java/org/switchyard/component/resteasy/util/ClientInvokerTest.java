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
 
package org.switchyard.component.resteasy.util;


import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.BaseClientResponse;
import org.jboss.resteasy.util.CaseInsensitiveMap;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.switchyard.Exchange;
import org.switchyard.component.resteasy.InboundHandler;
import org.switchyard.component.resteasy.config.model.v1.V1RESTEasyBindingModel;
import org.switchyard.component.resteasy.util.support.WarehouseResource;
import org.switchyard.config.model.switchyard.SwitchYardNamespace;

/**
 * Tests for ClientInvoker.
 */
public class ClientInvokerTest extends InboundHandler {

    public ClientInvokerTest() {
    }

    @Test
    public void testEndpointUri() throws Exception {
        V1RESTEasyBindingModel model = new V1RESTEasyBindingModel(SwitchYardNamespace.DEFAULT.uri());
        model.setAddress("http://www.example.org/path");
        model.setInterfaces("org.switchyard.component.resteasy.util.support.WarehouseResource");
        
        ClientExecutor mockClientExecutor = Mockito.mock(ClientExecutor.class, Mockito.RETURNS_DEEP_STUBS);
        final BaseClientResponse<?> mockResponse = Mockito.mock(BaseClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        ClientInvoker invoker = new ClientInvoker("http://www.example.org/path"
                                , WarehouseResource.class
                                , WarehouseResource.class.getMethod("getItem", Integer.class)
                                , model);
        invoker.setClientExecutor(mockClientExecutor);
        Exchange mockExchange = Mockito.mock(Exchange.class, Mockito.RETURNS_DEEP_STUBS);

        Mockito.when(mockExchange.getContext().getPropertyValue(ClientInvoker.CONTEXT_PROPERTY_PREFIX + ClientInvoker.KEY_ADDRESS)).thenReturn(null);
        Mockito.when(mockClientExecutor.execute(Mockito.isA(ClientRequest.class))).thenAnswer(new Answer<ClientResponse<?>>() {
            @Override
            public ClientResponse<?> answer(InvocationOnMock invocation) throws Throwable {
                ClientRequest request = invocation.getArgumentAt(0, ClientRequest.class);
                Assert.assertEquals("GET", request.getHttpMethod());
                Assert.assertEquals("http://www.example.org/path/warehouse/1", request.getUri());
                return mockResponse;
            }
        });
        invoker.invoke(mockExchange, new Object[]{new Integer(1)}, new CaseInsensitiveMap<String>());
        
        // Test overriding endpoint address via context property
        Mockito.when(mockExchange.getContext().getPropertyValue(ClientInvoker.CONTEXT_PROPERTY_PREFIX + ClientInvoker.KEY_ADDRESS)).thenReturn("http://www.modified.example.org:8080/modifiedPath");
        Mockito.when(mockClientExecutor.execute(Mockito.isA(ClientRequest.class))).thenAnswer(new Answer<ClientResponse<?>>() {
            @Override
            public ClientResponse<?> answer(InvocationOnMock invocation) throws Throwable {
                ClientRequest request = invocation.getArgumentAt(0, ClientRequest.class);
                Assert.assertEquals("GET", request.getHttpMethod());
                Assert.assertEquals("http://www.modified.example.org:8080/modifiedPath/warehouse/1", request.getUri());
                return mockResponse;
            }
        });
        invoker.invoke(mockExchange, new Object[]{new Integer(1)}, new CaseInsensitiveMap<String>());
}
}
