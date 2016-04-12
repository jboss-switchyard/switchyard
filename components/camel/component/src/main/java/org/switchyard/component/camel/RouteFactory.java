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
package org.switchyard.component.camel;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.spring.CamelContextFactoryBean;
import org.apache.camel.spring.CamelEndpointFactoryBean;
import org.switchyard.SwitchYardException;
import org.switchyard.common.camel.CamelModelFactory;
import org.switchyard.common.camel.SwitchYardCamelContext;
import org.switchyard.common.property.PropertyResolver;
import org.switchyard.common.type.Classes;
import org.switchyard.component.camel.model.CamelComponentImplementationModel;

/**
 * Creates RouteDefinition instances based off of a class containing @Route
 * methods and Java DSL route definitions.
 */
public final class RouteFactory {

    /** 
     * Utility class - so no need to directly instantiate.
     */
    private RouteFactory() {
        
    }

    /**
     * Returns a list of route definitions referenced by a camel implementation.
     * @param model implementation config model
     * @param camelContext CamelContext
     * @return list of route definitions
     */
    public static List<RouteDefinition> getRoutes(CamelComponentImplementationModel model, SwitchYardCamelContext camelContext) {
        if (model.getJavaClass() != null) {
            return createRoute(model.getJavaClass(), camelContext, model.getComponent().getTargetNamespace());
        }

        return loadRoute(model.getXMLPath(), camelContext, model.getModelConfiguration().getPropertyResolver());
    }

    /**
     * Loads a set of route definitions from an XML file.
     * 
     * @param xmlPath
     *            path to the file containing one or more route definitions
     * @param camelContext
     *            CamelContext
     * @param propertyResolver
     *            The property Resolver
     * @return list of route definitions
     */
    public static List<RouteDefinition> loadRoute(String xmlPath, SwitchYardCamelContext camelContext,
            PropertyResolver propertyResolver) {
        List<RouteDefinition> routes = null;
        
        try {
            Object obj = CamelModelFactory.createCamelModelObjectFromXML(xmlPath);
            
            // Look for top-level element - camelContext, routes or route
            if (obj instanceof CamelContextFactoryBean) {
                routes = processCamelContextElement((CamelContextFactoryBean)obj, camelContext);
            } else if (obj instanceof RoutesDefinition) {
                routes = ((RoutesDefinition)obj).getRoutes();
            } else if (obj instanceof RouteDefinition) {
                routes = new ArrayList<RouteDefinition>(1);
                routes.add((RouteDefinition)obj);
            }
            
            // If we couldn't find a route definition, throw an error
            if (routes == null) {
                CamelComponentMessages.MESSAGES.noRoutesFoundInXMLFile(xmlPath);
            }


            return routes;
        } catch (Exception e) {
            throw new SwitchYardException(e);
        }
    }

    /**
     * Loads a set of route definitions from an XML file.
     * @param xmlPath path to the file containing one or more route definitions
     * @return list of route definitions
     */
    public static List<RouteDefinition> loadRoute(String xmlPath) {
        return loadRoute(xmlPath, null, null);
    }

    private static List<RouteDefinition> processCamelContextElement(CamelContextFactoryBean camelContextFactoryBean, SwitchYardCamelContext camelContext) throws Exception {
        if (camelContext != null) {
            if (camelContextFactoryBean.getEndpoints() != null) {
                // processing camelContext/endpoint
                for (CamelEndpointFactoryBean epBean : camelContextFactoryBean.getEndpoints()) {
                    epBean.setCamelContext(camelContext);
                    camelContext.getWritebleRegistry().put(epBean.getId(), epBean.getObject());
                }
            }
            if (camelContextFactoryBean.getDataFormats() != null) {
                // processing camelContext/dataFormat
                for (DataFormatDefinition dataFormatDef : camelContextFactoryBean.getDataFormats().getDataFormats()) {
                    camelContext.getDataFormats().put(dataFormatDef.getId(), dataFormatDef);
                }
            }
        }
        return camelContextFactoryBean.getRoutes();
    }

    /**
     * Create a new route from the given class name and service name.
     * @param className name of the class containing an @Route definition
     * @return the route definition
     */
    public static List<RouteDefinition> createRoute(String className) {
        return createRoute(className, null, null);
    }

    /**
     * Create a new route from the given class name and service name.
     * @param className name of the class containing an @Route definition
     * @param namespace the namespace to append to switchyard:// service URIs
     * @return the route definition
     */
    public static List<RouteDefinition> createRoute(String className, String namespace) {
        return createRoute(className, null, namespace);
    }

    /**
     * Create a new route from the given class name and service name.
     * @param className name of the class containing an @Route definition
     * @param camelContext CamelContext
     * @param namespace the namespace to append to switchyard:// service URIs
     * @return the route definition
     */
    public static List<RouteDefinition> createRoute(String className, SwitchYardCamelContext camelContext, String namespace) {
        return createRoute(Classes.forName(className), camelContext, namespace);
    }

    /**
     * Create a new route from the given class and service name.
     * @param routeClass class containing an @Route definition
     * @return the route definition
     */
    public static List<RouteDefinition> createRoute(Class<?> routeClass) {
        return createRoute(routeClass, null, null);
    }

    /**
     * Create a new route from the given class and service name.
     * @param routeClass class containing an @Route definition
     * @param namespace the namespace to append to switchyard:// service URIs
     * @return the route definition
     */
    public static List<RouteDefinition> createRoute(Class<?> routeClass, SwitchYardCamelContext camelContext, String namespace) {
        if (!RouteBuilder.class.isAssignableFrom(routeClass)) {
            throw CamelComponentMessages.MESSAGES.javaDSLClassMustExtend(routeClass.getName(),
                    RouteBuilder.class.getName());
        }

        // Create the route and tell it to create a route
        RouteBuilder builder;
        try {
            builder = (RouteBuilder) routeClass.newInstance();
            if (camelContext != null) {
                builder.setContext(camelContext);
            }
            builder.configure();
            List<RouteDefinition> routes = builder.getRouteCollection().getRoutes();
            if (routes.isEmpty()) {
                throw CamelComponentMessages.MESSAGES.noRoutesFoundinJavaDSLClass(routeClass.getName());
            }
            return routes;
        } catch (Exception ex) {
            throw CamelComponentMessages.MESSAGES.failedToInitializeDSLClass(routeClass.getName(), ex);
        }
    }

}
