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
package org.switchyard.component.camel.deploy;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.HandlerException;
import org.switchyard.component.camel.deploy.support.CustomException;
import org.switchyard.component.camel.deploy.support.UndeclaredFaultException;
import org.switchyard.component.test.mixins.cdi.CDIMixIn;
import org.switchyard.test.InvocationFaultException;
import org.switchyard.test.Invoker;
import org.switchyard.test.ServiceOperation;
import org.switchyard.test.SwitchYardRunner;
import org.switchyard.test.SwitchYardTestCaseConfig;

/**
 * Test for {@link CamelActivator} that uses a implementation.camel and
 * test error handling.
 * 
 * @author Daniel Bevenius
 */
@RunWith(SwitchYardRunner.class)
@SwitchYardTestCaseConfig(config = "switchyard-activator-impl-declared-fault.xml", mixins = CDIMixIn.class)
public class CamelImplementationDeclaredFaultHandlingTest {

    @ServiceOperation("OrderService.getTitleForItem")
    private Invoker _getTitleForItem;

    @ServiceOperation("OrderServiceInOnly.getTitleForItem")
    private Invoker _getTitleForItemInOnly;

    private CamelContext _camelContext;

    @Test
    public void shouldThrowDeclaredExceptionFromCamelRoute() throws Exception {
        final MockEndpoint endpoint = _camelContext.getEndpoint("mock://throw", MockEndpoint.class);
        endpoint.whenAnyExchangeReceived(new DeclaredExceptionThrowingProcessor());
        try {
            _getTitleForItem.sendInOut("10");
            Assert.fail("Expected Exception was not thrown");
        } catch (InvocationFaultException e) {
            e.printStackTrace();
            Throwable cause = e.getCause();
            Assert.assertTrue("Unexpected Exception is thrown: " + cause.getClass().getName(), cause instanceof CustomException);
            Assert.assertEquals("dummy exception", cause.getMessage());
        }
    }

    @Test
    public void shouldWrapUndeclaredExceptionWithHandlerException() throws Exception {
        final MockEndpoint endpoint = _camelContext.getEndpoint("mock://throw", MockEndpoint.class);
        endpoint.whenAnyExchangeReceived(new UndeclaredExceptionThrowingProcessor());
        try {
            _getTitleForItem.sendInOut("10");
            Assert.fail("Expected Exception was not thrown");
        } catch (InvocationFaultException e) {
            e.printStackTrace();
            Throwable he = e.getCause();
            Assert.assertTrue("Undeclared fault should be wrapped with HandlerException", he instanceof HandlerException);
            Throwable cause = he.getCause();
            Assert.assertTrue("Unexpected Exception is thrown: " + cause.getClass().getName(), cause instanceof UndeclaredFaultException);
            Assert.assertEquals("dummy undeclared exception", cause.getMessage());
        }
    }

    @Test
    public void shouldWrapDeclaredExceptionWithHandlerExceptionOnInOnly() throws Exception {
        final MockEndpoint endpoint = _camelContext.getEndpoint("mock://throw", MockEndpoint.class);
        endpoint.whenAnyExchangeReceived(new DeclaredExceptionThrowingProcessor());
        try {
            _getTitleForItemInOnly.sendInOnly("10");
            Assert.fail("Expected Exception was not thrown");
        } catch (InvocationFaultException e) {
            e.printStackTrace();
            Throwable he = e.getCause();
            Assert.assertTrue("On IN_ONLY, any fault should be wrapped with HandlerException", he instanceof HandlerException);
            Throwable cause = he.getCause();
            Assert.assertTrue("Unexpected Exception is thrown: " + cause.getClass().getName(), cause instanceof CustomException);
            Assert.assertEquals("dummy exception", cause.getMessage());
        }
    }

    @Test
    public void shouldWrapUndeclaredExceptionWithHandlerExceptionOnInOnly() throws Exception {
        final MockEndpoint endpoint = _camelContext.getEndpoint("mock://throw", MockEndpoint.class);
        endpoint.whenAnyExchangeReceived(new UndeclaredExceptionThrowingProcessor());
        try {
            _getTitleForItemInOnly.sendInOnly("10");
            Assert.fail("Expected Exception was not thrown");
        } catch (InvocationFaultException e) {
            e.printStackTrace();
            Throwable he = e.getCause();
            Assert.assertTrue("On IN_ONLY, any fault should be wrapped with HandlerException", he instanceof HandlerException);
            Throwable cause = he.getCause();
            Assert.assertTrue("Unexpected Exception is thrown: " + cause.getClass().getName(), cause instanceof UndeclaredFaultException);
            Assert.assertEquals("dummy undeclared exception", cause.getMessage());
        }
    }

    private class DeclaredExceptionThrowingProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            throw new CustomException("dummy exception");
        }
    }

    private class UndeclaredExceptionThrowingProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            throw new UndeclaredFaultException("dummy undeclared exception");
        }
    }
}
