package org.switchyard.component.camel.deploy.support;

public class UndeclaredFaultException extends Exception {
    private static final long serialVersionUID = 1L;

    public UndeclaredFaultException(String message) {
        super(message);
    }
}