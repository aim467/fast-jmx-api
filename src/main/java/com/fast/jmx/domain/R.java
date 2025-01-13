package com.fast.jmx.domain;

import lombok.Data;

@Data
public class R<T> {

    private T data;

    private Integer code;

    private String message;


    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setData(data);
        r.setCode(200);
        return r;
    }

    public static <T> R<T> ok(String message) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMessage(message);
        return r;
    }


    public static <T> R<T> ok(String message, T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setData(data);
        r.setMessage(message);
        return r;
    }


    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> error(String message) {
        R<T> r = new R<>();
        r.setMessage(message);
        r.setCode(500);
        return r;
    }

    public static <T> R<T> error(Integer code, String message) {
        R<T> r = new R<>();
        r.setMessage(message);
        r.setCode(code);
        return r;
    }

}
