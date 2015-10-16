package org.switchyard.remote;

import java.net.MalformedURLException;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.switchyard.SwitchYardException;

/**
 * <p/>
 * This file is using the subset 13400-13599 for logger messages.
 * <p/>
 *
 */
@MessageBundle(projectCode = "SWITCHYARD")
public interface RemoteMessages {
    /**
     * Default messages. 
     */
    RemoteMessages MESSAGES = Messages.getBundle(RemoteMessages.class);

    /**
     * noRemoteEndpointFound method definition.
     * @param service service
     * @return SwitchYardException
     */
    @Message(id = 13400, value = "No remote endpoints found for service %s")
    SwitchYardException noRemoteEndpointFound(String service);

    /**
     * invalidURLForEndpoint method definition.
     * @param endpoint endpoint
     * @param mue mue
     * @return IllegalArgumentException
     */
    @Message(id = 13401, value = "Invalid URL for remote endpoint: %s")
    IllegalArgumentException invalidURLForEndpoint(String endpoint, @Cause MalformedURLException mue);

    /**
     * unsupportedWebServiceSecurityHeaderType method definition.
     * @param type type
     * @return IllegalArgumentException
     */
    @Message(id = 13402, value = "Unsupported Web Service Security header type '%s': only String or DOM Node is supported")
    IllegalArgumentException unsupportedWebServiceSecurityHeaderType(String type);

    /**
     * invalidWebServiceSecurityHeader method definition.
     * @param wsse wsse
     * @param e cause
     * @return IllegalArgumentException
     */
    @Message(id = 13403, value = "Invalid Web Service Security header '%s'")
    IllegalArgumentException invalidWebServiceSecurityHeader(Object wsse, @Cause Exception e);
}
