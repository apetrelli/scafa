module com.github.apetrelli.scafa.web {
	requires transitive com.github.apetrelli.scafa.http.server;
	requires transitive com.github.apetrelli.scafa.http.gateway;
	requires java.logging;
    requires transitive ini4j;
	exports com.github.apetrelli.scafa.web.handler;
}