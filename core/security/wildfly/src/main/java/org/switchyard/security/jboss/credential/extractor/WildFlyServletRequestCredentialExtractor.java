/*
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors.
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
package org.switchyard.security.jboss.credential.extractor;

import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;
import javax.servlet.ServletRequest;

import org.jboss.security.SecurityContextAssociation;
import org.switchyard.security.credential.Credential;
import org.switchyard.security.credential.SubjectCredential;
import org.switchyard.security.credential.extractor.DefaultServletRequestCredentialExtractor;

/**
 * WildFlyServletRequestCredentialExtractor.
 */
public class WildFlyServletRequestCredentialExtractor extends DefaultServletRequestCredentialExtractor {

    /**
     * Constructs a new WildFlyServletRequestCredentialExtractor.
     */
    public WildFlyServletRequestCredentialExtractor() {}

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Credential> extract(ServletRequest source) {
        Set<Credential> credentials = new HashSet<Credential>();
        if (source != null) {
            credentials.addAll(super.extract(source));
            Subject subject = SecurityContextAssociation.getSubject();
            if (subject != null) {
                credentials.add(new SubjectCredential(subject));
            }
        }
        return credentials;
    }

}
