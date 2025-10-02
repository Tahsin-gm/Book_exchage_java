package com.bookexchange.exception;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomErrorMessage {
    private String message;
    private String apiPath;
    private LocalDateTime localDateTime;
    private HttpStatus httpStatus;


}
