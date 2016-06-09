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

package org.switchyard.transform.config.model.v2;

import org.switchyard.config.Configuration;
import org.switchyard.config.model.Descriptor;
import org.switchyard.transform.config.model.JAXBTransformModel;
import org.switchyard.transform.config.model.v1.V1JAXBTransformModel;
import org.switchyard.transform.internal.TransformerFactoryClass;
import org.switchyard.transform.jaxb.internal.JAXBTransformerFactory;

/**
 * A version 2 JAXBTransformModel.
 */
@TransformerFactoryClass(JAXBTransformerFactory.class)
public class V2JAXBTransformModel extends V1JAXBTransformModel {

    /**
     * Constructs a new V2JAXBTransformModel.
     * @param namespace namespace
     */
    public V2JAXBTransformModel(String namespace) {
        super(namespace);
    }

    /**
     * Constructs a new V2JAXBTransformModel with the specified Configuration and Descriptor.
     * @param config the Configuration
     * @param desc the Descriptor
     */
    public V2JAXBTransformModel(Configuration config, Descriptor desc) {
        super(config, desc);
    }
    
    @Override
    public boolean isAttachmentEnabled() {
        String attachmentEnabled = getModelAttribute(ENABLE_ATTACHMENT);
        return Boolean.parseBoolean(attachmentEnabled);
    }

    @Override
    public JAXBTransformModel setAttachmentEnabled(boolean attachmentEnabled) {
        setModelAttribute(ENABLE_ATTACHMENT, Boolean.toString(attachmentEnabled));
        return this;
    }

    @Override
    public boolean isXOPPackageEnabled() {
        String xopPackageEnabled = getModelAttribute(ENABLE_XOP_PACKAGE);
        // true by default
        return xopPackageEnabled == null ? true : Boolean.parseBoolean(xopPackageEnabled);
    }

    @Override
    public JAXBTransformModel setXOPPackageEnabled(boolean xopEnabled) {
        setModelAttribute(ENABLE_XOP_PACKAGE, Boolean.toString(xopEnabled));
        return this;
    }
}
