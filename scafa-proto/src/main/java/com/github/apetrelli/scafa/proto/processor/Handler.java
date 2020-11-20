package com.github.apetrelli.scafa.proto.processor;

public interface Handler {

    void onConnect();

    void onDisconnect();
    
    void onError(Throwable exc); // NOSONAR

}
