module com.github.apetrelli.scafa.sync.http {
	requires transitive com.github.apetrelli.scafa.sync.proto;
	requires transitive com.github.apetrelli.scafa.http;
	requires java.logging;
	requires lombok;
	exports com.github.apetrelli.scafa.sync.http;
	exports com.github.apetrelli.scafa.sync.http.composite;
	exports com.github.apetrelli.scafa.sync.http.socket.direct;
	exports com.github.apetrelli.scafa.sync.http.impl;
	exports com.github.apetrelli.scafa.sync.http.output;
	exports com.github.apetrelli.scafa.sync.http.output.impl;
	exports com.github.apetrelli.scafa.sync.http.socket.server;
}