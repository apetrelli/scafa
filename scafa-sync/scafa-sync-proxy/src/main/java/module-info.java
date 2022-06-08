module com.github.apetrelli.scafa.sync.proxy {
    requires java.logging;
    requires transitive com.github.apetrelli.scafa.sync.http.proxy;
    requires transitive com.github.apetrelli.scafa.proxy;
    requires com.github.apetrelli.scafa.sync.proto.jnet;
    requires com.github.apetrelli.scafa.sync.proto.thread;
    requires commons.io;
    requires transitive ini4j;
    exports com.github.apetrelli.scafa.sync.proxy;
}