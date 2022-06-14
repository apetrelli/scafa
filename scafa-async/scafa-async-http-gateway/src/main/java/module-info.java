module com.github.apetrelli.scafa.async.http.gateway {
	requires java.logging;
	requires lombok;
	requires transitive com.github.apetrelli.scafa.async.http;
	exports com.github.apetrelli.scafa.async.http.gateway;
	exports com.github.apetrelli.scafa.async.http.gateway.direct;
	exports com.github.apetrelli.scafa.async.http.gateway.connection;
	exports com.github.apetrelli.scafa.async.http.gateway.handler;
}