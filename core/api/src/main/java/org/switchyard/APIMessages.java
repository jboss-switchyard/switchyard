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

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 * <p/>
 * This file is using the subset 10000-10199 for logger messages.
 * <p/>
 *
 */
@MessageBundle(projectCode = "SWITCHYARD")
public interface APIMessages {
    /**
     * The default messages.
     */
    APIMessages MESSAGES = Messages.getBundle(APIMessages.class);

    /**
     * unexpectedInterrupt method definition.
     * @return IllegalStateException
     */
    @Message(id = 10003, value = "Unexpected Interrupt exception.")
    IllegalStateException unexpectedInterrupt();

    /**
     * serviceOpNeedOneParamater method definition.
     * @return RuntimeException
     */
    @Message(id = 10004, value = "Service operations on a Java interface must have exactly one parameter.")
    RuntimeException serviceOpNeedOneParamater();

    /**
     * serviceOpOnlyOneParameter method definition.
     * @return SwitchYardException
     */
    @Message(id = 10005, value = "Service operations on a Java interface can only throw one type of exception.")
    SwitchYardException serviceOpOnlyOneParameter();

    /**
     * invalidPolicyName method definition.
     * @param name name
     * @return Exception
     */
    @Message(id = 10006, value = "Invalid policy name: %s doesn't exist.")
    Exception invalidPolicyName(String name);

    /**
     * nullTypeNamePassed method definition.
     * @return IllegalArgumentException
     */
    @Message(id = 10009, value = "null 'typeName' arg passed.")
    IllegalArgumentException nullTypeNamePassed();
    
    /**
     * unrecognizedURI method definition.
     * @param uri uri
     * @return IllegalArgumentException
     */
    @Message(id = 10010, value = "Unrecognized URI: %s")
    IllegalArgumentException unrecognizedURI(String uri);
 
    
    /**
     * unableUpdateMetadataType method definition.
     * @param klass klass
     * @return IllegalArgumentException
     */
    @Message(id = 10011, value = "Unable to update metadata type %s")
    IllegalArgumentException unableUpdateMetadataType(String klass);

    /**
     * transformerFailed method definition.
     * @param clazz transformer class
     * @param fromType from type
     * @param from from
     * @param toType to type
     * @param to to
     * @param e cause
     * @return failure message
     */
    @Message(id = 10012, value = "Transformer:[class='%s', fromType='%s', from='%s', toType='%s', to='%s'] failed")
    String transformerFailed(String clazz, String fromType, String from, String toType, String to, @Cause SwitchYardException e);
    
}
