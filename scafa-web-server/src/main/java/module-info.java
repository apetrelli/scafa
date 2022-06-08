module com.github.apetrelli.scafa.web {
	requires transitive com.github.apetrelli.scafa.async.http.gateway;
	requires transitive com.github.apetrelli.scafa.async.http.server;
	requires transitive com.github.apetrelli.scafa.sync.http.gateway;
	requires transitive com.github.apetrelli.scafa.sync.http.server;
	requires com.github.apetrelli.scafa.async.file.aio;
	requires com.github.apetrelli.scafa.async.proto.aio;
	requires com.github.apetrelli.scafa.sync.proto.jnet;
	requires com.github.apetrelli.scafa.sync.proto.thread;
	requires java.logging;
    requires transitive ini4j;
	exports com.github.apetrelli.scafa.web.handler;
}