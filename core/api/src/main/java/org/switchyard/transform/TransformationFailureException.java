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
package org.switchyard.transform;

import org.switchyard.HandlerException;

/**
 * Represents a transformation failure when handling an exchange.
 */
public class TransformationFailureException extends HandlerException {

    private static final long serialVersionUID = 1560153466685669837L;
    
    private Transformer<?,?> _transformer;

    /**
     * Create a new TransformationFailureException with the specified error message.
     * @param message error message
     */
    public TransformationFailureException(String message) {
        super(message);
    }
    
    /**
     * Create a new TransformationFailureException with the specified cause.
     * @param transformer transformer instance which has failed
     * @param cause error causing the handler to fail processing
     * @param message error message
     */
    public TransformationFailureException(Transformer<?,?> transformer, Throwable cause, String message) {
        super(message, cause);
        _transformer = transformer;
    }
    
    /**
     * Gets transformer instance which has failed.
     * @return Transformer transformer instance which has failed
     */
    public Transformer<?,?> getTransformer() {
        return _transformer;
    }
}
