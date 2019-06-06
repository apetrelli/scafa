package com.github.apetrelli.scafa.http.proxy;

public interface ResultHandler<T> {

    void handle(T result);
}
