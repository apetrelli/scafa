module com.github.apetrelli.scafa.sync.http.gateway {
	requires java.logging;
	requires lombok;
	requires transitive com.github.apetrelli.scafa.sync.http;
	exports com.github.apetrelli.scafa.sync.http.gateway;
	exports com.github.apetrelli.scafa.sync.http.gateway.connection;
	exports com.github.apetrelli.scafa.sync.http.gateway.direct;
	exports com.github.apetrelli.scafa.sync.http.gateway.handler;
}