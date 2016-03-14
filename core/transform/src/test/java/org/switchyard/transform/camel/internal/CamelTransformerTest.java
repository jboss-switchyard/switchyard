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

package org.switchyard.transform.camel.internal;

import org.apache.camel.ResolveEndpointFailedException;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.switchyard.Message;
import org.switchyard.MockDomain;
import org.switchyard.ServiceDomain;
import org.switchyard.SwitchYardException;
import org.switchyard.common.camel.CamelContextConfigurator;
import org.switchyard.common.camel.SwitchYardCamelContextImpl;
import org.switchyard.internal.DefaultMessage;
import org.switchyard.transform.AbstractTransformerTestCase;
import org.switchyard.transform.Transformer;

/**
 * Camel transformer tests.
 */
public class CamelTransformerTest extends AbstractTransformerTestCase {
    
    private static final String PATH_CAMEL_CONTEXT_XML = "org/switchyard/transform/camel/internal/camel-context.xml";
    
    private final Logger _logger = Logger.getLogger(CamelTransformerTest.class);
    
    @Test
    public void testEndpoint() throws Exception {
        SwitchYardCamelContextImpl camelContext = new SwitchYardCamelContextImpl();
        ServiceDomain domain = new MockDomain();
        domain.setProperty(CamelContextConfigurator.CAMEL_CONTEXT_CONFIG_XML, PATH_CAMEL_CONTEXT_XML);
        camelContext.setServiceDomain(domain);
        camelContext.start();
        
        Transformer<?,?> transformer = getTransformer("sw-config.xml", domain);
        Assert.assertEquals(CamelTransformer.class, transformer.getClass());
        CamelTransformer camel = CamelTransformer.class.cast(transformer);
        Message message = new DefaultMessage();
        Message output = camel.transform(message.setContent("input"));
        camelContext.stop();
        Assert.assertEquals("input-transformed", output.getContent(String.class));
    }
    
    @Test
    public void testNoEndpoint() throws Exception {
        SwitchYardCamelContextImpl camelContext = new SwitchYardCamelContextImpl();
        ServiceDomain domain = new MockDomain();
        domain.setProperty(CamelContextConfigurator.CAMEL_CONTEXT_CONFIG_XML, PATH_CAMEL_CONTEXT_XML);
        camelContext.setServiceDomain(domain);
        camelContext.start();
        
        Transformer<?,?> transformer = getTransformer("sw-config-no-endpoint.xml", domain);
        Assert.assertEquals(CamelTransformer.class, transformer.getClass());
        CamelTransformer camel = CamelTransformer.class.cast(transformer);
        try {
            Message message = new DefaultMessage();
            camel.transform(message.setContent("input"));
        } catch (SwitchYardException e) {
            _logger.info("Caught expected Exception", e);
            return;
        } finally {
            camelContext.stop();
        }
        Assert.fail("Camel transform succeeded without target endpoint");
    }
    
    @Test
    public void testNoCamelContextConfig() throws Exception {
        SwitchYardCamelContextImpl camelContext = new SwitchYardCamelContextImpl();
        ServiceDomain domain = new MockDomain();
        camelContext.setServiceDomain(domain);
        camelContext.start();
        
        try {
            getTransformer("sw-config-no-camel-context-config.xml", domain);
        } catch (ResolveEndpointFailedException e) {
            _logger.info("Caught expected Exception", e);
            return;
        } finally {
            camelContext.stop();
        }
        Assert.fail("Camel transform succeeded without target endpoint");
    }
    
    @Test
    public void testNoEndpointRef() throws Exception {
        SwitchYardCamelContextImpl camelContext = new SwitchYardCamelContextImpl();
        ServiceDomain domain = new MockDomain();
        domain.setProperty(CamelContextConfigurator.CAMEL_CONTEXT_CONFIG_XML, PATH_CAMEL_CONTEXT_XML);
        camelContext.setServiceDomain(domain);
        camelContext.start();
        
        try {
            getTransformer("sw-config-no-endpoint-ref.xml", domain);
        } catch (ResolveEndpointFailedException e) {
            _logger.info("Caught expected Exception", e);
            return;
        } finally {
            camelContext.stop();
        }
        Assert.fail("Camel transform succeeded without target endpoint");
    }

}
