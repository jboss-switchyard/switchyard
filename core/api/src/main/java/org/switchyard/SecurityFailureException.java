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
package org.switchyard;

/**
 * Represents a security error when handling an exchange.
 */
public class SecurityFailureException extends HandlerException {

    private static final long serialVersionUID = 5823897619210080802L;

    /**
     * Create a new SecurityFailureException with the specified error message.
     * @param message error text
     */
    public SecurityFailureException(String message) {
        super(message);
    }
    
    /**
     * Create a new SecurityFailureException with the specified cause.
     * @param cause error causing the handler to fail processing
     */
    public SecurityFailureException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Create a new SecurityFailureException with the specified error message and cause.
     * @param message error text
     * @param cause error causing the handler to fail processing
     */
    public SecurityFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
