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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpServer;

import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.jboss.resteasy.plugins.server.sun.http.HttpContextBuilder;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.switchyard.ServiceDomain;
import org.switchyard.component.common.Endpoint;
import org.switchyard.component.resteasy.RestEasyLogger;
import org.switchyard.component.resteasy.util.RESTEasyUtil;

/**
 * Publishes standalone RESTEasy resource.
 * <p>
 *     By default it will be published in port {@value #DEFAULT_PORT}. This can be configured making use of
 *     <i>{@value #DEFAULT_PORT_PROPERTY}</i> system property.
 * </p>
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2012 Red Hat Inc.
 */
@SuppressWarnings("restriction")
public class StandaloneResourcePublisher implements ResourcePublisher {

    private static final Logger LOGGER = Logger.getLogger(StandaloneResourcePublisher.class);

    // The global standalone HttpServer
    private static HttpServer _httpServer;
    private static HttpContextBuilder _contextBuilder;

    static {
        try {
            _contextBuilder = new HttpContextBuilder();
            _httpServer = HttpServer.create(new InetSocketAddress(getPort()), 10);
            _httpServer.setExecutor(null); // creates a default executor
            _httpServer.start();
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Started Sun HttpServer at port " + getPort());
            }
        } catch (IOException ioe) {
            RestEasyLogger.ROOT_LOGGER.unableToLaunchStandaloneHttpServer(ioe);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Endpoint publish(final ServiceDomain domain, final String context, final List<Object> instances, final Map<String, String> contextParams) throws Exception {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Publishing StandaloneResource: context=" + context + ", resources=" + instances + ", contextParams=" + contextParams);
        }
        
        List<Object> resourceInstances = new ArrayList<Object>();
        String path = _contextBuilder.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.equals(context)) {
            _contextBuilder.cleanup();
            try {
                _httpServer.removeContext(_contextBuilder.getPath());
            } catch (IllegalArgumentException iae) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("", iae);
                }
            }
            resourceInstances = _contextBuilder.getDeployment().getResources();
            _contextBuilder.getDeployment().getDefaultContextObjects().clear();
        }
        // Add as singleton instance
        for (Object instance : instances) {
            resourceInstances.add(instance);
        }
        _contextBuilder.getDeployment().setResources(resourceInstances);
        // Register @Provider classes
        final List<String> providers = RESTEasyUtil.getParamValues(contextParams, ResteasyContextParameters.RESTEASY_PROVIDERS);
        _contextBuilder.getDeployment().setScannedProviderClasses(providers != null ? providers : Collections.<String>emptyList());
        _contextBuilder.setPath(context);
        
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Published StandaloneResource - Refreshing Sun HTTP Server: context=" + context + ", resources=" + resourceInstances + ", providers=" + providers);
        }
        _contextBuilder.bind(_httpServer);

        return new StandaloneResource(new StandaloneResource.Callback() {
            @Override
            public void onStart() {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Starting StandaloneResource: context=" + context + ", resources=" + instances + ", providers=" + providers);
                }
            }
            
            @Override
            public void onStop() {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Stopping StandaloneResource: context=" + context + ", resouces=" + instances + ", providers=" + providers);
                }
                
                List<Object> resources = new ArrayList<Object>(_contextBuilder.getDeployment().getResources());
                List<String> scannedProviderClasses = new ArrayList<String>(_contextBuilder.getDeployment().getScannedProviderClasses());
                
                String path = _contextBuilder.getPath();
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                if (path.equals(context)) {
                    _contextBuilder.cleanup();
                    try {
                    _httpServer.removeContext(_contextBuilder.getPath());
                    } catch (IllegalArgumentException iae) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("", iae);
                        }
                        _contextBuilder.getDeployment().getDefaultContextObjects().clear();
                    }
                }
                
                for (int i=0; instances != null && i<instances.size(); i++) {
                    if (instances.get(i) != null) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("Removing RESTEasy Resource: " + instances.get(i));
                        }
                        resources.remove(instances.get(i));
                    }
                }
                _contextBuilder.getDeployment().setResources(resources);
                for (int i=0; providers != null && i<providers.size(); i++) {
                    if (providers.get(i) != null) {
                        scannedProviderClasses.remove(providers.get(i));
                    }
                }
                _contextBuilder.getDeployment().setScannedProviderClasses(scannedProviderClasses);
                
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Stopped StandaloneResource - Refreshing Sun HTTP Server: context=" + context + ", resources=" + resources + ", providers=" + scannedProviderClasses);
                }
                _contextBuilder.setPath(context);
                _contextBuilder.bind(_httpServer);
            }
        });
    }

    /**
     * Returns the port where the standalone publisher will be started
     * @return the port
     */
    static int getPort() {
        return Integer.getInteger(DEFAULT_PORT_PROPERTY, DEFAULT_PORT);
    }
    
}
