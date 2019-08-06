package com.github.apetrelli.scafa.proto.processor;

import java.io.IOException;

public interface Handler {

    void onConnect() throws IOException;

    void onDisconnect() throws IOException;

}
