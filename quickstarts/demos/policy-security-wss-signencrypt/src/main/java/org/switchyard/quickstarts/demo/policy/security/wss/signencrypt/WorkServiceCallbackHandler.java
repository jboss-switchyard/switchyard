/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.switchyard.quickstarts.demo.policy.security.wss.signencrypt;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

public class WorkServiceCallbackHandler implements CallbackHandler {

    private Map<String, String> _passwords = new HashMap<String, String>();

    public WorkServiceCallbackHandler() {
        _passwords.put("alice", "password");
        _passwords.put("bob", "password");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(Callback[] arg0) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < arg0.length; i++) {
            try {
                // A dirty hack to get this working with both of CXF2 and CXF3.
                // WSPasswordCallback has been moved to another package in CXF3
                Method getIdentifierMethod = arg0[i].getClass().getMethod("getIdentifier", new Class[0]);
                Method setPasswordMethod = arg0[i].getClass().getMethod("setPassword", new Class[] {String.class});
                String identifier = (String) getIdentifierMethod.invoke(arg0[i], new Object[0]);
                String password = _passwords.get(identifier);
                if (password != null) {
                    setPasswordMethod.invoke(arg0[i], new Object[] {password});
                    return;
                }
            } catch (Exception e) {
                throw new UnsupportedCallbackException(arg0[i], e.getMessage());
            }
        }
    }

}
