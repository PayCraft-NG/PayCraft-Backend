package com.aalto.paycraft.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class EmployerAlreadyExists extends RuntimeException {
    public EmployerAlreadyExists(String message){
        super(message);
    }
}