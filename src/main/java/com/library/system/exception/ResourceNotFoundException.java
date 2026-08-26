package com.library.system.exception;

import org.springframework.context.annotation.Configuration;


public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
