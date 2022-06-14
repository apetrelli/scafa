module com.github.apetrelli.scafa.proxy {
    requires java.logging;
    requires lombok;
    requires transitive com.github.apetrelli.scafa.http.proxy;
    requires org.apache.commons.io;
    requires transitive ini4j;
    exports com.github.apetrelli.scafa.proxy;
    exports com.github.apetrelli.scafa.proxy.config;
    exports com.github.apetrelli.scafa.proxy.config.ini;
}