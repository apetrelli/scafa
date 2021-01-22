module com.github.apetrelli.scafa.http.server {
	requires java.logging;
	requires transitive com.github.apetrelli.scafa.http;
	requires com.github.apetrelli.scafa.async.proto.aio;
	exports com.github.apetrelli.scafa.http.server;
	exports com.github.apetrelli.scafa.http.server.impl;
	exports com.github.apetrelli.scafa.http.server.statics;
	exports com.github.apetrelli.scafa.http.server.sync;
	exports com.github.apetrelli.scafa.http.server.sync.impl;
	exports com.github.apetrelli.scafa.http.server.sync.statics;
}