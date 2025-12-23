package com.campus.nav.exception;

/**
 * 认证授权异常
 */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
    
    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}