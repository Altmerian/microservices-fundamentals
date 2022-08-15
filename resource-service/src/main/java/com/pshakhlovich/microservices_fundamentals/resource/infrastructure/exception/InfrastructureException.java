package com.pshakhlovich.microservices_fundamentals.resource.infrastructure.exception;

public class InfrastructureException extends RuntimeException {

    public InfrastructureException() {
        super();
    }

    public InfrastructureException(String message) {
        super(message);
    }
}
