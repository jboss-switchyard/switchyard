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
package org.switchyard.policy;

import java.util.Set;
import org.switchyard.HandlerException;

/**
 * Represents a policy violation when handling an exchange.
 */
public class PolicyViolationException extends HandlerException {

    private static final long serialVersionUID = -2968953311317589557L;
    
    private Set<Policy> _violatedPolicies;
    
    /**
     * Create a new PolicyViolationException with violated policies and the specified error message.
     * @param policies violated policies
     * @param message error message
     */
    public PolicyViolationException(Set<Policy> policies, String message) {
        super(message);
        _violatedPolicies = policies;
    }
    
    /**
     * Gets violated policies.
     * @return violated policies
     */
    public Set<Policy> getViolatedPolicies() {
        return _violatedPolicies;
    }
}
