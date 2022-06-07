module com.github.apetrelli.scafa.sync.http {
	requires transitive com.github.apetrelli.scafa.sync.proto;
	requires transitive com.github.apetrelli.scafa.http;
	requires java.logging;
	exports com.github.apetrelli.scafa.sync.http;
	exports com.github.apetrelli.scafa.sync.http.composite;
	exports com.github.apetrelli.scafa.sync.http.direct;
	exports com.github.apetrelli.scafa.sync.http.impl;
	exports com.github.apetrelli.scafa.sync.http.output;
	exports com.github.apetrelli.scafa.sync.http.output.impl;
	exports com.github.apetrelli.scafa.sync.http.server;
}