module com.github.apetrelli.scafa.http.server {
	requires java.logging;
	requires transitive com.github.apetrelli.scafa.http;
	requires transitive com.github.apetrelli.scafa.async.http;
	requires transitive com.github.apetrelli.scafa.sync.http;
	requires com.github.apetrelli.scafa.async.proto;
	requires transitive com.github.apetrelli.scafa.async.file;
	exports com.github.apetrelli.scafa.async.http.server;
	exports com.github.apetrelli.scafa.async.http.server.impl;
	exports com.github.apetrelli.scafa.async.http.server.statics;
	exports com.github.apetrelli.scafa.sync.http.server;
	exports com.github.apetrelli.scafa.sync.http.server.impl;
	exports com.github.apetrelli.scafa.sync.http.server.statics;
}