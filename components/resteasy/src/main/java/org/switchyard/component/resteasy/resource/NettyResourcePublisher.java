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
 
package org.switchyard.component.resteasy.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.switchyard.ServiceDomain;
import org.switchyard.component.common.Endpoint;
import org.switchyard.component.resteasy.util.RESTEasyUtil;

/**
 * Publishes standalone RESTEasy resource to Netty.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2013 Red Hat Inc.
 */
public class NettyResourcePublisher implements ResourcePublisher {
    
    private static final Logger LOGGER = Logger.getLogger(NettyResourcePublisher.class);
    
    /** 
     * The global standalone NettyServer.
     *
     * Keep org.switchyard.component.resteasy.resource.NettyJaxrsServer class untill 
     * https://issues.jboss.org/browse/RESTEASY-794 moves to a released AS7 version that we can use.
     */
    private static NettyJaxrsServer _nettyServer;

    static {
        ResteasyDeployment deployment = new ResteasyDeployment();
        _nettyServer = new NettyJaxrsServer();
        _nettyServer.setRootResourcePath("");
        _nettyServer.setSecurityDomain(null);
        _nettyServer.setDeployment(deployment);
        _nettyServer.start();
    }
    
    /**
     * {@inheritDoc}
     */
    public Endpoint publish(final ServiceDomain domain, final String context, final List<Object> instances, final Map<String, String> contextParams) throws Exception {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Publishing StandaloneResource: resouces=" + instances + ", contextParams=" + contextParams);
        }
        
        _nettyServer.stop();
        // CAUTION: Note that this publisher ignores context. Use it only for test purpose.
        for (Object instance : instances) {
            _nettyServer.getDeployment().getResources().add(instance);
        }
        
        final List<String> providers = RESTEasyUtil.getParamValues(contextParams, ResteasyContextParameters.RESTEASY_PROVIDERS);
        _nettyServer.getDeployment().getScannedProviderClasses().addAll(providers != null ? providers : Collections.<String>emptyList());
        
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Published StandaloneResource - Restarting Netty JAXRS Server: resources=" + instances + ", providers=" + providers);
        }
        _nettyServer.start();

        return new StandaloneResource(new StandaloneResource.Callback() {
            @Override
            public void onStart() {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Starting StandaloneResource: resources=" + instances + ", providers=" + providers);
                }
            }
            
            @Override
            public void onStop() {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Stopping StandaloneResource: resouces=" + instances + ", providers=" + providers);
                }
                _nettyServer.stop();

                List<Object> resources = new ArrayList<Object>(_nettyServer.getDeployment().getResources());
                for (int i=0; instances != null && i<instances.size(); i++) {
                    if (instances.get(i) != null) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("Removing RESTEasy Resource: " + instances.get(i));
                        }
                        resources.remove(instances.get(i));
                    }
                }
                _nettyServer.getDeployment().setResources(resources);
                List<String> scannedProviders = new ArrayList<String>(_nettyServer.getDeployment().getScannedProviderClasses());
                for (int i=0; providers != null && i<providers.size(); i++) {
                    if (providers.get(i) != null) {
                        scannedProviders.remove(providers.get(i));
                    }
                }
                _nettyServer.getDeployment().setScannedProviderClasses(scannedProviders);
                
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Stopped StandaloneResource - Restarting Netty JAXRS Server: resources=" + resources + ", providers=" + scannedProviders);
                }
                _nettyServer.start();
                
            }
        });

    }
    
}
