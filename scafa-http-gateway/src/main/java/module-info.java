module com.github.apetrelli.scafa.http.gateway {
	requires java.logging;
	requires transitive com.github.apetrelli.scafa.http;
	requires transitive com.github.apetrelli.scafa.async.http;
	requires transitive com.github.apetrelli.scafa.sync.http;
	requires com.github.apetrelli.scafa.async.proto;
	requires com.github.apetrelli.scafa.sync.proto;
	exports com.github.apetrelli.scafa.async.http.gateway;
	exports com.github.apetrelli.scafa.async.http.gateway.direct;
	exports com.github.apetrelli.scafa.async.http.gateway.connection;
	exports com.github.apetrelli.scafa.async.http.gateway.handler;
	exports com.github.apetrelli.scafa.sync.http.gateway;
	exports com.github.apetrelli.scafa.sync.http.gateway.connection;
	exports com.github.apetrelli.scafa.sync.http.gateway.direct;
	exports com.github.apetrelli.scafa.sync.http.gateway.handler;
}