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

import org.switchyard.component.common.Endpoint;

/**
 * A standalone RESTEasy resource.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2012 Red Hat Inc.
 */
public class StandaloneResource implements Endpoint {
    
    private final Callback _callback;

    /**
     * Constructor.
     * @param cb cb
     */
    public StandaloneResource(Callback cb) {
        _callback = cb;
    }

    /**
     * {@inheritDoc}
     */
    public void start() {
        _callback.onStart();
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        _callback.onStop();
    }
    
    interface Callback {
        public void onStart();
        public void onStop();
    }
}
