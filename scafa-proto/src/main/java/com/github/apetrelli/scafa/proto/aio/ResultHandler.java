package com.github.apetrelli.scafa.proto.aio;

public interface ResultHandler<T> {

    void handle(T result);
}
