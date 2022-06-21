module com.github.apetrelli.scafa.sync.proxy {
    requires java.logging;
    requires lombok;
    requires transitive com.github.apetrelli.scafa.sync.http.proxy;
    requires transitive com.github.apetrelli.scafa.proxy;
    exports com.github.apetrelli.scafa.sync.proxy;
}