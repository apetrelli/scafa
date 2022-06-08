module com.github.apetrelli.scafa.async.proxy {
    requires java.logging;
    requires transitive com.github.apetrelli.scafa.async.http.proxy;
    requires transitive com.github.apetrelli.scafa.proxy;
    requires com.github.apetrelli.scafa.async.proto.aio;
    requires commons.io;
    requires transitive ini4j;
    exports com.github.apetrelli.scafa.async.proxy;
}