module com.github.apetrelli.scafa.async.proxy {
    requires java.logging;
    requires lombok;
    requires transitive com.github.apetrelli.scafa.async.http.proxy;
    requires transitive com.github.apetrelli.scafa.proxy;
    requires com.github.apetrelli.scafa.async.proto;
    exports com.github.apetrelli.scafa.async.proxy;
}