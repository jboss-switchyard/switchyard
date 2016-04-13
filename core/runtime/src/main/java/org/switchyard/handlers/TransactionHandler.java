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

package org.switchyard.handlers;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.switchyard.Exchange;
import org.switchyard.ExchangeHandler;
import org.switchyard.Property;
import org.switchyard.Scope;
import org.switchyard.TransactionFailureException;
import org.switchyard.label.BehaviorLabel;
import org.switchyard.policy.PolicyUtil;
import org.switchyard.policy.TransactionPolicy;
import org.switchyard.runtime.RuntimeLogger;
import org.switchyard.runtime.RuntimeMessages;
import org.switchyard.runtime.util.TransactionManagerLocator;


/**
 * Interprets transactional policy specified on an exchange and handles 
 * transactional requirements.
 */
public class TransactionHandler implements ExchangeHandler {
    
    private static final String SUSPENDED_TRANSACTION_PROPERTY = 
            "org.switchyard.exchange.transaction.suspended";
    private static final String INITIATED_TRANSACTION_PROPERTY = 
            "org.switchyard.exchange.transaction.initiated";
    private static final String BEFORE_INVOKED_PROPERTY =
            "org.switchyard.exchange.transaction.beforeInvoked";
    
    private static Logger _log = Logger.getLogger(TransactionHandler.class);
    
    private TransactionManager _transactionManager;

    /**
     * Create a new TransactionHandler.
     */
    public TransactionHandler() {
        _transactionManager = TransactionManagerLocator.locateTransactionManager();
        if (_transactionManager == null) {
            _log.debug("Unable to find TransactionManager - Transaction Policy handling will not be available.");
        }
    }
    
    @Override
    public void handleMessage(Exchange exchange) throws TransactionFailureException {
        // if no TM is available, there's nothing to do
        if (_transactionManager == null) {
            return;
        }
        
        Property prop = exchange.getContext().getProperty(BEFORE_INVOKED_PROPERTY, Scope.EXCHANGE);
        if (prop != null && Boolean.class.cast(prop.getValue())) {
            // OUT phase in IN_OUT exchange or 2nd invocation in IN_ONLY exchange
            handleAfter(exchange);
        } else {
            exchange.getContext().setProperty(BEFORE_INVOKED_PROPERTY, Boolean.TRUE, Scope.EXCHANGE).addLabels(BehaviorLabel.TRANSIENT.label());
            handleBefore(exchange);
        }
    }
    
    @Override
    public void handleFault(Exchange exchange) {
        // if no TM is available, there's nothing to do
        if (_transactionManager == null) {
            return;
        }
        
        try {
            Property rollbackOnFaultProperty = exchange.getContext().getProperty(Exchange.ROLLBACK_ON_FAULT);
            if (rollbackOnFaultProperty != null && rollbackOnFaultProperty.getValue() != null
                    && Boolean.class.cast(rollbackOnFaultProperty.getValue())) {
                    Transaction transaction = getCurrentTransaction();
                    if (transaction != null) {
                        transaction.setRollbackOnly();
                    }
            }
            handleAfter(exchange);
        } catch (Exception e) {
            _log.error(e);
        }
    }
    
    void setTransactionManager(TransactionManager transactionManager) {
        _transactionManager = transactionManager;
    }
    
    TransactionManager getTransactionManager() {
        return _transactionManager;
    }
    
    private void handleAfter(Exchange exchange) throws TransactionFailureException {
        Transaction transaction = null;
        try {
            // complete the transaction which is initiated by this handler
            transaction = (Transaction) exchange.getContext().getPropertyValue(INITIATED_TRANSACTION_PROPERTY);
            if (transaction != null) {
                endTransaction();
            }
        } catch (Exception e) {
            throw RuntimeMessages.MESSAGES.failedToCompleteTransaction(e);
        } finally {
            // resume the transaction which is suspended by this handler
            transaction = (Transaction) exchange.getContext().getPropertyValue(SUSPENDED_TRANSACTION_PROPERTY);
            if (transaction != null) {
                resumeTransaction(transaction);
            }
        }
    }
    
    private void handleBefore(Exchange exchange) throws TransactionFailureException {
        if (!(propagatesRequired(exchange) || suspendsRequired(exchange) || managedGlobalRequired(exchange)
                || managedLocalRequired(exchange) || noManagedRequired(exchange))) {
            return;
        }
        
        evaluatePolicyCombination(exchange);
        
        int txStatus = getCurrentTransactionStatus();
        
        // Check if propagated transaction is in valid state
        if (propagatesRequired(exchange)) {
            if (txStatus == Status.STATUS_NO_TRANSACTION) {
                // Start new transaction on managedGlobal later even if propagatesRequired
                if (!managedGlobalRequired(exchange)) {
                    throw RuntimeMessages.MESSAGES.noTransactionPropagated(TransactionPolicy.PROPAGATES_TRANSACTION.toString());
                }
            } else if (txStatus != Status.STATUS_ACTIVE) {
                throw RuntimeMessages.MESSAGES.propagatedTransactionHasInvalidStatus(txStatus);
            }
        } else if (managedGlobalRequired(exchange) && !suspendsRequired(exchange)) {
            // SwitchYard managed transaction must be in Status.STATUS_ACTIVE
            if (txStatus != Status.STATUS_NO_TRANSACTION && txStatus != Status.STATUS_ACTIVE) {
                throw RuntimeMessages.MESSAGES.propagatedTransactionHasInvalidStatus(txStatus);
            }
        }
        
        // Suspend existing transaction if required. We don't care about the transaction status on suspend
        if ((managedLocalRequired(exchange) || noManagedRequired(exchange) || suspendsRequired(exchange))
                && txStatus != Status.STATUS_NO_TRANSACTION) {
            suspendTransaction(exchange);
            txStatus = getCurrentTransactionStatus();
        }
        
        // Start new transaction if required
        if (managedLocalRequired(exchange)) {
            startTransaction(exchange);
            txStatus = getCurrentTransactionStatus();
        } else if (managedGlobalRequired(exchange) && txStatus == Status.STATUS_NO_TRANSACTION) {
            startTransaction(exchange);
            txStatus = getCurrentTransactionStatus();
        }
        
        provideRequiredPolicies(exchange);
    }

    private void evaluatePolicyCombination(Exchange exchange) throws TransactionFailureException {
        // check for incompatible policy definition 
        if (suspendsRequired(exchange) && propagatesRequired(exchange)) {
            throw RuntimeMessages.MESSAGES.invalidTransactionPolicy(TransactionPolicy.SUSPENDS_TRANSACTION.toString(), 
                    TransactionPolicy.PROPAGATES_TRANSACTION.toString());
        }
        if (managedGlobalRequired(exchange) && managedLocalRequired(exchange)
                || managedGlobalRequired(exchange) && noManagedRequired(exchange)
                || managedLocalRequired(exchange) && noManagedRequired(exchange)) {
            throw RuntimeMessages.MESSAGES.invalidTransactionPolicy(TransactionPolicy.MANAGED_TRANSACTION_GLOBAL.toString(), 
                    TransactionPolicy.NO_MANAGED_TRANSACTION.toString());
        }
        if (propagatesRequired(exchange) && managedLocalRequired(exchange)
                || propagatesRequired(exchange) && noManagedRequired(exchange)) {
            throw RuntimeMessages.MESSAGES.invalidTransactionPolicyCombo(TransactionPolicy.PROPAGATES_TRANSACTION.toString(), 
                    TransactionPolicy.MANAGED_TRANSACTION_LOCAL.toString(), TransactionPolicy.NO_MANAGED_TRANSACTION.toString());
        }
    }
    
    private void provideRequiredPolicies(Exchange exchange) {
        if (suspendsRequired(exchange)) {
            provideSuspends(exchange);
        } else if (propagatesRequired(exchange)) {
            providePropagates(exchange);
        }
        
        if (managedGlobalRequired(exchange)) {
            provideManagedGlobal(exchange);
        } else if (managedLocalRequired(exchange)) {
            provideManagedLocal(exchange);
        } else if (noManagedRequired(exchange)) {
            provideNoManaged(exchange);
        }
    }
    
    private boolean managedGlobalRequired(Exchange exchange) {
        return PolicyUtil.isRequired(exchange, TransactionPolicy.MANAGED_TRANSACTION_GLOBAL);
    }

    private boolean managedLocalRequired(Exchange exchange) {
        return PolicyUtil.isRequired(exchange, TransactionPolicy.MANAGED_TRANSACTION_LOCAL);
    }
    
    private boolean noManagedRequired(Exchange exchange) {
        return PolicyUtil.isRequired(exchange, TransactionPolicy.NO_MANAGED_TRANSACTION);
    }
    
    private boolean suspendsRequired(Exchange exchange) {
        return PolicyUtil.isRequired(exchange, TransactionPolicy.SUSPENDS_TRANSACTION);
    }
    
    private boolean propagatesRequired(Exchange exchange) {
        return PolicyUtil.isRequired(exchange, TransactionPolicy.PROPAGATES_TRANSACTION);
    }
    
    private void providePropagates(Exchange exchange) {
        PolicyUtil.provide(exchange, TransactionPolicy.PROPAGATES_TRANSACTION);
    }
    
    private void provideSuspends(Exchange exchange) {
        PolicyUtil.provide(exchange, TransactionPolicy.SUSPENDS_TRANSACTION);
    }

    private void provideManagedGlobal(Exchange exchange) {
        PolicyUtil.provide(exchange, TransactionPolicy.MANAGED_TRANSACTION_GLOBAL);
    }
    
    private void provideManagedLocal(Exchange exchange) {
        PolicyUtil.provide(exchange, TransactionPolicy.MANAGED_TRANSACTION_LOCAL);
    }
    
    private void provideNoManaged(Exchange exchange) {
        PolicyUtil.provide(exchange, TransactionPolicy.NO_MANAGED_TRANSACTION);
    }

    private void startTransaction(Exchange exchange) throws TransactionFailureException {
        if (_log.isDebugEnabled()) {
            printDebugInfo("Creating new transaction");
        }

        int txStatus = getCurrentTransactionStatus();

        if (txStatus == Status.STATUS_NO_TRANSACTION) {
            Transaction transaction = null;
            try {
                _transactionManager.begin();
                transaction = _transactionManager.getTransaction();
            } catch (Exception e) {
                throw RuntimeMessages.MESSAGES.failedCreateNewTransaction(e);
            }

            if (transaction != null) {
                if (_log.isDebugEnabled()) {
                    printDebugInfo("Created new transaction");
                }
                exchange.getContext().setProperty(INITIATED_TRANSACTION_PROPERTY, transaction, Scope.EXCHANGE).addLabels(BehaviorLabel.TRANSIENT.label());
            }
        } else {
            throw RuntimeMessages.MESSAGES.transactionAlreadyExists();
        }
    }
    
    private void endTransaction() throws TransactionFailureException {
        if (_log.isDebugEnabled()) {
            printDebugInfo("Completing transaction");
        }
        
        int txStatus = getCurrentTransactionStatus();

        if (txStatus == Status.STATUS_MARKED_ROLLBACK) {
            try {
                _transactionManager.rollback();
                if (_log.isDebugEnabled()) {
                    printDebugInfo("Transaction rolled back as it has been marked as RollbackOnly");
                }
            } catch (Exception e) {
                throw RuntimeMessages.MESSAGES.failedToRollbackTransaction(e);
            }
        } else if (txStatus == Status.STATUS_ACTIVE) {
            try {
                _transactionManager.commit();
                if (_log.isDebugEnabled()) {
                    printDebugInfo("Transaction has been committed");
                }
            } catch (Exception e) {
                throw RuntimeMessages.MESSAGES.failedToCommitTransaction(e);
            }
        } else if (txStatus == Status.STATUS_ROLLEDBACK) {
            // WFLY-1346 : if transaction timeout occurs, we need to disassociate
            // the transaction from the thread manually for Narayana Tx manager.
            // WFLY-4327 : In addition to above, tm.rollback() is required to clean up
            // Narayana internal static stuff rather than only disassociating the tx from
            // the thread by tm.suspend().
            try {
                //_transactionManager.suspend();
                _transactionManager.rollback();
            } catch (SystemException e) {
                RuntimeLogger.ROOT_LOGGER.failedToRollbackOnStatusRolledback(e);
            }
            throw RuntimeMessages.MESSAGES.transactionAlreadyRolledBack();
        } else if (txStatus == Status.STATUS_UNKNOWN) {
            // According to the WFLY-1346 fix, there is a case that has UNKNOWN status,
            // and the transaction should be rolled back in this case.
            try {
                _transactionManager.rollback();
                if (_log.isDebugEnabled()) {
                    printDebugInfo("Transaction has been rolled back due to its UNKNOWN status");
                }
            } catch (Exception e) {
                throw RuntimeMessages.MESSAGES.failedToRollbackTransaction(e);
            }
        } else {
            // WFLY-1346 : In any status other than we handled above, it needs to disassociate
            // the transaction from the thread by tm.suspend() manually for Narayana Tx manager.
            try {
                _transactionManager.suspend();
            } catch (SystemException e) {
                RuntimeLogger.ROOT_LOGGER.failedToSuspendTransactionOnExchange(e);
            }
            throw RuntimeMessages.MESSAGES.failedToCompleteWithStatus(txStatus);
        }
    }

    private void suspendTransaction(Exchange exchange) {
        if (_log.isDebugEnabled()) {
            printDebugInfo("Suspending active transaction");
        }

        Transaction transaction = null;
        try {
            transaction = _transactionManager.suspend();
        } catch (SystemException sysEx) {
            RuntimeLogger.ROOT_LOGGER.failedToSuspendTransactionOnExchange(sysEx);
        }
        if (transaction != null) {
            if (_log.isDebugEnabled()) {
                printDebugInfo("Suspended active transaction");
            }
            exchange.getContext().setProperty(SUSPENDED_TRANSACTION_PROPERTY, transaction, Scope.EXCHANGE).addLabels(BehaviorLabel.TRANSIENT.label());
        }
    }
    
    private void resumeTransaction(Transaction transaction) {
        try {
            if (_log.isDebugEnabled()) {
                printDebugInfo("Resuming suspended transaction");
            }

            _transactionManager.resume(transaction);

            if (_log.isDebugEnabled()) {
                printDebugInfo("Resumed suspended transaction");
            }
        } catch (Exception ex) {
            RuntimeLogger.ROOT_LOGGER.failedToResumeTransaction(ex);
        }
    }

    private Transaction getCurrentTransaction() throws TransactionFailureException {
        try {
            return _transactionManager.getTransaction();
        } catch (Exception e) {
            throw RuntimeMessages.MESSAGES.failedToRetrieveStatus(e);
        }
    }
    
    private int getCurrentTransactionStatus() throws TransactionFailureException {
        try {
            return _transactionManager.getStatus();
        } catch (Exception e) {
            throw RuntimeMessages.MESSAGES.failedToRetrieveStatus(e);
        }
    }

    private void printDebugInfo(String message) {
        StringBuilder buf = new StringBuilder(message);
        buf.append(" - [Thread: ");
        buf.append(Thread.currentThread().toString());
        buf.append(", Transaction: ");
        Transaction currentTx = null;
        try {
            currentTx = getCurrentTransaction();
        } catch (Exception e) {
            e.getMessage();
        } // ignore

        if (currentTx == null) {
            buf.append("N/A");
        } else {
            buf.append(currentTx.toString());
        }
        buf.append(']');
        _log.debug(buf.toString());
    }
}
