module com.github.apetrelli.scafa.server {
    requires java.logging;
    requires transitive com.github.apetrelli.scafa.async.http.proxy;
    requires com.github.apetrelli.scafa.async.proto.aio;
    requires com.github.apetrelli.scafa.sync.proto.jnet;
    requires com.github.apetrelli.scafa.sync.proto.thread;
    requires commons.io;
    requires transitive ini4j;
	requires com.github.apetrelli.scafa.proto;
    exports com.github.apetrelli.scafa.server;
}