package com.examen.orderservice.exception;

public class ProductUnAvailableException extends RuntimeException{

    int code=101;

    public ProductUnAvailableException(String message) {
        super(message);
    }
}
