package com.campus.nav.exception;

/**
 * 数据验证异常
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}