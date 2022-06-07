module com.github.apetrelli.scafa.http.gateway {
	requires java.logging;
	requires transitive com.github.apetrelli.scafa.http;
	requires transitive com.github.apetrelli.scafa.async.http;
	requires transitive com.github.apetrelli.scafa.sync.http;
	requires com.github.apetrelli.scafa.async.proto;
	requires com.github.apetrelli.scafa.sync.proto;
	exports com.github.apetrelli.scafa.http.gateway;
	exports com.github.apetrelli.scafa.http.gateway.direct;
	exports com.github.apetrelli.scafa.http.gateway.impl;
	exports com.github.apetrelli.scafa.http.gateway.sync;
	exports com.github.apetrelli.scafa.http.gateway.sync.connection;
	exports com.github.apetrelli.scafa.http.gateway.sync.direct;
	exports com.github.apetrelli.scafa.http.gateway.sync.handler;
}