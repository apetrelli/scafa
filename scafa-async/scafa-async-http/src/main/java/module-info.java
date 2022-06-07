module com.github.apetrelli.scafa.async.http {
	requires transitive com.github.apetrelli.scafa.http;
	requires java.logging;
	exports com.github.apetrelli.scafa.async.http;
	exports com.github.apetrelli.scafa.async.http.composite;
	exports com.github.apetrelli.scafa.async.http.direct;
	exports com.github.apetrelli.scafa.async.http.impl;
	exports com.github.apetrelli.scafa.async.http.output;
	exports com.github.apetrelli.scafa.async.http.output.impl;
	exports com.github.apetrelli.scafa.async.http.server;
}