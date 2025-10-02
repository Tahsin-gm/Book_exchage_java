package com.bookexchange.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<?> handleBookNotFoundException(BookNotFoundException ex, WebRequest request) {
        CustomErrorMessage error = new CustomErrorMessage(
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND

        );
        return new ResponseEntity<>(error,HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(DuplicateWishlistItemException.class)
    public ResponseEntity<?> handleDuplicateWishlistException(
            DuplicateWishlistItemException ex, WebRequest request) {
        CustomErrorMessage error = new CustomErrorMessage(
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedBidAcceptanceException.class)
    public ResponseEntity<?> handleUnauthorizedBidAcceptanceException(UnauthorizedBidAcceptanceException ex, WebRequest request) {
        CustomErrorMessage error = new CustomErrorMessage(
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(SelfBiddingNotAllowedException.class)
    public ResponseEntity<?> handleSelfBiddingNotAllowedException(SelfBiddingNotAllowedException ex, WebRequest request) {
        CustomErrorMessage error = new CustomErrorMessage(
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<?> handleUnauthorizedException(UnauthorizedActionException ex, WebRequest request) {
        CustomErrorMessage error = new CustomErrorMessage(
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

}