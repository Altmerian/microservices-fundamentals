package com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.exception;

public class InfrastructureException extends RuntimeException {

    public InfrastructureException() {
        super();
    }

    public InfrastructureException(String message) {
        super(message);
    }
}
