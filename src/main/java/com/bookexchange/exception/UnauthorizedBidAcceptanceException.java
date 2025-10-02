package com.bookexchange.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedBidAcceptanceException extends RuntimeException {
    public UnauthorizedBidAcceptanceException(String message) {
        super(message);
    }
}

