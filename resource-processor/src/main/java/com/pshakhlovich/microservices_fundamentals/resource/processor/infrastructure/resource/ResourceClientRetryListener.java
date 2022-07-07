package com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResourceClientRetryListener extends RetryListenerSupport {
    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        log.error("Resource client failed with exception: {}", throwable.getMessage());
        log.error("Retry Count {}", context.getRetryCount());
        super.onError(context, callback, throwable);
    }
}
