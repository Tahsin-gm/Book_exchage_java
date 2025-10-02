package com.bookexchange.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DuplicateWishlistItemException extends RuntimeException {
    public DuplicateWishlistItemException(String message) {
        super(message);
    }
}