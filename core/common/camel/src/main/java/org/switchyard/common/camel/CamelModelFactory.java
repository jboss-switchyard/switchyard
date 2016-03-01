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
package org.switchyard.common.camel;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.model.Constants;
import org.apache.camel.spi.NamespaceAware;
import org.apache.camel.spring.CamelContextFactoryBean;
import org.apache.camel.spring.SpringModelJAXBContextFactory;
import org.switchyard.SwitchYardException;
import org.switchyard.common.type.Classes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Utility class to handle camel JAXB model objects.
 */
public final class CamelModelFactory {

    /**
     * JAXB context for reading XML definitions.
     */
    private static JAXBContext JAXB_CONTEXT;

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(Constants.JAXB_CONTEXT_PACKAGES
                            + SpringModelJAXBContextFactory.ADDITIONAL_JAXB_CONTEXT_PACKAGES
                            , SpringModelJAXBContextFactory.class.getClassLoader());
        } catch (JAXBException e) {
            throw new SwitchYardException(e);
        }
    }

    /** 
     * Utility class - so no need to directly instantiate.
     */
    private CamelModelFactory() {
        
    }

    /**
     * Creates camel model object like CamelContextFactoryBean, RouteDefinition or RoutesDefinition
     * from XML file.
     * 
     * @param xmlPath path to the file
     * @return created camel model object
     * @throws Exception failed to unmarshall camel model object from XML file
     */
    public static Object createCamelModelObjectFromXML(String xmlPath) throws Exception {
        InputStream input = Classes.getResourceAsStream(xmlPath);
        if (input == null) {
            throw new FileNotFoundException(xmlPath);
        }
        
        InputSource source =  new InputSource(input);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(source);
        Element element = document.getDocumentElement();
        Binder<Node> binder = JAXB_CONTEXT.createBinder();
        Object obj = binder.unmarshal(element);
        injectNamespaces(element, binder);
        return obj;
    }
    
    private static void injectNamespaces(Element element, Binder<Node> binder) {
        NodeList list = element.getChildNodes();
        Namespaces namespaces = null;
        int size = list.getLength();
        for (int i = 0; i < size; i++) {
            Node child = list.item(i);
            if (child instanceof Element) {
                Element childElement = (Element) child;
                Object object = binder.getJAXBNode(child);
                if (object instanceof NamespaceAware) {
                    NamespaceAware namespaceAware = (NamespaceAware) object;
                    if (namespaces == null) {
                        namespaces = new Namespaces(element);
                    }
                    namespaces.configure(namespaceAware);
                }
                injectNamespaces(childElement, binder);
            }
        }
    }

    /**
     * Imports CamelContext configuration  into existing SwitchYardCamelContext instance
     * from CamelContextFactoryBean JAXB object.
     * 
     * @param context SwitchYardCamelContext
     * @param bean CamelContextFactoryBean JAXB object
     * @throws Exception failed to import configurations
     */
    public static void importCamelContextFactoryBean(SwitchYardCamelContext context, CamelContextFactoryBean bean) throws Exception {
        new CamelContextFactoryBeanDelegate(context, bean).importConfiguration();
    }

}

