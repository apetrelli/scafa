module com.github.apetrelli.scafa.async.http.server {
	requires java.logging;
	requires lombok;
	requires transitive com.github.apetrelli.scafa.async.http;
	requires transitive com.github.apetrelli.scafa.async.file;
	exports com.github.apetrelli.scafa.async.http.server;
	exports com.github.apetrelli.scafa.async.http.server.impl;
	exports com.github.apetrelli.scafa.async.http.server.statics;
}