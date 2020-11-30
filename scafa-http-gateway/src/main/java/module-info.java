module com.github.apetrelli.scafa.http.gateway {
	requires java.logging;
	requires transitive com.github.apetrelli.scafa.http;
	exports com.github.apetrelli.scafa.http.gateway;
	exports com.github.apetrelli.scafa.http.gateway.direct;
	exports com.github.apetrelli.scafa.http.gateway.impl;
	exports com.github.apetrelli.scafa.http.gateway.sync;
	exports com.github.apetrelli.scafa.http.gateway.sync.connection;
}