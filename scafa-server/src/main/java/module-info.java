module com.github.apetrelli.scafa.server {
    requires java.logging;
    requires com.github.apetrelli.scafa.http.proxy;
    requires commons.io;
    requires transitive ini4j;
	requires com.github.apetrelli.scafa.proto;
    exports com.github.apetrelli.scafa.server;
}