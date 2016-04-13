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
package org.switchyard.validate;

import org.switchyard.HandlerException;

/**
 * Represents a validation failure when handling an exchange.
 */
public class ValidationFailureException extends HandlerException {

    private static final long serialVersionUID = -432799653005625066L;
    
    private Validator<?> _validator;
    private ValidationResult _result;

    /**
     * Create a new ValidationFailureException with the specified error message.
     * @param validator Validator instance which has failed
     * @param result ValidationResult
     * @param message error message
     */
    public ValidationFailureException(Validator<?> validator, ValidationResult result, String message) {
        super(message);
        _validator = validator;
        _result = result;
    }
    
    /**
     * Create a new ValidationFailureException with the specified cause.
     * @param validator Validator instance which has failed
     * @param cause error causing the handler to fail processing
     * @param message error message
     */
    public ValidationFailureException(Validator<?> validator, Throwable cause, String message) {
        super(message, cause);
        _validator = validator;
    }
    
    /**
     * Gets a validator instance which has failed.
     * @return Validator
     */
    public Validator<?> getValidator() {
        return _validator;
    }
    
    /**
     * Gets ValidationResult.
     * @return ValidationResult
     */
    public ValidationResult getValidationResult() {
        return _result;
    }
}
