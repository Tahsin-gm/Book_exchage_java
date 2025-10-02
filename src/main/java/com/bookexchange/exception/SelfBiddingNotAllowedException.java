package com.bookexchange.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SelfBiddingNotAllowedException extends RuntimeException {
    public SelfBiddingNotAllowedException(String message) {
        super(message);
    }
}

