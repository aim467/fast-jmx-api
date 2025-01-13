package com.fast.jmx.exception;

public class FastJmxException extends RuntimeException {

    private String message;

    private Integer code;

    public FastJmxException(String message) {
        this.message = message;
    }

    public FastJmxException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
