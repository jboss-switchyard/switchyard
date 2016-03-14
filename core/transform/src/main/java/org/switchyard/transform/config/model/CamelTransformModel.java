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

package org.switchyard.transform.config.model;

import org.switchyard.config.model.transform.TransformModel;

/**
 * The model interface for transform.camel.
 * @see <code>CamelTransformer</code>
 */
public interface CamelTransformModel extends TransformModel {

    /** The "camel" name. */
    String CAMEL = "camel";

    /** The "endpointUri" name. */
    String ENDPOINT_URI = "endpointUri";

    /**
     * Get endpoint URI.
     * @return endpoint URI
     */
    String getEndpointUri();

    /**
     * Set endpoint URI.
     * @param uri endpoint URI
     * @return model representation
     */
    CamelTransformModel setXsltFile(String uri);

}
