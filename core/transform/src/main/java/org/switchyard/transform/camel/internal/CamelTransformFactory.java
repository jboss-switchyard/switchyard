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

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.switchyard.ServiceDomain;
import org.switchyard.common.camel.SwitchYardCamelContext;
import org.switchyard.transform.Transformer;
import org.switchyard.transform.config.model.CamelTransformModel;
import org.switchyard.transform.internal.TransformMessages;
import org.switchyard.transform.internal.TransformerFactory;

import javax.xml.namespace.QName;

/**
 * Camel Transformer factory.
 */
public class CamelTransformFactory implements TransformerFactory<CamelTransformModel> {

    /**
     * Create a {@link Transformer} instance from the supplied {@link CamelTransformModel}.
     * @param domain ServiceDomain instance.
     * @param model The model.
     * @return The Transformer instance.
     */
    public Transformer<?,?> newTransformer(ServiceDomain domain, CamelTransformModel model) {
        QName from = model.getFrom();
        QName to = model.getTo();
        String uri = model.getEndpointUri();
        CamelContext camelContext = (CamelContext)domain.getProperty(SwitchYardCamelContext.CAMEL_CONTEXT_PROPERTY);
        Endpoint endpoint = camelContext.getEndpoint(uri);
        if (endpoint == null) {
            throw TransformMessages.MESSAGES.camelEndpointNotFound(uri);
        }
        return new CamelTransformer(from, to, endpoint);
    }
}
