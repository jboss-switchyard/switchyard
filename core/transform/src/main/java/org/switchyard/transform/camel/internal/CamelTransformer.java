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

import javax.xml.namespace.QName;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultRouteContext;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.spi.UnitOfWork;
import org.switchyard.Context;
import org.switchyard.Message;
import org.switchyard.Property;
import org.switchyard.Scope;
import org.switchyard.common.camel.ContextPropertyUtil;
import org.switchyard.common.xml.QNameUtil;
import org.switchyard.config.model.Scannable;
import org.switchyard.label.BehaviorLabel;
import org.switchyard.transform.BaseTransformer;
import org.switchyard.transform.internal.TransformMessages;

/**
 * Camel {@link org.switchyard.transform.Transformer}. This transformer forwards message
 * to the specified Camel Endpoint and then return back the message processed by that Camel
 * Endpoint.
 */
@Scannable(false)
public class CamelTransformer extends BaseTransformer <Message, Message> {

    private Endpoint _endpoint;

    /**
     * Constructor.
     * @param from From type.
     * @param to To type.
     * @param endpoint endpoint.
     */
    protected CamelTransformer(final QName from, final QName to, Endpoint endpoint) {
        super(from, to);
        _endpoint = endpoint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message transform(Message message) {
        try {
            Exchange exchange = _endpoint.createExchange();
            UnitOfWork uow = exchange.getContext().getUnitOfWorkFactory().createUnitOfWork(exchange);
            RouteContext rc = new DefaultRouteContext(exchange.getContext());
            uow.pushRouteContext(rc);
            exchange.setUnitOfWork(uow);
            uow.start();
            exchange.getIn().setBody(message.getContent());
            copyProperties(message.getContext(), exchange);
            
            Producer producer = _endpoint.createProducer();
            producer.process(exchange);
            if (exchange.isFailed()) {
                if (exchange.getException() != null) {
                    throw TransformMessages.MESSAGES.failedToTransformViaCamelEndpoint(_endpoint.getEndpointUri(), exchange.getException());
                } else {
                    throw TransformMessages.MESSAGES.failedToTransformViaCamelEndpoint(_endpoint.getEndpointUri(), exchange.getIn().getBody(String.class));
                }
            }
            if (QNameUtil.isJavaMessageType(getTo())) {
                message.setContent(exchange.getIn().getBody(QNameUtil.toJavaMessageType(getTo())));
            } else {
                message.setContent(exchange.getIn().getBody());
            }
            return message;
        } catch (Exception e) {
            throw TransformMessages.MESSAGES.failedToTransformViaCamelEndpoint(_endpoint.getEndpointUri(), e);
        }
    }
    
    private void copyProperties(Context context, Exchange exchange) {
        for (Property property : context.getProperties()) {
            if (property.hasLabel(BehaviorLabel.TRANSIENT.label()) 
                    || ContextPropertyUtil.isReservedProperty(property.getName(), property.getScope())) {
                continue;
            }

            if (Scope.EXCHANGE.equals(property.getScope())) {
                exchange.setProperty(property.getName(), property.getValue());
            } else {
                exchange.getIn().setHeader(property.getName(), property.getValue());
            }
        }
    }
    
}
