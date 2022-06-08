module com.github.apetrelli.scafa.sync.http.server {
	requires java.logging;
	requires transitive com.github.apetrelli.scafa.sync.http;
	exports com.github.apetrelli.scafa.sync.http.server;
	exports com.github.apetrelli.scafa.sync.http.server.impl;
	exports com.github.apetrelli.scafa.sync.http.server.statics;
}