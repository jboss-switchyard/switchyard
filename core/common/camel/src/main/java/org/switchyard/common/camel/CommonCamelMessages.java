package org.switchyard.common.camel;

import java.io.FileNotFoundException;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 * <p/>
 * This file is using the subset 13300-13399 for logger messages.
 * <p/>
 *
 */
@MessageBundle(projectCode = "SWITCHYARD")
public interface CommonCamelMessages {
    /**
     * Default messages.
     */
    CommonCamelMessages MESSAGES = Messages.getBundle(CommonCamelMessages.class);

    /**
     * specifiedCamelContextFileIsNotFound method definition.
     * @param xmlPath path
     * @return FileNotFoundException
     */
    @Message(id = 13300, value = "CamelContext configuration file '%s' is not found")
    FileNotFoundException specifiedCamelContextFileIsNotFound(String xmlPath);
    
    /**
     * noCamelContextElementFound method definition.
     * @param xmlPath path
     * @return IllegalArgumentException
     */
    @Message(id = 13301, value = "Could not extract camelContext configuration from '%s'")
    IllegalArgumentException noCamelContextElementFound(String xmlPath);

}
