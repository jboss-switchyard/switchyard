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
package org.switchyard.component.itests.camelcontext.domain;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.apache.camel.CamelContext;
import org.apache.camel.ManagementStatisticsLevel;
import org.apache.camel.builder.DefaultErrorHandlerBuilder;
import org.apache.camel.builder.ErrorHandlerBuilder;
import org.apache.camel.builder.ErrorHandlerBuilderRef;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.model.dataformat.JsonDataFormat;
import org.apache.camel.processor.interceptor.DefaultTraceFormatter;
import org.apache.camel.processor.interceptor.Tracer;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.component.test.mixins.cdi.CDIMixIn;
import org.switchyard.test.SwitchYardRunner;
import org.switchyard.test.SwitchYardTestCaseConfig;

/**
 * Functional test for a camelContext XML configuration used by SwitchYard domain.
 * 
 */
@Named("camelContextManagedBeanProducer")
@RunWith(SwitchYardRunner.class)
@SwitchYardTestCaseConfig(
        config = "switchyard-camel-context-configuration-domain-test.xml"
        , mixins = CDIMixIn.class)
public class DomainCamelContextConfigurationTest  {
    
    private static final Logger LOG = Logger.getLogger(DomainCamelContextConfigurationTest.class);
    
    private CamelContext _camelContext;
    
    @Test
    public void testConfiguration() throws Exception {
        Assert.assertNotNull(_camelContext.getProperty("abc"));
        Assert.assertEquals("xyz", _camelContext.getProperty("abc"));
        Assert.assertEquals("foobar-camel-context", _camelContext.getName());
        Assert.assertEquals(true, _camelContext.isUseMDCLogging());
        Assert.assertEquals(ManagementStatisticsLevel.RoutesOnly
                , _camelContext.getManagementStrategy().getStatisticsLevel());
        Assert.assertEquals(false, _camelContext.isAllowUseOriginalMessage());
        Assert.assertEquals(true, _camelContext.isStreamCaching());
        DataFormatDefinition dfd = _camelContext.getDataFormats().get("transform-json");
        Assert.assertNotNull(dfd);
        Assert.assertEquals("json-jackson", dfd.getDataFormatName());
        Assert.assertTrue(dfd instanceof JsonDataFormat);
        
        MockEndpoint mock = _camelContext.getEndpoint("mock:output", MockEndpoint.class);
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("foobar-input");
        _camelContext.createProducerTemplate().sendBody("direct:input", "foobar-input");
        mock.assertIsSatisfied();
        
        // CamelContext should be able to find CDI beans produced by this class from registry.
        Assert.assertEquals(true, _camelContext.isTracing());
        DefaultTraceFormatter formatter =
                (DefaultTraceFormatter) ((Tracer)_camelContext.getDefaultTracer()).getFormatter();
        Assert.assertEquals(false, formatter.isShowBody());
        Assert.assertEquals(false, formatter.isShowBreadCrumb());
        Assert.assertEquals(100, formatter.getMaxChars());
        
        @SuppressWarnings("deprecation")
        ErrorHandlerBuilder builder = _camelContext.getErrorHandlerBuilder();
        Assert.assertEquals(ErrorHandlerBuilderRef.class, builder.getClass());
        Assert.assertEquals("foobarErrorHandler", ((ErrorHandlerBuilderRef)builder).getRef());
    }
    
    @Produces @Named("traceFormatter")
    public DefaultTraceFormatter getTraceFormatter() {
        DefaultTraceFormatter formatter = new DefaultTraceFormatter();
        formatter.setShowBody(false);
        formatter.setShowBreadCrumb(false);
        formatter.setMaxChars(100);
        return formatter;
    }
    
    @Produces @Named("foobarErrorHandler")
    public ErrorHandlerBuilder getErrorHandler() {
        LOG.info("Creating custom ErrorHandler - " + this);
        return new DefaultErrorHandlerBuilder().disableRedelivery();
    }

}

