package org.switchyard.common.camel;

import static org.jboss.logging.Logger.Level.WARN;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * <p/>
 * This file is using the subset 13200-13299 for logger messages.
 * <p/>
 *
 */
@MessageLogger(projectCode = "SWITCHYARD")
public interface CommonCamelLogger {
    /**
     * Default root logger.
     */
    CommonCamelLogger ROOT_LOGGER = Logger.getMessageLogger(CommonCamelLogger.class, CommonCamelLogger.class.getPackage().getName());

    /**
     * cdiNotDetected method definition.
     */
    @LogMessage(level = WARN) 
    @Message(id = 13200, value="CDI environment not detected, disabling Camel CDI integration")
    void cdiNotDetected();
    
    /**
     * camelContextConfigurationError method definition.
     * @param propertyName name of the property
     * @param propertyValue value of the property
     * @param error error when configuring property
     */
    @LogMessage(level = WARN) 
    @Message(id = 13201, value="Unable to set camel context configuration [name = %s, value = %s] : %s")
    void camelContextConfigurationError(String propertyName, Object propertyValue, Exception error);

    /**
     * unableToRegisterBean method definition.
     * @param id id
     * @param error error
     */
    @LogMessage(level = WARN)
    @Message(id = 13202, value="Unable to register bean '%s' due to '%s' - Ignoring")
    void unableToRegisterBean(String id, Exception error);

    /**
     * ignoringMultipleCamelContextElement method definition.
     * @param xmlPath XML path
     */
    @LogMessage(level = WARN)
    @Message(id = 13203, value = "Multiple camelContext elements are found in '%s'. Using first one and ignoring the others")
    void ignoringMultipleCamelContextElement(String xmlPath);

    /**
     * ignoringUnsupportedElement method definition.
     * @param xmlPath XML path
     * @param element unsupported XML element QName
     */
    @LogMessage(level = WARN)
    @Message(id = 13204, value = "CamelContext configuration file '%s' contains '%s' element, which is not supported in SwitchYard. Ignoring")
    void ignoringUnsupportedElement(String xmlPath, QName element);
}
