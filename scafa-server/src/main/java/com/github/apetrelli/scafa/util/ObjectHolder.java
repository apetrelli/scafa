package com.github.apetrelli.scafa.util;

public class ObjectHolder<T> {
    private T obj;
    
    public void setObj(T obj) {
        this.obj = obj;
    }
    
    public T getObj() {
        return obj;
    }
}