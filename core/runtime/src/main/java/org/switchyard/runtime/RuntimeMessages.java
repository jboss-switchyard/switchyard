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
package org.switchyard.runtime;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.switchyard.HandlerException;
import org.switchyard.SwitchYardException;
import org.switchyard.TransactionFailureException;

/**
 * <p/>
 * This file is using the subset 14000-14199 for logger messages.
 * <p/>
 *
 */
@MessageBundle(projectCode = "SWITCHYARD")
public interface RuntimeMessages {    
    /**
     * Default messages. 
     */
    RuntimeMessages MESSAGES = Messages.getBundle(RuntimeMessages.class);

    /**
     * validatorFailed method definition.
     * @param clazz validator class name
     * @param type Java type
     * @param name QName
     * @param detail detail
     * @return failure message
     */
    @Message(id = 14000, value = "Validator:[class=%s', type='%s', name='%s'] failed: %s")
    String validatorFailed(String clazz, String type, String name, String detail);

    /**
     * failedToCompleteTransaction method definition.
     * @param e e
     * @return TransactionFailureException
     */
    @Message(id = 14001, value = "TransactionHandler failed to complete a transaction")
    TransactionFailureException failedToCompleteTransaction(@Cause Exception e);

    /**
     * invalidTransactionPolicy method definition.
     * @param policyOne policyOne
     * @param policyTwo policyTwo
     * @return TransactionFailureException
     */
    @Message(id = 14002, value = "Invalid transaction policy : %s and %s cannot be requested simultaneously.")
    TransactionFailureException invalidTransactionPolicy(String policyOne, String policyTwo);

    /**
     * invalidTransactionPolicyCombo method definition.
     * @param policyOne policyOne
     * @param policyTwo policyTwo
     * @param policyThree policyThree
     * @return TransactionFailureException
     */
    @Message(id = 14003, value = "Invalid transaction policy : %s cannot be requested with %s nor %s")
    TransactionFailureException invalidTransactionPolicyCombo(String policyOne, String policyTwo, String policyThree);

    /**
     * noTransactionPropagated method definition.
     * @param policy policy
     * @return TransactionFailureException
     */
    @Message(id = 14004, value = "Invalid transaction status : %s is required but the transaction doesn't exist")
    TransactionFailureException noTransactionPropagated(String policy);

    /**
     * failedCreateNewTransaction method definition.
     * @param e e
     * @return TransactionFailureException
     */
    @Message(id = 14005, value = "Failed to create new transaction")
    TransactionFailureException failedCreateNewTransaction(@Cause Exception e);

    /**
     * transactionAlreadyExists method definition.
     * @return TransactionFailureException
     */
    @Message(id = 14006, value = "Transaction already exists")
    TransactionFailureException transactionAlreadyExists();

    /**
     * failedToRollbackTransaction method definition.
     * @param e e
     * @return TransactionFailureException
     */
    @Message(id = 14007, value = "Failed to rollback transaction")
    TransactionFailureException failedToRollbackTransaction(@Cause Exception e);

    /**
     * failedToCommitTransaction method definition.
     * @param e e
     * @return TransactionFailureException
     */
    @Message(id = 14008, value = "Failed to commit transaction")
    TransactionFailureException failedToCommitTransaction(@Cause Exception e);

    /**
     * failedToCompleteWithStatus method definition.
     * @param status status
     * @return TransactionFailureException
     */
    @Message(id = 14009, value = "Failed to complete transaction due to invalid status - code=%d: "
            + "see javax.transaction.Status.")
    TransactionFailureException failedToCompleteWithStatus(int status);

    /**
     * failedToRetrieveStatus method definition.
     * @param e e
     * @return TransactionFailureException
     */
    @Message(id = 14010, value = "Failed to retrieve transaction status")
    TransactionFailureException failedToRetrieveStatus(@Cause Exception e);

    /**
     * transformationsNotApplied method definition.
     * @param expectedPayload expectedPayload
     * @param actualPayload actualPayload
     * @return errormessage
     */
    @Message(id = 14011, value = "Transformations not applied.  Required payload type of '%s'.  "
            + "Actual payload type is '%s'.  You must define and register a Transformer to transform "
            + "between these types.")
    String transformationsNotApplied(String expectedPayload, String actualPayload);

    /**
     * noRegisteredService method definition.
     * @param serviceName serviceName
     * @return SwitchYardException
     */
    @Message(id = 14012, value = "No registered service found for %s")
    SwitchYardException noRegisteredService(String serviceName);

    /**
     * operationNotIncluded method definition.
     * @param operationName operationName
     * @param serviceName serviceName
     * @return HandlerException
     */
    @Message(id = 14013, value = "Operation %s is not included in interface for service: %s")
    HandlerException operationNotIncluded(String operationName, String serviceName);

    /**
     * requiredPolicesNeeded method definition.
     * @param requires requires
     * @return error message
     */
    @Message(id = 14014, value = "Required policies have not been provided: %s")
    String requiredPolicesNeeded(String requires);

    /**
     * multipleFallbackValidatorsAvailable method definition.
     * @return String
     */
    @Message(id = 14015, value = "Multiple possible fallback validators available:")
    String multipleFallbackValidatorsAvailable();

    /**
     * multipleFallbackRegistry method definition.
     * @return String
     */
    @Message(id = 14016, value = "Multiple possible fallback transformers available:")
    String multipleFallbackRegistry();

    /**
     * sendFaultNotAllowed method definition.
     * @return IllegalStateException
     */
    @Message(id = 14017, value = "Send fault not allowed on new exchanges")
    IllegalStateException sendFaultNotAllowed();

    /**
     * sendMessageNotAllowed method definition.
     * @param phase phase
     * @return IllegalStateException
     */
    @Message(id = 14018, value = "Send message not allowed for exchange in phase %s")
    IllegalStateException sendMessageNotAllowed(String phase);

    /**
     * invalidMessageArgument method definition.
     * @return IllegalArgumentException
     */
    @Message(id = 14019, value = "Invalid null 'message' argument in method call.") 
    IllegalArgumentException invalidMessageArgument();

    /**
     * exchangeInFaultState method definition.
     * @return IllegalStateException
     */
    @Message(id = 14020, value = "Exchange instance is in a FAULT state.")
    IllegalStateException exchangeInFaultState();

    /**
     * cannotChangeMetaDataAfterMessageSent method definition.
     * @return IllegalStateException
     */
    @Message(id = 14021, value = "Cannot change consumer metadata after message has been sent on exchange.")
    IllegalStateException cannotChangeMetaDataAfterMessageSent();

    /**
     * invalidConsumerContract method definition.
     * @return SwitchYardException
     */
    @Message(id = 14022, value = "Invalid consumer contract - IN_OUT exchanges require a reply handler.")
    SwitchYardException invalidConsumerContract();

    /**
     * cannotChangeMetadataAfterInvoke method definition.
     * @return IllegalStateException
     */
    @Message(id = 14023, value = "Cannot change provider metadata after provider has been invoked!")
    IllegalStateException cannotChangeMetadataAfterInvoke();

    /**
     * nullTypeArgument method definition.
     * @return IllegalArgumentException
     */
    @Message(id = 14024, value = "Null 'type' argument.")
    IllegalArgumentException nullTypeArgument();

    /**
     * noTransformRegistryAvailable method definition.
     * @param className className
     * @param typeName typeName
     * @return SwitchYardException
     */
    @Message(id = 14025, value = "Cannot convert from '%s' to '%s'.  No TransformRegistry available.")
    SwitchYardException noTransformRegistryAvailable(String className, String typeName);

    /**
     * noRegisteredTransformer method definition.
     * @param className className
     * @param typeName typeName
     * @param fromType fromType
     * @param toType toType
     * @return SwitchYardException
     */
    @Message(id = 14026, value = "Cannot convert from '%s' to '%s'.  No registered Transformer available "
            + "for transforming from '%s' to '%s'.  A Transformer must be registered.")
    SwitchYardException noRegisteredTransformer(String className, String typeName, String fromType, String toType);

    /**
     * transformerReturnedNull method definition.
     * @param contentClassName contentClassName
     * @param typeName typeName
     * @param transformerClassName transformerClassName
     * @return SwitchYardException
     */
    @Message(id = 14027, value = "Error converting from '%s' to '%s'.  Transformer '%s' returned null.")
    SwitchYardException transformerReturnedNull(String contentClassName, String typeName, String transformerClassName);

    /**
     * transformerReturnedIncompatibleType method definition.
     * @param contentClassName contentClassName
     * @param typeName typeName
     * @param transformerClassName transformerClassName
     * @param returnedTypeName returnedTypeName
     * @return SwitchYardException
     */
    @Message(id = 14028, value = "Error converting from '%s' to '%s'.  Transformer '%s' returned incompatible type '%s'.")
    SwitchYardException transformerReturnedIncompatibleType(String contentClassName, String typeName,
            String transformerClassName, String returnedTypeName);

    /**
     * propertyNameAndScopeCannotBeNull method definition.
     * @return IllegalArgumentException
     */
    @Message(id = 14029, value = "Property name and scope must not be null!")
    IllegalArgumentException propertyNameAndScopeCannotBeNull();

    /**
     * noOperationsInInterfaceForService method definition.
     * @param serviceName serviceName
     * @return SwitchYardException
     */
    @Message(id = 14030, value = "No operations in interface for service: %s")
    SwitchYardException noOperationsInInterfaceForService(String serviceName);

    /**
     * operationNameRequiredMultipleOps method definition.
     * @param serviceName serviceName
     * @return SwitchYardException
     */
    @Message(id = 14031, value = "Operation name required - "
            + "multiple operations on service interface: %s")
    SwitchYardException operationNameRequiredMultipleOps(String serviceName);

    /**
     * operationDoesNotExistForService method definition.
     * @param operationName operationName
     * @param serviceName serviceName
     * @return SwitchYardException
     */
    @Message(id = 14032, value = "Operation %s does not exist for service %s")
    SwitchYardException operationDoesNotExistForService(String operationName, String serviceName);

    /**
     * invalidPhaseForDispatch method definition.
     * @param phaseName phaseName
     * @return IllegalStateException
     */
    @Message(id = 14033, value = "Invalid phase for dispatch: %s")
    IllegalStateException invalidPhaseForDispatch(String phaseName);    

    /**
     * onlyDefaultMessageInstances method definition.
     * @return IllegalStateException
     */
    @Message(id = 14034, value = "This exchange may handle only DefaultMessage instances.")
    IllegalStateException onlyDefaultMessageInstances();    

    /**
     * messageOnlySentOnce method definition.
     * @return IllegalStateException
     */
    @Message(id = 14035, value = "Message may be sent only once. Use Message.copy() to re-send same payload.")
    IllegalStateException messageOnlySentOnce();

    /**
     * scopeDifferent method definition.
     * @param scope scope
     * @param source source
     * @return IllegalStateException
     */
    @Message(id = 14037, value = "Scope %s is different than expected %s")
    IllegalArgumentException scopeDifferent(String scope, String source);

    /**
     * propagatedTransactionHasInvalidStatus method definition.
     * @param txStatus Transaction status code
     * @return TransactionFailureException
     */
    @Message(id = 14038, value = "Transaction has invalid status '%s' - it must be '0' (STATUS_ACTIVE) on propagation")
    TransactionFailureException propagatedTransactionHasInvalidStatus(int txStatus);

    /**
     * transactionAlreadyRolledBack method definition.
     * @return TransactionFailureException
     */
    @Message(id = 14039, value = "Transaction was already rolled back somehow. It may be caused by the transaction timeout, or application did it before SwitchYard handled it.")
    TransactionFailureException transactionAlreadyRolledBack();
    
    /**
     * validatorFailed method definition.
     * @param clazz validator class name
     * @param type Java type
     * @param name QName
     * @param cause cause
     * @return failure message
     */
    @Message(id = 14040, value = "Validator:[class=%s', type='%s', name='%s'] failed")
    String validatorFailed(String clazz, String type, String name, @Cause SwitchYardException cause);

}
